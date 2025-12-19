package com.fhm.take2;

import com.crdt.Report;
import com.crdt.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ReportPageController {

    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private VBox reportContainer;
    @FXML private VBox recentPostsContainer;
    @FXML private ScrollPane recentScrollPane;
    @FXML private ScrollPane reportScrollPane;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;

    private User currentUser;
    private ArrayList<ReportPreviewTemplateController> reportPreviewControllers;
    private ArrayList<Report> reportsarr;
    private boolean updating = false;
    private boolean scrollCooldown = false;
    private int filter; // 0 for posts, 1 for subcreddits, 2 for comments, 3 for users

    public void InitData(User user, ArrayList<Report> reportsarr) {
        currentUser = user;
        reportPreviewControllers = new ArrayList<>();
        this.reportsarr = reportsarr;

        try {
            for (Report report : reportsarr) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Report_Preview_Template.fxml"));
                Node postNode = loader.load();

                ReportPreviewTemplateController controller = loader.getController();
                controller.init(report, user);

                reportContainer.getChildren().add(postNode);
                reportPreviewControllers.add(controller);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Setup scroll behavior
        reportScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            reportScrollPane.setVvalue(reportScrollPane.getVvalue() - delta / reportScrollPane.getContent().getBoundsInLocal().getHeight());
        });

        reportScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if(!updating && !scrollCooldown && newVal.doubleValue() >= reportScrollPane.getVmax()) {
                updating = true;
                scrollCooldown = true;
                try {
                    for (Report report : reportsarr) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Report_Preview_Template.fxml"));
                        Node postNode = loader.load();
                        ReportPreviewTemplateController controller = loader.getController();
                        controller.init(report, user);
                        reportContainer.getChildren().add(postNode);
                        reportPreviewControllers.add(controller);
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
        reportScrollPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) event.consume();
            if (event.getCode() == KeyCode.TAB) event.consume();
        });
    }

    @FXML
    void Chat(MouseEvent event) {
        if (currentUser == null) {
            return;
        }
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("message.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Chats");
            stage.setScene(new Scene(root, 800, 600));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.initOwner(reportContainer.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed");
        event.consume();
    }

    @FXML
    void ClearRecentPosts(MouseEvent event) {
        System.out.println("Clear Recent Posts Button Pressed");
        event.consume();
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        if (currentUser == null) {
            return;
        }
        System.out.println("My Profile Button Pressed");
        //Clean();
        event.consume();
    }
/*
    @FXML
    void Refresh() {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, searchField.getText(), filter);

            // Get the current stage
            Stage stage = (Stage) reportContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @FXML
    void GoHome(MouseEvent event) {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, null, 0);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage) reportContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert to match our dark theme
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    private void Clean() { //TODO: what does this do?
        if (reportPreviewControllers != null) {
            for(ReportPreviewTemplateController controller : reportPreviewControllers) {
                if(controller != null && controller.mediaViewController != null) {
                    controller.mediaViewController.Clean();
                }
            }
        }
    }
}