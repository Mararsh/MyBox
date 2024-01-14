package mara.mybox.db.data;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.AlarmClockTask;
import static mara.mybox.value.Languages.message;
import static mara.mybox.value.AppVariables.ExecutorService;
import static mara.mybox.value.AppVariables.ScheduledTasks;

/**
 * @Author Mara
 * @CreateDate 2018-7-13
 * @License Apache License Version 2.0
 */
public class AlarmClock extends BaseData {

    public static final String AlarmValueSeprator = "_FG-FG_";
    public static final int offset = 1;

    protected long atid;
    protected AlarmType alarmType;
    protected String title, description, sound;
    protected boolean isActive, isSoundLoop, isSoundContinully;
    protected int everyValue, soundLoopTimes;
    protected float volume;
    protected Date startTime, lastTime, nextTime;

    public static enum AlarmType {
        NotRepeat, EveryDay, Weekend, WorkingDays,
        EverySomeHours, EverySomeMinutes, EverySomeDays, EverySomeSeconds
    }

    private void init() {
        atid = -1;
        alarmType = null;

    }

    public AlarmClock() {
        init();
    }

    public void calculateNextTime() {
        if (!isActive || startTime == null) {
            nextTime = null;
            return;
        }
        Date now = new Date();
        if (startTime.after(now)) {
            nextTime = startTime;
        }
//        if (nextTime != null && nextTime.before(now)) {
//            if (alarmType == AlarmType.NotRepeat) {
//                nextTime = null;
//                isActive = false;
////                writeAlarmClock(alarm);
//                return;
//            }
//            if (last != null && last.after(now)) {
//                last = alarm.getLastTime();
//            }
//            long loops = (now.getTime() - last.getTime()) / period(alarm);
//            next = last + loops * alarm.getPeriod() + alarm.getPeriod();
//        }
//        if (alarm.getAlarmType() == AlarmType.Weekend) {
//            while (!DateTools.isWeekend(next)) {
//                next += 24 * 3600000;
//            }
//        }
//        if (alarm.getAlarmType() == AlarmType.WorkingDays) {
//            while (DateTools.isWeekend(next)) {
//                next += 24 * 3600000;
//            }
//        }
//        alarm.setNextTime(next);
    }

    public String scehduleKey() {
        return "AlarmClock" + atid;
    }

