package org.example.controller.menu;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class menu {
    @FXML
    private Button forumButton;
    
    @FXML
    private Button userForumButton;
    
    @FXML
    private Button adminPanelButton;
    
    @FXML
    private Button quizButton;
    
    @FXML
    private Button questionQuizButton;
    
    @FXML
    private Button competitionButton;
    
    @FXML
    private Pane contentPane;
    
    private Button selectedButton;
    private final Map<Button, String> buttonPathMap = new HashMap<>();
    
    @FXML
    private void initialize() {
        // Store button-path mappings
        buttonPathMap.put(forumButton, "/Forum/forum.fxml");
        buttonPathMap.put(userForumButton, "/Forum/user_forum.fxml");
        buttonPathMap.put(adminPanelButton, "/admin/AdminMenu.fxml");
        buttonPathMap.put(quizButton, "/quiz/AfficherQuiz.fxml");
        buttonPathMap.put(questionQuizButton, "/quiz/AfficherQuestionQuiz.fxml");
        buttonPathMap.put(competitionButton, "/quiz/AfficherQuiz.fxml");
        
        // Set up button actions
        forumButton.setOnAction(event -> handleButtonClick(forumButton));
        userForumButton.setOnAction(event -> handleButtonClick(userForumButton));
        adminPanelButton.setOnAction(event -> handleButtonClick(adminPanelButton));
        quizButton.setOnAction(event -> handleQuiz());
        questionQuizButton.setOnAction(event -> handleQuestionQuiz());
        competitionButton.setOnAction(event -> handleCompetition());
        
        // Add style classes for hover effects defined in CSS
        addButtonStyleHandlers(forumButton);
        addButtonStyleHandlers(userForumButton);
        addButtonStyleHandlers(adminPanelButton);
        addButtonStyleHandlers(quizButton);
        addButtonStyleHandlers(questionQuizButton);
        addButtonStyleHandlers(competitionButton);
    }
    
    /**
     * Sets up hover and press style handlers for a button
     */
    private void addButtonStyleHandlers(Button button) {
        button.setOnMouseEntered(e -> {
            if (button != selectedButton) {
                button.getStyleClass().add("menu-button-hover");
            }
        });
        
        button.setOnMouseExited(e -> {
            if (button != selectedButton) {
                button.getStyleClass().remove("menu-button-hover");
            }
        });
    }
    
    /**
     * Handles button click events
     * 
     * @param clickedButton The button that was clicked
     */
    private void handleButtonClick(Button clickedButton) {
        if (clickedButton == selectedButton) {
            return;
        }
        
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("menu-button-selected");
        }
        
        clickedButton.getStyleClass().add("menu-button-selected");
        selectedButton = clickedButton;
        
        String fxmlPath = buttonPathMap.get(clickedButton);
        loadContentIntoPane(fxmlPath);
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
    
    @FXML
    private void handleQuiz() {
        handleButtonClick(quizButton);
    }
    
    @FXML
    private void handleQuestionQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz/AfficherQuestionQuiz.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) questionQuizButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCompetition() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz/FrontQuiz.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) competitionButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads content from the specified FXML file into the main content pane.
     * 
     * @param fxmlPath The path to the FXML file
     */
    private void loadContentIntoPane(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            
            // Clear existing content
            contentPane.getChildren().clear();
            
            // Add new content with fade effect
            contentPane.getChildren().add(content);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
