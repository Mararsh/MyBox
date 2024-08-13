package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.data.StringTable;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Enumeration;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Era;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-11
 * @License Apache License Version 2.0
 */
public class BaseDataTools {

    public static String displayData(BaseTable table, BaseData data, List<String> columns, boolean isHtml) {
        if (data == null) {
            return null;
        }
        try {
            if (table == null) {
                return null;
            }
            String lineBreak = isHtml ? "<BR>" : "\n";
            String info = null;
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getColumnName())) {
                    continue;
                }
                Object value = data.getValue(column.getColumnName());
                String display = displayColumn(data, column, value);
                if (display == null || display.isBlank()) {
                    continue;
                }
                if (column.getType() == ColumnDefinition.ColumnType.Image
                        && (lineBreak.toLowerCase().equals("<br>") || lineBreak.toLowerCase().equals("</br>"))) {
                    display = "<img src=\"file:///" + display.replaceAll("\\\\", "/") + "\" width=200px>";
                }
                if (info != null) {
                    info += lineBreak;
                } else {
                    info = "";
                }
                info += data.label(column.getColumnName()) + ": " + display;
            }
            return info + displayDataMore(data, lineBreak);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String displayColumn(BaseData data, ColumnDefinition column, Object value) {
        if (data == null || column == null) {
            return null;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.displayColumn((GeographyCode) data, column, value);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.displayColumn((MyBoxLog) data, column, value);

        }
        return displayColumnBase(data, column, value);
    }

    public static String displayColumnBase(BaseData data, ColumnDefinition column, Object value) {
        if (data == null || column == null || value == null) {
            return null;
        }
        try {
            switch (column.getType()) {
                case String:
                case Enumeration:
                case EnumerationEditable:
                case Color:
                case File:
                case Image:
                case Era:
                case Clob:
                    String rvalue = (String) value;
                    return rvalue;
                case Double:
                case Longitude:
                case Latitude:
                    double dvalue = (double) value;
                    if (column.getMaxValue() != null && dvalue > (double) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && dvalue < (double) column.getMinValue()) {
                        return null;
                    }
                    return DoubleTools.invalidDouble(dvalue) ? null : (dvalue + "");
                case Float:
                    float fvalue = (float) value;
                    if (column.getMaxValue() != null && fvalue > (float) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && fvalue < (float) column.getMinValue()) {
                        return null;
                    }
                    return DoubleTools.invalidDouble(fvalue) ? null : (fvalue + "");
                case Long:
                    long lvalue = (long) value;
                    if (column.getMaxValue() != null && lvalue > (long) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && lvalue < (long) column.getMinValue()) {
                        return null;
                    }
                    return lvalue != AppValues.InvalidLong ? lvalue + "" : null;
                case Integer:
                    int ivalue = (int) value;
                    if (column.getMaxValue() != null && ivalue > (int) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && ivalue < (int) column.getMinValue()) {
                        return null;
                    }
                    return ivalue != AppValues.InvalidInteger ? ivalue + "" : null;
                case Boolean:
                    boolean bvalue = (boolean) value;
                    return bvalue + "";
                case Short:
                    short svalue = (short) value;
                    if (column.getMaxValue() != null && svalue > (short) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && svalue < (short) column.getMinValue()) {
                        return null;
                    }
                    return svalue != AppValues.InvalidShort ? svalue + "" : null;
                case Datetime:
                    return DateTools.datetimeToString((Date) value);
                case Date:
                    return DateTools.dateToString((Date) value);
                case Blob:
                    return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e, column.getColumnName());
        }
        return null;
    }

    public static String displayDataMore(BaseData data, String lineBreak) {
        if (data == null || lineBreak == null) {
            return "";
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.displayDataMore((GeographyCode) data, lineBreak);

        }
        return "";
    }

    public static String htmlData(BaseTable table, BaseData data) {
        try {

            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            StringTable htmlTable = new StringTable(names);
            names.addAll(Arrays.asList(Languages.message("Name"), Languages.message("Value")));
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                Object value = data.getValue(column.getColumnName());
                String html = htmlColumn(data, column, value);
                if (html != null) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(column.getColumnName(), html));
                    htmlTable.add(row);
                }
            }
            return StringTable.tableDiv(htmlTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String htmlColumn(BaseData data, ColumnDefinition column, Object value) {
        String display = displayColumn(data, column, value);
        if (display == null) {
            return null;
        }
        return StringTools.replaceLineBreak(display, "<BR>");
    }

    public static String htmlDataList(BaseTable table, List<BaseData> dataList, List<String> columns) {
        try {
            if (table == null || dataList == null || dataList.isEmpty()) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getColumnName())) {
                    continue;
                }
                names.add(column.getColumnName());
            }
            StringTable stringTable = new StringTable(names);
            for (BaseData data : dataList) {
                List<String> row = new ArrayList<>();
                for (Object o : table.getColumns()) {
                    ColumnDefinition column = (ColumnDefinition) o;
                    if (columns != null && !columns.contains(column.getColumnName())) {
                        continue;
                    }
                    Object value = data.getValue(column.getColumnName());
                    String display = displayColumn(data, column, value);
                    if (display == null || display.isBlank()) {
                        display = "";
                    }
                    row.add(display);
                }
                stringTable.add(row);
            }
            return StringTable.tableDiv(stringTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
