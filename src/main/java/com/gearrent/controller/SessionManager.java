package com.gearrent.controller;

import com.gearrent.entity.SystemUser;
import com.gearrent.entity.SystemUser.Role;

/**
 * Holds the currently authenticated user for the session.
 */
public class SessionManager {

    private static SessionManager instance;
    private SystemUser currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public SystemUser getCurrentUser() { return currentUser; }
    public void setCurrentUser(SystemUser user) { this.currentUser = user; }

    public boolean isAdmin()         { return currentUser != null && currentUser.getRole() == Role.Admin; }
    public boolean isBranchManager() { return currentUser != null && currentUser.getRole() == Role.BranchManager; }
    public boolean isStaff()         { return currentUser != null && currentUser.getRole() == Role.Staff; }

    /** Branch scope: -1 means Admin (sees all). */
    public int getScopedBranchId() {
        if (isAdmin()) return -1;
        return currentUser.getBranchId();
    }

    public void logout() { currentUser = null; }
}
