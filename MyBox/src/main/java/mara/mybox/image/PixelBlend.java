package mara.mybox.image;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-3-24 11:24:03
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
// https://en.wikipedia.org/wiki/Blend_modes
// https://blog.csdn.net/bravebean/article/details/51392440
// https://www.cnblogs.com/bigdream6/p/8385886.html
// https://baike.baidu.com/item/%E6%B7%B7%E5%90%88%E6%A8%A1%E5%BC%8F/6700481?fr=aladdin
public abstract class PixelBlend {

    public enum ImagesBlendMode {
        NORMAL,
        DISSOLVE,
        DARKEN,
        MULTIPLY,
        COLOR_BURN,
        LINEAR_BURN,
        SOFT_BURN,
        LIGHTEN,
        SCREEN,
        COLOR_DODGE,
        LINEAR_DODGE,
        SOFT_DODGE,
        DIVIDE,
        VIVID_LIGHT,
        LINEAR_LIGHT,
        SUBTRACT,
        AVERAGE,
        OVERLAY,
        HARD_LIGHT,
        SOFT_LIGHT,
        DIFFERENCE,
        NEGATION,
        EXCLUSION,
        REFLECT,
        GLOW,
        FREEZE,
        HEAT,
        STAMP,
        RED,
        GREEN,
        BLUE,
        HUE,
        SATURATION,
        COLOR,
        LUMINOSITY
    }

    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected boolean orderReversed = false;

    protected Color foreColor, backColor;
    protected int red, green, blue, alpha;

    public PixelBlend() {
    }

