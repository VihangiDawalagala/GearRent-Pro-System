package com.gearrent.entity;

public class EquipmentCategory {
    private int    categoryId;
    private String name;
    private String description;
    private double basePriceFactor;
    private double weekendMultiplier;
    private double defaultLateFee;
    private boolean active;

    public EquipmentCategory() {}

    public EquipmentCategory(int categoryId, String name, String description,
                             double basePriceFactor, double weekendMultiplier,
                             double defaultLateFee, boolean active) {
        this.categoryId       = categoryId;
        this.name             = name;
        this.description      = description;
        this.basePriceFactor  = basePriceFactor;
        this.weekendMultiplier= weekendMultiplier;
        this.defaultLateFee   = defaultLateFee;
        this.active           = active;
    }

    public int    getCategoryId()        { return categoryId; }
    public void   setCategoryId(int v)          { categoryId = v; }
    public String getName()              { return name; }
    public void   setName(String v)             { name = v; }
    public String getDescription()       { return description; }
    public void   setDescription(String v)      { description = v; }
    public double getBasePriceFactor()   { return basePriceFactor; }
    public void   setBasePriceFactor(double v)  { basePriceFactor = v; }
    public double getWeekendMultiplier() { return weekendMultiplier; }
    public void   setWeekendMultiplier(double v){ weekendMultiplier = v; }
    public double getDefaultLateFee()    { return defaultLateFee; }
    public void   setDefaultLateFee(double v)   { defaultLateFee = v; }
    public boolean isActive()            { return active; }
    public void   setActive(boolean v)          { active = v; }

    @Override public String toString() { return name; }
}
