package org.example.controller.menu;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class menu {
    @FXML
    private Button forumButton;
    
    @FXML
    private Button userForumButton;
    
    @FXML
    private void initialize() {
        forumButton.setOnAction(event -> handleForumButton());
        userForumButton.setOnAction(event -> handleUserForumButton());
    }
    
    private void handleForumButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Forum/forum.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Forum");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading admin forum: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleUserForumButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Forum/user_forum.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Forum");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading user forum: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
