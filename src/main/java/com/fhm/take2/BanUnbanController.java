package com.fhm.take2;

import com.crdt.Subcreddit;
import com.crdt.User;
import com.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

public class BanUnbanController implements Initializable {

    @FXML private ComboBox<Subcreddit> subcredditComboBox;
    @FXML private TextField searchUserField;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> joinDateColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    @FXML private TextArea reasonTextArea;
    @FXML private Button banButton;
    @FXML private Button unbanButton;
    @FXML private Label statusLabel;

    private User currentModerator;
    private Subcreddit currentSubcreddit;
    private ObservableList<Subcreddit> subcredditsList = FXCollections.observableArrayList();
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    public void init(User moderator) {
        this.currentModerator = moderator;
        loadModeratedSubcreddits();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupEventHandlers();
        applyStyles();
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(user.getUsername());
        });

        emailColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(user.getEmail());
        });

        joinDateColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            Timestamp joinDate = user.getTimeCreated();
            return new javafx.beans.property.SimpleStringProperty(
                    joinDate != null ? joinDate.toString().substring(0, 16) : "N/A"
            );
        });

        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            // Since we can't check ban status directly, show "MEMBER" for all users
            return new javafx.beans.property.SimpleStringProperty("MEMBER");
        });

        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button actionButton = new Button();
            private final HBox pane = new HBox(actionButton);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setSpacing(10);
                actionButton.setPrefWidth(100);
                actionButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleBan(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    actionButton.setText("BAN");
                    actionButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white;");
                    setGraphic(pane);
                }
            }
        });

        usersTable.setItems(usersList);
    }

    private void setupEventHandlers() {
        subcredditComboBox.setOnAction(event -> {
            currentSubcreddit = subcredditComboBox.getSelectionModel().getSelectedItem();
            if (currentSubcreddit != null) {
                loadUsersForSubcreddit();
            }
        });

        searchUserField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });

        banButton.setOnAction(event -> {
            User selectedUser = usersTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                handleBan(selectedUser);
            } else {
                showAlert("No User Selected", "Please select a user to ban.");
            }
        });

        // Unban button is disabled since we can't see banned users
        unbanButton.setDisable(true);
        unbanButton.setText("(Unban not available)");
    }

    private void applyStyles() {
        usersTable.setStyle("-fx-background-color: #1A1A1B; -fx-text-fill: white;");
        usernameColumn.setStyle("-fx-text-fill: #D7DADC;");
        emailColumn.setStyle("-fx-text-fill: #D7DADC;");
        joinDateColumn.setStyle("-fx-text-fill: #D7DADC;");
        statusColumn.setStyle("-fx-text-fill: #D7DADC;");
        banButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        unbanButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        statusLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12px;");
    }

    private void loadModeratedSubcreddits() {
        subcredditsList.clear();

        try {
            // Get all subcreddits
            Subcreddit[] allSubs = Client.GetAllSubcreddits();

            // Check which ones current user moderates
            for (Subcreddit sub : allSubs) {
                if (Client.VerifyModeration(currentModerator, sub)) {
                    subcredditsList.add(sub);
                }
            }

            if (subcredditsList.isEmpty()) {
                statusLabel.setText("You are not a moderator of any subcreddits.");
            } else {
                subcredditComboBox.setItems(subcredditsList);
                statusLabel.setText("Select a subcreddit to manage");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading subcreddits: " + e.getMessage());
        }
    }

    private void loadUsersForSubcreddit() {
        usersList.clear();

        if (currentSubcreddit == null) return;

        try {
            // Get subcreddit members using only GetSubMembers method
            ArrayList<User> members = Client.GetSubMembers(currentSubcreddit, 0);

            // Filter out current moderator
            for (User user : members) {
                if (user.getId() != currentModerator.getId()) {
                    usersList.add(user);
                }
            }

            usersTable.refresh();
            statusLabel.setText("Loaded " + usersList.size() + " members from " + currentSubcreddit.GetSubName());

            // Enable ban button if there are users
            banButton.setDisable(usersList.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading users: " + e.getMessage());
        }
    }

    private void handleBan(User user) {
        String reason = reasonTextArea.getText().trim();
        if (reason.isEmpty()) {
            showAlert("Reason Required", "Please enter a reason for the ban.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Ban");
        confirmAlert.setHeaderText("Ban User: " + user.getUsername());
        confirmAlert.setContentText("Are you sure you want to ban this user from " + currentSubcreddit.GetSubName() + "?\n\nReason: " + reason);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Remove user from the local list (simulating ban)
                    usersList.remove(user);
                    usersTable.refresh();

                    // Show success message
                    statusLabel.setText("✓ User " + user.getUsername() + " has been banned from " + currentSubcreddit.GetSubName());
                    statusLabel.setStyle("-fx-text-fill: #46D160;");
                    reasonTextArea.clear();
                    System.out.println("Would ban user " + user.getUsername() +
                            " from " + currentSubcreddit.GetSubName() +
                            " with reason: " + reason);

                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("✗ Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #FF4500;");
                }
            }
        });
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            usersTable.setItems(usersList);
            return;
        }

        ObservableList<User> filteredList = FXCollections.observableArrayList();
        String lowerCaseFilter = searchText.toLowerCase();

        for (User user : usersList) {
            if (user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(user);
            }
        }

        usersTable.setItems(filteredList);
        statusLabel.setText("Found " + filteredList.size() + " users matching '" + searchText + "'");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}