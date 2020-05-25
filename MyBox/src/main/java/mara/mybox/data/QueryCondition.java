package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2020-5-14
 * @License Apache License Version 2.0
 */
public class QueryCondition {

    private long qcid, time;
    private int operation, top;
    private String dataName, title, prefix, where, order, fetch;
    private DataOperation dataOperation;

    public enum DataOperation {
        QueryData, DisplayChart, ClearData, ExportData, Unknown
    }

    public QueryCondition() {
        qcid = -1;
        operation = top = -1;
        dataName = null;
        title = null;
        prefix = null;
        where = null;
        order = null;
        fetch = null;
        time = -1;
    }

    public boolean isValid() {
        return operation > 0 && operation < 5
                && prefix != null && title != null;
    }

    public String statement() {
        if (prefix == null) {
            return null;
        }
        String s = prefix;
        s += where == null || where.trim().isBlank() ? "" : " WHERE " + where;
        s += order == null || order.trim().isBlank() ? "" : " ORDER BY " + order;
        s += fetch == null || fetch.trim().isBlank() ? "" : " " + fetch;
        return s;
    }

    /*
        static methods
     */
    public static QueryCondition create() {
        return new QueryCondition();
    }

    public static DataOperation dataOperation(int operation) {
        switch (operation) {
            case 1:
                return DataOperation.QueryData;
            case 2:
                return DataOperation.DisplayChart;
            case 3:
                return DataOperation.ClearData;
            case 4:
                return DataOperation.ExportData;
            default:
                return DataOperation.Unknown;
        }
    }

    public static int operation(DataOperation dataOperation) {
        if (dataOperation == null) {
            return -1;
        }
        switch (dataOperation) {
            case QueryData:
                return 1;
            case DisplayChart:
                return 2;
            case ClearData:
                return 3;
            case ExportData:
                return 4;
            default:
                return -1;
        }
    }

    /*
        get/set
     */
    public long getQcid() {
        return qcid;
    }

    public QueryCondition setQcid(long qcid) {
        this.qcid = qcid;
        return this;
    }

    public String getDataName() {
        return dataName;
    }

    public QueryCondition setDataName(String dataName) {
        this.dataName = dataName;
        return this;
    }

    public int getOperation() {
        return operation;
    }

    public QueryCondition setOperation(int operation) {
        this.operation = operation;
        this.dataOperation = dataOperation(operation);
        return this;
    }

    public int getTop() {
        return top;
    }

    public QueryCondition setTop(int top) {
        this.top = top;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public QueryCondition setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getWhere() {
        return where;
    }

    public QueryCondition setWhere(String where) {
        this.where = where;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public QueryCondition setOrder(String order) {
        this.order = order;
        return this;
    }

    public String getFetch() {
        return fetch;
    }

    public QueryCondition setFetch(String fetch) {
        this.fetch = fetch;
        return this;
    }

    public DataOperation getDataOperation() {
        return dataOperation;
    }

    public QueryCondition setDataOperation(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
        this.operation = operation(dataOperation);
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public QueryCondition setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public long getTime() {
        return time;
    }

    public QueryCondition setTime(long time) {
        this.time = time;
        return this;
    }

}
