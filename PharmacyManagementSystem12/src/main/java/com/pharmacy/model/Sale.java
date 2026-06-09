package com.pharmacy.model;

import java.time.LocalDateTime;

public class Sale {
    private int id;
    private String buyerName;       
    private String buyerPhone;      
    private int medicineId;
    private int quantity;
    private double unitPrice;
    private double discount;
    private double totalPrice;
    private double totalAmount;
    private LocalDateTime saleDate;
    private String branchName;
    private int sellerId;
    private String sellerName;

    public Sale() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // ✅ These match database column names
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    // ✅ Keep old names for backward compatibility
    public String getCustomerName() { return buyerName; }
    public void setCustomerName(String customerName) { this.buyerName = customerName; }

    public String getCustomerPhone() { return buyerPhone; }
    public void setCustomerPhone(String customerPhone) { this.buyerPhone = customerPhone; }

    // Other getters/setters
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getDiscount() { 
        return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
}