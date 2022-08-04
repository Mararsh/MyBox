package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
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
    private boolean executeScript() {
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
            handleError(e.toString());
            return false;
        }
    }

    public boolean calculate(String expression) {
        try {
            if (Platform.isFxApplicationThread()) {
                this.expression = expression;
                return executeScript();
            }
            start();
            synchronized (lock) {
                this.expression = expression;
                lock.notify();
                lock.wait();
            }
            return true;
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    public void handleError(String e) {
        error = e;
        if (e != null) {
            MyBoxLog.debug(error + "\n" + expression);
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
                                executeScript();
                                expression = null;
                                lock.notify();
                                lock.wait();
                            }
                        }
                    } catch (Exception e) {
                        handleError(e.toString());
                    }
                    stop();
                }
            });
            synchronized (lock) {
                expression = null;
                lock.wait();
            }
        } catch (Exception e) {
            handleError(e.toString());
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
            findReplace = getFindReplace();
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
            handleError(e.toString());
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
            findReplace = getFindReplace();
            String filledScript = script;
            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn column = data2D.getColumns().get(i);
                String name = column.getColumnName();
                filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "}", tableRow.get(i + 1));
            }
            filledScript = findReplace.replaceStringAll(filledScript, "#{" + message("DataRowNumber") + "}", tableRow.get(0) + "");
            filledScript = findReplace.replaceStringAll(filledScript, "#{" + message("TableRowNumber") + "}",
                    tableRowNumber >= 0 ? (tableRowNumber + 1) + "" : message("NoTableRowNumberWhenAllPages"));
            return filledScript;
        } catch (Exception e) {
            handleError(e.toString());
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
            findReplace = createFindReplace();
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
        expression = tableRowExpression(data2D, script, tableRow, tableRowNumber);
        if (expression == null || expression.isBlank()) {
            handleError(message("InvalidExpression"));
            return false;
        }
        return calculate(expression);
    }

    public boolean calculateDataRowExpression(Data2D data2D,
            String script, List<String> dataRow, long dataRowNumber) {
        expression = dataRowExpression(data2D, script, dataRow, dataRowNumber);
        if (expression == null || expression.isBlank()) {
            handleError(message("InvalidExpression"));
            return false;
        }
        return calculate(expression);
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
            String filledScript = replaceDummyStatistic(data2D, script);
            if (allPages) {
                return calculateDataRowExpression(data2D, filledScript, row, 1);
            } else {
                row.add(0, "1");
                return calculateTableRowExpression(data2D, filledScript, row, 0);
            }
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    public String replaceDummyStatistic(Data2D data2D, String script) {
        try {
            if (data2D == null || !data2D.isValid() || script == null || script.isBlank()) {
                return script;
            }
            String filledScript = script;
            findReplace = getFindReplace();
            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn column = data2D.columns.get(i);
                String name = column.getColumnName();
                if (filledScript.contains("#{" + name + "-" + message("Mean") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("Mean") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("Median") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("Median") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("Mode") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("Mode") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("MinimumQ0") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("MinimumQ0") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerQuartile") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("LowerQuartile") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperQuartile") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("UpperQuartile") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("MaximumQ4") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("MaximumQ4") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("LowerExtremeOutlierLine") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerMildOutlierLine") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("LowerMildOutlierLine") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperMildOutlierLine") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("UpperMildOutlierLine") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}")) {
                    filledScript = findReplace.replaceStringAll(filledScript, "#{" + name + "-" + message("UpperExtremeOutlierLine") + "}", "1");
                }
            }
            return filledScript;
        } catch (Exception e) {
            handleError(e.toString());
            return null;
        }
    }

    /*
        static
     */
    public static FindReplaceString createFindReplace() {
        return FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
    }
}
