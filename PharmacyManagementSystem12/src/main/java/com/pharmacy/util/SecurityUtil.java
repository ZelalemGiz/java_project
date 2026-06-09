package com.pharmacy.util;

import org.mindrot.jbcrypt.BCrypt;

public class SecurityUtil {
    
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
    
    public static boolean checkPassword(String candidate, String hashed) {
        try {
            return BCrypt.checkpw(candidate, hashed);
        } catch (Exception e) {
            return false; // In case the hash isn't valid bcrypt format
        }
    }
}
