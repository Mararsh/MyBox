package mara.mybox.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import static mara.mybox.color.SRGB.SRGBtoCIELab;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2025-2-7
 * @License Apache License Version 2.0
 */
public class ColorMatch {

    // The "distance" and "threshold" may be squared values
    protected double threshold, trueThreshold,
            brightnessWeight, saturationWeight, hueWeight;
    protected MatchAlgorithm algorithm;
    protected boolean exlcuded;
    protected List<Color> colors;
    public final static MatchAlgorithm DefaultAlgorithm = MatchAlgorithm.RGBRoughWeightedEuclidean;

    public static enum MatchAlgorithm {
        CIEDE2000, CIE94, CIE76,
        HSBEuclidean, Hue, Saturation, Brightness,
        RGBEuclidean, RGBRoughWeightedEuclidean, RGBWeightedEuclidean, RGBManhattan,
        Red, Green, Blue
    }

    public ColorMatch() {
        init();
    }

    public final void init() {
        threshold = 0d;
        brightnessWeight = 1d;
        saturationWeight = 1d;
        hueWeight = 1d;
        exlcuded = false;
        algorithm = DefaultAlgorithm;
        trueThreshold = 0d;
        clearColors();
    }

    public ColorMatch copyTo(ColorMatch match) {
        if (match == null) {
            return match;
        }
        match.setAlgorithm(algorithm);
        match.setThreshold(threshold);
        match.setExlcuded(exlcuded);
        match.setColors(colors);
        match.setHueWeight(hueWeight);
        match.setBrightnessWeight(brightnessWeight);
        match.setSaturationWeight(saturationWeight);
        return match;
    }

    public String info() {
        return "Algorithm :" + algorithm + "\n"
                + "threshold :" + threshold + "\n"
                + "exlcuded :" + exlcuded + "\n"
                + "colors :" + (colors != null ? colors.size() : 0) + "\n"
                + "hueWeight :" + hueWeight + "\n"
                + "brightnessWeight :" + brightnessWeight + "\n"
                + "saturationWeight :" + saturationWeight;
    }

    /*
        parameters
     */
    public ColorMatch setThreshold(double value) {
        threshold = value;
        switch (algorithm) {
            case RGBRoughWeightedEuclidean:
            case RGBWeightedEuclidean:
            case RGBEuclidean:
            case CIEDE2000:
            case CIE94:
            case CIE76:
            case HSBEuclidean:
                trueThreshold = threshold * threshold;
                break;
            case Red:
            case Green:
            case Blue:
            case Hue:
            case Saturation:
            case Brightness:
            case RGBManhattan:
                trueThreshold = threshold;
                break;
        }
        return this;
    }

