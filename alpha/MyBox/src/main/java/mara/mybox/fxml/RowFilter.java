package mara.mybox.fxml;

import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class RowFilter extends ExpressionCalculator {

    public static String ReversedPrefix = "Reversed::";

    public String script;
    public long passedNumber, maxPassed;
    public boolean reversed, passed;

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

    @Override
    public void stopService() {
        super.stopService();
        passedNumber = 0;
        passed = false;
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
                if ("false".equals(expressionResult)) {
                    passed = true;
                }
            } else {
                if ("true".equals(expressionResult)) {
                    passed = true;
                }
            }
        }
        if (passed) {
            passedNumber++;
        }
        return passed;
    }

    public boolean filterTableRow(List<String> tableRow, long tableRowIndex) {
        error = null;
        if (tableRow == null) {
            passed = false;
            return false;
        }
        if (script == null || script.isBlank()) {
            passed = true;
            passedNumber++;
            return true;
        }
        return readResult(calculateTableRowExpression(script, tableRow, tableRowIndex));
    }

    public boolean filterDataRow(List<String> dataRow, long dataRowIndex) {
        try {
            error = null;
            if (dataRow == null) {
                passed = false;
                return false;
            }
            if (script == null || script.isBlank()) {
                passed = true;
                passedNumber++;
                return true;
            }
            return readResult(calculateDataRowExpression(script, dataRow, dataRowIndex));
        } catch (Exception e) {
            handleError(e);
        }
        return passed;
    }

    /*
        get/set
     */
    public String getScript() {
        return script;
    }

    public ExpressionCalculator setScript(String script) {
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
