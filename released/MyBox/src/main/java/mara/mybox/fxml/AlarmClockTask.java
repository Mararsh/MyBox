package mara.mybox.fxml;

import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import mara.mybox.controller.AlarmClockRunController;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2018-7-16
 * @License Apache License Version 2.0
 */
public class AlarmClockTask extends TimerTask {

    protected AlarmClock alarm;

    protected TimeUnit timeUnit = TimeUnit.SECONDS;
    protected long delay, period;

    public AlarmClockTask(AlarmClock alarmClock) {
        this.alarm = alarmClock;
//        AlarmClock.calculateNextTime(alarmClock);
//        delay = alarmClock.getNextTime() - new Date().getTime();
//        period = AlarmClock.period(alarmClock);
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

                        if (AppVariables.AlarmClockController != null
                                && AppVariables.AlarmClockController.getAlertClockTableController() != null) {
                            AppVariables.AlarmClockController.getAlertClockTableController().refreshAction();
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            });

//            if (alarm.getAlarmType() == NotRepeat) {
//                ScheduledFuture future = ScheduledTasks.get(alarm.getKey());
//                if (future != null) {
//                    future.cancel(true);
//                    ScheduledTasks.remove(alarm.getKey());
//                }
//                alarm.setIsActive(false);
//                AlarmClock.writeAlarmClock(alarm);
//            } else {
//                alarm.setLastTime(new Date().getTime());
//                alarm.setNextTime(-1);
//                AlarmClock.calculateNextTime(alarm);
//                AlarmClock.writeAlarmClock(alarm);
//            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public static boolean canTriggerAlarm(AlarmClock alarm) {
        long now = new Date().getTime();
        switch (alarm.getAlarmType()) {
            case NotRepeat:
            case EveryDay:
            case EverySomeDays:
            case EverySomeHours:
            case EverySomeMinutes:
            case EverySomeSeconds:
                return true;
            case Weekend:
                return DateTools.isWeekend(now);
            case WorkingDays:
                return !DateTools.isWeekend(now);
            default:
                return false;
        }
    }

    /*
        get/set
     */
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
