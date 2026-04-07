package com.gearrent.controller;

import com.gearrent.entity.*;
import com.gearrent.entity.Reservation.Status;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class ReservationController implements Initializable {

    @FXML private TableView<Reservation> tblRes;
    @FXML private TableColumn<Reservation,String> colId, colEquip, colCust, colBranch,
                                                   colStart, colEnd, colStatus;

    @FXML private ComboBox<Branch>     cmbBranch;
    @FXML private ComboBox<Equipment>  cmbEquipment;
    @FXML private ComboBox<Customer>   cmbCustomer;
    @FXML private DatePicker           dpStart, dpEnd;

    private Reservation selected;
    private final int scopedBranch = SessionManager.getInstance().getScopedBranchId();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadCombos();
        refresh();
        tblRes.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> selected = n);

        // When branch/dates change, reload available equipment
        cmbBranch.valueProperty().addListener((obs,o,n) -> reloadEquipment());
        dpStart.valueProperty().addListener((obs,o,n) -> reloadEquipment());
        dpEnd.valueProperty().addListener((obs,o,n) -> reloadEquipment());
    }

    private void setupTable() {
        colId    .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getReservationId())));
        colEquip .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipmentDisplay()));
        colCust  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        colBranch.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchName()));
        colStart .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));
        colEnd   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
    }

    private void loadCombos() {
        try {
            List<Branch> branches = scopedBranch > 0
                ? List.of(ServiceFactory.branches().getById(scopedBranch))
                : ServiceFactory.branches().getAllActive();
            cmbBranch.setItems(FXCollections.observableArrayList(branches));
            if (!branches.isEmpty()) cmbBranch.getSelectionModel().selectFirst();

            cmbCustomer.setItems(FXCollections.observableArrayList(ServiceFactory.customers().getAll()));

            // Default dates
            dpStart.setValue(LocalDate.now().plusDays(1));
            dpEnd.setValue(LocalDate.now().plusDays(3));
            reloadEquipment();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void reloadEquipment() {
        Branch b = cmbBranch.getValue();
        LocalDate s = dpStart.getValue(), e = dpEnd.getValue();
        if (b == null || s == null || e == null || e.isBefore(s)) return;
        try {
            List<Equipment> equip = ServiceFactory.equipment().getAvailableForPeriod(b.getBranchId(), s, e);
            cmbEquipment.setItems(FXCollections.observableArrayList(equip));
        } catch (Exception ex) { UIHelper.showError(ex.getMessage()); }
    }

    private void refresh() {
        try { tblRes.setItems(FXCollections.observableArrayList(
            ServiceFactory.reservations().getAll(scopedBranch))); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleCreate() {
        try {
            Branch b   = cmbBranch.getValue();
            Equipment e = cmbEquipment.getValue();
            Customer c  = cmbCustomer.getValue();
            LocalDate s = dpStart.getValue(), en = dpEnd.getValue();

            if (b == null)  throw new Exception("Select a branch.");
            if (e == null)  throw new Exception("Select equipment.");
            if (c == null)  throw new Exception("Select a customer.");
            if (s == null || en == null) throw new Exception("Select date range.");

            ServiceFactory.reservations().create(e.getEquipmentId(), c.getCustomerId(), b.getBranchId(), s, en);
            UIHelper.showInfo("Reservation created successfully.");
            refresh();
        } catch (Exception ex) { UIHelper.showError(ex.getMessage()); }
    }

    @FXML private void handleCancel() {
        if (selected == null || selected.getStatus() != Status.Active) {
            UIHelper.showError("Select an active reservation."); return;
        }
        if (!UIHelper.confirm("Cancel reservation #" + selected.getReservationId() + "?")) return;
        try { ServiceFactory.reservations().cancel(selected.getReservationId()); refresh(); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleConvert() {
        if (selected == null || selected.getStatus() != Status.Active) {
            UIHelper.showError("Select an active reservation to convert."); return;
        }
        if (!UIHelper.confirm("Convert reservation #" + selected.getReservationId() + " to a rental?")) return;
        try {
            var rental = ServiceFactory.rentals().convertReservation(selected.getReservationId(),
                com.gearrent.entity.Rental.PaymentStatus.Paid);
            UIHelper.showInfo(String.format(
                "Rental #%d created!\nRental Amount: %s\nFinal Payable (incl. deposit): %s",
                rental.getRentalId(),
                UIHelper.fmt(rental.getRentalAmount()),
                UIHelper.fmt(rental.getFinalPayable())));
            refresh();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }
}
