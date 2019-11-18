package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mara.mybox.db.TableAlarmClock;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.executorService;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.scheduledTasks;

/**
 * @Author Mara
 * @CreateDate 2018-7-13
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public final class AlarmClock {

    public static final String AlarmValueSeprator = "_FG-FG_";
    public static final int offset = 1;

    private SimpleStringProperty description, sound, start, status, repeat, next, last;
    private long key, startTime, lastTime, nextTime, period;
    private boolean isActive, isSoundLoop, isSoundContinully;
    private int alarmType, everyValue, soundLoopTimes;
    private float volume;

    public final static class AlarmType {

        public final static int NotRepeat = 0;
        public final static int EveryDay = 1;
        public final static int Weekend = 2;
        public final static int WorkingDays = 3;
        public final static int EverySomeHours = 4;
        public final static int EverySomeMinutes = 5;
        public final static int EverySomeDays = 6;
        public final static int EverySomeSeconds = 7;
    }

    public AlarmClock() {
//        this.key = new SimpleLongProperty(new Date().getTime());

    }

    public static List<AlarmClock> readAlarmClocks() {
        List<AlarmClock> alarms = TableAlarmClock.read();
        if (alarms != null) {
            for (AlarmClock a : alarms) {
                setExtraValues(a);
                if (a.isIsActive()) {
                    scehduleAlarmClock(a);
                }
            }
            writeAlarmClocks(alarms);
        }
        return alarms;
    }

    // Keep this method to migrate data from config file to derby db.
    public static List<AlarmClock> readAlarmClocksFromFile() {
        try {
            List<AlarmClock> alarms = new ArrayList<>();
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(AppVariables.AlarmClocksFile))) {
                Properties values = new Properties();
                values.load(in);
                for (Object key : values.keySet()) {
                    try {
                        AlarmClock alarm = new AlarmClock();
                        alarm.setKey(Long.valueOf((String) key));
                        String[] fields = values.getProperty((String) key).split(AlarmValueSeprator);
//                        logger.debug(Arrays.asList(fields));
                        if (fields.length != 12) {
                            continue;
                        }
                        alarm.setDescription(fields[0]);
                        alarm.setAlarmType(Integer.valueOf(fields[1]));
                        alarm.setStartTime(Long.valueOf(fields[2]));
                        alarm.setIsActive(Boolean.valueOf(fields[3]));
                        alarm.setSound(fields[4]);
                        alarm.setEveryValue(Integer.valueOf(fields[5]));
                        alarm.setLastTime(Long.valueOf(fields[6]));
                        alarm.setNextTime(Long.valueOf(fields[7]));
                        alarm.setIsSoundLoop(Boolean.valueOf(fields[8]));
                        alarm.setIsSoundContinully(Boolean.valueOf(fields[9]));
                        alarm.setSoundLoopTimes(Integer.valueOf(fields[10]));
                        alarm.setVolume(Float.valueOf(fields[11]));
                        setExtraValues(alarm);
                        alarms.add(alarm);
                        if (alarm.isIsActive()) {
                            scehduleAlarmClock(alarm);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            return alarms;
        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }
    }

    public static void calculateNextTime(AlarmClock alarm) {
        long now = new Date().getTime();
        if (!alarm.isIsActive()) {
            alarm.setNextTime(-1);
            return;
        }
        if (alarm.getStartTime() >= now) {
            alarm.setNextTime(alarm.getStartTime());
        }
        long next = alarm.getNextTime();
        if (next < now) {
            if (alarm.getAlarmType() == AlarmType.NotRepeat) {
                alarm.setNextTime(-1);
                alarm.setIsActive(false);
                writeAlarmClock(alarm);
                return;
            }
            long last = alarm.getStartTime();
            if (alarm.getLastTime() > last) {
                last = alarm.getLastTime();
            }
            long loops = (now - last) / alarm.getPeriod();
            next = last + loops * alarm.getPeriod() + alarm.getPeriod();
        }
        if (alarm.getAlarmType() == AlarmType.Weekend) {
            while (!DateTools.isWeekend(next)) {
                next += 24 * 3600000;
            }
        }
        if (alarm.getAlarmType() == AlarmType.WorkingDays) {
            while (DateTools.isWeekend(next)) {
                next += 24 * 3600000;
            }
        }
        alarm.setNextTime(next);
    }

    public static void setExtraValues(AlarmClock alarm) {
        if (alarm == null) {
            return;
        }
        alarm.setRepeat(getTypeString(alarm));
        alarm.setStart(DateTools.datetimeToString(alarm.getStartTime()));
        alarm.setPeriod(getPeriod(alarm));
        calculateNextTime(alarm);
        if (!alarm.isIsActive()) {
            alarm.setStatus(AppVariables.message("Inactive"));
        } else {
            alarm.setStatus(AppVariables.message("Active"));
        }
        if (alarm.getLastTime() > 0) {
            alarm.setLast(DateTools.datetimeToString(alarm.getLastTime()));
        } else {
            alarm.setLast("");
        }
        if (alarm.getNextTime() > 0) {
            alarm.setNext(DateTools.datetimeToString(alarm.getNextTime()));
        } else {
            alarm.setNext("");
        }

    }

    public static String getTypeString(AlarmClock alarm) {

        switch (alarm.getAlarmType()) {
            case AlarmType.NotRepeat:
                return AppVariables.message("NotRepeat");
            case AlarmType.EveryDay:
                return AppVariables.message("EveryDay");
            case AlarmType.Weekend:
                return AppVariables.message("Weekend");
            case AlarmType.WorkingDays:
                return AppVariables.message("WorkingDays");
            case AlarmType.EverySomeHours:
                return AppVariables.message("Every") + " " + alarm.getEveryValue() + " " + AppVariables.message("Hours");
            case AlarmType.EverySomeMinutes:
                return AppVariables.message("Every") + " " + alarm.getEveryValue() + " " + AppVariables.message("Minutes");
            case AlarmType.EverySomeDays:
                return AppVariables.message("Every") + " " + alarm.getEveryValue() + " " + AppVariables.message("Days");
            case AlarmType.EverySomeSeconds:
                return AppVariables.message("Every") + " " + alarm.getEveryValue() + " " + AppVariables.message("Seconds");
        }
        return null;
    }

    public static String getTypeString(int type) {

        switch (type) {
            case AlarmType.NotRepeat:
                return AppVariables.message("NotRepeat");
            case AlarmType.EveryDay:
                return AppVariables.message("EveryDay");
            case AlarmType.Weekend:
                return AppVariables.message("Weekend");
            case AlarmType.WorkingDays:
                return AppVariables.message("WorkingDays");
            case AlarmType.EverySomeHours:
            case AlarmType.EverySomeMinutes:
            case AlarmType.EverySomeDays:
            case AlarmType.EverySomeSeconds:
                return AppVariables.message("Every");
        }
        return null;
    }

    public static String getTypeUnit(int type) {

        switch (type) {
            case AlarmType.EverySomeHours:
                return AppVariables.message("Hours");
            case AlarmType.EverySomeMinutes:
                return AppVariables.message("Minutes");
            case AlarmType.EverySomeDays:
                return AppVariables.message("Days");
            case AlarmType.EverySomeSeconds:
                return AppVariables.message("Seconds");
        }
        return null;
    }

    public static long getPeriod(AlarmClock alarm) {

        switch (alarm.getAlarmType()) {
            case AlarmType.EveryDay:
            case AlarmType.Weekend:
            case AlarmType.WorkingDays:
            case AlarmType.EverySomeDays:
                return 24 * 3600000;
            case AlarmType.EverySomeHours:
                return alarm.getEveryValue() * 3600000;
            case AlarmType.EverySomeMinutes:
                return alarm.getEveryValue() * 60000;
            case AlarmType.EverySomeSeconds:
                return alarm.getEveryValue() * 1000;
        }
        return -1;

    }

    public static boolean writeAlarmClock(AlarmClock theAlarm) {
        List<AlarmClock> alarms = new ArrayList<>();
        alarms.add(theAlarm);
        return writeAlarmClocks(alarms);
    }

    public static boolean writeAlarmClocks(List<AlarmClock> alarms) {
        return TableAlarmClock.write(alarms);
    }

    public static int findAlarmIndex(ObservableList<AlarmClock> alarms, long key) {
        try {
            if (alarms == null || key <= 0) {
                return -1;
            }
            for (int i = 0; i < alarms.size(); i++) {
                if (alarms.get(i).getKey() == key) {
                    return i;
                }
            }
            return -1;
        } catch (Exception e) {
            logger.error(e.toString());
            return -1;
        }
    }

    public static boolean deleteAlarmClocks(ObservableList<AlarmClock> alarms) {
        try {
            if (alarms == null || alarms.isEmpty()) {
                return false;
            }
            TableAlarmClock.delete(alarms);
            for (AlarmClock alarm : alarms) {
                ScheduledFuture future = scheduledTasks.get(alarm.getKey());
                if (future != null) {
                    future.cancel(true);
                    scheduledTasks.remove(alarm.getKey());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean deleteAlarmClock(AlarmClock alarm) {
        ObservableList<AlarmClock> alarms = FXCollections.observableArrayList();
        alarms.add(alarm);
        return deleteAlarmClocks(alarms);
    }

    public static boolean clearAllAlarmClocks() {
        try {
            new TableAlarmClock().clear();
            for (Long key : scheduledTasks.keySet()) {
                ScheduledFuture future = scheduledTasks.get(key);
                future.cancel(true);
            }
            scheduledTasks = new HashMap<>();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static void scehduleAlarmClock(AlarmClock alarm) {
        if (alarm == null || alarm.getKey() <= 0) {
            return;
        }
        try {
            if (scheduledTasks != null) {
                ScheduledFuture oldFuture = scheduledTasks.get(alarm.getKey());
                if (oldFuture != null) {
                    oldFuture.cancel(true);
                    scheduledTasks.remove(alarm.getKey());
                }
            }
            if (!alarm.isIsActive()) {
                return;
            }
            AlarmClockTask task = new AlarmClockTask(alarm);
            ScheduledFuture newFuture;
            if (executorService == null) {
                executorService = Executors.newScheduledThreadPool(10);
            }
            if (alarm.getAlarmType() == AlarmType.NotRepeat) {
                newFuture = executorService.schedule(task, task.getDelay(), TimeUnit.MILLISECONDS);
            } else {
                if (task.getPeriod() <= 0) {
                    alarm.setIsActive(false);
                    writeAlarmClock(alarm);
                    return;
                }
                newFuture = executorService.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod(), TimeUnit.MILLISECONDS);
            }
            if (scheduledTasks == null) {
                scheduledTasks = new HashMap<>();
            }
            scheduledTasks.put(alarm.getKey(), newFuture);
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String Description) {
        this.description = new SimpleStringProperty(Description);
    }

    public String getSound() {
        return sound.get();
    }

    public void setSound(String sound) {
        this.sound = new SimpleStringProperty(sound);
    }

    public void setStart(String start) {
        this.start = new SimpleStringProperty(start);
    }

    public void setStatus(String status) {
        this.status = new SimpleStringProperty(status);
    }

    public void setRepeat(String repeat) {
        this.repeat = new SimpleStringProperty(repeat);
    }

    public void setNext(String next) {
        this.next = new SimpleStringProperty(next);
    }

    public void setLast(String last) {
        this.last = new SimpleStringProperty(last);
    }

    public String getStart() {
        return start.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getRepeat() {
        return repeat.get();
    }

    public String getNext() {
        return next.get();
    }

    public String getLast() {
        return last.get();
    }

    public void setKey(long key) {
        this.key = key;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public void setEveryValue(int everyValue) {
        this.everyValue = everyValue;
    }

    public long getKey() {
        return key;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public int getEveryValue() {
        return everyValue;
    }

    public long getNextTime() {
        return nextTime;
    }

    public void setNextTime(long nextTime) {
        this.nextTime = nextTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public boolean isIsSoundLoop() {
        return isSoundLoop;
    }

    public void setIsSoundLoop(boolean isSoundLoop) {
        this.isSoundLoop = isSoundLoop;
    }

    public boolean isIsSoundContinully() {
        return isSoundContinully;
    }

    public void setIsSoundContinully(boolean isSoundContinully) {
        this.isSoundContinully = isSoundContinully;
    }

    public int getSoundLoopTimes() {
        return soundLoopTimes;
    }

    public void setSoundLoopTimes(int soundLoopTimes) {
        this.soundLoopTimes = soundLoopTimes;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

}
