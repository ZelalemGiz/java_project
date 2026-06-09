package com.pharmacy.controller;

import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ForgotPasswordController {
    
    @FXML private TextField txtEmail;
    @FXML private VBox securityBox;
    @FXML private Label lblSecurityQuestion;
    @FXML private TextField txtSecurityAnswer;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    private Pharmacist foundPharmacist;
    
    @FXML
    public void handleVerifyEmail(ActionEvent event) {
        String email = txtEmail.getText().trim().toLowerCase();
        
        if (email.isEmpty()) {
            AlertUtil.showAlert("Error", "Please enter your email.", Alert.AlertType.ERROR);
            return;
        }
        
        foundPharmacist = pharmacistDAO.getPharmacistByEmail(email);
        
        if (foundPharmacist != null) {
            String question = foundPharmacist.getSecurityQuestion();
            if (question != null && !question.isEmpty()) {
                lblSecurityQuestion.setText("🔒 " + question);
                securityBox.setVisible(true);
                securityBox.setManaged(true);
                txtSecurityAnswer.requestFocus();
            } else {
                AlertUtil.showAlert("Notice", 
                    "No security question set.\nPlease contact administrator.", 
                    Alert.AlertType.INFORMATION);
            }
        } else {
            AlertUtil.showAlert("Error", "No account found with that email.", Alert.AlertType.ERROR);
            txtEmail.clear();
            txtEmail.requestFocus();
        }
    }
    
    @FXML
    public void handleResetPassword(ActionEvent event) {
        if (foundPharmacist == null) {
            AlertUtil.showAlert("Error", "Please verify your email first.", Alert.AlertType.ERROR);
            return;
        }
        
        String answer = txtSecurityAnswer.getText().trim();
        if (answer.isEmpty()) {
            AlertUtil.showAlert("Error", "Please enter your security answer.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!answer.equalsIgnoreCase(foundPharmacist.getSecurityAnswer())) {
            AlertUtil.showAlert("Error", "Incorrect security answer.", Alert.AlertType.ERROR);
            txtSecurityAnswer.clear();
            txtSecurityAnswer.requestFocus();
            return;
        }
        
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();
        
        if (newPass.isEmpty()) {
            AlertUtil.showAlert("Error", "Enter new password.", Alert.AlertType.ERROR);
            return;
        }
        if (newPass.length() < 4) {
            AlertUtil.showAlert("Error", "Password min 4 characters.", Alert.AlertType.ERROR);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            AlertUtil.showAlert("Error", "Passwords do not match.", Alert.AlertType.ERROR);
            return;
        }
        
        foundPharmacist.setPassword(newPass);
        if (pharmacistDAO.updatePharmacist(foundPharmacist)) {
            AlertUtil.showAlert("Success", "Password reset! You can now login.", Alert.AlertType.INFORMATION);
            handleBackToLogin(null);
        } else {
            AlertUtil.showAlert("Error", "Failed to update password.", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    public void handleBackToLogin(ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/login.fxml"));
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.setTitle("Pharmacy Management - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}