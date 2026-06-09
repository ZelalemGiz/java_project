package com.pharmacy.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private byte[] photo;  // ✅ ADDED: Profile photo as byte array

    // Constructors
    public User() {}

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public String getRole() { 
        return role; 
    }
    
    public void setRole(String role) { 
        this.role = role; 
    }
    
    // ✅ ADDED: Photo getter and setter
    public byte[] getPhoto() { 
        return photo;  // Can return null if no photo
    }
    
    public void setPhoto(byte[] photo) { 
        this.photo = photo;  // Can be null
    }
}