package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class MatrixCell extends BaseData {

    protected long mcid, mcxid;
    protected int col, row;
    protected double value;


    /*
        static methods
     */
    public static MatrixCell create() {
        return new MatrixCell();
    }

    public static boolean valid(MatrixCell data) {
        return data != null && data.getCol() >= 0 && data.getRow() >= 0;
    }

    public static boolean setValue(MatrixCell data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "mcid":
                    data.setMcid(value == null ? -1 : (long) value);
                    return true;
                case "mcxid":
                    data.setMcxid(value == null ? -1 : (long) value);
                    return true;
                case "col":
                    data.setCol(value == null ? 3 : (int) value);
                    return true;
                case "row":
                    data.setRow(value == null ? 3 : (int) value);
                    return true;
                case "value":
                    data.setValue(value == null ? 0 : (double) value);
                    return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(MatrixCell data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "mcid":
                    return data.getMcid();
                case "mcxid":
                    return data.getMcxid();
                case "row":
                    return data.getRow();
                case "col":
                    return data.getCol();
                case "value":
                    return (double) (data.getValue());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    public long getMcid() {
        return mcid;
    }

    public void setMcid(long mcid) {
        this.mcid = mcid;
    }

    public long getMcxid() {
        return mcxid;
    }

    /*
    get/set
     */
    public MatrixCell setMcxid(long mcxid) {
        this.mcxid = mcxid;
        return this;
    }

    public double getValue() {
        return value;
    }

    public MatrixCell setValue(double value) {
        this.value = value;
        return this;
    }

    public int getCol() {
        return col;
    }

    public MatrixCell setCol(int col) {
        this.col = col;
        return this;
    }

    public int getRow() {
        return row;
    }

    public MatrixCell setRow(int row) {
        this.row = row;
        return this;
    }

}
