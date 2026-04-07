package com.gearrent.entity;

public class MembershipConfig {
    private String level;        // Regular | Silver | Gold
    private double discountPercentage;
    private double depositLimit;

    public MembershipConfig() {}
    public MembershipConfig(String level, double discountPercentage, double depositLimit) {
        this.level              = level;
        this.discountPercentage = discountPercentage;
        this.depositLimit       = depositLimit;
    }

    public String getLevel()              { return level; }
    public void   setLevel(String v)               { level = v; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void   setDiscountPercentage(double v)  { discountPercentage = v; }
    public double getDepositLimit()       { return depositLimit; }
    public void   setDepositLimit(double v)        { depositLimit = v; }

    @Override public String toString() { return level; }
}
