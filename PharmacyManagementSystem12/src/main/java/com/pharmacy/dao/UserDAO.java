package com.pharmacy.dao;

import java.sql.*;

public class UserDAO {
    
    /**
     * Update username and password in users table
     * @param username
     * @param oldPassword
     * @param newPassword
     * @return 
     */
    public boolean updateCredentials(String username, String oldPassword, String newPassword) {
        String currentUsername = com.pharmacy.util.UserSession.getCurrentUser().getUsername();
        
        String checkSql = "SELECT id, password FROM users WHERE username = ?";
        String updateSql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            // ✅ Check with OLD/current username
            checkStmt.setString(1, currentUsername);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                String dbPass = rs.getString("password");
                int userId = rs.getInt("id");
                
                boolean isMatch = dbPass.equals(oldPassword);
                
                if (isMatch) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, username);   
                        updateStmt.setString(2, newPassword); 
                        updateStmt.setInt(3, userId);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ... other methods ...
}