package mara.mybox.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.color.Illuminant.IlluminantType;
import mara.mybox.color.Illuminant.Observer;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleMatrixTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:09:26
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
// http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
// http://brucelindbloom.com/index.html?ColorCalculator.html
// https://ww2.mathworks.cn/help/images/ref/whitepoint.html
// http://www.thefullwiki.org/Standard_illuminant
public class ChromaticAdaptation {

    public String source, target;
    public String BradfordMethod, XYZScalingMethod, VonKriesMethod;

    public ChromaticAdaptation() {

    }

    public ChromaticAdaptation(String source, String target,
            String BradfordMethod, String XYZScalingMethod, String VonKriesMethod) {
        this.source = source;
        this.target = target;
        this.BradfordMethod = BradfordMethod;
        this.XYZScalingMethod = XYZScalingMethod;
        this.VonKriesMethod = VonKriesMethod;
    }


    /*
        Generation of Chromatic Adaptation matrices
     */
    public static List<ChromaticAdaptation> all(int scale) {
        List<ChromaticAdaptation> data = new ArrayList<>();
        for (IlluminantType sourceIlluminant : IlluminantType.values()) {
            for (IlluminantType targetIlluminant : IlluminantType.values()) {
                for (Observer sourceObserver : Observer.values()) {
                    for (Observer targetObserver : Observer.values()) {
                        if (sourceIlluminant == targetIlluminant
                                && sourceObserver == targetObserver) {
                            continue;
                        }
                        double[][] m1 = matrix(sourceIlluminant, sourceObserver,
                                targetIlluminant, targetObserver, ChromaticAdaptationAlgorithm.Bradford, -1);
                        double[][] m2 = matrix(sourceIlluminant, sourceObserver,
                                targetIlluminant, targetObserver, ChromaticAdaptationAlgorithm.XYZScaling, -1);
                        double[][] m3 = matrix(sourceIlluminant, sourceObserver,
                                targetIlluminant, targetObserver, ChromaticAdaptationAlgorithm.VonKries, -1);
                        ChromaticAdaptation ca = new ChromaticAdaptation();
                        ca.setSource(sourceIlluminant + " - " + sourceObserver);
                        ca.setTarget(targetIlluminant + " - " + targetObserver);
                        ca.setBradfordMethod(DoubleMatrixTools.print(m1, 0, scale));
                        ca.setXYZScalingMethod(DoubleMatrixTools.print(m2, 0, scale));
                        ca.setVonKriesMethod(DoubleMatrixTools.print(m3, 0, scale));
                        data.add(ca);
                    }
                }
            }
        }
        return data;
    }

