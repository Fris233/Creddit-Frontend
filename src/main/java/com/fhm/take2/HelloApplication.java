package com.fhm.take2;

import com.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        String page = "home-page.fxml";
        boolean reachable = true;
        if(!Client.isServerReachable()) {
            page = "error_404.fxml";
            reachable = false;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(page));
        Scene scene = new Scene(fxmlLoader.load(), 1600, 900);
        if(reachable) {
            HomePageController homePageController = fxmlLoader.getController();
            homePageController.InitData(null, null);
        }
        else {
            Error404Controller error404Controller = fxmlLoader.getController();
            error404Controller.refreshButton.setOnAction(e -> {
                if(!Client.isServerReachable()) {
                    return;
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                HomePageController homePageController = loader.getController();
                homePageController.InitData(null, null);

                // Create the second scene
                Scene scene2 = new Scene(root);
                // Get the current stage
                Stage stage2 = (Stage)error404Controller.refreshButton.getScene().getWindow();
                // Set the new scene
                stage2.setScene(scene2);
            });
        }
        stage.setTitle("CREDDIT");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}