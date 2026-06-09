package com.pharmacy.controller;

import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PharmacistController {
    @FXML private TableView<Pharmacist> pharmacistTable;
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    private boolean isPharmacist = false;

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        // Check role
        if (UserSession.getCurrentUser() != null) {
            isPharmacist = "PHARMACIST".equalsIgnoreCase(UserSession.getCurrentUser().getRole());
        }
        
        TableColumn<Pharmacist, Integer> colId = getColumn(0);
        TableColumn<Pharmacist, String> colIdNumber = getColumn(1);
        TableColumn<Pharmacist, String> colFirst = getColumn(2);
        TableColumn<Pharmacist, String> colLast = getColumn(3);
        TableColumn<Pharmacist, String> colGender = getColumn(4);
        TableColumn<Pharmacist, String> colPhone = getColumn(5);
        TableColumn<Pharmacist, String> colRole = getColumn(6);
        TableColumn<Pharmacist, Double> colSalary = getColumn(7);
        TableColumn<Pharmacist, LocalDateTime> colEmploying = getColumn(8);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIdNumber.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        colFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colEmploying.setCellValueFactory(new PropertyValueFactory<>("employingDate"));

        // Format salary
        colSalary.setCellFactory(col -> new TableCell<Pharmacist, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        // Format date
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colEmploying.setCellFactory(col -> new TableCell<Pharmacist, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(fmt));
            }
        });

        loadData();
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<Pharmacist, T> getColumn(int index) {
        return (TableColumn<Pharmacist, T>) pharmacistTable.getColumns().get(index);
    }

    /**
     * Load data - branch filtered for pharmacists
     */
    public void loadData() {
        List<Pharmacist> pharmacists;
        
        if (isPharmacist) {
            // ✅ Pharmacist sees only same branch
            Pharmacist currentUser = pharmacistDAO.getPharmacistById(UserSession.getCurrentUser().getId());
            String branch = currentUser != null ? currentUser.getBranchName() : null;
            
            if (branch != null && !branch.isEmpty()) {
                pharmacists = pharmacistDAO.getPharmacistsByBranch(branch);
            } else {
                pharmacists = new ArrayList<>();
            }
        } else {
            // Admin sees all
            pharmacists = pharmacistDAO.getAllPharmacists();
        }
        
        pharmacistTable.setItems(FXCollections.observableArrayList(pharmacists));
        pharmacistTable.refresh();
    }

    @FXML
    public void handleAddPharmacist(ActionEvent event) {
        openAddPharmacistForm(null);
    }

    @FXML
    public void handleUpdatePharmacist(ActionEvent event) {
        Pharmacist selected = pharmacistTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showAlert("Warning", "Please select a pharmacist to update.", Alert.AlertType.WARNING);
            return;
        }
        
        openAddPharmacistForm(selected);
    }

    private void openAddPharmacistForm(Pharmacist pharmacist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pharmacy/view/addpharmacist.fxml"));
            Parent root = loader.load();
            
            AddPharmacistController addController = loader.getController();
            addController.setParentController(this);
            
            if (pharmacist != null) {
                addController.setPharmacistForEdit(pharmacist);
            }
            
            Stage stage = new Stage();
            stage.setTitle(pharmacist == null ? "Add Pharmacist" : "Update Pharmacist");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Could not load form.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeletePharmacist(ActionEvent event) {
        Pharmacist selected = pharmacistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showAlert("Warning", "Select a pharmacist.", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete");
        confirm.setHeaderText("Delete " + selected.getFirstName() + " " + selected.getLastName() + "?");
        confirm.setContentText("This cannot be undone.");
        
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK && pharmacistDAO.deletePharmacist(selected.getId())) {
                AlertUtil.showAlert("Success", "Deleted.", Alert.AlertType.INFORMATION);
                loadData();
            }
        });
    }
    
    @FXML
    public void handleRefresh(ActionEvent event) {
        loadData();
    }
}