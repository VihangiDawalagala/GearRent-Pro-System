package com.gearrent.dao;

import com.gearrent.entity.Equipment;
import com.gearrent.entity.Equipment.Status;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    private static final String BASE_SQL =
        "SELECT e.*, c.name AS category_name, b.name AS branch_name " +
        "FROM equipment e " +
        "JOIN equipment_categories c ON e.category_id = c.category_id " +
        "JOIN branches b ON e.branch_id = b.branch_id ";

    private Equipment map(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getString("equipment_id"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setCategoryName(rs.getString("category_name"));
        e.setBrand(rs.getString("brand"));
        e.setModel(rs.getString("model"));
        e.setPurchaseYear(rs.getInt("purchase_year"));
        e.setDailyBasePrice(rs.getDouble("daily_base_price"));
        e.setSecurityDeposit(rs.getDouble("security_deposit"));
        e.setStatus(Status.fromString(rs.getString("status")));
        e.setBranchId(rs.getInt("branch_id"));
        e.setBranchName(rs.getString("branch_name"));
        return e;
    }

    public List<Equipment> findAll() throws SQLException {
        List<Equipment> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE_SQL + "ORDER BY e.equipment_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Equipment> findByBranch(int branchId) throws SQLException {
        List<Equipment> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE_SQL + "WHERE e.branch_id=? ORDER BY e.equipment_id")) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Flexible filter: pass -1 / null to skip a filter */
    public List<Equipment> findFiltered(int branchId, int categoryId,
                                        String statusStr, String keyword) throws SQLException {
        List<Equipment> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(BASE_SQL + "WHERE 1=1 ");
        if (branchId   > 0)   sb.append("AND e.branch_id=? ");
        if (categoryId > 0)   sb.append("AND e.category_id=? ");
        if (statusStr  != null && !statusStr.isEmpty()) sb.append("AND e.status=? ");
        if (keyword    != null && !keyword.trim().isEmpty()) sb.append("AND (e.brand LIKE ? OR e.model LIKE ? OR e.equipment_id LIKE ?) ");
        sb.append("ORDER BY e.equipment_id");

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sb.toString())) {
            int idx = 1;
            if (branchId   > 0)   ps.setInt(idx++, branchId);
            if (categoryId > 0)   ps.setInt(idx++, categoryId);
            if (statusStr  != null && !statusStr.isEmpty()) ps.setString(idx++, statusStr);
            if (keyword    != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Equipment findById(String id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE_SQL + "WHERE e.equipment_id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Available equipment for a branch that does not overlap a date range */
    public List<Equipment> findAvailableForPeriod(int branchId, java.time.LocalDate start,
                                                   java.time.LocalDate end) throws SQLException {
        String sql = BASE_SQL +
            "WHERE e.branch_id=? AND e.status='Available' " +
            "AND e.equipment_id NOT IN (" +
            "  SELECT equipment_id FROM reservations " +
            "  WHERE status='Active' AND NOT (end_date < ? OR start_date > ?) " +
            ") AND e.equipment_id NOT IN (" +
            "  SELECT equipment_id FROM rentals " +
            "  WHERE rental_status='Active' AND NOT (end_date < ? OR start_date > ?) " +
            ")";
        List<Equipment> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ps.setDate(4, Date.valueOf(start));
            ps.setDate(5, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Equipment e) throws SQLException {
        String sql = "INSERT INTO equipment (equipment_id,category_id,brand,model,purchase_year,daily_base_price,security_deposit,status,branch_id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, e.getEquipmentId());
            ps.setInt(2, e.getCategoryId());
            ps.setString(3, e.getBrand());
            ps.setString(4, e.getModel());
            ps.setInt(5, e.getPurchaseYear());
            ps.setDouble(6, e.getDailyBasePrice());
            ps.setDouble(7, e.getSecurityDeposit());
            ps.setString(8, e.getStatus().getDisplay());
            ps.setInt(9, e.getBranchId());
            ps.executeUpdate();
        }
    }

    public void update(Equipment e) throws SQLException {
        String sql = "UPDATE equipment SET category_id=?,brand=?,model=?,purchase_year=?,daily_base_price=?,security_deposit=?,status=?,branch_id=? WHERE equipment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, e.getCategoryId());
            ps.setString(2, e.getBrand());
            ps.setString(3, e.getModel());
            ps.setInt(4, e.getPurchaseYear());
            ps.setDouble(5, e.getDailyBasePrice());
            ps.setDouble(6, e.getSecurityDeposit());
            ps.setString(7, e.getStatus().getDisplay());
            ps.setInt(8, e.getBranchId());
            ps.setString(9, e.getEquipmentId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(String equipmentId, Status status) throws SQLException {
        String sql = "UPDATE equipment SET status=? WHERE equipment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.getDisplay());
            ps.setString(2, equipmentId);
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM equipment WHERE equipment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean idExists(String id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT 1 FROM equipment WHERE equipment_id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
