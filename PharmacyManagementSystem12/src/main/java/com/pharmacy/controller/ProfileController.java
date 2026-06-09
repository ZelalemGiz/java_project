package com.pharmacy.controller;

import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.dao.UserDAO;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ProfileController {
    
    @FXML private ImageView profileImage;
    @FXML private TextField usernameField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    
    private final UserDAO userDAO = new UserDAO();
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();

    @FXML
    public void initialize() {
        if (UserSession.getCurrentUser() != null) {
            usernameField.setText(UserSession.getCurrentUser().getUsername());
            usernameField.setEditable(true);
        }
        loadProfilePhotoFromDatabase();
    }
    
    private void loadProfilePhotoFromDatabase() {
        if (profileImage == null) return;
        try {
            if (UserSession.getCurrentUser() != null) {
                Pharmacist pharmacist = pharmacistDAO.getPharmacistById(UserSession.getCurrentUser().getId());
                if (pharmacist != null && pharmacist.getPhoto() != null && pharmacist.getPhoto().length > 0) {
                    profileImage.setImage(new Image(new ByteArrayInputStream(pharmacist.getPhoto())));
                    return;
                }
            }
            loadDefaultImage();
        } catch (Exception e) {
            loadDefaultImage();
        }
    }
 
    private void loadDefaultImage() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/com/pharmacy/images/user.png"));
            if (img != null && !img.isError()) {
                profileImage.setImage(img);
            }
        } catch (Exception e) {
            profileImage.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 75px;");
        }
    }

    /**
     * Update username and password only
     * @param event
     */
    @FXML
    public void handleUpdateProfile(ActionEvent event) {
        String username = usernameField.getText().trim();
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        
        if (username.isEmpty()) { AlertUtil.showAlert("Error", "Username is required.", Alert.AlertType.ERROR); return; }
        if (oldPass.isEmpty()) { AlertUtil.showAlert("Error", "Old password is required.", Alert.AlertType.ERROR); return; }
        if (newPass.isEmpty()) { AlertUtil.showAlert("Error", "New password is required.", Alert.AlertType.ERROR); return; }
        if (newPass.length() < 4) { AlertUtil.showAlert("Error", "Password min 4 characters.", Alert.AlertType.ERROR); return; }
        if (oldPass.equals(newPass)) { AlertUtil.showAlert("Error", "New password must be different.", Alert.AlertType.ERROR); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText("Update Profile");
        confirm.setContentText("Save changes?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = userDAO.updateCredentials(username, oldPass, newPass);
                if (success) {
                    UserSession.getCurrentUser().setUsername(username);
                    AlertUtil.showAlert("Success", "Profile updated!", Alert.AlertType.INFORMATION);
                    oldPasswordField.clear();
                    newPasswordField.clear();
                    handleBack(event);
                } else {
                    AlertUtil.showAlert("Error", "Check old password.", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.setTitle("Pharmacy Management System");
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }
}