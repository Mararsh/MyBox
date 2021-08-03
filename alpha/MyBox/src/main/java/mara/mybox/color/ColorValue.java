package mara.mybox.color;

import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2019-5-24 7:59:40
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorValue {

    public static enum ColorType {
        CIEXYZ, CIExyY, CIELab, CIELCH, CIELuv,
        sRGB, AdobeRGB, AppleRGB, CIERGB, PALRGB, NTSCRGB, ColorMatchRGB, ProPhotoRGB, SMPTECRGB,
        HSB, CMYK
    }
    private ColorType type;
    private double d1, d2, d3, d4;
    private int i1 = -1, i2 = -1, i3 = -1, i4 = -1;
    private String colorSpace, conditions, values,
            gamma, illuminant, v1, v2, v3, v4;

    public ColorValue(String colorSpace, String conditions, double[] values) {
        this.colorSpace = colorSpace;
        d1 = DoubleTools.scale(values[0], 2);
        d2 = DoubleTools.scale(values[1], 2);
        d3 = DoubleTools.scale(values[2], 2);
        this.values = d1 + "  " + d2 + "  " + d3;
        if (values.length == 4) {
            d4 = DoubleTools.scale(values[3], 2);
            this.values += "  " + d4;
        }
        this.conditions = conditions;
    }

    public ColorValue(String colorSpace, String conditions, double[] values, int scale) {
        this.colorSpace = colorSpace;
        d1 = Math.round(values[0] * scale);
        d2 = Math.round(values[1] * scale);
        d3 = Math.round(values[2] * scale);
        this.values = (int) d1 + "  " + (int) d2 + "  " + (int) d3;
        if (values.length == 4) {
            d4 = Math.round(values[3] * scale);
            this.values += "  " + (int) d4;
        }
        this.conditions = conditions;
    }

    public ColorValue(String colorSpace, String gamma, String illuminant, double[] values) {
        this.colorSpace = colorSpace;
        d1 = DoubleTools.scale(values[0], 8);
        d2 = DoubleTools.scale(values[1], 8);
        d3 = DoubleTools.scale(values[2], 8);
        this.gamma = gamma;
        this.illuminant = illuminant;
    }

    public ColorValue(String colorSpace, String gamma, String illuminant, double[] values, int scale) {
        this.colorSpace = colorSpace;
        d1 = Math.round(values[0] * scale);
        d2 = Math.round(values[1] * scale);
        d3 = Math.round(values[2] * scale);
        this.gamma = gamma;
        this.illuminant = illuminant;
    }

    public ColorValue(String colorSpace, String gamma, String illuminant, double v1, double v2, double v3) {
        this.colorSpace = colorSpace;
        d1 = v1;
        d2 = v2;
        d3 = v3;
        this.gamma = gamma;
        this.illuminant = illuminant;
    }

    public ColorValue(String colorSpace, String gamma, String illuminant, double v1, double v2, double v3, double v4) {
        this.colorSpace = colorSpace;
        d1 = v1;
        d2 = v2;
        d3 = v3;
        d4 = v4;
        this.gamma = gamma;
        this.illuminant = illuminant;
    }

    public final void scale() {
        if (type == null) {
            return;
        }
        int ratio = 0;
        switch (type) {
            case CIEXYZ:
            case CMYK:
                ratio = 100;
                break;
            case sRGB:
            case AdobeRGB:
            case AppleRGB:
            case CIERGB:
            case PALRGB:
            case NTSCRGB:
            case ColorMatchRGB:
            case ProPhotoRGB:
            case SMPTECRGB:
                ratio = 255;
                break;
            default:
                return;
        }
        i1 = (int) Math.round(ratio * d1);
        i2 = (int) Math.round(ratio * d2);
        i3 = (int) Math.round(ratio * d3);
        i4 = (int) Math.round(ratio * d4);
    }

    /*
        get/set
     */
    public ColorType getType() {
        return type;
    }

    public void setType(ColorType type) {
        this.type = type;
    }

    public double getD1() {
        return d1;
    }

    public void setD1(double d1) {
        this.d1 = d1;
    }

    public double getD2() {
        return d2;
    }

    public void setD2(double d2) {
        this.d2 = d2;
    }

    public double getD3() {
        return d3;
    }

    public void setD3(double d3) {
        this.d3 = d3;
    }

    public double getD4() {
        return d4;
    }

    public void setD4(double d4) {
        this.d4 = d4;
    }

    public int getI1() {
        return i1;
    }

    public void setI1(int i1) {
        this.i1 = i1;
    }

    public int getI2() {
        return i2;
    }

    public void setI2(int i2) {
        this.i2 = i2;
    }

    public int getI3() {
        return i3;
    }

    public void setI3(int i3) {
        this.i3 = i3;
    }

    public int getI4() {
        return i4;
    }

    public void setI4(int i4) {
        this.i4 = i4;
    }

    public String getGamma() {
        return gamma;
    }

    public void setGamma(String gamma) {
        this.gamma = gamma;
    }

    public String getIlluminant() {
        return illuminant;
    }

    public void setIlluminant(String illuminant) {
        this.illuminant = illuminant;
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        this.colorSpace = colorSpace;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getV1() {
        return v1;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public String getV2() {
        return v2;
    }

    public void setV2(String v2) {
        this.v2 = v2;
    }

    public String getV3() {
        return v3;
    }

    public void setV3(String v3) {
        this.v3 = v3;
    }

    public String getV4() {
        return v4;
    }

    public void setV4(String v4) {
        this.v4 = v4;
    }

}
