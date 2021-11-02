package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class Data2Column extends ColumnDefinition {

    protected Data2DDefinition data2DDefinition;
    protected long d2cid, d2id;

    public final void initData2Column() {
        initColumnDefinition();
        d2cid = -1;
        d2id = -1;
    }

    public Data2Column() {
        initData2Column();
    }

    public Data2Column(String name, ColumnType type) {
        initData2Column();
        this.name = name;
        this.type = type;
    }

    public Data2Column(String name, ColumnType type, boolean notNull) {
        initData2Column();
        this.name = name;
        this.type = type;
        this.notNull = notNull;
    }

    public Data2Column(String name, ColumnType type, boolean notNull, boolean isPrimaryKey) {
        initData2Column();
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.isPrimaryKey = isPrimaryKey;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            Data2Column newColumn = (Data2Column) super.clone();
            newColumn.setD2cid(-1);
            return newColumn;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public Data2Column cloneBase() {
        try {
            return (Data2Column) clone();
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static methods
     */
    public static Data2Column create() {
        return new Data2Column();
    }

    public static Object getValue(Data2Column data, String column) {
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
                return data.getName();
            case "index":
                return data.getIndex();
            case "length":
                return data.getLength();
            case "width":
                return data.getWidth();
            case "is_primary":
                return data.isIsPrimaryKey();
            case "not_null":
                return data.isNotNull();
            case "is_id":
                return data.isIsID();
            case "editable":
                return data.isEditable();
            case "max_value":
                return number2String(data.getMaxValue());
            case "min_value":
                return number2String(data.getMinValue());
            case "time_format":
                return Era.format(data.getTimeFormat());
            case "label":
                return data.getLabel();
            case "foreign_name":
                return data.getForeignName();
            case "foreign_table":
                return data.getForeignTable();
            case "foreign_column":
                return data.getForeignColumn();
            case "values_list":
                return null;
        }
        return null;
    }

    public static boolean setValue(Data2Column data, String column, Object value) {
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
                    data.setName(value == null ? null : (String) value);
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
                case "is_primary":
                    data.setIsPrimaryKey(value == null ? false : (boolean) value);
                    return true;
                case "not_null":
                    data.setNotNull(value == null ? false : (boolean) value);
                    return true;
                case "is_id":
                    data.setIsID(value == null ? false : (boolean) value);
                    return true;
                case "editable":
                    data.setEditable(value == null ? false : (boolean) value);
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
                    data.setForeignName(value == null ? null : (String) value);
                    return true;
                case "foreign_table":
                    data.setForeignTable(value == null ? null : (String) value);
                    return true;
                case "foreign_column":
                    data.setForeignColumn(value == null ? null : (String) value);
                    return true;
                case "values_list":
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static StringTable validate(List<Data2Column> columns) {
        try {
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<String> colsNames = new ArrayList<>();
            List<String> tNames = new ArrayList<>();
            tNames.addAll(Arrays.asList(message("ID"), message("Name"), message("Reason")));
            StringTable colsTable = new StringTable(tNames, message("InvalidColumns"));
            for (int c = 0; c < columns.size(); c++) {
                Data2Column column = columns.get(c);
                if (!column.valid()) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(c + 1 + "", column.getName(), message("Invalid")));
                    colsTable.add(row);
                }
                if (colsNames.contains(column.getName())) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(c + 1 + "", column.getName(), message("Duplicated")));
                    colsTable.add(row);
                }
                colsNames.add(column.getName());
            }
            return colsTable;
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

    public Data2Column setData2DDefinition(Data2DDefinition data2DDefinition) {
        this.data2DDefinition = data2DDefinition;
        return this;
    }

    public long getD2cid() {
        return d2cid;
    }

    public Data2Column setD2cid(long d2cid) {
        this.d2cid = d2cid;
        return this;
    }

    public long getD2id() {
        return d2id;
    }

    public Data2Column setD2id(long d2id) {
        this.d2id = d2id;
        return this;
    }

}
