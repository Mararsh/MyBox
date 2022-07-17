package mara.mybox.db.data;

import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ColumnFilter;
import mara.mybox.fxml.RowFilter;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class Data2DStyle extends BaseData {

    public static final String ColumnSeparator = "::";

    protected Data2DDefinition data2DDefinition;
    protected long d2sid, d2id;
    protected long rowStart, rowEnd; // 0-based, exlcuded
    protected String title, columns, rowFilterString, columnFilterString,
            fontColor, bgColor, fontSize, moreStyle;
    protected boolean abnoramlValues, bold;
    protected float sequence;
    protected RowFilter rowFilter;
    protected ColumnFilter columnFilter;

    private void init() {
        d2sid = -1;
        d2id = -1;
        title = null;
        rowStart = -1;
        rowEnd = -1;
        columns = null;
        rowFilterString = null;
        columnFilterString = null;
        rowFilter = null;
        columnFilter = null;
        fontColor = null;
        bgColor = null;
        fontSize = null;
        moreStyle = null;
        abnoramlValues = false;
        bold = false;
        sequence = 0;
    }

    public Data2DStyle() {
        init();
    }

    public Data2DStyle cloneAll() {
        try {
            Data2DStyle newData = (Data2DStyle) super.clone();
            newData.cloneFrom(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public void cloneFrom(Data2DStyle style) {
        try {
            if (style == null) {
                return;
            }
            data2DDefinition = style.data2DDefinition;
            d2sid = style.d2sid;
            d2id = style.d2id;
            title = style.title;
            rowStart = style.rowStart;
            rowEnd = style.rowEnd;
            columns = style.columns;
            rowFilterString = style.rowFilterString;
            columnFilterString = style.columnFilterString;
            rowFilter = style.rowFilter;
            columnFilter = style.columnFilter;
            fontColor = style.fontColor;
            bgColor = style.bgColor;
            fontSize = style.fontSize;
            moreStyle = style.moreStyle;
            bold = style.bold;
            sequence = style.sequence;
            abnoramlValues = style.abnoramlValues;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public String finalStyle() {
        String styleValue = "";
        if (fontSize != null && !message("Default").equals(fontSize)) {
            styleValue = "-fx-font-size: " + fontSize + "; ";
        }
        if (fontColor != null && !message("Default").equals(fontColor)) {
            styleValue += "-fx-text-fill: " + fontColor + "; ";
        }
        if (bgColor != null && !message("Default").equals(bgColor)) {
            styleValue += "-fx-background-color: " + bgColor + "; ";
        }
        if (bold) {
            styleValue += "-fx-font-weight: bolder; ";
        }
        if (moreStyle != null && !moreStyle.isBlank()) {
            styleValue += moreStyle.replaceAll("\n", " ");
        }
        return styleValue.isBlank() ? null : styleValue.trim();
    }


    /*
        static methods
     */
    public static Data2DStyle create() {
        return new Data2DStyle();
    }

    public static boolean valid(Data2DStyle data) {
        return data != null && data.getD2id() >= 0;
    }

    public static boolean setValue(Data2DStyle data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "d2sid":
                    data.setD2sid(value == null ? -1 : (long) value);
                    return true;
                case "d2id":
                    data.setD2id(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "rowStart":
                    data.setRowStart(value == null ? -1 : (long) value);
                    return true;
                case "rowEnd":
                    data.setRowEnd(value == null ? -1 : (long) value);
                    return true;
                case "columns":
                    data.setColumns(value == null ? null : (String) value);
                    return true;
                case "rowFilter":
                    data.setRowFilterString(value == null ? null : (String) value);
                    return true;
                case "columnFilter":
                    data.setColumnFilterString(value == null ? null : (String) value);
                    return true;
                case "fontColor":
                    data.setFontColor(value == null ? null : (String) value);
                    return true;
                case "bgColor":
                    data.setBgColor(value == null ? null : (String) value);
                    return true;
                case "fontSize":
                    data.setFontSize(value == null ? null : (String) value);
                    return true;
                case "moreStyle":
                    data.setMoreStyle(value == null ? null : (String) value);
                    return true;
                case "bold":
                    data.setBold(value == null ? false : (boolean) value);
                    return true;
                case "sequence":
                    data.setSequence(value == null ? 0 : (float) value);
                    return true;
                case "abnoramlValues":
                    data.setAbnoramlValues(value == null ? false : (boolean) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Data2DStyle data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "d2sid":
                    return data.getD2sid();
                case "d2id":
                    return data.getD2id();
                case "title":
                    return data.getTitle();
                case "rowStart":
                    return data.getRowStart();
                case "rowEnd":
                    return data.getRowEnd();
                case "columns":
                    return data.getColumns();
                case "rowFilter":
                    return data.getRowFilterString();
                case "columnFilter":
                    return data.getColumnFilterString();
                case "fontColor":
                    return data.getFontColor();
                case "bgColor":
                    return data.getBgColor();
                case "fontSize":
                    return data.getFontSize();
                case "moreStyle":
                    return data.getMoreStyle();
                case "bold":
                    return data.isBold();
                case "sequence":
                    return data.getSequence();
                case "abnoramlValues":
                    return data.isAbnoramlValues();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        interface get
        1-based, inlcuded
     */
    public long getFrom() {
        return rowStart < 0 ? -1 : rowStart + 1;
    }

    public long getTo() {
        return rowEnd;
    }

    /*
        row filter
     */
    public Data2DStyle setRowFilter(String script, boolean reversed) {
        return setRowFilter(new RowFilter(script, reversed));
    }

    public Data2DStyle setRowFilter(RowFilter rowFilter) {
        this.rowFilter = rowFilter;
        if (rowFilter == null) {
            rowFilterString = null;
        } else {
            rowFilterString = rowFilter.toString();
        }
        return this;
    }

    public RowFilter getRowFilter() {
        return rowFilter;
    }

    public Data2DStyle setRowFilterString(String rowFilterString) {
        this.rowFilterString = rowFilterString;
        rowFilter = RowFilter.create().fromString(rowFilterString);
        return this;
    }

    public String getRowFilterString() {
        return rowFilterString;
    }

    /*
        column filter
     */
    public Data2DStyle setColumnFilter(ColumnFilter columnFilter) {
        this.columnFilter = columnFilter;
        if (columnFilter == null) {
            columnFilterString = null;
        } else {
            columnFilterString = columnFilter.toString();
        }
        return this;
    }

    public ColumnFilter getColumnFilter() {
        return columnFilter;
    }

    public Data2DStyle setColumnFilterString(String columnFilterString) {
        this.columnFilterString = columnFilterString;
        columnFilter = ColumnFilter.create().fromString(columnFilterString);
        return this;
    }

    public String getColumnFilterString() {
        return columnFilterString;
    }

    /*
        filter cell
     */
    public boolean filterCell(RowFilter calculator, List<String> tableRow, long tableRowIndex, int colIndex) {
        try {
            if (calculator == null || tableRow == null) {
                return false;
            }

            if (rowFilter != null) {
                if (!rowFilter.filterTableRow(calculator.data2D, tableRow, tableRowIndex)) {
                    return false;
                }
            }
            if (columnFilter != null) {
                if (!columnFilter.filter(calculator.data2D.getColumns().get(colIndex), tableRow.get(colIndex + 1))) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }


    /*
        get/set
     */
    public long getD2sid() {
        return d2sid;
    }

    public Data2DStyle setD2sid(long d2sid) {
        this.d2sid = d2sid;
        return this;
    }

    public long getD2id() {
        return d2id;
    }

    public Data2DStyle setD2id(long d2id) {
        this.d2id = d2id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Data2DStyle setTitle(String title) {
        this.title = title;
        return this;
    }

    public long getRowStart() {
        return rowStart;
    }

    public Data2DStyle setRowStart(long rowStart) {
        this.rowStart = rowStart;
        return this;
    }

    public long getRowEnd() {
        return rowEnd;
    }

    public Data2DStyle setRowEnd(long rowEnd) {
        this.rowEnd = rowEnd;
        return this;
    }

    public String getColumns() {
        return columns;
    }

    public Data2DStyle setColumns(String columns) {
        this.columns = columns;
        return this;
    }

    public String getFontColor() {
        return fontColor;
    }

    public Data2DStyle setFontColor(String fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public String getBgColor() {
        return bgColor;
    }

    public Data2DStyle setBgColor(String bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public String getFontSize() {
        return fontSize;
    }

    public Data2DStyle setFontSize(String fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public String getMoreStyle() {
        return moreStyle;
    }

    public Data2DStyle setMoreStyle(String moreStyle) {
        this.moreStyle = moreStyle;
        return this;
    }

    public boolean isBold() {
        return bold;
    }

    public Data2DStyle setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public float getSequence() {
        return sequence;
    }

    public Data2DStyle setSequence(float sequence) {
        this.sequence = sequence;
        return this;
    }

    public boolean isAbnoramlValues() {
        return abnoramlValues;
    }

    public Data2DStyle setAbnoramlValues(boolean abnoramlValues) {
        this.abnoramlValues = abnoramlValues;
        return this;
    }

    public Data2DDefinition getData2DDefinition() {
        return data2DDefinition;
    }

    public Data2DStyle setData2DDefinition(Data2DDefinition data2DDefinition) {
        this.data2DDefinition = data2DDefinition;
        return this;
    }

}
