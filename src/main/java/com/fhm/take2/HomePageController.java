package com.fhm.take2;

import com.Client;
import com.crdt.Post;
import com.crdt.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;

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
    private boolean updating = false;
    private boolean scrollCooldown = false;
    private int filter; // 0 for posts, 1 for subcreddits, 2 for comments, 3 for users

    public void InitData(User user, String searchPrompt, int filter) {
        currentUser = user;
        this.filter = filter;
        this.searchField.setText(searchPrompt);
        postPreviewControllers = new ArrayList<>();

        // Update UI based on login status
        updateLoginUI();

        try {
            Map<Post, Integer> postFeed = Client.GetPostFeed(currentUser, searchPrompt, 0);
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

        postsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if(!updating && !scrollCooldown && newVal.doubleValue() >= postsScrollPane.getVmax()) {
                updating = true;
                scrollCooldown = true;
                try {
                    Map<Post, Integer> postFeed = Client.GetPostFeed(currentUser, searchPrompt, postPreviewControllers.getLast().GetPostID());
                    for (Post post : postFeed.keySet()) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                        Node postNode = loader.load();
                        PostPreviewTemplateController controller = loader.getController();
                        controller.init(post, user, postFeed.get(post));
                        postsContainer.getChildren().add(postNode);
                        postPreviewControllers.add(controller);
                    }
                    updating = false;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.setOnFinished(e -> scrollCooldown = false);
                pause.play();
            }
        });
        postsScrollPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) event.consume();
            if (event.getCode() == KeyCode.TAB) event.consume();
        });
    }

    private void updateLoginUI() {
        if(currentUser != null) {
            // User is logged in
            loggedOutPane.setVisible(false);
            loggedInPane.setVisible(true);
            //userPFP.setImage(currentUser.getPfp());
        } else {
            // User is logged out
            loggedOutPane.setVisible(true);
            loggedInPane.setVisible(false);
            recentScrollPane.setVisible(false);
        }
    }

    @FXML
    void Chat(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("message.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Chats");
            stage.setScene(new Scene(root, 800, 600));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.initOwner(postsContainer.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed");
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
            Login();
            return;
        }
        Clean();
        try {
            if(!Client.isServerReachable()) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("error_404.fxml"));
                Parent root = fxmlLoader.load();

                Error404Controller error404Controller = fxmlLoader.getController();
                error404Controller.refreshButton.setOnAction(e -> {
                    if(!Client.isServerReachable()) {
                        return;
                    }
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
                    Parent root2 = null;
                    try {
                        root2 = loader.load();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    CreatePostPageController createPostPageController = loader.getController();
                    createPostPageController.InitData(currentUser);

                    // Get the current stage
                    Stage stage = (Stage) error404Controller.refreshButton.getScene().getWindow();

                    // Set the new scene
                    stage.setScene(new Scene(root2));
                });

                // Get the current stage
                Stage stage = (Stage) postsContainer.getScene().getWindow();
                // Set the new scene
                stage.setScene(new Scene(root));
            }
            else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
                Parent root = loader.load();

                CreatePostPageController createPostPageController = loader.getController();
                createPostPageController.InitData(currentUser);

                // Get the current stage
                Stage stage = (Stage) postsContainer.getScene().getWindow();
                // Set the new scene
                stage.setScene(new Scene(root));
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-subcreddit-page.fxml"));
            Parent root = loader.load();

            // Get the login controller
            CreateSubcredditPageController createSubcredditPageController = loader.getController();
            createSubcredditPageController.InitData(this.currentUser);

            // Create a new stage for login (dialog)
            Stage createSubcredditStage = new Stage();
            createSubcredditStage.setTitle("Create Subcreddit");
            createSubcredditStage.setScene(new Scene(root, 600, 400));
            createSubcredditStage.setResizable(false);

            // Set modality so it blocks interaction with homepage
            createSubcredditStage.initModality(Modality.WINDOW_MODAL);
            createSubcredditStage.initOwner(postsContainer.getScene().getWindow());

            // Set up callback for successful login
            createSubcredditPageController.setOnCreationSuccess(sub -> {
                createSubcredditStage.close();
                //TODO: Open Subcreddit Page here
            });

            createSubcredditStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void Login() {
        navigateToLoginDialog();
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
            loginStage.setTitle("Login"); // Changed to Creddit
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
                Refresh();
            });

            loginStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        System.out.println("My Profile Button Pressed");
        //Clean();
        event.consume();
    }

    @FXML
    void Refresh() {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, searchField.getText(), filter);

            // Get the current stage
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoHome(MouseEvent event) {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, null, 0);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            Refresh();
            event.consume();
        }
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