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
        if(!Client.isServerReachable()) {
            System.out.println("Server Unreachable!");
            //page = ""; // should have a no connection and retry page
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(page));
        Scene scene = new Scene(fxmlLoader.load(), 1600, 900);
        HomePageController homePageController = fxmlLoader.getController();
        homePageController.InitData(null);
        stage.setTitle("CREDDIT");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}