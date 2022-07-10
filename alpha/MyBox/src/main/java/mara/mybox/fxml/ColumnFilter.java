package mara.mybox.fxml;

import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class ColumnFilter {

    public boolean empty, zero, negative, positive, up, low, reversed, passed;
    public double lowValue, upValue;
    public String script;
    public ExpressionCalculator calculator;

    public ColumnFilter() {
        init();
    }

    private void init() {
        lowValue = upValue = AppValues.InvalidDouble;
        empty = zero = negative = positive = up = low = reversed = passed = false;
        script = null;
        calculator = null;
    }

    public static ColumnFilter create() {
        return new ColumnFilter();
    }

    /*
        get/set
     */
    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isZero() {
        return zero;
    }

    public void setZero(boolean zero) {
        this.zero = zero;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isLow() {
        return low;
    }

    public void setLow(boolean low) {
        this.low = low;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public double getLowValue() {
        return lowValue;
    }

    public void setLowValue(double lowValue) {
        this.lowValue = lowValue;
    }

    public double getUpValue() {
        return upValue;
    }

    public void setUpValue(double upValue) {
        this.upValue = upValue;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public ExpressionCalculator getCalculator() {
        return calculator;
    }

    public void setCalculator(ExpressionCalculator calculator) {
        this.calculator = calculator;
    }

}
