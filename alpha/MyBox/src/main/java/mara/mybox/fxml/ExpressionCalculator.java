package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.value.Languages.message;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 * @Author Mara
 * @CreateDate 2022-6-13
 * @License Apache License Version 2.0
 */
public class ExpressionCalculator {

    public final static String VariablePrefix = "__MyBox_VRB_";
    public static ScriptEngine scriptEngine;

    public String expression, result, error;
    public Map<String, String> variableNames;
    public Bindings variableValues;
    public FindReplaceString replaceHandler;

    public ExpressionCalculator() {
        // https://github.com/Mararsh/MyBox/issues/1568
        if (scriptEngine == null) {
            ScriptEngineManager factory = new ScriptEngineManager(ClassLoader.getSystemClassLoader());
            factory.registerEngineName("nashorn", new NashornScriptEngineFactory());
            scriptEngine = factory.getEngineByName("nashorn");
        }
        variableValues = scriptEngine.createBindings();
        variableNames = new HashMap<>();
        reset();
    }

    final public void reset() {
        expression = null;
        result = null;
        error = null;
        variableNames.clear();
        variableValues.clear();
        if (replaceHandler == null) {
            replaceHandler = createReplaceAll();
        }
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
            scriptEngine.setBindings(variableValues, ScriptContext.ENGINE_SCOPE);
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
        reset();
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
//        if (e != null && AppValues.Alpha) {
//            MyBoxLog.debug(error + "\n" + info());
//        }
    }

    public String info() {
        String info = "expression: " + expression;
        for (String variable : variableValues.keySet()) {
            info += "\n" + variable + ": " + variableValues.get(variable);
        }
        return info;
    }

    /*
        "dataRowNumber" is 1-based
     */
    public boolean makeExpression(Data2D data2D,
            String script, List<String> rowValues, long dataRowNumber) {

        try {
            reset();
            if (script == null || script.isBlank()
                    || rowValues == null || rowValues.isEmpty()
                    || data2D == null || !data2D.isValidDefinition()) {
                handleError(message("invalidParameter"));
                return false;
            }
            if (script.contains("#{" + message("TableRowNumber") + "}")) {
                handleError(message("NoTableRowNumberWhenAllPages"));
                return false;
            }
            expression = script;
            int index = 1, rowSize = rowValues.size();
            String value;
            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn column = data2D.getColumns().get(i);
                String name = column.getColumnName();
                value = i < rowSize ? rowValues.get(i) : null;
                String placeholder = "#{" + name + "}";
                String placeholderQuoted = "'" + placeholder + "'";
                String variableName = VariablePrefix + index++;
                if (expression.contains(placeholderQuoted)) {
                    expression = replaceAll(expression, placeholderQuoted, variableName);
                }
                expression = replaceAll(expression, placeholder, variableName);
                variableNames.put(placeholderQuoted, variableName);
                variableValues.put(variableName, value);
            }
            expression = replaceAll(expression, "#{" + message("DataRowNumber") + "}", dataRowNumber + "");
//            MyBoxLog.console(info());
            return true;
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    /*
        first value of "tableRow" should be "dataRowNumber" 
        "tableRowNumber" is 0-based
     */
    public boolean calculateTableRowExpression(Data2D data2D,
            String script, List<String> tableRow, long tableRowNumber) {
        try {
            reset();
            if (tableRow == null || tableRow.isEmpty()) {
                handleError(message("invalidParameter"));
                return false;
            }
            if (tableRowNumber < 0) {
                handleError(message("NoTableRowNumberWhenAllPages"));
                return false;
            }
            if (!makeExpression(data2D, script,
                    tableRow.subList(1, tableRow.size()),
                    Long.parseLong(tableRow.get(0)))) {
                return false;
            }
            expression = replaceAll(expression, "#{" + message("TableRowNumber") + "}",
                    (tableRowNumber + 1) + "");
            return executeScript();

        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    public boolean calculateDataRowExpression(Data2D data2D,
            String script, List<String> dataRow, long dataRowNumber) {

        try {
            reset();
            if (script != null && script.contains("#{" + message("TableRowNumber") + "}")) {
                handleError(message("NoTableRowNumberWhenAllPages"));
                return false;
            }
            if (!makeExpression(data2D, script, dataRow, dataRowNumber)) {
                return false;
            }
            return executeScript();
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
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
            if (data2D == null || !data2D.isValidDefinition() || script == null || script.isBlank()) {
                return script;
            }
            String filledScript = script;

            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn column = data2D.columns.get(i);
                String name = column.getColumnName();
                for (StatisticType stype : StatisticType.values()) {
                    filledScript = replaceAll(filledScript, "#{" + name + "-" + message(stype.name()) + "}", "1");
                }
            }
            return filledScript;
        } catch (Exception e) {
            handleError(e.toString());
            return null;
        }
    }

    public String replaceAll(String script, String string, String replaced) {
        return replaceHandler.replace(null, script, string, replaced);
    }


    /*
        get
     */
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
        return FindReplaceString.create()
                .setOperation(FindReplaceString.Operation.ReplaceAll)
                .setIsRegex(false)
                .setCaseInsensitive(false)
                .setMultiline(false);
    }

}
