package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class Matrix extends BaseData {

    protected String name, comments;
    protected int colsNumber, rowsNumber;
    protected short scale;
    protected Date modifyTime;

    public Matrix() {
        colsNumber = rowsNumber = 3;
        scale = 2;
        modifyTime = new Date();
    }

    /*
        static methods
     */
    public static Matrix create() {
        return new Matrix();
    }

    public static boolean valid(Matrix data) {
        return data != null && data.getName() != null
                && data.getColsNumber() > 0 && data.getRowsNumber() > 0;
    }

    public static boolean setValue(Matrix data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "mxid":
                    data.setId(value == null ? -1 : (long) value);
                    return true;
                case "name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "columns_number":
                    data.setColsNumber(value == null ? 3 : (int) value);
                    return true;
                case "rows_number":
                    data.setRowsNumber(value == null ? 3 : (int) value);
                    return true;
                case "scale":
                    data.setScale(value == null ? 2 : (short) value);
                    return true;
                case "modify_time":
                    data.setModifyTime(value == null ? new Date() : (Date) value);
                    return true;
                case "comments":
                    data.setComments(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Matrix data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "mxid":
                    return data.getId();
                case "name":
                    return data.getName();
                case "columns_number":
                    return data.getColsNumber();
                case "rows_number":
                    return data.getRowsNumber();
                case "scale":
                    return data.getScale();
                case "modify_time":
                    return data.getModifyTime();
                case "comments":
                    return data.getComments();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColsNumber() {
        return colsNumber;
    }

    public void setColsNumber(int colsNumber) {
        this.colsNumber = colsNumber;
    }

    public int getRowsNumber() {
        return rowsNumber;
    }

    public void setRowsNumber(int rowsNumber) {
        this.rowsNumber = rowsNumber;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public short getScale() {
        return scale;
    }

    public void setScale(short scale) {
        this.scale = scale;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
