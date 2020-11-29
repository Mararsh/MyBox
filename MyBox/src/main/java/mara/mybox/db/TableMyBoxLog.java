package mara.mybox.db;

import mara.mybox.db.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-11-25
 * @License Apache License Version 2.0
 */
public class TableMyBoxLog extends TableBase<MyBoxLog> {

    public TableMyBoxLog() {
        tableName = "MyBox_Log";
        defineColumns();
    }

    public TableMyBoxLog(boolean defineColumns) {
        tableName = "MyBox_Log";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableMyBoxLog defineColumns() {
        addColumn(new ColumnDefinition("mblid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("log_type", ColumnType.Short, true));
        addColumn(new ColumnDefinition("log", ColumnType.String, true).setLength(2048));
        addColumn(new ColumnDefinition("file_name", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("class_name", ColumnType.String).setLength(512));
        addColumn(new ColumnDefinition("method_name", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("line", ColumnType.Integer));
        addColumn(new ColumnDefinition("callers", ColumnType.String).setLength(10240));
        addColumn(new ColumnDefinition("comments", ColumnType.String).setLength(4096));
        return this;
    }

    public static final String Create_Index
            = "CREATE INDEX MyBox_Log_index on MyBox_Log (time, log_type)";

    public static final String AllQuery
            = " SELECT * FROM MyBox_Log  ORDER BY time DESC  ";

    public static final String TypeQuery
            = " SELECT * FROM MyBox_Log  WHERE log_type=? ORDER BY time DESC  ";

    @Override
    public MyBoxLog newData() {
        return new MyBoxLog();
    }

    @Override
    public boolean setValue(MyBoxLog data, String column, Object value) {
        if (data == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(MyBoxLog data, String column) {
        if (data == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public void setId(MyBoxLog source, MyBoxLog target) {
        try {
            if (source == null || target == null) {
                return;
            }
            target.setMblid(source.getMblid());
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public boolean valid(MyBoxLog data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

}
