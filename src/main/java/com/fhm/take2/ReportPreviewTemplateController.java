package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class ReportPreviewTemplateController {

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
    @FXML private Label reporttype;
    @FXML private Label status;
    @FXML private Label target;

    private Report report;
    private User currentUser;

    MediaViewController mediaViewController;

    public void init(Report report, User user) {
        this.report = report;
        this.currentUser = user;
        this.mediaViewController = null;
        reporttype.setText(report.getType()==null ? "Other" : report.getType().toString());
        subName.setText(report.getReporter().getUsername());
        timeLabel.setText(timeAgo(report.getTimeReported()));
        titleLabel.setText(report.getReason());
        status.setText(report.getStatus().toString());
        if(report.getStatus() == ReportStatus.PENDING){
            status.setTextFill(Color.RED);
        }
        else if(report.getStatus() == ReportStatus.RESOLVED){
            status.setTextFill(Color.LIMEGREEN);
        }
        else if(report.getStatus()==ReportStatus.DISMISSED){
            status.setTextFill(Color.ORANGE);
        }

        if(report.getTarget() instanceof Post){
            target.setText("Post Title: " + ((Post) report.getTarget()).GetTitle());
        }
        else if(report.getTarget() instanceof Comment){
            target.setText("Comment Author: " + ((Comment) report.getTarget()).getAuthor().getUsername());
        }
        else if(report.getTarget() instanceof User){
            target.setText("Target User: " + ((User) report.getTarget()).getUsername());
        }
    }

    public static String timeAgo(Timestamp timestamp) {
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
    void MoreOptionsButton(MouseEvent event) {
        System.out.println("More Options Pressed!");
        event.consume();
    }

    @FXML
    void Resolve(MouseEvent event) throws Exception {
        System.out.println("Approve Pressed!");
        status.setTextFill(Color.LIMEGREEN);
        Client.ResolveReport(report);
    }

    @FXML
    void Dismiss(MouseEvent event) throws Exception {
        System.out.println("Dismiss Pressed!");
        status.setTextFill(Color.ORANGE);
        Client.DismissReport(report);
    }

    @FXML
    void OpenTarget(MouseEvent event) {
        if(report.getTarget() instanceof Post){
            target.setText("Post Title: " + ((Post) report.getTarget()).GetTitle());
            if(mediaViewController != null)
                mediaViewController.Clean();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ActualPost_Template.fxml"));
                Parent root = loader.load();

                ActualPostTemplateController actualPostTemplateController = loader.getController();
                actualPostTemplateController.InitData(((Post) report.getTarget()).GetID(), /*0,*/ currentUser);

                Stage stage = (Stage) JoinButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            event.consume();
        }
        else if(report.getTarget() instanceof Comment){
            if(mediaViewController != null)
                mediaViewController.Clean();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ActualPost_Template.fxml"));
                Parent root = loader.load();

                ActualPostTemplateController actualPostTemplateController = loader.getController();
                actualPostTemplateController.InitData(((Comment) report.getTarget()).getPost().GetID(), currentUser);

                Stage stage = (Stage) JoinButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            event.consume();
        }
        else if(report.getTarget() instanceof User){
            if(mediaViewController != null)
                mediaViewController.Clean();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-page.fxml"));
                Parent root = loader.load();

                UserProfilePageController userProfilePageController = loader.getController();
                userProfilePageController.InitData(((User) report.getTarget()).getId(), currentUser, "", true);

                // Get the current stage
                Stage stage = (Stage) target.getScene().getWindow();
                // Set the new scene
                stage.setScene(new Scene(root));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            event.consume();
        }
    }
    }

