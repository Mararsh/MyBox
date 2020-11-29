package mara.mybox.db;

import java.io.File;
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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-4-5
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
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<VisitHistory> checkFilesExisted(Connection conn, List<VisitHistory> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        List<VisitHistory> valid = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (VisitHistory r : records) {
            if (r.getResourceType() == ResourceType.File || r.getResourceType() == ResourceType.Path) {
                String fname = r.getResourceValue();
                try {
                    if (!new File(fname).exists()) {
                        delete(conn, r);
                    } else if (!names.contains(fname)) {
                        names.add(fname);
                        valid.add(r);
                    }
                } catch (Exception e) {
                }
            } else {
                valid.add(r);
            }
        }
        return valid;
    }

    public static List<VisitHistory> findList(Connection conn, ResultSet results) {
        List<VisitHistory> records = new ArrayList<>();
        try {
            while (results.next()) {
                VisitHistory his = read(results);
                if (his != null) {
                    records.add(his);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return checkFilesExisted(conn, records);
    }

    public static List<VisitHistory> find(Connection conn, PreparedStatement statement) {
        List<VisitHistory> records = new ArrayList<>();
        try ( ResultSet results = statement.executeQuery()) {
            records = findList(conn, results);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<VisitHistory> find(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(AllQuery)) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            records = find(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (resourceType < 0) {
            return records;
        }
        final String sql = " SELECT  * FROM visit_history "
                + "  WHERE resource_type=? ORDER BY last_visit_time  DESC  ";
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(sql)) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            statement.setInt(1, resourceType);
            records = find(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return find(resourceType, types, count);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return find(resourceType, types, count);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return find(resourceType, types, count);
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            if (resourceType <= 0) {
                final String sql = " SELECT   * FROM visit_history "
                        + "  WHERE file_type=?  ORDER BY last_visit_time  DESC  ";
                try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                    if (count > 0) {
                        statement.setMaxRows(count);
                    }
                    statement.setInt(1, fileType);
                    records = find(conn, statement);
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
                    records = find(conn, statement);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int[] fileTypes, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (fileTypes == null || fileTypes.length == 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT   * FROM visit_history  WHERE ( file_type=" + fileTypes[0];
            for (int i = 1; i < fileTypes.length; ++i) {
                sql += " OR file_type=" + fileTypes[i];
            }
            sql += " )";
            if (resourceType > 0) {
                sql += "  AND resource_type=" + resourceType;
            }
            sql += " ORDER BY last_visit_time  DESC  ";
            if (count > 0) {
                statement.setMaxRows(count);
            }
            try ( ResultSet results = statement.executeQuery(sql)) {
                records = findList(conn, results);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int fileType, int operationType, int count) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return find(resourceType, types, operationType, count);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return find(resourceType, types, operationType, count);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return find(resourceType, types, operationType, count);
        }
        if (operationType < 0) {
            return find(resourceType, fileType, count);
        }
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
                        records = find(conn, statement);
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
                        records = find(conn, statement);
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
                        records = find(conn, statement);
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
                        records = find(conn, statement);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int[] fileTypes, int operationType, int count) {
        if (operationType < 0) {
            return find(resourceType, fileTypes, count);
        }
        List<VisitHistory> records = new ArrayList<>();
        if (fileTypes == null || fileTypes.length == 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM visit_history WHERE"
                    + " operation_type=" + operationType
                    + " AND ( file_type=" + fileTypes[0];
            for (int i = 1; i < fileTypes.length; ++i) {
                sql += " OR file_type=" + fileTypes[i];
            }
            sql += " )";
            if (resourceType > 0) {
                sql += "  AND resource_type=" + resourceType;
            }
            sql += " ORDER BY last_visit_time  DESC  ";
            if (count > 0) {
                statement.setMaxRows(count);
            }
            try ( ResultSet results = statement.executeQuery(sql)) {
                records = findList(conn, results);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static VisitHistory find(int resourceType, int fileType, int operationType, String value) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return find(conn, resourceType, fileType, operationType, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public static VisitHistory find(Connection conn, int resourceType, int fileType, int operationType, String value) {
        if (conn == null || resourceType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try {
            if (fileType <= 0) {
                final String sql = " SELECT * FROM visit_history WHERE resource_type=?"
                        + " AND operation_type=? AND  resource_value=?";
                try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setMaxRows(1);
                    statement.setInt(1, resourceType);
                    statement.setInt(2, operationType);
                    statement.setString(3, value);
                    VisitHistory his = null;
                    try ( ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            his = read(results);
                        }
                    }
                    return his;
                }
            } else {
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
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public static List<VisitHistory> findAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
                records = find(conn, statement);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<VisitHistory> findNoAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
                records = find(conn, statement);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static boolean update(int resourceType, int fileType, int operationType, String value) {
        return update(resourceType, fileType, operationType, value, null);
    }

    public static boolean update(Connection conn, int resourceType, int fileType, int operationType, String value) {
        return update(conn, resourceType, fileType, operationType, value, null);
    }

    public static boolean update(int resourceType, int fileType, int operationType, String value, String more) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return update(conn, resourceType, fileType, operationType, value, more);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean update(Connection conn, int resourceType, int fileType, int operationType, String value, String more) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        int finalType = fileType;
        if (fileType == FileType.MultipleFrames || fileType == FileType.Image) {
            String v = value.toLowerCase();
            if (v.endsWith(".gif")) {
                finalType = FileType.Gif;
            } else if (v.endsWith(".tif") || v.endsWith(".tiff")) {
                finalType = FileType.Tif;
            } else {
                finalType = FileType.Image;
            }
        }
        try {
            VisitHistory exist = find(conn, resourceType, finalType, operationType, value);
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
                        statement.setInt(4, finalType);
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
                        statement.setInt(5, finalType);
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
                        statement.setInt(2, finalType);
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
                        statement.setInt(2, finalType);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(Connection conn, int resourceType, int fileType, int operationType, String value) {
        if (conn == null || resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        String sql = "DELETE FROM visit_history "
                + " WHERE resource_type=? AND file_type=? AND operation_type=? AND  resource_value=?";
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, resourceType);
            statement.setInt(2, fileType);
            statement.setInt(3, operationType);
            statement.setString(4, value);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(int resourceType, int fileType, int operationType, String value) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return delete(conn, resourceType, resourceType, operationType, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(VisitHistory v) {
        return delete(v.getResourceType(), v.getFileType(), v.getOperationType(), v.getResourceValue());
    }

    public static boolean delete(Connection conn, VisitHistory v) {
        return delete(conn, v.getResourceType(), v.getFileType(), v.getOperationType(), v.getResourceValue());
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
            return false;
        }
    }

}
