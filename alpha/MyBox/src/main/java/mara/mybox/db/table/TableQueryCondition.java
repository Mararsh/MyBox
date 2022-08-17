package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.QueryCondition.DataOperation;
import mara.mybox.db.data.StringValue;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2020-5-14
 * @License Apache License Version 2.0
 */
public class TableQueryCondition extends BaseTable<StringValue> {
    
    public TableQueryCondition() {
        tableName = "Query_Condition";
        defineColumns();
    }
    
    public TableQueryCondition(boolean defineColumns) {
        tableName = "Query_Condition";
        if (defineColumns) {
            defineColumns();
        }
    }
    
    public final TableQueryCondition defineColumns() {
        addColumn(new ColumnDefinition("qcid", ColumnDefinition.ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("data_name", ColumnDefinition.ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("operation", ColumnDefinition.ColumnType.Short, true));  // 1: query  2:chart  3:export  4:clear
        addColumn(new ColumnDefinition("title", ColumnDefinition.ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("prefix", ColumnDefinition.ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("qwhere", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("qorder", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("qfetch", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("top", ColumnDefinition.ColumnType.Integer, true));
        addColumn(new ColumnDefinition("time", ColumnDefinition.ColumnType.Datetime, true));
        return this;
    }
    
    public static final String QCidQeury
            = "SELECT * FROM Query_Condition WHERE qcid=?";
    
    public static final String OperationQeury
            = " SELECT * FROM Query_Condition WHERE data_name=? AND operation=? ORDER BY time DESC";
    
    public static final String Insert
            = "INSERT INTO Query_Condition "
            + " ( data_name, operation, title, prefix, qwhere, qorder, qfetch, top, time )"
            + "VALUES(?,?,?,?,?,?,?,?,?)";
    
    public static final String Update
            = "UPDATE Query_Condition SET "
            + " data_name=?, operation=?, title=?, prefix=?, qwhere=?, qorder=?, qfetch=?, top=?, time=?"
            + " WHERE qcid=?";
    
    public static final String Delete
            = "DELETE FROM Query_Condition WHERE qcid=?";
    
    public static List<QueryCondition> readList(String dataName, DataOperation dataOperation) {
        return readList(dataName, dataOperation, 0);
    }
    
    public static List<QueryCondition> readList(String dataName,
            DataOperation dataOperation, int max) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, dataName, dataOperation, max);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return new ArrayList();
        }
    }
    
    public static List<QueryCondition> read(Connection conn,
            String dataName, DataOperation dataOperation, int max) {
        List<QueryCondition> conditions = new ArrayList();
        int operation = QueryCondition.operation(dataOperation);
        if (dataName == null || conn == null || operation <= 0) {
            return conditions;
        }
        try ( PreparedStatement statement = conn.prepareStatement(OperationQeury)) {
            statement.setMaxRows(max);
            statement.setString(1, dataName);
            statement.setShort(2, (short) operation);
            conn.setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    QueryCondition condition = read(results);
                    if (condition != null) {
                        conditions.add(condition);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            
        }
        return conditions;
    }
    
    public static QueryCondition read(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            QueryCondition condition = new QueryCondition();
            condition.setQcid(results.getLong("qcid"));
            condition.setDataName(results.getString("data_name"));
            condition.setOperation(results.getShort("operation"));
            condition.setTitle(results.getString("title"));
            condition.setPrefix(results.getString("prefix"));
            condition.setWhere(results.getString("qwhere"));
            condition.setOrder(results.getString("qorder"));
            condition.setFetch(results.getString("qfetch"));
            condition.setTop(results.getShort("top"));
            condition.setTime(results.getTimestamp("time").getTime());
            
            return condition;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    public static QueryCondition read(long qcid) {
        if (qcid <= 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, qcid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }
    
    public static QueryCondition read(Connection conn, long qcid) {
        if (conn == null || qcid < 0) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QCidQeury)) {
            return read(statement, qcid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }
    
    public static QueryCondition read(PreparedStatement statement, long qcid) {
        if (statement == null || qcid < 0) {
            return null;
        }
        try {
            statement.setMaxRows(1);
            statement.setLong(1, qcid);
            statement.getConnection().setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return read(results);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }
    
    public static QueryCondition read(QueryCondition queryCondition) {
        if (queryCondition == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, queryCondition);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }
    
    public static QueryCondition read(Connection conn, QueryCondition queryCondition) {
        if (queryCondition == null) {
            return null;
        }
        try ( Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            String sql = "SELECT * FROM Query_Condition WHERE "
                    + "data_name='" + DerbyBase.stringValue(queryCondition.getDataName()) + "' AND "
                    + "operation=" + queryCondition.getOperation() + " AND "
                    + "prefix='" + DerbyBase.stringValue(queryCondition.getPrefix()) + "' AND "
                    + "top=" + queryCondition.getTop() + " AND "
                    + (queryCondition.getWhere() == null ? " qwhere IS NULL " : " qwhere='" + DerbyBase.stringValue(queryCondition.getWhere()) + "'") + " AND "
                    + (queryCondition.getOrder() == null ? " qorder IS NULL " : " qorder='" + DerbyBase.stringValue(queryCondition.getOrder()) + "'") + " AND "
                    + (queryCondition.getFetch() == null ? " qfetch IS NULL " : " qfetch='" + DerbyBase.stringValue(queryCondition.getFetch()) + "'");
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    return read(results);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }
    
    public static boolean write(QueryCondition condition, boolean checkEqual) {
        if (condition == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return write(conn, condition, checkEqual);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean write(Connection conn, QueryCondition condition, boolean checkEqual) {
        if (conn == null || condition == null || !condition.isValid()) {
            return false;
        }
        try {
            QueryCondition exist = null;
            if (condition.getQcid() > 0) {
                exist = read(conn, condition.getQcid());
            } else if (checkEqual) {
                exist = read(conn, condition);
                if (exist != null) {
                    condition.setQcid(exist.getQcid());
                }
            }
            if (exist != null) {
                update(conn, condition);
            } else {
                insert(conn, condition);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean write(List<QueryCondition> conditions, boolean checkEqual) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
            
        }
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement idQuery = conn.prepareStatement(QCidQeury);
                 PreparedStatement insert = conn.prepareStatement(Insert);
                 PreparedStatement update = conn.prepareStatement(Update)) {
            conn.setAutoCommit(false);
            for (QueryCondition condition : conditions) {
                QueryCondition exist = null;
                if (condition.getQcid() > 0) {
                    exist = read(idQuery, condition.getQcid());
                } else if (checkEqual) {
                    exist = read(conn, condition);
                    if (exist != null) {
                        condition.setQcid(exist.getQcid());
                    }
                }
                if (exist != null) {
                    update(update, condition);
                } else {
                    insert(insert, condition);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean insert(QueryCondition condition) {
        if (condition == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return insert(conn, condition);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean insert(Connection conn, QueryCondition condition) {
        if (conn == null || condition == null || !condition.isValid()) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Insert)) {
            return insert(statement, condition);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean insert(PreparedStatement statement, QueryCondition condition) {
        if (statement == null || condition == null || !condition.isValid()) {
            return false;
        }
        try {
            statement.setString(1, condition.getDataName());
            statement.setShort(2, (short) condition.getOperation());
            statement.setString(3, condition.getTitle());
            statement.setString(4, condition.getPrefix());
            statement.setString(5, condition.getWhere());
            statement.setString(6, condition.getOrder());
            statement.setString(7, condition.getFetch());
            statement.setInt(8, condition.getTop());
            statement.setString(9, DateTools.datetimeToString(new Date()));
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean update(QueryCondition condition) {
        if (condition == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return update(conn, condition);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean update(Connection conn, QueryCondition condition) {
        if (conn == null || condition == null
                || condition.getQcid() <= 0 || !condition.isValid()) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Update)) {
            return update(statement, condition);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean update(PreparedStatement statement, QueryCondition condition) {
        if (statement == null || condition == null
                || condition.getQcid() <= 0 || !condition.isValid()) {
            return false;
        }
        try {
            statement.setString(1, condition.getDataName());
            statement.setShort(2, (short) condition.getOperation());
            statement.setString(3, condition.getTitle());
            statement.setString(4, condition.getPrefix());
            statement.setString(5, condition.getWhere());
            statement.setString(6, condition.getOrder());
            statement.setString(7, condition.getFetch());
            statement.setInt(8, condition.getTop());
            statement.setString(9, DateTools.datetimeToString(new Date()));
            statement.setLong(10, condition.getQcid());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public static boolean delete(QueryCondition condition) {
        if (condition == null || condition.getQcid() <= 0) {
            return false;
        }
        return delete(condition.getQcid());
    }
    
    public static boolean delete(long qcid) {
        if (qcid <= 0) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, qcid);
        } catch (Exception e) {
            MyBoxLog.error(e);
            
            return false;
        }
    }
    
    public static boolean delete(Connection conn, long qcid) {
        if (conn == null || qcid <= 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Delete)) {
            statement.setLong(1, qcid);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            
            return false;
        }
    }
    
    public static boolean delete(List<QueryCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(Delete)) {
                for (QueryCondition condition : conditions) {
                    statement.setLong(1, condition.getQcid());
                    statement.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            
            return false;
        }
    }
    
}
