package com.fhm.take2;

import com.Client;
import com.crdt.Comment;
import com.crdt.Media;
import com.crdt.MediaType;
import com.crdt.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddCommentPaneController {

    @FXML private AnchorPane commentAnchor;
    @FXML private Button commentButton;
    @FXML private StackPane commentMediaPane;
    @FXML private TextArea commentTextArea;

    private MediaViewController commentMediaViewController;
    private BooleanProperty validCommentInfo = new SimpleBooleanProperty(false);

    private User currentUser;
    private Comment parentComment;
    private CommentTemplateController parentController;

    public void Init(User user, Comment comment, CommentTemplateController parent) {
        this.currentUser = user;
        this.parentComment = comment;
        this.parentController = parent;

        int[] lastLineCount = { 1 };
        commentTextArea.textProperty().addListener((obs, oldText, newText) -> {
            int lines = newText.split("\n", -1).length;

            if (lines != lastLineCount[0]) {
                lastLineCount[0] = lines;

                commentTextArea.applyCss();
                commentTextArea.layout();

                var content = commentTextArea.lookup(".content");
                if (content != null) {
                    double height = 47 + (32 * (Math.min(lines, 4)));
                    commentTextArea.setPrefHeight(height);
                }
            }
        });

        commentTextArea.textProperty().addListener((obs, oldText, newText) -> {
            validCommentInfo.set(!newText.isBlank());
        });

        validCommentInfo.addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                commentButton.setStyle("-fx-background-color: #115bca; -fx-text-fill: #ffffff; -fx-background-radius: 30;"); //button blue and pressable
                commentButton.setDisable(false);
            }
            else {
                commentButton.setStyle("-fx-background-color: #191c1e; -fx-text-fill: #525454; -fx-background-radius: 30;"); //button grayed out
                commentButton.setDisable(true);
            }
        });

        commentTextArea.setOnDragOver(event -> {
            if (event.getGestureSource() != commentTextArea && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // Handle dropping files
        commentTextArea.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                List<File> files = db.getFiles();
                files.forEach(this::AddFile);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    void AttachMedia(MouseEvent event) {
        Window window = ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(window);
        if(file == null)
            return;
        AddFile(file);
        event.consume();
    }

    private void AddFile(File file) {
        if(commentMediaViewController != null) {
            RemoveMediaPane();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("media-view.fxml"));
            Node mediaNode = loader.load();
            commentMediaViewController = loader.getController();
            commentMediaViewController.init(null, true, new ArrayList<>());
            commentMediaPane.getChildren().add(mediaNode);
            commentMediaViewController.done.addListener((obs, oldVal, newVal) -> {
                if (newVal)
                    RemoveMediaPane();
            });
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        commentMediaViewController.AddMedia(file);
    }

    private void RemoveMediaPane() {
        commentMediaPane.getChildren().clear();
        if(commentMediaViewController != null)
            commentMediaViewController.Clean();
        commentMediaViewController = null;
    }

    @FXML
    void SendComment(MouseEvent event) {
        if(!validCommentInfo.get()) return;

        if(!Client.isServerReachable()) {
            new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
            return;
        }

        try {
            String content = commentTextArea.getText();

            String mediaUrl = "";
            MediaType mediaType = MediaType.NONE;

            Media media = null;
            if(commentMediaViewController != null) {
                File selectedFile = commentMediaViewController.GetFileArrayList().getFirst();
                String uploadResponse = Client.UploadFile(selectedFile);
                if (uploadResponse.isBlank()) {
                    new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
                    return;
                }
                Map<?, ?> json = Client.GetResponse(uploadResponse);
                mediaUrl = (String) json.get("url");
                // Detect media type
                String mime = Files.probeContentType(selectedFile.toPath());
                if (mime != null) {
                    if (mime.startsWith("image/")) mediaType = MediaType.IMAGE;
                    else if (mime.startsWith("video/")) mediaType = MediaType.VIDEO;
                    else if (mime.startsWith("audio/")) mediaType = MediaType.AUDIO;
                    else mediaType = MediaType.OTHER;
                }
                media = new Media(mediaType, mediaUrl);
            }

            // Now send post JSON
            Comment comment = new Comment(0, this.parentComment.getPost(), this.currentUser, content, media, this.parentComment.getID(), 0, 0, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), false);
            int id = Client.CreateComment(comment);
            if (id > 0) {
                comment.setId(id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Comment uploaded successfully!");
                alert.showAndWait();
                if(commentMediaViewController != null) {
                    commentMediaViewController.Clean();
                    RemoveMediaPane();
                }
                parentController.reply = comment;
                parentController.addedReply.set(true);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to send post!");
                alert.showAndWait();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }

        event.consume();
    }

    private void Clean() {
        Client.THREAD_POOL.submit(() -> {
            if(commentMediaViewController != null)
                commentMediaViewController.Clean();
        });
    }
}
