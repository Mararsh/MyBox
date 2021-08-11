package mara.mybox.controller;

import java.io.File;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javax.sound.sampled.Clip;
import mara.mybox.db.data.AlarmClock;
import static mara.mybox.db.data.AlarmClock.getTypeString;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.value.Languages;

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
    protected Label descLabel, soundLabel, timeLabel;

    public AlarmClockRunController() {
        baseTitle = Languages.message("AlarmClock");

    }

    @FXML
    public void manageAction(ActionEvent event) {
        knowAction(event);
        AlarmClockController.oneOpen();
    }

    public void inactive(ActionEvent event) {
        alarm.setIsActive(false);
        alarm.setStatus(Languages.message("Inactive"));
        alarm.setNextTime(-1);
        alarm.setNext("");
        AlarmClock.scheduleAlarmClock(alarm);
        AlarmClock.writeAlarmClock(alarm);
        knowAction(event);
        AlarmClockController controller = AlarmClockController.oneOpen();
        if (controller != null) {
            controller.alertClockTableController.refreshAction();
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
                soundString += Languages.message("Continually");
            } else {
                soundString += Languages.message("LoopTimes") + " " + alarm.getSoundLoopTimes();
            }
        }
        soundLabel.setText(soundString);
        String typeString = getTypeString(alarm);
        if (alarm.getNext() != null) {
            typeString += "     " + Languages.message("NextTime") + " " + alarm.getNext();
        }
        timeLabel.setText(typeString);
        playTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    String sound = alarm.getSound();
                    if (Languages.message("Meow").equals(sound)) {
                        File miao = FxFileTools.getInternalFile("/sound/miao4.mp3", "sound", "miao4.mp3");
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
        thread.setDaemon(false);
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
    public void cleanPane() {
        try {
            if (playTask != null && !playTask.isDone()) {
                playTask.cancel();
                playTask = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
