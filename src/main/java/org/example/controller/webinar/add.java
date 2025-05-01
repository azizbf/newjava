package org.example.controller.webinar;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.controller.login.SessionManager;
import org.example.models.user.User;
import org.example.models.webinar.GoogleMeetIntegration;
import utils.dataSource;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.ResourceBundle;

public class add implements Initializable {
    private User currentUser;
    
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker debutDateField;
    @FXML private TextField debutTimeField;
    @FXML private TextField durationField;
    @FXML private TextField categoryField;
    @FXML private TextField tagsField;
    @FXML private CheckBox registrationRequired;
    @FXML private TextField maxAttendeesField;
    @FXML private TextField platformField;
    @FXML private TextField linkField;
    @FXML private Button generateLinkButton;
    @FXML private CheckBox recordingAvailable;
    
    private final Random random = new Random();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        // Set default values
        debutDateField.setValue(LocalDate.now());
        debutTimeField.setText("12:00");
        maxAttendeesField.setText("50");
        durationField.setText("60");
        
        // If platform field is empty when generate link is clicked, set it to Google Meet
        if (generateLinkButton != null) {
            generateLinkButton.setOnAction(event -> generateMeetingLink());
        }
    }
    
    @FXML
    private void generateMeetingLink() {
        try {
            // Default to Google Meet if platform is empty
            if (platformField.getText().isEmpty()) {
                platformField.setText("Google Meet");
            }
            
            String platform = platformField.getText().toLowerCase();
            String link;
            
            // Generate different link formats based on the platform
            switch (platform) {
                case "zoom":
                    link = generateZoomLink();
                    break;
                case "teams":
                case "microsoft teams":
                    link = generateTeamsLink();
                    break;
                case "webex":
                case "cisco webex":
                    link = generateWebexLink();
                    break;
                case "google meet":
                    // Try to generate a real Google Meet link
                    try {
                        // Validate required fields
                        if (titleField.getText().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Input Error", "Missing Title", 
                                    "Please enter a title to create a real Google Meet link.");
                            return;
                        }
                        
                        if (debutDateField.getValue() == null || debutTimeField.getText().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Input Error", "Missing Date/Time", 
                                    "Please enter a date and time to create a real Google Meet link.");
                            return;
                        }
                        
                        // Parse time
                        LocalTime debutTime;
                        try {
                            debutTime = LocalTime.parse(debutTimeField.getText());
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid Time Format", 
                                    "Please enter time in HH:MM format (e.g., 14:30).");
                            return;
                        }
                        
                        // Parse duration
                        int duration;
                        try {
                            duration = Integer.parseInt(durationField.getText());
                        } catch (NumberFormatException e) {
                            duration = 60; // Default to 60 minutes
                        }
                        
                        // Get date and time
                        LocalDate debutDate = debutDateField.getValue();
                        LocalDateTime startDateTime = LocalDateTime.of(debutDate, debutTime);
                        
                        // Create real Google Meet link using API
                        link = GoogleMeetIntegration.createMeetLink(
                                titleField.getText(),
                                descriptionField.getText(),
                                startDateTime,
                                duration,
                                currentUser.getEmail()
                        );
                    } catch (Exception e) {
                        // If API call fails, fall back to simulated link
                        showAlert(Alert.AlertType.WARNING, "API Error", "Could Not Create Real Meeting", 
                                "Error: " + e.getMessage() + "\n\nFalling back to simulated link.");
                        link = generateGoogleMeetLink();
                    }
                    break;
                default:
                    link = generateGoogleMeetLink();
                    break;
            }
            
            linkField.setText(link);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Link Generation Failed", 
                    "Error: " + e.getMessage());
        }
    }
    
    private String generateGoogleMeetLink() {
        // Format: https://meet.google.com/abc-defg-hij
        StringBuilder code = new StringBuilder();
        
        // First part: 3 lowercase letters
        for (int i = 0; i < 3; i++) {
            code.append((char) (random.nextInt(26) + 'a'));
        }
        code.append('-');
        
        // Second part: 4 lowercase letters
        for (int i = 0; i < 4; i++) {
            code.append((char) (random.nextInt(26) + 'a'));
        }
        code.append('-');
        
        // Third part: 3 lowercase letters
        for (int i = 0; i < 3; i++) {
            code.append((char) (random.nextInt(26) + 'a'));
        }
        
        return "https://meet.google.com/" + code.toString();
    }
    
    private String generateZoomLink() {
        // Format: https://zoom.us/j/1234567890?pwd=abcdef
        StringBuilder meetingId = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            meetingId.append(random.nextInt(10));
        }
        
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append((char) (random.nextInt(26) + 'a'));
        }
        
        return "https://zoom.us/j/" + meetingId.toString() + "?pwd=" + password.toString();
    }
    
    private String generateTeamsLink() {
        // Format: https://teams.microsoft.com/l/meetup-join/[random string]
        StringBuilder randomPart = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 20; i++) {
            randomPart.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return "https://teams.microsoft.com/l/meetup-join/" + randomPart.toString();
    }
    
    private String generateWebexLink() {
        // Format: https://meetingsapac.webex.com/meet/[random string]
        StringBuilder randomPart = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        
        for (int i = 0; i < 10; i++) {
            randomPart.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return "https://meetingsapac.webex.com/meet/" + randomPart.toString();
    }

    @FXML
    public void handleAddWebinar() {
        try {
            // Validation
            if (titleField.getText().isEmpty() || descriptionField.getText().isEmpty() ||
                    debutDateField.getValue() == null || debutTimeField.getText().isEmpty() ||
                    durationField.getText().isEmpty() || categoryField.getText().isEmpty() ||
                    platformField.getText().isEmpty() || linkField.getText().isEmpty()) {
                
                showAlert(Alert.AlertType.ERROR, "Input Error", "Missing Fields", 
                        "Please fill in all required fields.");
                return;
            }
            
            // Get presenter ID from current user
            Integer presenterId = currentUser.getId();
            
            // Parse form data
            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate debutDate = debutDateField.getValue();
            
            // Parse time with validation
            LocalTime debutTime;
            try {
                debutTime = LocalTime.parse(debutTimeField.getText());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid Time Format", 
                        "Please enter time in HH:MM format (e.g., 14:30).");
                return;
            }
            
            // Combine date and time
            LocalDateTime debut = LocalDateTime.of(debutDate, debutTime);
            
            // Parse numeric fields with validation
            int duration;
            int maxAttendees;
            try {
                duration = Integer.parseInt(durationField.getText());
                maxAttendees = Integer.parseInt(maxAttendeesField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid Number", 
                        "Duration and Max Attendees must be valid numbers.");
                return;
            }
            
            String category = categoryField.getText();
            String tags = tagsField.getText();
            boolean isRegistrationRequired = registrationRequired.isSelected();
            String platform = platformField.getText();
            String lien = linkField.getText();
            boolean isRecordingAvailable = recordingAvailable.isSelected();

            // Insert into database
            try (Connection conn = dataSource.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO webinar (presenter_id, title, description, debut, duration, " +
                    "category, tags, registration_required, max_attendees, platform, lien, " +
                    "recording_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                stmt.setObject(1, presenterId, java.sql.Types.INTEGER);
                stmt.setString(2, title);
                stmt.setString(3, description);
                stmt.setTimestamp(4, Timestamp.valueOf(debut));
                stmt.setInt(5, duration);
                stmt.setString(6, category);
                stmt.setString(7, tags);
                stmt.setBoolean(8, isRegistrationRequired);
                stmt.setInt(9, maxAttendees);
                stmt.setString(10, platform);
                stmt.setString(11, lien);
                stmt.setBoolean(12, isRecordingAvailable);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Webinar Added", 
                            "Webinar has been added successfully!");
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to Add Webinar", 
                            "No rows were inserted.");
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "SQL Error", 
                    "Error: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error", 
                    "Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        titleField.clear();
        descriptionField.clear();
        debutDateField.setValue(LocalDate.now());
        debutTimeField.setText("12:00");
        durationField.setText("60");
        categoryField.clear();
        tagsField.clear();
        registrationRequired.setSelected(false);
        maxAttendeesField.setText("50");
        platformField.clear();
        linkField.clear();
        recordingAvailable.setSelected(false);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
