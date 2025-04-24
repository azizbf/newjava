package org.example.models.user;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String email;
    private String password;
    private String roles;
    private String name;
    private int loginCount;
    private String imageUrl;
    private String numTel;
    private LocalDateTime penalizedUntil;

    // Constructor
    public User(int id,String email, String password, String roles, String name,
                int loginCount, String imageUrl, String numTel, LocalDateTime penalizedUntil) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.name = name;
        this.loginCount = loginCount;
        this.imageUrl = imageUrl;
        this.numTel = numTel;
        this.penalizedUntil = penalizedUntil;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLoginCount() { return loginCount; }
    public void setLoginCount(int loginCount) { this.loginCount = loginCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }

    public LocalDateTime getPenalizedUntil() { return penalizedUntil; }
    public void setPenalizedUntil(LocalDateTime penalizedUntil) { this.penalizedUntil = penalizedUntil; }

    // toString for Debugging
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                ", name='" + name + '\'' +
                ", loginCount=" + loginCount +
                ", imageUrl='" + imageUrl + '\'' +
                ", numTel='" + numTel + '\'' +
                ", penalizedUntil=" + penalizedUntil +
                '}';
    }
}
