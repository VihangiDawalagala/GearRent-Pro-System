package com.gearrent.entity;

import java.time.LocalDate;

public class Rental {

    public enum PaymentStatus { Paid, PartiallyPaid, Unpaid;
        public static PaymentStatus fromString(String s) {
            if (s == null) return Unpaid;
            switch (s.replace(" ", "")) {
                case "Paid": return Paid;
                case "PartiallyPaid": return PartiallyPaid;
                default: return Unpaid;
            }
        }
        @Override public String toString() {
            return this == PartiallyPaid ? "Partially Paid" : name();
        }
    }

    public enum RentalStatus { Active, Returned, Overdue, Cancelled;
        public static RentalStatus fromString(String s) {
            if (s == null) return Active;
            for (RentalStatus rs : values()) if (rs.name().equalsIgnoreCase(s)) return rs;
            return Active;
        }
    }

    private int           rentalId;
    private String        equipmentId;
    private String        equipmentDisplay;
    private int           customerId;
    private String        customerName;
    private int           branchId;
    private String        branchName;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private double        rentalAmount;
    private double        securityDeposit;
    private double        membershipDiscount;
    private double        longRentalDiscount;
    private double        finalPayable;
    private PaymentStatus paymentStatus;
    private RentalStatus  rentalStatus;
    private Integer       reservationId;
    private LocalDate     createdAt;

    public Rental() {}

    // ── Getters & Setters ──────────────────────────────────
    public int           getRentalId()          { return rentalId; }
    public void          setRentalId(int v)               { rentalId = v; }
    public String        getEquipmentId()        { return equipmentId; }
    public void          setEquipmentId(String v)         { equipmentId = v; }
    public String        getEquipmentDisplay()   { return equipmentDisplay; }
    public void          setEquipmentDisplay(String v)    { equipmentDisplay = v; }
    public int           getCustomerId()         { return customerId; }
    public void          setCustomerId(int v)              { customerId = v; }
    public String        getCustomerName()       { return customerName; }
    public void          setCustomerName(String v)        { customerName = v; }
    public int           getBranchId()           { return branchId; }
    public void          setBranchId(int v)                { branchId = v; }
    public String        getBranchName()         { return branchName; }
    public void          setBranchName(String v)           { branchName = v; }
    public LocalDate     getStartDate()          { return startDate; }
    public void          setStartDate(LocalDate v)         { startDate = v; }
    public LocalDate     getEndDate()            { return endDate; }
    public void          setEndDate(LocalDate v)           { endDate = v; }
    public double        getRentalAmount()       { return rentalAmount; }
    public void          setRentalAmount(double v)         { rentalAmount = v; }
    public double        getSecurityDeposit()    { return securityDeposit; }
    public void          setSecurityDeposit(double v)      { securityDeposit = v; }
    public double        getMembershipDiscount() { return membershipDiscount; }
    public void          setMembershipDiscount(double v)   { membershipDiscount = v; }
    public double        getLongRentalDiscount() { return longRentalDiscount; }
    public void          setLongRentalDiscount(double v)   { longRentalDiscount = v; }
    public double        getFinalPayable()       { return finalPayable; }
    public void          setFinalPayable(double v)         { finalPayable = v; }
    public PaymentStatus getPaymentStatus()      { return paymentStatus; }
    public void          setPaymentStatus(PaymentStatus v) { paymentStatus = v; }
    public RentalStatus  getRentalStatus()       { return rentalStatus; }
    public void          setRentalStatus(RentalStatus v)   { rentalStatus = v; }
    public Integer       getReservationId()      { return reservationId; }
    public void          setReservationId(Integer v)       { reservationId = v; }
    public LocalDate     getCreatedAt()          { return createdAt; }
    public void          setCreatedAt(LocalDate v)         { createdAt = v; }

    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return startDate.until(endDate).getDays() + 1;
    }

    public long getDaysOverdue() {
        if (endDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) return endDate.until(today).getDays();
        return 0;
    }
}
