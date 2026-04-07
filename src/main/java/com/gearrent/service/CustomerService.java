package com.gearrent.service;

import com.gearrent.dao.CustomerDAO;
import com.gearrent.entity.Customer;
import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO dao = new CustomerDAO();

    public List<Customer> getAll()             throws SQLException { return dao.findAll(); }
    public Customer getById(int id)            throws SQLException { return dao.findById(id); }
    public List<Customer> search(String kw)    throws SQLException { return dao.search(kw); }
    public double getTotalDeposit(int custId)  throws SQLException { return dao.getTotalActiveDeposit(custId); }

    public void save(Customer c) throws Exception {
        if (c.getName()        == null || c.getName().isBlank())        throw new Exception("Customer name is required.");
        if (c.getNicPassport() == null || c.getNicPassport().isBlank()) throw new Exception("NIC/Passport is required.");
        if (c.getContactNo()   == null || c.getContactNo().isBlank())   throw new Exception("Contact number is required.");
        if (c.getMembershipLevel() == null)                             throw new Exception("Membership level is required.");
        if (dao.nicExists(c.getNicPassport(), c.getCustomerId()))
            throw new Exception("NIC/Passport '" + c.getNicPassport() + "' already registered.");
        if (c.getCustomerId() == 0) dao.insert(c);
        else dao.update(c);
    }

    public void delete(int id) throws Exception { dao.delete(id); }
}
