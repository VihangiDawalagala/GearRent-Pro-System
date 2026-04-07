package com.gearrent.entity;

import java.time.LocalDate;

public class RentalReturn {
    private int       returnId;
    private int       rentalId;
    private LocalDate actualReturnDate;
    private boolean   damaged;
    private String    damageDescription;
    private double    damageCharge;
    private double    lateFee;
    private double    totalCharges;
    private double    depositUsed;
    private double    refundAmount;
    private double    additionalPayment;
    private LocalDate createdAt;

    public RentalReturn() {}

    public int       getReturnId()           { return returnId; }
    public void      setReturnId(int v)               { returnId = v; }
    public int       getRentalId()           { return rentalId; }
    public void      setRentalId(int v)               { rentalId = v; }
    public LocalDate getActualReturnDate()   { return actualReturnDate; }
    public void      setActualReturnDate(LocalDate v) { actualReturnDate = v; }
    public boolean   isDamaged()             { return damaged; }
    public void      setDamaged(boolean v)            { damaged = v; }
    public String    getDamageDescription()  { return damageDescription; }
    public void      setDamageDescription(String v)   { damageDescription = v; }
    public double    getDamageCharge()       { return damageCharge; }
    public void      setDamageCharge(double v)        { damageCharge = v; }
    public double    getLateFee()            { return lateFee; }
    public void      setLateFee(double v)             { lateFee = v; }
    public double    getTotalCharges()       { return totalCharges; }
    public void      setTotalCharges(double v)        { totalCharges = v; }
    public double    getDepositUsed()        { return depositUsed; }
    public void      setDepositUsed(double v)         { depositUsed = v; }
    public double    getRefundAmount()       { return refundAmount; }
    public void      setRefundAmount(double v)        { refundAmount = v; }
    public double    getAdditionalPayment()  { return additionalPayment; }
    public void      setAdditionalPayment(double v)   { additionalPayment = v; }
    public LocalDate getCreatedAt()          { return createdAt; }
    public void      setCreatedAt(LocalDate v)        { createdAt = v; }
}
