package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
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

/**
 * @Author Mara
 * @CreateDate 2019-4-5
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableVisitHistory extends DerbyBase {

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

    public static List<VisitHistory> find(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql = " SELECT * FROM visit_history "
                    + " ORDER BY last_visit_time  DESC  ";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(results.getInt("resource_type"));
                his.setFileType(results.getInt("file_type"));
                his.setOperationType(results.getInt("operation_type"));
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (resourceType < 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql = " SELECT  * FROM visit_history "
                    + "  WHERE resource_type=" + resourceType
                    + " ORDER BY last_visit_time  DESC  ";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(resourceType);
                his.setFileType(results.getInt("file_type"));
                his.setOperationType(results.getInt("operation_type"));
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql;
            if (resourceType <= 0) {
                sql = " SELECT   * FROM visit_history "
                        + "  WHERE file_type=" + fileType
                        + " ORDER BY last_visit_time  DESC  ";
            } else {
                sql = " SELECT   * FROM visit_history "
                        + "  WHERE resource_type=" + resourceType
                        + " AND file_type=" + fileType
                        + " ORDER BY last_visit_time  DESC  ";
            }
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(resourceType);
                his.setFileType(fileType);
                his.setOperationType(results.getInt("operation_type"));
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> find(int resourceType, int[] fileTypes, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (fileTypes == null || fileTypes.length == 0) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql = " SELECT   * FROM visit_history  WHERE ( file_type=" + fileTypes[0];
            for (int i = 1; i < fileTypes.length; i++) {
                sql += " OR file_type=" + fileTypes[i];
            }
            sql += " )";
            if (resourceType > 0) {
                sql += "  AND resource_type=" + resourceType;
            }
            sql += " ORDER BY last_visit_time  DESC  ";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(resourceType);
                his.setFileType(results.getInt("file_type"));
                his.setOperationType(results.getInt("operation_type"));
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql;
            if (resourceType <= 0) {
                if (fileType <= 0) {
                    sql = " SELECT   * FROM visit_history "
                            + " WHERE  operation_type=" + operationType
                            + " ORDER BY last_visit_time  DESC  ";
                } else {
                    sql = " SELECT   * FROM visit_history "
                            + " WHERE file_type=" + fileType + " AND operation_type=" + operationType
                            + " ORDER BY last_visit_time  DESC  ";
                }
            } else {
                if (fileType <= 0) {
                    sql = " SELECT   * FROM visit_history "
                            + " WHERE resource_type=" + resourceType
                            + " AND operation_type=" + operationType
                            + " ORDER BY last_visit_time  DESC  ";
                } else {
                    sql = " SELECT   * FROM visit_history "
                            + " WHERE resource_type=" + resourceType
                            + " AND file_type=" + fileType + " AND operation_type=" + operationType
                            + " ORDER BY last_visit_time  DESC  ";
                }
            }
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(resourceType);
                his.setFileType(fileType);
                his.setOperationType(operationType);
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static VisitHistory find(int resourceType, int fileType, int operationType, String value) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            String sql = " SELECT   * FROM visit_history WHERE resource_type=" + resourceType
                    + " AND file_type=" + fileType + " AND operation_type=" + operationType
                    + " AND  resource_value='" + value + "' ";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(resourceType);
                his.setFileType(fileType);
                his.setOperationType(operationType);
                his.setResourceValue(value);
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                return his;
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static List<VisitHistory> findAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql;
            sql = " SELECT   * FROM visit_history "
                    + " WHERE resource_type=" + ResourceType.File
                    + " AND file_type=" + FileType.Image
                    + " AND SUBSTR(LOWER(resource_value), LENGTH(resource_value) - 3 ) IN ('.png',  '.tif', 'tiff') "
                    + " ORDER BY last_visit_time  DESC  ";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(ResourceType.File);
                his.setFileType(FileType.Image);
                his.setOperationType(results.getInt("operation_type"));
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static List<VisitHistory> findNoAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            if (count > 0) {
                statement.setMaxRows(count);
            }
            String sql;
            sql = " SELECT   * FROM visit_history "
                    + " WHERE resource_type=" + ResourceType.File
                    + " AND file_type=" + FileType.Image
                    + " AND SUBSTR(LOWER(resource_value), LENGTH(resource_value) - 3 ) IN ('.jpg', '.bmp', '.gif', '.pnm', 'wbmp') "
                    + " ORDER BY last_visit_time  DESC  ";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(ResourceType.File);
                his.setFileType(FileType.Image);
                his.setOperationType(results.getInt("operation_type"));
                his.setResourceValue(results.getString("resource_value"));
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(results.getTimestamp("last_visit_time"));
                his.setVisitCount(results.getInt("visit_count"));
                records.add(his);
            }
        } catch (Exception e) {  failed(e);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM visit_history WHERE resource_type=" + resourceType
                    + " AND file_type=" + fileType + " AND operation_type=" + operationType
                    + " AND  resource_value='" + value + "' ";
            ResultSet results = statement.executeQuery(sql);
            Date d = new Date();
            if (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(resourceType);
                his.setFileType(fileType);
                his.setOperationType(operationType);
                his.setResourceValue(value);
                his.setDataMore(results.getString("data_more"));
                his.setLastVisitTime(d);
                his.setVisitCount(results.getInt("visit_count") + 1);
                if (more == null) {
                    sql = "UPDATE visit_history SET visit_count=" + his.getVisitCount()
                            + ", last_visit_time='" + DateTools.datetimeToString(d) + "'  "
                            + " WHERE resource_type=" + resourceType
                            + " AND file_type=" + fileType + " AND operation_type=" + operationType
                            + " AND  resource_value='" + value + "' ";
                } else {
                    sql = "UPDATE visit_history SET visit_count=" + his.getVisitCount()
                            + ", data_more='" + more + "'"
                            + ", last_visit_time='" + DateTools.datetimeToString(d) + "'  "
                            + " WHERE resource_type=" + resourceType
                            + " AND file_type=" + fileType + " AND operation_type=" + operationType
                            + " AND  resource_value='" + value + "' ";
                }

            } else {
                if (more == null) {
                    sql = "INSERT INTO visit_history(resource_type, file_type, operation_type, resource_value, last_visit_time, visit_count) VALUES("
                            + resourceType + ", " + fileType + ", " + operationType
                            + ", '" + value + "', '"
                            + DateTools.datetimeToString(d) + "', 1)";
                } else {
                    sql = "INSERT INTO visit_history(resource_type, file_type, operation_type, resource_value, data_more, last_visit_time, visit_count) VALUES("
                            + resourceType + ", " + fileType + ", " + operationType
                            + ", '" + value + "', '" + more + "', '"
                            + DateTools.datetimeToString(d) + "', 1)";
                }
            }
            return statement.executeUpdate(sql) >= 0;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM visit_history WHERE resource_type=" + ResourceType.Menu
                    + " AND file_type=" + fileType + " AND operation_type=" + operationType
                    + " AND resource_value='" + name + "' "
                    + " AND data_more='" + fxml + "' ";
            ResultSet results = statement.executeQuery(sql);
            Date d = new Date();
            if (results.next()) {
                VisitHistory his = new VisitHistory();
                his.setResourceType(ResourceType.Menu);
                his.setFileType(fileType);
                his.setOperationType(operationType);
                his.setResourceValue(name);
                his.setDataMore(fxml);
                his.setLastVisitTime(d);
                his.setVisitCount(results.getInt("visit_count") + 1);
                sql = "UPDATE visit_history SET visit_count=" + his.getVisitCount()
                        + ", last_visit_time='" + DateTools.datetimeToString(d) + "'  "
                        + " WHERE resource_type=" + ResourceType.Menu
                        + " AND file_type=" + fileType + " AND operation_type=" + operationType
                        + " AND resource_value='" + name + "' "
                        + " AND data_more='" + fxml + "' ";

            } else {
                sql = "INSERT INTO visit_history(resource_type, file_type, operation_type, resource_value, data_more, last_visit_time, visit_count) VALUES("
                        + ResourceType.Menu + ", " + fileType + ", " + operationType
                        + ", '" + name + "', '" + fxml + "', '"
                        + DateTools.datetimeToString(d) + "', 1)";

            }
            return statement.executeUpdate(sql) >= 0;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(int resourceType, int fileType, int operationType, String value) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM visit_history "
                    + " WHERE resource_type=" + resourceType
                    + " AND file_type=" + fileType
                    + " AND operation_type=" + operationType
                    + " AND resource_value='" + value + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(VisitHistory v) {
        return delete(v.getResourceType(), v.getFileType(), v.getOperationType(), v.getResourceValue());
    }

    public static boolean clearType(int resourceType, int fileType, int operationType) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM visit_history "
                    + " WHERE resource_type=" + resourceType
                    + " AND file_type=" + fileType + " AND operation_type=" + operationType;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean clear() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM visit_history";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

}
