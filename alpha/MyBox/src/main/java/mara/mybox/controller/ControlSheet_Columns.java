package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Columns extends ControlSheet_Base {

    protected List<ColumnDefinition> checkColumns() {
        if (columns == null) {
            if (colsCheck != null && colsCheck.length > 0) {
                makeColumns(colsCheck.length);
            } else if (pageData != null && pageData.length > 0) {
                makeColumns(pageData[0].length);
            }
        }
        return columns;
    }

    public void makeColumns() {
        columns = null;
        checkColumns();
    }

    public void makeColumns(int number) {
        columns = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            ColumnDefinition column = new ColumnDefinition(message(colPrefix) + i, defaultColumnType, defaultColNotNull);
            columns.add(column);
        }
    }

    // start: 0-based
    public void makeColumns(int start, int number) {
        if (columns == null) {
            makeColumns(start);
        }
        List<String> columnNames = columnNames();
        List<ColumnDefinition> newColumns = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            String name = message(colPrefix) + (start + i);
            while (columnNames.contains(name)) {
                name += "m";
            }
            ColumnDefinition column = new ColumnDefinition(name, defaultColumnType, defaultColNotNull);
            newColumns.add(column);
            columnNames.add(name);
        }
        columns.addAll(start, newColumns);
    }

    protected List<String> columnNames() {
        try {
            if (checkColumns() == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                names.add(colName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    protected List<String> rowNames(int end) {
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

    protected String rowName(int row) {
        return message("Row") + (currentPageStart + row);
    }

    protected String colName(int col) {
        try {
            if (checkColumns() == null || columns.size() <= col) {
                return null;
            }
            return columns.get(col).getName();
        } catch (Exception e) {
            return null;
        }
    }

    protected String titleName() {
        if (sourceFile == null) {
            return "";
        }
        return sourceFile.getAbsolutePath();
    }

    protected boolean cellValid(int col, String value) {
        try {
            if (checkColumns() == null) {
                return false;
            }
            ColumnDefinition column = columns.get(col);
            return column.valid(value);
        } catch (Exception e) {
        }
        return false;
    }

    public void makeDefintionPane() {

    }

    protected StringTable validate() {
        try {
            dataInvalid = false;
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Reason")));
            StringTable table = new StringTable(names, message("InvalidData"));
            if (sheetInputs != null) {
                for (int i = 0; i < sheetInputs.length; i++) {
                    for (int j = 0; j < sheetInputs[i].length; j++) {
                        String value = cellString(i, j);
                        if (!cellValid(j, value)) {
                            sheetInputs[i][j].setStyle(NodeStyleTools.badStyle);
                            List<String> row = new ArrayList<>();
                            row.addAll(Arrays.asList((i + 1) + "", (j + 1) + "",
                                    (value == null || value.isBlank() ? message("Null") : message("InvalidValue"))));
                            table.add(row);
                        } else {
                            sheetInputs[i][j].setStyle(inputStyle);
                        }
                    }
                }
            }
            dataInvalid = !table.isEmpty();
            if (saveButton != null) {
                saveButton.setDisable(dataInvalid);
            }
            if (dataInvalid) {

            }
            return table;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        abstract
     */
    protected abstract boolean saveColumns();

    public abstract void makeSheet(String[][] data, List<ColumnDefinition> columns);

    protected abstract String cellString(int row, int col);

}
