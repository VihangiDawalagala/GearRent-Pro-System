package com.gearrent.controller;

import com.gearrent.entity.Branch;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class BranchController implements Initializable {

    @FXML private TableView<Branch>              tblBranches;
    @FXML private TableColumn<Branch,String>     colCode, colName, colAddress, colContact, colActive;
    @FXML private TextField  txtCode, txtName, txtAddress, txtContact;
    @FXML private CheckBox   chkActive;
    @FXML private Button     btnSave, btnDelete, btnClear;
    @FXML private Label      lblStatus;

    private Branch selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Admin only
        if (!SessionManager.getInstance().isAdmin()) {
            lblStatus.setText("Access denied – Admin only.");
            return;
        }
        setupTable();
        refresh();
        tblBranches.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> populate(n));
    }

    private void setupTable() {
        colCode   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchCode()));
        colName   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
        colContact.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContact()));
        colActive .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Yes" : "No"));
    }

    private void refresh() {
        try {
            tblBranches.setItems(FXCollections.observableArrayList(
                ServiceFactory.branches().getAll()));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void populate(Branch b) {
        if (b == null) { clearForm(); return; }
        selected = b;
        txtCode.setText(b.getBranchCode());
        txtName.setText(b.getName());
        txtAddress.setText(b.getAddress());
        txtContact.setText(b.getContact());
        chkActive.setSelected(b.isActive());
    }

    @FXML private void handleSave() {
        try {
            Branch b = selected != null ? selected : new Branch();
            b.setBranchCode(txtCode.getText().trim().toUpperCase());
            b.setName(txtName.getText().trim());
            b.setAddress(txtAddress.getText().trim());
            b.setContact(txtContact.getText().trim());
            b.setActive(chkActive.isSelected());
            ServiceFactory.branches().save(b);
            UIHelper.showInfo("Branch saved successfully.");
            clearForm(); refresh();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selected == null) { UIHelper.showError("Select a branch first."); return; }
        if (!UIHelper.confirm("Delete branch '" + selected.getName() + "'?")) return;
        try {
            ServiceFactory.branches().delete(selected.getBranchId());
            clearForm(); refresh();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        selected = null;
        txtCode.clear(); txtName.clear(); txtAddress.clear(); txtContact.clear();
        chkActive.setSelected(true);
        tblBranches.getSelectionModel().clearSelection();
    }
}
