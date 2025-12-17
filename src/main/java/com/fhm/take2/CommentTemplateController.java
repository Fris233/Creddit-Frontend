package com.fhm.take2;

import com.Client;
import com.crdt.Comment;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

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
    @FXML private Label votesLabel;

    private User currentUser;
    private Comment comment;
    private int myOGVote;
    private int myVote;
    private boolean modAuthor = false;

    MediaViewController mediaViewController;

    public void Init(Comment newComment, User user, int userVote) {
        this.comment = newComment;
        this.currentUser = user;
        this.mediaViewController = null;
        myOGVote = 0;
        if(user != null)
            myOGVote = userVote;
        myVote = myOGVote;
        ColorVote();
        usernameLabel.setText("u/" + comment.getAuthor().getUsername());
        timeLabel.setText(timeAgo(comment.getTimeCreated()));
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
    }

    @FXML
    void Downvote(MouseEvent event) {

    }

    @FXML
    void MoreOptionsButton(MouseEvent event) {

    }

    @FXML
    void OpenComment(MouseEvent event) {

    }

    @FXML
    void OpenPost(MouseEvent event) {

    }

    @FXML
    void OpenProfile(MouseEvent event) {

    }

    @FXML
    void Reply(MouseEvent event) {

    }

    @FXML
    void Upvote(MouseEvent event) {

    }

}
