package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.TextClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TableTextClipboard extends BaseTable<TextClipboard> {

    public TableTextClipboard() {
        tableName = "Text_Clipboard";
        defineColumns();
    }

    public TableTextClipboard(boolean defineColumns) {
        tableName = "Text_Clipboard";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTextClipboard defineColumns() {
        addColumn(new ColumnDefinition("tcid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("text", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("create_time", ColumnType.Datetime));
        orderColumns = "create_time DESC";
        return this;
    }

    public static final String QueryText
            = "SELECT * FROM Text_Clipboard WHERE text=?";

    @Override
    public boolean setValue(TextClipboard data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(TextClipboard data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(TextClipboard data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    public TextClipboard save(Connection conn, String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            Connection conn1 = conn;
            if (conn1 == null || conn1.isClosed()) {
                conn1 = DerbyBase.getConnection();
            }
            if (UserConfig.getBoolean("TextClipboardNoDuplication", true)) {
                TextClipboard exist = null;
                try (PreparedStatement statement = conn1.prepareStatement(QueryText)) {
                    statement.setString(1, text);
                    statement.setMaxRows(1);
                    conn1.setAutoCommit(true);
                    try (ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            exist = readData(results);
                            exist.setCreateTime(new Date());
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                if (exist != null) {
                    return updateData(conn1, exist);
                }
            }
            return insertData(conn1, new TextClipboard(text));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

}
