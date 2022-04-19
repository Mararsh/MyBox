package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.data.Era;
import mara.mybox.data.StringTable;
import static mara.mybox.db.data.ColumnDefinition.columnType;
import static mara.mybox.db.data.ColumnDefinition.number2String;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

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
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
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
            case "color":
                return data.getColor() == null ? null : data.getColor().toString();
            case "is_primary":
                return data.isIsPrimaryKey();
            case "not_null":
                return data.isNotNull();
            case "is_auto":
                return data.isAuto();
            case "editable":
                return data.isEditable();
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
            case "time_format":
                return Era.format(data.getTimeFormat());
            case "label":
                return data.getLabel();
            case "foreign_name":
                return data.getReferName();
            case "foreign_table":
                return data.getReferTable();
            case "foreign_column":
                return data.getReferColumn();
            case "values_list":
                return null;
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
                case "color":
                    data.setColor(value == null ? null : Color.web((String) value));
                    return true;
                case "is_primary":
                    data.setIsPrimaryKey(value == null ? false : (boolean) value);
                    return true;
                case "is_auto":
                    data.setAuto(value == null ? false : (boolean) value);
                    return true;
                case "not_null":
                    data.setNotNull(value == null ? false : (boolean) value);
                    return true;
                case "editable":
                    data.setEditable(value == null ? false : (boolean) value);
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
                case "time_format":
                    data.setTimeFormat(Era.format((short) value));
                    return true;
                case "label":
                    data.setLabel(value == null ? null : (String) value);
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
                case "values_list":
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static StringTable validate(List<Data2DColumn> columns) {
        try {
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<String> colsNames = new ArrayList<>();
            List<String> tNames = new ArrayList<>();
            tNames.addAll(Arrays.asList(message("ID"), message("Name"), message("Reason")));
            StringTable colsTable = new StringTable(tNames, message("InvalidColumns"));
            for (int c = 0; c < columns.size(); c++) {
                Data2DColumn column = columns.get(c);
                if (!column.valid()) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(c + 1 + "", column.getColumnName(), message("Invalid")));
                    colsTable.add(row);
                }
                if (colsNames.contains(column.getColumnName())) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(c + 1 + "", column.getColumnName(), message("Duplicated")));
                    colsTable.add(row);
                }
                colsNames.add(column.getColumnName());
            }
            return colsTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<String> toNames(List<Data2DColumn> cols) {
        try {
            if (cols == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Data2DColumn c : cols) {
                names.add(c.getColumnName());
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<Data2DColumn> toColumns(List<String> names) {
        try {
            if (names == null) {
                return null;
            }
            List<Data2DColumn> cols = new ArrayList<>();
            int index = -1;
            for (String c : names) {
                Data2DColumn col = new Data2DColumn(c, ColumnType.String);
                col.setIndex(index--);
                cols.add(col);
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<Data2DColumn> clone(List<Data2DColumn> columns) {
        try {
            if (columns == null) {
                return null;
            }
            List<Data2DColumn> cols = new ArrayList<>();
            int index = 0;
            for (Data2DColumn c : columns) {
                Data2DColumn col = c.cloneAll();
                col.setIndex(index++);
                cols.add(col);
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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

}
