package com.gearrent.service;

import com.gearrent.dao.SystemUserDAO;
import com.gearrent.entity.SystemUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

public class AuthService {

    private final SystemUserDAO userDAO = new SystemUserDAO();

    public SystemUser login(String username, String rawPassword) throws Exception {
        if (username == null || username.isBlank())
            throw new Exception("Username is required.");
        if (rawPassword == null || rawPassword.isBlank())
            throw new Exception("Password is required.");

        SystemUser user = userDAO.findByUsername(username.trim());
        if (user == null)
            throw new Exception("Invalid username or password.");
        if (!user.isActive())
            throw new Exception("Your account is disabled. Contact the administrator.");

        String hashed = sha256(rawPassword);
        if (!hashed.equalsIgnoreCase(user.getPasswordHash()))
            throw new Exception("Invalid username or password.");

        return user;
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public void changePassword(int userId, String oldRaw, String newRaw) throws Exception {
        SystemUser user = userDAO.findById(userId);
        if (user == null) throw new Exception("User not found.");
        if (!sha256(oldRaw).equalsIgnoreCase(user.getPasswordHash()))
            throw new Exception("Current password is incorrect.");
        if (newRaw == null || newRaw.length() < 6)
            throw new Exception("New password must be at least 6 characters.");
        userDAO.updatePassword(userId, sha256(newRaw));
    }
}
