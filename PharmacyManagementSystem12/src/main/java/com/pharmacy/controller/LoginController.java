package com.pharmacy.controller;

import com.pharmacy.service.AuthService;
import com.pharmacy.model.User;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Node;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private final AuthService authService = new AuthService();

    @FXML
    public void handleLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        User loggedInUser = authService.authenticate(user, pass);
        
        if (loggedInUser != null) {
            UserSession.setCurrentUser(loggedInUser);

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/dashboard.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("Pharmacy Management System");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showAlert("Error", "Could not load dashboard", Alert.AlertType.ERROR);
            }
        } else {
            AlertUtil.showAlert("Error", "Invalid credentials!", Alert.AlertType.ERROR);
        }
    }
    @FXML
     public void handleForgotPassword(ActionEvent event) {
    try {
        Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/forgotpassword.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(view));
        stage.setTitle("Forgot Password");
        stage.centerOnScreen();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
       @FXML
    public void handleBack(ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/welcome.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
