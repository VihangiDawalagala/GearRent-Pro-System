package com.gearrent.service;

import com.gearrent.dao.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public List<Object[]> getBranchRevenueReport(int branchId, LocalDate from, LocalDate to) throws SQLException {
        StringBuilder sb = new StringBuilder(
            "SELECT b.name AS branch_name, COUNT(r.rental_id) AS rental_count, " +
            "COALESCE(SUM(r.rental_amount - r.membership_discount - r.long_rental_discount),0) AS rental_income, " +
            "COALESCE(SUM(rr.late_fee),0) AS late_fee_total, " +
            "COALESCE(SUM(rr.damage_charge),0) AS damage_total " +
            "FROM rentals r JOIN branches b ON r.branch_id=b.branch_id " +
            "LEFT JOIN rental_returns rr ON rr.rental_id=r.rental_id " +
            "WHERE r.rental_status <> 'Cancelled' ");
        if (branchId > 0) sb.append("AND r.branch_id=? ");
        if (from != null) sb.append("AND r.start_date >= ? ");
        if (to   != null) sb.append("AND r.end_date <= ? ");
        sb.append("GROUP BY b.name ORDER BY b.name");

        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sb.toString())) {
            int i = 1;
            if (branchId > 0) ps.setInt(i++, branchId);
            if (from != null) ps.setDate(i++, Date.valueOf(from));
            if (to   != null) ps.setDate(i++, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[]{
                    rs.getString("branch_name"), rs.getInt("rental_count"),
                    rs.getDouble("rental_income"), rs.getDouble("late_fee_total"),
                    rs.getDouble("damage_total")});
            }
        }
        return rows;
    }

    public List<Object[]> getUtilisationReport(int branchId, LocalDate from, LocalDate to) throws SQLException {
        long totalDays = from.until(to).getDays() + 1;
        String sql =
            "SELECT e.equipment_id, CONCAT(e.brand,' ',e.model) AS brand_model, c.name AS category, " +
            "COALESCE(SUM(DATEDIFF(LEAST(r.end_date,?), GREATEST(r.start_date,?))+1),0) AS rented_days " +
            "FROM equipment e JOIN equipment_categories c ON e.category_id=c.category_id " +
            "LEFT JOIN rentals r ON r.equipment_id=e.equipment_id " +
            "  AND r.rental_status NOT IN ('Cancelled') AND r.start_date<=? AND r.end_date>=? " +
            "WHERE e.branch_id=? GROUP BY e.equipment_id, brand_model, category ORDER BY e.equipment_id";

        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(to)); ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to)); ps.setDate(4, Date.valueOf(from));
            ps.setInt(5, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long rented = rs.getLong("rented_days");
                    double pct  = totalDays > 0 ? Math.min(100, rented * 100.0 / totalDays) : 0;
                    rows.add(new Object[]{rs.getString("equipment_id"), rs.getString("brand_model"),
                        rs.getString("category"), rented, totalDays,
                        Math.round(pct * 10.0) / 10.0});
                }
            }
        }
        return rows;
    }
}
