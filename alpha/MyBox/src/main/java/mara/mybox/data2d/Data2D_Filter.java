package mara.mybox.data2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-31
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Filter extends Data2D_Convert {

    public void startTask(SingletonTask task, DataFilter rowFilter) {
        this.task = task;
        this.filter = rowFilter;
        startFilter();
    }

    public void stopTask() {
        task = null;
        stopFilter();
    }

    public boolean needFilter() {
        return filter != null && filter.needFilter();
    }

    public void startFilter() {
        if (filter == null) {
            return;
        }
        filter.start(task, (Data2D) this);
    }

    public void stopFilter() {
        if (filter == null) {
            return;
        }
        filter.stop();
    }

    public boolean filterDataRow(List<String> dataRow, long dataRowIndex) {
        error = null;
        if (filter == null) {
            return true;
        }
        if (filter.filterDataRow((Data2D) this, dataRow, dataRowIndex)) {
            return true;
        } else {
            error = filter.getError();
            return false;
        }
    }

    public boolean filterPassed() {
        return filter == null || filter.passed;
    }

    public boolean filterReachMaxPassed() {
        return filter != null && filter.reachMaxPassed();
    }

    public boolean calculateDataRowExpression(String script, List<String> dataRow, long dataRowNumber) {
        error = null;
        if (filter == null) {
            return true;
        }
        if (filter.calculator == null) {
            return false;
        }
        return filter.calculator.calculateDataRowExpression((Data2D) this, script, dataRow, dataRowNumber);
    }

    public boolean calculateTableRowExpression(String script, List<String> tableRow, long tableRowNumber) {
        error = null;
        if (filter == null) {
            return true;
        }
        if (filter.calculator == null) {
            return false;
        }
        return filter.calculator.calculateTableRowExpression((Data2D) this, script, tableRow, tableRowNumber);
    }

    public String expressionError() {
        if (filter == null) {
            return null;
        }
        return filter.getError();
    }

    public String expressionResult() {
        if (filter == null) {
            return null;
        }
        return filter.getResult();
    }

    /*
        statistic values
     */
    public void countStatistic() {
        try {
            resetStatistic();
            if (filter == null || filter.calculator == null || !isValid()) {
                return;
            }
            String script = filter.script;
            if (script == null || script.isBlank()) {
                return;
            }
            for (int i = 0; i < columnsNumber(); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                DescriptiveStatistic calculation = new DescriptiveStatistic()
                        .setStatisticObject(DescriptiveStatistic.StatisticObject.Columns)
                        .setInvalidAs(Double.NaN);
                if (script.contains("#{" + name + "-" + message("Mean") + "}")) {
                    calculation.setMean(true);
                }
                if (script.contains("#{" + name + "-" + message("Median") + "}")) {
                    calculation.setMedian(true);
                }
                if (script.contains("#{" + name + "-" + message("Mode") + "}")) {
                    calculation.setMode(true);
                }
                if (script.contains("#{" + name + "-" + message("MinimumQ0") + "}")) {
                    calculation.setMinimum(true);
                }
                if (script.contains("#{" + name + "-" + message("LowerQuartile") + "}")) {
                    calculation.setLowerQuartile(true);
                }
                if (script.contains("#{" + name + "-" + message("UpperQuartile") + "}")) {
                    calculation.setUpperQuartile(true);
                }
                if (script.contains("#{" + name + "-" + message("MaximumQ4") + "}")) {
                    calculation.setMaximum(true);
                }
                if (script.contains("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}")) {
                    calculation.setLowerExtremeOutlierLine(true);
                }
                if (script.contains("#{" + name + "-" + message("LowerMildOutlierLine") + "}")) {
                    calculation.setLowerMildOutlierLine(true);
                }
                if (script.contains("#{" + name + "-" + message("UpperMildOutlierLine") + "}")) {
                    calculation.setUpperMildOutlierLine(true);
                }
                if (script.contains("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}")) {
                    calculation.setUpperExtremeOutlierLine(true);
                }
                if (!calculation.need()) {
                    continue;
                }
                List<Integer> colIndices = new ArrayList<>();
                colIndices.add(colOrder(name));
                if (calculation.needNonStored()) {
                    ((Data2D) this).statisticByColumnsWithoutStored(colIndices, calculation);
                }
                if (calculation.needStored()) {
                    ((Data2D) this).statisticByColumnsForStored(colIndices, calculation);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    /*
        style
     */
    public String cellStyle(DataFilter styleFilter, int tableRowIndex, String colName) {
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
                styleFilter.setScript(style.getFilter()).setReversed(style.isFilterReversed());
                if (!styleFilter.filterTableRow((Data2D) this, tableRow, tableRowIndex)) {
                    continue;
                }
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
            return cellStyle;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
