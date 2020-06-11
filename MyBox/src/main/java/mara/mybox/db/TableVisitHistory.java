package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.VisitHistory.FileType;
import mara.mybox.data.VisitHistory.OperationType;
import mara.mybox.data.VisitHistory.ResourceType;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-4-5
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableVisitHistory extends DerbyBase {

    private static final String AllQuery
            = " SELECT * FROM visit_history  ORDER BY last_visit_time  DESC  ";

    public TableVisitHistory() {
        Table_Name = "visit_history";
        Keys = new ArrayList<>() {
            {
                add("resource_type");
                add("file_type");
                add("operation_type");
                add("resource_value");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE visit_history ( "
                + "  resource_type  SMALLINT NOT NULL, "
                + "  file_type  SMALLINT NOT NULL, "
                + "  operation_type  SMALLINT NOT NULL, "
                + "  resource_value  VARCHAR(1024) NOT NULL, "
                + "  data_more VARCHAR(1024), "
                + "  last_visit_time TIMESTAMP NOT NULL, "
                + "  visit_count  INT , "
                + "  PRIMARY KEY (resource_type, file_type, operation_type, resource_value)"
                + " )";
    }

    public static VisitHistory read(ResultSet results) {
        try {
            VisitHistory his = new VisitHistory();
            his.setResourceType(results.getInt("resource_type"));
            his.setFileType(results.getInt("file_type"));
            his.setOperationType(results.getInt("operation_type"));
            his.setResourceValue(results.getString("resource_value"));
            his.setDataMore(results.getString("data_more"));
            his.setLastVisitTime(results.getTimestamp("last_visit_time"));
            his.setVisitCount(results.getInt("visit_count"));
            return his;
        } catch (Exception e) {
            failed(e);
            return null;
            // logger.debug(e.toString());
        }
    }

    public static List<VisitHistory> findList(ResultSet results) {
        List<VisitHistory> records = new ArrayList<>();
        try {
            while (results.next()) {
                VisitHistory his = read(results);
                if (his != null) {
                    records.add(his);
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(PreparedStatement statement) {
        List<VisitHistory> records = new ArrayList<>();
        try ( ResultSet results = statement.executeQuery()) {
            return findList(results);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            try ( PreparedStatement statement = conn.prepareStatement(AllQuery)) {
                if (count > 0) {
                    statement.setMaxRows(count);
                }
                records = find(statement);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (resourceType < 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            final String sql = " SELECT  * FROM visit_history "
                    + "  WHERE resource_type=? ORDER BY last_visit_time  DESC  ";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                if (count > 0) {
                    statement.setMaxRows(count);
                }
                statement.setInt(1, resourceType);
                records = find(statement);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int fileType, int count) {
        if (fileType == 0) {
            return find(resourceType, count);
        }
        List<VisitHistory> records = new ArrayList<>();
        if (fileType < 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            if (resourceType <= 0) {
                final String sql = " SELECT   * FROM visit_history "
                        + "  WHERE file_type=?  ORDER BY last_visit_time  DESC  ";
                try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                    if (count > 0) {
                        statement.setMaxRows(count);
                    }
                    statement.setInt(1, fileType);
                    records = find(statement);
                }
            } else {
                final String sql = " SELECT   * FROM visit_history "
                        + "  WHERE resource_type=?  AND file_type=? "
                        + " ORDER BY last_visit_time  DESC  ";
                try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                    if (count > 0) {
                        statement.setMaxRows(count);
                    }
                    statement.setInt(1, resourceType);
                    statement.setInt(2, fileType);
                    records = find(statement);
                }
            }

        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int[] fileTypes, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (fileTypes == null || fileTypes.length == 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = " SELECT   * FROM visit_history  WHERE ( file_type=" + fileTypes[0];
            for (int i = 1; i < fileTypes.length; ++i) {
                sql += " OR file_type=" + fileTypes[i];
            }
            sql += " )";
            if (resourceType > 0) {
                sql += "  AND resource_type=" + resourceType;
            }
            sql += " ORDER BY last_visit_time  DESC  ";
            try ( Statement statement = conn.createStatement()) {
                if (count > 0) {
                    statement.setMaxRows(count);
                }
                try ( ResultSet results = statement.executeQuery(sql)) {
                    records = findList(results);
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int fileType, int operationType,
            int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (operationType < 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            if (resourceType <= 0) {
                if (fileType <= 0) {
                    final String sql = " SELECT   * FROM visit_history "
                            + " WHERE  operation_type=? "
                            + " ORDER BY last_visit_time  DESC  ";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        if (count > 0) {
                            statement.setMaxRows(count);
                        }
                        statement.setInt(1, operationType);
                        records = find(statement);
                    }
                } else {
                    final String sql = " SELECT   * FROM visit_history "
                            + " WHERE file_type=? AND operation_type=?"
                            + " ORDER BY last_visit_time  DESC  ";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        if (count > 0) {
                            statement.setMaxRows(count);
                        }
                        statement.setInt(1, fileType);
                        statement.setInt(2, operationType);
                        records = find(statement);
                    }
                }
            } else {
                if (fileType <= 0) {
                    final String sql = " SELECT   * FROM visit_history "
                            + " WHERE resource_type=? AND operation_type=?"
                            + " ORDER BY last_visit_time  DESC  ";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        if (count > 0) {
                            statement.setMaxRows(count);
                        }
                        statement.setInt(1, resourceType);
                        statement.setInt(2, operationType);
                        records = find(statement);
                    }
                } else {
                    final String sql = " SELECT   * FROM visit_history "
                            + " WHERE resource_type=? AND file_type=? AND operation_type=?"
                            + " ORDER BY last_visit_time  DESC  ";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        if (count > 0) {
                            statement.setMaxRows(count);
                        }
                        statement.setInt(1, resourceType);
                        statement.setInt(2, fileType);
                        statement.setInt(3, operationType);
                        records = find(statement);
                    }
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static VisitHistory find(int resourceType, int fileType, int operationType, String value) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return find(conn, resourceType, fileType, operationType, value);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static VisitHistory find(Connection conn,
            int resourceType, int fileType, int operationType, String value) {
        if (conn == null
                || resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try {
            final String sql = " SELECT * FROM visit_history WHERE resource_type=?"
                    + " AND file_type=? AND operation_type=? AND  resource_value=?";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setMaxRows(1);
                statement.setInt(1, resourceType);
                statement.setInt(2, fileType);
                statement.setInt(3, operationType);
                statement.setString(4, value);
                VisitHistory his = null;
                try ( ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        his = read(results);
                    }
                }
                return his;
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return null;
    }

    public static List<VisitHistory> findAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            final String sql = " SELECT   * FROM visit_history "
                    + " WHERE resource_type=? AND file_type=? "
                    + " AND SUBSTR(LOWER(resource_value), LENGTH(resource_value) - 3 ) IN ('.png',  '.tif', 'tiff') "
                    + " ORDER BY last_visit_time  DESC  ";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                if (count > 0) {
                    statement.setMaxRows(count);
                }
                statement.setInt(1, ResourceType.File);
                statement.setInt(2, FileType.Image);
                records = find(statement);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> findNoAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            final String sql = " SELECT   * FROM visit_history "
                    + " WHERE resource_type=? AND file_type=? "
                    + " AND SUBSTR(LOWER(resource_value), LENGTH(resource_value) - 3 ) IN ('.jpg', '.bmp', '.gif', '.pnm', 'wbmp') "
                    + " ORDER BY last_visit_time  DESC  ";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                if (count > 0) {
                    statement.setMaxRows(count);
                }
                statement.setInt(1, ResourceType.File);
                statement.setInt(2, FileType.Image);
                records = find(statement);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static boolean update(int resourceType, int fileType, int operationType, String value) {
        return update(resourceType, fileType, operationType, value, null);
    }

    public static boolean update(int resourceType, int fileType, int operationType, String value, String more) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            VisitHistory exist = find(conn, resourceType, fileType, operationType, value);
            Date d = new Date();
            if (exist != null) {
                if (more == null) {
                    final String sql = "UPDATE visit_history SET "
                            + " visit_count=?, last_visit_time=?"
                            + " WHERE resource_type=? AND file_type=? AND operation_type=? AND  resource_value=?";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        statement.setInt(1, exist.getVisitCount() + 1);
                        statement.setString(2, DateTools.datetimeToString(d));
                        statement.setInt(3, resourceType);
                        statement.setInt(4, fileType);
                        statement.setInt(5, operationType);
                        statement.setString(6, value);
                        return statement.executeUpdate() >= 0;
                    }
                } else {
                    final String sql = "UPDATE visit_history SET "
                            + " visit_count=?, data_more=?, last_visit_time=?"
                            + " WHERE resource_type=? AND file_type=? AND operation_type=?"
                            + " AND  resource_value=?";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        statement.setInt(1, exist.getVisitCount() + 1);
                        statement.setString(2, more);
                        statement.setString(3, DateTools.datetimeToString(d));
                        statement.setInt(4, resourceType);
                        statement.setInt(5, fileType);
                        statement.setInt(6, operationType);
                        statement.setString(7, value);
                        return statement.executeUpdate() >= 0;
                    }
                }
            } else {
                if (more == null) {
                    final String sql = "INSERT INTO visit_history "
                            + "(resource_type, file_type, operation_type, resource_value, last_visit_time, visit_count) "
                            + "VALUES(?, ?,?,?,?,? )";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        statement.setInt(1, resourceType);
                        statement.setInt(2, fileType);
                        statement.setInt(3, operationType);
                        statement.setString(4, value);
                        statement.setString(5, DateTools.datetimeToString(d));
                        statement.setInt(6, 1);
                        return statement.executeUpdate() >= 0;
                    }
                } else {
                    final String sql = "INSERT INTO visit_history "
                            + "(resource_type, file_type, operation_type, resource_value, data_more, last_visit_time, visit_count) "
                            + "VALUES(?, ?, ?, ?, ?, ? ,?)";
                    try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                        statement.setInt(1, resourceType);
                        statement.setInt(2, fileType);
                        statement.setInt(3, operationType);
                        statement.setString(4, value);
                        statement.setString(5, more);
                        statement.setString(6, DateTools.datetimeToString(d));
                        statement.setInt(7, 1);
                        return statement.executeUpdate() >= 0;
                    }
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updateMenu(int fileType, String name, String fxml) {
        return updateMenu(fileType, OperationType.Access, name, fxml);
    }

    public static boolean updateMenu(int fileType, int operationType, String name, String fxml) {
        if (fileType < 0 || operationType < 0 || name == null || fxml == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            VisitHistory exist = null;
            String query = " SELECT * FROM visit_history "
                    + " WHERE resource_type=? AND file_type=? AND operation_type=?"
                    + " AND resource_value=? AND data_more=?";
            try ( PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setInt(1, ResourceType.Menu);
                statement.setInt(2, fileType);
                statement.setInt(3, operationType);
                statement.setString(4, name);
                statement.setString(5, fxml);
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    exist = read(results);
                }
            }
            Date d = new Date();
            if (exist != null) {
                final String sql = "UPDATE visit_history SET "
                        + "visit_count=?, last_visit_time=?"
                        + " WHERE resource_type=? AND file_type=? AND operation_type=?"
                        + " AND resource_value=? AND data_more=?";
                try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setInt(1, exist.getVisitCount() + 1);
                    statement.setString(2, DateTools.datetimeToString(d));
                    statement.setInt(3, ResourceType.Menu);
                    statement.setInt(4, fileType);
                    statement.setInt(5, operationType);
                    statement.setString(6, name);
                    statement.setString(7, fxml);
                    return statement.executeUpdate() >= 0;
                }

            } else {
                final String sql = "INSERT INTO visit_history "
                        + "(resource_type, file_type, operation_type, resource_value, data_more, last_visit_time, visit_count) "
                        + "VALUES(?,?,?,?,?,?,?)";
                try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setInt(1, ResourceType.Menu);
                    statement.setInt(2, fileType);
                    statement.setInt(3, operationType);
                    statement.setString(4, name);
                    statement.setString(5, fxml);
                    statement.setString(6, DateTools.datetimeToString(d));
                    statement.setInt(7, 1);
                    return statement.executeUpdate() >= 0;
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(int resourceType, int fileType, int operationType, String value) {
        final String sql = "DELETE FROM visit_history "
                + " WHERE resource_type=? AND file_type=? "
                + " AND operation_type=? AND resource_value=?";
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, resourceType);
            statement.setInt(2, fileType);
            statement.setInt(3, operationType);
            statement.setString(4, value);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(VisitHistory v) {
        return delete(v.getResourceType(), v.getFileType(), v.getOperationType(), v.getResourceValue());
    }

    public static boolean clearType(int resourceType, int fileType, int operationType) {
        final String sql = "DELETE FROM visit_history "
                + " WHERE resource_type=? AND file_type=? AND operation_type=?";
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, resourceType);
            statement.setInt(2, fileType);
            statement.setInt(3, operationType);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean clear() {
        final String sql = "DELETE FROM visit_history";
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

}
