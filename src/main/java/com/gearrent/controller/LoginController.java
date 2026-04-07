package com.gearrent.controller;

import com.gearrent.entity.SystemUser;
import com.gearrent.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    @FXML
    private void handleLogin() {
        lblError.setText("");
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        try {
            SystemUser user = ServiceFactory.auth().login(username, password);
            SessionManager.getInstance().setCurrentUser(user);
            openMainWindow();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gearrent/fxml/main.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/com/gearrent/css/styles.css").toExternalForm());

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setTitle("GearRent Pro – " +
                    SessionManager.getInstance().getCurrentUser().getFullName());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(true);
        } catch (Exception e) {
            lblError.setText("Failed to load main window: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
