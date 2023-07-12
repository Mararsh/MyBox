package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    protected String currentSound;
    protected File miao;
    protected MediaPlayer mediaPlayer;

    @FXML
    protected ToggleGroup typeGroup, soundGroup, unitGroup;
    @FXML
    protected TextField descInput, startInput, everyInput, sysInput, localInput, loopInput, urlInput;
    @FXML
    protected CheckBox activeCheck;
    @FXML
    protected Pane alertClockTable;
    @FXML
    protected AlarmClockTableController alertClockTableController;
    @FXML
    protected Slider volumeSlider;
    @FXML
    protected RadioButton miaoRadio, sysButton, localButton, internetButton;

    public AlarmClockController() {
        baseTitle = Languages.message("AlarmClock");

        AlertClocksFileKey = "FileTargetPath";
        SystemMediaPathKey = "SystemMediaPath";
        MusicPathKey = "MusicPath";

        sourceExtensionFilter = FileFilters.SoundExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            AppVariables.alarmClockController = this;
            miao = FxFileTools.getInternalFile("/sound/guaiMiao3.mp3", "sound", "guaiMiao3.mp3");

            alertClockTableController.setAlarmClockController(this);

            startInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    Date d = DateTools.encodeDate(startInput.getText(), -1);
                    if (d == null) {
                        startInput.setStyle(UserConfig.badStyle());
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
                    sysInput.styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(localInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(everyInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(startInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(loopInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(urlInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

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
            MyBoxLog.error(e);
        }

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(saveButton, new Tooltip("F2 / CTRL+s"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSound() {
        RadioButton selected = (RadioButton) soundGroup.getSelectedToggle();
        localInput.setStyle(null);
        sysInput.setStyle(null);
        urlInput.setStyle(null);
        currentSound = null;
        if (Languages.message("LocalMusic").equals(selected.getText())) {
            final File file = new File(localInput.getText());
            if (!file.exists() || !file.isFile()) {
                localInput.setStyle(UserConfig.badStyle());
            } else {
                currentSound = file.getAbsolutePath();
            }

        } else if (Languages.message("InternetMusic").equals(selected.getText())) {
            try {
                currentSound = urlInput.getText();
            } catch (Exception e) {
                urlInput.setStyle(UserConfig.badStyle());
            }

        } else if (Languages.message("SystemSounds").equals(selected.getText())) {
            final File file = new File(sysInput.getText());
            if (!file.exists() || !file.isFile()) {
                sysInput.setStyle(UserConfig.badStyle());
            } else {
                currentSound = file.getAbsolutePath();
            }

        } else {
            currentSound = miao.getAbsolutePath();
        }

        playButton.setDisable(currentSound == null);
        playSound();
    }

    protected void checkType() {
        RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
        everyInput.setStyle(null);
//        if (Languages.message("WorkingDays").equals(selected.getText())) {
//            repeatType = AlarmClock.AlarmType.WorkingDays;
//        } else if (Languages.message("EveryDay").equals(selected.getText())) {
//            repeatType = AlarmClock.AlarmType.EveryDay;
//        } else if (Languages.message("Weekend").equals(selected.getText())) {
//            repeatType = AlarmClock.AlarmType.Weekend;
//        } else if (Languages.message("NotRepeat").equals(selected.getText())) {
//            repeatType = AlarmClock.AlarmType.NotRepeat;
//        } else if (Languages.message("Every").equals(selected.getText())) {
//            RadioButton unit = (RadioButton) unitGroup.getSelectedToggle();
//            if (Languages.message("Days").equals(unit.getText())) {
//                repeatType = AlarmClock.AlarmType.EverySomeDays;
//            } else if (Languages.message("Hours").equals(unit.getText())) {
//                repeatType = AlarmClock.AlarmType.EverySomeHours;
//            } else if (Languages.message("Minutes").equals(unit.getText())) {
//                repeatType = AlarmClock.AlarmType.EverySomeMinutes;
//            } else if (Languages.message("Seconds").equals(unit.getText())) {
//                repeatType = AlarmClock.AlarmType.EverySomeSeconds;
//            }
//            try {
//                everyValue = Integer.parseInt(everyInput.getText());
//                if (everyValue <= 0) {
//                    everyInput.setStyle(UserConfig.badStyle());
//                }
//            } catch (Exception e) {
//                everyInput.setStyle(UserConfig.badStyle());
//            }
//        }
    }

    protected void checkLoop() {
        try {
            loopValue = Integer.parseInt(loopInput.getText());
            loopInput.setStyle(null);
        } catch (Exception e) {
            loopValue = 0;
            loopInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    protected void playSound() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (currentSound == null) {
            return;
        }
        File file = new File(currentSound);
        if (file.exists() && file.isFile()) {
            SoundTools.playClip(new File(currentSound));
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        mediaPlayer = SoundTools.play(currentSound, volumeValue, loopValue);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        popError(e.toString());
                    }
                }
            });
        }
    }

    @FXML
    protected void selectSys() {
        try {
            String dPath = AppVariables.MyboxDataPath;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                dPath = "C:\\Windows\\media";
            }
            File path = UserConfig.getPath(SystemMediaPathKey, dPath);
            List<FileChooser.ExtensionFilter> wavExtensionFilter = new ArrayList<>();
            wavExtensionFilter.add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            File file = FxFileTools.selectFile(this, path, wavExtensionFilter);
            if (file == null) {
                return;
            }
            UserConfig.setString(SystemMediaPathKey, file.getParent());
            sysInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }

    }

    @FXML
    protected void selectLocal(ActionEvent event) {
        try {
            File file = FxFileTools.selectFile(this,
                    UserConfig.getPath(MusicPathKey), FileFilters.SoundExtensionFilter);
            if (file == null) {
                return;
            }
            UserConfig.setString(MusicPathKey, file.getParent());
            localInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAction() {
//        if (currentAlarm == null || !isEdit) {
//            currentAlarm = new AlarmClock();
//            currentAlarm.setKey(new Date().getTime());
//        }
////        if (repeatType == AlarmClock.AlarmType.NotRepeat
////                && startTime <= new Date().getTime()) {
////            alertInformation(Languages.message("AlarmNeverHappen"));
////            return;
////        }
//        currentAlarm.setAlarmType(repeatType);
//        currentAlarm.setDescription(descInput.getText());
//        currentAlarm.setStartTime(startTime);
//        currentAlarm.setIsActive(activeCheck.isSelected());
//        if (currentSound.contains(miao.getAbsolutePath())) {
//            currentAlarm.setSound(Languages.message("Meow"));
//        } else {
//            currentAlarm.setSound(currentSound);
//        }
        currentAlarm.setEveryValue(everyValue);
        currentAlarm.setIsSoundLoop(loopValue > 0);
        currentAlarm.setIsSoundContinully(loopValue < 0);
        currentAlarm.setSoundLoopTimes(loopValue);
        currentAlarm.setVolume(volumeValue);

//        alertClockTableController.saveAlarm(currentAlarm, !isEdit);
        reset();

    }

    protected void reset() {
        descInput.setText("");
        startInput.setText(DateTools.datetimeToString(new Date().getTime() + 300000));
        isEdit = false;
        currentAlarm = null;
    }

    protected void edit(AlarmClock alarm) {
        isEdit = true;
        currentAlarm = alarm;
//        descInput.setText(alarm.getDescription());
//        startInput.setText(alarm.getStart());
//        activeCheck.setSelected(alarm.isIsActive());
//        String type = AlarmClock.getTypeString(alarm.getAlarmType());
//        ObservableList<Toggle> tbuttons = typeGroup.getToggles();
//        for (Toggle button : tbuttons) {
//            RadioButton radioButton = (RadioButton) button;
//            if (radioButton.getText().equals(type)) {
//                button.setSelected(true);
//                break;
//            }
//        }
//        if (Languages.message("Every").equals(type)) {
//            String unit = AlarmClock.getTypeUnit(alarm.getAlarmType());
//            ObservableList<Toggle> ubuttons = unitGroup.getToggles();
//            for (Toggle button : ubuttons) {
//                RadioButton radioButton = (RadioButton) button;
//                if (radioButton.getText().equals(unit)) {
//                    button.setSelected(true);
//                    break;
//                }
//            }
//            everyInput.setText(alarm.getEveryValue() + "");
//        }
        String sound = alarm.getSound();
        if (Languages.message("meow").equals(sound)) {
            miaoRadio.setSelected(true);
            sound = miao.getAbsolutePath();
        } else if (sound.endsWith(".mp3")) {
            localButton.setSelected(true);
            localInput.setText(sound);
        } else if (sound.endsWith(".wav")) {
            sysButton.setSelected(true);
            sysInput.setText(sound);
        }
        loopInput.setText(alarm.getSoundLoopTimes() + "");
        volumeSlider.setValue(alarm.getVolume());
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
    public void cleanPane() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static AlarmClockController oneOpen() {
        AlarmClockController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof AlarmClockController) {
                try {
                    controller = (AlarmClockController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (AlarmClockController) WindowTools.openStage(Fxmls.AlarmClockFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
