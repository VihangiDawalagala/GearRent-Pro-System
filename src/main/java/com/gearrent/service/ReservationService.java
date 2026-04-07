package com.gearrent.service;

import com.gearrent.dao.*;
import com.gearrent.entity.*;
import com.gearrent.entity.Equipment.Status;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {
    private final ReservationDAO   rvDAO   = new ReservationDAO();
    private final EquipmentDAO     eqDAO   = new EquipmentDAO();
    private final CustomerDAO      custDAO = new CustomerDAO();
    private final MembershipConfigDAO memDAO = new MembershipConfigDAO();
    private final RentalDAO        rnDAO   = new RentalDAO();

    public List<Reservation> getAll(int branchId)    throws SQLException { return rvDAO.findAll(branchId); }
    public List<Reservation> getActive(int branchId) throws SQLException { return rvDAO.findActive(branchId); }
    public Reservation getById(int id)               throws SQLException { return rvDAO.findById(id); }

    public Reservation create(String equipmentId, int customerId, int branchId,
                               LocalDate start, LocalDate end) throws Exception {
        if (start == null || end == null) throw new Exception("Dates are required.");
        long days = start.until(end).getDays() + 1;
        if (days > 30) throw new Exception("Maximum reservation duration is 30 days.");
        if (days < 1)  throw new Exception("End date must be on or after start date.");
        if (start.isBefore(LocalDate.now())) throw new Exception("Start date cannot be in the past.");

        if (rvDAO.hasOverlap(equipmentId, start, end, -1))
            throw new Exception("Equipment is already reserved for overlapping dates.");
        if (rnDAO.hasActiveRentalOverlap(equipmentId, start, end, -1))
            throw new Exception("Equipment has an active rental for overlapping dates.");

        Equipment eq = eqDAO.findById(equipmentId);
        Customer cust = custDAO.findById(customerId);
        MembershipConfig mc = memDAO.findByLevel(cust.getMembershipLevel().name());
        double limit   = mc != null ? mc.getDepositLimit() : 500000;
        double current = custDAO.getTotalActiveDeposit(customerId);
        if (current + eq.getSecurityDeposit() > limit)
            throw new Exception("This reservation would exceed the customer's deposit limit.");

        Reservation rv = new Reservation();
        rv.setEquipmentId(equipmentId); rv.setCustomerId(customerId); rv.setBranchId(branchId);
        rv.setStartDate(start); rv.setEndDate(end);
        rv.setStatus(Reservation.Status.Active);

        DBConnection.beginTransaction();
        try {
            rvDAO.insert(rv);
            eqDAO.updateStatus(equipmentId, Status.Reserved);
            DBConnection.commit();
        } catch (Exception e) { DBConnection.rollback(); throw e; }
        return rv;
    }

    public void cancel(int reservationId) throws Exception {
        Reservation rv = rvDAO.findById(reservationId);
        if (rv == null) throw new Exception("Reservation not found.");
        if (rv.getStatus() != Reservation.Status.Active) throw new Exception("Only active reservations can be cancelled.");
        DBConnection.beginTransaction();
        try {
            rvDAO.updateStatus(reservationId, Reservation.Status.Cancelled);
            eqDAO.updateStatus(rv.getEquipmentId(), Status.Available);
            DBConnection.commit();
        } catch (Exception e) { DBConnection.rollback(); throw e; }
    }
}
