package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistory.OperationType;
import mara.mybox.db.data.VisitHistory.ResourceType;
import mara.mybox.db.data.VisitHistoryTools;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-4-5
 * @License Apache License Version 2.0
 */
public class TableVisitHistory extends BaseTable<VisitHistory> {

    public TableVisitHistory() {
        tableName = "visit_history";
        defineColumns();
    }

    public TableVisitHistory(boolean defineColumns) {
        tableName = "visit_history";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableVisitHistory defineColumns() {
        addColumn(new ColumnDefinition("resource_type", ColumnDefinition.ColumnType.Short, true, true));
        addColumn(new ColumnDefinition("file_type", ColumnDefinition.ColumnType.Short, true, true));
        addColumn(new ColumnDefinition("operation_type", ColumnDefinition.ColumnType.Short, true, true));
        addColumn(new ColumnDefinition("resource_value", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("data_more", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("last_visit_time", ColumnDefinition.ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("visit_count", ColumnDefinition.ColumnType.Integer));
        orderColumns = "last_visit_time DESC";
        return this;
    }

    private static final String Query_Resource_Type
            = "SELECT  * FROM visit_history WHERE resource_type=? "
            + "ORDER BY last_visit_time  DESC";

    private static final String Query_File_Type
            = "SELECT  * FROM visit_history WHERE file_type=? "
            + "ORDER BY last_visit_time  DESC";

    private static final String Query_Resource_File_Type
            = "SELECT  * FROM visit_history WHERE resource_type=? AND file_type=?"
            + " ORDER BY last_visit_time  DESC";

    private static final String Query_Operation_Type
            = "SELECT  * FROM visit_history WHERE operation_type=? "
            + "ORDER BY last_visit_time  DESC";

    private static final String Query_Operation_File_Type
            = "SELECT  * FROM visit_history WHERE file_type=? AND operation_type=?"
            + " ORDER BY last_visit_time  DESC";

    private static final String Query_Resource_Operation_Type
            = "SELECT  * FROM visit_history WHERE resource_type=? AND operation_type=? "
            + "ORDER BY last_visit_time  DESC";

    private static final String Query_Types
            = "SELECT  * FROM visit_history WHERE resource_type=? AND file_type=? AND operation_type=? "
            + "ORDER BY last_visit_time  DESC";

    private static final String Query_More
            = " SELECT * FROM visit_history WHERE resource_type=? AND file_type=? AND operation_type=?"
            + " AND resource_value=? AND data_more=?";

    private static final String Update_Visit
            = "UPDATE visit_history SET visit_count=?, last_visit_time=?"
            + " WHERE resource_type=? AND file_type=? AND operation_type=? AND  resource_value=?";

    private static final String Update_More
            = "UPDATE visit_history SET visit_count=?, data_more=?, last_visit_time=?"
            + " WHERE resource_type=? AND file_type=? AND operation_type=? AND  resource_value=?";

    private static final String Update_Visit_More
            = "UPDATE visit_history SET visit_count=?, last_visit_time=?"
            + " WHERE resource_type=? AND file_type=? AND operation_type=?"
            + " AND resource_value=? AND data_more=?";

    private static final String Insert_Visit
            = "INSERT INTO visit_history "
            + "(resource_type, file_type, operation_type, resource_value, last_visit_time, visit_count) "
            + "VALUES(?,?,?,?,?,?)";

    private static final String Insert_More
            = "INSERT INTO visit_history "
            + "(resource_type, file_type, operation_type, resource_value, data_more, last_visit_time, visit_count) "
            + "VALUES(?,?,?,?,?,?,?)";

    private static final String Delete_Visit
            = "DELETE FROM visit_history "
            + " WHERE resource_type=? AND file_type=? AND operation_type=? AND  resource_value=?";

    private static final String Clear_Visit
            = "DELETE FROM visit_history "
            + " WHERE resource_type=? AND file_type=? AND operation_type=?";

    @Override
    public boolean valid(VisitHistory record) {
        if (record == null) {
            return false;
        }
        int resourceType = record.getResourceType();
        if (resourceType == ResourceType.File || resourceType == ResourceType.Path) {
            String fname = record.getResourceValue();
            return VisitHistoryTools.validFile(fname, resourceType, record.getFileType());
        } else {
            return true;
        }
    }

    public List<VisitHistory> read(Connection conn, PreparedStatement statement, int count) {
        List<VisitHistory> records = new ArrayList<>();
        try {
            conn.setAutoCommit(true);
            List<String> names = new ArrayList<>();
            try (ResultSet results = statement.executeQuery();
                    PreparedStatement delete = conn.prepareStatement(Delete_Visit)) {
                while (results.next()) {
                    VisitHistory data = readData(results);
                    if (valid(data)) {
                        String name = data.getResourceValue();
                        if (!names.contains(name)) {
                            names.add(name);
                            records.add(data);
                            if (count > 0 && records.size() >= count) {
                                break;
                            }
                        }
                    } else {
                        try {
                            delete.setInt(1, data.getResourceType());
                            delete.setInt(2, data.getFileType());
                            delete.setInt(3, data.getOperationType());
                            delete.setString(4, data.getResourceValue());
                            delete.executeUpdate();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return records;
    }

    public List<VisitHistory> read(int resourceType, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (resourceType < 0) {
            return records;
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(Query_Resource_Type)) {
            conn.setAutoCommit(true);
            statement.setInt(1, resourceType);
            records = read(conn, statement, count);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public List<VisitHistory> read(int resourceType, int fileType, int count) {
        if (fileType == 0) {
            return read(resourceType, count);
        }
        List<VisitHistory> records = new ArrayList<>();
        if (fileType < 0) {
            return records;
        }
        int[] types = VisitHistory.typeGroup(fileType);
        if (types != null) {
            return read(resourceType, types, count);
        }
        try (Connection conn = DerbyBase.getConnection()) {
            if (resourceType <= 0) {
                try (PreparedStatement statement = conn.prepareStatement(Query_File_Type)) {
                    statement.setInt(1, fileType);
                    records = read(conn, statement, count);
                }
            } else {
                try (PreparedStatement statement = conn.prepareStatement(Query_Resource_File_Type)) {
                    statement.setInt(1, resourceType);
                    statement.setInt(2, fileType);
                    records = read(conn, statement, count);
                }
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return records;
    }

    public List<VisitHistory> read(int resourceType, int[] fileTypes, int count) {
        List<VisitHistory> records = new ArrayList<>();
        if (fileTypes == null || fileTypes.length == 0) {
            return records;
        }
        String sql = "SELECT * FROM visit_history  WHERE ( file_type=" + fileTypes[0];
        for (int i = 1; i < fileTypes.length; ++i) {
            sql += " OR file_type=" + fileTypes[i];
        }
        sql += " )";
        if (resourceType > 0) {
            sql += "  AND resource_type=" + resourceType;
        }
        sql += " ORDER BY last_visit_time  DESC";
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            records = read(conn, statement, count);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return records;
    }

    public List<VisitHistory> read(int resourceType, int fileType, int operationType, int count) {
        int[] types = VisitHistory.typeGroup(fileType);
        if (types != null) {
            return read(resourceType, types, operationType, count);
        }
        if (operationType < 0) {
            return read(resourceType, fileType, count);
        }
        List<VisitHistory> records = new ArrayList<>();
        try (Connection conn = DerbyBase.getConnection()) {
            if (resourceType <= 0) {
                if (fileType <= 0) {
                    try (PreparedStatement statement = conn.prepareStatement(Query_Operation_Type)) {
                        statement.setInt(1, operationType);
                        records = read(conn, statement, count);
                    }
                } else {
                    try (PreparedStatement statement = conn.prepareStatement(Query_Operation_File_Type)) {
                        statement.setInt(1, fileType);
                        statement.setInt(2, operationType);
                        records = read(conn, statement, count);
                    }
                }
            } else {
                if (fileType <= 0) {
                    try (PreparedStatement statement = conn.prepareStatement(Query_Resource_Operation_Type)) {
                        statement.setInt(1, resourceType);
                        statement.setInt(2, operationType);
                        records = read(conn, statement, count);
                    }
                } else {
                    try (PreparedStatement statement = conn.prepareStatement(Query_Types)) {
                        statement.setInt(1, resourceType);
                        statement.setInt(2, fileType);
                        statement.setInt(3, operationType);
                        records = read(conn, statement, count);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return records;
    }

    public List<VisitHistory> read(int resourceType, int[] fileTypes, int operationType, int count) {
        if (operationType < 0) {
            return read(resourceType, fileTypes, count);
        }
        List<VisitHistory> records = new ArrayList<>();
        if (fileTypes == null || fileTypes.length == 0) {
            return records;
        }
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
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            records = read(conn, statement, count);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return records;
    }

    public VisitHistory read(int resourceType, int fileType, int operationType, String value) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return read(conn, resourceType, fileType, operationType, value);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return null;
    }

    public VisitHistory read(Connection conn, int resourceType, int fileType, int operationType, String value) {
        if (conn == null || resourceType < 0 || operationType < 0 || value == null) {
            return null;
        }
        try {
            if (fileType <= 0) {
                final String sql = "SELECT * FROM visit_history WHERE resource_type=?"
                        + " AND operation_type=? AND  resource_value=?";
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setMaxRows(1);
                    statement.setInt(1, resourceType);
                    statement.setInt(2, operationType);
                    statement.setString(3, value);
                    VisitHistory his = null;
                    conn.setAutoCommit(true);
                    statement.setMaxRows(1);
                    try (ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            his = readData(results);
                        }
                    }
                    return his;
                }
            } else {
                final String sql = "SELECT * FROM visit_history WHERE resource_type=?"
                        + " AND file_type=? AND operation_type=? AND  resource_value=?";
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setMaxRows(1);
                    statement.setInt(1, resourceType);
                    statement.setInt(2, fileType);
                    statement.setInt(3, operationType);
                    statement.setString(4, value);
                    VisitHistory his = null;
                    conn.setAutoCommit(true);
                    statement.setMaxRows(1);
                    try (ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            his = readData(results);
                        }
                    }
                    return his;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return null;
    }

    public List<VisitHistory> readAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try (Connection conn = DerbyBase.getConnection()) {
            final String sql = " SELECT   * FROM visit_history "
                    + " WHERE resource_type=? AND file_type=? "
                    + " AND SUBSTR(LOWER(resource_value), LENGTH(resource_value) - 3 ) IN ('.png',  '.tif', 'tiff') "
                    + " ORDER BY last_visit_time  DESC  ";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, ResourceType.File);
                statement.setInt(2, FileType.Image);
                records = read(conn, statement, count);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return records;
    }

    public List<VisitHistory> readNoAlphaImages(int count) {
        List<VisitHistory> records = new ArrayList<>();
        try (Connection conn = DerbyBase.getConnection()) {
            final String sql = " SELECT   * FROM visit_history "
                    + " WHERE resource_type=? AND file_type=? "
                    + " AND SUBSTR(LOWER(resource_value), LENGTH(resource_value) - 3 ) IN ('.jpg', '.bmp', '.gif', '.pnm', 'wbmp') "
                    + " ORDER BY last_visit_time  DESC  ";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, ResourceType.File);
                statement.setInt(2, FileType.Image);
                records = read(conn, statement, count);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return records;
    }

    public boolean update(int resourceType, int fileType, int operationType, String value) {
        return update(resourceType, fileType, operationType, value, null);
    }

    public boolean update(int resourceType, int fileType, int operationType, String value, String more) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return update(conn, resourceType, fileType, operationType, value, more);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean update(Connection conn, int resourceType, int fileType, int operationType, String value) {
        return update(conn, resourceType, fileType, operationType, value, null);
    }

    public boolean update(Connection conn, int resourceType, int fileType, int operationType, String value, String more) {
        if (resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        if (!VisitHistoryTools.validFile(value, resourceType, fileType)) {
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
        if (fileType == FileType.DataFile) {
            String v = value.toLowerCase();
            if (v.endsWith(".csv")) {
                finalType = FileType.CSV;
            } else if (v.endsWith(".xlsx") || v.endsWith(".xls")) {
                finalType = FileType.Excel;
            } else {
                finalType = FileType.Text;
            }
        }
        try {
            VisitHistory exist = read(conn, resourceType, finalType, operationType, value);
            String d = DateTools.datetimeToString(new Date());
            if (exist != null) {
                if (more == null) {
                    try (PreparedStatement statement = conn.prepareStatement(Update_Visit)) {
                        statement.setInt(1, exist.getVisitCount() + 1);
                        statement.setString(2, d);
                        statement.setInt(3, resourceType);
                        statement.setInt(4, finalType);
                        statement.setInt(5, operationType);
                        statement.setString(6, value);
                        return statement.executeUpdate() >= 0;
                    }
                } else {
                    try (PreparedStatement statement = conn.prepareStatement(Update_More)) {
                        statement.setInt(1, exist.getVisitCount() + 1);
                        statement.setString(2, more);
                        statement.setString(3, d);
                        statement.setInt(4, resourceType);
                        statement.setInt(5, finalType);
                        statement.setInt(6, operationType);
                        statement.setString(7, value);
                        return statement.executeUpdate() >= 0;
                    }
                }
            } else {
                int ret;
                if (more == null) {
                    try (PreparedStatement statement = conn.prepareStatement(Insert_Visit)) {
                        statement.setInt(1, resourceType);
                        statement.setInt(2, finalType);
                        statement.setInt(3, operationType);
                        statement.setString(4, value);
                        statement.setString(5, d);
                        statement.setInt(6, 1);
                        ret = statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = conn.prepareStatement(Insert_More)) {
                        statement.setInt(1, resourceType);
                        statement.setInt(2, finalType);
                        statement.setInt(3, operationType);
                        statement.setString(4, value);
                        statement.setString(5, more);
                        statement.setString(6, d);
                        statement.setInt(7, 1);
                        ret = statement.executeUpdate();
                    }
                }
                trim(conn, resourceType, fileType, operationType);
                return ret >= 0;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean updateMenu(int fileType, String name, String fxml) {
        return updateMenu(fileType, OperationType.Access, name, fxml);
    }

    public boolean updateMenu(int fileType, int operationType, String name, String fxml) {
        if (fileType < 0 || operationType < 0 || name == null || fxml == null) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            VisitHistory exist = null;
            try (PreparedStatement statement = conn.prepareStatement(Query_More)) {
                statement.setInt(1, ResourceType.Menu);
                statement.setInt(2, fileType);
                statement.setInt(3, operationType);
                statement.setString(4, name);
                statement.setString(5, fxml);
                conn.setAutoCommit(true);
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    exist = readData(results);
                }
            }
            Date d = new Date();
            if (exist != null) {
                try (PreparedStatement statement = conn.prepareStatement(Update_Visit_More)) {
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
                try (PreparedStatement statement = conn.prepareStatement(Insert_More)) {
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
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean delete(Connection conn, int resourceType, int fileType, int operationType, String value) {
        if (conn == null || resourceType < 0 || fileType < 0 || operationType < 0 || value == null) {
            return false;
        }
        try (PreparedStatement statement = conn.prepareStatement(Delete_Visit)) {
            statement.setInt(1, resourceType);
            statement.setInt(2, fileType);
            statement.setInt(3, operationType);
            statement.setString(4, value);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean delete(Connection conn, VisitHistory v) {
        return delete(conn, v.getResourceType(), v.getFileType(), v.getOperationType(), v.getResourceValue());
    }

    public boolean clear(int resourceType, int fileType, int operationType) {
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(Clear_Visit)) {
            statement.setInt(1, resourceType);
            statement.setInt(2, fileType);
            statement.setInt(3, operationType);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public int clear() {
        final String sql = "DELETE FROM visit_history";
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return -1;
        }
    }

    public void trim(Connection conn, int resourceType, int fileType, int operationType) {
        try (PreparedStatement query = conn.prepareStatement(Query_Types);
                PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
            conn.setAutoCommit(true);
            query.setInt(1, resourceType);
            query.setInt(2, fileType);
            query.setInt(3, operationType);
            int qcount = 0, dcount = 0;
            try (ResultSet results = query.executeQuery()) {
                conn.setAutoCommit(false);
                while (results.next()) {
                    VisitHistory data = readData(results);
                    if (++qcount > AppVariables.fileRecentNumber || !valid(data)) {
                        if (setDeleteStatement(conn, delete, data)) {
                            delete.addBatch();
                            if (dcount > 0 && (dcount % Database.BatchSize == 0)) {
                                int[] res = delete.executeBatch();
                                for (int r : res) {
                                    if (r > 0) {
                                        dcount += r;
                                    }
                                }
                                conn.commit();
                                delete.clearBatch();
                            }
                        }
                    }
                }
                delete.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

}
