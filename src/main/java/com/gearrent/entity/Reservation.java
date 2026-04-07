package com.gearrent.entity;

import java.time.LocalDate;

public class Reservation {

    public enum Status { Active, Converted, Cancelled }

    private int       reservationId;
    private String    equipmentId;
    private String    equipmentDisplay;
    private int       customerId;
    private String    customerName;
    private int       branchId;
    private String    branchName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status    status;
    private LocalDate createdAt;

    public Reservation() {}

    public int       getReservationId()    { return reservationId; }
    public void      setReservationId(int v)        { reservationId = v; }
    public String    getEquipmentId()      { return equipmentId; }
    public void      setEquipmentId(String v)       { equipmentId = v; }
    public String    getEquipmentDisplay() { return equipmentDisplay; }
    public void      setEquipmentDisplay(String v)  { equipmentDisplay = v; }
    public int       getCustomerId()       { return customerId; }
    public void      setCustomerId(int v)            { customerId = v; }
    public String    getCustomerName()     { return customerName; }
    public void      setCustomerName(String v)       { customerName = v; }
    public int       getBranchId()         { return branchId; }
    public void      setBranchId(int v)              { branchId = v; }
    public String    getBranchName()       { return branchName; }
    public void      setBranchName(String v)         { branchName = v; }
    public LocalDate getStartDate()        { return startDate; }
    public void      setStartDate(LocalDate v)       { startDate = v; }
    public LocalDate getEndDate()          { return endDate; }
    public void      setEndDate(LocalDate v)         { endDate = v; }
    public Status    getStatus()           { return status; }
    public void      setStatus(Status v)             { status = v; }
    public LocalDate getCreatedAt()        { return createdAt; }
    public void      setCreatedAt(LocalDate v)       { createdAt = v; }

    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return startDate.until(endDate).getDays() + 1;
    }
}
