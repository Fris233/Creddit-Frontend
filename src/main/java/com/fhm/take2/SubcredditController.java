package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SubcredditController {

    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private VBox postsContainer;
    @FXML private ScrollPane postsScrollPane;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;

    @FXML private Text subcredditName;
    @FXML private Button joinButton;
    @FXML private TextArea descriptionText;
    @FXML private Text memberCount;
    @FXML private Text onlineCount;
    @FXML private Text createdDate;
    @FXML private Text privacyStatus;
    @FXML private VBox moderatorsList;
    @FXML private Button hotButton;
    @FXML private Button newButton;
    @FXML private Button topButton;

    private User currentUser;
    private Subcreddit currentSubcreddit;
    private ArrayList<PostPreviewTemplateController> postPreviewControllers;
    private boolean updating = false;
    private boolean scrollCooldown = false;
    private boolean isMember = false;
    private int lastPostId = 0;

    public void InitData(int subID, String searchPrompt, User user) {
        this.currentUser = user;
        this.searchField.setText(searchPrompt);
        try {
            this.currentSubcreddit = Client.GetSubcreddit(subID);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.postPreviewControllers = new ArrayList<>();

        updateLoginUI();

        updateSubcredditUI();

        checkMemberStatus();

        loadModerators();

        loadPosts();

        setupScrollBehavior();
    }

    private void updateSubcredditUI() {
        if (currentSubcreddit == null) return;

        subcredditName.setText("cr/" + currentSubcreddit.GetSubName());

        descriptionText.setText(currentSubcreddit.GetDescription());

        createdDate.setText("Created: " + currentSubcreddit.GetTimecreated().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        privacyStatus.setText("Privacy: " + (currentSubcreddit.GetPrivate() ? "Private" : "Public"));

        updateJoinButton();

        memberCount.setText("1234"); //dummy
        onlineCount.setText("125"); //dummy
    }

    private void checkMemberStatus() {
        if (currentUser == null || currentSubcreddit == null) {
            isMember = false;
            return;
        }

        try {
            isMember = Client.IsSubMember(currentUser, currentSubcreddit);
        } catch (Exception e) {
            System.err.println("Error checking member status: " + e.getMessage());
            isMember = false;
        }
    }

    private void loadModerators() {
        moderatorsList.getChildren().clear();

        if (currentSubcreddit == null) return;

        try {
            // Add creator as moderator
            User creator = currentSubcreddit.GetCreator();
            if (creator != null) {
                HBox creatorRow = new HBox(10.0);
                creatorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Circle creatorIcon = new Circle(5.0, Color.web("#0079d3"));

                Text creatorName = new Text("u/" + creator.getUsername() + " (Creator)");
                creatorName.setFill(Color.web("#d7dadc"));
                creatorName.setStyle("-fx-font-size: 14px;");

                creatorRow.getChildren().addAll(creatorIcon, creatorName);
                moderatorsList.getChildren().add(creatorRow);
            }
        } catch (Exception e) {
            System.err.println("Error loading moderators: " + e.getMessage());
        }
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        postPreviewControllers.clear();
        lastPostId = 0;

        if (currentSubcreddit == null) return;

        try {
            Map<Post, Integer> postFeed = Client.GetPostFeedFilterSub(
                    currentUser,
                    currentSubcreddit,
                    "",
                    lastPostId
            );

            for (Map.Entry<Post, Integer> entry : postFeed.entrySet()) {
                Post post = entry.getKey();
                int vote = entry.getValue();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                Node postNode = loader.load();

                PostPreviewTemplateController controller = loader.getController();
                controller.init(post, currentUser, vote);

                postsContainer.getChildren().add(postNode);
                postPreviewControllers.add(controller);

                if (post.GetID() > lastPostId) {
                    lastPostId = post.GetID();
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading posts: " + e.getMessage());
            // Show error message
            Text errorText = new Text("Failed to load posts. Please try again.");
            errorText.setFill(Color.WHITE);
            errorText.setStyle("-fx-font-size: 14px;");
            postsContainer.getChildren().add(errorText);
        }
    }

    private void loadMorePosts() {
        if (currentSubcreddit == null || updating) return;

        try {
            Map<Post, Integer> postFeed = Client.GetPostFeedFilterSub(
                    currentUser,
                    currentSubcreddit,
                    "",
                    lastPostId
            );

            for (Map.Entry<Post, Integer> entry : postFeed.entrySet()) {
                Post post = entry.getKey();
                int vote = entry.getValue();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                Node postNode = loader.load();

                PostPreviewTemplateController controller = loader.getController();
                controller.init(post, currentUser, vote);

                postsContainer.getChildren().add(postNode);
                postPreviewControllers.add(controller);

                if (post.GetID() > lastPostId) {
                    lastPostId = post.GetID();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading more posts: " + e.getMessage());
        }
    }

    private void setupScrollBehavior() {
        postsScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            postsScrollPane.setVvalue(postsScrollPane.getVvalue() - delta / postsScrollPane.getContent().getBoundsInLocal().getHeight());
        });

        postsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (!updating && !scrollCooldown && newVal.doubleValue() >= 0.95 &&
                    !postPreviewControllers.isEmpty()) {
                updating = true;
                scrollCooldown = true;

                loadMorePosts();
                updating = false;

                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> scrollCooldown = false);
                pause.play();
            }
        });

        postsScrollPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) event.consume();
            if (event.getCode() == KeyCode.TAB) event.consume();
        });
    }

    private void updateLoginUI() {
        if (currentUser != null) {
            loggedOutPane.setVisible(false);
            loggedInPane.setVisible(true);
        } else {
            loggedOutPane.setVisible(true);
            loggedInPane.setVisible(false);
        }
    }

    private void updateJoinButton() {
        if (isMember) {
            joinButton.setText("Joined");
            joinButton.setStyle("-fx-background-color: #2C3539; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            joinButton.setText("Join");
            joinButton.setStyle("-fx-background-color: #0079d3; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

//    private void updateSortButtons() {
//        hotButton.setStyle("-fx-background-color: " + ("hot".equals(currentSort) ? "#0079d3" : "#2C3539") + "; -fx-text-fill: white;");
//        newButton.setStyle("-fx-background-color: " + ("new".equals(currentSort) ? "#0079d3" : "#2C3539") + "; -fx-text-fill: white;");
//        topButton.setStyle("-fx-background-color: " + ("top".equals(currentSort) ? "#0079d3" : "#2C3539") + "; -fx-text-fill: white;");
//    }

    @FXML
    void GoHome(MouseEvent event) {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, null, 0);

            // Create the second scene
            Scene scene2 = new Scene(root);
            // Get the current stage
            Stage stage = (Stage)postsContainer.getScene().getWindow();
            // Set the new scene
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void createSubcreddit(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-subcreddit-page.fxml"));
            Parent root = loader.load();

            CreateSubcredditPageController controller = loader.getController();
            controller.InitData(this.currentUser);

            Stage createSubcredditStage = new Stage();
            createSubcredditStage.setTitle("Create Subcreddit");
            createSubcredditStage.setScene(new Scene(root, 600, 400));
            createSubcredditStage.setResizable(false);
            createSubcredditStage.initModality(Modality.WINDOW_MODAL);
            createSubcredditStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            controller.setOnCreationSuccess(sub -> {
                createSubcredditStage.close();
                goToSubcreddit(sub);
            });

            createSubcredditStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void checkRules(MouseEvent event) {
        Alert rulesAlert = new Alert(Alert.AlertType.INFORMATION);
        rulesAlert.setTitle("Creddit Rules");
        rulesAlert.setHeaderText("Community Guidelines");
        rulesAlert.setContentText("1. Be respectful\n2. No spam\n3. Follow content policies\n4. No harassment\n5. Keep discussions civil");
        styleAlert(rulesAlert);
        rulesAlert.showAndWait();
        event.consume();
    }

    @FXML
    void searchPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // Search within this subcreddit
            String searchTerm = searchField.getText();
            if (!searchTerm.isEmpty()) {
                searchInSubcreddit(searchTerm);
            }
        }
    }

    private void searchInSubcreddit(String searchTerm) {
        Clean();
        try {
            showAlert("Search", "Searching in cr/" + currentSubcreddit.GetSubName() + " for: " + searchTerm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Refresh() {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subcreddit-page.fxml"));
            Parent root = loader.load();

            SubcredditController controller = loader.getController();
            controller.InitData(currentSubcreddit.GetSubId(), this.searchField.getText(), currentUser);

            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();

            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root, 400, 500));
            loginStage.setResizable(false);
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(postsContainer.getScene().getWindow());

            loginController.setOnLoginSuccess(user -> {
                this.currentUser = user;
                updateLoginUI();
                loginStage.close();
                Refresh();
            });

            loginStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Login() {
        navigateToLoginDialog();
    }

    @FXML
    void createPost(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }

        Clean();
        try {
            if (!Client.isServerReachable()) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("error_404.fxml"));
                Parent root = fxmlLoader.load();

                Error404Controller error404Controller = fxmlLoader.getController();
                error404Controller.refreshButton.setOnAction(e -> {
                    if (!Client.isServerReachable()) {
                        return;
                    }
                    openCreatePostPage();
                });

                Stage stage = (Stage) postsContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                openCreatePostPage();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    private void openCreatePostPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(currentUser, null, currentSubcreddit);

            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void profilePressed(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }

        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("my-profile-page.fxml"));
            Parent root = loader.load();

            MyProfilePageController myProfilePageController = loader.getController();
            myProfilePageController.initData(this.currentUser, "", true, false, false, false, false, false);

            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void chat(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("message.fxml"));
            Parent root = fxmlLoader.load();
            MessageController messageController = fxmlLoader.getController();
            messageController.Init(this.currentUser);
            Stage stage = new Stage();
            stage.setTitle("Chats");
            stage.setScene(new Scene(root, 800, 600));
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.initOwner(postsContainer.getScene().getWindow());
            stage.showAndWait();
            messageController.Clean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void toggleJoinSubcreddit(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }

        try {
            if (isMember) {
                // Leave subcreddit
                boolean success = Client.LeaveSubcreddit(currentUser, currentSubcreddit);

                if (success) {
                    isMember = false;
                    showAlert("Left", "You've left c/" + currentSubcreddit.GetSubName());
                    updateJoinButton();
                } else {
                    showAlert("Error", "Failed to leave subcreddit. Please try again.");
                }
            } else {
                // Join subcreddit
                boolean success = Client.JoinSubcreddit(currentUser, currentSubcreddit);

                if (success) {
                    isMember = true;
                    showAlert("Joined", "You've joined c/" + currentSubcreddit.GetSubName() + "!");
                    updateJoinButton();
                } else {
                    showAlert("Error", "Failed to join subcreddit. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error toggling join status: " + e.getMessage());
            showAlert("Error", "Failed to update join status. Please try again.");
        }
        event.consume();
    }

    @FXML
    void showSubcredditRules(MouseEvent event) {
        if (currentSubcreddit == null) return;

        Alert rulesAlert = new Alert(Alert.AlertType.INFORMATION);
        rulesAlert.setTitle("c/" + currentSubcreddit.GetSubName() + " Rules");
        rulesAlert.setHeaderText("Subcreddit Rules");
        String defaultRules = "1. Be respectful and civil\n" +
                "2. No spam or self-promotion\n" +
                "3. Stay on topic\n" +
                "4. No personal attacks\n" +
                "5. Follow Creddit's content policy";

        rulesAlert.setContentText(defaultRules);
        styleAlert(rulesAlert);
        rulesAlert.showAndWait();
        event.consume();
    }

    private void goToSubcreddit(Subcreddit subcreddit) {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subcreddit-page.fxml"));
            Parent root = loader.load();

            SubcredditController controller = loader.getController();
            controller.InitData(subcreddit.GetSubId(), "", currentUser);

            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
    }

    private void Clean() {
        if (postPreviewControllers != null) {
            for (PostPreviewTemplateController controller : postPreviewControllers) {
                if (controller != null && controller.mediaViewController != null) {
                    controller.mediaViewController.Clean();
                }
            }
        }
    }
}