package mara.mybox.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.color.ChromaticAdaptation.ChromaticAdaptationAlgorithm;
import static mara.mybox.color.ChromaticAdaptation.matrix;
import mara.mybox.color.Illuminant.IlluminantType;
import mara.mybox.color.Illuminant.Observer;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 21:33:50
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 *
 * Reference:
 * http://brucelindbloom.com/index.html?WorkingSpaceInfo.html#Specifications
 */
public class RGBColorSpace extends CIEData {

    public String colorSpaceName, colorName, illuminantName, adaptAlgorithm;

    public RGBColorSpace(String colorSpace, String color,
            String illuminant, double[] values) {
        this.colorSpaceName = colorSpace;
        this.illuminantName = illuminant;
        this.colorName = color;
        this.adaptAlgorithm = "";
        setxyY(values);
    }

    public RGBColorSpace(String colorSpace, String color,
            String illuminant, String adaptAlgorithm, double[] values) {
        this.colorSpaceName = colorSpace;
        this.illuminantName = illuminant;
        this.adaptAlgorithm = adaptAlgorithm;
        this.colorName = color;
        setTristimulusValues(values[0], values[1], values[2]);
    }

    /*
        Static methods
     */
    public static List<RGBColorSpace> standard() {
        List<RGBColorSpace> data = new ArrayList<>();
        for (ColorSpaceType cs : ColorSpaceType.values()) {
            double[][] p = primaries(cs);
            String n = name(cs);
            String t = illuminantType(cs) + "";
            data.add(new RGBColorSpace(n, "Red", t, p[0]));
            data.add(new RGBColorSpace(n, "Green", t, p[1]));
            data.add(new RGBColorSpace(n, "Blue", t, p[2]));
        }
        return data;
    }

    public static List<RGBColorSpace> adapted() {
        List<RGBColorSpace> data = new ArrayList<>();
        for (ColorSpaceType cs : ColorSpaceType.values()) {
            String name = name(cs);
            double[][] primaries = primariesTristimulus(name);
            IlluminantType ci = illuminantType(cs);
            for (IlluminantType i : IlluminantType.values()) {
                for (ChromaticAdaptationAlgorithm a : ChromaticAdaptationAlgorithm.values()) {
                    if (ci != i) {
                        data.add(new RGBColorSpace(name, "Red", i + " - " + Observer.CIE1931, a + "",
                                ChromaticAdaptation.adapt(primaries[0], ci, Observer.CIE1931, i, Observer.CIE1931, a)));
                        data.add(new RGBColorSpace(name, "Green", i + " - " + Observer.CIE1931, a + "",
                                ChromaticAdaptation.adapt(primaries[1], ci, Observer.CIE1931, i, Observer.CIE1931, a)));
                        data.add(new RGBColorSpace(name, "Blue", i + " - " + Observer.CIE1931, a + "",
                                ChromaticAdaptation.adapt(primaries[2], ci, Observer.CIE1931, i, Observer.CIE1931, a)));
                    }
                    data.add(new RGBColorSpace(name, "Red", i + " - " + Observer.CIE1964, a + "",
                            ChromaticAdaptation.adapt(primaries[0], ci, Observer.CIE1931, i, Observer.CIE1964, a)));
                    data.add(new RGBColorSpace(name, "Green", i + " - " + Observer.CIE1964, a + "",
                            ChromaticAdaptation.adapt(primaries[1], ci, Observer.CIE1931, i, Observer.CIE1964, a)));
                    data.add(new RGBColorSpace(name, "Blue", i + " - " + Observer.CIE1964, a + "",
                            ChromaticAdaptation.adapt(primaries[2], ci, Observer.CIE1931, i, Observer.CIE1964, a)));
                }
            }
        }
        return data;
    }

    public static List<RGBColorSpace> all() {
        List<RGBColorSpace> data = new ArrayList<>();
        data.addAll(standard());
        data.addAll(adapted());
        return data;
    }

    public static List<RGBColorSpace> all(int scale) {
        List<RGBColorSpace> data = RGBColorSpace.all();
        for (RGBColorSpace d : data) {
            d.scaleValues(scale);
        }
        return data;
    }

