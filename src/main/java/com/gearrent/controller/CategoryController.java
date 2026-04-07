package com.gearrent.controller;

import com.gearrent.entity.EquipmentCategory;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    @FXML private TableView<EquipmentCategory> tblCats;
    @FXML private TableColumn<EquipmentCategory,String> colName, colFactor, colWE, colLateFee, colActive;
    @FXML private TextField txtName, txtDesc, txtFactor, txtWE, txtLateFee;
    @FXML private CheckBox chkActive;
    @FXML private Label lblStatus;

    private EquipmentCategory selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boolean allowed = SessionManager.getInstance().isAdmin()
                       || SessionManager.getInstance().isBranchManager();
        if (!allowed) { lblStatus.setText("Access denied."); return; }
        setupTable();
        refresh();
        tblCats.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> populate(n));
    }

    private void setupTable() {
        colName   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colFactor .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBasePriceFactor())));
        colWE     .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getWeekendMultiplier())));
        colLateFee.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getDefaultLateFee())));
        colActive .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Yes" : "No"));
    }

    private void refresh() {
        try { tblCats.setItems(FXCollections.observableArrayList(ServiceFactory.categories().getAll())); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void populate(EquipmentCategory c) {
        if (c == null) { clearForm(); return; }
        selected = c;
        txtName.setText(c.getName());
        txtDesc.setText(c.getDescription());
        txtFactor.setText(String.valueOf(c.getBasePriceFactor()));
        txtWE.setText(String.valueOf(c.getWeekendMultiplier()));
        txtLateFee.setText(String.valueOf(c.getDefaultLateFee()));
        chkActive.setSelected(c.isActive());
    }

    @FXML private void handleSave() {
        try {
            EquipmentCategory c = selected != null ? selected : new EquipmentCategory();
            c.setName(txtName.getText().trim());
            c.setDescription(txtDesc.getText().trim());
            c.setBasePriceFactor(Double.parseDouble(txtFactor.getText().trim()));
            c.setWeekendMultiplier(Double.parseDouble(txtWE.getText().trim()));
            c.setDefaultLateFee(Double.parseDouble(txtLateFee.getText().trim()));
            c.setActive(chkActive.isSelected());
            ServiceFactory.categories().save(c);
            UIHelper.showInfo("Category saved.");
            clearForm(); refresh();
        } catch (NumberFormatException e) {
            UIHelper.showError("Factor, multiplier and late fee must be valid numbers.");
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selected == null) { UIHelper.showError("Select a category first."); return; }
        if (!UIHelper.confirm("Delete category '" + selected.getName() + "'?")) return;
        try { ServiceFactory.categories().delete(selected.getCategoryId()); clearForm(); refresh(); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        selected = null;
        txtName.clear(); txtDesc.clear();
        txtFactor.setText("1.0"); txtWE.setText("1.2"); txtLateFee.setText("500");
        chkActive.setSelected(true);
        tblCats.getSelectionModel().clearSelection();
    }
}
