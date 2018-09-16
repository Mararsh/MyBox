package mara.mybox.controller;

import java.io.File;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javax.sound.sampled.Clip;
import mara.mybox.objects.AlarmClock;
import static mara.mybox.objects.AlarmClock.getTypeString;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.SoundTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-15
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class AlarmClockRunController extends BaseController {

    private AlarmClock alarm;
    private Clip player;

    @FXML
    private Label descLabel, soundLabel, timeLabel;

    @Override
    protected void initializeNext() {
        try {
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void manageAction(ActionEvent event) {
        openStage(CommonValues.AlarmClockFxml, false, true);
        knowAction(event);
    }

    @FXML
    private void knowAction(ActionEvent event) {
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
        descLabel.setText(alarm.getDescription());
        String soundString = alarm.getSound() + "   ";
        if (alarm.isIsSoundLoop()) {
            if (alarm.isIsSoundContinully()) {
                soundString += AppVaribles.getMessage("Continually");
            } else {
                soundString += AppVaribles.getMessage("LoopTimes") + " " + alarm.getSoundLoopTimes();
            }
        }
        soundLabel.setText(soundString);
        String typeString = getTypeString(alarm);
        if (alarm.getNext() != null) {
            typeString += "     " + AppVaribles.getMessage("NextTime") + " " + alarm.getNext();
        }
        timeLabel.setText(typeString);
        Task playTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    String sound = alarm.getSound();
                    if (AppVaribles.getMessage("meow").equals(sound)) {
                        File miao = FxmlTools.getUserFile(getClass(), "/sound/miao4.mp3", "miao4.mp3");
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

}