    public static StringTable allTable() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("ColorSpace"),
                    Languages.message("Illuminant"), Languages.message("AdaptationAlgorithm"), Languages.message("PrimaryColor"),
                    Languages.message("TristimulusX"), Languages.message("TristimulusY"), Languages.message("TristimulusZ"),
                    Languages.message("NormalizedX"), Languages.message("NormalizedY"), Languages.message("NormalizedZ"),
                    Languages.message("RelativeX"), Languages.message("RelativeY"), Languages.message("RelativeZ")
            ));
            StringTable table = new StringTable(names, Languages.message("RGBPrimaries"));
            List<RGBColorSpace> data = all(8);
            for (RGBColorSpace d : data) {
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        d.colorSpaceName, d.illuminantName, d.adaptAlgorithm, d.colorName,
                        d.X + "", d.Y + "", d.Z + "",
                        d.getNormalizedX() + "", d.getNormalizedY() + "", d.getNormalizedZ() + "",
                        d.getRelativeX() + "", d.getRelativeY() + "", d.getRelativeZ() + ""
                ));
                table.add(row);
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (ColorSpaceType cs : ColorSpaceType.values()) {
            names.add(name(cs));
        }
        return names;
    }

    public static String name(ColorSpaceType colorSpaceType) {
        try {
            switch (colorSpaceType) {
                case CIERGB:
                    return CIERGB;
                case ECIRGB:
                    return ECIRGB;
                case AdobeRGB:
                    return AdobeRGB;
                case AppleRGB:
                    return AppleRGB;
                case sRGB:
                    return sRGB;
                case PALRGB:
                    return PALRGB;
                case NTSCRGB:
                    return NTSCRGB;
                case ColorMatchRGB:
                    return ColorMatchRGB;
                case ProPhotoRGB:
                    return ProPhotoRGB;
                case SMPTECRGB:
                    return SMPTECRGB;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static ColorSpaceType type(String name) {
        try {
            if (CIERGB.equals(name)) {
                return ColorSpaceType.CIERGB;
            } else if (ECIRGB.equals(name)) {
                return ColorSpaceType.ECIRGB;
            } else if (AdobeRGB.equals(name)) {
                return ColorSpaceType.AdobeRGB;
            } else if (AppleRGB.equals(name)) {
                return ColorSpaceType.AppleRGB;
            } else if (sRGB.equals(name)) {
                return ColorSpaceType.sRGB;
            } else if (PALRGB.equals(name)) {
                return ColorSpaceType.PALRGB;
            } else if (NTSCRGB.equals(name)) {
                return ColorSpaceType.NTSCRGB;
            } else if (ColorMatchRGB.equals(name)) {
                return ColorSpaceType.ColorMatchRGB;
            } else if (ProPhotoRGB.equals(name)) {
                return ColorSpaceType.ProPhotoRGB;
            } else if (SMPTECRGB.equals(name)) {
                return ColorSpaceType.SMPTECRGB;
            }
        } catch (Exception e) {
        }
        return null;
    }

    // Based on CIE 1931 2 degree observer
    public static double[][] primariesNormalized(ColorSpaceType cs) {
        double[][] p = primaries(cs);
        double[][] n = new double[3][3];
        n[0][0] = p[0][0];
        n[0][1] = p[0][1];
        n[0][2] = 1 - p[0][0] - p[0][1];
        n[1][0] = p[1][0];
        n[1][1] = p[1][1];
        n[1][2] = 1 - p[1][0] - p[1][1];
        n[2][0] = p[2][0];
        n[2][1] = p[2][1];
        n[2][2] = 1 - p[2][0] - p[2][1];
        return n;
    }

    public static double[][] primariesNormalized(String csName) {
        return primariesNormalized(type(csName));
    }

    public static double[][] primariesRelative(ColorSpaceType cs) {
        double[][] p = primaries(cs);
        double[][] n = new double[3][3];
        n[0][0] = p[0][0] / p[0][1];
        n[0][1] = 1;
        n[0][2] = (1 - p[0][0] - p[0][1]) / p[0][1];
        n[1][0] = p[1][0] / p[1][1];
        n[1][1] = 1;
        n[1][2] = (1 - p[1][0] - p[1][1]) / p[1][1];
        n[2][0] = p[2][0] / p[2][1];
        n[2][1] = 1;
        n[2][2] = (1 - p[2][0] - p[2][1]) / p[2][1];
        return n;
    }

    public static double[][] primariesRelative(String csName) {
        return primariesRelative(type(csName));
    }

    public static double[][] primariesTristimulus(ColorSpaceType cs) {
        double[][] p = primaries(cs);
        double[][] n = new double[3][3];
        n[0][0] = p[0][0] * p[0][2] / p[0][1];
        n[0][1] = p[0][2];
        n[0][2] = (1 - p[0][0] - p[0][1]) * p[0][2] / p[0][1];
        n[1][0] = p[1][0] * p[1][2] / p[1][1];
        n[1][1] = p[1][2];
        n[1][2] = (1 - p[1][0] - p[1][1]) * p[1][2] / p[1][1];
        n[2][0] = p[2][0] * p[2][2] / p[2][1];
        n[2][1] = p[2][2];
        n[2][2] = (1 - p[2][0] - p[2][1]) * p[2][2] / p[2][1];
        return n;
    }

    public static double[][] primariesTristimulus(String csName) {
        return primariesTristimulus(type(csName));
    }

    public static double[][] primaries(String csName) {
        return primaries(type(csName));
    }

    public static double[][] primaries(ColorSpaceType colorSpaceType) {
        try {
            switch (colorSpaceType) {
                case CIERGB:
                    return CIEPrimariesE;
                case ECIRGB:
                    return ECIPrimariesD50;
                case AdobeRGB:
                    return AdobePrimariesD65;
                case AppleRGB:
                    return ApplePrimariesD65;
                case sRGB:
                    return sRGBPrimariesD65;
                case PALRGB:
                    return PALPrimariesD65;
                case NTSCRGB:
                    return NTSCPrimariesC;
                case ColorMatchRGB:
                    return ColorMatchPrimariesD50;
                case ProPhotoRGB:
                    return ProPhotoPrimariesD50;
                case SMPTECRGB:
                    return SMPTECPrimariesD65;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static double[] whitePoint(String csName) {
        return whitePoint(type(csName));
    }

    public static double[] whitePoint(ColorSpaceType colorSpaceType) {
        double[] xy = whitePointXY(colorSpaceType);
        return CIEDataTools.relative(xy[0], xy[1], 1 - xy[0] - xy[1]);
    }

    public static double[] whitePointXY(ColorSpaceType colorSpaceType) {
        try {
            switch (colorSpaceType) {
                case CIERGB:
                    return Illuminant.Illuminant1931E;
                case ECIRGB:
                    return Illuminant.Illuminant1931D50;
                case AdobeRGB:
                    return Illuminant.Illuminant1931D65;
                case AppleRGB:
                    return Illuminant.Illuminant1931D65;
                case sRGB:
                    return Illuminant.Illuminant1931D65;
                case PALRGB:
                    return Illuminant.Illuminant1931D65;
                case NTSCRGB:
                    return Illuminant.Illuminant1931C;
                case ColorMatchRGB:
                    return Illuminant.Illuminant1931D50;
                case ProPhotoRGB:
                    return Illuminant.Illuminant1931D50;
                case SMPTECRGB:
                    return Illuminant.Illuminant1931D65;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static double[][] whitePointMatrix(ColorSpaceType colorSpaceType) {
        return DoubleMatrixTools.columnVector(whitePoint(colorSpaceType));
    }

    public static String illuminantName(String csName) {
        return illuminantType(type(csName)) + " - " + Illuminant.Observer.CIE1931;
    }

    public static IlluminantType illuminantType(String csName) {
        return illuminantType(type(csName));
    }

    public static IlluminantType illuminantType(ColorSpaceType colorSpaceType) {
        try {
            switch (colorSpaceType) {
                case CIERGB:
                    return IlluminantType.E;
                case ECIRGB:
                    return IlluminantType.D50;
                case AdobeRGB:
                    return IlluminantType.D65;
                case AppleRGB:
                    return IlluminantType.D65;
                case sRGB:
                    return IlluminantType.D65;
                case PALRGB:
                    return IlluminantType.D65;
                case NTSCRGB:
                    return IlluminantType.C;
                case ColorMatchRGB:
                    return IlluminantType.D50;
                case ProPhotoRGB:
                    return IlluminantType.D50;
                case SMPTECRGB:
                    return IlluminantType.D65;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static double[][] primariesAdapted(ColorSpaceType cs,
            IlluminantType targetIlluminantType, Observer targetObserver,
            ChromaticAdaptationAlgorithm algorithm) {
        double[][] primaries = primariesTristimulus(cs);
        IlluminantType ci = illuminantType(cs);
        double[] red = ChromaticAdaptation.adapt(primaries[0], ci, Observer.CIE1931,
                targetIlluminantType, targetObserver, algorithm);
        double[] green = ChromaticAdaptation.adapt(primaries[1], ci, Observer.CIE1931,
                targetIlluminantType, targetObserver, algorithm);
        double[] blue = ChromaticAdaptation.adapt(primaries[2], ci, Observer.CIE1931,
                targetIlluminantType, targetObserver, algorithm);
        double[][] adpated = {red, green, blue};
        return adpated;
    }

    public static Object primariesAdapted(ColorSpaceType cs,
            double[][] targetWhitePoint, ChromaticAdaptationAlgorithm algorithm,
            int scale, boolean isDemo) {
        double[][] primaries = primariesTristimulus(cs);
        double[][] sourceWhitePoint = whitePointMatrix(cs);
        return primariesAdapted(primaries, sourceWhitePoint, targetWhitePoint,
                algorithm, scale, isDemo);
    }

    public static Object primariesAdapted(double[][] primaries,
            double[][] sourceWhitePoint, double[][] targetWhitePoint,
            ChromaticAdaptationAlgorithm algorithm, int scale, boolean isDemo) {
        try {
            double[][] adaptMatrix;
            String adaptString = null;
            Map<String, Object> adapt = null;
            if (DoubleMatrixTools.same(sourceWhitePoint, targetWhitePoint, scale)) {
                if (isDemo) {
                    adapt = new HashMap<>();
                    adapt.put("procedure", Languages.message("NeedNotAdaptChromatic"));
                    adapt.put("matrix", DoubleMatrixTools.identityDouble(3));
                    adapt.put("adaptedPrimaries", primaries);
                    return adapt;
                } else {
                    return primaries;
                }
            }
            Object adaptObject = matrix(sourceWhitePoint, targetWhitePoint, algorithm, scale, isDemo);
            if (isDemo) {
                adapt = (Map<String, Object>) adaptObject;
                adaptMatrix = (double[][]) adapt.get("adpatMatrix");
                adaptString = (String) adapt.get("procedure");
            } else {
                adaptMatrix = (double[][]) adaptObject;
            }
            double[][] sourceRed = DoubleMatrixTools.columnVector(primaries[0]);
            double[][] adaptedRed = DoubleMatrixTools.multiply(adaptMatrix, sourceRed);
            double[][] sourceGreen = DoubleMatrixTools.columnVector(primaries[1]);
            double[][] adaptedGreen = DoubleMatrixTools.multiply(adaptMatrix, sourceGreen);
            double[][] sourceBlue = DoubleMatrixTools.columnVector(primaries[2]);
            double[][] adaptedBlue = DoubleMatrixTools.multiply(adaptMatrix, sourceBlue);
            double[][] adaptedPrimaries = {
                DoubleMatrixTools.columnValues(adaptedRed, 0),
                DoubleMatrixTools.columnValues(adaptedGreen, 0),
                DoubleMatrixTools.columnValues(adaptedBlue, 0)
            };
            if (scale >= 0) {
                adaptedPrimaries = DoubleMatrixTools.scale(adaptedPrimaries, scale);
            } else {
                scale = 8;
            }
            if (isDemo) {
                String s = "";
                s += "\naaaaaaaaaaaaa " + Languages.message("Step") + " - " + Languages.message("ChromaticAdaptationMatrix") + " aaaaaaaaaaaaa\n\n";
                s += adaptString + "\n";
                s += "\naaaaaaaaaaaaa " + Languages.message("Step") + " - " + Languages.message("ChromaticAdaptation") + " aaaaaaaaaaaaa\n";
                s += "\nsourceRed = \n";
                s += DoubleMatrixTools.print(sourceRed, 20, scale);
                s += "\nadaptedRed = M * sourceRed  = \n";
                s += DoubleMatrixTools.print(adaptedRed, 20, scale);
                s += "\nsourceGreen = \n";
                s += DoubleMatrixTools.print(sourceGreen, 20, scale);
                s += "\nadaptedGreen = M * sourceGreen  = \n";
                s += DoubleMatrixTools.print(adaptedGreen, 20, scale);
                s += "\nsourceBlue = \n";
                s += DoubleMatrixTools.print(sourceBlue, 20, scale);
                s += "\nadaptedBlue = M * sourceBlue  = \n";
                s += DoubleMatrixTools.print(adaptedBlue, 20, scale);
                s += "\nadaptedPrimaries = \n";
                s += DoubleMatrixTools.print(adaptedPrimaries, 20, scale);
                adapt.put("procedure", s);
                adapt.put("adaptedPrimaries", adaptedPrimaries);
                return adapt;
            } else {
                return adaptedPrimaries;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
         Gamma
     */
    public static GammaType gamma(String csName) {
        return gammaType(type(csName));
    }

    public static GammaType gammaType(ColorSpaceType colorSpaceType) {
        try {
            switch (colorSpaceType) {
                case CIERGB:
                case AdobeRGB:
                case PALRGB:
                case NTSCRGB:
                case SMPTECRGB:
                    return GammaType.Gamma22;
                case ECIRGB:
                    return GammaType.GammaL;
                case AppleRGB:
                case ColorMatchRGB:
                case ProPhotoRGB:
                    return GammaType.Gamma18;
                case sRGB:
                    return GammaType.GammaSRGB;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String gammaName(GammaType gammaType) {
        try {
            switch (gammaType) {
                case GammaSRGB:
                    return "sRGB";
                case Gamma22:
                    return "2.2";
                case Gamma18:
                    return "1.8";
                case GammaL:
                    return "L*";
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String gammaName(String colorSpaceName) {
        try {
            if (RGBColorSpace.sRGB.equals(colorSpaceName)) {
                return "sRGB";
            } else if (RGBColorSpace.ECIRGB.equals(colorSpaceName)) {
                return "L*";
            } else if (RGBColorSpace.CIERGB.equals(colorSpaceName)
                    || RGBColorSpace.AdobeRGB.equals(colorSpaceName)
                    || RGBColorSpace.PALRGB.equals(colorSpaceName)
                    || RGBColorSpace.NTSCRGB.equals(colorSpaceName)
                    || RGBColorSpace.SMPTECRGB.equals(colorSpaceName)) {
                return "2.2";
            } else if (RGBColorSpace.AppleRGB.equals(colorSpaceName)
                    || RGBColorSpace.ColorMatchRGB.equals(colorSpaceName)
                    || RGBColorSpace.ProPhotoRGB.equals(colorSpaceName)) {
                return "1.8";
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static double gamma(double Gramma, double v) {
        if (v <= 0.0) {
            return 0.0;
        } else {
            return Math.pow(v, 1 / Gramma);
        }
    }

    public static double gamma22(double v) {
        return gamma(2.19921875, v);
    }

    public static double gamma18(double v) {
        return gamma(1.8, v);
    }

    public static double gammaSRGB(double v) {
        if (v <= 0.0031308) {
            return v * 12.92;
        } else {
            return 1.055 * Math.pow(v, 1d / 2.4) - 0.055;
        }
    }

    /*
         Linear
     */
    public static double linearColor(double Gramma, double v) {
        if (v <= 0.0) {
            return 0.0;
        } else {
            return Math.pow(v, Gramma);
        }
    }

    public static double linear22(double v) {
        return linearColor(2.19921875, v);
    }

    public static double linear18(double v) {
        return linearColor(1.8, v);
    }

    public static double linearSRGB(double v) {
        if (v <= 0.04045) {
            return v / 12.92;
        } else {
            return Math.pow((v + 0.055) / 1.055, 2.4);
        }
    }

    /*
        Data
     */
    // http://www.eci.org/en/downloads
    // {r,g,b}{x, y, Y}
    public static enum ColorSpaceType {
        CIERGB, ECIRGB, AdobeRGB, AppleRGB, sRGB, PALRGB, NTSCRGB, ColorMatchRGB,
        ProPhotoRGB, SMPTECRGB
    }

    public static enum GammaType {
        Linear, Gamma22, Gamma18, GammaSRGB, GammaL
    }

    public static String CIERGB = "CIE RGB";
    public static String ECIRGB = "ECI RGB v2";
    public static String AdobeRGB = "Adobe RGB (1998)";
    public static String AppleRGB = "Apple RGB";
    public static String sRGB = "sRGB";
    public static String PALRGB = "PAL/SECAM RGB";
    public static String NTSCRGB = "NTSC RGB";
    public static String ColorMatchRGB = "ColorMatch RGB";
    public static String ProPhotoRGB = "ProPhoto RGB";
    public static String SMPTECRGB = "SMPTE-C RGB";

    public static double[][] CIEPrimariesE = {
        {0.7350, 0.2650, 0.176204},
        {0.2740, 0.7170, 0.812985},
        {0.1670, 0.0090, 0.010811}
    };

    public static double[][] ECIPrimariesD50 = {
        {0.670000, 0.330000, 0.320250},
        {0.210000, 0.710000, 0.602071},
        {0.140000, 0.080000, 0.077679}
    };

    public static double[][] sRGBPrimariesD65 = {
        {0.6400, 0.3300, 0.212656},
        {0.3000, 0.6000, 0.715158},
        {0.1500, 0.0600, 0.072186}
    };

    public static double[][] AdobePrimariesD65 = {
        {0.6400, 0.3300, 0.297361},
        {0.2100, 0.7100, 0.627355},
        {0.1500, 0.0600, 0.075285}
    };

    public static double[][] ApplePrimariesD65 = {
        {0.6250, 0.3400, 0.244634},
        {0.2800, 0.5950, 0.672034},
        {0.1550, 0.0700, 0.083332}
    };

    public static double[][] PALPrimariesD65 = {
        {0.6400, 0.3300, 0.222021},
        {0.2900, 0.6000, 0.706645},
        {0.1500, 0.0600, 0.071334}
    };

    public static double[][] NTSCPrimariesC = {
        {0.6700, 0.3300, 0.298839},
        {0.2100, 0.7100, 0.586811},
        {0.1400, 0.0800, 0.114350}
    };

    public static double[][] ProPhotoPrimariesD50 = {
        {0.734700, 0.265300, 0.288040},
        {0.159600, 0.840400, 0.711874},
        {0.036600, 0.000100, 0.000086}
    };

    public static double[][] ColorMatchPrimariesD50 = {
        {0.630000, 0.340000, 0.274884},
        {0.295000, 0.605000, 0.658132},
        {0.150000, 0.075000, 0.066985}
    };

    public static double[][] SMPTECPrimariesD65 = {
        {0.6300, 0.3400, 0.212395},
        {0.3100, 0.5950, 0.701049},
        {0.1550, 0.0700, 0.086556}
    };

    //Extra calculated values
    public static double[][] CIEPrimariesD50 = {
        {0.737385, 0.264518, 0.174658},
        {0.266802, 0.718404, 0.824754},
        {0.174329, 0.000599, 0.000588}
    };

    public static double[][] sRGBPrimariesD50 = {
        {0.648431, 0.330856, 0.222491},
        {0.321152, 0.597871, 0.716888},
        {0.155886, 0.066044, 0.060621}
    };

    public static double[][] AdobePrimariesD50 = {
        {0.648431, 0.330856, 0.311114},
        {0.230154, 0.701572, 0.625662},
        {0.155886, 0.066044, 0.063224}
    };

    public static double[][] ApplePrimariesD50 = {
        {0.634756, 0.340596, 0.255166},
        {0.301775, 0.597511, 0.672578},
        {0.162897, 0.079001, 0.072256}
    };

    public static double[][] NTSCPrimariesD50 = {
        {0.671910, 0.329340, 0.310889},
        {0.222591, 0.710647, 0.591737},
        {0.142783, 0.096145, 0.097374}
    };

    public static double[][] PALPrimariesD50 = {
        {0.648431, 0.330856, 0.232289},
        {0.311424, 0.599693, 0.707805},
        {0.155886, 0.066044, 0.059906}
    };

    public static double[][] SMPTECPrimariesD50 = {
        {0.638852, 0.340194, 0.221685},
        {0.331007, 0.592082, 0.703264},
        {0.162897, 0.079001, 0.075052}
    };

    /*
        get/set
     */
    public String getColorSpaceName() {
        return colorSpaceName;
    }

    public void setColorSpaceName(String colorSpaceName) {
        this.colorSpaceName = colorSpaceName;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getIlluminantName() {
        return illuminantName;
    }

    public void setIlluminantName(String illuminantName) {
        this.illuminantName = illuminantName;
    }

    public String getAdaptAlgorithm() {
        return adaptAlgorithm;
    }

    public void setAdaptAlgorithm(String adaptAlgorithm) {
        this.adaptAlgorithm = adaptAlgorithm;
    }

}
