package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataCell extends BaseData {

    protected long dceid, dfid;
    protected long col, row;
    protected String value;

    private void init() {
        dceid = -1;
        dfid = -1;
        col = -1;
        row = -1;
        value = null;
    }

    public DataCell() {
        init();
    }


    /*
        static methods
     */
    public static DataCell create() {
        return new DataCell();
    }

    public static boolean valid(DataCell data) {
        return data != null && data.getCol() >= 0 && data.getRow() >= 0;
    }

    public static boolean setValue(DataCell data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dceid":
                    data.setDceid(value == null ? -1 : (long) value);
                    return true;
                case "dcdid":
                    data.setDfid(value == null ? -1 : (long) value);
                    return true;
                case "col":
                    data.setCol(value == null ? 3 : (long) value);
                    return true;
                case "row":
                    data.setRow(value == null ? 3 : (long) value);
                    return true;
                case "value":
                    data.setValue(value == null ? null : (String) value);
                    return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(DataCell data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "dceid":
                    return data.getDceid();
                case "dcdid":
                    return data.getDfid();
                case "row":
                    return data.getRow();
                case "col":
                    return data.getCol();
                case "value":
                    return data.getValue();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        get/set
     */
    public long getDceid() {
        return dceid;
    }

    public DataCell setDceid(long dceid) {
        this.dceid = dceid;
        return this;
    }

    public long getDfid() {
        return dfid;
    }

    public DataCell setDfid(long dfid) {
        this.dfid = dfid;
        return this;
    }

    public String getValue() {
        return value;
    }

    public DataCell setValue(String value) {
        this.value = value;
        return this;
    }

    public long getCol() {
        return col;
    }

    public DataCell setCol(long col) {
        this.col = col;
        return this;
    }

    public long getRow() {
        return row;
    }

    public DataCell setRow(long row) {
        this.row = row;
        return this;
    }

}
