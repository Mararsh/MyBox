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
import javafx.scene.input.KeyCode;
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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.StyleData;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.fxml.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    protected BorderPane borderPane;
    @FXML
    protected MediaView mediaView;
    @FXML
    protected VBox leftBox, playerBox, playerControlBox;
    @FXML
    protected Slider timeSlider, volumeSlider;
    @FXML
    protected ToggleButton soundButton, fullScreenButton;
    @FXML
    protected CheckBox randomCheck, autoplayCheck, msCheck;
    @FXML
    protected Label elapsedTimeLabel, leftTimeLabel;
    @FXML
    protected ComboBox<String> repeatSelector, speedSelector;
    @FXML
    protected ControlMediaTable tableController;
    @FXML
    protected Button dataButton, catButton;
    @FXML
    protected ImageView supportTipsView;

    public MediaPlayerController() {
        baseTitle = Languages.message("MediaPlayer");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Media);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (tableController != null) {
                tableController.setParentController(this);
                tableData = tableController.tableData;
                tableView = tableController.tableView;
            } else {
                tableData = FXCollections.observableArrayList();
            }
            currentIndex = 0;

            repeat = 1;
            repeatSelector.getItems().addAll(Arrays.asList("1", "2", "3", Languages.message("Infinite")
            ));
            repeatSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (Languages.message("Infinite").equals(newValue)) {
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
                @Override
                public void invalidated(Observable ov) {
                    if (player != null && timeSlider.isValueChanging()) {
                        player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
                    }
                }
            });

            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                @Override
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
                    UserConfig.setUserConfigBoolean("MediaPlayerAutoPlay", autoplayCheck.isSelected());
                }
            });
            autoplayCheck.setSelected(UserConfig.getUserConfigBoolean("MediaPlayerAutoPlay", true));

            msCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean("MediaPlayerShowMS", msCheck.isSelected());
                }
            });
            msCheck.setSelected(UserConfig.getUserConfigBoolean("MediaPlayerShowMS", true));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeTools.setTooltip(stopButton, new Tooltip(Languages.message("Stop") + "\nq / Q"));
            NodeTools.setTooltip(fullScreenButton, new Tooltip(Languages.message("FullScreen") + "\nf / F"));
            NodeTools.setTooltip(soundButton, new Tooltip(Languages.message("Mute") + "\nm / M"));
            NodeTools.setTooltip(dataButton, new Tooltip(Languages.message("ManageMediaLists")));
            NodeTools.setTooltip(supportTipsView, new Tooltip(Languages.message("MediaPlayerSupports")));
            NodeTools.setTooltip(catButton, new Tooltip(Languages.message("MiaoSounds")));
            NodeTools.setTooltip(speedSelector, new Tooltip("0~8"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        // https://stackoverflow.com/questions/43785310/how-to-disable-press-esc-to-exit-full-screen-mode-meassage-in-javafx
        getMyStage().setFullScreenExitHint(Languages.message("MediaFullScreenComments"));
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
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if (newValue.doubleValue() - oldValue.doubleValue() < 20) {
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
//                if (newValue.doubleValue() - oldValue.doubleValue() < 20) {
//                    return;
//                }
                if (player != null && playerBox.getChildren().contains(mediaView)) {
                    mediaView.setFitHeight(newValue.doubleValue() - playerControlBox.getHeight() - 5);
                }
            }
        });

        initPlayer();

        checkSoundButton();

        checkFullScreen();
    }

    @Override
    public boolean keyFilter(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != null) {
            switch (code) {
                case S:
                    playButton.fire();
                    return true;
                case Q:
                    stopButton.fire();
                    return true;
                case M:
                    soundButton.fire();
                    return true;
                case F:
                    fullScreenButton.fire();
                    return true;
            }
        }
        return super.keyFilter(event);
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
            StyleTools.setIconTooltips(playButton, "iconPlay.png", Languages.message("Start") + "\nF1 / s / S");
            playButton.applyCss();
            playButton.setUserData(null);

            currentLoop = 0;
            atEndOfMedia = false;
            timeSlider.setValue(0);
            elapsedTimeLabel.setText("");
            leftTimeLabel.setText("");
            NodeTools.removeTooltip(infoButton);

            mediaView.setMediaPlayer(null);
            mediaView.setFitHeight(50);
            mediaView.setFitWidth(50);
            tableController.markFileHandling(-1);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSoundButton() {
        if (player != null) {
            player.setMute(soundButton.isSelected());
        }
        if (soundButton.isSelected()) {
            StyleTools.setIconTooltips(soundButton, "iconAudio.png", Languages.message("Sound") + "\nm / M");
            soundButton.applyCss();
        } else {
            StyleTools.setIconTooltips(soundButton, "iconMute.png", Languages.message("Mute") + "\nm / M");
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
        StyleTools.setIconTooltips(fullScreenButton, "iconShrink.png", Languages.message("Recover") + "\nESC / f / F");
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
        StyleTools.setIconTooltips(fullScreenButton, "iconExpand.png", Languages.message("FullScreen") + "\nf / F");
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
        playerControlBox.requestFocus();
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
            if (isSettingValues) {
                return;
            }
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

    public void load(File file) {
        try {
            isSettingValues = true;
            initPlayer();
            tableData.clear();
            tableController.addFile(file);
            isSettingValues = false;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            dataChanged();
                        }
                    });
                }
            }, 3000);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                        StyleTools.setIconTooltips(playButton, "iconPlay.png", Languages.message("Continue") + "\nF1 / s / S");
                        playButton.applyCss();
                        break;
                    case PAUSED:
                        player.setCycleCount(repeat);
                        player.setVolume(volumeSlider.getValue() / 100.0);
                        player.setRate(speed);
                        player.play();
                        StyleTools.setIconTooltips(playButton, "iconPause.png", Languages.message("Pause") + "\nF1 / s / S");
                        playButton.applyCss();
                        break;
                    default:
                        playCurrent();
                }
            } else {
                playCurrent();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                || currentIndex < 0 || currentIndex >= tableData.size()) {
            initPlayer();
            return;
        }
        synchronized (this) {
            try {
                if (task != null && !task.isQuit()) {
                    task.cancel();
                }
                task = new SingletonTask<Void>() {

                    private int index;
                    private MediaInformation info;

                    @Override
                    protected boolean handle() {
                        error = null;
                        try {
                            index = getIndex(currentIndex);
                            List<Integer> tried = new ArrayList();
                            while (tried.size() < tableData.size()) {
                                info = tableData.get(index);
                                if (!tried.contains(index)) {
                                    tried.add(index);
                                }
                                if (info == null) {
                                    index = getIndex(++index);
                                    continue;
                                }
                                long wait = 0;
                                while (!info.isFinish()) {
                                    if (task == null || task.isQuit()) {
                                        return false;
                                    }
                                    Thread.sleep(500);
                                    wait += 500;
                                }
                                if (info.isFinish()) {
                                    return true;
                                } else {
                                    index = getIndex(++index);
                                }
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return false;
                    }

                    private int getIndex(int index) {
                        int newIndex = index;
                        if (newIndex >= tableData.size()) {
                            newIndex = 0;
                        }
                        if (!randomCheck.isSelected()) {
                            return newIndex;
                        }
                        if (randomPlayed == null || randomPlayed.size() >= tableData.size()) {
                            randomPlayed = new ArrayList();
                        }
                        if (randomPlayed.contains(newIndex)) {
                            Random r = new Random();
                            int v = r.nextInt(tableData.size());
                            while (randomPlayed.contains(v)) {
                                v = r.nextInt(tableData.size());
                            }
                            return v;
                        }
                        return newIndex;
                    }

                    @Override
                    protected void whenSucceeded() {
                        playMedia(index, info);
                    }

                };
                handling(task, Modality.WINDOW_MODAL, Languages.message("ReadingMedia..."));
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }

    }

    public void playMedia(int index, MediaInformation info) {
        if (info == null || !info.isFinish()) {
            popInformation(Languages.message("MediaNotReady"), 6000);
            initPlayer();
            return;
        }
        synchronized (this) {
            try {
                if (task != null && !task.isQuit()) {
                    task.cancel();
                }
                if (player != null) {
                    player.dispose();
                    player = null;
                }
                currentIndex = index;
                currentMedia = info;
                if (randomCheck.isSelected()) {
                    randomPlayed.add(currentIndex);
                }
                myStage.setTitle(getBaseTitle() + " - " + currentMedia.getAddress());
                isSettingValues = true;
                tableController.markFileHandling(currentIndex);
                isSettingValues = false;
                if (!currentMedia.getURI().getScheme().startsWith("file")) {
                    popInformation(Languages.message("ReadingStreamMedia...") + "\n" + currentMedia.getAddress(), 6000);
                } else {
                    popInformation(Languages.message("ReadingMedia...") + "\n" + currentMedia.getAddress());
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
                                error = Languages.message("InvalidData");
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
//                                    MyBoxLog.debug("Stopped");
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            initPlayer();
                                        }
                                    });

                                }
                            });

                            showPlayer();

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
                handling(task);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
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
            info.setAudioEncoding(Languages.message("NotSupport"));
        } else if (errorMsg.contains("ERROR_MEDIA_VIDEO_FORMAT_UNSUPPORTED")) {
            info.setVideoEncoding(Languages.message("NotSupport"));
        }
    }

    public void popMediaError(String error) {
        if (error == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                popError(error);
            }
        });
    }

    public void checkControls() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                checkFullScreen();
                checkSoundButton();
            }
        });
    }

    public void mediaReady() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                player.play();
                StyleTools.setIconTooltips(playButton, "iconPause.png", Languages.message("Pause") + "\nF1 / s / S");
                playButton.setUserData("Playing");
                playButton.applyCss();

                NodeTools.setTooltip(infoButton, currentMedia.getInfo());

                duration = player.getMedia().getDuration();
                updateStatus();
            }
        });
    }

    public void mediaEnd() {
        Platform.runLater(new Runnable() {
            @Override
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
            @Override
            public void run() {
                try {
                    mediaView.setMediaPlayer(player);
                    mediaView.setOnError(new EventHandler<MediaErrorEvent>() {
                        @Override
                        public void handle(MediaErrorEvent t) {
                            if (t.getMediaError() != null) {
                                popMediaError(t.getMediaError().toString());
                            }
                            player.stop();
                        }
                    });
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                    popMediaError(e.toString());
                }
            }
        });
    }

    protected void updateStatus() {
        Platform.runLater(new Runnable() {
            @Override
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
                        ? DateTools.timeMsDuration((long) elapsed.toMillis())
                        : DateTools.timeDuration((long) elapsed.toMillis())
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
                        ? DateTools.timeMsDuration((long) left.toMillis())
                        : DateTools.timeDuration((long) left.toMillis())
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
                    = (MediaListController) openStage(Fxmls.MediaListFxml);
            controller.setPlayerController(this);
            controller.loadList(tableController.mediaListName);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
