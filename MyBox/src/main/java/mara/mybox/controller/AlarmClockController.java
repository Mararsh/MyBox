package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import mara.mybox.data.AlarmClock;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MediaTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-13
 * @Description
 * @License Apache License Version 2.0
 */
public class AlarmClockController extends BaseController {

    protected List<AlarmClock> alarmClocks;
    protected final String AlertClocksFileKey, SystemMediaPathKey, MusicPathKey;
    protected int repeatType, everyValue, loopValue;
    protected long currentKey, startTime;
    protected AlarmClock currentAlarm;
    protected boolean isEdit, isPaused;
    protected float volumeValue;
    protected URI currentSound;
    protected File miao;
    protected MediaPlayer mediaPlayer;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ToggleGroup typeGroup, soundGroup, unitGroup;
    @FXML
    private TextField descInput, startInput, everyInput, sysInput, localInput, loopInput, urlInput;
    @FXML
    private CheckBox activeCheck, loopCheck;
    @FXML
    protected Pane alertClockTable;
    @FXML
    protected AlarmClockTableController alertClockTableController;
    @FXML
    protected Slider volumeSlider;
    @FXML
    protected RadioButton miaoRadio, sysButton, localButton, internetButton, continuallyButton, loopButton;

    public AlarmClockController() {
        baseTitle = AppVariables.message("AlarmClock");

        AlertClocksFileKey = "FileTargetPath";
        SystemMediaPathKey = "SystemMediaPath";
        MusicPathKey = "MusicPath";

        sourceExtensionFilter = CommonFxValues.SoundExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            AppVariables.alarmClockController = this;
            miao = FxmlControl.getInternalFile("/sound/guaiMiao3.mp3", "sound", "guaiMiao3.mp3");

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

            localInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSound();
                }
            });

            sysInput.textProperty().addListener(new ChangeListener<String>() {
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

            loopInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkLoop();
                }
            });
            checkLoop();

            saveButton.disableProperty().bind(
                    sysInput.styleProperty().isEqualTo(badStyle)
                            .or(localInput.styleProperty().isEqualTo(badStyle))
                            .or(everyInput.styleProperty().isEqualTo(badStyle))
                            .or(startInput.styleProperty().isEqualTo(badStyle))
                            .or(loopInput.styleProperty().isEqualTo(badStyle))
                            .or(urlInput.styleProperty().isEqualTo(badStyle))
            );

            FxmlControl.setTooltip(saveButton, new Tooltip("F2 / CTRL+s"));

            volumeValue = 1.0f;
            volumeSlider.setMax(100);
            volumeSlider.setMin(0);
            volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    volumeValue = newValue.intValue() / 100f;
                }
            });

            playButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    playSound();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkSound() {
        RadioButton selected = (RadioButton) soundGroup.getSelectedToggle();
        localInput.setStyle(null);
        sysInput.setStyle(null);
        urlInput.setStyle(null);
        currentSound = null;
        if (message("LocalMusic").equals(selected.getText())) {
            final File file = new File(localInput.getText());
            if (!file.exists() || !file.isFile()) {
                localInput.setStyle(badStyle);
            } else {
                currentSound = file.toURI();
            }

        } else if (message("InternetMusic").equals(selected.getText())) {
            try {
                currentSound = new URI(urlInput.getText());
            } catch (Exception e) {
                urlInput.setStyle(badStyle);
            }

        } else if (message("SystemSounds").equals(selected.getText())) {
            final File file = new File(sysInput.getText());
            if (!file.exists() || !file.isFile()) {
                sysInput.setStyle(badStyle);
            } else {
                currentSound = file.toURI();
            }

        } else {
            currentSound = miao.toURI();
        }

        playButton.setDisable(currentSound == null);
        playSound();
    }

    protected void checkType() {
        RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
        everyInput.setStyle(null);
        if (message("WorkingDays").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.WorkingDays;
        } else if (message("EveryDay").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.EveryDay;
        } else if (message("Weekend").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.Weekend;
        } else if (message("NotRepeat").equals(selected.getText())) {
            repeatType = AlarmClock.AlarmType.NotRepeat;
        } else if (message("Every").equals(selected.getText())) {
            RadioButton unit = (RadioButton) unitGroup.getSelectedToggle();
            if (message("Days").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeDays;
            } else if (message("Hours").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeHours;
            } else if (message("Minutes").equals(unit.getText())) {
                repeatType = AlarmClock.AlarmType.EverySomeMinutes;
            } else if (message("Seconds").equals(unit.getText())) {
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
        try {
            loopValue = Integer.valueOf(loopInput.getText());
            loopInput.setStyle(null);
        } catch (Exception e) {
            loopValue = 0;
            loopInput.setStyle(badStyle);
        }
    }

    @FXML
    private void playSound() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (currentSound == null) {
            return;
        }
        if (currentSound.getScheme().startsWith("file")) {
            FxmlControl.playClip(new File(currentSound.getPath()));
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        mediaPlayer = MediaTools.play(currentSound.toString(), volumeValue, loopValue);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            });
        }
    }

    @FXML
    private void selectSys() {
        try {
            final FileChooser fileChooser = new FileChooser();
            String defaultPath = AppVariables.MyboxDataPath;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                defaultPath = "C:\\Windows\\media";
            }
            File path = AppVariables.getUserConfigPath(SystemMediaPathKey, defaultPath);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("wav", "*.wav"));
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            recordFileOpened(file);
            AppVariables.setUserConfigValue(SystemMediaPathKey, file.getParent());

            sysInput.setText(file.getAbsolutePath());

        } catch (Exception e) {
//            logger.error(e.toString());
        }

    }

    @FXML
    private void selectLocal(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVariables.getUserConfigPath(MusicPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonFxValues.SoundExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            recordFileOpened(file);
            AppVariables.setUserConfigValue(MusicPathKey, file.getParent());

            localInput.setText(file.getAbsolutePath());

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (currentAlarm == null || !isEdit) {
            currentAlarm = new AlarmClock();
            currentAlarm.setKey(new Date().getTime());
        }
        if (repeatType == AlarmClock.AlarmType.NotRepeat
                && startTime <= new Date().getTime()) {
            alertInformation(message("AlarmNeverHappen"));
            return;
        }
        currentAlarm.setAlarmType(repeatType);
        currentAlarm.setDescription(descInput.getText());
        currentAlarm.setStartTime(startTime);
        currentAlarm.setIsActive(activeCheck.isSelected());
        if (currentSound.toString().contains(miao.getAbsolutePath())) {
            currentAlarm.setSound(message("meow"));
        } else {
            currentAlarm.setSound(currentSound.toString());
        }
        currentAlarm.setEveryValue(everyValue);
        currentAlarm.setIsSoundLoop(loopCheck.isSelected());
        currentAlarm.setIsSoundContinully(loopValue < 0);
        currentAlarm.setSoundLoopTimes(loopValue);
        currentAlarm.setVolume(volumeValue);

        alertClockTableController.saveAlarm(currentAlarm, !isEdit);
        reset();

    }

    protected void reset() {
        descInput.setText("");
        startInput.setText(DateTools.datetimeToString(new Date().getTime() + 300000));
        saveButton.setText(message("Add"));
        isEdit = false;
        currentAlarm = null;
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
        if (AppVariables.message("Every").equals(type)) {
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
        if (message("meow").equals(sound)) {
            miaoRadio.setSelected(true);
            sound = miao.getAbsolutePath();
        } else if (sound.endsWith(".mp3")) {
            localButton.setSelected(true);
            localInput.setText(sound);
        } else if (sound.endsWith(".wav")) {
            sysButton.setSelected(true);
            sysInput.setText(sound);
        }
        loopCheck.setSelected(alarm.isIsSoundLoop());
        if (alarm.isIsSoundContinully()) {
            continuallyButton.setSelected(true);
        } else {
            loopButton.setSelected(true);
        }
        loopInput.setText(alarm.getSoundLoopTimes() + "");
        volumeSlider.setValue(alarm.getVolume());

        saveButton.setText(message("Save"));
    }

    public boolean isIsEdit() {
        return isEdit;
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
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

    @Override
    public boolean checkBeforeNextAction() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        return true;
    }

}
