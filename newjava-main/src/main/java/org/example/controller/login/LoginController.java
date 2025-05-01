package org.example.controller.login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the login screen
 */
public class LoginController {
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    /**
     * Handle login button click event
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validate inputs
        if (email.isEmpty()) {
            showError("Please enter your email");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter your password");
            return;
        }
        
        // Use the LoginHandler to process the login
        Stage stage = (Stage) loginButton.getScene().getWindow();
        boolean success = LoginHandler.handleLogin(email, password, stage);
        
        if (!success) {
            showError("Invalid email or password");
        }
    }
    
    /**
     * Show an error message
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Add any initialization code here
        
        // Add listeners for when enter is pressed in the text fields
        emailField.setOnAction(event -> {
            if (emailField.getText().trim().isEmpty()) {
                showError("Please enter your email");
            } else {
                passwordField.requestFocus();
            }
        });
        
        passwordField.setOnAction(event -> {
            if (!emailField.getText().trim().isEmpty() && !passwordField.getText().isEmpty()) {
                handleLogin();
            } else {
                showError("Please fill in all fields");
            }
        });
    }
} 