    public static StringTable table(int scale) {
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(Languages.message("Source"), Languages.message("Target"),
                "Bradford", "XYZ", "Von Kries"
        ));
        StringTable table = new StringTable(names, Languages.message("ChromaticAdaptationMatrix"));
        for (IlluminantType sourceIlluminant : IlluminantType.values()) {
            for (IlluminantType targetIlluminant : IlluminantType.values()) {
                for (Observer sourceObserver : Observer.values()) {
                    for (Observer targetObserver : Observer.values()) {
                        if (sourceIlluminant == targetIlluminant
                                && sourceObserver == targetObserver) {
                            continue;
                        }
                        double[][] m1 = matrix(sourceIlluminant, sourceObserver,
                                targetIlluminant, targetObserver, ChromaticAdaptationAlgorithm.Bradford, -1);
                        double[][] m2 = matrix(sourceIlluminant, sourceObserver,
                                targetIlluminant, targetObserver, ChromaticAdaptationAlgorithm.XYZScaling, -1);
                        double[][] m3 = matrix(sourceIlluminant, sourceObserver,
                                targetIlluminant, targetObserver, ChromaticAdaptationAlgorithm.VonKries, -1);
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(sourceIlluminant.name() + " - " + sourceObserver.name(),
                                targetIlluminant.name() + " - " + targetObserver.name(),
                                DoubleMatrixTools.html(m1, scale),
                                DoubleMatrixTools.html(m2, scale),
                                DoubleMatrixTools.html(m3, scale)
                        ));
                        table.add(row);
                    }
                }
            }
        }
        return table;
    }

    public static String allTexts(int scale) {
        StringBuilder s = new StringBuilder();
        for (IlluminantType sourceIlluminant : IlluminantType.values()) {
            for (IlluminantType targetIlluminant : IlluminantType.values()) {
                for (Observer sourceObserver : Observer.values()) {
                    for (Observer targetObserver : Observer.values()) {
                        if (sourceIlluminant == targetIlluminant
                                && sourceObserver == targetObserver) {
                            continue;
                        }
                        for (ChromaticAdaptationAlgorithm a : ChromaticAdaptationAlgorithm.values()) {
                            double[][] m = matrix(sourceIlluminant, sourceObserver,
                                    targetIlluminant, targetObserver, a, -1);
                            s.append(Languages.message("Source")).append(":  ").append(sourceIlluminant).append(" - ").
                                    append(sourceObserver).append("\n");
                            s.append(Languages.message("Target")).append(":  ").append(targetIlluminant).append(" - ").
                                    append(targetObserver).append("\n");
                            s.append(Languages.message("Algorithm")).append(":  ").append(a).append("\n");
                            s.append(Languages.message("ChromaticAdaptationMatrix")).append(":  ").append("\n");
                            s.append(DoubleMatrixTools.print(m, 20, scale)).append("\n\n");
                        }
                    }
                }
            }
        }
        return s.toString();
    }

    public static double[] adapt(double[] xyz,
            IlluminantType fromType, Observer fromObserver,
            IlluminantType toType, Observer toObserver,
            ChromaticAdaptationAlgorithm algorithm) {

        return adapt(xyz[0], xyz[1], xyz[2], fromType, fromObserver, toType, toObserver, algorithm);
    }

    public static double[] adapt(double x, double y, double z,
            IlluminantType fromType, Observer fromObserver,
            IlluminantType toType, Observer toObserver,
            ChromaticAdaptationAlgorithm algorithm) {
        Illuminant from = new Illuminant(fromType, fromObserver);
        double[][] sourceWhitePoint = from.whitePoint();

        Illuminant to = new Illuminant(toType, toObserver);
        double[][] targetWhitePoint = to.whitePoint();

        return (double[]) adapt(x, y, z, sourceWhitePoint, targetWhitePoint, algorithm, -1, false);
    }

    public static double[] adapt(double[] xyz,
            IlluminantType fromType, IlluminantType toType,
            ChromaticAdaptationAlgorithm algorithm) {
        return adapt(xyz[0], xyz[1], xyz[2], fromType, toType, algorithm);
    }

    public static double[] adapt(double x, double y, double z,
            IlluminantType fromType, IlluminantType toType,
            ChromaticAdaptationAlgorithm algorithm) {
        Illuminant from = new Illuminant(fromType);
        double[][] sourceWhitePoint = from.whitePoint();

        Illuminant to = new Illuminant(toType);
        double[][] targetWhitePoint = to.whitePoint();

        return (double[]) adapt(x, y, z, sourceWhitePoint, targetWhitePoint, algorithm, -1, false);
    }

    public static Object adapt(double x, double y, double z,
            double sourceWhitePointX, double sourceWhitePointY, double sourceWhitePointZ,
            double targetWhitePointX, double targetWhitePointY, double targetWhitePointZ,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        double[][] sourceWhitePoint
                = DoubleMatrixTools.columnVector(sourceWhitePointX, sourceWhitePointY, sourceWhitePointZ);
        double[][] targetWhitePoint
                = DoubleMatrixTools.columnVector(targetWhitePointX, targetWhitePointY, targetWhitePointZ);
        return adapt(x, y, z, sourceWhitePoint, targetWhitePoint, algorithm, scale, isDemo);
    }

    public static double[] adapt(double[] xyz,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm) {
        return adapt(xyz[0], xyz[1], xyz[2], sourceWhitePoint, targetWhitePoint, algorithm);
    }

    public static double[] adapt(double x, double y, double z,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm) {
        return (double[]) adapt(x, y, z, sourceWhitePoint, targetWhitePoint, algorithm, -1, false);
    }

    public static Object adapt(double[] xyz,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        return adapt(xyz[0], xyz[1], xyz[2], sourceWhitePoint, targetWhitePoint, algorithm, scale, isDemo);
    }

    public static Object adapt(double x, double y, double z,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        try {
            if (DoubleMatrixTools.same(sourceWhitePoint, targetWhitePoint, scale)) {
                double[] result = {x, y, z};
                if (isDemo) {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("procedure", Languages.message("NeedNotAdaptChromatic"));
                    ret.put("matrix", DoubleMatrixTools.identityDouble(3));
                    ret.put("adaptedColor", result);
                    return ret;
                } else {
                    return result;
                }
            }
            double[][] adaptMatrix;
            String adaptString = null;
            Object adaptObject = matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale, isDemo);
            if (isDemo) {
                Map<String, Object> adapt = (Map<String, Object>) adaptObject;
                adaptMatrix = (double[][]) adapt.get("adpatMatrix");
                adaptString = (String) adapt.get("procedure");
            } else {
                adaptMatrix = (double[][]) adaptObject;
            }
            double[][] sourceColor = DoubleMatrixTools.columnVector(x, y, z);
            double[][] adaptedColor = DoubleMatrixTools.multiply(adaptMatrix, sourceColor);
            double[] result = DoubleMatrixTools.columnValues(adaptedColor, 0);
            if (isDemo) {
                String s = "";
                s += "\naaaaaaaaaaaaa " + Languages.message("Step") + " - " + Languages.message("ChromaticAdaptationMatrix") + " aaaaaaaaaaaaa\n\n";
                s += adaptString + "\n";

                s += "\naaaaaaaaaaaaa " + Languages.message("Step") + " - " + Languages.message("ChromaticAdaptation") + " aaaaaaaaaaaaa\n\n";
                s += "SourceColor = \n";
                s += DoubleMatrixTools.print(sourceColor, 20, scale);

                s += "\nAdaptedColor = M * SourceColor = \n";
                s += DoubleMatrixTools.print(adaptedColor, 20, scale);

                Map<String, Object> ret = new HashMap<>();

                ret.put("matrix", adaptMatrix);
                ret.put("procedure", s);
                ret.put("adaptedColor", result);
                return ret;
            } else {
                return result;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] matrix(
            IlluminantType fromType, IlluminantType toType,
            ChromaticAdaptationAlgorithm algorithm, int scale) {
        Illuminant from = new Illuminant(fromType);
        double[][] sourceWhitePoint = from.whitePoint();

        Illuminant to = new Illuminant(toType);
        double[][] targetWhitePoint = to.whitePoint();

        return matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale);
    }

    public static double[][] matrix(
            IlluminantType fromType, Observer fromObserver,
            IlluminantType toType, Observer toObserver,
            ChromaticAdaptationAlgorithm algorithm, int scale) {
        Illuminant from = new Illuminant(fromType, fromObserver);
        double[][] sourceWhitePoint = from.whitePoint();

        Illuminant to = new Illuminant(toType, toObserver);
        double[][] targetWhitePoint = to.whitePoint();

        return matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale);
    }

    public static double[][] matrix(
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale) {
        return (double[][]) matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale, false);
    }

    public static Map<String, Object> matrixDemo(
            double sourceWhitePointX, double sourceWhitePointY, double sourceWhitePointZ,
            double targetWhitePointX, double targetWhitePointY, double targetWhitePointZ,
            ChromaticAdaptationAlgorithm algorithm, int scale) {
        double[][] sourceWhitePoint = DoubleMatrixTools.columnVector(sourceWhitePointX, sourceWhitePointY, sourceWhitePointZ);
        double[][] targetWhitePoint = DoubleMatrixTools.columnVector(targetWhitePointX, targetWhitePointY, targetWhitePointZ);
        return ChromaticAdaptation.matrixDemo(sourceWhitePoint, targetWhitePoint, algorithm, scale);
    }

    public static Map<String, Object> matrixDemo(
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale) {
        return (Map<String, Object>) matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale, true);
    }

    public static Object matrix(
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        try {
            if (targetWhitePoint == null || DoubleMatrixTools.same(sourceWhitePoint, targetWhitePoint, scale)) {
                if (isDemo) {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("procedure", Languages.message("NeedNotAdaptChromatic"));
                    ret.put("adpatMatrix", DoubleMatrixTools.identityDouble(3));
                    return ret;
                } else {
                    return DoubleMatrixTools.identityDouble(3);
                }
            }
            double[][] MA, MAI;
            if (algorithm == null) {
                algorithm = ChromaticAdaptationAlgorithm.Bradford;
            }
            switch (algorithm) {
                case Bradford:
                    MA = Bradford;
                    MAI = BradfordInversed;
                    break;
                case XYZScaling:
                    MA = XYZScaling;
                    MAI = XYZScalingInversed;
                    break;
                case VonKries:
                    MA = VonKries;
                    MAI = VonKriesInversed;
                    break;
                default:
                    return null;
            }
            double[][] sourceCone = DoubleMatrixTools.multiply(MA, sourceWhitePoint);
            double[][] targetCone = DoubleMatrixTools.multiply(MA, targetWhitePoint);
            double[][] ratioMatrix = new double[3][3];
            ratioMatrix[0][0] = targetCone[0][0] / sourceCone[0][0];
            ratioMatrix[1][1] = targetCone[1][0] / sourceCone[1][0];
            ratioMatrix[2][2] = targetCone[2][0] / sourceCone[2][0];
            double[][] M = DoubleMatrixTools.multiply(MAI, ratioMatrix);
            M = DoubleMatrixTools.multiply(M, MA);
            if (scale >= 0) {
                M = DoubleMatrixTools.scale(M, scale);
            } else {
                scale = 8;
            }
            if (isDemo) {
                String s = "";
                s += "SourceWhitePoint = \n";
                s += DoubleMatrixTools.print(sourceWhitePoint, 20, scale);

                s += "TargetWhitePoint = \n";
                s += DoubleMatrixTools.print(targetWhitePoint, 20, scale);

                s += "\n" + Languages.message("Algorithm") + ": " + algorithm + "\n";
                s += "MA = \n";
                s += DoubleMatrixTools.print(MA, 20, scale);
                s += "MA_Inversed =\n";
                s += DoubleMatrixTools.print(MAI, 20, scale);

                s += "\n" + "SourceCone = MA * SourceWhitePoint =\n";
                s += DoubleMatrixTools.print(sourceCone, 20, scale);
                s += "\n" + "TargetCone = MA * TargetWhitePoint =\n";
                s += DoubleMatrixTools.print(targetCone, 20, scale);

                s += "\n" + "RatioMatrix = TargetCone / SourceCone =\n";
                s += DoubleMatrixTools.print(ratioMatrix, 20, scale);

                s += "\n" + "Adaptation_Matrix = MA_Inversed * RatioMatrix * MA =\n";
                s += DoubleMatrixTools.print(M, 20, scale);
                Map<String, Object> ret = new HashMap<>();

                ret.put("procedure", s);
                ret.put("adpatMatrix", M);
                return ret;
            } else {
                return M;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
        Chromatic Adaptation matrix
        Algorithm: Bradford
     */
    public static double[] AtoD50(double[] a) {
        double[][] matrix = {
            {0.8779529, -0.0915288, 0.2566181},
            {-0.1117372, 1.0924325, 0.0851788},
            {0.0502012, -0.0837636, 2.3994031}
        };
        double[] d50 = DoubleMatrixTools.multiply(matrix, a);
        return d50;
    }

    public static double[] BtoD50(double[] b) {
        double[][] matrix = {
            {0.9850292, -0.0093910, -0.0026720},
            {-0.0147751, 1.0146711, -0.0000389},
            {-0.0017035, 0.0035957, 0.9660561}
        };
        double[] d50 = DoubleMatrixTools.multiply(matrix, b);
        return d50;
    }

    public static double[] CtoD50(double[] c) {
        double[][] matrix = {
            {1.0376976, 0.0153932, -0.0582624},
            {0.0170675, 1.0056038, -0.0188973},
            {-0.0120126, 0.0204361, 0.6906380}
        };
        double[] d50 = DoubleMatrixTools.multiply(matrix, c);
        return d50;
    }

    public static double[] EtoD65(double[] e) {
        double[][] matrix = {
            {0.9531874, -0.0265906, 0.0238731},
            {-0.0382467, 1.0288406, 0.0094060},
            {0.0026068, -0.0030332, 1.0892565}
        };
        double[] d65 = DoubleMatrixTools.multiply(matrix, e);
        return d65;
    }

    public static double[] EtoD50(double[] e) {
        double[][] matrix = {
            {0.9977545, -0.0041632, -0.0293713},
            {-0.0097677, 1.0183168, -0.0085490},
            {-0.0074169, 0.0134416, 0.8191853}
        };
        double[] d65 = DoubleMatrixTools.multiply(matrix, e);
        return d65;
    }

    public static double[] D50toE(double[] d50) {
        double[][] matrix = {
            {1.0025535, 0.0036238, 0.0359837},
            {0.0096914, 0.9819125, 0.0105947},
            {0.0089181, -0.0160789, 1.2208770}
        };
        double[] e = DoubleMatrixTools.multiply(matrix, d50);
        return e;
    }

    public static double[] D50toD65(double[] d50) {
        double[][] matrix = {
            {0.9555766, -0.0230393, 0.0631636},
            {-0.0282895, 1.0099416, 0.0210077},
            {0.0122982, -0.0204830, 1.3299098}
        };
        double[] d65 = DoubleMatrixTools.multiply(matrix, d50);
        return d65;
    }

    public static double[] D55toD50(double[] d55) {
        double[][] matrix = {
            {1.0184567, 0.0093864, -0.0213199},
            {0.0120291, 0.9951460, -0.0072228},
            {-0.0039673, 0.0064899, 0.8925936}
        };
        double[] d50 = DoubleMatrixTools.multiply(matrix, d55);
        return d50;
    }

    public static double[] D65toE(double[] d65) {
        double[][] matrix = {
            {1.0502616, 0.0270757, -0.0232523},
            {0.0390650, 0.9729502, -0.0092579},
            {-0.0024047, 0.0026446, 0.9180873}
        };
        double[] e = DoubleMatrixTools.multiply(matrix, d65);
        return e;
    }

    public static double[] D65toD50(double[] d65) {
        double[][] matrix = {
            {1.0478112, 0.0228866, -0.0501270},
            {0.0295424, 0.9904844, -0.0170491},
            {-0.0092345, 0.0150436, 0.7521316}
        };
        double[] d50 = DoubleMatrixTools.multiply(matrix, d65);
        return d50;
    }

    /*
        Data
     */
    public static enum ChromaticAdaptationAlgorithm {
        Bradford, XYZScaling, VonKries
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (ChromaticAdaptationAlgorithm c : ChromaticAdaptationAlgorithm.values()) {
            names.add(c + "");
        }
        return names;
    }

    // Chromatic Adaptation Algorithms
    public static double[][] XYZScaling = {
        {1.0000000, 0.0000000, 0.0000000},
        {0.0000000, 1.0000000, 0.0000000},
        {0.0000000, 0.0000000, 1.0000000}
    };

    public static double[][] XYZScalingInversed = {
        {1.0000000, 0.0000000, 0.0000000},
        {0.0000000, 1.0000000, 0.0000000},
        {0.0000000, 0.0000000, 1.0000000}
    };

    public static double[][] Bradford = {
        {0.8951000, 0.2664000, -0.1614000},
        {-0.7502000, 1.7135000, 0.0367000},
        {0.0389000, -0.0685000, 1.0296000}
    };

    public static double[][] BradfordInversed = {
        {0.9869929, -0.1470543, 0.1599627},
        {0.4323053, 0.5183603, 0.0492912},
        {-0.0085287, 0.0400428, 0.9684867}
    };

    public static double[][] VonKries = {
        {0.4002400, 0.7076000, -0.0808100},
        {-0.2263000, 1.1653200, 0.0457000},
        {0.0000000, 0.0000000, 0.9182200}
    };

    public static double[][] VonKriesInversed = {
        {1.8599364, -1.1293816, 0.2198974},
        {0.3611914, 0.6388125, -0.0000064},
        {0.0000000, 0.0000000, 1.0890636}
    };

    /*
        get/set
     */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBradfordMethod() {
        return BradfordMethod;
    }

    public void setBradfordMethod(String BradfordMethod) {
        this.BradfordMethod = BradfordMethod;
    }

    public String getXYZScalingMethod() {
        return XYZScalingMethod;
    }

    public void setXYZScalingMethod(String XYZScalingMethod) {
        this.XYZScalingMethod = XYZScalingMethod;
    }

    public String getVonKriesMethod() {
        return VonKriesMethod;
    }

    public void setVonKriesMethod(String VonKriesMethod) {
        this.VonKriesMethod = VonKriesMethod;
    }

}
