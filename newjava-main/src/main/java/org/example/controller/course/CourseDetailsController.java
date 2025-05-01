package org.example.controller.course;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.models.course.Course;
import org.example.models.lesson.Lesson;
import org.example.services.CourseService;
import org.example.services.InscriptionCoursService;
import org.example.services.LessonService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CourseDetailsController {

    @FXML
    private Label courseTitle;
    
    @FXML
    private Label courseSubtitle;
    
    @FXML
    private ImageView courseImage;
    
    @FXML
    private Label learnersLabel;
    
    @FXML
    private Label likesLabel;
    
    @FXML
    private Label dislikesLabel;
    
    @FXML
    private Label durationLabel;
    
    @FXML
    private Label descriptionLabel;
    
    @FXML
    private Label priceLabel;
    
    @FXML
    private Button startLearningButton;
    
    @FXML
    private ListView<Lesson> lessonsListView;
    
    @FXML
    private ImageView publisherImage;
    
    @FXML
    private Label publisherName;
    
    @FXML
    private Label publisherInfo;
    
    private Course currentCourse;
    private CourseService courseService;
    private LessonService lessonService;
    private InscriptionCoursService inscriptionCoursService;
    private int currentUserId = -1;
    
    @FXML
    public void initialize() {
        courseService = new CourseService();
        lessonService = new LessonService();
        inscriptionCoursService = new InscriptionCoursService();
        
        // Ensure course details stylesheets are properly applied
        Scene scene = startLearningButton.getScene();
        if (scene != null) {
            // Ensure the course details CSS is applied
            String courseDetailsCssPath = getClass().getResource("/css/course_details.css").toExternalForm();
            if (!scene.getStylesheets().contains(courseDetailsCssPath)) {
                scene.getStylesheets().add(courseDetailsCssPath);
            }
            
            System.out.println("Course Details: Applied course_details.css");
        }
        
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
    }
    
    /**
     * Sets the course to be displayed and updates the UI
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        
        // Update UI elements with course data
        courseTitle.setText(course.getTitre());
        
        // Set subtitle (using first sentence of description or a default)
        String description = course.getDescription();
        int firstSentenceEnd = description.indexOf('.');
        if (firstSentenceEnd > 0) {
            courseSubtitle.setText(description.substring(0, firstSentenceEnd + 1));
        } else {
            courseSubtitle.setText("This online course will help you master new skills and advance your career.");
        }
        
        try {
            // Load course image
            String imagePath = course.getImg();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    // Try as file path
                    Image image = new Image("file:forum_files/" + imagePath);
                    courseImage.setImage(image);
                } catch (Exception e) {
                    // Use default image if loading fails
                    courseImage.setImage(new Image(getClass().getResourceAsStream("/images/default-course.png")));
                }
            } else {
                // Use default image if no path specified
                courseImage.setImage(new Image(getClass().getResourceAsStream("/images/default-course.png")));
            }
            
            // Load publisher image (placeholder for now)
            try {
                publisherImage.setImage(new Image(getClass().getResourceAsStream("/images/default-publisher.png")));
            } catch (Exception e) {
                System.err.println("Error loading publisher image: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error loading course image: " + e.getMessage());
        }
        
        // Set course details
        learnersLabel.setText(String.valueOf(course.getLearnerCount()));
        likesLabel.setText(String.valueOf(course.getLikes()));
        dislikesLabel.setText(String.valueOf(course.getDislikes()));
        
        // Set duration
        String duration = course.getMinimal_hours() + "-" + course.getMaximal_hours();
        durationLabel.setText(duration);
        
        // Set description
        descriptionLabel.setText(course.getDescription());
        
        // Set price
        if (course.isIs_free()) {
            priceLabel.setText("Free");
            startLearningButton.setText("Start Learning");
        } else {
            priceLabel.setText("$" + String.format("%.2f", course.getPrice()));
            startLearningButton.setText("Purchase Course");
        }
        
        // Load lessons for this course
        loadLessons();
    }
    
    /**
     * Loads lessons for the current course
     */
    private void loadLessons() {
        try {
            List<Lesson> lessons = lessonService.getLessonsByCourse(currentCourse.getId());
            lessonsListView.getItems().setAll(lessons);
        } catch (SQLException e) {
            System.err.println("Error loading lessons: " + e.getMessage());
            // Show a placeholder if lessons can't be loaded
            Lesson placeholder = new Lesson();
            placeholder.setTitre("No lessons available");
            placeholder.setOrdre(1);
            lessonsListView.getItems().add(placeholder);
        }
    }
    
    /**
     * Handle back button click to return to courses list
     */
    @FXML
    private void handleBack() {
        try {
            // First load the front_menu.fxml (main container with navigation bar)
            FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/Menu/front_menu.fxml"));
            Parent menuRoot = menuLoader.load();
            
            // Get the front menu controller
            org.example.controller.menu.FrontMenu frontMenuController = menuLoader.getController();
            
            // Pass the current user ID to the front menu controller
            if (currentUserId > 0) {
                frontMenuController.setCurrentUserId(currentUserId);
            }
            
            // Create the scene with the front menu
            Scene scene = startLearningButton.getScene();
            
            // Clear all existing stylesheets to prevent style contamination
            scene.getStylesheets().clear();
            
            // Add the menu CSS
            String menuCssPath = getClass().getResource("/css/menu.css").toExternalForm();
            scene.getStylesheets().add(menuCssPath);
            
            // Set the main menu as the root
            scene.setRoot(menuRoot);
            
            // Now trigger the courses button to load the courses content
            // This will load the courses content into the content pane
            frontMenuController.handleCoursesButton();
            
            System.out.println("Navigation: Back to front menu with courses content");
            
        } catch (IOException e) {
            System.err.println("Error returning to courses: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Could not return to courses page", e.getMessage());
        }
    }
    
    /**
     * Handle start learning/purchase button click
     */
    @FXML
    private void handleStartLearning() {
        // Check if user is logged in
        if (currentUserId <= 0) {
            showAlert(Alert.AlertType.INFORMATION, "Login Required", 
                "Please log in", "You need to log in to enroll in this course.");
            return;
        }
        
        // Check if course is free or paid
        if (currentCourse.isIs_free()) {
            enrollInCourse();
        } else {
            // Show payment dialog/process
            showPaymentDialog();
        }
    }
    
    /**
     * Enroll the current user in the course
     */
    private void enrollInCourse() {
        try {
            // Check if already enrolled
            boolean alreadyEnrolled = inscriptionCoursService.isUserEnrolled(currentUserId, currentCourse.getId());
            
            if (alreadyEnrolled) {
                // Already enrolled, go to learning interface
                showLearningInterface();
            } else {
                try {
                    // Enroll the user - this method returns void, not boolean
                    inscriptionCoursService.enrollUserInCourse(currentUserId, currentCourse.getId());
                    
                    // If no exception was thrown, enrollment was successful
                    showAlert(Alert.AlertType.INFORMATION, "Enrollment Successful", 
                        "Successfully enrolled", "You are now enrolled in " + currentCourse.getTitre());
                    
                    // Go to learning interface
                    showLearningInterface();
                } catch (Exception e) {
                    System.err.println("Error enrolling user: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Enrollment Failed", 
                        "Could not enroll in course", "Please try again later.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in enrollment: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Enrollment Error", 
                "Database error occurred", e.getMessage());
        }
    }
    
    /**
     * Show the learning interface for this course
     */
    private void showLearningInterface() {
        try {
            // Load the course learning interface
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson/course_learning.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            org.example.controller.lesson.CourseLearningController controller = loader.getController();
            
            // Pass the course and user ID to the controller
            controller.setCourse(currentCourse);
            controller.setCurrentUserId(currentUserId);
            
            // Set the scene
            Scene scene = startLearningButton.getScene();
            
            // Clear all existing stylesheets to prevent style contamination
            scene.getStylesheets().clear();
            
            // Add the learning interface CSS
            String learningCssPath = getClass().getResource("/css/course_learning.css").toExternalForm();
            scene.getStylesheets().add(learningCssPath);
            
            // Set the new root
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Error loading learning interface: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Could not open learning interface", e.getMessage());
        }
    }
    
    /**
     * Show payment dialog for paid courses
     */
    private void showPaymentDialog() {
        // TODO: Implement payment dialog
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
            "Payment Processing", "Payment processing is coming soon!");
    }
    
    /**
     * Set the current user ID
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("Course details received user ID: " + userId);
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
     * Handle the test learning interface button click
     * This is for testing purposes only - bypasses login and enrollment requirements
     */
    @FXML
    private void handleTestLearning() {
        try {
            System.out.println("Test learning button clicked, current course: " + 
                (currentCourse != null ? currentCourse.getTitre() : "null"));
            
            if (currentCourse == null) {
                showAlert(Alert.AlertType.ERROR, "Test Error", 
                    "No course selected", "Please ensure a course is selected before testing.");
                return;
            }
            
            // Get the path to the FXML file
            String fxmlPath = "/lesson/course_learning.fxml";
            System.out.println("Loading FXML from: " + fxmlPath);
            
            // First check if the resource exists
            java.net.URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("CRITICAL ERROR: FXML resource not found: " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Resource Error", 
                    "FXML file not found", "Could not find " + fxmlPath + " in the resources directory.");
                return;
            }
            System.out.println("Found FXML resource at: " + resourceUrl);
            
            // Check if CSS exists
            String cssPath = "/css/course_learning.css";
            java.net.URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                System.err.println("WARNING: CSS resource not found: " + cssPath);
                // We'll continue without CSS if it's missing
            } else {
                System.out.println("Found CSS resource at: " + cssUrl);
            }
            
            // Load the course learning interface
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            
            // Load the FXML
            Parent root;
            try {
                root = loader.load();
                System.out.println("FXML successfully loaded");
            } catch (Exception e) {
                System.err.println("Failed to load FXML: " + e.getMessage());
                e.printStackTrace();
                // Show the full exception details including cause
                Throwable cause = e.getCause();
                String causeMessage = (cause != null) ? cause.toString() : "Unknown cause";
                showAlert(Alert.AlertType.ERROR, "FXML Error", 
                    "Could not load learning interface", 
                    "FXML loading error: " + e.getMessage() + "\nCause: " + causeMessage);
                return;
            }
            
            // Get the controller
            try {
                Object controllerObj = loader.getController();
                System.out.println("Controller class: " + (controllerObj != null ? controllerObj.getClass().getName() : "null"));
                
                if (controllerObj == null) {
                    System.err.println("CRITICAL ERROR: Controller is null");
                    showAlert(Alert.AlertType.ERROR, "Controller Error", 
                        "Controller is null", "The FXML loader could not find or create the controller.");
                    return;
                }
                
                if (!(controllerObj instanceof org.example.controller.lesson.CourseLearningController)) {
                    System.err.println("CRITICAL ERROR: Wrong controller type: " + controllerObj.getClass().getName());
                    showAlert(Alert.AlertType.ERROR, "Controller Error", 
                        "Wrong controller type", "Expected CourseLearningController but got " + 
                        controllerObj.getClass().getName());
                    return;
                }
                
                org.example.controller.lesson.CourseLearningController controller = 
                    (org.example.controller.lesson.CourseLearningController)controllerObj;
                
                // Pass the course data - using a test user ID of 999 for testing
                try {
                    controller.setCurrentUserId(999); // Test user ID
                    System.out.println("Set user ID successfully");
                    
                    controller.setCourse(currentCourse);
                    System.out.println("Set course successfully: " + currentCourse.getTitre());
                } catch (Exception e) {
                    System.err.println("Error setting controller properties: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Controller Error", 
                        "Could not initialize learning interface", "Error setting properties: " + e.getMessage());
                    return;
                }
            } catch (Exception e) {
                System.err.println("Failed to initialize controller: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Controller Error", 
                    "Could not initialize learning interface", "Controller error: " + e.getMessage());
                return;
            }
            
            // Set the scene
            try {
                Scene scene = startLearningButton.getScene();
                
                if (scene == null) {
                    System.err.println("Scene is null - cannot navigate");
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                        "Cannot access the scene", "The application scene is not available.");
                    return;
                }
                
                // Clear all existing stylesheets to prevent style contamination
                scene.getStylesheets().clear();
                
                // Add the learning interface CSS if it exists
                if (cssUrl != null) {
                    String learningCssPath = cssUrl.toExternalForm();
                    System.out.println("Adding CSS: " + learningCssPath);
                    scene.getStylesheets().add(learningCssPath);
                } else {
                    System.out.println("Skipping CSS addition as resource not found");
                }
                
                // Set the new root
                scene.setRoot(root);
                
                System.out.println("TEST MODE: Successfully launched learning interface");
            } catch (Exception e) {
                System.err.println("Scene transition error: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Scene Error", 
                    "Could not switch to learning interface", "Scene error: " + e.getMessage());
                return;
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in handleTestLearning: " + e.getMessage());
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Caused by: " + cause.toString());
                cause.printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Test Navigation Error", 
                "Could not open test learning interface", "Unexpected error: " + e.getMessage() +
                (cause != null ? "\nCause: " + cause.toString() : ""));
        }
    }
} 