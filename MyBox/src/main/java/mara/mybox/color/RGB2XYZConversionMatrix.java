package mara.mybox.color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.color.ChromaticAdaptation.ChromaticAdaptationAlgorithm;
import static mara.mybox.color.ChromaticAdaptation.matrix;
import mara.mybox.color.Illuminant.IlluminantType;
import mara.mybox.color.Illuminant.Observer;
import mara.mybox.color.RGBColorSpace.ColorSpaceType;
import static mara.mybox.color.RGBColorSpace.primariesTristimulus;
import static mara.mybox.color.RGBColorSpace.whitePointMatrix;
import mara.mybox.tools.MatrixTools;
import static mara.mybox.value.AppVaribles.message;

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
public class RGB2XYZConversionMatrix {

    public String rgb, rgbWhite, xyzWhite, algorithm;
    public String rgb2xyz, xyz2rgb;

    public RGB2XYZConversionMatrix() {

    }

    /*
        Generation of Conversion matrices
     */
    public static List<RGB2XYZConversionMatrix> all(int scale) {
        List<RGB2XYZConversionMatrix> data = new ArrayList();
        for (ColorSpaceType colorSpace : ColorSpaceType.values()) {
            IlluminantType csWhite = RGBColorSpace.illuminantType(colorSpace);
            double[][] rgb2xyz = rgb2xyz(colorSpace);
            for (IlluminantType targetIlluminant : IlluminantType.values()) {
                for (Observer sourceObserver : Observer.values()) {
                    for (Observer targetObserver : Observer.values()) {
                        if (csWhite != targetIlluminant
                                || sourceObserver == targetObserver) {
                            for (ChromaticAdaptationAlgorithm algorithm : ChromaticAdaptationAlgorithm.values()) {
                                double[][] adpatM = matrix(csWhite, Observer.CIE1931,
                                        targetIlluminant, targetObserver, algorithm, -1);
                                double[][] adpatedC = MatrixTools.multiply(adpatM, rgb2xyz);
                                RGB2XYZConversionMatrix c = new RGB2XYZConversionMatrix();
                                c.setRgb(RGBColorSpace.name(colorSpace));
                                c.setRgbWhite(csWhite + " - " + Observer.CIE1931);
                                c.setXyzWhite(targetIlluminant + " - " + targetObserver);
                                c.setAlgorithm(algorithm + "");
                                c.setRgb2xyz(MatrixTools.print(adpatedC, 0, scale));
                                double[][] xyz2rgb = MatrixTools.inverse(adpatedC);
                                c.setXyz2rgb(MatrixTools.print(xyz2rgb, 0, scale));
                                data.add(c);
                            }
                        } else {
                            RGB2XYZConversionMatrix c = new RGB2XYZConversionMatrix();
                            c.setRgb(RGBColorSpace.name(colorSpace));
                            c.setRgbWhite(csWhite + " - " + Observer.CIE1931);
                            c.setXyzWhite(targetIlluminant + " - " + targetObserver);
                            c.setAlgorithm("");
                            c.setRgb2xyz(MatrixTools.print(rgb2xyz, 0, scale));
                            double[][] xyz2rgb = MatrixTools.inverse(rgb2xyz);
                            c.setXyz2rgb(MatrixTools.print(xyz2rgb, 0, scale));
                            data.add(c);
                        }
                    }
                }
            }

        }
        return data;
    }