    public boolean setColorWeights(String weights) {
        try {
            String[] values = weights.split(":");
            hueWeight = Double.parseDouble(values[0]);
            saturationWeight = Double.parseDouble(values[1]);
            brightnessWeight = Double.parseDouble(values[2]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getColorWeights() {
        return hueWeight + ":" + saturationWeight + ":" + brightnessWeight;
    }

    public boolean addColor(Color color) {
        if (color == null) {
            return false;
        }
        if (colors == null) {
            colors = new ArrayList<>();
        }
        if (!colors.contains(color)) {
            colors.add(color);
            return true;
        } else {
            return false;
        }
    }

    public void clearColors() {
        colors = null;
    }

    public static MatchAlgorithm algorithm(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            for (MatchAlgorithm a : MatchAlgorithm.values()) {
                if (Languages.matchIgnoreCase(a.name(), name)) {
                    return a;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /*
        match
     */
    public boolean isMatch(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return false;
        }
        if (color1.getRGB() == color2.getRGB()) {
            return true;
        } else if (threshold == 0 || color1.getRGB() == 0 || color2.getRGB() == 0) {
            return false;
        }
        return distance(color1, color2) <= trueThreshold;
    }

    public boolean isMatch(Color color) {
        return isMatch(colors, color);
    }

    public boolean isMatch(List<Color> colors, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (exlcuded) {
            for (Color oColor : colors) {
                if (isMatch(color, oColor)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (isMatch(color, oColor)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
        distance
     */
    public double distance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        switch (algorithm) {
            case RGBRoughWeightedEuclidean:
                return rgbRoughWeightedEuclideanDistance(color1, color2);
            case RGBWeightedEuclidean:
                return rgbWeightEuclideanDistance(color1, color2);
            case RGBEuclidean:
                return rgbEuclideanDistance(color1, color2);
            case Red:
                return redDistance(color1, color2);
            case Green:
                return greenDistance(color1, color2);
            case Blue:
                return blueDistance(color1, color2);
            case RGBManhattan:
                return manhattanDistance(color1, color2);
            case CIEDE2000:
                return CIEDE2000Distance(color1, color2);
            case CIE94:
                return cie94Distance(color1, color2);
            case CIE76:
                return cie76Distance(color1, color2);
            case HSBEuclidean:
                return hsbEuclideanDistance(color1, color2);
            case Hue:
                return hueDistance(color1, color2);
            case Saturation:
                return saturationDistance(color1, color2);
            case Brightness:
                return brightnessDistance(color1, color2);
        }
        return Integer.MAX_VALUE;
    }

    public int rgbEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int redDistance = color1.getRed() - color2.getRed();
        int greenDistance = color1.getGreen() - color2.getGreen();
        int blueDistance = color1.getBlue() - color2.getBlue();
        return redDistance * redDistance
                + greenDistance * greenDistance
                + blueDistance * blueDistance;
    }

    // https://www.compuphase.com/cmetric.htm
    public static int rgbWeightEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int redDistance = color1.getRed() - color2.getRed();
        int greenDistance = color1.getGreen() - color2.getGreen();
        int blueDistance = color1.getBlue() - color2.getBlue();
        int redAvg = (color1.getRed() + color2.getRed()) / 2;
        return Math.round(((512 + redAvg) * redDistance * redDistance) >> 8
                + 4 * greenDistance * greenDistance
                + ((767 - redAvg) * blueDistance * blueDistance) >> 8);
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public int rgbRoughWeightedEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int redDistance = color1.getRed() - color2.getRed();
        int greenDistance = color1.getGreen() - color2.getGreen();
        int blueDistance = color1.getBlue() - color2.getBlue();
        return 2 * redDistance * redDistance
                + 4 * greenDistance * greenDistance
                + 3 * blueDistance * blueDistance;
    }

    public int manhattanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(color1.getRed() - color2.getRed())
                + Math.abs(color1.getGreen() - color2.getGreen())
                + Math.abs(color1.getBlue() - color2.getBlue());
    }

    public int redDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(color1.getRed() - color2.getRed());
    }

    public int greenDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(color1.getGreen() - color2.getGreen());
    }

    public int blueDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(color1.getBlue() - color2.getBlue());
    }

    public double hsbEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        javafx.scene.paint.Color fxColor1 = ColorConvertTools.converColor(color1);
        javafx.scene.paint.Color fxColor2 = ColorConvertTools.converColor(color2);
        double hueDistance = Math.abs(fxColor1.getHue() - fxColor2.getHue());
        hueDistance = Math.min(hueDistance, 360 - hueDistance) / 1.8;
        double saturationDistance = Math.abs(fxColor1.getSaturation() - fxColor2.getSaturation()) * 100;
        double brightnessDistance = Math.abs(fxColor1.getBrightness() - fxColor2.getBrightness()) * 100;
        return hueDistance * hueDistance
                + saturationDistance * saturationDistance
                + brightnessDistance * brightnessDistance;
    }

    public double hueDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(ColorConvertTools.getHue(color1) - ColorConvertTools.getHue(color2));
    }

    public double saturationDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(ColorConvertTools.getSaturation(color1) - ColorConvertTools.getSaturation(color2)) * 100;
    }

