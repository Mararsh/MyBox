package mara.mybox.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.color.ChromaticAdaptation.ChromaticAdaptationAlgorithm;
import mara.mybox.color.RGBColorSpace.ColorSpaceType;
import static mara.mybox.color.RGBColorSpace.primariesTristimulus;
import static mara.mybox.color.RGBColorSpace.whitePointMatrix;
import mara.mybox.data.StringTable;
import mara.mybox.tools.MatrixDoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:09:26
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
// http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
// http://brucelindbloom.com/index.html?WorkingSpaceInfo.html
// http://brucelindbloom.com/index.html?ColorCalculator.html
public class RGB2RGBConversionMatrix {

    public String source, sourceWhite, target, targetWhite;
    public String algorithm, source2target;

    public RGB2RGBConversionMatrix() {

    }

    /*
        Generation of Conversion matrices
     */
    public static List<RGB2RGBConversionMatrix> all(int scale) {
        List<RGB2RGBConversionMatrix> data = new ArrayList<>();
        for (ColorSpaceType source : ColorSpaceType.values()) {
            Illuminant.IlluminantType sourceWhite = RGBColorSpace.illuminantType(source);
            for (ColorSpaceType target : ColorSpaceType.values()) {
                if (source == target) {
                    continue;
                }
                for (ChromaticAdaptationAlgorithm algorithm : ChromaticAdaptationAlgorithm.values()) {
                    double[][] source2target = rgb2rgb(source, target, algorithm);
                    RGB2RGBConversionMatrix c = new RGB2RGBConversionMatrix();
                    c.setSource(RGBColorSpace.name(source));
                    c.setSourceWhite(sourceWhite + "");
                    c.setTarget(RGBColorSpace.name(target));
                    c.setTargetWhite(RGBColorSpace.illuminantType(target) + "");
                    c.setAlgorithm(algorithm + "");
                    c.setSource2target(MatrixDoubleTools.print(source2target, 0, scale));
                    data.add(c);
                }
            }

        }
        return data;
    }

