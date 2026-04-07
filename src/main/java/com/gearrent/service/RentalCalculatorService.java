package com.gearrent.service;

import com.gearrent.dao.MembershipConfigDAO;
import com.gearrent.entity.EquipmentCategory;
import com.gearrent.dao.PricingRuleDAO;
import com.gearrent.entity.MembershipConfig;
import com.gearrent.entity.PricingRule;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.sql.SQLException;

/**
 * Pure pricing logic – no DB writes.
 */
public class RentalCalculatorService {

    private final MembershipConfigDAO membershipDAO = new MembershipConfigDAO();

    /** Calculate base rental cost day-by-day, applying weekend multiplier */
    public double calcRentalAmount(double equipmentDailyBasePrice,
                                   EquipmentCategory category,
                                   LocalDate startDate, LocalDate endDate) {
        double total = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dow = current.getDayOfWeek();
            boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            double dailyPrice = equipmentDailyBasePrice
                    * category.getBasePriceFactor()
                    * (isWeekend ? category.getWeekendMultiplier() : 1.0);
            total += dailyPrice;
            current = current.plusDays(1);
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /** Long rental discount (applied on rental amount, not deposit) */
    public double calcLongRentalDiscount(double rentalAmount, long durationDays, PricingRule rule) {
        if (rule == null) return 0;
        if (durationDays >= rule.getLongRentalMinDays()) {
            return Math.round(rentalAmount * rule.getLongRentalDiscountPct() / 100.0 * 100.0) / 100.0;
        }
        return 0;
    }

    /** Membership discount (applied after long-rental discount) */
    public double calcMembershipDiscount(double rentalAmountAfterLongDiscount,
                                         String membershipLevel) throws SQLException {
        MembershipConfig mc = membershipDAO.findByLevel(membershipLevel);
        if (mc == null || mc.getDiscountPercentage() == 0) return 0;
        return Math.round(rentalAmountAfterLongDiscount * mc.getDiscountPercentage() / 100.0 * 100.0) / 100.0;
    }

    /**
     * Returns array: [rentalAmount, longRentalDiscount, membershipDiscount, finalPayable]
     * finalPayable = rentalAmount - longDiscount - memberDiscount + securityDeposit
     */
    public double[] buildRentalCostBreakdown(double equipmentDailyBase,
                                              EquipmentCategory category,
                                              LocalDate start, LocalDate end,
                                              double securityDeposit,
                                              String membershipLevel,
                                              PricingRule rule) throws SQLException {
        double rentalAmount         = calcRentalAmount(equipmentDailyBase, category, start, end);
        long   days                 = start.until(end).getDays() + 1;
        double longDiscount         = calcLongRentalDiscount(rentalAmount, days, rule);
        double afterLong            = rentalAmount - longDiscount;
        double memberDiscount       = calcMembershipDiscount(afterLong, membershipLevel);
        double finalPayable         = afterLong - memberDiscount + securityDeposit;

        return new double[]{
            rentalAmount, longDiscount, memberDiscount,
            Math.round(finalPayable * 100.0) / 100.0
        };
    }

    /** Late fee calculation */
    public double calcLateFee(LocalDate scheduledEnd, LocalDate actualReturn,
                               EquipmentCategory category, PricingRule rule) {
        if (!actualReturn.isAfter(scheduledEnd)) return 0;
        long daysLate = scheduledEnd.until(actualReturn).getDays();
        double feePerDay = (category != null) ? category.getDefaultLateFee()
                : (rule != null ? rule.getGlobalLateFeePerDay() : 500.0);
        return daysLate * feePerDay;
    }
}
