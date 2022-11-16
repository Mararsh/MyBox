package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableAlarmClock;
import mara.mybox.db.table.TableBlobValue;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.db.table.TableNamedValues;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-11
 * @License Apache License Version 2.0
 */
public class BaseDataAdaptor {

    public static BaseTable dataTable(BaseData data) {
        if (data == null) {
            return null;
        }
        if (data instanceof StringValues) {
            return new TableStringValues();

        } else if (data instanceof MyBoxLog) {
            return new TableMyBoxLog();

        } else if (data instanceof VisitHistory) {
            return new TableVisitHistory();

        } else if (data instanceof GeographyCode) {
            return new TableGeographyCode();

        } else if (data instanceof ImageEditHistory) {
            return new TableImageEditHistory();

        } else if (data instanceof FileBackup) {
            return new TableFileBackup();

        } else if (data instanceof Tag) {
            return new TableTag();

        } else if (data instanceof ColorPaletteName) {
            return new TableColorPaletteName();

        } else if (data instanceof ColorData) {
            return new TableColor();

        } else if (data instanceof ColorPalette) {
            return new TableColorPalette();

        } else if (data instanceof TreeNode) {
            return new TableTreeNode();

        } else if (data instanceof TreeNodeTag) {
            return new TableTreeNodeTag();

        } else if (data instanceof WebHistory) {
            return new TableWebHistory();

        } else if (data instanceof ImageClipboard) {
            return new TableImageClipboard();

        } else if (data instanceof TextClipboard) {
            return new TableTextClipboard();

        } else if (data instanceof ImageScope) {
            return new TableImageScope();

        } else if (data instanceof Data2DDefinition) {
            return new TableData2DDefinition();

        } else if (data instanceof Data2DColumn) {
            return new TableData2DColumn();

        } else if (data instanceof Data2DCell) {
            return new TableData2DCell();

        } else if (data instanceof BlobValue) {
            return new TableBlobValue();

        } else if (data instanceof Data2DRow) {
            return new TableData2D();

        } else if (data instanceof NamedValues) {
            return new TableNamedValues();

        } else if (data instanceof Data2DStyle) {
            return new TableData2DStyle();

        } else if (data instanceof AlarmClock) {
            return new TableAlarmClock();

        }
        return null;
    }

    public static boolean valid(BaseData data) {
        if (data == null) {
            return false;
        }
        if (data instanceof MyBoxLog) {
            return MyBoxLog.valid((MyBoxLog) data);

        } else if (data instanceof StringValues) {
            return StringValues.valid((StringValues) data);

        } else if (data instanceof VisitHistory) {
            return VisitHistory.valid((VisitHistory) data);

        } else if (data instanceof GeographyCode) {
            return GeographyCode.valid((GeographyCode) data);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.valid((ColumnDefinition) data);

        } else if (data instanceof ImageEditHistory) {
            return ImageEditHistory.valid((ImageEditHistory) data);

        } else if (data instanceof FileBackup) {
            return FileBackup.valid((FileBackup) data);

        } else if (data instanceof Tag) {
            return Tag.valid((Tag) data);

        } else if (data instanceof ColorPaletteName) {
            return ColorPaletteName.valid((ColorPaletteName) data);

        } else if (data instanceof ColorData) {
            return ColorData.valid((ColorData) data);

        } else if (data instanceof ColorPalette) {
            return ColorPalette.valid((ColorPalette) data);

        } else if (data instanceof TreeNode) {
            return TreeNode.valid((TreeNode) data);

        } else if (data instanceof TreeNodeTag) {
            return TreeNodeTag.valid((TreeNodeTag) data);

        } else if (data instanceof WebHistory) {
            return WebHistory.valid((WebHistory) data);

        } else if (data instanceof ImageClipboard) {
            return ImageClipboard.valid((ImageClipboard) data);

        } else if (data instanceof TextClipboard) {
            return TextClipboard.valid((TextClipboard) data);

        } else if (data instanceof ImageScope) {
            return ImageScope.valid((ImageScope) data);

        } else if (data instanceof Data2DDefinition) {
            return Data2DDefinition.valid((Data2DDefinition) data);

        } else if (data instanceof Data2DColumn) {
            return Data2DColumn.valid((Data2DColumn) data);

        } else if (data instanceof Data2DCell) {
            return Data2DCell.valid((Data2DCell) data);

        } else if (data instanceof BlobValue) {
            return BlobValue.valid((BlobValue) data);

        } else if (data instanceof Data2DRow) {
            return Data2DRow.valid((Data2DRow) data);

        } else if (data instanceof NamedValues) {
            return NamedValues.valid((NamedValues) data);

        } else if (data instanceof Data2DStyle) {
            return Data2DStyle.valid((Data2DStyle) data);

        } else if (data instanceof AlarmClock) {
            return AlarmClock.valid((AlarmClock) data);

        } else {
            return true;
        }

    }

