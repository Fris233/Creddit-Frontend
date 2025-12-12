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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CreateSubcredditPageController {

    @FXML private TextField categorySearchField;
    @FXML private HBox hbSelectedCategories;
    @FXML private ListView<String> lvSuggestions;
    @FXML private Label mediaLabel;
    @FXML private Button nextButtonPG3;
    @FXML private VBox page1VBox;
    @FXML private VBox page2VBox;
    @FXML private VBox page3VBox;
    @FXML private VBox page4VBox;
    @FXML private HBox publicHBox;
    @FXML private HBox privateHBox;
    @FXML private TextArea subDescTextArea;
    @FXML private TextField subNameTextField;
    @FXML private Label topicCountLabel;
    @FXML private Label subDescLengthLabel;
    @FXML private Label subNameLengthLabel;

    private Consumer<Subcreddit> onCreationSuccess;

    private File subIcon = null;
    private User currentUser;
    private BooleanProperty validSubInfo = new SimpleBooleanProperty(false);
    private int currentPage = 1;
    private boolean isPrivate = false;

    private ArrayList<String> allCategories;
    private ArrayList<String> selectedCategories;

    private ObservableList<String> suggestions;

    public void InitData(User user) {
        this.currentUser = user;
        try {
            allCategories = new ArrayList<>(Arrays.asList(Client.GetAllCategories()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectedCategories = new ArrayList<>();

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


        subDescTextArea.textProperty().addListener((obs, oldText, newText) -> {
            subDescLengthLabel.setText(String.valueOf(newText.length()));
        });

        subNameTextField.textProperty().addListener((obs, oldText, newText) -> {
            if(newText.length() > 24) {
                subNameLengthLabel.setStyle("-fx-text-fill: red");
                validSubInfo.set(false);
            }
            else {
                subNameLengthLabel.setStyle("-fx-text-fill: #ffffff");
                validSubInfo.set(!newText.isBlank());
            }
            subNameLengthLabel.setText(newText.length() + "/24");
        });

        categorySearchField.textProperty().addListener((obs, oldText, newText) -> {
            if(newText.length() > 50)
                categorySearchField.setText(oldText);
        });

        validSubInfo.addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                nextButtonPG3.setStyle("-fx-background-color: #0079D3; -fx-text-fill: #ffffff; -fx-background-radius: 15;"); //button blue and pressable
                nextButtonPG3.setDisable(false);
            }
            else {
                nextButtonPG3.setStyle("-fx-background-color: #191c1e; -fx-text-fill: #525454; -fx-background-radius: 15;"); //button grayed out
                nextButtonPG3.setDisable(true);
            }
        });

        // Handle dragging over the pane
        page4VBox.setOnDragOver(event -> {
            if (currentPage == 4 && event.getGestureSource() != page4VBox && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // Handle dropping files
        page4VBox.setOnDragDropped(event -> {
            if(currentPage == 4) {
                var db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    List<File> files = db.getFiles();
                    files.forEach(this::AddFile);
                }
                event.setDropCompleted(success);
            }
            event.consume();
        });
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
        try {
            String mime = Files.probeContentType(file.toPath());
            if (mime != null) {
                if (mime.startsWith("image/")) {
                    this.subIcon = file;
                    String[] str = file.toURI().toString().split("/");
                    mediaLabel.setText(str[str.length-1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // filter
        suggestions.setAll(allCategories.stream().filter(c -> c.toLowerCase().contains(lower)).toList());

        // remove selection when typing
        lvSuggestions.getSelectionModel().clearSelection();

        if (suggestions.isEmpty()) {
            lvSuggestions.setVisible(false);
        } else {
            lvSuggestions.setVisible(true);

            // auto shrink / expand
            int maxVisible = Math.min(suggestions.size(), 6);
            lvSuggestions.setPrefHeight(maxVisible * lvSuggestions.getFixedCellSize());
        }
    }

    private void AddCategory(String category) {
        if(selectedCategories.contains(category) || selectedCategories.size() == 3)
            return;
        selectedCategories.add(category);
        topicCountLabel.setText("Topics" + selectedCategories.size() + "/3");
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
    void NextPage(MouseEvent event) {
        if(currentPage == 4)
            return;
        currentPage++;
        RenderPages();
    }

    @FXML
    void BackPage(MouseEvent event) {
        if(currentPage == 1)
            return;
        currentPage--;
        RenderPages();
    }

    private void RenderPages() {
        page1VBox.setVisible(false);
        page2VBox.setVisible(false);
        page3VBox.setVisible(false);
        page4VBox.setVisible(false);
        if(currentPage == 1)
            page1VBox.setVisible(true);
        else if(currentPage == 2)
            page2VBox.setVisible(true);
        else if(currentPage == 3)
            page3VBox.setVisible(true);
        else if(currentPage == 4)
            page4VBox.setVisible(true);
    }

    @FXML
    void Cancel(MouseEvent event) {

    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        if(!validSubInfo.get()) return;

        if(!Client.isServerReachable()) {
            new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
            return;
        }

        try {
            String mediaUrl = null;
            MediaType mediaType = null;

            if(subIcon != null) {
                String uploadResponse = Client.UploadFile(subIcon);
                if (uploadResponse == null) {
                    new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
                    return;
                }
                Map<?, ?> json = Client.GetResponse(uploadResponse);
                mediaUrl = (String) json.get("url");
                mediaType = MediaType.IMAGE;
            }

            // Now send post JSON
            Subcreddit sub = new Subcreddit(1, subNameTextField.getText(), subDescTextArea.getText(), null, this.currentUser, new Media(mediaType, mediaUrl), isPrivate);
            int id = Client.CreateSubcreddit(sub);
            if (id > 0) {
                sub.SetID(id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Subcreddit Created successfully!");
                alert.showAndWait();
                if (onCreationSuccess != null) {
                    onCreationSuccess.accept(sub);
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create subcreddit!");
                alert.showAndWait();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }

        event.consume();
    }

    // Set callback for successful login
    public void setOnCreationSuccess(Consumer<Subcreddit> onCreationSuccess) {
        this.onCreationSuccess = onCreationSuccess;
    }

    @FXML
    void MakePrivate(MouseEvent event) {
        isPrivate = true;
        privateHBox.setStyle("-fx-background-color #343536;");
        publicHBox.setStyle("");
    }

    @FXML
    void MakePublic(MouseEvent event) {
        isPrivate = false;
        publicHBox.setStyle("-fx-background-color #343536;");
        privateHBox.setStyle("");
    }
}
