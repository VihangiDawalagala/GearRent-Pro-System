package com.gearrent.dao;

import com.gearrent.entity.Reservation;
import com.gearrent.entity.Reservation.Status;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private static final String BASE =
        "SELECT r.*, " +
        "CONCAT(e.brand,' ',e.model,' [',e.equipment_id,']') AS equipment_display, " +
        "c.name AS customer_name, b.name AS branch_name " +
        "FROM reservations r " +
        "JOIN equipment e ON r.equipment_id = e.equipment_id " +
        "JOIN customers c ON r.customer_id = c.customer_id " +
        "JOIN branches b ON r.branch_id = b.branch_id ";

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation rv = new Reservation();
        rv.setReservationId(rs.getInt("reservation_id"));
        rv.setEquipmentId(rs.getString("equipment_id"));
        rv.setEquipmentDisplay(rs.getString("equipment_display"));
        rv.setCustomerId(rs.getInt("customer_id"));
        rv.setCustomerName(rs.getString("customer_name"));
        rv.setBranchId(rs.getInt("branch_id"));
        rv.setBranchName(rs.getString("branch_name"));
        rv.setStartDate(rs.getDate("start_date").toLocalDate());
        rv.setEndDate(rs.getDate("end_date").toLocalDate());
        rv.setStatus(Status.valueOf(rs.getString("status")));
        rv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toLocalDate());
        return rv;
    }

    public List<Reservation> findAll(int branchId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE + (branchId > 0 ? "WHERE r.branch_id=? " : "") + "ORDER BY r.start_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            if (branchId > 0) ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Reservation> findActive(int branchId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = BASE + "WHERE r.status='Active' " +
                     (branchId > 0 ? "AND r.branch_id=? " : "") + "ORDER BY r.start_date";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            if (branchId > 0) ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Reservation findById(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE + "WHERE r.reservation_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Check overlap for equipment within a date range, excluding a specific reservation */
    public boolean hasOverlap(String equipmentId, LocalDate start, LocalDate end, int excludeReservationId) throws SQLException {
        String sql = "SELECT 1 FROM reservations WHERE equipment_id=? AND status='Active' " +
                     "AND reservation_id<>? AND NOT (end_date < ? OR start_date > ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, equipmentId);
            ps.setInt(2, excludeReservationId);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int insert(Reservation rv) throws SQLException {
        String sql = "INSERT INTO reservations (equipment_id,customer_id,branch_id,start_date,end_date,status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rv.getEquipmentId());
            ps.setInt(2, rv.getCustomerId());
            ps.setInt(3, rv.getBranchId());
            ps.setDate(4, Date.valueOf(rv.getStartDate()));
            ps.setDate(5, Date.valueOf(rv.getEndDate()));
            ps.setString(6, rv.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { rv.setReservationId(keys.getInt(1)); return rv.getReservationId(); }
            }
        }
        return -1;
    }

    public void updateStatus(int reservationId, Status status) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE reservations SET status=? WHERE reservation_id=?")) {
            ps.setString(1, status.name()); ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }
}
