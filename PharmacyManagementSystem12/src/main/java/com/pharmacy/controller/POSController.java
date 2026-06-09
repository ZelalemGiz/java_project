package com.pharmacy.controller;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.dao.SalesDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class POSController {

    @FXML private TextField searchField, qtyField, discountField, buyer_name, buyer_phone;
    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<Medicine, String> colMedName, colMedCategory;
    @FXML private TableColumn<Medicine, Double> colMedPrice;
    @FXML private TableColumn<Medicine, Integer> colMedStock;
    @FXML private TableColumn<CartItem, String> colCartName;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double> colCartPrice, colCartTotal;
    @FXML private Label subtotalLabel, totalLabel;

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SalesDAO salesDAO = new SalesDAO();
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    private final ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private boolean isPharmacist = false;

    public static class CartItem {
        private final SimpleIntegerProperty medicineId, quantity;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty unitPrice, total;
        
        public CartItem(int id, String name, int qty, double price) {
            this.medicineId = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(qty);
            this.unitPrice = new SimpleDoubleProperty(price);
            this.total = new SimpleDoubleProperty(qty * price);
        }
        public int getMedicineId() { return medicineId.get(); }
        public String getName() { return name.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getTotal() { return total.get(); }
    }

    @FXML
    public void initialize() {
        if (UserSession.getCurrentUser() != null) {
            isPharmacist = "PHARMACIST".equalsIgnoreCase(UserSession.getCurrentUser().getRole());
        }
        
        colMedName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMedCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colMedPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colMedStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCartName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        cartTable.setItems(cartList);

        loadMedicines();
        searchField.textProperty().addListener((obs, old, newText) -> filterMedicines(newText));
        discountField.textProperty().addListener((obs, old, newText) -> calculateTotals());
    }

    private void loadMedicines() {
        List<Medicine> medicines;
        if (isPharmacist) {
            Pharmacist ph = pharmacistDAO.getPharmacistById(UserSession.getCurrentUser().getId());
            String branch = ph != null ? ph.getBranchName() : null;
            medicines = (branch != null && !branch.isEmpty()) ? 
                medicineDAO.getMedicinesByBranch(branch) : new ArrayList<>();
        } else {
            medicines = medicineDAO.getAllMedicines();
        }
        medicineList.setAll(medicines);
        medicineTable.setItems(medicineList);
    }

    private void filterMedicines(String query) {
        if (query == null || query.isEmpty()) {
            medicineTable.setItems(medicineList);
        } else {
            medicineTable.setItems(FXCollections.observableArrayList(
                medicineList.filtered(m ->
                    m.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (m.getCategory() != null && m.getCategory().toLowerCase().contains(query.toLowerCase())))
            ));
        }
    }

    @FXML
    public void handleAddToCart(ActionEvent event) {
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showAlert("Warning", "Select a medicine.", Alert.AlertType.WARNING); return; }
        try {
            int qty = Integer.parseInt(qtyField.getText());
            if (qty <= 0) throw new NumberFormatException();
            if (qty > selected.getStock()) {
                AlertUtil.showAlert("Error", "Not enough stock. Only " + selected.getStock() + " left.", Alert.AlertType.ERROR);
                return;
            }
            if (cartList.stream().anyMatch(c -> c.getMedicineId() == selected.getId())) {
                AlertUtil.showAlert("Warning", "Already in cart.", Alert.AlertType.WARNING); return;
            }
            cartList.add(new CartItem(selected.getId(), selected.getName(), qty, selected.getSellingPrice()));
            calculateTotals(); qtyField.setText("1");
        } catch (NumberFormatException e) { AlertUtil.showAlert("Error", "Invalid quantity.", Alert.AlertType.ERROR); }
    }

    @FXML
    public void handleRemoveFromCart(ActionEvent event) {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) { cartList.remove(selected); calculateTotals(); }
        else AlertUtil.showAlert("Warning", "Select an item.", Alert.AlertType.WARNING);
    }

    private void calculateTotals() {
        double subtotal = cartList.stream().mapToDouble(CartItem::getTotal).sum();
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        
        // ✅ Discount as percentage (e.g., 10 = 10%)
        double discountPercent = 0;
        try {
            discountPercent = Double.parseDouble(discountField.getText());
            if (discountPercent < 0) discountPercent = 0;
            if (discountPercent > 100) discountPercent = 100;
        } catch (NumberFormatException e) {}
        
        double discountAmount = subtotal * (discountPercent / 100.0);
        double total = Math.max(0, subtotal - discountAmount);
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    public void handleCancelSale(ActionEvent event) {
        cartList.clear(); calculateTotals(); qtyField.setText("1");
        discountField.setText("0"); buyer_name.clear(); buyer_phone.clear();
    }

    @FXML
    public void handleCompleteSale(ActionEvent event) {
        if (cartList.isEmpty()) { AlertUtil.showAlert("Warning", "Cart is empty.", Alert.AlertType.WARNING); return; }
        try {
            double subtotal = cartList.stream().mapToDouble(CartItem::getTotal).sum();
            double discountPercent = Double.parseDouble(discountField.getText().isEmpty() ? "0" : discountField.getText());
            double discountAmount = subtotal * (discountPercent / 100.0);
            double total = Math.max(0, subtotal - discountAmount);

            List<SaleItem> items = new ArrayList<>();
            for (CartItem ci : cartList) {
                SaleItem si = new SaleItem();
                si.setMedicineId(ci.getMedicineId()); si.setQuantity(ci.getQuantity()); si.setUnitPrice(ci.getUnitPrice());
                // ✅ Proportional discount per item
                double itemDiscount = subtotal > 0 ? (ci.getTotal() / subtotal) * discountAmount : 0;
                si.setDiscount(itemDiscount);
                si.setTotalPrice(ci.getTotal() - itemDiscount);
                items.add(si);
            }

            Sale sale = new Sale();
            sale.setCustomerName(buyer_name.getText().isEmpty() ? "Walk-in Customer" : buyer_name.getText());
            sale.setCustomerPhone(buyer_phone.getText().isEmpty() ? "N/A" : buyer_phone.getText());
            if (!items.isEmpty()) {
                SaleItem fi = items.get(0);
                sale.setMedicineId(fi.getMedicineId()); sale.setQuantity(fi.getQuantity());
                sale.setUnitPrice(fi.getUnitPrice()); sale.setDiscount(fi.getDiscount()); sale.setTotalPrice(fi.getTotalPrice());
            }
            sale.setTotalAmount(total); sale.setSaleDate(LocalDateTime.now());
            
        if (!items.isEmpty()) {
         Medicine firstMed = medicineDAO.getMedicineById(items.get(0).getMedicineId());
           sale.setBranchName(firstMed != null ? firstMed.getBranchName() : "Main Branch");
           } else {
           sale.setBranchName("Main Branch");
          }

        if (UserSession.getCurrentUser() != null) {
             sale.setSellerId(UserSession.getCurrentUser().getId());
             }

            if (salesDAO.addSaleWithItems(sale, items)) {
                for (CartItem ci : cartList) {
                    Medicine m = medicineDAO.getMedicineById(ci.getMedicineId());
                    if (m != null) { m.setStock(m.getStock() - ci.getQuantity()); medicineDAO.updateMedicine(m); }
                }
                generateReceipt(sale, total, discountPercent, subtotal);
                AlertUtil.showAlert("Success", "Transaction complete!", Alert.AlertType.INFORMATION);
                handleCancelSale(null); loadMedicines();
            } else AlertUtil.showAlert("Error", "Failed to save.", Alert.AlertType.ERROR);
        } catch (Exception e) { AlertUtil.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    private void generateReceipt(Sale sale, double total, double discountPercent, double subtotal) {
        String name = buyer_name.getText().isEmpty() ? "Walk-in Customer" : buyer_name.getText();
        double discountAmount = subtotal * (discountPercent / 100.0);
        try {
            new File("src/main/resources/bills").mkdirs();
            String filename = "src/main/resources/bills/Receipt_" + System.currentTimeMillis() + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(filename));
            doc.open();
            doc.add(new Paragraph("=========================================="));
            doc.add(new Paragraph("  " + (sale.getBranchName() != null ? sale.getBranchName().toUpperCase() : "PHARMACY")));
            doc.add(new Paragraph("=========================================="));
            doc.add(new Paragraph("Date: " + LocalDateTime.now()));
            doc.add(new Paragraph("Customer: " + name));
            if (UserSession.getCurrentUser() != null) doc.add(new Paragraph("Served by: " + UserSession.getCurrentUser().getUsername()));
            doc.add(new Paragraph(" "));
            
            PdfPTable table = new PdfPTable(4);
            table.addCell("Item"); table.addCell("Qty"); table.addCell("Price"); table.addCell("Total price");
            for (CartItem ci : cartList) {
                table.addCell(ci.getName()); table.addCell(String.valueOf(ci.getQuantity()));
                table.addCell(String.format("$%.2f", ci.getUnitPrice())); table.addCell(String.format("$%.2f", ci.getTotal()));
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Subtotal: $" + String.format("%.2f", subtotal)));
            doc.add(new Paragraph("Discount: " + String.format("%.1f", discountPercent) + "% (-$" + String.format("%.2f", discountAmount) + ")"));
            doc.add(new Paragraph("TOTAL PAID: $" + String.format("%.2f", total)));
            doc.add(new Paragraph("=========================================="));
            doc.add(new Paragraph("      Thank you, visit again!"));
            doc.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleBack(ActionEvent e) { goToDashboard(e); }
    
    private void goToDashboard(ActionEvent e) {
        try {
            Parent v = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/dashboard.fxml"));
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(v)); s.centerOnScreen();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}