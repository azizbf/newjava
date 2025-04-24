package org.example.controller.user;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.util.Duration;
import org.example.models.user.User;
import utils.dataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Controller for the sign up screen
 */
public class SignUpController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField phoneField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button signUpButton;

    @FXML
    private Button closeButton;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Phone validation pattern (accepts formats like +216 12 345 678 or 12345678)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+\\d{1,3}\\s?)?\\d{8,}$");

    // Reference to the stage for closing
    private Stage stage;

    // Flag to indicate if a user was successfully created
    private boolean userCreated = false;

    // Store the email of the created user for pre-filling login form
    private String createdUserEmail = "";

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Add listeners for field validation
        nameField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        emailField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        roleComboBox.setItems(FXCollections.observableArrayList("Client", "Admin"));
        roleComboBox.setValue("Client"); // Default role
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
    }

    /**
     * Handle sign up button click event
     */
    @FXML
    private void handleSignUp() {
        // Validate all inputs
        if (!validateInputs()) {
            return;
        }

        // Get user data
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneField.getText().trim();

        // Create and save user
        User newUser = createUserObject(name, email, password, phone, roleComboBox.getValue());
        boolean success = saveUser(newUser);

        if (success) {
            // Store the created user's email
            this.userCreated = true;
            this.createdUserEmail = email;

            // Show success message
            showSuccess("Account created successfully!");

            // Disable the signup button to prevent multiple submissions
            signUpButton.setDisable(true);

            // Close the window after a short delay
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                closeWindow();
            });
            delay.play();
        } else {
            showError("Failed to create account. Please try again.");
        }
    }

    /**
     * Validate all user inputs
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        // Check if name is provided
        if (nameField.getText().trim().isEmpty()) {
            showError("Please enter your full name");
            nameField.requestFocus();
            return false;
        }

        // Check if email is provided and valid
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email address");
            emailField.requestFocus();
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            emailField.requestFocus();
            return false;
        }

        // Check if password is provided and meets requirements
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showError("Please enter a password");
            passwordField.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            passwordField.requestFocus();
            return false;
        }

        // Check if passwords match
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            confirmPasswordField.requestFocus();
            return false;
        }

        // Check if phone number is valid (if provided)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showError("Please enter a valid phone number");
            phoneField.requestFocus();
            return false;
        }

        if (roleComboBox.getValue() == null) {
            showError("Please select a role");
            roleComboBox.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Create a User object with the provided information
     *
     * @param name User's full name
     * @param email User's email address
     * @param password User's password
     * @param phone User's phone number
     * @return A new User object
     */
    private User createUserObject(String name, String email, String password, String phone, String role) {
        int id = 0;
        String roles = "client";
        int loginCount = 0;
        String imageUrl = "default_profile.png";
        LocalDateTime penalizedUntil = null;

        return new User(
                id,
                email,
                password,
                role,
                name,
                loginCount,
                imageUrl,
                phone,
                penalizedUntil
        );
    }

    /**
     * Save the user to the database or storage system
     *
     * @param user The User object to save
     * @return true if user was saved successfully, false otherwise
     */
    private boolean saveUser(User user) {
        try {
            int login_count = 0;
            LocalDateTime penalized_until = LocalDateTime.now();

            try {
                Connection conn = dataSource.getInstance().getConnection();

                String query = "INSERT INTO user (email, roles, password, name, login_count, image_url, numtel, penalized_until) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getRoles());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getName());
                stmt.setInt(5, login_count);
                stmt.setString(6, user.getImageUrl());
                stmt.setString(7, user.getNumTel());
                stmt.setTimestamp(8, Timestamp.valueOf(penalized_until));

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("✅ User registered successfully!");
                } else {
                    System.out.println("❌ Failed to register user.");
                }

            } catch (SQLException e) {
                System.err.println("❌ Database error: " + e.getMessage());
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
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
        successLabel.setVisible(false);
    }

    /**
     * Show a success message
     *
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    /**
     * Close the window
     */
    @FXML
    public void closeWindow() {
        if (stage != null) {
            stage.close();
        } else if (closeButton != null) {
            Stage currentStage = (Stage) closeButton.getScene().getWindow();
            currentStage.close();
        }
    }

    /**
     * Set the stage for this controller
     *
     * @param stage The stage to set
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Check if a user was created
     *
     * @return true if a user was created, false otherwise
     */
    public boolean isUserCreated() {
        return userCreated;
    }

    /**
     * Get the email of the created user
     *
     * @return The email of the created user
     */
    public String getCreatedUserEmail() {
        return createdUserEmail;
    }
}