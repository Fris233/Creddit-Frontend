package com.fhm.take2;
import com.crdt.Gender;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MessageController implements Initializable {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private VBox friendsListContainer;
    @FXML private Label friendName;
    @FXML private Label friendStatus;
    private User currentUser;
    private List<User> friends = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupScrollPane();
        applyInlineStyles();
        addSampleMessages();
        setupFriendlist();
        showEmptyChatState();

        // Setup scroll behavior
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            scrollPane.setVvalue(scrollPane.getVvalue() - delta / scrollPane.getContent().getBoundsInLocal().getHeight());
        });
    }

    private void applyInlineStyles() {
        // Apply dark theme styles directly
        messagesContainer.setStyle("-fx-background-color: #1E1E1E;");
        scrollPane.setStyle("-fx-background: #1E1E1E; -fx-border-color: #1E1E1E;");
        messageInput.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-color: #555555; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 15 8 15;");
        sendButton.setStyle("-fx-background-color: #0066CC; -fx-text-fill: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-padding: 8 20 8 20; -fx-font-weight: bold;");
    }

    private void setupEventHandlers() {
        // Send button click handler
        sendButton.setOnAction(_ -> sendMessage());

        // Enter key handler for text field
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Disable send button when input is empty
        messageInput.textProperty().addListener((_, _, newValue) -> sendButton.setDisable(newValue.trim().isEmpty()));

        // Initially disable send button
        sendButton.setDisable(true);
    }

    private void setupScrollPane() {
        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((_, _, _) -> scrollPane.setVvalue(1.0));

        scrollPane.setFitToWidth(true);
    }

    private void addSampleMessages() {
        // sample received messages (left side)
        addReceivedMessage("Hello! How are you doing?");
        addReceivedMessage("I got your earlier message");
        addReceivedMessage("Let me know when you're available");

        // sample sent messages (right side)
        addSentMessage("Hi there! I'm doing great.");
        addSentMessage("Thanks for getting back to me");
        addSentMessage("I'll be available in 30 minutes");
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            addSentMessage(text);
            messageInput.clear();
        }
    }

    private void addReceivedMessage(String message) {
        HBox messageBubble = createMessageBubble(message, false);
        messagesContainer.getChildren().add(messageBubble);
    }

    private void addSentMessage(String message) {
        HBox messageBubble = createMessageBubble(message, true);
        messagesContainer.getChildren().add(messageBubble);
    }

    private HBox createMessageBubble(String message, boolean isSent) {
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new Insets(5, 10, 5, 10));

        // Create message text
        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: white;");

        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setMaxWidth(300); // Limit bubble width
        textFlow.setPadding(new Insets(8, 12, 8, 12));


        if (isSent) {
            // Sent messages on the right
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle("-fx-background-color: #0066CC; -fx-background-radius: 15 15 5 15;");
        } else {
            // Received messages on the left
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 15 15 15 5;");
        }

        messageContainer.getChildren().add(textFlow);
        HBox.setHgrow(messageContainer, Priority.ALWAYS);

        return messageContainer;
    }

    private void setupFriendlist() {
        try {
            User friend1 = new User(1, "Hassan", "hassan@gmail.com", "0001", Gender.MALE, "lelelelelelelelelelelelelele", null, null, null, true);
            friends.add(friend1);

            User friend2 = new User(1, "Hasan", "hasan@gmail.com", "0010", Gender.MALE, "صلي على محمد", null, null, null, false);
            friends.add(friend2);

            displayFriendList();

        } catch (Exception e) {
            System.out.println("Error creating dummy users: " + e.getMessage());
            friends = new ArrayList<>();
        }
    }

    private void displayFriendList() {
        friendsListContainer.getChildren().clear();

        for (User user : friends) {
            HBox friendItem = createFriendItem(user);
            friendsListContainer.getChildren().add(friendItem);
        }
    }

    private HBox createFriendItem(User user) {
        HBox container = new HBox();
        container.setSpacing(12);
        container.setPadding(new Insets(12));
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-radius: 4; -fx-cursor: hand;");

        // Hover effect
        container.setOnMouseEntered(_ -> {
            if (currentUser != user) {
                container.setStyle("-fx-background-color: #272729; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });
        container.setOnMouseExited(_ -> {
            if (currentUser != user) {
                container.setStyle("-fx-background-color: transparent; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });


        Text statusDot = new Text("•");
        boolean isOnline = user.getActive();
        statusDot.setStyle(isOnline ?
                "-fx-fill: #46D160; -fx-font-size: 24px;" :
                "-fx-fill: #818384; -fx-font-size: 24px;");

        // User info
        VBox userInfo = new VBox();
        userInfo.setSpacing(2);
        HBox.setHgrow(userInfo, Priority.ALWAYS);


        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-text-fill: #D7DADC; -fx-font-size: 14px; -fx-font-weight: bold;");


        Label messageLabel = new Label("Click to start chatting"); // Placeholder
        messageLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12px;");
        messageLabel.setMaxWidth(150);
        messageLabel.setWrapText(true);

        userInfo.getChildren().addAll(usernameLabel, messageLabel);


        Label timeLabel = new Label(isOnline ? "now" : "recently");
        timeLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 11px;");

        container.getChildren().addAll(statusDot, userInfo, timeLabel);

        // Click to select friend
        container.setOnMouseClicked(_ -> selectUser(user));

        return container;
    }

    private void selectUser(User user) {
        currentUser = user;

        // Update chat header with user's username
        friendName.setText(user.getUsername());
        friendStatus.setText(user.getActive() ? "Online" : "Offline");

        // Clear existing messages and show sample conversation
        messagesContainer.getChildren().clear();
        addSampleMessages();
    }

    private void showEmptyChatState() {
        messagesContainer.getChildren().clear();
        friendName.setText("Select a chat");
        friendStatus.setText("Send an invite message to start chatting!");
    }

}
