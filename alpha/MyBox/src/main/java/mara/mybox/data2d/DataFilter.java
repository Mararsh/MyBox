package mara.mybox.data2d;

import java.util.List;
import mara.mybox.calculation.ExpressionCalculator;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class DataFilter {

    private String sourceScript, filledScript;
    public long passedNumber, maxPassed;
    public boolean matchFalse, passed;
    public FxTask task;
    public ExpressionCalculator calculator;

    public DataFilter() {
        init();
    }

    public DataFilter(String script, boolean matchFalse) {
        init();
        sourceScript = script;
        filledScript = script;
        this.matchFalse = matchFalse;
    }

    private void init() {
        maxPassed = -1;
        matchFalse = false;
        resetNumber();
        calculator = new ExpressionCalculator();
    }

    public void resetNumber() {
        passedNumber = 0;
        passed = false;
    }

    public void start(FxTask task, Data2D data2D) {
        resetNumber();
        this.task = task;
        calculator.reset();
    }

    public void stop() {
        resetNumber();
        task = null;
        calculator.reset();
    }

    public static DataFilter create() {
        return new DataFilter();
    }

    public boolean scriptEmpty() {
        return sourceScript == null || sourceScript.isBlank();
    }

    public boolean needFilter() {
        return !scriptEmpty() || maxPassed > 0;
    }

    public boolean reachMaxPassed() {
        return maxPassed > 0 && passedNumber > maxPassed;
    }

    public boolean readResult(boolean calculateSuccess) {
        passed = false;
        if (calculateSuccess) {
            if (matchFalse) {
                passed = "false".equals(calculator.getResult());
            } else {
                passed = "true".equals(calculator.getResult());
            }
        }
        if (passed) {
            passedNumber++;
        }
        handleError(calculator.getError());
        return passed;
    }

    public boolean filterTableRow(Data2D data2D, List<String> tableRow, long tableRowIndex) {
        if (tableRow == null) {
            passed = false;
            return false;
        }
        if (sourceScript == null || sourceScript.isBlank()) {
            passed = true;
            passedNumber++;
            return true;
        }
        return readResult(calculator.calculateTableRowExpression(data2D, filledScript, tableRow, tableRowIndex));
    }

    public boolean filterDataRow(Data2D data2D, List<String> dataRow, long dataRowIndex) {
        try {
            handleError(null);
            if (dataRow == null || data2D == null) {
                passed = false;
                return false;
            }
            if (sourceScript == null || sourceScript.isBlank()) {
                passed = true;
                passedNumber++;
                return true;
            }
            return readResult(calculator.calculateDataRowExpression(data2D, filledScript, dataRow, dataRowIndex));
        } catch (Exception e) {
            passed = false;
            MyBoxLog.error(e);
            return false;
        }
    }

    public String getError() {
        return calculator.getError();
    }

    public String getResult() {
        return calculator.getResult();
    }

    public void handleError(String error) {
//        if (error != null && AppValues.Alpha) {
//            if (task != null) {
//                task.setError(error);
//            }
//            MyBoxLog.debug(error + "\n" + sourceScript);
//        }
    }

    public void clear() {
        sourceScript = null;
        filledScript = null;
        maxPassed = -1;
        matchFalse = false;
        passedNumber = 0;
        passed = false;
        task = null;
        calculator.reset();
    }

    @Override
    public String toString() {
        String string = sourceScript == null ? "" : sourceScript;
        if (matchFalse) {
            string += "\n" + message("MatchFalse");
        }
        if (maxPassed > 0) {
            string += "\n" + message("MaximumNumber") + ": " + maxPassed;
        }
        return string;
    }

    /*
        get/set
     */
    public FxTask getTask() {
        return task;
    }

    public DataFilter setTask(FxTask task) {
        this.task = task;
        return this;
    }

    public String getSourceScript() {
        return sourceScript;
    }

    public DataFilter setSourceScript(String sourceScript) {
        this.sourceScript = sourceScript;
        this.filledScript = sourceScript;
        return this;
    }

    public String getFilledScript() {
        return filledScript;
    }

    public DataFilter setFilledScript(String filledScript) {
        this.filledScript = filledScript;
        return this;
    }

    public long getPassedNumber() {
        return passedNumber;
    }

    public DataFilter setPassedNumber(long passedNumber) {
        this.passedNumber = passedNumber;
        return this;
    }

    public long getMaxPassed() {
        return maxPassed;
    }

    public DataFilter setMaxPassed(long maxPassed) {
        this.maxPassed = maxPassed;
        return this;
    }

    public boolean isMatchFalse() {
        return matchFalse;
    }

    public DataFilter setMatchFalse(boolean matchFalse) {
        this.matchFalse = matchFalse;
        return this;
    }

    public boolean isPassed() {
        return passed;
    }

    public DataFilter setPassed(boolean passed) {
        this.passed = passed;
        return this;
    }

}
