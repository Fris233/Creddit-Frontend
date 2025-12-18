package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CreatePostPageController {

    @FXML private Button CreateSubcredditButton;
    @FXML private Button HomeButton;
    @FXML private Button RulesButton;
    @FXML private TextArea contentArea;
    @FXML private AnchorPane loggedInPane;
    @FXML private StackPane mediaPane;
    @FXML private Button postButton;
    @FXML private VBox createInfoContainer;

    @FXML private TextField categorySearchField;
    @FXML private ListView<String> lvSuggestions;
    @FXML private HBox hbSelectedCategories;

    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<Subcreddit> subcredditComboBox;
    @FXML private Label timeLabel;
    @FXML private TextField titleField;
    @FXML private ImageView userPFP;

    private MediaViewController mediaViewController;
    private User currentUser;
    private BooleanProperty validPostInfo = new SimpleBooleanProperty(false);

    private ArrayList<String> allCategories;
    private ArrayList<String> selectedCategories;

    private ArrayList<Subcreddit> mySubcreddits;

    private ObservableList<String> suggestions;

    public void InitData(User user) {
        this.currentUser = user;
        try {
            allCategories = new ArrayList<>(Arrays.asList(Client.GetAllCategories()));
            mySubcreddits = Client.GetUserSubcreddits(this.currentUser);
            mySubcreddits.addFirst(null);
            subcredditComboBox.setItems(FXCollections.observableArrayList(mySubcreddits));
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectedCategories = new ArrayList<>();
        mediaViewController = null;

        contentArea.setWrapText(true);

        suggestions = FXCollections.observableArrayList();
        lvSuggestions.setItems(suggestions);
        lvSuggestions.setVisible(false);
        lvSuggestions.setFocusTraversable(false);
        lvSuggestions.setFixedCellSize(24);
        categorySearchField.textProperty().addListener((obs, old, text) -> {
            UpdateSuggestions(text);
        });

        lvSuggestions.setOnMouseClicked(e -> {
            if(e.getClickCount() == 1)
                HandleSelect();
        });

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
            if(newText.length() > 255)
                titleField.setText(oldText);
            validPostInfo.set(!newText.isBlank());
        });

        categorySearchField.textProperty().addListener((obs, oldText, newText) -> {
            if(newText.length() > 50)
                categorySearchField.setText(oldText);
        });

        validPostInfo.addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                postButton.setStyle("-fx-background-color: #115bca; -fx-text-fill: #ffffff; -fx-background-radius: 30;"); //button blue and pressable
                postButton.setDisable(false);
            }
            else {
                postButton.setStyle("-fx-background-color: #191c1e; -fx-text-fill: #525454; -fx-background-radius: 30;"); //button grayed out
                postButton.setDisable(true);
            }
        });

        // Setup scroll behavior
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            scrollPane.setVvalue(scrollPane.getVvalue() - delta / scrollPane.getContent().getBoundsInLocal().getHeight());
        });

        scrollPane.setOnDragOver(event -> {
            if (event.getGestureSource() != scrollPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        scrollPane.setOnDragDropped(event -> {
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
    public void InitData(User user, Subcreddit subcreddit) {
        this.currentUser = user;

        try {
            allCategories = new ArrayList<>(Arrays.asList(Client.GetAllCategories()));
            mySubcreddits = Client.GetUserSubcreddits(this.currentUser);
            mySubcreddits.addFirst(null);
            subcredditComboBox.setItems(FXCollections.observableArrayList(mySubcreddits));

            // If a subcreddit is specified, pre-select it
            if (subcreddit != null) {
                for (int i = 0; i < mySubcreddits.size(); i++) {
                    if (mySubcreddits.get(i) != null &&
                            mySubcreddits.get(i).GetSubId() == subcreddit.GetSubId()) {
                        subcredditComboBox.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        selectedCategories = new ArrayList<>();
        mediaViewController = null;

        contentArea.setWrapText(true);

        suggestions = FXCollections.observableArrayList();
        lvSuggestions.setItems(suggestions);
        lvSuggestions.setVisible(false);
        lvSuggestions.setFocusTraversable(false);
        lvSuggestions.setFixedCellSize(24);
        categorySearchField.textProperty().addListener((obs, old, text) -> {
            UpdateSuggestions(text);
        });

        lvSuggestions.setOnMouseClicked(e -> {
            if(e.getClickCount() == 1)
                HandleSelect();
        });

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
            if(newText.length() > 255)
                titleField.setText(oldText);
            validPostInfo.set(!newText.isBlank());
        });

        categorySearchField.textProperty().addListener((obs, oldText, newText) -> {
            if(newText.length() > 50)
                categorySearchField.setText(oldText);
        });

        validPostInfo.addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                postButton.setStyle("-fx-background-color: #115bca; -fx-text-fill: #ffffff; -fx-background-radius: 30;");
                postButton.setDisable(false);
            }
            else {
                postButton.setStyle("-fx-background-color: #191c1e; -fx-text-fill: #525454; -fx-background-radius: 30;");
                postButton.setDisable(true);
            }
        });

        // Setup scroll behavior
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            scrollPane.setVvalue(scrollPane.getVvalue() - delta / scrollPane.getContent().getBoundsInLocal().getHeight());
        });

        scrollPane.setOnDragOver(event -> {
            if (event.getGestureSource() != scrollPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        scrollPane.setOnDragDropped(event -> {
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
    void Chat(MouseEvent event) {
        System.out.println("Chat Button Pressed!");
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed!");
        if(mediaViewController != null)
            mediaViewController.Clean();
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        System.out.println("Create Subcreddit Button Pressed!");
        if(mediaViewController != null)
            mediaViewController.Clean();
        event.consume();
    }

    @FXML
    void GoHome(MouseEvent event) {
        System.out.println("Dashboard Pressed!");
        if(mediaViewController != null)
            mediaViewController.Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, null, 0);

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
        if(mediaViewController != null)
            mediaViewController.Clean();
        event.consume();
    }

    private void HandleSelect() {
        String chosen;

        int sel = lvSuggestions.getSelectionModel().getSelectedIndex();

        if (sel != -1) {
            chosen = suggestions.get(sel);
        } else {
            if(!suggestions.isEmpty())
                chosen = suggestions.get(0);
            else
                chosen = categorySearchField.getText();
        }

        categorySearchField.setText(chosen);
        lvSuggestions.setVisible(false);
        categorySearchField.requestFocus();
        categorySearchField.positionCaret(categorySearchField.getLength());
    }

    private void UpdateSuggestions(String input) {
        if (input == null || input.trim().isEmpty()) {
            suggestions.clear();
            lvSuggestions.setVisible(false);
            return;
        }

        String lower = input.toLowerCase();

        suggestions.setAll(allCategories.stream().filter(c -> c.toLowerCase().contains(lower)).toList());

        lvSuggestions.getSelectionModel().clearSelection();

        if (suggestions.isEmpty()) {
            lvSuggestions.setVisible(false);
        } else {
            lvSuggestions.setVisible(true);

            int maxVisible = Math.min(suggestions.size(), 6);
            lvSuggestions.setPrefHeight(maxVisible * lvSuggestions.getFixedCellSize());
        }
    }

    private void AddCategory(String category) {
        if(selectedCategories.contains(category))
            return;
        selectedCategories.add(category);
        RenderSelectedCategories();
    }

    private void RenderSelectedCategories() {
        hbSelectedCategories.getChildren().clear();
        for(String str : selectedCategories) {
            HBox chip = CreateChip(str);
            hbSelectedCategories.getChildren().add(chip);
        }
    }

    private HBox CreateChip(String str) {
        Label name = new Label(str);
        Button remove = new Button("X");
        remove.setOnAction(e -> {
            selectedCategories.remove(str);
            RenderSelectedCategories();
        });
        remove.setStyle("-fx-background-color: transparent;");
        HBox box = new HBox(name, remove);
        //box.setPrefHeight(24);
        box.setStyle("-fx-background-radius: 10; -fx-padding: 5 0 0 10; -fx-background-color: #d0d0d0; -fx-spacing: 15;");
        return box;
    }

    @FXML
    void CategorySearchKeyHandler(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            HandleSelect();
            String text = categorySearchField.getText().trim();
            if(text.isEmpty())
                return;
            if(selectedCategories.stream().anyMatch(c -> c.equalsIgnoreCase(text)))
                return;
            AddCategory(text);
            categorySearchField.clear();
            lvSuggestions.setVisible(false);
        }
        else if(event.getCode() == KeyCode.TAB) {
            if(suggestions.isEmpty())
                return;
            HandleSelect();
        }
        else if(event.getCode() == KeyCode.UP) {
            if (!suggestions.isEmpty()) {
                int sel = lvSuggestions.getSelectionModel().getSelectedIndex();
                if (sel == -1) sel = suggestions.size() - 1;
                else sel = (sel - 1 + suggestions.size()) % suggestions.size(); // circular
                lvSuggestions.getSelectionModel().select(sel);
            }
        }
        else if(event.getCode() == KeyCode.DOWN) {
            if (!suggestions.isEmpty()) {
                int sel = lvSuggestions.getSelectionModel().getSelectedIndex();
                if (sel == -1) sel = 0;     // first press = select top item
                else sel = (sel + 1) % suggestions.size(); // circular
                lvSuggestions.getSelectionModel().select(sel);
            }
        }
        event.consume();
    }

    @FXML
    void SendPost(MouseEvent event) {
        if(!validPostInfo.get()) return;

        if(!Client.isServerReachable()) {
            new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
            return;
        }

        try {
            String title = titleField.getText();
            String content = contentArea.getText();

            String mediaUrl = null;
            MediaType mediaType = null;

            ArrayList<Media> media = new ArrayList<>();
            if(mediaViewController != null) {
                ArrayList<File> fileArrayList = mediaViewController.GetFileArrayList();
                for (File selectedFile : fileArrayList) {
                    String uploadResponse = Client.UploadFile(selectedFile);
                    if (uploadResponse == null) {
                        new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
                        return;
                    }
                    Map<?, ?> json = Client.GetResponse(uploadResponse);
                    mediaUrl = (String) json.get("url");

                    String mime = Files.probeContentType(selectedFile.toPath());
                    if (mime != null) {
                        if (mime.startsWith("image/")) mediaType = MediaType.IMAGE;
                        else if (mime.startsWith("video/")) mediaType = MediaType.VIDEO;
                        else if (mime.startsWith("audio/")) mediaType = MediaType.AUDIO;
                        else mediaType = MediaType.OTHER;
                    }
                    media.add(new Media(mediaType, mediaUrl));
                }
            }

            Post post = new Post(1, currentUser, subcredditComboBox.getSelectionModel().getSelectedItem(), title, content, media, selectedCategories, null, null, 0, 0);
            int id = Client.CreatePost(post);
            if (id > 0) {
                post.SetID(id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Post uploaded successfully!");
                alert.showAndWait();
                if(mediaViewController != null) {
                    mediaViewController.Clean();
                    RemoveMediaPane();
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ActualPost_Template.fxml"));
                    Parent root = loader.load();

                    ActualPostTemplateController actualPostTemplateController = loader.getController();
                    actualPostTemplateController.InitData(post, currentUser, 0);

                    Stage stage = (Stage) postButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
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

    @FXML
    void AttachMedia(MouseEvent event) {
        Window window = ((javafx.scene.Node) event.getSource()).getScene().getWindow();

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
        if(mediaViewController == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("media-view.fxml"));
                Node mediaNode = loader.load();

                mediaViewController = loader.getController();
                mediaViewController.init(null, true, new ArrayList<>());

                mediaPane.getChildren().add(mediaNode);

                mediaViewController.done.addListener((obs, oldVal, newVal) -> {
                    if (newVal)
                        RemoveMediaPane();
                });
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        mediaViewController.AddMedia(file);
    }

    private void RemoveMediaPane() {
        mediaPane.getChildren().clear();
        if(mediaViewController != null)
            mediaViewController.Clean();
        mediaViewController = null;
    }

}
