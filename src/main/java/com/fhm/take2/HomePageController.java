package com.fhm.take2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePageController {

    @FXML
    private Button CreateSubcredditButton;

    @FXML
    private Button CredditButton;

    @FXML
    private Button HomeButton;

    @FXML
    private Button RecentButton;

    @FXML
    private Button RulesButton;

    @FXML
    private Button MessagesButton;


    @FXML
    private void openMessagingWindow() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("message-window"));
            Stage stage = new Stage();
            stage.setTitle("Message Window");
            stage.setScene(new Scene(root, 800, 600));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
