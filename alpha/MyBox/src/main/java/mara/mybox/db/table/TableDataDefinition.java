package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-1-23
 * @License Apache License Version 2.0
 */
public class TableDataDefinition extends BaseTable<DataDefinition> {

    public TableDataDefinition() {
        tableName = "Data_Definition";
        defineColumns();
    }

    public TableDataDefinition(boolean defineColumns) {
        tableName = "Data_Definition";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableDataDefinition defineColumns() {
        addColumn(new ColumnDefinition("dfid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("data_type", ColumnType.Short, true));
        addColumn(new ColumnDefinition("data_name", ColumnType.String, true).setLength(4096));
        addColumn(new ColumnDefinition("file", ColumnType.String).setLength(32672));
        addColumn(new ColumnDefinition("charset", ColumnType.String).setLength(32));
        addColumn(new ColumnDefinition("delimiter", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("has_header", ColumnType.Boolean));
        addColumn(new ColumnDefinition("columns_number", ColumnType.Long));
        addColumn(new ColumnDefinition("rows_number", ColumnType.Long));
        addColumn(new ColumnDefinition("scale", ColumnType.Short));
        addColumn(new ColumnDefinition("max_random", ColumnType.Integer));
        addColumn(new ColumnDefinition("modify_time", ColumnType.Datetime));
        addColumn(new ColumnDefinition("comments", ColumnType.String).setLength(32672));
        return this;
    }

    public static final String Query_TypeFile
            = "SELECT * FROM Data_Definition WHERE data_type=? AND file=?";

    public static final String Query_TypeName
            = "SELECT * FROM Data_Definition WHERE data_type=? AND data_name=?";

    public static final String Query_TypeFileName
            = "SELECT * FROM Data_Definition WHERE data_type=? AND file=? AND data_name=?";

    public static final String Query_Type
            = "SELECT * FROM Data_Definition WHERE data_type=?";

    public static final String DeleteID
            = "DELETE FROM Data_Definition WHERE dfid=?";

    public static final String Delete_TypeFile
            = "DELETE FROM Data_Definition WHERE data_type=? AND file=?";

    public static final String Delete_TypeName
            = "DELETE FROM Data_Definition WHERE data_type=? AND data_name=?";

    public static final String Delete_TypeFileName
            = "DELETE FROM Data_Definition WHERE data_type=? AND file=? AND data_name=?";

    /*
        local methods
     */
    public DataDefinition queryFile(File file) {
        if (file == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return queryFile(conn, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataDefinition queryFile(Connection conn, File file) {
        if (conn == null || file == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Query_TypeFile)) {
            statement.setShort(1, DataDefinition.dataType(DataType.DataFile));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public int deleteFile(Connection conn, File file) {
        if (conn == null || file == null) {
            return -1;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Delete_TypeFile)) {
            statement.setShort(1, DataDefinition.dataType(DataType.DataFile));
            statement.setString(2, DerbyBase.stringValue(file.getAbsolutePath()));
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public DataDefinition queryName(DataType type, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return queryName(conn, type, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataDefinition queryName(Connection conn, DataType type, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Query_TypeName)) {
            statement.setShort(1, DataDefinition.dataType(type));
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
        try ( PreparedStatement statement = conn.prepareStatement(Delete_TypeName)) {
            statement.setShort(1, DataDefinition.dataType(type));
            statement.setString(2, DerbyBase.stringValue(name));
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

}
