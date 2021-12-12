package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.WebHistory;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-5-4
 * @License Apache License Version 2.0
 */
public class TableWebHistory extends BaseTable<WebHistory> {

    public TableWebHistory() {
        tableName = "Web_History";
        defineColumns();
    }

    public TableWebHistory(boolean defineColumns) {
        tableName = "Web_History";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableWebHistory defineColumns() {
        addColumn(new ColumnDefinition("whid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("address", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("visit_time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("title", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("icon", ColumnType.String).setLength(StringMaxLength));
        orderColumns = "visit_time DESC";
        return this;
    }

    public static final String Create_Time_Index
            = "CREATE INDEX Web_History_time_index on Web_History ( visit_time )";

    public static final String QueryID
            = "SELECT * FROM Web_History WHERE whid=?";

    public static final String QueryAddresses
            = "SELECT address FROM Web_History ORDER BY visit_time DESC";

    public static final String Times
            = "SELECT DISTINCT visit_time FROM Web_History ORDER BY visit_time DESC";

    public List<String> recent(int number) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return recent(conn, number);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<String> recent(Connection conn, int number) {
        List<String> recent = new ArrayList<>();
        if (conn == null) {
            return recent;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryAddresses);
                 ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                String address = results.getString("address");
                if (!recent.contains(address)) {
                    recent.add(address);
                }
                if (recent.size() >= number) {
                    break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return recent;
    }

    /*
        Static methods
     */
    public static List<Date> times() {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return times(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<Date> times(Connection conn) {
        List<Date> times = new ArrayList();
        if (conn == null) {
            return times;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Times);
                 ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Date time = results.getTimestamp("visit_time");
                if (time != null) {
                    times.add(time);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return times;
    }

}
