package mara.mybox.color;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Description
 * @License Apache License Version 2.0
 *
 * Reference http://brucelindbloom.com/index.html?Eqn_RGB_to_XYZ.html //
 * http://www.easyrgb.com/en/math.php //
 * http://www.color.org/registry/index.xalter //
 * https://www.w3.org/Graphics/Color/sRGB.html //
 * https://ninedegreesbelow.com/photography/xyz-rgb.html //
 * https://stackoverflow.com/questions/40017741/mathematical-conversion-srgb-and-adobergb
 * https://supportdownloads.adobe.com/detail.jsp?ftpID=3680 //
 */
public class ColorBase {


    public static int colorSpaceType(String colorSpaceType) {
        int csType = -1;
        if (colorSpaceType != null) {
            switch (colorSpaceType) {
                case "sRGB":
                    csType = ICC_ColorSpace.CS_sRGB;
                    break;
                case "XYZ":
                    csType = ICC_ColorSpace.CS_CIEXYZ;
                    break;
                case "PYCC":
                    csType = ICC_ColorSpace.CS_PYCC;
                    break;
                case "GRAY":
                    csType = ICC_ColorSpace.CS_GRAY;
                    break;
                case "LINEAR_RGB":
                    csType = ICC_ColorSpace.CS_LINEAR_RGB;
                    break;
            }
        }
        return csType;
    }

    /*
        Methods based on JDK
     */
    public static String colorSpaceType(int colorType) {
        switch (colorType) {
            case ColorSpace.TYPE_XYZ:
                return "XYZ";
            case ColorSpace.TYPE_Lab:
                return "Lab";
            case ColorSpace.TYPE_Luv:
                return "Luv";
            case ColorSpace.TYPE_YCbCr:
                return "YCbCr";
            case ColorSpace.TYPE_Yxy:
                return "Yxy";
            case ColorSpace.TYPE_RGB:
                return "RGB";
            case ColorSpace.TYPE_GRAY:
                return "GRAY";
            case ColorSpace.TYPE_HSV:
                return "HSV";
            case ColorSpace.TYPE_HLS:
                return "HLS";
            case ColorSpace.TYPE_CMYK:
                return "CMYK";
            case ColorSpace.TYPE_CMY:
                return "CMY";
            case ColorSpace.TYPE_2CLR:
                return "2CLR";
            case ColorSpace.TYPE_3CLR:
                return "3CLR";
            case ColorSpace.TYPE_4CLR:
                return "4CLR";
            case ColorSpace.TYPE_5CLR:
                return "5CLR";
            case ColorSpace.TYPE_6CLR:
                return "6CLR";
            case ColorSpace.TYPE_7CLR:
                return "CMY";
            case ColorSpace.TYPE_8CLR:
                return "8CLR";
            case ColorSpace.TYPE_9CLR:
                return "9CLR";
            case ColorSpace.TYPE_ACLR:
                return "ACLR";
            case ColorSpace.TYPE_BCLR:
                return "BCLR";
            case ColorSpace.TYPE_CCLR:
                return "CCLR";
            case ColorSpace.TYPE_DCLR:
                return "DCLR";
            case ColorSpace.TYPE_ECLR:
                return "ECLR";
            case ColorSpace.TYPE_FCLR:
                return "FCLR";
            case ColorSpace.CS_sRGB:
                return "sRGB";
            case ColorSpace.CS_LINEAR_RGB:
                return "LINEAR_RGB";
            case ColorSpace.CS_CIEXYZ:
                return "CIEXYZ";
            case ColorSpace.CS_PYCC:
                return "PYCC";
            case ColorSpace.CS_GRAY:
                return "GRAY";
            default:
                return "Unknown";

        }

    }

    public static String profileClass(int value) {
        switch (value) {
            case ICC_Profile.CLASS_INPUT:
                return "InputDeviceProfile";
            case ICC_Profile.CLASS_DISPLAY:
                return "DisplayDeviceProfile";
            case ICC_Profile.CLASS_OUTPUT:
                return "OutputDeviceProfile";
            case ICC_Profile.CLASS_DEVICELINK:
                return "DeviceLinkProfile";
            case ICC_Profile.CLASS_ABSTRACT:
                return "AbstractProfile";
            case ICC_Profile.CLASS_COLORSPACECONVERSION:
                return "ColorSpaceConversionProfile";
            case ICC_Profile.CLASS_NAMEDCOLOR:
                return "NamedColorProfile";
            default:
                return "Unknown";
        }
    }

    public static String profileClassSignature(int value) {
        switch (value) {
            case ICC_Profile.icSigInputClass:
                return "InputDeviceProfile";
            case ICC_Profile.icSigDisplayClass:
                return "DisplayDeviceProfile";
            case ICC_Profile.icSigOutputClass:
                return "OutputDeviceProfile";
            case ICC_Profile.icSigLinkClass:
                return "DeviceLinkProfile";
            case ICC_Profile.icSigAbstractClass:
                return "AbstractProfile";
            case ICC_Profile.icSigColorSpaceClass:
                return "ColorSpaceConversionProfile";
            case ICC_Profile.icSigNamedColorClass:
                return "NamedColorProfile";
            default:
                return "Unknown";
        }
    }

    public static double[] clipRGB(double[] rgb) {
        double[] outputs = new double[rgb.length];
        System.arraycopy(rgb, 0, outputs, 0, rgb.length);
//        for (int i = 0; i < outputs.length; ++i) {
//            if (outputs[i] < 0) {
//                for (int j = 0; j < outputs.length; ++j) {
//                    outputs[j] = outputs[j] - outputs[i];
//                }
//            }
//        }
        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] < 0) {
                outputs[i] = 0;
            }
            if (outputs[i] > 1) {
                outputs[i] = 1;
            }
        }
        return outputs;
    }

    public static double[] array(Color color) {
        double[] rgb = new double[3];
        rgb[0] = color.getRed() / 255d;
        rgb[1] = color.getGreen() / 255d;
        rgb[2] = color.getBlue() / 255d;
        return rgb;
    }

    public static float[] array(float r, float g, float b) {
        float[] rgb = new float[3];
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
        return rgb;
    }

    public static double[] array(double r, double g, double b) {
        double[] rgb = new double[3];
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
        return rgb;
    }

    public static float[] arrayFloat(double r, double g, double b) {
        float[] rgb = new float[3];
        rgb[0] = (float) r;
        rgb[1] = (float) g;
        rgb[2] = (float) b;
        return rgb;
    }

    public static float[] arrayFloat(double[] d) {
        float[] rgb = new float[3];
        rgb[0] = (float) d[0];
        rgb[1] = (float) d[1];
        rgb[2] = (float) d[2];
        return rgb;
    }

    public static double[] arrayDouble(float r, float g, float b) {
        double[] rgb = new double[3];
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
        return rgb;
    }

    public static double[] arrayDouble(float[] frgb) {
        double[] rgb = new double[3];
        rgb[0] = frgb[0];
        rgb[1] = frgb[1];
        rgb[2] = frgb[2];
        return rgb;
    }

}
