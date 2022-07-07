package mara.mybox.data;

import mara.mybox.fxml.ExpressionCalculator;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class RowFilter {

    public long passedNumber, maxPassed;
    public boolean reversed, passed;
    public String script;
    public ExpressionCalculator calculator;

    public RowFilter() {
        passedNumber = -1;
        maxPassed = -1;
        reversed = false;
        passed = true;
        script = null;
        calculator = null;
    }

    public static RowFilter create() {
        return new RowFilter();
    }

    public boolean needFilter() {
        return (script != null && !script.isBlank())
                || maxPassed > 0;
    }

    public boolean reachMaxPassed() {
        return maxPassed > 0 && passedNumber > maxPassed;
    }

    public boolean readResult(boolean calculateFailed) {
        passed = calculator == null || calculateFailed ? false : "true".equals(calculator.expressionResult);
        passed = reversed ? !passed : passed;
        if (passed) {
            passedNumber++;
        }
        return passed;
    }

    /*
        get/set
     */
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

    public String getScript() {
        return script;
    }

    public RowFilter setScript(String script) {
        this.script = script;
        return this;
    }

    public ExpressionCalculator getCalculator() {
        return calculator;
    }

    public RowFilter setCalculator(ExpressionCalculator calculator) {
        this.calculator = calculator;
        return this;
    }

}
