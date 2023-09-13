package mara.mybox.db.data;

import javafx.scene.paint.Color;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import static mara.mybox.db.data.ColumnDefinition.columnType;
import static mara.mybox.db.data.ColumnDefinition.number2String;
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
            Data2DColumn newData = (Data2DColumn) super.clone();
            newData.cloneFrom(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public void cloneFrom(Data2DColumn c) {
        try {
            if (c == null) {
                return;
            }
            super.cloneFrom(c);
            data2DDefinition = c.data2DDefinition;
            d2cid = c.d2cid;
            d2id = c.d2id;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public Data2DColumn copy() {
        try {
            Data2DColumn column = cloneAll();
            column.setD2cid(-1);
            column.setIndex(-1);
            return column;
        } catch (Exception e) {
            return null;
        }
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
            case "column_type":
                return columnType(data.getType());
            case "column_name":
                return data.getColumnName();
            case "index":
                return data.getIndex();
            case "length":
                return data.getLength();
            case "width":
                return data.getWidth();
            case "scale":
                return data.getScale();
            case "color":
                return data.getColor() == null ? null : data.getColor().toString();
            case "is_primary":
                return data.isIsPrimaryKey();
            case "not_null":
                return data.isNotNull();
            case "is_auto":
                return data.isAuto();
            case "invalid_as":
                return data.getInvalidAs().ordinal();
            case "editable":
                return data.isEditable();
            case "fix_year":
                return data.isFixTwoDigitYear();
            case "format":
                return data.getFormat();
            case "century":
                return data.getCentury();
            case "on_delete":
                return onDelete(data.getOnDelete());
            case "on_update":
                return onUpdate(data.getOnUpdate());
            case "default_value":
                return data.getDefaultValue();
            case "max_value":
                return number2String(data.getMaxValue());
            case "min_value":
                return number2String(data.getMinValue());
            case "foreign_name":
                return data.getReferName();
            case "foreign_table":
                return data.getReferTable();
            case "foreign_column":
                return data.getReferColumn();
            case "description":
                return data.getDescription();
        }
        return null;
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
                case "column_type":
                    data.setType(columnType((short) value));
                    return true;
                case "column_name":
                    data.setColumnName(value == null ? null : (String) value);
                    return true;
                case "index":
                    data.setIndex(value == null ? null : (int) value);
                    return true;
                case "length":
                    data.setLength(value == null ? null : (int) value);
                    return true;
                case "width":
                    data.setWidth(value == null ? null : (int) value);
                    return true;
                case "scale":
                    data.setScale(value == null ? null : (int) value);
                    return true;
                case "color":
                    data.setColor(value == null ? null : Color.web((String) value));
                    return true;
                case "is_primary":
                    data.setIsPrimaryKey(value == null ? false : (boolean) value);
                    return true;
                case "is_auto":
                    data.setAuto(value == null ? false : (boolean) value);
                    return true;
                case "invalid_as":
                    data.setInvalidAs(value == null ? InvalidAs.Skip : InvalidAs.values()[(short) value]);
                    return true;
                case "not_null":
                    data.setNotNull(value == null ? false : (boolean) value);
                    return true;
                case "editable":
                    data.setEditable(value == null ? false : (boolean) value);
                    return true;
                case "format":
                    data.setFormat(value == null ? null : (String) value);
                    return true;
                case "fix_year":
                    data.setFixTwoDigitYear(value == null ? false : (boolean) value);
                    return true;
                case "century":
                    data.setCentury(value == null ? null : (int) value);
                    return true;
                case "on_delete":
                    data.setOnDelete(onDelete((short) value));
                    return true;
                case "on_update":
                    data.setOnUpdate(onUpdate((short) value));
                    return true;
                case "default_value":
                    data.setDefaultValue(value == null ? null : (String) value);
                    return true;
                case "max_value":
                    data.setMaxValue(string2Number(data.getType(), (String) value));
                    return true;
                case "min_value":
                    data.setMinValue(string2Number(data.getType(), (String) value));
                    return true;
                case "foreign_name":
                    data.setReferName(value == null ? null : (String) value);
                    return true;
                case "foreign_table":
                    data.setReferTable(value == null ? null : (String) value);
                    return true;
                case "foreign_column":
                    data.setReferColumn(value == null ? null : (String) value);
                    return true;
                case "description":
                    data.setDescription(value == null ? null : (String) value);
                    return true;
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

    @Override
    public Data2DColumn setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }
}
