package com.pharmacy.controller;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddMedicineController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtCostPrice;
    @FXML private TextField txtSellingPrice;
    @FXML private TextField txtStock;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpExpiryDate;
    @FXML private ComboBox<String> cbBranch;

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private MedicineController parentController;
    private Medicine editingMedicine;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        cbCategory.setItems(FXCollections.observableArrayList(
            "Tablet", "Capsule", "Syrup", "Injection", "Cream", "Ointment",
            "Drops", "Inhaler", "Spray", "Gel", "Powder", "Suspension",
            "Antibiotic", "Pain Killer", "Vitamin", "Supplement", "Other"
        ));
        cbCategory.setPromptText("Select Category");
        
        cbStatus.setItems(FXCollections.observableArrayList("Available", "Low Stock", "Out of Stock"));
        cbStatus.getSelectionModel().selectFirst();
        
        if (cbBranch != null) {
            cbBranch.setItems(FXCollections.observableArrayList(
                "Main Branch", "Branch 1", "Branch 2", "Branch 3", "Branch 4", "Branch 5"
            ));
            cbBranch.setPromptText("Select Branch");
        }
    }

    public void setParentController(MedicineController parentController) {
        this.parentController = parentController;
    }
    
    public void setMedicineForEdit(Medicine medicine) {
        this.editingMedicine = medicine;
        this.isEditMode = true;
        
        txtName.setText(medicine.getName());
        cbCategory.setValue(medicine.getCategory());
        txtCostPrice.setText(String.valueOf(medicine.getCostPrice()));
        txtSellingPrice.setText(String.valueOf(medicine.getSellingPrice()));
        txtStock.setText(String.valueOf(medicine.getStock()));
        cbStatus.setValue(medicine.getStatus());
        if (medicine.getExpiryDate() != null) dpExpiryDate.setValue(medicine.getExpiryDate());
        if (cbBranch != null && medicine.getBranchName() != null) cbBranch.setValue(medicine.getBranchName());
    }

    @FXML
    public void handleSave(ActionEvent event) {
        
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showError("Medicine name is required.", txtName); return;
        }
        if (name.length() < 2) {
            showError("Name must be at least 2 characters.", txtName); return;
        }
        if (name.length() > 100) {
            showError("Name is too long (max 100 chars).", txtName); return;
        }

        if (cbCategory.getValue() == null) {
            showError("Please select a category.", cbCategory); return;
        }

        if (cbBranch == null || cbBranch.getValue() == null) {
            showError("Please select a branch.", cbBranch); return;
        }

        double costPrice;
        try {
            costPrice = Double.parseDouble(txtCostPrice.getText().trim());
            if (costPrice < 0) { showError("Cost price cannot be negative.", txtCostPrice); return; }
            if (costPrice > 999999.99) { showError("Cost price too high (max 999,999.99).", txtCostPrice); return; }
        } catch (NumberFormatException e) {
            showError("Cost price must be a valid number (e.g., 10.50).", txtCostPrice); return;
        }

        double sellingPrice;
        try {
            sellingPrice = Double.parseDouble(txtSellingPrice.getText().trim());
            if (sellingPrice < 0) { showError("Selling price cannot be negative.", txtSellingPrice); return; }
            if (sellingPrice < costPrice) { showError("Selling price must be ≥ cost price.", txtSellingPrice); return; }
            if (sellingPrice > 999999.99) { showError("Selling price too high (max 999,999.99).", txtSellingPrice); return; }
        } catch (NumberFormatException e) {
            showError("Selling price must be a valid number (e.g., 15.99).", txtSellingPrice); return;
        }

        int stock;
        try {
            stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) { showError("Stock cannot be negative.", txtStock); return; }
            if (stock > 99999) { showError("Stock too high (max 99,999).", txtStock); return; }
        } catch (NumberFormatException e) {
            showError("Stock must be a whole number (e.g., 100).", txtStock); return;
        }
        
        if (dpExpiryDate.getValue() == null) {
            showError("Expiry date is required.", dpExpiryDate); return;
        }
        if (dpExpiryDate.getValue().isBefore(LocalDate.now())) {
            showError("Expiry date cannot be in the past.", dpExpiryDate); return;
        }
        if (dpExpiryDate.getValue().isAfter(LocalDate.now().plusYears(10))) {
            showError("Expiry date too far (max 10 years).", dpExpiryDate); return;
        }
        
        // 8. Status - auto-set based on stock
        if (stock == 0) cbStatus.setValue("Out of Stock");
        else if (stock < 10) cbStatus.setValue("Low Stock");
        else cbStatus.setValue("Available");
        
        if (cbStatus.getValue() == null) {
            showError("Please select a status.", cbStatus); return;
        }
        
        try {
            Medicine m = isEditMode ? editingMedicine : new Medicine();
            
            m.setName(name);
            m.setCategory(cbCategory.getValue());
            m.setCostPrice(costPrice);
            m.setSellingPrice(sellingPrice);
            m.setProfit(sellingPrice - costPrice);
            m.setStock(stock);
            m.setStatus(cbStatus.getValue());
            m.setExpiryDate(dpExpiryDate.getValue());
            if (cbBranch != null && cbBranch.getValue() != null) m.setBranchName(cbBranch.getValue());

            boolean success = isEditMode ? medicineDAO.updateMedicine(m) : medicineDAO.addMedicine(m);

            if (success) {
                AlertUtil.showAlert("Success", isEditMode ? "Medicine updated!" : "Medicine saved!", Alert.AlertType.INFORMATION);
                if (parentController != null) parentController.loadData();
                closeWindow();
            } else {
                AlertUtil.showAlert("Error", "Failed to save medicine.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleClear(ActionEvent event) {
        txtName.clear();
        cbCategory.getSelectionModel().clearSelection();
        txtCostPrice.clear();
        txtSellingPrice.clear();
        txtStock.clear();
        cbStatus.getSelectionModel().selectFirst();
        dpExpiryDate.setValue(null);
        if (cbBranch != null) cbBranch.getSelectionModel().clearSelection();
    }
    
    @FXML
    public void handleBack(ActionEvent event) { closeWindow(); }
    
    private void closeWindow() { ((Stage) txtName.getScene().getWindow()).close(); }
    
    private void showError(String msg, Control field) {
        AlertUtil.showAlert("Validation Error", msg, Alert.AlertType.ERROR);
        if (field != null) field.requestFocus();
    }
}