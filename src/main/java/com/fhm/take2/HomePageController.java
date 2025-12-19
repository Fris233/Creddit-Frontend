package com.fhm.take2;

import com.Client;
import com.crdt.Admin;
import com.crdt.Post;
import com.crdt.Subcreddit;
import com.crdt.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class HomePageController {

    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private VBox postsContainer;
    @FXML private VBox recentPostsContainer;
    @FXML private ScrollPane recentScrollPane;
    @FXML private ScrollPane postsScrollPane;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;
    @FXML private HBox filterHBox;
    @FXML private Button filterComments;
    @FXML private Button filterPosts;
    @FXML private Button filterSubcreddits;
    @FXML private Button filterUsers;
    @FXML private AnchorPane reportsAnchor;
    @FXML private AnchorPane analyticsAnchor;

    private User currentUser;
    private ArrayList<PostPreviewTemplateController> postPreviewControllers;
    private ArrayList<ViewMiniSubcredditControllet> viewMiniSubcredditControllets;
    private boolean updating = false;
    private boolean scrollCooldown = false;
    private int filter; // 0 for posts, 1 for subcreddits, 2 for comments, 3 for users

    public void InitData(User user, String searchPrompt, int filter) {
        currentUser = user;
        this.filter = filter;
        this.searchField.setText(searchPrompt);
        if(currentUser != null && currentUser.getPfp() != null && !currentUser.getPfp().GetURL().isBlank())
            userPFP.setImage(new Image(currentUser.getPfp().GetURL(), true));
        postPreviewControllers = new ArrayList<>();

        if(user instanceof Admin) {
            System.out.println("Admin detected");
            reportsAnchor.setVisible(true);
            analyticsAnchor.setVisible(true);
        }
        else {
            reportsAnchor.setVisible(false);
            analyticsAnchor.setVisible(false);
        }

        updateLoginUI();

        postsScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            postsScrollPane.setVvalue(postsScrollPane.getVvalue() - delta / postsScrollPane.getContent().getBoundsInLocal().getHeight());
        });

        postsScrollPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) event.consume();
            if (event.getCode() == KeyCode.TAB) event.consume();
        });

        if(this.filter == 0){
            filterHBox.setVisible(!searchPrompt.isBlank());

            try {
                Map<Post, Integer> postFeed = Client.GetPostFeed(currentUser, searchPrompt, 0);
                for (Post post : postFeed.keySet()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                    Node postNode = loader.load();
                    PostPreviewTemplateController controller = loader.getController();
                    controller.init(post, user, postFeed.get(post));
                    postsContainer.getChildren().add(postNode);
                    postPreviewControllers.add(controller);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            postsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if(!updating && !scrollCooldown && newVal.doubleValue() >= postsScrollPane.getVmax()) {
                    updating = true;
                    scrollCooldown = true;
                    try {
                        Map<Post, Integer> postFeed = Client.GetPostFeed(currentUser, searchPrompt, postPreviewControllers.getLast().GetPostID());
                        for (Post post : postFeed.keySet()) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                            Node postNode = loader.load();
                            PostPreviewTemplateController controller = loader.getController();
                            controller.init(post, user, postFeed.get(post));
                            postsContainer.getChildren().add(postNode);
                            postPreviewControllers.add(controller);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    updating = false;
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> scrollCooldown = false);
                    pause.play();
                }
            });

        } else if(this.filter == 1){
            filterHBox.setVisible(true);

            try {
                ArrayList<Subcreddit> subFeed = Client.GetSubFeed(currentUser, searchPrompt, 0);
                for (Subcreddit sub : subFeed) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("view-mini-subcreddit.fxml"));
                    Node subcredditNode = loader.load();
                    ViewMiniSubcredditControllet controller = loader.getController();
                    controller.initData(sub, currentUser);
                    postsContainer.getChildren().add(subcredditNode);
                    viewMiniSubcredditControllets.add(controller);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            postsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if(!updating && !scrollCooldown && newVal.doubleValue() >= postsScrollPane.getVmax()) {
                    updating = true;
                    scrollCooldown = true;
                    try {
                        ArrayList<Subcreddit> subFeed = Client.GetSubFeed(currentUser, searchPrompt, viewMiniSubcredditControllets.getLast().getSubID());
                        for (Subcreddit sub : subFeed) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("view-mini-subcreddit.fxml"));
                            Node subcredditNode = loader.load();
                            ViewMiniSubcredditControllet controller = loader.getController();
                            controller.initData(sub, currentUser);
                            postsContainer.getChildren().add(subcredditNode);
                            viewMiniSubcredditControllets.add(controller);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    updating = false;
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> scrollCooldown = false);
                    pause.play();
                }
            });
        }

    }

    private void updateLoginUI() {
        if(currentUser != null) {
            loggedOutPane.setVisible(false);
            loggedInPane.setVisible(true);
        } else {
            loggedOutPane.setVisible(true);
            loggedInPane.setVisible(false);
            recentScrollPane.setVisible(false);
        }
    }

    @FXML
    void Chat(MouseEvent event) {
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
    void CheckAnalytics(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("analytics.fxml"));
            Parent root = fxmlLoader.load();
            AnalyticsController analyticsController = fxmlLoader.getController();
            analyticsController.Init(this.currentUser);
            Stage stage = new Stage();
            stage.setTitle("Analytics");
            stage.setScene(new Scene(root, 400, 280));
            stage.setMinWidth(400);
            stage.setMinHeight(280);
            stage.initOwner(postsContainer.getScene().getWindow());
            stage.showAndWait();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void CheckReports(MouseEvent event) {
        event.consume();
    }

    @FXML
    void ClearRecentPosts(MouseEvent event) {
        System.out.println("Clear Recent Posts Button Pressed");
        event.consume();
    }

    @FXML
    void CreatePost(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        Clean();
        try {
            if(!Client.isServerReachable()) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("error_404.fxml"));
                Parent root = fxmlLoader.load();

                Error404Controller error404Controller = fxmlLoader.getController();
                error404Controller.refreshButton.setOnAction(e -> {
                    if(!Client.isServerReachable()) {
                        return;
                    }
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
                    Parent root2 = null;
                    try {
                        root2 = loader.load();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    CreatePostPageController createPostPageController = loader.getController();
                    createPostPageController.InitData(currentUser, null, null);

                    Stage stage = (Stage) error404Controller.refreshButton.getScene().getWindow();

                    stage.setScene(new Scene(root2));
                });

                Stage stage = (Stage) postsContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
            else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
                Parent root = loader.load();

                CreatePostPageController createPostPageController = loader.getController();
                createPostPageController.InitData(currentUser, null,null);

                Stage stage = (Stage) postsContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void CreateSubcreddit(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-subcreddit-page.fxml"));
            Parent root = loader.load();

            CreateSubcredditPageController createSubcredditPageController = loader.getController();
            createSubcredditPageController.InitData(this.currentUser);

            Stage createSubcredditStage = new Stage();
            createSubcredditStage.setTitle("Create Subcreddit");
            createSubcredditStage.setScene(new Scene(root, 600, 400));
            createSubcredditStage.setResizable(false);
            createSubcredditStage.initModality(Modality.WINDOW_MODAL);
            createSubcredditStage.initOwner(postsContainer.getScene().getWindow());

            createSubcredditPageController.setOnCreationSuccess(sub -> {
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
    void Login() {
        navigateToLoginDialog();
    }

    private void navigateToLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();

            Stage loginStage = new Stage();
            loginStage.setTitle("Login"); // Changed to Creddit
            loginStage.setScene(new Scene(root, 400, 500));
            loginStage.setResizable(false);

            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(postsContainer.getScene().getWindow());

            loginController.setOnLoginSuccess(user -> {
                this.currentUser = user;
                updateLoginUI();
                loginStage.close();
                HelloApplication.startSession(currentUser);
                Refresh();
            });

            loginStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        System.out.println("My Profile Button Pressed");
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("my-profile-page.fxml"));
            Parent root = loader.load();

            MyProfilePageController myProfilePageController = loader.getController();
            myProfilePageController.initData(this.currentUser, "", true, false, false, false, false, false);

            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void Refresh() {
        Clean();
        String searchPrompt = searchField.getText();
        if (searchPrompt != null && searchPrompt.matches("^creddit/post/\\d+$")) {
            String[] strings = searchPrompt.split("/");
            int id = Integer.parseInt(strings[strings.length - 1]);
            Clean();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ActualPost_Template.fxml"));
                Parent root = loader.load();

                ActualPostTemplateController actualPostTemplateController = loader.getController();
                actualPostTemplateController.InitData(id, 0, currentUser);

                Stage stage = (Stage) postsContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        else if (searchPrompt != null && searchPrompt.matches("^creddit/comment/\\d+$")) {

        }
        else if (searchPrompt != null && searchPrompt.matches("^creddit/user/\\d+$")) {
            String[] strings = searchPrompt.split("/");
            int id = Integer.parseInt(strings[strings.length - 1]);
            Clean();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-page.fxml"));
                Parent root = loader.load();

                UserProfilePageController userProfilePageController = loader.getController();
                userProfilePageController.InitData(id, currentUser, searchField.getText(), true);

                // Get the current stage
                Stage stage = (Stage) postsContainer.getScene().getWindow();
                // Set the new scene
                stage.setScene(new Scene(root));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        else if (searchPrompt != null && searchPrompt.matches("^creddit/subcreddit/\\d+$")) {
            String[] strings = searchPrompt.split("/");
            int id = Integer.parseInt(strings[strings.length - 1]);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("subcreddit-page.fxml"));
                Parent root = loader.load();

                SubcredditController controller = loader.getController();
                controller.InitData(id, searchPrompt, currentUser);

                Stage stage = (Stage) postsContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
                Parent root = loader.load();

                HomePageController homePageController = loader.getController();
                homePageController.InitData(currentUser, searchPrompt, filter);

                Stage stage = (Stage) postsContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void GoHome(MouseEvent event) {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, "", 0);

            Scene scene2 = new Scene(root);
            Stage stage = (Stage)postsContainer.getScene().getWindow();
            stage.setScene(scene2);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            Refresh();
            event.consume();
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0E1113;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    private void Clean() {
        Client.THREAD_POOL.submit(() -> {
            if (postPreviewControllers != null) {
                for(PostPreviewTemplateController controller : postPreviewControllers) {
                    if(controller != null && controller.mediaViewController != null) {
                        controller.mediaViewController.Clean();
                    }
                }
            }
        });
    }

    public void goToSubcreddit(Subcreddit subcreddit) {
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

    @FXML
    public void onFilterPostPressed(MouseEvent event) {
        filter = 0;
        Refresh();
    }

    @FXML
    public void onFilterSubCredditPressed(MouseEvent event) {
        filter = 1;
        Refresh();
    }
}