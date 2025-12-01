package com.fhm.take2;

import com.Client;
import com.crdt.User;
import com.crdt.Gender;
import com.crdt.Media;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.function.Consumer;

public class SignUpController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private StackPane passwordContainer;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField visibleConfirmPasswordField;

    @FXML
    private StackPane confirmPasswordContainer;

    @FXML
    private Button toggleConfirmPasswordButton;

    @FXML
    private ComboBox<Gender> genderComboBox;

    @FXML
    private Button signUpButton;

    private Consumer<User> onSignUpSuccess;
    private Runnable onReturnToLogin;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
//a
    @FXML
    public void initialize() {
        System.out.println("Sign Up Controller Initialized");

        // Setup button hover effects
        setupSignUpButtonEffects();
        setupTogglePasswordButtons();

        // Setup text field focus effects
        setupTextFieldEffects();

        // Setup gender combo box
        setupGenderComboBox();

        // Setup password visibility
        setupPasswordVisibility();

        // Set focus to email field when the form loads
        emailField.requestFocus();
    }

    private void setupTogglePasswordButtons() {
        // Set the eye icon for both buttons
        Image eyeImage = new Image(getClass().getResourceAsStream("/com/fhm/take2/assets/eye3.png"));

        ImageView eyeIcon1 = new ImageView(eyeImage);
        eyeIcon1.setFitHeight(20);
        eyeIcon1.setFitWidth(20);
        eyeIcon1.setPreserveRatio(true);
        togglePasswordButton.setGraphic(eyeIcon1);
        togglePasswordButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        ImageView eyeIcon2 = new ImageView(eyeImage);
        eyeIcon2.setFitHeight(20);
        eyeIcon2.setFitWidth(20);
        eyeIcon2.setPreserveRatio(true);
        toggleConfirmPasswordButton.setGraphic(eyeIcon2);
        toggleConfirmPasswordButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Add hover effects
        setupToggleButtonHover(togglePasswordButton);
        setupToggleButtonHover(toggleConfirmPasswordButton);
    }

    private void setupToggleButtonHover(Button button) {
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            button.setStyle("-fx-background-color: #404040; -fx-border-color: transparent;");
        });

        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        });
    }

    private void setupPasswordVisibility() {
        // Initially hide the visible password fields
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.setManaged(false);

        // Sync text between password fields
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

        // Sync text between confirm password fields
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!visibleConfirmPasswordField.isFocused()) {
                visibleConfirmPasswordField.setText(newValue);
            }
        });

        visibleConfirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!confirmPasswordField.isFocused()) {
                confirmPasswordField.setText(newValue);
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

    @FXML
    private void toggleConfirmPasswordVisibility(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            // Show the visible confirm password field
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            visibleConfirmPasswordField.requestFocus();
        } else {
            // Show the confirm password field
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);
            confirmPasswordField.requestFocus();
        }
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

    // Set callback for returning to login
    public void setOnReturnToLogin(Runnable onReturnToLogin) {
        this.onReturnToLogin = onReturnToLogin;
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
        TextField[] textFields = {emailField, usernameField, passwordField, visiblePasswordField,
                confirmPasswordField, visibleConfirmPasswordField};

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

    private void handleSignUp() {
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();
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
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            showAlert("Validation Error", "Please confirm your password");
            if (isConfirmPasswordVisible) {
                visibleConfirmPasswordField.requestFocus();
            } else {
                confirmPasswordField.requestFocus();
            }
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Passwords do not match");
            passwordField.clear();
            visiblePasswordField.clear();
            confirmPasswordField.clear();
            visibleConfirmPasswordField.clear();
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }

        // Updated password validation: 8-32 characters
        if (password.length() < 8) {
            showAlert("Validation Error", "Password must be at least 8 characters long");
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }

        if (password.length() > 32) {
            showAlert("Validation Error", "Password cannot exceed 32 characters");
            if (isPasswordVisible) {
                visiblePasswordField.requestFocus();
                visiblePasswordField.positionCaret(visiblePasswordField.getLength());
            } else {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getLength());
            }
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

        // Perform signup process using Client.register
        performSignUp(email, username, password, gender);
    }

    private void performSignUp(String email, String username, String password, Gender gender) {
        new Thread(() -> {
            try {
                // Create User object with registration data
                String bio = ""; // Empty bio
                Media pfp = null; // No profile picture
                Timestamp timeCreated = null; // Will be set by server
                boolean active = true;

                // Create user object
                User user = new User(0, username, email, password, gender, bio, pfp, timeCreated, active);

                // Use Client.register instead of Client.signup
                boolean registrationSuccess = Client.register(user);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    signUpButton.setDisable(false);
                    signUpButton.setText("SIGN UP");

                    if (registrationSuccess) {
                        System.out.println("Registration successful for user: " + username);

                        showAlertAndReturnToLogin("Registration Successful",
                                "Welcome to Creddit! Your account has been created successfully.\n\n" +
                                        "Please log in with your credentials.");
                    } else {
                        showAlert("Registration Failed", "Registration failed. Please try again.");
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
                    } else if (errorMessage != null && errorMessage.contains("400")) {
                        showAlert("Registration Failed", "Invalid data format. Please check your input.");
                    } else {
                        showAlert("Signup Error", "An error occurred during registration: " + errorMessage);
                    }
                });
            }
        }).start();
    }

    private void showAlertAndReturnToLogin(String title, String message) {
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

        // Show alert and wait for user to click OK
        alert.showAndWait().ifPresent(response -> {
            // After user clicks OK, return to login page
            if (onReturnToLogin != null) {
                onReturnToLogin.run();
            } else {
                // Fallback: navigate to login manually
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Get the current stage (signup window)
            Stage currentStage = (Stage) signUpButton.getScene().getWindow();

            // Set the new scene (replace signup with login)
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login - Creddit");
            currentStage.centerOnScreen();

            LoginController loginController = loader.getController();

            loginController.setOnLoginSuccess(user -> {
                currentStage.close();
                try {
                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("home-page.fxml"));
                    Parent root2 = loader2.load();

                    HomePageController homePageController = loader2.getController();
                    homePageController.InitData(user, null);

                    HelloApplication.startSession(user);

                    // Create the second scene
                    Scene scene2 = new Scene(root2);
                    // Get the current stage
                    Stage stage = (Stage)currentStage.getOwner();
                    // Set the new scene
                    stage.setScene(scene2);
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Unable to load login page: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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
        usernameField.clear();
        passwordField.clear();
        visiblePasswordField.clear();
        confirmPasswordField.clear();
        visibleConfirmPasswordField.clear();
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