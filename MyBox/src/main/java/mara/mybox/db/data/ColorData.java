package mara.mybox.db.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import mara.mybox.color.AdobeRGB;
import mara.mybox.color.AppleRGB;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.SRGB;
import mara.mybox.db.table.TableColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2020-1-7
 * @License Apache License Version 2.0
 */
public class ColorData {

    protected int colorValue;
    protected double paletteIndex = -1;
    protected javafx.scene.paint.Color color;
    protected String rgba, rgb, colorName, colorDisplay, colorSimpleDisplay;
    protected String srgb, hsb, adobeRGB, appleRGB, eciRGB, SRGBLinear, adobeRGBLinear,
            appleRGBLinear, calculatedCMYK, eciCMYK, adobeCMYK, xyz, cieLab,
            lchab, cieLuv, lchuv;
    protected float[] adobeRGBValues, appleRGBValues, eciRGBValues, eciCmykValues, adobeCmykValues;
    protected double[] cmyk, xyzValues, cieLabValues, lchabValues, cieLuvValues, lchuvValues;
    protected BooleanProperty inPalette;
    protected boolean isSettingValues;

    private void init() {
        inPalette = new SimpleBooleanProperty(false);
    }

    public ColorData() {
        init();
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
            color = javafx.scene.paint.Color.web(web);
            rgba = FxmlColor.color2rgba(color);  // rgba is saved as upper-case in db
            rgb = FxmlColor.color2rgb(color);
            bindInPalette();
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
    }

    public void bindInPalette() {
        if (inPalette == null) {
            inPalette = new SimpleBooleanProperty(false);
        }
        inPalette.addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
            try {
//                MyBoxLog.debug(colorName + " " + rgba + " " + newVal);
                if (isSettingValues) {
                    return;
                }
                if (newVal) {
                    ColorData data = TableColorData.endPalette(this, false);
                    if (data != null) {
                        paletteIndex = data.getPaletteIndex();
                    }
                } else {
                    paletteIndex = -1;
                    TableColorData.removeFromPalette(rgba);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
            }
        });
    }

    public ColorData calculate() {
        if (rgba == null || srgb != null) {
            return this;
        }
        colorValue = ImageColor.getRGB(color);
        if (colorName == null) {
            colorName = "";
        }
        srgb = Math.round(color.getRed() * 255) + " "
                + Math.round(color.getGreen() * 255) + " "
                + Math.round(color.getBlue() * 255) + " "
                + Math.round(color.getOpacity() * 100) + "%";
        hsb = Math.round(color.getHue()) + " "
                + Math.round(color.getSaturation() * 100) + "% "
                + Math.round(color.getBrightness() * 100) + "%";

        adobeRGBValues = SRGB.srgb2profile(ImageValue.adobeRGBProfile(), color);
        adobeRGB = Math.round(adobeRGBValues[0] * 255) + " "
                + Math.round(adobeRGBValues[1] * 255) + " "
                + Math.round(adobeRGBValues[2] * 255);

        appleRGBValues = SRGB.srgb2profile(ImageValue.appleRGBProfile(), color);
        appleRGB = Math.round(appleRGBValues[0] * 255) + " "
                + Math.round(appleRGBValues[1] * 255) + " "
                + Math.round(appleRGBValues[2] * 255);

        eciRGBValues = SRGB.srgb2profile(ImageValue.eciRGBProfile(), color);
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

        eciCmykValues = SRGB.srgb2profile(ImageValue.eciCmykProfile(), color);
        eciCMYK = Math.round(eciCmykValues[0] * 100) + " " + Math.round(eciCmykValues[1] * 100) + " "
                + Math.round(eciCmykValues[2] * 100) + " " + Math.round(eciCmykValues[3] * 100);

        adobeCmykValues = SRGB.srgb2profile(ImageValue.adobeCmykProfile(), color);
        adobeCMYK = Math.round(adobeCmykValues[0] * 100) + " " + Math.round(adobeCmykValues[1] * 100) + " "
                + Math.round(adobeCmykValues[2] * 100) + " " + Math.round(adobeCmykValues[3] * 100);

        xyzValues = SRGB.toXYZd50(ImageColor.converColor(color));
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
                    + "Adobe RGB: " + adobeRGB + "\n"
                    + "Apple RGB: " + appleRGB + "\n"
                    + "ECI RGB: " + eciRGB + "\n"
                    + "sRGB Linear: " + SRGBLinear + "\n"
                    + "Adobe RGB Linear: " + adobeRGBLinear + "\n"
                    + "Apple RGB Linear: " + appleRGBLinear + "\n"
                    + "Calculated CMYK: " + calculatedCMYK + "\n"
                    + "ECI CMYK: " + eciCMYK + "\n"
                    + "Adobe CMYK Uncoated FOGRA29: " + adobeCMYK + "\n"
                    + "XYZ: " + xyz + "\n"
                    + "CIE-L*ab: " + cieLab + "\n"
                    + "LCH(ab): " + lchab + "\n"
                    + "CIE-L*uv: " + cieLuv + "\n"
                    + "LCH(uv): " + lchuv;
        }
        return colorDisplay;
    }

    public String htmlDisplay() {
        return display().replaceAll("\n", "</br>");
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
                    + "HSB: " + hsb;
        }
        return colorSimpleDisplay;
    }

    public String htmlSimpleDisplay() {
        return simpleDisplay().replaceAll("\n", "</br>");
    }


    /*
        get/set
     */
    public Color getColor() {
        if (color == null && rgba != null) {
            setWeb(rgba);
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

    public void setColor(Color color) {
        this.color = color;
    }

    public int getColorValue() {
        return colorValue;
    }

    public void setColorValue(int colorValue) {
        this.colorValue = colorValue;
    }

    public String getRgba() {
        return rgba.toUpperCase();
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

    public String getRgb() {
        return rgb;
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

    public double getPaletteIndex() {
        return paletteIndex;
    }

    public void setPaletteIndex(double paletteIndex) {
        this.paletteIndex = paletteIndex;
    }

    public boolean getInPalette() {
        return inPalette.get();
    }

    public BooleanProperty getInPaletteProperty() {
        return inPalette;
    }

    public void setInPalette(boolean in) {
        isSettingValues = true;
        if (inPalette == null) {
            inPalette = new SimpleBooleanProperty(in);
        } else {
            inPalette.set(in);
        }
        isSettingValues = false;
    }

}