    public int addInSchedule() {
        try {
            removeFromSchedule();
            if (!isActive) {
                return 0;
            }
            AlarmClockTask task = new AlarmClockTask(this);
            ScheduledFuture newFuture;
            if (ExecutorService == null) {
                ExecutorService = Executors.newScheduledThreadPool(10);
            }
            if (alarmType == AlarmType.NotRepeat) {
                newFuture = ExecutorService.schedule(task, task.getDelay(), TimeUnit.MILLISECONDS);
            } else {
                if (task.getPeriod() <= 0) {
                    isActive = false;
                    return 1;
                }
                newFuture = ExecutorService.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod(), TimeUnit.MILLISECONDS);
            }
            if (ScheduledTasks == null) {
                ScheduledTasks = new HashMap<>();
            }
            ScheduledTasks.put(scehduleKey(), newFuture);
            return 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public boolean removeFromSchedule() {
        try {
            if (ScheduledTasks == null) {
                return false;
            }
            String key = scehduleKey();
            ScheduledFuture future = ScheduledTasks.get(key);
            if (future != null) {
                future.cancel(true);
                ScheduledTasks.remove(key);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        Static methods
     */
    public static AlarmClock create() {
        return new AlarmClock();
    }

    public static boolean setValue(AlarmClock data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "atid":
                    data.setAtid(value == null ? null : (long) value);
                    return true;
                case "alarm_type":
                    data.setAlarmType(value == null ? null : AlarmType.values()[(int) value]);
                    return true;
                case "every_value":
                    data.setEveryValue(value == null ? null : (int) value);
                    return true;
                case "start_time":
                    data.setStartTime(value == null ? null : (Date) value);
                    return true;
                case "last_time":
                    data.setLastTime(value == null ? null : (Date) value);
                    return true;
                case "next_time":
                    data.setNextTime(value == null ? null : (Date) value);
                    return true;
                case "sound_loop_times":
                    data.setSoundLoopTimes(value == null ? null : (int) value);
                    return true;
                case "is_sound_loop":
                    data.setIsSoundLoop(value == null ? null : (boolean) value);
                    return true;
                case "is_sound_continully":
                    data.setIsSoundContinully(value == null ? null : (boolean) value);
                    return true;
                case "is_active":
                    data.setIsActive(value == null ? null : (boolean) value);
                    return true;
                case "volume":
                    data.setVolume(value == null ? null : (float) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "sound":
                    data.setSound(value == null ? null : (String) value);
                    return true;
                case "description":
                    data.setDescription(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(AlarmClock data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "atid":
                return data.getAtid();
            case "alarm_type":
                AlarmType type = data.getAlarmType();
                return type == null ? -1 : type.ordinal();
            case "every_value":
                return data.getEveryValue();
            case "start_time":
                return data.getStartTime();
            case "last_time":
                return data.getLastTime();
            case "next_time":
                return data.getNextTime();
            case "sound_loop_times":
                return data.getSoundLoopTimes();
            case "is_sound_loop":
                return data.isIsSoundLoop();
            case "is_sound_continully":
                return data.isIsSoundContinully();
            case "is_active":
                return data.isIsActive();
            case "volume":
                return data.getVolume();
            case "title":
                return data.getTitle();
            case "sound":
                return data.getSound();
            case "description":
                return data.getDescription();
        }
        return null;
    }

    public static boolean valid(AlarmClock data) {
        return data != null && data.getAlarmType() == null;
    }

    public static void setExtraValues2(AlarmClock alarm) {
        if (alarm == null) {
            return;
        }
//        calculateNextTime(alarm);
//        if (!alarm.isIsActive()) {
//            alarm.setStatus(message("Inactive"));
//        } else {
//            alarm.setStatus(message("Active"));
//        }
//        if (alarm.getLastTime() > 0) {
//            alarm.setLast(DateTools.datetimeToString(alarm.getLastTime()));
//        } else {
//            alarm.setLast("");
//        }
//        if (alarm.getNextTime() > 0) {
//            alarm.setNext(DateTools.datetimeToString(alarm.getNextTime()));
//        } else {
//            alarm.setNext("");
//        }

    }

    public static String typeString(AlarmClock alarm) {
        AlarmType type = alarm.getAlarmType();
        switch (type) {
            case NotRepeat:
                return message("NotRepeat");
            case EveryDay:
                return message("EveryDay");
            case Weekend:
                return message("Weekend");
            case WorkingDays:
                return message("WorkingDays");
            case EverySomeHours:
            case EverySomeMinutes:
            case EverySomeDays:
            case EverySomeSeconds:
                return message("Every") + " " + alarm.getEveryValue() + " " + typeUnit(type);
        }
        return null;
    }

    public static String typeUnit(AlarmType type) {
        switch (type) {
            case EverySomeHours:
                return message("Hours");
            case EverySomeMinutes:
                return message("Minutes");
            case EverySomeDays:
                return message("Days");
            case EverySomeSeconds:
                return message("Seconds");
        }
        return null;
    }

    public static long period(AlarmClock alarm) {
        switch (alarm.getAlarmType()) {
            case EveryDay:
            case Weekend:
            case WorkingDays:
            case EverySomeDays:
                return 24 * 3600000;
            case EverySomeHours:
                return alarm.getEveryValue() * 3600000;
            case EverySomeMinutes:
                return alarm.getEveryValue() * 60000;
            case EverySomeSeconds:
                return alarm.getEveryValue() * 1000;
        }
        return -1;
    }

    public static boolean scheduleAll() {
        return true;
//        try ( Connection conn = DerbyBase.getConnection()) {
//            TableAlarmClock tableAlarmClock = new TableAlarmClock();
//            List<AlarmClock> alarms = tableAlarmClock.readAll(conn);
//            if (alarms == null) {
//                return true;
//            }
//            for (AlarmClock alarm : alarms) {
//                if (alarm.addInSchedule() > 0) {
//                    tableAlarmClock.writeData(conn, alarm);
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            MyBoxLog.error(e);
//            return false;
//        }
    }

    /*
        set
     */
    public void setAtid(long atid) {
        this.atid = atid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setIsSoundLoop(boolean isSoundLoop) {
        this.isSoundLoop = isSoundLoop;
    }

    public void setIsSoundContinully(boolean isSoundContinully) {
        this.isSoundContinully = isSoundContinully;
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
    }

    public void setEveryValue(int everyValue) {
        this.everyValue = everyValue;
    }

    public void setSoundLoopTimes(int soundLoopTimes) {
        this.soundLoopTimes = soundLoopTimes;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public void setNextTime(Date nextTime) {
        this.nextTime = nextTime;
    }

    /*
        get
     */
    public static String getAlarmValueSeprator() {
        return AlarmValueSeprator;
    }

    public static int getOffset() {
        return offset;
    }

    public long getAtid() {
        return atid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSound() {
        return sound;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public boolean isIsSoundLoop() {
        return isSoundLoop;
    }

    public boolean isIsSoundContinully() {
        return isSoundContinully;
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public int getEveryValue() {
        return everyValue;
    }

    public int getSoundLoopTimes() {
        return soundLoopTimes;
    }

    public float getVolume() {
        return volume;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public Date getNextTime() {
        return nextTime;
    }

}
