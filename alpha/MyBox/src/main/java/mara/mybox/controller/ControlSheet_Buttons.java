package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Buttons extends ControlSheet_Edit {

    protected char copyDelimiter = ',';

    public abstract void copyCols(List<Integer> cols, boolean withNames, boolean toSystemClipboard);

    public abstract void setCols(List<Integer> cols, String value);

    public abstract void sort(int col, boolean asc);

    @FXML
    @Override
    public void copyToSystemClipboard() {
        DataCopyToSystemClipboardController controller = (DataCopyToSystemClipboardController) openChildStage(Fxmls.DataCopyToSystemClipboardFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        DataCopyToMyBoxClipboardController controller = (DataCopyToMyBoxClipboardController) openChildStage(Fxmls.DataCopyToMyBoxClipboardFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void copyRowsCols(List<Integer> rows, List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || colsCheck == null) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String[][] data;
                private List<String> colsNames;

                @Override
                protected boolean handle() {
                    colsNames = null;
                    if (withNames) {
                        colsNames = new ArrayList<>();
                        for (int c = 0; c < cols.size(); c++) {
                            colsNames.add(colsCheck[cols.get(c)].getText());
                        }
                    }
                    data = new String[rows.size()][cols.size()];
                    for (int r = 0; r < rows.size(); ++r) {
                        int row = rows.get(r);
                        for (int c = 0; c < cols.size(); c++) {
                            data[r][c] = cellString(row, cols.get(c));
                        }
                    }
                    if (toSystemClipboard) {
                        return data != null;
                    } else {
                        return DataClipboard.createData(tableDataDefinition, colsNames, data) != null;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (toSystemClipboard) {
                        TextClipboardTools.copyToSystemClipboard(myController,
                                TextTools.dataText(data, copyDelimiter + "", colsNames, null));
                    } else {
                        popSuccessful();
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        DataClipboardPopController.open((ControlSheet) this);
    }

    public void paste(String[][] data, int row, int col, boolean enlarge) {
        try {
            if (data == null || data.length == 0) {
                popError(message("NoData"));
                return;
            }
            pickData();
            if (pageData == null) {
                if (enlarge) {
                    makeSheet(data);
                }
                return;
            }
            if (row < 0 || row > rowsNumber - 1) {
                row = rowsNumber - 1;
            }
            if (col < 0 || col > colsNumber - 1) {
                col = colsNumber - 1;
            }
            int dataRowsSize = data.length, rowsSize = rowsNumber;
            int dataColsSize = data[0].length, colsSize = colsNumber;
            if (row + dataRowsSize > rowsNumber && enlarge) {
                rowsSize = row + dataRowsSize;
            }
            if (col + dataColsSize > colsNumber && enlarge) {
                colsSize = col + dataColsSize;
            }
            String[][] values = new String[rowsSize][colsSize];
            for (int r = 0; r < rowsSize; r++) {
                for (int c = 0; c < colsSize; c++) {
                    if (r >= row && r < row + dataRowsSize && c >= col && c < col + dataColsSize) {
                        values[r][c] = data[r - row][c - col];
                    } else if (r < rowsNumber && c < colsNumber) {
                        values[r][c] = pageData[r][c];
                    } else {
                        values[r][c] = defaultColValue;
                    }
                }
            }
            makeSheet(values);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void paste(File file, int row, int col, boolean enlarge) {
        try {
            if (file == null) {
                popError(message("NoData"));
                return;
            }

//            isSettingValues = true;
//            for (int r = 0; r < Math.min(sheetInputs.length, values.length); ++r) {
//                sheetInputs[r][col].setText(values[r]);
//            }
//            isSettingValues = false;
//            sheetChanged();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @FXML
    public void setDataAction() {
        DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void setRowsCols(List<Integer> rows, List<Integer> cols, String value) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || sheetInputs == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        isSettingValues = true;
        Random random = null;
        for (int r = 0; r < rows.size(); ++r) {
            int row = rows.get(r);
            for (int c = 0; c < cols.size(); ++c) {
                int col = cols.get(c);
                String v = value;
                if (AppValues.MyBoxRandomFlag.equals(value)) {
                    if (random == null) {
                        random = new Random();
                    }
                    v = columns.get(col).random(random, maxRandom, scale);
                }
                sheetInputs[row][col].setText(v);
            }
        }
        isSettingValues = false;
        sheetChanged();
        popSuccessful();
    }

    @FXML
    public void sortDataAction() {
        DataSortController controller = (DataSortController) openChildStage(Fxmls.DataSortFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void sortRows(List<Integer> rows, int col, boolean asc) {
        if (sheetInputs == null || columns == null
                || rows == null || rows.isEmpty() || col < 0 || col >= columns.size()) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String[][] sortedData;

                @Override
                protected boolean handle() {
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
                    sortedData = new String[rowsSize][colsTotal];
                    for (int r = 0; r < rowsSize; r++) {
                        int row = sortedRows.get(r);
                        for (int c = 0; c < colsTotal; c++) {
                            sortedData[r][c] = cellString(row, c);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    int colsTotal = colsCheck.length;
                    for (int r = 0; r < rows.size(); r++) {
                        int row = rows.get(r);
                        for (int c = 0; c < colsTotal; c++) {
                            sheetInputs[row][c].setText(sortedData[r][c]);
                        }
                    }
                    isSettingValues = false;
                    sheetChanged();
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    @FXML
    public void widthDataAction() {
        DataWidthController controller = (DataWidthController) openChildStage(Fxmls.DataWidthFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void widthCols(List<Integer> cols, int width) {
        if (cols == null || cols.isEmpty() || colsCheck == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        for (int c = 0; c < cols.size(); ++c) {
            int col = cols.get(c);
            columns.get(col).setWidth(width);
            colsCheck[col].setPrefWidth(width);
            if (sheetInputs != null) {
                for (int r = 0; r < sheetInputs.length; ++r) {
                    sheetInputs[r][col].setPrefWidth(width);
                }
            }
        }
        makeDefintionPane();
        popSuccessful();
    }

    @FXML
    public void rowsAddAction() {
        DataRowsAddController controller = (DataRowsAddController) openChildStage(Fxmls.DataRowsAddFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void addRows(int row, boolean above, int number) {
        if (number < 1 || columns == null || columns.isEmpty()) {
            return;
        }
        String[][] current = pickData();
        String[][] values;
        int cNumber = columns.size();
        if (current == null) {
            values = new String[number][cNumber];
            for (int r = 0; r < number; ++r) {
                for (int c = 0; c < cNumber; ++c) {
                    values[r][c] = defaultColValue;
                }
            }
        } else {
            int rNumber = current.length;
            values = new String[rNumber + number][cNumber];
            int base;
            if (row < 0) {
                base = 0;
            } else {
                base = row + (above ? 0 : 1);
            }
            for (int r = 0; r < base; ++r) {
                for (int c = 0; c < cNumber; ++c) {
                    values[r][c] = current[r][c];
                }
            }
            for (int r = base; r < base + number; ++r) {
                for (int c = 0; c < cNumber; ++c) {
                    values[r][c] = defaultColValue;
                }
            }
            for (int r = base + number; r < rNumber + number; ++r) {
                for (int c = 0; c < cNumber; ++c) {
                    values[r][c] = current[r - number][c];
                }
            }
        }
        makeSheet(values);
    }

    @FXML
    public void rowsDeleteAction() {
        DataRowsDeleteController controller = (DataRowsDeleteController) openChildStage(Fxmls.DataRowsDeleteFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void deleteRows(List<Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            popError(message("NoSelection"));
            return;
        }
        if (rowsCheck == null || columns == null || columns.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        String[][] values = null;
        values = new String[rowsCheck.length - rows.size()][columns.size()];
        int rowIndex = 0;
        for (int r = 0; r < rowsCheck.length; ++r) {
            if (rows.contains(r)) {
                continue;
            }
            for (int c = 0; c < columns.size(); ++c) {
                values[rowIndex][c] = cellString(r, c);
            }
            rowIndex++;
        }
        makeSheet(values);
        popSuccessful();
    }

    public void deletePageRows() {
        makeSheet(null);
        popSuccessful();
    }

    public void deleteAllRows() {
        deletePageRows();
    }

    @FXML
    public void columnsAddAction() {
        DataColumnsAddController controller = (DataColumnsAddController) openChildStage(Fxmls.DataColumnsAddFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void addCols(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        int base;
        if (col < 0) {
            base = 0;
        } else {
            base = col + (left ? 0 : 1);
        }
        makeColumns(base, number);
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
            return;
        }
        int rNumber = current.length;
        int cNumber = current[0].length + number;
        String[][] values = new String[rNumber][cNumber];
        for (int r = 0; r < rNumber; ++r) {
            for (int c = 0; c < base; ++c) {
                values[r][c] = current[r][c];
            }
            for (int c = base; c < base + number; ++c) {
                values[r][c] = defaultColValue;
            }
            for (int c = base + number; c < cNumber; ++c) {
                values[r][c] = current[r][c - number];
            }
        }
        makeSheet(values);
    }

    @FXML
    public void columnsDeleteAction() {
        DataColumnsDeleteController controller = (DataColumnsDeleteController) openChildStage(Fxmls.DataColumnsDeleteFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void deleteCols(List<Integer> cols) {
        if (cols == null || cols.isEmpty() || columns == null || columns.isEmpty()) {
            popError(message("NoSelection"));
            return;
        }
        List<ColumnDefinition> leftColumns = new ArrayList<>();
        List<Integer> leftColumnsIndex = new ArrayList<>();
        for (int c = 0; c < columns.size(); ++c) {
            if (!cols.contains(c)) {
                leftColumnsIndex.add(c);
                leftColumns.add(columns.get(c));
            }
        }
        String[][] values = null;
        if (rowsCheck != null) {
            values = new String[rowsCheck.length][leftColumns.size()];
            for (int r = 0; r < rowsCheck.length; ++r) {
                for (int c = 0; c < leftColumnsIndex.size(); ++c) {
                    values[r][c] = cellString(r, leftColumnsIndex.get(c));
                }
            }
        }
        makeSheet(values, leftColumns);
        popSuccessful();
    }

    public void deleteAllCols() {
        if (columns != null) {
            columns.clear();
        }
        makeSheet(null);
        popSuccessful();
    }

    @FXML
    public void calculateDataAction() {
        DataCalculateController controller = (DataCalculateController) openChildStage(Fxmls.DataCalculateFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

}
