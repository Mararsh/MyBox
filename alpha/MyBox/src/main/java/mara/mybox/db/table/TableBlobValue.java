package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.BlobValue;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-1-17
 * @License Apache License Version 2.0
 */
public class TableBlobValue extends BaseTable<BlobValue> {

    public TableBlobValue() {
        tableName = "Blob_Value";
        defineColumns();
    }

    public TableBlobValue(boolean defineColumns) {
        tableName = "Blob_Value";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableBlobValue defineColumns() {
        addColumn(new ColumnDefinition("key_name", ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("blob_value", ColumnType.Blob));
        return this;
    }

    public static final String Query
            = "SELECT * FROM Blob_Value WHERE key_name=?";

    public BlobValue read(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return read(conn, key);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public BlobValue read(Connection conn, String key) {
        if (conn == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Query)) {
            statement.setString(1, key);
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    BlobValue data = readData(results);
                    return data;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

}
