package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.AlarmClock;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableAlarmClock extends DerbyBase {

    public TableAlarmClock() {
        Table_Name = "Alarm_Clock";
        Keys = new ArrayList<>() {
            {
                add("key_value");
            }
        };

        Create_Table_Statement
                = " CREATE TABLE Alarm_Clock ( "
                + "  key_value  BIGINT NOT NULL PRIMARY KEY, "
                + "  description VARCHAR(1024), "
                + "  alarm_type INT  NOT NULL, "
                + "  start_time TIMESTAMP, "
                + "  is_active SMALLINT, "
                + "  sound VARCHAR(1024), "
                + "  every_value INT, "
                + "  last_time TIMESTAMP, "
                + "  next_time TIMESTAMP, "
                + "  is_sound_loop SMALLINT, "
                + "  is_sound_continully SMALLINT, "
                + "  sound_loop_times INT, "
                + "  volume FLOAT "
                + " )";
    }

    @Override
    public boolean init(Connection conn) {
        try {
            if (conn == null) {
                return false;
            }
            Statement statement = conn.createStatement();
            statement.executeUpdate(Create_Table_Statement);
            List<AlarmClock> values = AlarmClock.readAlarmClocksFromFile();
            if (values != null && !values.isEmpty()) {
                String sql;
                for (AlarmClock v : values) {
                    sql = "INSERT INTO Alarm_Clock(key_value, description , alarm_type, start_time, is_active , sound , every_value , "
                            + " last_time , next_time ,  is_sound_loop , is_sound_continully , sound_loop_times ,  volume ) VALUES(";
                    sql += v.getKey() + ", '" + v.getDescription() + "', ";
                    sql += v.getAlarmType() + ", '" + DateTools.datetimeToString(v.getStartTime()) + "', ";
                    sql += (v.isIsActive() ? 1 : 0) + ", '" + v.getSound() + "', ";
                    sql += v.getEveryValue() + ", '" + DateTools.datetimeToString(v.getLastTime()) + "', '" + DateTools.datetimeToString(v.getNextTime()) + "', ";
                    sql += (v.isIsSoundLoop() ? 1 : 0) + ", " + (v.isIsSoundContinully() ? 1 : 0) + ", ";
                    sql += v.getSoundLoopTimes() + ", " + v.getVolume() + " )";
//                    logger.debug(sql);
                    statement.executeUpdate(sql);
                }
                try {
                    new File(AppVariables.AlarmClocksFile).delete();
                } catch (Exception e) {
                    failed(e);
                }
            }
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static List<AlarmClock> read() {
        List<AlarmClock> alarms = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Alarm_Clock";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                AlarmClock a = new AlarmClock();
                a.setKey(results.getLong("key_value"));
                a.setDescription(results.getString("description"));
                a.setAlarmType(results.getInt("alarm_type"));
                a.setStartTime(results.getTimestamp("start_time").getTime());
                a.setIsActive(results.getShort("is_active") > 0);
                a.setSound(results.getString("sound"));
                a.setEveryValue(results.getInt("every_value"));
                a.setLastTime(results.getTimestamp("last_time").getTime());
                a.setNextTime(results.getTimestamp("next_time").getTime());
                a.setIsSoundLoop(results.getShort("is_sound_loop") > 0);
                a.setIsSoundContinully(results.getShort("is_sound_continully") > 0);
                a.setSoundLoopTimes(results.getInt("sound_loop_times"));
                a.setVolume(results.getFloat("volume"));
                alarms.add(a);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return alarms;
    }

    public static boolean write(List<AlarmClock> alarms) {
        if (alarms == null || alarms.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            String sql;
            for (AlarmClock a : alarms) {
                Statement statement = conn.createStatement();
                sql = " SELECT alarm_type FROM Alarm_Clock WHERE key_value=" + a.getKey();
                boolean exist;
                try ( ResultSet results = statement.executeQuery(sql)) {
                    exist = results.next();
                }
                if (exist) {
                    sql = "UPDATE Alarm_Clock ";
                    sql += " SET description='" + a.getDescription() + "'";
                    sql += " , alarm_type=" + a.getAlarmType();
                    sql += " , start_time='" + DateTools.datetimeToString(a.getStartTime()) + "'";
                    sql += " , is_active=" + (a.isIsActive() ? 1 : 0);
                    sql += " , sound='" + a.getSound() + "'";
                    sql += " , every_value=" + a.getEveryValue();
                    sql += " , last_time='" + DateTools.datetimeToString(a.getLastTime()) + "'";
                    sql += " , next_time='" + DateTools.datetimeToString(a.getNextTime()) + "'";
                    sql += " , is_sound_loop=" + (a.isIsSoundLoop() ? 1 : 0);
                    sql += " , is_sound_continully=" + (a.isIsSoundContinully() ? 1 : 0);
                    sql += " , sound_loop_times=" + a.getSoundLoopTimes();
                    sql += " , volume=" + a.getVolume();
                    sql += " WHERE key_value=" + a.getKey();
                    statement.executeUpdate(sql);
                } else {
                    sql = "INSERT INTO Alarm_Clock(key_value, description , alarm_type, start_time, is_active , sound , every_value , "
                            + " last_time , next_time ,  is_sound_loop , is_sound_continully , sound_loop_times ,  volume ) VALUES(";
                    sql += a.getKey() + ", '" + a.getDescription() + "', ";
                    sql += a.getAlarmType() + ", '" + DateTools.datetimeToString(a.getStartTime()) + "', ";
                    sql += (a.isIsActive() ? 1 : 0) + ", '" + a.getSound() + "', ";
                    sql += a.getEveryValue() + ", '" + DateTools.datetimeToString(a.getLastTime()) + "', '" + DateTools.datetimeToString(a.getNextTime()) + "', ";
                    sql += (a.isIsSoundLoop() ? 1 : 0) + ", " + (a.isIsSoundContinully() ? 1 : 0) + ", ";
                    sql += a.getSoundLoopTimes() + ", " + a.getVolume() + " )";
                    statement.executeUpdate(sql);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<AlarmClock> alarms) {
        if (alarms == null || alarms.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( '" + alarms.get(0) + "'";
            for (int i = 1; i < alarms.size(); ++i) {
                inStr += ", '" + alarms.get(i) + "'";
            }
            inStr += " )";
            String sql = "DELETE FROM Alarm_Clock WHERE key_value IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

}
