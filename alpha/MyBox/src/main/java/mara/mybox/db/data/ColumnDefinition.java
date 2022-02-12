package mara.mybox.db.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.paint.Color;
import mara.mybox.data.Era;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.IntTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public class ColumnDefinition extends BaseData {

    protected String name, label, foreignName, foreignTable, foreignColumn;
    protected ColumnType type;
    protected int index, length, width;
    protected Color color;
    protected boolean isPrimaryKey, notNull, isID, editable;
    protected OnDelete onDelete;
    protected OnUpdate onUpdate;
    protected Era.Format timeFormat;
    protected Object defaultValue, value;
    protected Number maxValue, minValue;
    protected Map<Object, String> values;  // value, displayString

    public static enum ColumnType {
        String, Boolean, Text,
        Color, // rgba
        File, Image, // string of the path
        Double, Float, Long, Integer, Short,
        Datetime, Date, Era, // Looks Derby does not support date of BC(before Christ)
        Clob, Blob, Unknown
    }

    public static enum OnDelete {
        NoAction, Restrict, Cascade, SetNull
    }

    public static enum OnUpdate {
        NoAction, Restrict
    }

    public final void initColumnDefinition() {
        type = ColumnType.String;
        index = -1;
        isPrimaryKey = notNull = isID = false;
        editable = true;
        length = StringMaxLength;
        width = 100; // px
        onDelete = OnDelete.Restrict;
        onUpdate = OnUpdate.Restrict;
        timeFormat = Era.Format.Datetime;
        maxValue = null;
        minValue = null;
        color = null;
    }

    public ColumnDefinition() {
        initColumnDefinition();
    }

    public ColumnDefinition(String name, ColumnType type) {
        initColumnDefinition();
        this.name = name;
        this.type = type;
    }

    public ColumnDefinition(String name, ColumnType type, boolean notNull) {
        initColumnDefinition();
        this.name = name;
        this.type = type;
        this.notNull = notNull;
    }

    public ColumnDefinition(String name, ColumnType type, boolean notNull, boolean isPrimaryKey) {
        initColumnDefinition();
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.isPrimaryKey = isPrimaryKey;
    }

    public ColumnDefinition cloneAll() {
        try {
            ColumnDefinition newData = (ColumnDefinition) super.clone();
            newData.cloneFrom(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public void cloneFrom(ColumnDefinition c) {
        try {
            if (c == null) {
                return;
            }
            name = c.name;
            label = c.label;
            foreignName = c.foreignName;
            foreignTable = c.foreignTable;
            foreignColumn = c.foreignColumn;
            type = c.type;
            index = c.index;
            length = c.length;
            width = c.width;
            color = c.color;
            isPrimaryKey = c.isPrimaryKey;
            notNull = c.notNull;
            isID = c.isID;
            editable = c.editable;
            onDelete = c.onDelete;
            onUpdate = c.onUpdate;
            timeFormat = c.timeFormat;
            defaultValue = c.defaultValue;
            value = c.value;
            maxValue = c.maxValue;
            minValue = c.minValue;
            values = c.values;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public boolean isForeignKey() {
        return foreignTable != null && foreignColumn != null;
    }

    public String foreignText() {
        if (!isForeignKey()) {
            return null;
        }
        String sql = (foreignName != null && !foreignName.isBlank() ? "CONSTRAINT  " + foreignName + " " : "")
                + "FOREIGN KEY (" + name + ") REFERENCES " + foreignTable + " (" + foreignColumn + ") ON DELETE ";
        switch (onDelete) {
            case NoAction:
                sql += "NO ACTION";
                break;
            case Restrict:
                sql += "RESTRICT";
                break;
            case SetNull:
                sql += "SET NULL";
                break;
            default:
                sql += "CASCADE";
                break;
        }
        sql += " ON UPDATE ";
        switch (onUpdate) {
            case NoAction:
                sql += "NO ACTION";
                break;
            default:
                sql += "RESTRICT";
                break;
        }
        return sql;
    }

    public boolean valid() {
        return valid(this);
    }

    public boolean validValue(String value) {
        try {
            if (value == null || value.isBlank()) {
                return !notNull;
            }
            switch (type) {
                case String:
                case Text:
                case File:
                case Image:
                    return length <= 0 || value.length() <= length;
                case Color:
                    if (length > 0 && value.length() > length) {
                        return false;
                    }
                    Color.web(value);
                    return true;
                case Double:
                    Double.parseDouble(value.replaceAll(",", ""));
                    return true;
                case Float:
                    Float.parseFloat(value.replaceAll(",", ""));
                    return true;
                case Long:
                case Era:
                    Long.parseLong(value);
                    return true;
                case Integer:
                    Integer.parseInt(value.replaceAll(",", ""));
                    return true;
                case Boolean:
                    String v = value.toLowerCase();
                    return "1".equals(v) || "0".equals(v)
                            || "true".equals(v) || "false".equals(v)
                            || "yes".equals(v) || "no".equals(v)
                            || Languages.message("true").equals(v) || Languages.message("false").equals(v)
                            || Languages.message("yes").equals(v) || Languages.message("no").equals(v);
                case Short:
                    Short.parseShort(value);
                    return true;
                case Datetime:
                    return DateTools.stringToDatetime(value) != null;
                default:
                    return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean validNotNull(String value) {
        return !notNull || (value != null && !value.isEmpty());
    }

    public int compare(String value1, String value2) {
        try {
            if (value1 == null) {
                if (value2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (value2 == null) {
                return 1;
            }
            switch (type) {
                case Double:
                    double d1 = Double.parseDouble(value1);
                    double d2 = Double.parseDouble(value2);
                    if (d1 == d2) {
                        return 0;
                    } else if (d1 > d2) {
                        return 1;
                    } else {
                        return -1;
                    }
                case Float:
                    float f1 = Float.parseFloat(value1);
                    float f2 = Float.parseFloat(value2);
                    if (f1 == f2) {
                        return 0;
                    } else if (f1 > f2) {
                        return 1;
                    } else {
                        return -1;
                    }
                case Long:
                case Era:
                    long l1 = Long.parseLong(value1);
                    long l2 = Long.parseLong(value2);
                    if (l1 == l2) {
                        return 0;
                    } else if (l1 > l2) {
                        return 1;
                    } else {
                        return -1;
                    }
                case Integer:
                    int i1 = Integer.parseInt(value1);
                    int i2 = Integer.parseInt(value2);
                    if (i1 == i2) {
                        return 0;
                    } else if (i1 > i2) {
                        return 1;
                    } else {
                        return -1;
                    }
                case Short:
                    short s1 = Short.parseShort(value1);
                    short s2 = Short.parseShort(value2);
                    if (s1 == s2) {
                        return 0;
                    } else if (s1 > s2) {
                        return 1;
                    } else {
                        return -1;
                    }
                default:
                    return StringTools.compare(value1, value2);
            }
        } catch (Exception e) {
        }
        return 1;
    }

    public boolean isNumberType() {
        return type == ColumnType.Double || type == ColumnType.Float
                || type == ColumnType.Integer || type == ColumnType.Long || type == ColumnType.Short;
    }

    // works on java 17 while not work on java 16
//    public String random(Random random, int maxRandom, short scale) {
//        if (random == null) {
//            random = new Random();
//        }
//        switch (type) {
//            case Double:
//                return DoubleTools.format(DoubleTools.random(random, maxRandom), scale);
//            case Float:
//                return FloatTools.format(random.nextFloat(maxRandom), scale);
//            case Integer:
//                return StringTools.format(random.nextInt(maxRandom));
//            case Long:
//                return StringTools.format(random.nextLong(maxRandom));
//            case Short:
//                return StringTools.format((short) random.nextInt(maxRandom));
//            default:
//                return (char) ('a' + random.nextInt(25)) + "";
//        }
//    }
    // works on java 16
    public String random(Random random, int maxRandom, short scale, boolean nonNegative) {
        if (random == null) {
            random = new Random();
        }
        switch (type) {
            case Double:
                return DoubleTools.format(DoubleTools.random(random, maxRandom, nonNegative), scale);
            case Float:
                return FloatTools.format(FloatTools.random(random, maxRandom, nonNegative), scale);
            case Integer:
                return StringTools.format(IntTools.random(random, maxRandom, nonNegative));
            case Long:
                return StringTools.format((long) FloatTools.random(random, maxRandom, nonNegative));
            case Short:
                return StringTools.format((short) IntTools.random(random, maxRandom, nonNegative));
            case Boolean:
                return random.nextInt(2) + "";
            case Datetime:
            case Date:
            case Era:
                return DateTools.randomTimeString(random);
            default:
                return DoubleTools.format(DoubleTools.random(random, maxRandom, nonNegative), scale);
//                return (char) ('a' + random.nextInt(25)) + "";
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            ColumnDefinition newColumn = (ColumnDefinition) super.clone();
            return newColumn;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public ColumnDefinition cloneBase() {
        try {
            return (ColumnDefinition) clone();
        } catch (Exception e) {
            return null;
        }
    }

    public Object value(ResultSet results) {
        try {
            if (results == null || type == null || name == null) {
                return null;
            }
            switch (type) {
                case String:
                case Text:
                case Color:
                case File:
                case Image:
                    return results.getString(name);
                case Double:
                    return results.getDouble(name);
                case Float:
                    return results.getFloat(name);
                case Long:
                case Era:
                    return results.getLong(name);
                case Integer:
                    return results.getInt(name);
                case Boolean:
                    return results.getBoolean(name);
                case Short:
                    return results.getShort(name);
                case Datetime:
                    return results.getTimestamp(name);
                case Date:
                    return results.getDate(name);
                case Blob:
                    return results.getBlob(name);
                case Clob:
                    return results.getClob(name);
                default:
                    MyBoxLog.debug(name + " " + type);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString(), name + " " + type);
        }
        return null;
    }

    public Object fromString(String string) {
        try {
            switch (type) {
                case Double:
                    return Double.parseDouble(string.replaceAll(",", ""));
                case Float:
                    return Float.parseFloat(string.replaceAll(",", ""));
                case Long:
                case Era:
                    return Long.parseLong(string.replaceAll(",", ""));
                case Integer:
                    return Integer.parseInt(string.replaceAll(",", ""));
                case Boolean:
                    String v = string.toLowerCase();
                    return "1".equals(v) || "0".equals(v)
                            || "true".equals(v) || "false".equals(v)
                            || "yes".equals(v) || "no".equals(v)
                            || Languages.message("true").equals(v) || Languages.message("false").equals(v)
                            || Languages.message("yes").equals(v) || Languages.message("no").equals(v);
                case Short:
                    return Short.parseShort(string.replaceAll(",", ""));
                case Datetime:
                    return DateTools.stringToDatetime(string);
                default:
                    return string;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String toString(Object value) {
        try {
            if (value == null) {
                return null;
            }
            switch (type) {
                case Datetime:
                    return DateTools.datetimeToString((Date) value);
                default:
                    return value + "";
            }
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static methods
     */
    public static ColumnDefinition create() {
        return new ColumnDefinition();
    }

    public static boolean valid(ColumnDefinition data) {
        return data != null
                && data.getType() != null
                && data.getName() != null && !data.getName().isBlank();
    }

    public static short columnType(ColumnType type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static ColumnType columnType(short type) {
        ColumnType[] types = ColumnType.values();
        if (type < 0 || type > types.length) {
            MyBoxLog.console(type);
            return ColumnType.String;
        }
        return types[type];
    }

    public static List<ColumnType> editTypes() {
        List<ColumnType> types = new ArrayList<>();
        types.addAll(Arrays.asList(ColumnType.String, ColumnType.Boolean,
                ColumnType.Double, ColumnType.Float, ColumnType.Long, ColumnType.Integer, ColumnType.Short,
                ColumnType.Datetime));
        return types;
    }

    public static String number2String(Number n) {
        return n != null ? n + "" : null;
    }

    public static Number string2Number(ColumnType type, String s) {
        try {
            if (null == type || s == null) {
                return null;
            }
            switch (type) {
                case Double:
                    return Double.parseDouble(s);
                case Float:
                    return Float.parseFloat(s);
                case Long:
                    return Long.parseLong(s);
                case Integer:
                    return Integer.parseInt(s);
                case Short:
                    return Short.parseShort(s);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /*
        customized get/set
     */
    public String getLabel() {
        if (label == null && name != null) {
            label = Languages.tableMessage(name.toLowerCase());
        }
        return label;
    }

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public ColumnDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public ColumnType getType() {
        return type;
    }

    public ColumnDefinition setType(ColumnType type) {
        this.type = type;
        return this;
    }

    public int getLength() {
        return length;
    }

    public ColumnDefinition setLength(int length) {
        this.length = length;
        return this;
    }

    public boolean isIsPrimaryKey() {
        return isPrimaryKey;
    }

    public ColumnDefinition setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
        return this;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public ColumnDefinition setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public ColumnDefinition setIndex(int index) {
        this.index = index;
        return this;
    }

    public boolean isIsID() {
        return isID;
    }

    public ColumnDefinition setIsID(boolean isID) {
        this.isID = isID;
        return this;
    }

    public String getForeignTable() {
        return foreignTable;
    }

    public ColumnDefinition setForeignTable(String foreignTable) {
        this.foreignTable = foreignTable;
        return this;
    }

    public String getForeignColumn() {
        return foreignColumn;
    }

    public ColumnDefinition setForeignColumn(String foreignColumn) {
        this.foreignColumn = foreignColumn;
        return this;
    }

    public OnDelete getOnDelete() {
        return onDelete;
    }

    public ColumnDefinition setOnDelete(OnDelete onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public OnUpdate getOnUpdate() {
        return onUpdate;
    }

    public ColumnDefinition setOnUpdate(OnUpdate onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public Era.Format getTimeFormat() {
        return timeFormat;
    }

    public ColumnDefinition setTimeFormat(Era.Format timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    public boolean isEditable() {
        return editable;
    }

    public ColumnDefinition setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public ColumnDefinition setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ColumnDefinition setValue(Object value) {
        this.value = value;
        return this;
    }

    public Number getMaxValue() {
        return maxValue;
    }

    public ColumnDefinition setMaxValue(Number maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public Number getMinValue() {
        return minValue;
    }

    public ColumnDefinition setMinValue(Number minValue) {
        this.minValue = minValue;
        return this;
    }

    public Map<Object, String> getValues() {
        return values;
    }

    public ColumnDefinition setValues(Map<Object, String> values) {
        this.values = values;
        return this;
    }

    public ColumnDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getForeignName() {
        return foreignName;
    }

    public ColumnDefinition setForeignName(String foreignName) {
        this.foreignName = foreignName;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ColumnDefinition setWidth(int width) {
        this.width = width;
        return this;
    }

    public String getTypeString() {
        return message(type.name());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
