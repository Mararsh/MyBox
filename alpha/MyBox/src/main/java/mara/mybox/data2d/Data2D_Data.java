package mara.mybox.data2d;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.db.data.Data2DDefinition.DataType.Texts;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.NumberTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Data extends Data2D_Attributes {

    public Data2D_Data initData(File file, String sheet, long dataSize, long currentPage) {
        resetData();
        this.file = file;
        this.sheet = sheet;
        this.rowsNumber = dataSize;
        this.currentPage = currentPage;
        return this;
    }

    public File tmpFile(String name, String operation, String ext) {
        return tmpFile(name, operation, ext,
                UserConfig.getBoolean("Data2DTmpDataUnderGeneratedPath", false));
    }

    public File tmpFile(String name, String operation, String ext, boolean underGeneratedPath) {
        String prefix;
        if (name != null && !name.isBlank()) {
            prefix = name;
        } else {
            prefix = shortName();
        }
        if (prefix.startsWith(TmpTable.TmpTablePrefix)
                || prefix.startsWith(TmpTable.TmpTablePrefix.toLowerCase())) {
            prefix = prefix.substring(TmpTable.TmpTablePrefix.length());
        }
        if (operation != null && !operation.isBlank()) {
            if (prefix != null && !prefix.isBlank()) {
                prefix += "_" + operation;
            } else {
                prefix = operation;
            }
        }
        if (underGeneratedPath) {
            return FileTmpTools.generateFile(prefix, ext);
        } else {
            return FileTmpTools.tmpFile(prefix, ext);
        }
    }

    /*
        file
     */
    public Data2D_Data initFile(File file) {
        if (file != null && file.equals(this.file)) {
            return initData(file, sheet, rowsNumber, currentPage);
        } else {
            return initData(file, null, 0, 0);
        }
    }

    public boolean isMutiplePages() {
        return dataLoaded && pagesNumber > 1;
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public Data2D_Data setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
        return this;
    }

    // file columns are not necessary in order of columns definition.
    // column's index remembers the order of columns
    // when index is less than 0, it is new column
    public List<String> fileRow(List<String> fileRow) {
        try {
            if (fileRow == null) {
                return null;
            }
            List<String> row = new ArrayList<>();
            int len = fileRow.size();
            for (int i = 0; i < columns.size(); i++) {
                String value = null;
                int index = columns.get(i).getIndex();
                if (index >= 0 && index < len) {
                    value = fileRow.get(index);
                }
                row.add(value);
            }
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean supportMultipleLine() {
        return dataType != DataType.Texts && dataType != DataType.Matrix;
    }

    /*
        matrix
     */
    public boolean isSquareMatrix() {
        return dataType == DataType.Matrix && tableColsNumber() == tableRowsNumber();
    }

    /*
        values
     */
    public String randomDouble(Random random, boolean nonNegative) {
        return NumberTools.format(DoubleTools.random(random, maxRandom, nonNegative), scale);
    }

    public String randomString(Random random) {
        return randomDouble(random, true);
//        return (char) ('a' + random.nextInt(25)) + "";
    }


    /*
        table data
     */
    public int tableRowsNumber() {
        return pageData == null ? 0 : pageData.size();
    }

    public int tableColsNumber() {
        return columns == null ? 0 : columns.size();
    }

    public int colOrder(String name) {
        try {
            if (name == null || name.isBlank()) {
                return -1;
            }
            for (int i = 0; i < columns.size(); i++) {
                if (name.equals(columns.get(i).getColumnName())) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public int idOrder() {
        try {
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn c = columns.get(i);
                if (c.isIsPrimaryKey() && c.isAuto()) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public List<String> tableRow(int rowIndex) {
        try {
            List<String> trow = pageData.get(rowIndex);
            List<String> row = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                String v = trow.get(i + 1);
                v = savedValue(i, v);
                row.add(v);
            }
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> tableRowShow(int rowIndex) {
        try {
            List<String> trow = pageData.get(rowIndex);
            List<String> row = new ArrayList<>();
            String rindex = trow.get(0);
            row.add(rindex != null && rindex.startsWith("-1") ? null : rindex);
            for (int i = 0; i < columns.size(); i++) {
                String v = trow.get(i + 1);
                v = formatValue(i, v);
                row.add(v);
            }
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public List<List<String>> tableRows(boolean rowNumber) {
        try {
            List<List<String>> rows = new ArrayList<>();
            for (int i = 0; i < pageData.size(); i++) {
                List<String> row = new ArrayList<>();
                if (rowNumber) {
                    row.add("" + (i + 1));
                    row.addAll(tableRowShow(i));
                } else {
                    row.addAll(tableRow(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (Exception e) {
            return null;
        }
    }

    public List<List<String>> tableRows() {
        return tableRows(false);
    }

    public List<String> newRow() {
        try {
            List<String> newRow = new ArrayList<>();
            newRow.add("-1");
            for (Data2DColumn column : columns) {
                if (column.getDefaultValue() != null) {
                    newRow.add(column.getDefaultValue());
                } else {
                    newRow.add(defaultColValue());
                }
            }
            return newRow;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> copyRow(List<String> row) {
        if (row == null) {
            return null;
        }
        List<String> newRow = new ArrayList<>();
        newRow.addAll(row);
        newRow.set(0, "-1");
        return newRow;
    }

    public boolean hasPage() {
        return isValidDefinition() && pageData != null;
    }

    public boolean hasPageData() {
        return isValidDefinition() && pageData != null && !pageData.isEmpty();
    }

    public boolean isPagesChanged() {
        return isMutiplePages() && isTableChanged();
    }

    public boolean isTmpData() {
        switch (dataType) {
            case CSV:
            case Excel:
            case Texts:
                return file == null;
            case DatabaseTable:
            case InternalTable:
                return sheet == null;
            default:
                return d2did < 0;
        }
    }

    public boolean isTmpFile() {
        return FileTmpTools.isTmpFile(file);
    }

    public boolean needBackup() {
        return file != null && isDataFile() && !isTmpFile()
                && UserConfig.getBoolean("Data2DFileBackupWhenSave", true);
    }

    public List<List<String>> tmpData(int rows, int cols) {
        Random random = new Random();
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                if (dataType == DataType.Matrix) {
                    row.add(randomDouble(random, true));
                } else {
                    row.add(randomString(random));
                }
            }
            data.add(row);
        }
        return data;
    }

    public String random(Random random, int col, boolean nonNegative) {
        try {
            return column(col).random(random, maxRandom, scale, nonNegative);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean verifyData() {
        try {
            if (pageData == null || pageData.isEmpty()) {
                return true;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Invalid")));
            StringTable stringTable = new StringTable(names, displayName());
            for (int r = 0; r < pageData.size(); r++) {
                List<String> dataRow = pageData.get(r);
                for (int c = 0; c < columns.size(); c++) {
                    Data2DColumn column = columns.get(c);
                    String value = dataRow.get(c + 1);
                    String item = null;
                    if (column.isNotNull() && (value == null || value.isBlank())) {
                        item = message("Null");
                    } else if (column.validValue(value)) {
                        item = message("DataType");
                    } else if (validValue(value)) {
                        item = message("TextDataComments");
                    }
                    if (item == null) {
                        continue;
                    }
                    List<String> invalid = new ArrayList<>();
                    invalid.addAll(Arrays.asList((r + 1) + "", (c + 1) + "", item));
                    stringTable.add(invalid);
                }
            }

            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    /*
        page
     */
    public List<String> pageRow(int row) {
        if (pageData == null || row < 0 || row > pageData.size() - 1) {
            return null;
        }
        try {
            return pageData.get(row);
        } catch (Exception e) {
            return null;
        }
    }

    public long pageRowIndex(int row) {
        if (pageData == null || row < 0 || row > pageData.size() - 1) {
            return -1;
        }
        try {
            return Long.parseLong(pageData.get(row).get(0));
        } catch (Exception e) {
            return -1;
        }
    }


    /*
        columns
     */
    public String defaultColValue() {
        return isMatrix() ? "0" : "";
    }

    public ColumnType defaultColumnType() {
        return isMatrix() ? ColumnType.Double : ColumnType.String;
    }

    public String colPrefix() {
        return message("Column");
    }

    public boolean defaultColNotNull() {
        return isMatrix();
    }

    public Data2DColumn column(int col) {
        try {
            return columns.get(col);
        } catch (Exception e) {
            return null;
        }
    }

    public int columnsNumber() {
        if (columns == null) {
            return 0;
        } else {
            return columns.size();
        }
    }

    public List<String> columnNames() {
        try {
            if (!isValidDefinition()) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : columns) {
                names.add(column.getColumnName());
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> columnNames(List<Integer> indices) {
        try {
            if (indices == null || columns == null || columns.isEmpty()) {
                return null;
            }
            List<String> names = new ArrayList<>();
            int len = columns.size();
            for (Integer i : indices) {
                if (i >= 0 && i < len) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Data2DColumn> makeColumns(List<Integer> indices, boolean rowNumber) {
        return makeColumns(columns, indices, rowNumber);
    }

    public static List<Data2DColumn> makeColumns(List<Data2DColumn> sourceColumns,
            List<Integer> indices, boolean rowNumber) {
        try {
            if (indices == null || sourceColumns == null || sourceColumns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> targetCcolumns = new ArrayList<>();
            if (rowNumber) {
                targetCcolumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (Integer i : indices) {
                Data2DColumn column = sourceColumns.get(i).cloneAll();
                String name = column.getColumnName();
                column.setD2cid(-1).setD2id(-1).setColumnName(name);
                targetCcolumns.add(column);
            }
            return targetCcolumns;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Integer> columnIndices() {
        try {
            if (!isValidDefinition()) {
                return null;
            }
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                indices.add(i);
            }
            return indices;
        } catch (Exception e) {
            return null;
        }
    }

    public Data2DColumn columnByName(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            for (Data2DColumn c : columns) {
                if (name.equals(c.getColumnName())) {
                    return c;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String columnName(int col) {
        try {
            return column(col).getColumnName();
        } catch (Exception e) {
            return null;
        }
    }

    public String formatValue(int col, String value) {
        try {
            return column(col).format(value, validateEdit());
        } catch (Exception e) {
            return null;
        }
    }

    public String savedValue(int col, String value) {
        try {
            return column(col).savedValue(value, validateEdit());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isValidDefinition() {
        return super.isValidDefinition() && columns != null && !columns.isEmpty();
    }

    public int newColumnIndex() {
        return --newColumnIndex;
    }

    public List<String> editableColumnNames() {
        if (columns == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Data2DColumn col : columns) {
            if (col.isEditable()) {
                names.add(col.getColumnName());
            }
        }
        return names;
    }

    public List<String> numberColumnNames() {
        if (columns == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Data2DColumn col : columns) {
            if (col.isNumberType()) {
                names.add(col.getColumnName());
            }
        }
        return names;
    }

    public List<String> timeColumnNames() {
        if (columns == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Data2DColumn col : columns) {
            if (col.isTimeType()) {
                names.add(col.getColumnName());
            }
        }
        return names;
    }

    public List<Data2DColumn> toColumns(List<String> names) {
        try {
            if (names == null) {
                return null;
            }
            List<Data2DColumn> cols = new ArrayList<>();
            Random random = new Random();
            for (String c : names) {
                Data2DColumn col = new Data2DColumn(c, defaultColumnType());
                col.setIndex(newColumnIndex());
                col.setColor(FxColorTools.randomColor(random));
                cols.add(col);
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Data2DColumn> tmpColumns(int cols) {
        List<String> names = new ArrayList<>();
        for (int c = 1; c <= cols; c++) {
            names.add(colPrefix() + c);
        }
        return toColumns(names);
    }

    public List<Data2DColumn> fixColumnNames(List<Data2DColumn> inColumns) {
        if (inColumns == null || inColumns.isEmpty()) {
            return inColumns;
        }
        List<String> validNames = new ArrayList<>();
        List<Data2DColumn> targetColumns = new ArrayList<>();
        for (Data2DColumn column : inColumns) {
            Data2DColumn tcolumn = column.cloneAll();
            String name = DerbyBase.checkIdentifier(validNames, tcolumn.getColumnName(), true);
            tcolumn.setColumnName(name);
            targetColumns.add(tcolumn);
        }
        return targetColumns;
    }

    public void resetStatistic() {
        if (!isValidDefinition()) {
            return;
        }
        for (Data2DColumn column : columns) {
            column.setStatistic(null);
        }
    }

    public boolean includeCoordinate() {
        if (columns == null) {
            return false;
        }
        boolean hasLongitude = false, haslatitude = false;
        for (Data2DColumn column : columns) {
            if (column.getType() == ColumnType.Longitude) {
                hasLongitude = true;
            } else if (column.getType() == ColumnType.Latitude) {
                haslatitude = true;
            }
        }
        return hasLongitude && haslatitude;
    }

    public List<String> placeholders(boolean allStatistic) {
        try {
            if (!isValidDefinition()) {
                return null;
            }
            List<String> list = new ArrayList<>();
            for (Data2DColumn column : columns) {
                String name = column.getColumnName();
                list.add("#{" + name + "}");
            }
            list.add("#{" + message("TableRowNumber") + "}");
            list.add("#{" + message("DataRowNumber") + "}");
            for (Data2DColumn column : columns) {
                String name = column.getColumnName();
                if (allStatistic || column.isNumberType()) {
                    list.add("#{" + name + "-" + message("Mean") + "}");
                    list.add("#{" + name + "-" + message("Median") + "}");
                    list.add("#{" + name + "-" + message("Mode") + "}");
                    list.add("#{" + name + "-" + message("MinimumQ0") + "}");
                    list.add("#{" + name + "-" + message("LowerQuartile") + "}");
                    list.add("#{" + name + "-" + message("UpperQuartile") + "}");
                    list.add("#{" + name + "-" + message("MaximumQ4") + "}");
                    list.add("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}");
                    list.add("#{" + name + "-" + message("LowerMildOutlierLine") + "}");
                    list.add("#{" + name + "-" + message("UpperMildOutlierLine") + "}");
                    list.add("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}");
                }
            }
            return list;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
