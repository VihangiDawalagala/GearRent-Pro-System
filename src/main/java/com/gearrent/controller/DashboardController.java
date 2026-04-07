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
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label     lblActiveRentals, lblOverdueRentals, lblReservations, lblCustomers;
    @FXML private TableView<Rental> tblOverdue;
    @FXML private TableColumn<Rental,String> colOvRentalId, colOvCustomer, colOvEquipment,
                                              colOvBranch, colOvDue, colOvDaysOver, colOvContact;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colOvRentalId .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRentalId())));
        colOvCustomer .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        colOvEquipment.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipmentDisplay()));
        colOvBranch   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchName()));
        colOvDue      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        colOvDaysOver .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDaysOverdue() + " day(s)"));
        colOvContact  .setCellValueFactory(d -> {
            try {
                var cust = ServiceFactory.customers().getById(d.getValue().getCustomerId());
                return new SimpleStringProperty(cust != null ? cust.getContactNo() : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });

        // Highlight overdue rows in red
        tblOverdue.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Rental r, boolean empty) {
                super.updateItem(r, empty);
                getStyleClass().remove("row-overdue");
                if (!empty && r != null) getStyleClass().add("row-overdue");
            }
        });
    }

    private void loadData() {
        int branchId = SessionManager.getInstance().getScopedBranchId();
        try {
            List<Rental> overdue = ServiceFactory.rentals().getOverdue();
            if (branchId > 0) overdue.removeIf(r -> r.getBranchId() != branchId);

            tblOverdue.setItems(FXCollections.observableArrayList(overdue));
            lblOverdueRentals.setText(String.valueOf(overdue.size()));

            List<Rental> active = ServiceFactory.rentals().getFiltered(branchId, "Active", null, null);
            lblActiveRentals.setText(String.valueOf(active.size()));

            var reservations = ServiceFactory.reservations().getActive(branchId);
            lblReservations.setText(String.valueOf(reservations.size()));

            var customers = ServiceFactory.customers().getAll();
            lblCustomers.setText(String.valueOf(customers.size()));

        } catch (Exception e) {
            UIHelper.showError("Dashboard load error: " + e.getMessage());
        }
    }
}
