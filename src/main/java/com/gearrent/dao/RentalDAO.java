package com.gearrent.dao;

import com.gearrent.entity.Rental;
import com.gearrent.entity.Rental.PaymentStatus;
import com.gearrent.entity.Rental.RentalStatus;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    private static final String BASE =
        "SELECT r.*, " +
        "CONCAT(e.brand,' ',e.model,' [',e.equipment_id,']') AS equipment_display, " +
        "c.name AS customer_name, b.name AS branch_name " +
        "FROM rentals r " +
        "JOIN equipment e ON r.equipment_id = e.equipment_id " +
        "JOIN customers c ON r.customer_id = c.customer_id " +
        "JOIN branches b ON r.branch_id = b.branch_id ";

    private Rental map(ResultSet rs) throws SQLException {
        Rental rn = new Rental();
        rn.setRentalId(rs.getInt("rental_id"));
        rn.setEquipmentId(rs.getString("equipment_id"));
        rn.setEquipmentDisplay(rs.getString("equipment_display"));
        rn.setCustomerId(rs.getInt("customer_id"));
        rn.setCustomerName(rs.getString("customer_name"));
        rn.setBranchId(rs.getInt("branch_id"));
        rn.setBranchName(rs.getString("branch_name"));
        rn.setStartDate(rs.getDate("start_date").toLocalDate());
        rn.setEndDate(rs.getDate("end_date").toLocalDate());
        rn.setRentalAmount(rs.getDouble("rental_amount"));
        rn.setSecurityDeposit(rs.getDouble("security_deposit"));
        rn.setMembershipDiscount(rs.getDouble("membership_discount"));
        rn.setLongRentalDiscount(rs.getDouble("long_rental_discount"));
        rn.setFinalPayable(rs.getDouble("final_payable"));
        rn.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
        rn.setRentalStatus(RentalStatus.fromString(rs.getString("rental_status")));
        int resId = rs.getInt("reservation_id");
        rn.setReservationId(rs.wasNull() ? null : resId);
        rn.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toLocalDate());
        return rn;
    }

    public List<Rental> findFiltered(int branchId, String statusStr, LocalDate from, LocalDate to) throws SQLException {
        List<Rental> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(BASE + "WHERE 1=1 ");
        if (branchId > 0) sb.append("AND r.branch_id=? ");
        if (statusStr != null && !statusStr.isEmpty()) sb.append("AND r.rental_status=? ");
        if (from != null) sb.append("AND r.start_date >= ? ");
        if (to   != null) sb.append("AND r.end_date <= ? ");
        sb.append("ORDER BY r.created_at DESC");
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sb.toString())) {
            int i = 1;
            if (branchId > 0) ps.setInt(i++, branchId);
            if (statusStr != null && !statusStr.isEmpty()) ps.setString(i++, statusStr);
            if (from != null) ps.setDate(i++, Date.valueOf(from));
            if (to   != null) ps.setDate(i++, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Rental findById(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE + "WHERE r.rental_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Rental> findOverdue() throws SQLException {
        List<Rental> list = new ArrayList<>();
        String sql = BASE + "WHERE r.rental_status NOT IN ('Returned','Cancelled') AND r.end_date < CURDATE() ORDER BY r.end_date";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Rental> findActiveByCustomer(int customerId) throws SQLException {
        List<Rental> list = new ArrayList<>();
        String sql = BASE + "WHERE r.customer_id=? AND r.rental_status IN ('Active','Overdue') ORDER BY r.start_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Rental> findByCustomer(int customerId) throws SQLException {
        List<Rental> list = new ArrayList<>();
        String sql = BASE + "WHERE r.customer_id=? ORDER BY r.created_at DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean hasActiveRentalOverlap(String equipmentId, LocalDate start, LocalDate end, int excludeRentalId) throws SQLException {
        String sql = "SELECT 1 FROM rentals WHERE equipment_id=? AND rental_status IN ('Active','Overdue') " +
                     "AND rental_id<>? AND NOT (end_date < ? OR start_date > ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, equipmentId);
            ps.setInt(2, excludeRentalId);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int insert(Rental rn) throws SQLException {
        String sql = "INSERT INTO rentals (equipment_id,customer_id,branch_id,start_date,end_date," +
                     "rental_amount,security_deposit,membership_discount,long_rental_discount," +
                     "final_payable,payment_status,rental_status,reservation_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rn.getEquipmentId());
            ps.setInt(2, rn.getCustomerId());
            ps.setInt(3, rn.getBranchId());
            ps.setDate(4, Date.valueOf(rn.getStartDate()));
            ps.setDate(5, Date.valueOf(rn.getEndDate()));
            ps.setDouble(6, rn.getRentalAmount());
            ps.setDouble(7, rn.getSecurityDeposit());
            ps.setDouble(8, rn.getMembershipDiscount());
            ps.setDouble(9, rn.getLongRentalDiscount());
            ps.setDouble(10, rn.getFinalPayable());
            ps.setString(11, rn.getPaymentStatus().toString());
            ps.setString(12, rn.getRentalStatus().name());
            if (rn.getReservationId() != null) ps.setInt(13, rn.getReservationId());
            else ps.setNull(13, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { rn.setRentalId(keys.getInt(1)); return rn.getRentalId(); }
            }
        }
        return -1;
    }

    public void updateStatus(int rentalId, RentalStatus status) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE rentals SET rental_status=? WHERE rental_id=?")) {
            ps.setString(1, status.name()); ps.setInt(2, rentalId);
            ps.executeUpdate();
        }
    }

    public void updatePaymentStatus(int rentalId, PaymentStatus status) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE rentals SET payment_status=? WHERE rental_id=?")) {
            ps.setString(1, status.toString()); ps.setInt(2, rentalId);
            ps.executeUpdate();
        }
    }

    /** Mark overdue: sets status=Overdue where past end_date and still Active */
    public void markOverdueRentals() throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE rentals SET rental_status='Overdue' WHERE rental_status='Active' AND end_date < CURDATE()")) {
            ps.executeUpdate();
        }
    }
}
