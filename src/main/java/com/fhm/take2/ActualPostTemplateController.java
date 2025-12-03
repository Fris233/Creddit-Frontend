package com.fhm.take2;

import com.Client;
import com.crdt.Post;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class ActualPostTemplateController {

    @FXML private Button JoinButton;
    @FXML private Button MoreOptionsButton;
    @FXML private ImageView downvoteButton;
    @FXML private Label modLabel;
    @FXML private StackPane mediaAnchor;
    @FXML private AnchorPane voteAnchor;
    @FXML private Label subName;
    @FXML private ImageView subPFP;
    @FXML private Label timeLabel;
    @FXML private Label titleLabel;
    @FXML private ImageView upvoteButton;
    @FXML private Label votesLabel;
    @FXML private Label commentsLabel;
    @FXML private ImageView shareButton;
    @FXML private Label posterName;
    @FXML private Label postDesc;

    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private VBox postsContainer;
    @FXML private VBox recentPostsContainer;
    @FXML private ScrollPane recentScrollPane; //TODO SEX
    @FXML private ScrollPane postsScrollPane;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;

    private Post post;
    private User currentUser;
    private int myOGVote;
    private int myVote;

    MediaViewController mediaViewController;

    public void InitData(Post post, User user, int userVote) {
        this.post = post;
        this.currentUser = user;
        this.mediaViewController = null;
        myOGVote = 0;
        updateLoginUI();
        if(user != null)
            myOGVote = userVote;
        myVote = myOGVote;
        ColorVote();
        subName.setText(post.GetSubcreddit() != null? "cr/" + post.GetSubcreddit().GetSubName() : ""); //TODO: CHANGE TERTIARY OPERATOR
        timeLabel.setText(timeAgo(post.GetTimeCreated()));
        titleLabel.setText(post.GetTitle());
        votesLabel.setText(String.valueOf(post.GetVotes()));
        commentsLabel.setText(String.valueOf(post.GetComments()));
       /* if(post.GetContent() == null)
            postDesc.setText("sex"); //TODO sex
        else*/
            postDesc.setText(post.GetContent());
        posterName.setText("u/" + post.GetAuthor().getUsername());
        if(post.GetMedia() != null && !post.GetMedia().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("media-view.fxml"));
                Node mediaNode = loader.load();

                mediaViewController = loader.getController();
                mediaViewController.init(post.GetMedia(), false, null);

                mediaAnchor.getChildren().add(mediaNode);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
        if(post.GetSubcreddit() == null) {
            JoinButton.setDisable(true);
            JoinButton.setVisible(false);
        }
        postsScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            postsScrollPane.setVvalue(postsScrollPane.getVvalue() - delta / postsScrollPane.getContent().getBoundsInLocal().getHeight());
        });
    }

    private static String timeAgo(Timestamp timestamp) {
        Instant now = Instant.now();
        Instant created = timestamp.toInstant();
        Duration duration = Duration.between(created, now);
        long seconds = duration.getSeconds();

        if(seconds < 60)
            return seconds + (seconds == 1? " second ago" : " seconds ago");

        long minutes = seconds / 60;
        if(minutes < 60)
            return minutes + (minutes == 1? " minute ago" : " minutes ago");

        long hours = minutes / 60;
        if(hours < 24)
            return hours + (hours == 1? " hour ago" : " hours ago");

        long days = hours / 24;
        if(days < 30)
            return days + (days == 1? " day ago" : " days ago");

        long months = days / 30;
        if(months < 12)
            return months + (months == 1? " month ago" : " months ago");

        long years = months / 12;
        return years + (years == 1? " year ago" : " years ago");
    }

    private void ColorVote() {
        switch (myVote) {
            case 1:
                voteAnchor.setStyle("-fx-background-color: #d93900; -fx-background-radius: 25;");
                upvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow_fill.png"))));
                downvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                break;
            case -1:
                voteAnchor.setStyle("-fx-background-color: #6a5cff; -fx-background-radius: 25;");
                upvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                downvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow_fill.png"))));
                break;
            default:
                voteAnchor.setStyle("-fx-background-color: black; -fx-background-radius: 25;");
                upvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                downvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                break;
        }
    }

    @FXML
    void Upvote(MouseEvent event) {
        if(currentUser == null) {
            System.out.println("Not logged in!");
            event.consume();
            return;
        }
        System.out.println("Upvote Pressed!");
        try {
            myVote = myVote != 1? 1 : 0;
            if(Client.Vote(currentUser, post, myVote)) {
                votesLabel.setText(String.valueOf(post.GetVotes() + (myVote - myOGVote)));
                ColorVote();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }
    @FXML
    void Downvote(MouseEvent event) {
        if(currentUser == null) {
            System.out.println("Not logged in!");
            event.consume();
            return;
        }
        try {
            myVote = myVote != -1? -1 : 0;
            if(Client.Vote(currentUser, post, myVote)) {
                votesLabel.setText(String.valueOf(post.GetVotes() + (myVote - myOGVote)));
                ColorVote();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Downvote Pressed!");
        event.consume();
    }

    @FXML
    void JoinSubcreddit(MouseEvent event) {
        if(currentUser == null) {
            System.out.println("Not logged in!");
            event.consume();
            return;
        }
        System.out.println("Join Subcreddit Pressed!");
        event.consume();
    }

    @FXML
    void MoreOptionsButton(MouseEvent event) {
        System.out.println("More Options Pressed!");
        event.consume();
    }

    @FXML
    void OpenPost(MouseEvent event) {
        System.out.println("Open Post Pressed!");
        //TODO: DO NOT FORGET TO UNCOMMENT THIS!!
        /*if(mediaViewController != null)
            mediaViewController.Clean();*/
        event.consume();
    }

    @FXML
    void Share(MouseEvent event) {
        //TODO: share?
        System.out.println("Share clicked");
    }

    @FXML
    void OpenPoster(MouseEvent event) {
        //TODO: Click on Poster username
        System.out.println("Share clicked");
    }

    @FXML
    void OpenSubcreddit(MouseEvent event) {
        if(mediaViewController != null)
            mediaViewController.Clean();
        if(post.GetSubcreddit() == null) {
            System.out.println("Open user profile");
        }
        else {
            System.out.println("Open Subcreddit Pressed!");
        }
        event.consume();
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
            recentScrollPane.setDisable(true);
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
            Login();
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
            Login();
            return;
        }
        System.out.println("Create Subcreddit Button Pressed");
        Clean();
        event.consume();
    }

    @FXML
    void Login() {
        System.out.println("Login Button Pressed");
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
            Login();
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
        if(mediaViewController != null) {
           mediaViewController.Clean();
        }
    }
}

