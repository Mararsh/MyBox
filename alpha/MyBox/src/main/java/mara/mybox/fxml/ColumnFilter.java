package mara.mybox.fxml;

import mara.mybox.db.data.Data2DColumn;
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
    public static String Mode = "MyBox-mode";
    public static String Median = "MyBox-median";

    public boolean work, empty, zero, negative, positive, number, nonNumeric,
            equal, larger, less, columnExpression;
    public String largerValue, lessValue, equalValue;

    public ColumnFilter() {
        init();
    }

    private void init() {
        work = number = true;
        largerValue = lessValue = equalValue = null;
        empty = zero = negative = positive = equal = larger = less = columnExpression
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
            String[] vs = columnFilterString.split(ValueSeperator);
            for (String v : vs) {
                if (v == null || v.isBlank()) {
                    continue;
                }
                if ("work".equalsIgnoreCase(v)) {
                    work = true;
                } else if ("empty".equalsIgnoreCase(v)) {
                    empty = true;
                } else if ("zero".equalsIgnoreCase(v)) {
                    zero = true;
                } else if ("negative".equalsIgnoreCase(v)) {
                    negative = true;
                } else if ("positive".equalsIgnoreCase(v)) {
                    positive = true;
                } else if ("number".equalsIgnoreCase(v)) {
                    number = true;
                } else if ("nonNumeric".equalsIgnoreCase(v)) {
                    nonNumeric = true;
                } else if (v.startsWith(EqualToPrefix)) {
                    equal = true;
                    equalValue = v.substring(EqualToPrefix.length());
                } else if (v.startsWith(LargerThanPrefix)) {
                    larger = true;
                    largerValue = v.substring(LargerThanPrefix.length());
                } else if (v.startsWith(LessThanPrefix)) {
                    less = true;
                    lessValue = v.substring(LessThanPrefix.length());
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
        String columnFilterString = "";
        if (work) {
            columnFilterString += "work" + ValueSeperator;
        }
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
        if (number) {
            columnFilterString += "number" + ValueSeperator;
        }
        if (nonNumeric) {
            columnFilterString += "nonNumeric" + ValueSeperator;
        }
        if (equal && equalValue != null && !equalValue.isBlank()) {
            columnFilterString += EqualToPrefix + equalValue + ValueSeperator;
        }
        if (largerValue != null && !largerValue.isBlank()) {
            columnFilterString += LargerThanPrefix + largerValue + ValueSeperator;
        }
        if (lessValue != null && !lessValue.isBlank()) {
            columnFilterString += LessThanPrefix + lessValue + ValueSeperator;
        }
        if (columnExpression && script != null && !script.isBlank()) {
            if (reversed) {
                columnFilterString += ReversedPrefix;
            }
            columnFilterString += script;
        }
        return columnFilterString;
    }

    public String value2Name(String value) {
        if (value == null) {
            return null;
        }
        if (Q1.equals(value)) {
            return message("LowerQuartile");
        } else if (Q3.equals(value)) {
            return message("UpperQuartile");
        } else if (E1.equals(value)) {
            return message("LowerExtremeOutlierLine");
        } else if (E2.equals(value)) {
            return message("LowerMildOutlierLine");
        } else if (E3.equals(value)) {
            return message("UpperMildOutlierLine");
        } else if (E4.equals(value)) {
            return message("UpperExtremeOutlierLine");
        } else if (Mode.equals(value)) {
            return message("Mode");
        } else if (Median.equals(value)) {
            return message("Median");
        } else {
            return value;
        }
    }

    public String name2Value(String name) {
        if (name == null) {
            return null;
        }
        if (message("LowerQuartile").equals(name)) {
            return Q1;
        } else if (message("UpperQuartile").equals(name)) {
            return Q3;
        } else if (message("LowerExtremeOutlierLine").equals(name)) {
            return E1;
        } else if (message("LowerMildOutlierLine").equals(name)) {
            return E2;
        } else if (message("UpperMildOutlierLine").equals(name)) {
            return E3;
        } else if (message("UpperExtremeOutlierLine").equals(name)) {
            return E4;
        } else if (message("Mode").equals(name)) {
            return Mode;
        } else if (message("Median").equals(name)) {
            return Median;
        } else {
            return name;
        }
    }

    // return true if the value satisfies one of conditions
    public boolean filter(Data2DColumn column, String value) {
        try {
            handleError(null);
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
                if (!column.isNumberType()) {
                    if (largerValue != null && largerValue.compareTo(value) < 0) {
                        passed = true;
                        return true;
                    }
                    if (lessValue != null && lessValue.compareTo(value) > 0) {
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
//                if (largerValue != null && largerThanNumber != AppValues.InvalidDouble
//                        && number > largerThanNumber) {
//                    passed = true;
//                    return true;
//                }
//                if (lessValue != null && lessThanNumber != AppValues.InvalidDouble
//                        && number < lessThanNumber) {
//                    passed = true;
//                    return true;
//                }
            }
            if (columnExpression) {
                return filterScript(value);
            }
        } catch (Exception e) {
            handleError(e.toString());
        }
        passed = false;
        return false;
    }

    private boolean filterScript(String value) {
        try {
            handleError(null);
            if (script == null || script.isBlank()) {
                passed = true;
                return true;
            }
            return readResult(calculator.calculateDataColumnExpression(data2D, script, value));
        } catch (Exception e) {
            handleError(e.toString());
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

    public String getLargerValue() {
        return largerValue;
    }

    public ColumnFilter setLargerValue(String largerValue) {
        this.largerValue = largerValue;
        return this;
    }

    public String getLessValue() {
        return lessValue;
    }

    public ColumnFilter setLessValue(String lessValue) {
        this.lessValue = lessValue;
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

    public boolean isNumber() {
        return number;
    }

    public ColumnFilter setNumber(boolean number) {
        this.number = number;
        return this;
    }

    public boolean isNonNumeric() {
        return nonNumeric;
    }

    public ColumnFilter setNonNumeric(boolean nonNumeric) {
        this.nonNumeric = nonNumeric;
        return this;
    }

    public boolean isColumnExpression() {
        return columnExpression;
    }

    public ColumnFilter setColumnExpression(boolean columnExpression) {
        this.columnExpression = columnExpression;
        return this;
    }

}
