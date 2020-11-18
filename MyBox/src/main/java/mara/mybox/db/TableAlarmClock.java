package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.AlarmClock;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

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
            conn.createStatement().executeUpdate(Create_Table_Statement);
            List<AlarmClock> values = AlarmClock.readAlarmClocksFromFile();
            if (values == null || values.isEmpty()) {
                return false;
            }
            final String sql = "INSERT INTO Alarm_Clock "
                    + " (key_value, description , alarm_type, start_time, is_active , sound , every_value , "
                    + " last_time , next_time ,  is_sound_loop , is_sound_continully , sound_loop_times ,  volume ) "
                    + " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                for (AlarmClock v : values) {
                    statement.setLong(1, v.getKey());
                    statement.setString(2, v.getDescription());
                    statement.setInt(3, v.getAlarmType());
                    statement.setString(4, DateTools.datetimeToString(v.getStartTime()));
                    statement.setShort(5, (short) (v.isIsActive() ? 1 : 0));
                    statement.setString(6, v.getSound());
                    statement.setInt(7, v.getEveryValue());
                    statement.setString(8, DateTools.datetimeToString(v.getLastTime()));
                    statement.setString(9, DateTools.datetimeToString(v.getNextTime()));
                    statement.setShort(10, (short) (v.isIsSoundLoop() ? 1 : 0));
                    statement.setShort(11, (short) (v.isIsSoundContinully() ? 1 : 0));
                    statement.setInt(12, v.getSoundLoopTimes());
                    statement.setFloat(13, v.getVolume());
                    statement.executeUpdate();
                }
            }
            try {
                new File(AppVariables.AlarmClocksFile).delete();
            } catch (Exception e) {
                failed(e);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            final String sql = " SELECT * FROM Alarm_Clock";
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
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
            }

        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return alarms;
    }

    public static boolean write(List<AlarmClock> alarms) {
        if (alarms == null || alarms.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            final String querySql = " SELECT alarm_type FROM Alarm_Clock WHERE key_value=?";
            final String updateSql = "UPDATE Alarm_Clock "
                    + " SET description=? , alarm_type=?, start_time=? , is_active=? , sound=?,"
                    + "  every_value=? , last_time=?, next_time=?, is_sound_loop=?, "
                    + " is_sound_continully=?, sound_loop_times=?, volume=?"
                    + " WHERE key_value=?";
            final String insertSql = "INSERT INTO Alarm_Clock "
                    + " (key_value, description , alarm_type, start_time, is_active , sound , every_value , "
                    + " last_time , next_time ,  is_sound_loop , is_sound_continully , sound_loop_times ,  volume ) "
                    + " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            try ( PreparedStatement query = conn.prepareStatement(querySql);
                     PreparedStatement update = conn.prepareStatement(updateSql);
                     PreparedStatement insert = conn.prepareStatement(insertSql)) {
                for (AlarmClock a : alarms) {
                    query.setLong(1, a.getKey());
                    boolean exist = false;
                    try ( ResultSet results = query.executeQuery()) {
                        if (results.next()) {
                            exist = true;
                        }
                    }
                    if (exist) {
                        update.setString(1, a.getDescription());
                        update.setInt(2, a.getAlarmType());
                        update.setString(3, DateTools.datetimeToString(a.getStartTime()));
                        update.setShort(4, (short) (a.isIsActive() ? 1 : 0));
                        update.setString(5, a.getSound());
                        update.setInt(6, a.getEveryValue());
                        update.setString(7, DateTools.datetimeToString(a.getLastTime()));
                        update.setString(8, DateTools.datetimeToString(a.getNextTime()));
                        update.setShort(9, (short) (a.isIsSoundLoop() ? 1 : 0));
                        update.setShort(10, (short) (a.isIsSoundContinully() ? 1 : 0));
                        update.setInt(11, a.getSoundLoopTimes());
                        update.setFloat(12, a.getVolume());
                        update.setLong(13, a.getKey());
                        update.executeUpdate();
                    } else {
                        insert.setLong(1, a.getKey());
                        insert.setString(2, a.getDescription());
                        insert.setInt(3, a.getAlarmType());
                        insert.setString(4, DateTools.datetimeToString(a.getStartTime()));
                        insert.setShort(5, (short) (a.isIsActive() ? 1 : 0));
                        insert.setString(6, a.getSound());
                        insert.setInt(7, a.getEveryValue());
                        insert.setString(8, DateTools.datetimeToString(a.getLastTime()));
                        insert.setString(9, DateTools.datetimeToString(a.getNextTime()));
                        insert.setShort(10, (short) (a.isIsSoundLoop() ? 1 : 0));
                        insert.setShort(11, (short) (a.isIsSoundContinully() ? 1 : 0));
                        insert.setInt(12, a.getSoundLoopTimes());
                        insert.setFloat(13, a.getVolume());
                        insert.executeUpdate();
                    }
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<AlarmClock> alarms) {
        if (alarms == null || alarms.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Alarm_Clock WHERE key_value=?")) {
                for (int i = 0; i < alarms.size(); ++i) {
                    statement.setLong(1, alarms.get(i).getKey());
                    statement.executeUpdate();
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

}
