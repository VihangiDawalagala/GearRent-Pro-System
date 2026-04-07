package com.gearrent.controller;

import com.gearrent.dao.PricingRuleDAO;
import com.gearrent.entity.MembershipConfig;
import com.gearrent.entity.PricingRule;
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
    @FXML private TextField txtMinDays, txtLongDiscount, txtLateFee;
    @FXML private Label     lblPricingStatus;

    private MembershipConfig selected;
    private final PricingRuleDAO ruleDAO = new PricingRuleDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!SessionManager.getInstance().isAdmin()) return;

        colLevel   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLevel()));
        colDiscount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiscountPercentage() + "%"));
        colLimit   .setCellValueFactory(d -> new SimpleStringProperty(UIHelper.fmt(d.getValue().getDepositLimit())));

        refresh();
        loadPricingRules();

        tblMem.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            selected = n;
            lblLevel.setText(n.getLevel());
            txtDiscount.setText(String.valueOf(n.getDiscountPercentage()));
            txtLimit.setText(String.valueOf(n.getDepositLimit()));
        });
    }

    private void refresh() {
        try {
            tblMem.setItems(FXCollections.observableArrayList(ServiceFactory.memberships().getAll()));
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    private void loadPricingRules() {
        try {
            PricingRule pr = ruleDAO.findFirst();
            if (pr != null) {
                txtMinDays.setText(String.valueOf(pr.getLongRentalMinDays()));
                txtLongDiscount.setText(String.valueOf(pr.getLongRentalDiscountPct()));
                txtLateFee.setText(String.valueOf(pr.getGlobalLateFeePerDay()));
                lblPricingStatus.setText("Current: " + pr.getLongRentalMinDays() +
                    " days min, " + pr.getLongRentalDiscountPct() +
                    "% discount, LKR " + pr.getGlobalLateFeePerDay() + "/day late fee");
            }
        } catch (Exception e) {
            lblPricingStatus.setText("Could not load pricing rules.");
        }
    }

    @FXML private void handleSave() {
        if (selected == null) { UIHelper.showError("Select a membership level first."); return; }
        try {
            selected.setDiscountPercentage(Double.parseDouble(txtDiscount.getText().trim()));
            selected.setDepositLimit(Double.parseDouble(txtLimit.getText().trim()));
            ServiceFactory.memberships().update(selected);
            UIHelper.showInfo("Membership config updated successfully.");
            refresh();
        } catch (NumberFormatException e) {
            UIHelper.showError("Discount and limit must be valid numbers.");
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }

    @FXML private void handleSavePricing() {
        try {
            int minDays     = Integer.parseInt(txtMinDays.getText().trim());
            double longDisc = Double.parseDouble(txtLongDiscount.getText().trim());
            double lateFee  = Double.parseDouble(txtLateFee.getText().trim());

            if (minDays < 1) throw new Exception("Minimum days must be at least 1.");
            if (longDisc < 0 || longDisc > 100) throw new Exception("Discount must be 0-100%.");
            if (lateFee < 0) throw new Exception("Late fee cannot be negative.");

            PricingRule pr = ruleDAO.findFirst();
            if (pr == null) pr = new PricingRule();
            pr.setLongRentalMinDays(minDays);
            pr.setLongRentalDiscountPct(longDisc);
            pr.setGlobalLateFeePerDay(lateFee);
            ruleDAO.update(pr);

            lblPricingStatus.setText("Saved! " + minDays + " days min, " +
                longDisc + "% discount, LKR " + lateFee + "/day late fee");
            UIHelper.showInfo("Pricing rules saved successfully!");
        } catch (NumberFormatException e) {
            UIHelper.showError("All pricing fields must be valid numbers.");
        } catch (Exception e) { UIHelper.showError(e.getMessage()); }
    }
}