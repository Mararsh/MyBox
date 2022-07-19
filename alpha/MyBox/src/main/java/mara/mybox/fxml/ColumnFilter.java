package mara.mybox.fxml;

import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.db.data.Data2DColumn;
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
            equal, larger, less, compareString, columnExpression;
    public String largerValue, lessValue, equalValue;

    public ColumnFilter() {
        init();
    }

    private void init() {
        work = empty = zero = negative = positive = number = number
                = equal = larger = less = compareString = columnExpression
                = reversed = passed = false;
        largerValue = lessValue = equalValue = null;
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
                } else if ("compareString".equalsIgnoreCase(v)) {
                    compareString = true;
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
        if (compareString) {
            columnFilterString += "compareString" + ValueSeperator;
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
            if (column == null) {
                handleError(message("InvalidData"));
                return false;
            }
            if (!work) {
                passed = true;
                return true;
            }
            if (empty && (value == null || value.isBlank())) {
                passed = true;
                return true;
            }
            double numberValue = Double.NaN;
            try {
                numberValue = Double.valueOf(value);
            } catch (Exception e) {
            }
            if (numberValue != Double.NaN) {
                if (number) {
                    passed = true;
                    return true;
                }
                if (zero && numberValue == 0) {
                    passed = true;
                    return true;
                }
                if (positive && numberValue > 0) {
                    passed = true;
                    return true;
                }
                if (negative && numberValue < 0) {
                    passed = true;
                    return true;
                }
            } else {
                if (nonNumeric) {
                    passed = true;
                    return true;
                }
            }
            DoubleStatistic statistic = column.getDoubleStatistic();
            if (equal) {
                if (compareString) {
                    if (compareString(equalValue, statistic, value) == 0) {
                        passed = true;
                        return true;
                    }
                } else {
                    if (compareNumber(equalValue, statistic, numberValue) == 0) {
                        passed = true;
                        return true;
                    }
                }
            }
            if (larger) {
                if (compareString) {
                    if (compareString(largerValue, statistic, value) == 1) {
                        passed = true;
                        return true;
                    }
                } else {
                    if (compareNumber(largerValue, statistic, numberValue) == 1) {
                        passed = true;
                        return true;
                    }
                }
            }
            if (less) {
                if (compareString) {
                    if (compareString(lessValue, statistic, value) == -1) {
                        passed = true;
                        return true;
                    }
                } else {
                    if (compareNumber(lessValue, statistic, numberValue) == -1) {
                        passed = true;
                        return true;
                    }
                }
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

    public int compareString(String name, DoubleStatistic statistic, String value) {
        if (name == null || value == null) {
            return -5;
        }
        if (ColumnFilter.Q1.equals(name)) {
            if (statistic != null && statistic.lowerQuartileValue != null) {
                return value.compareTo(statistic.lowerQuartileValue + "");
            }
        } else if (ColumnFilter.Q3.equals(name)) {
            if (statistic != null && statistic.upperQuartileValue != null) {
                return value.compareTo(statistic.upperQuartileValue + "");
            }
        } else if (ColumnFilter.E1.equals(name)) {
            if (statistic != null && statistic.lowerExtremeOutlierLine != Double.NaN) {
                return value.compareTo(statistic.lowerExtremeOutlierLine + "");
            }
        } else if (ColumnFilter.E2.equals(name)) {
            if (statistic != null && statistic.lowerMildOutlierLine != Double.NaN) {
                return value.compareTo(statistic.lowerMildOutlierLine + "");
            }
        } else if (ColumnFilter.E3.equals(name)) {
            if (statistic != null && statistic.upperMildOutlierLine != Double.NaN) {
                return value.compareTo(statistic.upperMildOutlierLine + "");
            }
        } else if (ColumnFilter.E4.equals(name)) {
            if (statistic != null && statistic.upperExtremeOutlierLine != Double.NaN) {
                return value.compareTo(statistic.upperExtremeOutlierLine + "");
            }
        } else if (ColumnFilter.Mode.equals(name)) {
            if (statistic != null && statistic.modeValue != null) {
                return value.compareTo(statistic.modeValue + "");
            }
        } else if (ColumnFilter.Median.equals(name)) {
            if (statistic != null && statistic.medianValue != null) {
                return value.compareTo(statistic.medianValue + "");
            }
        } else {
            return value.compareTo(name + "");
        }
        return -4;
    }

    public int compareNumber(double v1, double v2) {
        if (Double.isNaN(v1) || Double.isNaN(v2)) {
            return -5;
        }
        if (v1 == v2) {
            return 0;
        } else if (v1 > v2) {
            return 1;
        } else {
            return -1;
        }
    }

    public int compareNumber(String name, DoubleStatistic statistic, double value) {
        if (name == null || Double.isNaN(value)) {
            return -5;
        }
        if (ColumnFilter.Q1.equals(name)) {
            if (statistic != null && statistic.lowerQuartile != Double.NaN) {
                return compareNumber(value, statistic.lowerQuartile);
            }
        } else if (ColumnFilter.Q3.equals(name)) {
            if (statistic != null && statistic.upperQuartile != Double.NaN) {
                return compareNumber(value, statistic.upperQuartile);
            }
        } else if (ColumnFilter.E1.equals(name)) {
            if (statistic != null && statistic.lowerExtremeOutlierLine != Double.NaN) {
                return compareNumber(value, statistic.lowerExtremeOutlierLine);
            }
        } else if (ColumnFilter.E2.equals(name)) {
            if (statistic != null && statistic.lowerMildOutlierLine != Double.NaN) {
                return compareNumber(value, statistic.lowerMildOutlierLine);
            }
        } else if (ColumnFilter.E3.equals(name)) {
            if (statistic != null && statistic.upperMildOutlierLine != Double.NaN) {
                return compareNumber(value, statistic.upperMildOutlierLine);
            }
        } else if (ColumnFilter.E4.equals(name)) {
            if (statistic != null && statistic.upperExtremeOutlierLine != Double.NaN) {
                return compareNumber(value, statistic.upperExtremeOutlierLine);
            }
        } else if (ColumnFilter.Mode.equals(name)) {
            if (statistic != null && statistic.modeValue != null) {
                try {
                    return compareNumber(value, Double.valueOf(statistic.modeValue + ""));
                } catch (Exception ex) {
                }
            }
        } else if (ColumnFilter.Median.equals(name)) {
            if (statistic != null && statistic.median != Double.NaN) {
                return compareNumber(value, statistic.median);
            }
        } else {
            try {
                return compareNumber(value, Double.valueOf(name + ""));
            } catch (Exception ex) {
            }
        }
        return -4;
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

    public boolean isCompareString() {
        return compareString;
    }

    public ColumnFilter setCompareString(boolean compareString) {
        this.compareString = compareString;
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
