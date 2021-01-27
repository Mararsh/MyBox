package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.Location;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-7-5
 * @License Apache License Version 2.0
 */
public class TableLocationData extends BaseTable<Location> {

    protected TableDataset tableDataset;

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
        addColumn(new ColumnDefinition("ldid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("datasetid", ColumnType.Long, true)
                .setForeignName("Location_Data_datasetid_fk").setForeignTable("Dataset").setForeignColumn("dsid"));
        addColumn(new ColumnDefinition("label", ColumnType.String).setLength(2048));
        addColumn(new ColumnDefinition("address", ColumnType.String).setLength(4096));
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
        addColumn(new ColumnDefinition("location_comments", ColumnType.String).setLength(32672));
        return this;
    }

    /*
        View
     */
    public static final String CreateView
            = " CREATE VIEW Location_Data_View AS "
            + " SELECT Location_Data.*, Dataset.* "
            //            + " CASE WHEN start_time != " + CommonValues.InvalidLong + " AND end_time !=" + CommonValues.InvalidLong
            //            + " THEN end_time - start_time ELSE " + CommonValues.InvalidLong + " END AS duration "
            + " FROM Location_Data JOIN Dataset ON Location_Data.datasetid=Dataset.dsid";

    /*
        Prepared Statements
     */
    public static final String ViewSelect
            = "SELECT * FROM Location_Data_View";

    public static final String SizeSelectPrefix
            = "SELECT count(Location_Data.ldid) FROM Location_Data JOIN Dataset"
            + "  ON Location_Data.datasetid=Dataset.dsid ";

    public static final String ClearPrefix
            = "DELETE FROM Location_Data WHERE ldid IN "
            + "( SELECT Location_Data.ldid FROM  Location_Data JOIN Dataset "
            + "  ON Location_Data.datasetid=Dataset.dsid";

    public static final String Datasets
            = "SELECT DISTINCT data_set AS name, Dataset.* "
            + " FROM Location_Data JOIN Dataset ON Location_Data.datasetid=Dataset.dsid";

    public static final String Times
            = "SELECT DISTINCT start_time FROM Location_Data ORDER BY start_time";

    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("datasetid".equals(column) && results.findColumn("dsid") > 0) {
                return getTableDataset().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(Location data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("datasetid".equals(column) && value instanceof Dataset) {
            data.setDataset((Dataset) value);
        }
        return true;
    }

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

    public Dataset dataset(String datasetName) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return dataset(conn, datasetName);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Dataset dataset(Connection conn, String datasetName) {
        return getTableDataset().read(conn, tableName, datasetName);
    }

    public Dataset queryAndCreateDataset(String datasetName) {
        if (datasetName == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return queryAndCreateDataset(conn, datasetName);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Dataset queryAndCreateDataset(Connection conn, String datasetName) {
        try {
            if (conn == null || datasetName == null) {
                return null;
            }
            Dataset dataset = dataset(conn, datasetName);
            if (dataset == null) {
                dataset = new Dataset();
                dataset.setDataCategory(tableName);
                dataset.setDataSet(datasetName);
                tableDataset.insertData(conn, dataset);
                conn.commit();
                dataset = dataset(conn, datasetName);
            }
            return dataset;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean delete(String dataset, boolean deleteDataset) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return delete(conn, dataset, deleteDataset);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean delete(Connection conn, String dataset, boolean deleteDataset) {
        try {
            Dataset datasetValue = dataset(conn, dataset);
            if (datasetValue == null) {
                return false;
            }
            String sql = "DELETE FROM Location_Data WHERE datasetid=" + datasetValue.getId();
            DerbyBase.update(conn, sql);
            if (deleteDataset) {
                sql = "DELETE FROM Dataset WHERE dsid=" + datasetValue.getId();
                return DerbyBase.update(conn, sql) > 0;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public List<Dataset> datasets() {
        return getTableDataset().datasets(tableName);
    }

    /*
        static methods
     */
    public static List<Date> times() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
                long time = results.getLong("start_time");
                if (time != CommonValues.InvalidLong) {
                    times.add(new Date(time));
                }
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
        return Arrays.asList(
                message("Dataset"), message("Longitude"), message("Latitude")
        );
    }

    @Override
    public List<String> importAllFields() {
        return Arrays.asList(
                message("Dataset"), message("Label"), message("Address"),
                message("Longitude"), message("Latitude"), message("Altitude"),
                message("Precision"), message("Speed"), message("Direction"), message("CoordinateSystem"),
                message("DataValue"), message("DataSize"), message("StartTime"), message("EndTime"),
                message("Image"), message("Comments")
        );
    }

    /*
        get/set
     */
    public TableDataset getTableDataset() {
        if (tableDataset == null) {
            tableDataset = new TableDataset();
        }
        return tableDataset;
    }

    public void setTableDataset(TableDataset tableDataset) {
        this.tableDataset = tableDataset;
    }

}
