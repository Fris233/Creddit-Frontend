package com.fhm.take2;

import com.Client;
import com.crdt.Post;
import com.crdt.Subcreddit;
import com.crdt.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

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
    @FXML private ImageView userPFP;
    @FXML private Label usernameLabel;
    @FXML private Label usernameLabel1;

    private User currentUser, profileUser;
    private ArrayList<Subcreddit> profileSubs;
    private ArrayList<PostPreviewTemplateController> postPreviewControllers;
    private boolean updating = false;
    private boolean scrollCooldown = false;

    public void InitData(User profileUser, User currentUser, String searchPrompt, boolean filterPosts) {
        this.currentUser = currentUser;
        this.profileUser = profileUser;
        this.searchField.setText(searchPrompt);
        postPreviewControllers = new ArrayList<>();

        updateLoginUI();

        usernameLabel.setText(this.profileUser.getUsername());
        usernameLabel1.setText("u/" + this.profileUser.getUsername());
        registerDateLabel.setText(this.profileUser.getLastSeen().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if(this.profileUser.getLastSeen().toInstant().isAfter(Instant.now().minusSeconds(60)))
            lastOnlineLabel.setText("now");
        else
            lastOnlineLabel.setText(PostPreviewTemplateController.timeAgo(this.profileUser.getLastSeen()));

        if(filterPosts)
            filterPostsButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else
            filterCommentsButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");

        try {
            profileSubs = Client.GetUserSubcreddits(this.profileUser);
            if(Client.GetFriends(this.currentUser).contains(this.profileUser)) {
                unfriendButtonAnchor.setDisable(false);
                unfriendButtonAnchor.setVisible(true);
            }
            else if(Client.GetReceivedFriendRequests(this.currentUser).contains(this.profileUser)) {
                friendRequestAnchor.setDisable(false);
                friendRequestAnchor.setVisible(true);
            }
            else if(Client.GetSentFriendRequests(this.currentUser).contains(this.profileUser)) {
                pendingFriendButton.setDisable(false);
                pendingFriendButton.setVisible(true);
            }
            else {
                friendButtonAnchor.setDisable(false);
                friendButtonAnchor.setVisible(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        subcredditCountLabel.setText(String.valueOf(profileSubs.size()));
        bioTextArea.setText(this.profileUser.getBio());

        try {
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

    }

    @FXML
    void Block(MouseEvent event) {

    }

    @FXML
    void CancelFriendRequest(MouseEvent event) {

    }

    @FXML
    void Chat(MouseEvent event) {

    }

    @FXML
    void CheckRules(MouseEvent event) {

    }

    @FXML
    void CreatePost(MouseEvent event) {

    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {

    }

    @FXML
    void FilterComments(MouseEvent event) {

    }

    @FXML
    void FilterPosts(MouseEvent event) {

    }

    @FXML
    void FriendRequest(MouseEvent event) {

    }

    @FXML
    void GoHome(MouseEvent event) {

    }

    @FXML
    void Login(MouseEvent event) {

    }

    @FXML
    void ProfilePressed(MouseEvent event) {

    }

    @FXML
    void RejectFriend(MouseEvent event) {

    }

    @FXML
    void Report(MouseEvent event) {

    }

    @FXML
    void SearchPressed(KeyEvent event) {

    }

    @FXML
    void Share(MouseEvent event) {

    }

    @FXML
    void Unfriend(MouseEvent event) {

    }

}
