package com.fhm.take2;

import com.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Error404Controller {

    @FXML private Button refreshButton;

    @FXML
    void Refresh(MouseEvent event) {
        if(!Client.isServerReachable()) {
            event.consume();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(null, null);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)refreshButton.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

}