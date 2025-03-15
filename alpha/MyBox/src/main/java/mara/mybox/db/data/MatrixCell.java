package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class MatrixCell extends BaseData {

    protected long dataID, columnID, rowID;
    protected double value;

    private void init() {
        dataID = -1;
        columnID = -1;
        rowID = -1;
        value = Double.NaN;
    }

    public MatrixCell() {
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
    public static MatrixCell create() {
        return new MatrixCell();
    }

    public static boolean valid(MatrixCell data) {
        return data != null && data.getColumnID() >= 0 && data.getRowID() >= 0;
    }

    public static boolean setValue(MatrixCell data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "mcdid":
                    data.setDataID(value == null ? -1 : (long) value);
                    return true;
                case "col":
                    data.setColumnID(value == null ? -1 : (long) value);
                    return true;
                case "row":
                    data.setRowID(value == null ? -1 : (long) value);
                    return true;
                case "value":
                    data.setValue(value == null ? Double.NaN : (double) value);
                    return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(MatrixCell data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "mcdid":
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
    public long getDataID() {
        return dataID;
    }

    public MatrixCell setDataID(long dataid) {
        this.dataID = dataid;
        return this;
    }

    public double getValue() {
        return value;
    }

    public MatrixCell setValue(double value) {
        this.value = value;
        return this;
    }

    public long getColumnID() {
        return columnID;
    }

    public MatrixCell setColumnID(long col) {
        this.columnID = col;
        return this;
    }

    public long getRowID() {
        return rowID;
    }

    public MatrixCell setRowID(long row) {
        this.rowID = row;
        return this;
    }

}
