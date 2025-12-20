package com.fhm.take2;

import com.Client;
import com.crdt.Post;
import com.crdt.Report;
import com.crdt.Subcreddit;
import com.crdt.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UserProfilePageController {

    @FXML private TextArea bioTextArea;
    @FXML private Button filterCommentsButton;
    @FXML private Button filterPostsButton;
    @FXML private AnchorPane friendButtonAnchor;
    @FXML private AnchorPane friendRequestAnchor;
    @FXML private Label lastOnlineLabel;
    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private Button pendingFriendButton;
    @FXML private VBox postsContainer;
    @FXML private ScrollPane postsScrollPane;
    @FXML private ImageView profilePFP;
    @FXML private Label registerDateLabel;
    @FXML private TextField searchField;
    @FXML private Label subcredditCountLabel;
    @FXML private AnchorPane unfriendButtonAnchor;
    @FXML private AnchorPane chatAnchor;
    @FXML private ImageView userPFP;
    @FXML private Label usernameLabel;
    @FXML private Label usernameLabel1;

    private User currentUser, profileUser;
    private ArrayList<Subcreddit> profileSubs;
    private ArrayList<PostPreviewTemplateController> postPreviewControllers;
    private boolean updating = false;
    private boolean scrollCooldown = false;
    private boolean filterPosts;

    public void InitData(int profileUserID, User currentUser, String searchPrompt, boolean filterPosts) {
        this.currentUser = currentUser;
        try {
            this.profileUser = Client.GetUser(profileUserID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.filterPosts = filterPosts;
        this.searchField.setText(searchPrompt);
        postPreviewControllers = new ArrayList<>();

        updateLoginUI();

        usernameLabel.setText(this.profileUser.getUsername());
        usernameLabel1.setText("u/" + this.profileUser.getUsername());
        registerDateLabel.setText(this.profileUser.getTimeCreated().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if(this.profileUser.getLastSeen().toInstant().isAfter(Instant.now().minusSeconds(60)))
            lastOnlineLabel.setText("now");
        else
            lastOnlineLabel.setText(PostPreviewTemplateController.timeAgo(this.profileUser.getLastSeen()));

        if(filterPosts)
            filterPostsButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else
            filterCommentsButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");

        if(currentUser != null) {
            chatAnchor.setVisible(true);
            try {
                profileSubs = Client.GetUserSubcreddits(this.profileUser);

                if (Client.GetFriends(this.currentUser).contains(this.profileUser))
                    unfriendButtonAnchor.setVisible(true);
                else if (Client.GetReceivedFriendRequests(this.currentUser).contains(this.profileUser))
                    friendRequestAnchor.setVisible(true);
                else if (Client.GetSentFriendRequests(this.currentUser).contains(this.profileUser))
                    pendingFriendButton.setVisible(true);
                else
                    friendButtonAnchor.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        subcredditCountLabel.setText(String.valueOf(profileSubs.size()));
        bioTextArea.setText(this.profileUser.getBio());

        try {
            if(filterPosts) {
                Map<Post, Integer> postFeed = Client.GetPostFeedFilterAuthor(this.currentUser, this.profileUser, searchPrompt, 0);
                for (Post post : postFeed.keySet()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                    Node postNode = loader.load();

                    PostPreviewTemplateController controller = loader.getController();
                    controller.init(post, this.currentUser, postFeed.get(post));

                    postsContainer.getChildren().add(postNode);
                    postPreviewControllers.add(controller);
                }
            }
            else {
                //TODO: Filter by comments here
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
                    Map<Post, Integer> postFeed = Client.GetPostFeedFilterAuthor(currentUser, this.profileUser, searchPrompt, postPreviewControllers.getLast().GetPostID());
                    for (Post post : postFeed.keySet()) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                        Node postNode = loader.load();
                        PostPreviewTemplateController controller = loader.getController();
                        controller.init(post, this.currentUser, postFeed.get(post));
                        postsContainer.getChildren().add(postNode);
                        postPreviewControllers.add(controller);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                updating = false;
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> scrollCooldown = false);
                pause.play();
            }
        });

        pendingFriendButton.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                pendingFriendButton.setAlignment(Pos.CENTER);
                pendingFriendButton.setText("X");
                pendingFriendButton.setStyle("-fx-background-color: gray; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
            }
            else {
                pendingFriendButton.setAlignment(Pos.CENTER_RIGHT);
                pendingFriendButton.setText("Pending Friend");
                pendingFriendButton.setStyle("-fx-background-color: red; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
            }
        });
    }

    private void updateLoginUI() {
        if(currentUser != null) {
            // User is logged in
            loggedOutPane.setDisable(true);
            loggedOutPane.setVisible(false);
            loggedInPane.setDisable(false);
            loggedInPane.setVisible(true);
            //userPFP.setImage(currentUser.getPfp());
        } else {
            // User is logged out
            loggedOutPane.setDisable(false);
            loggedOutPane.setVisible(true);
            loggedInPane.setDisable(true);
            loggedInPane.setVisible(false);
        }
    }

    @FXML
    void AcceptFriend(MouseEvent event) {
        try {
            Client.AcceptFriendRequest(profileUser, currentUser);
            friendRequestAnchor.setDisable(true);
            friendRequestAnchor.setVisible(false);
            unfriendButtonAnchor.setDisable(false);
            unfriendButtonAnchor.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void RejectFriend(MouseEvent event) {
        try {
            Client.Unfriend(currentUser, profileUser);
            friendRequestAnchor.setDisable(true);
            friendRequestAnchor.setVisible(false);
            friendButtonAnchor.setDisable(false);
            friendButtonAnchor.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void FriendRequest(MouseEvent event) {
        try {
            Client.SendFriendRequest(currentUser, profileUser);
            friendButtonAnchor.setDisable(true);
            friendButtonAnchor.setVisible(false);
            pendingFriendButton.setDisable(false);
            pendingFriendButton.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void CancelFriendRequest(MouseEvent event) {
        try {
            Client.Unfriend(currentUser, profileUser);
            pendingFriendButton.setDisable(true);
            pendingFriendButton.setVisible(false);
            friendButtonAnchor.setDisable(false);
            friendButtonAnchor.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void Unfriend(MouseEvent event) {
        try {
            Client.Unfriend(currentUser, profileUser);
            unfriendButtonAnchor.setDisable(true);
            unfriendButtonAnchor.setVisible(false);
            friendButtonAnchor.setDisable(false);
            friendButtonAnchor.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void Block(MouseEvent event) {
        System.out.println("Block Button Pressed");
        event.consume();
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
        //TODO
    }

    @FXML
    void CreatePost(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        System.out.println("Create Post Button Pressed");
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(currentUser, null, null);

            // Get the current stage
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
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
        System.out.println("Create Subcreddit Button Pressed");
        Clean();
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
        event.consume();
    }

    @FXML
    void FilterComments(MouseEvent event) {
        Clean();
        //TODO this.filterPosts = false
        System.out.println("Filter Comments pressed");
        event.consume();
    }

    @FXML
    void FilterPosts(MouseEvent event) {
        this.filterPosts = true;
        Refresh();
        event.consume();
    }

    @FXML
    void GoHome(MouseEvent event) {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, "", 0);

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
    void Login() {
        navigateToLoginDialog();
    }

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
            showAlert("Navigation Error", "Unable to load login page: " + e.getMessage());
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
    void Report(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        try {
            if(Client.ReportExists(new Report(0, this.currentUser, this.profileUser, null, null, null, null))) {
                showAlert("Duplicate Report", "You have already submitted a report on this target!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-report-page.fxml"));
            Parent root = loader.load();
            CreateReportPageController reportPageController = loader.getController();
            Stage popup = new Stage();
            popup.setTitle("Report Page");
            popup.setScene(new Scene(root));
            popup.initModality(Modality.APPLICATION_MODAL);
            reportPageController.initData(this.currentUser, profileUser, popup);
            popup.showAndWait();
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

    @FXML
    void Share(MouseEvent event) {
        System.out.println("Share User Pressed");
        event.consume();
    }

    @FXML
    void Refresh() {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-page.fxml"));
            Parent root = loader.load();

            UserProfilePageController userProfilePageController = loader.getController();
            userProfilePageController.InitData(profileUser.getId(), currentUser, searchField.getText(), filterPosts);

            // Get the current stage
            Stage stage = (Stage) filterPostsButton.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
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
        Client.THREAD_POOL.submit(() -> {
            if (postPreviewControllers != null) {
                for(PostPreviewTemplateController controller : postPreviewControllers) {
                    if(controller != null && controller.mediaViewController != null) {
                        controller.mediaViewController.Clean();
                    }
                }
            }
        });
    }
}
