package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.util.Duration;
import mara.mybox.data.MediaInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-11-23
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/8/javafx/media-tutorial/playercontrol.htm
public class MediaPlayerController extends BaseController {

    public static String MiaoGuaiGuaiBenBen = "MiaoGuaiGuaiBenBen";
    protected ObservableList<MediaInformation> tableData;
    protected TableView<MediaInformation> tableView;
    protected MediaPlayer player;
    protected AudioClip audioPlayer;
    protected boolean atEndOfMedia, isSettingTimer;
    protected Duration duration;
    protected double volumn, speed;
    protected int repeat, currentLoop, currentIndex;
    protected URI uri;
    protected MediaInformation currentMedia;
    protected List<Integer> randomPlayed;

    @FXML
    private BorderPane borderPane;
    @FXML
    private MediaView mediaView;
    @FXML
    private VBox leftBox, playerBox, playerControlBox;
    @FXML
    private Slider timeSlider, volumeSlider;
    @FXML
    private ToggleButton soundButton, fullScreenButton;
    @FXML
    private CheckBox randomCheck, autoplayCheck, msCheck;
    @FXML
    private Label elapsedTimeLabel, leftTimeLabel;
    @FXML
    private ComboBox<String> repeatSelector, speedSelector;
    @FXML
    protected MediaTableController tableController;
    @FXML
    protected Button dataButton, catButton;
    @FXML
    protected ImageView supportTipsView;

    public MediaPlayerController() {
        baseTitle = AppVariables.message("MediaPlayer");

        SourceFileType = VisitHistory.FileType.Media;
        SourcePathType = VisitHistory.FileType.Media;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;
        AddFileType = VisitHistory.FileType.Media;
        AddPathType = VisitHistory.FileType.Media;

        targetPathKey = "MediaFilePath";
        sourcePathKey = "MediaFilePath";

        sourceExtensionFilter = CommonFxValues.JdkMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initializeNext() {
        try {
            if (tableController != null) {
                tableController.setParentController(this);
                tableData = tableController.tableData;
                tableView = tableController.tableView;
            } else {
                tableData = FXCollections.observableArrayList();
            }
            currentIndex = 0;

            repeat = 1;
            repeatSelector.getItems().addAll(Arrays.asList(
                    "1", "2", "3", message("Infinite")
            ));
            repeatSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (message("Infinite").equals(newValue)) {
                            repeat = MediaPlayer.INDEFINITE;
                            return;
                        }
                        int v = Integer.valueOf(newValue);
                        if (v <= 0) {
                            repeat = MediaPlayer.INDEFINITE;
                        } else {
                            repeat = v;
                        }
                        repeatSelector.getEditor().setStyle(null);
                    } catch (Exception e) {
                        repeatSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
            repeatSelector.getSelectionModel().select(0);

            speed = 1.0;
            speedSelector.getItems().addAll(Arrays.asList(
                    "1", "1.2", "1.5", "2", "0.5", "0.8", "0.3", "3", "5", "8"
            ));
            speedSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.valueOf(newValue);
                        if (v <= 0 || v > 8) {
                            speedSelector.getEditor().setStyle(badStyle);
                        } else {
                            speed = v;
                            speedSelector.getEditor().setStyle(null);
                            if (player != null && player.getStatus() == Status.PLAYING) {
//                                player.pause();
                                player.setRate(speed);
//                                player.play();
                            }
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
            speedSelector.getSelectionModel().select(0);

            soundButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkSoundButton();
                }
            });

            timeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (player != null && timeSlider.isValueChanging()) {
                        player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
                    }
                }
            });

            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (player != null && volumeSlider.isValueChanging()) {
                        player.setVolume(volumeSlider.getValue() / 100.0);
                    }
                }
            });

            playButton.disableProperty().bind(Bindings.isEmpty(tableData));

            autoplayCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("MediaPlayerAutoPlay", autoplayCheck.isSelected());
                }
            });
            autoplayCheck.setSelected(AppVariables.getUserConfigBoolean("MediaPlayerAutoPlay", true));

            msCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("MediaPlayerShowMS", msCheck.isSelected());
                }
            });
            msCheck.setSelected(AppVariables.getUserConfigBoolean("MediaPlayerShowMS", true));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        // https://stackoverflow.com/questions/43785310/how-to-disable-press-esc-to-exit-full-screen-mode-meassage-in-javafx
        getMyStage().setFullScreenExitHint(message("MediaFullScreenComments"));
        myStage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
                if (myStage.isFullScreen()) {
                    enterFullScreen();
                } else {
                    quitFullScreen();
                }
            }
        });

        // https://stackoverflow.com/questions/48692409/making-a-video-media-player-in-javafx-take-as-much-space-as-possible-but-no-mo/48693456?r=SearchResults#48693456
        mediaView.setPreserveRatio(true);
        playerBox.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
