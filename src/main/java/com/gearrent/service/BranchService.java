package com.gearrent.service;

import com.gearrent.dao.BranchDAO;
import com.gearrent.entity.Branch;
import java.sql.SQLException;
import java.util.List;

public class BranchService {
    private final BranchDAO dao = new BranchDAO();

    public List<Branch> getAll()        throws SQLException { return dao.findAll(); }
    public List<Branch> getAllActive()   throws SQLException { return dao.findAllActive(); }
    public Branch        getById(int id) throws SQLException { return dao.findById(id); }

    public void save(Branch b) throws Exception {
        if (b.getBranchCode() == null || b.getBranchCode().isBlank()) throw new Exception("Branch code is required.");
        if (b.getName()       == null || b.getName().isBlank())       throw new Exception("Branch name is required.");
        if (dao.codeExists(b.getBranchCode(), b.getBranchId()))
            throw new Exception("Branch code '" + b.getBranchCode() + "' is already in use.");
        if (b.getBranchId() == 0) dao.insert(b);
        else dao.update(b);
    }

    public void delete(int id) throws Exception { dao.delete(id); }
}
