package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.paint.Color;
import mara.mybox.color.SRGB;
import mara.mybox.data.ColorData;
import mara.mybox.db.TableColorData;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlColor {

    public static Map<String, Color> NamedColor;
    public static Map<Color, String> ColorsName;

    public static Map<String, Color> namedColor() {
        if (NamedColor != null) {
            return NamedColor;
        }
        NamedColor = new HashMap<>();
        NamedColor.put("aliceblue", Color.ALICEBLUE);
        NamedColor.put("antiquewhite", Color.ANTIQUEWHITE);
        NamedColor.put("aqua", Color.AQUA);
        NamedColor.put("aquamarine", Color.AQUAMARINE);
        NamedColor.put("azure", Color.AZURE);
        NamedColor.put("beige", Color.BEIGE);
        NamedColor.put("bisque", Color.BISQUE);
        NamedColor.put("black", Color.BLACK);
        NamedColor.put("blanchedalmond", Color.BLANCHEDALMOND);
        NamedColor.put("blue", Color.BLUE);
        NamedColor.put("blueviolet", Color.BLUEVIOLET);
        NamedColor.put("brown", Color.BROWN);
        NamedColor.put("burlywood", Color.BURLYWOOD);
        NamedColor.put("cadetblue", Color.CADETBLUE);
        NamedColor.put("chartreuse", Color.CHARTREUSE);
        NamedColor.put("chocolate", Color.CHOCOLATE);
        NamedColor.put("coral", Color.CORAL);
        NamedColor.put("cornflowerblue", Color.CORNFLOWERBLUE);
        NamedColor.put("cornsilk", Color.CORNSILK);
        NamedColor.put("crimson", Color.CRIMSON);
        NamedColor.put("cyan", Color.CYAN);
        NamedColor.put("darkblue", Color.DARKBLUE);
        NamedColor.put("darkcyan", Color.DARKCYAN);
        NamedColor.put("darkgoldenrod", Color.DARKGOLDENROD);
        NamedColor.put("darkgray", Color.DARKGRAY);
        NamedColor.put("darkgreen", Color.DARKGREEN);
        NamedColor.put("darkgrey", Color.DARKGREY);
        NamedColor.put("darkkhaki", Color.DARKKHAKI);
        NamedColor.put("darkmagenta", Color.DARKMAGENTA);
        NamedColor.put("darkolivegreen", Color.DARKOLIVEGREEN);
        NamedColor.put("darkorange", Color.DARKORANGE);
        NamedColor.put("darkorchid", Color.DARKORCHID);
        NamedColor.put("darkred", Color.DARKRED);
        NamedColor.put("darksalmon", Color.DARKSALMON);
        NamedColor.put("darkseagreen", Color.DARKSEAGREEN);
        NamedColor.put("darkslateblue", Color.DARKSLATEBLUE);
        NamedColor.put("darkslategray", Color.DARKSLATEGRAY);
        NamedColor.put("darkslategrey", Color.DARKSLATEGREY);
        NamedColor.put("darkturquoise", Color.DARKTURQUOISE);
        NamedColor.put("darkviolet", Color.DARKVIOLET);
        NamedColor.put("deeppink", Color.DEEPPINK);
        NamedColor.put("deepskyblue", Color.DEEPSKYBLUE);
        NamedColor.put("dimgray", Color.DIMGRAY);
        NamedColor.put("dimgrey", Color.DIMGREY);
        NamedColor.put("dodgerblue", Color.DODGERBLUE);
        NamedColor.put("firebrick", Color.FIREBRICK);
        NamedColor.put("floralwhite", Color.FLORALWHITE);
        NamedColor.put("forestgreen", Color.FORESTGREEN);
        NamedColor.put("fuchsia", Color.FUCHSIA);
        NamedColor.put("gainsboro", Color.GAINSBORO);
        NamedColor.put("ghostwhite", Color.GHOSTWHITE);
        NamedColor.put("gold", Color.GOLD);
        NamedColor.put("goldenrod", Color.GOLDENROD);
        NamedColor.put("gray", Color.GRAY);
        NamedColor.put("green", Color.GREEN);
        NamedColor.put("greenyellow", Color.GREENYELLOW);
        NamedColor.put("grey", Color.GREY);
        NamedColor.put("honeydew", Color.HONEYDEW);
        NamedColor.put("hotpink", Color.HOTPINK);
        NamedColor.put("indianred", Color.INDIANRED);
        NamedColor.put("indigo", Color.INDIGO);
        NamedColor.put("ivory", Color.IVORY);
        NamedColor.put("khaki", Color.KHAKI);
        NamedColor.put("lavender", Color.LAVENDER);
        NamedColor.put("lavenderblush", Color.LAVENDERBLUSH);
        NamedColor.put("lawngreen", Color.LAWNGREEN);
        NamedColor.put("lemonchiffon", Color.LEMONCHIFFON);
        NamedColor.put("lightblue", Color.LIGHTBLUE);
        NamedColor.put("lightcoral", Color.LIGHTCORAL);
        NamedColor.put("lightcyan", Color.LIGHTCYAN);
        NamedColor.put("lightgoldenrodyellow", Color.LIGHTGOLDENRODYELLOW);
        NamedColor.put("lightgray", Color.LIGHTGRAY);
        NamedColor.put("lightgreen", Color.LIGHTGREEN);
        NamedColor.put("lightgrey", Color.LIGHTGREY);
        NamedColor.put("lightpink", Color.LIGHTPINK);
        NamedColor.put("lightsalmon", Color.LIGHTSALMON);
        NamedColor.put("lightseagreen", Color.LIGHTSEAGREEN);
        NamedColor.put("lightskyblue", Color.LIGHTSKYBLUE);
        NamedColor.put("lightslategray", Color.LIGHTSLATEGRAY);
        NamedColor.put("lightslategrey", Color.LIGHTSLATEGREY);
        NamedColor.put("lightsteelblue", Color.LIGHTSTEELBLUE);
        NamedColor.put("lightyellow", Color.LIGHTYELLOW);
        NamedColor.put("lime", Color.LIME);
        NamedColor.put("limegreen", Color.LIMEGREEN);
        NamedColor.put("linen", Color.LINEN);
        NamedColor.put("magenta", Color.MAGENTA);
        NamedColor.put("maroon", Color.MAROON);
        NamedColor.put("mediumaquamarine", Color.MEDIUMAQUAMARINE);
        NamedColor.put("mediumblue", Color.MEDIUMBLUE);
        NamedColor.put("mediumorchid", Color.MEDIUMORCHID);
        NamedColor.put("mediumpurple", Color.MEDIUMPURPLE);
        NamedColor.put("mediumseagreen", Color.MEDIUMSEAGREEN);
        NamedColor.put("mediumslateblue", Color.MEDIUMSLATEBLUE);
        NamedColor.put("mediumspringgreen", Color.MEDIUMSPRINGGREEN);
        NamedColor.put("mediumturquoise", Color.MEDIUMTURQUOISE);
        NamedColor.put("mediumvioletred", Color.MEDIUMVIOLETRED);
        NamedColor.put("midnightblue", Color.MIDNIGHTBLUE);
        NamedColor.put("mintcream", Color.MINTCREAM);
        NamedColor.put("mistyrose", Color.MISTYROSE);
        NamedColor.put("moccasin", Color.MOCCASIN);
        NamedColor.put("navajowhite", Color.NAVAJOWHITE);
        NamedColor.put("navy", Color.NAVY);
        NamedColor.put("oldlace", Color.OLDLACE);
        NamedColor.put("olive", Color.OLIVE);
        NamedColor.put("olivedrab", Color.OLIVEDRAB);
        NamedColor.put("orange", Color.ORANGE);
        NamedColor.put("orangered", Color.ORANGERED);
        NamedColor.put("orchid", Color.ORCHID);
        NamedColor.put("palegoldenrod", Color.PALEGOLDENROD);
        NamedColor.put("palegreen", Color.PALEGREEN);
        NamedColor.put("paleturquoise", Color.PALETURQUOISE);
        NamedColor.put("palevioletred", Color.PALEVIOLETRED);
        NamedColor.put("papayawhip", Color.PAPAYAWHIP);
        NamedColor.put("peachpuff", Color.PEACHPUFF);
        NamedColor.put("peru", Color.PERU);
        NamedColor.put("pink", Color.PINK);
        NamedColor.put("plum", Color.PLUM);
        NamedColor.put("powderblue", Color.POWDERBLUE);
        NamedColor.put("purple", Color.PURPLE);
        NamedColor.put("red", Color.RED);
        NamedColor.put("rosybrown", Color.ROSYBROWN);
        NamedColor.put("royalblue", Color.ROYALBLUE);
        NamedColor.put("saddlebrown", Color.SADDLEBROWN);
        NamedColor.put("salmon", Color.SALMON);
        NamedColor.put("sandybrown", Color.SANDYBROWN);
        NamedColor.put("seagreen", Color.SEAGREEN);
        NamedColor.put("seashell", Color.SEASHELL);
        NamedColor.put("sienna", Color.SIENNA);
        NamedColor.put("silver", Color.SILVER);
        NamedColor.put("skyblue", Color.SKYBLUE);
        NamedColor.put("slateblue", Color.SLATEBLUE);
        NamedColor.put("slategray", Color.SLATEGRAY);
        NamedColor.put("slategrey", Color.SLATEGREY);
        NamedColor.put("snow", Color.SNOW);
        NamedColor.put("springgreen", Color.SPRINGGREEN);
        NamedColor.put("steelblue", Color.STEELBLUE);
        NamedColor.put("tan", Color.TAN);
        NamedColor.put("teal", Color.TEAL);
        NamedColor.put("thistle", Color.THISTLE);
        NamedColor.put("tomato", Color.TOMATO);
        NamedColor.put("transparent", Color.TRANSPARENT);
        NamedColor.put("turquoise", Color.TURQUOISE);
        NamedColor.put("violet", Color.VIOLET);
        NamedColor.put("wheat", Color.WHEAT);
        NamedColor.put("white", Color.WHITE);
        NamedColor.put("whitesmoke", Color.WHITESMOKE);
        NamedColor.put("yellow", Color.YELLOW);
        NamedColor.put("yellowgreen", Color.YELLOWGREEN);
        return NamedColor;
    }

    public static Map<Color, String> colorsName() {
        if (ColorsName != null) {
            return ColorsName;
        }
        Map<String, Color> named = namedColor();
        ColorsName = new HashMap<>();
        for (String name : named.keySet()) {
            ColorsName.put(named.get(name), name);
        }
        return ColorsName;
    }

    public static List<Color> commonColors() {
        Set<Color> colors = colorsName().keySet();
        List<Color> ordered = new ArrayList<>();
        ordered.addAll(colors);
        List<Color> special = new ArrayList<>();
        special.add(Color.WHITE);
        special.add(Color.BLACK);
        special.add(Color.TRANSPARENT);
        ordered.removeAll(special);
        Collections.sort(ordered, new Comparator<Color>() {
            @Override
            public int compare(Color o1, Color o2) {
                return compareColor(o1, o2);
            }
        });
        ordered.addAll(special);
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

    public static String colorNameDisplay(Color color) {
        if (color == null) {
            return "";
        }
        ColorData data = TableColorData.read(color);
        if (data != null) {
            return data.display();
        }
        data = new ColorData(color.toString());
        return data.display();
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
