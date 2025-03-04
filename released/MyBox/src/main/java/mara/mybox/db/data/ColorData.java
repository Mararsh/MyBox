package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.color.AdobeRGB;
import mara.mybox.color.AppleRGB;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.SRGB;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.FxColorTools;
import static mara.mybox.fxml.image.FxColorTools.color2css;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.image.data.ImageColorSpace;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.HtmlWriteTools;
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
    protected javafx.scene.paint.Color color, invertColor, complementaryColor;
    protected String rgba, rgb, colorName, colorDisplay, colorSimpleDisplay, vSeparator;
    protected String srgb, hsb, hue, rybAngle, saturation, brightness, opacity, invertRGB, complementaryRGB,
            adobeRGB, appleRGB, eciRGB, SRGBLinear, adobeRGBLinear,
            appleRGBLinear, calculatedCMYK, eciCMYK, adobeCMYK, xyz, cieLab,
            lchab, cieLuv, lchuv;
    protected float[] adobeRGBValues, appleRGBValues, eciRGBValues, eciCmykValues, adobeCmykValues;
    protected double[] cmyk, xyzD50, cieLabValues, lchabValues, cieLuvValues, lchuvValues;
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
        vSeparator = " ";
    }

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
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
            MyBoxLog.debug(e);
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
//            MyBoxLog.debug(e);
        }
    }

    public boolean needCalculate() {
        return color == null || srgb == null;
    }

    public boolean needConvert() {
        return ryb < 0 || hsb == null || invertRGB == null;
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
        srgb = Math.round(color.getRed() * 255) + vSeparator
                + Math.round(color.getGreen() * 255) + vSeparator
                + Math.round(color.getBlue() * 255) + vSeparator
                + Math.round(color.getOpacity() * 100) + "%";

        adobeRGBValues = SRGB.srgb2profile(ImageColorSpace.adobeRGBProfile(), color);
        adobeRGB = Math.round(adobeRGBValues[0] * 255) + vSeparator
                + Math.round(adobeRGBValues[1] * 255) + vSeparator
                + Math.round(adobeRGBValues[2] * 255);

        appleRGBValues = SRGB.srgb2profile(ImageColorSpace.appleRGBProfile(), color);
        appleRGB = Math.round(appleRGBValues[0] * 255) + vSeparator
                + Math.round(appleRGBValues[1] * 255) + vSeparator
                + Math.round(appleRGBValues[2] * 255);

        eciRGBValues = SRGB.srgb2profile(ImageColorSpace.eciRGBProfile(), color);
        eciRGB = Math.round(eciRGBValues[0] * 255) + vSeparator
                + Math.round(eciRGBValues[1] * 255) + vSeparator
                + Math.round(eciRGBValues[2] * 255);

        SRGBLinear = Math.round(RGBColorSpace.linearSRGB(color.getRed()) * 255) + vSeparator
                + Math.round(RGBColorSpace.linearSRGB(color.getGreen()) * 255) + vSeparator
                + Math.round(RGBColorSpace.linearSRGB(color.getBlue()) * 255);

        adobeRGBLinear = Math.round(AdobeRGB.linearAdobeRGB(adobeRGBValues[0]) * 255) + vSeparator
                + Math.round(AdobeRGB.linearAdobeRGB(adobeRGBValues[1]) * 255) + vSeparator
                + Math.round(AdobeRGB.linearAdobeRGB(adobeRGBValues[2]) * 255);

        appleRGBLinear = Math.round(AppleRGB.linearAppleRGB(appleRGBValues[0]) * 255) + vSeparator
                + Math.round(AppleRGB.linearAppleRGB(appleRGBValues[1]) * 255) + vSeparator
                + Math.round(AppleRGB.linearAppleRGB(appleRGBValues[2]) * 255);

        cmyk = SRGB.rgb2cmyk(color);
        calculatedCMYK = Math.round(cmyk[0] * 100) + vSeparator
                + Math.round(cmyk[1] * 100) + vSeparator
                + Math.round(cmyk[2] * 100) + vSeparator
                + Math.round(cmyk[3] * 100);

        eciCmykValues = SRGB.srgb2profile(ImageColorSpace.eciCmykProfile(), color);
        eciCMYK = Math.round(eciCmykValues[0] * 100) + vSeparator
                + Math.round(eciCmykValues[1] * 100) + vSeparator
                + Math.round(eciCmykValues[2] * 100) + vSeparator
                + Math.round(eciCmykValues[3] * 100);

        adobeCmykValues = SRGB.srgb2profile(ImageColorSpace.adobeCmykProfile(), color);
        adobeCMYK = Math.round(adobeCmykValues[0] * 100) + vSeparator
                + Math.round(adobeCmykValues[1] * 100) + vSeparator
                + Math.round(adobeCmykValues[2] * 100) + vSeparator
                + Math.round(adobeCmykValues[3] * 100);

        xyzD50 = SRGB.SRGBtoXYZd50(ColorConvertTools.converColor(color));
        xyz = DoubleTools.scale(xyzD50[0], 6) + vSeparator
                + DoubleTools.scale(xyzD50[1], 6) + vSeparator
                + DoubleTools.scale(xyzD50[2], 6);

        cieLabValues = CIEColorSpace.XYZd50toCIELab(xyzD50[0], xyzD50[1], xyzD50[2]);
        cieLab = DoubleTools.scale(cieLabValues[0], 2) + vSeparator
                + DoubleTools.scale(cieLabValues[1], 2) + vSeparator
                + DoubleTools.scale(cieLabValues[2], 2);

        lchabValues = CIEColorSpace.LabtoLCHab(cieLabValues);
        lchab = DoubleTools.scale(lchabValues[0], 2) + vSeparator
                + DoubleTools.scale(lchabValues[1], 2) + vSeparator
                + DoubleTools.scale(lchabValues[2], 2);

        cieLuvValues = CIEColorSpace.XYZd50toCIELuv(xyzD50[0], xyzD50[1], xyzD50[2]);
        cieLuv = DoubleTools.scale(cieLuvValues[0], 2) + vSeparator
                + DoubleTools.scale(cieLuvValues[1], 2) + vSeparator
                + DoubleTools.scale(cieLuvValues[2], 2);

        lchuvValues = CIEColorSpace.LuvtoLCHuv(cieLuvValues);
        lchuv = DoubleTools.scale(lchuvValues[0], 2) + vSeparator
                + DoubleTools.scale(lchuvValues[1], 2) + vSeparator
                + DoubleTools.scale(lchuvValues[2], 2);

        return this;
    }

    public ColorData convert() {
        if (!needConvert()) {
            return this;
        }
        if (needCalculate()) {
            calculate();
        }
        if (color == null) {
            return this;
        }
        long h = Math.round(color.getHue());
        long s = Math.round(color.getSaturation() * 100);
        long b = Math.round(color.getBrightness() * 100);
        long a = Math.round(color.getOpacity() * 100);
        hsb = h + vSeparator + s + "%" + vSeparator + b + "%" + vSeparator + a + "%";
        hue = StringTools.fillLeftZero(h, 3);
        saturation = StringTools.fillLeftZero(s, 3);
        brightness = StringTools.fillLeftZero(b, 3);
        opacity = StringTools.fillLeftZero(a, 3);

        ryb = ColorConvertTools.hue2ryb(h);
        rybAngle = StringTools.fillLeftZero(FloatTools.toInt(ryb), 3);

        invertColor = color.invert();
        invertRGB = hsba(invertColor);

        complementaryColor = ColorConvertTools.converColor(ColorConvertTools.rybComplementary(this));
        complementaryRGB = hsba(complementaryColor);
        return this;
    }

    public ColorData cloneValues() {
        try {
            ColorData newData = (ColorData) super.clone();
            newData.setCpid(-1);
            newData.setPaletteid(-1);
            return newData;
        } catch (Exception e) {
            return null;
        }
    }

    public String hsba(Color c) {
        long h = Math.round(c.getHue());
        long s = Math.round(c.getSaturation() * 100);
        long b = Math.round(c.getBrightness() * 100);
        long a = Math.round(c.getOpacity() * 100);
        return h + vSeparator + s + "%" + vSeparator + b + "%" + vSeparator + a + "%";
    }

    public String display() {
        if (colorDisplay == null) {
            if (colorName != null) {
                colorDisplay = colorName + "\n";
            } else {
                colorDisplay = "";
            }
            if (needConvert()) {
                convert();
            }
            colorDisplay += rgba + "\n" + rgb + "\n" + colorValue + "\n"
                    + "sRGB: " + srgb + "\n"
                    + "HSBA: " + hsb + "\n"
                    + message("RYBAngle") + ": " + rybAngle + "°\n"
                    + message("Opacity") + ": " + opacity + "\n"
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
                    + (orderNumner == Float.MAX_VALUE ? ""
                            : ("\n" + message("OrderNumber") + ": " + orderNumner));
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
            if (needConvert()) {
                convert();
            }
            colorSimpleDisplay += colorValue + "\n" + rgba + "\n" + rgb + "\n"
                    + "sRGB: " + srgb + "\n"
                    + "HSBA: " + hsb + "\n"
                    + message("RYBAngle") + ": " + rybAngle + "°\n"
                    + message("CalculatedCMYK") + ": " + calculatedCMYK + "\n";
        }
        return colorSimpleDisplay;
    }

    public String html() {
        if (needConvert()) {
            convert();
        }
        ColorData invertData = new ColorData(invertColor).setvSeparator(vSeparator).convert();
        ColorData complementaryData = new ColorData(complementaryColor).setvSeparator(vSeparator).convert();
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Data"), message("Color"),
                message("RGBInvertColor"), message("RYBComplementaryColor")));
        StringTable table = new StringTable(names, message("Color"));
        List<String> row = new ArrayList<>();
        row.add(message("Color"));
        row.add("<DIV style=\"width: 50px;  background-color:"
                + color2css(getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
        row.add("<DIV style=\"width: 50px;  background-color:"
                + color2css(invertData.getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
        row.add("<DIV style=\"width: 50px;  background-color:"
                + color2css(complementaryData.getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Name"), getColorName() + "", invertData.getColorName() + "", complementaryData.getColorName() + ""));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("RGBA", getRgba(), invertData.getRgba(), complementaryData.getRgba()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("RGB", getRgb(), invertData.getRgb(), complementaryData.getRgb()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("sRGB", getSrgb(), invertData.getSrgb(), complementaryData.getSrgb()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("HSBA", getHsb(), invertData.getHsb(), complementaryData.getHsb()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Hue"), getHue(), invertData.getHue(), complementaryData.getHue()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Saturation"), getSaturation(), invertData.getSaturation(), complementaryData.getSaturation()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Brightness"), getBrightness(), invertData.getBrightness(), complementaryData.getBrightness()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("RYBAngle"), getRybAngle(), invertData.getRybAngle(), complementaryData.getRybAngle()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Opacity"), getOpacity(), invertData.getOpacity(), complementaryData.getOpacity()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("CalculatedCMYK"), getCalculatedCMYK(), invertData.getCalculatedCMYK(), complementaryData.getCalculatedCMYK()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Adobe RGB", getAdobeRGB(), invertData.getAdobeRGB(), complementaryData.getAdobeRGB()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Apple RGB", getAppleRGB(), invertData.getAppleRGB(), complementaryData.getAppleRGB()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("ECI RGB", getEciRGB(), invertData.getEciRGB(), complementaryData.getEciRGB()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("sRGB Linear", getSRGBLinear(), invertData.getSRGBLinear(), complementaryData.getSRGBLinear()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Adobe RGB Linear", getAdobeRGBLinear(), invertData.getAdobeRGBLinear(), complementaryData.getAdobeRGBLinear()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Apple RGB Linear", getAppleRGBLinear(), invertData.getAppleRGBLinear(), complementaryData.getAppleRGBLinear()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("ECI CMYK", getEciCMYK(), invertData.getEciCMYK(), complementaryData.getEciCMYK()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Adobe CMYK Uncoated FOGRA29", getAdobeCMYK(), invertData.getAdobeCMYK(), complementaryData.getAdobeCMYK()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("XYZ", getXyz(), invertData.getXyz(), complementaryData.getXyz()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("CIE-L*ab", getCieLab(), invertData.getCieLab(), complementaryData.getCieLab()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("LCH(ab)", getLchab(), invertData.getLchab(), complementaryData.getLchab()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("CIE-L*uv", getCieLuv(), invertData.getCieLuv(), complementaryData.getCieLuv()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("LCH(uv)", getLchuv(), invertData.getLchuv(), complementaryData.getLchuv()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Value"), getColorValue() + "", invertData.getColorValue() + "", complementaryData.getColorValue() + ""));
        table.add(row);
        return HtmlWriteTools.html(message("Color"), HtmlStyles.styleValue("Table"), table.body());
    }

    public String title() {
        if (colorName != null && !colorName.isBlank()) {
            return colorName + " " + rgba;
        } else {
            return rgba;
        }
    }

    /*
       customzied get/set
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

    public String getRybAngle() {
        if (needConvert()) {
            convert();
        }
        return rybAngle;
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
            MyBoxLog.debug(e);
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

    public double[] getXyzD50() {
        return xyzD50;
    }

    public void setXyzD50(double[] xyzD50) {
        this.xyzD50 = xyzD50;
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

    public Color getInvertColor() {
        return invertColor;
    }

    public Color getComplementaryColor() {
        return complementaryColor;
    }

    public String getHue() {
        return hue;
    }

    public String getSaturation() {
        return saturation;
    }

    public String getBrightness() {
        return brightness;
    }

    public String getInvertRGB() {
        return invertRGB;
    }

    public String getComplementaryRGB() {
        return complementaryRGB;
    }

    public boolean isIsSettingValues() {
        return isSettingValues;
    }

    public float getRyb() {
        return ryb;
    }

    public String getOpacity() {
        return opacity;
    }

    public void setOpacity(String opacity) {
        this.opacity = opacity;
    }

    public String getvSeparator() {
        return vSeparator;
    }

    public ColorData setvSeparator(String vSeparator) {
        this.vSeparator = vSeparator;
        return this;
    }

}
