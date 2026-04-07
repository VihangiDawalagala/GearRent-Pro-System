package com.gearrent.service;

import com.gearrent.dao.EquipmentDAO;
import com.gearrent.entity.Equipment;
import com.gearrent.entity.Equipment.Status;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EquipmentService {
    private final EquipmentDAO dao = new EquipmentDAO();

    public List<Equipment> getAll() throws SQLException { return dao.findAll(); }
    public List<Equipment> getByBranch(int branchId) throws SQLException { return dao.findByBranch(branchId); }
    public Equipment       getById(String id) throws SQLException { return dao.findById(id); }

    public List<Equipment> filter(int branchId, int categoryId, String status, String keyword)
            throws SQLException { return dao.findFiltered(branchId, categoryId, status, keyword); }

    public List<Equipment> getAvailableForPeriod(int branchId, LocalDate s, LocalDate e)
            throws SQLException { return dao.findAvailableForPeriod(branchId, s, e); }

    public void save(Equipment eq) throws Exception {
        if (eq.getEquipmentId() == null || eq.getEquipmentId().isBlank()) throw new Exception("Equipment ID is required.");
        if (eq.getBrand() == null || eq.getBrand().isBlank())  throw new Exception("Brand is required.");
        if (eq.getModel() == null || eq.getModel().isBlank())  throw new Exception("Model is required.");
        if (eq.getDailyBasePrice() <= 0) throw new Exception("Daily base price must be > 0.");
        if (eq.getSecurityDeposit() < 0) throw new Exception("Security deposit cannot be negative.");
        if (eq.getPurchaseYear() < 2000 || eq.getPurchaseYear() > LocalDate.now().getYear())
            throw new Exception("Purchase year is out of range.");
        if (dao.idExists(eq.getEquipmentId())) dao.update(eq);
        else dao.insert(eq);
    }

    public void updateStatus(String id, Status status) throws SQLException { dao.updateStatus(id, status); }
    public void delete(String id) throws Exception { dao.delete(id); }
}
