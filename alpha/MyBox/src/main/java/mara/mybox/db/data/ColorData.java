package mara.mybox.db.data;

import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.color.AdobeRGB;
import mara.mybox.color.AppleRGB;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.SRGB;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-7
 * @License Apache License Version 2.0
 */
public class ColorData extends BaseData {

    protected int colorValue;
    protected javafx.scene.paint.Color color;
    protected String rgba, rgb, colorName, colorDisplay, colorSimpleDisplay;
    protected String srgb, hsb, adobeRGB, appleRGB, eciRGB, SRGBLinear, adobeRGBLinear,
            appleRGBLinear, calculatedCMYK, eciCMYK, adobeCMYK, xyz, cieLab,
            lchab, cieLuv, lchuv;
    protected float[] adobeRGBValues, appleRGBValues, eciRGBValues, eciCmykValues, adobeCmykValues;
    protected double[] cmyk, xyzValues, cieLabValues, lchabValues, cieLuvValues, lchuvValues;
    protected boolean isSettingValues;
    protected long paletteid, cpid;
    protected float orderNumner, ryb;

    // rgba is saved as upper-case in db
    private void init() {
        colorValue = AppValues.InvalidInteger;
        orderNumner = Float.MAX_VALUE;
        ryb = -1;
        paletteid = -1;
        cpid = -1;
    }

    public ColorData() {
        init();
    }

    public ColorData(Color color) {
        init();
        try {
            this.color = color;
            rgba = FxColorTools.color2rgba(color);
            rgb = FxColorTools.color2rgb(color);
            colorValue = FxColorTools.color2Value(color);
        } catch (Exception e) {
        }
    }

    public ColorData(int value) {
        init();
        setValue(value);
    }

    public ColorData(int value, String name) {
        init();
        colorName = name;
        setValue(value);
    }

