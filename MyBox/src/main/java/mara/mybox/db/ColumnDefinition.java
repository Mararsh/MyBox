package mara.mybox.db;

import java.util.Map;
import mara.mybox.data.Era;
import static mara.mybox.value.AppVariables.tableMessage;

/**
 * @Author Mara
 * @CreateDate 2020-7-12
 * @License Apache License Version 2.0
 */
public class ColumnDefinition {

    protected String name, label, foreignName, foreignTable, foreignColumn;
    protected ColumnType type;
    protected int index, length;
    protected boolean isPrimaryKey, notNull, isID, editable;
    protected OnDelete onDelete;
    protected OnUpdate onUpdate;
    protected Era.Format timeFormat;
    protected Object defaultValue, value, maxValue, minValue;
    protected Map<Object, String> values;  // value, displayString

    public static enum ColumnType {
        Boolean, String, Text,
        Color, // rgba
        File, // string of the path
        Double, Float, Long, Integer, Short,
        Datetime, Date, Era, // Looks Derby does not support date of BC(before Christ)
        Unknown
    }

    public enum OnDelete {
        NoAction, Restrict, Cascade, SetNull
    }

    public enum OnUpdate {
        NoAction, Restrict
    }

    private void init() {
        index = length = -1;
        isPrimaryKey = notNull = false;
        editable = true;
        type = ColumnType.Unknown;
        onDelete = OnDelete.Restrict;
        onUpdate = OnUpdate.Restrict;
        timeFormat = Era.Format.Datetime;
    }

    public ColumnDefinition() {
        init();
    }

    public ColumnDefinition(String name, ColumnType type) {
        init();
        this.name = name;
        this.type = type;
    }

    public ColumnDefinition(String name, ColumnType type, boolean notNull) {
        init();
        this.name = name;
        this.type = type;
        this.notNull = notNull;
    }

    public ColumnDefinition(String name, ColumnType type, boolean notNull, boolean isPrimaryKey) {
        init();
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.isPrimaryKey = isPrimaryKey;
    }

    public static ColumnDefinition create() {
        return new ColumnDefinition();
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


    /*
        static methods
     */
 /*
        customized get/set
     */
    public String getLabel() {
        if (label == null && name != null) {
            label = tableMessage(name.toLowerCase());
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

    public Object getMaxValue() {
        return maxValue;
    }

    public ColumnDefinition setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public Object getMinValue() {
        return minValue;
    }

    public ColumnDefinition setMinValue(Object minValue) {
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

}
