package mara.mybox.fxml;

import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import mara.mybox.controller.AlarmClockRunController;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.db.data.AlarmClock.AlarmType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.scheduledTasks;

import mara.mybox.value.Fxmls;

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
//            MyBoxLog.debug("call");
            if (!canTriggerAlarm(alarm)) {
                MyBoxLog.debug("Can not tigger alarm due to not satisfied");
                return;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        AlarmClockRunController controller = (AlarmClockRunController) WindowTools.openStage(Fxmls.AlarmClockRunFxml);
                        controller.runAlarm(alarm);

                        if (AppVariables.alarmClockController != null
                                && AppVariables.alarmClockController.getAlertClockTableController() != null) {
                            AppVariables.alarmClockController.getAlertClockTableController().refreshAction();
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
