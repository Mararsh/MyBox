package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.Dataset;
import mara.mybox.data.Location;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2020-7-5
 * @License Apache License Version 2.0
 */
public class TableLocationData extends TableBase<Location> {

    protected TableDataset tableDataset;

    public TableLocationData() {
        tableName = "Location_Data";
        addColumn(new ColumnDefinition("ldid", ColumnDefinition.ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("datasetid", ColumnDefinition.ColumnType.Long, true)
                .setForeignName("Location_Data_datasetid_fk").setForeignTable("Dataset").setForeignColumn("dsid"));
        addColumn(new ColumnDefinition("label", ColumnDefinition.ColumnType.String).setLength(2048));
        addColumn(new ColumnDefinition("address", ColumnDefinition.ColumnType.String).setLength(4096));
        addColumn(new ColumnDefinition("longitude", ColumnDefinition.ColumnType.Double, true).setMaxValue((double) 180).setMinValue((double) -180));
        addColumn(new ColumnDefinition("latitude", ColumnDefinition.ColumnType.Double, true).setMaxValue((double) 90).setMinValue((double) -90));
        addColumn(new ColumnDefinition("altitude", ColumnDefinition.ColumnType.Double));
        addColumn(new ColumnDefinition("precision", ColumnDefinition.ColumnType.Double));
        addColumn(new ColumnDefinition("speed", ColumnDefinition.ColumnType.Double));
        addColumn(new ColumnDefinition("direction", ColumnDefinition.ColumnType.Short));
        addColumn(new ColumnDefinition("coordinate_system", ColumnDefinition.ColumnType.Short));
        addColumn(new ColumnDefinition("data_value", ColumnDefinition.ColumnType.Double));
        addColumn(new ColumnDefinition("data_size", ColumnDefinition.ColumnType.Double));
        addColumn(new ColumnDefinition("start_time", ColumnDefinition.ColumnType.Era));
        addColumn(new ColumnDefinition("end_time", ColumnDefinition.ColumnType.Era));
        addColumn(new ColumnDefinition("location_image", ColumnDefinition.ColumnType.File).setLength(2048));
        addColumn(new ColumnDefinition("location_comments", ColumnDefinition.ColumnType.String).setLength(32672));
    }

    /*
        View
     */
    public static final String CreateView
            = " CREATE VIEW Location_Data_View AS "
            + " SELECT Location_Data.*, Dataset.* "
            //            + " CASE WHEN start_time != " + Long.MIN_VALUE + " AND end_time !=" + Long.MIN_VALUE
            //            + " THEN end_time - start_time ELSE " + Long.MIN_VALUE + " END AS duration "
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
    public Location readData(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            Location location = new Location();
            location.setId(results.getLong("ldid"));
            try {
                // query results from "Location_Data_View" instead of table
                if (results.findColumn("dsid") > 0) {
                    Dataset dataset = getTableDataset().readData(results);
                    location.setDataset(dataset);
                }
            } catch (Exception e) {
            }
            location.setDatasetid(results.getLong("datasetid"));
            location.setLabel(results.getString("label"));
            location.setAddress(results.getString("address"));
            location.setLongitude(results.getDouble("longitude"));
            location.setLatitude(results.getDouble("latitude"));
            location.setAltitude(results.getDouble("altitude"));
            location.setPrecision(results.getDouble("precision"));
            location.setSpeed(results.getDouble("speed"));
            location.setDirection(results.getShort("direction"));
            location.setCoordinateSystem(new CoordinateSystem(results.getShort("coordinate_system")));
            location.setDataValue(results.getDouble("data_value"));
            location.setDataSize(results.getDouble("data_size"));
            location.setStartTime(results.getLong("start_time"));
            location.setEndTime(results.getLong("end_time"));
            location.setImage(results.getString("location_image"));
            location.setComments(results.getString("location_comments"));
            return location;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return null;
        }
    }

    @Override
    public int setValues(PreparedStatement statement, Location data) {
        if (statement == null || data == null) {
            return -1;
        }
        try {
            int count = 1;
            Location locationData = (Location) data;
            statement.setLong(count++, locationData.getDatasetid());
            if (locationData.getLabel() == null) {
                statement.setNull(count++, Types.VARCHAR);
            } else {
                statement.setString(count++, locationData.getLabel());
            }
            if (locationData.getAddress() == null) {
                statement.setNull(count++, Types.VARCHAR);
            } else {
                statement.setString(count++, locationData.getAddress());
            }
            statement.setDouble(count++, locationData.getLongitude());
            statement.setDouble(count++, locationData.getLatitude());
            statement.setDouble(count++, locationData.getAltitude());
            statement.setDouble(count++, locationData.getPrecision());
            statement.setDouble(count++, locationData.getSpeed());
            statement.setShort(count++, (short) locationData.getDirection());
            statement.setShort(count++, locationData.getCoordinateSystem().intValue());
            statement.setDouble(count++, locationData.getDataValue());
            statement.setDouble(count++, locationData.getDataSize());
            statement.setLong(count++, locationData.getStartTime());
            statement.setLong(count++, locationData.getEndTime());
            if (locationData.getImage() == null) {
                statement.setNull(count++, Types.VARCHAR);
            } else {
                statement.setString(count++, locationData.getImage());
            }
            if (locationData.getComments() == null) {
                statement.setNull(count++, Types.VARCHAR);
            } else {
                statement.setString(count++, locationData.getComments());
            }
            return count;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return -1;
        }
    }

    public Dataset dataset(String datasetName) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return dataset(conn, datasetName);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public Dataset dataset(Connection conn, String datasetName) {
        return getTableDataset().read(conn, "Location_Data", datasetName);
    }

    public Dataset queryAndCreate(String datasetName) {
        if (datasetName == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return queryAndCreate(conn, datasetName);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public Dataset queryAndCreate(Connection conn, String datasetName) {
        try {
            if (conn == null || datasetName == null) {
                return null;
            }
            Dataset dataset = dataset(conn, datasetName);
            if (dataset == null) {
                dataset = new Dataset();
                dataset.setDataCategory("Location_Data");
                dataset.setDataSet(datasetName);
                tableDataset.insertData(conn, dataset);
                conn.commit();
                dataset = dataset(conn, datasetName);
            }
            return dataset;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public boolean delete(String dataset, boolean deleteDataset) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return delete(conn, dataset, deleteDataset);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
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
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    /*
        static methods
     */
    public static List<Dataset> datasets() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return datasets(conn);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static List<Dataset> datasets(Connection conn) {
        List<Dataset> datasets = new ArrayList();
        if (conn == null) {
            return datasets;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Datasets)) {
            try ( ResultSet results = statement.executeQuery()) {
                TableDataset tableDataset = new TableDataset();
                while (results.next()) {
                    Dataset dataset = tableDataset.readData(results);
                    if (dataset != null) {
                        datasets.add(dataset);
                    }
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return datasets;
    }

    public static List<Date> times() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return times(conn);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static List<Date> times(Connection conn) {
        List<Date> times = new ArrayList();
        if (conn == null) {
            return times;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Times)) {
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    long time = results.getLong("start_time");
                    if (time != Long.MIN_VALUE) {
                        times.add(new Date(time));
                    }
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return times;
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
