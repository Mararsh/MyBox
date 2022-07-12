package mara.mybox.fxml;

import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class ColumnFilter extends RowFilter {

    public boolean empty, zero, negative, positive, up, low;
    public double lowValue, upValue;

    public ColumnFilter() {
        init();
    }

    private void init() {
        lowValue = upValue = AppValues.InvalidDouble;
        empty = zero = negative = positive = up = low = reversed = passed = false;
        script = null;
    }

    public static ColumnFilter create() {
        return new ColumnFilter();
    }

    @Override
    public ColumnFilter fromString(String rowFilterString) {
        if (rowFilterString == null || rowFilterString.isBlank()) {
            script = null;
        } else {
            if (rowFilterString.startsWith("Reversed;;")) {
                script = rowFilterString.substring("Reversed;;".length());
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
            rowFilterString = (reversed ? "Reversed;;" : "") + script;
        }
        return rowFilterString;
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

}
