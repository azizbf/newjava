package org.example.models;

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

    // Constructeur par défaut
    public User() {
    }

    // Constructeur avec tous les champs
    public User(int id, String email, String roles, String password, String name, 
               int loginCount, String imageUrl, String numtel, LocalDateTime penalizedUntil) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.name = name;
        this.loginCount = loginCount;
        this.imageUrl = imageUrl;
        this.numtel = numtel;
        this.penalizedUntil = penalizedUntil;
    }

    // Getters et Setters
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

    // Méthode toString pour l'affichage
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", name='" + name + '\'' +
                ", loginCount=" + loginCount +
                ", imageUrl='" + imageUrl + '\'' +
                ", numtel='" + numtel + '\'' +
                ", penalizedUntil=" + penalizedUntil +
                '}';
    }

    // Méthodes utilitaires
    public boolean isPenalized() {
        if (penalizedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(penalizedUntil);
    }

    public void incrementLoginCount() {
        this.loginCount++;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
} 