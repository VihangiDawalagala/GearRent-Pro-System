package com.gearrent.entity;

public class Customer {

    public enum MembershipLevel { Regular, Silver, Gold }

    private int             customerId;
    private String          name;
    private String          nicPassport;
    private String          contactNo;
    private String          email;
    private String          address;
    private MembershipLevel membershipLevel;

    public Customer() {}

    public Customer(int customerId, String name, String nicPassport,
                    String contactNo, String email, String address,
                    MembershipLevel membershipLevel) {
        this.customerId      = customerId;
        this.name            = name;
        this.nicPassport     = nicPassport;
        this.contactNo       = contactNo;
        this.email           = email;
        this.address         = address;
        this.membershipLevel = membershipLevel;
    }

    public int             getCustomerId()      { return customerId; }
    public void            setCustomerId(int v)           { customerId = v; }
    public String          getName()            { return name; }
    public void            setName(String v)              { name = v; }
    public String          getNicPassport()     { return nicPassport; }
    public void            setNicPassport(String v)       { nicPassport = v; }
    public String          getContactNo()       { return contactNo; }
    public void            setContactNo(String v)         { contactNo = v; }
    public String          getEmail()           { return email; }
    public void            setEmail(String v)             { email = v; }
    public String          getAddress()         { return address; }
    public void            setAddress(String v)           { address = v; }
    public MembershipLevel getMembershipLevel() { return membershipLevel; }
    public void            setMembershipLevel(MembershipLevel v) { membershipLevel = v; }

    @Override public String toString() { return name + " [" + nicPassport + "]"; }
}
