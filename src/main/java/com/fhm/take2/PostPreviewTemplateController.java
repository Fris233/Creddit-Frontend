package com.fhm.take2;

import com.crdt.Post;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class PostPreviewTemplateController {

    @FXML private Button JoinButton;
    @FXML private Button MoreOptionsButton;
    @FXML private ImageView downvoteButton;
    @FXML private Label modLabel;
    @FXML private ImageView previewMedia;
    @FXML private AnchorPane mediaAnchor;
    @FXML private Label subName;
    @FXML private ImageView subPFP;
    @FXML private Label timeLabel;
    @FXML private Label titleLabel;
    @FXML private ImageView upvoteButton;
    @FXML private Label votesLabel;
    @FXML private Label commentsLabel;

    private Post post;

    public void init(Post post) {
        this.post = post;
        subName.setText(post.GetSubcreddit() != null? "cr/" + post.GetSubcreddit().GetSubName() : "u/" + post.GetAuthor().getUsername());
        timeLabel.setText(timeAgo(post.GetTimeCreated()));
        titleLabel.setText(post.GetTitle());
        votesLabel.setText(String.valueOf(post.GetVotes()));
        commentsLabel.setText(String.valueOf(post.GetComments()));
        if(post.GetMedia() == null || post.GetMedia().isEmpty()) {
            System.out.println("Deleting media anchor pane");
            ((Pane)mediaAnchor.getParent()).getChildren().remove(mediaAnchor);
        }
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
        if(post.GetSubcreddit() == null) {
            System.out.println("Open user profile");
        }
        else {
            System.out.println("Open Subcreddit Pressed!");
        }
        event.consume();
    }

}

