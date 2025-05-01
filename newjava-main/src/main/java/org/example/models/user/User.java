package org.example.models.user;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String email;
    private String roles;
    private String password;
    private String name;
    private int loginCount;
    private String imageUrl;
    private String numtel;
    private LocalDateTime penalizedUntil;

    // Default constructor
    public User() {
        this.loginCount = 0;
    }

    // Constructor with essential fields
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.loginCount = 0;
    }

    // Full constructor
    public User(String email, String roles, String password, String name, String imageUrl, String numtel) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.name = name;
        this.loginCount = 0;
        this.imageUrl = imageUrl;
        this.numtel = numtel;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }

    public LocalDateTime getPenalizedUntil() {
        return penalizedUntil;
    }

    public void setPenalizedUntil(LocalDateTime penalizedUntil) {
        this.penalizedUntil = penalizedUntil;
    }

    // Check if user is currently penalized
    public boolean isPenalized() {
        return penalizedUntil != null && LocalDateTime.now().isBefore(penalizedUntil);
    }

    // Check if user has admin role
    public boolean isAdmin() {
        return roles != null && roles.toLowerCase().contains("admin");
    }

    // Increment login count
    public void incrementLoginCount() {
        this.loginCount++;
    }

    // toString Method for Debugging
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", loginCount=" + loginCount +
                ", imageUrl='" + imageUrl + '\'' +
                ", numtel='" + numtel + '\'' +
                ", penalizedUntil=" + penalizedUntil +
                '}';
    }
}