package com.pharmacy.dao;

import com.pharmacy.model.Pharmacist;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PharmacistDAO {
    

    private Pharmacist mapToPharmacist(ResultSet rs) throws SQLException {
        Pharmacist p = new Pharmacist();
        p.setId(rs.getInt("id"));
        
        Blob photoBlob = rs.getBlob("photo");
        if (photoBlob != null) {
            p.setPhoto(photoBlob.getBytes(1, (int) photoBlob.length()));
        }
        
        p.setIdNumber(rs.getString("idNumber"));
        p.setFirstName(rs.getString("firstName"));
        p.setMiddleName(rs.getString("middleName"));
        p.setLastName(rs.getString("lastName"));
        p.setGender(rs.getString("gender"));
        
        Date dob = rs.getDate("dateofbirth");
        if (dob != null) {
            p.setDateOfBirth(dob.toLocalDate());
        }
        
        p.setNationality(rs.getString("nationality"));
        p.setEducationLevel(rs.getString("education_level"));
        p.setSalary(rs.getDouble("salary"));
        p.setPhoneNumber(rs.getString("phoneNumber"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setPassword(rs.getString("password"));
        p.setRole(rs.getString("role"));
        p.setSecurityQuestion(rs.getString("security_question"));
        p.setSecurityAnswer(rs.getString("security_answer"));
        
        Timestamp empDate = rs.getTimestamp("employing_date");
        if (empDate != null) {
            p.setEmployingDate(empDate.toLocalDateTime());
        }
        p.setBranchName(rs.getString("branch_name"));
        
        return p;
    }
    
    public List<Pharmacist> getAllPharmacists() {
        List<Pharmacist> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacists";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapToPharmacist(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Pharmacist getPharmacistById(int id) {
        String sql = "SELECT * FROM pharmacists WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToPharmacist(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addPharmacist(Pharmacist p) {
        String sql = "INSERT INTO pharmacists (photo, idNumber, firstName, middleName, lastName, gender, " +
                     "dateofbirth, nationality, education_level, salary, phoneNumber, email, address, " +
                     "password, role, security_question, security_answer, branch_name) VALUES (?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                if (p.getPhoto() != null) {
                    pstmt.setBlob(1, new javax.sql.rowset.serial.SerialBlob(p.getPhoto()));
                } else {
                    pstmt.setNull(1, Types.BLOB);
                }
                
                pstmt.setString(2, p.getIdNumber());
                pstmt.setString(3, p.getFirstName());
                pstmt.setString(4, p.getMiddleName());
                pstmt.setString(5, p.getLastName());
                pstmt.setString(6, p.getGender());
                
                if (p.getDateOfBirth() != null) {
                    pstmt.setDate(7, Date.valueOf(p.getDateOfBirth()));
                } else {
                    pstmt.setNull(7, Types.DATE);
                }
                
                pstmt.setString(8, p.getNationality());
                pstmt.setString(9, p.getEducationLevel());
                pstmt.setDouble(10, p.getSalary());
                pstmt.setString(11, p.getPhoneNumber());
                pstmt.setString(12, p.getEmail());
                pstmt.setString(13, p.getAddress());
                pstmt.setString(14, p.getPassword());
                pstmt.setString(15, p.getRole());
                pstmt.setString(16, p.getSecurityQuestion());
                pstmt.setString(17, p.getSecurityAnswer());
                pstmt.setString(18, p.getBranchName());
                
                if (pstmt.executeUpdate() > 0) {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        int pId = rs.getInt(1);
                        
                        String userSql = "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement uStmt = conn.prepareStatement(userSql)) {
                            uStmt.setInt(1, pId);
                            uStmt.setString(2, p.getEmail());
                            uStmt.setString(3, p.getPassword());
                            uStmt.setString(4, p.getRole());
                            uStmt.executeUpdate();
                        }
                        
                        conn.commit();
                        return true;
                    }
                }
                
                conn.rollback();
                return false;
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

    public boolean updatePharmacist(Pharmacist p) {
        String sql = "UPDATE pharmacists SET photo=?, idNumber=?, firstName=?, middleName=?, lastName=?, " +
             "gender=?, dateofbirth=?, nationality=?, education_level=?, salary=?, phoneNumber=?, " +
             "email=?, address=?, password=?, role=?, security_question=?, security_answer=?,branch_name=? WHERE id=?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                if (p.getPhoto() != null) {
                    pstmt.setBlob(1, new javax.sql.rowset.serial.SerialBlob(p.getPhoto()));
                } else {
                    pstmt.setNull(1, Types.BLOB);
                }
                
                pstmt.setString(2, p.getIdNumber());
                pstmt.setString(3, p.getFirstName());
                pstmt.setString(4, p.getMiddleName());
                pstmt.setString(5, p.getLastName());
                pstmt.setString(6, p.getGender());
                
                if (p.getDateOfBirth() != null) {
                    pstmt.setDate(7, Date.valueOf(p.getDateOfBirth()));
                } else {
                    pstmt.setNull(7, Types.DATE);
                }
                
                pstmt.setString(8, p.getNationality());
                pstmt.setString(9, p.getEducationLevel());
                pstmt.setDouble(10, p.getSalary());
                pstmt.setString(11, p.getPhoneNumber());
                pstmt.setString(12, p.getEmail());
                pstmt.setString(13, p.getAddress());
                pstmt.setString(14, p.getPassword());
                pstmt.setString(15, p.getRole());
                pstmt.setString(16, p.getSecurityQuestion());
                pstmt.setString(17, p.getSecurityAnswer());
                pstmt.setString(18, p.getBranchName());
                pstmt.setInt(19, p.getId());
                int result = pstmt.executeUpdate();
                
                String userSql = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
                try (PreparedStatement uStmt = conn.prepareStatement(userSql)) {
                    uStmt.setString(1, p.getEmail());
                    uStmt.setString(2, p.getPassword());
                    uStmt.setString(3, p.getRole());
                    uStmt.setInt(4, p.getId());
                    uStmt.executeUpdate();
                }
                
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

    public boolean deletePharmacist(int id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement uStmt = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
                uStmt.setInt(1, id);
                uStmt.executeUpdate();
            }
            
            try (PreparedStatement pStmt = conn.prepareStatement("DELETE FROM pharmacists WHERE id=?")) {
                pStmt.setInt(1, id);
                int result = pStmt.executeUpdate();
                conn.commit();
                return result > 0;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e)
                       {
                    e.printStackTrace(); }
            }
        }
    }

    public Pharmacist getPharmacistByEmail(String email) {
        String sql = "SELECT * FROM pharmacists WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToPharmacist(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
 * Get pharmacists by branch name
     * @param branchName
     * @return 
 */
public List<Pharmacist> getPharmacistsByBranch(String branchName) {
    List<Pharmacist> list = new ArrayList<>();
    String sql = "SELECT * FROM pharmacists WHERE branch_name = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, branchName);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            list.add(mapToPharmacist(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
}