//                if (newValue.doubleValue() - oldValue.doubleValue() < 3) {
//                    return;
//                }
                if (player != null && playerBox.getChildren().contains(mediaView)) {
                    mediaView.setFitWidth(newValue.doubleValue() - 5);
                }
            }
        });
        playerBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
//                if (newValue.doubleValue() - oldValue.doubleValue() < 3) {
//                    return;
//                }
                if (player != null && playerBox.getChildren().contains(mediaView)) {
                    mediaView.setFitHeight(newValue.doubleValue() - playerControlBox.getHeight() - 5);
                }
            }
        });

        FxmlControl.setTooltip(stopButton, new Tooltip(message("Stop") + "\nq / Q"));
        stopButton.applyCss();
        FxmlControl.setTooltip(fullScreenButton, new Tooltip(message("FullScreen") + "\nf / F"));
        fullScreenButton.applyCss();
        FxmlControl.setTooltip(soundButton, new Tooltip(message("Mute") + "\nm / M"));
        soundButton.applyCss();
        FxmlControl.setTooltip(dataButton, new Tooltip(message("ManageMediaLists")));
        dataButton.applyCss();
        FxmlControl.setTooltip(supportTipsView, new Tooltip(message("MediaPlayerSupports")));
        supportTipsView.applyCss();
        FxmlControl.setTooltip(catButton, new Tooltip(message("MiaoSounds")));
        catButton.applyCss();

        FxmlControl.setTooltip(speedSelector, new Tooltip("0~8"));

        initPlayer();

        checkSoundButton();

        checkFullScreen();
    }

    @Override
    public void keyHandler(KeyEvent event) {
        super.keyHandler(event);

        String text = event.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        switch (text) {
            case "s":
            case "S":
                playButton.fire();
                return;
            case "q":
            case "Q":
                stopButton.fire();
                return;
            case "m":
            case "M":
                soundButton.fire();
                return;
            case "f":
            case "F":
                fullScreenButton.fire();

        }
    }

    protected void initPlayer() {
        try {
            if (player != null) {
                player.dispose();
                player = null;
            }
            if (getMyStage().isFullScreen()) {
                isSettingValues = true;
                fullScreenButton.setSelected(false);
                isSettingValues = false;
                getMyStage().setFullScreen(false);
                quitFullScreen();
            }
            currentMedia = null;
            currentIndex = 0;
            randomPlayed = null;
            ControlStyle.setIcon(playButton, ControlStyle.getIcon("iconPlay.png"));
            FxmlControl.setTooltip(playButton, new Tooltip(message("Start") + "\nF1 / s / S"));
            playButton.applyCss();
            playButton.setUserData(null);

            currentLoop = 0;
            atEndOfMedia = false;
            timeSlider.setValue(0);
            elapsedTimeLabel.setText("");
            leftTimeLabel.setText("");
            FxmlControl.removeTooltip(infoButton);

            mediaView.setMediaPlayer(null);
            mediaView.setFitHeight(50);
            mediaView.setFitWidth(50);
            tableController.markFileHandling(-1);

        } catch (Exception e) {
        }
    }

    protected void checkSoundButton() {
        if (player != null) {
            player.setMute(soundButton.isSelected());
        }
        if (soundButton.isSelected()) {
            ControlStyle.setIcon(soundButton, ControlStyle.getIcon("iconAudio.png"));
            FxmlControl.setTooltip(soundButton, new Tooltip(message("Sound") + "\nm / M"));
            soundButton.applyCss();
        } else {
            ControlStyle.setIcon(soundButton, ControlStyle.getIcon("iconMute.png"));
            FxmlControl.setTooltip(soundButton, new Tooltip(message("Mute") + "\nm / M"));
            soundButton.applyCss();
        }
    }

    protected void checkFullScreen() {
//        if (player == null) {
//            return;
//        }
        if (fullScreenButton.isSelected()) {
            getMyStage().setFullScreen(true);
            enterFullScreen();

        } else {
            getMyStage().setFullScreen(false);
            quitFullScreen();
        }
    }

    public void enterFullScreen() {
        if (!getMyStage().isFullScreen()) {
            return;
        }
        isSettingValues = true;
        if (!thisPane.getChildren().contains(mediaView)) {
            thisPane.getChildren().add(mediaView);
            thisPane.getChildren().add(playerControlBox);
            playerControlBox.setAlignment(Pos.BOTTOM_CENTER);
            thisPane.getChildren().remove(borderPane);
        }
        mediaView.setFitWidth(myStage.getWidth());
        mediaView.setFitHeight(myStage.getHeight());
        rightPaneControl.setVisible(false);
        playerControlBox.setVisible(false);

        fullScreenButton.setSelected(true);
        ControlStyle.setIcon(fullScreenButton, ControlStyle.getIcon("iconShrink.png"));
        FxmlControl.setTooltip(fullScreenButton, new Tooltip(message("Recover") + "\nESC / f / F"));
        fullScreenButton.applyCss();

        thisPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fullScreenClicked();
            }
        });
        mediaView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fullScreenClicked();
            }
        });
        isSettingValues = false;
    }

    public void quitFullScreen() {
        if (getMyStage().isFullScreen()) {
            return;
        }
        isSettingValues = true;
        thisPane.setOnMouseClicked(null);
        mediaView.setOnMouseClicked(null);
        fullScreenButton.setSelected(false);
        ControlStyle.setIcon(fullScreenButton, ControlStyle.getIcon("iconExpand.png"));
        FxmlControl.setTooltip(fullScreenButton, new Tooltip(message("FullScreen") + "\nf / F"));
        fullScreenButton.applyCss();

        if (!leftBox.getChildren().contains(playerControlBox)) {
            leftBox.getChildren().add(playerControlBox);
        }
        playerControlBox.setVisible(true);
        rightPaneControl.setVisible(true);
        mediaView.setFitWidth(playerBox.getWidth() - 5);
        mediaView.setFitHeight(playerBox.getHeight() - playerControlBox.getHeight() - 5);

        if (!playerBox.getChildren().contains(mediaView)) {
            playerBox.getChildren().add(mediaView);
        }
        if (!thisPane.getChildren().contains(borderPane)) {
            thisPane.getChildren().add(borderPane);
        }

        isSettingValues = false;
    }

    public void fullScreenClicked() {
        if (!getMyStage().isFullScreen()) {
            return;
        }
        playerControlBox.setVisible(true);
        playerControlBox.toFront();
//        playerControlBox.setOpacity(0.5);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (getMyStage().isFullScreen()) {
                            playerControlBox.setVisible(false);
                        }
                        timer = null;
                    }
                });
            }
        }, 6000);
    }

    @Override
    public void dataChanged() {
        try {
            if (player == null && currentMedia == null) {
                if (autoplayCheck.isSelected() && !tableData.isEmpty()) {
                    currentIndex = 0;
                    playCurrent();
                }
                return;
            }
            if (currentMedia != null) {
                int index = tableData.indexOf(currentMedia);
                if (index < 0) {
                    initPlayer();
                }
            }

        } catch (Exception e) {

        }
    }

    public void load(URI uri) {
        try {
            this.uri = uri;
            tableData.clear();
            tableData.add(new MediaInformation(uri));
            initPlayer();
        } catch (Exception e) {
        }
    }

    @FXML
    @Override
    public void playAction() {
        try {
            if (player != null && player.getStatus() != null) {
                switch (player.getStatus()) {
                    case PLAYING:
                        if (atEndOfMedia) {
                            player.seek(player.getStartTime());
                            atEndOfMedia = false;
                        }
                        player.pause();
                        ControlStyle.setIcon(playButton, ControlStyle.getIcon("iconPlay.png"));
                        FxmlControl.setTooltip(playButton, new Tooltip(message("Continue") + "\nF1 / s / S"));
                        playButton.applyCss();
                        break;
                    case PAUSED:
                        player.setCycleCount(repeat);
                        player.setVolume(volumeSlider.getValue() / 100.0);
                        player.setRate(speed);
                        player.play();
                        ControlStyle.setIcon(playButton, ControlStyle.getIcon("iconPause.png"));
                        FxmlControl.setTooltip(playButton, new Tooltip(message("Pause") + "\nF1 / s / S"));
                        playButton.applyCss();
                        break;
                    default:
                        playCurrent();
                }
            } else {
                playCurrent();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    @Override
    public void stopAction() {
        initPlayer();
    }

    public void playIndex(int index) {
        if (index < 0 || index > tableData.size() - 1) {
            return;
        }
        currentIndex = index;
        playCurrent();
    }

    @FXML
    @Override
    public void nextAction() {
        if (player == null || tableData.isEmpty()) {
            return;
        }
        if (currentIndex < 0 || currentIndex >= tableData.size() - 1) {
            currentIndex = 0;
        } else {
            currentIndex++;
        }
        playCurrent();
    }

    @FXML
    @Override
    public void previousAction() {
        if (player == null || tableData.isEmpty()) {
            return;
        }
        if (currentIndex == 0) {
            currentIndex = tableData.size() - 1;
        } else if (currentIndex < 0 || currentIndex > tableData.size() - 1) {
            currentIndex = 0;
        } else {
            currentIndex--;
        }
        playCurrent();
    }

    public void playCurrent() {
        if (tableData.isEmpty()
                || currentIndex < 0 || currentIndex > tableData.size() - 1) {
            initPlayer();
            return;
        }
        synchronized (this) {
            try {
                if (task != null) {
                    return;
                }
                if (player != null) {
                    player.dispose();
                    player = null;
                }
                if (randomCheck.isSelected()) {
                    if (randomPlayed == null || randomPlayed.size() >= tableData.size()) {
                        randomPlayed = new ArrayList();
                    }
                    if (randomPlayed.contains(currentIndex)) {
                        Random r = new Random();
                        int v = r.nextInt(tableData.size());
                        while (randomPlayed.contains(v)) {
                            v = r.nextInt(tableData.size());
                        }
                        currentIndex = v;
                    }
                    randomPlayed.add(currentIndex);
                }
                currentMedia = tableData.get(currentIndex);
                if (currentMedia.getURI() == null) {
                    initPlayer();
                    return;
                }

                myStage.setTitle(getBaseTitle() + " - " + currentMedia.getAddress());
                isSettingValues = true;
                tableController.markFileHandling(currentIndex);
                isSettingValues = false;
                if (!currentMedia.getURI().getScheme().startsWith("file")) {
                    popInformation(message("ReadingStreamMedia...") + "\n" + currentMedia.getAddress(), 6000);
                } else {
                    popInformation(message("ReadingMedia...") + "\n" + currentMedia.getAddress());
                }

                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;
                        try {
                            Media media = new Media(currentMedia.getURI().toString());
                            if (media.getError() == null) {
                                media.setOnError(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleMediaError(currentMedia, media.getError());
//                                        task.cancel();
                                    }
                                });
                            } else {

                                error = media.getError().toString();
                                handleMediaError(currentMedia, media.getError());
                                return true;
                            }
                            player = new MediaPlayer(media);
                            if (player == null) {
                                error = AppVariables.message("InvalidData");
                                return true;
                            }
                            if (player.getError() != null) {
                                handleMediaError(currentMedia, player.getError());
                                return true;
                            }
                            player.setOnError(new Runnable() {
                                @Override
                                public void run() {
                                    handleMediaError(currentMedia, player.getError());
                                }
                            });

//                        player.setCycleCount(repeat);
                            player.setVolume(volumeSlider.getValue() / 100.0);
                            player.setRate(speed);
                            checkControls();

                            player.setOnReady(new Runnable() {
                                @Override
                                public void run() {
                                    mediaReady();
                                }
                            });

                            player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                                @Override
                                public void changed(ObservableValue ov, Duration oldValue, Duration newValue) {
                                    updateStatus();
                                }
                            });

                            player.setOnEndOfMedia(new Runnable() {
                                @Override
                                public void run() {
                                    mediaEnd();
                                }
                            });

                            player.setOnStopped(new Runnable() {
                                @Override
                                public void run() {
//                                    logger.debug("Stopped");
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            initPlayer();
                                        }
                                    });

                                }
                            });

                            showPlayer();

