package com.fhm.take2;

import com.Client;
import com.crdt.Post;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class HomePageController {

    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private VBox postsContainer;
    @FXML private VBox recentPostsContainer;
    @FXML private ScrollPane recentScrollPane;
    @FXML private ScrollPane postsScrollPane;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;

    private User currentUser;
    private ArrayList<PostPreviewTemplateController> postPreviewControllers;

    public void InitData(User user, String searchPrompt) {
        currentUser = user;
        this.searchField.setText(searchPrompt);
        postPreviewControllers = new ArrayList<>();

        // Update UI based on login status
        updateLoginUI();

        try {
            Map<Post, Integer> postFeed = Client.GetPostFeed(currentUser, 0);
            for (Post post : postFeed.keySet()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                Node postNode = loader.load();

                PostPreviewTemplateController controller = loader.getController();
                controller.init(post, user, postFeed.get(post));

                postsContainer.getChildren().add(postNode);
                postPreviewControllers.add(controller);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Setup scroll behavior
        postsScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            postsScrollPane.setVvalue(postsScrollPane.getVvalue() - delta / postsScrollPane.getContent().getBoundsInLocal().getHeight());
        });
    }

    private void updateLoginUI() {
        if(currentUser != null) {
            // User is logged in
            loggedOutPane.setDisable(true);
            loggedOutPane.setVisible(false);
            loggedInPane.setDisable(false);
            loggedInPane.setVisible(true);
            // userPFP.setImage(currentUser.getProfilePicture());
        } else {
            // User is logged out
            loggedOutPane.setDisable(false);
            loggedOutPane.setVisible(true);
            loggedInPane.setDisable(true);
            loggedInPane.setVisible(false);
        }
    }

    @FXML
    void Chat(MouseEvent event) {
        if (currentUser == null) {
            showLoginAlert();
            return;
        }
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("message.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Chats");
            stage.setScene(new Scene(root, 800, 600));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed");
        Clean();
        event.consume();
    }

    @FXML
    void ClearRecentPosts(MouseEvent event) {
        System.out.println("Clear Recent Posts Button Pressed");
        event.consume();
    }

    @FXML
    void CreatePost(MouseEvent event) {
        if (currentUser == null) {
            showLoginAlert();
            return;
        }
        System.out.println("Create Post Button Pressed");
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(currentUser);

            // Get the current stage
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Create Post - Reddit");
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        if (currentUser == null) {
            showLoginAlert();
            return;
        }
        System.out.println("Create Subcreddit Button Pressed");
        Clean();
        event.consume();
    }

    @FXML
    void Login(MouseEvent event) {
        System.out.println("Login Button Pressed");
        navigateToLoginDialog();
        event.consume();
    }

    // In the navigateToLoginDialog method, update the stage title:
    private void navigateToLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Get the login controller
            LoginController loginController = loader.getController();

            // Create a new stage for login (dialog)
            Stage loginStage = new Stage();
            loginStage.setTitle("Login - Creddit"); // Changed to Creddit
            loginStage.setScene(new Scene(root, 400, 500));
            loginStage.setResizable(false);

            // Set modality so it blocks interaction with homepage
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(postsContainer.getScene().getWindow());

            // Set up callback for successful login
            loginController.setOnLoginSuccess(user -> {
                this.currentUser = user;
                updateLoginUI();
                loginStage.close();
                HelloApplication.startSession(currentUser);
                // Refresh the page to show user-specific content
                Refresh(null);
            });

            loginStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Unable to load login page: " + e.getMessage());
        }
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        if (currentUser == null) {
            showLoginAlert();
            return;
        }
        System.out.println("Profile Button Pressed");
        Clean();
        event.consume();
    }

    @FXML
    void Refresh(MouseEvent event) {
        System.out.println("Dashboard Button Pressed");
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, searchField.getText());

            // Get the current stage
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Reddit - Home");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(event != null)
            event.consume();
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        System.out.println("Search Pressed");
        Clean();
        event.consume();
    }

    private void showLoginAlert() {
        showAlert("Login Required", "Please log in to access this feature.");
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert to match our dark theme
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    private void Clean() {
        if (postPreviewControllers != null) {
            for(PostPreviewTemplateController controller : postPreviewControllers) {
                if(controller != null && controller.mediaViewController != null) {
                    controller.mediaViewController.Clean();
                }
            }
        }
    }
}