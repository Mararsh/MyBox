package mara.mybox.db.data;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.paint.Color;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.GeoCoordinatePoint;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.IntTools;
import mara.mybox.tools.LongTools;
import mara.mybox.tools.ShortTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public class ColumnDefinition extends BaseData {

    protected String tableName, columnName, label, referName, referTable, referColumn,
            defaultValue, description, format;
    protected ColumnType type;
    protected int index, length, width, scale;
    protected Color color;
    protected boolean isPrimaryKey, notNull, editable, auto;
    protected OnDelete onDelete;
    protected OnUpdate onUpdate;
    protected Object value;
    protected Number maxValue, minValue;
    protected Map<Object, String> displayMap;
    protected DoubleStatistic statistic;

    public static enum ColumnType {
        String, Boolean, Text,
        Color, // rgba
        File, Image, // string of the path
        Double, Float, Long, Integer, Short,
        Datetime, Date, Era, // Looks Derby does not support date of BC(before Christ)
        Clob, Blob,
        Enumeration, Longitude, Latitude, GeoCoordinate
    }

    public static enum OnDelete {
        NoAction, Restrict, Cascade, SetNull
    }

    public static enum OnUpdate {
        NoAction, Restrict
    }

    public static enum DataFormat {
        ScientificNotation, CommaSeparated, None
    }

    public final void initColumnDefinition() {
        tableName = null;
        columnName = null;
        type = ColumnType.String;
        index = -1;
        isPrimaryKey = notNull = auto = false;
        editable = true;
        length = StringMaxLength;
        width = 100; // px
        scale = 8;
        onDelete = OnDelete.Restrict;
        onUpdate = OnUpdate.Restrict;
        format = null;
        maxValue = null;
        minValue = null;
        color = FxColorTools.randomColor();
        defaultValue = null;
        referName = null;
        referTable = null;
        referColumn = null;
        label = null;
        statistic = null;
        displayMap = null;
    }

    public ColumnDefinition() {
        initColumnDefinition();
    }

    public ColumnDefinition(String name, ColumnType type) {
        initColumnDefinition();
        this.columnName = name;
        this.type = type;
    }

    public ColumnDefinition(String name, ColumnType type, boolean notNull) {
        initColumnDefinition();
        this.columnName = name;
        this.type = type;
        this.notNull = notNull;
    }

    public ColumnDefinition(String name, ColumnType type, boolean notNull, boolean isPrimaryKey) {
        initColumnDefinition();
        this.columnName = name;
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
            tableName = c.tableName;
            columnName = c.columnName;
            label = c.label;
            referName = c.referName;
            referTable = c.referTable;
            referColumn = c.referColumn;
            type = c.type;
            index = c.index;
            length = c.length;
            width = c.width;
            scale = c.scale;
            color = c.color;
            isPrimaryKey = c.isPrimaryKey;
            notNull = c.notNull;
            auto = c.auto;
            editable = c.editable;
            onDelete = c.onDelete;
            onUpdate = c.onUpdate;
            format = c.format;
            defaultValue = c.defaultValue;
            value = c.value;
            maxValue = c.maxValue;
            minValue = c.minValue;
            columnValues = c.columnValues;
            statistic = c.statistic;
            description = c.description;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public boolean isForeignKey() {
        return referTable != null && referColumn != null;
    }

    public String foreignText() {
        if (!isForeignKey()) {
            return null;
        }
        String sql = (referName != null && !referName.isBlank() ? "CONSTRAINT  " + referName + " " : "")
                + "FOREIGN KEY (" + columnName + ") REFERENCES " + referTable + " (" + referColumn + ") ON DELETE ";
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
                case Enumeration:
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
                case GeoCoordinate:
                    return GeoCoordinatePoint.valid(value, ",");
                case Float:
                    Float.parseFloat(value.replaceAll(",", ""));
                    return true;
                case Long:
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
                            || message("true").equals(v) || message("false").equals(v)
                            || message("yes").equals(v) || message("no").equals(v);
                case Short:
                    Short.parseShort(value);
                    return true;
                case Datetime:
                case Date:
                case Era:
                    return DateTools.encodeDate(value) != null;
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

    // invalid values are counted as smaller
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
                    return DoubleTools.compare(value1, value2, false);
                case Float:
                    return FloatTools.compare(value1, value2, false);
                case Long:
                case Era:
                    return LongTools.compare(value1, value2, false);
                case Integer:
                    return IntTools.compare(value1, value2, false);
                case Short:
                    return ShortTools.compare(value1, value2, false);
                case Datetime:
                case Date:
                    return DateTools.compare(value1, value2, false);
                case GeoCoordinate:
                    return DoublePoint.compare(value1, value2, ",", false);
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

    public boolean isTextType() {
        return type == ColumnType.String || type == ColumnType.Text;
    }

    public boolean isEnumType() {
        return type == ColumnType.Enumeration;
    }

    public List<String> enumValues() {
        if (!isEnumType()) {
            return null;
        }
        return StringTools.toList(format, AppValues.MyBoxSeparator);
    }

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
                return StringTools.format(LongTools.random(random, maxRandom, nonNegative));
            case Short:
                return StringTools.format((short) IntTools.random(random, maxRandom, nonNegative));
            case Boolean:
                return random.nextInt(2) + "";
            case Datetime:
            case Era:
                return DateTools.randomTimeString(random);
            case Date:
                return DateTools.randomDateString(random);
            case GeoCoordinate:
                String x = DoubleTools.format(DoubleTools.random(random, Math.min(maxRandom, 180), nonNegative), scale);
                String y = DoubleTools.format(DoubleTools.random(random, Math.min(maxRandom, 90), nonNegative), scale);
                return x + "," + y;
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

    // results.getDouble/getFloat/getInt/getShort returned is 0 if the value is SQL NULL.
    // But we need distinct zero and null.
    // https://docs.oracle.com/en/java/javase/18/docs/api/java.sql/java/sql/ResultSet.html#getDouble(java.lang.String)
    public Object value(ResultSet results) {
        try {
            if (results == null || type == null || columnName == null) {
                return null;
            }
            String savedName = DerbyBase.savedName(columnName);
            if (results.findColumn(savedName) < 0) {
                return null;
            }
            switch (type) {
                case String:
                case Text:
                case Enumeration:
                case Color:
                case File:
                case Image:
                    return results.getString(savedName);
                case Double:
                    double d;
                    try {
                        d = Double.valueOf(results.getObject(savedName).toString());
                        if (DoubleTools.invalidDouble(d)) {
                            d = Double.NaN;
                        }
                    } catch (Exception e) {
                        d = Double.NaN;
                    }
                    return d;
                case Float:
                    float f;
                    try {
                        f = Float.valueOf(results.getObject(savedName).toString());
                        if (FloatTools.invalidFloat(f)) {
                            f = Float.NaN;
                        }
                    } catch (Exception e) {
                        f = Float.NaN;
                    }
                    return f;
                case Long:
                case Era:
                    long l;
                    try {
                        l = Long.valueOf(results.getObject(savedName).toString());
                    } catch (Exception e) {
                        l = AppValues.InvalidLong;
                    }
                    return l;
                case Integer:
                    int i;
                    try {
                        i = Integer.valueOf(results.getObject(savedName).toString());
                    } catch (Exception e) {
                        i = AppValues.InvalidInteger;
                    }
                    return i;
                case Short:
                    short s;
                    try {
                        s = Short.valueOf(results.getObject(savedName).toString());
                    } catch (Exception e) {
                        s = AppValues.InvalidShort;
                    }
                    return s;
                case Boolean:
                    return results.getBoolean(savedName);
                case Datetime:
                    return results.getTimestamp(savedName);
                case Date:
                    return results.getDate(savedName);
                case Blob:
                    return results.getBlob(savedName);
                case Clob:
                    return results.getClob(savedName);
                case GeoCoordinate:
                    DoublePoint p = DoublePoint.parse(results.getString(savedName), ",");
                    return p;
                default:
                    MyBoxLog.debug(savedName + " " + type);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString(), tableName + " " + columnName + " " + type);
        }
        return null;
    }

    public Object fromString(String string, InvalidAs invalidAs) {
        try {
            switch (type) {
                case Double:
                    return Double.parseDouble(string.replaceAll(",", ""));
                case Float:
                    return Float.parseFloat(string.replaceAll(",", ""));
                case Long:
                    return Math.round(Double.parseDouble(string.replaceAll(",", "")));
                case Integer:
                    return (int) Math.round(Double.parseDouble(string.replaceAll(",", "")));
                case Boolean:
                    return string2Boolean(string);
                case Short:
                    return (short) Math.round(Double.parseDouble(string.replaceAll(",", "")));
                case Datetime:
                case Date:
                    return DateTools.encodeDate(string);
                case Era:
                    return DateTools.encodeDate(string).getTime();
                case GeoCoordinate:
                    return DoublePoint.parse(string, ",");
                default:
                    return string;
            }
        } catch (Exception e) {
        }
        if (isNumberType() && invalidAs == InvalidAs.Zero) {
            switch (type) {
                case Double:
                    return 0d;
                case Float:
                    return 0f;
                default:
                    return 0;
            }
        } else {
            return null;
        }
    }

    public String toString(Object value) {
        try {
            if (value == null) {
                return null;
            }
            switch (type) {
                case Datetime:
                    return DateTools.datetimeToString((Date) value);
                case Date:
                    return DateTools.dateToString((Date) value);
                case Era:
                    return DateTools.datetimeToString(new Date((long) value));
                default:
                    return value + "";
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String display(String string, int maxLen) {
        try {
            if (string == null) {
                return null;
            }
            Object o = fromString(string, InvalidAs.Blank);
            if (o == null) {
                return string;
            }
            switch (type) {
                case Double:
                    return DoubleTools.format((double) o, format);
                case Float:
                    return FloatTools.format((float) o, format);
                case Long:
                    return LongTools.format((long) o, format);
                case Integer:
                    return IntTools.format((int) o, format);
                case Short:
                    return ShortTools.format((short) o, format);
                case Datetime:
                    return DateTools.datetimeToString((Date) o, format);
                case Date:
                    return DateTools.datetimeToString((Date) o, format);
                case Era:
                    return DateTools.datetimeToString(new Date((long) o), format);
                default:
                    String s = o.toString();
                    return s.length() > maxLen ? s.substring(0, maxLen) : s;
            }
        } catch (Exception e) {
            return string;
        }
    }

    public String savedValue(String string) {
        String savedValue = toString(fromString(string, InvalidAs.Blank));
        return savedValue;
    }

    public boolean valueQuoted() {
        return !isNumberType() && type != ColumnType.Boolean;
    }

    public String makeDefaultValue() {
        Object v = fromString(defaultValue, InvalidAs.Blank);
        switch (type) {
            case String:
            case Text:
            case Enumeration:
            case File:
            case Image:
            case Color:
            case GeoCoordinate:
                if (v != null) {
                    return "'" + defaultValue + "'";
                } else {
                    return "''";
                }
            case Double:
            case Float:
            case Long:
            case Integer:
            case Era:
            case Short:
                if (v != null) {
                    return v + "";
                } else {
                    return "0";
                }
            case Boolean:
                if (v != null) {
                    return v + "";
                } else {
                    return "false";
                }
            case Datetime:
                if (v != null) {
                    return "'" + defaultValue + "'";
                } else {
                    return " CURRENT TIMESTAMP ";
                }
            case Date:
                if (v != null) {
                    return "'" + defaultValue + "'";
                } else {
                    return " CURRENT DATE ";
                }
            default:
                return null;
        }
    }

    public Object defaultValue() {
        Object v = fromString(defaultValue, InvalidAs.Blank);
        switch (type) {
            case String:
            case Text:
            case Enumeration:
            case File:
            case Image:
            case Color:
            case GeoCoordinate:
                if (v != null) {
                    return defaultValue;
                } else {
                    return "";
                }
            case Double:
            case Float:
            case Long:
            case Integer:
            case Era:
            case Short:

                if (v != null) {
                    return v;
                } else {
                    return 0;
                }
            case Boolean:
                if (v != null) {
                    return v;
                } else {
                    return false;
                }
            case Datetime:
                if (v != null) {
                    return v;
                } else {
                    return new Timestamp(new Date().getTime());
                }
            case Date:
                if (v != null) {
                    return v;
                } else {
                    return new java.sql.Date(new Date().getTime());
                }
            default:
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
                && data.getColumnName() != null && !data.getColumnName().isBlank();
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
            return ColumnType.String;
        }
        return types[type];
    }

    public static ColumnType sqlColumnType(int type) {
        switch (type) {
            case java.sql.Types.BOOLEAN:
                return ColumnType.Boolean;
            case java.sql.Types.DOUBLE:
                return ColumnType.Double;
            case java.sql.Types.FLOAT:
                return ColumnType.Float;
            case java.sql.Types.INTEGER:
                return ColumnType.Integer;
            case java.sql.Types.BIGINT:
                return ColumnType.Long;
            case java.sql.Types.SMALLINT:
                return ColumnType.Short;
            case java.sql.Types.TINYINT:
                return ColumnType.Short;
            case java.sql.Types.DATE:
                return ColumnType.Date;
            case java.sql.Types.TIMESTAMP:
                return ColumnType.Datetime;
            case java.sql.Types.VARCHAR:
                return ColumnType.String;
            case java.sql.Types.CLOB:
                return ColumnType.Clob;
            case java.sql.Types.BLOB:
                return ColumnType.Blob;
            default:
                return ColumnType.String;
        }
    }

    public static List<ColumnType> editTypes() {
        List<ColumnType> types = new ArrayList<>();
        types.addAll(Arrays.asList(ColumnType.String, ColumnType.Boolean,
                ColumnType.Double, ColumnType.Float, ColumnType.Long, ColumnType.Integer, ColumnType.Short,
                ColumnType.Datetime, ColumnType.Date, ColumnType.Enumeration));
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
                    return Double.parseDouble(s.replaceAll(",", ""));
                case Float:
                    return Float.parseFloat(s.replaceAll(",", ""));
                case Long:
                    return Math.round(Double.parseDouble(s.replaceAll(",", "")));
                case Integer:
                    return (int) Math.round(Double.parseDouble(s.replaceAll(",", "")));
                case Short:
                    return (short) Math.round(Double.parseDouble(s.replaceAll(",", "")));
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean string2Boolean(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }
        return "1".equals(string)
                || "true".equalsIgnoreCase(string) || "yes".equalsIgnoreCase(string)
                || message("true").equals(string) || message("Yes").equals(string);
    }

    public static short onUpdate(OnUpdate type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static OnUpdate onUpdate(short type) {
        OnUpdate[] types = OnUpdate.values();
        if (type < 0 || type > types.length) {
            return OnUpdate.Restrict;
        }
        return types[type];
    }

    public static short onDelete(OnDelete type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static OnDelete onDelete(short type) {
        OnDelete[] types = OnDelete.values();
        if (type < 0 || type > types.length) {
            return OnDelete.Restrict;
        }
        return types[type];
    }

    public static OnUpdate updateRule(short type) {
        switch (type) {
            case DatabaseMetaData.importedKeyRestrict:
                return OnUpdate.Restrict;
            default:
                return OnUpdate.NoAction;
        }
    }

    public static OnDelete deleteRule(short type) {
        switch (type) {
            case DatabaseMetaData.importedKeyRestrict:
                return OnDelete.Restrict;
            case DatabaseMetaData.importedKeyCascade:
                return OnDelete.Cascade;
            case DatabaseMetaData.importedKeySetNull:
                return OnDelete.SetNull;
            default:
                return OnDelete.NoAction;
        }
    }

    /*
        customized get/set
     */
    public String getLabel() {
        if (label == null && columnName != null) {
            label = Languages.tableMessage(columnName.toLowerCase());
        }
        return label;
    }

    public boolean isId() {
        return isPrimaryKey && auto;
    }

    /*
        get/set
     */
    public String getTableName() {
        return tableName;
    }

    public ColumnDefinition setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public ColumnDefinition setColumnName(String name) {
        this.columnName = name;
        return this;
    }

    public String getColumnName() {
        return columnName;
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

    public String getReferTable() {
        return referTable;
    }

    public ColumnDefinition setReferTable(String primaryKeyTable) {
        this.referTable = primaryKeyTable;
        return this;
    }

    public String getReferColumn() {
        return referColumn;
    }

    public ColumnDefinition setReferColumn(String primaryKeyColumn) {
        this.referColumn = primaryKeyColumn;
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

    public String getFormat() {
        return format;
    }

    public ColumnDefinition setFormat(String format) {
        this.format = format;
        return this;
    }

    public boolean isEditable() {
        return editable;
    }

    public ColumnDefinition setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public ColumnDefinition setDefaultValue(String defaultValue) {
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

    public Map<Object, String> getDisplayMap() {
        return displayMap;
    }

    public ColumnDefinition setDisplayMap(Map<Object, String> data) {
        this.displayMap = data;
        return this;
    }

    public ColumnDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getReferName() {
        return referName;
    }

    public ColumnDefinition setReferName(String foreignName) {
        this.referName = foreignName;
        return this;
    }

    public boolean isAuto() {
        return auto;
    }

    public ColumnDefinition setAuto(boolean auto) {
        this.auto = auto;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ColumnDefinition setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public ColumnDefinition setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public String getTypeString() {
        return message(type.name());
    }

    public Color getColor() {
        return color;
    }

    public ColumnDefinition setColor(Color color) {
        this.color = color;
        return this;
    }

    public DoubleStatistic getStatistic() {
        return statistic;
    }

    public ColumnDefinition setStatistic(DoubleStatistic statistic) {
        this.statistic = statistic;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ColumnDefinition setDescription(String description) {
        this.description = description;
        return this;
    }

}
