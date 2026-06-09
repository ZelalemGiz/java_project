package com.pharmacy.dao;

import com.pharmacy.model.Medicine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {
    
    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapToMedicine(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    /**
 * Get medicines by branch name
     * @param branchName
     * @return 
 */
public List<Medicine> getMedicinesByBranch(String branchName) {
    List<Medicine> list = new ArrayList<>();
    String sql = "SELECT * FROM medicines WHERE branch_name = ? ORDER BY name";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, branchName);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            list.add(mapToMedicine(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

    public boolean addMedicine(Medicine m) {
        String sql = "INSERT INTO medicines (name, category, cost_price, price, profit, stock, status, expiry_date, branch_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getCategory());
            pstmt.setDouble(3, m.getCostPrice());
            pstmt.setDouble(4, m.getSellingPrice());
            pstmt.setDouble(5, m.getProfit());
            pstmt.setInt(6, m.getStock());
            pstmt.setString(7, m.getStatus());
            if (m.getExpiryDate() != null) {
                pstmt.setDate(8, Date.valueOf(m.getExpiryDate()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            pstmt.setString(9, m.getBranchName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name=?, category=?, cost_price=?, price=?, profit=?, stock=?, status=?, expiry_date=?, branch_name=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getCategory());
            pstmt.setDouble(3, m.getCostPrice());
            pstmt.setDouble(4, m.getSellingPrice());
            pstmt.setDouble(5, m.getProfit());
            pstmt.setInt(6, m.getStock());
            pstmt.setString(7, m.getStatus());
            if (m.getExpiryDate() != null) {
                pstmt.setDate(8, Date.valueOf(m.getExpiryDate()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            pstmt.setString(9, m.getBranchName());
            pstmt.setInt(10, m.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete medicine - removes related records first to avoid FK constraint error
     * @param id
     * @return 
     */
    public boolean deleteMedicine(int id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Delete related sale_items
            String deleteSaleItems = "DELETE FROM sale_items WHERE medicine_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSaleItems)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            
            // Step 2: Delete related sales
            String deleteSales = "DELETE FROM sales WHERE medicine_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSales)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            
            // Step 3: Delete related medicine_batches
            String deleteBatches = "DELETE FROM medicine_batches WHERE medicine_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBatches)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            
            // Step 4: Delete the medicine
            String deleteMedicine = "DELETE FROM medicines WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMedicine)) {
                pstmt.setInt(1, id);
                int result = pstmt.executeUpdate();
                conn.commit();
                return result > 0;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Remove expired medicines - safely with related record cleanup
     * @return 
     */
    public int removeExpiredMedicines() {
        Connection conn = null;
        int deletedCount = 0;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Delete sale_items for expired medicines
            String deleteSaleItemsSql = 
                "DELETE si FROM sale_items si " +
                "INNER JOIN medicines m ON si.medicine_id = m.id " +
                "WHERE m.expiry_date < CURDATE()";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(deleteSaleItemsSql);
            }
            
            // Step 2: Delete sales for expired medicines
            String deleteSalesSql = 
                "DELETE s FROM sales s " +
                "INNER JOIN medicines m ON s.medicine_id = m.id " +
                "WHERE m.expiry_date < CURDATE()";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(deleteSalesSql);
            }
            
            // Step 3: Delete medicine_batches for expired medicines
            String deleteBatchesSql = 
                "DELETE mb FROM medicine_batches mb " +
                "INNER JOIN medicines m ON mb.medicine_id = m.id " +
                "WHERE m.expiry_date < CURDATE()";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(deleteBatchesSql);
            }
            
            // Step 4: Delete expired medicines
            String deleteMedicinesSql = "DELETE FROM medicines WHERE expiry_date < CURDATE()";
            try (Statement stmt = conn.createStatement()) {
                deletedCount = stmt.executeUpdate(deleteMedicinesSql);
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return deletedCount;
    }

    public boolean updateStock(int medicineId, int newStock) {
        String sql = "UPDATE medicines SET stock = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, medicineId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Medicine getMedicineById(int id) {
        String sql = "SELECT * FROM medicines WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToMedicine(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Helper method to map ResultSet to Medicine object
     */
    private Medicine mapToMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setCostPrice(rs.getDouble("cost_price"));
        m.setSellingPrice(rs.getDouble("price"));
        m.setProfit(rs.getDouble("profit"));
        m.setStock(rs.getInt("stock"));
        m.setStatus(rs.getString("status"));
        
        Timestamp addDateTs = rs.getTimestamp("add_date");
        if (addDateTs != null) {
            m.setDateAdded(addDateTs.toLocalDateTime());
        }
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            m.setExpiryDate(expiryDate.toLocalDate());
        }
        
        m.setBranchName(rs.getString("branch_name"));
        return m;
    }
}