    public static StringTable allTable(int scale) {
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(Languages.message("SourceColorSpace"),
                Languages.message("SourceReferenceWhite"), Languages.message("TargetColorSpace"), Languages.message("TargetReferenceWhite"),
                "Bradford", "XYZ", "Von Kries"
        ));
        StringTable table = new StringTable(names, Languages.message("LinearRGB2RGBMatrix"));
        for (ColorSpaceType source : ColorSpaceType.values()) {
            Illuminant.IlluminantType sourceWhite = RGBColorSpace.illuminantType(source);
            for (ColorSpaceType target : ColorSpaceType.values()) {
                if (source == target) {
                    continue;
                }
                double[][] m1 = rgb2rgb(source, target, ChromaticAdaptationAlgorithm.Bradford);
                double[][] m2 = rgb2rgb(source, target, ChromaticAdaptationAlgorithm.XYZScaling);
                double[][] m3 = rgb2rgb(source, target, ChromaticAdaptationAlgorithm.VonKries);
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        RGBColorSpace.name(source), sourceWhite.name(), RGBColorSpace.name(target),
                        RGBColorSpace.illuminantType(target).name(),
                        MatrixDoubleTools.html(m1, scale),
                        MatrixDoubleTools.html(m2, scale),
                        MatrixDoubleTools.html(m3, scale)
                ));
                table.add(row);
            }
        }
        return table;
    }

    public static String allTexts(int scale) {
        StringBuilder s = new StringBuilder();
        for (ColorSpaceType source : ColorSpaceType.values()) {
            Illuminant.IlluminantType sourceWhite = RGBColorSpace.illuminantType(source);
            for (ColorSpaceType target : ColorSpaceType.values()) {
                if (source == target) {
                    continue;
                }
                for (ChromaticAdaptationAlgorithm algorithm : ChromaticAdaptationAlgorithm.values()) {
                    double[][] source2target = rgb2rgb(source, target, algorithm);
                    s.append(Languages.message("SourceColorSpace")).append(": ").
                            append(RGBColorSpace.name(source)).append("\n");
                    s.append(Languages.message("SourceReferenceWhite")).append(": ").
                            append(sourceWhite).append("\n");
                    s.append(Languages.message("TargetColorSpace")).append(": ").
                            append(RGBColorSpace.name(target)).append("\n");
                    s.append(Languages.message("TargetReferenceWhite")).append(": ").
                            append(RGBColorSpace.illuminantType(target)).append("\n");
                    s.append(Languages.message("AdaptationAlgorithm")).append(": ").
                            append(algorithm).append("\n");
                    s.append(Languages.message("LinearRGB2RGBMatrix")).append(": \n");
                    s.append(MatrixDoubleTools.print(source2target, 20, scale)).append("\n");
                }
            }
        }
        return s.toString();
    }

    public static double[][] rgb2rgb(ColorSpaceType source, ColorSpaceType target) {
        return rgb2rgb(source, target, ChromaticAdaptationAlgorithm.Bradford);
    }

    public static double[][] rgb2rgb(ColorSpaceType source, ColorSpaceType target,
            ChromaticAdaptationAlgorithm algorithm) {
        return rgb2rgb(primariesTristimulus(source), whitePointMatrix(source),
                primariesTristimulus(target), whitePointMatrix(target),
                algorithm, -1);
    }

    public static double[][] rgb2rgb(
            double[][] sourcePrimaries, double[][] sourceWhitePoint,
            double[][] targetPrimaries, double[][] targetWhitePoint,
            ChromaticAdaptation.ChromaticAdaptationAlgorithm algorithm, int scale) {
        return (double[][]) rgb2rgb(sourcePrimaries, sourceWhitePoint, targetPrimaries, targetWhitePoint,
                algorithm, scale, false);
    }

    public static Object rgb2rgb(
            double[][] sourcePrimaries, double[][] sourceWhitePoint,
            double[][] targetPrimaries, double[][] targetWhitePoint,
            ChromaticAdaptation.ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        try {
            Map<String, Object> map;

            Object rgb2xyzObject = RGB2XYZConversionMatrix.rgb2xyz(sourcePrimaries, sourceWhitePoint,
                    targetWhitePoint, algorithm, scale, isDemo);
            double[][] rgb2xyzMatrix;
            String rgb2xyzString = null;
            if (isDemo) {
                map = (Map<String, Object>) rgb2xyzObject;
                rgb2xyzMatrix = (double[][]) map.get("conversionMatrix");
                rgb2xyzString = (String) map.get("procedure");
            } else {
                rgb2xyzMatrix = (double[][]) rgb2xyzObject;
            }

            Object xyz2rgbObject = RGB2XYZConversionMatrix.xyz2rgb(targetPrimaries, targetWhitePoint, targetWhitePoint,
                    algorithm, scale, isDemo);
            double[][] xyz2rgbMatrix;
            String xyz2rgbString = null;
            if (isDemo) {
                map = (Map<String, Object>) xyz2rgbObject;
                xyz2rgbMatrix = (double[][]) map.get("conversionMatrix");
                xyz2rgbString = (String) map.get("procedure");
            } else {
                xyz2rgbMatrix = (double[][]) xyz2rgbObject;
            }

            double[][] conversionMatrix = MatrixDoubleTools.multiply(xyz2rgbMatrix, rgb2xyzMatrix);
            Object ret;
            if (isDemo) {
                String s = "ccccccccccccc " + Languages.message("Step") + " - Source Linear RGB -> XYZ  ccccccccccccc\n";
                s += rgb2xyzString + "\n";
                s += "\nccccccccccccc " + Languages.message("Step") + " - XYZ -> target Linear RGB  ccccccccccccc\n";
                s += xyz2rgbString + "\n";
                s += "\nccccccccccccc " + Languages.message("Step") + " -  Source Linear RGB -> target Linear RGB  ccccccccccccc\n";
                s += "\nRGB_to_RGB_Matrix = XYZ_to_RGB_Matrix * RGB_to_XYZ_Matrix =\n";
                s += MatrixDoubleTools.print(conversionMatrix, 20, scale);
                map = new HashMap<>();
                map.put("procedure", s);
                map.put("conversionMatrix", conversionMatrix);
                ret = map;
            } else {
                ret = conversionMatrix;
            }
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceWhite() {
        return sourceWhite;
    }

    public void setSourceWhite(String sourceWhite) {
        this.sourceWhite = sourceWhite;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetWhite() {
        return targetWhite;
    }

    public void setTargetWhite(String targetWhite) {
        this.targetWhite = targetWhite;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSource2target() {
        return source2target;
    }

    public void setSource2target(String source2target) {
        this.source2target = source2target;
    }

}
