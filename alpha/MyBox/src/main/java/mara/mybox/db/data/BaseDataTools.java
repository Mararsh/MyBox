package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.db.table.TableDataset;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.db.table.TableMatrix;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-11
 * @License Apache License Version 2.0
 */
public class BaseDataTools {

    public static BaseTable getTable(BaseData data) {
        if (data == null) {
            return null;
        }
        if (data instanceof GeographyCode) {
            return new TableGeographyCode();

        } else if (data instanceof Dataset) {
            return new TableDataset();

        } else if (data instanceof EpidemicReport) {
            return new TableEpidemicReport();

        } else if (data instanceof Location) {
            return new TableLocationData();

        } else if (data instanceof Matrix) {
            return new TableMatrix();

        } else if (data instanceof MatrixCell) {
            return new TableMatrixCell();

        } else if (data instanceof MyBoxLog) {
            return new TableMyBoxLog();

        } else if (data instanceof DataDefinition) {
            return new TableDataDefinition();

        } else if (data instanceof ColumnDefinition) {
            return new TableDataColumn();

        } else if (data instanceof ImageEditHistory) {
            return new TableImageEditHistory();

        } else if (data instanceof FileBackup) {
            return new TableFileBackup();

        } else if (data instanceof Note) {
            return new TableNote();

        } else if (data instanceof Notebook) {
            return new TableNotebook();

        } else if (data instanceof Tag) {
            return new TableTag();

        } else if (data instanceof NoteTag) {
            return new TableNoteTag();

        } else if (data instanceof ColorPaletteName) {
            return new TableColorPaletteName();

        } else if (data instanceof ColorData) {
            return new TableColor();

        } else if (data instanceof ColorPalette) {
            return new TableColorPalette();

        } else if (data instanceof TreeNode) {
            return new TableTree();

        } else if (data instanceof WebFavorite) {
            return new TableWebFavorite();

        } else if (data instanceof WebHistory) {
            return new TableWebHistory();

        } else if (data instanceof ImageClipboard) {
            return new TableImageClipboard();

        } else if (data instanceof TextClipboard) {
            return new TableTextClipboard();

        }
        return null;
    }

    public static boolean valid(BaseData data) {
        if (data == null) {
            return false;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.valid((GeographyCode) data);

        } else if (data instanceof Dataset) {
            return Dataset.valid((Dataset) data);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.valid((EpidemicReport) data);

        } else if (data instanceof Location) {
            return Location.valid((Location) data);

        } else if (data instanceof Matrix) {
            return Matrix.valid((Matrix) data);

        } else if (data instanceof MatrixCell) {
            return MatrixCell.valid((MatrixCell) data);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.valid((MyBoxLog) data);

        } else if (data instanceof DataDefinition) {
            return DataDefinition.valid((DataDefinition) data);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.valid((ColumnDefinition) data);

        } else if (data instanceof ImageEditHistory) {
            return ImageEditHistory.valid((ImageEditHistory) data);

        } else if (data instanceof FileBackup) {
            return FileBackup.valid((FileBackup) data);

        } else if (data instanceof Note) {
            return Note.valid((Note) data);

        } else if (data instanceof Notebook) {
            return Notebook.valid((Notebook) data);

        } else if (data instanceof Tag) {
            return Tag.valid((Tag) data);

        } else if (data instanceof NoteTag) {
            return NoteTag.valid((NoteTag) data);

        } else if (data instanceof ColorPaletteName) {
            return ColorPaletteName.valid((ColorPaletteName) data);

        } else if (data instanceof ColorData) {
            return ColorData.valid((ColorData) data);

        } else if (data instanceof ColorPalette) {
            return ColorPalette.valid((ColorPalette) data);

        } else if (data instanceof TreeNode) {
            return TreeNode.valid((TreeNode) data);

        } else if (data instanceof WebFavorite) {
            return WebFavorite.valid((WebFavorite) data);

        } else if (data instanceof WebHistory) {
            return WebHistory.valid((WebHistory) data);

        } else if (data instanceof ImageClipboard) {
            return ImageClipboard.valid((ImageClipboard) data);

        } else if (data instanceof TextClipboard) {
            return TextClipboard.valid((TextClipboard) data);

        }

        return false;
    }

    public static Object getColumnValue(BaseData data, String name) {
        if (data == null || name == null) {
            return null;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.getValue((GeographyCode) data, name);

        } else if (data instanceof Dataset) {
            return Dataset.getValue((Dataset) data, name);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.getValue((EpidemicReport) data, name);

        } else if (data instanceof Location) {
            return Location.getValue((Location) data, name);

        } else if (data instanceof Matrix) {
            return Matrix.getValue((Matrix) data, name);

        } else if (data instanceof MatrixCell) {
            return MatrixCell.getValue((MatrixCell) data, name);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.getValue((MyBoxLog) data, name);

        } else if (data instanceof DataDefinition) {
            return DataDefinition.getValue((DataDefinition) data, name);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.getValue((ColumnDefinition) data, name);

        } else if (data instanceof ImageEditHistory) {
            return ImageEditHistory.getValue((ImageEditHistory) data, name);

        } else if (data instanceof FileBackup) {
            return FileBackup.getValue((FileBackup) data, name);

        } else if (data instanceof Note) {
            return Note.getValue((Note) data, name);

        } else if (data instanceof Notebook) {
            return Notebook.getValue((Notebook) data, name);

        } else if (data instanceof Tag) {
            return Tag.getValue((Tag) data, name);

        } else if (data instanceof NoteTag) {
            return NoteTag.getValue((NoteTag) data, name);

        } else if (data instanceof ColorPaletteName) {
            return ColorPaletteName.getValue((ColorPaletteName) data, name);

        } else if (data instanceof ColorData) {
            return ColorData.getValue((ColorData) data, name);

        } else if (data instanceof ColorPalette) {
            return ColorPalette.getValue((ColorPalette) data, name);

        } else if (data instanceof TreeNode) {
            return TreeNode.getValue((TreeNode) data, name);

        } else if (data instanceof WebFavorite) {
            return WebFavorite.getValue((WebFavorite) data, name);

        } else if (data instanceof WebHistory) {
            return WebHistory.getValue((WebHistory) data, name);

        } else if (data instanceof ImageClipboard) {
            return ImageClipboard.getValue((ImageClipboard) data, name);

        } else if (data instanceof TextClipboard) {
            return TextClipboard.getValue((TextClipboard) data, name);

        }

        return null;
    }

