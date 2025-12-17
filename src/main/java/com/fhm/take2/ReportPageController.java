package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;

import java.io.IOException;
import java.sql.Timestamp;

public class ReportPageController {

    @FXML private Button hateButton;
    @FXML private Button harasmentButton;
    @FXML private Button misinfoButton;
    @FXML private Button nsfwButton;
    @FXML private Button otherButton;
    @FXML private TextArea reasonTextField;
    @FXML private ImageView returnIcon;
    @FXML private Label rulesButton;
    @FXML private Button spamButton;
    @FXML private Button submitButton;

    private User reporter;
    private Reportable target;
    ReportType reportType;
    String reason;

    public void initData(User reporter, Reportable target){
        this.reporter = reporter;
        this.target = target;

        reasonTextField.setVisible(false);
        submitButton.setDisable(true);
    }

    @FXML
    public void onSpamPressed(MouseEvent event) throws IOException {
        spamButton.setStyle("-fx-background-color:  #48494b");
        harasmentButton.setStyle("-fx-background-color:   #aaa7ad");
        hateButton.setStyle("-fx-background-color:   #aaa7ad");
        misinfoButton.setStyle("-fx-background-color:  #aaa7ad");
        nsfwButton.setStyle("-fx-background-color:  #aaa7ad");
        otherButton.setStyle("-fx-background-color: #aaa7ad");
        submitButton.setStyle("-fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);

        reportType = ReportType.SPAM;
    }

    @FXML
    public void onHarassmentPressed(MouseEvent event) throws IOException {
        spamButton.setStyle("-fx-background-color:  #aaa7ad");
        harasmentButton.setStyle("-fx-background-color:   #48494b");
        hateButton.setStyle("-fx-background-color:   #aaa7ad");
        misinfoButton.setStyle("-fx-background-color:  #aaa7ad");
        nsfwButton.setStyle("-fx-background-color:  #aaa7ad");
        otherButton.setStyle("-fx-background-color: #aaa7ad");
        submitButton.setStyle("-fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);

        reportType = ReportType.HARASSMENT;
    }

    @FXML
    public void onHatePressed(MouseEvent event) throws IOException {
        spamButton.setStyle("-fx-background-color:  #aaa7ad");
        harasmentButton.setStyle("-fx-background-color:   #aaa7ad");
        hateButton.setStyle("-fx-background-color:   #48494b");
        misinfoButton.setStyle("-fx-background-color:  #aaa7ad");
        nsfwButton.setStyle("-fx-background-color:  #aaa7ad");
        otherButton.setStyle("-fx-background-color: #aaa7ad");
        submitButton.setStyle("-fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);

        reportType = ReportType.HATE_SPEECH;
    }

    @FXML
    public void onMisInfoPressed(MouseEvent event) throws IOException {
        spamButton.setStyle("-fx-background-color:  #aaa7ad");
        harasmentButton.setStyle("-fx-background-color:   #aaa7ad");
        hateButton.setStyle("-fx-background-color:   #aaa7ad");
        misinfoButton.setStyle("-fx-background-color:  #48494b");
        nsfwButton.setStyle("-fx-background-color:  #aaa7ad");
        otherButton.setStyle("-fx-background-color: #aaa7ad");
        submitButton.setStyle("-fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);

        reportType = ReportType.MISINFORMATION;
    }

    @FXML
    public void onNSFWPressed(MouseEvent event) throws IOException {
        spamButton.setStyle("-fx-background-color:  #aaa7ad");
        harasmentButton.setStyle("-fx-background-color:   #aaa7ad");
        hateButton.setStyle("-fx-background-color:   #aaa7ad");
        misinfoButton.setStyle("-fx-background-color:  #aaa7ad");
        nsfwButton.setStyle("-fx-background-color:  #48494b");
        otherButton.setStyle("-fx-background-color: #aaa7ad");
        submitButton.setStyle("-fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);

        reportType = ReportType.NSFW;
    }

    @FXML
    public void OtherPressed(MouseEvent event) throws IOException {
        spamButton.setStyle("-fx-background-color:  #aaa7ad");
        harasmentButton.setStyle("-fx-background-color:   #aaa7ad");
        hateButton.setStyle("-fx-background-color:   #aaa7ad");
        misinfoButton.setStyle("-fx-background-color:  #aaa7ad");
        nsfwButton.setStyle("-fx-background-color:  #aaa7ad");
        otherButton.setStyle("-fx-background-color: #48494b");
        submitButton.setStyle("-fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(true);

        reportType = ReportType.OTHER;
    }

    @FXML
    public void onSubmitPressed(MouseEvent event) throws Exception {
        Client.submitReport(new Report(5, reporter, target, reasonTextField.toString(), reportType, ReportStatus.PENDING, null));
    }
}
