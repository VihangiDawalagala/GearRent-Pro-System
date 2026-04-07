package com.gearrent.entity;

public class SystemUser {

    public enum Role {
        Admin("Admin"),
        BranchManager("Branch Manager"),
        Staff("Staff");

        private final String display;
        Role(String d) { this.display = d; }
        public String getDisplay() { return display; }
        public static Role fromString(String s) {
            for (Role r : values()) if (r.display.equalsIgnoreCase(s) || r.name().equalsIgnoreCase(s)) return r;
            return Staff;
        }
        @Override public String toString() { return display; }
    }

    private int    userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role   role;
    private int    branchId;
    private String branchName;
    private boolean active;

    public SystemUser() {}

    public int    getUserId()       { return userId; }
    public void   setUserId(int v)         { userId = v; }
    public String getUsername()     { return username; }
    public void   setUsername(String v)    { username = v; }
    public String getPasswordHash() { return passwordHash; }
    public void   setPasswordHash(String v){ passwordHash = v; }
    public String getFullName()     { return fullName; }
    public void   setFullName(String v)    { fullName = v; }
    public Role   getRole()         { return role; }
    public void   setRole(Role v)          { role = v; }
    public int    getBranchId()     { return branchId; }
    public void   setBranchId(int v)       { branchId = v; }
    public String getBranchName()   { return branchName; }
    public void   setBranchName(String v)  { branchName = v; }
    public boolean isActive()       { return active; }
    public void   setActive(boolean v)     { active = v; }

    @Override public String toString() { return fullName + " (" + role + ")"; }
}
