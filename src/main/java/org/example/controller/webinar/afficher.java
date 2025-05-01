package org.example.controller.webinar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import org.example.controller.login.SessionManager;
import org.example.models.user.User;
import org.example.models.webinar.webinar;
import org.example.models.webinar.inscription;
import utils.dataSource;
import org.example.models.webinar.mailing;
import org.example.models.webinar.SmsSender;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import java.time.YearMonth;
import java.util.ArrayList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.File;

public class afficher implements Initializable {
    private User currentUser;
    private int userId;
    private mailing mailing;
    private SmsSender smsSender;
    @FXML
    private TableView<webinar> webinarTableView;
    @FXML
    private TableColumn<webinar, Integer> idColumn;
    @FXML
    private TableColumn<webinar, String> titleColumn;
    @FXML
    private TableColumn<webinar, String> descriptionColumn;
    @FXML
    private TableColumn<webinar, LocalDateTime> debutColumn;
    @FXML
    private TableColumn<webinar, Integer> durationColumn;
    @FXML
    private TableColumn<webinar, String> categoryColumn;
    @FXML
    private TableColumn<webinar, String> platformColumn;
    @FXML
    private TableColumn<webinar, Void> actionsColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortComboBox;
    
    // Create a FilteredList to hold the filtered data
    private FilteredList<webinar> filteredData;
    // SortedList to hold sorted data
    private SortedList<webinar> sortedData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        System.out.println(currentUser.getId());
        mailing = new mailing();
        smsSender = new SmsSender();
        
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        debutColumn.setCellValueFactory(new PropertyValueFactory<>("debut"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        platformColumn.setCellValueFactory(new PropertyValueFactory<>("platform"));

        // Format the date column
        debutColumn.setCellFactory(column -> new TableCell<webinar, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Configure the actions column
        setupActionsColumn();
        
        // Initialize sorting options
        sortComboBox.getItems().addAll(
            "Default",
            "Title (A-Z)",
            "Title (Z-A)",
            "Start Date (ASC)",
            "Start Date (DESC)",
            "Duration (Shortest first)",
            "Duration (Longest first)"
        );
        sortComboBox.setValue("Default");
        
        // Add listener for sort selection
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            applySorting(newValue);
        });

        // Initialize the filtered list with all webinars
        ObservableList<webinar> webinars = getWebinarList();
        filteredData = new FilteredList<>(webinars, p -> true);
        
        // Set up the search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(webinar -> {
                    // If search field is empty, display all webinars
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    // Compare webinar title with search text
                    String lowerCaseFilter = newValue.toLowerCase();
                    
                    // Check if the webinar title contains the search string
                    if (webinar.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches title
                    }
                    return false; // Does not match
                });
            });
        }
        
        // Wrap the filtered list in a SortedList
        sortedData = new SortedList<>(filteredData);
        
        // Bind the SortedList comparator to the TableView comparator
        sortedData.comparatorProperty().bind(webinarTableView.comparatorProperty());
        
        // Set the sorted and filtered data to the table
        webinarTableView.setItems(sortedData);
    }

    /**
     * Method required by the FrontMenu framework
     * @param userId the current user's ID
     */
    public void setCurrentUserId(int userId) {
        this.userId = userId;
        // We already get the user from SessionManager, but we could update here if needed
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<webinar, Void>, TableCell<webinar, Void>>() {
            @Override
            public TableCell<webinar, Void> call(TableColumn<webinar, Void> param) {
                return new TableCell<webinar, Void>() {
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            webinar webinar = getTableView().getItems().get(getIndex());
                            HBox buttonsBox = new HBox(5);

                            // Check if current user is the presenter
                            if (webinar.getPresenterId() == currentUser.getId()) {
                                // Create Update button
                                Button updateBtn = new Button("Update");
                                updateBtn.setOnAction(event -> handleUpdateWebinar(webinar));
                                
                                // Create Delete button
                                Button deleteBtn = new Button("Delete");
                                deleteBtn.setOnAction(event -> handleDeleteWebinar(webinar));

                                buttonsBox.getChildren().addAll(updateBtn, deleteBtn);
                            } else {
                                // Create Inscription button for non-presenters
                                Button inscriptionBtn = new Button("Register");
                                inscriptionBtn.setOnAction(event -> handleInscriptionWebinar(webinar));
                                
                                // Disable inscription button if user already registered
                                if (isUserRegistered(webinar.getId(), currentUser.getId())) {
                                    inscriptionBtn.setDisable(true);
                                    inscriptionBtn.setText("Registered");
                                }
                                
                                buttonsBox.getChildren().add(inscriptionBtn);
                            }
                            
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        });
    }

    private ObservableList<webinar> getWebinarList() {
        ObservableList<webinar> webinars = FXCollections.observableArrayList();

        String sql = "SELECT * FROM webinar";

        try (Connection conn = dataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                webinar w = new webinar(
                        rs.getInt("id"),
                        rs.getInt("presenter_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("debut").toLocalDateTime(),
                        rs.getInt("duration"),
                        rs.getString("category"),
                        rs.getString("tags"),
                        rs.getBoolean("registration_required"),
                        rs.getInt("max_attendees"),
                        rs.getString("platform"),
                        rs.getString("lien"),
                        rs.getBoolean("recording_available")
                );
                webinars.add(w);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading webinars: " + e.getMessage());
        }

        return webinars;
    }

    private boolean isUserRegistered(int webinarId, int userId) {
        String sql = "SELECT COUNT(*) FROM inscription WHERE webinar_id = ? AND user_id = ?";
        try (Connection conn = dataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, webinarId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking registration: " + e.getMessage());
        }
        return false;
    }

    private void handleUpdateWebinar(webinar webinar) {
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Update Webinar");
            dialog.setHeaderText("Update webinar details");
            
            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
            
            // Create the grid and set its properties
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            // Create form fields
            TextField titleField = new TextField(webinar.getTitle());
            TextField descriptionField = new TextField(webinar.getDescription());
            
            DatePicker debutDatePicker = new DatePicker(webinar.getDebut().toLocalDate());
            TextField timeField = new TextField(
                    webinar.getDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            
            TextField durationField = new TextField(String.valueOf(webinar.getDuration()));
            TextField categoryField = new TextField(webinar.getCategory());
            TextField tagsField = new TextField(webinar.getTags());
            
            CheckBox registrationRequiredCheck = new CheckBox();
            registrationRequiredCheck.setSelected(webinar.isRegistrationRequired());
            
            TextField maxAttendeesField = new TextField(String.valueOf(webinar.getMaxAttendees()));
            TextField platformField = new TextField(webinar.getPlatform());
            TextField linkField = new TextField(webinar.getLien());
            
            CheckBox recordingAvailableCheck = new CheckBox();
            recordingAvailableCheck.setSelected(webinar.isRecordingAvailable());
            
            // Add labels and fields to the grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionField, 1, 1);
            
            grid.add(new Label("Start Date:"), 0, 2);
            grid.add(debutDatePicker, 1, 2);
            
            grid.add(new Label("Start Time (HH:mm):"), 0, 3);
            grid.add(timeField, 1, 3);
            
            grid.add(new Label("Duration (minutes):"), 0, 4);
            grid.add(durationField, 1, 4);
            
            grid.add(new Label("Category:"), 0, 5);
            grid.add(categoryField, 1, 5);
            
            grid.add(new Label("Tags:"), 0, 6);
            grid.add(tagsField, 1, 6);
            
            grid.add(new Label("Registration Required:"), 0, 7);
            grid.add(registrationRequiredCheck, 1, 7);
            
            grid.add(new Label("Max Attendees:"), 0, 8);
            grid.add(maxAttendeesField, 1, 8);
            
            grid.add(new Label("Platform:"), 0, 9);
            grid.add(platformField, 1, 9);
            
            grid.add(new Label("Link:"), 0, 10);
            grid.add(linkField, 1, 10);
            
            grid.add(new Label("Recording Available:"), 0, 11);
            grid.add(recordingAvailableCheck, 1, 11);
            
            // Set the dialog content
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the title field by default
            titleField.requestFocus();
            
            // Convert the result to a webinar when the update button is clicked
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == updateButtonType) {
                // Process the result
                try {
                    // Parse and validate fields
                    String title = titleField.getText();
                    String description = descriptionField.getText();
                    
                    LocalDate debutDate = debutDatePicker.getValue();
                    LocalTime debutTime;
                    try {
                        debutTime = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception e) {
                        throw new Exception("Invalid time format. Please use HH:mm");
                    }
                    
                    int duration = Integer.parseInt(durationField.getText());
                    String category = categoryField.getText();
                    String tags = tagsField.getText();
                    boolean isRegistrationRequired = registrationRequiredCheck.isSelected();
                    int maxAttendees = Integer.parseInt(maxAttendeesField.getText());
                    String platform = platformField.getText();
                    String lien = linkField.getText();
                    boolean isRecordingAvailable = recordingAvailableCheck.isSelected();
                    
                    // Combine date and time
                    LocalDateTime debutDateTime = LocalDateTime.of(debutDate, debutTime);
                    
                    // Update the webinar in the database
                    saveWebinarUpdate(webinar.getId(), title, description, debutDateTime, duration, 
                            category, tags, isRegistrationRequired, maxAttendees, platform, lien, 
                            isRecordingAvailable);
                    
                    // Refresh the table to show the updated data
                    webinarTableView.setItems(getWebinarList());
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Input Error");
                    alert.setHeaderText("Invalid Number Format");
                    alert.setContentText("Please ensure all numeric fields contain valid numbers.");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Update Failed");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating webinar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveWebinarUpdate(int id, String title, String description, LocalDateTime debut, 
                                 int duration, String category, String tags, boolean registrationRequired,
                                 int maxAttendees, String platform, String lien, boolean recordingAvailable) throws SQLException {
        
        String sql = "UPDATE webinar SET title=?, description=?, debut=?, duration=?, category=?, " +
                    "tags=?, registration_required=?, max_attendees=?, platform=?, lien=?, " +
                    "recording_available=? WHERE id=?";
        
        try (Connection conn = dataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setTimestamp(3, Timestamp.valueOf(debut));
            stmt.setInt(4, duration);
            stmt.setString(5, category);
            stmt.setString(6, tags);
            stmt.setBoolean(7, registrationRequired);
            stmt.setInt(8, maxAttendees);
            stmt.setString(9, platform);
            stmt.setString(10, lien);
            stmt.setBoolean(11, recordingAvailable);
            stmt.setInt(12, id);
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Webinar updated successfully!");
                
                // Refresh the table
                refreshWebinarList();
            } else {
                System.out.println("❌ Failed to update webinar.");
            }
        }
    }

    private void handleDeleteWebinar(webinar webinar) {
        int webinarId = webinar.getId();

        String deleteInscriptionsQuery = "DELETE FROM inscription WHERE webinar_id = ?";
        String deleteWebinarQuery = "DELETE FROM webinar WHERE id = ?";

        try (Connection conn = dataSource.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (
                    PreparedStatement deleteInscriptionsStmt = conn.prepareStatement(deleteInscriptionsQuery);
                    PreparedStatement deleteWebinarStmt = conn.prepareStatement(deleteWebinarQuery)
            ) {
                deleteInscriptionsStmt.setInt(1, webinarId);
                deleteInscriptionsStmt.executeUpdate();

                deleteWebinarStmt.setInt(1, webinarId);
                int rowsAffected = deleteWebinarStmt.executeUpdate();

                conn.commit();

                if (rowsAffected > 0) {
                    System.out.println("✅ Webinar and related inscriptions deleted successfully.");
                    // 🔁 Actually update the UI
                    refreshWebinarList();
                    setupActionsColumn(); // (Optional) refresh buttons
                } else {
                    System.out.println("⚠️ No webinar found with the given ID.");
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Transaction failed: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting webinar: " + e.getMessage());
        }
    }


    private void handleInscriptionWebinar(webinar webinar) {
        try (Connection conn = dataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO inscription (webinar_id, user_id) VALUES (?, ?)")) {
            
            pstmt.setInt(1, webinar.getId());
            pstmt.setInt(2, currentUser.getId());
            String subject = "webinar- Esprit Learning Platform";
            String name = currentUser.getName();
            String email = currentUser.getEmail();
            String content = String.format("""
                    Dear %s,

                    Thanks for your registration,

                    Best regards,
                    Esprit E-Learning Platform Team
                    """, name);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Registration successful!");
                
                // Send email notification
                mailing.sendEmail(email, subject, content);
                
                // Send SMS notification if phone number is available
                String phoneNumber = currentUser.getNumTel();
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    String smsMessage = "You have successfully registered for the webinar: " + webinar.getTitle();
                    smsSender.sendSms(phoneNumber, smsMessage);
                }
                
                // Refresh table to update button state
                refreshWebinarList();
                setupActionsColumn();
            } else {
                System.out.println("Registration failed.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error registering for webinar: " + e.getMessage());
        }
    }

    /**
     * Apply sorting based on the selected option
     */
    private void applySorting(String sortOption) {
        // Remove existing sorting
        webinarTableView.getSortOrder().clear();
        
        switch (sortOption) {
            case "Title (A-Z)":
                titleColumn.setSortType(TableColumn.SortType.ASCENDING);
                webinarTableView.getSortOrder().add(titleColumn);
                break;
                
            case "Title (Z-A)":
                titleColumn.setSortType(TableColumn.SortType.DESCENDING);
                webinarTableView.getSortOrder().add(titleColumn);
                break;
                
            case "Start Date (ASC)":
                debutColumn.setSortType(TableColumn.SortType.ASCENDING);
                webinarTableView.getSortOrder().add(debutColumn);
                break;
                
            case "Start Date (DESC)":
                debutColumn.setSortType(TableColumn.SortType.DESCENDING);
                webinarTableView.getSortOrder().add(debutColumn);
                break;
                
            case "Duration (Shortest first)":
                durationColumn.setSortType(TableColumn.SortType.ASCENDING);
                webinarTableView.getSortOrder().add(durationColumn);
                break;
                
            case "Duration (Longest first)":
                durationColumn.setSortType(TableColumn.SortType.DESCENDING);
                webinarTableView.getSortOrder().add(durationColumn);
                break;
                
            default: // "Default" or any other option
                // Use default sorting (normally by ID)
                idColumn.setSortType(TableColumn.SortType.ASCENDING);
                webinarTableView.getSortOrder().add(idColumn);
                break;
        }
        
        // Force a sort
        webinarTableView.sort();
    }
    
    /**
     * Method to refresh the webinar list with fresh data
     */
    public void refreshWebinarList() {
        ObservableList<webinar> webinars = getWebinarList();
        filteredData = new FilteredList<>(webinars, p -> true);
        
        // Reset the search predicate
        if (searchField != null && !searchField.getText().isEmpty()) {
            String searchText = searchField.getText();
            filteredData.setPredicate(webinar -> {
                String lowerCaseFilter = searchText.toLowerCase();
                return webinar.getTitle().toLowerCase().contains(lowerCaseFilter);
            });
        }
        
        // Create a new SortedList with the updated filtered data
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(webinarTableView.comparatorProperty());
        
        // Update the table items
        webinarTableView.setItems(sortedData);
        
        // Re-apply current sorting if any
        if (sortComboBox != null && sortComboBox.getValue() != null) {
            applySorting(sortComboBox.getValue());
        }
    }

    /**
     * Method to get webinars the current user has registered for
     */
    private ObservableList<webinar> getUserRegisteredWebinars() {
        ObservableList<webinar> registeredWebinars = FXCollections.observableArrayList();
        
        String sql = "SELECT w.* FROM webinar w " +
                     "JOIN inscription i ON w.id = i.webinar_id " +
                     "WHERE i.user_id = ?";
        
        try (Connection conn = dataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, currentUser.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    webinar w = new webinar(
                            rs.getInt("id"),
                            rs.getInt("presenter_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getTimestamp("debut").toLocalDateTime(),
                            rs.getInt("duration"),
                            rs.getString("category"),
                            rs.getString("tags"),
                            rs.getBoolean("registration_required"),
                            rs.getInt("max_attendees"),
                            rs.getString("platform"),
                            rs.getString("lien"),
                            rs.getBoolean("recording_available")
                    );
                    registeredWebinars.add(w);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading registered webinars: " + e.getMessage());
        }
        
        return registeredWebinars;
    }
    
    /**
     * Show calendar view with registered webinars
     */
    @FXML
    private void showCalendarView() {
        try {
            // Get registered webinars
            ObservableList<webinar> registeredWebinars = getUserRegisteredWebinars();
            
            // Create a new dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("My Webinar Calendar");
            dialog.setHeaderText("Webinars You've Registered For");
            
            // Add close button
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);
            
            // Create a VBox as the root container
            VBox root = new VBox(10);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setPrefWidth(800);
            root.setPrefHeight(600);
            
            // Current month tracking
            final YearMonth[] currentYearMonth = {YearMonth.now()};
            
            // Create month view with navigation buttons
            HBox dateNavigation = new HBox(10);
            dateNavigation.setAlignment(Pos.CENTER);
            
            // Previous month button
            Button prevMonthBtn = new Button("←");
            prevMonthBtn.setStyle("-fx-font-size: 16; -fx-padding: 5 10;");
            
            // Month/Year label
            Label monthYearLabel = new Label(currentYearMonth[0].format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            monthYearLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            // Next month button
            Button nextMonthBtn = new Button("→");
            nextMonthBtn.setStyle("-fx-font-size: 16; -fx-padding: 5 10;");
            
            // Current month button
            Button currentMonthBtn = new Button("Today");
            
            dateNavigation.getChildren().addAll(prevMonthBtn, monthYearLabel, nextMonthBtn, currentMonthBtn);
            
            // Create a GridPane for the calendar
            GridPane calendarGrid = new GridPane();
            calendarGrid.setHgap(5);
            calendarGrid.setVgap(5);
            calendarGrid.setGridLinesVisible(true); // Show grid lines for clarity
            
            // Create column labels (days of the week)
            String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (int i = 0; i < daysOfWeek.length; i++) {
                Label dayLabel = new Label(daysOfWeek[i]);
                dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
                calendarGrid.add(dayLabel, i, 0);
                
                // Set column constraints to make columns equal width
                ColumnConstraints colConstraints = new ColumnConstraints();
                colConstraints.setPercentWidth(100.0 / 7);
                colConstraints.setHgrow(Priority.ALWAYS);
                calendarGrid.getColumnConstraints().add(colConstraints);
            }
            
            // Function to refresh calendar based on selected month
            Runnable refreshCalendar = () -> {
                // Update month/year label
                monthYearLabel.setText(currentYearMonth[0].format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                
                // Clear existing calendar cells (except headers)
                for (Node node : new ArrayList<>(calendarGrid.getChildren())) {
                    Integer rowIndex = GridPane.getRowIndex(node);
                    if (rowIndex != null && rowIndex > 0) {
                        calendarGrid.getChildren().remove(node);
                    }
                }
                
                // Get current year/month
                YearMonth yearMonth = currentYearMonth[0];
                
                // Calculate first day of month and last day of month
                LocalDate firstDayOfMonth = yearMonth.atDay(1);
                LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
                
                // Calculate day of week of first day (0 = Monday, 6 = Sunday in our grid)
                int firstDayOfWeek = (firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;
                
                // Calculate weeks in month
                int weeksInMonth = (int) Math.ceil((firstDayOfWeek + yearMonth.lengthOfMonth()) / 7.0);
                
                // Create calendar cells
                for (int week = 0; week < weeksInMonth; week++) {
                    for (int day = 0; day < 7; day++) {
                        int dayOfMonth = week * 7 + day - firstDayOfWeek + 1;
                        
                        VBox dayCell = new VBox(5);
                        dayCell.setPadding(new javafx.geometry.Insets(5));
                        dayCell.setMinHeight(80);
                        
                        if (dayOfMonth > 0 && dayOfMonth <= lastDayOfMonth.getDayOfMonth()) {
                            // Valid day in month
                            LocalDate currentDate = yearMonth.atDay(dayOfMonth);
                            
                            // Add date label
                            Label dateLabel = new Label(String.valueOf(dayOfMonth));
                            dateLabel.setStyle("-fx-font-weight: bold;");
                            dayCell.getChildren().add(dateLabel);
                            
                            // Check for webinars on this date
                            for (webinar webinar : registeredWebinars) {
                                LocalDate webinarDate = webinar.getDebut().toLocalDate();
                                if (webinarDate.equals(currentDate)) {
                                    // Create a webinar entry
                                    String timeText = webinar.getDebut().toLocalTime().format(
                                            DateTimeFormatter.ofPattern("HH:mm"));
                                    HBox webinarEntry = new HBox(5);
                                    
                                    Label timeLabel = new Label(timeText);
                                    timeLabel.setStyle("-fx-font-weight: bold;");
                                    
                                    Tooltip tooltip = new Tooltip(webinar.getTitle() + "\n" + 
                                            "Duration: " + webinar.getDuration() + " min\n" +
                                            "Platform: " + webinar.getPlatform());
                                    
                                    Label titleLabel = new Label(webinar.getTitle());
                                    titleLabel.setTooltip(tooltip);
                                    
                                    // Limit title length to fit
                                    if (titleLabel.getText().length() > 15) {
                                        titleLabel.setText(titleLabel.getText().substring(0, 12) + "...");
                                    }
                                    
                                    webinarEntry.getChildren().addAll(timeLabel, titleLabel);
                                    webinarEntry.setStyle("-fx-background-color: #e6f7ff; -fx-padding: 2; -fx-background-radius: 3;");
                                    
                                    dayCell.getChildren().add(webinarEntry);
                                }
                            }
                            
                            // Highlight today
                            if (currentDate.equals(LocalDate.now())) {
                                dayCell.setStyle("-fx-background-color: #f5f5f5;");
                            }
                        }
                        
                        calendarGrid.add(dayCell, day, week + 1);
                    }
                }
                
                // Set row constraints to make rows expand properly
                while (calendarGrid.getRowConstraints().size() <= weeksInMonth) {
                    RowConstraints rowConstraints = new RowConstraints();
                    rowConstraints.setVgrow(Priority.ALWAYS);
                    calendarGrid.getRowConstraints().add(rowConstraints);
                }
            };
            
            // Add event handlers for navigation buttons
            prevMonthBtn.setOnAction(e -> {
                currentYearMonth[0] = currentYearMonth[0].minusMonths(1);
                refreshCalendar.run();
            });
            
            nextMonthBtn.setOnAction(e -> {
                currentYearMonth[0] = currentYearMonth[0].plusMonths(1);
                refreshCalendar.run();
            });
            
            currentMonthBtn.setOnAction(e -> {
                currentYearMonth[0] = YearMonth.now();
                refreshCalendar.run();
            });
            
            // Initial calendar setup
            refreshCalendar.run();
            
            // Add elements to root container
            root.getChildren().addAll(dateNavigation, calendarGrid);
            
            // Set the dialog content
            dialog.getDialogPane().setContent(root);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Error showing calendar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openAddWebinarForm() {
        try {
            // Load the add webinar FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/webinar/add.fxml"));
            
            // If that fails, try alternate paths
            if (loader.getLocation() == null) {
                // Try with different path formats
                URL resource = getClass().getResource("/webinar/add.fxml");
                if (resource == null) {
                    resource = getClass().getResource("../../../webinar/add.fxml");
                }
                if (resource == null) {
                    resource = getClass().getResource("../../../resources/webinar/add.fxml");
                }
                if (resource == null) {
                    resource = new File("src/main/resources/webinar/add.fxml").toURI().toURL();
                }
                
                if (resource != null) {
                    loader = new FXMLLoader(resource);
                } else {
                    throw new IOException("Could not find add.fxml resource");
                }
            }
            
            Parent root = loader.load();
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Create a new stage
            Stage addWebinarStage = new Stage();
            addWebinarStage.setTitle("Add New Webinar");
            addWebinarStage.setScene(scene);
            
            // Set the stage as modal (user must interact with this window before continuing)
            addWebinarStage.initModality(Modality.APPLICATION_MODAL);
            
            // Set owner stage
            addWebinarStage.initOwner(webinarTableView.getScene().getWindow());
            
            // Show the stage and wait for it to close
            addWebinarStage.showAndWait();
            
            // Refresh the table when the form closes
            refreshWebinarList();
            
        } catch (IOException e) {
            System.err.println("❌ Error loading add webinar form: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open Add Webinar form");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPlease make sure the FXML file is in the correct location.");
            alert.showAndWait();
        }
    }
}
