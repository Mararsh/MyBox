package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Location;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-5
 * @License Apache License Version 2.0
 */
public class TableLocationData extends BaseTable<Location> {

    public TableLocationData() {
        tableName = "Location_Data";
        defineColumns();
    }

    public TableLocationData(boolean defineColumns) {
        tableName = "Location_Data";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableLocationData defineColumns() {
        addColumn(new ColumnDefinition("ldid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("dataset", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("label", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("address", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("longitude", ColumnType.Double, true).setMaxValue((double) 180).setMinValue((double) -180));
        addColumn(new ColumnDefinition("latitude", ColumnType.Double, true).setMaxValue((double) 90).setMinValue((double) -90));
        addColumn(new ColumnDefinition("altitude", ColumnType.Double));
        addColumn(new ColumnDefinition("precision", ColumnType.Double));
        addColumn(new ColumnDefinition("speed", ColumnType.Double).setMinValue((double) 0));
        addColumn(new ColumnDefinition("direction", ColumnType.Short).setMinValue((short) 0));
        addColumn(new ColumnDefinition("coordinate_system", ColumnType.Short).setMinValue((short) 0));
        addColumn(new ColumnDefinition("data_value", ColumnType.Double));
        addColumn(new ColumnDefinition("data_size", ColumnType.Double));
        addColumn(new ColumnDefinition("start_time", ColumnType.Era).setMinValue((long) 0));
        addColumn(new ColumnDefinition("end_time", ColumnType.Era).setMinValue((long) 0));
        addColumn(new ColumnDefinition("location_image", ColumnType.Image));
        addColumn(new ColumnDefinition("location_comments", ColumnType.String).setLength(StringMaxLength));
        return this;
    }


    /*
        Prepared Statements
     */
    public static final String Times
            = "SELECT DISTINCT start_time FROM Location_Data ORDER BY start_time";

    @Override
    public void setId(Location source, Location target) {
        try {
            if (source == null || target == null) {
                return;
            }
            source.setLdid(target.getLdid());
        } catch (Exception e) {
        }
    }

    public boolean delete(String dataset) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, dataset);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean delete(Connection conn, String dataset) {
        try {
            String sql;
            if (dataset == null || dataset.isBlank()) {
                sql = "DELETE FROM Location_Data WHERE dataset=null'";
            } else {
                sql = "DELETE FROM Location_Data WHERE dataset='" + dataset + "'";
            }
            DerbyBase.update(conn, sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }


    /*
        static methods
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
        try {
            conn.setAutoCommit(true);
            try ( PreparedStatement statement = conn.prepareStatement(Times);
                     ResultSet results = statement.executeQuery();) {
                while (results.next()) {
                    long time = results.getLong("start_time");
                    if (time != AppValues.InvalidLong) {
                        times.add(new Date(time));
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return times;
    }

    /*
        import/export
     */
    @Override
    public List<String> importNecessaryFields() {
        return Arrays.asList(Languages.message("Dataset"), Languages.message("Longitude"), Languages.message("Latitude")
        );
    }

    @Override
    public List<String> importAllFields() {
        return Arrays.asList(Languages.message("Dataset"), Languages.message("Label"), Languages.message("Address"),
                Languages.message("Longitude"), Languages.message("Latitude"), Languages.message("Altitude"),
                Languages.message("Precision"), Languages.message("Speed"), Languages.message("Direction"), Languages.message("CoordinateSystem"),
                Languages.message("DataValue"), Languages.message("DataSize"), Languages.message("StartTime"), Languages.message("EndTime"),
                Languages.message("Image"), Languages.message("Comments")
        );
    }

}
