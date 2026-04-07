package com.gearrent.dao;

import com.gearrent.entity.Customer;
import com.gearrent.entity.Customer.MembershipLevel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("customer_id"),
            rs.getString("name"),
            rs.getString("nic_passport"),
            rs.getString("contact_no"),
            rs.getString("email"),
            rs.getString("address"),
            MembershipLevel.valueOf(rs.getString("membership_level"))
        );
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM customers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Customer findById(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM customers WHERE customer_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR nic_passport LIKE ? OR email LIKE ? ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name,nic_passport,contact_no,email,address,membership_level) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getNicPassport());
            ps.setString(3, c.getContactNo());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAddress());
            ps.setString(6, c.getMembershipLevel().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setCustomerId(keys.getInt(1));
            }
        }
    }

    public void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?,nic_passport=?,contact_no=?,email=?,address=?,membership_level=? WHERE customer_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getNicPassport());
            ps.setString(3, c.getContactNo());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAddress());
            ps.setString(6, c.getMembershipLevel().name());
            ps.setInt(7, c.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("DELETE FROM customers WHERE customer_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean nicExists(String nic, int excludeId) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT 1 FROM customers WHERE nic_passport=? AND customer_id<>?")) {
            ps.setString(1, nic); ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** Total active security deposit currently held for a customer */
    public double getTotalActiveDeposit(int customerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(security_deposit),0) FROM rentals " +
                     "WHERE customer_id=? AND rental_status IN ('Active','Overdue')";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }
}
