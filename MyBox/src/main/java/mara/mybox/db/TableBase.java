package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.data.Era;
import mara.mybox.data.StringTable;
import mara.mybox.db.ColumnDefinition.ColumnType;
import static mara.mybox.db.DerbyBase.BatchSize;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.tableMessage;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public abstract class TableBase<D> {

    protected String tableName, idColumn;
    protected List<ColumnDefinition> columns;
    protected List<String> primaryColumns, foreignColumns;
    protected Era.Format timeFormat;
    protected boolean supportBatchUpdate;

    /*
        Abstract methods
     */
    public abstract D newData();

    public abstract boolean setValue(D data, String column, Object value);

    public abstract Object getValue(D data, String column);

    public abstract void setId(D source, D target);

    public abstract boolean valid(D data);


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
        try {
            try ( PreparedStatement statement = conn.prepareStatement(queryStatement())) {
                if (setColumnsValues(statement, primaryColumns(), data, 1) < 0) {
                    return null;
                }
                return query(conn, statement);
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
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
                setValue(data, column.name, value);
//                logger.debug(column.name + " " + value);
            }
            for (int i = 0; i < foreignColumns.size(); ++i) {
                String name = foreignColumns.get(i);
//                logger.debug(name);
                Object value = readForeignValue(results, name);
                if (!setForeignValue(data, name, value)) {
                    return null;
                }
            }
            return data;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return null;
    }

    public Object readColumnValue(ResultSet results, ColumnDefinition column) {
        try {
            if (results == null || column == null) {
                return null;
            }
            switch (column.type) {
                case String:
                case Text:
                case Color:
                case File:
                    return results.getString(column.name);
                case Double:
                    return results.getDouble(column.name);
                case Float:
                    return results.getFloat(column.name);
                case Long:
                case Era:
                    return results.getLong(column.name);
                case Integer:
                    return results.getInt(column.name);
                case Boolean:
                    return results.getBoolean(column.name);
                case Short:
                    return results.getShort(column.name);
                case Datetime:
                    return results.getTimestamp(column.name).getTime();
                case Date:
                    return results.getDate(column.name).getTime();
                default:
                    logger.debug(column.name + " " + column.getType());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return null;
    }

    public List<D> readData(String sql) {
        return readData(sql, -1);
    }

    public List<D> readData(String sql, int max) {
//        logger.debug(sql + " " + max);
        List<D> dataList = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            if (max > 0) {
                statement.setMaxRows(max);
            }
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    D data = readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return dataList;
    }

    public boolean setColumnValue(PreparedStatement statement, ColumnDefinition column, D data, int index) {
        if (statement == null || data == null || column == null || index < 0) {
            return false;
        }
        try {
            Object value = getValue(data, column.name);
//            logger.error(index + " " + column.name + " " + column.type + " " + value);
            switch (column.type) {
                case String:
                case Text:
                case Color:
                case File:
                    if (value == null) {
                        statement.setNull(index, Types.VARCHAR);
                    } else {
                        statement.setString(index, (String) value);
                    }
                    break;
                case Double:
                    double d;
                    if (value == null) {
                        d = CommonValues.InvalidDouble;
                    } else {
                        d = (double) value;
                    }
                    statement.setDouble(index, d);
                    break;
                case Float:
                    float f;
                    if (value == null) {
                        f = Float.MIN_VALUE;
                    } else {
                        f = (float) value;
                    }
                    statement.setFloat(index, f);
                    break;
                case Long:
                case Era:
                    long l;
                    if (value == null) {
                        l = CommonValues.InvalidLong;
                    } else {
                        l = (long) value;
                    }
                    statement.setLong(index, l);
                    break;
                case Integer:
                    int ii;
                    if (value == null) {
                        ii = CommonValues.InvalidInteger;
                    } else {
                        ii = (int) value;
                    }
                    statement.setInt(index, ii);
                    break;
                case Boolean:
                    boolean b;
                    if (value == null) {
                        b = false;
                    } else {
                        b = (boolean) value;
                    }
                    statement.setBoolean(index, b);
                    break;
                case Short:
                    short s;
                    if (value == null) {
                        s = CommonValues.InvalidShort;
                    } else {
                        s = (short) value;
                    }
                    statement.setShort(index, s);
                    break;
                case Datetime:
                    if (value == null) {
                        statement.setNull(index, Types.VARCHAR);
                    } else {
                        Date datetime = (Date) value;
                        statement.setString(index, DateTools.datetimeToString(datetime.getTime()));
                    }
                    break;
                case Date:
                    if (value == null) {
                        statement.setNull(index, Types.VARCHAR);
                    } else {
                        Date date = (Date) value;
                        statement.setString(index, DateTools.datetimeToString(date.getTime()).substring(0, 10));
                    }
                    break;
                default:
                    logger.debug(column.name + " " + column.getType() + " " + value.toString());
                    return false;
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
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
            failed(e);
            logger.debug(e.toString());
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
            failed(e);
            logger.debug(e.toString());
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
            return setColumnsValues(statement, primaryColumns(), data, index) > 0;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

    public boolean setDeleteStatement(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || statement == null || !valid(data)) {
            return false;
        }
        return setColumnsValues(statement, primaryColumns(), data, 1) > 0;
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
            if (column.isNotNull() && !column.isID) {
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
        columns = new ArrayList<>();
        primaryColumns = new ArrayList<>();
        foreignColumns = new ArrayList<>();
        timeFormat = Era.Format.Datetime;
        supportBatchUpdate = false;
    }

    public TableBase() {
        init();
    }

    public TableBase addColumn(ColumnDefinition column) {
        if (column != null) {
            column.setIndex(columns.size() + 1);
            columns.add(column);
            if (column.isIsID()) {
                idColumn = column.getName();
            }
            if (column.isIsPrimaryKey()) {
                primaryColumns.add(column.getName());
            }
            if (column.getForeignTable() != null && column.getForeignColumn() != null) {
                foreignColumns.add(column.getName());
            }
        }
        return this;
    }

    public String createTableStatement() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String sql = "CREATE TABLE " + tableName + " ( \n";
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            sql += column.getName() + " ";
            switch (column.getType()) {
                case String:
                case Text:
                    sql += "VARCHAR(" + column.getLength() + ")";
                    break;
                case Color:
                    sql += "VARCHAR(16)";
                    break;
                case File:
                    sql += "VARCHAR(4096)";
                    break;
                case Double:
                    sql += "DOUBLE";
                    break;
                case Float:
                    sql += "FLOAT";
                    break;
                case Long:
                case Era:
                    sql += "BIGINT";
                    break;
                case Integer:
                    sql += "INT";
                    break;
                case Boolean:
                    sql += "BOOLEAN";
                    break;
                case Short:
                    sql += "SMALLINT";
                    break;
                case Datetime:
                    sql += "TIMESTAMP";
                    break;
                case Date:
                    sql += "DATE";
                    break;
                default:
                    logger.debug(column.getName() + " " + column.getType());
                    return null;
            }
            if (column.isNotNull()) {
                sql += " NOT NULL";
            }
            if (column.isIsID()) {
                sql += "  GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)";
            }
            sql += ", \n";
        }
        sql += "PRIMARY KEY ( ";
        for (int i = 0; i < primaryColumns.size(); ++i) {
            if (i > 0) {
                sql += ", ";
            }
            sql += primaryColumns.get(i);

        }
        sql += " ) ";
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            String f = column.foreignText();
            if (f != null) {
                sql += ", \n" + f;
            }
        }
        sql += "\n)";
//        logger.debug(sql);
        return sql;
    }

    public boolean createTable(Connection conn) {
        try {
            if (conn == null) {
                return false;
            }
            String sql = createTableStatement();
//            logger.debug(sql);
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean dropTable(Connection conn) {
        try {
            if (conn == null) {
                return false;
            }
            String sql = "DROP TABLE " + tableName;
//            logger.debug(sql);
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean clearTable(Connection conn) {
        try {
            if (conn == null) {
                return false;
            }
            String sql = "DELETE FROM " + tableName;
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public String name() {
        return tableMessage(tableName.toLowerCase());
    }

    public List<String> columnLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            labels.add(column.getLabel());
        }
        return labels;
    }

    public String sizeStatement() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String sql = "SELECT COUNT(" + columns.get(0).getName() + ") FROM " + tableName;
        return sql;
    }

    public int size() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return size(conn);
        } catch (Exception e) {
            failed(e);
            return 0;
        }
    }

    public int size(Connection conn) {
        try {
            conn.setReadOnly(true);
            String sql = sizeStatement();
            ResultSet results = conn.createStatement().executeQuery(sql);
            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (Exception e) {
            failed(e);
            return 0;
        }
    }

    public String queryStatement() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String sql = null;
        for (String column : primaryColumns) {
            if (sql == null) {
                sql = "SELECT * FROM " + tableName + " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += column + "=? ";
        }
        return sql;
    }

    public List<ColumnDefinition> insertColumns() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        List<ColumnDefinition> columnsList = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            String name = column.getName();
            if (idColumn != null && name.equals(idColumn)) {
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
            String name = column.getName();
            if (idColumn != null && name.equals(idColumn)) {
                continue;
            }
            if (sql == null) {
                sql = "INSERT INTO " + tableName + " ( ";
                v = "?";
            } else {
                sql += ", ";
                v += ", ?";
            }
            sql += name;
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
            String name = column.getName();
            if (primaryColumns.contains(name)) {
                continue;
            }
            columnsList.add(column);
        }
        return columnsList;
    }

    public List<ColumnDefinition> primaryColumns() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        List<ColumnDefinition> columnsList = new ArrayList<>();
        for (String name : primaryColumns) {
            columnsList.add(column(name));
        }
        return columnsList;
    }

    public String updateStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String update = null;
        for (ColumnDefinition column : columns) {
            String name = column.getName();
            if (primaryColumns.contains(name)) {
                continue;
            }
            if (update == null) {
                update = "UPDATE " + tableName + " SET ";
            } else {
                update += ", ";
            }
            update += name + "=? ";
        }
        String where = null;
        for (String key : primaryColumns) {
            if (where == null) {
                where = " WHERE ";
            } else {
                where += " AND ";
            }
            where += key + "=?";
        }
        return update + (where != null ? where : "");
    }

    public String deleteStatement() {
        if (tableName == null || columns.isEmpty() || primaryColumns.isEmpty()) {
            return null;
        }
        String delete = "DELETE FROM " + tableName;
        String where = null;
        for (String key : primaryColumns) {
            if (where == null) {
                where = " WHERE ";
            } else {
                where += " AND ";
            }
            where += key + "=?";
        }
        return delete + (where != null ? where : "");
    }

    public int columnIndex(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            return -1;
        }
        for (int i = 0; i < columns.size() - 1; i++) {
            ColumnDefinition column = columns.get(i);
            if (columnName.equals(column.getName())) {
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
            if (column.getName().equals(columnName)) {
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
            s.append(column.getName()).append("\t\t")
                    .append(column.getType().name()).append("\t\t")
                    .append(column.getLength() > 0 ? column.getLength() + "" : " ").append("\t\t")
                    .append(column.isNotNull() ? message("NotNull") : "")
                    .append("\n");
        }
        return s.toString();
    }

    public String columnsTable() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Column"), message("Type"), message("Length"),
                message("NotNull"), message("PrimaryKey"), message("AutoGenerated"), message("ForeignKey")));
        StringTable table = new StringTable(names, tableName);
        for (ColumnDefinition column : columns) {
            List<String> row = new ArrayList<>();
            row.add(column.getName());
            row.add(column.getType().name());
            row.add(column.getLength() > 0 ? column.getLength() + "" : "");
            row.add(column.isNotNull() ? message("Yes") : "");
            row.add(column.isIsPrimaryKey() ? message("Yes") : "");
            row.add(column.isIsID() ? message("Yes") : "");
            row.add(column.isForeignKey() ? message("Yes") : "");
            table.add(row);
        }
        return StringTable.tableDiv(table);
    }

    public String html() {
        if (tableName == null || columns.isEmpty()) {
            return null;
        }
        String html = columnsTable() + "</BR><HR>" + createTableStatement().replaceAll("\n", "</BR>");
        return HtmlTools.html(tableName, html);
    }

    public D exist(D data) {
        return query(data);
    }

    public D query(D data) {
        if (data == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return readData(conn, data);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return null;
        }
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
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public List<D> query(PreparedStatement statement) {
        List<D> dataList = new ArrayList<>();
        try {
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    D data = readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return dataList;
    }

    public List<D> query(String sql) {
        List<D> dataList = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    D data = readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return dataList;
    }

    public D writeData(D data) {
        if (!valid(data)) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return writeData(conn, data);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public D writeData(Connection conn, D data) {
        if (!valid(data) || conn == null) {
            return null;
        }
        try {
            D exist = exist(data);
            if (exist != null) {
                if (idColumn != null) {
                    setId(exist, data);
                }
                return updateData(conn, data);
            } else {
                return insertData(conn, data);
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public D insertData(Connection conn, D data) {
        if (conn == null || !valid(data)) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(insertStatement())) {
            return insertData(conn, statement, data);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return null;
    }

    public D insertData(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || !valid(data)) {
            return null;
        }
        try {
            if (setInsertStatement(conn, statement, data)) {
                if (statement.executeUpdate() > 0) {
                    return data;
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(tableName + " " + e.toString());
        }
        return null;
    }

    public D updateData(Connection conn, D data) {
        if (conn == null || !valid(data)) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(updateStatement())) {
            return updateData(conn, statement, data);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public D updateData(Connection conn, PreparedStatement statement, D data) {
        if (conn == null || !valid(data)) {
            return null;
        }
        try {
            if (!setUpdateStatement(conn, statement, data)) {
                return null;
            }
            if (statement.executeUpdate() > 0) {
                return data;
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return null;
    }

    public boolean deleteData(D data) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(deleteStatement())) {
            setDeleteStatement(conn, statement, data);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public int deleteData(List<D> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        int count = 0;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(deleteStatement())) {
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
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return count;
    }

    public TableBase readDefinitionFromDB(String tableName) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return readDefinitionFromDB(conn, tableName);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            this.tableName = tableName;
            return this;
        }
    }

    public TableBase readDefinitionFromDB(Connection conn, String tableName) {
        this.tableName = tableName;
        try ( Statement statement = conn.createStatement()) {
            conn.setReadOnly(true);
            String sql = "SELECT columnname, columndatatype, columnnumber FROM SYS.SYSTABLES t, SYS.SYSCOLUMNS c "
                    + " where t.TABLEID=c.REFERENCEID AND tablename='" + tableName.toUpperCase() + "'"
                    + " order by columnnumber";
            try ( ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    ColumnDefinition column = ColumnDefinition.create()
                            .setName(resultSet.getString("columnname").toLowerCase())
                            .setIndex(resultSet.getInt("columnnumber"));
                    String type = resultSet.getString("columndatatype");
                    if (type.endsWith(" NOT NULL")) {
                        column.setNotNull(true);
                        type = type.substring(0, type.length() - " NOT NULL".length()).trim();
                    }
                    switch (type) {
                        case "DOUBLE":
                            column.setType(ColumnType.Double);
                            break;
                        case "BIGINT":
                            column.setType(ColumnType.Long);
                            break;
                        case "SMALLINT":
                            column.setType(ColumnType.Short);
                            break;
                        case "BOOLEAN":
                            column.setType(ColumnType.Boolean);
                            break;
                        case "INTEGER":
                            column.setType(ColumnType.Integer);
                            break;
                        case "FLOAT":
                            column.setType(ColumnType.Float);
                            break;
                        case "TIMESTAMP":
                            column.setType(ColumnType.Datetime);
                            break;
                        case "Date":
                            column.setType(ColumnType.Date);
                            break;
                        default:
                            if (type.startsWith("VARCHAR") || type.startsWith("CHAR")) {
                                try {
                                    column.setLength(Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")"))));
                                    column.setType(ColumnType.String);
                                } catch (Exception e) {
                                    logger.debug(type);
                                    column.setType(ColumnType.Unknown);
                                }
                            } else {
                                column.setType(ColumnType.Unknown);
                                logger.debug(type);
                            }
                    }
                    addColumn(column);
//                    logger.debug(column.getIndex() + " " + column.getName() + " " + column.getType().name() + " " + column.getLength());
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return this;
    }

    /*
        get/set
     */
    public String getTableName() {
        return tableName;
    }

    public TableBase setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public TableBase setIdColumn(String idColumn) {
        this.idColumn = idColumn;
        return this;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public TableBase setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
        return this;
    }

    public List<String> getPrimaryColumns() {
        return primaryColumns;
    }

    public TableBase setPrimaryColumns(List<String> primaryColumns) {
        this.primaryColumns = primaryColumns;
        return this;
    }

    public Era.Format getTimeFormat() {
        return timeFormat;
    }

    public TableBase setTimeFormat(Era.Format timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    public List<String> getForeignColumns() {
        return foreignColumns;
    }

    public TableBase setForeignColumns(List<String> foreignColumns) {
        this.foreignColumns = foreignColumns;
        return this;
    }

    public boolean isSupportBatchUpdate() {
        return supportBatchUpdate;
    }

    public TableBase setSupportBatchUpdate(boolean supportBatchUpdate) {
        this.supportBatchUpdate = supportBatchUpdate;
        return this;
    }

}