    public static Object getColumnValue(BaseData data, String name) {
        if (data == null || name == null) {
            return null;
        }
        if (data instanceof MyBoxLog) {
            return MyBoxLog.getValue((MyBoxLog) data, name);

        } else if (data instanceof StringValues) {
            return StringValues.getValue((StringValues) data, name);

        } else if (data instanceof VisitHistory) {
            return VisitHistory.getValue((VisitHistory) data, name);

        } else if (data instanceof GeographyCode) {
            return GeographyCode.getValue((GeographyCode) data, name);

        } else if (data instanceof ImageEditHistory) {
            return ImageEditHistory.getValue((ImageEditHistory) data, name);

        } else if (data instanceof FileBackup) {
            return FileBackup.getValue((FileBackup) data, name);

        } else if (data instanceof Tag) {
            return Tag.getValue((Tag) data, name);

        } else if (data instanceof ColorPaletteName) {
            return ColorPaletteName.getValue((ColorPaletteName) data, name);

        } else if (data instanceof ColorData) {
            return ColorData.getValue((ColorData) data, name);

        } else if (data instanceof ColorPalette) {
            return ColorPalette.getValue((ColorPalette) data, name);

        } else if (data instanceof TreeNode) {
            return TreeNode.getValue((TreeNode) data, name);

        } else if (data instanceof TreeNodeTag) {
            return TreeNodeTag.getValue((TreeNodeTag) data, name);

        } else if (data instanceof WebHistory) {
            return WebHistory.getValue((WebHistory) data, name);

        } else if (data instanceof ImageClipboard) {
            return ImageClipboard.getValue((ImageClipboard) data, name);

        } else if (data instanceof TextClipboard) {
            return TextClipboard.getValue((TextClipboard) data, name);

        } else if (data instanceof ImageScope) {
            return ImageScope.getValue((ImageScope) data, name);

        } else if (data instanceof Data2DDefinition) {
            return Data2DDefinition.getValue((Data2DDefinition) data, name);

        } else if (data instanceof Data2DColumn) {
            return Data2DColumn.getValue((Data2DColumn) data, name);

        } else if (data instanceof Data2DCell) {
            return Data2DCell.getValue((Data2DCell) data, name);

        } else if (data instanceof BlobValue) {
            return BlobValue.getValue((BlobValue) data, name);

        } else if (data instanceof Data2DRow) {
            return Data2DRow.getValue((Data2DRow) data, name);

        } else if (data instanceof NamedValues) {
            return NamedValues.getValue((NamedValues) data, name);

        } else if (data instanceof Data2DStyle) {
            return Data2DStyle.getValue((Data2DStyle) data, name);

        } else if (data instanceof AlarmClock) {
            return AlarmClock.getValue((AlarmClock) data, name);

        } else {
            return data.getColumnValue(name);
        }

    }

