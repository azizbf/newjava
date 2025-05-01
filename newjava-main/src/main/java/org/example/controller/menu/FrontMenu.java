package org.example.controller.menu;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FrontMenu {
    @FXML
    private Button homeButton;
    
    @FXML
    private Button forumButton;
    
    @FXML
    private Button projectsButton;
    
    @FXML
    private Button coursesButton;
    
    @FXML
    private Button webinarsButton;
    
    @FXML
    private Button competitionsButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private AnchorPane contentPane;
    
    private Button selectedButton;
    private final Map<Button, String> buttonPathMap = new HashMap<>();
    private int currentUserId = -1; // Default to invalid ID until properly set from login
    
    @FXML
    private void initialize() {
        // Store button-path mappings
        buttonPathMap.put(forumButton, "/Forum/user_forum.fxml");
        buttonPathMap.put(projectsButton, "/Projects/projects.fxml");
        buttonPathMap.put(coursesButton, "/Menu/front_courses.fxml");
        buttonPathMap.put(webinarsButton, "/Webinars/webinars.fxml");
        buttonPathMap.put(competitionsButton, "/Competitions/competitions.fxml");
        buttonPathMap.put(profileButton, "/User/profile.fxml");
        
        // Set up button actions
        homeButton.setOnAction(event -> handleHomeButton());
        forumButton.setOnAction(event -> handleButtonClick(forumButton));
        projectsButton.setOnAction(event -> handleButtonClick(projectsButton));
        coursesButton.setOnAction(event -> handleButtonClick(coursesButton));
        webinarsButton.setOnAction(event -> handleButtonClick(webinarsButton));
        competitionsButton.setOnAction(event -> handleButtonClick(competitionsButton));
        profileButton.setOnAction(event -> handleButtonClick(profileButton));
        logoutButton.setOnAction(event -> handleLogout());
        
        // Mark home as selected by default
        selectButton(homeButton);
    }
    
    /**
     * Handles logout button click
     */
    private void handleLogout() {
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginRoot = loader.load();
            
            // Get current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create new scene with login screen
            Scene scene = new Scene(loginRoot);
            
            // Set the scene on the stage
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
            
            System.out.println("User logged out successfully");
            
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to select a button and apply styling
     */
    private void selectButton(Button button) {
        // Clear selection from previous button
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("nav-button-selected");
        }
        
        // Apply selection to new button
        button.getStyleClass().add("nav-button-selected");
        selectedButton = button;
    }
    
    /**
     * Event handler for buttons that load content
     */
    private void handleButtonClick(Button clickedButton) {
        // If the button is already selected, do nothing
        if (clickedButton == selectedButton) {
            return;
        }
        
        // Select the clicked button
        selectButton(clickedButton);
        
        // Load content associated with the button
        String fxmlPath = buttonPathMap.get(clickedButton);
        if (fxmlPath != null) {
            String errorMessage = "Error loading " + clickedButton.getText().toLowerCase();
            loadContentIntoPane(fxmlPath, errorMessage);
        }
    }
    
    /**
     * Handles home button click - returns to dashboard
     */
    private void handleHomeButton() {
        selectButton(homeButton);
        // Reset to the default welcome screen
        loadHomeContent();
    }
    
    /**
     * Loads default home/dashboard content
     */
    private void loadHomeContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu/front_home.fxml"));
            Parent content = loader.load();
            
            setContent(content);
        } catch (Exception e) {
            // If front_home.fxml doesn't exist yet, we'll just clear the content
            contentPane.getChildren().clear();
            System.err.println("Home screen could not be loaded: " + e.getMessage());
        }
    }
    
    /**
     * Load content into the content pane from FXML
     */
    private void loadContentIntoPane(String fxmlPath, String errorMessage) {
        try {
            // Load the content
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            
            // Try to set user ID if the controller supports it
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    // Check if controller has setCurrentUserId method
                    java.lang.reflect.Method setUserIdMethod = controller.getClass().getMethod("setCurrentUserId", int.class);
                    if (setUserIdMethod != null) {
                        setUserIdMethod.invoke(controller, currentUserId);
                        System.out.println("Set user ID " + currentUserId + " on controller " + controller.getClass().getSimpleName());
                    }
                } catch (NoSuchMethodException e) {
                    // Controller doesn't support setCurrentUserId method, which is fine
                    System.out.println("Controller " + controller.getClass().getSimpleName() + " doesn't support setCurrentUserId");
                } catch (Exception e) {
                    System.err.println("Error setting user ID: " + e.getMessage());
                }
            }
            
            setContent(content);
        } catch (Exception e) {
            System.err.println(errorMessage + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to set content with fade animation
     */
    private void setContent(Parent content) {
        // Make content fill the pane
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        
        // Add fade animation
        content.setOpacity(0);
        
        // Clear existing content and add new
        contentPane.getChildren().clear();
        contentPane.getChildren().add(content);
        
        // Play fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    /**
     * Forum button click handler for the feature cards
     */
    @FXML
    private void handleForumButton() {
        // Same as clicking the forum button in top nav
        handleButtonClick(forumButton);
    }
    
    /**
     * Courses button click handler for the feature cards
     * Made public so it can be called from other controllers
     */
    @FXML
    public void handleCoursesButton() {
        handleButtonClick(coursesButton);
    }
    
    /**
     * Webinars button click handler for the feature cards
     */
    @FXML
    private void handleWebinarsButton() {
        handleButtonClick(webinarsButton);
    }
    
    /**
     * Competitions button click handler for the feature cards
     */
    @FXML
    private void handleCompetitionsButton() {
        handleButtonClick(competitionsButton);
    }
    
    /**
     * Set the current user ID from the login process
     * 
     * @param userId The authenticated user's ID
     */
    public void setCurrentUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }
        System.out.println("Setting user ID in FrontMenu: " + userId);
        this.currentUserId = userId;
    }
    
    /**
     * Get the current user ID
     * 
     * @return The current user ID
     */
    public int getCurrentUserId() {
        return currentUserId;
    }
} 