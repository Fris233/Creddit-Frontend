package com.fhm.take2;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.net.URL;
import java.util.ResourceBundle;

public class MessageController implements Initializable {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupScrollPane();
        applyInlineStyles();
        addSampleMessages(); // Add sample messages to demonstrate layout
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
        sendButton.setOnAction(event -> sendMessage());

        // Enter key handler for text field
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Disable send button when input is empty
        messageInput.textProperty().addListener((observable, oldValue, newValue) -> {
            sendButton.setDisable(newValue.trim().isEmpty());
        });

        // Initially disable send button
        sendButton.setDisable(true);
    }

    private void setupScrollPane() {
        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });

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
}