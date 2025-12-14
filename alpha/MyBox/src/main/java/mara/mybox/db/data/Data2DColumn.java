package mara.mybox.db.data;

import javafx.scene.control.CheckBox;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public class Data2DColumn extends ColumnDefinition {

    protected Data2DDefinition data2DDefinition;
    protected long columnID, dataID;

    public final void initData2DColumn() {
        initColumnDefinition();
        columnID = -1;
        dataID = -1;
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
            newColumn.columnID = columnID;
            newColumn.dataID = dataID;
            return newColumn;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public Data2DColumn copy() {
        try {
            Data2DColumn newColumn = cloneAll();
            newColumn.setColumnID(-1).setDataID(-1).setIndex(-1)
                    .setReferTable(null).setReferColumn(null).setReferName(null);
            return newColumn;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    @Override
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
                return data.getColumnID();
            case "d2id":
                return data.getDataID();
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
                    data.setColumnID(value == null ? -1 : (long) value);
                    return true;
                case "d2id":
                    data.setDataID(value == null ? -1 : (long) value);
                    return true;
                default:
                    return ColumnDefinition.setValue(data, column, value);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static boolean matchCheckBox(CheckBox cb, String name) {
        if (cb == null || name == null) {
            return false;
        }
        if (name.equals(cb.getText())) {
            return true;
        }
        try {
            Data2DColumn col = (Data2DColumn) cb.getUserData();
            return name.equals(col.getColumnName())
                    || name.equals(col.getLabel());
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCheckBoxColumnName(CheckBox cb) {
        if (cb == null) {
            return null;
        }
        try {
            Data2DColumn col = (Data2DColumn) cb.getUserData();
            return col.getColumnName();
        } catch (Exception e) {
        }
        return cb.getText();
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

    public long getColumnID() {
        return columnID;
    }

    public Data2DColumn setColumnID(long d2cid) {
        this.columnID = d2cid;
        return this;
    }

    public long getDataID() {
        return dataID;
    }

    public Data2DColumn setDataID(long d2id) {
        this.dataID = d2id;
        return this;
    }

    @Override
    public Data2DColumn setLabel(String label) {
        this.label = label;
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
