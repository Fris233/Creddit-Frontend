package com.fhm.take2;

import com.crdt.Comment;
import com.crdt.Post;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class FilterCommentTemplateController {

    @FXML private Button MoreOptionsButton;
    @FXML private VBox anchor;
    @FXML private Label contentLabel;
    @FXML private ImageView downvoteButton;
    @FXML private StackPane mediaAnchor;
    @FXML private Label modLabel;
    @FXML private Label replyCountLabel;
    @FXML private Label timeLabel;
    @FXML private ImageView upvoteButton;
    @FXML private ImageView userPFP;
    @FXML private Label usernameLabel;
    @FXML private AnchorPane voteAnchor;
    @FXML private Label votesLabel;

    private Comment currentComment;
    private Post currentPost;

    public void initData(Comment comment){
        this.currentComment = comment;
        this.currentPost = comment.getPost();

        contentLabel.setText(currentComment.getContent());
        timeLabel.setText(currentComment.getTimeCreated().toString());
        votesLabel.setText(String.valueOf(currentComment.getVotes()));
        replyCountLabel.setText(String.valueOf(currentComment.getReplyCount()));
    }

    @FXML
    public void onCommentPressed(MouseEvent event) {

    }

    public int getId(){return currentComment.getID();}

}
