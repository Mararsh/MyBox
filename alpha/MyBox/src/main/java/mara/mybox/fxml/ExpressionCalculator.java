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
    public String expression, expressionResult, error;
    public Data2D data2D;
    public boolean stopped;
    public SingletonTask task;

    public ExpressionCalculator() {
        init();
    }

    private ExpressionCalculator init() {
        stopService();
        data2D = null;
        return this;
    }

    public ExpressionCalculator reset() {
        init();
        if (findReplace != null) {
            findReplace.reset();
        }
        return this;
    }

    public ExpressionCalculator reset(Data2D data2D) {
        reset();
        this.data2D = data2D;
        return this;
    }

    public ExpressionCalculator cloneEnv(ExpressionCalculator calculator) {
        reset();
        if (calculator != null) {
            webEngine = calculator.webEngine;
            findReplace = calculator.findReplace;
            if (findReplace != null) {
                findReplace.reset();
            }
            data2D = calculator.data2D;
            task = calculator.task;
        }
        return this;
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
        try {
            if (task == null || task.isQuit()) {
                return calculateExpression(tableRowExpression(script, tableRow, tableRowNumber));
            }
            synchronized (expressionLock) {
                this.expression = tableRowExpression(script, tableRow, tableRowNumber);
                expressionLock.notify();
                expressionLock.wait();
            }
        } catch (Exception e) {
            handleError(e);
            return false;
        }
        return true;
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

    public boolean calculateDataColumnExpression(String script, String value) {
        try {
            if (task == null || task.isQuit()) {
                return calculateExpression(dataColumnExpression(script, value));
            }
            synchronized (expressionLock) {
                this.expression = dataColumnExpression(script, value);
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
    public String valueExpression(String script, String name, String value) {
        try {
            if (script == null || script.isBlank()) {
                return script;
            }
            String filledScript = script;
            if (findReplace == null) {
                findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                        .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
            }
            filledScript = findReplace.replaceStringAll(filledScript, name, value);
            return filledScript;
        } catch (Exception e) {
            handleError(e);
            return null;
        }
    }

    public String dataColumnExpression(String script, String value) {
        return valueExpression(script, ColumnFilter.placehold(), value);
    }

    /*
         "dataRowNumber" is 1-based
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
                if (findReplace == null) {
                    findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                            .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
                }
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
            if (findReplace == null) {
                findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                        .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
            }
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
        task
     */
    public void startService(SingletonTask inTask) {
        try {
            stopService();
            task = inTask;
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
                                calculateExpression();
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
        expressionResult = null;
        error = null;
    }

    /*
        set
     */
    public ExpressionCalculator setWebEngine(WebEngine webEngine) {
        this.webEngine = webEngine;
        return this;
    }

    public ExpressionCalculator setFindReplace(FindReplaceString findReplace) {
        this.findReplace = findReplace;
        return this;
    }

    public ExpressionCalculator setData2D(Data2D data2D) {
        this.data2D = data2D;
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

}
