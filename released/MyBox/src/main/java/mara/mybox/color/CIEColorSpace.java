package mara.mybox.color;

import java.util.ArrayList;
import java.util.List;
import static mara.mybox.color.AdobeRGB.gammaAdobeRGB;
import static mara.mybox.color.AppleRGB.gammaAppleRGB;
import static mara.mybox.color.ColorBase.clipRGB;
import static mara.mybox.color.SRGB.gammaSRGB;
import mara.mybox.tools.DoubleMatrixTools;

/**
 * @Author Mara
 * @CreateDate 2019-5-24 8:44:35
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class CIEColorSpace {

    public static enum ColorSpaceType {
        CIEXYZ, CIExyY, CIELuv, CIELab, LCHab, LCHuv
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (ColorSpaceType cs : ColorSpaceType.values()) {
            names.add(cs + "");
        }
        return names;
    }

    public static List<ColorValue> XYZd50toAll(double[] xyzD50) {
        List<ColorValue> colors = new ArrayList<>();

        return colors;
    }

    public static double[] XYZd50toCIERGB(double[] xyz) {
        double[][] matrix = {
            {2.3706743, -0.9000405, -0.4706338},
            {-0.5138850, 1.4253036, 0.0885814},
            {0.0052982, -0.0146949, 1.0093968}
        };
        double[] cieRGB = DoubleMatrixTools.multiply(matrix, xyz);
        return cieRGB;
    }

    public static double[] XYZd50toCIELab(double X, double Y, double Z) {
        return XYZtoCIELab(0.96422, 1, 0.82521, X, Y, Z);
    }

    // http://brucelindbloom.com/index.html?Eqn_XYZ_to_Lab.html
    public static double[] XYZtoCIELab(
            double rX, double rY, double rZ,
            double X, double Y, double Z) {
        double xr = X / rX;
        double yr = Y / rY;
        double zr = Z / rZ;
        if (xr > 0.008856) {
            xr = Math.pow(xr, 1d / 3);
        } else {
            xr = (903.3 * xr + 16) / 116d;
        }
        if (yr > 0.008856) {
            yr = Math.pow(yr, 1d / 3);
        } else {
            yr = (903.3 * yr + 16) / 116d;
        }
        if (zr > 0.008856) {
            zr = Math.pow(zr, 1d / 3);
        } else {
            zr = (903.3 * zr + 16) / 116d;
        }
        double[] CIELab = new double[3];
        CIELab[0] = 116 * yr - 16;
        CIELab[1] = 500 * (xr - yr);
        CIELab[2] = 200 * (yr - zr);
        return CIELab;
    }

    public static double[] XYZd50toCIELuv(double X, double Y, double Z) {
        return XYZtoCIELuv(0.96422, 1, 0.82521, X, Y, Z);
    }

    // http://brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
    public static double[] XYZtoCIELuv(
            double rX, double rY, double rZ,
            double X, double Y, double Z) {
        if (X == 0 && Y == 0 && Z == 0) {
            return new double[3];
        }
        double yr = Y / rY;
        double u = (4 * X) / (X + 15 * Y + 3 * Z);
        double v = (9 * Y) / (X + 15 * Y + 3 * Z);
        double ru = (4 * rX) / (rX + 15 * rY + 3 * rZ);
        double rv = (9 * rY) / (rX + 15 * rY + 3 * rZ);
        double[] CIELuv = new double[3];
        if (yr > 0.008856) {
            CIELuv[0] = 116 * Math.pow(yr, 1d / 3) - 16;
        } else {
            CIELuv[0] = 903.3 * yr;
        }
        CIELuv[1] = 13 * CIELuv[0] * (u - ru);
        CIELuv[2] = 13 * CIELuv[0] * (v - rv);
        return CIELuv;
    }

    public static double[] LabtoLCHab(double[] Lab) {
        return LabtoLCHab(Lab[0], Lab[1], Lab[2]);
    }

    public static double[] LabtoLCHab(double L, double a, double b) {
        double[] LCH = new double[3];
        LCH[0] = L;
        LCH[1] = Math.sqrt(a * a + b * b);
        double d = Math.atan2(b, a) * 180 / Math.PI;
        if (d >= 0) {
            LCH[2] = d;
        } else {
            LCH[2] = d + 360;
        }
        return LCH;
    }

    public static double[] LuvtoLCHuv(double[] Luv) {
        return LuvtoLCHuv(Luv[0], Luv[1], Luv[2]);
    }

    public static double[] LuvtoLCHuv(double L, double u, double v) {
        double[] LCH = new double[3];
        LCH[0] = L;
        LCH[1] = Math.sqrt(u * u + v * v);
        double d = Math.atan2(v, u) * 180 / Math.PI;
        if (d >= 0) {
            LCH[2] = d;
        } else {
            LCH[2] = d + 360;
        }
        return LCH;
    }

    /*
        sRGB
     */
    public static double[] XYZd50toSRGBd65(double X, double Y, double Z) {
        return XYZd50toSRGBd65(ColorBase.array(X, Y, Z));
    }

    public static double[] XYZd50toSRGBd65(double[] xyzD50) {
        double[] xyzD65 = ChromaticAdaptation.D50toD65(xyzD50);
        return XYZd65toSRGBd65(xyzD65);
    }

    public static double[] XYZd50toSRGBd65Linear(double X, double Y, double Z) {
        return XYZd50toSRGBd65Linear(ColorBase.array(X, Y, Z));
    }

    public static double[] XYZd50toSRGBd65Linear(double[] xyzD50) {
        double[] xyzD65 = ChromaticAdaptation.D50toD65(xyzD50);
        return XYZd65toSRGBd65Linear(xyzD65);
    }

    public static double[] XYZd50toSRGBd50(double[] xyz) {
        double[][] matrix = {
            {3.1338561, -1.6168667, -0.4906146},
            {-0.9787684, 1.9161415, 0.0334540},
            {0.0719453, -0.2289914, 1.4052427}
        };
        double[] linearRGB = DoubleMatrixTools.multiply(matrix, xyz);
        linearRGB = clipRGB(linearRGB);

        double[] srgb = gammaSRGB(linearRGB);
        return srgb;
    }

    public static double[] XYZd65toSRGBd65(double[] xyzd65) {
        double[] linearRGB = XYZd65toSRGBd65Linear(xyzd65);
        double[] srgb = gammaSRGB(linearRGB);
        return srgb;
    }

    public static double[] XYZd65toSRGBd65Linear(double[] xyzd65) {
        double[][] matrix = {
            {3.2409699419045235, -1.5373831775700944, -0.49861076029300355},
            {-0.9692436362808797, 1.8759675015077204, 0.0415550574071756},
            {0.05563007969699365, -0.20397695888897652, 1.0569715142428786}
        };
        double[] linearRGB = DoubleMatrixTools.multiply(matrix, xyzd65);
        linearRGB = clipRGB(linearRGB);
        return linearRGB;
    }

    /*
        Adobe RGB
     */
    public static double[] XYZd50toAdobeRGBd65(double X, double Y, double Z) {
        return XYZd50toAdobeRGBd65(ColorBase.array(X, Y, Z));
    }

    public static double[] XYZd50toAdobeRGBd65(double[] xyzD50) {
        double[] xyzD65 = ChromaticAdaptation.D50toD65(xyzD50);
        return XYZd65toAdobeRGBd65(xyzD65);
    }

    public static double[] XYZd50toAdobeRGBd65Linear(double X, double Y, double Z) {
        return XYZd50toAdobeRGBd65Linear(ColorBase.array(X, Y, Z));
    }

    public static double[] XYZd50toAdobeRGBd65Linear(double[] xyzD50) {
        double[] xyzD65 = ChromaticAdaptation.D50toD65(xyzD50);
        return XYZd65toAdobeRGBd65Linear(xyzD65);
    }

    public static double[] XYZd65toAdobeRGBd65(double[] xyzd65) {
        double[] linearRGB = XYZd65toAdobeRGBd65Linear(xyzd65);
        double[] rgb = gammaAdobeRGB(linearRGB);
        return rgb;
    }

    public static double[] XYZd65toAdobeRGBd65Linear(double[] xyzd65) {
        double[][] matrix = {
            {2.0413690, -0.5649464, -0.3446944},
            {-0.9692660, 1.8760108, 0.0415560},
            {0.0134474, -0.1183897, 1.0154096}
        };
        double[] linearRGB = DoubleMatrixTools.multiply(matrix, xyzd65);
        linearRGB = clipRGB(linearRGB);
        return linearRGB;
    }

    public static double[] XYZd50toAdobeRGBd50(double[] xyzD50) {
        double[][] matrix = {
            {1.9624274, -0.6105343, -0.3413404},
            {-0.9787684, 1.9161415, 0.0334540},
            {0.0286869, -0.1406752, 1.3487655}
        };
        double[] linearRGB = DoubleMatrixTools.multiply(matrix, xyzD50);
        linearRGB = clipRGB(linearRGB);

        double[] rgb = gammaAdobeRGB(linearRGB);
        return rgb;
    }

    /*
        Apple RGB
     */
    public static double[] XYZd50toAppleRGBd65(double X, double Y, double Z) {
        return XYZd50toAppleRGBd65(ColorBase.array(X, Y, Z));
    }

    public static double[] XYZd50toAppleRGBd65(double[] xyzD50) {
        double[] xyzD65 = ChromaticAdaptation.D50toD65(xyzD50);
        return XYZd65toAppleRGBd65(xyzD65);
    }

    public static double[] XYZd50toAppleRGBd65Linear(double X, double Y, double Z) {
        return XYZd50toAppleRGBd65Linear(ColorBase.array(X, Y, Z));
    }

    public static double[] XYZd50toAppleRGBd65Linear(double[] xyzD50) {
        double[] xyzD65 = ChromaticAdaptation.D50toD65(xyzD50);
        return XYZd65toAppleRGBd65Linear(xyzD65);
    }

    public static double[] XYZd65toAppleRGBd65(double[] xyzd65) {
        double[] linearRGB = XYZd65toAppleRGBd65Linear(xyzd65);
        double[] rgb = gammaAppleRGB(linearRGB);
        return rgb;
    }

    public static double[] XYZd65toAppleRGBd65Linear(double[] xyzd65) {
        double[][] matrix = {
            {2.9515373, -1.2894116, -0.4738445},
            {-1.0851093, 1.9908566, 0.0372026},
            {0.0854934, -0.2694964, 1.09129756}
        };
        double[] linearRGB = DoubleMatrixTools.multiply(matrix, xyzd65);
        linearRGB = clipRGB(linearRGB);
        return linearRGB;
    }

    public static double[] XYZd50toAppleRGBd50(double[] xyzD50) {
        double[][] matrix = {
            {2.8510695, -1.3605261, -0.4708281},
            {-1.0927680, 2.0348871, 0.0227598},
            {0.1027403, -0.2964984, 1.4510659}
        };
        double[] linearRGB = DoubleMatrixTools.multiply(matrix, xyzD50);
        linearRGB = clipRGB(linearRGB);

        double[] rgb = gammaAppleRGB(linearRGB);
        return rgb;
    }

}
