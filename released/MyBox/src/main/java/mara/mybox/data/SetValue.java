package mara.mybox.data;

import java.util.List;
import java.util.Random;
import mara.mybox.data2d.Data2D;
import static mara.mybox.db.data.ColumnDefinition.DefaultInvalidAs;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Empty;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Null;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Skip;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Use;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Zero;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-8-13
 * @License Apache License Version 2.0
 */
public class SetValue {

    public ValueType type;
    public String parameter, error;
    public int start, digit, scale;
    public boolean fillZero, aotoDigit, valueInvalid;
    public InvalidAs invalidAs;

    final public static ValueType DefaultValueType = ValueType.Zero;

    public static enum ValueType {
        Value, Zero, One, Empty, Null, Random, RandomNonNegative,
        Scale, Prefix, Suffix,
        NumberSuffix, NumberPrefix, NumberReplace, NumberSuffixString, NumberPrefixString,
        Expression, GaussianDistribution, Identify, UpperTriangle, LowerTriangle
    }

    public SetValue() {
        init();
    }

    public final void init() {
        type = DefaultValueType;
        parameter = null;
        start = 0;
        digit = 0;
        scale = 5;
        fillZero = true;
        aotoDigit = true;
        invalidAs = DefaultInvalidAs;
    }

    public int countFinalDigit(long dataSize) {
        int finalDigit = digit;
        if (isFillZero()) {
            if (isAotoDigit()) {
                finalDigit = (dataSize + "").length();
            }
        } else {
            finalDigit = 0;
        }
        return finalDigit;
    }

    public String scale(String value) {
        double d = DoubleTools.toDouble(value, invalidAs);
        if (DoubleTools.invalidDouble(d)) {
            if (null == invalidAs) {
                return value;
            } else {
                switch (invalidAs) {
                    case Zero:
                        return "0";
                    case Empty:
                        return "";
                    case Null:
                        return null;
                    case Skip:
                        return null;
                    case Use:
                        return value;
                    default:
                        return value;
                }
            }
        } else {
            return DoubleTools.scaleString(d, scale);
        }
    }

    public String dummyValue(Data2D data2D, Data2DColumn column) {
        return makeValue(data2D, column, column.dummyValue(), data2D.dummyRow(),
                1, start, countFinalDigit(data2D.getRowsNumber()), new Random());
    }

    public String makeValue(Data2D data2D, Data2DColumn column,
            String currentValue, List<String> row, long rowIndex,
            int dataIndex, int ddigit, Random random) {
        try {
            error = null;
            valueInvalid = false;
            switch (type) {
                case Zero:
                    return "0";

                case One:
                    return "1";

                case Empty:
                    return "";

                case Null:
                    return null;

                case Random:
                    return data2D.random(random, column, false);

                case RandomNonNegative:
                    return data2D.random(random, column, true);

                case Scale:
                    return scale(currentValue);

                case Prefix:
                    return currentValue == null ? parameter : (parameter + currentValue);

                case Suffix:
                    return currentValue == null ? parameter : (currentValue + parameter);

                case NumberSuffix:
                    String suffix = StringTools.fillLeftZero(dataIndex, ddigit);
                    return currentValue == null ? suffix : (currentValue + suffix);

                case NumberPrefix:
                    String prefix = StringTools.fillLeftZero(dataIndex, ddigit);
                    return currentValue == null ? prefix : (prefix + currentValue);

                case NumberReplace:
                    return StringTools.fillLeftZero(dataIndex, ddigit);

                case NumberSuffixString:
                    String ssuffix = StringTools.fillLeftZero(dataIndex, ddigit);
                    return parameter == null ? ssuffix : (parameter + ssuffix);

                case NumberPrefixString:
                    String sprefix = StringTools.fillLeftZero(dataIndex, ddigit);
                    return parameter == null ? sprefix : (sprefix + parameter);

                case Expression:
                    if (data2D.calculateDataRowExpression(parameter, row, rowIndex)) {
                        return data2D.expressionResult();
                    } else {
                        valueInvalid = true;
                        error = data2D.expressionError();
                        return currentValue;
                    }

                case Value:
                    return parameter;

            }
            valueInvalid = true;
            error = "InvalidData";
            return currentValue;
        } catch (Exception e) {
            error = e.toString();
            return currentValue;
        }
    }

    public String majorParameter() {
        try {
            switch (type) {
                case Scale:
                    return scale + "";

                case Suffix:
                case Prefix:
                case NumberSuffixString:
                case NumberPrefixString:
                case Expression:
                case Value:
                    return parameter;

                case NumberSuffix:
                case NumberPrefix:
                    return start + "";
            }

        } catch (Exception e) {
        }
        return null;
    }

    /*
        set
     */
    public SetValue setType(ValueType type) {
        this.type = type;
        return this;
    }

    public SetValue setParameter(String value) {
        this.parameter = value;
        return this;
    }

    public SetValue setStart(int start) {
        this.start = start;
        return this;
    }

    public SetValue setDigit(int digit) {
        this.digit = digit;
        return this;
    }

    public SetValue setFillZero(boolean fillZero) {
        this.fillZero = fillZero;
        return this;
    }

    public SetValue setAotoDigit(boolean aotoDigit) {
        this.aotoDigit = aotoDigit;
        return this;
    }

    public SetValue setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public SetValue setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public SetValue setError(String error) {
        this.error = error;
        return this;
    }

    /*
        get
     */
    public ValueType getType() {
        return type;
    }

    public String getParameter() {
        return parameter;
    }

    public int getStart() {
        return start;
    }

    public int getDigit() {
        return digit;
    }

    public boolean isFillZero() {
        return fillZero;
    }

    public boolean isAotoDigit() {
        return aotoDigit;
    }

    public int getScale() {
        return scale;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

    public String getError() {
        return error;
    }

}
