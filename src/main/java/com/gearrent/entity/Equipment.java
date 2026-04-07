package com.gearrent.entity;

public class Equipment {

    public enum Status {
        Available("Available"),
        Reserved("Reserved"),
        Rented("Rented"),
        UnderMaintenance("Under Maintenance");

        private final String display;
        Status(String d) { this.display = d; }
        public String getDisplay() { return display; }
        public static Status fromString(String s) {
            for (Status st : values()) if (st.display.equalsIgnoreCase(s) || st.name().equalsIgnoreCase(s)) return st;
            return Available;
        }
        @Override public String toString() { return display; }
    }

    private String   equipmentId;
    private int      categoryId;
    private String   categoryName;
    private String   brand;
    private String   model;
    private int      purchaseYear;
    private double   dailyBasePrice;
    private double   securityDeposit;
    private Status   status;
    private int      branchId;
    private String   branchName;

    public Equipment() {}

    // Getters & Setters
    public String   getEquipmentId()    { return equipmentId; }
    public void     setEquipmentId(String v)     { equipmentId = v; }
    public int      getCategoryId()     { return categoryId; }
    public void     setCategoryId(int v)         { categoryId = v; }
    public String   getCategoryName()   { return categoryName; }
    public void     setCategoryName(String v)    { categoryName = v; }
    public String   getBrand()          { return brand; }
    public void     setBrand(String v)           { brand = v; }
    public String   getModel()          { return model; }
    public void     setModel(String v)           { model = v; }
    public int      getPurchaseYear()   { return purchaseYear; }
    public void     setPurchaseYear(int v)       { purchaseYear = v; }
    public double   getDailyBasePrice() { return dailyBasePrice; }
    public void     setDailyBasePrice(double v)  { dailyBasePrice = v; }
    public double   getSecurityDeposit(){ return securityDeposit; }
    public void     setSecurityDeposit(double v) { securityDeposit = v; }
    public Status   getStatus()         { return status; }
    public void     setStatus(Status v)          { status = v; }
    public int      getBranchId()       { return branchId; }
    public void     setBranchId(int v)           { branchId = v; }
    public String   getBranchName()     { return branchName; }
    public void     setBranchName(String v)      { branchName = v; }

    public String getDisplayName() { return brand + " " + model + " [" + equipmentId + "]"; }

    @Override public String toString() { return getDisplayName(); }
}
