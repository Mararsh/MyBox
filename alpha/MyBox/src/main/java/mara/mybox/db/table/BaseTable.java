package mara.mybox.db.table;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.data.Era;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.BatchSize;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.BaseDataAdaptor;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @param <D> Should be extened from "BaseData"
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public abstract class BaseTable<D> {

    public final static int FilenameMaxLength = 32672;
    public final static int StringMaxLength = 32672;

    protected String tableName, idColumn, orderColumns;
    protected List<ColumnDefinition> columns, primaryColumns, foreignColumns, referredColumns;
    protected Era.Format timeFormat;
    protected boolean supportBatchUpdate;
    protected long newID = -1;

    /*
        methods need implemented
     */
    public Object readForeignValue(ResultSet results, String column) {
        return null;
    }

    public boolean setForeignValue(D data, String column, Object value) {
        return true;
    }

    public D readData(Connection conn, D data) {
        if (conn == null || data == null) {
            return null;
        }
        String sql = queryStatement();
        if (sql == null || sql.isBlank()) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            if (setColumnsValues(statement, primaryColumns, data, 1) < 0) {
                return null;
            }
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return null;
        }
    }

    public D readData(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            D data = newData();
            for (int i = 0; i < columns.size(); ++i) {
                ColumnDefinition column = columns.get(i);
                Object value = readColumnValue(results, column);
                setValue(data, column.getColumnName(), value);
            }
            for (int i = 0; i < foreignColumns.size(); ++i) {
                ColumnDefinition column = foreignColumns.get(i);
                String name = column.getColumnName();
                Object value = readForeignValue(results, name);
                if (!setForeignValue(data, name, value)) {
                    return null;
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return null;
    }

    public Object readColumnValue(ResultSet results, ColumnDefinition column) {
        if (results == null || column == null) {
            return null;
        }
        return column.value(results);
    }

    public boolean setColumnValue(PreparedStatement statement, ColumnDefinition column, D data, int index) {
        if (statement == null || data == null || column == null || index < 0) {
            return false;
        }
        try {
            Object value = getValue(data, column.getColumnName());
            // Not check maxValue/minValue.
            boolean notNull = column.isNotNull();
            switch (column.getType()) {
                case String:
                case Text:
                case Color:
                case File:
                case Image:
                    if (value == null) {
                        if (notNull) {
                            statement.setString(index, (String) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.VARCHAR);
                        }
                    } else {
                        String s = (String) value;
                        if (column.getLength() > 0 && s.length() > column.getLength()) {
                            s = s.substring(0, column.getLength());
                        }
                        statement.setString(index, s);
                    }
                    break;
                case Double:
                    double d;
                    if (value == null) {
                        if (notNull) {
                            statement.setDouble(index, (double) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.DOUBLE);
                        }
                    } else {
                        d = (double) value;
                        statement.setDouble(index, d);
                    }
                    break;
                case Float:
                    float f;
                    if (value == null) {
                        if (notNull) {
                            statement.setFloat(index, (float) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.FLOAT);
                        }
                    } else {
                        f = (float) value;
                        statement.setFloat(index, f);
                    }
                    break;
                case Long:
                case Era:
                    long l;
                    if (value == null) {
                        if (column.isAuto()) {
                            statement.setLong(index, -1);
                        } else if (notNull) {
                            statement.setLong(index, (long) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.BIGINT);
                        }
                    } else {
                        l = (long) value;
                        statement.setLong(index, l);
                    }
                    break;
                case Integer:
                    int ii;
                    if (value == null) {
                        if (notNull) {
                            statement.setInt(index, (int) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.INTEGER);
                        }
                    } else {
                        ii = (int) value;
                        statement.setInt(index, ii);
                    }
                    break;
                case Boolean:
                    boolean b;
                    if (value == null) {
                        if (notNull) {
                            statement.setBoolean(index, false);
                        } else {
                            statement.setNull(index, Types.BOOLEAN);
                        }
                    } else {
                        statement.setBoolean(index, (boolean) value);
                    }
                    break;
                case Short:
                    short s;
                    if (value == null) {
                        if (notNull) {
                            statement.setShort(index, (short) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.SMALLINT);
                        }
                    } else {
                        s = (short) value;
                        statement.setShort(index, s);
                    }
                    break;
                case Datetime:
                    if (value == null) {
                        if (notNull) {
                            statement.setTimestamp(index, (Timestamp) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.TIMESTAMP);
                        }
                    } else {
                        Date datetime = (Date) value;
                        statement.setTimestamp(index, new Timestamp(datetime.getTime()));
                    }
                    break;
                case Date:
                    if (value == null) {
                        if (notNull) {
                            statement.setDate(index, (java.sql.Date) column.defaultValue());
                        } else {
                            statement.setNull(index, Types.DATE);
                        }
                    } else {
                        Date date = (Date) value;
                        statement.setDate(index, new java.sql.Date(date.getTime()));
                    }
                    break;
//                case Clob:
//                    if (value == null) {
//                        statement.setNull(index, Types.CLOB);
//                    } else {
//                        statement.setClob(index, (Clob) value);
//                    }
//                    break;
//                case Blob:
//                    if (value == null) {
//                        statement.setNull(index, Types.BLOB);
//                    } else {
//                        statement.setBlob(index, (Blob) value);
//                    }
//                    break;
                default:
                    MyBoxLog.debug(column.getColumnName() + " " + column.getType() + " " + value.toString());
                    return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString(), tableName + " " + column.getColumnName());
            return false;
        }
    }

    public int setColumnsValues(PreparedStatement statement, List<ColumnDefinition> valueColumns, D data, int startIndex) {
        if (statement == null || data == null || startIndex < 0) {
            return -1;
        }
        try {
            int index = startIndex;
            for (int i = 0; i < valueColumns.size(); ++i) {
                ColumnDefinition column = valueColumns.get(i);
                if (!setColumnValue(statement, column, data, index++)) {
                    return -1;
                }
            }
            return index;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public int setColumnNamesValues(PreparedStatement statement, List<String> valueColumns, D data, int startIndex) {
        if (statement == null || data == null || startIndex < 0) {
            return -1;
        }
        try {
            int index = startIndex;
            for (int i = 0; i < valueColumns.size(); ++i) {
                ColumnDefinition column = column(valueColumns.get(i));
                if (!setColumnValue(statement, column, data, index++)) {
                    return -1;
                }
            }
            return index;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public boolean setInsertStatement(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || statement == null || data == null) {
            return false;
        }
        return setColumnsValues(statement, insertColumns(), data, 1) > 0;
    }

    public boolean setUpdateStatement(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || statement == null || !valid(data)) {
            return false;
        }
        try {
            int index = setColumnsValues(statement, updateColumns(), data, 1);
            if (index < 0) {
                return false;
            }
            return setColumnsValues(statement, primaryColumns, data, index) > 0;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return false;
        }
    }

    public boolean setDeleteStatement(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || statement == null || data == null) {
            return false;
        }
        return setColumnsValues(statement, primaryColumns, data, 1) > 0;
    }

    public List<String> allFields() {
        List<String> names = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            names.add(column.getLabel());
        }
        return names;
    }

    public List<String> importNecessaryFields() {
        List<String> names = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            if (column.isNotNull() && !column.isAuto()) {
                names.add(column.getLabel());
            }
        }
        return names;
    }

    public List<String> importAllFields() {
        return allFields();
    }

    public List<String> exportAllFields() {
        return allFields();
    }

    /*
        general methods which may need not change
     */
    private void init() {
        tableName = null;
        idColumn = null;
        orderColumns = null;
        columns = new ArrayList<>();
        primaryColumns = new ArrayList<>();
        foreignColumns = new ArrayList<>();
        referredColumns = new ArrayList<>();
        timeFormat = Era.Format.Datetime;
        supportBatchUpdate = false;
        newID = -1;
    }

    public BaseTable() {
        init();
    }

    public void reset() {
        init();
    }

    public BaseTable addColumn(ColumnDefinition column) {
        if (column != null) {
            column.setTableName(tableName);
            column.setIndex(columns.size() + 1);
            if (column.isIsPrimaryKey()) {
                primaryColumns.add(column);
                if (column.isAuto()) {
                    idColumn = column.getColumnName();
                }
            }
            if (column.getReferTable() != null && column.getReferColumn() != null) {
                foreignColumns.add(column);
            }
            columns.add(column);
        }
        return this;
    }

    public String createTableStatement() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String sql = "CREATE TABLE " + tableName + " ( \n";
        for (int i = 0; i < columns.size(); ++i) {
            if (i > 0) {
                sql += ", \n";
            }
            sql += createColumnDefiniton(columns.get(i));
        }
        if (!primaryColumns.isEmpty()) {
            sql += ", \n";
            sql += "PRIMARY KEY ( ";
            for (int i = 0; i < primaryColumns.size(); ++i) {
                if (i > 0) {
                    sql += ", ";
                }
                sql += primaryColumns.get(i).getColumnName();

            }
            sql += " ) ";
        }
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            String f = column.foreignText();
            if (f != null) {
                sql += ", \n" + f;
            }
        }
        sql += "\n)";
