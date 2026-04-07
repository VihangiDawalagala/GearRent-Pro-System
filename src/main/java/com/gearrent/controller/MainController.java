package com.gearrent.controller;

import com.gearrent.entity.SystemUser;
import com.gearrent.entity.SystemUser.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label     lblUserInfo;
    @FXML private StackPane contentArea;

    // Nav buttons
    @FXML private Button btnBranches, btnCategories, btnMembership, btnUsers;
    @FXML private Button btnDashboard, btnEquipment, btnCustomers;
    @FXML private Button btnReservations, btnRentals, btnReturns, btnOverdue, btnReports;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SessionManager sm = SessionManager.getInstance();
        SystemUser user   = sm.getCurrentUser();

        lblUserInfo.setText(user.getFullName() + "  |  " + user.getRole().getDisplay() +
                (sm.isAdmin() ? "" : "  –  " + user.getBranchName()));

        // Role-based visibility
        boolean isAdmin = sm.isAdmin();
        boolean isMgr   = sm.isBranchManager();

        btnBranches.setVisible(isAdmin);   btnBranches.setManaged(isAdmin);
        btnMembership.setVisible(isAdmin); btnMembership.setManaged(isAdmin);
        btnUsers.setVisible(isAdmin);      btnUsers.setManaged(isAdmin);
        btnCategories.setVisible(isAdmin || isMgr);
        btnCategories.setManaged(isAdmin || isMgr);

        showDashboard();
    }

    @FXML private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gearrent/fxml/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 480, 360);
            scene.getStylesheets().add(
                    getClass().getResource("/com/gearrent/css/styles.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(scene); stage.setMaximized(false);
            stage.setWidth(480); stage.setHeight(360);
            stage.setTitle("GearRent Pro – Login");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Navigation handlers ──────────────────────────────────────
    @FXML public void showDashboard()    { loadView("/com/gearrent/fxml/dashboard.fxml"); }
    @FXML public void showBranches()     { loadView("/com/gearrent/fxml/branch.fxml"); }
    @FXML public void showCategories()   { loadView("/com/gearrent/fxml/category.fxml"); }
    @FXML public void showEquipment()    { loadView("/com/gearrent/fxml/equipment.fxml"); }
    @FXML public void showCustomers()    { loadView("/com/gearrent/fxml/customer.fxml"); }
    @FXML public void showMembership()   { loadView("/com/gearrent/fxml/membership.fxml"); }
    @FXML public void showReservations() { loadView("/com/gearrent/fxml/reservation.fxml"); }
    @FXML public void showRentals()      { loadView("/com/gearrent/fxml/rental.fxml"); }
    @FXML public void showReturns()      { loadView("/com/gearrent/fxml/returns.fxml"); }
    @FXML public void showOverdue()      { loadView("/com/gearrent/fxml/overdue.fxml"); }
    @FXML public void showReports()      { loadView("/com/gearrent/fxml/report.fxml"); }
    @FXML public void showUsers()        { loadView("/com/gearrent/fxml/user.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            Label err = new Label("Failed to load view: " + fxmlPath + "\n" + e.getMessage());
            err.setStyle("-fx-text-fill: red; -fx-padding: 20;");
            contentArea.getChildren().setAll(err);
            e.printStackTrace();
        }
    }
}
