package com.pharmacy.service;

import com.pharmacy.dao.SalesDAO;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillingService {
    private final SalesDAO salesDAO = new SalesDAO();

    /**
     * Process a sale with items and generate bill
     * @param sale
     * @param items
     * @return 
     */
    public boolean processSale(Sale sale, List<SaleItem> items) {
        boolean success = salesDAO.addSaleWithItems(sale, items);
        if (success) {
            generateBill(sale);
        }
        return success;
    }
    
    /**
     * Simple sale without items
     * @param sale
     * @return 
     */
    public boolean processSale(Sale sale) {
        List<SaleItem> items = new java.util.ArrayList<>();
        SaleItem item = new SaleItem();
        item.setMedicineId(sale.getMedicineId());
        item.setQuantity(sale.getQuantity());
        item.setUnitPrice(sale.getUnitPrice());
        item.setDiscount(sale.getDiscount());
        item.setTotalPrice(sale.getTotalPrice());
        items.add(item);
        
        boolean success = salesDAO.addSaleWithItems(sale, items);
        if (success) {
            generateBill(sale);
        }
        return success;
    }
    
    /**
     * Generate bill file in resources/bills folder
     */
    private void generateBill(Sale sale) {
        try {
            File billDir = new File("src/main/resources/bills");
            if (!billDir.exists()) billDir.mkdirs();
            
            String filename = "Bill_" + sale.getId() + "_" + System.currentTimeMillis() + ".csv";
            FileWriter w = new FileWriter(new File(billDir, filename));
            
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            w.write("=== PHARMACY BILL ===\n");
            w.write("Bill No: BILL-" + String.format("%05d", sale.getId()) + "\n");
            w.write("Date: " + sale.getSaleDate().format(fmt) + "\n");
            w.write("Customer: " + sale.getBuyerName() + "\n");
            w.write("Phone: " + sale.getBuyerPhone() + "\n");
            w.write("Branch: " + sale.getBranchName() + "\n");
            w.write("Seller: " + (sale.getSellerName() != null ? sale.getSellerName() : "N/A") + "\n");
            w.write("-----------------------------\n");
            w.write("Medicine ID: " + sale.getMedicineId() + "\n");
            w.write("Quantity: " + sale.getQuantity() + "\n");
            w.write("Unit Price: $" + String.format("%.2f", sale.getUnitPrice()) + "\n");
            w.write("Discount: $" + String.format("%.2f", sale.getDiscount()) + "\n");
            w.write("Total: $" + String.format("%.2f", sale.getTotalAmount()) + "\n");
            w.write("=============================\n");
            
            w.close();
            System.out.println("Bill generated: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}