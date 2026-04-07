package com.gearrent.controller;

import com.gearrent.entity.*;
import com.gearrent.entity.Equipment.Status;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class EquipmentController implements Initializable {

    @FXML private TableView<Equipment> tblEq;
    @FXML private TableColumn<Equipment,String> colId, colCat, colBrand, colModel, colYear,
                                                 colPrice, colDeposit, colStatus, colBranch;

    // Filters
    @FXML private ComboBox<Branch>            cmbFilterBranch;
    @FXML private ComboBox<EquipmentCategory> cmbFilterCat;
    @FXML private ComboBox<String>            cmbFilterStatus;
    @FXML private TextField                   txtSearch;

    // Form fields
    @FXML private TextField  txtId, txtBrand, txtModel, txtYear, txtPrice, txtDeposit;
    @FXML private ComboBox<Branch>            cmbBranch;
    @FXML private ComboBox<EquipmentCategory> cmbCat;
    @FXML private ComboBox<Status>            cmbStatus;
    @FXML private Label lblStatus;

    private Equipment selected;
    private final int scopedBranch = SessionManager.getInstance().getScopedBranchId();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadCombos();
        refresh();
        tblEq.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> populate(n));
    }

    private void setupTable() {
        colId     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipmentId()));
        colCat    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoryName()));
        colBrand  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBrand()));
        colModel  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getModel()));
        colYear   .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPurchaseYear())));
        colPrice  .setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getDailyBasePrice())));
        colDeposit.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getSecurityDeposit())));
        colStatus .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().getDisplay()));
        colBranch .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranchName()));

        tblEq.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Equipment e, boolean empty) {
                super.updateItem(e, empty);
                getStyleClass().removeAll("row-rented","row-maintenance","row-reserved");
                if (!empty && e != null) {
                    if (e.getStatus() == Status.Rented)           getStyleClass().add("row-rented");
                    else if (e.getStatus() == Status.UnderMaintenance) getStyleClass().add("row-maintenance");
                    else if (e.getStatus() == Status.Reserved)    getStyleClass().add("row-reserved");
                }
            }
        });
    }

    private void loadCombos() {
        try {
            List<Branch> branches = scopedBranch > 0
                ? List.of(ServiceFactory.branches().getById(scopedBranch))
                : ServiceFactory.branches().getAllActive();
            List<EquipmentCategory> cats = ServiceFactory.categories().getAllActive();

            cmbBranch.setItems(FXCollections.observableArrayList(branches));
            cmbCat.setItems(FXCollections.observableArrayList(cats));
            cmbStatus.setItems(FXCollections.observableArrayList(Status.values()));

            // Filter combos
            List<Branch> allBranches = new ArrayList<>();
            allBranches.add(null); // "All"
            allBranches.addAll(branches);
            cmbFilterBranch.setItems(FXCollections.observableArrayList(allBranches));

            List<EquipmentCategory> allCats = new ArrayList<>();
            allCats.add(null);
            allCats.addAll(cats);
            cmbFilterCat.setItems(FXCollections.observableArrayList(allCats));

            List<String> statuses = new ArrayList<>(List.of("", "Available","Reserved","Rented","Under Maintenance"));
            cmbFilterStatus.setItems(FXCollections.observableArrayList(statuses));

            // Default form values
            if (!branches.isEmpty()) cmbBranch.getSelectionModel().selectFirst();
            if (!cats.isEmpty())     cmbCat.getSelectionModel().selectFirst();
            cmbStatus.getSelectionModel().select(Status.Available);

        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void refresh() {
        try {
            Branch selBranch = cmbFilterBranch.getValue();
            EquipmentCategory selCat = cmbFilterCat.getValue();
            String status = cmbFilterStatus.getValue();
            String kw = txtSearch.getText();

            int bId  = (selBranch != null) ? selBranch.getBranchId()    : (scopedBranch > 0 ? scopedBranch : -1);
            int cId  = (selCat    != null) ? selCat.getCategoryId()     : -1;

            tblEq.setItems(FXCollections.observableArrayList(
                ServiceFactory.equipment().filter(bId, cId, status == null ? "" : status, kw)));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleFilter() { refresh(); }

    private void populate(Equipment e) {
        if (e == null) { clearForm(); return; }
        selected = e;
        txtId.setText(e.getEquipmentId());
        txtId.setDisable(true);
        txtBrand.setText(e.getBrand());
        txtModel.setText(e.getModel());
        txtYear.setText(String.valueOf(e.getPurchaseYear()));
        txtPrice.setText(String.valueOf(e.getDailyBasePrice()));
        txtDeposit.setText(String.valueOf(e.getSecurityDeposit()));
        cmbStatus.setValue(e.getStatus());
        cmbBranch.getItems().stream().filter(b -> b != null && b.getBranchId() == e.getBranchId())
            .findFirst().ifPresent(cmbBranch::setValue);
        cmbCat.getItems().stream().filter(c -> c != null && c.getCategoryId() == e.getCategoryId())
            .findFirst().ifPresent(cmbCat::setValue);
    }

    @FXML private void handleSave() {
        try {
            Equipment eq = selected != null ? selected : new Equipment();
            String id = txtId.getText().trim().toUpperCase();
            if (id.isEmpty()) throw new Exception("Equipment ID is required.");
            eq.setEquipmentId(id);
            eq.setBrand(txtBrand.getText().trim());
            eq.setModel(txtModel.getText().trim());
            eq.setPurchaseYear(Integer.parseInt(txtYear.getText().trim()));
            eq.setDailyBasePrice(Double.parseDouble(txtPrice.getText().trim()));
            eq.setSecurityDeposit(Double.parseDouble(txtDeposit.getText().trim()));
            eq.setStatus(cmbStatus.getValue());
            if (cmbBranch.getValue() == null) throw new Exception("Select a branch.");
            if (cmbCat.getValue() == null)    throw new Exception("Select a category.");
            eq.setBranchId(cmbBranch.getValue().getBranchId());
            eq.setCategoryId(cmbCat.getValue().getCategoryId());
            ServiceFactory.equipment().save(eq);
            UIHelper.showInfo("Equipment saved.");
            clearForm(); refresh();
        } catch (NumberFormatException e) {
            UIHelper.showError("Year, price and deposit must be valid numbers.");
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selected == null) { UIHelper.showError("Select equipment first."); return; }
        if (!UIHelper.confirm("Delete equipment " + selected.getEquipmentId() + "?")) return;
        try { ServiceFactory.equipment().delete(selected.getEquipmentId()); clearForm(); refresh(); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        selected = null;
        txtId.clear(); txtId.setDisable(false);
        txtBrand.clear(); txtModel.clear();
        txtYear.setText(String.valueOf(LocalDate.now().getYear()));
        txtPrice.clear(); txtDeposit.clear();
        cmbStatus.getSelectionModel().select(Status.Available);
        tblEq.getSelectionModel().clearSelection();
    }
}
