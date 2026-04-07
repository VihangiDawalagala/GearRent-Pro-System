package com.gearrent.dao;

import com.gearrent.entity.PricingRule;
import java.sql.*;

public class PricingRuleDAO {

    public PricingRule findFirst() throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM pricing_rules LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                PricingRule pr = new PricingRule();
                pr.setRuleId(rs.getInt("rule_id"));
                pr.setLongRentalMinDays(rs.getInt("long_rental_min_days"));
                pr.setLongRentalDiscountPct(rs.getDouble("long_rental_discount_pct"));
                pr.setGlobalLateFeePerDay(rs.getDouble("global_late_fee_per_day"));
                return pr;
            }
        }
        // return safe defaults if table is empty
        PricingRule pr = new PricingRule();
        pr.setLongRentalMinDays(7);
        pr.setLongRentalDiscountPct(10.0);
        pr.setGlobalLateFeePerDay(500.0);
        return pr;
    }

    public void update(PricingRule pr) throws SQLException {
        String sql = "UPDATE pricing_rules SET long_rental_min_days=?, " +
                     "long_rental_discount_pct=?, global_late_fee_per_day=? WHERE rule_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pr.getLongRentalMinDays());
            ps.setDouble(2, pr.getLongRentalDiscountPct());
            ps.setDouble(3, pr.getGlobalLateFeePerDay());
            ps.setInt(4, pr.getRuleId());
            ps.executeUpdate();
        }
    }
}
