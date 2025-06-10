package mara.mybox.image.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.image.data.PixelsBlend.ImagesBlendMode;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-3-24 11:24:03
 * @License Apache License Version 2.0
 */
// https://en.wikipedia.org/wiki/Blend_modes
// https://blog.csdn.net/bravebean/article/details/51392440
// https://www.cnblogs.com/bigdream6/p/8385886.html
// https://baike.baidu.com/item/%E6%B7%B7%E5%90%88%E6%A8%A1%E5%BC%8F/6700481?fr=aladdin
public class PixelsBlendFactory {

    public static List<String> blendModes() {
        List<String> names = new ArrayList<>();
        for (ImagesBlendMode mode : ImagesBlendMode.values()) {
            names.add(modeName(mode));
        }
        return names;
    }

    public static ImagesBlendMode blendMode(String name) {
        if (name == null) {
            return null;
        }
        for (ImagesBlendMode mode : ImagesBlendMode.values()) {
            if (Languages.matchIgnoreCase(modeName(mode), name)) {
                return mode;
            }
        }
        return null;
    }

    public static String modeName(ImagesBlendMode mode) {
        if (mode == null) {
            return null;
        }
        switch (mode) {
            case KubelkaMunk:
                return message("KubelkaMunkMode");
            case CMYK:
                return message("CMYKMode");
            case CMYK_WEIGHTED:
                return message("CMYKWeightedMode");
            case MULTIPLY:
                return message("MultiplyMode");
            case NORMAL:
                return message("NormalMode");
            case DISSOLVE:
                return message("DissolveMode");
            case DARKEN:
                return message("DarkenMode");
            case COLOR_BURN:
                return message("ColorBurnMode");
            case LINEAR_BURN:
                return message("LinearBurnMode");
            case LIGHTEN:
                return message("LightenMode");
            case SCREEN:
                return message("ScreenMode");
            case COLOR_DODGE:
                return message("ColorDodgeMode");
            case LINEAR_DODGE:
                return message("LinearDodgeMode");
            case DIVIDE:
                return message("DivideMode");
            case VIVID_LIGHT:
                return message("VividLightMode");
            case LINEAR_LIGHT:
                return message("LinearLightMode");
            case SUBTRACT:
                return message("SubtractMode");
            case OVERLAY:
                return message("OverlayMode");
            case HARD_LIGHT:
                return message("HardLightMode");
            case SOFT_LIGHT:
                return message("SoftLightMode");
            case DIFFERENCE:
                return message("DifferenceMode");
            case EXCLUSION:
                return message("ExclusionMode");
            case HUE:
                return message("HueMode");
            case SATURATION:
                return message("SaturationMode");
            case COLOR:
                return message("ColorMode");
            case LUMINOSITY:
                return message("LuminosityMode");
        }
        return null;
    }

