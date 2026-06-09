package com.pharmacy.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {
    
    
    @FXML
    public void initialize() {
        // Save reference to welcome scene
        getCurrentScene();
    }
    
    @FXML
    public void handleLogin(ActionEvent event) {
        switchTo("login.fxml", "Pharmacy Management - Login", event);
    }
    
    @FXML
    public void handleViewMembers(ActionEvent event) {
        switchTo("members.fxml", "Group Members", event);
    }
    
    @FXML
    public void handleMinimize(ActionEvent event) {
        getStage(event).setIconified(true);
    }
    
    @FXML
    public void handleClose(ActionEvent event) {
        getStage(event).close();
    }
    private void switchTo(String fxml, String title, ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/" + fxml));
            Stage stage = getStage(event);
            stage.setScene(new Scene(view));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
    
    private Scene getCurrentScene() {
        // Used to save welcome scene reference
        return null;
    }
}