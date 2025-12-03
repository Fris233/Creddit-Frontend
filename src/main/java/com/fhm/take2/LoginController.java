package com.fhm.take2;

import com.Client;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
//a
import java.io.IOException;
import java.util.function.Consumer;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private StackPane passwordContainer;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private Button okButton;

    private Consumer<User> onLoginSuccess;
    private boolean isPasswordVisible = false;
    @FXML
    public void initialize() {
        System.out.println("Login Controller Initialized");
        setupOkButtonEffects();
        setupTogglePasswordButton();
        setupTextFieldEffects();
        setupPasswordVisibility();
        emailField.requestFocus();
        setupEnterKeySupport();
    }

    private void setupTogglePasswordButton() {
        // Set the eye icon
        Image eyeImage = new Image(getClass().getResourceAsStream("/com/fhm/take2/assets/eye3.png"));
        ImageView eyeIcon = new ImageView(eyeImage);
        eyeIcon.setFitHeight(20);
        eyeIcon.setFitWidth(20);
        eyeIcon.setPreserveRatio(true);

        togglePasswordButton.setGraphic(eyeIcon);
        togglePasswordButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        togglePasswordButton.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            togglePasswordButton.setStyle("-fx-background-color: #404040; -fx-border-color: transparent;");
        });

        togglePasswordButton.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            togglePasswordButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        });
    }

    private void setupPasswordVisibility() {
        // Initially hide the visible password field
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        // Sync the text between password fields
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!visiblePasswordField.isFocused()) {
                visiblePasswordField.setText(newValue);
            }
        });

        visiblePasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newValue);
            }
        });
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show the visible password field
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getLength());
        } else {
            // Show the password field
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getLength());
        }
    }

    // Set callback for successful login
    public void setOnLoginSuccess(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    private void setupOkButtonEffects() {
        String baseStyle = "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";
        String hoverStyle = "-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";
        String pressedStyle = "-fx-background-color: #3d8b40; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";

        okButton.setStyle(baseStyle);

        okButton.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            okButton.setStyle(hoverStyle);
        });

        okButton.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            okButton.setStyle(baseStyle);
        });

        okButton.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            okButton.setStyle(pressedStyle);
        });

        okButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            okButton.setStyle(hoverStyle);
        });
    }

    private void setupTextFieldEffects() {
        TextField[] textFields = {emailField, passwordField, visiblePasswordField};

        for (TextField field : textFields) {
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    // Focus gained
                    field.setStyle(field.getStyle().replace("#555", "#0079d3"));
                } else {
                    // Focus lost
                    field.setStyle(field.getStyle().replace("#0079d3", "#555"));
                }
            });
        }
    }

    private void setupEnterKeySupport() {
        // Allow pressing Enter in password field to trigger login
        passwordField.setOnAction(event -> handleLogin());
        visiblePasswordField.setOnAction(event -> handleLogin());

        // Allow pressing Enter in email field to move to password field
        emailField.setOnAction(event -> {
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        });
    }

    @FXML
    private void handleOkButton(ActionEvent event) {
        System.out.println("OK button clicked");
        handleLogin();
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        System.out.println("Sign up link clicked");
        navigateToSignUp();
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (email == null || email.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your email or username");
            emailField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your password");
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }

        User user = performLogin(email, password);

        if (user != null) {
            showAlert("Login Successful", "Welcome back, " + user.getUsername() + "!");
            // Call the success callback if set
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
        } else {
            //showAlert("Login Failed", "Invalid credentials. Please try again.");
            passwordField.clear();
            visiblePasswordField.clear();
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        }
    }

    private User performLogin(String email, String password) {
        try {
            return Client.login(email, password);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            if(e.getMessage().equalsIgnoreCase("online"))
                showAlert("Login Failed", "You are already logged in on another device");
            else
                showAlert("Login Failed", "Invalid credentials. Please try again.");
            return null;
        }
    }

    private void navigateToSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-up.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) signUpLink.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Sign Up - Creddit");
            currentStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        ButtonType okButton = alert.getButtonTypes().get(0);
        Node okButtonNode = dialogPane.lookupButton(okButton);
        okButtonNode.setStyle("-fx-background-color: #0E1113; -fx-text-fill: white; -fx-background-radius: 10;");

        alert.showAndWait();
    }
    public void clearForm() {
        emailField.clear();
        passwordField.clear();
        visiblePasswordField.clear();
        emailField.requestFocus();
    }

    public void setEmail(String email) {
        emailField.setText(email);
    }

    public void setPassword(String password) {
        passwordField.setText(password);
        visiblePasswordField.setText(password);
    }
}