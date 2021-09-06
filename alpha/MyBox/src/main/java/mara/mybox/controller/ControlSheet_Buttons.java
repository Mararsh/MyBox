package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Buttons extends ControlSheet_Sheet {

    protected char copyDelimiter = ',';

    @FXML
    public void copyDataAction() {
        DataCopyController controller = (DataCopyController) openChildStage(Fxmls.DataCopyFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public boolean copyRowsCols(List<Integer> rows, List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return false;
        }

        String[][] data = new String[rows.size()][cols.size()];
        for (int r = 0; r < rows.size(); ++r) {
            int row = rows.get(r);
            for (int c = 0; c < cols.size(); c++) {
                data[r][c] = cellString(row, cols.get(c));
            }
        }
        List<String> colsNames = null;
        if (withNames) {
            colsNames = new ArrayList<>();
            for (int c = 0; c < cols.size(); c++) {
                colsNames.add(colsCheck[cols.get(c)].getText());
            }
        }
        if (toSystemClipboard) {
            TextClipboardTools.copyToSystemClipboard(myController,
                    TextTools.dataText(data, copyDelimiter + "", colsNames, null));
        }
        return true;
    }

    public boolean copyCols(List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        return copyRowsCols(rowsIndex(true), cols, withNames, toSystemClipboard);
    }

    @FXML
    public void pasteDataAction() {
        DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    @FXML
    public void setDataAction() {
        DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public boolean setRowsCols(List<Integer> rows, List<Integer> cols, String value) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        isSettingValues = true;
        Random random = new Random();
        for (int r = 0; r < rows.size(); ++r) {
            int row = rows.get(r);
            for (int c = 0; c < cols.size(); ++c) {
                int col = cols.get(c);
                String v = value;
                if (value == null) {
                    ColumnType type = columns.get(col).getType();
                    if (type == ColumnType.Double || type == ColumnType.Float) {
                        v = DoubleTools.format(DoubleTools.random(random, maxRandom), scale);
                    } else {
                        v = StringTools.format(random.nextInt(maxRandom));
                    }
                }
                sheetInputs[row][col].setText(v);
            }
        }
        isSettingValues = false;
        sheetChanged();
        popSuccessful();
        return true;
    }

    public boolean setCols(List<Integer> cols, String value) {
        return setRowsCols(rowsIndex(true), cols, value);
    }

    @FXML
    public void sortDataAction() {
        DataSortController controller = (DataSortController) openChildStage(Fxmls.DataSortFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected boolean sortRows(List<Integer> rows, int col, boolean asc) {
        if (sheetInputs == null || columns == null || colsCheck == null || col < 0 || col >= columns.size()) {
            return false;
        }

        Collections.sort(rows, new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                if (row1 > row2) {
                    return 1;
                } else if (row1 < row2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        List<Integer> sortedRows = new ArrayList<>();
        sortedRows.addAll(rows);
        ColumnDefinition column = columns.get(col);
        Collections.sort(sortedRows, new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                int v = column.compare(cellString(row1, col), cellString(row2, col));
                return asc ? v : -v;
            }
        });

        int colsTotal = colsCheck.length;
        int rowsSize = sortedRows.size();
        String[][] sortedData = new String[rowsSize][colsTotal];
        for (int r = 0; r < rowsSize; r++) {
            int row = sortedRows.get(r);
            for (int c = 0; c < colsTotal; c++) {
                sortedData[r][c] = cellString(row, c);
            }
        }
        isSettingValues = true;
        for (int r = 0; r < rows.size(); r++) {
            int row = rows.get(r);
            for (int c = 0; c < colsTotal; c++) {
                sheetInputs[row][c].setText(sortedData[r][c]);
            }
        }
        isSettingValues = false;
        sheetChanged();
        popSuccessful();
        return true;
    }

    public boolean sort(int col, boolean asc) {
        return sortRows(rowsIndex(true), col, asc);
    }

    @FXML
    public void sizeDataAction() {
        DataSizeController controller = (DataSizeController) openChildStage(Fxmls.DataSizeFxml, false);
        controller.setParameters((ControlSheet) this);
    }

    // Notice: this does not concern columns names
    public void resizeSheet(int rowsNumber, int colsNumber) {
        if (sheetInputs != null) {
            if (sheetInputs.length > rowsNumber || sheetInputs[0].length > colsNumber) {
                if (!PopTools.askSure(baseTitle, message("DataReduceWarn"))) {
                    return;
                }
            }
        }
        if (rowsNumber <= 0 || colsNumber <= 0) {
            makeSheet(null);
            return;
        }
        String[][] values = new String[rowsNumber][colsNumber];
        if (sheetInputs != null && sheetInputs.length > 0) {
            int drow = Math.min(sheetInputs.length, rowsNumber);
            int dcol = Math.min(sheetInputs[0].length, colsNumber);
            for (int j = 0; j < drow; ++j) {
                for (int i = 0; i < dcol; ++i) {
                    values[j][i] = cellString(j, i);
                }
            }
        }
        makeSheet(values);
        popSuccessful();
    }

    protected void addRowsNumber() {
        if (colsCheck == null || colsCheck.length == 0) {
            return;
        }
        String value = askValue("", message("AddRowsNumber"), "1");
        if (value == null) {
            return;
        }
        try {
            int number = Integer.parseInt(value);
            if (sheetInputs == null || sheetInputs.length == 0) {
                resizeSheet(number, colsCheck.length);
            } else {
                resizeSheet(sheetInputs.length + number, colsCheck.length);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    protected void addColsNumber() {
        String value = askValue("", message("AddColsNumber"), "1");
        if (value == null) {
            return;
        }
        try {
            int number = Integer.parseInt(value);
            if (colsCheck == null || colsCheck.length == 0) {
                insertPageCol(0, true, number);
            } else {
                insertPageCol(colsCheck.length - 1, false, number);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    public void setSize(int rowsNumber, int colsNumber) {
        resizeSheet(rowsNumber, colsNumber);
    }

    protected void insertPageCol(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        int base = col + (left ? 0 : 1);
        makeColumns(base, number);
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
        } else {
            int rNumber = current.length;
            int cNumber = current[0].length + number;
            String[][] values = new String[rNumber][cNumber];
            for (int j = 0; j < rNumber; ++j) {
                for (int i = 0; i < base; ++i) {
                    values[j][i] = current[j][i];
                }
                for (int i = base + number; i < cNumber; ++i) {
                    values[j][i] = current[j][i - 1];
                }
                for (int i = base; i < base + number; ++i) {
                    values[j][i] = defaultColValue;
                }
            }
            makeSheet(values);
        }
    }

    @FXML
    public void widthDataAction() {
        DataWidthController controller = (DataWidthController) openChildStage(Fxmls.DataWidthFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public boolean widthCols(List<Integer> cols, int width) {
        if (cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        for (int c = 0; c < cols.size(); ++c) {
            int col = cols.get(c);
            colsCheck[col].setPrefWidth(width);
            if (sheetInputs != null) {
                for (int r = 0; r < sheetInputs.length; ++r) {
                    sheetInputs[r][col].setPrefWidth(width);
                }
            }
        }
        makeDefintionPane();
        popSuccessful();
        return true;
    }

    @FXML
    public void deleteDataAction() {
        DataDeleteController controller = (DataDeleteController) openChildStage(Fxmls.DataDeleteFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected boolean deleteRows(List<Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        String[][] values = new String[rowsCheck.length - rows.size()][columns.size()];
        int rowIndex = 0;
        for (int r = 0; r < rowsCheck.length; ++r) {
            if (!rows.contains(r)) {
                continue;
            }
            for (int c = 0; c < columns.size(); ++c) {
                values[rowIndex][c] = cellString(r, c);
            }
            rowIndex++;
        }
        makeSheet(values);
        popSuccessful();
        return true;
    }

    public boolean deleteCols(List<Integer> cols) {
        if (cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        List<ColumnDefinition> leftColumns = new ArrayList<>();
        List<Integer> leftColumnsIndex = new ArrayList<>();
        for (int c = 0; c < columns.size(); ++c) {
            if (!cols.contains(c)) {
                leftColumnsIndex.add(c);
                leftColumns.add(columns.get(c));
            }
        }
        String[][] values = new String[rowsCheck.length][leftColumns.size()];
        for (int r = 0; r < rowsCheck.length; ++r) {
            for (int c = 0; c < leftColumnsIndex.size(); ++c) {
                values[r][c] = cellString(r, leftColumnsIndex.get(c));
            }
        }
        makeSheet(values, leftColumns);
        popSuccessful();
        return true;
    }

    public boolean deleteAllCols() {
        if (columns != null) {
            columns.clear();
        }
        makeSheet(null);
        popSuccessful();
        return true;
    }

    public boolean deletePageRows() {
        makeSheet(null);
        popSuccessful();
        return true;
    }

    public boolean deleteAllRows() {
        popSuccessful();
        return deletePageRows();
    }

}