    final public void setValue(int value) {
        colorValue = value;
        try {
            color = FxColorTools.value2color(value);
            rgba = FxColorTools.color2rgba(color);  // rgba is saved as upper-case in db
            rgb = FxColorTools.color2rgb(color);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public ColorData(String web) {
        init();
        setWeb(web);
    }

    public ColorData(String web, String name) {
        init();
        colorName = name;
        setWeb(web);
    }

    // https://openjfx.io/javadoc/14/javafx.graphics/javafx/scene/paint/Color.html#web(java.lang.String)
    final public void setWeb(String web) {
        try {
            String value = web.trim();
            color = Color.web(value);
            rgba = FxColorTools.color2rgba(color);
            rgb = FxColorTools.color2rgb(color);
            colorValue = FxColorTools.color2Value(color);
            if (colorName == null
                    && !value.startsWith("#")
                    && !value.startsWith("0x") && !value.startsWith("0X")
                    && !value.startsWith("rgb") && !value.startsWith("hsl")) {
                colorName = value;
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
    }

    public boolean needCalculate() {
        return color == null || srgb == null;
    }

    public boolean calculateBase() {
        if (colorValue != AppValues.InvalidInteger) {
            color = FxColorTools.value2color(colorValue);
            rgba = FxColorTools.color2rgba(color);
            rgb = FxColorTools.color2rgb(color);
        } else if (color != null) {
            colorValue = FxColorTools.color2Value(color);
            rgba = FxColorTools.color2rgba(color);
            rgb = FxColorTools.color2rgb(color);
        } else if (rgba != null) {
            color = Color.web(rgba);
            rgb = FxColorTools.color2rgb(color);
            colorValue = FxColorTools.color2Value(color);
        } else if (rgb != null) {
            color = Color.web(rgb);
            rgba = FxColorTools.color2rgba(color);
            colorValue = FxColorTools.color2Value(color);
        } else {
            return false;
        }
        return true;
    }

    public ColorData calculate() {
        if (!needCalculate()) {
            return this;
        }
        if (colorName == null) {
            colorName = "";
        }
        if (!calculateBase()) {
            return this;
        }
        srgb = Math.round(color.getRed() * 255) + " "
                + Math.round(color.getGreen() * 255) + " "
                + Math.round(color.getBlue() * 255) + " "
                + Math.round(color.getOpacity() * 100) + "%";

        hsb = Math.round(color.getHue()) + " "
                + Math.round(color.getSaturation() * 100) + "% "
                + Math.round(color.getBrightness() * 100) + "%";

        ryb = ryb();

        adobeRGBValues = SRGB.srgb2profile(ImageColorSpace.adobeRGBProfile(), color);
        adobeRGB = Math.round(adobeRGBValues[0] * 255) + " "
                + Math.round(adobeRGBValues[1] * 255) + " "
                + Math.round(adobeRGBValues[2] * 255);

        appleRGBValues = SRGB.srgb2profile(ImageColorSpace.appleRGBProfile(), color);
        appleRGB = Math.round(appleRGBValues[0] * 255) + " "
                + Math.round(appleRGBValues[1] * 255) + " "
                + Math.round(appleRGBValues[2] * 255);

        eciRGBValues = SRGB.srgb2profile(ImageColorSpace.eciRGBProfile(), color);
        eciRGB = Math.round(eciRGBValues[0] * 255) + " "
                + Math.round(eciRGBValues[1] * 255) + " "
                + Math.round(eciRGBValues[2] * 255);

        SRGBLinear = Math.round(RGBColorSpace.linearSRGB(color.getRed()) * 255) + " "
                + Math.round(RGBColorSpace.linearSRGB(color.getGreen()) * 255) + " "
                + Math.round(RGBColorSpace.linearSRGB(color.getBlue()) * 255);

        adobeRGBLinear = Math.round(AdobeRGB.linearAdobeRGB(adobeRGBValues[0]) * 255) + " "
                + Math.round(AdobeRGB.linearAdobeRGB(adobeRGBValues[1]) * 255) + " "
                + Math.round(AdobeRGB.linearAdobeRGB(adobeRGBValues[2]) * 255);

        appleRGBLinear = Math.round(AppleRGB.linearAppleRGB(appleRGBValues[0]) * 255) + " "
                + Math.round(AppleRGB.linearAppleRGB(appleRGBValues[1]) * 255) + " "
                + Math.round(AppleRGB.linearAppleRGB(appleRGBValues[2]) * 255);

        cmyk = SRGB.rgb2cmyk(color);
        calculatedCMYK = Math.round(cmyk[0] * 100) + " " + Math.round(cmyk[1] * 100) + " "
                + Math.round(cmyk[2] * 100) + " " + Math.round(cmyk[3] * 100);

        eciCmykValues = SRGB.srgb2profile(ImageColorSpace.eciCmykProfile(), color);
        eciCMYK = Math.round(eciCmykValues[0] * 100) + " " + Math.round(eciCmykValues[1] * 100) + " "
                + Math.round(eciCmykValues[2] * 100) + " " + Math.round(eciCmykValues[3] * 100);

        adobeCmykValues = SRGB.srgb2profile(ImageColorSpace.adobeCmykProfile(), color);
        adobeCMYK = Math.round(adobeCmykValues[0] * 100) + " " + Math.round(adobeCmykValues[1] * 100) + " "
                + Math.round(adobeCmykValues[2] * 100) + " " + Math.round(adobeCmykValues[3] * 100);

        xyzValues = SRGB.toXYZd50(ColorConvertTools.converColor(color));
        xyz = DoubleTools.scale(xyzValues[0], 6) + " "
                + DoubleTools.scale(xyzValues[1], 6) + " "
                + DoubleTools.scale(xyzValues[2], 6);

        cieLabValues = CIEColorSpace.XYZd50toCIELab(xyzValues[0], xyzValues[1], xyzValues[2]);
        cieLab = DoubleTools.scale(cieLabValues[0], 2) + " "
                + DoubleTools.scale(cieLabValues[1], 2) + " "
                + DoubleTools.scale(cieLabValues[2], 2);

        lchabValues = CIEColorSpace.LabtoLCHab(cieLabValues);
        lchab = DoubleTools.scale(lchabValues[0], 2) + " "
                + DoubleTools.scale(lchabValues[1], 2) + " "
                + DoubleTools.scale(lchabValues[2], 2);

        cieLuvValues = CIEColorSpace.XYZd50toCIELuv(xyzValues[0], xyzValues[1], xyzValues[2]);
        cieLuv = DoubleTools.scale(cieLuvValues[0], 2) + " "
                + DoubleTools.scale(cieLuvValues[1], 2) + " "
                + DoubleTools.scale(cieLuvValues[2], 2);

        lchuvValues = CIEColorSpace.LuvtoLCHuv(cieLuvValues);
        lchuv = DoubleTools.scale(lchuvValues[0], 2) + " "
                + DoubleTools.scale(lchuvValues[1], 2) + " "
                + DoubleTools.scale(lchuvValues[2], 2);

        return this;
    }

    public String display() {
        if (colorDisplay == null) {
            if (colorName != null) {
                colorDisplay = colorName + "\n";
            } else {
                colorDisplay = "";
            }
            if (srgb == null) {
                calculate();
            }
            colorDisplay += rgba + "\n" + rgb + "\n" + colorValue + "\n"
                    + "sRGB: " + srgb + "\n"
                    + "HSB: " + hsb + "\n"
                    + message("RYBAngle") + ": " + FloatTools.toInt(ryb()) + "°\n"
                    + message("CalculatedCMYK") + ": " + calculatedCMYK + "\n"
                    + "Adobe RGB: " + adobeRGB + "\n"
                    + "Apple RGB: " + appleRGB + "\n"
                    + "ECI RGB: " + eciRGB + "\n"
                    + "sRGB Linear: " + SRGBLinear + "\n"
                    + "Adobe RGB Linear: " + adobeRGBLinear + "\n"
                    + "Apple RGB Linear: " + appleRGBLinear + "\n"
                    + "ECI CMYK: " + eciCMYK + "\n"
                    + "Adobe CMYK Uncoated FOGRA29: " + adobeCMYK + "\n"
                    + "XYZ: " + xyz + "\n"
                    + "CIE-L*ab: " + cieLab + "\n"
                    + "LCH(ab): " + lchab + "\n"
                    + "CIE-L*uv: " + cieLuv + "\n"
                    + "LCH(uv): " + lchuv
                    + (orderNumner == Float.MAX_VALUE ? "" : "\nOrderNumber: " + orderNumner);
        }
        return colorDisplay;
    }

    public String simpleDisplay() {
        if (colorSimpleDisplay == null) {
            if (colorName != null) {
                colorSimpleDisplay = colorName + "\n";
            } else {
                colorSimpleDisplay = "";
            }
            if (srgb == null) {
                calculate();
            }
            colorSimpleDisplay += colorValue + "\n" + rgba + "\n" + rgb + "\n"
                    + "sRGB: " + srgb + "\n"
                    + "HSB: " + hsb
                    + message("RYBAngle") + ": " + FloatTools.toInt(ryb()) + "°\n"
                    + message("CalculatedCMYK") + ": " + calculatedCMYK + "\n";
        }
        return colorSimpleDisplay;
    }

    public float ryb() {
        if (ryb >= 0 || color == null && !calculateBase()) {
            return ryb;
        }
        return ColorConvertTools.hue2ryb(color.getHue());
    }

    /*
        Static methods
     */
    public static ColorData create() {
        return new ColorData();
    }

    public static boolean setValue(ColorData data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "color_value":
                    data.setColorValue(value == null ? AppValues.InvalidInteger : (int) value);
                    return true;
                case "rgba":
                    data.setRgba(value == null ? null : (String) value);
                    return true;
                case "color_name":
                    data.setColorName(value == null ? null : (String) value);
                    return true;
                case "rgb":
                    data.setRgb(value == null ? null : (String) value);
                    return true;
                case "srgb":
                    data.setSrgb(value == null ? null : (String) value);
                    return true;
                case "hsb":
                    data.setHsb(value == null ? null : (String) value);
                    return true;
                case "ryb":
                    data.setRyb(value == null ? -1 : (float) value);
                    return true;
                case "lchuv":
                    data.setLchuv(value == null ? null : (String) value);
                    return true;
                case "cieLuv":
                    data.setCieLuv(value == null ? null : (String) value);
                    return true;
                case "lchab":
                    data.setLchab(value == null ? null : (String) value);
                    return true;
                case "cieLab":
                    data.setCieLab(value == null ? null : (String) value);
                    return true;
                case "xyz":
                    data.setXyz(value == null ? null : (String) value);
                    return true;
                case "adobeCMYK":
                    data.setAdobeCMYK(value == null ? null : (String) value);
                    return true;
                case "adobeRGB":
                    data.setAdobeRGB(value == null ? null : (String) value);
                    return true;
                case "appleRGB":
                    data.setAppleRGB(value == null ? null : (String) value);
                    return true;
                case "eciRGB":
                    data.setEciRGB(value == null ? null : (String) value);
                    return true;
                case "sRGBLinear":
                    data.setSRGBLinear(value == null ? null : (String) value);
                    return true;
                case "adobeRGBLinear":
                    data.setAdobeRGBLinear(value == null ? null : (String) value);
                    return true;
                case "appleRGBLinear":
                    data.setAppleRGBLinear(value == null ? null : (String) value);
                    return true;
                case "calculatedCMYK":
                    data.setCalculatedCMYK(value == null ? null : (String) value);
                    return true;
                case "eciCMYK":
                    data.setEciCMYK(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(ColorData data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "color_value":
                return data.getColorValue();
            case "rgba":
                return data.getRgba();
            case "color_name":
                return data.getColorName();
            case "rgb":
                return data.getRgb();
            case "srgb":
                return data.getSrgb();
            case "hsb":
                return data.getHsb();
            case "ryb":
                return data.getRyb();
            case "adobeRGB":
                return data.getAdobeRGB();
            case "appleRGB":
                return data.getAppleRGB();
            case "eciRGB":
                return data.getEciRGB();
            case "sRGBLinear":
                return data.getSRGBLinear();
            case "adobeRGBLinear":
                return data.getAdobeRGBLinear();
            case "appleRGBLinear":
                return data.getAppleRGBLinear();
            case "calculatedCMYK":
                return data.getCalculatedCMYK();
            case "eciCMYK":
                return data.getEciCMYK();
            case "adobeCMYK":
                return data.getAdobeCMYK();
            case "xyz":
                return data.getXyz();
            case "cieLab":
                return data.getCieLab();
            case "lchab":
                return data.getLchab();
            case "cieLuv":
                return data.getCieLuv();
            case "lchuv":
                return data.getLchuv();
        }
        return null;
    }

    public static boolean valid(ColorData data) {
        return data != null && data.getRgba() != null;
    }

    public static String htmlValue(ColorData data) {
        return StringTools.replaceHtmlLineBreak(data.display());
    }

    public static String htmlSimpleValue(ColorData data) {
        return StringTools.replaceHtmlLineBreak(data.simpleDisplay());
    }

    /*
        get/set
     */
    public Color getColor() {
        if (color == null) {
            if (rgba != null) {
                setWeb(rgba);
            } else if (rgb != null) {
                setWeb(rgb);
            }
        }
        return color;
    }

    public ColorData setRgba(String rgba) {
        this.rgba = rgba;
        if (color == null) {
            setWeb(rgba);
        }
        return this;
    }

    public String getRgba() {
        if (rgba == null && rgb != null) {
            setWeb(rgb);
        }
        if (rgba != null) {
            return rgba.toUpperCase();
        } else {
            return null;
        }
    }

    public ColorData setRgb(String rgb) {
        if (rgb == null) {
            return this;
        }
        this.rgb = rgb;
        if (color == null) {
            setWeb(rgb);
        }
        return this;
    }

    public String getRgb() {
        if (rgb == null && rgba != null) {
            setWeb(rgba);
        }
        return rgb;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getColorValue() {
        return colorValue;
    }

    public void setColorValue(int colorValue) {
        this.colorValue = colorValue;
    }

    public String getColorName() {
        return colorName;
    }

    public ColorData setColorName(String colorName) {
        this.colorName = colorName;
        return this;
    }

    public String getColorDisplay() {
        return display();
    }

    public ColorData setColorDisplay(String colorDisplay) {
        this.colorDisplay = colorDisplay;
        return this;
    }

    public String getColorSimpleDisplay() {
        return simpleDisplay();
    }

    public ColorData setColorSimpleDisplay(String colorSimpleDisplay) {
        this.colorSimpleDisplay = colorSimpleDisplay;
        return this;
    }

    public String getSrgb() {
        return srgb;
    }

    public void setSrgb(String srgb) {
        this.srgb = srgb;
    }

    public String getHsb() {
        return hsb;
    }

    public void setHsb(String hsb) {
        this.hsb = hsb;
    }

    public float[] getAdobeRGBValues() {
        return adobeRGBValues;
    }

    public void setAdobeRGBValues(float[] adobeRGBValues) {
        this.adobeRGBValues = adobeRGBValues;
    }

    public float[] getAppleRGBValues() {
        return appleRGBValues;
    }

    public void setAppleRGBValues(float[] appleRGBValues) {
        this.appleRGBValues = appleRGBValues;
    }

    public float[] getEciRGBValues() {
        return eciRGBValues;
    }

    public void setEciRGBValues(float[] eciRGBValues) {
        this.eciRGBValues = eciRGBValues;
    }

    public float[] getEciCmykValues() {
        return eciCmykValues;
    }

    public void setEciCmykValues(float[] eciCmykValues) {
        this.eciCmykValues = eciCmykValues;
    }

    public float[] getAdobeCmykValues() {
        return adobeCmykValues;
    }

    public void setAdobeCmykValues(float[] adobeCmykValues) {
        this.adobeCmykValues = adobeCmykValues;
    }

    public double[] getCmyk() {
        return cmyk;
    }

    public void setCmyk(double[] cmyk) {
        this.cmyk = cmyk;
    }

    public double[] getXyzValues() {
        return xyzValues;
    }

    public void setXyzValues(double[] xyzValues) {
        this.xyzValues = xyzValues;
    }

    public double[] getCieLabValues() {
        return cieLabValues;
    }

    public void setCieLabValues(double[] cieLabValues) {
        this.cieLabValues = cieLabValues;
    }

    public double[] getLchabValues() {
        return lchabValues;
    }

    public void setLchabValues(double[] lchabValues) {
        this.lchabValues = lchabValues;
    }

    public double[] getCieLuvValues() {
        return cieLuvValues;
    }

    public void setCieLuvValues(double[] cieLuvValues) {
        this.cieLuvValues = cieLuvValues;
    }

    public double[] getLchuvValues() {
        return lchuvValues;
    }

    public void setLchuvValues(double[] lchuvValues) {
        this.lchuvValues = lchuvValues;
    }

    public String getAdobeRGB() {
        return adobeRGB;
    }

    public void setAdobeRGB(String adobeRGB) {
        this.adobeRGB = adobeRGB;
    }

    public String getAppleRGB() {
        return appleRGB;
    }

    public void setAppleRGB(String appleRGB) {
        this.appleRGB = appleRGB;
    }

    public String getEciRGB() {
        return eciRGB;
    }

    public void setEciRGB(String eciRGB) {
        this.eciRGB = eciRGB;
    }

    public String getSRGBLinear() {
        return SRGBLinear;
    }

    public void setSRGBLinear(String SRGBLinear) {
        this.SRGBLinear = SRGBLinear;
    }

    public String getAdobeRGBLinear() {
        return adobeRGBLinear;
    }

    public void setAdobeRGBLinear(String adobeRGBLinear) {
        this.adobeRGBLinear = adobeRGBLinear;
    }

    public String getAppleRGBLinear() {
        return appleRGBLinear;
    }

    public void setAppleRGBLinear(String appleRGBLinear) {
        this.appleRGBLinear = appleRGBLinear;
    }

    public String getCalculatedCMYK() {
        return calculatedCMYK;
    }

    public void setCalculatedCMYK(String calculatedCMYK) {
        this.calculatedCMYK = calculatedCMYK;
    }

    public String getEciCMYK() {
        return eciCMYK;
    }

    public void setEciCMYK(String eciCMYK) {
        this.eciCMYK = eciCMYK;
    }

    public String getAdobeCMYK() {
        return adobeCMYK;
    }

    public void setAdobeCMYK(String adobeCMYK) {
        this.adobeCMYK = adobeCMYK;
    }

    public String getXyz() {
        return xyz;
    }

    public void setXyz(String xyz) {
        this.xyz = xyz;
    }

    public String getCieLab() {
        return cieLab;
    }

    public void setCieLab(String cieLab) {
        this.cieLab = cieLab;
    }

    public String getLchab() {
        return lchab;
    }

    public void setLchab(String lchab) {
        this.lchab = lchab;
    }

    public String getCieLuv() {
        return cieLuv;
    }

    public void setCieLuv(String cieLuv) {
        this.cieLuv = cieLuv;
    }

    public String getLchuv() {
        return lchuv;
    }

    public void setLchuv(String lchuv) {
        this.lchuv = lchuv;
    }

    public float getOrderNumner() {
        return orderNumner;
    }

    public ColorData setOrderNumner(float orderNumner) {
        this.orderNumner = orderNumner;
        return this;
    }

    public long getPaletteid() {
        return paletteid;
    }

    public ColorData setPaletteid(long paletteid) {
        this.paletteid = paletteid;
        return this;
    }

    public float getRyb() {
        return ryb;
    }

    public ColorData setRyb(float ryb) {
        this.ryb = ryb;
        return this;
    }

    public long getCpid() {
        return cpid;
    }

    public ColorData setCpid(long cpid) {
        this.cpid = cpid;
        return this;
    }

}
