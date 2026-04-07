package com.gearrent.dao;

import com.gearrent.entity.EquipmentCategory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentCategoryDAO {

    private EquipmentCategory map(ResultSet rs) throws SQLException {
        return new EquipmentCategory(
            rs.getInt("category_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("base_price_factor"),
            rs.getDouble("weekend_multiplier"),
            rs.getDouble("default_late_fee"),
            rs.getBoolean("is_active")
        );
    }

    public List<EquipmentCategory> findAll() throws SQLException {
        List<EquipmentCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM equipment_categories ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<EquipmentCategory> findAllActive() throws SQLException {
        List<EquipmentCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM equipment_categories WHERE is_active=1 ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public EquipmentCategory findById(int id) throws SQLException {
        String sql = "SELECT * FROM equipment_categories WHERE category_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(EquipmentCategory c) throws SQLException {
        String sql = "INSERT INTO equipment_categories (name,description,base_price_factor,weekend_multiplier,default_late_fee,is_active) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setDouble(3, c.getBasePriceFactor());
            ps.setDouble(4, c.getWeekendMultiplier());
            ps.setDouble(5, c.getDefaultLateFee());
            ps.setBoolean(6, c.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setCategoryId(keys.getInt(1));
            }
        }
    }

    public void update(EquipmentCategory c) throws SQLException {
        String sql = "UPDATE equipment_categories SET name=?,description=?,base_price_factor=?,weekend_multiplier=?,default_late_fee=?,is_active=? WHERE category_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setDouble(3, c.getBasePriceFactor());
            ps.setDouble(4, c.getWeekendMultiplier());
            ps.setDouble(5, c.getDefaultLateFee());
            ps.setBoolean(6, c.isActive());
            ps.setInt(7, c.getCategoryId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM equipment_categories WHERE category_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
