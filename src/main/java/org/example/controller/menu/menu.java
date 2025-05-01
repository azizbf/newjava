package org.example.controller.menu;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class menu {
    @FXML
    private Button forumButton;
    
    @FXML
    private Button userForumButton;
    
    @FXML
    private Pane contentPane;
    
    private Button selectedButton;
    private final Map<Button, String> buttonPathMap = new HashMap<>();
    
    @FXML
    private void initialize() {
        // Store button-path mappings
        buttonPathMap.put(forumButton, "/Forum/forum.fxml");
        buttonPathMap.put(userForumButton, "/Forum/user_forum.fxml");
        
        // Set up button actions
        forumButton.setOnAction(event -> handleButtonClick(forumButton));
        userForumButton.setOnAction(event -> handleButtonClick(userForumButton));
        
        // Add style classes for hover effects defined in CSS
        addButtonStyleHandlers(forumButton);
        addButtonStyleHandlers(userForumButton);
    }
    
    /**
     * Sets up hover and press style handlers for a button
     */
    private void addButtonStyleHandlers(Button button) {
        // These events are handled by CSS, but we can add additional effects here if needed
        button.setOnMouseEntered(e -> {
            if (button != selectedButton) {
                // Additional hover effects can be added here
            }
        });
        
        button.setOnMouseExited(e -> {
            if (button != selectedButton) {
                // Reset any additional hover effects here
            }
        });
    }
    
    /**
     * Handles button click events
     * 
     * @param clickedButton The button that was clicked
     */
    private void handleButtonClick(Button clickedButton) {
        // If the button is already selected, do nothing
        if (clickedButton == selectedButton) {
            return;
        }
        
        // Clear selection style from previous button
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("menu-button-selected");
        }
        
        // Add selection style to clicked button
        clickedButton.getStyleClass().add("menu-button-selected");
        selectedButton = clickedButton;
        
        // Load content associated with the button
        String fxmlPath = buttonPathMap.get(clickedButton);
        String errorMessage = "Error loading " + clickedButton.getText().toLowerCase();
        loadContentIntoPane(fxmlPath, errorMessage);
    }
    
    /**
     * Legacy methods for backward compatibility
     */
    private void handleForumButton() {
        handleButtonClick(forumButton);
    }
    
    private void handleUserForumButton() {
        handleButtonClick(userForumButton);
    }
    
    /**
     * Loads content from the specified FXML file into the main content pane.
     * 
     * @param fxmlPath The path to the FXML file
     * @param errorMessage The error message to display if loading fails
     */
    private void loadContentIntoPane(String fxmlPath, String errorMessage) {
        try {
            // Load the content
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            
            // Wrap the content in an AnchorPane to make it fill the contentPane
            AnchorPane wrapper = new AnchorPane();
            wrapper.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            
            // Clear the contentPane and add the wrapper with a fade effect
            wrapper.setOpacity(0);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(wrapper);
            
            // Make the wrapper fill the contentPane
            wrapper.prefWidthProperty().bind(contentPane.widthProperty());
            wrapper.prefHeightProperty().bind(contentPane.heightProperty());
            
            // Add fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), wrapper);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
        } catch (Exception e) {
            System.err.println(errorMessage + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
