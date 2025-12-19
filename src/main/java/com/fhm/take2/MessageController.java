package com.fhm.take2;
import com.Client;
import com.crdt.Gender;
import com.crdt.Message;
import com.crdt.User;
import javafx.animation.PauseTransition;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class MessageController {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private VBox friendsListContainer;
    @FXML private Label friendName;
    @FXML private Label friendStatus;
    private User currentFriend;
    private List<User> friends = new ArrayList<>();
    private Map<User, ArrayList<Message>> unreadMessages = new HashMap<>();
    private Map<User, ArrayList<Message>> allMessages = new HashMap<>();
    private boolean isLoadingOlderMessages = false;
    private int currentLoadedCount = 0;
    private boolean scrollCooldown = false;

    private User currentUser;

    public void Init(User user) {
        this.currentUser = user;
        setupEventHandlers();
        setupScrollPane();
        applyInlineStyles();
//        addSampleMessages();
        setupFriendlist();
        showEmptyChatState();

        // Setup scroll behavior
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            scrollPane.setVvalue(scrollPane.getVvalue() - delta / scrollPane.getContent().getBoundsInLocal().getHeight());
        });

        setupScrollListener();
    }

    private void setupScrollListener() {
        scrollPane.vvalueProperty().addListener((obs, oldValue, newValue) -> {
            // When scrolled to top (or near top), load older messages
            if (newValue.doubleValue() < 0.1 && !isLoadingOlderMessages && currentFriend != null) {
                loadOlderMessages();
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> scrollCooldown = false);
                pause.play();
            }
        });
    }

    private void loadOlderMessages() {
        if (currentFriend == null) return;

        isLoadingOlderMessages = true;

        // Get older messages (simulating database fetch)
        List<Message> olderMessages = fetchOlderMessages(currentFriend, currentLoadedCount);

        if (!olderMessages.isEmpty()) {
            // Add older messages to the TOP of the container
            for (int i = olderMessages.size() - 1; i >= 0; i--) {
                Message msg = olderMessages.get(i);
                addMessageToTop(msg);
            }

            currentLoadedCount += olderMessages.size();
        }

        isLoadingOlderMessages = false;
    }

    private List<Message> fetchOlderMessages(User user, int offset) {
        // Simulate fetching older messages from database
        List<Message> olderMsgs = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            olderMsgs.add(new Message(
                    -1, // dummy ID
                    user, // sender
                    null, // receiver (would be current user)
                    "Older message " + (offset + i),
                    null, // media
                    new java.sql.Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * (i + offset))), // older time
                    null, // edit time
                    true // read
            ));
        }

        return olderMsgs;
    }

    private void addMessageToTop(Message message) {
        HBox messageBubble = createMessageBubbleFromMessage(message);
        messagesContainer.getChildren().add(0, messageBubble); // Add to TOP
    }

    private HBox createMessageBubbleFromMessage(Message message) {
        boolean isSentByMe = message.GetSender().equals(currentFriend); // Need to compare properly
        return createMessageBubble(message.GetText(), isSentByMe);
    }

    private void addMessageToBottom(Message message) {
        HBox messageBubble = createMessageBubbleFromMessage(message);
        messagesContainer.getChildren().add(messageBubble); // Add to BOTTOM
    }

    private int getUnreadCount(User user) {
        return unreadMessages.getOrDefault(user, new ArrayList<>()).size();
    }

    private String getLastMessagePreview(User user) {
        ArrayList<Message> messages = allMessages.get(user);
        if (messages != null && !messages.isEmpty()) {
            Message lastMsg = messages.get(messages.size() - 1);
            return lastMsg.GetText();
        }

        // Check unread messages
        ArrayList<Message> unread = unreadMessages.get(user);
        if (unread != null && !unread.isEmpty()) {
            return unread.get(unread.size() - 1).GetText();
        }

        return "Click to start chatting";
    }

    private void loadUnreadMessages(User user) {
        ArrayList<Message> unread = unreadMessages.get(user);
        if (unread != null) {
            // Add to all messages
            allMessages.get(user).addAll(unread);

            // Display unread messages
            for (Message msg : unread) {
                addMessageToBottom(msg);
            }

            currentLoadedCount = unread.size();
        }
    }

    private void loadRecentMessages(User user) {
        // Get recent messages (from allMessages or fetch from database)
        ArrayList<Message> recent = fetchRecentMessages(user, Math.max(20 - currentLoadedCount, 0));

        if (!recent.isEmpty()) {
            // Add to all messages
            allMessages.get(user).addAll(recent);

            // Display at bottom
            for (Message msg : recent) {
                addMessageToBottom(msg);
            }

            currentLoadedCount += recent.size();
        }
    }

    private ArrayList<Message> fetchRecentMessages(User user, int count) {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(new Message(
                    -1, // dummy ID
                    user, // sender
                    null, // receiver
                    "Recent message " + i,
                    null, // media
                    new java.sql.Timestamp(System.currentTimeMillis() - (1000 * 60 * i)), // recent time
                    null, // edit time
                    true // read
            ));
        }
        return messages;
    }

    private void markMessagesAsRead(User user) {
        // Mark all unread messages as read
        ArrayList<Message> unread = unreadMessages.get(user);
        if (unread != null) {
            try {
                Client.ReadMessage(this.currentUser, user);
                unread.clear();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyInlineStyles() {
        // Apply dark theme styles directly
        messagesContainer.setStyle("-fx-background-color: #1E1E1E;");
        scrollPane.setStyle("-fx-background: #1E1E1E; -fx-border-color: #1E1E1E;");
        messageInput.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-color: #555555; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 15 8 15;");
        sendButton.setStyle("-fx-background-color: #0066CC; -fx-text-fill: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-padding: 8 20 8 20; -fx-font-weight: bold;");
    }

    private void setupEventHandlers() {
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
        //messagesContainer.heightProperty().addListener((_, _, _) -> scrollPane.setVvalue(1.0));

        scrollPane.setFitToWidth(true);
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && currentFriend != null) {
            // Create message
            Message newMessage = new Message(
                    0, // will be set by database
                    this.currentUser, // sender (would be current loggedin user)
                    this.currentFriend, // receiver
                    text,
                    null, // media
                    Timestamp.from(Instant.now()), // create time
                    Timestamp.from(Instant.now()), // edit time
                    false // not read yet by receiver
            );
            try {
                Client.SendPM(newMessage);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            addMessageToBottom(newMessage);
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
            friends = Client.GetFriends(this.currentUser);
            for(User friend : friends) {
                unreadMessages.put(friend, new ArrayList<>());
            }
            ArrayList<Message> unread = Client.GetUnreadPM(this.currentUser);
            for(Message msg : unread) {
                unreadMessages.get(msg.GetSender()).add(msg);
            }

            displayFriendList();

        } catch (Exception e) {
            e.printStackTrace();
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

        // Online status indicator
        Circle onlineCircle = new Circle(5);
        boolean isOnline = user.getLastSeen().after(Timestamp.from(Instant.now().minusSeconds(10)));
        onlineCircle.setFill(isOnline ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.GRAY);
        onlineCircle.setStroke(javafx.scene.paint.Color.TRANSPARENT);

        // User info
        VBox userInfo = new VBox();
        userInfo.setSpacing(2);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-text-fill: #D7DADC; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Last message preview
        String lastMessage = getLastMessagePreview(user);
        Label messageLabel = new Label(lastMessage);
        messageLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12px;");
        messageLabel.setMaxWidth(150);
        messageLabel.setWrapText(true);

        userInfo.getChildren().addAll(usernameLabel, messageLabel);

        // Time and unread count container
        VBox rightInfo = new VBox();
        rightInfo.setSpacing(4);
        rightInfo.setAlignment(Pos.TOP_RIGHT);

        // Time label
        Label timeLabel = new Label(isOnline ? "Online" : "Offline");
        timeLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 11px;");

        // Unread count badge
        int unreadCount = getUnreadCount(user);
        if (unreadCount > 0) {
            Circle unreadBadge = new Circle(10);
            unreadBadge.setFill(javafx.scene.paint.Color.GREEN);

            Label countLabel = new Label(String.valueOf(unreadCount));
            countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
            countLabel.setAlignment(Pos.CENTER);

            HBox badgeContainer = new HBox(unreadBadge, countLabel);
            badgeContainer.setAlignment(Pos.CENTER);
            badgeContainer.setSpacing(2);

            rightInfo.getChildren().add(badgeContainer);
        }

        rightInfo.getChildren().add(timeLabel);

        container.getChildren().addAll(onlineCircle, userInfo, rightInfo);


        container.setOnMouseEntered(_ -> {
            if (currentFriend != user) {
                container.setStyle("-fx-background-color: #272729; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });
        container.setOnMouseExited(_ -> {
            if (currentFriend != user) {
                container.setStyle("-fx-background-color: transparent; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });

        // Click to select friend
        container.setOnMouseClicked(_ -> selectUser(user));

        return container;
    }

    private void selectUser(User user) {
        currentFriend = user;
        currentLoadedCount = 0;

        // Update chat header
        friendName.setText(user.getUsername());
        friendStatus.setText("");

        // Clear current messages
        messagesContainer.getChildren().clear();

        // Load unread messages first (add to allMessages and clear unread)
        loadUnreadMessages(user);

        // Load recent messages (minimum 20)
        loadRecentMessages(user);

        // Mark all as read
        markMessagesAsRead(user);

        // Update friend list to remove unread badge
        displayFriendList();
    }

    private void showEmptyChatState() {
        messagesContainer.getChildren().clear();
        friendName.setText("Select a chat");
        friendStatus.setText("Send an invite message to start chatting!");
    }

}
