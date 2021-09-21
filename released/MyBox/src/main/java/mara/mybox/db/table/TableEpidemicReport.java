package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.db.DerbyBase.stringValue;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class TableEpidemicReport extends BaseTable<EpidemicReport> {

    /*
        Table
     */
    public TableEpidemicReport() {
        tableName = "Epidemic_Report";
        defineColumns();
    }

    public TableEpidemicReport(boolean defineColumns) {
        tableName = "Epidemic_Report";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableEpidemicReport defineColumns() {
        addColumn(new ColumnDefinition("epid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("data_set", ColumnType.String, true).setLength(1024));
        addColumn(new ColumnDefinition("time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("locationid", ColumnType.Long, true)
                .setForeignName("Epidemic_Report_locationid_fk").setForeignTable("Geography_Code").setForeignColumn("gcid"));
        addColumn(new ColumnDefinition("confirmed", ColumnType.Long));
        addColumn(new ColumnDefinition("healed", ColumnType.Long));
        addColumn(new ColumnDefinition("dead", ColumnType.Long));
        addColumn(new ColumnDefinition("increased_confirmed", ColumnType.Long));
        addColumn(new ColumnDefinition("increased_healed", ColumnType.Long));
        addColumn(new ColumnDefinition("increased_dead", ColumnType.Long));
        addColumn(new ColumnDefinition("source", ColumnType.Short, true));  // 1:predefined 2:added 3:filled 4:statistic others:unknown
        return this;
    }

    /*
        View
     */
    public static final String CreateStatisticView
            = " CREATE VIEW Epidemic_Report_Statistic_View AS "
            + "  SELECT Epidemic_Report.*, Geography_Code.*,  "
            + "  CASE WHEN confirmed <= 0 THEN 0 ELSE healed * DOUBLE('1000.0') / confirmed END AS healed_confirmed_permillage, "
            + "  CASE WHEN confirmed <= 0 THEN 0 ELSE dead * DOUBLE('1000.0') / confirmed END AS dead_confirmed_permillage, "
            + "  CASE WHEN population <= 0 THEN 0 ELSE confirmed * DOUBLE('1000.0') / population END AS confirmed_population_permillage, "
            + "  CASE WHEN population <= 0 THEN 0 ELSE healed * DOUBLE('1000.0') / population END AS healed_population_permillage, "
            + "  CASE WHEN population <= 0 THEN 0 ELSE dead * DOUBLE('1000.0') / population END AS dead_population_permillage, "
            + "  CASE WHEN area <= 0 THEN 0 ELSE confirmed * DOUBLE('1000000000.0') / area END AS confirmed_area_permillage, "
            + "  CASE WHEN area <= 0 THEN 0 ELSE healed * DOUBLE('1000000000.0') / area END AS healed_area_permillage, "
            + "  CASE WHEN area <= 0 THEN 0 ELSE dead * DOUBLE('1000000000.0') / area END AS dead_area_permillage "
            + "  FROM Epidemic_Report JOIN Geography_Code ON Epidemic_Report.locationid=Geography_Code.gcid";


    /*
        Indexes
     */
    public static final String Create_Index_DatasetTimeDesc
            = " CREATE INDEX  Epidemic_Report_DatasetTimeDesc_index on Epidemic_Report ( "
            + "  data_set, time DESC, locationid, confirmed DESC"
            + " )";

    public static final String Create_Index_DatasetTimeAsc
            = " CREATE INDEX  Epidemic_Report_DatasetTimeAsc_index on Epidemic_Report ( "
            + "  data_set, time ASC, locationid, confirmed DESC"
            + " )";

    public static final String Create_Index_TimeAsc
            = " CREATE INDEX  Epidemic_Report_timeAsc_index on Epidemic_Report ( "
            + "  time ASC, locationid, confirmed DESC"
            + " )";

    /*
        Prepared Statements
     */
    public static final String StatisticViewSelect
            = "SELECT * FROM Epidemic_Report_Statistic_View";

    public static final String SizeSelectPrefix
            = "SELECT count(Epidemic_Report.epid) FROM Epidemic_Report JOIN Geography_Code"
            + "  ON Epidemic_Report.locationid=Geography_Code.gcid ";

    public static final String ClearPrefix
            = "DELETE FROM Epidemic_Report WHERE epid IN "
            + "( SELECT Epidemic_Report.epid FROM  Epidemic_Report JOIN Geography_Code "
            + "  ON Epidemic_Report.locationid=Geography_Code.gcid";

    public static final String EPidQuery
            = StatisticViewSelect + " WHERE epid=?";

    public static final String EqualCondition
            = " data_set=?  AND time=?  AND locationid=?";

    public static final String EqualQuery
            = StatisticViewSelect + " WHERE " + EqualCondition;

    public static final String ExistQuery
            = "SELECT * FROM Epidemic_Report WHERE " + EqualCondition;

    public static final String DatasetQuery
            = StatisticViewSelect + " WHERE data_set=?";

    public static final String LocationidQuery
            = StatisticViewSelect + " WHERE locationid=? ORDER BY time ASC";

    public static final String Insert
            = "INSERT INTO Epidemic_Report "
            + " (data_set, locationid, time, source,"
            + " confirmed, healed, dead,"
            + " increased_confirmed,  increased_healed, increased_dead) "
            + " VALUES(?,?,?,?,?,?,?,?,?,? )";

    public static final String UpdateAsEPid
            = "UPDATE Epidemic_Report SET "
            + "data_set=?, locationid=?, time=?, confirmed=?, healed=?, dead=?, "
            + "increased_confirmed=?,  increased_healed=?, increased_dead=?, "
            + "source=? WHERE epid=?";

    public static final String UpdateAsEqual
            = "UPDATE Epidemic_Report SET "
            + "confirmed=?, healed=?, dead=?, "
            + "increased_confirmed=?, increased_healed=?, increased_dead=?"
            + "source=? WHERE " + EqualCondition;

    public static final String Datasets
            = "SELECT DISTINCT data_set FROM Epidemic_Report";

    public static final String Times
            = " SELECT DISTINCT time FROM Epidemic_Report  ORDER BY time ASC";

    public static final String DatasetTimes
            = " SELECT DISTINCT time FROM Epidemic_Report WHERE data_set=?  ORDER BY time ASC";

    public static final String DatasetCount
            = "SELECT count(epid) FROM Epidemic_Report WHERE data_set=?";

    public static final String DeleteEPid
            = "DELETE FROM Epidemic_Report WHERE epid=? AND source<>1 ";

    public static final String DeleteDataset
            = "DELETE FROM Epidemic_Report WHERE data_set=? AND source<>1 ";

    public static void moveEPid() {
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            long max = 0;
            String sql = "SELECT max(epid) FROM Epidemic_Report";
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    max = results.getLong(1);
                }
            }
            sql = "ALTER TABLE Epidemic_Report ALTER COLUMN epid RESTART WITH " + (max + 1);
            statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static List<EpidemicReport> dataQuery(String sql, boolean decodeAncestors) {
        List<EpidemicReport> reports = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            conn.setReadOnly(true);
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    EpidemicReport report = statisticViewQuery(conn, results, decodeAncestors);
                    reports.add(report);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return reports;
    }

    public static List<EpidemicReport> read(String dataset, int max, boolean decodeAncestors) {
        List<EpidemicReport> reports = new ArrayList<>();
        if (dataset == null) {
            return reports;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            try ( PreparedStatement statement = conn.prepareStatement(DatasetQuery)) {
                statement.setMaxRows(max);
                statement.setString(1, dataset);
                try ( ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        EpidemicReport location = statisticViewQuery(conn, results, decodeAncestors);
                        reports.add(location);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return reports;
    }

    public static EpidemicReport read(Connection conn, String sql, boolean decodeAncestors) {
        EpidemicReport report = null;
        try ( Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    report = statisticViewQuery(conn, results, decodeAncestors);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return report;
    }

    public static EpidemicReport read(String dataset, Date time, long location, boolean decodeAncestors) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, dataset, time, location, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static EpidemicReport read(Connection conn, String dataset, Date time, long location, boolean decodeAncestors) {
        EpidemicReport report = null;
        try ( PreparedStatement statement = conn.prepareStatement(EqualQuery)) {
            statement.setMaxRows(1);
            statement.setString(1, dataset);
            statement.setString(2, DateTools.datetimeToString(time));
            statement.setLong(3, location);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    report = statisticViewQuery(conn, results, decodeAncestors);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return report;
    }

    public static EpidemicReport readBySql(Connection conn, String dataset, Date time, long location, boolean decodeAncestors) {
        EpidemicReport report = null;
        try ( Statement statement = conn.createStatement()) {
            String sql = StatisticViewSelect + " WHERE "
                    + " data_set='" + stringValue(dataset) + "' AND "
                    + "  time='" + DateTools.datetimeToString(time) + "' AND "
                    + " locationid=" + location;
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    report = statisticViewQuery(conn, results, decodeAncestors);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return report;
    }

    public static EpidemicReport statisticViewQuery(Connection conn, ResultSet results, boolean decodeAncestors) {
        if (results == null) {
            return null;
        }
        try {
            EpidemicReport report = new EpidemicReport();
            report.setEpid(results.getLong("epid"));
            report.setDataSet(results.getString("data_set"));
            report.setLocationid(results.getLong("locationid"));
            GeographyCode location = TableGeographyCode.readResults(conn, results, decodeAncestors);
            report.setLocation(location);
            report.setTime(results.getTimestamp("time").getTime());
            report.setConfirmed(results.getLong("confirmed"));
            report.setHealed(results.getLong("healed"));
            report.setDead(results.getLong("dead"));
            report.setIncreasedConfirmed(results.getLong("increased_confirmed"));
            report.setIncreasedHealed(results.getLong("increased_healed"));
            report.setIncreasedDead(results.getLong("increased_dead"));
            report.setHealedConfirmedPermillage(DoubleTools.scale(results.getDouble("healed_confirmed_permillage"), 2));
            report.setDeadConfirmedPermillage(DoubleTools.scale(results.getDouble("dead_confirmed_permillage"), 2));
            report.setConfirmedPopulationPermillage(DoubleTools.scale(results.getDouble("confirmed_population_permillage"), 2));
            report.setHealedPopulationPermillage(DoubleTools.scale(results.getDouble("healed_population_permillage"), 2));
            report.setDeadPopulationPermillage(DoubleTools.scale(results.getDouble("dead_population_permillage"), 2));
            report.setConfirmedAreaPermillage(DoubleTools.scale(results.getDouble("confirmed_area_permillage"), 2));
            report.setHealedAreaPermillage(DoubleTools.scale(results.getDouble("healed_area_permillage"), 2));
            report.setDeadAreaPermillage(DoubleTools.scale(results.getDouble("dead_area_permillage"), 2));
            report.setSource(results.getShort("source"));
            return report;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // no location values
    public static EpidemicReport read(Connection conn, ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            EpidemicReport report = new EpidemicReport();
            report.setEpid(results.getLong("epid"));
            report.setDataSet(results.getString("data_set"));
            report.setLocationid(results.getLong("locationid"));
            report.setTime(results.getTimestamp("time").getTime());
            report.setConfirmed(results.getLong("confirmed"));
            report.setHealed(results.getLong("healed"));
            report.setDead(results.getLong("dead"));
            report.setIncreasedConfirmed(results.getLong("increased_confirmed"));
            report.setIncreasedHealed(results.getLong("increased_healed"));
            report.setIncreasedDead(results.getLong("increased_dead"));
            report.setSource(results.getShort("source"));
            return report;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean validReport(EpidemicReport report) {
        try {
            return (report != null)
                    && (report.getLocationid() > 0)
                    && (report.getTime() > 0)
                    && (report.getConfirmed() > 0
                    || report.getHealed() > 0
                    || report.getDead() > 0);
//            if (!valid) {
//                return false;
//            }
//            // Only care data and ignore timeDuration
//            String dateString = DateTools.datetimeToString(report.getTime()).substring(0, 10) + EpidemicReport.COVID19TIME;
//            report.setTime(DateTools.stringToDatetime(dateString).getTime());
//            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean write(EpidemicReport report) {
        if (!validReport(report)) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return write(conn, report);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(Connection conn, EpidemicReport report) {
        if (conn == null || !validReport(report)) {
            return false;
        }
        try {
            boolean exist = false;
            if (report.getEpid() <= 0) {
                try ( PreparedStatement statement = conn.prepareStatement(EqualQuery)) {
                    statement.setMaxRows(1);
                    statement.setString(1, report.getDataSet());
                    statement.setString(2, DateTools.datetimeToString(report.getTime()));
                    statement.setLong(3, report.getLocationid());
                    try ( ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            exist = true;
                            report.setEpid(results.getLong("epid"));
                        }
                    }
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(EPidQuery)) {
                    statement.setMaxRows(1);
                    statement.setLong(1, report.getEpid());
                    try ( ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            exist = true;
                        }
                    }
                }
            }
            if (exist) {
                return update(conn, report);
            } else {
                return insert(conn, report);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static long write(List<EpidemicReport> reports, boolean replace) {
        if (reports == null || reports.isEmpty()) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            return write(conn, reports, replace);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static long write(Connection conn, List<EpidemicReport> reports, boolean replace) {
        if (conn == null || reports == null || reports.isEmpty()) {
            return 0;
        }
        try ( PreparedStatement equalQuery = conn.prepareStatement(ExistQuery);
                 PreparedStatement epidQuery = conn.prepareStatement(EPidQuery);
                 PreparedStatement update = conn.prepareStatement(UpdateAsEPid);
                 PreparedStatement insert = conn.prepareStatement(Insert)) {
            epidQuery.setMaxRows(1);
            equalQuery.setMaxRows(1);
            long count = 0;
            conn.setAutoCommit(false);
            for (EpidemicReport report : reports) {
                if (!validReport(report)) {
                    continue;
                }
                boolean exist = false;
                if (report.getEpid() <= 0) {
                    equalQuery.setString(1, report.getDataSet());
                    equalQuery.setString(2, DateTools.datetimeToString(report.getTime()));
                    equalQuery.setLong(3, report.getLocationid());
                    try ( ResultSet results = equalQuery.executeQuery()) {
                        if (results.next()) {
                            exist = true;
                            report.setEpid(results.getLong("epid"));
                        }
                    }
                } else {
                    epidQuery.setLong(1, report.getEpid());
                    try ( ResultSet results = epidQuery.executeQuery()) {
                        if (results.next()) {
                            exist = true;
                        }
                    }
                }
                if (exist) {
                    if (replace && updateAsEPid(update, report)) {
                        count++;
                    }
                } else {
                    if (insert(insert, report)) {
                        count++;
                    }
                }
            }
            conn.commit();
            return count;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    public static boolean insert(Connection conn, EpidemicReport report) {
        if (conn == null || !validReport(report)) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Insert)) {
            return insert(statement, report);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean insert(PreparedStatement statement, EpidemicReport report) {
        if (statement == null || !validReport(report)) {
            return false;
        }
        try {
            setInsert(statement, report);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    // Should not run Batch updateData against EpidemicReport because each new data need compare previous data.
    // External data may include unexpected data inconsistent.
    private static boolean setInsert(PreparedStatement statement, EpidemicReport report) {
        if (statement == null || !validReport(report)) {
            return false;
        }
        try {
            statement.setString(1, report.getDataSet());
            statement.setLong(2, report.getLocationid());
            statement.setString(3, DateTools.datetimeToString(report.getTime()));
            statement.setShort(4, (short) report.getSource());
            statement.setLong(5, report.getConfirmed());
            statement.setLong(6, report.getHealed());
            statement.setLong(7, report.getDead());
            statement.setLong(8, report.getIncreasedConfirmed());
            statement.setLong(9, report.getIncreasedHealed());
            statement.setLong(10, report.getIncreasedDead());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String equalCondition(EpidemicReport report) {
        try {
            return " data_set='" + stringValue(report.getDataSet()) + "' "
                    + " AND time='" + DateTools.datetimeToString(report.getTime()) + "'  "
                    + " AND locationid=" + report.getLocationid();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean update(Connection conn, EpidemicReport report) {
        if (conn == null || !validReport(report)) {
            return false;
        }
        try {
            if (report.getEpid() > 0) {
                try ( PreparedStatement statement = conn.prepareStatement(UpdateAsEPid)) {
                    return updateAsEPid(statement, report);
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(UpdateAsEqual)) {
                    return updateAsEqual(statement, report);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean updateAsEPid(PreparedStatement statement, EpidemicReport report) {
        if (statement == null || !validReport(report) || report.getEpid() < 0) {
            return false;
        }
        try {
            if (setUpdateAsEPid(statement, report)) {
                return statement.executeUpdate() > 0;
            } else {
                return false;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    // Should not run Batch updateData against EpidemicReport because each new data need compare previous data.
    // External data may include unexpected data inconsistent.
    private static boolean setUpdateAsEPid(PreparedStatement statement, EpidemicReport report) {
        if (statement == null || !validReport(report) || report.getEpid() < 0) {
            return false;
        }
        try {
            statement.setString(1, report.getDataSet());
            statement.setLong(2, report.getLocationid());
            statement.setString(3, DateTools.datetimeToString(report.getTime()));
            statement.setLong(4, report.getConfirmed());
            statement.setLong(5, report.getHealed());
            statement.setLong(6, report.getDead());
            statement.setLong(7, report.getIncreasedConfirmed());
            statement.setLong(8, report.getIncreasedHealed());
            statement.setLong(9, report.getIncreasedDead());
            statement.setShort(10, (short) report.getSource());
            statement.setLong(11, report.getEpid());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean updateAsEqual(PreparedStatement statement, EpidemicReport report) {
        if (statement == null || !validReport(report)) {
            return false;
        }
        try {
            if (setUpdateAsEqual(statement, report)) {
                return statement.executeUpdate() > 0;
            } else {
                return false;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    // Should not run Batch updateData against EpidemicReport because each new data need compare previous data.
    // External data may include unexpected data inconsistent.
    private static boolean setUpdateAsEqual(PreparedStatement statement, EpidemicReport report) {
        if (statement == null || !validReport(report)) {
            return false;
        }
        try {
            statement.setLong(1, report.getConfirmed());
            statement.setLong(3, report.getHealed());
            statement.setLong(4, report.getDead());
            statement.setLong(5, report.getIncreasedConfirmed());
            statement.setLong(6, report.getIncreasedHealed());
            statement.setLong(7, report.getIncreasedDead());
            statement.setShort(8, (short) report.getSource());
            statement.setString(9, report.getDataSet());
            statement.setString(10, DateTools.datetimeToString(report.getTime()));
            statement.setLong(11, report.getLocationid());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static int update(List<EpidemicReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return 0;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement update = conn.prepareStatement(UpdateAsEPid)) {
            conn.setAutoCommit(false);
            int count = 0;
            for (EpidemicReport report : reports) {
                if (!validReport(report)) {
                    continue;
                }
                if (updateAsEPid(update, report)) {
                    count++;
                }
            }
            conn.commit();
            return count;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    public static List<String> datasets() {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return datasets(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<String> datasets(Connection conn) {
        List<String> datasets = new ArrayList();
        if (conn == null) {
            return datasets;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Datasets)) {
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    String c = results.getString("data_set");
                    if (c != null && !c.trim().isBlank()) {
                        datasets.add(c);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return datasets;
    }

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
        List<Date> times = new ArrayList<>();
        if (conn == null) {
            return times;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Times)) {
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Date d = results.getTimestamp("time");
                    if (d != null) {
                        times.add(d);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return times;
    }

    public static List<Date> times(Connection conn, String dataset) {
        if (dataset == null) {
            return times(conn);
        }
        List<Date> times = new ArrayList<>();
        if (conn == null) {
            return times;
        }
        try ( PreparedStatement statement = conn.prepareStatement(DatasetTimes)) {
            statement.setString(1, dataset);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Date d = results.getTimestamp("time");
                    if (d != null) {
                        times.add(d);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return times;
    }

    public static int datasetCount(Connection conn, String dataset) {
        int size = 0;
        try ( PreparedStatement statement = conn.prepareStatement(DatasetCount)) {
            statement.setString(1, dataset);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    size = results.getInt(1);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return size;
    }

    public static boolean delete(long epid) {
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(DeleteEPid)) {
            statement.setLong(1, epid);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(PreparedStatement statement, long epid) {
        if (statement == null) {
            return false;
        }
        try {
            statement.setLong(1, epid);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(String dataset) {
        if (dataset == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(DeleteDataset)) {
            statement.setString(1, dataset);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public List<String> importNecessaryFields() {
        return Arrays.asList(Languages.message("DataSet"), Languages.message("Time"), Languages.message("Confirmed"), Languages.message("Healed"), Languages.message("Dead"),
                Languages.message("Longitude"), Languages.message("Latitude"), Languages.message("Level"),
                Languages.message("Continent"), Languages.message("Country"), Languages.message("Province"), Languages.message("City"),
                Languages.message("County"), Languages.message("Town"), Languages.message("Village"), Languages.message("Building"), Languages.message("PointOfInterest")
        );
    }

    @Override
    public List<String> importAllFields() {
        return Arrays.asList(Languages.message("DataSet"), Languages.message("Time"), Languages.message("Confirmed"), Languages.message("Healed"), Languages.message("Dead"),
                Languages.message("IncreasedConfirmed"), Languages.message("IncreasedHealed"), Languages.message("IncreasedDead"),
                Languages.message("Longitude"), Languages.message("Latitude"), Languages.message("Level"),
                Languages.message("Continent"), Languages.message("Country"), Languages.message("Province"), Languages.message("City"),
                Languages.message("County"), Languages.message("Town"), Languages.message("Village"), Languages.message("Building"), Languages.message("PointOfInterest"),
                Languages.message("DataSource")
        );
    }

}
