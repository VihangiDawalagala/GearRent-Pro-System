package com.gearrent.dao;

import com.gearrent.entity.MembershipConfig;
import com.gearrent.entity.PricingRule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipConfigDAO {

    public List<MembershipConfig> findAll() throws SQLException {
        List<MembershipConfig> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM membership_configs ORDER BY FIELD(level,'Regular','Silver','Gold')");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new MembershipConfig(
                    rs.getString("level"),
                    rs.getDouble("discount_percentage"),
                    rs.getDouble("deposit_limit")
                ));
            }
        }
        return list;
    }

    public MembershipConfig findByLevel(String level) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM membership_configs WHERE level=?")) {
            ps.setString(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new MembershipConfig(
                    rs.getString("level"),
                    rs.getDouble("discount_percentage"),
                    rs.getDouble("deposit_limit"));
            }
        }
        return null;
    }

    public void update(MembershipConfig mc) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE membership_configs SET discount_percentage=?,deposit_limit=? WHERE level=?")) {
            ps.setDouble(1, mc.getDiscountPercentage());
            ps.setDouble(2, mc.getDepositLimit());
            ps.setString(3, mc.getLevel());
            ps.executeUpdate();
        }
    }
}



