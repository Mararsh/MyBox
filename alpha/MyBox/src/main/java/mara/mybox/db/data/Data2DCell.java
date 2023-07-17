package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class Data2DCell extends BaseData {

    protected long dceid, d2did;
    protected long col, row;
    protected String value;

    private void init() {
        dceid = -1;
        d2did = -1;
        col = -1;
        row = -1;
        value = null;
    }

    public Data2DCell() {
        init();
    }


    /*
        static methods
     */
    public static Data2DCell create() {
        return new Data2DCell();
    }

    public static boolean valid(Data2DCell data) {
        return data != null && data.getCol() >= 0 && data.getRow() >= 0;
    }

    public static boolean setValue(Data2DCell data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dceid":
                    data.setDceid(value == null ? -1 : (long) value);
                    return true;
                case "dcdid":
                    data.setD2did(value == null ? -1 : (long) value);
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
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(Data2DCell data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "dceid":
                    return data.getDceid();
                case "dcdid":
                    return data.getD2did();
                case "row":
                    return data.getRow();
                case "col":
                    return data.getCol();
                case "value":
                    return data.getValue();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return null;
    }

    /*
        get/set
     */
    public long getDceid() {
        return dceid;
    }

    public Data2DCell setDceid(long dceid) {
        this.dceid = dceid;
        return this;
    }

    public long getD2did() {
        return d2did;
    }

    public Data2DCell setD2did(long d2did) {
        this.d2did = d2did;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Data2DCell setValue(String value) {
        this.value = value;
        return this;
    }

    public long getCol() {
        return col;
    }

    public Data2DCell setCol(long col) {
        this.col = col;
        return this;
    }

    public long getRow() {
        return row;
    }

    public Data2DCell setRow(long row) {
        this.row = row;
        return this;
    }

}
