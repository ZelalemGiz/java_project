package com.pharmacy.controller;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class InventoryController {
    
    @FXML private TableView<Medicine> inventoryTable;
    @FXML private TextField searchField;
    @FXML private Label labelTotalStock;
    @FXML private Label labelTotalLowStock;
    @FXML private Label labelTotalExpiryStock;
    
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    
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
        TableColumn<Medicine, Integer> colId = (TableColumn<Medicine, Integer>) inventoryTable.getColumns().get(0);
        TableColumn<Medicine, String> colName = (TableColumn<Medicine, String>) inventoryTable.getColumns().get(1);
        TableColumn<Medicine, Integer> colStock = (TableColumn<Medicine, Integer>) inventoryTable.getColumns().get(2);
        TableColumn<Medicine, String> colQuantityStatus = (TableColumn<Medicine, String>) inventoryTable.getColumns().get(3);
        TableColumn<Medicine, String> colExpiryStatus = (TableColumn<Medicine, String>) inventoryTable.getColumns().get(4);

        // Set cell value factories
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        // Quantity Status column
        colQuantityStatus.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getStock();
            String status;
            if (stock == 0) {
                status = "Out of Stock";
            } else if (stock < 10) {
                status = "Low Stock";
            } else {
                status = "In Stock";
            }
            return new SimpleStringProperty(status);
        });
        
        // Expiry Status column
        colExpiryStatus.setCellValueFactory(cellData -> {
            Medicine med = cellData.getValue();
            if (med.getExpiryDate() == null) {
                return new SimpleStringProperty("N/A");
            }
            
            LocalDate expiry = med.getExpiryDate();
            LocalDate now = LocalDate.now();
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(now, expiry);
            
            if (daysUntilExpiry < 0) {
                return new SimpleStringProperty("EXPIRED");
            } else if (daysUntilExpiry <= 30) {
                return new SimpleStringProperty("Expires in " + daysUntilExpiry + " days");
            } else {
                return new SimpleStringProperty("OK (" + daysUntilExpiry + " days)");
            }
        });

        colQuantityStatus.setCellFactory(column -> new TableCell<Medicine, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Out of Stock")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (item.equals("Low Stock")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981;");
                    }
                }
            }
        });
        
        colExpiryStatus.setCellFactory(column -> new TableCell<Medicine, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("EXPIRED")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (item.startsWith("Expires in")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981;");
                    }
                }
            }
        });
        
        colId.setStyle("-fx-alignment: CENTER;");
        colStock.setStyle("-fx-alignment: CENTER;");

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterMedicines(newText);
        });

        loadData();
    }
    private void loadData() {
        medicineList.setAll(medicineDAO.getAllMedicines());
        inventoryTable.setItems(medicineList);
        inventoryTable.refresh();
        updateSummaryCards();
    }

    private void filterMedicines(String query) {
        if (query == null || query.isEmpty()) {
            inventoryTable.setItems(medicineList);
        } else {
            ObservableList<Medicine> filtered = FXCollections.observableArrayList();
            for (Medicine m : medicineList) {
                if (m.getName().toLowerCase().contains(query.toLowerCase()) ||
                    String.valueOf(m.getId()).contains(query) ||
                    (m.getCategory() != null && m.getCategory().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(m);
                }
            }
            inventoryTable.setItems(filtered);
        }
    }

    private void updateSummaryCards() {
        int totalStock = 0;
        int lowStockCount = 0;
        int expiredCount = 0;
        LocalDate today = LocalDate.now();
        
        for (Medicine m : medicineList) {
            totalStock += m.getStock();
            
            if (m.getStock() < 10 && m.getStock() > 0) {
                lowStockCount++;
            }
            
            if (m.getExpiryDate() != null && m.getExpiryDate().isBefore(today)) {
                expiredCount++;
            }
        }
        
        labelTotalStock.setText(String.valueOf(totalStock));
        labelTotalLowStock.setText(String.valueOf(lowStockCount));
        labelTotalExpiryStock.setText(String.valueOf(expiredCount));
    }
    
    @FXML
    public void handleRefresh(ActionEvent event) {
        loadData();
        searchField.clear();
        AlertUtil.showAlert("Info", "Inventory data refreshed.", Alert.AlertType.INFORMATION);
    }
}