    public static boolean setColumnValue(BaseData data, String name, Object value) {
        if (data == null || name == null) {
            return false;
        }
        if (data instanceof GeographyCode) {
            return GeographyCode.setValue((GeographyCode) data, name, value);

        } else if (data instanceof Dataset) {
            return Dataset.setValue((Dataset) data, name, value);

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.setValue((EpidemicReport) data, name, value);

        } else if (data instanceof Location) {
            return Location.setValue((Location) data, name, value);

        } else if (data instanceof Matrix) {
            return Matrix.setValue((Matrix) data, name, value);

        } else if (data instanceof MatrixCell) {
            return MatrixCell.setValue((MatrixCell) data, name, value);

        } else if (data instanceof MyBoxLog) {
            return MyBoxLog.setValue((MyBoxLog) data, name, value);

        } else if (data instanceof DataDefinition) {
            return DataDefinition.setValue((DataDefinition) data, name, value);

        } else if (data instanceof ColumnDefinition) {
            return ColumnDefinition.setValue((ColumnDefinition) data, name, value);

        } else if (data instanceof ImageEditHistory) {
            return ImageEditHistory.setValue((ImageEditHistory) data, name, value);

        } else if (data instanceof FileBackup) {
            return FileBackup.setValue((FileBackup) data, name, value);

        } else if (data instanceof Note) {
            return Note.setValue((Note) data, name, value);

        } else if (data instanceof Notebook) {
            return Notebook.setValue((Notebook) data, name, value);

        } else if (data instanceof Tag) {
            return Tag.setValue((Tag) data, name, value);

        } else if (data instanceof NoteTag) {
            return NoteTag.setValue((NoteTag) data, name, value);

        } else if (data instanceof ColorPaletteName) {
            return ColorPaletteName.setValue((ColorPaletteName) data, name, value);

        } else if (data instanceof ColorData) {
            return ColorData.setValue((ColorData) data, name, value);

        } else if (data instanceof ColorPalette) {
            return ColorPalette.setValue((ColorPalette) data, name, value);

        } else if (data instanceof TreeNode) {
            return TreeNode.setValue((TreeNode) data, name, value);

        } else if (data instanceof WebFavorite) {
            return WebFavorite.setValue((WebFavorite) data, name, value);

        } else if (data instanceof WebHistory) {
            return WebHistory.setValue((WebHistory) data, name, value);

        } else if (data instanceof ImageClipboard) {
            return ImageClipboard.setValue((ImageClipboard) data, name, value);

        } else if (data instanceof TextClipboard) {
            return TextClipboard.setValue((TextClipboard) data, name, value);

        }
        return false;
    }

    public static String displayData(BaseTable table, BaseData data, List<String> columns, boolean isHtml) {
        if (data == null) {
            return null;
        }
        try {
            if (table == null) {
                table = getTable(data);
            }
            if (table == null) {
                return null;
            }
            String lineBreak = isHtml ? "<BR>" : "\n";
            String info = null;
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getName())) {
                    continue;
                }
                Object value = getColumnValue(data, column.getName());
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

        } else if (data instanceof EpidemicReport) {
            return EpidemicReport.displayColumn((EpidemicReport) data, column, value);

        } else if (data instanceof Location) {
            return Location.displayColumn((Location) data, column, value);

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
                case Text:
                case Color:
                case File:
                case Image:
                    String rvalue = (String) value;
                    return rvalue;
                case Double:
                    double dvalue = (double) value;
                    if (column.getMaxValue() != null && dvalue > (double) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && dvalue < (double) column.getMinValue()) {
                        return null;
                    }
                    return dvalue != AppValues.InvalidDouble ? dvalue + "" : null;
                case Float:
                    float fvalue = (float) value;
                    if (column.getMaxValue() != null && fvalue > (float) column.getMaxValue()) {
                        return null;
                    }
                    if (column.getMinValue() != null && fvalue < (float) column.getMinValue()) {
                        return null;
                    }
                    return fvalue != AppValues.InvalidDouble ? fvalue + "" : null;
                case Long:
                case Era:
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
                case Date:
                    return DateTools.datetimeToString((Date) value);
            }
        } catch (Exception e) {
            MyBoxLog.error(e, column.getName());
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
                table = getTable(data);
            }
            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            StringTable htmlTable = new StringTable(names);
            names.addAll(Arrays.asList(Languages.message("Name"), Languages.message("Value")));
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                Object value = getColumnValue(data, column.getName());
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
                table = getTable(dataList.get(0));
            }
            if (table == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Object o : table.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                if (columns != null && !columns.contains(column.getName())) {
                    continue;
                }
                names.add(column.getLabel());
            }
            StringTable stringTable = new StringTable(names);
            for (BaseData data : dataList) {
                List<String> row = new ArrayList<>();
                for (Object o : table.getColumns()) {
                    ColumnDefinition column = (ColumnDefinition) o;
                    if (columns != null && !columns.contains(column.getName())) {
                        continue;
                    }
                    Object value = getColumnValue(data, column.getName());
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
