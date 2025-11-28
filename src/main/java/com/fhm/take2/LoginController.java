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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private Button okButton;

    private Consumer<User> onLoginSuccess;

    // Initialize method - called after FXML fields are injected
    @FXML
    public void initialize() {
        System.out.println("Login Controller Initialized");

        // Setup button hover effects
        setupOkButtonEffects();

        // Setup text field focus effects
        setupTextFieldEffects();

        // Set focus to email field when the form loads
        emailField.requestFocus();

        // Add Enter key support for login
        setupEnterKeySupport();
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
        TextField[] textFields = {emailField, passwordField};

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

        // Allow pressing Enter in email field to move to password field
        emailField.setOnAction(event -> passwordField.requestFocus());
    }

    @FXML
    private void handleOkButton(ActionEvent event) {
        System.out.println("OK button clicked");
        handleLogin();
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        System.out.println("Sign up link clicked - navigating to sign up page");
        navigateToSignUp();
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Basic validation
        if (email == null || email.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your email or username");
            emailField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your password");
            passwordField.requestFocus();
            return;
        }

        // Here you would typically validate credentials
        System.out.println("Login attempt with:");
        System.out.println("Email/Username: " + email);
        System.out.println("Password: " + maskPassword(password));

        // Use actual login logic with your Client class
        User user = performLogin(email, password);

        if (user != null) {
            showAlert("Login Successful", "Welcome back, " + user.getUsername() + "!");
            // Call the success callback if set
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
        } else {
            showAlert("Login Failed", "Invalid credentials. Please try again.");
            // Clear password field on failed login
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    private User performLogin(String email, String password) {
        try {
            // Use your actual Client.login method which calls User.login internally
            User user = Client.login(email, password);

            if (user != null) {
                System.out.println("Login successful for user: " + user.getUsername());
                System.out.println("User ID: " + user.getId());
                System.out.println("User Email: " + user.getEmail());
                return user;
            } else {
                System.out.println("Login failed - null user returned");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Login Error", "An error occurred during login: " + e.getMessage());
            return null;
        }
    }

    private void navigateToSignUp() {
        try {
            System.out.println("Loading signup.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-up.fxml"));
            Parent root = loader.load();
            System.out.println("signup.fxml loaded successfully");

            // Get the signup controller
            SignUpController signUpController = loader.getController();

            // Set up callback for successful signup
            signUpController.setOnSignUpSuccess(user -> {
                // When signup is successful, close the signup window and call login success
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(user);
                }
            });

            // Get the current stage (login window)
            Stage currentStage = (Stage) signUpLink.getScene().getWindow();
            System.out.println("Current stage: " + currentStage);

            // Set the new scene (replace login with signup)
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Sign Up - Creddit");
            currentStage.centerOnScreen();

            System.out.println("Navigation to sign up completed");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading signup.fxml: " + e.getMessage());
            showAlert("Navigation Error", "Unable to load sign up page: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error: " + e.getMessage());
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private String maskPassword(String password) {
        return password.replaceAll(".", "*");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert to match our dark theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        // Apply custom style to buttons
        ButtonType okButton = alert.getButtonTypes().get(0);
        Node okButtonNode = dialogPane.lookupButton(okButton);
        okButtonNode.setStyle("-fx-background-color: #0E1113; -fx-text-fill: white; -fx-background-radius: 10;");

        alert.showAndWait();
    }

    // Additional utility methods
    public void clearForm() {
        emailField.clear();
        passwordField.clear();
        emailField.requestFocus();
    }

    public void setEmail(String email) {
        emailField.setText(email);
    }

    public void setPassword(String password) {
        passwordField.setText(password);
    }
}