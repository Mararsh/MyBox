package mara.mybox.db.data;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.data.ColumnDefinition.getValue;
import static mara.mybox.db.data.ColumnDefinition.setValue;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.IntTools;
import mara.mybox.tools.LongTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.tools.ShortTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.matchIgnoreCase;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public class ColumnDefinition extends BaseData {

    protected String tableName, columnName, referName, referTable, referColumn,
            defaultValue, description, format, label;
    protected ColumnType type;
    protected int index, length, width, scale, century;
    protected Color color;
    protected boolean isPrimaryKey, notNull, editable, auto, fixTwoDigitYear;
    protected OnDelete onDelete;
    protected OnUpdate onUpdate;
    protected Object value;
    protected Number maxValue, minValue;
    protected DoubleStatistic statistic;

    final public static InvalidAs DefaultInvalidAs = InvalidAs.Skip;

    public static enum ColumnType {
        String, Boolean, Enumeration, EnumerationEditable,
        Color, // rgba
        File, Image, // string of the path
        Double, Float, Long, Integer, Short,
        Datetime, Date, Era, // Looks Derby does not support date of BC(before Christ)
        Clob, // CLOB is handled as string internally, and maxmium length is Integer.MAX(2G)
        Blob, // BLOB is handled as InputStream internally
        Longitude, Latitude, EnumeratedShort, NumberBoolean
    }

    public static enum OnDelete {
        NoAction, Restrict, Cascade, SetNull
    }

    public static enum OnUpdate {
        NoAction, Restrict
    }

    public enum InvalidAs {
        Zero, Empty, Null, Skip, Use, Fail
    }

    public final void initColumnDefinition() {
        tableName = null;
        columnName = null;
        type = ColumnType.String;
        index = -1;
        isPrimaryKey = notNull = auto = fixTwoDigitYear = false;
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
        statistic = null;
        description = null;
        century = 2000;
        label = null;
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
            return newData.cloneFrom(this);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public ColumnDefinition cloneFrom(ColumnDefinition c) {
        try {
            if (c == null) {
                return this;
            }
            tableName = c.tableName;
            columnName = c.columnName;
            referName = c.referName;
            referTable = c.referTable;
            referColumn = c.referColumn;
            type = c.type;
            index = c.index;
            length = c.length;
            width = c.width;
            scale = c.scale;
            format = c.format;
            color = c.color;
            isPrimaryKey = c.isPrimaryKey;
            notNull = c.notNull;
            auto = c.auto;
            editable = c.editable;
            onDelete = c.onDelete;
            onUpdate = c.onUpdate;
            defaultValue = c.defaultValue;
            value = c.value;
            maxValue = c.maxValue;
            minValue = c.minValue;
            statistic = c.statistic;
            description = c.description;
            fixTwoDigitYear = c.fixTwoDigitYear;
            century = c.century;
            label = c.label;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return this;
    }

    public boolean isForeignKey() {
        return referTable != null && referColumn != null;
//        return referTable != null && !referTable.isBlank()
//                && referColumn != null && !referColumn.isBlank()
//                && referName != null && !referName.isBlank();
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

    public boolean isId() {
        return isPrimaryKey && auto;
    }

    public boolean validValue(String value) {
        if (value == null || value.isBlank()) {
            return !notNull;
        }
        return fromString(value, InvalidAs.Fail) != null;
    }

    public boolean validNotNull(String value) {
        return !notNull || (value != null && !value.isEmpty());
    }

    public int compare(Object value1, Object value2) {
        if (value1 == null) {
            if (value2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (value2 == null) {
            return 1;
        }
        return compare(toString(value1), toString(value2));
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
                    return LongTools.compare(value1, value2, false);
                case Integer:
                    return IntTools.compare(value1, value2, false);
                case Short:
                    return ShortTools.compare(value1, value2, false);
                case Datetime:
                case Date:
                case Era:
                    return compareDate(value1, value2, false);
                default:
                    return StringTools.compare(value1, value2);
            }
        } catch (Exception e) {
        }
        return 1;
    }

    // invalid values are always in the end
    public int compareDate(String s1, String s2, boolean desc) {
        double d1, d2;
        try {
            d1 = toDate(s1).getTime();
        } catch (Exception e) {
            d1 = Double.NaN;
        }
        try {
            d2 = toDate(s2).getTime();
        } catch (Exception e) {
            d2 = Double.NaN;
        }
        return DoubleTools.compare(d1, d2, desc);
    }

    public boolean isBooleanType() {
        return type == ColumnType.Boolean;
    }

    public boolean isNumberType() {
        return isNumberType(type);
    }

    public boolean isDBNumberType() {
        return isDBNumberType(type);
    }

    public boolean isDBStringType() {
        return isDBStringType(type);
    }

    public boolean isDoubleType() {
        return type == ColumnType.Double;
    }

    public boolean isDBDoubleType() {
        return type == ColumnType.Double
                || type == ColumnType.Longitude || type == ColumnType.Latitude;
    }

    public boolean isTimeType() {
        return isTimeType(type);
    }

    public boolean isEnumType() {
        return type == ColumnType.Enumeration
                || type == ColumnType.EnumerationEditable
                || type == ColumnType.EnumeratedShort;
    }

    public List<String> enumNames() {
        if (!isEnumType()) {
            return null;
        }
        return StringTools.toList(format, "\n");
    }

    public String enumName(Short v) {
        try {
            return enumNames().get((int) v);
        } catch (Exception e) {
            return v != null ? v + "" : null;
        }
    }

    public Short enumValue(String v) {
        List<String> names = enumNames();
        try {

            Short s = Short.valueOf(v.replaceAll(",", ""));
            if (!ShortTools.invalidShort(s) && s >= 0 && s < names.size()) {
                return s;
            }
        } catch (Exception e) {
        }
        try {
            for (int i = 0; i < names.size(); i++) {
                if (matchIgnoreCase(v, names.get(i))) {
                    return (short) i;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public boolean needScale() {
        return type == ColumnType.Double || type == ColumnType.Float;
    }

    public boolean supportMultipleLine() {
        return type == ColumnType.String || type == ColumnType.Clob;
    }

    public String random(Random random, int maxRandom, short scale, boolean nonNegative) {
        if (random == null) {
            random = new Random();
        }
        switch (type) {
            case Double:
                return NumberTools.format(DoubleTools.random(random, maxRandom, nonNegative), scale);
            case Float:
                return NumberTools.format(FloatTools.random(random, maxRandom, nonNegative), scale);
            case Integer:
                return StringTools.format(IntTools.random(random, maxRandom, nonNegative));
            case Long:
                return StringTools.format(LongTools.random(random, maxRandom, nonNegative));
            case Short:
                return StringTools.format((short) IntTools.random(random, maxRandom, nonNegative));
            case Boolean:
                return random.nextInt(2) > 0 ? "true" : "false";
            case NumberBoolean:
                return random.nextInt(2) + "";
            case Datetime:
            case Date:
            case Era:
                return DateTools.randomDateString(random, format);
            default:
                return NumberTools.format(DoubleTools.random(random, maxRandom, nonNegative), scale);
//                return (char) ('a' + random.nextInt(25)) + "";
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
        Object o;
        String savedName;
        try {
            if (results == null || type == null || columnName == null) {
                return null;
            }
            savedName = DerbyBase.savedName(columnName);
            o = results.getObject(savedName);
            if (o == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        try {
//            MyBoxLog.console(columnName + " " + type + " " + savedName + "  " + o + " " + o.getClass());
            switch (type) {
                case String:
                case Enumeration:
                case EnumerationEditable:
                case Color:
                case File:
                case Image:
                    try {
                        return (String) o;
                    } catch (Exception e) {
                        return null;
                    }
                case Era:
                    try {
                        long e = Long.parseLong(o + "");
                        if (e >= 10000 || e <= -10000) {
                            return e;
                        } else {
                            return e;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Double:
                    try {
                        double d = Double.parseDouble(o + "");
                        if (DoubleTools.invalidDouble(d)) {
                            return null;
                        } else {
                            return d;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Longitude:
                    try {
                        double d = Double.parseDouble(o + "");
                        if (d >= -180 && d <= 180) {
                            return d;
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Latitude:
                    try {
                        double d = Double.parseDouble(o + "");
                        if (d >= -90 && d <= 90) {
                            return d;
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Float:
                    try {
                        float f = Float.parseFloat(o + "");
                        if (FloatTools.invalidFloat(f)) {
                            return null;
                        } else {
                            return f;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Long:
                    try {
                        long l = Long.parseLong(o + "");
                        if (LongTools.invalidLong(l)) {
                            return null;
                        } else {
                            return l;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Integer:
                    try {
                        int i = Integer.parseInt(o + "");
                        if (IntTools.invalidInt(i)) {
                            return null;
                        } else {
                            return i;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Short:
                    try {
                        short s = Short.parseShort(o + "");
                        if (ShortTools.invalidShort(s)) {
                            return null;
                        } else {
                            return s;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case EnumeratedShort:
                    try {
                        short s = Short.parseShort(o + "");
                        if (ShortTools.invalidShort(s)) {
                            return null;
                        } else {
                            return s;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case Boolean:
                    return StringTools.isTrue(o + "");
                case NumberBoolean:
                    String s = o + "";
                    if (StringTools.isTrue(s)) {
                        return 1;
                    }
                    if (StringTools.isFalse(s)) {
                        return 0;
                    }
                    try {
                        double d = Double.parseDouble(s);
                        if (d > 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } catch (Exception e) {
                        return 0;
                    }
                case Datetime:
                    return toDate(o + "");
                case Date:
                    return toDate(o + "");
                case Clob:
//                    MyBoxLog.console(columnName + " " + type + " " + savedName + "  " + o + " " + o.getClass());
                    Clob clob = (Clob) o;
                    return clob.getSubString(1, (int) clob.length());
                case Blob:
//                    MyBoxLog.console(tableName + " " + columnName + " " + type);
                    Blob blob = (Blob) o;
                    return blob.getBinaryStream();
                default:
                    MyBoxLog.debug(savedName + " " + type);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString(), tableName + " " + columnName + " " + type);
        }
        return null;
    }

    // remove format and convert to real value type
    public Object fromString(String string, InvalidAs invalidAs) {
        try {
            switch (type) {
                case Double:
                    Double d = Double.valueOf(string.replaceAll(",", ""));
                    if (!DoubleTools.invalidDouble(d)) {
                        return d;
                    }
                    break;
                case Longitude:
                    Double lo = Double.valueOf(string.replaceAll(",", ""));
                    if (lo >= -180 && lo <= 180) {
                        return lo;
                    }
                    break;
                case Latitude:
                    Double la = Double.valueOf(string.replaceAll(",", ""));
                    if (la >= -90 && la <= 90) {
                        return la;
                    }
                    break;
                case Float:
                    Double df = Double.valueOf(string.replaceAll(",", ""));
//                    MyBoxLog.console(string + "    ---  " + df);
                    if (!DoubleTools.invalidDouble(df)) {
                        return df;
                    }
//                    Float.valueOf may lose precision
//                    Float f = Float.valueOf(string.replaceAll(",", ""));
//                    MyBoxLog.console(string + "    ---  " + f);
//                    if (!FloatTools.invalidFloat(f)) {
//                        return f;
//                    }
//                    break;
                case Long:
                    Long l = Long.valueOf(string.replaceAll(",", ""));
                    if (!LongTools.invalidLong(l)) {
                        return l;
                    }
                    break;
                case Integer:
                    Integer i = Integer.valueOf(string.replaceAll(",", ""));
                    if (!IntTools.invalidInt(i)) {
                        return i;
                    }
                    break;
                case Short:
                    Short s = Short.valueOf(string.replaceAll(",", ""));
                    if (!ShortTools.invalidShort(s)) {
                        return s;
                    }
                    break;
                case EnumeratedShort:
                    Short es = enumValue(string);
                    if (es != null) {
                        return es;
                    }
                    break;
                case Boolean:
                    return StringTools.isTrue(string);
                case NumberBoolean:
                    if (StringTools.isTrue(string)) {
                        return 1;
                    }
                    if (StringTools.isFalse(string)) {
                        return 0;
                    }
                    double n = Double.parseDouble(string.replaceAll(",", ""));
                    if (!DoubleTools.invalidDouble(n)) {
                        if (n > 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                case Datetime:
                case Date:
                    Date t = stringToDate(string, fixTwoDigitYear, century);
                    if (t != null) {
                        return t;
                    }
                    break;
                case Era:
                    Date e = stringToDate(string, fixTwoDigitYear, century);
                    if (e != null) {
                        return e.getTime();
                    }
                    break;
                case Color:
                    try {
                        Color.web(string);
                        return string;
                    } catch (Exception exx) {
                    }
                default:
                    return string;
            }
        } catch (Exception e) {
        }
        if (invalidAs == null) {
            return string;
        }
        switch (invalidAs) {
            case Fail:
                return null;
            case Zero:
                if (isDBNumberType()) {
                    switch (type) {
                        case Double:
                            return 0d;
                        case Float:
                            return 0f;
                        case Long:
                            return 0l;
                        case Short:
                        case EnumeratedShort:
                            return (short) 0;
                        default:
                            return 0;
                    }
                } else {
                    return "0";
                }
            case Null:
                if (isDBStringType()) {
                    return "";
                } else {
                    return null;
                }
            case Empty:
                return "";
            case Skip:
                return string;
            case Use:
                return string;
            default:
                return string;
        }
    }

    public String toString(Object value) {
        if (value == null) {
            return null;
        }
        String s = value + "";
        try {
            switch (type) {
                case Double:
                case Longitude:
                case Latitude:
                    Double d = (Double) value;
                    if (DoubleTools.invalidDouble(d)) {
                        return null;
                    } else {
                        return s;
                    }
                case Float:
                    Float f = (Float) value;
                    if (FloatTools.invalidFloat(f)) {
                        return null;
                    } else {
                        return s;
                    }
                case Long:
                    Long l = (Long) value;
                    if (LongTools.invalidLong(l)) {
                        return null;
                    } else {
                        return s;
                    }
                case Integer:
                    Integer i = (Integer) value;
                    if (IntTools.invalidInt(i)) {
                        return null;
                    } else {
                        return s;
                    }
                case Short:
                    Short st = (Short) value;
                    if (ShortTools.invalidShort(st)) {
                        return null;
                    } else {
                        return s;
                    }
                case EnumeratedShort:
                    Short es = (Short) value;
                    if (ShortTools.invalidShort(es)
                            || enumName(es) == null) {
                        return null;
                    } else {
                        return s;
                    }
                case Datetime:
                case Date:
                    return DateTools.datetimeToString((Date) value);
                case Era: {
                    try {
                        long lv = Long.parseLong(value.toString());
                        if (lv >= 10000 || lv <= -10000) {
                            return DateTools.datetimeToString(new Date(lv));
                        }
                    } catch (Exception exx) {
                        return s;
                    }
                }
                default:
                    return s;
            }
        } catch (Exception e) {
            return s;
        }
    }

    public String formatValue(Object v) {
        return formatString(toString(v), InvalidAs.Use);
    }

    public String formatString(String v) {
        return formatString(v, InvalidAs.Use);
    }

    public String formatString(String string, InvalidAs inAs) {
        Object o = null;
        try {
            o = fromString(string, inAs);
        } catch (Exception e) {
        }
        if (o == null) {
            return null;
        }
        try {
            switch (type) {
                case Double:
                    return DoubleTools.format((Double) o, format, scale);
                case Float:
                    return FloatTools.format((Float) o, format, scale);
                case Long:
                    return LongTools.format((Long) o, format, scale);
                case Integer:
                    return IntTools.format((Integer) o, format, scale);
                case Short:
                    return ShortTools.format((Short) o, format, scale);
                case EnumeratedShort:
                    return enumName((Short) o);
                case Datetime:
                    return DateTools.datetimeToString((Date) o, format);
                case Date:
                    return DateTools.datetimeToString((Date) o, format);
                case Era: {
                    String s = toString(o);
                    try {
                        long lv = Long.parseLong(s);
                        if (lv >= 10000 || lv <= -10000) {
                            return DateTools.datetimeToString(new Date(lv), format);
                        }
                    } catch (Exception ex) {
                        return DateTools.datetimeToString(toDate(s), format);
                    }
                }
            }
        } catch (Exception e) {
        }
        return toString(o);
    }

    public String exportValue(Object v, boolean format) {
        return format ? formatValue(v) : toString(v);
    }

    public String removeFormat(String string) {
        return removeFormat(string, InvalidAs.Use);
    }

    public String removeFormat(String string, InvalidAs invalidAs) {
        if (invalidAs == null) {
            return string;
        }
        switch (type) {
            case Datetime:
            case Date:
            case Era:
            case Enumeration:
            case EnumerationEditable:
                return string;
            default:
                Object o = null;
                try {
                    o = fromString(string, invalidAs);
                } catch (Exception e) {
                }
                return toString(o);
        }
    }

    public boolean valueQuoted() {
        return !isDBNumberType() && type != ColumnType.Boolean;
    }

    public String dbDefaultValue() {
        Object v = fromString(defaultValue, InvalidAs.Null);
        switch (type) {
            case String:
            case Enumeration:
            case EnumerationEditable:
            case File:
            case Image:
            case Color:
            case Clob:
                if (v != null) {
                    return "'" + defaultValue + "'";
                } else {
                    return "''";
                }
            case Double:
            case Float:
            case Long:
            case Integer:
            case Short:
            case EnumeratedShort:
            case Longitude:
            case Latitude:
            case Era:
                if (v != null) {
                    return v + "";
                } else {
                    return "0";
                }
            case Boolean:
                return StringTools.isTrue(v + "") + "";
            case NumberBoolean:
                return StringTools.isTrue(v + "") ? "1" : "0";
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
                return "''";
        }
    }

    public Object defaultValue() {
        return fromString(defaultValue, InvalidAs.Null);
    }

    public String getFormatDisplay() {
        return format == null ? null : format.replaceAll(AppValues.MyBoxSeparator, "\n");
    }

    public Date toDate(String string) {
        return stringToDate(string, fixTwoDigitYear, century);
    }

    public double toDouble(String string) {
        try {
            if (null == type || string == null) {
                return Double.NaN;
            }
            switch (type) {
                case Datetime:
                case Date:
                case Era:
                    return toDate(string).getTime() + 0d;
                case Boolean:
                    return StringTools.isTrue(string) ? 1 : 0;
                case NumberBoolean:
                    if (StringTools.isTrue(string)) {
                        return 1;
                    }
                    if (StringTools.isFalse(string)) {
                        return 0;
                    }
                    double n = Double.parseDouble(string.replaceAll(",", ""));
                    if (n > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                default:
                    return Double.parseDouble(string.replaceAll(",", ""));
            }
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public String dummyValue() {
        switch (type) {
            case String:
            case Enumeration:
            case EnumerationEditable:
            case File:
            case Image:
            case Color:
            case Clob:
                return "";
            case Double:
            case Float:
            case Long:
            case Integer:
            case Short:
            case EnumeratedShort:
            case Longitude:
            case Latitude:
            case Era:
                return "0";
            case Boolean:
                return "false";
            case NumberBoolean:
                return "0";
            case Datetime:
                return DateTools.nowString();
            case Date:
                return DateTools.nowDate();
            default:
                return "''";
        }
    }

    public String info() {
        return info(this);
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

    public static Object getValue(ColumnDefinition data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "column_type":
                return columnTypeValue(data.getType());
            case "column_name":
                return data.getColumnName();
            case "label":
                return data.getLabel();
            case "index":
                return data.getIndex();
            case "length":
                return data.getLength();
            case "width":
                return data.getWidth();
            case "scale":
                return data.getScale();
            case "color":
                return colorValue(data.getColor());
            case "is_primary":
                return data.isIsPrimaryKey();
            case "not_null":
                return data.isNotNull();
            case "is_auto":
                return data.isAuto();
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

    public static boolean setValue(ColumnDefinition data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "column_type":
                    data.setType(columnTypeFromValue((short) value));
                    return true;
                case "column_name":
                    data.setColumnName(value == null ? null : (String) value);
                    return true;
                case "label":
                    data.setLabel(value == null ? null : (String) value);
                    return true;
                case "index":
                    data.setIndex(value == null ? -1 : (int) value);
                    return true;
                case "length":
                    data.setLength(value == null ? StringMaxLength : (int) value);
                    return true;
                case "width":
                    data.setWidth(value == null ? 100 : (int) value);
                    return true;
                case "scale":
                    data.setScale(value == null ? 8 : (int) value);
                    return true;
                case "color":
                    data.setColor(color(value));
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
                case "format":
                    data.setFormat(value == null ? null : (String) value);
                    return true;
                case "fix_year":
                    data.setFixTwoDigitYear(value == null ? false : (boolean) value);
                    return true;
                case "century":
                    data.setCentury(value == null ? 2000 : (int) value);
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
            MyBoxLog.debug(e + " column:" + column + " value:" + value);
        }
        return false;
    }

    public static short columnTypeValue(ColumnType type) {
        if (type == null) {
            return 0;
        }
        return (short) (type.ordinal());
    }

    public static ColumnType columnTypeFromValue(short type) {
        ColumnType[] types = ColumnType.values();
        if (type < 0 || type > types.length) {
            return ColumnType.String;
        }
        return types[type];
    }

    public static ColumnType columnTypeFromName(String name) {
        if (name == null || name.isBlank()) {
            return ColumnType.String;
        }
        for (ColumnType t : ColumnType.values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return ColumnType.String;
    }

    public static String columnTypeName(ColumnType type) {
        try {
            return type.name();
        } catch (Exception e) {
            return null;
        }
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

    public static InvalidAs invalidAs(Object v) {
        try {
            return InvalidAs.values()[(short) v];
        } catch (Exception e) {
            return DefaultInvalidAs;
        }
    }

    public static short invalidAsValue(InvalidAs v) {
        try {
            return (short) v.ordinal();
        } catch (Exception e) {
            return 3;
        }
    }

    public static InvalidAs invalidAsFromName(String name) {
        if (name == null || name.isBlank()) {
            return DefaultInvalidAs;
        }
        for (InvalidAs v : InvalidAs.values()) {
            if (v.name().equalsIgnoreCase(name)) {
                return v;
            }
        }
        return DefaultInvalidAs;
    }

    public static String invalidAsName(InvalidAs v) {
        try {
            return v.name();
        } catch (Exception e) {
            return null;
        }
    }

    public static Color color(Object v) {
        try {
            return Color.web((String) v);
        } catch (Exception e) {
            return null;
        }
    }

    public static String colorValue(Color v) {
        try {
            return v.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNumberType(ColumnType type) {
        return type == ColumnType.Double
                || type == ColumnType.Float
                || type == ColumnType.Integer
                || type == ColumnType.Long
                || type == ColumnType.Short
                || type == ColumnType.EnumeratedShort
                || type == ColumnType.NumberBoolean;
    }

    public static boolean isDBNumberType(ColumnType type) {
        return isNumberType(type)
                || type == ColumnType.Longitude
                || type == ColumnType.Latitude;
    }

    public static boolean isDBStringType(ColumnType type) {
        return type == ColumnType.String
                || type == ColumnType.Clob
                || type == ColumnType.File
                || type == ColumnType.Image
                || type == ColumnType.Enumeration
                || type == ColumnType.EnumerationEditable
                || type == ColumnType.Color;
    }

    public static boolean isTimeType(ColumnType type) {
        return type == ColumnType.Datetime
                || type == ColumnType.Date
                || type == ColumnType.Era;
    }

    public static boolean isEnumeratedType(ColumnType type) {
        return type == ColumnType.Enumeration
                || type == ColumnType.EnumerationEditable
                || type == ColumnType.EnumeratedShort;
    }

    public static String number2String(Number n) {
        return n != null ? n + "" : null;
    }

    public static Number string2Number(ColumnType sourceType, String string) {
        try {
            if (null == sourceType || string == null) {
                return null;
            }
            switch (sourceType) {
                case Double:
                    return Double.valueOf(string.replaceAll(",", ""));
                case Float:
                    return Float.valueOf(string.replaceAll(",", ""));
                case Long:
                    return Long.valueOf(string.replaceAll(",", ""));
                case Integer:
                    return Integer.valueOf(string.replaceAll(",", ""));
                case Short:
                    return Short.valueOf(string.replaceAll(",", ""));
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static Date stringToDate(String string) {
        return DateTools.encodeDate(string, 0);
    }

    public static Date stringToDate(String string, boolean fixTwoDigitYear, int century) {
        return DateTools.encodeDate(string, fixTwoDigitYear ? century : 0);
    }

    public static String info(ColumnDefinition column) {
        try {
            if (column == null) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            s.append(message("Name")).append(": ").append(column.getColumnName()).append("\n");
            s.append(message("Label")).append(": ").append(column.getLabel()).append("\n");
            s.append(message("Type")).append(": ").append(column.getType()).append("\n");
            s.append(message("Length")).append(": ").append(column.getLength()).append("\n");
            s.append(message("Width")).append(": ").append(column.getWidth()).append("\n");
            s.append(message("DisplayFormat")).append(": ").append(column.getFormat()).append("\n");
            s.append(message("NotNull")).append(": ").append(column.isNotNull()).append("\n");
            s.append(message("Editable")).append(": ").append(column.isEditable()).append("\n");
            s.append(message("PrimaryKey")).append(": ").append(column.isIsPrimaryKey()).append("\n");
            s.append(message("AutoGenerated")).append(": ").append(column.isAuto()).append("\n");
            s.append(message("DefaultValue")).append(": ").append(column.getDefaultValue()).append("\n");
            s.append(message("Color")).append(": ").append(column.getColor()).append("\n");
            s.append(message("DecimalScale")).append(": ").append(column.getScale()).append("\n");
            s.append(message("Century")).append(": ").append(column.getCentury()).append("\n");
            s.append(message("FixTwoDigitYears")).append(": ").append(column.isFixTwoDigitYear()).append("\n");
            s.append(message("Description")).append(": ").append(column.getDescription()).append("\n");
            s.append(message("ReferTable")).append(": ").append(column.getReferTable()).append("\n");
            s.append(message("ReferColumn")).append(": ").append(column.getReferColumn()).append("\n");
            s.append(message("ReferName")).append(": ").append(column.getReferName()).append("\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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

    public int getCentury() {
        return century;
    }

    public ColumnDefinition setCentury(int century) {
        this.century = century;
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

    public boolean isFixTwoDigitYear() {
        return fixTwoDigitYear;
    }

    public ColumnDefinition setFixTwoDigitYear(boolean fixTwoDigitYear) {
        this.fixTwoDigitYear = fixTwoDigitYear;
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

    public String getLabel() {
        return label != null ? label : columnName;
    }

    public ColumnDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

}
