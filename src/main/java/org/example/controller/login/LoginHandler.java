package org.example.controller.login;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.menu.FrontMenu;
import org.example.models.User;
import utils.dataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles the login process and directs users to the appropriate menu based on their role
 */
public class LoginHandler {
    
    /**
     * Handle login with email and password
     * 
     * @param email User's email
     * @param password User's password
     * @param stage Primary stage
     * @return true if login was successful
     */
    public static boolean handleLogin(String email, String password, Stage stage) {
        try (Connection conn = dataSource.getInstance().getConnection()) {
            String query = "SELECT * FROM user WHERE email = ? AND password = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Créer un objet User avec les données de la base
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("roles"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getInt("login_count"),
                    rs.getString("image_url"),
                    rs.getString("numtel"),
                    rs.getTimestamp("penalized_until") != null ? rs.getTimestamp("penalized_until").toLocalDateTime() : null
                );

                // Debug logs
                System.out.println("DEBUG: User ID: " + user.getId());
                System.out.println("DEBUG: User Email: " + user.getEmail());
                System.out.println("DEBUG: User Roles: " + user.getRoles());

                // Rediriger vers le menu approprié en fonction du rôle
                if ("admin".equals(user.getRoles())) {
                    System.out.println("DEBUG: User is admin, redirecting to admin panel");
                    loadAdminMenu(stage);
                } else {
                    System.out.println("DEBUG: User is not admin, redirecting to front menu");
                    loadFrontMenu(stage, user);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static void loadFrontMenu(Stage stage, User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginHandler.class.getResource("/Menu/front_menu.fxml"));
        Parent root = loader.load();
        FrontMenu frontMenu = loader.getController();
        frontMenu.setCurrentUser(user);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Esprit Learning Platform");
        stage.show();
    }

    private static void loadAdminMenu(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginHandler.class.getResource("/admin/AdminMenu.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Esprit Admin Panel");
        stage.show();
    }
} 