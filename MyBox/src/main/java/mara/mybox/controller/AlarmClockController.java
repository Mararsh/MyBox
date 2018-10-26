package mara.mybox.controller;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AlarmClock;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.SoundTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-7-13
 * @Description
 * @License Apache License Version 2.0
 */
public class AlarmClockController extends BaseController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ToggleGroup typeGroup, soundGroup, unitGroup, loopGroup;
    @FXML
    private TextField descInput, startInput, everyInput, wavInput, mp3Input, loopInput, urlInput;
    @FXML
    private CheckBox activeCheck, loopCheck;
    @FXML
    private Button playButton, pauseButton, saveButton;
    @FXML
    protected Pane alertClockTable;
    @FXML
    protected AlarmClockTableController alertClockTableController;
    @FXML
    protected Slider volumeSlider;
    @FXML
    protected RadioButton miaoButton, wavButton, mp3Button, internetButton, continuallyButton, loopButton;

    protected List<AlarmClock> alarmClocks;
    protected final String AlertClocksFileKey, SystemMediaPathKey, MusicPathKey;
    protected Clip player;
    protected int repeatType, everyValue, loopValue;
    protected long currentKey, startTime;
    protected AlarmClock currentAlarm;
    protected boolean isEdit, isPaused, isContinully, isURL;
    protected float volumeValue;
    protected URL currentURL;
    protected File currentSound, miao;

    public AlarmClockController() {
        AlertClocksFileKey = "FileTargetPath";
        SystemMediaPathKey = "SystemMediaPath";
        MusicPathKey = "MusicPath";

        fileExtensionFilter = CommonValues.SoundExtensionFilter;
    }

    @Override
    protected void initializeNext() {
        try {
            AppVaribles.alarmClockController = this;
            miao = FxmlTools.getUserFile(getClass(), "/sound/miao4.mp3", "miao4.mp3");

            alertClockTableController.setAlarmClockController(this);

            startInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    Date d = DateTools.stringToDatetime(startInput.getText());
                    if (d == null) {
                        startInput.setStyle(badStyle);
                        startTime = -1;
                    } else {
                        startInput.setStyle(null);
                        startTime = d.getTime();
                    }
                }
            });
            startInput.setText(DateTools.datetimeToString(new Date().getTime() + 300000));

            soundGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSound();
                }
            });
            mp3Input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSound();
                }
            });
            wavInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSound();
                }
            });
            urlInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSound();
                }
            });
            checkSound();

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            everyInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkType();
                }
            });
            unitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            loopGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLoop();
                }
            });
            loopInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkLoop();
                }
            });
            checkLoop();

            saveButton.disableProperty().bind(
                    wavInput.styleProperty().isEqualTo(badStyle)
                            .or(mp3Input.styleProperty().isEqualTo(badStyle))
                            .or(everyInput.styleProperty().isEqualTo(badStyle))
                            .or(startInput.styleProperty().isEqualTo(badStyle))
                            .or(loopInput.styleProperty().isEqualTo(badStyle))
                            .or(urlInput.styleProperty().isEqualTo(badStyle))
            );

            FloatControl control = SoundTools.getControl(miao);
            volumeSlider.setMax(control.getMaximum());
            volumeSlider.setMin(control.getMinimum());
            volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    volumeValue = newValue.intValue();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkSound() {
        RadioButton selected = (RadioButton) soundGroup.getSelectedToggle();
        isURL = false;
        mp3Input.setStyle(null);
        wavInput.setStyle(null);
        urlInput.setStyle(null);
        currentURL = null;
        currentSound = null;
        if (getMessage("LocalMusic").equals(selected.getText())) {
            final File file = new File(mp3Input.getText());
            if (!file.exists() || !file.isFile() || !file.getName().endsWith(".mp3")) {
                mp3Input.setStyle(badStyle);
            } else {
                currentSound = file;
            }

        } else if (getMessage("InternetMusic").equals(selected.getText())) {
            try {
                currentURL = new URL(urlInput.getText());
                isURL = true;
            } catch (Exception e) {
                urlInput.setStyle(badStyle);
            }

        } else if (getMessage("SystemSounds").equals(selected.getText())) {
            final File file = new File(wavInput.getText());
            if (!file.exists() || !file.isFile() || !file.getName().endsWith(".wav")) {
                wavInput.setStyle(badStyle);
            } else {
                currentSound = file;
            }

        } else {
            currentSound = miao;
        }

        if (currentSound == null && currentURL == null) {
            playButton.setDisable(true);
        } else {
            playButton.setDisable(false);
            FloatControl control = null;
            if (currentSound != null) {
                control = SoundTools.getControl(currentSound);
            } else if (currentURL != null) {
//                control = SoundTools.getControl(currentURL);
            }
            if (control != null) {
                volumeSlider.setMax(control.getMaximum());
                volumeSlider.setMin(control.getMinimum());
                volumeSlider.setValue(0);
            }
        }
        playButton.setText(getMessage("Play"));
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playSound(event);
            }
        });
        pauseButton.setDisable(true);
        pauseButton.setText(getMessage("Pause"));
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pauseSound(event);
            }
        });
    }

    protected void checkType() {
        RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
        everyInput.setStyle(null);
        if (getMessage("WorkingDays").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.WorkingDays;
        } else if (getMessage("EveryDay").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.EveryDay;
        } else if (getMessage("Weekend").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.Weekend;
        } else if (getMessage("NotRepeat").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.NotRepeat;
        } else if (getMessage("Every").equals(selected.getText())) {
            RadioButton unit = (RadioButton) unitGroup.getSelectedToggle();
            if (getMessage("Days").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeDays;
            } else if (getMessage("Hours").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeHours;
            } else if (getMessage("Minutes").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeMinutes;
            } else if (getMessage("Seconds").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeSeconds;
            }
            try {
                everyValue = Integer.valueOf(everyInput.getText());
                if (everyValue <= 0) {
                    everyInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                everyInput.setStyle(badStyle);
            }
        }
    }

    protected void checkLoop() {
        RadioButton selected = (RadioButton) loopGroup.getSelectedToggle();
        if (getMessage("Continually").equals(selected.getText())) {
            isContinully = true;
            loopInput.setStyle(null);
        } else {
            isContinully = false;
            try {
                loopValue = Integer.valueOf(loopInput.getText());
                loopInput.setStyle(null);
            } catch (Exception e) {
                loopValue = 0;
                loopInput.setStyle(badStyle);
            }
        }
    }

    @FXML
    private void playSound(ActionEvent event) {
        if (currentSound == null && currentURL == null) {
            return;
        }
        play(currentSound, null);
    }

    protected void play(final File file, final URL url) {
        if (file == null && url == null) {
            return;
        }
        Task playTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    if (player != null) {
                        player.close();
                    }
                    if (file != null) {
                        player = SoundTools.playback(file, volumeValue);
                    } else {
                        player = SoundTools.playback(url, volumeValue);
                    }
                    if (loopCheck.isSelected()) {
                        if (isContinully) {
                            player.loop(Clip.LOOP_CONTINUOUSLY);
                        } else {
                            player.loop(loopValue - 1);
                        }
                    }
                    player.addLineListener(new LineListener() {
                        @Override
                        public void update(LineEvent e) {
                            if (e.getType() == LineEvent.Type.STOP && !isPaused) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeSound();
                                    }
                                });
                            }
                        }
                    });
                    player.start();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setText(AppVaribles.getMessage("Stop"));
                            playButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    closeSound();
                                }
                            });
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.getMessage("Pause"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseSound(event);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                }
                return null;
            }
        };
        Thread thread = new Thread(playTask);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    private void closeSound() {
        playButton.setText(AppVaribles.getMessage("Play"));
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playSound(event);
            }
        });
        pauseButton.setDisable(true);
        pauseButton.setText(AppVaribles.getMessage("Pause"));
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pauseSound(event);
            }
        });
        if (player != null) {
            player.stop();
            player.drain();
            player.close();
            player = null;
        }
    }

    @FXML
    private void pauseSound(ActionEvent event) {
        isPaused = true;
        if (player == null) {
            closeSound();
            return;
        }
        playButton.setText(AppVaribles.getMessage("Stop"));
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closeSound();
            }
        });
        pauseButton.setDisable(false);
        pauseButton.setText(AppVaribles.getMessage("Continue"));
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                continueSound(event);

            }
        });
        player.stop();
    }

    private void continueSound(ActionEvent event) {
        isPaused = false;
        if (player == null) {
            closeSound();
            return;
        }
        playButton.setText(AppVaribles.getMessage("Stop"));
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closeSound();
            }
        });
        pauseButton.setDisable(false);
        pauseButton.setText(AppVaribles.getMessage("Pause"));
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pauseSound(event);
            }
        });
        player.start();
    }

    @FXML
    private void selectSound(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            String defaultPath = CommonValues.UserFilePath;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                defaultPath = "C:\\Windows\\media";
            }
            File path = new File(AppVaribles.getConfigValue(SystemMediaPathKey, defaultPath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("wav", "*.wav"));
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, file.getParent());
            AppVaribles.setConfigValue(SystemMediaPathKey, file.getParent());

            wavInput.setText(file.getAbsolutePath());
            play(file, null);
        } catch (Exception e) {
//            logger.error(e.toString());
        }

    }

    @FXML
    private void selectMusic(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(MusicPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, file.getParent());
            AppVaribles.setConfigValue(MusicPathKey, file.getParent());

            mp3Input.setText(file.getAbsolutePath());
            play(file, null);

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    private void saveAlarm(ActionEvent event) {
        if (currentAlarm == null || !isEdit) {
            currentAlarm = new AlarmClock();
            currentAlarm.setKey(new Date().getTime());
        }
        if (repeatType == AlarmClock.AlarmType.NotRepeat
                && startTime <= new Date().getTime()) {
            alertInformation(getMessage("AlarmNeverHappen"));
            return;
        }
        currentAlarm.setAlarmType(repeatType);
        currentAlarm.setDescription(descInput.getText());
        currentAlarm.setStartTime(startTime);
        currentAlarm.setIsActive(activeCheck.isSelected());
        if (miao.getAbsolutePath().equals(currentSound.getAbsolutePath())) {
            currentAlarm.setSound(getMessage("meow"));
        } else {
            currentAlarm.setSound(currentSound.getAbsolutePath());
        }
        currentAlarm.setEveryValue(everyValue);
        currentAlarm.setIsSoundLoop(loopCheck.isSelected());
        currentAlarm.setIsSoundContinully(isContinully);
        currentAlarm.setSoundLoopTimes(loopValue);
        currentAlarm.setVolume(volumeValue);

        alertClockTableController.saveAlarm(currentAlarm, !isEdit);
        reset();

    }

    protected void reset() {
        descInput.setText("");
        startInput.setText(DateTools.datetimeToString(new Date().getTime() + 300000));
        saveButton.setText(getMessage("Add"));
        isEdit = false;
        currentAlarm = null;
        closeSound();
    }

    protected void edit(AlarmClock alarm) {
        isEdit = true;
        currentAlarm = alarm;
        descInput.setText(alarm.getDescription());
        startInput.setText(alarm.getStart());
        activeCheck.setSelected(alarm.isIsActive());
        String type = AlarmClock.getTypeString(alarm.getAlarmType());
        ObservableList<Toggle> tbuttons = typeGroup.getToggles();
        for (Toggle button : tbuttons) {
            RadioButton radioButton = (RadioButton) button;
            if (radioButton.getText().equals(type)) {
                button.setSelected(true);
                break;
            }
        }
        if (AppVaribles.getMessage("Every").equals(type)) {
            String unit = AlarmClock.getTypeUnit(alarm.getAlarmType());
            ObservableList<Toggle> ubuttons = unitGroup.getToggles();
            for (Toggle button : ubuttons) {
                RadioButton radioButton = (RadioButton) button;
                if (radioButton.getText().equals(unit)) {
                    button.setSelected(true);
                    break;
                }
            }
            everyInput.setText(alarm.getEveryValue() + "");
        }
        String sound = alarm.getSound();
        if (getMessage("meow").equals(sound)) {
            miaoButton.setSelected(true);
            sound = miao.getAbsolutePath();
        } else if (sound.endsWith(".mp3")) {
            mp3Button.setSelected(true);
            mp3Input.setText(sound);
        } else if (sound.endsWith(".wav")) {
            wavButton.setSelected(true);
            wavInput.setText(sound);
        }
        loopCheck.setSelected(alarm.isIsSoundLoop());
        if (alarm.isIsSoundContinully()) {
            continuallyButton.setSelected(true);
        } else {
            loopButton.setSelected(true);
        }
        loopInput.setText(alarm.getSoundLoopTimes() + "");
        FloatControl control = SoundTools.getControl(new File(sound));
        if (control != null) {
            volumeSlider.setMax(control.getMaximum());
            volumeSlider.setMin(control.getMinimum());
        }
        volumeSlider.setValue(alarm.getVolume());

        saveButton.setText(getMessage("Save"));
    }

    @Override
    public boolean stageClosing() {
//        logger.debug("stageClosing");
        if (player != null) {
            player.stop();
            player.drain();
            player.close();
            player = null;
        }
        return super.stageClosing();
    }

    public boolean isIsEdit() {
        return isEdit;
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public Clip getPlayer() {
        return player;
    }

    public void setPlayer(Clip player) {
        this.player = player;
    }

    public Pane getAlertClockTable() {
        return alertClockTable;
    }

    public void setAlertClockTable(Pane alertClockTable) {
        this.alertClockTable = alertClockTable;
    }

    public AlarmClockTableController getAlertClockTableController() {
        return alertClockTableController;
    }

    public void setAlertClockTableController(AlarmClockTableController alertClockTableController) {
        this.alertClockTableController = alertClockTableController;
    }

}
