package com.gearrent.controller;

import com.gearrent.entity.*;
import com.gearrent.entity.Rental.PaymentStatus;
import com.gearrent.entity.Rental.RentalStatus;
import com.gearrent.service.RentalCalculatorService;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class RentalController implements Initializable {

    // ── List side ─────────────────────────────────────────
    @FXML private TableView<Rental> tblRentals;
    @FXML private TableColumn<Rental,String> colId, colEquip, colCust, colBranch,
                                              colStart, colEnd, colFinal, colPayment, colStatus;
    @FXML private ComboBox<String>    cmbFilterStatus;
    @FXML private DatePicker          dpFilterFrom, dpFilterTo;

    // ── Create form side ────────────────────────────────────
    @FXML private ComboBox<Branch>        cmbBranch;
    @FXML private ComboBox<Customer>      cmbCustomer;
    @FXML private ComboBox<Equipment>     cmbEquipment;
    @FXML private DatePicker              dpStart, dpEnd;
    @FXML private ComboBox<PaymentStatus> cmbPayment;
    @FXML private Label                   lblRentalAmt, lblLongDisc, lblMemDisc, lblDeposit, lblFinal;

    private final int scopedBranch = SessionManager.getInstance().getScopedBranchId();
    private final RentalCalculatorService calc = new RentalCalculatorService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilterCombos();
        loadFormCombos();
        refresh();

        // Auto-preview cost when fields change
        cmbEquipment.valueProperty().addListener((o,v,n) -> previewCost());
        cmbCustomer .valueProperty().addListener((o,v,n) -> previewCost());
        dpStart.valueProperty().addListener((o,v,n) -> { reloadEquipment(); previewCost(); });
        dpEnd  .valueProperty().addListener((o,v,n) -> { reloadEquipment(); previewCost(); });
        cmbBranch.valueProperty().addListener((o,v,n) -> reloadEquipment());
    }

    private void setupTable() {
        colId     .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRentalId())));
        colEquip  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipmentDisplay()));
        colCust   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        colBranch .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchName()));
        colStart  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));
        colEnd    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        colFinal  .setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getFinalPayable())));
        colPayment.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentStatus().toString()));
        colStatus .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRentalStatus().name()));

        tblRentals.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Rental r, boolean empty) {
                super.updateItem(r, empty);
                getStyleClass().removeAll("row-overdue","row-returned");
                if (!empty && r != null) {
                    if (r.getRentalStatus() == RentalStatus.Overdue) getStyleClass().add("row-overdue");
                    else if (r.getRentalStatus() == RentalStatus.Returned) getStyleClass().add("row-returned");
                }
            }
        });
    }

    private void setupFilterCombos() {
        cmbFilterStatus.setItems(FXCollections.observableArrayList(
            "", "Active", "Returned", "Overdue", "Cancelled"));
        cmbFilterStatus.getSelectionModel().selectFirst();
        dpFilterFrom.setValue(LocalDate.now().minusMonths(1));
    }

    private void loadFormCombos() {
        try {
            List<Branch> branches = scopedBranch > 0
                ? List.of(ServiceFactory.branches().getById(scopedBranch))
                : ServiceFactory.branches().getAllActive();
            cmbBranch.setItems(FXCollections.observableArrayList(branches));
            if (!branches.isEmpty()) cmbBranch.getSelectionModel().selectFirst();

            cmbCustomer.setItems(FXCollections.observableArrayList(ServiceFactory.customers().getAll()));
            cmbPayment.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
            cmbPayment.getSelectionModel().select(PaymentStatus.Paid);

            dpStart.setValue(LocalDate.now());
            dpEnd.setValue(LocalDate.now().plusDays(2));
            reloadEquipment();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void reloadEquipment() {
        Branch b = cmbBranch.getValue();
        LocalDate s = dpStart.getValue(), e = dpEnd.getValue();
        if (b == null || s == null || e == null || e.isBefore(s)) return;
        try {
            List<Equipment> list = ServiceFactory.equipment().getAvailableForPeriod(b.getBranchId(), s, e);
            cmbEquipment.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ex) { UIHelper.showError(ex.getMessage()); }
    }

    private void previewCost() {
        Equipment eq = cmbEquipment.getValue();
        Customer cust = cmbCustomer.getValue();
        LocalDate s = dpStart.getValue(), e = dpEnd.getValue();
        if (eq == null || cust == null || s == null || e == null || e.isBefore(s)) {
            clearCostLabels(); return;
        }
        try {
            EquipmentCategory cat = ServiceFactory.categories().getById(eq.getCategoryId());
            var rule = new com.gearrent.dao.PricingRuleDAO().findFirst();
            double[] bd = calc.buildRentalCostBreakdown(
                eq.getDailyBasePrice(), cat, s, e, eq.getSecurityDeposit(),
                cust.getMembershipLevel().name(), rule);
            lblRentalAmt.setText(UIHelper.fmt(bd[0]));
            lblLongDisc .setText("− " + UIHelper.fmt(bd[1]));
            lblMemDisc  .setText("− " + UIHelper.fmt(bd[2]));
            lblDeposit  .setText("+ " + UIHelper.fmt(eq.getSecurityDeposit()));
            lblFinal    .setText(UIHelper.fmt(bd[3]));
        } catch (Exception ex) { clearCostLabels(); }
    }

    private void clearCostLabels() {
        lblRentalAmt.setText("—"); lblLongDisc.setText("—");
        lblMemDisc.setText("—"); lblDeposit.setText("—"); lblFinal.setText("—");
    }

    @FXML private void handleFilter() { refresh(); }

    private void refresh() {
        try {
            String status = cmbFilterStatus.getValue();
            tblRentals.setItems(FXCollections.observableArrayList(
                ServiceFactory.rentals().getFiltered(scopedBranch,
                    status == null || status.isEmpty() ? null : status,
                    dpFilterFrom.getValue(), dpFilterTo.getValue())));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleCreate() {
        try {
            Branch b = cmbBranch.getValue();    if (b == null)  throw new Exception("Select a branch.");
            Equipment eq = cmbEquipment.getValue(); if (eq == null) throw new Exception("Select equipment.");
            Customer c = cmbCustomer.getValue(); if (c == null)  throw new Exception("Select a customer.");
            LocalDate s = dpStart.getValue(), e = dpEnd.getValue();
            if (s == null || e == null) throw new Exception("Select dates.");

            Rental r = ServiceFactory.rentals().createRental(
                eq.getEquipmentId(), c.getCustomerId(), b.getBranchId(),
                s, e, cmbPayment.getValue());

            UIHelper.showInfo(String.format(
                "Rental #%d created!\n\nRental Amt:    %s\nLong Discount: %s\nMbr Discount:  %s\nDeposit:       %s\n─────────────────\nTotal Payable: %s",
                r.getRentalId(),
                UIHelper.fmt(r.getRentalAmount()),
                UIHelper.fmt(r.getLongRentalDiscount()),
                UIHelper.fmt(r.getMembershipDiscount()),
                UIHelper.fmt(r.getSecurityDeposit()),
                UIHelper.fmt(r.getFinalPayable())));
            refresh(); clearCostLabels(); reloadEquipment();
        } catch (Exception ex) { UIHelper.showError(ex.getMessage()); }
    }

    @FXML private void handleCancel() {
        Rental r = tblRentals.getSelectionModel().getSelectedItem();
        if (r == null) { UIHelper.showError("Select a rental."); return; }
        if (!UIHelper.confirm("Cancel rental #" + r.getRentalId() + "?")) return;
        try { ServiceFactory.rentals().cancel(r.getRentalId()); refresh(); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }
}
