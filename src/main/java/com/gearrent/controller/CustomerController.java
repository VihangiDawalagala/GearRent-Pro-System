package com.gearrent.controller;

import com.gearrent.entity.Customer;
import com.gearrent.entity.Customer.MembershipLevel;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML private TableView<Customer> tblCust;
    @FXML private TableColumn<Customer,String> colId, colName, colNic, colContact, colEmail, colLevel;
    @FXML private TextField txtSearch;

    @FXML private TextField txtName, txtNic, txtContact, txtEmail, txtAddress;
    @FXML private ComboBox<MembershipLevel> cmbLevel;
    @FXML private Label lblDeposit;

    private Customer selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        cmbLevel.setItems(FXCollections.observableArrayList(MembershipLevel.values()));
        cmbLevel.getSelectionModel().select(MembershipLevel.Regular);
        refresh();
        tblCust.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> populate(n));
    }

    private void setupTable() {
        colId     .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCustomerId())));
        colName   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colNic    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNicPassport()));
        colContact.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactNo()));
        colEmail  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colLevel  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMembershipLevel().name()));
    }

    @FXML private void handleSearch() {
        String kw = txtSearch.getText().trim();
        try {
            tblCust.setItems(FXCollections.observableArrayList(
                kw.isEmpty() ? ServiceFactory.customers().getAll()
                             : ServiceFactory.customers().search(kw)));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void refresh() { txtSearch.clear(); handleSearch(); }

    private void populate(Customer c) {
        if (c == null) { clearForm(); return; }
        selected = c;
        txtName.setText(c.getName());
        txtNic.setText(c.getNicPassport());
        txtContact.setText(c.getContactNo());
        txtEmail.setText(c.getEmail());
        txtAddress.setText(c.getAddress());
        cmbLevel.setValue(c.getMembershipLevel());
        try {
            double dep = ServiceFactory.customers().getTotalDeposit(c.getCustomerId());
            lblDeposit.setText("Active Deposits Held: " + UIHelper.fmt(dep));
        } catch (Exception ignored) {}
    }

    @FXML private void handleSave() {
        try {
            Customer c = selected != null ? selected : new Customer();
            c.setName(txtName.getText().trim());
            c.setNicPassport(txtNic.getText().trim());
            c.setContactNo(txtContact.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            c.setAddress(txtAddress.getText().trim());
            c.setMembershipLevel(cmbLevel.getValue() != null ? cmbLevel.getValue() : MembershipLevel.Regular);
            ServiceFactory.customers().save(c);
            UIHelper.showInfo("Customer saved.");
            clearForm(); refresh();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selected == null) { UIHelper.showError("Select a customer."); return; }
        if (!UIHelper.confirm("Delete customer '" + selected.getName() + "'?")) return;
        try { ServiceFactory.customers().delete(selected.getCustomerId()); clearForm(); refresh(); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    @FXML private void handleViewHistory() {
        if (selected == null) { UIHelper.showError("Select a customer first."); return; }
        try {
            var rentals = ServiceFactory.rentals().getByCustomer(selected.getCustomerId());
            StringBuilder sb = new StringBuilder("Rental History for " + selected.getName() + ":\n\n");
            if (rentals.isEmpty()) sb.append("No rental history.");
            else rentals.forEach(r -> sb.append(String.format(
                "  #%d | %s – %s | %s | %s\n",
                r.getRentalId(), r.getStartDate(), r.getEndDate(),
                r.getEquipmentDisplay(), r.getRentalStatus())));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rental History"); alert.setHeaderText(null);
            TextArea ta = new TextArea(sb.toString());
            ta.setEditable(false); ta.setPrefHeight(300);
            alert.getDialogPane().setContent(ta);
            alert.showAndWait();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void clearForm() {
        selected = null;
        txtName.clear(); txtNic.clear(); txtContact.clear(); txtEmail.clear(); txtAddress.clear();
        cmbLevel.getSelectionModel().select(MembershipLevel.Regular);
        lblDeposit.setText("");
        tblCust.getSelectionModel().clearSelection();
    }
}
