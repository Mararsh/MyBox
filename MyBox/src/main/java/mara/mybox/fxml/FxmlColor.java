package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.paint.Color;
import mara.mybox.color.AdobeRGB;
import mara.mybox.color.AppleRGB;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.SRGB;
import mara.mybox.db.TableSRGB;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlColor {

    public static Map<String, Color> namedColor;
    public static Map<Color, String> colorsName;

    public static Map<String, Color> namedColor() {
        if (namedColor != null) {
            return namedColor;
        }
        Map<String, Color> named = new HashMap<>();
        named.put("aliceblue", Color.ALICEBLUE);
        named.put("antiquewhite", Color.ANTIQUEWHITE);
        named.put("aqua", Color.AQUA);
        named.put("aquamarine", Color.AQUAMARINE);
        named.put("azure", Color.AZURE);
        named.put("beige", Color.BEIGE);
        named.put("bisque", Color.BISQUE);
        named.put("black", Color.BLACK);
        named.put("blanchedalmond", Color.BLANCHEDALMOND);
        named.put("blue", Color.BLUE);
        named.put("blueviolet", Color.BLUEVIOLET);
        named.put("brown", Color.BROWN);
        named.put("burlywood", Color.BURLYWOOD);
        named.put("cadetblue", Color.CADETBLUE);
        named.put("chartreuse", Color.CHARTREUSE);
        named.put("chocolate", Color.CHOCOLATE);
        named.put("coral", Color.CORAL);
        named.put("cornflowerblue", Color.CORNFLOWERBLUE);
        named.put("cornsilk", Color.CORNSILK);
        named.put("crimson", Color.CRIMSON);
        named.put("cyan", Color.CYAN);
        named.put("darkblue", Color.DARKBLUE);
        named.put("darkcyan", Color.DARKCYAN);
        named.put("darkgoldenrod", Color.DARKGOLDENROD);
        named.put("darkgray", Color.DARKGRAY);
        named.put("darkgreen", Color.DARKGREEN);
        named.put("darkgrey", Color.DARKGREY);
        named.put("darkkhaki", Color.DARKKHAKI);
        named.put("darkmagenta", Color.DARKMAGENTA);
        named.put("darkolivegreen", Color.DARKOLIVEGREEN);
        named.put("darkorange", Color.DARKORANGE);
        named.put("darkorchid", Color.DARKORCHID);
        named.put("darkred", Color.DARKRED);
        named.put("darksalmon", Color.DARKSALMON);
        named.put("darkseagreen", Color.DARKSEAGREEN);
        named.put("darkslateblue", Color.DARKSLATEBLUE);
        named.put("darkslategray", Color.DARKSLATEGRAY);
        named.put("darkslategrey", Color.DARKSLATEGREY);
        named.put("darkturquoise", Color.DARKTURQUOISE);
        named.put("darkviolet", Color.DARKVIOLET);
        named.put("deeppink", Color.DEEPPINK);
        named.put("deepskyblue", Color.DEEPSKYBLUE);
        named.put("dimgray", Color.DIMGRAY);
        named.put("dimgrey", Color.DIMGREY);
        named.put("dodgerblue", Color.DODGERBLUE);
        named.put("firebrick", Color.FIREBRICK);
        named.put("floralwhite", Color.FLORALWHITE);
        named.put("forestgreen", Color.FORESTGREEN);
        named.put("fuchsia", Color.FUCHSIA);
        named.put("gainsboro", Color.GAINSBORO);
        named.put("ghostwhite", Color.GHOSTWHITE);
        named.put("gold", Color.GOLD);
        named.put("goldenrod", Color.GOLDENROD);
        named.put("gray", Color.GRAY);
        named.put("green", Color.GREEN);
        named.put("greenyellow", Color.GREENYELLOW);
        named.put("grey", Color.GREY);
        named.put("honeydew", Color.HONEYDEW);
        named.put("hotpink", Color.HOTPINK);
        named.put("indianred", Color.INDIANRED);
        named.put("indigo", Color.INDIGO);
        named.put("ivory", Color.IVORY);
        named.put("khaki", Color.KHAKI);
        named.put("lavender", Color.LAVENDER);
        named.put("lavenderblush", Color.LAVENDERBLUSH);
        named.put("lawngreen", Color.LAWNGREEN);
        named.put("lemonchiffon", Color.LEMONCHIFFON);
        named.put("lightblue", Color.LIGHTBLUE);
        named.put("lightcoral", Color.LIGHTCORAL);
        named.put("lightcyan", Color.LIGHTCYAN);
        named.put("lightgoldenrodyellow", Color.LIGHTGOLDENRODYELLOW);
        named.put("lightgray", Color.LIGHTGRAY);
        named.put("lightgreen", Color.LIGHTGREEN);
        named.put("lightgrey", Color.LIGHTGREY);
        named.put("lightpink", Color.LIGHTPINK);
        named.put("lightsalmon", Color.LIGHTSALMON);
        named.put("lightseagreen", Color.LIGHTSEAGREEN);
        named.put("lightskyblue", Color.LIGHTSKYBLUE);
        named.put("lightslategray", Color.LIGHTSLATEGRAY);
        named.put("lightslategrey", Color.LIGHTSLATEGREY);
        named.put("lightsteelblue", Color.LIGHTSTEELBLUE);
        named.put("lightyellow", Color.LIGHTYELLOW);
        named.put("lime", Color.LIME);
        named.put("limegreen", Color.LIMEGREEN);
        named.put("linen", Color.LINEN);
        named.put("magenta", Color.MAGENTA);
        named.put("maroon", Color.MAROON);
        named.put("mediumaquamarine", Color.MEDIUMAQUAMARINE);
        named.put("mediumblue", Color.MEDIUMBLUE);
        named.put("mediumorchid", Color.MEDIUMORCHID);
        named.put("mediumpurple", Color.MEDIUMPURPLE);
        named.put("mediumseagreen", Color.MEDIUMSEAGREEN);
        named.put("mediumslateblue", Color.MEDIUMSLATEBLUE);
        named.put("mediumspringgreen", Color.MEDIUMSPRINGGREEN);
        named.put("mediumturquoise", Color.MEDIUMTURQUOISE);
        named.put("mediumvioletred", Color.MEDIUMVIOLETRED);
        named.put("midnightblue", Color.MIDNIGHTBLUE);
        named.put("mintcream", Color.MINTCREAM);
        named.put("mistyrose", Color.MISTYROSE);
        named.put("moccasin", Color.MOCCASIN);
        named.put("navajowhite", Color.NAVAJOWHITE);
        named.put("navy", Color.NAVY);
        named.put("oldlace", Color.OLDLACE);
        named.put("olive", Color.OLIVE);
        named.put("olivedrab", Color.OLIVEDRAB);
        named.put("orange", Color.ORANGE);
        named.put("orangered", Color.ORANGERED);
        named.put("orchid", Color.ORCHID);
        named.put("palegoldenrod", Color.PALEGOLDENROD);
        named.put("palegreen", Color.PALEGREEN);
        named.put("paleturquoise", Color.PALETURQUOISE);
        named.put("palevioletred", Color.PALEVIOLETRED);
        named.put("papayawhip", Color.PAPAYAWHIP);
        named.put("peachpuff", Color.PEACHPUFF);
        named.put("peru", Color.PERU);
        named.put("pink", Color.PINK);
        named.put("plum", Color.PLUM);
        named.put("powderblue", Color.POWDERBLUE);
        named.put("purple", Color.PURPLE);
        named.put("red", Color.RED);
        named.put("rosybrown", Color.ROSYBROWN);
        named.put("royalblue", Color.ROYALBLUE);
        named.put("saddlebrown", Color.SADDLEBROWN);
        named.put("salmon", Color.SALMON);
        named.put("sandybrown", Color.SANDYBROWN);
        named.put("seagreen", Color.SEAGREEN);
        named.put("seashell", Color.SEASHELL);
        named.put("sienna", Color.SIENNA);
        named.put("silver", Color.SILVER);
        named.put("skyblue", Color.SKYBLUE);
        named.put("slateblue", Color.SLATEBLUE);
        named.put("slategray", Color.SLATEGRAY);
        named.put("slategrey", Color.SLATEGREY);
        named.put("snow", Color.SNOW);
        named.put("springgreen", Color.SPRINGGREEN);
        named.put("steelblue", Color.STEELBLUE);
        named.put("tan", Color.TAN);
        named.put("teal", Color.TEAL);
        named.put("thistle", Color.THISTLE);
        named.put("tomato", Color.TOMATO);
        named.put("transparent", Color.TRANSPARENT);
        named.put("turquoise", Color.TURQUOISE);
        named.put("violet", Color.VIOLET);
        named.put("wheat", Color.WHEAT);
        named.put("white", Color.WHITE);
        named.put("whitesmoke", Color.WHITESMOKE);
        named.put("yellow", Color.YELLOW);
        named.put("yellowgreen", Color.YELLOWGREEN);
        return named;
    }

    public static Map<Color, String> colorsName() {
        if (colorsName != null) {
            return colorsName;
        }
        Map<String, Color> named = namedColor();
        Map<Color, String> colors = new HashMap<>();
        for (String name : named.keySet()) {
            colors.put(named.get(name), name);
        }
        return colors;
    }

    public static List<Color> commonColors() {
        Set<Color> colors = colorsName().keySet();
        List<Color> ordered = new ArrayList<>();
        ordered.addAll(colors);
        Collections.sort(ordered, new Comparator<Color>() {
            @Override
            public int compare(Color o1, Color o2) {
                return compareColor(o1, o2);
            }
        });
        return ordered;
    }

    public static int compareColor(Color o1, Color o2) {
        double diff = o2.getHue() - o1.getHue();
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        } else {
            diff = o2.getSaturation() - o1.getSaturation();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                diff = o2.getBrightness() - o1.getBrightness();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    diff = o2.getOpacity() - o1.getOpacity();
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    public static String colorName(String color) {
        return colorName(Color.web(color));
    }

    public static String colorName(Color color) {
        SRGB srgb = TableSRGB.read(color.toString());
        if (srgb != null && srgb.getColorName() != null) {
            return srgb.getColorName();
        }
        String name = colorsName().get(color);
        if (name != null) {
            TableSRGB.name(color.toString(), name);
        }
        return name;
    }

    public static String colorNameDisplay(Color color) {
        if (color == null) {
            return "";
        }
        String s = "";
        String name = colorName(color);
        if (name != null) {
            s = name + "\n";
        }
        return s + colorDisplay(color);
    }

    public static String colorDisplay(Color color) {
        if (color == null) {
            return "";
        }
        SRGB srgb = TableSRGB.read(color.toString());
        if (srgb != null && srgb.getColorDisplay() != null) {
            return srgb.getColorDisplay();
        }
        String s = color.toString() + "\n";
        s += "sRGB  " + message("Red") + ":" + Math.round(color.getRed() * 255) + " "
                + message("Green") + ":" + Math.round(color.getGreen() * 255) + " "
                + message("Blue") + ":" + Math.round(color.getBlue() * 255)
                + message("Opacity") + ":" + Math.round(color.getOpacity() * 100) + "%\n";
        s += "HSB  " + message("Hue") + ":" + Math.round(color.getHue()) + " "
                + message("Saturation") + ":" + Math.round(color.getSaturation() * 100) + "% "
                + message("Brightness") + ":" + Math.round(color.getBrightness() * 100) + "%\n";

        float[] adobeRGB = SRGB.srgb2profile(ImageValue.adobeRGBProfile(), color);
        s += "Adobe RGB:  " + Math.round(adobeRGB[0] * 255) + " " + Math.round(adobeRGB[1] * 255) + " " + Math.round(adobeRGB[2] * 255) + " \n";

        float[] appleRGB = SRGB.srgb2profile(ImageValue.appleRGBProfile(), color);
        s += "Apple RGB:  " + Math.round(appleRGB[0] * 255) + " " + Math.round(appleRGB[1] * 255) + " " + Math.round(appleRGB[2] * 255) + " \n";

        float[] eciRGB = SRGB.srgb2profile(ImageValue.eciRGBProfile(), color);
        s += "ECI RGB:  " + Math.round(eciRGB[0] * 255) + " " + Math.round(eciRGB[1] * 255) + " " + Math.round(eciRGB[2] * 255) + " \n";

        s += "sRGB Linear:  "
                + Math.round(RGBColorSpace.linearSRGB(color.getRed()) * 255) + " "
                + Math.round(RGBColorSpace.linearSRGB(color.getGreen()) * 255) + " "
                + Math.round(RGBColorSpace.linearSRGB(color.getBlue()) * 255) + " " + " \n";

        s += "Adobe RGB Linear:  " + Math.round(AdobeRGB.linearAdobeRGB(adobeRGB[0]) * 255) + " "
                + Math.round(AdobeRGB.linearAdobeRGB(adobeRGB[1]) * 255) + " "
                + Math.round(AdobeRGB.linearAdobeRGB(adobeRGB[2]) * 255) + " \n";

        s += "Apple RGB Linear:  " + Math.round(AppleRGB.linearAppleRGB(appleRGB[0]) * 255) + " "
                + Math.round(AppleRGB.linearAppleRGB(appleRGB[1]) * 255) + " "
                + Math.round(AppleRGB.linearAppleRGB(appleRGB[2]) * 255) + " \n";

        double[] cmyk = SRGB.rgb2cmyk(color);
        s += "Calculated CMYK:  " + Math.round(cmyk[0] * 100) + " " + Math.round(cmyk[1] * 100) + " "
                + Math.round(cmyk[2] * 100) + " " + Math.round(cmyk[3] * 100) + " \n";

        float[] cmyk2 = SRGB.srgb2profile(ImageValue.eciCmykProfile(), color);
        s += "ECI CMYK:  " + Math.round(cmyk2[0] * 100) + " " + Math.round(cmyk2[1] * 100) + " "
                + Math.round(cmyk2[2] * 100) + " " + Math.round(cmyk2[3] * 100) + " \n";

        cmyk2 = SRGB.srgb2profile(ImageValue.adobeCmykProfile(), color);
        s += "Adobe CMYK Uncoated FOGRA29:  " + Math.round(cmyk2[0] * 100) + " " + Math.round(cmyk2[1] * 100) + " "
                + Math.round(cmyk2[2] * 100) + " " + Math.round(cmyk2[3] * 100) + " \n";

        double[] xyz = SRGB.toXYZd50(ImageColor.converColor(color));
        s += "XYZ: " + DoubleTools.scale(xyz[0], 6) + " "
                + DoubleTools.scale(xyz[1], 6) + " "
                + DoubleTools.scale(xyz[2], 6) + "\n";
        double[] cieLab = CIEColorSpace.XYZd50toCIELab(xyz[0], xyz[1], xyz[2]);
        s += "CIE-L*ab:  " + DoubleTools.scale(cieLab[0], 2) + " " + DoubleTools.scale(cieLab[1], 2) + " " + DoubleTools.scale(cieLab[2], 2) + " \n";
        double[] LCHab = CIEColorSpace.LabtoLCHab(cieLab);
        s += "LCH(ab):  " + DoubleTools.scale(LCHab[0], 2) + " " + DoubleTools.scale(LCHab[1], 2) + " " + DoubleTools.scale(LCHab[2], 2) + " \n";
        double[] cieLuv = CIEColorSpace.XYZd50toCIELuv(xyz[0], xyz[1], xyz[2]);
        s += "CIE-L*uv:  " + DoubleTools.scale(cieLuv[0], 2) + " " + DoubleTools.scale(cieLuv[1], 2) + " " + DoubleTools.scale(cieLuv[2], 2) + " \n";
        double[] LCHuv = CIEColorSpace.LuvtoLCHuv(cieLuv);
        s += "LCH(uv):  " + DoubleTools.scale(LCHuv[0], 2) + " " + DoubleTools.scale(LCHuv[1], 2) + " " + DoubleTools.scale(LCHuv[2], 2) + " \n";

        TableSRGB.display(color.toString(), s);

        return s;
    }

    public static String colorDisplaySimple(Color color) {
        if (color == null) {
            return "";
        }
        String s = color.toString() + "\n";
        s += "sRGB  " + message("Red") + ":" + Math.round(color.getRed() * 255) + " "
                + message("Green") + ":" + Math.round(color.getGreen() * 255) + " "
                + message("Blue") + ":" + Math.round(color.getBlue() * 255)
                + message("Opacity") + ":" + Math.round(color.getOpacity() * 100) + "%\n";
        s += "HSB  " + message("Hue") + ":" + Math.round(color.getHue()) + " "
                + message("Saturation") + ":" + Math.round(color.getSaturation() * 100) + "% "
                + message("Brightness") + ":" + Math.round(color.getBrightness() * 100) + "%\n";

        return s;
    }

    public static String rgb2Hex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static String rgb2AlphaHex(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getOpacity() * 255),
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static String rgb2css(Color color) {
        return "rgba(" + (int) (color.getRed() * 255) + ","
                + (int) (color.getGreen() * 255) + ","
                + (int) (color.getBlue() * 255) + ","
                + color.getOpacity() + ")";
    }

    public static String rgb2Hex(java.awt.Color color) {
        return String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String rgb2AlphaHex(java.awt.Color color) {
        return String.format("#%02X%02X%02X%02X",
                color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static float[] toFloat(Color color) {
        float[] srgb = new float[3];
        srgb[0] = (float) color.getRed();
        srgb[1] = (float) color.getGreen();
        srgb[2] = (float) color.getBlue();
        return srgb;
    }

    public static double[] toDouble(Color color) {
        double[] srgb = new double[3];
        srgb[0] = color.getRed();
        srgb[1] = color.getGreen();
        srgb[2] = color.getBlue();
        return srgb;
    }

    public static double[] SRGBtoAdobeRGB(Color color) {
        return SRGB.SRGBtoAdobeRGB(toDouble(color));
    }

    public static double[] SRGBtoAppleRGB(Color color) {
        return SRGB.SRGBtoAppleRGB(toDouble(color));
    }

}
