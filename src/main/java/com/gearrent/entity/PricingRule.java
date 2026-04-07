package com.gearrent.entity;

public class PricingRule {
    private int    ruleId;
    private int    longRentalMinDays;
    private double longRentalDiscountPct;
    private double globalLateFeePerDay;

    public PricingRule() {}

    public int    getRuleId()               { return ruleId; }
    public void   setRuleId(int v)                   { ruleId = v; }
    public int    getLongRentalMinDays()    { return longRentalMinDays; }
    public void   setLongRentalMinDays(int v)        { longRentalMinDays = v; }
    public double getLongRentalDiscountPct(){ return longRentalDiscountPct; }
    public void   setLongRentalDiscountPct(double v) { longRentalDiscountPct = v; }
    public double getGlobalLateFeePerDay()  { return globalLateFeePerDay; }
    public void   setGlobalLateFeePerDay(double v)   { globalLateFeePerDay = v; }
}
