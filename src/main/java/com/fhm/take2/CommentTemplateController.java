package com.fhm.take2;

import com.Client;
import com.crdt.Comment;
import com.crdt.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;

public class CommentTemplateController {

    @FXML private Button MoreOptionsButton;
    @FXML private Label contentLabel;
    @FXML private ImageView downvoteButton;
    @FXML private StackPane mediaAnchor;
    @FXML private Label modLabel;
    @FXML private Button replyButton;
    @FXML private Label replyCountLabel;
    @FXML private Label timeLabel;
    @FXML private ImageView upvoteButton;
    @FXML private ImageView userPFP;
    @FXML private Label usernameLabel;
    @FXML private AnchorPane voteAnchor;
    @FXML private AnchorPane anchor;
    @FXML private Label votesLabel;

    private User currentUser;
    private Comment comment;
    private int myOGVote;
    private int myVote;
    private boolean modAuthor = false;
    private boolean replying = false;

    BooleanProperty addedReply = new SimpleBooleanProperty(false);
    Comment reply;
    MediaViewController mediaViewController;
    private ActualPostTemplateController parentPage;
    private int level;
    private int ind;
    private AddCommentPaneController addCommentPaneController = null;

    public void Init(Comment newComment, User user, int userVote, int replyLevel, ActualPostTemplateController parent) {
        this.comment = newComment;
        this.currentUser = user;
        this.parentPage = parent;
        this.level = replyLevel;
        this.mediaViewController = null;
        myOGVote = 0;
        if(user != null)
            myOGVote = userVote;
        if(this.currentUser == null)
            replyButton.setDisable(true);
        myVote = myOGVote;
        ColorVote();
        this.anchor.setPadding(new Insets(0, 0, 5, level * 30));
        usernameLabel.setText("u/" + comment.getAuthor().getUsername());
        timeLabel.setText(PostPreviewTemplateController.timeAgo(comment.getTimeCreated()));
        contentLabel.setText(comment.getContent());
        votesLabel.setText(String.valueOf(comment.getVotes()));
        replyCountLabel.setText(String.valueOf(comment.getReplyCount()));
        if(comment.getMedia() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("media-view.fxml"));
                Node mediaNode = loader.load();

                mediaViewController = loader.getController();
                mediaViewController.init(new ArrayList<>(Arrays.asList(comment.getMedia())), false, null);

                mediaAnchor.getChildren().add(mediaNode);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        if(comment.getPost().GetSubcreddit() != null) {
            try {
                modAuthor = Client.VerifyModeration(comment.getAuthor(), comment.getPost().GetSubcreddit());
                if(modAuthor) {
                    modLabel.setVisible(true);
                }
                if(comment.getPost().GetAuthor().equals(comment.getAuthor())) {
                    modLabel.setText("AUTHOR");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        addedReply.addListener((obs, oldV, newV) -> {
            if(newV) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                    Node node = loader.load();

                    CommentTemplateController commentTemplateController = loader.getController();
                    commentTemplateController.Init(this.reply, currentUser, 0, this.level + 1, this.parentPage);

                    ind = this.parentPage.parentCommentControllers.indexOf(this);
                    if(ind < 0)
                        ind = this.parentPage.replyControllers.indexOf(this);
                    this.parentPage.replyControllers.add(ind + 1, commentTemplateController);
                    this.parentPage.postsContainer.getChildren().add(ind + 1, node);
                    this.reply = null;
                    this.addedReply.set(false);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
    void OpenComment(MouseEvent event) {
        System.out.println("Open Comment Pressed!");
        event.consume();
    }

    @FXML
    void OpenProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-page.fxml"));
            Parent root = loader.load();

            UserProfilePageController userProfilePageController = loader.getController();
            userProfilePageController.InitData(comment.getAuthor().getId(), currentUser, "", true);

            // Get the current stage
            Stage stage = (Stage) userPFP.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void Reply(MouseEvent event) {
        if(!replying) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("add-comment-pane.fxml"));
                Node node = loader.load();

                addCommentPaneController = loader.getController();
                addCommentPaneController.Init(currentUser, this.comment, this);

                ind = this.parentPage.parentCommentControllers.indexOf(this);
                if(ind < 0)
                    ind = this.parentPage.replyControllers.indexOf(this);
                this.parentPage.postsContainer.getChildren().add(ind + 1, node);
                System.out.println("Added small reply box after comment at index : " + ind);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            this.parentPage.postsContainer.getChildren().remove(ind + 1);
            addCommentPaneController.Clean();
            addCommentPaneController = null;
        }
        replying = !replying;
        event.consume();
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
            if(Client.Vote(currentUser, comment, myVote)) {
                votesLabel.setText(String.valueOf(comment.getVotes() + (myVote - myOGVote)));
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
            if(Client.Vote(currentUser, comment, myVote)) {
                votesLabel.setText(String.valueOf(comment.getVotes() + (myVote - myOGVote)));
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
    void MoreOptionsButton(MouseEvent event) {
        System.out.println("More Options Pressed!");
        event.consume();
    }

    public void Clean() {
        if(mediaViewController != null) {
            mediaViewController.Clean();
        }
    }

    int GetCommentID() {return this.comment.getID();}
}
