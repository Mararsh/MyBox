package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageQuantization extends PixelsOperation {

    protected QuantizationAlgorithm algorithm;
    protected int channelSize, rgbMod, rgbOffset,
            hueMod, saturationMod, brightnessMod, hueOffset, saturationOffset, brightnessOffset;

    public static enum QuantizationAlgorithm {
        RGB_Uniform, HSB_Uniform, Statistic, MedianCut, kMeansClustering, ANN
    }

    public ImageQuantization() {
        init(QuantizationAlgorithm.RGB_Uniform, 8);
    }

    public ImageQuantization(BufferedImage image) {
        init(image, QuantizationAlgorithm.RGB_Uniform, 8);
    }

    public ImageQuantization(BufferedImage image, QuantizationAlgorithm algorithm, int channelSize) {
        init(image, algorithm, channelSize);
    }

    public ImageQuantization(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        init(this.image, QuantizationAlgorithm.RGB_Uniform, 8);
    }

    private void init(QuantizationAlgorithm algorithm, int channelSize) {
        set(algorithm, channelSize);
    }

    private void init(BufferedImage image, QuantizationAlgorithm algorithm, int channelSize) {
        this.image = image;
        set(algorithm, channelSize);
    }

    public void set(QuantizationAlgorithm algorithm, int channelSize) {
        this.operationType = OperationType.Quantization;
        this.algorithm = algorithm;
        setChannelSize(channelSize);
    }

    public void set(BufferedImage image, QuantizationAlgorithm algorithm, int channelSize,
            int rgbMod, int rgbOffset, int hueMod, int saturationMod, int brightnessMod,
            int hueOffset, int saturationOffset, int brightnessOffset) {
        this.operationType = OperationType.Quantization;
        this.image = image;
        this.algorithm = algorithm;
        this.channelSize = channelSize;
        this.rgbMod = rgbMod;
        this.rgbOffset = rgbOffset;
        this.hueMod = hueMod;
        this.hueOffset = hueOffset;
        this.saturationMod = saturationMod;
        this.saturationOffset = saturationOffset;
        this.brightnessMod = brightnessMod;
        this.brightnessOffset = brightnessOffset;
    }

    public void setChannelSize(int channelSize) {
        this.channelSize = channelSize;
        rgbMod = 256 / channelSize;
        rgbOffset = rgbMod / 2;
        hueMod = 360 / channelSize;
        hueOffset = hueMod / 2;
        saturationMod = brightnessMod = 100 / channelSize;
        saturationOffset = brightnessOffset = saturationMod / 2;
    }

    @Override
    public Color operateColor(Color color) {
        switch (algorithm) {
            case RGB_Uniform:
                return rgbColorUniform(color);
            case HSB_Uniform:
                return hsbColorUniform(color);
            case Statistic:
                return rgbColorUniform(color);
            case kMeansClustering:
                return rgbColorUniform(color);
            case ANN:
                return rgbColorUniform(color);
            default:
                return rgbColorUniform(color);
        }

    }

    public Color rgbColorUniform(Color color) {
        if (rgbMod <= 0) {
            return color;
        }
        int red, green, blue;

        int v = color.getRed();
        v = v - (v % rgbMod) + rgbOffset;
        red = Math.min(Math.max(v, 0), 255);

        v = color.getGreen();
        v = v - (v % rgbMod) + rgbOffset;
        green = Math.min(Math.max(v, 0), 255);

        v = color.getBlue();
        v = v - (v % rgbMod) + rgbOffset;
        blue = Math.min(Math.max(v, 0), 255);

        Color newColor = new Color(red, green, blue, color.getAlpha());
        return newColor;
    }

    public Color hsbColorUniform(Color color) {
        if (hueMod <= 0 || saturationMod <= 0 || brightnessMod <= 0) {
            return color;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float h, s, b;

        int v = (int) (hsb[0] * 360);
        v = v - (v % hueMod) + hueOffset;
        h = (float) Math.min(Math.max(v / 360.0f, 0.0f), 1.0f);

        v = (int) (hsb[1] * 100);
        v = v - (v % saturationMod) + saturationOffset;
        s = (float) Math.min(Math.max(v / 100.0f, 0.0f), 1.0f);

        v = (int) (hsb[2] * 100);
        v = v - (v % brightnessMod) + brightnessOffset;
        b = (float) Math.min(Math.max(v / 100.0f, 0.0f), 1.0f);

        Color newColor = Color.getHSBColor(h, s, b);
        return newColor;
    }

    public QuantizationAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(QuantizationAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getChannelSize() {
        return channelSize;
    }

    public int getRgbMod() {
        return rgbMod;
    }

    public void setRgbMod(int rgbMod) {
        this.rgbMod = rgbMod;
    }

    public int getRgbOffset() {
        return rgbOffset;
    }

    public void setRgbOffset(int rgbOffset) {
        this.rgbOffset = rgbOffset;
    }

    public int getHueMod() {
        return hueMod;
    }

    public void setHueMod(int hueMod) {
        this.hueMod = hueMod;
    }

    public int getSaturationMod() {
        return saturationMod;
    }

    public void setSaturationMod(int saturationMod) {
        this.saturationMod = saturationMod;
    }

    public int getBrightnessMod() {
        return brightnessMod;
    }

    public void setBrightnessMod(int brightnessMod) {
        this.brightnessMod = brightnessMod;
    }

    public int getHueOffset() {
        return hueOffset;
    }

    public void setHueOffset(int hueOffset) {
        this.hueOffset = hueOffset;
    }

    public int getSaturationOffset() {
        return saturationOffset;
    }

    public void setSaturationOffset(int saturationOffset) {
        this.saturationOffset = saturationOffset;
    }

    public int getBrightnessOffset() {
        return brightnessOffset;
    }

    public void setBrightnessOffset(int brightnessOffset) {
        this.brightnessOffset = brightnessOffset;
    }

}