    public static String allTexts(int scale) {
        StringBuilder s = new StringBuilder();
        for (ColorSpaceType colorSpace : ColorSpaceType.values()) {
            IlluminantType csWhite = RGBColorSpace.illuminantType(colorSpace);
            double[][] rgb2xyz = rgb2xyz(colorSpace);
            for (IlluminantType targetIlluminant : IlluminantType.values()) {
                for (Observer sourceObserver : Observer.values()) {
                    for (Observer targetObserver : Observer.values()) {
                        if (csWhite != targetIlluminant
                                || sourceObserver == targetObserver) {
                            for (ChromaticAdaptationAlgorithm algorithm : ChromaticAdaptationAlgorithm.values()) {
                                double[][] adpatM = matrix(csWhite, Observer.CIE1931,
                                        targetIlluminant, targetObserver, algorithm, -1);
                                double[][] adpatedC = MatrixTools.multiply(adpatM, rgb2xyz);
                                double[][] xyz2rgb = MatrixTools.inverse(adpatedC);
                                s.append(message("RGBColorSpace")).append(": ").
                                        append(RGBColorSpace.name(colorSpace)).append("\n");
                                s.append(message("RGBReferenceWhite")).append(": ").
                                        append(csWhite).append(" - ").append(Observer.CIE1931).append("\n");
                                s.append(message("XYZReferenceWhite")).append(": ").
                                        append(targetIlluminant).append(" - ").append(targetObserver).append("\n");
                                s.append(message("AdaptationAlgorithm")).append(": ").
                                        append(algorithm).append("\n");
                                s.append("Linear RGB -> XYZ: ").append("\n");
                                s.append(MatrixTools.print(adpatedC, 20, scale));
                                s.append("XYZ -> Linear RGB: ").append("\n");
                                s.append(MatrixTools.print(xyz2rgb, 20, scale)).append("\n");
                            }
                        } else {
                            double[][] xyz2rgb = MatrixTools.inverse(rgb2xyz);
                            s.append(message("RGBColorSpace")).append(": ").
                                    append(RGBColorSpace.name(colorSpace)).append("\n");
                            s.append(message("RGBReferenceWhite")).append(": ").
                                    append(csWhite).append(" - ").append(Observer.CIE1931).append("\n");
                            s.append(message("XYZReferenceWhite")).append(": ").
                                    append(targetIlluminant).append(" - ").append(targetObserver).append("\n");
                            s.append("Linear RGB -> XYZ: ").append("\n");
                            s.append(MatrixTools.print(rgb2xyz, 20, scale));
                            s.append("XYZ -> Linear RGB: ").append("\n");
                            s.append(MatrixTools.print(xyz2rgb, 20, scale)).append("\n");
                        }
                    }
                }
            }
        }
        return s.toString();
    }

    public static double[][] rgb2xyz(ColorSpaceType colorSpace) {
        return rgb2xyz(primariesTristimulus(colorSpace), whitePointMatrix(colorSpace));
    }

    public static double[][] rgb2xyz(double[][] primaries, double[][] sourceWhitePoint) {
        return (double[][]) rgb2xyz(primaries, sourceWhitePoint, null, null, -1, false);
    }

    public static double[][] xyz2rgb(ColorSpaceType colorSpace) {
        return MatrixTools.inverse(rgb2xyz(colorSpace));
    }

    public static double[][] xyz2rgb(double[][] primaries, double[][] sourceWhitePoint) {
        return MatrixTools.inverse(rgb2xyz(primaries, sourceWhitePoint));
    }

