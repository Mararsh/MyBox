package mara.mybox.data2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data.FindReplaceString;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-31
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Filter extends Data2D_Data {

    public void startTask(FxTask task, DataFilter filter) {
        this.task = task;
        this.filter = filter;
        startFilter();
    }

    public void startFilter(DataFilter filter) {
        this.filter = filter;
        startFilter();
    }

    public void stopTask() {
        task = null;
        stopFilter();
    }

    public String filterScipt() {
        return filter == null ? null : filter.getFilledScript();
    }

    public boolean filterEmpty() {
        return filter == null || filter.scriptEmpty();
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

    public void resetFilterNumber() {
        if (filter == null) {
            return;
        }
        filter.resetNumber();
    }

    public boolean filterTableRow(List<String> tableRow, long tableRowIndex) {
        error = null;
        if (filter == null) {
            return true;
        }
        if (filter.filterTableRow((Data2D) this, tableRow, tableRowIndex)) {
            return true;
        } else {
            error = filter.getError();
            return false;
        }
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

    public boolean fillFilterStatistic() {
        if (filter == null || filter.scriptEmpty()) {
            return true;
        }
        String filled = calculateScriptStatistic(filter.getSourceScript());
        if (filled == null || filled.isBlank()) {
            return false;
        }
        filter.setFilledScript(filled);
        return true;
    }

    public String calculateScriptStatistic(String script) {
        if (script == null || script.isBlank()) {
            return script;
        }
        List<String> scripts = new ArrayList<>();
        scripts.add(script);
        scripts = calculateScriptsStatistic(scripts);
        if (scripts == null || scripts.isEmpty()) {
            return null;
        }
        return scripts.get(0);
    }

    public List<String> calculateScriptsStatistic(List<String> scripts) {
        try {
            if (scripts == null || scripts.isEmpty()) {
                return scripts;
            }
            DescriptiveStatistic calculation = new DescriptiveStatistic()
                    .setStatisticObject(DescriptiveStatistic.StatisticObject.Columns)
                    .setInvalidAs(InvalidAs.Blank);
            List<Integer> colIndices = new ArrayList<>();
            for (String script : scripts) {
                checkFilterStatistic(script, calculation, colIndices);
            }
            if (!calculation.need()) {
                return scripts;
            }
            DoubleStatistic[] sData = null;
            if (isTmpData()) {
                sData = ((Data2D) this).statisticByColumnsForCurrentPage(colIndices, calculation);
            } else {
                Data2D filter2D = ((Data2D) this).cloneAll();
                filter2D.resetStatistic();
                filter2D.startTask(task, null);
                if (calculation.needNonStored()) {
                    sData = filter2D.statisticByColumnsWithoutStored(colIndices, calculation);
                }
                if (calculation.needStored()) {
                    sData = filter2D.statisticByColumnsForStored(colIndices, calculation);
                }
                filter2D.stopTask();
            }
            if (sData == null) {
                return null;
            }
            for (int c = 0; c < colIndices.size(); c++) {
                column(colIndices.get(c)).setStatistic(sData[c]);
            }
            List<String> filled = new ArrayList<>();
            FindReplaceString findReplace = ExpressionCalculator.createReplaceAll();
            for (String script : scripts) {
                if (task == null || !task.isWorking()) {
                    return null;
                }
                filled.add(replaceFilterStatistic(findReplace, script));
            }
            return filled;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public void checkFilterStatistic(String script, DescriptiveStatistic calculation, List<Integer> colIndices) {
        try {
            if (script == null || script.isBlank() || calculation == null || colIndices == null) {
                return;
            }
            for (int i = 0; i < columnsNumber(); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                for (StatisticType stype : StatisticType.values()) {
                    if (script.contains("#{" + name + "-" + message(stype.name()) + "}")) {
                        calculation.add(stype);
                    }
                }
                if (!calculation.need()) {
                    continue;
                }
                int col = colOrder(name);
                if (!colIndices.contains(col)) {
                    colIndices.add(col);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public String replaceFilterStatistic(FindReplaceString findReplace, String script) {
        try {
            if (!isValid() || script == null || script.isBlank()) {
                return script;
            }
            String filledScript = script;
            for (int i = 0; i < columnsNumber(); i++) {
                if (task == null || !task.isWorking()) {
                    return message("Canceled");
                }
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                if (filledScript.contains("#{" + name + "-" + message("Mean") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().mean)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("Mean") + "}",
                            column.getStatistic().mean + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("Median") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().median)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("Median") + "}",
                            column.getStatistic().median + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("Mode") + "}")) {
                    if (column.getStatistic() == null || column.getStatistic().modeValue == null) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("Mode") + "}",
                            column.getStatistic().modeValue.toString());
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("MinimumQ0") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().minimum)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("MinimumQ0") + "}",
                            column.getStatistic().minimum + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerQuartile") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().lowerQuartile)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("LowerQuartile") + "}",
                            column.getStatistic().lowerQuartile + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperQuartile") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().upperQuartile)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("UpperQuartile") + "}",
                            column.getStatistic().upperQuartile + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("MaximumQ4") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().maximum)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("MaximumQ4") + "}",
                            column.getStatistic().maximum + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().lowerExtremeOutlierLine)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("LowerExtremeOutlierLine") + "}",
                            column.getStatistic().lowerExtremeOutlierLine + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerMildOutlierLine") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().lowerMildOutlierLine)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("LowerMildOutlierLine") + "}",
                            column.getStatistic().lowerMildOutlierLine + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperMildOutlierLine") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().upperMildOutlierLine)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("UpperMildOutlierLine") + "}",
                            column.getStatistic().upperMildOutlierLine + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}")) {
                    if (column.getStatistic() == null || DoubleTools.invalidDouble(column.getStatistic().upperExtremeOutlierLine)) {
                        return null;
                    }
                    filledScript = findReplace.replace(task, filledScript, "#{" + name + "-" + message("UpperExtremeOutlierLine") + "}",
                            column.getStatistic().upperExtremeOutlierLine + "");
                    if (task == null || !task.isWorking()) {
                        return message("Canceled");
                    }
                }
            }
            return filledScript;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public String cellStyle(DataFilter styleFilter, int tableRowIndex, String colName) {
        try {
            if (styleFilter == null || styles == null || styles.isEmpty() || colName == null || colName.isBlank()) {
                return null;
            }
            List<String> tableRow = pageRow(tableRowIndex);
            if (tableRow == null || tableRow.size() < 1) {
                return null;
            }
            int colIndex = colOrder(colName);
            if (colIndex < 0) {
                return null;
            }
            String cellStyle = null;
            long dataRowIndex;
            try {
                dataRowIndex = Long.parseLong(tableRow.get(0)) - 1;
            } catch (Exception e) {
                dataRowIndex = -1;
            }
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
                if (rowStart >= 0) {
                    if (dataRowIndex <= 0 || dataRowIndex < rowStart) {
                        continue;
                    }
                    long rowEnd = style.getRowEnd();
                    if (rowEnd >= 0 && dataRowIndex >= rowEnd) {
                        continue;
                    }
                }
                styleFilter.setSourceScript(style.getFilter()).setReversed(style.isFilterReversed());
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

    public List<String> namesInScript(String script) {
        if (script == null || script.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (String name : columnNames()) {
            if (script.contains("#{" + name + "}")
                    || script.contains("#{" + name + "}-" + message("Mean"))
                    || script.contains("#{" + name + "}-" + message("Median"))
                    || script.contains("#{" + name + "}-" + message("Mode"))
                    || script.contains("#{" + name + "}-" + message("MinimumQ0"))
                    || script.contains("#{" + name + "}-" + message("LowerQuartile"))
                    || script.contains("#{" + name + "}-" + message("UpperQuartile"))
                    || script.contains("#{" + name + "}-" + message("MaximumQ4"))
                    || script.contains("#{" + name + "}-" + message("LowerExtremeOutlierLine"))
                    || script.contains("#{" + name + "}-" + message("LowerMildOutlierLine"))
                    || script.contains("#{" + name + "}-" + message("UpperMildOutlierLine"))
                    || script.contains("#{" + name + "}-" + message("UpperExtremeOutlierLine"))) {
                names.add(name);
            }
        }
        return names;
    }

}
