package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class Data2DStyle extends BaseData {

    protected Data2DDefinition data2DDefinition;
    protected long d2sid, d2id, rowStart, rowEnd;
    protected String colName, moreConditions, style;

    private void init() {
        d2sid = -1;
        d2id = -1;
        rowStart = -1;
        rowEnd = -1;
        colName = null;
        moreConditions = null;
        style = null;
    }

    public Data2DStyle() {
        init();
    }

    /*
        static methods
     */
    public static Data2DStyle create() {
        return new Data2DStyle();
    }

    public static boolean valid(Data2DStyle data) {
        return data != null && data.getD2id() >= 0 && data.getStyle() != null;
    }

    public static boolean setValue(Data2DStyle data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "d2sid":
                    data.setD2sid(value == null ? -1 : (long) value);
                    return true;
                case "d2id":
                    data.setD2id(value == null ? -1 : (long) value);
                    return true;
                case "rowStart":
                    data.setRowStart(value == null ? -1 : (long) value);
                    return true;
                case "rowEnd":
                    data.setRowEnd(value == null ? -1 : (long) value);
                    return true;
                case "colName":
                    data.setColName(value == null ? null : (String) value);
                    return true;
                case "moreConditions":
                    data.setMoreConditions(value == null ? null : (String) value);
                    return true;
                case "style":
                    data.setStyle(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Data2DStyle data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "d2sid":
                    return data.getD2sid();
                case "d2id":
                    return data.getD2id();
                case "rowStart":
                    return data.getRowStart();
                case "rowEnd":
                    return data.getRowEnd();
                case "colName":
                    return data.getColName();
                case "moreConditions":
                    return data.getMoreConditions();
                case "style":
                    return data.getStyle();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        get/set
     */
    public long getD2sid() {
        return d2sid;
    }

    public Data2DStyle setD2sid(long d2sid) {
        this.d2sid = d2sid;
        return this;
    }

    public long getD2id() {
        return d2id;
    }

    public Data2DStyle setD2id(long d2id) {
        this.d2id = d2id;
        return this;
    }

    public long getRowStart() {
        return rowStart;
    }

    public Data2DStyle setRowStart(long rowStart) {
        this.rowStart = rowStart;
        return this;
    }

    public long getRowEnd() {
        return rowEnd;
    }

    public Data2DStyle setRowEnd(long rowEnd) {
        this.rowEnd = rowEnd;
        return this;
    }

    public String getColName() {
        return colName;
    }

    public Data2DStyle setColName(String colName) {
        this.colName = colName;
        return this;
    }

    public String getMoreConditions() {
        return moreConditions;
    }

    public Data2DStyle setMoreConditions(String moreConditions) {
        this.moreConditions = moreConditions;
        return this;
    }

    public String getStyle() {
        return style;
    }

    public Data2DStyle setStyle(String style) {
        this.style = style;
        return this;
    }

    public Data2DDefinition getData2DDefinition() {
        return data2DDefinition;
    }

    public Data2DStyle setData2DDefinition(Data2DDefinition data2DDefinition) {
        this.data2DDefinition = data2DDefinition;
        return this;
    }

}
