package org.example.controller.user;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.models.user.User;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.dataSource;

public class Login {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        // Handle button click event
        loginButton.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Simple validation logic
        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill out both fields.");
        } else {
            try {
                Connection conn = dataSource.getInstance().getConnection();
                String query = "SELECT * FROM user WHERE email = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("✅ Login successful!");

                    // Load the menu.fxml window
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu/menu.fxml"));
                    Parent root = loader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Menu");
                    stage.show();

                    // Close the login window
                    loginButton.getScene().getWindow().hide();

                } else {
                    System.out.println("❌ Incorrect email or password.");
                }

            } catch (SQLException e) {
                System.err.println("❌ Database error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ UI loading error: " + e.getMessage());
            }
        }
    }
}