    public double brightnessDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(ColorConvertTools.getBrightness(color1) - ColorConvertTools.getBrightness(color2)) * 100;
    }

    public double cie76Distance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        double[] lab1 = SRGBtoCIELab(color1);
        double[] lab2 = SRGBtoCIELab(color2);

        double lDistance = lab1[0] - lab2[0];
        double aDistance = lab1[1] - lab2[1];
        double bDistance = lab1[2] - lab2[2];
        return lDistance * lDistance + aDistance * aDistance + bDistance * bDistance;
    }

    public double cie94Distance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        double[] lab1 = SRGBtoCIELab(color1);
        double[] lab2 = SRGBtoCIELab(color2);

        double L1 = lab1[0];
        double a1 = lab1[1];
        double b1 = lab1[2];
        double L2 = lab2[0];
        double a2 = lab2[1];
        double b2 = lab2[2];

        // Following lines are generated by DeepSeek
        // 计算亮度差
        double deltaL = L1 - L2;

        // 计算色度C
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double deltaC = C1 - C2;

        // 计算色调差ΔH
        double deltaH = Math.sqrt(Math.pow(a1 - a2, 2) + Math.pow(b1 - b2, 2) - Math.pow(deltaC, 2));

        // 计算权重因子
        double SL = 1.0; // 亮度权重因子
        double SC = 1.0 + 0.045 * (C1 + C2) / 2.0; // 色度权重因子
        double SH = 1.0 + 0.015 * (C1 + C2) / 2.0; // 色调权重因子

        // 计算CIE94色差
        double term1 = deltaL / (brightnessWeight * SL);
        double term2 = deltaC / (saturationWeight * SC);
        double term3 = deltaH / (hueWeight * SH);

        // Avoid "sqrt"
        return term1 * term1 + term2 * term2 + term3 * term3;
    }

    public double CIEDE2000Distance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        double[] lab1 = SRGBtoCIELab(color1);
        double[] lab2 = SRGBtoCIELab(color2);

        double L1 = lab1[0];
        double a1 = lab1[1];
        double b1 = lab1[2];
        double L2 = lab2[0];
        double a2 = lab2[1];
        double b2 = lab2[2];

        // Following lines are generated by DeepSeek
        // 步骤1: 计算色度C和色调角h
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double C_avg = (C1 + C2) / 2.0;

        // 步骤2: 计算G因子（色度非线性补偿）
        double G = 0.5 * (1 - Math.sqrt(Math.pow(C_avg, 7) / (Math.pow(C_avg, 7) + Math.pow(25, 7))));
        double a1_prime = a1 * (1 + G);
        double a2_prime = a2 * (1 + G);

        // 更新色度C'
        C1 = Math.sqrt(a1_prime * a1_prime + b1 * b1);
        C2 = Math.sqrt(a2_prime * a2_prime + b2 * b2);

        // 步骤3: 计算色调角差Δh'
        double h1 = Math.toDegrees(Math.atan2(b1, a1_prime));
        if (h1 < 0) {
            h1 += 360;
        }
        double h2 = Math.toDegrees(Math.atan2(b2, a2_prime));
        if (h2 < 0) {
            h2 += 360;
        }

        double delta_h_prime;
        if (Math.abs(h2 - h1) <= 180) {
            delta_h_prime = h2 - h1;
        } else {
            delta_h_prime = (h2 - h1 > 180) ? (h2 - h1 - 360) : (h2 - h1 + 360);
        }

        // 步骤4: 计算色调平均H'
        double H_prime_avg = (Math.abs(h1 - h2) > 180) ? ((h1 + h2 + 360) / 2.0) : ((h1 + h2) / 2.0);

        // 步骤5: 补偿因子计算
        double T = 1 - 0.17 * Math.cos(Math.toRadians(H_prime_avg - 30))
                + 0.24 * Math.cos(Math.toRadians(2 * H_prime_avg))
                + 0.32 * Math.cos(Math.toRadians(3 * H_prime_avg + 6))
                - 0.20 * Math.cos(Math.toRadians(4 * H_prime_avg - 63));

        double delta_L_prime = L2 - L1;
        double delta_C_prime = C2 - C1;
        double delta_H_prime = 2 * Math.sqrt(C1 * C2) * Math.sin(Math.toRadians(delta_h_prime / 2.0));

        // 步骤6: 权重因子
        double SL = 1 + (0.015 * Math.pow((L1 + L2) / 2.0 - 50, 2)) / Math.sqrt(20 + Math.pow((L1 + L2) / 2.0 - 50, 2));
        double SC = 1 + 0.045 * ((C1 + C2) / 2.0);
        double SH = 1 + 0.015 * ((C1 + C2) / 2.0) * T;

        // 步骤7: 旋转因子RT
        double delta_theta = 30 * Math.exp(-Math.pow((H_prime_avg - 275) / 25, 2));
        double RT = -2 * Math.sqrt(Math.pow(C_avg, 7) / (Math.pow(C_avg, 7) + Math.pow(25, 7))) * Math.sin(Math.toRadians(2 * delta_theta));

        // 最终计算ΔE00
        double term1 = delta_L_prime / (brightnessWeight * SL);
        double term2 = delta_C_prime / (saturationWeight * SC);
        double term3 = delta_H_prime / (hueWeight * SH);

        // Avoid "sqrt"
        return term1 * term1 + term2 * term2 + term3 * term3 + RT * term2 * term3;
    }

    /*
        get/set
     */
    public List<Color> getColors() {
        return colors;
    }

    public ColorMatch setColors(List<Color> colors) {
        this.colors = colors;
        return this;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getTrueThreshold() {
        return trueThreshold;
    }

    public MatchAlgorithm getAlgorithm() {
        if (algorithm == null) {
            algorithm = DefaultAlgorithm;
        }
        return algorithm;
    }

    public ColorMatch setAlgorithm(MatchAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public double getBrightnessWeight() {
        return brightnessWeight;
    }

    public ColorMatch setBrightnessWeight(double brightnessWeight) {
        this.brightnessWeight = brightnessWeight;
        return this;
    }

    public double getSaturationWeight() {
        return saturationWeight;
    }

    public ColorMatch setSaturationWeight(double saturationWeight) {
        this.saturationWeight = saturationWeight;
        return this;
    }

    public double getHueWeight() {
        return hueWeight;
    }

    public ColorMatch setHueWeight(double hueWeight) {
        this.hueWeight = hueWeight;
        return this;
    }

    public boolean isExlcuded() {
        return exlcuded;
    }

    public ColorMatch setExlcuded(boolean exlcuded) {
        this.exlcuded = exlcuded;
        return this;
    }

}
