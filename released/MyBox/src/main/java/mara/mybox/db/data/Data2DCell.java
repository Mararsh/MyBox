package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class Data2DCell extends BaseData {

    protected long cellID, dataID;
    protected long columnID, rowID;
    protected String value;

    private void init() {
        cellID = -1;
        dataID = -1;
        columnID = -1;
        rowID = -1;
        value = null;
    }

    public Data2DCell() {
        init();
    }

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    /*
        static methods
     */
    public static Data2DCell create() {
        return new Data2DCell();
    }

    public static boolean valid(Data2DCell data) {
        return data != null && data.getColumnID() >= 0 && data.getRowID() >= 0;
    }

    public static boolean setValue(Data2DCell data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dceid":
                    data.setCellID(value == null ? -1 : (long) value);
                    return true;
                case "dcdid":
                    data.setDataID(value == null ? -1 : (long) value);
                    return true;
                case "col":
                    data.setColumnID(value == null ? 3 : (long) value);
                    return true;
                case "row":
                    data.setRowID(value == null ? 3 : (long) value);
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
                    return data.getCellID();
                case "dcdid":
                    return data.getDataID();
                case "row":
                    return data.getRowID();
                case "col":
                    return data.getColumnID();
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
    public long getCellID() {
        return cellID;
    }

    public Data2DCell setCellID(long dceid) {
        this.cellID = dceid;
        return this;
    }

    public long getDataID() {
        return dataID;
    }

    public Data2DCell setDataID(long dataid) {
        this.dataID = dataid;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Data2DCell setValue(String value) {
        this.value = value;
        return this;
    }

    public long getColumnID() {
        return columnID;
    }

    public Data2DCell setColumnID(long col) {
        this.columnID = col;
        return this;
    }

    public long getRowID() {
        return rowID;
    }

    public Data2DCell setRowID(long row) {
        this.rowID = row;
        return this;
    }

}
