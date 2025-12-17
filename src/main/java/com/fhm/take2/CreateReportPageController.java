package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateReportPageController {

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
    Stage reportStage;

    public void initData(User reporter, Reportable target,  Stage reportStage) {
        this.reporter = reporter;
        this.target = target;
        this.reportStage = reportStage;

        reasonTextField.setVisible(false);
        submitButton.setDisable(true);
    }

    @FXML
    public void onSpamPressed(MouseEvent event) throws IOException {
        spamButton.setStyle(spamButton.getStyle() + "; -fx-background-color:  #48494b");
        harasmentButton.setStyle(harasmentButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        hateButton.setStyle(hateButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        misinfoButton.setStyle(misinfoButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        nsfwButton.setStyle(nsfwButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        otherButton.setStyle(otherButton.getStyle() + "; -fx-background-color: #aaa7ad");
        submitButton.setStyle(submitButton.getStyle() + "; -fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);
        reasonTextField.setText("");
        submitButton.setDisable(false);

        reportType = ReportType.SPAM;
    }

    @FXML
    public void onHarassmentPressed(MouseEvent event) throws IOException {
        spamButton.setStyle(spamButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        harasmentButton.setStyle(harasmentButton.getStyle() + "; -fx-background-color:   #48494b");
        hateButton.setStyle(hateButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        misinfoButton.setStyle(misinfoButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        nsfwButton.setStyle(nsfwButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        otherButton.setStyle(otherButton.getStyle() + "; -fx-background-color: #aaa7ad");
        submitButton.setStyle(submitButton.getStyle() + "; -fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);
        reasonTextField.setText("");
        submitButton.setDisable(false);

        reportType = ReportType.HARASSMENT;
    }

    @FXML
    public void onHatePressed(MouseEvent event) throws IOException {
        spamButton.setStyle(spamButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        harasmentButton.setStyle(harasmentButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        hateButton.setStyle(hateButton.getStyle() + "; -fx-background-color:   #48494b");
        misinfoButton.setStyle(misinfoButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        nsfwButton.setStyle(nsfwButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        otherButton.setStyle(otherButton.getStyle() + "; -fx-background-color: #aaa7ad");
        submitButton.setStyle(submitButton.getStyle() + "; -fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);
        reasonTextField.setText("");
        submitButton.setDisable(false);

        reportType = ReportType.HATE_SPEECH;
    }

    @FXML
    public void onMisInfoPressed(MouseEvent event) throws IOException {
        spamButton.setStyle(spamButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        harasmentButton.setStyle(harasmentButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        hateButton.setStyle(hateButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        misinfoButton.setStyle(misinfoButton.getStyle() + "; -fx-background-color:  #48494b");
        nsfwButton.setStyle(nsfwButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        otherButton.setStyle(otherButton.getStyle() + "; -fx-background-color: #aaa7ad");
        submitButton.setStyle(submitButton.getStyle() + "; -fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);
        reasonTextField.setText("");
        submitButton.setDisable(false);

        reportType = ReportType.MISINFORMATION;
    }

    @FXML
    public void onNSFWPressed(MouseEvent event) throws IOException {
        spamButton.setStyle(spamButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        harasmentButton.setStyle(harasmentButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        hateButton.setStyle(hateButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        misinfoButton.setStyle(misinfoButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        nsfwButton.setStyle(nsfwButton.getStyle() + "; -fx-background-color:  #48494b");
        otherButton.setStyle(otherButton.getStyle() + "; -fx-background-color: #aaa7ad");
        submitButton.setStyle(submitButton.getStyle() + "; -fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(false);
        reasonTextField.setText("");
        submitButton.setDisable(false);

        reportType = ReportType.NSFW;
    }

    @FXML
    public void OtherPressed(MouseEvent event) throws IOException {
        spamButton.setStyle(spamButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        harasmentButton.setStyle(harasmentButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        hateButton.setStyle(hateButton.getStyle() + "; -fx-background-color:   #aaa7ad");
        misinfoButton.setStyle(misinfoButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        nsfwButton.setStyle(nsfwButton.getStyle() + "; -fx-background-color:  #aaa7ad");
        otherButton.setStyle(otherButton.getStyle() + "; -fx-background-color: #48494b");
        submitButton.setStyle(submitButton.getStyle() + "; -fx-background-color:  #1338be; -fx-text-fill: white;");
        reasonTextField.setVisible(true);
        submitButton.setDisable(false);

        reportType = ReportType.OTHER;
    }

    @FXML
    public void onSubmitPressed(MouseEvent event) throws Exception {
        if(Client.submitReport(new Report(5, reporter, target, reasonTextField.getText(), reportType, ReportStatus.PENDING, null))){
            showAlert("Report Submitted", "Report Submitted Successfully");
            reportStage.close();
        }
        else
            showAlert("Report Not Submitted", "Report Failed to Submit");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        ButtonType okButton = alert.getButtonTypes().get(0);
        Node okButtonNode = dialogPane.lookupButton(okButton);
        okButtonNode.setStyle("-fx-background-color: #0E1113; -fx-text-fill: white; -fx-background-radius: 10;");

        alert.showAndWait();
    }

    @FXML
    public void onReturnPressed(MouseEvent event) throws IOException {
        reportStage.close();
    }

    @FXML
    public void onRulesPressed(MouseEvent event) throws IOException {
        //todo
    }
}
