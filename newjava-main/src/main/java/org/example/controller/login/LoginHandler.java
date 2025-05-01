package org.example.controller.login;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.menu.FrontMenu;
import org.example.controller.menu.menu;
import org.example.models.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.dataSource;

/**
 * Handles the login process and directs users to the appropriate menu based on their role
 */
public class LoginHandler {
    
    /**
     * Show the appropriate menu based on the user object
     * 
     * @param user The authenticated user object
     * @param stage The primary stage to show the menu on
     */
    public static void showMenuForUser(User user, Stage stage) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            
            FXMLLoader loader;
            
            if (user.isAdmin()) {
                // Admin users see the admin menu
                System.out.println("Loading admin dashboard for user: " + user.getEmail());
                loader = new FXMLLoader(LoginHandler.class.getResource("/Menu/menu.fxml"));
                Parent root = loader.load();
                menu adminMenu = loader.getController();
                
                // Set up stage
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Esprit Admin Panel");
                stage.show();
            } else {
                // Regular users see the front-end menu
                System.out.println("Loading user interface for user: " + user.getEmail());
                loader = new FXMLLoader(LoginHandler.class.getResource("/Menu/front_menu.fxml"));
                Parent root = loader.load();
                FrontMenu frontMenu = loader.getController();
                
                // Set user ID for the menu controller
                frontMenu.setCurrentUserId(user.getId());
                
                // Set up stage
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Esprit Learning Platform");
                stage.show();
            }
        } catch (Exception e) {
            System.err.println("Error loading menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle login with email and password
     * Authenticates against the database
     * 
     * @param email User's email
     * @param password User's password
     * @param stage Primary stage
     * @return true if login was successful
     */
    public static boolean handleLogin(String email, String password, Stage stage) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dataSource.getInstance().getConnection();
            String query = "SELECT * FROM user WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                // User exists, now check password
                String dbPassword = rs.getString("password");
                
                if (password.equals(dbPassword)) {
                    System.out.println("✅ Password matches for user: " + email);
                    
                    // Create User object from database result
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRoles(rs.getString("roles"));
                    user.setName(rs.getString("name"));
                    user.setLoginCount(rs.getInt("login_count"));
                    user.setImageUrl(rs.getString("image_url"));
                    user.setNumtel(rs.getString("numtel"));
                    
                    // Get penalized_until if not null
                    java.sql.Timestamp penalizedTimestamp = rs.getTimestamp("penalized_until");
                    if (penalizedTimestamp != null) {
                        user.setPenalizedUntil(penalizedTimestamp.toLocalDateTime());
                    }
                    
                    // Check if user is penalized
                    if (user.isPenalized()) {
                        System.out.println("❌ User is penalized until: " + user.getPenalizedUntil());
                        return false;
                    }
                    
                    // Increment login count
                    user.incrementLoginCount();
                    
                    // Update login count in database
                    updateLoginCount(user.getId(), user.getLoginCount());
                    
                    // Show the appropriate menu
                    showMenuForUser(user, stage);
                    return true;
                } else {
                    System.out.println("❌ Password doesn't match for user: " + email);
                }
            } else {
                System.out.println("❌ No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) dataSource.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Update user's login count in the database
     */
    private static void updateLoginCount(int userId, int loginCount) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = dataSource.getInstance().getConnection();
            String query = "UPDATE user SET login_count = ? WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, loginCount);
            stmt.setInt(2, userId);
            int updatedRows = stmt.executeUpdate();
            System.out.println("Updated login count for user ID " + userId + ": " + updatedRows + " rows affected");
        } catch (SQLException e) {
            System.err.println("❌ Error updating login count: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) dataSource.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
} 