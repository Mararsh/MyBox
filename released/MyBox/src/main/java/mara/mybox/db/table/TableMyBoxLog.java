package mara.mybox.db.table;

import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-11-25
 * @License Apache License Version 2.0
 */
public class TableMyBoxLog extends BaseTable<MyBoxLog> {

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

}
