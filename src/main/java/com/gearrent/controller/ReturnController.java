package com.gearrent.controller;

import com.gearrent.entity.Rental;
import com.gearrent.entity.RentalReturn;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReturnController implements Initializable {

    @FXML private TableView<Rental> tblActive;
    @FXML private TableColumn<Rental,String> colId, colEquip, colCust, colBranch,
                                              colStart, colEnd, colDays, colDeposit;

    @FXML private Label      lblRentalInfo;
    @FXML private DatePicker dpActualReturn;
    @FXML private CheckBox   chkDamaged;
    @FXML private TextField  txtDamageDesc, txtDamageCharge;
    @FXML private Label      lblLateFee, lblDamageTotal, lblDepositUsed, lblRefund, lblExtra;

    private Rental selectedRental;
    private final int scopedBranch = SessionManager.getInstance().getScopedBranchId();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        refresh();
        tblActive.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> selectRental(n));
        chkDamaged.selectedProperty().addListener((obs,o,n) -> {
            txtDamageDesc.setDisable(!n);
            txtDamageCharge.setDisable(!n);
        });
        txtDamageDesc.setDisable(true);
        txtDamageCharge.setDisable(true);
        dpActualReturn.setValue(LocalDate.now());

        dpActualReturn.valueProperty().addListener((obs,o,n) -> previewCharges());
        txtDamageCharge.textProperty().addListener((obs,o,n) -> previewCharges());
    }

    private void setupTable() {
        colId     .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRentalId())));
        colEquip  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipmentDisplay()));
        colCust   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        colBranch .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchName()));
        colStart  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));
        colEnd    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        colDays   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDaysOverdue() > 0
            ? "⚠ " + d.getValue().getDaysOverdue() + " overdue" : "On time"));
        colDeposit.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getSecurityDeposit())));
    }

    private void refresh() {
        try {
            var list = ServiceFactory.rentals().getFiltered(scopedBranch, "Active", null, null);
            var overdue = ServiceFactory.rentals().getOverdue();
            list.removeIf(r -> overdue.stream().anyMatch(o -> o.getRentalId() == r.getRentalId()));
            list.addAll(overdue);
            if (scopedBranch > 0) list.removeIf(r -> r.getBranchId() != scopedBranch);
            tblActive.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void selectRental(Rental r) {
        selectedRental = r;
        if (r == null) { lblRentalInfo.setText("Select a rental from the table."); return; }
        lblRentalInfo.setText(String.format(
            "Rental #%d | %s | %s – %s | Deposit: %s",
            r.getRentalId(), r.getEquipmentDisplay(),
            r.getStartDate(), r.getEndDate(), UIHelper.fmt(r.getSecurityDeposit())));
        previewCharges();
    }

    private void previewCharges() {
        if (selectedRental == null) return;
        try {
            LocalDate ret = dpActualReturn.getValue();
            if (ret == null) return;

            double lateFee = 0;
            long over = selectedRental.getEndDate().until(ret).getDays();
            if (over > 0) {
                var cat = ServiceFactory.categories().getById(
                    ServiceFactory.equipment().getById(selectedRental.getEquipmentId()).getCategoryId());
                lateFee = over * cat.getDefaultLateFee();
            }

            double dmg = chkDamaged.isSelected() ? parseDouble(txtDamageCharge.getText()) : 0;
            double total   = lateFee + dmg;
            double deposit = selectedRental.getSecurityDeposit();
            double used    = Math.min(deposit, total);
            double refund  = Math.max(0, deposit - total);
            double extra   = Math.max(0, total - deposit);

            lblLateFee   .setText(UIHelper.fmt(lateFee));
            lblDamageTotal.setText(UIHelper.fmt(total));
            lblDepositUsed.setText(UIHelper.fmt(used));
            lblRefund    .setText(UIHelper.fmt(refund));
            lblExtra     .setText(UIHelper.fmt(extra));
        } catch (Exception ignored) {}
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }

    @FXML private void handleProcessReturn() {
        if (selectedRental == null) { UIHelper.showError("Select a rental."); return; }
        try {
            LocalDate ret = dpActualReturn.getValue();
            if (ret == null) throw new Exception("Select actual return date.");
            boolean dmg = chkDamaged.isSelected();
            double charge = dmg ? parseDouble(txtDamageCharge.getText()) : 0;
            if (dmg && txtDamageDesc.getText().isBlank())
                throw new Exception("Describe the damage.");

            RentalReturn result = ServiceFactory.returns().processReturn(
                selectedRental.getRentalId(), ret, dmg, txtDamageDesc.getText(), charge);

            String msg = String.format(
                "Return processed for Rental #%d\n\nLate Fee:    %s\nDamage:      %s\nTotal Charges: %s\nDeposit Used:  %s\n─────────────────────────\n%s",
                selectedRental.getRentalId(),
                UIHelper.fmt(result.getLateFee()),
                UIHelper.fmt(result.getDamageCharge()),
                UIHelper.fmt(result.getTotalCharges()),
                UIHelper.fmt(result.getDepositUsed()),
                result.getRefundAmount() > 0
                    ? "✅ Refund to customer: " + UIHelper.fmt(result.getRefundAmount())
                    : "⚠ Additional payment required: " + UIHelper.fmt(result.getAdditionalPayment()));
            UIHelper.showInfo(msg);
            refresh(); clearForm();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void clearForm() {
        selectedRental = null;
        lblRentalInfo.setText("");
        chkDamaged.setSelected(false);
        txtDamageDesc.clear(); txtDamageCharge.clear();
        dpActualReturn.setValue(LocalDate.now());
        lblLateFee.setText("—"); lblDamageTotal.setText("—");
        lblDepositUsed.setText("—"); lblRefund.setText("—"); lblExtra.setText("—");
        tblActive.getSelectionModel().clearSelection();
    }
}
