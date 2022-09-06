package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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

    public ScriptEngine scriptEngine;
    public FindReplaceString findReplace;
    public String expression, result, error;

    public ExpressionCalculator() {
        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName("nashorn");
        reset();
    }

    final public void reset() {
        expression = null;
        result = null;
        error = null;
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
            Object o = scriptEngine.eval(expression);
            if (o != null) {
                result = o.toString();
            }
            return true;
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    public String calculate(String expression) {
        this.expression = expression;
        if (executeScript()) {
            return result;
        } else {
            return null;
        }
    }

    public void handleError(String e) {
        error = e;
        if (e != null) {
            MyBoxLog.debug(error + "\n" + expression);
        }
    }

    public static String eval(String script) {
        ExpressionCalculator calculator = new ExpressionCalculator();
        return calculator.calculate(script);
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

                List<String> names = data2D.columnNames();
                for (int i = 0; i < names.size(); i++) {
                    filledScript = replaceStringAll(filledScript, "#{" + names.get(i) + "}", dataRow.get(i));
                }
                filledScript = replaceStringAll(filledScript, "#{" + message("DataRowNumber") + "}", dataRowNumber + "");
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

            String filledScript = script;
            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn column = data2D.getColumns().get(i);
                String name = column.getColumnName();
                filledScript = replaceStringAll(filledScript, "#{" + name + "}", tableRow.get(i + 1));
            }
            filledScript = replaceStringAll(filledScript, "#{" + message("DataRowNumber") + "}", tableRow.get(0) + "");
            filledScript = replaceStringAll(filledScript, "#{" + message("TableRowNumber") + "}",
                    tableRowNumber >= 0 ? (tableRowNumber + 1) + "" : message("NoTableRowNumberWhenAllPages"));
            return filledScript;
        } catch (Exception e) {
            handleError(e.toString());
            return null;
        }
    }

    /*
     calculate
     */
    public boolean calculateTableRowExpression(Data2D data2D,
            String script, List<String> tableRow, long tableRowNumber) {
        expression = tableRowExpression(data2D, script, tableRow, tableRowNumber);
        if (expression == null || expression.isBlank()) {
            handleError(message("InvalidExpression"));
            return false;
        }
        return calculate(expression) != null;
    }

    public boolean calculateDataRowExpression(Data2D data2D,
            String script, List<String> dataRow, long dataRowNumber) {
        expression = dataRowExpression(data2D, script, dataRow, dataRowNumber);
        if (expression == null || expression.isBlank()) {
            handleError(message("InvalidExpression"));
            return false;
        }
        return calculate(expression) != null;
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

            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn column = data2D.columns.get(i);
                String name = column.getColumnName();
                if (filledScript.contains("#{" + name + "-" + message("Mean") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("Mean") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("Median") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("Median") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("Mode") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("Mode") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("MinimumQ0") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("MinimumQ0") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerQuartile") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("LowerQuartile") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperQuartile") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("UpperQuartile") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("MaximumQ4") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("MaximumQ4") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("LowerExtremeOutlierLine") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("LowerMildOutlierLine") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("LowerMildOutlierLine") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperMildOutlierLine") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("UpperMildOutlierLine") + "}", "1");
                }
                if (filledScript.contains("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}")) {
                    filledScript = replaceStringAll(filledScript, "#{" + name + "-" + message("UpperExtremeOutlierLine") + "}", "1");
                }
            }
            return filledScript;
        } catch (Exception e) {
            handleError(e.toString());
            return null;
        }
    }

    public String replaceStringAll(String script, String string, String replaced) {
        return getFindReplace().replaceStringAll(script, string, replaced);
    }

    /*
        set
     */
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

    /*
        static
     */
    public static FindReplaceString createFindReplace() {
        return FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
    }
}
