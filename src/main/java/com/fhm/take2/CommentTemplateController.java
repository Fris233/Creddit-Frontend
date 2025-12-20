package com.fhm.take2;

import com.Client;
import com.crdt.Comment;
import com.crdt.Report;
import com.crdt.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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
    @FXML private VBox anchor;
    @FXML private Label votesLabel;
    @FXML private VBox moreOptionsVBox;
    @FXML private Button reportButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

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
    private int root;
    private boolean moreOptioning = false;

    public void Init(Comment newComment, User user, int userVote, int replyLevel, ActualPostTemplateController parent, int root) {
        this.comment = newComment;
        this.currentUser = user;
        this.parentPage = parent;
        this.level = replyLevel;
        this.mediaViewController = null;
        this.root = root;
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
                mediaViewController.init(new ArrayList<>(Arrays.asList(comment.getMedia())), 0, null);

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
                    this.reply = null;
                    this.addedReply.set(false);
                    this.parentPage.Refresh();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        moreOptionsVBox.setVisible(false);
        if(this.currentUser == null) {
            moreOptionsVBox.setDisable(true);
        }
        else {
            if (!this.currentUser.equals(this.comment.getAuthor())) {
                moreOptionsVBox.getChildren().remove(editButton);
                moreOptionsVBox.getChildren().remove(deleteButton);
            } else {
                moreOptionsVBox.getChildren().remove(reportButton);
            }
        }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ActualPost_Template.fxml"));
            Parent root = loader.load();

            ActualPostTemplateController actualPostTemplateController = loader.getController();
            actualPostTemplateController.InitData(comment.getPost().GetID(), comment.getID(), this.currentUser);

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

                addCommentPaneController.cancelButton.setOnMouseClicked((mouseEvent) -> {
                    this.parentPage.postsContainer.getChildren().remove(ind + 3 + (level == 0? 0 : root));
                    addCommentPaneController.Clean();
                    addCommentPaneController = null;
                    replying = false;
                });

                ind = this.parentPage.parentCommentControllers.indexOf(this);
                if(ind < 0)
                    ind = this.parentPage.replyControllers.indexOf(this) + 1;
                this.parentPage.postsContainer.getChildren().add(ind + 3 + (level == 0? 0 : root), node);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            this.parentPage.postsContainer.getChildren().remove(ind + 3 + (level == 0? 0 : root));
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
    void Report(MouseEvent event) {
        try {
            if(Client.ReportExists(new Report(0, this.currentUser, this.comment, null, null, null, null))) {
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
            reportPageController.initData(this.currentUser, this.comment, popup);
            popup.showAndWait();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void Edit(MouseEvent event) {
        if(!replying) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("add-comment-pane.fxml"));
                Node node = loader.load();

                addCommentPaneController = loader.getController();
                addCommentPaneController.editing = true;
                addCommentPaneController.Init(currentUser, this.comment, this);
                addCommentPaneController.commentButton.setText("Apply");

                addCommentPaneController.cancelButton.setOnMouseClicked((mouseEvent) -> {
                    this.parentPage.postsContainer.getChildren().remove(ind + 3 + (level == 0? 0 : root));
                    addCommentPaneController.Clean();
                    addCommentPaneController = null;
                    replying = false;
                });

                ind = this.parentPage.parentCommentControllers.indexOf(this);
                if(ind < 0)
                    ind = this.parentPage.replyControllers.indexOf(this) + 1;
                this.parentPage.postsContainer.getChildren().add(ind + 3 + (level == 0? 0 : root), node);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            this.parentPage.postsContainer.getChildren().remove(ind + 3 + (level == 0? 0 : root));
            addCommentPaneController.Clean();
            addCommentPaneController = null;
        }
        replying = !replying;
        event.consume();
    }

    @FXML
    void Delete(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Action");
        alert.setHeaderText("Delete comment?");
        alert.setContentText("Are you sure you want to delete this comment?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Client.DeleteComment(this.comment);
                this.parentPage.Refresh();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MoreOptionsButton();
        }
        event.consume();
    }

    @FXML
    void MoreOptionsButton() {
        moreOptioning = !moreOptioning;
        moreOptionsVBox.setVisible(moreOptioning);
    }

    private void GoHome() {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, "", 0);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)contentLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void Clean() {
        if(mediaViewController != null) {
            mediaViewController.Clean();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert to match our dark theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    int GetCommentID() {return this.comment.getID();}
}
