package org.example.controller.lesson;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// Temporarily comment out media imports
// import javafx.scene.media.Media;
// import javafx.scene.media.MediaException;
// import javafx.scene.media.MediaPlayer;
// import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
// Temporarily comment out WebView import
// import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.models.course.Course;
import org.example.models.lesson.Lesson;
import org.example.services.LessonService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CourseLearningController {
    
    // Temporarily comment out MediaView
    // @FXML private MediaView mediaView;
    
    // Temporarily comment out WebView
    // @FXML private WebView webView;
    
    @FXML private VBox webViewPlaceholder;
    @FXML private StackPane mediaContainer;
    @FXML private Button playPauseButton;
    @FXML private Slider timeSlider;
    @FXML private Label timeLabel;
    @FXML private Button fullScreenButton;
    @FXML private Label courseTitle;
    @FXML private Label lessonTitle;
    @FXML private Label lessonDescription;
    @FXML private ListView<Lesson> lessonsListView;
    @FXML private Button previousLessonButton;
    @FXML private Button nextLessonButton;
    @FXML private VBox sidebarContainer;
    @FXML private Button toggleSidebarButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox noContentPlaceholder;
    @FXML private HBox mediaControls;
    @FXML private Button backButton;
    
    private Course currentCourse;
    private LessonService lessonService;
    private List<Lesson> lessons;
    private int currentLessonIndex = 0;
    // Temporarily comment out MediaPlayer
    // private MediaPlayer mediaPlayer;
    private Timeline timelineUpdater;
    private final BooleanProperty sidebarVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private boolean updatingSlider = false;
    private int currentUserId;
    
    @FXML
    public void initialize() {
        try {
            System.out.println("CourseLearningController: initializing");
            
            // Initialize services
            lessonService = new LessonService();
            
            // Configure lessons list view
            lessonsListView.setCellFactory(param -> new ListCell<Lesson>() {
                @Override
                protected void updateItem(Lesson lesson, boolean empty) {
                    super.updateItem(lesson, empty);
                    
                    if (empty || lesson == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(lesson.getOrdre() + ". " + lesson.getTitre());
                    }
                }
            });
            
            // Setup lesson selection listener
            lessonsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldLesson, newLesson) -> {
                if (newLesson != null) {
                    currentLessonIndex = lessons.indexOf(newLesson);
                    loadLesson(newLesson);
                }
            });
            
            // Connect time slider to media player
            timeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                /*
                if (mediaPlayer != null && !updatingSlider && timeSlider.isValueChanging()) {
                    double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
                    double newTime = totalDuration * (newValue.doubleValue() / 100.0);
                    mediaPlayer.seek(Duration.seconds(newTime));
                }
                */
                
                // Just update for visual feedback 
                if (timeSlider.isValueChanging()) {
                    System.out.println("Slider changed: " + newValue);
                    // Update time label based on slider position
                    int minute = (int) (5 * newValue.doubleValue() / 100);
                    int second = (int) (60 * (newValue.doubleValue() / 100) % 60);
                    timeLabel.setText(String.format("%02d:%02d / 05:00", minute, second));
                }
            });
            
            // Setup sidebar visibility binding
            sidebarVisible.addListener((obs, wasVisible, isNowVisible) -> {
                sidebarContainer.setVisible(isNowVisible);
                sidebarContainer.setManaged(isNowVisible);
                
                // Update toggle button icon
                updateToggleButtonIcon();
            });
            
            // Initially the sidebar is visible
            sidebarVisible.set(true);
            
            // Setup navigation buttons
            updateNavigationButtons();
            
            // Handle image placeholders - create default graphics
            setupDefaultIcons();
            
            // Don't try to access the scene here as it might not be attached yet
            // Instead, we'll add a listener to be called once the scene is available
            mediaContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    System.out.println("Scene is now available - initializing scene-dependent components");
                    initializeWithScene(newScene);
                }
            });
            
            System.out.println("CourseLearningController: initialization complete");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize components that depend on the scene being available
     */
    private void initializeWithScene(Scene scene) {
        try {
            // Apply CSS
            String cssPath = getClass().getResource("/css/course_learning.css").toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
                System.out.println("Added CSS: " + cssPath);
            }
            
            // Fix back button icon if needed
            setupBackButton();
            
        } catch (Exception e) {
            System.err.println("Error in initializeWithScene: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Setup default icons for buttons to avoid errors with missing images
     */
    private void setupDefaultIcons() {
        try {
            // Setup default icons for buttons to avoid errors
            Button[] buttons = {playPauseButton, fullScreenButton, toggleSidebarButton};
            for (Button btn : buttons) {
                if (btn.getGraphic() == null) {
                    // Create a default graphic for buttons without images
                    javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(20, 20);
                    rect.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                    btn.setGraphic(rect);
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up default icons: " + e.getMessage());
        }
    }
    
    /**
     * Set the course to be displayed
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        courseTitle.setText(course.getTitre());
        
        // Load lessons for this course
        loadLessons();
    }
    
    /**
     * Load lessons for the current course
     */
    private void loadLessons() {
        try {
            lessons = lessonService.getLessonsByCourse(currentCourse.getId());
            lessonsListView.getItems().setAll(lessons);
            
            if (!lessons.isEmpty()) {
                // Select the first lesson
                lessonsListView.getSelectionModel().select(0);
                loadLesson(lessons.get(0));
            } else {
                // No lessons available
                showNoContent("No lessons available for this course");
            }
            
            updateNavigationButtons();
        } catch (Exception e) {
            System.err.println("Error loading lessons: " + e.getMessage());
            showNoContent("Error loading course lessons");
        }
    }
    
    /**
     * Load a specific lesson's content
     */
    private void loadLesson(Lesson lesson) {
        try {
            lessonTitle.setText(lesson.getTitre());
            lessonDescription.setText(lesson.getDescription());
            
            // Show loading indicator
            showLoading(false);
            
            // Stop any currently playing media
            /*
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            */
            
            // Hide all content containers initially
            // mediaView.setVisible(false);
            // webView.setVisible(false);
            webViewPlaceholder.setVisible(false);
            noContentPlaceholder.setVisible(false);
            mediaControls.setVisible(false);
            
            // Check what type of content we have and load accordingly
            String videoPath = lesson.getVideoPath();
            if (videoPath != null && !videoPath.isEmpty()) {
                // Instead of loading video, just show info about it
                showVideoInfo(videoPath);
            } else {
                // No content available
                showNoContent("No video path specified for this lesson");
            }
            
            // Update navigation buttons
            updateNavigationButtons();
        } catch (Exception e) {
            System.err.println("Error in loadLesson: " + e.getMessage());
            e.printStackTrace();
            showNoContent("Error loading lesson: " + e.getMessage());
        }
    }
    
    /**
     * Display video information instead of loading the actual video
     */
    private void showVideoInfo(String videoPath) {
        try {
            // Hide loading indicator
            showLoading(false);
            
            // Make media controls visible with dummy data
            mediaControls.setVisible(true);
            
            // Update time label with dummy data
            timeLabel.setText("00:00 / 05:00");
            
            // Reset slider
            timeSlider.setValue(0);
            
            // We're not really playing anything
            isPlaying.set(false);
            updateTogglePlayIcon();
            
            System.out.println("Displayed video info for: " + videoPath);
            
        } catch (Exception e) {
            System.err.println("Error in showVideoInfo: " + e.getMessage());
            e.printStackTrace();
            showNoContent("Error displaying video info: " + e.getMessage());
        }
    }
    
    /**
     * Load a video file (disabled for now)
     */
    private void loadVideo(String videoPath) {
        // This method is disabled to avoid MediaView issues
        showVideoInfo(videoPath);
    }
    
    /**
     * Handle play/pause button click
     */
    @FXML
    private void handlePlayPause() {
        // Disabled for now - just toggle the play/pause icon
        isPlaying.set(!isPlaying.get());
        updateTogglePlayIcon();
        
        if (isPlaying.get()) {
            System.out.println("Play button clicked (placeholder only)");
        } else {
            System.out.println("Pause button clicked (placeholder only)");
        }
    }
    
    /**
     * Update the play/pause button icon based on current state
     */
    private void updateTogglePlayIcon() {
        try {
            ImageView imageView = (ImageView) playPauseButton.getGraphic();
            if (imageView == null) {
                imageView = new ImageView();
                imageView.setFitHeight(20.0);
                imageView.setFitWidth(20.0);
                playPauseButton.setGraphic(imageView);
            }
            
            // Create a colored rectangle as fallback
            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(20, 20);
            rect.setFill(isPlaying.get() ? javafx.scene.paint.Color.ORANGE : javafx.scene.paint.Color.GREEN);
            playPauseButton.setGraphic(rect);
            
            /*
            String iconPath = isPlaying.get() ? 
                "/images/pause_button.png" : 
                "/images/play_button.png";
                
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                if (icon != null && !icon.isError()) {
                    imageView.setImage(icon);
                } else {
                    // Use colored rectangle as fallback
                    javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(20, 20);
                    rect.setFill(isPlaying.get() ? javafx.scene.paint.Color.ORANGE : javafx.scene.paint.Color.GREEN);
                    playPauseButton.setGraphic(rect);
                }
            } catch (Exception e) {
                System.err.println("Error loading play/pause icon: " + e.getMessage());
                // Use colored rectangle as fallback
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(20, 20);
                rect.setFill(isPlaying.get() ? javafx.scene.paint.Color.ORANGE : javafx.scene.paint.Color.GREEN);
                playPauseButton.setGraphic(rect);
            }
            */
        } catch (Exception e) {
            System.err.println("Error updating play/pause icon: " + e.getMessage());
        }
    }
    
    /**
     * Setup timeline to update time slider and label (disabled for now)
     */
    private void setupTimelineUpdater() {
        /*
        if (timelineUpdater != null) {
            timelineUpdater.stop();
        }
        
        timelineUpdater = new Timeline(
            new KeyFrame(Duration.millis(500), event -> updateTimeDisplay())
        );
        timelineUpdater.setCycleCount(Timeline.INDEFINITE);
        timelineUpdater.play();
        */
        
        // For now, just set the slider and time label once
        timeSlider.setValue(0);
        timeLabel.setText("00:00 / 05:00");
    }
    
    /**
     * Update the time display (disabled for now)
     */
    private void updateTimeDisplay() {
        // This method is disabled as we don't have a real MediaPlayer
        /*
        if (mediaPlayer != null) {
            Platform.runLater(() -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration totalDuration = mediaPlayer.getTotalDuration();
                
                // Format the time strings
                String currentTimeStr = formatDuration(currentTime);
                String totalTimeStr = formatDuration(totalDuration);
                
                // Update the time label
                timeLabel.setText(currentTimeStr + " / " + totalTimeStr);
                
                // Update the slider value without triggering the change listener
                updatingSlider = true;
                double percentage = (currentTime.toSeconds() / totalDuration.toSeconds()) * 100.0;
                timeSlider.setValue(percentage);
                updatingSlider = false;
            });
        }
        */
    }
    
    /**
     * Format duration as mm:ss
     */
    private String formatDuration(Duration duration) {
        int seconds = (int) Math.floor(duration.toSeconds());
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Show or hide the loading indicator
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
    }
    
    /**
     * Show no content message
     */
    private void showNoContent(String message) {
        try {
            // Find the label in the VBox
            Label noContentLabel = (Label) noContentPlaceholder.getChildren().stream()
                .filter(node -> node instanceof Label)
                .findFirst()
                .orElse(null);
                
            if (noContentLabel != null) {
                noContentLabel.setText(message);
            } else {
                // If no label exists, create one
                Label newLabel = new Label(message);
                newLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                newLabel.setFont(new javafx.scene.text.Font(18.0));
                noContentPlaceholder.getChildren().add(newLabel);
            }
            
            // Check if there's an ImageView in the VBox and make sure it has an image
            ImageView placeholderImage = (ImageView) noContentPlaceholder.getChildren().stream()
                .filter(node -> node instanceof ImageView)
                .findFirst()
                .orElse(null);
                
            if (placeholderImage != null) {
                try {
                    Image img = new Image(getClass().getResourceAsStream("/images/video_placeholder.png"));
                    if (img != null && !img.isError()) {
                        placeholderImage.setImage(img);
                    } else {
                        // Create a simple placeholder shape
                        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(80, 80);
                        rect.setFill(javafx.scene.paint.Color.DARKGRAY);
                        // Replace the ImageView with the rectangle
                        int index = noContentPlaceholder.getChildren().indexOf(placeholderImage);
                        if (index >= 0) {
                            noContentPlaceholder.getChildren().set(index, rect);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading placeholder image: " + e.getMessage());
                    // Remove the ImageView or replace with a shape
                    placeholderImage.setVisible(false);
                }
            }
            
            noContentPlaceholder.setVisible(true);
            mediaControls.setVisible(false);
            
        } catch (Exception e) {
            System.err.println("Error showing no content message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update the navigation buttons based on current position
     */
    private void updateNavigationButtons() {
        if (lessons == null || lessons.isEmpty()) {
            previousLessonButton.setDisable(true);
            nextLessonButton.setDisable(true);
            return;
        }
        
        previousLessonButton.setDisable(currentLessonIndex <= 0);
        nextLessonButton.setDisable(currentLessonIndex >= lessons.size() - 1);
    }
    
    /**
     * Handle fullscreen button click
     */
    @FXML
    private void handleFullScreen() {
        Stage stage = (Stage) mediaContainer.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }
    
    /**
     * Toggle sidebar visibility
     */
    @FXML
    private void toggleSidebar() {
        sidebarVisible.set(!sidebarVisible.get());
    }
    
    /**
     * Update the toggle sidebar button icon
     */
    private void updateToggleButtonIcon() {
        try {
            ImageView imageView = (ImageView) toggleSidebarButton.getGraphic();
            if (imageView == null) {
                imageView = new ImageView();
                imageView.setFitHeight(24.0);
                imageView.setFitWidth(24.0);
                toggleSidebarButton.setGraphic(imageView);
            }
            
            try {
                String iconPath = sidebarVisible.get() ? 
                    "/images/collapse_menu.png" : 
                    "/images/menu_icon.png";
                    
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                if (icon != null && !icon.isError()) {
                    imageView.setImage(icon);
                } else {
                    // Use a colored rectangle as fallback
                    javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(24, 24);
                    rect.setFill(javafx.scene.paint.Color.LIGHTBLUE);
                    toggleSidebarButton.setGraphic(rect);
                }
            } catch (Exception e) {
                System.err.println("Error loading sidebar toggle icon: " + e.getMessage());
                // Use a colored rectangle as fallback
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(24, 24);
                rect.setFill(javafx.scene.paint.Color.LIGHTBLUE);
                toggleSidebarButton.setGraphic(rect);
            }
        } catch (Exception e) {
            System.err.println("Error updating sidebar toggle icon: " + e.getMessage());
        }
    }
    
    /**
     * Go to previous lesson
     */
    @FXML
    private void handlePreviousLesson() {
        if (currentLessonIndex > 0) {
            currentLessonIndex--;
            lessonsListView.getSelectionModel().select(currentLessonIndex);
        }
    }
    
    /**
     * Go to next lesson
     */
    @FXML
    private void handleNextLesson() {
        if (currentLessonIndex < lessons.size() - 1) {
            currentLessonIndex++;
            lessonsListView.getSelectionModel().select(currentLessonIndex);
        }
    }
    
    /**
     * Handle notes button click
     */
    @FXML
    private void handleNotes() {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
            "Notes Feature", "The notes feature is coming soon!");
    }
    
    /**
     * Handle resources button click
     */
    @FXML
    private void handleResources() {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
            "Resources Feature", "The resources feature is coming soon!");
    }
    
    /**
     * Handle back button click
     */
    @FXML
    private void handleBack() {
        try {
            // Stop any playing media - no longer needed as we don't use MediaPlayer
            /*
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            */
            
            // Stop timeline
            if (timelineUpdater != null) {
                timelineUpdater.stop();
            }
            
            // Return to course details
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/course/course_details.fxml"));
            Parent root = loader.load();
            
            // Get the course details controller
            org.example.controller.course.CourseDetailsController controller = loader.getController();
            
            // Pass the current user ID and course
            if (currentUserId > 0) {
                controller.setCurrentUserId(currentUserId);
            }
            
            controller.setCourse(currentCourse);
            
            // Set the scene
            Scene scene = mediaContainer.getScene();
            
            // Clear all existing stylesheets to prevent style contamination
            scene.getStylesheets().clear();
            
            // Add the course details CSS
            String courseDetailsCss = getClass().getResource("/css/course_details.css").toExternalForm();
            scene.getStylesheets().add(courseDetailsCss);
            
            // Set the new root
            scene.setRoot(root);
            
            System.out.println("Navigated back to course details");
            
        } catch (IOException e) {
            System.err.println("Error returning to course details: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Could not return to course details", e.getMessage());
        }
    }
    
    /**
     * Set the current user ID
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
    
    /**
     * Helper method to show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Setup the back button with a proper icon
     */
    private void setupBackButton() {
        try {
            // Check the graphic of the back button
            if (backButton != null) {
                // Get current graphic
                Node graphic = backButton.getGraphic();
                
                if (graphic instanceof ImageView) {
                    ImageView imgView = (ImageView) graphic;
                    Image img = imgView.getImage();
                    
                    // Check if the image is valid
                    if (img == null || img.isError()) {
                        // Create a simple arrow shape
                        javafx.scene.shape.Polygon arrow = new javafx.scene.shape.Polygon();
                        arrow.getPoints().addAll(
                            16.0, 0.0,  // Top right
                            16.0, 16.0, // Bottom right
                            0.0, 8.0    // Middle left
                        );
                        arrow.setFill(javafx.scene.paint.Color.WHITE);
                        
                        // Use this shape as the graphic
                        backButton.setGraphic(arrow);
                    }
                } else if (graphic == null) {
                    // Create a simple arrow shape if there's no graphic
                    javafx.scene.shape.Polygon arrow = new javafx.scene.shape.Polygon();
                    arrow.getPoints().addAll(
                        16.0, 0.0,  // Top right
                        16.0, 16.0, // Bottom right
                        0.0, 8.0    // Middle left
                    );
                    arrow.setFill(javafx.scene.paint.Color.WHITE);
                    
                    // Use this shape as the graphic
                    backButton.setGraphic(arrow);
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up back button: " + e.getMessage());
        }
    }
} 