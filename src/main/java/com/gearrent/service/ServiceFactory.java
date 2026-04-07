package com.gearrent.service;

/**
 * Facade that provides singleton instances of every service.
 * Controllers use: ServiceFactory.branches().save(b)
 */
public class ServiceFactory {

    private static BranchService          branches;
    private static EquipmentCategoryService categories;
    private static EquipmentService        equipment;
    private static CustomerService         customers;
    private static UserService             users;
    private static ReservationService      reservations;
    private static RentalService           rentals;
    private static ReturnService           returns;
    private static ReportService           reports;
    private static MembershipService       memberships;
    private static AuthService             auth;

    public static BranchService           branches()     { if (branches     == null) branches     = new BranchService();    return branches; }
    public static EquipmentCategoryService categories()  { if (categories   == null) categories   = new EquipmentCategoryService(); return categories; }
    public static EquipmentService        equipment()    { if (equipment    == null) equipment    = new EquipmentService();  return equipment; }
    public static CustomerService         customers()    { if (customers    == null) customers    = new CustomerService();   return customers; }
    public static UserService             users()        { if (users        == null) users        = new UserService();       return users; }
    public static ReservationService      reservations() { if (reservations == null) reservations = new ReservationService(); return reservations; }
    public static RentalService           rentals()      { if (rentals      == null) rentals      = new RentalService();     return rentals; }
    public static ReturnService           returns()      { if (returns      == null) returns      = new ReturnService();     return returns; }
    public static ReportService           reports()      { if (reports      == null) reports      = new ReportService();     return reports; }
    public static MembershipService       memberships()  { if (memberships  == null) memberships  = new MembershipService(); return memberships; }
    public static AuthService             auth()         { if (auth         == null) auth         = new AuthService();       return auth; }
}
