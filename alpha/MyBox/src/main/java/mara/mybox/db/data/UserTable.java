package mara.mybox.db.data;

import mara.mybox.data.Era;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;


/**
 * @Author Mara
 * @CreateDate 2020-12-23
 * @License Apache License Version 2.0
 */
public class UserTable extends BaseData {

    protected String tableName, columnName, defaultValue;
    protected ColumnDefinition.ColumnType columnType;
    protected int length;
    protected boolean isPrimaryKey, notNull, isID;
    protected Era.Format timeFormat;
    protected double maxValue, minValue;
    protected String[] values;

    public UserTable() {
    }

    /*
        static methods
     */
    public static UserTable create() {
        return new UserTable();
    }

    public static boolean valid(UserTable data) {
        return data != null && data.getTableName() != null
                && data.getColumnName() != null && data.getColumnType() != null;
    }

    public static boolean setValue(UserTable data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "table_name":
                    data.setTableName(value == null ? null : (String) value);
                    return true;
                case "column_name":
                    data.setColumnName(value == null ? null : (String) value);
                    return true;
                case "is_primary_key":
                    data.setIsPrimaryKey(value == null ? false : (boolean) value);
                    return true;
                case "not_null":
                    data.setNotNull(value == null ? false : (boolean) value);
                    return true;
                case "is_id":
                    data.setIsID(value == null ? false : (boolean) value);
                    return true;
                case "column_type":
                    data.setColumnType(value == null ? ColumnType.String : ColumnDefinition.columnType((short) value));
                    return true;
                case "time_format":
                    data.setTimeFormat(value == null ? Era.Format.Datetime : Era.format((short) value));
                    return true;
                case "length":
                    data.setLength(value == null ? 0 : (int) value);
                    return true;
                case "default_value":
                    data.setDefaultValue(value == null ? null : (String) value);
                    return true;
                case "max_value":
                    data.setMaxValue(value == null ? AppValues.InvalidDouble : (double) value);
                    return true;
                case "min_value":
                    data.setMinValue(value == null ? AppValues.InvalidDouble : (double) value);
                    return true;
                case "values":
                    data.setValues(value == null ? null : ((String) value).split(","));
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(UserTable data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "table_name":
                    return data.getTableName();
                case "column_name":
                    return data.getColumnName();
                case "is_primary_key":
                    return data.isPrimaryKey;
                case "not_null":
                    return data.isNotNull();
                case "is_id":
                    return data.isIsID();
                case "column_type":
                    return ColumnDefinition.columnType(data.getColumnType());
                case "time_format":
                    return Era.format(data.getTimeFormat());
                case "length":
                    return data.getLength();
                case "default_value":
                    return data.getDefaultValue();
                case "max_value":
                    return data.getMaxValue();
                case "min_value":
                    return data.getMinValue();
                case "values":
                    if (data.getValues() != null) {
                        String s = null;
                        for (String v : data.getValues()) {
                            if (s == null) {
                                s = v;
                            } else {
                                s += "," + v;
                            }
                        }
                        return s;
                    } else {
                        return null;
                    }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        get/set
     */
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ColumnDefinition.ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnDefinition.ColumnType columnType) {
        this.columnType = columnType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isIsID() {
        return isID;
    }

    public void setIsID(boolean isID) {
        this.isID = isID;
    }

    public Era.Format getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(Era.Format timeFormat) {
        this.timeFormat = timeFormat;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

}