//        MyBoxLog.debug(sql);
        return sql;
    }

    public String createColumnDefiniton(ColumnDefinition column) {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String def = column.getColumnName() + " ";
        ColumnType type = column.getType();
        switch (type) {
            case String:
            case Text:
            case File:
            case Image:
                def += "VARCHAR(" + column.getLength() + ")";
                break;
            case Color:
                def += "VARCHAR(16)";
                break;
            case Double:
                def += "DOUBLE";
                break;
            case Float:
                def += "FLOAT";
                break;
            case Long:
            case Era:
                def += "BIGINT";
                break;
            case Integer:
                def += "INT";
                break;
            case Short:
                def += "SMALLINT";
                break;
            case Boolean:
                def += "BOOLEAN";
                break;
            case Datetime:
                def += "TIMESTAMP";
                break;
            case Date:
                def += "DATE";
                break;
            case Blob:
                def += "BLOB";
                break;
            case Clob:
                def += "CLOB";
                break;
            default:
                MyBoxLog.debug(column.getColumnName() + " " + type);
                return null;
        }
        if (column.isAuto()) {
            def += " NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)";

        } else if (column.isNotNull()) {
            def += " NOT NULL WITH DEFAULT " + column.getDefValue();
        } else if (column.getDefaultValue() != null) {
            def += " WITH DEFAULT " + column.getDefValue();
        }
        return def;
    }

    public boolean createTable(Connection conn) {
        if (conn == null) {
            return false;
        }
        String sql = null;
        try {
            sql = createTableStatement();
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean dropTable(Connection conn) {
        if (conn == null) {
            return false;
        }
        String sql = null;
        try {
            sql = "DROP TABLE " + tableName;
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean dropColumn(Connection conn, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return false;
        }
        String sql = null;
        try {
            sql = "ALTER TABLE " + tableName + " DROP COLUMN " + name;
            MyBoxLog.debug(sql);
            return conn.createStatement().executeUpdate(sql) >= 0;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean addColumn(Connection conn, ColumnDefinition column) {
        if (conn == null || column == null) {
            return false;
        }
        String sql = null;
        try {
            sql = "ALTER TABLE " + tableName + " ADD COLUMN  " + createColumnDefiniton(column);
            return conn.createStatement().executeUpdate(sql) >= 0;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean setValue(D data, String column, Object value) {
        if (column == null) {
            return false;
        }
        if (data instanceof BaseData) {
            return BaseDataAdaptor.setColumnValue((BaseData) data, column, value);
        }
        return false;
    }

    public Object getValue(D data, String column) {
        if (data == null || column == null) {
            return null;
        }
        if (data instanceof BaseData) {
            return BaseDataAdaptor.getColumnValue((BaseData) data, column);
        }
        return null;
    }

    public void setId(D source, D target) {
        if (source == null || target == null || idColumn == null) {
            return;
        }
        setValue(target, idColumn, getValue(source, idColumn));
    }

    // https://stackoverflow.com/questions/18555122/create-instance-of-generic-type-in-java-when-parameterized-type-passes-through-h?r=SearchResults
    // https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/Class.html#newInstance()
    public D newData() {
        try {
            Class<D> entityClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            D data = entityClass.getDeclaredConstructor().newInstance();
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean valid(D data) {
        if (data == null) {
            return false;
        }
        if (data instanceof BaseData) {
            return BaseDataAdaptor.valid((BaseData) data);
        }
        return false;
    }

    public String name() {
        return Languages.tableMessage(tableName.toLowerCase());
    }

    public List<String> columnLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            labels.add(column.getLabel());
        }
        return labels;
    }

    public List<String> columnNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            names.add(column.getColumnName());
        }
        return names;
    }

    public String sizeStatement() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String sql = "SELECT COUNT(" + columns.get(0).getColumnName() + ") FROM " + tableName;
        return sql;
    }

    public int size() {
        return conditionSize(null);
    }

    public int size(Connection conn) {
        return conditionSize(conn, null);
    }

    public int conditionSize(String condition) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return conditionSize(conn, condition);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return 0;
        }
    }

    public int conditionSize(Connection conn, String condition) {
        if (conn == null) {
            return 0;
        }
        String c = "";
        if (condition != null && !condition.isBlank()) {
            if (!condition.trim().startsWith("ORDER BY")) {
                c = " WHERE " + condition;
            }
        }
        String sql = sizeStatement() + c;
        int size = 0;
        try ( PreparedStatement sizeQuery = conn.prepareStatement(sql);
                 ResultSet results = sizeQuery.executeQuery()) {
            if (results != null && results.next()) {
                size = results.getInt(1);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return size;
    }

    public boolean isEmpty() {
        String sql = "SELECT * FROM " + tableName + " FETCH FIRST ROW ONLY";
        return isEmpty(sql);
    }

    public boolean isEmpty(String sql) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return isEmpty(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return true;
        }
    }

    public boolean isEmpty(Connection conn, String sql) {
        boolean isEmpty = true;
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            isEmpty = results == null || !results.next();
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return isEmpty;
    }

    public String queryStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String sql = null;
        for (ColumnDefinition column : primaryColumns) {
            if (sql == null) {
                sql = "SELECT * FROM " + tableName + " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += column.getColumnName() + "=? ";
        }
        return sql;
    }

    public List<ColumnDefinition> insertColumns() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        List<ColumnDefinition> columnsList = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            if (column.isAuto()) {
                continue;
            }
            columnsList.add(column);
        }
        return columnsList;
    }

    public String insertStatement() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String sql = null;
        String v = null;
        for (ColumnDefinition column : columns) {
            if (column.isAuto()) {
                continue;
            }
            if (sql == null) {
                sql = "INSERT INTO " + tableName + " ( ";
                v = "?";
            } else {
                sql += ", ";
                v += ", ?";
            }
            sql += column.getColumnName();
        }
        sql += " ) VALUES ( " + v + ") ";
        return sql;
    }

    public List<ColumnDefinition> updateColumns() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        List<ColumnDefinition> columnsList = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            if (column.isIsPrimaryKey() || column.isAuto()) {
                continue;
            }
            columnsList.add(column);
        }
        return columnsList;
    }

    public String updateStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String update = null;
        for (ColumnDefinition column : columns) {
            if (column.isIsPrimaryKey() || column.isAuto()) {
                continue;
            }
            if (update == null) {
                update = "UPDATE " + tableName + " SET ";
            } else {
                update += ", ";
            }
            update += column.getColumnName() + "=? ";
        }
        String where = null;
        for (ColumnDefinition column : columns) {
            if (!column.isIsPrimaryKey()) {
                continue;
            }
            if (where == null) {
                where = " WHERE ";
            } else {
                where += " AND ";
            }
            where += column.getColumnName() + "=?";
        }
        return update + (where != null ? where : "");
    }

    public String deleteStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String delete = "DELETE FROM " + tableName;
        String where = null;
        for (ColumnDefinition column : primaryColumns) {
            if (where == null) {
                where = " WHERE ";
            } else {
                where += " AND ";
            }
            where += column.getColumnName() + "=?";
        }
        return delete + (where != null ? where : "");
    }

    public int columnIndex(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            return -1;
        }
        for (int i = 0; i < columns.size() - 1; i++) {
            ColumnDefinition column = columns.get(i);
            if (columnName.equalsIgnoreCase(column.getColumnName())) {
                return i;
            }
        }
        return -1;
    }

    public ColumnDefinition columnByMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        for (ColumnDefinition column : columns) {
            if (column.getLabel().equals(message)) {
                return column;
            }
        }
        return null;
    }

    public ColumnDefinition column(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            return null;
        }
        for (ColumnDefinition column : columns) {
            if (column.getColumnName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }

    public String columnsList() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        for (ColumnDefinition column : columns) {
            s.append(column.getColumnName()).append("\t\t")
                    .append(column.getType().name()).append("\t\t")
                    .append(column.getLength() > 0 ? column.getLength() + "" : " ").append("\t\t")
                    .append(column.isNotNull() ? message("NotNull") : "")
                    .append("\n");
        }
        return s.toString();
    }

    public String columnsHtml() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Column"), message("Type"), message("Length"),
                message("NotNull"), message("PrimaryKey"), message("AutoGenerated"),
                message("ReferTable"), message("ReferColumn")));
        StringTable table = new StringTable(names, tableName);
        for (ColumnDefinition column : columns) {
            List<String> row = new ArrayList<>();
            row.add(column.getColumnName());
            row.add(column.getType().name());
            row.add(column.getLength() > 0 ? column.getLength() + "" : "");
            row.add(column.isNotNull() ? message("Yes") : "");
            row.add(column.isIsPrimaryKey() ? message("Yes") : "");
            row.add(column.isAuto() ? message("Yes") : "");
            row.add(column.getReferTable());
            row.add(column.getReferColumn());
            table.add(row);
        }
        return StringTable.tableDiv(table);
    }

    public String referredColumnsHtml() {
        if (tableName == null || referredColumns.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Column"), message("Type"), message("Length"),
                message("NotNull"), message("PrimaryKey"), message("AutoGenerated"),
                message("ReferredByTable"), message("ReferredByColumn")));
        StringTable table = new StringTable(names, message("Referred"));
        for (ColumnDefinition column : referredColumns) {
            List<String> row = new ArrayList<>();
            row.add(column.getColumnName());
            row.add(column.getType().name());
            row.add(column.getLength() > 0 ? column.getLength() + "" : "");
            row.add(column.isNotNull() ? message("Yes") : "");
            row.add(column.isIsPrimaryKey() ? message("Yes") : "");
            row.add(column.isAuto() ? message("Yes") : "");
            row.add(column.getReferTable());
            row.add(column.getReferColumn());
            table.add(row);
        }
        return StringTable.tableDiv(table);
    }

    public String html() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String html = columnsHtml();
        String referred = referredColumnsHtml();
        if (referred != null) {
            html += "</BR>" + referred;
        }
        html += "</BR><HR>" + createTableStatement().replaceAll("\n", "</BR>");
        return HtmlWriteTools.html(tableName, HtmlStyles.styleValue("Default"), html);
    }

    public D exist(D data) {
        return query(data);
    }

    public D exist(Connection conn, D data) {
        return readData(conn, data);
    }

    public D query(D data) {
        if (data == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return readData(conn, data);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return null;
        }
    }

    public D queryOne(String sql) {
        D data = null;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(sql)) {
            data = query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return data;
    }

    public D query(Connection conn, PreparedStatement statement) {
        if (conn == null || statement == null) {
            return null;
        }
        try {
            D data;
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    data = readData(results);
                } else {
                    return null;
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return null;
        }
    }

    public D query(Connection conn, PreparedStatement statement, String value) {
        if (conn == null || statement == null || value == null) {
            return null;
        }
        try {
            D data;
            statement.setMaxRows(1);
            statement.setString(1, value);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    data = readData(results);
                } else {
                    return null;
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName + " " + value);
            return null;
        }
    }

    public List<D> queryPreLike(Connection conn, PreparedStatement statement, String value) {
        List<D> dataList = new ArrayList<>();
        if (conn == null || statement == null || value == null) {
            return dataList;
        }
        try {
            statement.setMaxRows(1);
            statement.setString(1, "%" + value);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    D data = readData(results);
                    dataList.add(data);
                }
            }
            return dataList;
        } catch (Exception e) {
            MyBoxLog.error(e, tableName + " " + value);
        }
        return dataList;
    }

    public List<D> query(PreparedStatement statement) {
        List<D> dataList = new ArrayList<>();
        try ( ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                D data = readData(results);
                if (data != null) {
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return dataList;
    }

    public List<D> query(String sql) {
        return query(sql, -1);
    }

    public List<D> query(String sql, int max) {
        List<D> dataList = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection()) {
            dataList = query(conn, sql, max);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return dataList;
    }

    public List<D> query(Connection conn, String sql) {
        return query(conn, sql, -1);
    }

    public List<D> query(Connection conn, String sql, int max) {
        List<D> dataList = new ArrayList<>();
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            if (max > 0) {
                statement.setMaxRows(max);
            }
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    D data = readData(results);
                    dataList.add(data);
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return dataList;
    }

    public String queryAllStatement() {
        return "SELECT * FROM " + tableName
                + (orderColumns != null ? " ORDER BY " + orderColumns : "");
    }

    public List<D> query(long start, long size) {
        if (start < 0 || size <= 0) {
            return new ArrayList<>();
        }
        String sql = queryAllStatement()
                + " OFFSET " + start + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        return readData(sql);
    }

    public List<D> queryConditions(String condition, String orderby, long start, long size) {
        List<D> dataList = new ArrayList<>();
        if (start < 0 || size <= 0) {
            return dataList;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return queryConditions(conn, condition, orderby, start, size);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return dataList;
        }
    }

    public List<D> queryConditions(Connection conn, String condition, String orderby, long start, long size) {
        if (conn == null || start < 0 || size <= 0) {
            return new ArrayList<>();
        }
        String c = "";
        if (condition != null && !condition.isBlank()) {
            if (condition.trim().startsWith("ORDER BY")) {
                c = condition;
            } else {
                c = " WHERE " + condition;
            }
        }
        if (orderby != null && !orderby.isBlank()) {
            c += " ORDER BY " + orderby;
        }
        if (orderColumns != null && (c.isBlank() || !c.contains("ORDER BY"))) {
            c += " ORDER BY " + orderColumns;
        }
        String sql = "SELECT * FROM " + tableName + c
                + " OFFSET " + start + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        return query(conn, sql);
    }

    public List<D> query() {
        return readAll();
    }

    public List<D> readData(String sql) {
        return query(sql);
    }

    public List<D> readData(String sql, int max) {
        return query(sql, max);
    }

    public List<D> readAll() {
        return query(queryAllStatement());
    }

    public List<D> readAll(Connection conn) {
        return query(conn, queryAllStatement());
    }

    public D writeData(D data) {
        newID = -1;
        if (!valid(data)) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return writeData(conn, data);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return null;
        }
    }

    public D writeData(Connection conn, D data) {
        newID = -1;
        if (!valid(data) || conn == null) {
            return null;
        }
        try {
            D exist = exist(conn, data);
            if (exist != null) {
                if (idColumn != null) {
                    setId(exist, data);
                }
                return updateData(conn, data);
            } else {
                return insertData(conn, data);
            }
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return null;
        }
    }

    public D insertData(D data) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return insertData(conn, data);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return null;
        }
    }

    public D insertData(Connection conn, D data) {
        newID = -1;
        if (conn == null || !valid(data)) {
            return null;
        }
        String sql = insertStatement();
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            return insertData(conn, statement, data);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return null;
    }

    public D insertData(Connection conn, PreparedStatement statement, D data) {
        newID = -1;
        if (conn == null || !valid(data)) {
            return null;
        }
        try {
            if (setInsertStatement(conn, statement, data)) {
                if (statement.executeUpdate() > 0) {
                    if (idColumn != null) {
                        boolean ac = conn.getAutoCommit();
                        conn.setAutoCommit(false);
                        try ( Statement query = conn.createStatement();
                                 ResultSet resultSet = query.executeQuery("VALUES IDENTITY_VAL_LOCAL()")) {
                            if (resultSet.next()) {
                                newID = resultSet.getLong(1);
                                setValue(data, idColumn, newID);
                            }
                        } catch (Exception e) {
                            MyBoxLog.error(e, tableName);
                        }
                        conn.setAutoCommit(ac);
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return null;
    }

    public int insertList(List<D> dataList) {
        if (dataList == null) {
            return -1;
        }
        int count = -1;
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            count = insertList(conn, dataList);
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return count;
    }

    public int insertList(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null) {
            return -1;
        }
        String sql = insertStatement();
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            for (D data : dataList) {
                if (setInsertStatement(conn, statement, data)) {
                    count += statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return count;
    }

    public D updateData(D data) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return updateData(conn, data);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return null;
        }
    }

    public D updateData(Connection conn, D data) {
        if (conn == null || !valid(data)) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(updateStatement())) {
            return updateData(conn, statement, data);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            MyBoxLog.console(updateStatement());
            return null;
        }
    }

    public D updateData(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || !valid(data)) {
            MyBoxLog.console(tableName);
            return null;
        }
        try {
            if (!setUpdateStatement(conn, statement, data)) {
                MyBoxLog.console(tableName);
                return null;
            }
            int ret = statement.executeUpdate();
            if (ret > 0) {
                return data;
            }
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return null;
    }

    public int updateList(List<D> dataList) {
        if (dataList == null) {
            return -1;
        }
        int count = -1;
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            count = updateList(conn, dataList);
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return count;
    }

    public int updateList(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null) {
            return -1;
        }
        String sql = updateStatement();
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            for (D data : dataList) {
                if (setUpdateStatement(conn, statement, data)) {
                    count += statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return count;
    }

    public int update(Connection conn, PreparedStatement statement, String value) {
        if (conn == null || statement == null || value == null) {
            return -1;
        }
        try {
            statement.setString(1, value);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e, tableName + " " + value);
            return -1;
        }
    }

    public int updatePreLike(Connection conn, PreparedStatement statement, String value) {
        if (conn == null || statement == null || value == null) {
            return -1;
        }
        try {
            statement.setString(1, "%" + value);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e, tableName + " " + value);
            return -1;
        }
    }

    public int update(Connection conn, String sql) {
        if (conn == null || sql == null) {
            return -1;
        }
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return -1;
        }
    }

    public int deleteData(D data) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return deleteData(conn, data);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public int deleteData(Connection conn, D data) {
        try ( PreparedStatement statement = conn.prepareStatement(deleteStatement())) {
            setDeleteStatement(conn, statement, data);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public int deleteData(List<D> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return deleteData(conn, dataList);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public int deleteData(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null || dataList.isEmpty()) {
            return 0;
        }
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(deleteStatement())) {
            conn.setAutoCommit(false);
            for (int i = 0; i < dataList.size(); ++i) {
                D data = dataList.get(i);
                if (!setDeleteStatement(conn, statement, data)) {
                    continue;
                }
                statement.addBatch();
                if (i > 0 && (i % BatchSize == 0)) {
                    int[] res = statement.executeBatch();
                    for (int r : res) {
                        if (r > 0) {
                            count += r;
                        }
                    }
                    conn.commit();
                    statement.clearBatch();
                }
            }
            int[] res = statement.executeBatch();
            for (int r : res) {
                if (r > 0) {
                    count += r;
                }
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return count;
    }

    public int deleteCondition(String condition) {
        String sql = "DELETE FROM " + tableName
                + (condition == null || condition.isBlank() ? "" : " WHERE " + condition);
        return DerbyBase.update(sql);
    }

    public long clearData() {
        try ( Connection conn = DerbyBase.getConnection()) {
            return clearData(conn);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public int clearData(Connection conn) {
        if (conn == null) {
            return -1;
        }
        return DerbyBase.update("DELETE FROM " + tableName);
    }

    public BaseTable readDefinitionFromDB(String tableName) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return readDefinitionFromDB(conn, tableName);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return this;
        }
    }

    public static String savedName(String referedName) {
        if (referedName == null) {
            return null;
        }
        return referedName.startsWith("\"") && referedName.endsWith("\"")
                ? referedName.substring(1, referedName.length() - 1) : referedName.toUpperCase();
    }

    public static String referredName(String nameFromDB) {
        if (nameFromDB == null) {
            return null;
        }
        return nameFromDB.equals(nameFromDB.toUpperCase())
                ? nameFromDB.toLowerCase() : "\"" + nameFromDB + "\"";
    }

    public BaseTable readDefinitionFromDB(Connection conn, String referredTableName) {
        if (referredTableName == null || referredTableName.isBlank()) {
            return null;
        }
        try {
            init();
            tableName = referredTableName;
            String savedTableName = savedName(referredTableName);
            DatabaseMetaData dbMeta = conn.getMetaData();
            try ( ResultSet resultSet = dbMeta.getColumns(null, "MARA", savedTableName, "%")) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("COLUMN_NAME");
                    String referredColumnName = referredName(savedColumnName);
                    String defaultValue = resultSet.getString("COLUMN_DEF");
                    if (defaultValue != null && defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                        defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                    }
                    ColumnDefinition column = ColumnDefinition.create()
                            .setTableName(tableName)
                            .setColumnName(referredColumnName)
                            .setType(ColumnDefinition.sqlColumnType(resultSet.getInt("DATA_TYPE")))
                            .setLength(resultSet.getInt("COLUMN_SIZE"))
                            .setNotNull("NO".equalsIgnoreCase(resultSet.getString("IS_NULLABLE")))
                            .setAuto("YES".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT")))
                            .setDefaultValue(defaultValue);
                    columns.add(column);
                }
            } catch (Exception e) {
                MyBoxLog.error(e, tableName);
            }
            primaryColumns = new ArrayList<>();
            try ( ResultSet resultSet = dbMeta.getPrimaryKeys(null, "MARA", savedTableName)) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("COLUMN_NAME");
                    String referredName = referredName(savedColumnName);
                    for (ColumnDefinition column : columns) {
                        if (referredName.equals(column.getColumnName())) {
                            column.setIsPrimaryKey(true);
                            if (column.isAuto()) {
                                column.setAuto(true);
                                idColumn = referredName;
                            }
                            primaryColumns.add(column);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
//                MyBoxLog.console(e);
            }
            foreignColumns = new ArrayList<>();
            try ( ResultSet resultSet = dbMeta.getImportedKeys(null, "MARA", savedTableName)) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("FKCOLUMN_NAME");
                    String referredName = referredName(savedColumnName);
                    for (ColumnDefinition column : columns) {
                        if (referredName.equals(column.getColumnName())) {
                            column.setReferName(resultSet.getString("FK_NAME"))
                                    .setReferTable(resultSet.getString("PKTABLE_NAME"))
                                    .setReferColumn(resultSet.getString("PKCOLUMN_NAME"))
                                    .setOnDelete(ColumnDefinition.deleteRule(resultSet.getShort("DELETE_RULE")))
                                    .setOnUpdate(ColumnDefinition.updateRule(resultSet.getShort("UPDATE_RULE")));
                            foreignColumns.add(column);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
            referredColumns = new ArrayList<>();
            try ( ResultSet resultSet = dbMeta.getExportedKeys(null, "MARA", savedTableName)) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("PKCOLUMN_NAME");
                    String referredName = referredName(savedColumnName);
                    for (ColumnDefinition column : columns) {
                        if (referredName.equals(column.getColumnName())) {
                            ColumnDefinition rcolumn = column.cloneAll();
                            rcolumn.setReferName(resultSet.getString("FK_NAME"))
                                    .setReferTable(resultSet.getString("FKTABLE_NAME"))
                                    .setReferColumn(resultSet.getString("FKCOLUMN_NAME"))
                                    .setOnDelete(ColumnDefinition.deleteRule(resultSet.getShort("DELETE_RULE")))
                                    .setOnUpdate(ColumnDefinition.updateRule(resultSet.getShort("UPDATE_RULE")));
                            referredColumns.add(rcolumn);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return this;
    }

    public boolean sameRow(D row1, D row2) {
        if (tableName == null || columns.isEmpty() || row1 == null || row2 == null) {
            return false;
        }
        for (ColumnDefinition column : primaryColumns) {
            String name = column.getColumnName();
            Object v1 = getValue(row1, name);
            Object v2 = getValue(row2, name);
            if (v1 == null || v2 == null || !v1.equals(v2)) {
                return false;
            }
        }
        return true;
    }

    public boolean exist(Connection conn, String referredName) {
        if (conn == null || referredName == null) {
            return false;
        }
        try ( ResultSet resultSet = conn.getMetaData().getColumns(null, "MARA", savedName(referredName), "%")) {
            return resultSet.next();
        } catch (Exception e) {
            MyBoxLog.error(e, referredName);
        }
        return false;
    }

    public String string(String value) {
        return DerbyBase.stringValue(value);
    }

    public void print(D data) {
        if (data == null) {
            return;
        }
        for (ColumnDefinition column : columns) {
            MyBoxLog.console(column.getColumnName() + ": " + getValue(data, column.getColumnName()));
        }
    }

    /*
        get/set
     */
    public String getTableName() {
        return tableName;
    }

    public BaseTable setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public BaseTable setIdColumn(String idColumn) {
        this.idColumn = idColumn;
        return this;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public BaseTable setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
        return this;
    }

    public Era.Format getTimeFormat() {
        return timeFormat;
    }

    public BaseTable setTimeFormat(Era.Format timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    public List<ColumnDefinition> getPrimaryColumns() {
        return primaryColumns;
    }

    public void setPrimaryColumns(List<ColumnDefinition> primaryColumns) {
        this.primaryColumns = primaryColumns;
    }

    public List<ColumnDefinition> getForeignColumns() {
        return foreignColumns;
    }

    public void setForeignColumns(List<ColumnDefinition> foreignColumns) {
        this.foreignColumns = foreignColumns;
    }

    public boolean isSupportBatchUpdate() {
        return supportBatchUpdate;
    }

    public BaseTable setSupportBatchUpdate(boolean supportBatchUpdate) {
        this.supportBatchUpdate = supportBatchUpdate;
        return this;
    }

    public long getNewID() {
        return newID;
    }

    public void setNewID(long newID) {
        this.newID = newID;
    }

    public String getOrderColumns() {
        return orderColumns;
    }

    public void setOrderColumns(String orderColumns) {
        this.orderColumns = orderColumns;
    }

}
