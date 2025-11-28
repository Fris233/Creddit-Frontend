package com.fhm.take2;

import com.Client;
import com.crdt.User;
import com.crdt.Gender;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.function.Consumer;

public class SignUpController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<Gender> genderComboBox;

    @FXML
    private Button signUpButton;

    // REMOVED: private Hyperlink loginLink;

    private Consumer<User> onSignUpSuccess;

    // Initialize method - called after FXML fields are injected
    @FXML
    public void initialize() {
        System.out.println("Sign Up Controller Initialized");

        // Setup button hover effects
        setupSignUpButtonEffects();

        // Setup text field focus effects
        setupTextFieldEffects();

        // Setup gender combo box
        setupGenderComboBox();

        // Set focus to email field when the form loads
        emailField.requestFocus();
    }

    private void setupGenderComboBox() {
        if (genderComboBox != null) {
            // Clear any existing items
            genderComboBox.getItems().clear();

            // Add gender options
            genderComboBox.getItems().addAll(Gender.MALE, Gender.FEMALE);

            // Set default value
            genderComboBox.setValue(Gender.MALE);

            // Simple string conversion for display
            genderComboBox.setConverter(new StringConverter<Gender>() {
                @Override
                public String toString(Gender gender) {
                    if (gender == null) return "";
                    switch (gender) {
                        case MALE: return "Male";
                        case FEMALE: return "Female";
                        default: return gender.name();
                    }
                }

                @Override
                public Gender fromString(String string) {
                    if (string == null) return Gender.MALE;
                    switch (string.toLowerCase()) {
                        case "male": return Gender.MALE;
                        case "female": return Gender.FEMALE;
                        default: return Gender.MALE;
                    }
                }
            });

            // Basic styling
            genderComboBox.setStyle("-fx-background-color: #2C3539; -fx-text-fill: white;");

            System.out.println("Gender ComboBox items: " + genderComboBox.getItems());
        } else {
            System.err.println("Gender ComboBox is null - check FXML binding");
        }
    }

    // Set callback for successful signup
    public void setOnSignUpSuccess(Consumer<User> onSignUpSuccess) {
        this.onSignUpSuccess = onSignUpSuccess;
    }

    private void setupSignUpButtonEffects() {
        String baseStyle = "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";
        String hoverStyle = "-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";
        String pressedStyle = "-fx-background-color: #3d8b40; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";

        signUpButton.setStyle(baseStyle);

        signUpButton.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            signUpButton.setStyle(hoverStyle);
        });

        signUpButton.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            signUpButton.setStyle(baseStyle);
        });

        signUpButton.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            signUpButton.setStyle(pressedStyle);
        });

        signUpButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            signUpButton.setStyle(hoverStyle);
        });
    }

    private void setupTextFieldEffects() {
        TextField[] textFields = {emailField, usernameField, passwordField, confirmPasswordField};

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

    @FXML
    private void handleSignUpButton(ActionEvent event) {
        System.out.println("Sign Up button clicked");
        handleSignUp();
    }

    // REMOVED: handleLoginLink method

    private void handleSignUp() {
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Gender gender = genderComboBox.getValue();

        // Basic validation
        if (email == null || email.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your email");
            emailField.requestFocus();
            return;
        }

        if (username == null || username.trim().isEmpty()) {
            showAlert("Validation Error", "Please choose a username");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Please create a password");
            passwordField.requestFocus();
            return;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            showAlert("Validation Error", "Please confirm your password");
            confirmPasswordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Passwords do not match");
            passwordField.clear();
            confirmPasswordField.clear();
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 8) {
            showAlert("Validation Error", "Password must be at least 8 characters long");
            passwordField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Validation Error", "Please enter a valid email address");
            emailField.requestFocus();
            return;
        }

        if (username.length() > 32) {
            showAlert("Validation Error", "Username must be 32 characters or less");
            usernameField.requestFocus();
            return;
        }

        if (gender == null) {
            showAlert("Validation Error", "Please select your gender");
            genderComboBox.requestFocus();
            return;
        }

        // Show loading state
        signUpButton.setDisable(true);
        signUpButton.setText("Signing Up...");

        // Perform signup process
        performSignUp(email, username, password, gender);
    }

    private void performSignUp(String email, String username, String password, Gender gender) {
        new Thread(() -> {
            try {
                String bio = ""; // Empty bio

                Client.register(new User(0, username, email, password, gender, bio, null, null, true));

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    signUpButton.setDisable(false);
                    signUpButton.setText("SIGN UP");

                    if (user != null) {
                        System.out.println("Signup successful for user: " + user.getUsername());
                        System.out.println("User ID: " + user.getId());
                        System.out.println("User Gender: " + user.getGender());

                        showAlert("Registration Successful", "Welcome to Creddit! You are now logged in.");
                        // Call the success callback if set
                        if (onSignUpSuccess != null) {
                            onSignUpSuccess.accept(user);
                        }
                    } else {
                        showAlert("Registration Failed", "This email or username might already be taken. Please try again.");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    signUpButton.setDisable(false);
                    signUpButton.setText("SIGN UP");

                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (errorMessage.contains("409") || errorMessage.contains("already exists"))) {
                        showAlert("Registration Failed", "This email or username is already taken. Please try a different one.");
                    } else {
                        showAlert("Signup Error", "An error occurred during signup: " + errorMessage);
                    }
                });
            }
        }).start();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // REMOVED: navigateToLogin method

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
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        if (genderComboBox != null) {
            genderComboBox.setValue(Gender.MALE);
        }
        emailField.requestFocus();
    }

    public void setEmail(String email) {
        emailField.setText(email);
    }

    public void setUsername(String username) {
        usernameField.setText(username);
    }
}