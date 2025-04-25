package org.example.controller.forum;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.models.forum.Post;
import org.example.models.forum.Comment;
import org.example.models.forum.Tag;
import org.example.services.forum.PostService;
import org.example.services.forum.CommentService;
import org.example.services.forum.TagService;
import org.example.services.forum.PostReactionService;
import org.example.services.UserService;
import org.example.utils.InputValidator;
import org.example.utils.InputValidator.ValidationResult;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class UserForumController {
    @FXML
    private ListView<Integer> forumListView;
    @FXML
    private TextArea postTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField tagTextField;
    @FXML
    private VBox postDetailsBox;
    @FXML
    private TextArea commentTextArea;
    @FXML
    private ListView<String> commentsListView;
    @FXML
    private Label likesLabel;
    @FXML
    private Label dislikesLabel;
    @FXML
    private Button postButton;
    @FXML
    private Label postTitleLabel;
    @FXML
    private Label postContentLabel;
    @FXML
    private Label postTagsLabel;
    @FXML
    private Label titleCountLabel;
    @FXML
    private Label contentCountLabel;
    @FXML
    private Label commentCountLabel;
    @FXML
    private Button deletePostButton;
    @FXML
    private Button deleteCommentButton;
    
    private PostService postService;
    private CommentService commentService;
    private TagService tagService;
    private PostReactionService reactionService;
    private UserService userService;
    private Post selectedPost;
    private int currentUserId = 1; // Temporary hardcoded user ID
    private final int MAX_TITLE_LENGTH = 100;
    private final int MAX_CONTENT_LENGTH = 5000;
    private final int MAX_COMMENT_LENGTH = 1000;
    private List<Comment> currentComments = new ArrayList<>();
    private Comment selectedComment;

    @FXML
    private void initialize() {
        try {
            postService = new PostService();
            commentService = new CommentService();
            tagService = new TagService();
            reactionService = new PostReactionService();
            userService = new UserService();
            
            // Find a valid user from the database
            initializeValidUser();
            
            // Set up custom cell factories
            setupForumListViewCellFactory();
            setupCommentsListViewCellFactory();
            
            // Configure les écouteurs et validations en temps réel
            setupInputValidation();
            
            loadPosts();
            
            forumListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    try {
                        // Extract post ID from the custom cell content
                        int postId = (int) forumListView.getSelectionModel().getSelectedItem();
                        selectedPost = postService.read(postId);
                        if (selectedPost != null) {
                            showPostDetails(selectedPost);
                        } else {
                            showError("Publication non trouvée");
                        }
                    } catch (SQLException e) {
                        showError("Erreur de base de données: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        showError("Erreur inattendue: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            showError("Erreur d'initialisation du forum: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeValidUser() {
        try {
            // Try to find a valid user or create one if none exists
            currentUserId = userService.getOrCreateValidUserId();
        } catch (SQLException e) {
            showError("Erreur lors de l'initialisation de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupForumListViewCellFactory() {
        forumListView.setCellFactory(listView -> new ListCell<Integer>() {
            private final TranslateTransition translate = new TranslateTransition(Duration.millis(200));
            
            {
                // Set up hover effect animation
                setOnMouseEntered(e -> {
                    if (!isEmpty()) {
                        setStyle("-fx-background-color: #f5f5f5;");
                        translate.setNode(this);
                        translate.setFromX(0);
                        translate.setToX(5);
                        translate.playFromStart();
                    }
                });
                
                setOnMouseExited(e -> {
                    if (!isEmpty()) {
                        setStyle("-fx-background-color: transparent;");
                        translate.setNode(this);
                        translate.setFromX(5);
                        translate.setToX(0);
                        translate.playFromStart();
                    }
                });
            }
            
            @Override
            protected void updateItem(Integer postId, boolean empty) {
                super.updateItem(postId, empty);
                
                // Reset styles and transition state
                setStyle("-fx-background-color: transparent;");
                
                if (empty || postId == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        Post post = postService.read(postId);
                        
                        // Create custom layout for each post cell
                        VBox container = new VBox();
                        container.setSpacing(5);
                        container.setPadding(new Insets(10));
                        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
                        
                        // Post title with styling
                        Label titleLabel = new Label(post.getTitle());
                        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                        titleLabel.setStyle("-fx-text-fill: #303030;");
                        
                        // Post date
                        Label dateLabel = new Label(post.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                        dateLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");
                        
                        // Tags for the post
                        HBox tagsBox = new HBox();
                        tagsBox.setSpacing(5);
                        List<Tag> tags = tagService.getTagsForPost(post.getId());
                        for (Tag tag : tags) {
                            Label tagLabel = new Label("#" + tag.getName());
                            tagLabel.setStyle("-fx-text-fill: #5e35b1; -fx-font-size: 11px; -fx-background-color: #f3e5f5; -fx-background-radius: 12; -fx-padding: 2 8;");
                            tagsBox.getChildren().add(tagLabel);
                            if (tagsBox.getChildren().size() >= 3) break; // Limit to 3 tags
                        }
                        
                        // Engagement statistics
                        HBox statsBox = new HBox();
                        statsBox.setSpacing(15);
                        statsBox.setAlignment(Pos.CENTER_LEFT);
                        
                        Label likesStats = new Label("👍 " + reactionService.getLikeCount(post.getId()));
                        likesStats.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
                        
                        Label commentsStats = new Label("💬 " + getCommentCount(post.getId()));
                        commentsStats.setStyle("-fx-text-fill: #3949ab; -fx-font-size: 12px;");
                        
                        statsBox.getChildren().addAll(likesStats, commentsStats);
                        
                        // Add all components to the container
                        container.getChildren().addAll(titleLabel, dateLabel, tagsBox, statsBox);
                        
                        setGraphic(container);
                        setText(null);
                    } catch (SQLException e) {
                        setText("Error loading post");
                        setGraphic(null);
                    }
                }
            }
        });
    }
    
    private void setupCommentsListViewCellFactory() {
        commentsListView.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String commentStr, boolean empty) {
                super.updateItem(commentStr, empty);
                
                if (empty || commentStr == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    try {
                        // Extract comment ID and content from string
                        String[] parts = commentStr.split(" - ", 2);
                        int commentId = Integer.parseInt(parts[0]);
                        Comment comment = commentService.read(commentId);
                        
                        // Create custom layout for each comment cell
                        VBox container = new VBox();
                        container.setSpacing(5);
                        container.setPadding(new Insets(8));
                        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
                        
                        // Comment user info
                        HBox userInfoBox = new HBox();
                        userInfoBox.setAlignment(Pos.CENTER_LEFT);
                        userInfoBox.setSpacing(8);
                        
                        Label userLabel = new Label("User #" + comment.getOwnerId());
                        userLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                        userLabel.setStyle("-fx-text-fill: #5e35b1;");
                        
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        
                        Label dateLabel = new Label(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
                        dateLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");
                        
                        userInfoBox.getChildren().addAll(userLabel, spacer, dateLabel);
                        
                        // Comment content
                        Label contentLabel = new Label(comment.getContent());
                        contentLabel.setWrapText(true);
                        contentLabel.setStyle("-fx-text-fill: #424242; -fx-font-size: 13px;");
                        
                        // Add all components to the container
                        container.getChildren().addAll(userInfoBox, contentLabel);
                        
                        setGraphic(container);
                        setText(null);
                    } catch (Exception e) {
                        setText("Error loading comment");
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadPosts() {
        try {
            forumListView.getItems().clear();
            List<Post> posts = postService.readAll();
            for (Post post : posts) {
                forumListView.getItems().add(post.getId());
            }
        } catch (SQLException e) {
            showError("Error loading posts: " + e.getMessage());
        }
    }

    private void showPostDetails(Post post) {
        // Hide the details box initially for animation
        postDetailsBox.setOpacity(0);
        postDetailsBox.setScaleX(0.95);
        postDetailsBox.setScaleY(0.95);
        postDetailsBox.setVisible(true);
        
        try {
            // Load tags
            List<Tag> tags = tagService.getTagsForPost(post.getId());
            StringBuilder tagString = new StringBuilder();
            for (Tag tag : tags) {
                tagString.append(tag.getName()).append(", ");
            }
            // Remove trailing comma if exists
            if (tagString.length() > 2) {
                tagString.setLength(tagString.length() - 2);
            }
            
            // Set post details in post details section
            postTitleLabel.setText(post.getTitle());
            postContentLabel.setText(post.getContent());
            postTagsLabel.setText("Tags: " + tagString.toString());
            
            // Check if the post belongs to the current user and update delete button visibility
            if (post.getOwnerId() == currentUserId) {
                // Post belongs to current user, allow deletion
                deletePostButton.setVisible(true);
                deletePostButton.setManaged(true);
            } else {
                // Post belongs to someone else, hide delete button
                deletePostButton.setVisible(false);
                deletePostButton.setManaged(false);
            }
            
            // Load comments
            loadComments(post.getId());
            
            // Hide the delete comment button initially as no comment is selected
            deleteCommentButton.setVisible(false);
            deleteCommentButton.setManaged(false);
            
            // Load reactions
            updateReactionCounts(post.getId());
            
            // Animate the post details box
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), postDetailsBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), postDetailsBox);
            scaleIn.setFromX(0.95);
            scaleIn.setFromY(0.95);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            
            fadeIn.play();
            scaleIn.play();
            
        } catch (SQLException e) {
            showError("Error loading post details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadComments(int postId) {
        try {
            commentsListView.getItems().clear();
            currentComments = commentService.readByPostId(postId);
            
            if (currentComments.isEmpty()) {
                commentsListView.getItems().add("No comments yet. Be the first to comment!");
                return;
            }
            
            for (Comment comment : currentComments) {
                // Tenter de récupérer le nom d'utilisateur
                String userInfo;
                try {
                    userInfo = userService.getUsernameById(comment.getOwnerId());
                } catch (SQLException e) {
                    userInfo = "User ID: " + comment.getOwnerId();
                }
                
                commentsListView.getItems().add(String.format("%d - [%s] %s - %s", 
                    comment.getId(),
                    userInfo,
                    comment.getContent(),
                    comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            }
            
            // Configurer le gestionnaire de sélection pour les commentaires
            commentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals("No comments yet. Be the first to comment!")) {
                    // Analyser la sélection pour extraire l'ID du commentaire
                    try {
                        int index = commentsListView.getSelectionModel().getSelectedIndex();
                        if (index >= 0 && index < currentComments.size()) {
                            selectedComment = currentComments.get(index);
                            
                            // Afficher le bouton de suppression uniquement si le commentaire appartient à l'utilisateur actuel
                            if (selectedComment.getOwnerId() == currentUserId) {
                                deleteCommentButton.setVisible(true);
                                deleteCommentButton.setManaged(true);
                            } else {
                                deleteCommentButton.setVisible(false);
                                deleteCommentButton.setManaged(false);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        deleteCommentButton.setVisible(false);
                        deleteCommentButton.setManaged(false);
                    }
                } else {
                    selectedComment = null;
                    deleteCommentButton.setVisible(false);
                    deleteCommentButton.setManaged(false);
                }
            });
            
        } catch (SQLException e) {
            showError("Error loading comments: " + e.getMessage());
        }
    }

    private void updateReactionCounts(int postId) {
        try {
            int likes = reactionService.getLikeCount(postId);
            int dislikes = reactionService.getDislikeCount(postId);
            likesLabel.setText("Likes: " + likes);
            dislikesLabel.setText("Dislikes: " + dislikes);
        } catch (SQLException e) {
            showError("Error loading reaction counts: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewPost() {
        // Clear the form for a new post
        titleTextField.clear();
        postTextField.clear();
        tagTextField.clear();
        
        // Reset selection and hide details
        forumListView.getSelectionModel().clearSelection();
        selectedPost = null;
        postDetailsBox.setVisible(false);
        
        // Focus on the title field
        titleTextField.requestFocus();
    }

    /**
     * Valide l'ensemble du formulaire de création de publication
     * @return true si le formulaire est valide, false sinon
     */
    private boolean validatePostForm() {
        String title = titleTextField.getText().trim();
        String content = postTextField.getText().trim();
        String tags = tagTextField.getText().trim();
        
        StringBuilder errorMessages = new StringBuilder();
        boolean isValid = true;
        
        // Validation du titre
        ValidationResult titleResult = InputValidator.validateTitle(title);
        if (!titleResult.isValid()) {
            errorMessages.append("- ").append(titleResult.getErrorMessage()).append("\n");
            isValid = false;
        }
        
        // Validation du contenu
        ValidationResult contentResult = InputValidator.validatePostContent(content);
        if (!contentResult.isValid()) {
            errorMessages.append("- ").append(contentResult.getErrorMessage()).append("\n");
            isValid = false;
        }
        
        // Validation des tags
        ValidationResult tagsResult = InputValidator.validateTags(tags);
        if (!tagsResult.isValid()) {
            errorMessages.append("- ").append(tagsResult.getErrorMessage()).append("\n");
            isValid = false;
        }
        
        if (!isValid) {
            Platform.runLater(() -> showError("Veuillez corriger les erreurs suivantes:\n" + errorMessages.toString()));
        }
        
        return isValid;
    }

    @FXML
    private void handlePost() {
        // Valider le formulaire
        if (!validatePostForm()) {
            return;
        }
        
        String title = titleTextField.getText().trim();
        String content = postTextField.getText().trim();
        String tags = tagTextField.getText().trim();
        
        try {
            // Create the post
            Post post = new Post();
            post.setTitle(title);
            post.setContent(content);
            post.setOwnerId(currentUserId);
            postService.create(post);
            
            // Add tags if present
            if (!tags.isEmpty()) {
                String[] tagArray = tags.split(",");
                for (String tagName : tagArray) {
                    Tag tag = new Tag();
                    tag.setName(tagName.trim());
                    tagService.create(tag);
                    tagService.addTagToPost(post.getId(), tag.getId());
                }
            }
            
            // Animation for success feedback
            Button postButton = this.postButton;
            String originalStyle = postButton.getStyle();
            
            postButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 24; -fx-padding: 12 25; -fx-cursor: hand;");
            
            // Create a scale animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), postButton);
            scaleUp.setFromX(1.0);
            scaleUp.setFromY(1.0);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), postButton);
            scaleDown.setFromX(1.1);
            scaleDown.setFromY(1.1);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            
            scaleUp.setOnFinished(e -> {
                scaleDown.play();
            });
            
            scaleDown.setOnFinished(e -> {
                // Reset button style after animation
                postButton.setStyle(originalStyle);
                
                // Clear the form
                titleTextField.clear();
                postTextField.clear();
                tagTextField.clear();
                
                // Reset form styles
                titleTextField.setStyle("");
                postTextField.setStyle("");
                tagTextField.setStyle("");
                
                // Show success message AFTER animation using Platform.runLater
                Platform.runLater(() -> {
                    showInfo("Publication créée avec succès !");
                });
                
                // Reload the posts
                loadPosts();
            });
            
            scaleUp.play();
            
        } catch (SQLException e) {
            // Check if it's a foreign key constraint error
            if (e.getMessage().contains("foreign key constraint") && e.getMessage().contains("owner_id")) {
                // Try to reinitialize valid user and retry
                try {
                    // Show warning using Platform.runLater
                    Platform.runLater(() -> {
                        showWarning("Erreur de référence utilisateur. Tentative de réinitialisation...");
                    });
                    
                    currentUserId = userService.getOrCreateValidUserId();
                    
                    // Retry post creation with new user ID
                    Post post = new Post();
                    post.setTitle(title);
                    post.setContent(content);
                    post.setOwnerId(currentUserId);
                    postService.create(post);
                    
                    // Clear the form
                    titleTextField.clear();
                    postTextField.clear();
                    tagTextField.clear();
                    
                    // Success message and refresh using Platform.runLater
                    Platform.runLater(() -> {
                        showInfo("Publication créée avec succès après réinitialisation !");
                    });
                    
                    // Reload the posts
                    loadPosts();
                } catch (SQLException retryEx) {
                    Platform.runLater(() -> {
                        showError("Échec de la réinitialisation. Erreur: " + retryEx.getMessage());
                    });
                    retryEx.printStackTrace();
                }
            } else {
                Platform.runLater(() -> {
                    showError("Erreur lors de la création de la publication: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }
    }

    /**
     * Valide le formulaire de commentaire
     * @return true si le formulaire est valide, false sinon
     */
    private boolean validateCommentForm() {
        if (selectedPost == null) {
            Platform.runLater(() -> showError("Aucune publication sélectionnée"));
            return false;
        }
        
        String content = commentTextArea.getText().trim();
        ValidationResult commentResult = InputValidator.validateCommentContent(content);
        
        if (!commentResult.isValid()) {
            Platform.runLater(() -> showError(commentResult.getErrorMessage()));
            return false;
        }
        
        return true;
    }

    @FXML
    private void handleComment() {
        // Valider le formulaire de commentaire
        if (!validateCommentForm()) {
            return;
        }
        
        String content = commentTextArea.getText().trim();
        
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setPostId(selectedPost.getId());
            comment.setOwnerId(currentUserId);
            commentService.create(comment);
            
            // Clear the comment text field
            commentTextArea.clear();
            commentTextArea.setStyle("");
            
            // Show success message using Platform.runLater
            Platform.runLater(() -> {
                showInfo("Commentaire ajouté avec succès !");
            });
            
            // Reload comments with animation
            loadCommentsWithAnimation(selectedPost.getId());
            
            // Update the reaction counts
            updateReactionCounts(selectedPost.getId());
        } catch (SQLException e) {
            // Check if it's a foreign key constraint error
            if (e.getMessage().contains("foreign key constraint") && (e.getMessage().contains("owner_id") || e.getMessage().contains("user"))) {
                // Try to reinitialize valid user and retry
                try {
                    showWarning("Erreur de référence utilisateur. Tentative de réinitialisation...");
                    currentUserId = userService.getOrCreateValidUserId();
                    
                    // Retry comment creation with new user ID
                    Comment comment = new Comment();
                    comment.setContent(content);
                    comment.setPostId(selectedPost.getId());
                    comment.setOwnerId(currentUserId);
                    commentService.create(comment);
                    
                    // Clear the comment text field
                    commentTextArea.clear();
                    
                    // Show messages using Platform.runLater
                    Platform.runLater(() -> {
                        showWarning("Erreur de référence utilisateur. Tentative de réinitialisation...");
                    });
                    
                    // Show success message using Platform.runLater
                    Platform.runLater(() -> {
                        showInfo("Commentaire ajouté avec succès après réinitialisation !");
                    });
                    
                    // Reload comments with animation
                    loadCommentsWithAnimation(selectedPost.getId());
                    
                    // Update the reaction counts
                    updateReactionCounts(selectedPost.getId());
                } catch (SQLException retryEx) {
                    Platform.runLater(() -> {
                        showError("Échec de la réinitialisation. Erreur: " + retryEx.getMessage());
                    });
                    retryEx.printStackTrace();
                }
            } else {
                Platform.runLater(() -> {
                    showError("Erreur lors de l'ajout du commentaire: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLike() {
        if (selectedPost == null) return;
        try {
            reactionService.toggleReaction(selectedPost.getId(), currentUserId, true);
            updateReactionCounts(selectedPost.getId());
        } catch (SQLException e) {
            // Check if it's a foreign key constraint error
            if (e.getMessage().contains("foreign key constraint") && e.getMessage().contains("user_id")) {
                try {
                    // Try to reinitialize valid user and retry
                    currentUserId = userService.getOrCreateValidUserId();
                    reactionService.toggleReaction(selectedPost.getId(), currentUserId, true);
                    updateReactionCounts(selectedPost.getId());
                } catch (SQLException retryEx) {
                    Platform.runLater(() -> {
                        showError("Erreur lors de la réaction: " + retryEx.getMessage());
                    });
                }
            } else {
                Platform.runLater(() -> {
                    showError("Erreur lors de la réaction: " + e.getMessage());
                });
            }
        }
    }

    @FXML
    private void handleDislike() {
        if (selectedPost == null) return;
        try {
            reactionService.toggleReaction(selectedPost.getId(), currentUserId, false);
            updateReactionCounts(selectedPost.getId());
        } catch (SQLException e) {
            // Check if it's a foreign key constraint error
            if (e.getMessage().contains("foreign key constraint") && e.getMessage().contains("user_id")) {
                try {
                    // Try to reinitialize valid user and retry
                    currentUserId = userService.getOrCreateValidUserId();
                    reactionService.toggleReaction(selectedPost.getId(), currentUserId, false);
                    updateReactionCounts(selectedPost.getId());
                } catch (SQLException retryEx) {
                    Platform.runLater(() -> {
                        showError("Erreur lors de la réaction: " + retryEx.getMessage());
                    });
                }
            } else {
                Platform.runLater(() -> {
                    showError("Erreur lors de la réaction: " + e.getMessage());
                });
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    // Helper method to get comment count for a post
    private int getCommentCount(int postId) throws SQLException {
        List<Comment> comments = commentService.readByPostId(postId);
        return comments.size();
    }

    private void loadCommentsWithAnimation(int postId) throws SQLException {
        List<Comment> comments = commentService.readByPostId(postId);
        
        commentsListView.getItems().clear();
        for (Comment comment : comments) {
            commentsListView.getItems().add(comment.getId() + " - " + comment.getContent());
        }
        
        // Apply a fade transition to the comments list
        FadeTransition fade = new FadeTransition(Duration.millis(300), commentsListView);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.play();
        
        // Scroll to the last comment
        if (!comments.isEmpty()) {
            commentsListView.scrollTo(comments.size() - 1);
        }
    }

    /**
     * Configure la validation en temps réel des champs de saisie
     */
    private void setupInputValidation() {
        // Validation du titre en temps réel
        titleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            titleCountLabel.setText(length + "/" + MAX_TITLE_LENGTH);
            
            if (length > MAX_TITLE_LENGTH) {
                titleTextField.setText(oldValue);
                titleCountLabel.setText(oldValue.length() + "/" + MAX_TITLE_LENGTH);
                titleTextField.setStyle("-fx-border-color: #ff5555; -fx-background-color: #fff0f0;");
            } else if (length == 0) {
                // Champ vide
                titleCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                titleTextField.setStyle("");
            } else if (length < 5) {
                // Trop court
                titleCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5555;");
                titleTextField.setStyle("-fx-border-color: #ffaa55; -fx-background-color: #fffaf0;");
            } else if (length > MAX_TITLE_LENGTH * 0.9) {
                // Proche de la limite
                titleCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5555;");
                titleTextField.setStyle("-fx-border-color: #ffaa55;");
            } else {
                // Valide
                titleCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                titleTextField.setStyle("-fx-border-color: #4CAF50; -fx-background-color: #f0fff0;");
            }
            
            // Vérification du format
            if (length > 0 && !newValue.matches("^[a-zA-Z0-9\\sÀ-ÿ\\.,\\?!;:'\"-_\\(\\)]+$")) {
                titleTextField.setStyle("-fx-border-color: #ff5555; -fx-background-color: #fff0f0;");
            }
        });
        
        // Validation du contenu en temps réel
        postTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            contentCountLabel.setText(length + "/" + MAX_CONTENT_LENGTH);
            
            if (length > MAX_CONTENT_LENGTH) {
                postTextField.setText(oldValue);
                contentCountLabel.setText(oldValue.length() + "/" + MAX_CONTENT_LENGTH);
                postTextField.setStyle("-fx-border-color: #ff5555; -fx-background-color: #fff0f0;");
            } else if (length == 0) {
                // Champ vide
                contentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                postTextField.setStyle("");
            } else if (length < 10) {
                // Trop court
                contentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5555;");
                postTextField.setStyle("-fx-border-color: #ffaa55; -fx-background-color: #fffaf0;");
            } else if (length > MAX_CONTENT_LENGTH * 0.9) {
                // Proche de la limite
                contentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5555;");
                postTextField.setStyle("-fx-border-color: #ffaa55;");
            } else {
                // Valide
                contentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                postTextField.setStyle("-fx-border-color: #4CAF50; -fx-background-color: #f0fff0;");
            }
        });
        
        // Validation des tags en temps réel
        tagTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                tagTextField.setStyle("");
                return;
            }
            
            ValidationResult result = InputValidator.validateTags(newValue);
            if (result.isValid()) {
                tagTextField.setStyle("-fx-border-color: #4CAF50; -fx-background-color: #f0fff0;");
            } else {
                tagTextField.setStyle("-fx-border-color: #ffaa55; -fx-background-color: #fffaf0;");
            }
        });
        
        // Validation du commentaire en temps réel
        commentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            commentCountLabel.setText(length + "/" + MAX_COMMENT_LENGTH);
            
            if (length > MAX_COMMENT_LENGTH) {
                commentTextArea.setText(oldValue);
                commentCountLabel.setText(oldValue.length() + "/" + MAX_COMMENT_LENGTH);
                commentTextArea.setStyle("-fx-border-color: #ff5555; -fx-background-color: #fff0f0;");
            } else if (length == 0) {
                // Champ vide
                commentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                commentTextArea.setStyle("");
            } else if (length < 2) {
                // Trop court
                commentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5555;");
                commentTextArea.setStyle("-fx-border-color: #ffaa55; -fx-background-color: #fffaf0;");
            } else if (length > MAX_COMMENT_LENGTH * 0.9) {
                // Proche de la limite
                commentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff5555;");
                commentTextArea.setStyle("-fx-border-color: #ffaa55;");
            } else {
                // Valide
                commentCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                commentTextArea.setStyle("-fx-border-color: #4CAF50; -fx-background-color: #f0fff0;");
            }
            
            // Vérification du format
            if (length > 0 && !newValue.matches("^[a-zA-Z0-9\\sÀ-ÿ\\.,\\?!;:'\"-_\\(\\)\\[\\]]+$")) {
                commentTextArea.setStyle("-fx-border-color: #ff5555; -fx-background-color: #fff0f0;");
            }
        });
        
        // Initialisation des compteurs
        titleCountLabel.setText("0/" + MAX_TITLE_LENGTH);
        contentCountLabel.setText("0/" + MAX_CONTENT_LENGTH);
        commentCountLabel.setText("0/" + MAX_COMMENT_LENGTH);
    }

    /**
     * Gérer la suppression d'un post par l'utilisateur courant
     */
    @FXML
    private void handleDeletePost() {
        if (selectedPost == null) {
            showError("No post selected");
            return;
        }
        
        // Vérifier que le post appartient à l'utilisateur actuel
        if (selectedPost.getOwnerId() != currentUserId) {
            showError("You can only delete your own posts");
            return;
        }
        
        // Confirmer la suppression
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Post");
        alert.setHeaderText("Delete Post");
        alert.setContentText("Are you sure you want to delete this post? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int postId = selectedPost.getId();
                    System.out.println("Deleting post ID: " + postId);
                    
                    // 1. Supprimer les réactions
                    try {
                        reactionService.deleteAllForPost(postId);
                    } catch (SQLException e) {
                        System.out.println("Error deleting reactions: " + e.getMessage());
                    }
                    
                    // 2. Supprimer les associations de tags
                    try {
                        tagService.removeAllTagsFromPost(postId);
                    } catch (SQLException e) {
                        System.out.println("Error removing tag associations: " + e.getMessage());
                    }
                    
                    // 3. Supprimer les commentaires
                    try {
                        List<Comment> comments = commentService.readByPostId(postId);
                        for (Comment comment : comments) {
                            commentService.delete(comment.getId());
                        }
                    } catch (SQLException e) {
                        System.out.println("Error deleting comments: " + e.getMessage());
                    }
                    
                    // 4. Supprimer le post
                    postService.delete(postId);
                    
                    // Mettre à jour l'interface
                    loadPosts();
                    
                    // Masquer les détails du post
                    postDetailsBox.setVisible(false);
                    selectedPost = null;
                    
                    showInfo("Post deleted successfully");
                } catch (SQLException e) {
                    showError("Error deleting post: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Gérer la suppression d'un commentaire par l'utilisateur courant
     */
    @FXML
    private void handleDeleteComment() {
        if (selectedComment == null) {
            showError("No comment selected");
            return;
        }
        
        // Vérifier que le commentaire appartient à l'utilisateur actuel
        if (selectedComment.getOwnerId() != currentUserId) {
            showError("You can only delete your own comments");
            return;
        }
        
        // Confirmer la suppression
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Comment");
        alert.setHeaderText("Delete Comment");
        alert.setContentText("Are you sure you want to delete this comment? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Supprimer le commentaire
                    commentService.delete(selectedComment.getId());
                    
                    // Recharger les commentaires
                    loadComments(selectedPost.getId());
                    
                    // Réinitialiser la sélection
                    selectedComment = null;
                    
                    // Masquer le bouton de suppression
                    deleteCommentButton.setVisible(false);
                    deleteCommentButton.setManaged(false);
                    
                    showInfo("Comment deleted successfully");
                } catch (SQLException e) {
                    showError("Error deleting comment: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
} 