package com.fhm.take2;

import com.Client;
import com.crdt.Message;
import com.crdt.User;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class MessageController {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private VBox friendsListContainer;
    @FXML private Label friendName;
    @FXML private Label friendStatus;

    private User currentUser;
    private User currentFriend;

    private final List<User> friends = new ArrayList<>();
    private final Map<Integer, List<Message>> allMessages = new HashMap<>();
    private final Map<Integer, List<Message>> unreadMessages = new HashMap<>();
    private final ContextMenu messageContextMenu = new ContextMenu();
    private HBox selectedMessageBubble = null;

    private boolean receiving = true;
    private boolean isLoadingOlderMessages = false;
    private boolean scrollCooldown = false;

    /* --------------------------------------------------- */

    public void Init(User user) {
        this.currentUser = user;

        setupEventHandlers();
        setupScrollPane();
        applyInlineStyles();
        setupFriendlist();
        showEmptyChatState();
        setupContextMenu(); // Initialize context menu

        try {
            for (Message msg : Client.GetUnreadPM(currentUser)) {
                unreadMessages
                        .computeIfAbsent(msg.GetSender().getId(), k -> new ArrayList<>())
                        .add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread receiver = new Thread(() -> {
            while (receiving) {
                try {
                    loadRecentMessages();
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        });
        receiver.setDaemon(true);
        receiver.start();

        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            scrollPane.setVvalue(scrollPane.getVvalue()
                    - delta / scrollPane.getContent().getBoundsInLocal().getHeight());
        });

        setupScrollListener();
    }

    /* --------------------------------------------------- */

    private void setupContextMenu() {
        MenuItem deleteItem = new MenuItem("Delete Message");
        deleteItem.setOnAction(e -> deleteSelectedMessage());
        messageContextMenu.getItems().add(deleteItem);
    }

    private void deleteSelectedMessage() {
        if (selectedMessageBubble == null) return;

        // Get the message from the bubble's userData
        Message msg = (Message) selectedMessageBubble.getUserData();
        if (msg == null) return;

        // Check if message belongs to current user
        if (!msg.GetSender().equals(currentUser)) {
            return; // Can only delete own messages
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Delete this message?");
        alert.setContentText("This message will be deleted for everyone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Call server to delete message
                Client.DeletePM(msg);

                // Remove from local storage
                allMessages.get(currentFriend.getId()).removeIf(m -> m.GetID() == msg.GetID());

                // Remove from UI
                messagesContainer.getChildren().remove(selectedMessageBubble);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* --------------------------------------------------- */

    private void setupScrollListener() {
        scrollPane.vvalueProperty().addListener((_, _, v) -> {
            if (v.doubleValue() <= 0.01 && !scrollCooldown && !isLoadingOlderMessages && currentFriend != null) {
                loadOlderMessages();
                scrollCooldown = true;

                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> scrollCooldown = false);
                pause.play();
            }
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /* --------------------------------------------------- */

    private void loadOlderMessages() {
        if (currentFriend == null) return;

        isLoadingOlderMessages = true;

        try {
            List<Message> list = allMessages.get(currentFriend.getId());

            int beforeId = list.isEmpty()
                    ? Integer.MAX_VALUE
                    : list.get(0).GetID();

            List<Message> older = Client.GetPMFeed(currentUser, currentFriend, beforeId);

            if (!older.isEmpty()) {
                for (Message msg : older) {
                    if (!containsMessage(list, msg)) {
                        list.add(0, msg);
                        addMessageToTop(msg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isLoadingOlderMessages = false;
    }

    /* --------------------------------------------------- */

    private void loadRecentMessages() {
        try {
            List<Message> unread = Client.GetUnreadPM(currentUser);
            if (unread.isEmpty()) return;

            Platform.runLater(() -> {
                for (Message msg : unread) {
                    int senderId = msg.GetSender().getId();

                    allMessages
                            .computeIfAbsent(senderId, k -> new ArrayList<>());

                    if (!containsMessage(allMessages.get(senderId), msg)) {
                        allMessages.get(senderId).add(msg);

                        if (currentFriend != null && senderId == currentFriend.getId()) {
                            addMessageToBottom(msg);
                        }
                    }
                }

                try {
                    if (currentFriend != null) {
                        Client.ReadMessage(currentUser, currentFriend);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* --------------------------------------------------- */

    private void loadUnreadMessages(User user) {
        List<Message> unread = unreadMessages.get(user.getId());
        List<Message> all = allMessages.get(user.getId());

        if (unread == null || unread.isEmpty()) return;

        for (Message msg : unread) {
            if (!containsMessage(all, msg)) {
                all.add(msg);
            }
        }

        unread.clear();
    }

    /* --------------------------------------------------- */

    private boolean containsMessage(List<Message> list, Message msg) {
        return list.stream().anyMatch(m -> m.GetID() == msg.GetID());
    }

    /* --------------------------------------------------- */

    private void addMessageToTop(Message msg) {
        HBox bubble = createMessageBubbleFromMessage(msg);
        messagesContainer.getChildren().addFirst(bubble);
    }

    private void addMessageToBottom(Message msg) {
        boolean shouldScroll = scrollPane.getVvalue() >= 0.99;
        HBox bubble = createMessageBubbleFromMessage(msg);
        messagesContainer.getChildren().add(bubble);

        if (shouldScroll) {
            scrollToBottom();
        }
    }

    private HBox createMessageBubbleFromMessage(Message msg) {
        HBox bubble = createMessageBubble(msg, msg.GetSender().equals(currentUser));
        bubble.setUserData(msg); // Store the Message object
        return bubble;
    }

    /* --------------------------------------------------- */

    private void selectUser(User user) {
        currentFriend = user;

        friendName.setText(user.getUsername());
        friendStatus.setText("");

        messagesContainer.getChildren().clear();

        loadUnreadMessages(user);

        for (Message msg : allMessages.get(user.getId())) {
            addMessageToBottom(msg);
        }

        if (allMessages.get(user.getId()).size() < 20) {
            loadOlderMessages();
        }

        try {
            Client.ReadMessage(currentUser, user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        displayFriendList();
        scrollToBottom();
    }

    /* --------------------------------------------------- */

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || currentFriend == null) return;

        Message msg = new Message(
                0,
                currentUser,
                currentFriend,
                text,
                null,
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()),
                false,
                false
        );

        try {
            Client.SendPM(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        allMessages.get(currentFriend.getId()).add(msg);
        addMessageToBottom(msg);
        messageInput.clear();
        scrollToBottom();
    }

    /* --------------------------------------------------- */

    private void setupFriendlist() {
        try {
            friends.clear();
            friends.addAll(Client.GetFriends(currentUser));

            for (User u : friends) {
                allMessages.putIfAbsent(u.getId(), new ArrayList<>());
                unreadMessages.putIfAbsent(u.getId(), new ArrayList<>());
            }

            displayFriendList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayFriendList() {
        friendsListContainer.getChildren().clear();
        for (User u : friends) {
            friendsListContainer.getChildren().add(createFriendItem(u));
        }
    }

    /* --------------------------------------------------- */

    private HBox createFriendItem(User user) {
        HBox container = new HBox();
        container.setSpacing(12);
        container.setPadding(new Insets(12));
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: transparent; -fx-background-radius: 4; -fx-cursor: hand;");

        container.setOnMouseEntered(_ -> {
            if (currentFriend != user) {
                container.setStyle("-fx-background-color: #272729; -fx-background-radius: 4;");
            }
        });
        container.setOnMouseExited(_ -> {
            if (currentFriend != user) {
                container.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;");
            }
        });

        Circle online = new Circle(5,
                user.getLastSeen().after(Timestamp.from(Instant.now().minusSeconds(10)))
                        ? javafx.scene.paint.Color.GREEN
                        : javafx.scene.paint.Color.GRAY
        );

        Label name = new Label(user.getUsername());
        name.setStyle("-fx-text-fill: #D7DADC; -fx-font-weight: bold;");

        VBox info = new VBox(name);
        HBox.setHgrow(info, Priority.ALWAYS);

        container.getChildren().addAll(online, info);
        container.setOnMouseClicked((mouseEvent) -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                container.setStyle("-fx-background-color: #272729; -fx-background-radius: 4;");
                selectUser(user);
                mouseEvent.consume();
            }
        });

        return container;
    }

    /* --------------------------------------------------- */

    private void setupEventHandlers() {
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
                e.consume();
            }
        });

        messageInput.textProperty().addListener((_, _, v) ->
                sendButton.setDisable(v.trim().isEmpty()));
    }

    private void setupScrollPane() {
        scrollPane.setFitToWidth(true);

        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void applyInlineStyles() {
        messagesContainer.setStyle("-fx-background-color: #1E1E1E;");
        scrollPane.setStyle("-fx-background: #1E1E1E;");
    }

    private void showEmptyChatState() {
        messagesContainer.getChildren().clear();
        friendName.setText("Select a chat");
        friendStatus.setText("Send an invite message to start chatting!");
    }

    private HBox createMessageBubble(Message msg, boolean isSent) {
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new Insets(5, 10, 5, 10));
        messageContainer.setMaxWidth(Double.MAX_VALUE);

        Text messageText = new Text(msg.GetText());
        messageText.setStyle("-fx-fill: white;");
        messageText.setWrappingWidth(280);

        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setPadding(new Insets(8, 12, 8, 12));
        textFlow.setMaxWidth(300);

        if (isSent) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle(
                    "-fx-background-color: #0066CC;" +
                            "-fx-background-radius: 15 15 5 15;"
            );

            // Add right-click context menu for sent messages
            messageContainer.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    selectedMessageBubble = messageContainer;
                    messageContextMenu.show(messageContainer, e.getScreenX(), e.getScreenY());
                    e.consume();
                }
            });

        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle(
                    "-fx-background-color: #2D2D2D;" +
                            "-fx-background-radius: 15 15 15 5;"
            );
        }

        messageContainer.getChildren().add(textFlow);
        HBox.setHgrow(messageContainer, Priority.ALWAYS);

        return messageContainer;
    }

    public void Clean() {
        receiving = false;
    }
}