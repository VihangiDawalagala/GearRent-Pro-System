package com.gearrent.dao;

import com.gearrent.entity.SystemUser;
import com.gearrent.entity.SystemUser.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemUserDAO {

    private SystemUser map(ResultSet rs) throws SQLException {
        SystemUser u = new SystemUser();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(Role.fromString(rs.getString("role")));
        u.setBranchId(rs.getInt("branch_id"));
        u.setBranchName(rs.getString("branch_name"));
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }

    private static final String BASE =
        "SELECT u.*, COALESCE(b.name,'—') AS branch_name FROM system_users u " +
        "LEFT JOIN branches b ON u.branch_id = b.branch_id ";

    public List<SystemUser> findAll() throws SQLException {
        List<SystemUser> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE + "ORDER BY u.full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public SystemUser findByUsername(String username) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE + "WHERE u.username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public SystemUser findById(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(BASE + "WHERE u.user_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(SystemUser u) throws SQLException {
        String sql = "INSERT INTO system_users (username,password_hash,full_name,role,branch_id,is_active) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getRole().getDisplay());
            if (u.getBranchId() > 0) ps.setInt(5, u.getBranchId());
            else ps.setNull(5, Types.INTEGER);
            ps.setBoolean(6, u.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setUserId(keys.getInt(1));
            }
        }
    }

    public void update(SystemUser u) throws SQLException {
        String sql = "UPDATE system_users SET username=?,full_name=?,role=?,branch_id=?,is_active=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getRole().getDisplay());
            if (u.getBranchId() > 0) ps.setInt(4, u.getBranchId());
            else ps.setNull(4, Types.INTEGER);
            ps.setBoolean(5, u.isActive());
            ps.setInt(6, u.getUserId());
            ps.executeUpdate();
        }
    }

    public void updatePassword(int userId, String newHash) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE system_users SET password_hash=? WHERE user_id=?")) {
            ps.setString(1, newHash); ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("DELETE FROM system_users WHERE user_id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public boolean usernameExists(String username, int excludeId) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT 1 FROM system_users WHERE username=? AND user_id<>?")) {
            ps.setString(1, username); ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
