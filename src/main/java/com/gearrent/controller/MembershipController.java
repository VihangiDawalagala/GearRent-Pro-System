package com.gearrent.controller;

import com.gearrent.entity.MembershipConfig;
import com.gearrent.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MembershipController implements Initializable {

    @FXML private TableView<MembershipConfig> tblMem;
    @FXML private TableColumn<MembershipConfig,String> colLevel, colDiscount, colLimit;
    @FXML private Label     lblLevel;
    @FXML private TextField txtDiscount, txtLimit;

    private MembershipConfig selected;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!SessionManager.getInstance().isAdmin()) return;
        colLevel   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLevel()));
        colDiscount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiscountPercentage() + "%"));
        colLimit   .setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getDepositLimit())));
        refresh();
        tblMem.getSelectionModel().selectedItemProperty().addListener((obs,o,n) -> {
            if (n == null) return;
            selected = n;
            lblLevel.setText(n.getLevel());
            txtDiscount.setText(String.valueOf(n.getDiscountPercentage()));
            txtLimit.setText(String.valueOf(n.getDepositLimit()));
        });
    }

    private void refresh() {
        try { tblMem.setItems(FXCollections.observableArrayList(ServiceFactory.memberships().getAll())); }
        catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleSave() {
        if (selected == null) { UIHelper.showError("Select a membership level."); return; }
        try {
            selected.setDiscountPercentage(Double.parseDouble(txtDiscount.getText().trim()));
            selected.setDepositLimit(Double.parseDouble(txtLimit.getText().trim()));
            ServiceFactory.memberships().update(selected);
            UIHelper.showInfo("Membership config updated.");
            refresh();
        } catch (NumberFormatException e) {
            UIHelper.showError("Discount and limit must be valid numbers.");
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }
}