    public static boolean setColumnValue(BaseData data, String name, Object value) {
        if (data == null || name == null) {
            return false;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.setValue((GeographyCode) data, name, value);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.setValue((MyBoxLog) data, name, value);

        } else if (data instanceof ImageEditHistory) {
            return ImageEditHistory.setValue((ImageEditHistory) data, name, value);

        } else if (data instanceof FileBackup) {
            return FileBackup.setValue((FileBackup) data, name, value);

        } else if (data instanceof Tag) {
            return Tag.setValue((Tag) data, name, value);

        } else if (data instanceof ColorPaletteName) {
            return ColorPaletteName.setValue((ColorPaletteName) data, name, value);

        } else if (data instanceof ColorData) {
            return ColorData.setValue((ColorData) data, name, value);

        } else if (data instanceof ColorPalette) {
            return ColorPalette.setValue((ColorPalette) data, name, value);

        } else if (data instanceof TreeNode) {
            return TreeNode.setValue((TreeNode) data, name, value);

        } else if (data instanceof TreeNodeTag) {
            return TreeNodeTag.setValue((TreeNodeTag) data, name, value);

        } else if (data instanceof WebHistory) {
            return WebHistory.setValue((WebHistory) data, name, value);

        } else if (data instanceof ImageClipboard) {
            return ImageClipboard.setValue((ImageClipboard) data, name, value);

        } else if (data instanceof TextClipboard) {
            return TextClipboard.setValue((TextClipboard) data, name, value);

        } else if (data instanceof ImageScope) {
            return ImageScope.setValue((ImageScope) data, name, value);

        } else if (data instanceof Data2DDefinition) {
            return Data2DDefinition.setValue((Data2DDefinition) data, name, value);

        } else if (data instanceof Data2DColumn) {
            return Data2DColumn.setValue((Data2DColumn) data, name, value);

        } else if (data instanceof Data2DCell) {
            return Data2DCell.setValue((Data2DCell) data, name, value);

        } else if (data instanceof BlobValue) {
            return BlobValue.setValue((BlobValue) data, name, value);

        } else if (data instanceof Data2DRow) {
            return Data2DRow.setValue((Data2DRow) data, name, value);

        } else if (data instanceof NamedValues) {
            return NamedValues.setValue((NamedValues) data, name, value);

        } else if (data instanceof Data2DStyle) {
            return Data2DStyle.setValue((Data2DStyle) data, name, value);

        } else if (data instanceof AlarmClock) {
            return AlarmClock.setValue((AlarmClock) data, name, value);

        } else {
            return data.setColumnValue(name, value);
        }

    }

    public static String displayData(BaseTable table, BaseData data, List<String> columns, boolean isHtml) {
        if (data == null) {
            return null;
        }
        try {
            if (table == null) {
                table = dataTable(data);
            }
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
                Object value = getColumnValue(data, column.getColumnName());
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
                info += column.getLabel() + ": " + display;
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
                case Color:
                case File:
                case Image:
                case Era:
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
                table = dataTable(data);
            }
            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            StringTable htmlTable = new StringTable(names);
            names.addAll(Arrays.asList(Languages.message("Name"), Languages.message("Value")));
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                Object value = getColumnValue(data, column.getColumnName());
                String html = htmlColumn(data, column, value);
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

    public static String htmlColumn(BaseData data, ColumnDefinition column, Object value) {
        String display = displayColumn(data, column, value);
        if (display == null) {
            return null;
        }
        return display.replaceAll("\n", "<BR>");
    }

    public static String htmlDataList(BaseTable table, List<BaseData> dataList, List<String> columns) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                return null;
            }
            if (table == null) {
                table = dataTable(dataList.get(0));
            }
            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getColumnName())) {
                    continue;
                }
                names.add(column.getLabel());
            }
            StringTable stringTable = new StringTable(names);
            for (BaseData data : dataList) {
                List<String> row = new ArrayList<>();
                for (Object o : table.getColumns()) {
                    ColumnDefinition column = (ColumnDefinition) o;
                    if (columns != null && !columns.contains(column.getColumnName())) {
                        continue;
                    }
                    Object value = getColumnValue(data, column.getColumnName());
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
