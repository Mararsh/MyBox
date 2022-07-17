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

    public final Object lock = new Object();
    public WebEngine webEngine;
    public FindReplaceString findReplace;
    public String expression, result, error;
    public boolean serviceRunning;

    public ExpressionCalculator() {
        init();
    }

    private ExpressionCalculator init() {
        stop();
        return this;
    }

    /*
        calculate
     */
    public boolean calculate() {
        try {
            error = null;
            result = null;
            if (expression == null || expression.isBlank()) {
                return true;
            }
            webEngine = getWebEngine();
            Object o = webEngine.executeScript(expression);
            if (o != null) {
                result = o.toString();
            }
            return true;
        } catch (Exception e) {
            handleError(e);
            return false;
        }
    }

    public boolean calculate(String expression) {
        try {
            if (Platform.isFxApplicationThread()) {
                this.expression = expression;
                return calculate();
            }
            start();
            synchronized (lock) {
                this.expression = expression;
                lock.notify();
                lock.wait();
            }
            return true;
        } catch (Exception e) {
            handleError(e);
            return false;
        }
    }

    public void handleError(Exception e) {
        error = e == null ? null : e.toString();
        if (e != null) {
            MyBoxLog.console(error);
        }
    }

    /*
        service
     */
    public void start() {
        try {
            if (serviceRunning || Platform.isFxApplicationThread()) {
                return;
            }
            stop();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        serviceRunning = true;
                        while (serviceRunning) {
                            synchronized (lock) {
                                calculate();
                                expression = null;
                                lock.notify();
                                lock.wait();
                            }
                        }
                    } catch (Exception e) {
                        handleError(e);
                    }
                    stop();
                }
            });
            synchronized (lock) {
                expression = null;
                lock.wait();
            }
        } catch (Exception e) {
            handleError(e);
            stop();
        }
    }

    public void stop() {
        serviceRunning = false;
        synchronized (lock) {
            expression = null;
            lock.notify();
        }
        result = null;
        error = null;
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
            filledScript = getFindReplace().replaceStringAll(filledScript, name, value);
            return filledScript;
        } catch (Exception e) {
            handleError(e);
            return null;
        }
    }

    public String dataColumnExpression(Data2D data2D, String script, String value) {
        return valueExpression(script, ColumnFilter.placehold(), value);
    }

    /*
         "dataRowNumber" is 1-based
     */
    public String dataRowExpression(Data2D data2D, String script, List<String> dataRow, long dataRowNumber) {
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
                findReplace = getFindReplace();
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
    public String tableRowExpression(Data2D data2D, String script, List<String> tableRow, long tableRowNumber) {
        try {
            if (script == null || script.isBlank()
                    || tableRow == null || tableRow.isEmpty()
                    || data2D == null || !data2D.isValid()) {
                return script;
            }
            String filledScript = script;
            List<String> names = data2D.columnNames();
            findReplace = getFindReplace();
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

    public ExpressionCalculator setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    /*
        get
     */
    public WebEngine getWebEngine() {
        if (webEngine == null) {
            webEngine = new WebView().getEngine();
        }
        return webEngine;
    }

    public FindReplaceString getFindReplace() {
        if (findReplace == null) {
            findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
        }
        return findReplace;
    }

    public String getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

    public boolean calculateTableRowExpression(Data2D data2D,
            String script, List<String> tableRow, long tableRowNumber) {
        return calculate(tableRowExpression(data2D, script, tableRow, tableRowNumber));
    }

    public boolean calculateDataRowExpression(Data2D data2D,
            String script, List<String> dataRow, long dataRowNumber) {
        return calculate(dataRowExpression(data2D, script, dataRow, dataRowNumber));
    }

    public boolean calculateDataColumnExpression(Data2D data2D, String script, String value) {
        return calculate(dataColumnExpression(data2D, script, value));
    }

    public boolean validateExpression(Data2D data2D, String script, boolean allPages) {
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
                return calculate(dataRowExpression(data2D, script, row, 1));
            } else {
                row.add(0, "1");
                return calculateTableRowExpression(data2D, script, row, 0);
            }
        } catch (Exception e) {
            handleError(e);
            return false;
        }
    }

}
