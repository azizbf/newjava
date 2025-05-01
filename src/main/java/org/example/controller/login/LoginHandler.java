package org.example.controller.login;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.menu.FrontMenu;
import org.example.controller.menu.menu;
import org.example.util.UserSession;

/**
 * Handles the login process and directs users to the appropriate menu based on their role
 */
public class LoginHandler {
    
    // User IDs 1-10 are considered admin users for this example
    private static final int ADMIN_USER_THRESHOLD = 10;
    
    /**
     * Show the appropriate menu based on user ID
     * 
     * @param userId The ID of the authenticated user
     * @param stage The primary stage to show the menu on
     */
    public static void showMenuForUser(int userId, String userEmail, Stage stage) {
        try {
            // Store user info in session
            UserSession.getInstance().setUserId(userId);
            UserSession.getInstance().setUserEmail(userEmail);
            
            FXMLLoader loader;
            
            if (isAdminUser(userId)) {
                // Admin users see the admin menu
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
                loader = new FXMLLoader(LoginHandler.class.getResource("/Menu/front_menu.fxml"));
                Parent root = loader.load();
                FrontMenu frontMenu = loader.getController();
                
                // Set user ID for the menu controller
                frontMenu.setCurrentUserId(userId);
                
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
     * Determine if a user is an admin based on their ID
     * 
     * @param userId The user's ID
     * @return true if the user is an admin, false otherwise
     */
    private static boolean isAdminUser(int userId) {
        // Users with IDs 1-10 are considered admins
        return userId > 0 && userId <= ADMIN_USER_THRESHOLD;
    }
    
    /**
     * Handle login with email and password
     * In a real app, this would authenticate against a database
     * 
     * @param email User's email
     * @param password User's password
     * @param stage Primary stage
     * @return true if login was successful
     */
    public static boolean handleLogin(String email, String password, Stage stage) {
        // This is a simplified example where we just check if the password is not empty
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            // In a real app, you would query your database for the user
            // For this example, we'll just use a mock user ID based on the email
            int userId = mockUserIdFromEmail(email);
            
            // Show the appropriate menu
            showMenuForUser(userId, email, stage);
            return true;
        }
        return false;
    }
    
    /**
     * Mock method to generate a user ID from an email
     * In a real app, this would be replaced with a database lookup
     * 
     * @param email The user's email
     * @return A mocked user ID (1 for admin@esprit.tn, others are regular users)
     */
    private static int mockUserIdFromEmail(String email) {
        if (email.equalsIgnoreCase("admin@esprit.tn")) {
            return 1; // Admin user
        } else if (email.toLowerCase().startsWith("admin")) {
            return 5; // Another admin user
        } else {
            return 20; // Regular user
        }
    }
} 