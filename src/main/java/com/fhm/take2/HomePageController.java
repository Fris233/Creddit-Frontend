package com.fhm.take2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomePageController {

    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private VBox postsContainer;
    @FXML private VBox recentPostsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private ScrollPane scrollPane1;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;

    @FXML
    void Chat(MouseEvent event) {
        System.out.println("Chat Button Pressed");
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
    void CreatePost(MouseEvent event) {
        System.out.println("Create Post Button Pressed");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            //CreatePostPageController createPostPageController = loader.getController();
            //createPostPageController.InitData(currentUser);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        System.out.println("Create Subcreddit Button Pressed");
        event.consume();
    }

    @FXML
    void Login(MouseEvent event) {
        System.out.println("Login Button Pressed");
        loggedOutPane.setDisable(true);
        loggedOutPane.setVisible(false);
        loggedInPane.setDisable(false);
        loggedInPane.setVisible(true);
        event.consume();
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        System.out.println("Profile Button Pressed");
        event.consume();
    }

    @FXML
    void Refresh(MouseEvent event) {
        System.out.println("Dashboard Button Pressed");
        event.consume();
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        System.out.println("Search Pressed");
        event.consume();
    }

}
