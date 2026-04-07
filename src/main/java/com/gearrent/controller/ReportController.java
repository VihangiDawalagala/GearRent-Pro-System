package com.gearrent.controller;

import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    // ── Revenue Report ─────────────────────────────────────────
    @FXML private DatePicker dpRevFrom, dpRevTo;
    @FXML private TableView<Object[]> tblRevenue;
    @FXML private TableColumn<Object[],String> colRevBranch, colRevCount,
                                                colRevIncome, colRevLate, colRevDamage;
    @FXML private Label lblRevTotal;

    // ── Utilisation Report ─────────────────────────────────────
    @FXML private ComboBox<com.gearrent.entity.Branch> cmbUtilBranch;
    @FXML private DatePicker dpUtilFrom, dpUtilTo;
    @FXML private TableView<Object[]> tblUtil;
    @FXML private TableColumn<Object[],String> colUtilId, colUtilModel, colUtilCat,
                                                colUtilRented, colUtilTotal, colUtilPct;

    private final int scopedBranch = SessionManager.getInstance().getScopedBranchId();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRevTable();
        setupUtilTable();
        loadBranchCombo();

        // Default date ranges
        dpRevFrom .setValue(LocalDate.now().withDayOfMonth(1));
        dpRevTo   .setValue(LocalDate.now());
        dpUtilFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpUtilTo  .setValue(LocalDate.now());
    }

    // ── Revenue ────────────────────────────────────────────────
    private void setupRevTable() {
        colRevBranch.setCellValueFactory(d -> new SimpleStringProperty((String)      d.getValue()[0]));
        colRevCount .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue()[1])));
        colRevIncome.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt((double) d.getValue()[2])));
        colRevLate  .setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt((double) d.getValue()[3])));
        colRevDamage.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt((double) d.getValue()[4])));
    }

    @FXML private void handleRunRevenue() {
        LocalDate from = dpRevFrom.getValue();
        LocalDate to   = dpRevTo.getValue();
        if (from == null || to == null || from.isAfter(to)) {
            UIHelper.showError("Select a valid date range."); return;
        }
        try {
            List<Object[]> rows = ServiceFactory.reports()
                    .getBranchRevenueReport(scopedBranch, from, to);
            tblRevenue.setItems(FXCollections.observableArrayList(rows));

            double totalIncome = rows.stream().mapToDouble(r -> (double) r[2]).sum();
            double totalLate   = rows.stream().mapToDouble(r -> (double) r[3]).sum();
            double totalDmg    = rows.stream().mapToDouble(r -> (double) r[4]).sum();
            lblRevTotal.setText(String.format(
                "Totals  —  Rental Income: %s  |  Late Fees: %s  |  Damages: %s  |  Grand Total: %s",
                UIHelper.fmt(totalIncome), UIHelper.fmt(totalLate), UIHelper.fmt(totalDmg),
                UIHelper.fmt(totalIncome + totalLate + totalDmg)));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    // ── Utilisation ────────────────────────────────────────────
    private void setupUtilTable() {
        colUtilId    .setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[0]));
        colUtilModel .setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[1]));
        colUtilCat   .setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[2]));
        colUtilRented.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3] + " days"));
        colUtilTotal .setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4] + " days"));
        colUtilPct   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5] + "%"));

        // Colour-code by utilisation %
        tblUtil.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Object[] row, boolean empty) {
                super.updateItem(row, empty);
                getStyleClass().removeAll("row-high-util","row-low-util");
                if (!empty && row != null) {
                    double pct = (double) row[5];
                    if (pct >= 70) getStyleClass().add("row-high-util");
                    else if (pct <= 20) getStyleClass().add("row-low-util");
                }
            }
        });
    }

    private void loadBranchCombo() {
        try {
            List<com.gearrent.entity.Branch> branches;
            if (scopedBranch > 0) {
                branches = List.of(ServiceFactory.branches().getById(scopedBranch));
            } else {
                branches = ServiceFactory.branches().getAllActive();
            }
            cmbUtilBranch.setItems(FXCollections.observableArrayList(branches));
            if (!branches.isEmpty()) cmbUtilBranch.getSelectionModel().selectFirst();
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleRunUtilisation() {
        var branch = cmbUtilBranch.getValue();
        LocalDate from = dpUtilFrom.getValue();
        LocalDate to   = dpUtilTo.getValue();
        if (branch == null) { UIHelper.showError("Select a branch."); return; }
        if (from == null || to == null || from.isAfter(to)) {
            UIHelper.showError("Select a valid date range."); return;
        }
        try {
            List<Object[]> rows = ServiceFactory.reports()
                    .getUtilisationReport(branch.getBranchId(), from, to);
            tblUtil.setItems(FXCollections.observableArrayList(rows));
            if (rows.isEmpty()) UIHelper.showInfo("No data for selected period.");
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }
}
