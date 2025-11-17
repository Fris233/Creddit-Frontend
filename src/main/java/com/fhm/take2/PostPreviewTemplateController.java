package com.fhm.take2;

import com.crdt.Post;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class PostPreviewTemplateController {

    @FXML private Button JoinButton;
    @FXML private Button MoreOptionsButton;
    @FXML private ImageView downvoteButton;
    @FXML private Label modLabel;
    @FXML private ImageView previewMedia;
    @FXML private Label subName;
    @FXML private ImageView subPFP;
    @FXML private Label timeLabel;
    @FXML private Label titleLabel;
    @FXML private ImageView upvoteButton;
    @FXML private Label votesLabel;
    @FXML private Label commentsLabel;

    public void init(Post post) {
        subName.setText(post.GetSubcreddit() != null? "cr/" + post.GetSubcreddit().GetSubName() : "u/" + post.GetAuthor().getUsername());
        //TODO: TO BE CONTINUED
    }

    @FXML
    void Upvote(MouseEvent event) {
        System.out.println("Upvote Pressed!");
        event.consume();
    }
    @FXML
    void Downvote(MouseEvent event) {
        System.out.println("Downvote Pressed!");
        event.consume();
    }

    @FXML
    void JoinSubcreddit(MouseEvent event) {
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
        event.consume();
    }

    @FXML
    void OpenSubcreddit(MouseEvent event) {
        System.out.println("Open Subcreddit Pressed!");
        event.consume();
    }

}

