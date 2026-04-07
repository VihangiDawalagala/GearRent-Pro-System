package com.gearrent.controller;

import com.gearrent.entity.Rental;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class OverdueController implements Initializable {

    @FXML private TableView<Rental> tblOverdue;
    @FXML private TableColumn<Rental,String> colId, colEquip, colCust, colBranch,
                                              colDue, colDays, colContact, colEmail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        refresh();
    }

    private void setupTable() {
        colId    .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRentalId())));
        colEquip .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipmentDisplay()));
        colCust  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        colBranch.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchName()));
        colDue   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        colDays  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDaysOverdue() + " day(s)"));
        colContact.setCellValueFactory(d -> {
            try {
                var c = ServiceFactory.customers().getById(d.getValue().getCustomerId());
                return new SimpleStringProperty(c != null ? c.getContactNo() : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });
        colEmail.setCellValueFactory(d -> {
            try {
                var c = ServiceFactory.customers().getById(d.getValue().getCustomerId());
                return new SimpleStringProperty(c != null ? c.getEmail() : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });

        tblOverdue.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Rental r, boolean empty) {
                super.updateItem(r, empty);
                getStyleClass().remove("row-overdue");
                if (!empty && r != null) getStyleClass().add("row-overdue");
            }
        });
    }

    private void refresh() {
        try {
            int branch = SessionManager.getInstance().getScopedBranchId();
            var list = ServiceFactory.rentals().getOverdue();
            if (branch > 0) list.removeIf(r -> r.getBranchId() != branch);
            tblOverdue.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleRefresh() { refresh(); }
}
