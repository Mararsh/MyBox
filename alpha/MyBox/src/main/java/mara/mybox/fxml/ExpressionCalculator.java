package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-6-13
 * @License Apache License Version 2.0
 */
public class ExpressionCalculator {

    public final Object expressionLock = new Object();
    public WebEngine webEngine;
    public FindReplaceString findReplace;
    public String expression, expressionResult, error, filterScript;
    public long filterPassedNumber, maxFilterPassed;
    public Data2D data2D;
    public boolean filterReversed, filterPassed, stopped;
    public SingletonTask task;

    public ExpressionCalculator() {
        findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
    }

    /*
        calculate
     */
    public boolean calculateExpression() {
        try {
            error = null;
            expressionResult = null;
            if (expression == null || expression.isBlank()) {
                return true;
            }
            if (webEngine == null) {
                webEngine = new WebView().getEngine();
            }
            Object o = webEngine.executeScript(expression);
            if (o != null) {
                expressionResult = o.toString();
            }
            return true;
        } catch (Exception e) {
            handleError(e);
            return false;
        }
    }

    public boolean calculateExpression(String expression) {
        this.expression = expression;
        return calculateExpression();
    }

    public boolean calculateTableRowExpression(String script, List<String> tableRow, long tableRowNumber) {
        return calculateExpression(tableRowExpression(script, tableRow, tableRowNumber));
    }

    public boolean calculateDataRowExpression(String script, List<String> dataRow, long dataRowNumber) {
        try {
            if (task == null || task.isQuit()) {
                return calculateExpression(dataRowExpression(script, dataRow, dataRowNumber));
            }
            synchronized (expressionLock) {
                this.expression = dataRowExpression(script, dataRow, dataRowNumber);
                expressionLock.notify();
                expressionLock.wait();
            }
        } catch (Exception e) {
            handleError(e);
            return false;
        }
        return true;
    }

    public boolean validateExpression(String script, boolean allPages) {
        try {
            handleError(null);
            if (script == null || script.isBlank()) {
                return true;
            }
            List<String> row = new ArrayList<>();
            for (int i = 0; i < data2D.columnsNumber(); i++) {
                row.add("0");
            }
            if (allPages) {
                return calculateExpression(dataRowExpression(script, row, 1));
            } else {
                row.add(0, "1");
                return calculateTableRowExpression(script, row, 0);
            }
        } catch (Exception e) {
            handleError(e);
            return false;
        }
    }

    public void handleError(Exception e) {
        error = e == null ? null : e.toString();
        if (data2D != null) {
            data2D.setError(error);
        }
        if (task != null) {
            task.setError(error);
        }
        if (e != null) {
            MyBoxLog.debug(error);
        }
    }


