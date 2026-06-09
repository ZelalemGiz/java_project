package com.pharmacy.controller;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MedicineController {
    @FXML private TableView<Medicine> medicineTable;
    @FXML private Button btnReport;
    @FXML private Button btnRemoveExpired;
    @FXML private Button btnDelete;
    @FXML private Button btnAddUpdate;
    
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    private boolean isPharmacist = false;
    private boolean isAdmin = false;

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
        // Check roles
        if (UserSession.getCurrentUser() != null) {
            isAdmin = "ADMIN".equalsIgnoreCase(UserSession.getCurrentUser().getRole());
            isPharmacist = "PHARMACIST".equalsIgnoreCase(UserSession.getCurrentUser().getRole());
        }
        
        // Hide admin-only buttons for pharmacists
        if (!isAdmin) {
            setNodeVisible(btnRemoveExpired, false);
            setNodeVisible(btnDelete, false);
            setNodeVisible(btnAddUpdate, false);
        }
        
        // Hide report button for admin
        if (isAdmin) {
            setNodeVisible(btnReport, false);
        }

        // Setup columns
        TableColumn<Medicine, Integer> colId = getColumn(0);
        TableColumn<Medicine, String> colName = getColumn(1);
        TableColumn<Medicine, String> colCategory = getColumn(2);
        TableColumn<Medicine, Double> colCost = getColumn(3);
        TableColumn<Medicine, Double> colPrice = getColumn(4);
        TableColumn<Medicine, Double> colProfit = getColumn(5);
        TableColumn<Medicine, Integer> colStock = getColumn(6);
        TableColumn<Medicine, String> colStatus = getColumn(7);
        TableColumn<Medicine, LocalDateTime> colDate = getColumn(8);
        TableColumn<Medicine, LocalDate> colExpiry = getColumn(9);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colProfit.setCellValueFactory(new PropertyValueFactory<>("profit"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        loadData();
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<Medicine, T> getColumn(int index) {
        return (TableColumn<Medicine, T>) medicineTable.getColumns().get(index);
    }

    private void setNodeVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    public void loadData() {
        List<Medicine> medicines;
        
        if (isPharmacist) {
            // ✅ Pharmacist sees only their branch medicines
            Pharmacist ph = pharmacistDAO.getPharmacistById(UserSession.getCurrentUser().getId());
            String branch = ph != null ? ph.getBranchName() : null;
            
            if (branch != null && !branch.isEmpty()) {
                medicines = medicineDAO.getMedicinesByBranch(branch);
            } else {
                medicines = new ArrayList<>();
            }
        } else {
            // ✅ Admin sees all medicines
            medicines = medicineDAO.getAllMedicines();
        }
        
        medicineTable.setItems(FXCollections.observableArrayList(medicines));
        medicineTable.refresh();
    }

    @FXML
    public void handleDeleteMedicine(ActionEvent event) {
        if (isPharmacist) {
            AlertUtil.showAlert("Access Denied", "Only admins can delete medicines.", Alert.AlertType.ERROR);
            return;
        }
        
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (medicineDAO.deleteMedicine(selected.getId())) {
                AlertUtil.showAlert("Success", "Medicine deleted.", Alert.AlertType.INFORMATION);
                loadData();
            }
        } else {
            AlertUtil.showAlert("Warning", "Select a medicine.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void handleRemoveExpired(ActionEvent event) {
        if (isPharmacist) {
            AlertUtil.showAlert("Access Denied", "Only admins can remove expired medicines.", Alert.AlertType.ERROR);
            return;
        }
        
        int count = medicineDAO.removeExpiredMedicines();
        AlertUtil.showAlert("Success", count + " expired medicines removed.", Alert.AlertType.INFORMATION);
        loadData();
    }

    @FXML
    public void handleAddUpdateMedicine(ActionEvent event) {
        if (isPharmacist) {
            AlertUtil.showAlert("Access Denied", "Only admins can add/update medicines.", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pharmacy/view/addmedicine.fxml"));
            Parent root = loader.load();
            
            AddMedicineController addController = loader.getController();
            addController.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Add Medicine");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Could not load Add Medicine view.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleReportLowStock(ActionEvent event) {
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getStock() < 10) {
                AlertUtil.showAlert("Report Sent", "Administrator notified about low stock for: " + selected.getName(), Alert.AlertType.INFORMATION);
            } else {
                AlertUtil.showAlert("Warning", "Stock level is adequate.", Alert.AlertType.WARNING);
            }
        } else {
            AlertUtil.showAlert("Warning", "Select a medicine to report.", Alert.AlertType.WARNING);
        }
    }
}