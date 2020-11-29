package mara.mybox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.db.ColumnDefinition;
import mara.mybox.db.TableBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public abstract class TableData implements Cloneable {

    protected TableBase table;
    protected int maxColumnIndex;

    private void init() {
        maxColumnIndex = 20;
    }

    public TableData() {
        init();
    }

    public TableData(TableBase table) {
        this.table = table;
        init();
    }

    /*
        Abstract methods
     */
    protected abstract TableBase getTable();

    protected abstract Object getValue(String name);

    protected abstract boolean setValue(String name, Object value);

    protected abstract boolean valid();

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    protected String display(ColumnDefinition column, Object value) {
        if (column == null || value == null) {
            return null;
        }
        try {
            switch (column.getType()) {
                case String:
                case Text:
                case Color:
                case File:
                    return (String) value;
                case Double:
                    double dvalue = (double) value;
                    return dvalue != Double.MAX_VALUE ? dvalue + "" : null;
                case Float:
                    float fvalue = (float) value;
                    return fvalue != Float.MIN_VALUE ? fvalue + "" : null;
                case Long:
                case Era:
                    long lvalue = (long) value;
                    return lvalue != Long.MIN_VALUE ? lvalue + "" : null;
                case Integer:
                    int ivalue = (int) value;
                    return ivalue != Integer.MIN_VALUE ? ivalue + "" : null;
                case Boolean:
                    boolean bvalue = (boolean) value;
                    return bvalue + "";
                case Short:
                    short svalue = (short) value;
                    return svalue != Short.MIN_VALUE ? svalue + "" : null;
                case Datetime:
                case Date:
                    return DateTools.datetimeToString((Date) value);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public String display(String lineBreak) {
        try {
            String info = null;
            for (Object o : getTable().getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                Object value = getValue(column.getName());
                String display = display(column, value);
                if (display != null) {
                    if (info != null) {
                        info += lineBreak;
                    } else {
                        info = "";
                    }
                    info += column.getLabel() + ":" + display;
                }
            }
            return info;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected String html(ColumnDefinition column, Object value) {
        String display = display(column, value);
        if (display == null) {
            return null;
        }
        return display.replaceAll("\n", "</BR>");
    }

    public String html() {
        try {
            List<String> names = new ArrayList<>();
            StringTable htmlTable = new StringTable(names);
            names.addAll(Arrays.asList(message("Name"), message("Value")));
            for (Object o : getTable().getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                Object value = getValue(column.getName());
                String html = html(column, value);
                if (html != null) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(column.getLabel(), html));
                    htmlTable.add(row);
                }
            }
            return StringTable.tableDiv(htmlTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        get/set
     */
    public void setTable(TableBase table) {
        this.table = table;
    }

}
