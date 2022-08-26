package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.db.data.ColumnDefinition;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @License Apache License Version 2.0
 */
public class TableAlarmClock extends BaseTable<AlarmClock> {

    public TableAlarmClock() {
        tableName = "Alarm_Clock";
        defineColumns();
    }

    public TableAlarmClock(boolean defineColumns) {
        tableName = "Alarm_Clock";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableAlarmClock defineColumns() {
        addColumn(new ColumnDefinition("atid", ColumnDefinition.ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("alarm_type", ColumnDefinition.ColumnType.Integer, true));
        addColumn(new ColumnDefinition("every_value", ColumnDefinition.ColumnType.Integer));
        addColumn(new ColumnDefinition("start_time", ColumnDefinition.ColumnType.Datetime));
        addColumn(new ColumnDefinition("last_time", ColumnDefinition.ColumnType.Datetime));
        addColumn(new ColumnDefinition("next_time", ColumnDefinition.ColumnType.Datetime));
        addColumn(new ColumnDefinition("sound_loop_times", ColumnDefinition.ColumnType.Integer));
        addColumn(new ColumnDefinition("is_sound_loop", ColumnDefinition.ColumnType.Boolean));
        addColumn(new ColumnDefinition("is_sound_continully", ColumnDefinition.ColumnType.Boolean));
        addColumn(new ColumnDefinition("is_active", ColumnDefinition.ColumnType.Boolean));
        addColumn(new ColumnDefinition("volume", ColumnDefinition.ColumnType.Float));
        addColumn(new ColumnDefinition("title", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("sound", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("description", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        return this;
    }

    @Override
    public int deleteData(Connection conn, AlarmClock alarmClock) {
        if (alarmClock == null) {
            return -1;
        }
        alarmClock.removeFromSchedule();
        return super.deleteData(conn, alarmClock);
    }

    @Override
    public int deleteData(Connection conn, List<AlarmClock> dataList) {
        if (conn == null || dataList == null || dataList.isEmpty()) {
            return 0;
        }
        for (AlarmClock alarmClock : dataList) {
            alarmClock.removeFromSchedule();
        }
        return super.deleteData(conn, dataList);
    }

    @Override
    public long clearData(Connection conn) {
        try ( PreparedStatement statement = conn.prepareStatement("SELECT * FROM Alarm_Clock")) {
            conn.setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    AlarmClock alarmClock = readData(results);
                    if (alarmClock != null) {
                        alarmClock.removeFromSchedule();
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
        return super.clearData(conn);
    }

}
