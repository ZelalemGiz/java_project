package com.pharmacy.util;

import com.pharmacy.model.User;

public class UserSession {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static void cleanUserSession() {
        currentUser = null;
    }
}
