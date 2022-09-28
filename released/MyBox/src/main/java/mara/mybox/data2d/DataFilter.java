package mara.mybox.data2d;

import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.SingletonTask;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class DataFilter {

    private String sourceScript, filledScript;
    public long passedNumber, maxPassed;
    public boolean reversed, passed;
    public SingletonTask task;
    public ExpressionCalculator calculator;

    public DataFilter() {
        init();
    }

    public DataFilter(String script, boolean reversed) {
        init();
        sourceScript = script;
        filledScript = script;
        this.reversed = reversed;
    }

    private void init() {
        maxPassed = -1;
        reversed = false;
        resetNumber();
        calculator = new ExpressionCalculator();
    }

    public void resetNumber() {
        passedNumber = 0;
        passed = false;
    }

    public void start(SingletonTask task, Data2D data2D) {
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
            if (reversed) {
                passed = !"true".equals(calculator.getResult());
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
        if (task != null) {
            task.setError(error);
        }
        if (error != null) {
            MyBoxLog.debug(error + "\n" + sourceScript);
        }
    }

    /*
        get/set
     */
    public SingletonTask getTask() {
        return task;
    }

    public DataFilter setTask(SingletonTask task) {
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

    public boolean isReversed() {
        return reversed;
    }

    public DataFilter setReversed(boolean reversed) {
        this.reversed = reversed;
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
