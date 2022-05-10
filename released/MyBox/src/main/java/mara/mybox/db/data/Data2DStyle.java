package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class Data2DStyle extends BaseData {

    protected Data2DDefinition data2DDefinition;
    protected long d2sid, d2id, row;
    protected String colName, style;

    private void init() {
        d2sid = -1;
        d2id = -1;
        row = -1;
        colName = null;
        style = null;
    }

    public Data2DStyle() {
        init();
    }

    public Data2DStyle(long d2id, long row, String colName, String style) {
        init();
        this.d2id = d2id;
        this.row = row;
        this.colName = colName;
        this.style = style;
    }

    public Data2DStyle(String key, String style) {
        init();
        try {
            int pos = key.indexOf(",");
            row = Integer.valueOf(key.substring(0, pos));
            colName = key.substring(pos + 1);
            this.style = style;
        } catch (Exception e) {
        }
    }


    /*
        static methods
     */
    public static Data2DStyle create() {
        return new Data2DStyle();
    }

    public static boolean valid(Data2DStyle data) {
        return data != null && data.getD2id() >= 0 && data.getColName() != null
                && data.getStyle() != null;
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
                case "colName":
                    data.setColName(value == null ? null : (String) value);
                    return true;
                case "row":
                    data.setRow(value == null ? -1 : (long) value);
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
                case "colName":
                    return data.getColName();
                case "row":
                    return data.getRow();
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

    public long getRow() {
        return row;
    }

    public void setRow(long row) {
        this.row = row;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
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

    public void setData2DDefinition(Data2DDefinition data2DDefinition) {
        this.data2DDefinition = data2DDefinition;
    }

}
