package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 * @Author Mara
 * @CreateDate 2022-6-13
 * @License Apache License Version 2.0
 */
public class ExpressionCalculator {

    public static ScriptEngine scriptEngine;
    public FindReplaceString findReplace;
    public String expression, result, error;

    public ExpressionCalculator() {
        // https://github.com/Mararsh/MyBox/issues/1568
        if (scriptEngine == null) {
            ScriptEngineManager factory = new ScriptEngineManager(ClassLoader.getSystemClassLoader());
            factory.registerEngineName("nashorn", new NashornScriptEngineFactory());
            scriptEngine = factory.getEngineByName("nashorn");
        }
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

    public String calculate(String script) {
        this.expression = script;
        if (executeScript()) {
            return result;
        } else {
            return null;
        }
    }

    public boolean condition(String script) {
        calculate(script);
        return "true".equals(result);
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
                for (int i = 0; i < data2D.columnsNumber(); i++) {
                    Data2DColumn column = data2D.getColumns().get(i);
                    String name = column.getColumnName();
                    filledScript = replace(filledScript, "#{" + name + "}", column.filterValue(dataRow.get(i)));
                }
                filledScript = replace(filledScript, "#{" + message("DataRowNumber") + "}", dataRowNumber + "");
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
                filledScript = replace(filledScript, "#{" + name + "}", column.filterValue(tableRow.get(i + 1)));
            }
            filledScript = replace(filledScript, "#{" + message("DataRowNumber") + "}", tableRow.get(0) + "");
            filledScript = replace(filledScript, "#{" + message("TableRowNumber") + "}",
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
                for (StatisticType stype : StatisticType.values()) {
                    filledScript = replace(filledScript, "#{" + name + "-" + message(stype.name()) + "}", "1");
                }
            }
            return filledScript;
        } catch (Exception e) {
            handleError(e.toString());
            return null;
        }
    }

    public String replace(String script, String string, String replaced) {
        return getFindReplace().replace(script, string, replaced);
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
            findReplace = createReplaceAll();
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
    public static FindReplaceString createReplaceAll() {
        return FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
    }
}
