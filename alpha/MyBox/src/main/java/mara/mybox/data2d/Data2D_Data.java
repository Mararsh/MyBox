package mara.mybox.data2d;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Data extends Data2D_Attributes {

    @Override
    public boolean isValid() {
        return super.isValid() && columns != null && !columns.isEmpty();
    }

    public Data2D_Data initData(File file, String sheet, long dataSize, long currentPage) {
        resetData();
        this.file = file;
        this.sheet = sheet;
        this.dataSize = dataSize;
        this.currentPage = currentPage;
        return this;
    }

    public File tmpFile(String name, String operation, String suffix) {
        String path = AppPaths.getGeneratedPath();
        if (name != null && !name.isBlank()) {
            return getPathTempFile(path, name, suffix);
        }
        String pname = shortName();
        if (pname.startsWith(Data2D.TmpTablePrefix)) {
            pname = pname.substring(Data2D.TmpTablePrefix.length());
        }
        pname = pname + ((operation == null || operation.isBlank()) ? "" : "_" + operation);
        return getPathTempFile(path, pname, suffix);
    }

    /*
        file
     */
    public Data2D_Data initFile(File file) {
        if (file != null && file.equals(this.file)) {
            return initData(file, sheet, dataSize, currentPage);
        } else {
            return initData(file, null, 0, 0);
        }
    }

    public boolean isMutiplePages() {
        return pagesNumber > 1;
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
        return type != Type.Texts && type != Type.Matrix;
    }

    /*
        matrix
     */
    public boolean isSquareMatrix() {
        return type == Type.Matrix && tableColsNumber() == tableRowsNumber();
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
    public List<List<String>> tableData() {
        return loadController == null ? null : loadController.getTableData();
    }

    public void setTableChanged(boolean changed) {
        tableChanged = changed;
    }

    public int tableRowsNumber() {
        return loadController == null ? 0 : tableData().size();
    }

    public int tableColsNumber() {
        return columns == null ? 0 : columns.size();
    }

    // Column's index, instead of column name or table index, is the key to determine the column.
    // Columns order of table is synchronized when columns are applied. 
    // Columns order of file is synchronized when file is saved. 
    public int colOrder(int colIndex) {
        try {
            for (int i = 0; i < columns.size(); i++) {
                if (colIndex == columns.get(i).getIndex()) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
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

    public List<String> tableRow(int rowIndex, boolean withRowNumber) {
        try {
            List<String> trow = tableData().get(rowIndex);
            List<String> row = new ArrayList<>();
            if (withRowNumber) {
                row.add(trow.get(0));
            }
            for (int i = 0; i < columns.size(); i++) {
                String v = trow.get(i + 1);
                row.add(columns.get(i).savedValue(v));
            }
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public List<List<String>> tableRows(boolean withRowNumber) {
        try {
            List<List<String>> rows = new ArrayList<>();
            for (int i = 0; i < tableData().size(); i++) {
                List<String> row = tableRow(i, withRowNumber);
                rows.add(row);
            }
            return rows;
        } catch (Exception e) {
            return null;
        }
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

    public boolean hasData() {
        return isValid() && tableData() != null && !tableData().isEmpty();
    }

    public boolean isTmpData() {
        switch (type) {
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

    public List<List<String>> tmpData(int rows, int cols) {
        Random random = new Random();
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                if (type == Type.Matrix) {
                    row.add(randomDouble(random, true));
                } else {
                    row.add(randomString(random));
                }
            }
            data.add(row);
        }
        return data;
    }

    public String rowName(int row) {
        return message("Row") + (startRowOfCurrentPage + row + 1);
    }

    public List<String> rowNames() {
        try {
            return rowNames(tableData().size());
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> rowNames(int end) {
        try {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < end; i++) {
                names.add(rowName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public String random(Random random, int col, boolean nonNegative) {
        try {
            return column(col).random(random, maxRandom, scale, nonNegative);
        } catch (Exception e) {
            return null;
        }
    }

    /*
        table view
     */
    public List<String> tableViewRow(int row) {
        if (loadController == null || row < 0 || row > tableData().size() - 1) {
            return null;
        }
        try {
            return tableData().get(row);
        } catch (Exception e) {
            return null;
        }
    }

    public long tableViewRowIndex(int row) {
        if (loadController == null || row < 0 || row > tableData().size() - 1) {
            return -1;
        }
        try {
            return Long.valueOf(tableData().get(row).get(0));
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
            if (!isColumnsValid()) {
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

    public List<Integer> columnIndices() {
        try {
            if (!isColumnsValid()) {
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

    public boolean isColumnsValid() {
        return columns != null && !columns.isEmpty();
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
        Random random = new Random();
        for (Data2DColumn column : inColumns) {
            Data2DColumn tcolumn = column.cloneAll();
            String name = tcolumn.getColumnName();
            while (validNames.contains(name)) {
                name += random.nextInt(10);
            }
            tcolumn.setColumnName(name);
            targetColumns.add(tcolumn);
            validNames.add(name);
        }
        return targetColumns;
    }

    public void resetStatistic() {
        if (!isValid()) {
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

}