    public PixelBlend(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public PixelBlend(ImagesBlendMode blendMode, float opacity) {
        this.blendMode = blendMode;
        this.opacity = opacity;
    }

    public PixelBlend(ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        this.blendMode = blendMode;
        this.opacity = opacity;
        this.orderReversed = orderReversed;
    }

    public PixelBlend(float alpha) {
        this.opacity = alpha;
    }

    public static List<String> allBlendModes() {
        return Arrays.asList(AppVariables.message("NormalMode"),
                AppVariables.message("DissolveMode"), AppVariables.message("MultiplyMode"), AppVariables.message("ScreenMode"),
                AppVariables.message("OverlayMode"), AppVariables.message("HardLightMode"), AppVariables.message("SoftLightMode"),
                AppVariables.message("ColorDodgeMode"), AppVariables.message("LinearDodgeMode"), AppVariables.message("DivideMode"),
                AppVariables.message("ColorBurnMode"), AppVariables.message("LinearBurnMode"), AppVariables.message("VividLightMode"),
                AppVariables.message("LinearLightMode"), AppVariables.message("SubtractMode"), AppVariables.message("DifferenceMode"),
                AppVariables.message("ExclusionMode"), AppVariables.message("DarkenMode"), AppVariables.message("LightenMode"),
                AppVariables.message("HueMode"), AppVariables.message("SaturationMode"), AppVariables.message("ColorMode"),
                AppVariables.message("LuminosityMode"));
    }

    public static ImagesBlendMode getBlendModeByName(String mode) {

        if (AppVariables.message("NormalMode").equals(mode)) {
            return ImagesBlendMode.NORMAL;

        } else if (AppVariables.message("DissolveMode").equals(mode)) {
            return ImagesBlendMode.DISSOLVE;

        } else if (AppVariables.message("MultiplyMode").equals(mode)) {
            return ImagesBlendMode.MULTIPLY;

        } else if (AppVariables.message("ScreenMode").equals(mode)) {
            return ImagesBlendMode.SCREEN;

        } else if (AppVariables.message("OverlayMode").equals(mode)) {
            return ImagesBlendMode.OVERLAY;

        } else if (AppVariables.message("HardLightMode").equals(mode)) {
            return ImagesBlendMode.HARD_LIGHT;

        } else if (AppVariables.message("SoftLightMode").equals(mode)) {
            return ImagesBlendMode.SOFT_LIGHT;

        } else if (AppVariables.message("ColorDodgeMode").equals(mode)) {
            return ImagesBlendMode.COLOR_DODGE;

        } else if (AppVariables.message("LinearDodgeMode").equals(mode)) {
            return ImagesBlendMode.LINEAR_DODGE;

        } else if (AppVariables.message("DivideMode").equals(mode)) {
            return ImagesBlendMode.DIVIDE;

        } else if (AppVariables.message("ColorBurnMode").equals(mode)) {
            return ImagesBlendMode.COLOR_BURN;

        } else if (AppVariables.message("LinearBurnMode").equals(mode)) {
            return ImagesBlendMode.LINEAR_BURN;

        } else if (AppVariables.message("VividLightMode").equals(mode)) {
            return ImagesBlendMode.VIVID_LIGHT;

        } else if (AppVariables.message("LinearLightMode").equals(mode)) {
            return ImagesBlendMode.LINEAR_LIGHT;

        } else if (AppVariables.message("SubtractMode").equals(mode)) {
            return ImagesBlendMode.SUBTRACT;

        } else if (AppVariables.message("DifferenceMode").equals(mode)) {
            return ImagesBlendMode.DIFFERENCE;

        } else if (AppVariables.message("ExclusionMode").equals(mode)) {
            return ImagesBlendMode.EXCLUSION;

        } else if (AppVariables.message("DarkenMode").equals(mode)) {
            return ImagesBlendMode.DARKEN;

        } else if (AppVariables.message("LightenMode").equals(mode)) {
            return ImagesBlendMode.LIGHTEN;

        } else if (AppVariables.message("HueMode").equals(mode)) {
            return ImagesBlendMode.HUE;

        } else if (AppVariables.message("SaturationMode").equals(mode)) {
            return ImagesBlendMode.SATURATION;

        } else if (AppVariables.message("ColorMode").equals(mode)) {
            return ImagesBlendMode.COLOR;

        } else if (AppVariables.message("LuminosityMode").equals(mode)) {
            return ImagesBlendMode.LUMINOSITY;

        } else {
            return ImagesBlendMode.NORMAL;
        }

    }

    protected int blend(int forePixel, int backPixel) {
        if (forePixel == 0) {                       // Pass transparency
            return backPixel;
        }
        if (backPixel == 0) {                       // Pass transparency
            return forePixel;
        }
        if (orderReversed) {
            foreColor = new Color(backPixel);
            backColor = new Color(forePixel);
        } else {
            foreColor = new Color(forePixel);
            backColor = new Color(backPixel);
        }
        alpha = (int) (opacity * 255);
        makeRGB();
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor.getRGB();
    }

    protected void makeRGB() {
        red = (int) (foreColor.getRed() * opacity + backColor.getRed() * (1.0f - opacity));
        green = (int) (foreColor.getGreen() * opacity + backColor.getGreen() * (1.0f - opacity));
        blue = (int) (foreColor.getBlue() * opacity + backColor.getBlue() * (1.0f - opacity));
        alpha = 255;
    }

    /*
        static methods
     */
    public static PixelBlend newColorBlend(ImagesBlendMode blendMode) {
        switch (blendMode) {
            case NORMAL:
                return new NormalBlend();
            case DISSOLVE:
                return new DissolveBlend();
            case MULTIPLY:
                return new MultiplyBlend();
            case SCREEN:
                return new ScreenBlend();
            case OVERLAY:
                return new OverlayBlend();
            case HARD_LIGHT:
                return new HardLightBlend();
            case SOFT_LIGHT:
                return new SoftLightBlend();
            case COLOR_DODGE:
                return new ColorDodgeBlend();
            case LINEAR_DODGE:
                return new LinearDodgeBlend();
            case DIVIDE:
                return new DivideBlend();
            case COLOR_BURN:
                return new ColorBurnBlend();
            case LINEAR_BURN:
                return new LinearBurnBlend();
            case VIVID_LIGHT:
                return new VividLightBlend();
            case LINEAR_LIGHT:
                return new LinearLightBlend();
            case SUBTRACT:
                return new SubtractBlend();
            case DIFFERENCE:
                return new DifferenceBlend();
            case EXCLUSION:
                return new ExclusionBlend();
            case DARKEN:
                return new DarkenBlend();
            case LIGHTEN:
                return new LightenBlend();
            case HUE:
                return new HueBlend();
            case SATURATION:
                return new SaturationBlend();
            case LUMINOSITY:
                return new LuminosityBlend();
            case COLOR:
                return new ColorBlend();
            default:
                return new NormalBlend();

        }
    }

    public static class NormalBlend extends PixelBlend {

        public NormalBlend() {
            this.blendMode = ImagesBlendMode.NORMAL;
        }

    }

    public static class DissolveBlend extends PixelBlend {

        public DissolveBlend() {
            this.blendMode = ImagesBlendMode.DISSOLVE;

        }

        @Override
        protected void makeRGB() {
            float random = new Random().nextInt(101) / 100.0f;
            red = (int) (foreColor.getRed() * random + backColor.getRed() * (1.0f - random));
            green = (int) (foreColor.getGreen() * random + backColor.getGreen() * (1.0f - random));
            blue = (int) (foreColor.getBlue() * random + backColor.getBlue() * (1.0f - random));
        }
    }

    public static class MultiplyBlend extends PixelBlend {

        public MultiplyBlend() {
            this.blendMode = ImagesBlendMode.MULTIPLY;

        }

        @Override
        protected void makeRGB() {
            red = foreColor.getRed() * backColor.getRed() / 255;
            green = foreColor.getGreen() * backColor.getGreen() / 255;
            blue = foreColor.getBlue() * backColor.getBlue() / 255;
        }
    }

    public static class ScreenBlend extends PixelBlend {

        public ScreenBlend() {
            this.blendMode = ImagesBlendMode.SCREEN;

        }

        @Override
        protected void makeRGB() {

            red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 255;
            green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 255;
            blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 255;

        }
    }

    public static class OverlayBlend extends PixelBlend {

        public OverlayBlend() {
            this.blendMode = ImagesBlendMode.OVERLAY;

        }

        @Override
        protected void makeRGB() {

            if (backColor.getRed() < 128) {
                red = foreColor.getRed() * backColor.getRed() / 128;
            } else {
                red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 128;
            }
            if (backColor.getGreen() < 128) {
                green = foreColor.getGreen() * backColor.getGreen() / 128;
            } else {
                green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 128;
            }
            if (backColor.getBlue() < 128) {
                blue = foreColor.getBlue() * backColor.getBlue() / 128;
            } else {
                blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 128;
            }

        }
    }

    public static class HardLightBlend extends PixelBlend {

        public HardLightBlend() {
            this.blendMode = ImagesBlendMode.HARD_LIGHT;

        }

        @Override
        protected void makeRGB() {

            if (foreColor.getRed() < 128) {
                red = foreColor.getRed() * backColor.getRed() / 128;
            } else {
                red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 128;
            }
            if (foreColor.getGreen() < 128) {
                green = foreColor.getGreen() * backColor.getGreen() / 128;
            } else {
                green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 128;
            }
            if (foreColor.getBlue() < 128) {
                blue = foreColor.getBlue() * backColor.getBlue() / 128;
            } else {
                blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 128;
            }

        }
    }

    public static class SoftLightBlend extends PixelBlend {

        public SoftLightBlend() {
            this.blendMode = ImagesBlendMode.SOFT_LIGHT;

        }

        @Override
        protected void makeRGB() {

            if (foreColor.getRed() < 128) {
                red = backColor.getRed()
                        + (2 * foreColor.getRed() - 255) * (backColor.getRed() - backColor.getRed() * backColor.getRed() / 255) / 255;
            } else {
                red = (int) (backColor.getRed()
                        + (2 * foreColor.getRed() - 255) * (Math.sqrt(backColor.getRed() / 255.0f) * 255 - backColor.getRed()) / 255);
            }
            if (foreColor.getGreen() < 128) {
                green = backColor.getGreen()
                        + (2 * foreColor.getGreen() - 255) * (backColor.getGreen() - backColor.getGreen() * backColor.getGreen() / 255) / 255;
            } else {
                green = (int) (backColor.getGreen()
                        + (2 * foreColor.getGreen() - 255) * (Math.sqrt(backColor.getGreen() / 255.0f) * 255 - backColor.getGreen()) / 255);
            }
            if (foreColor.getBlue() < 128) {
                blue = backColor.getBlue()
                        + (2 * foreColor.getBlue() - 255) * (backColor.getBlue() - backColor.getBlue() * backColor.getBlue() / 255) / 255;
            } else {
                blue = (int) (backColor.getBlue()
                        + (2 * foreColor.getBlue() - 255) * (Math.sqrt(backColor.getBlue() / 255.0f) * 255 - backColor.getBlue()) / 255);
            }

        }
    }

    public static class ColorDodgeBlend extends PixelBlend {

        public ColorDodgeBlend() {
            this.blendMode = ImagesBlendMode.COLOR_DODGE;

        }

        @Override
        protected void makeRGB() {

            red = foreColor.getRed() == 255 ? 255
                    : (backColor.getRed() + (foreColor.getRed() * backColor.getRed()) / (255 - foreColor.getRed()));
            green = foreColor.getGreen() == 255 ? 255
                    : (backColor.getGreen() + (foreColor.getGreen() * backColor.getGreen()) / (255 - foreColor.getGreen()));
            blue = foreColor.getBlue() == 255 ? 255
                    : (backColor.getBlue() + (foreColor.getBlue() * backColor.getBlue()) / (255 - foreColor.getBlue()));

        }
    }

    public static class LinearDodgeBlend extends PixelBlend {

        public LinearDodgeBlend() {
            this.blendMode = ImagesBlendMode.LINEAR_DODGE;

        }

        @Override
        protected void makeRGB() {

            red = foreColor.getRed() + backColor.getRed();
            green = foreColor.getGreen() + backColor.getGreen();
            blue = foreColor.getBlue() + backColor.getBlue();

        }
    }

    public static class DivideBlend extends PixelBlend {

        public DivideBlend() {
            this.blendMode = ImagesBlendMode.DIVIDE;

        }

        @Override
        protected void makeRGB() {

            red = foreColor.getRed() == 0 ? 255 : ((backColor.getRed() * 255) / foreColor.getRed());
            green = foreColor.getGreen() == 0 ? 255 : ((backColor.getGreen() * 255) / foreColor.getGreen());
            blue = foreColor.getBlue() == 0 ? 255 : ((backColor.getBlue() * 255) / foreColor.getBlue());
        }
    }

    public static class ColorBurnBlend extends PixelBlend {

        public ColorBurnBlend() {
            this.blendMode = ImagesBlendMode.COLOR_BURN;

        }

        @Override
        protected void makeRGB() {

            red = foreColor.getRed() == 0 ? 0
                    : (backColor.getRed() - (255 - foreColor.getRed()) * 255 / foreColor.getRed());
            green = foreColor.getGreen() == 0 ? 0
                    : (backColor.getGreen() - (255 - foreColor.getGreen()) * 255 / foreColor.getGreen());
            blue = foreColor.getBlue() == 0 ? 0
                    : (backColor.getBlue() - (255 - foreColor.getBlue()) * 255 / foreColor.getBlue());

        }
    }

    public static class LinearBurnBlend extends PixelBlend {

        public LinearBurnBlend() {
            this.blendMode = ImagesBlendMode.LINEAR_BURN;

        }

        @Override
        protected void makeRGB() {

            red = backColor.getRed() == 0 ? 0
                    : foreColor.getRed() + backColor.getRed() - 255;
            green = backColor.getGreen() == 0 ? 0
                    : foreColor.getGreen() + backColor.getGreen() - 255;
            blue = backColor.getBlue() == 0 ? 0
                    : foreColor.getBlue() + backColor.getBlue() - 255;

        }
    }

    public static class VividLightBlend extends PixelBlend {

        public VividLightBlend() {
            this.blendMode = ImagesBlendMode.VIVID_LIGHT;

        }

        @Override
        protected void makeRGB() {

            if (foreColor.getRed() < 128) {
                red = foreColor.getRed() == 0 ? backColor.getRed()
                        : (backColor.getRed() - (255 - backColor.getRed()) * (255 - 2 * foreColor.getRed()) / (2 * foreColor.getRed()));
            } else {
                red = foreColor.getRed() == 255 ? backColor.getRed()
                        : (backColor.getRed() + backColor.getRed() * (2 * foreColor.getRed() - 255) / (2 * (255 - foreColor.getRed())));
            }
            if (foreColor.getGreen() < 128) {
                green = foreColor.getGreen() == 0 ? backColor.getGreen()
                        : (backColor.getGreen() - (255 - backColor.getGreen()) * (255 - 2 * foreColor.getGreen()) / (2 * foreColor.getGreen()));
            } else {
                green = foreColor.getGreen() == 255 ? backColor.getGreen()
                        : (backColor.getGreen() + backColor.getGreen() * (2 * foreColor.getGreen() - 255) / (2 * (255 - foreColor.getGreen())));
            }
            if (foreColor.getBlue() < 128) {
                blue = foreColor.getBlue() == 0 ? backColor.getBlue()
                        : (backColor.getBlue() - (255 - backColor.getBlue()) * (255 - 2 * foreColor.getBlue()) / (2 * foreColor.getBlue()));
            } else {
                blue = foreColor.getBlue() == 255 ? backColor.getBlue()
                        : (backColor.getBlue() + backColor.getBlue() * (2 * foreColor.getBlue() - 255) / (2 * (255 - foreColor.getBlue())));
            }

        }
    }

    public static class LinearLightBlend extends PixelBlend {

        public LinearLightBlend() {
            this.blendMode = ImagesBlendMode.LINEAR_LIGHT;

        }

        @Override
        protected void makeRGB() {

            red = 2 * foreColor.getRed() + backColor.getRed() - 255;
            green = 2 * foreColor.getGreen() + backColor.getGreen() - 255;
            blue = 2 * foreColor.getBlue() + backColor.getBlue() - 255;

        }
    }

    public static class SubtractBlend extends PixelBlend {

        public SubtractBlend() {
            this.blendMode = ImagesBlendMode.SUBTRACT;

        }

        @Override
        protected void makeRGB() {

            red = backColor.getRed() - foreColor.getRed();
            green = backColor.getGreen() - foreColor.getGreen();
            blue = backColor.getBlue() - foreColor.getBlue();

        }
    }

    public static class DifferenceBlend extends PixelBlend {

        public DifferenceBlend() {
            this.blendMode = ImagesBlendMode.DIFFERENCE;

        }

        @Override
        protected void makeRGB() {

            red = Math.abs(backColor.getRed() - foreColor.getRed());
            green = Math.abs(backColor.getGreen() - foreColor.getGreen());
            blue = Math.abs(backColor.getBlue() - foreColor.getBlue());

        }
    }

    public static class ExclusionBlend extends PixelBlend {

        public ExclusionBlend() {
            this.blendMode = ImagesBlendMode.EXCLUSION;

        }

        @Override
        protected void makeRGB() {

            red = backColor.getRed() + foreColor.getRed() - backColor.getRed() * foreColor.getRed() / 128;
            green = backColor.getGreen() + foreColor.getGreen() - backColor.getGreen() * foreColor.getGreen() / 128;
            blue = backColor.getBlue() + foreColor.getBlue() - backColor.getBlue() * foreColor.getBlue() / 128;

        }
    }

    public static class DarkenBlend extends PixelBlend {

        public DarkenBlend() {
            this.blendMode = ImagesBlendMode.DARKEN;

        }

        @Override
        protected void makeRGB() {

            red = Math.min(backColor.getRed(), foreColor.getRed());
            green = Math.min(backColor.getGreen(), foreColor.getGreen());
            blue = Math.min(backColor.getBlue(), foreColor.getBlue());

        }
    }

    public static class LightenBlend extends PixelBlend {

        public LightenBlend() {
            this.blendMode = ImagesBlendMode.LIGHTEN;

        }

        @Override
        protected void makeRGB() {

            red = Math.max(backColor.getRed(), foreColor.getRed());
            green = Math.max(backColor.getGreen(), foreColor.getGreen());
            blue = Math.max(backColor.getBlue(), foreColor.getBlue());

        }
    }

    public static class HueBlend extends PixelBlend {

        public HueBlend() {
            this.blendMode = ImagesBlendMode.HUE;

        }

        @Override
        protected int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] hA = ImageColor.pixel2HSB(forePixel);
            float[] hB = ImageColor.pixel2HSB(backPixel);
            Color hColor = Color.getHSBColor(hA[0], hB[1], hB[2]);
            return hColor.getRGB();
        }
    }

    public static class SaturationBlend extends PixelBlend {

        public SaturationBlend() {
            this.blendMode = ImagesBlendMode.SATURATION;

        }

        @Override
        protected int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] sA = ImageColor.pixel2HSB(forePixel);
            float[] sB = ImageColor.pixel2HSB(backPixel);
            Color sColor = Color.getHSBColor(sB[0], sA[1], sB[2]);
            return sColor.getRGB();
        }
    }

    public static class LuminosityBlend extends PixelBlend {

        public LuminosityBlend() {
            this.blendMode = ImagesBlendMode.LUMINOSITY;

        }

        @Override
        protected int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] bA = ImageColor.pixel2HSB(forePixel);
            float[] bB = ImageColor.pixel2HSB(backPixel);
            Color newColor = Color.getHSBColor(bB[0], bB[1], bA[2]);
            return newColor.getRGB();
        }
    }

    public static class ColorBlend extends PixelBlend {

        public ColorBlend() {
            this.blendMode = ImagesBlendMode.COLOR;

        }

        @Override
        protected int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] cA = ImageColor.pixel2HSB(forePixel);
            float[] cB = ImageColor.pixel2HSB(backPixel);
            Color cColor = Color.getHSBColor(cA[0], cA[1], cB[2]);
            return cColor.getRGB();
        }
    }

    /*
        get/set
     */
    public ImagesBlendMode getBlendMode() {
        return blendMode;
    }

    public PixelBlend setBlendMode(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
        return this;
    }

    public float getOpacity() {
        return opacity;
    }

    public PixelBlend setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public boolean isOrderReversed() {
        return orderReversed;
    }

    public PixelBlend setOrderReversed(boolean orderReversed) {
        this.orderReversed = orderReversed;
        return this;
    }

    public Color getForeColor() {
        return foreColor;
    }

    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

}