    public static PixelsBlend create(ImagesBlendMode blendMode) {
        if (blendMode == null) {
            return null;
        }
        switch (blendMode) {
            case KubelkaMunk:
                return new KubelkaMunkBlend();
            case CMYK:
                return new CMYKBlend(false);
            case CMYK_WEIGHTED:
                return new CMYKBlend(true);
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

    public static class NormalBlend extends PixelsBlend {

        public NormalBlend() {
            this.blendMode = ImagesBlendMode.NORMAL;
        }

    }

    public static class DissolveBlend extends PixelsBlend {

        public DissolveBlend() {
            this.blendMode = ImagesBlendMode.DISSOLVE;

        }

        @Override
        public void makeRGB() {
            float random = new Random().nextInt(101) / 100.0f;
            red = (int) (foreColor.getRed() * random + backColor.getRed() * (1.0f - random));
            green = (int) (foreColor.getGreen() * random + backColor.getGreen() * (1.0f - random));
            blue = (int) (foreColor.getBlue() * random + backColor.getBlue() * (1.0f - random));
        }
    }

    public static class KubelkaMunkBlend extends PixelsBlend {

        public KubelkaMunkBlend() {
            this.blendMode = ImagesBlendMode.KubelkaMunk;
        }

        @Override
        public void makeRGB() {
            Color blended = mixPigments(foreColor, backColor, weight);
            red = blended.getRed();
            green = blended.getGreen();
            blue = blended.getBlue();
        }

        // Provided by deepseek.
        public static Color mixPigments(Color color1, Color color2, double ratio) {
            // 1. 将RGB转换为反射率（0-255 -> 0.0-1.0）
            double[] reflectance1 = toReflectance(color1);
            double[] reflectance2 = toReflectance(color2);

            // 2. 使用Kubelka-Munk公式计算混合后的吸收/散射
            double[] mixed = new double[3];
            for (int i = 0; i < 3; i++) {
                double k1 = kmTransform(reflectance1[i]);
                double k2 = kmTransform(reflectance2[i]);

                // 3. 按比例混合K值
                double kmMixed = ratio * k1 + (1 - ratio) * k2;

                // 4. 反向转换回反射率
                mixed[i] = inverseKmTransform(kmMixed);
            }

            // 5. 将反射率转回RGB
            return toColor(mixed);
        }

        // RGB转反射率（简单线性转换）
        private static double[] toReflectance(Color color) {
            return new double[]{
                color.getRed() / 255.0,
                color.getGreen() / 255.0,
                color.getBlue() / 255.0
            };
        }

        // Kubelka-Munk变换：反射率 -> K/S值
        private static double kmTransform(double reflectance) {
            return (1 - reflectance) * (1 - reflectance) / (2 * reflectance);
        }

        // 反向Kubelka-Munk变换
        private static double inverseKmTransform(double km) {
            double r = 1 + km - Math.sqrt(km * km + 2 * km);
            // 限制在有效范围内
            return Math.max(0, Math.min(1, r));
        }

        // 反射率转RGB
        private static Color toColor(double[] reflectance) {
            int r = (int) Math.round(reflectance[0] * 255);
            int g = (int) Math.round(reflectance[1] * 255);
            int b = (int) Math.round(reflectance[2] * 255);
            return new Color(
                    Math.max(0, Math.min(255, r)),
                    Math.max(0, Math.min(255, g)),
                    Math.max(0, Math.min(255, b))
            );
        }

        public static void main(String[] args) {
            // 测试颜料混合
            Color cyan = new Color(0, 183, 235);   // 青色颜料
            Color magenta = new Color(213, 0, 143); // 品红颜料
            Color yellow = new Color(252, 220, 0);  // 黄色颜料

            // 青 + 品红 = 蓝色
            Color blue = mixPigments(cyan, magenta, 0.5);
            System.out.println("Cyan + Magenta = " + formatColor(blue));

            // 品红 + 黄 = 红色
            Color red = mixPigments(magenta, yellow, 0.5);
            System.out.println("Magenta + Yellow = " + formatColor(red));

            // 青 + 黄 = 绿色
            Color green = mixPigments(cyan, yellow, 0.5);
            System.out.println("Cyan + Yellow = " + formatColor(green));

            // 三原色混合 = 黑色
            Color black = mixPigments(blue, yellow, 0.5);
            System.out.println("Blue + Yellow = " + formatColor(black));
        }

        private static String formatColor(Color color) {
            return String.format("[R=%d, G=%d, B=%d]",
                    color.getRed(), color.getGreen(), color.getBlue());
        }

    }

    public static class CMYKBlend extends PixelsBlend {

        private final boolean isWeighted;

        public CMYKBlend(boolean weighted) {
            isWeighted = weighted;
            this.blendMode = isWeighted
                    ? ImagesBlendMode.CMYK_WEIGHTED : ImagesBlendMode.CMYK;
        }

        @Override
        public void makeRGB() {
            Color blended = isWeighted
                    ? mix(foreColor, backColor, weight) : mix(foreColor, backColor);
            red = blended.getRed();
            green = blended.getGreen();
            blue = blended.getBlue();
        }

        // Provided by deepseek.
        public static Color mix(Color color1, Color color2) {
            return cmyToRgb(mixColors(rgbToCmy(color1), rgbToCmy(color2)));
        }

        public static Color mix(Color color1, Color color2, double w) {
            return cmyToRgb(mixColors(rgbToCmy(color1), rgbToCmy(color2), w));
        }

        // CMY颜色类（0.0-1.0范围）
        public static class CMYColor {

            public double c; // 青
            public double m; // 品红
            public double y; // 黄

            public CMYColor(double c, double m, double y) {
                this.c = clamp(c);
                this.m = clamp(m);
                this.y = clamp(y);
            }

            private double clamp(double value) {
                return Math.max(0.0, Math.min(1.0, value));
            }
        }

        // RGB转CMY（基于减色原理）
        public static CMYColor rgbToCmy(Color rgb) {
            double r = rgb.getRed() / 255.0;
            double g = rgb.getGreen() / 255.0;
            double b = rgb.getBlue() / 255.0;

            double c = 1.0 - r;
            double m = 1.0 - g;
            double y = 1.0 - b;

            return new CMYColor(c, m, y);
        }

        // CMY转RGB
        public static Color cmyToRgb(CMYColor cmy) {
            double r = (1.0 - cmy.c) * 255;
            double g = (1.0 - cmy.m) * 255;
            double b = (1.0 - cmy.y) * 255;

            return new Color((int) Math.round(r), (int) Math.round(g), (int) Math.round(b));
        }

        // 减色混合核心算法（CMY混合）
        public static CMYColor mixColors(CMYColor color1, CMYColor color2) {
            // 减色混合公式：混合后吸收率 = 1 - (1 - c1) * (1 - c2)
            double c = 1 - (1 - color1.c) * (1 - color2.c);
            double m = 1 - (1 - color1.m) * (1 - color2.m);
            double y = 1 - (1 - color1.y) * (1 - color2.y);

            return new CMYColor(c, m, y);
        }

        public static CMYColor mixColors(CMYColor color1, CMYColor color2, double w) {
            // 减色混合公式：混合后吸收率 = 1 - (1 - c1) * (1 - c2)
            double w2 = 1 - w;
            double c = 1 - (1 - color1.c * w) * (1 - color2.c * w2);
            double m = 1 - (1 - color1.m * w) * (1 - color2.m * w2);
            double y = 1 - (1 - color1.y * w) * (1 - color2.y * w2);

            return new CMYColor(c, m, y);
        }

        // 调整颜色饱和度（添加补色）
        public static CMYColor adjustSaturation(CMYColor color, double percent) {
            // 计算补色（取反）
            double gray = (color.c + color.m + color.y) / 3.0;
            CMYColor complementary = new CMYColor(
                    gray + (gray - color.c) * percent,
                    gray + (gray - color.m) * percent,
                    gray + (gray - color.y) * percent
            );
            return mixColors(color, complementary);
        }

        // 调整明度（添加黑/白）
        public static CMYColor adjustBrightness(CMYColor color, double whiteAmount, double blackAmount) {
            // 添加白色 = 减少CMY值
            double c = color.c * (1 - whiteAmount);
            double m = color.m * (1 - whiteAmount);
            double y = color.y * (1 - whiteAmount);

            // 添加黑色 = 增加CMY值
            c = c + (1 - c) * blackAmount;
            m = m + (1 - m) * blackAmount;
            y = y + (1 - y) * blackAmount;

            return new CMYColor(c, m, y);
        }

        // 示例使用
        public static void main(String[] args) {
            // 创建基础颜色（青、品红、黄）
            Color cyanRgb = new Color(0, 255, 255);
            Color magentaRgb = new Color(255, 0, 255);
            Color yellowRgb = new Color(255, 255, 0);

            // 转换为CMY
            CMYColor cyan = rgbToCmy(cyanRgb);
            CMYColor magenta = rgbToCmy(magentaRgb);
            CMYColor yellow = rgbToCmy(yellowRgb);

            // 示例1: 青 + 黄 = 绿
            CMYColor green = mixColors(cyan, yellow);
            Color greenRgb = cmyToRgb(green);
            System.out.println("青 + 黄 = 绿: RGB("
                    + greenRgb.getRed() + ", " + greenRgb.getGreen() + ", " + greenRgb.getBlue() + ")");

            // 示例2: 品红 + 青 = 蓝
            CMYColor blue = mixColors(magenta, cyan);
            Color blueRgb = cmyToRgb(blue);
            System.out.println("品红 + 青 = 蓝: RGB("
                    + blueRgb.getRed() + ", " + blueRgb.getGreen() + ", " + blueRgb.getBlue() + ")");

            // 示例3: 调整饱和度
            CMYColor desaturated = adjustSaturation(green, -0.5); // 降低50%饱和度
            Color desaturatedRgb = cmyToRgb(desaturated);
            System.out.println("降低饱和度: RGB("
                    + desaturatedRgb.getRed() + ", " + desaturatedRgb.getGreen() + ", " + desaturatedRgb.getBlue() + ")");

            // 示例4: 调整明度
            CMYColor darkened = adjustBrightness(green, 0.0, 0.3); // 添加30%黑色
            Color darkenedRgb = cmyToRgb(darkened);
            System.out.println("变暗效果: RGB("
                    + darkenedRgb.getRed() + ", " + darkenedRgb.getGreen() + ", " + darkenedRgb.getBlue() + ")");
        }

    }

    public static class MultiplyBlend extends PixelsBlend {

        public MultiplyBlend() {
            this.blendMode = ImagesBlendMode.MULTIPLY;

        }

        @Override
        public void makeRGB() {
            red = foreColor.getRed() * backColor.getRed() / 255;
            green = foreColor.getGreen() * backColor.getGreen() / 255;
            blue = foreColor.getBlue() * backColor.getBlue() / 255;
        }
    }

    public static class ScreenBlend extends PixelsBlend {

        public ScreenBlend() {
            this.blendMode = ImagesBlendMode.SCREEN;

        }

        @Override
        public void makeRGB() {

            red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 255;
            green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 255;
            blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 255;

        }
    }

    public static class OverlayBlend extends PixelsBlend {

        public OverlayBlend() {
            this.blendMode = ImagesBlendMode.OVERLAY;

        }

        @Override
        public void makeRGB() {

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

    public static class HardLightBlend extends PixelsBlend {

        public HardLightBlend() {
            this.blendMode = ImagesBlendMode.HARD_LIGHT;

        }

        @Override
        public void makeRGB() {

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

    public static class SoftLightBlend extends PixelsBlend {

        public SoftLightBlend() {
            this.blendMode = ImagesBlendMode.SOFT_LIGHT;

        }

        @Override
        public void makeRGB() {

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

    public static class ColorDodgeBlend extends PixelsBlend {

        public ColorDodgeBlend() {
            this.blendMode = ImagesBlendMode.COLOR_DODGE;

        }

        @Override
        public void makeRGB() {

            red = foreColor.getRed() == 255 ? 255
                    : (backColor.getRed() + (foreColor.getRed() * backColor.getRed()) / (255 - foreColor.getRed()));
            green = foreColor.getGreen() == 255 ? 255
                    : (backColor.getGreen() + (foreColor.getGreen() * backColor.getGreen()) / (255 - foreColor.getGreen()));
            blue = foreColor.getBlue() == 255 ? 255
                    : (backColor.getBlue() + (foreColor.getBlue() * backColor.getBlue()) / (255 - foreColor.getBlue()));

        }
    }

    public static class LinearDodgeBlend extends PixelsBlend {

        public LinearDodgeBlend() {
            this.blendMode = ImagesBlendMode.LINEAR_DODGE;

        }

        @Override
        public void makeRGB() {

            red = foreColor.getRed() + backColor.getRed();
            green = foreColor.getGreen() + backColor.getGreen();
            blue = foreColor.getBlue() + backColor.getBlue();

        }
    }

    public static class DivideBlend extends PixelsBlend {

        public DivideBlend() {
            this.blendMode = ImagesBlendMode.DIVIDE;

        }

        @Override
        public void makeRGB() {

            red = foreColor.getRed() == 0 ? 255 : ((backColor.getRed() * 255) / foreColor.getRed());
            green = foreColor.getGreen() == 0 ? 255 : ((backColor.getGreen() * 255) / foreColor.getGreen());
            blue = foreColor.getBlue() == 0 ? 255 : ((backColor.getBlue() * 255) / foreColor.getBlue());
        }
    }

    public static class ColorBurnBlend extends PixelsBlend {

        public ColorBurnBlend() {
            this.blendMode = ImagesBlendMode.COLOR_BURN;

        }

        @Override
        public void makeRGB() {

            red = foreColor.getRed() == 0 ? 0
                    : (backColor.getRed() - (255 - foreColor.getRed()) * 255 / foreColor.getRed());
            green = foreColor.getGreen() == 0 ? 0
                    : (backColor.getGreen() - (255 - foreColor.getGreen()) * 255 / foreColor.getGreen());
            blue = foreColor.getBlue() == 0 ? 0
                    : (backColor.getBlue() - (255 - foreColor.getBlue()) * 255 / foreColor.getBlue());

        }
    }

    public static class LinearBurnBlend extends PixelsBlend {

        public LinearBurnBlend() {
            this.blendMode = ImagesBlendMode.LINEAR_BURN;

        }

        @Override
        public void makeRGB() {

            red = backColor.getRed() == 0 ? 0
                    : foreColor.getRed() + backColor.getRed() - 255;
            green = backColor.getGreen() == 0 ? 0
                    : foreColor.getGreen() + backColor.getGreen() - 255;
            blue = backColor.getBlue() == 0 ? 0
                    : foreColor.getBlue() + backColor.getBlue() - 255;

        }
    }

    public static class VividLightBlend extends PixelsBlend {

        public VividLightBlend() {
            this.blendMode = ImagesBlendMode.VIVID_LIGHT;

        }

        @Override
        public void makeRGB() {

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

    public static class LinearLightBlend extends PixelsBlend {

        public LinearLightBlend() {
            this.blendMode = ImagesBlendMode.LINEAR_LIGHT;

        }

        @Override
        public void makeRGB() {

            red = 2 * foreColor.getRed() + backColor.getRed() - 255;
            green = 2 * foreColor.getGreen() + backColor.getGreen() - 255;
            blue = 2 * foreColor.getBlue() + backColor.getBlue() - 255;

        }
    }

    public static class SubtractBlend extends PixelsBlend {

        public SubtractBlend() {
            this.blendMode = ImagesBlendMode.SUBTRACT;

        }

        @Override
        public void makeRGB() {

            red = backColor.getRed() - foreColor.getRed();
            green = backColor.getGreen() - foreColor.getGreen();
            blue = backColor.getBlue() - foreColor.getBlue();

        }
    }

    public static class DifferenceBlend extends PixelsBlend {

        public DifferenceBlend() {
            this.blendMode = ImagesBlendMode.DIFFERENCE;

        }

        @Override
        public void makeRGB() {

            red = Math.abs(backColor.getRed() - foreColor.getRed());
            green = Math.abs(backColor.getGreen() - foreColor.getGreen());
            blue = Math.abs(backColor.getBlue() - foreColor.getBlue());

        }
    }

    public static class ExclusionBlend extends PixelsBlend {

        public ExclusionBlend() {
            this.blendMode = ImagesBlendMode.EXCLUSION;

        }

        @Override
        public void makeRGB() {

            red = backColor.getRed() + foreColor.getRed() - backColor.getRed() * foreColor.getRed() / 128;
            green = backColor.getGreen() + foreColor.getGreen() - backColor.getGreen() * foreColor.getGreen() / 128;
            blue = backColor.getBlue() + foreColor.getBlue() - backColor.getBlue() * foreColor.getBlue() / 128;

        }
    }

    public static class DarkenBlend extends PixelsBlend {

        public DarkenBlend() {
            this.blendMode = ImagesBlendMode.DARKEN;

        }

        @Override
        public void makeRGB() {

            red = Math.min(backColor.getRed(), foreColor.getRed());
            green = Math.min(backColor.getGreen(), foreColor.getGreen());
            blue = Math.min(backColor.getBlue(), foreColor.getBlue());

        }
    }

    public static class LightenBlend extends PixelsBlend {

        public LightenBlend() {
            this.blendMode = ImagesBlendMode.LIGHTEN;

        }

        @Override
        public void makeRGB() {

            red = Math.max(backColor.getRed(), foreColor.getRed());
            green = Math.max(backColor.getGreen(), foreColor.getGreen());
            blue = Math.max(backColor.getBlue(), foreColor.getBlue());

        }
    }

    public static class HueBlend extends PixelsBlend {

        public HueBlend() {
            this.blendMode = ImagesBlendMode.HUE;

        }

        @Override
        public int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] hA = ColorConvertTools.pixel2hsb(forePixel);
            float[] hB = ColorConvertTools.pixel2hsb(backPixel);
            Color hColor = Color.getHSBColor(hA[0], hB[1], hB[2]);
            return hColor.getRGB();
        }
    }

    public static class SaturationBlend extends PixelsBlend {

        public SaturationBlend() {
            this.blendMode = ImagesBlendMode.SATURATION;

        }

        @Override
        public int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] sA = ColorConvertTools.pixel2hsb(forePixel);
            float[] sB = ColorConvertTools.pixel2hsb(backPixel);
            Color sColor = Color.getHSBColor(sB[0], sA[1], sB[2]);
            return sColor.getRGB();
        }
    }

    public static class LuminosityBlend extends PixelsBlend {

        public LuminosityBlend() {
            this.blendMode = ImagesBlendMode.LUMINOSITY;

        }

        @Override
        public int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] bA = ColorConvertTools.pixel2hsb(forePixel);
            float[] bB = ColorConvertTools.pixel2hsb(backPixel);
            Color newColor = Color.getHSBColor(bB[0], bB[1], bA[2]);
            return newColor.getRGB();
        }
    }

    public static class ColorBlend extends PixelsBlend {

        public ColorBlend() {
            this.blendMode = ImagesBlendMode.COLOR;

        }

        @Override
        public int blend(int forePixel, int backPixel) {
            if (forePixel == 0) {                       // Pass transparency
                return backPixel;
            }
            if (backPixel == 0) {                       // Pass transparency
                return forePixel;
            }
            float[] cA = ColorConvertTools.pixel2hsb(forePixel);
            float[] cB = ColorConvertTools.pixel2hsb(backPixel);
            Color cColor = Color.getHSBColor(cA[0], cA[1], cB[2]);
            return cColor.getRGB();
        }
    }

}
