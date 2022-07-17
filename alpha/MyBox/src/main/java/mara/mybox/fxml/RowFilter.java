package mara.mybox.fxml;

import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class RowFilter {

    public static String ReversedPrefix = "Reversed::";

    public Data2D data2D;
    public String script;
    public long passedNumber, maxPassed;
    public boolean reversed, passed;
    public SingletonTask task;
    public ExpressionCalculator calculator;

    public RowFilter() {
        init();
    }

    public RowFilter(String script, boolean reversed) {
        init();
        this.script = script;
        this.reversed = reversed;
    }

    private void init() {
        passedNumber = 0;
        maxPassed = -1;
        reversed = false;
        passed = false;
        calculator = new ExpressionCalculator();
    }

    public void start(SingletonTask task, Data2D data2D) {
        passedNumber = 0;
        passed = false;
        this.task = task;
        this.data2D = data2D;
        calculator.start();
    }

    public void stop() {
        passedNumber = 0;
        passed = false;
        task = null;
        calculator.stop();
    }

    public static RowFilter create() {
        return new RowFilter();
    }

    public RowFilter fromString(String rowFilterString) {
        if (rowFilterString == null || rowFilterString.isBlank()) {
            script = null;
        } else {
            if (rowFilterString.startsWith(ReversedPrefix)) {
                script = rowFilterString.substring(ReversedPrefix.length());
                reversed = true;
            } else {
                script = rowFilterString;
                reversed = false;
            }
        }
        return this;
    }

    @Override
    public String toString() {
        String rowFilterString;
        if (script == null) {
            rowFilterString = null;
        } else {
            rowFilterString = (reversed ? ReversedPrefix : "") + script;
        }
        return rowFilterString;
    }

    public boolean needFilter() {
        return (script != null && !script.isBlank())
                || maxPassed > 0;
    }

    public boolean reachMaxPassed() {
        return maxPassed > 0 && passedNumber > maxPassed;
    }

    public boolean readResult(boolean calculateSuccess) {
        passed = false;
        if (calculateSuccess) {
            if (reversed) {
                if ("false".equals(calculator.getResult())) {
                    passed = true;
                }
            } else {
                if ("true".equals(calculator.getResult())) {
                    passed = true;
                }
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
        if (script == null || script.isBlank()) {
            passed = true;
            passedNumber++;
            return true;
        }
        return readResult(calculator.calculateTableRowExpression(data2D, script, tableRow, tableRowIndex));
    }

    public boolean filterDataRow(Data2D data2D, List<String> dataRow, long dataRowIndex) {
        try {
            handleError(null);
            if (dataRow == null || data2D == null) {
                passed = false;
                return false;
            }
            if (script == null || script.isBlank()) {
                passed = true;
                passedNumber++;
                return true;
            }
            return readResult(calculator.calculateDataRowExpression(data2D, script, dataRow, dataRowIndex));
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
            MyBoxLog.console(error);
        }
    }

    /*
        get/set
     */
    public Data2D getData2D() {
        return data2D;
    }

    public RowFilter setData2D(Data2D data2D) {
        this.data2D = data2D;
        return this;
    }

    public SingletonTask getTask() {
        return task;
    }

    public RowFilter setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    public String getScript() {
        return script;
    }

    public RowFilter setScript(String script) {
        this.script = script;
        return this;
    }

    public long getPassedNumber() {
        return passedNumber;
    }

    public RowFilter setPassedNumber(long passedNumber) {
        this.passedNumber = passedNumber;
        return this;
    }

    public long getMaxPassed() {
        return maxPassed;
    }

    public RowFilter setMaxPassed(long maxPassed) {
        this.maxPassed = maxPassed;
        return this;
    }

    public boolean isReversed() {
        return reversed;
    }

    public RowFilter setReversed(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    public boolean isPassed() {
        return passed;
    }

    public RowFilter setPassed(boolean passed) {
        this.passed = passed;
        return this;
    }

}
