package com.gearrent.service;

import com.gearrent.dao.*;
import com.gearrent.entity.*;
import com.gearrent.entity.Equipment.Status;
import com.gearrent.entity.Rental.RentalStatus;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReturnService {
    private final RentalDAO         rnDAO   = new RentalDAO();
    private final RentalReturnDAO   retDAO  = new RentalReturnDAO();
    private final EquipmentDAO      eqDAO   = new EquipmentDAO();
    private final EquipmentCategoryDAO catDAO = new EquipmentCategoryDAO();
    private final RentalCalculatorService calc = new RentalCalculatorService();

    public RentalReturn processReturn(int rentalId, LocalDate actualReturn,
                                      boolean isDamaged, String damageDesc,
                                      double damageCharge) throws Exception {
        Rental rn = rnDAO.findById(rentalId);
        if (rn == null) throw new Exception("Rental not found.");
        if (rn.getRentalStatus() == RentalStatus.Returned)  throw new Exception("Already returned.");
        if (rn.getRentalStatus() == RentalStatus.Cancelled) throw new Exception("Cannot return a cancelled rental.");
        if (actualReturn == null) throw new Exception("Actual return date is required.");
        if (actualReturn.isBefore(rn.getStartDate())) throw new Exception("Return date cannot be before start date.");

        Equipment eq = eqDAO.findById(rn.getEquipmentId());
        EquipmentCategory cat = catDAO.findById(eq.getCategoryId());
        PricingRule rule = new PricingRuleDAO().findFirst();

        double lateFee  = calc.calcLateFee(rn.getEndDate(), actualReturn, cat, rule);
        double total    = lateFee + (isDamaged ? damageCharge : 0);
        double deposit  = rn.getSecurityDeposit();
        double used     = Math.min(deposit, total);
        double refund   = Math.max(0, deposit - total);
        double extra    = Math.max(0, total - deposit);

        RentalReturn ret = new RentalReturn();
        ret.setRentalId(rentalId);
        ret.setActualReturnDate(actualReturn);
        ret.setDamaged(isDamaged);
        ret.setDamageDescription(isDamaged ? damageDesc : null);
        ret.setDamageCharge(isDamaged ? damageCharge : 0);
        ret.setLateFee(lateFee);
        ret.setTotalCharges(total);
        ret.setDepositUsed(used);
        ret.setRefundAmount(refund);
        ret.setAdditionalPayment(extra);

        Status newEquipStatus = isDamaged ? Status.UnderMaintenance : Status.Available;

        DBConnection.beginTransaction();
        try {
            retDAO.insert(ret);
            rnDAO.updateStatus(rentalId, RentalStatus.Returned);
            eqDAO.updateStatus(rn.getEquipmentId(), newEquipStatus);
            DBConnection.commit();
        } catch (Exception e) { DBConnection.rollback(); throw e; }
        return ret;
    }

    public RentalReturn getByRentalId(int rentalId) throws SQLException {
        return new RentalReturnDAO().findByRentalId(rentalId);
    }
}
