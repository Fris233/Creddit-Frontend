package com.fhm.take2;

import com.crdt.Media;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MediaViewController {

    @FXML private ImageView mediaImage;
    @FXML private Label mediaIndexLabel;
    @FXML private AnchorPane mediaPane;
    @FXML private StackPane stackPane;
    @FXML private HBox topHBox;
    @FXML private Label mediaTimeLabel;
    @FXML private MediaView mediaViewer;
    @FXML private Slider progressSlider;
    @FXML private Label unknownMediaLabel;
    @FXML private Slider volumeSlider;
    @FXML private Button nextButton;
    @FXML private Button prevButton;
    @FXML private Button removeButton;

    private ArrayList<Media> mediaArrayList = null;
    private ArrayList<File> fileArrayList = null;
    private int currentMediaIndex = 0;

    private MediaPlayer mp;
    private boolean isSeeking = false;
    private BooleanProperty paused = new SimpleBooleanProperty(true);
    private boolean isAudio = false;
    private int mode = 0; //0 for normal viewing, 1 for normal creating, 2 for editing
    BooleanProperty done = new SimpleBooleanProperty(false);

    private final int prevOrigX = 0;
    private final int nextOrigX = 798;

    private final int origWidth = 765;
    private final int origHeight = 380;

    Map<Integer, Image> images;
    Map<Integer, MediaPlayer> mediaPlayers;

    public void init(ArrayList<Media> mediaArrayList, int mode, ArrayList<File> fileArrayList) {
        if(mode != 1)
            this.mediaArrayList = mediaArrayList;
        images = new HashMap<>();
        mediaPlayers = new HashMap<>();
        this.currentMediaIndex = 0;
        this.mode = mode;
        if(mode != 0)
            this.fileArrayList = fileArrayList;
        else
            removeButton.setVisible(false);

        if(mode == 0) {
            PrevNextButtons();
            DisplayMedia();
        }

        mediaPane.setOnMouseClicked(e -> mediaPane.requestFocus());

        volumeSlider.setMin(0);
        volumeSlider.setMax(100);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mp != null)
                mp.setVolume(newVal.doubleValue()/100.0);
        });

        paused.addListener((obs, oldVal, newVal) -> {
            if(isAudio) {
                if (!newVal)
                    mediaImage.setImage(new Image(getClass().getResource("/com/fhm/take2/assets/audio_default.gif").toExternalForm()));
                else
                    mediaImage.setImage(new Image(getClass().getResource("/com/fhm/take2/assets/audio_default_static.png").toExternalForm()));
            }
        });
    }

    @FXML
    void HandleKeyPress(KeyEvent event) {
        if(mp == null)  return;

        if (event.getCode() == KeyCode.RIGHT) {
            // Seek forward 5 seconds (but not past end)
            progressSlider.setValue(progressSlider.getValue() + 5);
            mp.seek(Duration.seconds(progressSlider.getValue()));
            UpdateMediaTimeLabel(Duration.seconds(progressSlider.getValue()), mp.getTotalDuration());
            event.consume();
        }
        else if (event.getCode() == KeyCode.LEFT) {
            // Seek backward 5 seconds (but not before start)
            progressSlider.setValue(progressSlider.getValue() - 5);
            mp.seek(Duration.seconds(progressSlider.getValue()));
            UpdateMediaTimeLabel(Duration.seconds(progressSlider.getValue()), mp.getTotalDuration());
            event.consume();
        }
        else if (event.getCode() == KeyCode.PERIOD) {
            // Seek forward 1 frame (assumes 60 fps)
            progressSlider.setValue(progressSlider.getValue() + (1/60.0));
            mp.seek(Duration.seconds(progressSlider.getValue()));
            UpdateMediaTimeLabel(Duration.seconds(progressSlider.getValue()), mp.getTotalDuration());
            event.consume();
        }
        else if (event.getCode() == KeyCode.COMMA) {
            // Seek backward 1 frame (assumes 60 fps)
            progressSlider.setValue(progressSlider.getValue() - (1/60.0));
            mp.seek(Duration.seconds(progressSlider.getValue()));
            UpdateMediaTimeLabel(Duration.seconds(progressSlider.getValue()), mp.getTotalDuration());
            event.consume();
        }
        if (event.getCode() == KeyCode.UP) {
            volumeSlider.setValue(volumeSlider.getValue() + 5);
            event.consume();
        }
        else if (event.getCode() == KeyCode.DOWN) {
            volumeSlider.setValue(volumeSlider.getValue() - 5);
            event.consume();
        }

        if(mp.getTotalDuration() != null) {
            progressSlider.setValue(mp.getCurrentTime().toSeconds());
            UpdateMediaTimeLabel(mp.getCurrentTime(), mp.getTotalDuration());
        }
    }
    @FXML
    void HandleKeyRelease(KeyEvent event) {
        if(mp == null)  return;

        if(event.getCode() == KeyCode.SPACE) {
            if(mp.getStatus() == MediaPlayer.Status.PLAYING) {
                PauseMedia();
            }
            else {
                PlayMedia();
            }
            event.consume();
        }
    }

    private void UpdateIndexLabel() {
        int sz = 0;
        if(mode == 0)
            sz = mediaArrayList.size();
        if(mode == 1)
            sz = fileArrayList.size();
        if(mode == 2)
            sz = mediaArrayList.size() + fileArrayList.size();
        mediaIndexLabel.setText((currentMediaIndex + 1) + "/" + sz);
    }

    @FXML
    void MediaClicked(MouseEvent event) {
        if(mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING) {
            PauseMedia();
        }
        else {
            PlayMedia();
        }
        event.consume();
    }

    private void handleSeek(MouseEvent event) {
        if (mp == null || mp.getTotalDuration() == null)
            return;

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            isSeeking = true;
            if(!paused.get())
                mp.pause();
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mp.seek(Duration.seconds(progressSlider.getValue()));
            UpdateMediaTimeLabel(Duration.seconds(progressSlider.getValue()), mp.getTotalDuration());
            progressSlider.getParent().requestFocus();
            isSeeking = false;
            if(!paused.get())
                mp.play();
        }
        event.consume();
        mediaPane.requestFocus();
    }

    private String detectType(Object item) {
        String name;

        if (item instanceof File f) {
            name = f.getName().toLowerCase();
        }
        else if (item instanceof Media m) {
            return m.GetType().toString();
        }
        else {
            return "Other";
        }

        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif"))
            return "Image";

        if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".mkv"))
            return "Video";

        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg"))
            return "Audio";

        return "Other";
    }

    private String extractUrl(Object obj) {
        if (obj instanceof File f) {
            return f.toURI().toString();
        }
        if (obj instanceof Media m) {
            return m.GetURL();
        }
        return null;
    }

    private void DisplayMedia() {
        topHBox.setVisible(false);
        isAudio = false;
        paused.set(true);
        mediaImage.setImage(null);
        mediaImage.setVisible(false);
        mediaViewer.setMediaPlayer(null);
        mediaPane.setVisible(false);
        mediaViewer.setVisible(false);
        mediaViewer.setFitWidth(0);
        mediaViewer.setFitHeight(0);
        mediaImage.setFitWidth(0);
        mediaImage.setFitHeight(0);
        unknownMediaLabel.setVisible(false);
        if(mp != null && mp.getTotalDuration() != null && !mp.getTotalDuration().isUnknown() && !mp.getTotalDuration().isIndefinite())
            mp.pause();
        mp = null;

        // Handle media display
        Object item;
        if(mode == 0) {
            //if(mediaArrayList == null || mediaArrayList.isEmpty()) return;
            item = mediaArrayList.get(currentMediaIndex);  // Media
        }
        else if(mode == 1) {
            //if(fileArrayList == null || fileArrayList.isEmpty()) return;
            item = fileArrayList.get(currentMediaIndex);   // File
        }
        else {
            //if((mediaArrayList == null && fileArrayList == null) || (mediaArrayList.isEmpty() && fileArrayList.isEmpty())) return;
            if(currentMediaIndex < mediaArrayList.size())
                item = mediaArrayList.get(currentMediaIndex);
            else
                item = fileArrayList.get(currentMediaIndex - mediaArrayList.size());
        }

        String url = extractUrl(item);

        if (url != null && !url.isEmpty()) {
            String type = detectType(item);

            if (type.equals("Image")) {
                Image image;
                if(images.containsKey(currentMediaIndex)) {
                    image = images.get(currentMediaIndex);
                    topHBox.setVisible(true);
                }
                else {
                    image = new Image(url, true);
                    image.progressProperty().addListener((obs, oldV, newV) -> {
                        if (newV.doubleValue() == 1.0) {
                            topHBox.setVisible(true);
                        }
                    });

                    image.errorProperty().addListener((obs, oldV, newV) -> {
                        if (image.getException() != null) {
                            System.out.println("Failed to load image: " + image.getException());
                        }
                    });
                    images.put(currentMediaIndex, image);
                }
                mediaImage.setImage(image);
                mediaImage.setFitWidth(origWidth);
                mediaImage.setFitHeight(origHeight);
                mediaImage.setVisible(true);
            }
            else if (type.equals("Video") || type.equals("Audio")) {
                mediaViewer.setFitWidth(origWidth);
                mediaViewer.setFitHeight(origHeight);
                if(mediaPlayers.containsKey(currentMediaIndex)) {
                    mp = mediaPlayers.get(currentMediaIndex);
                    progressSlider.setMin(0);
                    progressSlider.setMax(mp.getTotalDuration().toSeconds());
                    progressSlider.setValue(mp.getCurrentTime().toSeconds());
                    UpdateMediaTimeLabel(mp.getCurrentTime(), mp.getTotalDuration());
                    mp.setVolume(volumeSlider.getValue() / 100.0);
                    /*mp.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        if (!isSeeking && mp.getTotalDuration() != null) {
                            Platform.runLater(() -> {
                                progressSlider.setValue(newTime.toSeconds());
                                UpdateMediaTimeLabel(newTime, mp.getTotalDuration());
                            });
                        }
                    });*/
                    progressSlider.setOnMousePressed(this::handleSeek);
                    progressSlider.setOnMouseReleased(this::handleSeek);
                    mp.setAutoPlay(false);
                    if (type.equals("Video")) {
                        mediaViewer.setMediaPlayer(mp);
                    } else {
                        isAudio = true;
                        mediaViewer.setMediaPlayer(null); // prevent audio-only issues
                        mediaImage.setImage(new Image(getClass().getResource("/com/fhm/take2/assets/audio_default_static.png").toExternalForm()));
                        mediaImage.setVisible(true);
                    }
                    mediaPane.setVisible(true);
                    mediaViewer.setVisible(true);
                    topHBox.setVisible(true);
                }
                else {
                    javafx.scene.media.Media mediaObj = new javafx.scene.media.Media(url);
                    mediaObj.setOnError(() -> {
                        Platform.runLater(this::RemoveMedia);
                        System.out.println("corrupted media detected");
                    });
                    mp = new MediaPlayer(mediaObj);
                    mp.setOnError(() -> {
                        Platform.runLater(this::RemoveMedia);
                        System.out.println("corrupted media detected");
                        mp = null;
                    });

                    mp.setOnReady(() -> {
                        if(mp.getTotalDuration() == null || mp.getTotalDuration().isUnknown() || mp.getTotalDuration().isIndefinite()) {
                            System.out.println("corrupted media detected");
                            Platform.runLater(this::RemoveMedia);
                            return;
                        }
                        progressSlider.setMin(0);
                        progressSlider.setMax(mp.getTotalDuration().toSeconds());
                        progressSlider.setValue(0);
                        UpdateMediaTimeLabel(mp.getCurrentTime(), mp.getTotalDuration());
                        mp.setVolume(volumeSlider.getValue() / 100.0);
                        mp.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                            if (!isSeeking && mp.getTotalDuration() != null) {
                                Platform.runLater(() -> {
                                    progressSlider.setValue(newTime.toSeconds());
                                    UpdateMediaTimeLabel(newTime, mp.getTotalDuration());
                                });
                            }
                        });
                        progressSlider.setOnMousePressed(this::handleSeek);
                        progressSlider.setOnMouseReleased(this::handleSeek);
                        mp.setAutoPlay(false);
                        if (type.equals("Video")) {
                            mediaViewer.setMediaPlayer(mp);
                        } else {
                            isAudio = true;
                            mediaViewer.setMediaPlayer(null); // prevent audio-only issues
                            mediaImage.setImage(new Image(getClass().getResource("/com/fhm/take2/assets/audio_default_static.png").toExternalForm()));
                            mediaImage.setVisible(true);
                        }
                        mediaPane.setVisible(true);
                        mediaViewer.setVisible(true);
                        topHBox.setVisible(true);
                    });
                    mediaPlayers.put(currentMediaIndex, mp);
                }
            } else {
                topHBox.setVisible(true);
                unknownMediaLabel.setVisible(true);
                unknownMediaLabel.setText(url);
            }
        }
    }

    private void UpdateMediaTimeLabel(Duration current, Duration total) {
        long currentSec = (long) current.toSeconds();
        long totalSec = (long) total.toSeconds();

        String currentStr = FormatTime(currentSec);
        String totalStr = FormatTime(totalSec);

        mediaTimeLabel.setText(currentStr + " / " + totalStr);
    }

    private String FormatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0)
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    void OpenUnknownMedia(MouseEvent event) {
        if(mode != 0) return;

        String url = unknownMediaLabel.getText();
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.consume();
    }

    @FXML
    void PauseMedia() { if(mp != null) mp.pause(); mediaPane.requestFocus(); paused.set(true);  }

    @FXML
    void PlayMedia() {  if(mp != null) mp.play(); mediaPane.requestFocus(); paused.set(false); }

    @FXML
    void NextMedia(MouseEvent event) {
        int limit = 0;
        if(mode == 0)
            limit = mediaArrayList.size();
        if(mode == 1)
            limit = fileArrayList.size();
        if(mode == 2) //sex
            limit = mediaArrayList.size() + fileArrayList.size();
        if(currentMediaIndex == limit)
            return;
        currentMediaIndex++;
        PrevNextButtons();
        DisplayMedia();
        event.consume();
    }

    @FXML
    void PrevMedia(MouseEvent event) {
        if(currentMediaIndex == 0)
            return;
        currentMediaIndex--;
        PrevNextButtons();
        DisplayMedia();
        event.consume();
    }

    void AddMedia(File file) {
        if(mode == 0 || file == null)
            return;
        for(File f : fileArrayList)
            if(extractUrl(f).equals(extractUrl(file)))
                return;
        fileArrayList.add(file);
        currentMediaIndex = fileArrayList.size() - 1 + (mode == 2? mediaArrayList.size() : 0);
        PrevNextButtons();
        DisplayMedia();
    }

    void AddMedia(Media media) {
        if(mode == 1 || media == null)
            return;
        for(Media md : mediaArrayList)
            if(extractUrl(md).equals(extractUrl(media)))
                return;
        mediaArrayList.add(media);
        currentMediaIndex = mediaArrayList.size() - 1;
        PrevNextButtons();
        DisplayMedia();
    }

    ArrayList<File> GetFileArrayList() {
        if(mp != null && mp.getTotalDuration() != null && !mp.getTotalDuration().isUnknown() && !mp.getTotalDuration().isIndefinite())
            RemoveMedia();
        return this.fileArrayList;
    }

    ArrayList<Media> getMediaArrayList() {
        if(mp != null && mp.getTotalDuration() != null && !mp.getTotalDuration().isUnknown() && !mp.getTotalDuration().isIndefinite())
            RemoveMedia();
        return this.mediaArrayList;
    }

    @FXML
    void RemoveMedia() {
        if(mode == 0)
            return;
        fileArrayList.remove(currentMediaIndex);
        if(fileArrayList.isEmpty() && (mediaArrayList == null || mediaArrayList.isEmpty())) {
            done.set(true);
            return;
        }
        if(currentMediaIndex > 0 && currentMediaIndex == (fileArrayList.size() - (mode == 2? mediaArrayList.size() : 0)))
            currentMediaIndex--;
        PrevNextButtons();
        DisplayMedia();
    }

    private void PrevNextButtons() {
        if(mp != null && (mp.getTotalDuration() == null || mp.getTotalDuration().isUnknown() || mp.getTotalDuration().isIndefinite()))
            RemoveMedia();
        UpdateIndexLabel();
        int sz = 0;
        if(mode == 0)
            sz = mediaArrayList.size();
        if(mode == 1)
            sz = fileArrayList.size();
        if(mode == 2)
            sz = mediaArrayList.size() + fileArrayList.size();

        prevButton.setVisible(currentMediaIndex != 0);
        nextButton.setVisible(currentMediaIndex != sz - 1);
    }

    void Clean() {
        for(int k : mediaPlayers.keySet()) {
            mp = mediaPlayers.get(k);
            if(mp != null && mp.getTotalDuration() != null && !mp.getTotalDuration().isUnknown() && !mp.getTotalDuration().isIndefinite())
                mp.dispose();
        }
    }
}
