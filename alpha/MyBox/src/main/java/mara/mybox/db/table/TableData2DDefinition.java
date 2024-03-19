package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DDefinition.DataType;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.WindowTools.recordError;
import static mara.mybox.fxml.WindowTools.recordInfo;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-2
 * @License Apache License Version 2.0
 */
public class TableData2DDefinition extends BaseTable<Data2DDefinition> {

    public TableData2DDefinition() {
        tableName = "Data2D_Definition";
        defineColumns();
    }

    public TableData2DDefinition(boolean defineColumns) {
        tableName = "Data2D_Definition";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableData2DDefinition defineColumns() {
        addColumn(new ColumnDefinition("d2did", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("data_type", ColumnType.Short, true));
        addColumn(new ColumnDefinition("data_name", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("file", ColumnType.File).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("sheet", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("charset", ColumnType.String).setLength(32));
        addColumn(new ColumnDefinition("delimiter", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("has_header", ColumnType.Boolean));
        addColumn(new ColumnDefinition("columns_number", ColumnType.Long));
        addColumn(new ColumnDefinition("rows_number", ColumnType.Long));
        addColumn(new ColumnDefinition("scale", ColumnType.Short));
        addColumn(new ColumnDefinition("max_random", ColumnType.Integer));
        addColumn(new ColumnDefinition("modify_time", ColumnType.Datetime));
        addColumn(new ColumnDefinition("comments", ColumnType.String).setLength(StringMaxLength));
        orderColumns = "modify_time DESC";
        return this;
    }

    public static final String QueryID
            = "SELECT * FROM Data2D_Definition WHERE d2did=?";

    public static final String Query_TypeFile
            = "SELECT * FROM Data2D_Definition WHERE data_type=? AND file=? ORDER BY modify_time DESC";

    public static final String Query_TypeName
            = "SELECT * FROM Data2D_Definition WHERE data_type=? AND data_name=? ORDER BY modify_time DESC";

    public static final String Query_TypeFileSheet
            = "SELECT * FROM Data2D_Definition WHERE data_type=? AND file=? AND sheet=? ORDER BY modify_time DESC";

    public static final String Query_Table
            = "SELECT * FROM Data2D_Definition WHERE data_type=? AND sheet=? ORDER BY modify_time DESC";

    public static final String Query_UserTable
            = "SELECT * FROM Data2D_Definition WHERE data_type=5 AND sheet=? ORDER BY modify_time DESC";

    public static final String Query_Type
            = "SELECT * FROM Data2D_Definition WHERE data_type=? ORDER BY modify_time DESC";

    public static final String DeleteID
            = "DELETE FROM Data2D_Definition WHERE d2did=?";

    public static final String Delete_TypeFile
            = "DELETE FROM Data2D_Definition WHERE data_type=? AND file=?";

    public static final String Delete_TypeName
            = "DELETE FROM Data2D_Definition WHERE data_type=? AND data_name=?";

    public static final String Delete_InvalidExcelSheet
            = "DELETE FROM Data2D_Definition WHERE data_type=2 AND sheet IS NULL";

    public static final String Delete_UserTable
            = "DELETE FROM Data2D_Definition WHERE data_type=5 AND sheet=?";


    /*
        local methods
     */
    public Data2DDefinition queryID(Connection conn, long d2did) {
        if (conn == null || d2did < 0) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(QueryID)) {
            statement.setLong(1, d2did);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DDefinition queryFile(DataType type, File file) {
        if (file == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection();) {
            return queryFile(conn, type, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DDefinition queryFile(Connection conn, DataType type, File file) {
        if (conn == null || file == null) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_TypeFile)) {
            statement.setShort(1, Data2DDefinition.type(type));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public int deleteFile(Connection conn, DataType type, File file) {
        if (conn == null || file == null) {
            return -1;
        }
        try (PreparedStatement statement = conn.prepareStatement(Delete_TypeFile)) {
            statement.setShort(1, Data2DDefinition.type(type));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public Data2DDefinition queryName(DataType type, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection();) {
            return queryName(conn, type, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DDefinition queryName(Connection conn, DataType type, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_TypeName)) {
            statement.setShort(1, Data2DDefinition.type(type));
            statement.setString(2, DerbyBase.stringValue(name));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public int deleteName(Connection conn, DataType type, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return -1;
        }
        try (PreparedStatement statement = conn.prepareStatement(Delete_TypeName)) {
            statement.setShort(1, Data2DDefinition.type(type));
            statement.setString(2, DerbyBase.stringValue(name));
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public Data2DDefinition queryFileSheet(Connection conn, DataType type, File file, String sheet) {
        if (conn == null || file == null) {
            return null;
        }
        if (sheet == null || sheet.isBlank()) {
            return queryFile(conn, type, file);
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_TypeFileSheet)) {
            statement.setShort(1, Data2DDefinition.type(type));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            statement.setString(3, DerbyBase.stringValue(sheet));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DDefinition queryTable(Connection conn, String tname, DataType type) {
        if (conn == null || tname == null) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_Table)) {
            statement.setShort(1, Data2DDefinition.type(type));
            statement.setString(2, DerbyBase.stringValue(tname));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DDefinition queryUserTable(Connection conn, String tname) {
        if (conn == null || tname == null) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_UserTable)) {
            statement.setString(1, DerbyBase.stringValue(tname));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public int deleteUserTable(Connection conn, String tname) {
        if (conn == null || tname == null) {
            return -1;
        }
        String fixedName = DerbyBase.fixedIdentifier(tname);
        try (PreparedStatement statement = conn.prepareStatement("DROP TABLE " + fixedName)) {
            if (statement.executeUpdate() < 0) {
                return -2;
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return -3;
        }
        try (PreparedStatement statement = conn.prepareStatement(Delete_UserTable)) {
            statement.setString(1, DerbyBase.stringValue(DerbyBase.savedName(fixedName)));
            return statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return -1;
        }
    }

    public Data2DDefinition queryClipboard(Connection conn, File file) {
        if (conn == null || file == null) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_TypeFile)) {
            statement.setShort(1, Data2DDefinition.type(DataType.MyBoxClipboard));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DDefinition writeTable(Connection conn, DataTable table) {
        if (conn == null || table == null) {
            return null;
        }
        MyBoxLog.console(table.getSheet() + "  " + table.getType());
        if (queryTable(conn, table.getSheet(), table.getType()) != null) {
            return updateData(conn, table);
        } else {
            return insertData(conn, table);
        }
    }

    public int deleteClipboard(Connection conn, File file) {
        if (conn == null || file == null) {
            return -1;
        }
        try (PreparedStatement statement = conn.prepareStatement(Delete_TypeFile)) {
            statement.setShort(1, Data2DDefinition.type(DataType.MyBoxClipboard));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int clearInvalid(BaseTaskController taskController, Connection conn, boolean clearTmpTables) {
        int invalidCount = 0;
        try {
            recordInfo(taskController, message("Check") + ": " + tableName);

            invalidCount = clearInvalidExcelSheet(taskController, conn);

            if (taskController != null && taskController.getTask() != null
                    && taskController.getTask().isCancelled()) {
                return invalidCount;
            }

            invalidCount += clearInvalidTable(taskController, conn);

            if (taskController != null && taskController.getTask() != null
                    && taskController.getTask().isCancelled()) {
                return invalidCount;
            }

            if (clearTmpTables) {
                invalidCount += clearTmpTables(taskController, conn);
            }
        } catch (Exception e) {
            recordError(taskController, e.toString() + "\n" + tableName);
        }
        return invalidCount;
    }

    public int clearInvalidExcelSheet(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            recordInfo(taskController, Delete_InvalidExcelSheet);
            update(conn, Delete_InvalidExcelSheet);
            String sql = "SELECT * FROM Data2D_Definition WHERE data_type < 4";
            recordInfo(taskController, sql);
            try (PreparedStatement query = conn.prepareStatement(sql);
                    PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
                conn.setAutoCommit(true);
                try (ResultSet results = query.executeQuery()) {
                    conn.setAutoCommit(false);
                    while (results.next()) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        Data2DDefinition data = readData(results);
                        File file = data.getFile();
                        if (file == null || !file.exists() || !file.isFile()) {
                            if (setDeleteStatement(conn, delete, data)) {
                                delete.addBatch();
                                if (invalidCount > 0 && (invalidCount % Database.BatchSize == 0)) {
                                    int[] res = delete.executeBatch();
                                    for (int r : res) {
                                        if (r > 0) {
                                            invalidCount += r;
                                        }
                                    }
                                    conn.commit();
                                    delete.clearBatch();
                                }
                            }
                            if (file != null) {
                                recordInfo(taskController, message("NotFound") + ": " + file.getAbsolutePath());
                            }
                        }
                    }
                } catch (Exception e) {
                    recordError(taskController, e.toString() + "\n" + tableName);
                }
                int[] res = delete.executeBatch();
                for (int r : res) {
                    if (r > 0) {
                        invalidCount += r;
                    }
                }
                conn.commit();
            } catch (Exception e) {
                recordError(taskController, e.toString() + "\n" + tableName);
            }
            recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                    + message("Expired") + ": " + invalidCount);

        } catch (Exception e) {
            recordError(taskController, e.toString() + "\n" + tableName);
        }
        return invalidCount;
    }

    public int clearInvalidTable(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            String sql = "SELECT * FROM Data2D_Definition WHERE data_type ="
                    + Data2D.type(Data2DDefinition.DataType.DatabaseTable);
            recordInfo(taskController, sql);
            conn.setAutoCommit(true);
            try (ResultSet results = conn.prepareStatement(sql).executeQuery();
                    PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
                while (results.next()) {
                    rowCount++;
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    Data2DDefinition data = readData(results);
                    String tname = data.getSheet();
                    if (DerbyBase.exist(conn, tname) == 0) {
                        if (setDeleteStatement(conn, delete, data)) {
                            delete.addBatch();
                            if (invalidCount > 0 && (invalidCount % Database.BatchSize == 0)) {
                                int[] res = delete.executeBatch();
                                for (int r : res) {
                                    if (r > 0) {
                                        invalidCount += r;
                                    }
                                }
                                conn.commit();
                                delete.clearBatch();
                            }
                        }
                        recordInfo(taskController, message("NotFound") + ": " + tname);
                    }
                }
                int[] res = delete.executeBatch();
                for (int r : res) {
                    if (r > 0) {
                        invalidCount += r;
                    }
                }
            } catch (Exception e) {
                recordError(taskController, e.toString() + "\n" + tableName);
            }
            recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                    + message("Expired") + ": " + invalidCount);
        } catch (Exception e) {
            recordError(taskController, e.toString() + "\n" + tableName);
        }
        return invalidCount;
    }

    public int clearTmpTables(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            conn.setAutoCommit(true);
            String sql = "SELECT * FROM Data2D_Definition WHERE data_type="
                    + Data2D.type(Data2DDefinition.DataType.DatabaseTable)
                    + " AND ( sheet like '" + TmpTable.TmpTablePrefix + "%'"
                    + " OR sheet like '" + TmpTable.TmpTablePrefix.toLowerCase() + "%' )";
            recordInfo(taskController, sql);
            try (ResultSet results = conn.prepareStatement(sql).executeQuery()) {
                while (results.next()) {
                    rowCount++;
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    Data2DDefinition data = readData(results);
                    String tname = data.getSheet();
                    deleteUserTable(conn, tname);
                    recordInfo(taskController, message("Delete") + ": " + tname);
                }
            } catch (Exception e) {
                recordError(taskController, e.toString() + "\n" + tableName);
            }
            conn.commit();
            recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                    + message("Expired") + ": " + invalidCount);
        } catch (Exception e) {
            recordError(taskController, e.toString() + "\n" + tableName);
        }
        return invalidCount;
    }

}
