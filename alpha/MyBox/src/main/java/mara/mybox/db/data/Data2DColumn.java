package mara.mybox.db.data;

import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public class Data2DColumn extends ColumnDefinition {

    protected Data2DDefinition data2DDefinition;
    protected long d2cid, d2id;

    public final void initData2DColumn() {
        initColumnDefinition();
        d2cid = -1;
        d2id = -1;
        data2DDefinition = null;
    }

    public Data2DColumn() {
        initData2DColumn();
    }

    public Data2DColumn(String name, ColumnType type) {
        initData2DColumn();
        this.columnName = name;
        this.type = type;
    }

    public Data2DColumn(String name, ColumnType type, int width) {
        initData2DColumn();
        this.columnName = name;
        this.type = type;
        this.width = width;
    }

    public Data2DColumn(String name, ColumnType type, boolean notNull) {
        initData2DColumn();
        this.columnName = name;
        this.type = type;
        this.notNull = notNull;
    }

    public Data2DColumn(String name, ColumnType type, boolean notNull, boolean isPrimaryKey) {
        initData2DColumn();
        this.columnName = name;
        this.type = type;
        this.notNull = notNull;
        this.isPrimaryKey = isPrimaryKey;
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

    @Override
    public Data2DColumn cloneBase() {
        try {
            return (Data2DColumn) clone();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Data2DColumn cloneAll() {
        try {
            Data2DColumn newColumn = (Data2DColumn) super.clone();
            newColumn.data2DDefinition = data2DDefinition;
            newColumn.d2cid = d2cid;
            newColumn.d2id = d2id;
            return newColumn;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public Data2DColumn copy() {
        try {
            Data2DColumn newColumn = cloneAll();
            newColumn.setD2cid(-1).setD2id(-1).setIndex(-1)
                    .setReferTable(null).setReferColumn(null).setReferName(null);
            return newColumn;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public String info() {
        return Data2DColumnTools.columnInfo(this);
    }

    /*
        static methods
     */
    public static Data2DColumn create() {
        return new Data2DColumn();
    }

    public static Object getValue(Data2DColumn data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "d2cid":
                return data.getD2cid();
            case "d2id":
                return data.getD2id();
            default:
                return ColumnDefinition.getValue(data, column);
        }
    }

    public static boolean setValue(Data2DColumn data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "d2cid":
                    data.setD2cid(value == null ? -1 : (long) value);
                    return true;
                case "d2id":
                    data.setD2id(value == null ? -1 : (long) value);
                    return true;
                default:
                    return ColumnDefinition.setValue(data, column, value);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    /*
        get/set
     */
    public Data2DDefinition getData2DDefinition() {
        return data2DDefinition;
    }

    public Data2DColumn setData2DDefinition(Data2DDefinition data2DDefinition) {
        this.data2DDefinition = data2DDefinition;
        return this;
    }

    public long getD2cid() {
        return d2cid;
    }

    public Data2DColumn setD2cid(long d2cid) {
        this.d2cid = d2cid;
        return this;
    }

    public long getD2id() {
        return d2id;
    }

    public Data2DColumn setD2id(long d2id) {
        this.d2id = d2id;
        return this;
    }

    @Override
    public Data2DColumn setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public Data2DColumn setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public Data2DColumn setWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public Data2DColumn setScale(int scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public Data2DColumn setFixTwoDigitYear(boolean fixTwoDigitYear) {
        this.fixTwoDigitYear = fixTwoDigitYear;
        return this;
    }

}
