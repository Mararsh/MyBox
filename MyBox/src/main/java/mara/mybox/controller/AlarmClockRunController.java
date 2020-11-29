package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javax.sound.sampled.Clip;
import mara.mybox.data.AlarmClock;
import static mara.mybox.data.AlarmClock.getTypeString;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.SoundTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-15
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class AlarmClockRunController extends BaseController {

    private AlarmClock alarm;
    private Clip player;
    private Task playTask;

    @FXML
    private Label descLabel, soundLabel, timeLabel;

    public AlarmClockRunController() {
        baseTitle = AppVariables.message("AlarmClock");

    }

    @FXML
    public void manageAction(ActionEvent event) {
        knowAction(event);
        Stage stage = FxmlStage.findStage(message("AlarmClock"));
        if (stage != null) {
            stage.toFront();
            stage.requestFocus();
        } else {
            openStage(CommonValues.AlarmClockFxml);
        }
    }

    public void inactive(ActionEvent event) {
        alarm.setIsActive(false);
        alarm.setStatus(AppVariables.message("Inactive"));
        alarm.setNextTime(-1);
        alarm.setNext("");
        AlarmClock.scheduleAlarmClock(alarm);
        AlarmClock.writeAlarmClock(alarm);
        knowAction(event);
        List<Stage> stages = FxmlStage.findStages(message("AlarmClock"));
        if (stages != null) {
            for (Stage stage : stages) {
                if (stage.getUserData() != null) {
                    try {
                        AlarmClockController controller = (AlarmClockController) (stage.getUserData());
                        controller.alertClockTableController.refreshAction();
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            }
        }
    }

    @FXML
    public void knowAction(ActionEvent event) {
        if (player != null) {
            player.stop();
            player.drain();
            player.close();
            player = null;
        }
        closeStage();
    }

    public void runAlarm(final AlarmClock alarm) {
        this.alarm = alarm;
        getMyStage().setTitle(baseTitle + " - " + alarm.getDescription());
        descLabel.setText(alarm.getDescription());
        String soundString = alarm.getSound() + "   ";
        if (alarm.isIsSoundLoop()) {
            if (alarm.isIsSoundContinully()) {
                soundString += AppVariables.message("Continually");
            } else {
                soundString += AppVariables.message("LoopTimes") + " " + alarm.getSoundLoopTimes();
            }
        }
        soundLabel.setText(soundString);
        String typeString = getTypeString(alarm);
        if (alarm.getNext() != null) {
            typeString += "     " + AppVariables.message("NextTime") + " " + alarm.getNext();
        }
        timeLabel.setText(typeString);
        playTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    String sound = alarm.getSound();
                    if (AppVariables.message("Meow").equals(sound)) {
                        File miao = FxmlControl.getInternalFile("/sound/miao4.mp3", "sound", "miao4.mp3");
                        sound = miao.getAbsolutePath();
                    }
                    player = SoundTools.playback(sound, alarm.getVolume());
                    if (alarm.isIsSoundLoop()) {
                        if (alarm.isIsSoundContinully()) {
                            player.loop(Clip.LOOP_CONTINUOUSLY);
                        } else {
                            player.loop(alarm.getSoundLoopTimes() - 1);
                        }
                    }
                    player.start();
                } catch (Exception e) {
                }
                return null;
            }
        };
        Thread thread = new Thread(playTask);
        thread.setDaemon(true);
        thread.start();

    }

    public AlarmClock getAlarm() {
        return alarm;
    }

    public void setAlarm(AlarmClock alarm) {
        this.alarm = alarm;
    }

    public Label getDescLabel() {
        return descLabel;
    }

    public void setDescLabel(Label descLabel) {
        this.descLabel = descLabel;
    }

    public Label getSoundLabel() {
        return soundLabel;
    }

    public void setSoundLabel(Label soundLabel) {
        this.soundLabel = soundLabel;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(Label timeLabel) {
        this.timeLabel = timeLabel;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (playTask != null && playTask.isRunning()) {
            playTask.cancel();
            playTask = null;
        }
        return true;
    }

}
