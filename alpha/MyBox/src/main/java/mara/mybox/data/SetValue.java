package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2022-8-13
 * @License Apache License Version 2.0
 */
public class SetValue {

    public ValueType type;
    public String value;
    public int start, digit;
    public boolean fillZero, aotoDigit;

    public static enum ValueType {
        Value, Zero, One, Blank, Random, RandomNonNegative, Prefix, Suffix, SuffixNumber,
        Expression, GaussianDistribution, Identify, UpperTriangle, LowerTriangle
    }

    public SetValue() {
        init();
    }

    public final void init() {
        type = ValueType.Value;
        value = null;
        start = 0;
        digit = 0;
        fillZero = true;
        aotoDigit = true;
    }

    public boolean isZero() {
        return type == ValueType.Zero;
    }

    public boolean isOne() {
        return type == ValueType.One;
    }

    public boolean isBlank() {
        return type == ValueType.Blank;
    }

    public boolean isRandom() {
        return type == ValueType.Random;
    }

    public boolean isRandomNonNegative() {
        return type == ValueType.RandomNonNegative;
    }

    public boolean isPrefix() {
        return type == ValueType.Prefix;
    }

    public boolean isSuffix() {
        return type == ValueType.Suffix;
    }

    public boolean isSuffixNumber() {
        return type == ValueType.SuffixNumber;
    }

    public boolean isExpression() {
        return type == ValueType.Expression;
    }

    public boolean isGaussianDistribution() {
        return type == ValueType.GaussianDistribution;
    }

    public boolean isIdentify() {
        return type == ValueType.Identify;
    }

    public boolean isUpperTriangle() {
        return type == ValueType.UpperTriangle;
    }

    public boolean isLowerTriangle() {
        return type == ValueType.LowerTriangle;
    }

    /*
        set/set
     */
    public ValueType getType() {
        return type;
    }

    public SetValue setType(ValueType type) {
        this.type = type;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SetValue setValue(String value) {
        this.value = value;
        return this;
    }

    public int getStart() {
        return start;
    }

    public SetValue setStart(int start) {
        this.start = start;
        return this;
    }

    public int getDigit() {
        return digit;
    }

    public SetValue setDigit(int digit) {
        this.digit = digit;
        return this;
    }

    public boolean isFillZero() {
        return fillZero;
    }

    public SetValue setFillZero(boolean fillZero) {
        this.fillZero = fillZero;
        return this;
    }

    public boolean isAotoDigit() {
        return aotoDigit;
    }

    public SetValue setAotoDigit(boolean aotoDigit) {
        this.aotoDigit = aotoDigit;
        return this;
    }

}
