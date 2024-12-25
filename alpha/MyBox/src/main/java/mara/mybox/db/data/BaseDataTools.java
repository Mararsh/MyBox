package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-11
 * @License Apache License Version 2.0
 */
public class BaseDataTools {

    public static String displayData(BaseTable table, BaseData data, boolean isHtml) {
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
                info += table.label(column.getColumnName()) + ": " + display;
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
        return column.displayValue(value);
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
