package com.pharmacy.controller;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.dao.SalesDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.model.Sale;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import java.io.File;
import java.io.FileWriter;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalesController {
    @FXML private TableView<Sale> salesTable;
    @FXML private TextField searchField;
    @FXML private Button btnDeleteSale;
    private final SalesDAO salesDAO = new SalesDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    private boolean isPharmacist = false;

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void initialize() {
        if (UserSession.getCurrentUser() != null) {
            isPharmacist = "PHARMACIST".equalsIgnoreCase(UserSession.getCurrentUser().getRole());
        }
        if (isPharmacist && btnDeleteSale != null) {
            btnDeleteSale.setVisible(false);
            btnDeleteSale.setManaged(false);
        }
        
        TableColumn<Sale, Integer> colId = getColumn(0);
        TableColumn<Sale, LocalDateTime> colDate = getColumn(1);
        TableColumn<Sale, String> colBuyerName = getColumn(2);
        TableColumn<Sale, String> colBuyerPhone = getColumn(3);
        TableColumn<Sale, Integer> colMedId = getColumn(4);
        TableColumn<Sale, Integer> colQty = getColumn(5);
        TableColumn<Sale, Double> colUnitPrice = getColumn(6);
        TableColumn<Sale, Double> colDiscount = getColumn(7);
        TableColumn<Sale, Double> colTotalPrice = getColumn(8);
        TableColumn<Sale, Double> colTotalAmount = getColumn(9);
        TableColumn<Sale, String> colBranch = getColumn(10);
        TableColumn<Sale, String> colSeller = getColumn(11);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colBuyerName.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        colBuyerPhone.setCellValueFactory(new PropertyValueFactory<>("buyerPhone"));
        colMedId.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("sellerName"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colDate.setCellFactory(col -> new TableCell<Sale, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(fmt));
            }
        });
        formatCurrencyColumn(colUnitPrice);
        formatCurrencyColumn(colDiscount);
        formatCurrencyColumn(colTotalPrice);
        formatCurrencyColumn(colTotalAmount);
        colId.setStyle("-fx-alignment: CENTER;");
        colQty.setStyle("-fx-alignment: CENTER;");
        colMedId.setStyle("-fx-alignment: CENTER;");

        loadData();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newText) -> filterSales(newText));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<Sale, T> getColumn(int index) {
        return (TableColumn<Sale, T>) salesTable.getColumns().get(index);
    }
    
    private void formatCurrencyColumn(TableColumn<Sale, Double> col) {
        col.setCellFactory(c -> {
            return new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : String.format("$%.2f", item));
                }
            };
        });
    }

    private void loadData() {
        List<Sale> sales = isPharmacist ? 
            salesDAO.getSalesBySellerId(UserSession.getCurrentUser().getId()) : 
            salesDAO.getAllSales();
        
        // ✅ Force branch name from medicine table
        for (Sale s : sales) {
            Medicine med = medicineDAO.getMedicineById(s.getMedicineId());
            if (med != null && med.getBranchName() != null) {
                s.setBranchName(med.getBranchName());
            } else if (s.getBranchName() == null) {
                s.setBranchName("N/A");
            }
            if (s.getSellerName() == null && s.getSellerId() > 0) {
                Pharmacist ph = pharmacistDAO.getPharmacistById(s.getSellerId());
                if (ph != null) s.setSellerName(ph.getFirstName() + " " + ph.getLastName());
            }
        }
        salesTable.setItems(FXCollections.observableArrayList(sales));
        salesTable.refresh();
    }
    
    private void filterSales(String query) {
        if (query == null || query.isEmpty()) { loadData(); return; }
        loadData(); // Reload then filter
        salesTable.setItems(FXCollections.observableArrayList(
            salesTable.getItems().stream()
                .filter(s -> matches(s, query.toLowerCase()))
                .collect(java.util.stream.Collectors.toList())
        ));
    }
    
    private boolean matches(Sale s, String q) {
        return (s.getBuyerName() != null && s.getBuyerName().toLowerCase().contains(q)) ||
               (s.getBuyerPhone() != null && s.getBuyerPhone().contains(q)) ||
               String.valueOf(s.getId()).contains(q) ||
               (s.getSellerName() != null && s.getSellerName().toLowerCase().contains(q)) ||
               (s.getBranchName() != null && s.getBranchName().toLowerCase().contains(q));
    }

    @FXML
    public void handleDeleteSale(ActionEvent event) {
        Sale selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showAlert("Error", "Select a sale.", Alert.AlertType.ERROR); return; }
        new Alert(Alert.AlertType.CONFIRMATION, "Delete Sale #" + selected.getId() + "?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    salesDAO.deleteSaleItems(selected.getId());
                    if (salesDAO.deleteSale(selected.getId())) { 
                        AlertUtil.showAlert("Success", "Deleted.", Alert.AlertType.INFORMATION);
                        loadData(); 
                    }
                }
            });
    }

    @FXML
    public void handleExportToCSV(ActionEvent event) {
        if (salesTable.getItems().isEmpty()) { AlertUtil.showAlert("Warning", "No data.", Alert.AlertType.WARNING); return; }
        try {
            new File("src/main/resources/bills").mkdirs();
            FileWriter w = new FileWriter("src/main/resources/bills/Sale_" + System.currentTimeMillis() + ".csv");
            w.write("ID,Date,Buyer,Phone,Medicine,Qty,Price,Discount,Total,Amount,Branch,Seller\n");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Sale s : salesTable.getItems()) {
                w.write(String.format("%d,%s,%s,%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%s,%s\n",
                    s.getId(), s.getSaleDate() != null ? s.getSaleDate().format(fmt) : "",
                    s.getBuyerName(), s.getBuyerPhone(), s.getMedicineId(), s.getQuantity(),
                    s.getUnitPrice(), s.getDiscount(), s.getTotalPrice(), s.getTotalAmount(),
                    s.getBranchName(), s.getSellerName()));
            }
            w.close();
            AlertUtil.showAlert("Success", "Exported.", Alert.AlertType.INFORMATION);
        } catch (Exception e) { AlertUtil.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML public void handleRefresh(ActionEvent event) { loadData(); }
}