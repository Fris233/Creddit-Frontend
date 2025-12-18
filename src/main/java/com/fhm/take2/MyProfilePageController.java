package com.fhm.take2;

import com.Client;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MyProfilePageController {

    @FXML private TextArea bioTextArea;
    @FXML private Button createSubcredditButton;
    @FXML private ImageView editIcon;
    @FXML private Button filterCommentsButton;
    @FXML private Button filterDownvotedButton;
    @FXML private Button filterHistoryButton;
    @FXML private Button filterPostsButton;
    @FXML private Button filterSavedButton;
    @FXML private Button filterUpvotedButton;
    @FXML private AnchorPane friendButtonAnchor;
    @FXML private AnchorPane friendRequestAnchor;
    @FXML private Button homePageButton;
    @FXML private Label lastOnlineLabel;
    @FXML private AnchorPane loggedInPane;
    @FXML private Button pendingFriendButton;
    @FXML private VBox postsContainer;
    @FXML private ScrollPane postsScrollPane;
    @FXML private ImageView profilePFP;
    @FXML private Label recentButton;
    @FXML private ImageView refreshButton;
    @FXML private Label registerDateLabel;
    @FXML private TextField searchField;
    @FXML private ImageView shareIcon;
    @FXML private Label subcredditCountLabel;
    @FXML private AnchorPane unfriendButtonAnchor;
    @FXML private ImageView userPFP;
    @FXML private Label usernameLabel;
    @FXML private Label usernameLabel1;

    private User currentUser;
    private ArrayList<Subcreddit> userSubcreddits;
    private ArrayList<PostPreviewTemplateController> postPreviewControllers;
    private String searchPrompt;
    private boolean filterPosts;
    private boolean filterComments;
    private boolean filterSaved;
    private boolean filterHistory;
    private boolean filterUpvoted;
    private boolean filterDownvoted;
    private boolean updating =  false;
    private boolean scrollCooldown = false;


    public void initData(User currentUser, String searchPrompt,  boolean filterPosts, boolean filterComments, boolean filterSaved, boolean filterHistory, boolean filterUpvoted, boolean filterDownvoted) throws Exception {
        this.currentUser = currentUser;
        this.searchPrompt = searchPrompt;
        this.filterPosts = filterPosts;
        this.filterComments = filterComments;
        this.filterSaved = filterSaved;
        this.filterHistory = filterHistory;
        this.filterUpvoted = filterUpvoted;
        this.filterDownvoted = filterDownvoted;

        this.searchField.setText(searchPrompt);
        postPreviewControllers = new ArrayList<>();

        if(filterPosts)
            filterPostsButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else if(filterComments)
            filterCommentsButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else if(filterSaved)
            filterSavedButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else if(filterHistory)
            filterHistoryButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else if(filterUpvoted)
            filterUpvotedButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");
        else if(filterDownvoted)
            filterDownvotedButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; -fx-background-radius: 20;");

        usernameLabel.setText(this.currentUser.getUsername());
        usernameLabel1.setText("u/" + this.currentUser.getUsername());
        registerDateLabel.setText(this.currentUser.getTimeCreated().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lastOnlineLabel.setText("now");
        try{
            this.userSubcreddits = Client.GetUserSubcreddits(this.currentUser);
            subcredditCountLabel.setText(String.valueOf(this.userSubcreddits.size()));
        }catch(Exception e){
            e.printStackTrace();
        }

        bioTextArea.setText(this.currentUser.getBio());
        bioTextArea.setEditable(false);

        try{
            if(filterPosts) {
                Map<Post, Integer> postFeed = Client.GetPostFeedFilterAuthor(this.currentUser, this.currentUser, searchPrompt, 0);
                for (Post post : postFeed.keySet()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                    Node postNode = loader.load();

                    PostPreviewTemplateController controller = loader.getController();
                    controller.init(post, this.currentUser, postFeed.get(post));

                    postsContainer.getChildren().add(postNode);
                    postPreviewControllers.add(controller);
                }
            } else if(filterComments) {
                //todo
            } else if(filterSaved) {
                //todo
            } else if(filterHistory) {
                //todo
            } else if(filterUpvoted) {
                ArrayList<Post> postFeed = Client.GetPostFeedFilterVote(this.currentUser, searchPrompt, 1, 0);
                for(Post post : postFeed) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                    Node postNode = loader.load();

                    PostPreviewTemplateController controller = loader.getController();
                    controller.init(post, this.currentUser, 1);

                    postsContainer.getChildren().add(postNode);
                    postPreviewControllers.add(controller);
                }
            } else if(filterDownvoted) {
                ArrayList<Post> postFeed = Client.GetPostFeedFilterVote(this.currentUser, searchPrompt, -1, 0);
                for(Post post : postFeed) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                    Node postNode = loader.load();

                    PostPreviewTemplateController controller = loader.getController();
                    controller.init(post, this.currentUser, -1);

                    postsContainer.getChildren().add(postNode);
                    postPreviewControllers.add(controller);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        // Setup scroll behavior copied from miho
        postsScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            postsScrollPane.setVvalue(postsScrollPane.getVvalue() - delta / postsScrollPane.getContent().getBoundsInLocal().getHeight());
        });

        postsScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if(!updating && !scrollCooldown && newVal.doubleValue() >= postsScrollPane.getVmax()) {
                updating = true;
                scrollCooldown = true;
                try {
                    if(filterPosts) {
                        Map<Post, Integer> postFeed = Client.GetPostFeedFilterAuthor(this.currentUser, this.currentUser, searchPrompt, postPreviewControllers.getLast().GetPostID());
                        for (Post post : postFeed.keySet()) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                            Node postNode = loader.load();
                            PostPreviewTemplateController controller = loader.getController();
                            controller.init(post, this.currentUser, postFeed.get(post));
                            postsContainer.getChildren().add(postNode);
                            postPreviewControllers.add(controller);
                        }
                    } else if(filterComments) {
                        //todo
                    } else if(filterSaved) {
                        //todo
                    } else if(filterHistory) {
                        //todo
                    } else if(filterUpvoted) {
                        ArrayList<Post> postFeed = Client.GetPostFeedFilterVote(this.currentUser, searchPrompt, 1, postPreviewControllers.getLast().GetPostID());
                        for(Post post : postFeed) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                            Node postNode = loader.load();

                            PostPreviewTemplateController controller = loader.getController();
                            controller.init(post, this.currentUser, 1);

                            postsContainer.getChildren().add(postNode);
                            postPreviewControllers.add(controller);
                        }
                    }else if(filterDownvoted) {
                        ArrayList<Post> postFeed = Client.GetPostFeedFilterVote(this.currentUser, searchPrompt, -1, postPreviewControllers.getLast().GetPostID());
                        for(Post post : postFeed) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("Post_Preview_Template.fxml"));
                            Node postNode = loader.load();

                            PostPreviewTemplateController controller = loader.getController();
                            controller.init(post, this.currentUser, -1);

                            postsContainer.getChildren().add(postNode);
                            postPreviewControllers.add(controller);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                updating = false;
                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.setOnFinished(e -> scrollCooldown = false);
                pause.play();
            }
        });

    }

    @FXML
    void EditProfile(MouseEvent event) {
        System.out.println("Edit Profile Page Pressed!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-profile-page.fxml"));
            Parent root = loader.load();

            EditProfilePageController editProfilePageController = loader.getController();
            editProfilePageController.initdata(this.currentUser);

            // Get the current stage
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
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
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(currentUser);

            // Get the current stage
            Stage stage = (Stage) postsContainer.getScene().getWindow();
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
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-subcreddit-page.fxml"));
            Parent root = loader.load();

            // Get the login controller
            CreateSubcredditPageController createSubcredditPageController = loader.getController();
            createSubcredditPageController.InitData(this.currentUser);

            // Create a new stage for login (dialog)
            Stage createSubcredditStage = new Stage();
            createSubcredditStage.setTitle("Create Subcreddit");
            createSubcredditStage.setScene(new Scene(root, 600, 400));
            createSubcredditStage.setResizable(false);

            // Set modality so it blocks interaction with homepage
            createSubcredditStage.initModality(Modality.WINDOW_MODAL);
            createSubcredditStage.initOwner(postsContainer.getScene().getWindow());

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
    void FilterPosts(MouseEvent event) {
        this.filterPosts = true;
        this.filterComments = false;
        this.filterSaved = false;
        this.filterHistory = false;
        this.filterUpvoted = false;
        this.filterDownvoted = false;
        Refresh();
        event.consume();
    }

    @FXML
    void FilterComments(MouseEvent event) {
        //todo
        event.consume();
    }

    @FXML
    void FilterSaved(MouseEvent event) {
        //todo
        event.consume();
    }

    @FXML
    void FilterHistory(MouseEvent event) {
        //todo
        event.consume();
    }

    @FXML
    void FilterUpvoted(MouseEvent event) {
        this.filterPosts = false;
        this.filterComments = false;
        this.filterSaved = false;
        this.filterHistory = false;
        this.filterUpvoted = true;
        this.filterDownvoted = false;
        Refresh();
        event.consume();
    }

    @FXML
    void FilterDownvoted(MouseEvent event) {
        this.filterPosts = false;
        this.filterComments = false;
        this.filterSaved = false;
        this.filterHistory = false;
        this.filterUpvoted = false;
        this.filterDownvoted = true;
        Refresh();
        event.consume();
    }

    //TODO show friends requests etc... meho here, just make it a new page (friends page)
    @FXML
    void Friends(MouseEvent event) {
        Clean();
        event.consume();
    }

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
    void ProfilePressed(MouseEvent event) {
        Refresh();
        event.consume();
    }

    @FXML
    void Refresh() {
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("my-profile-page.fxml"));
            Parent root = loader.load();

            MyProfilePageController myProfilePageController = loader.getController();
            myProfilePageController.initData(this.currentUser, searchField.getText(), this.filterPosts, this.filterComments, this.filterSaved, this.filterHistory, this.filterUpvoted, this.filterDownvoted);

            // Get the current stage
            Stage stage = (Stage) filterPostsButton.getScene().getWindow();
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
            stage.initOwner(postsContainer.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
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
}
