package com.gearrent.service;

import com.gearrent.dao.*;
import com.gearrent.entity.*;
import com.gearrent.entity.Equipment.Status;
import com.gearrent.entity.Rental.PaymentStatus;
import com.gearrent.entity.Rental.RentalStatus;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RentalService {

    private final RentalDAO          rentalDAO  = new RentalDAO();
    private final ReservationDAO     reservDAO  = new ReservationDAO();
    private final EquipmentDAO       equipDAO   = new EquipmentDAO();
    private final EquipmentCategoryDAO catDAO   = new EquipmentCategoryDAO();
    private final CustomerDAO        custDAO    = new CustomerDAO();
    private final MembershipConfigDAO memDAO    = new MembershipConfigDAO();
    private final RentalCalculatorService calc  = new RentalCalculatorService();
    private final PricingRuleDAO     ruleDAO    = new PricingRuleDAO();

    // ── Create rental (direct) ───────────────────────────────
    public Rental createRental(String equipmentId, int customerId, int branchId,
                                LocalDate start, LocalDate end,
                                PaymentStatus paymentStatus) throws Exception {
        validateDates(equipmentId, start, end, -1);

        Equipment eq       = equipDAO.findById(equipmentId);
        EquipmentCategory cat = catDAO.findById(eq.getCategoryId());
        Customer cust      = custDAO.findById(customerId);
        PricingRule rule   = ruleDAO.findFirst();

        checkDepositLimit(customerId, eq.getSecurityDeposit(), cust.getMembershipLevel().name());

        double[] bd = calc.buildRentalCostBreakdown(
            eq.getDailyBasePrice(), cat, start, end,
            eq.getSecurityDeposit(), cust.getMembershipLevel().name(), rule);

        Rental rn = buildRental(equipmentId, customerId, branchId, start, end,
                                 bd, eq.getSecurityDeposit(), paymentStatus, null);

        DBConnection.beginTransaction();
        try {
            rentalDAO.insert(rn);
            equipDAO.updateStatus(equipmentId, Status.Rented);
            DBConnection.commit();
        } catch (Exception e) { DBConnection.rollback(); throw e; }
        return rn;
    }

    // ── Convert reservation → rental ─────────────────────────
    public Rental convertReservation(int reservationId, PaymentStatus paymentStatus) throws Exception {
        Reservation rv = reservDAO.findById(reservationId);
        if (rv == null) throw new Exception("Reservation not found.");
        if (rv.getStatus() != Reservation.Status.Active) throw new Exception("Reservation is not active.");

        Equipment eq = equipDAO.findById(rv.getEquipmentId());
        if (eq.getStatus() != Status.Available && eq.getStatus() != Status.Reserved)
            throw new Exception("Equipment is no longer available.");
        if (rentalDAO.hasActiveRentalOverlap(rv.getEquipmentId(), rv.getStartDate(), rv.getEndDate(), -1))
            throw new Exception("Equipment has an overlapping active rental.");

        EquipmentCategory cat = catDAO.findById(eq.getCategoryId());
        Customer cust = custDAO.findById(rv.getCustomerId());
        PricingRule rule = ruleDAO.findFirst();

        double[] bd = calc.buildRentalCostBreakdown(
            eq.getDailyBasePrice(), cat, rv.getStartDate(), rv.getEndDate(),
            eq.getSecurityDeposit(), cust.getMembershipLevel().name(), rule);

        Rental rn = buildRental(rv.getEquipmentId(), rv.getCustomerId(), rv.getBranchId(),
                                 rv.getStartDate(), rv.getEndDate(), bd,
                                 eq.getSecurityDeposit(), paymentStatus, reservationId);

        DBConnection.beginTransaction();
        try {
            rentalDAO.insert(rn);
            reservDAO.updateStatus(reservationId, Reservation.Status.Converted);
            equipDAO.updateStatus(rv.getEquipmentId(), Status.Rented);
            DBConnection.commit();
        } catch (Exception e) { DBConnection.rollback(); throw e; }
        return rn;
    }

    // ── Queries ──────────────────────────────────────────────
    public List<Rental> getFiltered(int branchId, String status, LocalDate from, LocalDate to)
            throws SQLException {
        rentalDAO.markOverdueRentals();
        return rentalDAO.findFiltered(branchId, status, from, to);
    }

    public List<Rental> getOverdue() throws SQLException {
        rentalDAO.markOverdueRentals();
        return rentalDAO.findOverdue();
    }

    public Rental getById(int id)             throws SQLException { return rentalDAO.findById(id); }
    public List<Rental> getByCustomer(int id) throws SQLException { return rentalDAO.findByCustomer(id); }

    public void cancel(int rentalId) throws Exception {
        Rental rn = rentalDAO.findById(rentalId);
        if (rn == null) throw new Exception("Rental not found.");
        if (rn.getRentalStatus() == RentalStatus.Returned) throw new Exception("Cannot cancel a returned rental.");
        DBConnection.beginTransaction();
        try {
            rentalDAO.updateStatus(rentalId, RentalStatus.Cancelled);
            equipDAO.updateStatus(rn.getEquipmentId(), Status.Available);
            DBConnection.commit();
        } catch (Exception e) { DBConnection.rollback(); throw e; }
    }

    // ── Helpers ──────────────────────────────────────────────
    private Rental buildRental(String eqId, int custId, int branchId,
                                LocalDate start, LocalDate end, double[] bd,
                                double deposit, PaymentStatus pmt, Integer resId) {
        Rental rn = new Rental();
        rn.setEquipmentId(eqId); rn.setCustomerId(custId); rn.setBranchId(branchId);
        rn.setStartDate(start); rn.setEndDate(end);
        rn.setRentalAmount(bd[0]); rn.setLongRentalDiscount(bd[1]);
        rn.setMembershipDiscount(bd[2]); rn.setFinalPayable(bd[3]);
        rn.setSecurityDeposit(deposit);
        rn.setPaymentStatus(pmt); rn.setRentalStatus(RentalStatus.Active);
        rn.setReservationId(resId);
        return rn;
    }

    private void validateDates(String eqId, LocalDate start, LocalDate end, int excludeId)
            throws Exception {
        if (start == null || end == null) throw new Exception("Dates are required.");
        if (end.isBefore(start)) throw new Exception("End date must be on or after start date.");
        long days = start.until(end).getDays() + 1;
        if (days > 30) throw new Exception("Maximum rental duration is 30 days.");
        if (start.isBefore(LocalDate.now())) throw new Exception("Start date cannot be in the past.");
        if (rentalDAO.hasActiveRentalOverlap(eqId, start, end, excludeId))
            throw new Exception("Equipment has an overlapping active rental.");
    }

    private void checkDepositLimit(int custId, double newDeposit, String level) throws Exception {
        MembershipConfig mc = memDAO.findByLevel(level);
        double limit   = mc != null ? mc.getDepositLimit() : 500000;
        double current = custDAO.getTotalActiveDeposit(custId);
        if (current + newDeposit > limit)
            throw new Exception(String.format(
                "Deposit limit exceeded. Current: LKR %.2f, New: LKR %.2f, Limit: LKR %.2f",
                current, newDeposit, limit));
    }
}
