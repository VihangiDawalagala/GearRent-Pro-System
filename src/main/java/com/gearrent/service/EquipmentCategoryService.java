package com.gearrent.service;

import com.gearrent.dao.EquipmentCategoryDAO;
import com.gearrent.entity.EquipmentCategory;
import java.sql.SQLException;
import java.util.List;

public class EquipmentCategoryService {
    private final EquipmentCategoryDAO dao = new EquipmentCategoryDAO();

    public List<EquipmentCategory> getAll()      throws SQLException { return dao.findAll(); }
    public List<EquipmentCategory> getAllActive() throws SQLException { return dao.findAllActive(); }
    public EquipmentCategory getById(int id)     throws SQLException { return dao.findById(id); }

    public void save(EquipmentCategory c) throws Exception {
        if (c.getName() == null || c.getName().isBlank())   throw new Exception("Category name is required.");
        if (c.getBasePriceFactor()  <= 0) throw new Exception("Base price factor must be > 0.");
        if (c.getWeekendMultiplier()<= 0) throw new Exception("Weekend multiplier must be > 0.");
        if (c.getDefaultLateFee()   < 0)  throw new Exception("Late fee cannot be negative.");
        if (c.getCategoryId() == 0) dao.insert(c);
        else dao.update(c);
    }

    public void delete(int id) throws Exception { dao.delete(id); }
}
