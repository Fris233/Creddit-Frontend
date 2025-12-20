package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class EditProfilePageController {

    @FXML private Button applyButton;
    @FXML private TextArea bioTextArea;
    @FXML private TextArea bio;
    @FXML private Button cancelButton;
    @FXML private ImageView editIcon;
    @FXML private Label errorLabel;
    @FXML private Label lastOnlineLabel;
    @FXML private AnchorPane loggedInPane;
    @FXML private TextField passwordTextField;
    @FXML private Label registerDateLabel;
    @FXML private TextField searchField;
    @FXML private ImageView shareIcon;
    @FXML private Label subcredditCountLabel;
    @FXML private ImageView userPFP;
    @FXML private Label usernameLabel;
    @FXML private TextField usernameTextField;

    User user;
    File pfp;

    public void initdata(User user) throws Exception {
        this.user = user;
        /*if(user.getPfp() != null && !user.getPfp().GetURL().isBlank())
            userPFP.setImage(new Image(user.getPfp().GetURL(), true));*/
        usernameTextField.setText(user.getUsername());
        passwordTextField.setText(user.getPassword());
        bioTextArea.setText(user.getBio());
        usernameLabel.setText(user.getUsername());
        registerDateLabel.setText(this.user.getTimeCreated().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        subcredditCountLabel.setText(String.valueOf(Client.GetUserSubcreddits(user).size()));
        lastOnlineLabel.setText("now");
        bio.setEditable(false);
        bio.setText(user.getBio());
    }


    @FXML
    void ApplyEdits(MouseEvent event) throws Exception {
        //todo check data validity
        /*if(this.pfp != null)
            user.setPFP(new Media(MediaType.IMAGE, Client.UploadFile(this.pfp)));*/
        user.setUsername(this.usernameTextField.getText());
        user.setPassword(this.passwordTextField.getText());
        user.setBio(this.bioTextArea.getText());
        if(Client.editUser(this.user)){
            showAlert("Edits Applied", "Edits Applied Successfully");
        }
        else
            showAlert("Edits Not Applied", "Edits Were not Applied");
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
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
            this.pfp = file;
            this.userPFP.setImage(new Image(this.pfp.toURI().toString()));
        }
        event.consume();
    }

    @FXML
    void Cancel(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("my-profile-page.fxml"));
            Parent root = loader.load();

            MyProfilePageController myProfilePageController = loader.getController();
            myProfilePageController.initData(user, "", true, false, false, false, false, false);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)usernameLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void Chat(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("message.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Chats");
            stage.setScene(new Scene(root, 800, 600));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.initOwner(usernameLabel.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed");
        event.consume();
        //todo
    }

    @FXML
    void CreatePost(MouseEvent event) {
        System.out.println("Create Post Button Pressed");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(user, null, null);

            // Get the current stage
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        System.out.println("Create Subcreddit Button Pressed");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-subcreddit-page.fxml"));
            Parent root = loader.load();

            // Get the login controller
            CreateSubcredditPageController createSubcredditPageController = loader.getController();
            createSubcredditPageController.InitData(this.user);

            // Create a new stage for login (dialog)
            Stage createSubcredditStage = new Stage();
            createSubcredditStage.setTitle("Create Subcreddit");
            createSubcredditStage.setScene(new Scene(root, 600, 400));
            createSubcredditStage.setResizable(false);

            // Set modality so it blocks interaction with homepage
            createSubcredditStage.initModality(Modality.WINDOW_MODAL);
            createSubcredditStage.initOwner(usernameLabel.getScene().getWindow());

            // Set up callback for successful login
            createSubcredditPageController.setOnCreationSuccess(sub -> {
                createSubcredditStage.close();
                //TODO: Open Subcreddit Page here
            });

            createSubcredditStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void GoHome(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(user, "", 0);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)usernameLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        System.out.println("My Profile Button Pressed");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("my-profile-page.fxml"));
            Parent root = loader.load();

            MyProfilePageController myProfilePageController = loader.getController();
            myProfilePageController.initData(this.user, "", true, false, false, false, false, false);

            // Get the current stage
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void Refresh() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-profile-page.fxml"));
            Parent root = loader.load();

            EditProfilePageController editProfilePageController = loader.getController();
            editProfilePageController.initdata(this.user);

            // Get the current stage
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            Refresh();
            event.consume();
        }
    }

    @FXML
    void Share(MouseEvent event) {
        //todo
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
}
