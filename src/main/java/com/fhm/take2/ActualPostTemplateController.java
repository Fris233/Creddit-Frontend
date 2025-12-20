package com.fhm.take2;

import com.Client;
import com.crdt.*;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class ActualPostTemplateController {

    @FXML private Button JoinButton;
    @FXML private Button MoreOptionsButton;
    @FXML private ImageView downvoteButton;
    @FXML private Label modLabel;
    @FXML private StackPane mediaAnchor;
    @FXML private StackPane commentMediaPane;
    @FXML private AnchorPane voteAnchor;
    @FXML private Label subName;
    @FXML private ImageView subPFP;
    @FXML private Label timeLabel;
    @FXML private Label titleLabel;
    @FXML private ImageView upvoteButton;
    @FXML private Label votesLabel;
    @FXML private Label commentsLabel;
    @FXML private Label posterName;
    @FXML private Label postDesc;
    @FXML private TextArea commentTextArea;
    @FXML private Button commentButton;
    @FXML private AnchorPane loggedInPane;
    @FXML private AnchorPane loggedOutPane;
    @FXML private AnchorPane commentAnchor;
    @FXML VBox postsContainer;
    @FXML private VBox recentPostsContainer;
    @FXML private ScrollPane recentScrollPane; //TODO SEX
    @FXML private ScrollPane postsScrollPane;
    @FXML private TextField searchField;
    @FXML private ImageView userPFP;
    @FXML private VBox moreOptionsVBox;
    @FXML private Button shareButton;
    @FXML private Button bookmarkButton;
    @FXML private Button reportButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Post post;
    private Comment comment;
    private User currentUser;
    private int myOGVote;
    private int myVote;
    private boolean subMember = false;
    private boolean modAuthor = false;

    MediaViewController mediaViewController;

    ArrayList<CommentTemplateController> parentCommentControllers = new ArrayList<>();
    ArrayList<CommentTemplateController> replyControllers = new ArrayList<>();

    private MediaViewController commentMediaViewController;
    private BooleanProperty validCommentInfo = new SimpleBooleanProperty(false);
    private boolean updating = false;
    private boolean scrollCooldown = false;
    private boolean moreOptioning = false;

    public void InitData(int postid, int commentid, User user) {
        this.currentUser = user;
        this.mediaViewController = null;
        try {
            Map<Post, Integer> postMap = Client.GetPost(postid, this.currentUser);
            for(Post p : postMap.keySet())
                this.post = p;
            myOGVote = postMap.get(this.post);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLoginUI();
        myVote = myOGVote;
        ColorVote();
        subName.setText(post.GetSubcreddit() != null? "cr/" + post.GetSubcreddit().GetSubName() : "");
        timeLabel.setText(PostPreviewTemplateController.timeAgo(post.GetTimeCreated()));
        titleLabel.setText(post.GetTitle());
        votesLabel.setText(String.valueOf(post.GetVotes()));
        commentsLabel.setText(String.valueOf(post.GetComments()));
       /* if(post.GetContent() == null)
            postDesc.setText("sex"); //TODO sex
        else*/
        postDesc.setText(post.GetContent());
        posterName.setText("u/" + post.GetAuthor().getUsername());
        if(post.GetMedia() != null && !post.GetMedia().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("media-view.fxml"));
                Node mediaNode = loader.load();

                mediaViewController = loader.getController();
                mediaViewController.init(post.GetMedia(), 0, null);

                mediaAnchor.getChildren().add(mediaNode);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
        if(currentUser == null)
            commentAnchor.setDisable(true);
        if(post.GetSubcreddit() != null) {
            JoinButton.setVisible(true);
            try {
                if(user != null)
                    subMember = Client.IsSubMember(this.currentUser, post.GetSubcreddit());
                modAuthor = Client.VerifyModeration(post.GetAuthor(), post.GetSubcreddit());
                if(modAuthor) {
                    modLabel.setVisible(true);
                }
                if(post.GetSubcreddit().GetCreator().equals(post.GetAuthor())) {
                    modLabel.setText("CREATOR");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if(post.GetSubcreddit().GetLogo() != null && !post.GetSubcreddit().GetLogo().GetURL().isBlank())
                subPFP.setImage(new Image(post.GetSubcreddit().GetLogo().GetURL(), true));
        }
        else {
            if(post.GetAuthor().getPfp() != null && !post.GetAuthor().getPfp().GetURL().isBlank())
                subPFP.setImage(new Image(post.GetAuthor().getPfp().GetURL(), true));
        }
        UpdateJoinButton();

        int[] lastLineCount = { 1 };
        commentTextArea.textProperty().addListener((obs, oldText, newText) -> {
            int lines = newText.split("\n", -1).length;

            if (lines != lastLineCount[0]) {
                lastLineCount[0] = lines;

                commentTextArea.applyCss();
                commentTextArea.layout();

                var content = commentTextArea.lookup(".content");
                if (content != null) {
                    double height = 47 + (32 * (Math.min(lines, 4)));
                    commentTextArea.setPrefHeight(height);
                }
            }
        });

        commentTextArea.textProperty().addListener((obs, oldText, newText) -> {
            validCommentInfo.set(!newText.isBlank());
        });

        validCommentInfo.addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                commentButton.setStyle("-fx-background-color: #115bca; -fx-text-fill: #ffffff; -fx-background-radius: 30;"); //button blue and pressable
                commentButton.setDisable(false);
            }
            else {
                commentButton.setStyle("-fx-background-color: #191c1e; -fx-text-fill: #525454; -fx-background-radius: 30;"); //button grayed out
                commentButton.setDisable(true);
            }
        });

        commentTextArea.setOnDragOver(event -> {
            if (event.getGestureSource() != commentTextArea && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // Handle dropping files
        commentTextArea.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                List<File> files = db.getFiles();
                files.forEach(this::AddFile);
            }
            event.setDropCompleted(success);
            event.consume();
        });

        try {
            CommentFeed commentFeed;
            if(commentid == 0)
                commentFeed = Client.GetPostCommentFeed(currentUser, this.post, 0);
            else
                commentFeed = Client.GetCommentFeed(currentUser, commentid);
            Map<Integer, Integer> votes = commentFeed.votes();
            int root = 0;
            for (Comment comment : commentFeed.parents()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                Node node = loader.load();
                CommentTemplateController commentTemplateController = loader.getController();
                commentTemplateController.Init(comment, currentUser, votes.getOrDefault(comment.getID(), 0), 0, this, root);
                postsContainer.getChildren().add(node);
                parentCommentControllers.add(commentTemplateController);
                Comment[] lv2 = commentFeed.lv2().get(comment.getID());
                for (Comment lv2_reply : lv2) {
                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                    Node node2 = loader2.load();
                    CommentTemplateController commentTemplateController2 = loader2.getController();
                    commentTemplateController2.Init(lv2_reply, currentUser, votes.getOrDefault(comment.getID(), 0), 1, this, root);
                    postsContainer.getChildren().add(node2);
                    replyControllers.add(commentTemplateController2);
                    Comment[] lv3 = commentFeed.lv3().get(lv2_reply.getID());
                    for (Comment lv3_reply : lv3) {
                        FXMLLoader loader3 = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                        Node node3 = loader3.load();
                        CommentTemplateController commentTemplateController3 = loader3.getController();
                        commentTemplateController3.Init(lv3_reply, currentUser, votes.getOrDefault(comment.getID(), 0), 2, this, root);
                        postsContainer.getChildren().add(node3);
                        replyControllers.add(commentTemplateController3);
                    }
                }
                root++;
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
                    CommentFeed commentFeed;
                    if(commentid == 0) {
                        commentFeed = Client.GetPostCommentFeed(currentUser, this.post, parentCommentControllers.getLast().GetCommentID());
                        Map<Integer, Integer> votes = commentFeed.votes();
                        int root = 0;
                        for (Comment comment : commentFeed.parents()) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                            Node node = loader.load();

                            CommentTemplateController commentTemplateController = loader.getController();
                            commentTemplateController.Init(comment, currentUser, votes.getOrDefault(comment.getID(), 0), 0, this, root);

                            postsContainer.getChildren().add(node);
                            parentCommentControllers.add(commentTemplateController);
                            // Set the new scene
                            Comment[] lv2 = commentFeed.lv2().get(comment.getID());
                            for (Comment lv2_reply : lv2) {

                                FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                                Node node2 = loader2.load();

                                CommentTemplateController commentTemplateController2 = loader2.getController();
                                commentTemplateController2.Init(lv2_reply, currentUser, votes.getOrDefault(comment.getID(), 0), 1, this, root);

                                postsContainer.getChildren().add(node2);
                                replyControllers.add(commentTemplateController2);

                                Comment[] lv3 = commentFeed.lv3().get(lv2_reply.getID());
                                for (Comment lv3_reply : lv3) {
                                    FXMLLoader loader3 = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                                    Node node3 = loader3.load();

                                    CommentTemplateController commentTemplateController3 = loader3.getController();
                                    commentTemplateController3.Init(lv3_reply, currentUser, votes.getOrDefault(comment.getID(), 0), 2, this, root);

                                    postsContainer.getChildren().add(node3);
                                    replyControllers.add(commentTemplateController3);
                                }
                            }
                            root++;
                        }
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

        commentTextArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                handlePaste();
                event.consume();
            }
        });

        postsScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY() * 2;
            postsScrollPane.setVvalue(postsScrollPane.getVvalue() - delta / postsScrollPane.getContent().getBoundsInLocal().getHeight());
        });

        moreOptionsVBox.setVisible(false);
        if(this.currentUser == null) {
            moreOptionsVBox.setDisable(true);
        }
        else {
            if (!this.currentUser.equals(this.post.GetAuthor())) {
                moreOptionsVBox.getChildren().remove(editButton);
                moreOptionsVBox.getChildren().remove(deleteButton);
            } else {
                moreOptionsVBox.getChildren().remove(reportButton);
                moreOptionsVBox.getChildren().remove(bookmarkButton);
            }
        }
    }

    private void handlePaste() {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        // Check if clipboard has files
        if (clipboard.hasFiles()) {
            for (java.io.File file : clipboard.getFiles()) {
                AddFile(file);
            }
        }
    }

    private void ColorVote() {
        switch (myVote) {
            case 1:
                voteAnchor.setStyle("-fx-background-color: #d93900; -fx-background-radius: 25;");
                upvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow_fill.png"))));
                downvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                break;
            case -1:
                voteAnchor.setStyle("-fx-background-color: #6a5cff; -fx-background-radius: 25;");
                upvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                downvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow_fill.png"))));
                break;
            default:
                voteAnchor.setStyle("-fx-background-color: black; -fx-background-radius: 25;");
                upvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                downvoteButton.setImage(new Image(String.valueOf(this.getClass().getResource("assets/vote_arrow.png"))));
                break;
        }
    }

    private void UpdateJoinButton() {
        if(subMember) {
            JoinButton.setText("Joined");
            JoinButton.setStyle("-fx-text-fill: #ffffff; -fx-border-color: gray; -fx-border-radius: 20; -fx-background-radius: 20");
        }
        else {
            JoinButton.setText("Join");
            JoinButton.setStyle("-fx-background-color: #115bca; -fx-text-fill: #ffffff; -fx-border-radius: 20; -fx-background-radius: 20");
        }
    }

    @FXML
    void Upvote(MouseEvent event) {
        if(currentUser == null) {
            System.out.println("Not logged in!");
            event.consume();
            return;
        }
        try {
            myVote = myVote != 1? 1 : 0;
            if(Client.Vote(currentUser, post, myVote)) {
                votesLabel.setText(String.valueOf(post.GetVotes() + (myVote - myOGVote)));
                ColorVote();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(moreOptioning)
            MoreOptionsButton();
        event.consume();
    }
    @FXML
    void Downvote(MouseEvent event) {
        if(currentUser == null) {
            System.out.println("Not logged in!");
            event.consume();
            return;
        }
        try {
            myVote = myVote != -1? -1 : 0;
            if(Client.Vote(currentUser, post, myVote)) {
                votesLabel.setText(String.valueOf(post.GetVotes() + (myVote - myOGVote)));
                ColorVote();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(moreOptioning)
            MoreOptionsButton();
        event.consume();
    }

    @FXML
    void JoinSubcreddit(MouseEvent event) {
        if(currentUser == null) {
            System.out.println("Not logged in!");
            event.consume();
            return;
        }
        if(subMember) {
            if (Client.LeaveSubcreddit(this.currentUser, post.GetSubcreddit()))
                subMember = false;
        }
        else {
            if (Client.JoinSubcreddit(this.currentUser, post.GetSubcreddit()))
                subMember = true;
        }
        UpdateJoinButton();
        event.consume();
    }

    @FXML
    void MoreOptionsButton() {
        moreOptioning = !moreOptioning;
        moreOptionsVBox.setVisible(moreOptioning);
    }

    @FXML
    void Share(MouseEvent event) {
        //TODO: share?
        System.out.println("Share clicked");
        event.consume();
    }

    @FXML
    void Bookmark(MouseEvent event) {
        //TODO: bookmark?
        System.out.println("Bookmark clicked");
        event.consume();
    }

    @FXML
    void Report(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }

        try {
            if(Client.ReportExists(new Report(0, this.currentUser, this.post, null, null, null, null))) {
                showAlert("Duplicate Report", "You have already submitted a report on this target!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-report-page.fxml"));
            Parent root = loader.load();
            CreateReportPageController reportPageController = loader.getController();
            Stage popup = new Stage();
            popup.setTitle("Report Page");
            popup.setScene(new Scene(root));
            popup.initModality(Modality.APPLICATION_MODAL);
            reportPageController.initData(this.currentUser, this.post, popup);
            popup.showAndWait();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void Edit(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(currentUser, this.post, null);

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
    void Delete(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Action");
        alert.setHeaderText("Delete post?");
        alert.setContentText("Are you sure you want to delete this post?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Client.DeletePost(this.post);
                GoHome();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MoreOptionsButton();
        }
        event.consume();
    }

    @FXML
    void OpenPoster(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-page.fxml"));
            Parent root = loader.load();

            UserProfilePageController userProfilePageController = loader.getController();
            userProfilePageController.InitData(post.GetAuthor().getId(), currentUser, "", true);

            // Get the current stage
            Stage stage = (Stage) JoinButton.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        event.consume();
    }

    @FXML
    void OpenSubcreddit(MouseEvent event) {
        if(mediaViewController != null)
            mediaViewController.Clean();
        if(post.GetSubcreddit() == null) {
            System.out.println("Open user profile");
        }
        else {
            System.out.println("Open Subcreddit Pressed!");
        }
        event.consume();
    }

    private void updateLoginUI() {
        if(currentUser != null) {
            // User is logged in
            loggedOutPane.setVisible(false);
            loggedInPane.setVisible(true);
            //userPFP.setImage(currentUser.getPfp());
        } else {
            // User is logged out
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
        if(moreOptioning)
            MoreOptionsButton();
        event.consume();
    }

    @FXML
    void CheckRules(MouseEvent event) {
        System.out.println("Rules Button Pressed");
        if(moreOptioning)
            MoreOptionsButton();
        event.consume();
    }

    @FXML
    void ClearRecentPosts(MouseEvent event) {
        System.out.println("Clear Recent Posts Button Pressed");
        if(moreOptioning)
            MoreOptionsButton();
        event.consume();
    }

    @FXML
    void CreatePost(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        System.out.println("Create Post Button Pressed");
        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-post-page.fxml"));
            Parent root = loader.load();

            CreatePostPageController createPostPageController = loader.getController();
            createPostPageController.InitData(currentUser, null, null);

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
        if (currentUser == null) {
            Login();
            return;
        }
        System.out.println("Create Subcreddit Button Pressed");
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
    void SendComment(MouseEvent event) {
        if(!validCommentInfo.get()) return;

        if(!Client.isServerReachable()) {
            new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
            return;
        }

        try {
            String content = commentTextArea.getText();

            String mediaUrl = "";
            MediaType mediaType = MediaType.NONE;

            Media media = null;
            if(commentMediaViewController != null) {
                File selectedFile = commentMediaViewController.GetFileArrayList().getFirst();
                String uploadResponse = Client.UploadFile(selectedFile);
                if (uploadResponse.isBlank()) {
                    new Alert(Alert.AlertType.ERROR, "Server unreachable! Check your connection and try again!").showAndWait();
                    return;
                }
                Map<?, ?> json = Client.GetResponse(uploadResponse);
                mediaUrl = (String) json.get("url");
                // Detect media type
                String mime = Files.probeContentType(selectedFile.toPath());
                if (mime != null) {
                    if (mime.startsWith("image/")) mediaType = MediaType.IMAGE;
                    else if (mime.startsWith("video/")) mediaType = MediaType.VIDEO;
                    else if (mime.startsWith("audio/")) mediaType = MediaType.AUDIO;
                    else mediaType = MediaType.OTHER;
                }
                media = new Media(mediaType, mediaUrl);
            }

            // Now send post JSON
            Comment comment = new Comment(0, this.post, this.currentUser, content, media, 0, 0, 0, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), false);
            int id = Client.CreateComment(comment);
            if (id > 0) {
                comment.setId(id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Comment uploaded successfully!");
                alert.showAndWait();
                if(commentMediaViewController != null) {
                    commentMediaViewController.Clean();
                    RemoveMediaPane();
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Comment_Template.fxml"));
                    Node node = loader.load();

                    CommentTemplateController commentTemplateController = loader.getController();
                    commentTemplateController.Init(comment, currentUser, 0, 0, this, 0);

                    // Get the current stage
                    parentCommentControllers.addFirst(commentTemplateController);
                    postsContainer.getChildren().addFirst(node);
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to send post!");
                alert.showAndWait();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }
        if(moreOptioning)
            MoreOptionsButton();
        event.consume();
    }

    @FXML
    void AttachMedia(MouseEvent event) {
        if(moreOptioning)
            MoreOptionsButton();
        Window window = ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(window);
        if(file == null)
            return;
        AddFile(file);

        event.consume();
    }

    private void AddFile(File file) {
        if(moreOptioning)
            MoreOptionsButton();
        if(commentMediaViewController != null) {
            RemoveMediaPane();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("media-view.fxml"));
            Node mediaNode = loader.load();
            commentMediaViewController = loader.getController();
            commentMediaViewController.init(null, 0, new ArrayList<>());
            commentMediaPane.getChildren().add(mediaNode);
            commentMediaViewController.done.addListener((obs, oldVal, newVal) -> {
                if (newVal)
                    RemoveMediaPane();
            });
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        commentMediaViewController.AddMedia(file);
    }

    private void RemoveMediaPane() {
        commentMediaPane.getChildren().clear();
        if(commentMediaViewController != null)
            commentMediaViewController.Clean();
        commentMediaViewController = null;
    }

    @FXML
    void Login() {
        System.out.println("Login Button Pressed");
        if(moreOptioning)
            MoreOptionsButton();
        navigateToLoginDialog();
    }

    // In the navigateToLoginDialog method, update the stage title:
    private void navigateToLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Get the login controller
            LoginController loginController = loader.getController();

            // Create a new stage for login (dialog)
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root, 400, 500));
            loginStage.setResizable(false);

            // Set modality so it blocks interaction with homepage
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(postsContainer.getScene().getWindow());

            // Set up callback for successful login
            loginController.setOnLoginSuccess(user -> {
                this.currentUser = user;
                updateLoginUI();
                loginStage.close();
                HelloApplication.startSession(currentUser);
                // Refresh the page to show user-specific content
                Refresh();
            });

            loginStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Unable to load login page: " + e.getMessage());
        }
    }

    @FXML
    void ProfilePressed(MouseEvent event) {
        if (currentUser == null) {
            Login();
            return;
        }
        System.out.println("Profile Button Pressed");
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
    void GoHome() {

        Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-page.fxml"));
            Parent root = loader.load();

            HomePageController homePageController = loader.getController();
            homePageController.InitData(currentUser, "", 0);

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
    }

    @FXML
    void Refresh() {
        Clean();
        if(mediaViewController != null)
            mediaViewController.Clean();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ActualPost_Template.fxml"));
            Parent root = loader.load();

            ActualPostTemplateController actualPostTemplateController = loader.getController();
            actualPostTemplateController.InitData(post.GetID(), this.comment.getID(), currentUser);

            // Get the current stage
            Stage stage = (Stage) JoinButton.getScene().getWindow();
            // Set the new scene
            stage.setScene(new Scene(root));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    void SearchPressed(KeyEvent event) {
        System.out.println("Search Pressed");
        Clean();
        event.consume();
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

        alert.showAndWait();
    }

    private void Clean() {
        Client.THREAD_POOL.submit(() -> {
            if(mediaViewController != null)
                mediaViewController.Clean();
            if(commentMediaViewController != null)
                commentMediaViewController.Clean();
            if(parentCommentControllers != null && !parentCommentControllers.isEmpty())
                for(CommentTemplateController controller : parentCommentControllers)
                    controller.Clean();
            if(replyControllers != null && !replyControllers.isEmpty())
                for(CommentTemplateController controller : replyControllers)
                    controller.Clean();
        });
    }
}

