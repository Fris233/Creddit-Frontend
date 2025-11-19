package com.fhm.take2;

import com.Client;
import com.crdt.Media;
import com.crdt.MediaType;
import com.crdt.Post;
import com.crdt.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

public class CreatePostPageController {

    @FXML private Button CreateSubcredditButton;
    @FXML private Button HomeButton;
    @FXML private Button RulesButton;
    @FXML private TextArea contentArea;
    @FXML private AnchorPane loggedInPane;
    @FXML private Button postButton;
    @FXML private VBox createInfoContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<?> subcredditComboBox;
    @FXML private Label timeLabel;
    @FXML private TextField titleField;
    @FXML private ImageView userPFP;

    private ArrayList<File> selectedFiles;
    private User currentUser;
    private BooleanProperty validPostInfo = new SimpleBooleanProperty(false);

    public void InitData(User user) {
        selectedFiles = new ArrayList<>();
        currentUser = user;

        contentArea.setWrapText(true);

        int[] lastLineCount = { 1 };

        contentArea.setPrefHeight(contentArea.getMinHeight());

        contentArea.textProperty().addListener((obs, oldText, newText) -> {
            int lines = newText.split("\n", -1).length;

            if (lines != lastLineCount[0]) {
                lastLineCount[0] = lines;

                contentArea.applyCss();
                contentArea.layout();

                var content = contentArea.lookup(".content");
                if (content != null) {
                    double height = 52 + (32 * (Math.min(lines, 14)));
                    contentArea.setPrefHeight(height);
                }
            }
        });

        titleField.textProperty().addListener((obs, oldText, newText) -> {
            if(newText.isEmpty())
                validPostInfo.set(false);
            else
                validPostInfo.set(true);
        });

        validPostInfo.addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                postButton.setStyle("-fx-background-color: #115bca; -fx-text-fill: #ffffff; -fx-background-radius: 30;"); //button blue and pressable
            }
            else {
                postButton.setStyle("-fx-background-color: #191c1e; -fx-text-fill: #525454; -fx-background-radius: 30;"); //button grayed out
            }
        });
    }

    @FXML
    void Chat(MouseEvent event) {
        System.out.println("Chat Button Pressed!");
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed!");
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        System.out.println("Create Subcreddit Button Pressed!");
        event.consume();
    }

    @FXML
    void GoHome(MouseEvent event) {
        System.out.println("Dashboard Pressed!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)createInfoContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        System.out.println("Profile Pressed!");
        event.consume();
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        System.out.println("Search Pressed!");
        event.consume();
    }

    @FXML
    void SendPost(MouseEvent event) {
        if(!validPostInfo.get()) return;

        System.out.println("Post Button Pressed!");

        if(!Client.isServerReachable()) {
            new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
            return;
        }

        try {
            String title = titleField.getText();
            String content = contentArea.getText();

            String mediaUrl = null;
            String mediaType = null;

            // If file selected, upload first
            for(File selectedFile : selectedFiles) {
                System.out.println("Uploading file: " + selectedFile.getName());
                String uploadResponse = uploadFile(selectedFile);
                if(uploadResponse == null) {
                    new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
                    //UpdateBaseURL();
                    return;
                }
                Map<?, ?> json = Client.GetResponse(uploadResponse);
                mediaUrl = (String) json.get("url");

                // Detect media type
                String mime = Files.probeContentType(selectedFile.toPath());
                if (mime != null) {
                    if (mime.startsWith("image/")) mediaType = "image";
                    else if (mime.startsWith("video/")) mediaType = "video";
                    else if (mime.startsWith("audio/")) mediaType = "audio";
                    else if (mime.equals("application/pdf")) mediaType = "pdf";
                }
            }

            // Now send post JSON
            Post post = new Post(1, Client.GetUser(1), null, title, content, null, null, null, null, 0, 0);
            String jsonBody = Client.GetJSON(post);

            URL url = new URL(System.getenv("Base_URL") + "/post/create");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            if (conn.getResponseCode() == 200) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Post uploaded successfully!");
                alert.showAndWait();
                titleField.clear();
                contentArea.clear();
                selectedFiles = new ArrayList<>();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to send post!");
                alert.showAndWait();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }

        event.consume();
    }

    private String uploadFile(File file) throws Exception {
        if(!Client.isServerReachable())
            return null;
        String boundary = "----Boundary" + System.currentTimeMillis();
        URL url = new URL(System.getenv("Base_URL") + "/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            out.writeBytes("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n");
            Files.copy(file.toPath(), out);
            out.writeBytes("\r\n--" + boundary + "--\r\n");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return response.toString();
    }

}