    public static Object rgb2xyz(double[][] primaries,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        try {
            double[][] t = MatrixTools.transpose(primaries);
            double[][] i = MatrixTools.inverse(t);
            double[][] s = MatrixTools.multiply(i, sourceWhitePoint);
            double[][] conversionMatrix = new double[3][3];
            conversionMatrix[0][0] = s[0][0] * t[0][0];
            conversionMatrix[1][0] = s[0][0] * t[1][0];
            conversionMatrix[2][0] = s[0][0] * t[2][0];
            conversionMatrix[0][1] = s[1][0] * t[0][1];
            conversionMatrix[1][1] = s[1][0] * t[1][1];
            conversionMatrix[2][1] = s[1][0] * t[2][1];
            conversionMatrix[0][2] = s[2][0] * t[0][2];
            conversionMatrix[1][2] = s[2][0] * t[1][2];
            conversionMatrix[2][2] = s[2][0] * t[2][2];
            Object ret;
            String d = "";
            Map<String, Object> map;
            if (isDemo) {
                d += "WhitePoint =\n";
                d += MatrixTools.print(sourceWhitePoint, 20, scale);
                d += "\nRGB Primaires = (Each primary color is one row)\n";
                d += MatrixTools.print(primaries, 20, scale);
                d += "\nRGB_Transposed_Primaires = (Each primary color is one column)\n";
                d += MatrixTools.print(t, 20, scale);
                d += "\nInversed = inverse(RGB_Transposed_Primaires) = \n";
                d += MatrixTools.print(i, 20, scale);
                d += "\nSrgb = Inversed * RGB_WhitePoint  = \n";
                d += MatrixTools.print(s, 20, scale);
                d += "\nRGB_to_XYZ_Matrix = (Column in Srgb) * (columns in RGB_Transposed_Primaires) =\n";
                d += MatrixTools.print(conversionMatrix, 20, scale);
                d += message("XYZSameWhiteRGB") + "\n";
            }
            if (targetWhitePoint == null || MatrixTools.same(sourceWhitePoint, targetWhitePoint, scale)) {
                if (isDemo) {
                    d = "ccccccccccccc " + message("Step") + " - Linear RGB -> XYZ ccccccccccccc\n\n" + d;
                    map = new HashMap();
                    map.put("procedure", d);
                    map.put("conversionMatrix", conversionMatrix);
                    ret = map;
                } else {
                    ret = conversionMatrix;
                }
                return ret;
            }
            double[][] adaptMatrix;
            String adaptString = null;
            Object adaptObject = matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale, isDemo);
            if (isDemo) {
                map = (Map<String, Object>) adaptObject;
                adaptMatrix = (double[][]) map.get("adpatMatrix");
                adaptString = (String) map.get("procedure");
            } else {
                adaptMatrix = (double[][]) adaptObject;
            }
            double[][] adaptedConversionMatrix = MatrixTools.multiply(adaptMatrix, conversionMatrix);
            if (scale >= 0) {
                adaptedConversionMatrix = MatrixTools.scale(adaptedConversionMatrix, scale);
            } else {
                scale = 8;
            }
            if (isDemo) {
                d = "ccccccccccccc " + message("Step") + " - Linear RGB -> XYZ , "
                        + message("RGBXYZsameWhite") + " ccccccccccccc\n\n" + d;
                d += "\naaaaaaaaaaaaa " + message("Step") + " - " + message("ChromaticAdaptation") + " aaaaaaaaaaaaa\n\n";
                d += adaptString + "\n";
                d += "\nccccccccccccc " + message("Step") + " - Linear RGB -> XYZ , "
                        + message("RGBXYZdifferentWhite") + " ccccccccccccc\n";
                d += "\nRGB Primaires = (Each primary color is one row)\n";
                d += MatrixTools.print(primaries, 20, scale);
                d += "\nRGB_WhitePoint =\n";
                d += MatrixTools.print(sourceWhitePoint, 20, scale);
                d += "\nXYZ_WhitePoint =\n";
                d += MatrixTools.print(targetWhitePoint, 20, scale);
                d += "\nRGB_to_XYZ_Matrix_different_WhitePoint = Adaptation_Matrix * RGB_to_XYZ_Matrix_same_WhitePoint\n";
                d += MatrixTools.print(adaptedConversionMatrix, 20, scale);
                map = new HashMap();
                map.put("procedure", d);
                map.put("conversionMatrix", adaptedConversionMatrix);
                ret = map;
            } else {
                ret = adaptedConversionMatrix;
            }
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object xyz2rgb(double[][] primaries,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        Object rgb2xyz = rgb2xyz(primaries, sourceWhitePoint, targetWhitePoint,
                algorithm, scale, isDemo);
        double[][] conversionMatrix;
        Object rgb2xyzObject = rgb2xyz(primaries, sourceWhitePoint, targetWhitePoint, algorithm, scale, isDemo);
        if (isDemo) {
            Map<String, Object> map = (Map<String, Object>) rgb2xyzObject;
            conversionMatrix = (double[][]) map.get("conversionMatrix");
            conversionMatrix = MatrixTools.inverse(conversionMatrix);
            map.put("conversionMatrix", conversionMatrix);
            String s = (String) map.get("procedure");
            s += "\nccccccccccccc " + message("Step") + " - XYZ -> Linear RGB  ccccccccccccc\n\n";
            s += "\nXYZ_to_RGB_Matrix = inverse( RGB_to_XYZ_Matrix ) = \n";
            s += MatrixTools.print(conversionMatrix, 20, scale);
            map.put("procedure", s);
            return map;
        } else {
            conversionMatrix = (double[][]) rgb2xyzObject;
            conversionMatrix = MatrixTools.inverse(conversionMatrix);
            return conversionMatrix;
        }
    }

    /*
        get/set
     */
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getXyzWhite() {
        return xyzWhite;
    }

    public void setXyzWhite(String xyzWhite) {
        this.xyzWhite = xyzWhite;
    }

    public String getRgb() {
        return rgb;
    }

    public void setRgb(String rgb) {
        this.rgb = rgb;
    }

    public String getRgbWhite() {
        return rgbWhite;
    }

    public void setRgbWhite(String rgbWhite) {
        this.rgbWhite = rgbWhite;
    }

    public String getRgb2xyz() {
        return rgb2xyz;
    }

    public void setRgb2xyz(String rgb2xyz) {
        this.rgb2xyz = rgb2xyz;
    }

    public String getXyz2rgb() {
        return xyz2rgb;
    }

    public void setXyz2rgb(String xyz2rgb) {
        this.xyz2rgb = xyz2rgb;
    }

}
