package com.fhm.take2;

import com.crdt.Media;
import com.crdt.User;
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
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;

public class MediaViewController {

    @FXML private ImageView mediaImage;
    @FXML private Label mediaIndexLabel;
    @FXML private AnchorPane mediaPane;
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
    private boolean creating = false;
    BooleanProperty done = new SimpleBooleanProperty(false);

    public void init(ArrayList<Media> mediaArrayList, boolean creating, ArrayList<File> fileArrayList) {
        if(!creating)
            this.mediaArrayList = mediaArrayList;
        this.currentMediaIndex = 0;
        this.creating = creating;
        if(creating)
            this.fileArrayList = fileArrayList;
        else {
            removeButton.setDisable(true);
            removeButton.setVisible(false);
        }

        PrevNextButtons();
        DisplayMedia();

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
        if (event.getCode() == KeyCode.UP) {
            volumeSlider.setValue(volumeSlider.getValue() + 5);
            event.consume();
        }
        else if (event.getCode() == KeyCode.DOWN) {
            // Seek backward 5 seconds (but not before start)
            volumeSlider.setValue(volumeSlider.getValue() - 5);
            event.consume();
        }
        else if(event.getCode() == KeyCode.SPACE) {
            if(mp.getStatus() == MediaPlayer.Status.PLAYING) {
                PauseMedia();
            }
            else {
                PlayMedia();
            }
            event.consume();
        }

        if(mp.getTotalDuration() != null) {
            progressSlider.setValue(mp.getCurrentTime().toSeconds());
            UpdateMediaTimeLabel(mp.getCurrentTime(), mp.getTotalDuration());
        }
    }

    private void UpdateIndexLabel() {
        mediaIndexLabel.setText((currentMediaIndex + 1) + "/" + (creating? fileArrayList.size() : mediaArrayList.size()));
    }

    @FXML
    void MediaClicked() {
        if(mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING) {
            PauseMedia();
        }
        else {
            PlayMedia();
        }
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
        Clean();

        // Handle media display
        Object item;
        if(!creating) {
            if(mediaArrayList == null || mediaArrayList.isEmpty()) return;
            item = mediaArrayList.get(currentMediaIndex);  // Media
        }
        else {
            if(fileArrayList == null || fileArrayList.isEmpty()) return;
            item = fileArrayList.get(currentMediaIndex);   // File
        }

        String url = extractUrl(item);

        System.out.println(url);
        if (url != null && !url.isEmpty()) {
            String type = detectType(item);

            if (type.equals("Image")) {
                Image image = new Image(url, true);
                mediaImage.setImage(image);
                mediaImage.setVisible(true);
            }
            else if (type.equals("Video") || type.equals("Audio")) {
                javafx.scene.media.Media mediaObj = new javafx.scene.media.Media(url);
                mp = new MediaPlayer(mediaObj);
                progressSlider.setValue(0);
                UpdateMediaTimeLabel(Duration.seconds(0), Duration.seconds(0));
                mp.setOnReady(() -> {
                    progressSlider.setMin(0);
                    progressSlider.setMax(mp.getTotalDuration().toSeconds());
                    UpdateMediaTimeLabel(mp.getCurrentTime(), mp.getTotalDuration());
                });
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
            } else {
                unknownMediaLabel.setDisable(false);
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
    void OpenUnknownMedia() {
        String url = unknownMediaLabel.getText();
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void PauseMedia() { if(mp != null) mp.pause(); mediaPane.requestFocus(); paused.set(true);  }

    @FXML
    void PlayMedia() {  if(mp != null) mp.play(); mediaPane.requestFocus(); paused.set(false); }

    @FXML
    void NextMedia() {
        if(currentMediaIndex == (creating? fileArrayList.size() : mediaArrayList.size()) - 1)
            return;
        currentMediaIndex++;
        PrevNextButtons();
        DisplayMedia();
    }

    @FXML
    void PrevMedia() {
        if(currentMediaIndex == 0)
            return;
        currentMediaIndex--;
        PrevNextButtons();
        DisplayMedia();
    }

    void AddMedia(File file) {
        if(!creating || file == null)
            return;
        for(File f : fileArrayList)
            if(extractUrl(f).equals(extractUrl(file)))
                return;
        fileArrayList.add(file);
        currentMediaIndex = fileArrayList.size() - 1;
        PrevNextButtons();
        DisplayMedia();
    }

    ArrayList<File> GetFileArrayList() {  return this.fileArrayList; }

    @FXML
    void RemoveMedia() {
        if(!creating)
            return;
        fileArrayList.remove(currentMediaIndex);
        if(fileArrayList.isEmpty())
            done.set(true);
        if(currentMediaIndex > 0 && currentMediaIndex == fileArrayList.size())
            currentMediaIndex--;
        PrevNextButtons();
        DisplayMedia();
    }

    private void PrevNextButtons() {
        UpdateIndexLabel();
        int sz = creating? fileArrayList.size() : mediaArrayList.size();
        if(currentMediaIndex == 0) {
            prevButton.setDisable(true);
            prevButton.setVisible(false);
        }
        else {
            prevButton.setDisable(false);
            prevButton.setVisible(true);
        }
        if(currentMediaIndex == sz - 1) {
            nextButton.setDisable(true);
            nextButton.setVisible(false);
        }
        else {
            nextButton.setDisable(false);
            nextButton.setVisible(true);
        }
    }

    void Clean() {
        isAudio = false;
        paused.set(true);
        mediaImage.setImage(null);
        mediaImage.setVisible(false);
        mediaViewer.setMediaPlayer(null);
        mediaPane.setVisible(false);
        unknownMediaLabel.setDisable(true);
        unknownMediaLabel.setVisible(false);
        if(mp != null)
            mp.dispose();
        mp = null;
    }
}
