package com.gearrent.service;

import com.gearrent.dao.SystemUserDAO;
import com.gearrent.entity.SystemUser;
import com.gearrent.entity.SystemUser.Role;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final SystemUserDAO dao = new SystemUserDAO();

    public List<SystemUser> getAll()    throws SQLException { return dao.findAll(); }
    public SystemUser getById(int id)   throws SQLException { return dao.findById(id); }

    public void save(SystemUser u, String rawPassword) throws Exception {
        if (u.getUsername() == null || u.getUsername().isBlank()) throw new Exception("Username is required.");
        if (u.getFullName() == null || u.getFullName().isBlank()) throw new Exception("Full name is required.");
        if (u.getRole() == null)                                  throw new Exception("Role is required.");
        if (u.getRole() != Role.Admin && u.getBranchId() <= 0)
            throw new Exception("Branch must be assigned for Branch Manager or Staff.");
        if (dao.usernameExists(u.getUsername(), u.getUserId()))
            throw new Exception("Username '" + u.getUsername() + "' is already taken.");
        if (u.getUserId() == 0) {
            if (rawPassword == null || rawPassword.isBlank()) throw new Exception("Password is required for new users.");
            u.setPasswordHash(AuthService.sha256(rawPassword));
            dao.insert(u);
        } else {
            dao.update(u);
            if (rawPassword != null && !rawPassword.isBlank())
                dao.updatePassword(u.getUserId(), AuthService.sha256(rawPassword));
        }
    }

    public void delete(int id) throws Exception { dao.delete(id); }
}
