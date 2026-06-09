package com.pharmacy.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DashboardDAO {
    
    public double getDailySales() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public int getCustomerCount() {
        String sql = "SELECT COUNT(DISTINCT buyer_name) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public Map<LocalDate, Double> getRevenueLast7Days() {
        Map<LocalDate, Double> revenueMap = new LinkedHashMap<>();
        String sql = "SELECT DATE(sale_date) as sale_day, SUM(total_amount) as total " +
                     "FROM sales WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                     "GROUP BY DATE(sale_date) ORDER BY sale_day";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate date = rs.getDate("sale_day").toLocalDate();
                double total = rs.getDouble("total");
                revenueMap.put(date, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenueMap;
    }
    
    public Map<String, Integer> getTopSellingMedicines() {
        Map<String, Integer> topMeds = new LinkedHashMap<>();
        String sql = "SELECT m.name, SUM(s.quantity) as total_qty " +
                     "FROM sales s JOIN medicines m ON s.medicine_id = m.id " +
                     "GROUP BY m.name ORDER BY total_qty DESC LIMIT 5";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                topMeds.put(rs.getString("name"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topMeds;
    }
    /**
 * Get daily sales for a specific seller (pharmacist)
     * @param sellerId
     * @return 
 */
public double getDailySalesBySeller(int sellerId) {
    String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE() AND seller_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, sellerId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) return rs.getDouble(1);
    } catch (SQLException e) { e.printStackTrace(); }
    return 0.0;
}
}