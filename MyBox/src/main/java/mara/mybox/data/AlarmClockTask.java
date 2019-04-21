package mara.mybox.data;

import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import mara.mybox.controller.AlarmClockRunController;
import mara.mybox.data.AlarmClock.AlarmType;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.scheduledTasks;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-16
 * @Description
 * @License Apache License Version 2.0
 */
public class AlarmClockTask extends TimerTask {

    protected AlarmClock alarm;

    protected TimeUnit timeUnit = TimeUnit.SECONDS;
    protected long delay, period;

    public AlarmClockTask(AlarmClock alarm) {
        this.alarm = alarm;
        AlarmClock.calculateNextTime(alarm);
        delay = alarm.getNextTime() - new Date().getTime();
        period = alarm.getPeriod();
    }

    @Override
    public void run() {
        try {
//            logger.debug("call");
            if (!canTriggerAlarm(alarm)) {
                logger.debug("Can not tigger alarm due to not satisfied");
                return;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        final AlarmClockRunController controller = (AlarmClockRunController) FxmlStage.openStage(getClass(),
                                null, CommonValues.AlarmClockRunFxml, false);
                        controller.runAlarm(alarm);

                        if (AppVaribles.alarmClockController != null
                                && AppVaribles.alarmClockController.getAlertClockTableController() != null) {
                            AppVaribles.alarmClockController.getAlertClockTableController().refreshAction();
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            });

            if (alarm.getAlarmType() == AlarmType.NotRepeat) {
                ScheduledFuture future = scheduledTasks.get(alarm.getKey());
                if (future != null) {
                    future.cancel(true);
                    scheduledTasks.remove(alarm.getKey());
                }
                alarm.setIsActive(false);
                AlarmClock.writeAlarmClock(alarm);
            } else {
                alarm.setLastTime(new Date().getTime());
                alarm.setNextTime(-1);
                AlarmClock.calculateNextTime(alarm);
                AlarmClock.writeAlarmClock(alarm);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public static boolean canTriggerAlarm(AlarmClock alarm) {
        long now = new Date().getTime();
        switch (alarm.getAlarmType()) {
            case AlarmType.NotRepeat:
            case AlarmType.EveryDay:
            case AlarmType.EverySomeDays:
            case AlarmType.EverySomeHours:
            case AlarmType.EverySomeMinutes:
            case AlarmType.EverySomeSeconds:
                return true;
            case AlarmType.Weekend:
                return DateTools.isWeekend(now);
            case AlarmType.WorkingDays:
                return !DateTools.isWeekend(now);
            default:
                return false;
        }
    }

    public AlarmClock getAlarm() {
        return alarm;
    }

    public void setAlarm(AlarmClock alarm) {
        this.alarm = alarm;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

}
