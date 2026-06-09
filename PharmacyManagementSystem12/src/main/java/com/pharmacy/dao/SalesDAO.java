package com.pharmacy.dao;

import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {
    
    /**
     * Add sale with branch info (from medicine) and seller ID
     * @param sale
     * @param items
     * @return 
     */
    public boolean addSaleWithItems(Sale sale, List<SaleItem> items) {
        // ✅ 10 placeholders - removed branch_number
        String insertSaleSql = "INSERT INTO sales (buyer_name, buyer_phone, medicine_id, quantity, " +
                               "unit_price, discount, total_price, total_amount, branch_name, seller_id) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity, unit_price, discount, total_price) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            int saleId = -1;
            SaleItem firstItem = items.get(0);
            
            try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                saleStmt.setString(1, sale.getBuyerName());
                saleStmt.setString(2, sale.getBuyerPhone());
                saleStmt.setInt(3, firstItem.getMedicineId());
                saleStmt.setInt(4, sale.getQuantity());
                saleStmt.setDouble(5, sale.getUnitPrice());
                saleStmt.setDouble(6, sale.getDiscount());
                saleStmt.setDouble(7, sale.getTotalPrice());
                saleStmt.setDouble(8, sale.getTotalAmount());
                saleStmt.setString(9, sale.getBranchName());      
                
                if (sale.getSellerId() > 0) {
                    saleStmt.setInt(10, sale.getSellerId());      // ✅ Seller ID (param 10)
                } else {
                    saleStmt.setNull(10, Types.INTEGER);
                }
                
                saleStmt.executeUpdate();
                
                ResultSet rs = saleStmt.getGeneratedKeys();
                if (rs.next()) {
                    saleId = rs.getInt(1);
                }
            }
            
            if (saleId != -1) {
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                    for (SaleItem item : items) {
                        itemStmt.setInt(1, saleId);
                        itemStmt.setInt(2, item.getMedicineId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.setDouble(5, item.getDiscount());
                        itemStmt.setDouble(6, item.getTotalPrice());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }
                conn.commit();
                return true;
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
        }
    }
    
    public List<Sale> getAllSales() {
        return getSalesWithFilter(null);
    }
    
    public List<Sale> getSalesBySellerId(int sellerId) {
        return getSalesWithFilter(sellerId);
    }
    
    private List<Sale> getSalesWithFilter(Integer sellerId) {
        List<Sale> list = new ArrayList<>();
        // ✅ Get branch name from medicines table via JOIN
        String sql = "SELECT s.*, p.firstName, p.lastName, m.branch_name as med_branch " +
                     "FROM sales s " +
                     "LEFT JOIN pharmacists p ON s.seller_id = p.id " +
                     "LEFT JOIN medicines m ON s.medicine_id = m.id ";
        
        if (sellerId != null) {
            sql += "WHERE s.seller_id = ? ";
        }
        sql += "ORDER BY s.sale_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (sellerId != null) {
                pstmt.setInt(1, sellerId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapToSale(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private Sale mapToSale(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        s.setBuyerName(rs.getString("buyer_name"));
        s.setBuyerPhone(rs.getString("buyer_phone"));
        s.setMedicineId(rs.getInt("medicine_id"));
        s.setQuantity(rs.getInt("quantity"));
        s.setUnitPrice(rs.getDouble("unit_price"));
        s.setDiscount(rs.getDouble("discount"));
        s.setTotalPrice(rs.getDouble("total_price"));
        s.setTotalAmount(rs.getDouble("total_amount"));
        
        Timestamp ts = rs.getTimestamp("sale_date");
        if (ts != null) s.setSaleDate(ts.toLocalDateTime());
        
        // ✅ Branch name - try from sales table first, then from medicine
        String branchName = rs.getString("branch_name");
        if (branchName == null || branchName.isEmpty()) {
            branchName = rs.getString("med_branch");  // From medicine table
        }
        s.setBranchName(branchName);
        
        s.setSellerId(rs.getInt("seller_id"));
        
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        if (firstName != null) {
            s.setSellerName(firstName + (lastName != null ? " " + lastName : ""));
        }
        
        return s;
    }
    
    public boolean deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteSaleItems(int saleId) {
        String sql = "DELETE FROM sale_items WHERE sale_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}