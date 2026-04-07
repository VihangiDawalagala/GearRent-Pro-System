package com.gearrent.controller;

import com.gearrent.entity.Branch;
import com.gearrent.entity.SystemUser;
import com.gearrent.entity.SystemUser.Role;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private TableView<SystemUser> tblUsers;
    @FXML private TableColumn<SystemUser,String> colId, colUsername, colFullName,
                                                  colRole, colBranch, colActive;

    @FXML private TextField             txtUsername, txtFullName, txtPassword;
    @FXML private ComboBox<Role>        cmbRole;
    @FXML private ComboBox<Branch>      cmbBranch;
    @FXML private CheckBox              chkActive;
    @FXML private Label                 lblStatus;

    private SystemUser selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!SessionManager.getInstance().isAdmin()) {
            lblStatus.setText("Access denied – Admin only.");
            return;
        }
        setupTable();
        loadCombos();
        refresh();
        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> populate(n));
        // Branch combo visibility based on role
        cmbRole.valueProperty().addListener((obs,o,n) -> {
            boolean needsBranch = n != null && n != Role.Admin;
            cmbBranch.setDisable(!needsBranch);
            if (!needsBranch) cmbBranch.getSelectionModel().clearSelection();
        });
    }

    private void setupTable() {
        colId      .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUserId())));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colRole    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().getDisplay()));
        colBranch  .setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getBranchName() != null ? d.getValue().getBranchName() : "—"));
        colActive  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Yes" : "No"));
    }

    private void loadCombos() {
        cmbRole.setItems(FXCollections.observableArrayList(Role.values()));
        cmbRole.getSelectionModel().select(Role.Staff);
        try {
            List<Branch> branches = ServiceFactory.branches().getAllActive();
            cmbBranch.setItems(FXCollections.observableArrayList(branches));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void refresh() {
        try { tblUsers.setItems(FXCollections.observableArrayList(ServiceFactory.users().getAll())); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void populate(SystemUser u) {
        if (u == null) { clearForm(); return; }
        selected = u;
        txtUsername.setText(u.getUsername());
        txtFullName.setText(u.getFullName());
        txtPassword.clear(); // never show existing password
        cmbRole.setValue(u.getRole());
        chkActive.setSelected(u.isActive());
        if (u.getBranchId() > 0) {
            cmbBranch.getItems().stream()
                .filter(b -> b.getBranchId() == u.getBranchId())
                .findFirst().ifPresent(cmbBranch::setValue);
        } else {
            cmbBranch.getSelectionModel().clearSelection();
        }
    }

    @FXML private void handleSave() {
        try {
            SystemUser u = selected != null ? selected : new SystemUser();
            u.setUsername(txtUsername.getText().trim());
            u.setFullName(txtFullName.getText().trim());
            u.setRole(cmbRole.getValue());
            u.setActive(chkActive.isSelected());

            Branch b = cmbBranch.getValue();
            u.setBranchId(b != null ? b.getBranchId() : 0);

            String pw = txtPassword.getText().trim();
            ServiceFactory.users().save(u, pw.isEmpty() ? null : pw);
            UIHelper.showInfo("User saved successfully.");
            clearForm(); refresh();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selected == null) { UIHelper.showError("Select a user first."); return; }
        if (selected.getRole() == Role.Admin
                && selected.getUserId() == SessionManager.getInstance().getCurrentUser().getUserId()) {
            UIHelper.showError("You cannot delete your own admin account."); return;
        }
        if (!UIHelper.confirm("Delete user '" + selected.getUsername() + "'?")) return;
        try { ServiceFactory.users().delete(selected.getUserId()); clearForm(); refresh(); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        selected = null;
        txtUsername.clear(); txtFullName.clear(); txtPassword.clear();
        cmbRole.getSelectionModel().select(Role.Staff);
        cmbBranch.getSelectionModel().clearSelection();
        chkActive.setSelected(true);
        tblUsers.getSelectionModel().clearSelection();
    }
}
