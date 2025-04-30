package org.example.controller.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    @FXML
    private Button signUpButton;

    @FXML
    private Button scanCINButton;

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
     * Handle CIN scanner login
     */
    @FXML
    private void handleScanCINLogin() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "\"C:\\Users\\Lenovo\\Desktop\\newjava\\src\\main\\resources\\IntelligentScanner\\cinScanner.py\""
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String cinNumber = reader.readLine();
            process.waitFor();

            if (cinNumber != null && !cinNumber.trim().isEmpty()) {
                // Use the LoginHandler to process the CIN login
                Stage stage = (Stage) scanCINButton.getScene().getWindow();
                boolean success = LoginHandler.handleCINLogin(cinNumber.trim(), stage);

                if (!success) {
                    showError("No account found with this CIN");
                }
            } else {
                showError("Could not scan CIN. Please try again or use email login.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error scanning CIN: " + e.getMessage());
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

        // Add event handler for the signup button
        signUpButton.setOnAction(event -> openSignUpForm());
    }

    /**
     * Open the signup form when the signup button is clicked
     */
    private void openSignUpForm() {
        try {
            // Load the signup FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/SignUp.fxml"));
            Parent signUpRoot = loader.load();

            // Get the controller
            org.example.controller.user.SignUpController signUpController = loader.getController();

            // Create a new stage for the signup form
            Stage signUpStage = new Stage();
            signUpStage.initModality(Modality.APPLICATION_MODAL);
            signUpStage.initStyle(StageStyle.DECORATED);
            signUpStage.setTitle("Create Account");
            signUpStage.setScene(new Scene(signUpRoot));

            // Set the close button action
            signUpController.setStage(signUpStage);

            // Show the signup form and wait for it to close
            signUpStage.showAndWait();

            // If the user was created successfully, pre-fill the login form
            if (signUpController.isUserCreated()) {
                String email = signUpController.getCreatedUserEmail();
                emailField.setText(email);
                passwordField.requestFocus();
                errorLabel.setVisible(false);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not open sign up form");
        }
    }

    /**
     * Handle forgot password link click
     */
    @FXML
    private void handleForgotPassword() {
        try {
            // Load the password reset FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/PasswordReset.fxml"));
            Parent resetRoot = loader.load();

            // Get the controller
            PasswordResetController resetController = loader.getController();

            // Create a new stage for the password reset form
            Stage resetStage = new Stage();
            resetStage.initModality(Modality.APPLICATION_MODAL);
            resetStage.initStyle(StageStyle.DECORATED);
            resetStage.setTitle("Reset Password");
            resetStage.setScene(new Scene(resetRoot));

            // Set the stage in the controller
            resetController.setStage(resetStage);

            // Show the password reset form
            resetStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not open password reset form");
        }
    }
}