//                        player.setCycleCount(repea
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error != null) {
                            popError(error);
                        }
                        tableController.tableView.refresh();
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

    }

    public void handleMediaError(MediaInformation info, MediaException exception) {
        if (exception == null) {
            return;
        }
        String msg = MediaInformation.exceptionMessage(exception);
        popMediaError(info.getAddress() + "\n" + msg);
        String errorMsg = exception.getMessage();
        if (errorMsg.contains("ERROR_MEDIA_AUDIO_FORMAT_UNSUPPORTED")) {
            info.setAudioEncoding(message("NotSupport"));
        } else if (errorMsg.contains("ERROR_MEDIA_VIDEO_FORMAT_UNSUPPORTED")) {
            info.setVideoEncoding(message("NotSupport"));
        }
    }

    public void popMediaError(String error) {
        if (error == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            public void run() {
                popError(error);
            }
        });
    }

    public void checkControls() {
        Platform.runLater(new Runnable() {
            public void run() {
                checkFullScreen();
                checkSoundButton();
            }
        });
    }

    public void mediaReady() {
        Platform.runLater(new Runnable() {
            public void run() {

                player.play();
                ControlStyle.setIcon(playButton, ControlStyle.getIcon("iconPause.png"));
                FxmlControl.setTooltip(playButton, new Tooltip(message("Pause") + "\nF1 / s / S"));
                playButton.setUserData("Playing");
                playButton.applyCss();

                FxmlControl.setTooltip(infoButton, currentMedia.getInfo());

                duration = player.getMedia().getDuration();
                updateStatus();
            }
        });
    }

    public void mediaEnd() {
        Platform.runLater(new Runnable() {
            public void run() {
                atEndOfMedia = true;
                if (randomCheck.isSelected()) {
                    if (randomPlayed == null) {
                        randomPlayed = new ArrayList();
                        currentIndex = new Random().nextInt(tableData.size());
                        playCurrent();
                    } else {
                        if (randomPlayed.size() >= tableData.size()) {
                            currentLoop++;
                            if (repeat > 0 && currentLoop >= repeat) {
                                player.stop();
                            } else {
                                randomPlayed = new ArrayList();
                                currentIndex = new Random().nextInt(tableData.size());
                                playCurrent();
                            }
                        } else {
                            Random r = new Random();
                            int v = r.nextInt(tableData.size());
                            while (randomPlayed.contains(v)) {
                                v = r.nextInt(tableData.size());
                            }
                            currentIndex = v;
                            playCurrent();
                        }
                    }
                } else {
                    int index = tableData.indexOf(currentMedia);
                    if (index >= 0) {
                        currentIndex = index;
                    } else if (currentIndex > tableData.size() - 1) {
                        currentIndex = 0;
                    }
                    if (currentIndex >= tableData.size() - 1) {
                        currentLoop++;
                    }
                    if (repeat > 0 && currentLoop >= repeat) {
                        player.stop();
                    } else {
                        nextAction();
                    }
                }
            }
        });
    }

    public void showPlayer() {
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    mediaView.setMediaPlayer(player);
                    mediaView.setOnError(new EventHandler<MediaErrorEvent>() {
                        public void handle(MediaErrorEvent t) {
                            if (t.getMediaError() != null) {
                                popMediaError(t.getMediaError().toString());
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error(e.toString());
                    popMediaError(e.toString());
                }
            }
        });
    }

    protected void updateStatus() {

        Platform.runLater(new Runnable() {
            public void run() {
                if (player == null) {
                    elapsedTimeLabel.setText("");
                    leftTimeLabel.setText("");
                    timeSlider.setValue(0);
                    return;
                }

                Duration elapsed = player.getCurrentTime();
                if (elapsed == null) {
                    return;
                }
                elapsedTimeLabel.setText(msCheck.isSelected()
                        ? DateTools.showDuration((long) elapsed.toMillis())
                        : DateTools.showSeconds((long) elapsed.toSeconds())
                );
                Duration total = player.getTotalDuration();
                timeSlider.setDisable(total.isUnknown());
                if (total.lessThanOrEqualTo(Duration.ZERO)) {
                    return;
                }
                if (!timeSlider.isDisabled() && !timeSlider.isValueChanging()) {
                    timeSlider.setValue(elapsed.toMillis() * 100.0 / total.toMillis());
                }

                Duration left = total.subtract(elapsed);
                leftTimeLabel.setText(msCheck.isSelected()
                        ? DateTools.showDuration((long) left.toMillis())
                        : DateTools.showSeconds((long) left.toSeconds())
                );

            }
        });

    }

    @FXML
    @Override
    public void infoAction() {
        tableController.popInfo(currentMedia);
    }

    @FXML
    public void fullScreenAction() {
        if (isSettingValues) {
            return;
        }
        checkFullScreen();
    }

    @FXML
    public void dataAction() {
        try {
            MediaListController controller
                    = (MediaListController) openStage(CommonValues.MediaListFxml);
            controller.setPlayerController(this);
            controller.loadList(tableController.mediaListName);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void catAction() {
        tableController.loadMiaoSounds();

    }

    public void loadFile(File file) {
        tableController.addFile(file);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (player != null) {
            player.dispose();
            player = null;
        }
        return true;
    }

}
