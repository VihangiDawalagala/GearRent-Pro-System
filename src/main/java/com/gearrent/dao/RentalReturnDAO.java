package com.gearrent.dao;

import com.gearrent.entity.RentalReturn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalReturnDAO {

    private RentalReturn map(ResultSet rs) throws SQLException {
        RentalReturn r = new RentalReturn();
        r.setReturnId(rs.getInt("return_id"));
        r.setRentalId(rs.getInt("rental_id"));
        r.setActualReturnDate(rs.getDate("actual_return_date").toLocalDate());
        r.setDamaged(rs.getBoolean("is_damaged"));
        r.setDamageDescription(rs.getString("damage_description"));
        r.setDamageCharge(rs.getDouble("damage_charge"));
        r.setLateFee(rs.getDouble("late_fee"));
        r.setTotalCharges(rs.getDouble("total_charges"));
        r.setDepositUsed(rs.getDouble("deposit_used"));
        r.setRefundAmount(rs.getDouble("refund_amount"));
        r.setAdditionalPayment(rs.getDouble("additional_payment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime().toLocalDate());
        return r;
    }

    public RentalReturn findByRentalId(int rentalId) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM rental_returns WHERE rental_id=?")) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<RentalReturn> findAll() throws SQLException {
        List<RentalReturn> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM rental_returns ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(RentalReturn r) throws SQLException {
        String sql = "INSERT INTO rental_returns (rental_id,actual_return_date,is_damaged,damage_description," +
                     "damage_charge,late_fee,total_charges,deposit_used,refund_amount,additional_payment) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getRentalId());
            ps.setDate(2, Date.valueOf(r.getActualReturnDate()));
            ps.setBoolean(3, r.isDamaged());
            ps.setString(4, r.getDamageDescription());
            ps.setDouble(5, r.getDamageCharge());
            ps.setDouble(6, r.getLateFee());
            ps.setDouble(7, r.getTotalCharges());
            ps.setDouble(8, r.getDepositUsed());
            ps.setDouble(9, r.getRefundAmount());
            ps.setDouble(10, r.getAdditionalPayment());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setReturnId(keys.getInt(1));
            }
        }
    }
}
