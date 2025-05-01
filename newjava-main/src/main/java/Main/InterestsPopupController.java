package Main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InterestsPopupController implements Initializable {

    @FXML
    private TextField searchInterestsField;
    
    @FXML
    private Label errorMessageLabel;
    
    @FXML
    private ScrollPane interestsScrollPane;
    
    @FXML
    private ScrollPane selectedInterestsScrollPane;
    
    @FXML
    private VBox interestsContainer;
    
    @FXML
    private FlowPane selectedInterestsContainer;
    
    @FXML
    private Button applyFilterButton;
    
    @FXML
    private Button cancelFilterButton;

    // Lists to store all available interests and the currently selected ones
    private final ObservableList<String> allInterests = FXCollections.observableArrayList();
    private final ObservableList<String> selectedInterests = FXCollections.observableArrayList();
    
    // Property to store the final result (selected interests)
    private final StringProperty result = new SimpleStringProperty();
    
    // Flag to track if the popup was confirmed or cancelled
    private boolean confirmed = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate the list of all interests
        populateInterests();
        
        // Setup the search functionality
        setupSearch();
        
        // Setup the initial UI
        setupUI();
        
        // Setup event handlers
        setupEventHandlers();
        
        // Clear any error messages
        errorMessageLabel.setText("");
        errorMessageLabel.setVisible(false);
    }
    
    private void populateInterests() {
        // TODO: Load these from a database or configuration file in a real application
        allInterests.addAll(
            "Programming", "Web Development", "Mobile Development", "AI/ML", 
            "Data Science", "Cybersecurity", "DevOps", "Cloud Computing",
            "Game Development", "UI/UX Design", "Blockchain", "IoT",
            "Computer Networks", "Database Management", "Operating Systems",
            "Computer Architecture", "Algorithms", "Data Structures",
            "Software Engineering", "Project Management", "Agile Methodologies"
        );
        
        // Initial display of all interests
        displayFilteredInterests(allInterests);
    }
    
    private void setupSearch() {
        // Setup the search functionality with filtering
        searchInterestsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                displayFilteredInterests(allInterests);
            } else {
                FilteredList<String> filteredInterests = allInterests.filtered(
                    interest -> interest.toLowerCase().contains(newValue.toLowerCase())
                );
                displayFilteredInterests(filteredInterests);
            }
        });
    }
    
    private void setupUI() {
        // Configure the scroll panes
        interestsScrollPane.setFitToWidth(true);
        selectedInterestsScrollPane.setFitToWidth(true);
        
        // Set up the selected interests container
        selectedInterestsContainer.setPadding(new Insets(5));
        selectedInterestsContainer.setHgap(5);
        selectedInterestsContainer.setVgap(5);
        
        // Add the "No interests selected" placeholder
        updateSelectedInterestsDisplay();
    }
    
    private void setupEventHandlers() {
        // Set up the Apply button action
        applyFilterButton.setOnAction(event -> {
            if (selectedInterests.isEmpty()) {
                errorMessageLabel.setText("Please select at least one interest");
                errorMessageLabel.setVisible(true);
            } else {
                // Join the selected interests with a delimiter
                result.set(String.join(",", selectedInterests));
                confirmed = true;
                closePopup();
            }
        });
        
        // Set up the Cancel button action
        cancelFilterButton.setOnAction(event -> {
            confirmed = false;
            closePopup();
        });
    }
    
    private void displayFilteredInterests(List<String> interests) {
        interestsContainer.getChildren().clear();
        
        if (interests.isEmpty()) {
            Label noMatchesLabel = new Label("No matching interests found");
            noMatchesLabel.getStyleClass().add("no-interests-message");
            interestsContainer.getChildren().add(noMatchesLabel);
        } else {
            for (String interest : interests) {
                CheckBox checkBox = new CheckBox(interest);
                checkBox.getStyleClass().add("interest-checkbox");
                
                // Set the initial state based on whether it's already selected
                checkBox.setSelected(selectedInterests.contains(interest));
                
                // Add change listener
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        if (!selectedInterests.contains(interest)) {
                            selectedInterests.add(interest);
                        }
                    } else {
                        selectedInterests.remove(interest);
                    }
                    updateSelectedInterestsDisplay();
                    errorMessageLabel.setVisible(false);
                });
                
                interestsContainer.getChildren().add(checkBox);
            }
        }
    }
    
    private void updateSelectedInterestsDisplay() {
        selectedInterestsContainer.getChildren().clear();
        
        if (selectedInterests.isEmpty()) {
            Label noSelectionLabel = new Label("No interests selected");
            noSelectionLabel.getStyleClass().add("no-interests-message");
            selectedInterestsContainer.getChildren().add(noSelectionLabel);
        } else {
            for (String interest : selectedInterests) {
                HBox tagBox = createInterestTag(interest);
                selectedInterestsContainer.getChildren().add(tagBox);
            }
        }
    }
    
    private HBox createInterestTag(String interest) {
        HBox tagBox = new HBox();
        tagBox.getStyleClass().add("interest-tag");
        tagBox.setAlignment(Pos.CENTER);
        
        Label tagLabel = new Label(interest);
        
        // Add course count in parentheses with a random number between 1-30 for the example
        int randomCount = 1 + (int)(Math.random() * 30);
        Label countLabel = new Label(" (" + randomCount + ")");
        countLabel.getStyleClass().add("course-count");
        
        Label closeButton = new Label("×");
        closeButton.getStyleClass().add("interest-tag-close");
        closeButton.setOnMouseClicked(event -> {
            selectedInterests.remove(interest);
            
            // Update the corresponding checkbox in the interests list
            for (Node node : interestsContainer.getChildren()) {
                if (node instanceof CheckBox && ((CheckBox) node).getText().equals(interest)) {
                    ((CheckBox) node).setSelected(false);
                    break;
                }
            }
            
            updateSelectedInterestsDisplay();
        });
        
        tagBox.getChildren().addAll(tagLabel, countLabel, closeButton);
        return tagBox;
    }
    
    private void closePopup() {
        Stage stage = (Stage) applyFilterButton.getScene().getWindow();
        stage.close();
    }
    
    // Getter for the result property
    public StringProperty resultProperty() {
        return result;
    }
    
    // Getter for the confirmed flag
    public boolean isConfirmed() {
        return confirmed;
    }
    
    // Method to set pre-selected interests
    public void setSelectedInterests(List<String> interests) {
        selectedInterests.clear();
        List<String> validInterests = interests.stream()
            .filter(allInterests::contains)
            .collect(Collectors.toList());
        selectedInterests.addAll(validInterests);
        updateSelectedInterestsDisplay();
    }
} 