    /*
        expression
     */
    public String dataRowExpression(String script, List<String> dataRow, long dataRowNumber) {
        try {
            if (script == null || script.isBlank()
                    || dataRow == null || dataRow.isEmpty()
                    || data2D == null || !data2D.isValid()) {
                return script;
            }

            String filledScript = script;
            if (filledScript.contains("#{" + message("TableRowNumber") + "}")) {
                filledScript = message("NoTableRowNumberWhenAllPages");
            } else {
                List<String> names = data2D.columnNames();
                for (int i = 0; i < names.size(); i++) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + names.get(i) + "}", dataRow.get(i));
                }
                filledScript = findReplace.replaceStringAll(filledScript, "#{" + message("DataRowNumber") + "}", dataRowNumber + "");
            }
            return filledScript;
        } catch (Exception e) {
            handleError(e);
            return null;
        }
    }

    /*
        first value of "tableRow" should be "dataRowNumber" 
        "tableRowNumber" is 0-based while "dataRowNumber" is 1-based
     */
    public String tableRowExpression(String script, List<String> tableRow, long tableRowNumber) {
        try {
            if (script == null || script.isBlank()
                    || tableRow == null || tableRow.isEmpty()
                    || data2D == null || !data2D.isValid()) {
                return script;
            }
            String filledScript = script;
            List<String> names = data2D.columnNames();
            for (int i = 0; i < names.size(); i++) {
                filledScript = findReplace.replaceStringAll(filledScript, "#{" + names.get(i) + "}", tableRow.get(i + 1));
            }
            filledScript = findReplace.replaceStringAll(filledScript, "#{" + message("DataRowNumber") + "}", tableRow.get(0) + "");
            filledScript = findReplace.replaceStringAll(filledScript, "#{" + message("TableRowNumber") + "}",
                    tableRowNumber >= 0 ? (tableRowNumber + 1) + "" : message("NoTableRowNumberWhenAllPages"));
            return filledScript;
        } catch (Exception e) {
            handleError(e);
            return null;
        }
    }

    /*
        filter
     */
    public boolean needFilter() {
        return (filterScript != null && !filterScript.isBlank())
                || maxFilterPassed > 0;
    }

    public void startFilter() {
        filterPassedNumber = 0;
    }

    public boolean reachMaxFilterPassed() {
        return maxFilterPassed > 0 && filterPassedNumber > maxFilterPassed;
    }

    public boolean filterTableRow(List<String> tableRow, long tableRowIndex) {
        return filterTableRow(filterScript, tableRow, tableRowIndex);
    }

    public boolean filterTableRow(String script, List<String> tableRow, long tableRowIndex) {
        if (script == null || script.isBlank()) {
            filterPassed = true;
            filterPassedNumber++;
            return true;
        }
        filterPassed = calculateTableRowExpression(script, tableRow, tableRowIndex)
                && "true".equals(expressionResult);
        if (filterReversed) {
            filterPassed = !filterPassed;
        }
        if (filterPassed) {
            filterPassedNumber++;
        }
        return filterPassed;
    }

    public boolean filterDataRow(List<String> dataRow, long dataRowIndex) {
        return filterDataRow(filterScript, dataRow, dataRowIndex);
    }

    public boolean filterDataRow(String script, List<String> dataRow, long dataRowIndex) {
        try {
            error = null;
            filterPassed = false;
            if (dataRow == null) {
                return false;
            }
            if (script == null || script.isBlank()) {
                filterPassed = true;
                filterPassedNumber++;
                return true;
            }
            if (task == null || task.isQuit()) {
                return calculateExpression(dataRowExpression(script, dataRow, dataRowIndex));
            }
            synchronized (expressionLock) {
                expression = dataRowExpression(script, dataRow, dataRowIndex);
                expressionLock.notify();
                expressionLock.wait();
                filterPassed = "true".equals(expressionResult);
                expressionResult = null;
            }
            if (filterReversed) {
                filterPassed = !filterPassed;
            }
        } catch (Exception e) {
            handleError(e);
        }
        if (filterPassed) {
            filterPassedNumber++;
        }
        return filterPassed;
    }

    /*
        task
     */
    public void startService(SingletonTask inTask) {
        try {
            stopped = true;
            task = inTask;
            startFilter();
            if (task == null || task.isQuit()) {
                return;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        stopped = false;
                        while (!stopped && data2D != null && task != null && !task.isQuit()) {
                            synchronized (expressionLock) {
                                if (expression != null) {
                                    calculateExpression();
                                }
                                expression = null;
                                expressionLock.notify();
                                expressionLock.wait();
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        if (task != null) {
                            task.setError(error);
                        }
                        MyBoxLog.debug(e);
                    }
                    stopService();
                }
            });
            synchronized (expressionLock) {
                expression = null;
                expressionLock.wait();
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void stopService() {
        stopped = true;
        task = null;
        synchronized (expressionLock) {
            expression = null;
            expressionLock.notify();
        }
    }

    /*
        set
     */
    public ExpressionCalculator setWebEngine(WebEngine webEngine) {
        this.webEngine = webEngine;
        return this;
    }

    public ExpressionCalculator setData2D(Data2D data2D) {
        this.data2D = data2D;
        return this;
    }

    public ExpressionCalculator setFilterScript(String filterScript) {
        this.filterScript = filterScript;
        return this;
    }

    public ExpressionCalculator setFilterPassedNumber(long filterPassedNumber) {
        this.filterPassedNumber = filterPassedNumber;
        return this;
    }

    public ExpressionCalculator setMaxFilterPassed(long maxFilterPassed) {
        this.maxFilterPassed = maxFilterPassed;
        return this;
    }

    public ExpressionCalculator setFilterReversed(boolean filterReversed) {
        this.filterReversed = filterReversed;
        return this;
    }

    public ExpressionCalculator setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public ExpressionCalculator setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    /*
        get
     */
    public String getExpressionResult() {
        return expressionResult;
    }

    public String getError() {
        return error;
    }

    public long getMaxFilterPassed() {
        return maxFilterPassed;
    }

    public long getFilterPassedNumber() {
        return filterPassedNumber;
    }

}
