package com.gearrent.dao;

import com.gearrent.entity.Branch;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

    private Branch map(ResultSet rs) throws SQLException {
        return new Branch(
            rs.getInt("branch_id"),
            rs.getString("branch_code"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getString("contact"),
            rs.getBoolean("is_active")
        );
    }

    public List<Branch> findAll() throws SQLException {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT * FROM branches ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Branch> findAllActive() throws SQLException {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT * FROM branches WHERE is_active=1 ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Branch findById(int id) throws SQLException {
        String sql = "SELECT * FROM branches WHERE branch_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Branch b) throws SQLException {
        String sql = "INSERT INTO branches (branch_code,name,address,contact,is_active) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBranchCode());
            ps.setString(2, b.getName());
            ps.setString(3, b.getAddress());
            ps.setString(4, b.getContact());
            ps.setBoolean(5, b.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) b.setBranchId(keys.getInt(1));
            }
        }
    }

    public void update(Branch b) throws SQLException {
        String sql = "UPDATE branches SET branch_code=?,name=?,address=?,contact=?,is_active=? WHERE branch_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getBranchCode());
            ps.setString(2, b.getName());
            ps.setString(3, b.getAddress());
            ps.setString(4, b.getContact());
            ps.setBoolean(5, b.isActive());
            ps.setInt(6, b.getBranchId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM branches WHERE branch_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean codeExists(String code, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM branches WHERE branch_code=? AND branch_id<>?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
