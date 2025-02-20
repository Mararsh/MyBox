package mara.mybox.color;

import java.awt.Color;
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

    private double threshold, realThreshold;
    protected double brightnessWeight, saturationWeight, hueWeight;
    private boolean accurateMatch;
    protected MatchAlgorithm algorithm;
    public final static MatchAlgorithm DefaultAlgorithm = MatchAlgorithm.RGBRoughWeightedEuclidean;

    public static enum MatchAlgorithm {
        CIEDE2000, CIE94, CIE76,
        HSBEuclidean, Hue, Saturation, Brightness,
        RGBEuclidean, RGBRoughWeightedEuclidean, RGBWeightedEuclidean, RGBManhattan,
        Red, Green, Blue,
        CMC
    }

    public ColorMatch() {
        init();
    }

    public final void init() {
        algorithm = DefaultAlgorithm;
        setThreshold(suggestedThreshold(algorithm));
        brightnessWeight = 1d;
        saturationWeight = 1d;
        hueWeight = 1d;
    }

    public ColorMatch copyTo(ColorMatch match) {
        if (match == null) {
            return match;
        }
        match.setAlgorithm(algorithm);
        match.setThreshold(threshold);
        match.setHueWeight(hueWeight);
        match.setBrightnessWeight(brightnessWeight);
        match.setSaturationWeight(saturationWeight);
        return match;
    }

    public String info() {
        return "Algorithm :" + algorithm + "\n"
                + "threshold :" + threshold + "\n"
                + "hueWeight :" + hueWeight + "\n"
                + "brightnessWeight :" + brightnessWeight + "\n"
                + "saturationWeight :" + saturationWeight;
    }

    /*
        parameters
     */
    public final ColorMatch setThreshold(double value) {
        threshold = value;
        accurateMatch = threshold < 1e-10;
        switch (algorithm) {
            case RGBRoughWeightedEuclidean:
            case RGBWeightedEuclidean:
            case RGBEuclidean:
            case HSBEuclidean:
            case CIEDE2000:
            case CIE94:
            case CIE76:
            case CMC:
                realThreshold = accurateMatch ? 1e-10 : threshold * threshold;
                break;
            case Red:
            case Green:
            case Blue:
            case Hue:
            case Saturation:
            case Brightness:
            case RGBManhattan:
                realThreshold = threshold;
                break;
        }
        return this;
    }

    public static double suggestedThreshold(MatchAlgorithm a) {
        switch (a) {
            case RGBRoughWeightedEuclidean:
                return 20d;
            case RGBWeightedEuclidean:
                return 20d;
            case RGBEuclidean:
                return 20d;
            case CIEDE2000:
                return 5d;
            case CIE94:
                return 5d;
            case CIE76:
                return 5d;
            case CMC:
                return 5d;
            case HSBEuclidean:
                return 20d;
            case Red:
            case Green:
            case Blue:
                return 20d;
            case Hue:
                return 20d;
            case Saturation:
                return 20d;
            case Brightness:
                return 20d;
            case RGBManhattan:
                return 20d;
        }
        return 20d;
    }

    public static boolean supportWeights(MatchAlgorithm a) {
        switch (a) {
            case RGBRoughWeightedEuclidean:
            case RGBWeightedEuclidean:
            case RGBEuclidean:
            case Red:
            case Green:
            case Blue:
            case Hue:
            case Saturation:
            case Brightness:
            case RGBManhattan:
            case CIE76:
                return false;
            case CIEDE2000:
            case CIE94:
            case HSBEuclidean:
            case CMC:
                return true;
        }
        return false;
    }

    public static MatchAlgorithm algorithm(String as) {
        try {
            for (MatchAlgorithm a : MatchAlgorithm.values()) {
                if (Languages.matchIgnoreCase(a.name(), as)) {
                    return a;
                }
            }
        } catch (Exception e) {
        }
        return DefaultAlgorithm;
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

    /*
        match
     */
    public boolean isMatch(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return false;
        }
        if (color1.getRGB() == color2.getRGB()) {
            return true;
        } else if (accurateMatch || color1.getRGB() == 0 || color2.getRGB() == 0) {
            return false;
        }
        return distance(color1, color2) <= realThreshold;
    }

    public boolean isMatchColors(List<Color> colors, Color color, boolean excluded) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (excluded) {
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
                return ciede2000Distance(color1, color2);
            case CIE94:
                return cie94Distance(color1, color2);
            case CIE76:
                return cie76Distance(color1, color2);
            case CMC:
                return cmcDistance(color1, color2);
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

    public static double rgbEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        int redDistance = color1.getRed() - color2.getRed();
        int greenDistance = color1.getGreen() - color2.getGreen();
        int blueDistance = color1.getBlue() - color2.getBlue();
        return redDistance * redDistance
                + greenDistance * greenDistance
                + blueDistance * blueDistance;
    }

    // https://www.compuphase.com/cmetric.htm
    public static double rgbWeightEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
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
    public static double rgbRoughWeightedEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        int redDistance = color1.getRed() - color2.getRed();
        int greenDistance = color1.getGreen() - color2.getGreen();
        int blueDistance = color1.getBlue() - color2.getBlue();
        return 2 * redDistance * redDistance
                + 4 * greenDistance * greenDistance
                + 3 * blueDistance * blueDistance;
    }

    public static double manhattanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(color1.getRed() - color2.getRed())
                + Math.abs(color1.getGreen() - color2.getGreen())
                + Math.abs(color1.getBlue() - color2.getBlue());
    }

    public static double redDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(color1.getRed() - color2.getRed());
    }

    public static double greenDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(color1.getGreen() - color2.getGreen());
    }

    public static double blueDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(color1.getBlue() - color2.getBlue());
    }

    public double hsbEuclideanDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        float[] hsb1 = ColorConvertTools.color2hsb(color1);
        float[] hsb2 = ColorConvertTools.color2hsb(color2);
        double hueDistance = Math.abs(hsb1[0] * 360 - hsb2[0] * 360);
        hueDistance = Math.min(hueDistance, 360 - hueDistance) / 1.8;
        double saturationDistance = Math.ceil(Math.abs(hsb1[1] - hsb2[1]) * 100);
        double brightnessDistance = Math.ceil(Math.abs(hsb1[2] - hsb2[2]) * 100);
        return hueWeight * hueDistance * hueDistance
                + saturationWeight * saturationDistance * saturationDistance
                + brightnessWeight * brightnessDistance * brightnessDistance;
    }

    public static double hueDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(ColorConvertTools.getHue(color1) - ColorConvertTools.getHue(color2)) * 360;
    }

    public static double saturationDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(ColorConvertTools.getSaturation(color1) - ColorConvertTools.getSaturation(color2)) * 100;
    }

    public static double brightnessDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(ColorConvertTools.getBrightness(color1) - ColorConvertTools.getBrightness(color2)) * 100;
    }

    public static double cie76Distance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
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
            return Double.MAX_VALUE;
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

    public double ciede2000Distance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
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

    public double cmcDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Double.MAX_VALUE;
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
        // 计算 C1 和 C2
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);

        // 计算 ΔL, ΔC, ΔH
        double deltaL = L2 - L1;
        double deltaC = C2 - C1;
        double deltaH = Math.sqrt(Math.pow(a2 - a1, 2) + Math.pow(b2 - b1, 2) - Math.pow(deltaC, 2));

        // 计算 h1（色相）
        double h1 = Math.toDegrees(Math.atan2(b1, a1));
        if (h1 < 0) {
            h1 += 360; // 确保色相在 [0, 360] 范围内
        }
        // 计算 S_L, S_C
        double S_L = L1 < 16 ? 0.511 : (0.040975 * L1) / (1 + 0.01765 * L1);
        double S_C = (0.0638 * C1) / (1 + 0.0131 * C1) + 0.638;

        // 计算 T
        double F = Math.sqrt(Math.pow(C1, 4) / (Math.pow(C1, 4) + 1900));
        double T = F == 0 ? 1 : (0.56 + Math.abs(0.2 * Math.cos(Math.toRadians(h1 + 168))));

        // 计算 ΔE*cmc
        double termL = deltaL / (brightnessWeight * S_L);
        double termC = deltaC / (saturationWeight * S_C);
        double termH = deltaH / (S_C * T);

        // Avoid "sqrt"
        return termL * termL + termC * termC + termH * termH;
    }

    /*
        get/set
     */
    public double getThreshold() {
        return threshold;
    }

    public double getRealThreshold() {
        return realThreshold;
    }

    public MatchAlgorithm getAlgorithm() {
        if (algorithm == null) {
            algorithm = DefaultAlgorithm;
        }
        return algorithm;
    }

    public ColorMatch setAlgorithm(MatchAlgorithm algorithm) {
        if (algorithm != null) {
            this.algorithm = algorithm;
        }
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

}
