package com.pharmacy.controller;

import com.pharmacy.dao.SalesDAO;
import com.pharmacy.model.Sale;
import com.pharmacy.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ReportsController {
    
    @FXML private TableView<Sale> reportsTable;
    @FXML private Label labelTotalRevenue;
    @FXML private Label labelTotalSales;
    @FXML private Label labelDiscount;
    
    private SalesDAO salesDAO = new SalesDAO();
    
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
        // FXML column order:
        // 0: Sale ID
        // 1: Total Amount (quantity)
        // 2: Pharmacist(seller) ID
        // 3: Total price ($)
        // 4: Date
        // 5: (empty column for actions)
        
        TableColumn<Sale, Integer> colId = (TableColumn<Sale, Integer>) reportsTable.getColumns().get(0);
        TableColumn<Sale, Integer> colQuantity = (TableColumn<Sale, Integer>) reportsTable.getColumns().get(1);
        TableColumn<Sale, Integer> colSellerId = (TableColumn<Sale, Integer>) reportsTable.getColumns().get(2);
        TableColumn<Sale, Double> colTotalPrice = (TableColumn<Sale, Double>) reportsTable.getColumns().get(3);
        TableColumn<Sale, LocalDateTime> colDate = (TableColumn<Sale, LocalDateTime>) reportsTable.getColumns().get(4);
        TableColumn<Sale, String> colAction = (TableColumn<Sale, String>) reportsTable.getColumns().get(5);

        // Set cell value factories
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSellerId.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        
        // Action column - set text to empty since there's nothing to show
        colAction.setText("");
        colAction.setCellValueFactory(cellData -> null);

        // Format columns
        formatDateColumn(colDate);
        formatCurrencyColumn(colTotalPrice);
        formatCenterColumn(colId);
        formatCenterColumn(colQuantity);
        formatCenterColumn(colSellerId);

        loadData();
    }
    
    private void formatDateColumn(TableColumn<Sale, LocalDateTime> column) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        column.setCellFactory(col -> new TableCell<Sale, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
    }
    
    private void formatCurrencyColumn(TableColumn<Sale, Double> column) {
        column.setCellFactory(col -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
    }
    
    private void formatCenterColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER;");
    }

    private void loadData() {
        reportsTable.setItems(FXCollections.observableArrayList(salesDAO.getAllSales()));
        reportsTable.refresh();
        updateSummaryCards();
    }
    
    private void updateSummaryCards() {
        double totalRevenue = 0;
        double totalSalesAmount = 0;
        double totalDiscounts = 0;
        
        for (Sale sale : reportsTable.getItems()) {
            totalRevenue += sale.getTotalAmount();
            totalSalesAmount += sale.getTotalPrice();
            totalDiscounts += sale.getDiscount();
        }
        
        int totalSalesCount = reportsTable.getItems().size();
        
        labelTotalRevenue.setText(String.format("$%.2f", totalRevenue));
        labelTotalSales.setText(String.format("$%.2f", totalSalesAmount));
        labelDiscount.setText(String.format("$%.2f", totalDiscounts));
    }

    @FXML
    public void handleExportCSV(ActionEvent event) {
        try {
            String filename = "Sales_Report_" + System.currentTimeMillis() + ".csv";
            FileWriter writer = new FileWriter(filename);
            
            // Header matching table columns
            writer.write("Sale ID,Total Amount (quantity),Pharmacist(seller) ID,Total price ($),Date\n");
            
            // Data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Sale sale : reportsTable.getItems()) {
                writer.write(String.format("%d,%d,%d,%.2f,%s\n",
                    sale.getId(),
                    sale.getQuantity(),
                    sale.getMedicineId(),
                    sale.getTotalPrice(),
                    sale.getSaleDate() != null ? sale.getSaleDate().format(formatter) : "N/A"
                ));
            }
            
            // Add summary at end
            writer.write("\n");
            writer.write("Summary\n");
            writer.write("Total Revenue," + labelTotalRevenue.getText() + "\n");
            writer.write("Total Sales," + labelTotalSales.getText() + "\n");
            writer.write("Total Discounts," + labelDiscount.getText() + "\n");
            
            writer.close();
            AlertUtil.showAlert("Success", "Report exported to:\n" + filename, Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Failed to export report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    public void handleRefresh(ActionEvent event) {
        loadData();
        AlertUtil.showAlert("Info", "Report data refreshed.", Alert.AlertType.INFORMATION);
    }
}