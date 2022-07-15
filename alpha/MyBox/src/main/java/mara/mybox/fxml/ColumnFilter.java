package mara.mybox.fxml;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class ColumnFilter extends RowFilter {

    public static String ValueSeperator = "--MyBox--";
    public static String EqualToPrefix = "EqualTo::";
    public static String LargerThanPrefix = "LargerThan::";
    public static String LessThanPrefix = "LessThan::";
    public static String Q1 = "MyBox-q1";
    public static String Q3 = "MyBox-q3";
    public static String E1 = "MyBox-e1";
    public static String E2 = "MyBox-e2";
    public static String E3 = "MyBox-e3";
    public static String E4 = "MyBox-e4";

    public boolean work, empty, zero, negative, positive, equal, larger, less, columnExpression;
    public String largerThan, lessThan, equalValue;
    public double largerThanNumber, lessThanNumber;

    public ColumnFilter() {
        init();
    }

    private void init() {
        largerThan = lessThan = equalValue = null;
        largerThanNumber = lessThanNumber = AppValues.InvalidDouble;
        work = empty = zero = negative = positive = equal = larger = less = columnExpression
                = reversed = passed = false;
        script = null;
    }

    public static ColumnFilter create() {
        return new ColumnFilter();
    }

    public static String placehold() {
        return "#{" + message("ColumnValue") + "}";
    }

    @Override
    public ColumnFilter fromString(String columnFilterString) {
        init();
        if (columnFilterString != null && !columnFilterString.isBlank()) {
            work = true;
            String[] vs = columnFilterString.split(ValueSeperator);
            for (String v : vs) {
                if (v == null || v.isBlank()) {
                    continue;
                }
                if ("empty".equalsIgnoreCase(v)) {
                    empty = true;
                } else if ("zero".equalsIgnoreCase(v)) {
                    zero = true;
                } else if ("negative".equalsIgnoreCase(v)) {
                    negative = true;
                } else if ("positive".equalsIgnoreCase(v)) {
                    positive = true;
                } else if (v.startsWith(EqualToPrefix)) {
                    equal = true;
                    equalValue = v.substring(EqualToPrefix.length());
                } else if (v.startsWith(LargerThanPrefix)) {
                    larger = true;
                    largerThan = v.substring(LargerThanPrefix.length());
                    try {
                        largerThanNumber = Double.valueOf(largerThan);
                    } catch (Exception e) {
                    }
                } else if (v.startsWith(LessThanPrefix)) {
                    less = true;
                    lessThan = v.substring(LessThanPrefix.length());
                    try {
                        lessThanNumber = Double.valueOf(lessThan);
                    } catch (Exception e) {
                    }
                } else if (v.startsWith(ReversedPrefix)) {
                    columnExpression = true;
                    script = v.substring(ReversedPrefix.length());
                    reversed = true;
                } else {
                    columnExpression = true;
                    script = v;
                    reversed = false;
                }
            }
        } else {
            work = false;
        }
        return this;
    }

    @Override
    public String toString() {
        if (!work) {
            return null;
        }
        String columnFilterString = "";
        if (empty) {
            columnFilterString += "empty" + ValueSeperator;
        }
        if (zero) {
            columnFilterString += "zero" + ValueSeperator;
        }
        if (negative) {
            columnFilterString += "negative" + ValueSeperator;
        }
        if (positive) {
            columnFilterString += "positive" + ValueSeperator;
        }
        if (equal && equalValue != null && !equalValue.isBlank()) {
            columnFilterString += EqualToPrefix + equalValue + ValueSeperator;
        }
        if (largerThan != null && !largerThan.isBlank()) {
            columnFilterString += LargerThanPrefix + largerThan + ValueSeperator;
        }
        if (lessThan != null && !lessThan.isBlank()) {
            columnFilterString += LessThanPrefix + lessThan + ValueSeperator;
        }
        if (columnExpression && script != null && !script.isBlank()) {
            if (reversed) {
                columnFilterString += ReversedPrefix;
            }
            columnFilterString += script;
        }
        return columnFilterString;
    }

    // return true if the value satisfies one of conditions
    public boolean filter(String value, boolean isNumber) {
        try {
            error = null;
            passed = false;
            if (!work) {
                passed = true;
                return true;
            }
            if (equal) {
                if (equalValue != null && !equalValue.isBlank()) {
                    if (equalValue.equals(value)) {
                        passed = true;
                        return true;
                    }
                } else {
                    if (value != null && !value.isBlank()) {
                        passed = true;
                        return true;
                    }
                }
            }
            double number = AppValues.InvalidDouble;
            try {
                number = Double.valueOf(value);
            } catch (Exception e) {
            }
            if (number == AppValues.InvalidDouble) {
                if (empty) {
                    passed = true;
                    return true;
                }
                if (!isNumber) {
                    if (largerThan != null && largerThan.compareTo(value) < 0) {
                        passed = true;
                        return true;
                    }
                    if (lessThan != null && lessThan.compareTo(value) > 0) {
                        passed = true;
                        return true;
                    }
                }
            } else {
                if (zero && number == 0) {
                    passed = true;
                    return true;
                }
                if (positive && number > 0) {
                    passed = true;
                    return true;
                }
                if (negative && number < 0) {
                    passed = true;
                    return true;
                }
                if (largerThan != null && largerThanNumber != AppValues.InvalidDouble
                        && number > largerThanNumber) {
                    passed = true;
                    return true;
                }
                if (lessThan != null && lessThanNumber != AppValues.InvalidDouble
                        && number < lessThanNumber) {
                    passed = true;
                    return true;
                }
            }
            if (columnExpression) {
                return filterScript(value);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
        }
        passed = false;
        return false;
    }

    private boolean filterScript(String value) {
        try {
            error = null;
            if (script == null || script.isBlank()) {
                passed = true;
                return true;
            }
            return readResult(calculateDataColumnExpression(script, value));
        } catch (Exception e) {
            handleError(e);
            passed = false;
            return passed;
        }
    }

    /*
        get/set
     */
    public boolean isWork() {
        return work;
    }

    public ColumnFilter setWork(boolean work) {
        this.work = work;
        return this;
    }

    public boolean isEmpty() {
        return empty;
    }

    public ColumnFilter setEmpty(boolean empty) {
        this.empty = empty;
        return this;
    }

    public boolean isZero() {
        return zero;
    }

    public ColumnFilter setZero(boolean zero) {
        this.zero = zero;
        return this;
    }

    public boolean isNegative() {
        return negative;
    }

    public ColumnFilter setNegative(boolean negative) {
        this.negative = negative;
        return this;
    }

    public boolean isPositive() {
        return positive;
    }

    public ColumnFilter setPositive(boolean positive) {
        this.positive = positive;
        return this;
    }

    public String getLargerThan() {
        return largerThan;
    }

    public ColumnFilter setLargerThan(String largerThan) {
        this.largerThan = largerThan;
        return this;
    }

    public String getLessThan() {
        return lessThan;
    }

    public ColumnFilter setLessThan(String lessThan) {
        this.lessThan = lessThan;
        return this;
    }

    public boolean isEqual() {
        return equal;
    }

    public ColumnFilter setEqual(boolean equal) {
        this.equal = equal;
        return this;
    }

    public String getEqualValue() {
        return equalValue;
    }

    public ColumnFilter setEqualValue(String equalValue) {
        this.equalValue = equalValue;
        return this;
    }

    public boolean isLarger() {
        return larger;
    }

    public ColumnFilter setLarger(boolean larger) {
        this.larger = larger;
        return this;
    }

    public boolean isLess() {
        return less;
    }

    public ColumnFilter setLess(boolean less) {
        this.less = less;
        return this;
    }

    public double getLargerThanNumber() {
        return largerThanNumber;
    }

    public double getLessThanNumber() {
        return lessThanNumber;
    }

    public ColumnFilter setLargerThanNumber(double largerThanNumber) {
        this.largerThanNumber = largerThanNumber;
        return this;
    }

    public ColumnFilter setLessThanNumber(double lessThanNumber) {
        this.lessThanNumber = lessThanNumber;
        return this;
    }

    public boolean isColumnExpression() {
        return columnExpression;
    }

    public void setColumnExpression(boolean columnExpression) {
        this.columnExpression = columnExpression;
    }

}
