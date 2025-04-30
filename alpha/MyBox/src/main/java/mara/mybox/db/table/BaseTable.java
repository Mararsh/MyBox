package mara.mybox.db.table;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
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
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Boolean;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Clob;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Enumeration;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Era;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Short;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.IntTools;
import mara.mybox.tools.LongTools;
import mara.mybox.tools.ShortTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
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

    public String tableName, idColumnName, orderColumns, tableTitle;
    public List<ColumnDefinition> columns, primaryColumns, foreignColumns, referredColumns;
    public boolean supportBatchUpdate;
    public long newID = -1;

    public abstract boolean valid(D data);

    public abstract boolean setValue(D data, String column, Object value);

    public abstract Object getValue(D data, String column);

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
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            if (setColumnsValues(statement, primaryColumns, data, 1) < 0) {
                return null;
            }
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
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
            MyBoxLog.debug(e, tableName);
        }
        return null;
    }

    public Object readColumnValue(ResultSet results, ColumnDefinition column) {
        if (results == null || column == null) {
            return null;
        }
        return column.value(results);
    }

    public boolean setColumnValue(PreparedStatement statement,
            ColumnDefinition column, D data, int index) {
        if (statement == null || data == null || column == null || index < 0) {
            return false;
        }
        try {
            Object value = getValue(data, column.getColumnName());
            // Not check maxValue/minValue.
            boolean notPermitNull = column.isNotNull();
            switch (column.getType()) {
                case String:
                case Color:
                case File:
                case Image:
                case Enumeration:
                case EnumerationEditable:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.VARCHAR);
                    } else {
                        String s;
                        try {
                            s = (String) value;
                        } catch (Exception ex) {
                            try {
                                s = (String) column.defaultValue();
                            } catch (Exception exs) {
                                s = "";
                            }
                        }
                        if (s == null && !notPermitNull) {
                            s = "";
                        }
                        if (s == null) {
                            statement.setNull(index, Types.VARCHAR);
                        } else {
                            if (column.getLength() > 0 && s.length() > column.getLength()) {
                                s = s.substring(0, column.getLength());
                            }
                            statement.setString(index, s);
                        }
                    }
                    break;
                case Double:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.DOUBLE);
                    } else {
                        double d;
                        try {
                            d = (double) value;
                        } catch (Exception ex) {
                            try {
                                d = (double) column.defaultValue();
                            } catch (Exception exs) {
                                d = AppValues.InvalidDouble;
                            }
                        }
                        if (DoubleTools.invalidDouble(d)) {
                            d = AppValues.InvalidDouble;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.DOUBLE);
                                break;
                            }
                        }
                        statement.setDouble(index, d);
                    }
                    break;
                case Longitude:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.DOUBLE);
                    } else {
                        double d;
                        try {
                            d = (double) value;
                        } catch (Exception ex) {
                            d = -200;
                        }
                        if (DoubleTools.invalidDouble(d)
                                || d > 180 || d < -180) {
                            d = -200;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.DOUBLE);
                                break;
                            }
                        }
                        statement.setDouble(index, d);
                    }
                    break;
                case Latitude:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.DOUBLE);
                    } else {
                        double d;
                        try {
                            d = (double) value;
                        } catch (Exception ex) {
                            d = -200;
                        }
                        if (DoubleTools.invalidDouble(d)
                                || d > 90 || d < -90) {
                            d = -200;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.DOUBLE);
                                break;
                            }
                        }
                        statement.setDouble(index, d);
                    }
                    break;
                case Float:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.FLOAT);
                    } else {
                        float f;
                        try {
                            f = (float) value;
                        } catch (Exception ex) {
                            try {
                                f = (float) column.defaultValue();
                            } catch (Exception exs) {
                                f = AppValues.InvalidFloat;
                            }
                        }
                        if (FloatTools.invalidFloat(f)) {
                            f = AppValues.InvalidFloat;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.FLOAT);
                                break;
                            }
                        }
                        statement.setFloat(index, f);
                    }
                    break;
                case Long:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.BIGINT);
                    } else {
                        long l;
                        try {
                            l = (long) value;
                        } catch (Exception ex) {
                            try {
                                l = (long) column.defaultValue();
                            } catch (Exception exx) {
                                l = AppValues.InvalidLong;
                            }
                        }
                        if (LongTools.invalidLong(l)) {
                            l = AppValues.InvalidLong;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.BIGINT);
                                break;
                            }
                        }
                        statement.setLong(index, l);
                    }
                    break;
                case Integer:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.INTEGER);
                    } else {
                        int i;
                        try {
                            i = (int) value;
                        } catch (Exception ex) {
                            try {
                                i = (int) column.defaultValue();
                            } catch (Exception exx) {
                                i = AppValues.InvalidInteger;
                            }
                        }
                        if (IntTools.invalidInt(i)) {
                            i = AppValues.InvalidInteger;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.INTEGER);
                                break;
                            }
                        }
                        statement.setInt(index, i);
                    }
                    break;
                case Short:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.SMALLINT);
                    } else {
                        short r;
                        try {
                            if (value instanceof Integer) { // sometime value becomes Integer...
                                r = (short) ((int) value);
                            } else {
                                r = (short) value;
                            }
                        } catch (Exception ex) {
                            try {
                                r = (short) column.defaultValue();
                            } catch (Exception exx) {
                                r = AppValues.InvalidShort;
                            }
                        }
                        if (ShortTools.invalidShort(r)) {
                            r = AppValues.InvalidShort;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.SMALLINT);
                                break;
                            }
                        }
                        statement.setShort(index, r);
                    }
                    break;
                case EnumeratedShort:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.SMALLINT);
                    } else {
                        short r;
                        try {
                            if (value instanceof Integer) { // sometime value becomes Integer...
                                r = (short) ((int) value);
                            } else {
                                r = (short) value;
                            }
                        } catch (Exception ex) {
                            r = 0;
                        }
                        if (ShortTools.invalidShort(r)) {
                            r = 0;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.SMALLINT);
                                break;
                            }
                        }
                        statement.setShort(index, r);
                    }
                    break;
                case Boolean:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.BOOLEAN);
                    } else {
                        boolean b;
                        try {
                            b = (boolean) value;
                        } catch (Exception ex) {
                            try {
                                b = (boolean) column.defaultValue();
                            } catch (Exception exx) {
                                b = false;
                            }
                        }
                        statement.setBoolean(index, b);
                    }
                    break;
                case NumberBoolean:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.SMALLINT);
                    } else {
                        short r;
                        try {
                            if (value instanceof Integer) {
                                r = (short) ((int) value);
                            } else {
                                r = (short) value;
                            }
                        } catch (Exception ex) {
                            r = 0;
                        }
                        if (ShortTools.invalidShort(r)) {
                            r = 0;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.SMALLINT);
                                break;
                            }
                        }
                        statement.setShort(index, r);
                    }
                    break;
                case Datetime:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.TIMESTAMP);
                    } else {
                        Date dt;
                        try {
                            dt = (Date) value;
                        } catch (Exception ex) {
                            try {
                                dt = (Timestamp) column.defaultValue();
                            } catch (Exception exx) {
                                dt = null;
                            }
                        }
                        if (dt == null) {
                            if (!notPermitNull) {
                                statement.setNull(index, Types.TIMESTAMP);
                                break;
                            }
                            dt = new Date();
                        }
                        statement.setTimestamp(index, new Timestamp(dt.getTime()));
                    }
                    break;
                case Date:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.DATE);
                    } else {
                        Date dd;
                        try {
                            dd = (Date) value;
                        } catch (Exception ex) {
                            try {
                                dd = (java.sql.Date) column.defaultValue();
                            } catch (Exception exx) {
                                dd = null;
                            }
                        }
                        if (dd == null) {
                            if (!notPermitNull) {
                                statement.setNull(index, Types.DATE);
                                break;
                            }
                            dd = new Date();
                        }
                        statement.setDate(index, new java.sql.Date(dd.getTime()));
                    }
                    break;
                case Era:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.BIGINT);
                    } else {
                        long el;
                        try {
                            el = Long.parseLong(value + "");
                            if (el < 10000 && el > -10000) {
                                Date ed = DateTools.encodeDate((String) value);
                                el = ed.getTime();
                            }
                        } catch (Exception ex) {
                            try {
                                Date ed = DateTools.encodeDate((String) value);
                                el = ed.getTime();
                            } catch (Exception e) {
                                el = AppValues.InvalidLong;
                            }
                        }
                        if (LongTools.invalidLong(el)) {
                            el = AppValues.InvalidLong;
                            if (!notPermitNull) {
                                statement.setNull(index, Types.BIGINT);
                                break;
                            }
                        }
                        statement.setLong(index, el);
                    }
                    break;
                case Clob:
                    if (value == null && !notPermitNull) {
                        statement.setNull(index, Types.CLOB);
                    } else {
                        String cb;
                        try {
                            cb = (String) value;
                        } catch (Exception ex) {
                            try {
                                cb = (String) column.defaultValue();
                            } catch (Exception exs) {
                                cb = null;
                            }
                        }
                        if (cb == null) {
                            if (!notPermitNull) {
                                statement.setNull(index, Types.CLOB);
                                break;
                            }
                            cb = "";
                        }
                        // CLOB is handled as string internally, and maxmium length is Integer.MAX(2G)
                        statement.setCharacterStream(index, new BufferedReader(new StringReader(cb)), cb.length());
                    }
                    break;
                case Blob:
                    if (value == null) {
                        statement.setNull(index, Types.BLOB);
                    } else {
                        // BLOB is handled as InputStream internally
                        try {
                            statement.setBinaryStream(index, (InputStream) value);
                        } catch (Exception ex) {
                            statement.setNull(index, Types.BLOB);
                        }
                    }
                    break;
                default:
                    MyBoxLog.debug(column.getColumnName() + " " + column.getType() + " " + value);
                    return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString(), tableName + " " + column.getColumnName());
            return false;
        }
    }

    public int setColumnsValues(PreparedStatement statement,
            List<ColumnDefinition> valueColumns, D data, int startIndex) {
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
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public int setColumnNamesValues(PreparedStatement statement,
            List<String> valueColumns, D data, int startIndex) {
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
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public boolean setInsertStatement(Connection conn,
            PreparedStatement statement, D data) {
        if (conn == null || statement == null || data == null) {
            return false;
        }
        return setColumnsValues(statement, insertColumns(), data, 1) > 0;
    }

    public boolean setUpdateStatement(Connection conn,
            PreparedStatement statement, D data) {
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
            MyBoxLog.debug(e, tableName);
            return false;
        }
    }

    public boolean setDeleteStatement(Connection conn,
            PreparedStatement statement, D data) {
        if (conn == null || statement == null || data == null) {
            return false;
        }
        return setColumnsValues(statement, primaryColumns, data, 1) > 0;
    }

    public List<String> allFields() {
        List<String> names = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            names.add(column.getColumnName());
        }
        return names;
    }

    public List<String> importNecessaryFields() {
        List<String> names = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            if (column.isNotNull() && !column.isAuto()) {
                names.add(column.getColumnName());
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
        idColumnName = null;
        orderColumns = null;
        columns = new ArrayList<>();
        primaryColumns = new ArrayList<>();
        foreignColumns = new ArrayList<>();
        referredColumns = new ArrayList<>();
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
                    idColumnName = column.getColumnName();
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
        String sql = "CREATE TABLE " + DerbyBase.fixedIdentifier(tableName) + " ( \n";
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
            if (f != null && !f.isBlank()) {
                sql += ", \n" + f;
            }
        }
        sql += "\n)";
        return sql;
    }

    public String createColumnDefiniton(ColumnDefinition column) {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String colName = DerbyBase.fixedIdentifier(column.getColumnName());
        String def = colName + " ";
        ColumnType type = column.getType();
        switch (type) {
            case String:
            case File:
            case Image:
            case Enumeration:
            case EnumerationEditable:
                def += "VARCHAR(" + column.getLength() + ")";
                break;
            case Color:
                def += "VARCHAR(16)";
                break;
            case Double:
            case Longitude:
            case Latitude:
                def += "DOUBLE";
                break;
            case Float:
                def += "FLOAT";
                break;
            case Long:
                def += "BIGINT";
                break;
            case Integer:
                def += "INT";
                break;
            case Short:
            case EnumeratedShort:
                def += "SMALLINT";
                break;
            case Boolean:
                def += "BOOLEAN";
                break;
            case NumberBoolean:
                def += "SMALLINT";
                break;
            case Datetime:
                def += "TIMESTAMP";
                break;
            case Date:
                def += "DATE";
                break;
            case Era:
                def += "BIGINT";
                break;
            case Clob:
                def += "CLOB";
                break;
            case Blob:
                def += "BLOB";
                break;
            default:
                MyBoxLog.debug(colName + " " + type);
                return null;
        }
        if (column.isAuto()) {
            def += " NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)";

        } else if (column.isNotNull()) {
            def += " NOT NULL WITH DEFAULT " + column.dbDefaultValue();
        } else if (column.getDefaultValue() != null) {
            def += " WITH DEFAULT " + column.dbDefaultValue();
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
//            MyBoxLog.console(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
            return false;
        }
    }

    public final boolean createTable(Connection conn, boolean dropExisted) {
        if (conn == null || tableName == null) {
            return false;
        }
        try {
            if (DerbyBase.exist(conn, tableName) > 0) {
                if (!dropExisted) {
                    return true;
                }
                dropTable(conn);
                conn.commit();
            }
            createTable(conn);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean dropTable(Connection conn) {
        if (conn == null) {
            return false;
        }
        String sql = null;
        try {
            sql = "DROP TABLE " + DerbyBase.fixedIdentifier(tableName);
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
            return false;
        }
    }

    public boolean dropColumn(Connection conn, String colName) {
        if (conn == null || colName == null || colName.isBlank()) {
            return false;
        }
        String sql = null;
        try {
            sql = "ALTER TABLE " + DerbyBase.fixedIdentifier(tableName)
                    + " DROP COLUMN " + DerbyBase.fixedIdentifier(colName);
//            MyBoxLog.console(sql);
            return conn.createStatement().executeUpdate(sql) >= 0;
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
            return false;
        }
    }

    public boolean addColumn(Connection conn, ColumnDefinition column) {
        if (conn == null || column == null) {
            return false;
        }
        String sql = null;
        try {
            sql = "ALTER TABLE " + DerbyBase.fixedIdentifier(tableName)
                    + " ADD COLUMN  " + createColumnDefiniton(column);
            return conn.createStatement().executeUpdate(sql) >= 0;
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
            return false;
        }
    }

    public void setId(D source, D target) {
        if (source == null || target == null || idColumnName == null) {
            return;
        }
        setValue(target, idColumnName, getValue(source, idColumnName));
    }

    // https://stackoverflow.com/questions/18555122/create-instance-of-generic-type-in-java-when-parameterized-type-passes-through-h?r=SearchResults
    // https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/Class.html#newInstance()
    public D newData() {
        try {
            Class<D> entityClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            D data = entityClass.getDeclaredConstructor().newInstance();
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public String name() {
        return tableName;
    }

    public List<String> columnNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            names.add(column.getColumnName());
        }
        return names;
    }

    public String values(D data) {
        if (data == null) {
            return null;
        }
        String s = "", name;
        for (ColumnDefinition column : columns) {
            name = column.getColumnName();
            s += name + ": " + getValue(data, name) + "\n";
        }
        return s;
    }

    public String sizeStatement() {
        if (tableName == null) {
            return null;
        }
        return "SELECT COUNT("
                + (idColumnName != null ? idColumnName : "*")
                + ") FROM " + DerbyBase.fixedIdentifier(tableName);
    }

    public int size() {
        return conditionSize(null);
    }

    public int size(Connection conn) {
        return conditionSize(conn, null);
    }

    public int conditionSize(String condition) {
        try (Connection conn = DerbyBase.getConnection()) {
            return conditionSize(conn, condition);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return 0;
        }
    }

    public int conditionSize(Connection conn, String condition) {
        try {
            if (conn == null || conn.isClosed()) {
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
            conn.setAutoCommit(true);
            try (PreparedStatement sizeQuery = conn.prepareStatement(sql);
                    ResultSet results = sizeQuery.executeQuery()) {
                if (results != null && results.next()) {
                    size = results.getInt(1);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e, sql);
            }
            return size;
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public boolean isEmpty(Connection conn) {
        String sql = "SELECT * FROM " + DerbyBase.fixedIdentifier(tableName) + " FETCH FIRST ROW ONLY";
        return isEmpty(conn, sql);
    }

    public boolean isEmpty(String sql) {
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return isEmpty(conn, sql);
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
            return true;
        }
    }

    public boolean isEmpty(Connection conn, String sql) {
        try {
            boolean isEmpty = true;
            conn.setAutoCommit(true);
            try (PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet results = statement.executeQuery()) {
                isEmpty = results == null || !results.next();
            } catch (Exception e) {
                MyBoxLog.debug(e, sql);
            }
            return isEmpty;
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return false;
        }
    }

    public String queryStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String sql = null;
        for (ColumnDefinition column : primaryColumns) {
            if (sql == null) {
                sql = "SELECT * FROM " + DerbyBase.fixedIdentifier(tableName) + " WHERE ";
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
                sql = "INSERT INTO " + DerbyBase.fixedIdentifier(tableName) + " ( ";
                v = "?";
            } else {
                sql += ", ";
                v += ", ?";
            }
            sql += DerbyBase.fixedIdentifier(column.getColumnName());
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
                update = "UPDATE " + DerbyBase.fixedIdentifier(tableName) + " SET ";
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
            where += DerbyBase.fixedIdentifier(column.getColumnName()) + "=?";
        }
        return update + (where != null ? where : "");
    }

    public String deleteStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String delete = "DELETE FROM " + DerbyBase.fixedIdentifier(tableName);
        String where = null;
        for (ColumnDefinition column : primaryColumns) {
            if (where == null) {
                where = " WHERE ";
            } else {
                where += " AND ";
            }
            where += DerbyBase.fixedIdentifier(column.getColumnName()) + "=?";
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
            if (column.getColumnName().equals(message)) {
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

    public String columnLabel(ColumnDefinition column) {
        if (column == null) {
            return null;
        }
        return column.label();
    }

    public String label(String columnName) {
        return columnLabel(column(columnName));
    }

    public String columnsText() {
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

    public String definitionHtml() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String html = columnsHtml();
        String referred = referredColumnsHtml();
        if (referred != null) {
            html += "</BR>" + referred;
        }
        html += "</BR><HR>" + StringTools.replaceHtmlLineBreak(createTableStatement());
        return HtmlWriteTools.html(tableName, HtmlStyles.styleValue("Default"), html);
    }

    public String displayValue(ColumnDefinition column, Object v) {
        if (column == null || v == null) {
            return null;
        }
        return column.formatValue(v);
    }

    public String exportValue(ColumnDefinition column, Object v, boolean format) {
        if (column == null || v == null) {
            return null;
        }
        return column.exportValue(v, format);
    }

    public Object importValue(ColumnDefinition column, String v) {
        if (column == null || v == null) {
            return null;
        }
        return column.fromString(v, InvalidAs.Use);
    }

    public String htmlList(BaseData data) {
        try {
            if (data == null || columns == null) {
                return null;
            }
            String lineBreak = "<BR>";
            String info = null;
            for (ColumnDefinition column : columns) {
                Object value = data.getValue(column.getColumnName());
                String display = displayValue(column, value);
                if (display == null || display.isBlank()) {
                    continue;
                }
                if (column.getType() == ColumnDefinition.ColumnType.Image) {
                    display = "<img src=\"file:///" + display.replaceAll("\\\\", "/") + "\" width=200px>";
                } else {
                    display = StringTools.replaceLineBreak(display, lineBreak);
                }
                if (info != null) {
                    info += lineBreak;
                } else {
                    info = "";
                }
                info += label(column.getColumnName()) + ": " + display;
            }
            return info;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public StringTable htmlTable(BaseData data) {
        try {
            if (data == null) {
                return null;
            }
            String lineBreak = "<BR>";
            List<String> names = new ArrayList<>();
            StringTable htmlTable = new StringTable(names);
            names.addAll(Arrays.asList(message("Name"), message("Value")));
            for (ColumnDefinition column : columns) {
                Object value = data.getValue(column.getColumnName());
                String display = displayValue(column, value);
                if (display == null || display.isBlank()) {
                    continue;
                }
                if (column.getType() == ColumnDefinition.ColumnType.Image) {
                    display = "<img src=\"file:///" + display.replaceAll("\\\\", "/") + "\" width=200px>";
                } else {
                    display = StringTools.replaceLineBreak(display, lineBreak);
                }
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(column.getColumnName(), display));
                htmlTable.add(row);
            }
            return htmlTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String text(BaseData data) {
        try {
            if (data == null || columns == null) {
                return null;
            }
            String lineBreak = "\n";
            String info = null;
            for (ColumnDefinition column : columns) {
                Object value = data.getValue(column.getColumnName());
                String display = displayValue(column, value);
                if (display == null || display.isBlank()) {
                    continue;
                }
                if (info != null) {
                    info += lineBreak;
                } else {
                    info = "";
                }
                info += label(column.getColumnName()) + ": " + display;
            }
            return info;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        data 2d
     */
    public DataTable recordTable(Connection conn) {
        if (conn == null) {
            return null;
        }
        try {
            TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
            DataTable dataTable = BaseTableTools.isInternalTable(tableName)
                    ? new DataInternalTable() : new DataTable();
            String sheet = DerbyBase.fixedIdentifier(tableName);
            dataTable.setSheet(sheet)
                    .setColsNumber(columns.size());
            if (this instanceof BaseNodeTable) {
                dataTable.setDataName(((BaseNodeTable) this).getDataName());
            } else {
                dataTable.setDataName(tableName);
            }
            dataTable = tableData2DDefinition.writeTable(conn, dataTable);
            if (dataTable == null) {
                return null;
            }
            long tableID = dataTable.getDataID();
            List<Data2DColumn> data2dColumns = new ArrayList<>();
            for (ColumnDefinition column : columns) {
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(column);
                dataColumn.setColumnName(DerbyBase.fixedIdentifier(column.getColumnName()));
                dataColumn.setData2DDefinition(dataTable).setDataID(tableID).setColumnID(-1);
                data2dColumns.add(dataColumn);
            }
            TableData2DColumn tableData2DColumn = new TableData2DColumn();
            tableData2DColumn.setTableData2DDefinition(tableData2DDefinition);
            if (tableData2DColumn.save(conn, tableID, data2dColumns)) {
                dataTable.setColumns(data2dColumns);
                dataTable.setColsNumber(data2dColumns.size());
                return dataTable;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        query
     */
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
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return readData(conn, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return null;
        }
    }

    public D queryOne(String sql) {
        D data = null;
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            data = query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
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
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    data = readData(results);
                } else {
                    return null;
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return null;
        }
    }

    public D query(Connection conn, PreparedStatement statement, String value) {
        if (conn == null || statement == null || value == null) {
            return null;
        }
        try {
            statement.setString(1, value);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName + " " + value);
            return null;
        }
    }

    public List<D> queryPreLike(Connection conn, PreparedStatement statement,
            String value) {
        List<D> dataList = new ArrayList<>();
        if (conn == null || statement == null || value == null) {
            return dataList;
        }
        try {
            statement.setMaxRows(1);
            statement.setString(1, "%" + value);
            conn.setAutoCommit(true);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName + " " + value);
        }
        return dataList;
    }

    public List<D> query(PreparedStatement statement) {
        List<D> dataList = new ArrayList<>();
        if (statement == null) {
            return dataList;
        }
        try (ResultSet results = statement.executeQuery()) {
            while (results != null && results.next()) {
                D data = readData(results);
                if (data != null) {
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        return dataList;
    }

    public List<D> query(String sql) {
        return query(sql, -1);
    }

    public List<D> query(String sql, int max) {
        List<D> dataList = new ArrayList<>();
        try (Connection conn = DerbyBase.getConnection()) {
            dataList = query(conn, sql, max);
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
        }
        return dataList;
    }

    public List<D> query(Connection conn, String sql) {
        return query(conn, sql, -1);
    }

    public List<D> query(Connection conn, String sql, int max) {
        List<D> dataList = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            if (max > 0) {
                statement.setMaxRows(max);
            }
            conn.setAutoCommit(true);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
        }
        return dataList;
    }

    public String queryAllStatement() {
        return "SELECT * FROM " + DerbyBase.fixedIdentifier(tableName)
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

    public List<D> queryConditions(String condition, String orderby, long start,
            long size) {
        List<D> dataList = new ArrayList<>();
        if (start < 0 || size <= 0) {
            return dataList;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return queryConditions(conn, condition, orderby, start, size);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return dataList;
        }
    }

    public List<D> queryConditions(Connection conn, String condition,
            String orderby, long start, long size) {
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
        String sql = "SELECT * FROM " + DerbyBase.fixedIdentifier(tableName) + c
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

    public D query(long id) {
        if (id < 0 || idColumnName == null || idColumnName.isBlank()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return query(conn, id);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public D query(Connection conn, long id) {
        if (conn == null || id < 0 || idColumnName == null || idColumnName.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumnName + "=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        update
     */
    public D writeData(D data) {
        newID = -1;
        if (!valid(data)) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return writeData(conn, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
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
                if (idColumnName != null) {
                    setId(exist, data);
                }
                return updateData(conn, data);
            } else {
                return insertData(conn, data);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return null;
        }
    }

    public D insertData(D data) {
        if (data == null) {
            newID = -1;
            return data;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return insertData(conn, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return null;
        }
    }

    public D insertData(Connection conn, D data) {
        newID = -1;
        if (conn == null || !valid(data)) {
            return null;
        }
        String sql = insertStatement();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            return insertData(conn, statement, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
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
                    if (idColumnName != null) {
//                        boolean ac = conn.getAutoCommit();
//                        conn.setAutoCommit(true);
                        try (Statement query = conn.createStatement();
                                ResultSet resultSet = query.executeQuery("VALUES IDENTITY_VAL_LOCAL()")) {
                            if (resultSet.next()) {
                                newID = resultSet.getLong(1);
                                setValue(data, idColumnName, newID);
//                                MyBoxLog.console(tableName + "  " + newID);
                            }
                        } catch (Exception e) {
                            MyBoxLog.debug(e, tableName);
                        }
//                        conn.setAutoCommit(ac);
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
//        MyBoxLog.console(tableName + "  " + newID);
        return null;
    }

    public int insertList(List<D> dataList) {
        if (dataList == null) {
            return -1;
        }
        int count = -1;
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            count = insertList(conn, dataList);
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        return count;
    }

    public int insertList(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null) {
            return -1;
        }
        String sql = insertStatement();
        int count = 0;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int i = 0; i < dataList.size(); ++i) {
                D data = dataList.get(i);
                if (!setInsertStatement(conn, statement, data)) {
                    continue;
                }
                statement.addBatch();
                if (i > 0 && (i % Database.BatchSize == 0)) {
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
            statement.clearBatch();
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
        }
        return count;
    }

    public D updateData(D data) {
        try (Connection conn = DerbyBase.getConnection()) {
            return updateData(conn, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return null;
        }
    }

    public D updateData(Connection conn, D data) {
        if (conn == null || !valid(data)) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(updateStatement())) {
            return updateData(conn, statement, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
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
            MyBoxLog.debug(e, tableName);
        }
        return null;
    }

    public int updateList(List<D> dataList) {
        if (dataList == null) {
            return -1;
        }
        int count = -1;
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            count = updateList(conn, dataList);
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        return count;
    }

    public int updateList(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null) {
            return -1;
        }
        String sql = updateStatement();
        int count = 0;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int i = 0; i < dataList.size(); ++i) {
                D data = dataList.get(i);
                if (!setUpdateStatement(conn, statement, data)) {
                    continue;
                }
                statement.addBatch();
                if (i > 0 && (i % Database.BatchSize == 0)) {
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
            statement.clearBatch();
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
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
            MyBoxLog.debug(e, tableName + " " + value);
            return -1;
        }
    }

    public int updatePreLike(Connection conn, PreparedStatement statement,
            String value) {
        if (conn == null || statement == null || value == null) {
            return -1;
        }
        try {
            statement.setString(1, "%" + value);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName + " " + value);
            return -1;
        }
    }

    public int update(Connection conn, String sql) {
        if (conn == null || sql == null) {
            return -1;
        }
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e, sql);
            return -1;
        }
    }

    public int setAll(List<D> dataList) {
        try (Connection conn = DerbyBase.getConnection()) {
            return setAll(conn, dataList);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public int setAll(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null) {
            return -1;
        }
        List<D> shouldDelete = new ArrayList<>();
        List<D> shouldUpdate = new ArrayList<>();
        try (PreparedStatement query = conn.prepareStatement(queryAllStatement());
                ResultSet results = query.executeQuery()) {
            conn.setAutoCommit(true);
            while (results != null && results.next()) {
                D tableItem = readData(results);
                if (tableItem == null) {
                    continue;
                }
                boolean inList = false;
                for (D listItem : dataList) {
                    if (sameRow(tableItem, listItem)) {
                        inList = true;
                        break;
                    }
                }
                if (inList) {
                    shouldUpdate.add(tableItem);
                } else {
                    shouldDelete.add(tableItem);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        int count = 0;
        try {
            deleteData(conn, shouldDelete);
            int ret = updateList(conn, shouldUpdate);
            if (ret > 0) {
                count += ret;
            }
            List<D> shouldInsert = new ArrayList<>();
            for (D item : dataList) {
                if (!valid(item)) {
                    continue;
                }
                boolean inList = false;
                for (D update : shouldUpdate) {
                    if (sameRow(item, update)) {
                        inList = true;
                        break;
                    }
                }
                if (!inList) {
                    shouldInsert.add(item);
                }
            }
            ret = insertList(conn, shouldInsert);
            if (ret > 0) {
                count += ret;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        return count;
    }

    public int deleteData(D data) {
        try (Connection conn = DerbyBase.getConnection()) {
            return deleteData(conn, data);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public int deleteData(Connection conn, D data) {
        try (PreparedStatement statement = conn.prepareStatement(deleteStatement())) {
            setDeleteStatement(conn, statement, data);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public int deleteData(List<D> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return deleteData(conn, dataList);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public int deleteData(Connection conn, List<D> dataList) {
        if (conn == null || dataList == null || dataList.isEmpty()) {
            return 0;
        }
        int count = 0;
        try (PreparedStatement statement = conn.prepareStatement(deleteStatement())) {
            conn.setAutoCommit(false);
            for (int i = 0; i < dataList.size(); ++i) {
                D data = dataList.get(i);
                if (!setDeleteStatement(conn, statement, data)) {
                    continue;
                }
                statement.addBatch();
                if (i > 0 && (i % Database.BatchSize == 0)) {
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
            statement.clearBatch();
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        return count;
    }

    public int deleteCondition(String condition) {
        String sql = "DELETE FROM " + DerbyBase.fixedIdentifier(tableName)
                + (condition == null || condition.isBlank() ? "" : " WHERE " + condition);
        return DerbyBase.update(sql);
    }

    public long clearData() {
        try (Connection conn = DerbyBase.getConnection()) {
            return clearData(conn);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return -1;
        }
    }

    public long clearData(Connection conn) {
        int count = -1;
        String clearSQL = "DELETE FROM " + DerbyBase.fixedIdentifier(tableName);
        try (PreparedStatement clear = conn.prepareStatement(clearSQL)) {
            count = clear.executeUpdate();
            if (count >= 0 && idColumnName != null) {
                String resetSQL = "ALTER TABLE " + DerbyBase.fixedIdentifier(tableName)
                        + " ALTER COLUMN " + idColumnName + " RESTART WITH 1";
                try (PreparedStatement reset = conn.prepareStatement(resetSQL)) {
                    reset.executeUpdate();
                } catch (Exception e) {
                    MyBoxLog.debug(e, resetSQL);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, clearSQL);
        }
        return count;
    }

    public BaseTable readDefinitionFromDB(String tableName) {
        try (Connection conn = DerbyBase.getConnection()) {
            return readDefinitionFromDB(conn, tableName);
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
            return this;
        }
    }

    public BaseTable readDefinitionFromDB(Connection conn, String tname) {
        if (tname == null || tname.isBlank()) {
            return null;
        }
        try {
            init();
            tableName = tname;
            String savedTableName = DerbyBase.savedName(tname);
            DatabaseMetaData dbMeta = conn.getMetaData();
            try (ResultSet resultSet = dbMeta.getColumns(null, "MARA", savedTableName, "%")) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("COLUMN_NAME");
                    String referredName = DerbyBase.fixedIdentifier(savedColumnName);
                    String defaultValue = resultSet.getString("COLUMN_DEF");
                    if (defaultValue != null && defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                        defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                    }
                    ColumnDefinition column = ColumnDefinition.create()
                            .setTableName(tableName)
                            .setColumnName(referredName)
                            .setType(ColumnDefinition.sqlColumnType(resultSet.getInt("DATA_TYPE")))
                            .setLength(resultSet.getInt("COLUMN_SIZE"))
                            .setNotNull("NO".equalsIgnoreCase(resultSet.getString("IS_NULLABLE")))
                            .setAuto("YES".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT")))
                            .setDefaultValue(defaultValue);
                    columns.add(column);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e, tableName);
            }
            primaryColumns = new ArrayList<>();
            try (ResultSet resultSet = dbMeta.getPrimaryKeys(null, "MARA", savedTableName)) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("COLUMN_NAME");
                    String referredName = DerbyBase.fixedIdentifier(savedColumnName);
                    for (ColumnDefinition column : columns) {
                        if (referredName.equals(column.getColumnName())) {
                            column.setIsPrimaryKey(true);
                            if (column.isAuto()) {
                                column.setAuto(true);
                                idColumnName = referredName;
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
            try (ResultSet resultSet = dbMeta.getImportedKeys(null, "MARA", savedTableName)) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("FKCOLUMN_NAME");
                    String referredName = DerbyBase.fixedIdentifier(savedColumnName);
                    for (ColumnDefinition column : columns) {
                        if (referredName.equals(column.getColumnName())) {
                            column.setReferName(DerbyBase.fixedIdentifier(resultSet.getString("FK_NAME")))
                                    .setReferTable(DerbyBase.fixedIdentifier(resultSet.getString("PKTABLE_NAME")))
                                    .setReferColumn(DerbyBase.fixedIdentifier(resultSet.getString("PKCOLUMN_NAME")))
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
            try (ResultSet resultSet = dbMeta.getExportedKeys(null, "MARA", savedTableName)) {
                while (resultSet.next()) {
                    String savedColumnName = resultSet.getString("PKCOLUMN_NAME");
                    String referredName = DerbyBase.fixedIdentifier(savedColumnName);
                    for (ColumnDefinition column : columns) {
                        if (referredName.equals(column.getColumnName())) {
                            ColumnDefinition rcolumn = column.cloneAll();
                            rcolumn.setReferName(DerbyBase.fixedIdentifier(resultSet.getString("FK_NAME")))
                                    .setReferTable(DerbyBase.fixedIdentifier(resultSet.getString("FKTABLE_NAME")))
                                    .setReferColumn(DerbyBase.fixedIdentifier(resultSet.getString("FKCOLUMN_NAME")))
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
            MyBoxLog.debug(e, tableName);
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

    public String string(String value) {
        return DerbyBase.stringValue(value);
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

    public String getIdColumnName() {
        return idColumnName;
    }

    public BaseTable setIdColumn(String idColumn) {
        this.idColumnName = idColumn;
        return this;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public BaseTable setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
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

    public String getTableTitle() {
        return tableTitle != null ? tableTitle : tableName;
    }

    public BaseTable setTableTitle(String tableTitle) {
        this.tableTitle = tableTitle;
        return this;
    }

    public List<ColumnDefinition> getReferredColumns() {
        return referredColumns;
    }

    public BaseTable setReferredColumns(List<ColumnDefinition> referredColumns) {
        this.referredColumns = referredColumns;
        return this;
    }

}
