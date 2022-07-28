package mara.mybox.data2d;

import java.util.Arrays;
import java.util.List;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.RowFilter;
import mara.mybox.fxml.SingletonTask;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D extends Data2D_Operations {

    @Override
    public Data2D cloneAll() {
        try {
            Data2D newData = (Data2D) super.clone();
            newData.cloneAll(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public void startTask(SingletonTask task, RowFilter rowFilter) {
        this.task = task;
        this.rowFilter = rowFilter;
        startFilter();
    }

    public void stopTask() {
        task = null;
        stopFilter();
    }

    /*
        static
     */
    public static Data2D create(Type type) {
        if (type == null) {
            return null;
        }
        Data2D data;
        switch (type) {
            case CSV:
                data = new DataFileCSV();
                break;
            case Excel:
                data = new DataFileExcel();
                break;
            case Texts:
                data = new DataFileText();
                break;
            case Matrix:
                data = new DataMatrix();
                break;
            case MyBoxClipboard:
                data = new DataClipboard();
                break;
            case DatabaseTable:
                data = new DataTable();
                break;
            case InternalTable:
                data = new DataInternalTable();
                break;
            default:
                return null;
        }
        data.setType(type);
        return data;
    }

    /*
        filter
     */
    public boolean needFilter() {
        return rowFilter != null && rowFilter.needFilter();
    }

    public void startFilter() {
        if (rowFilter == null) {
            return;
        }
        rowFilter.start(task, this);
    }

    public void stopFilter() {
        if (rowFilter == null) {
            return;
        }
        rowFilter.stop();
    }

    public boolean filterDataRow(List<String> dataRow, long dataRowIndex) {
        error = null;
        if (rowFilter == null) {
            return true;
        }
        if (rowFilter.filterDataRow(this, dataRow, dataRowIndex)) {
            return true;
        } else {
            error = rowFilter.getError();
            return false;
        }
    }

    public boolean filterPassed() {
        return rowFilter == null || rowFilter.passed;
    }

    public boolean filterReachMaxPassed() {
        return rowFilter != null && rowFilter.reachMaxPassed();
    }

    public boolean calculateDataRowExpression(String script, List<String> dataRow, long dataRowNumber) {
        error = null;
        if (rowFilter == null) {
            return true;
        }
        return rowFilter.calculator.calculate(rowFilter.calculator.dataRowExpression(this, script, dataRow, dataRowNumber));
    }

    public String expressionError() {
        if (rowFilter == null) {
            return null;
        }
        return rowFilter.getError();
    }

    public String expressionResult() {
        if (rowFilter == null) {
            return null;
        }
        return rowFilter.getResult();
    }

    /*
        style
     */
    public String cellStyle(RowFilter styleFilter, int tableRowIndex, String colName) {
        try {
            if (styleFilter == null || styles == null || styles.isEmpty() || colName == null || colName.isBlank()) {
                return null;
            }
            List<String> tableRow = tableViewRow(tableRowIndex);
            if (tableRow == null || tableRow.size() < 1) {
                return null;
            }
            int colIndex = colOrder(colName);
            if (colIndex < 0) {
                return null;
            }
            String cellStyle = null;
            long dataRowIndex = Long.parseLong(tableRow.get(0)) - 1;
            for (Data2DStyle style : styles) {
                String names = style.getColumns();
                if (names != null && !names.isBlank()) {
                    String[] cols = names.split(Data2DStyle.ColumnSeparator);
                    if (cols != null && cols.length > 0) {
                        if (!(Arrays.asList(cols).contains(colName))) {
                            continue;
                        }
                    }
                }
                long rowStart = style.getRowStart();
                if (dataRowIndex < rowStart) {
                    continue;
                }
                if (rowStart >= 0) {
                    long rowEnd = style.getRowEnd();
                    if (rowEnd >= 0 && dataRowIndex >= rowEnd) {
                        continue;
                    }
                }
                styleFilter.setData2D(this);
                if (style.filterCell(styleFilter, tableRow, tableRowIndex, colIndex)) {
                    String styleValue = style.finalStyle();
                    if (styleValue == null || styleValue.isBlank()) {
                        cellStyle = null;
                    } else if (cellStyle == null) {
                        cellStyle = style.finalStyle();
                    } else {
                        if (!cellStyle.trim().endsWith(";")) {
                            cellStyle += ";";
                        }
                        cellStyle += style.finalStyle();
                    }
                }
            }
            return cellStyle;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
