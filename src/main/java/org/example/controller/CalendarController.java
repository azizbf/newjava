package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CalendarController {
    @FXML
    private DatePicker datePicker;
    @FXML
    private VBox eventsContainer;
    @FXML
    private Button addEventButton;
    @FXML
    private Label selectedDateLabel;

    @FXML
    private void initialize() {
        // Initialize date picker with current date
        datePicker.setValue(LocalDate.now());
        updateSelectedDateLabel();
        
        // Add listener to date picker
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateSelectedDateLabel();
            loadEventsForDate(newValue);
        });
    }

    private void updateSelectedDateLabel() {
        LocalDate selectedDate = datePicker.getValue();
        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        selectedDateLabel.setText("Selected Date: " + formattedDate);
    }

    @FXML
    private void handleAddEvent(ActionEvent event) {
        // TODO: Implement event addition logic
        // This could open a new window or dialog to add an event
    }

    private void loadEventsForDate(LocalDate date) {
        // TODO: Implement loading events from database for the selected date
        eventsContainer.getChildren().clear();
        // Add logic to load and display events
    }
} 