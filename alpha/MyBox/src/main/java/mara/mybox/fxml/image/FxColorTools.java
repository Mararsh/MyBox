package mara.mybox.fxml.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.paint.Color;
import mara.mybox.color.SRGB;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableColor;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class FxColorTools {

    public static Map<String, Color> WebColors, ChineseColors, JapaneseColors;
    public static Map<Color, String> WebColorNames, ChineseColorNames, JapaneseColorNames;

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

    public static String colorNameDisplay(TableColor tableColor, Color color) {
        if (color == null) {
            return "";
        }
        ColorData data = tableColor.read(color);
        if (data != null) {
            return data.display();
        }
        data = new ColorData(FxColorTools.color2rgba(color));
        return data.display();
    }

    public static String colorDisplaySimple(Color color) {
        if (color == null) {
            return "";
        }
        String s = FxColorTools.color2rgba(color) + "\n";
        s += "sRGB  " + Languages.message("Red") + ":" + Math.round(color.getRed() * 255) + " "
                + Languages.message("Green") + ":" + Math.round(color.getGreen() * 255) + " "
                + Languages.message("Blue") + ":" + Math.round(color.getBlue() * 255)
                + Languages.message("Opacity") + ":" + Math.round(color.getOpacity() * 100) + "%\n";
        s += "HSB  " + Languages.message("Hue") + ":" + Math.round(color.getHue()) + " "
                + Languages.message("Saturation") + ":" + Math.round(color.getSaturation() * 100) + "% "
                + Languages.message("Brightness") + ":" + Math.round(color.getBrightness() * 100) + "%\n";

        return s;
    }

    public static String color2rgb(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static String color2RGBA(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
    }

    public static String color2css(Color color) {
        return "rgba(" + (int) (color.getRed() * 255) + ","
                + (int) (color.getGreen() * 255) + ","
                + (int) (color.getBlue() * 255) + ","
                + color.getOpacity() + ")";
    }

    public static String color2hsla(Color color) {
        return "hsla("
                + Math.round(color.getHue()) + ","
                + Math.round(color.getSaturation() * 100) + "%,"
                + Math.round(color.getBrightness() * 100) + "%,"
                + DoubleTools.scale2(color.getOpacity()) + ")";
    }

    public static String color2rgba(Color color) {
        return String.format("0x%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
    }

    public static Color value2color(int value) {
        return ColorConvertTools.converColor(new java.awt.Color(value, true));
    }

    public static Color invert(Color color) {
        return new Color(1 - color.getRed(), 1 - color.getGreen(), 1 - color.getBlue(), color.getOpacity());
    }

    public static boolean isLightColor(Color color) {
        return color.grayscale().getRed() > 0.5;
    }

    public static boolean isTransparent(Color color) {
        return color.equals(Color.TRANSPARENT);
    }

    public static Color foreColor(Color backColor) {
        return isLightColor(backColor) ? Color.BLACK : Color.WHITE;
    }

    public static int color2Value(Color color) {
        return color == null ? AppValues.InvalidInteger
                : ColorConvertTools.converColor(color).getRGB();
    }

    public static int web2Value(String web) {
        try {
            return FxColorTools.color2Value(Color.web(web));
        } catch (Exception e) {
            return AppValues.InvalidInteger;
        }
    }

    public static java.awt.Color toAwtColor(Color color) {
        java.awt.Color newColor = new java.awt.Color((int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
        return newColor;
    }

    public static String randomRGB() {
        Random random = new Random();
        return randomRGB(random);
    }

    public static String randomRGB(Random random) {
        String color = String.format("#%02X%02X%02X",
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256));
        return color;
    }

    public static List<String> randomRGB(int size) {
        Random random = new Random();
        List<String> colors = new ArrayList<>();
        if (size > 256 * 256 * 256 - 1) {
            return null;
        }
        while (colors.size() < size) {
            while (true) {
                String color = randomRGB(random);
                if (!colors.contains(color)) {
                    colors.add(color);
                    break;
                }
            }
        }
        return colors;
    }

    public static String randomRGBExcept(Collection<String> excepts) {
        Random random = new Random();
        while (true) {
            String color = randomRGB(random);
            if (!excepts.contains(color)) {
                return color;
            }
        }
    }

    public static String randomRGBA() {
        Random random = new Random();
        return randomRGBA(random);
    }

    public static String randomRGBA(Random random) {
        while (true) {
            String color = String.format("0x%02X%02X%02X%02X",
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            if (!"0xFFFFFFFF".equals(color)) {
                return color;
            }
        }
    }

    public static Color randomColor() {
        return Color.web(randomRGB());
    }

    public static Color randomColor(Random random) {
        return Color.web(randomRGB(random));
    }

    public static List<String> randomRGBA(int size) {
        Random random = new Random();
        List<String> colors = new ArrayList<>();
        if (size > 256 * 256 * 256 - 1) {
            return null;
        }
        while (colors.size() < size) {
            while (true) {
                String color = randomRGBA(random);
                if (!colors.contains(color)) {
                    colors.add(color);
                    break;
                }
            }
        }
        return colors;
    }

    public static String randomRGBAExcept(Collection<String> excepts) {
        Random random = new Random();
        while (true) {
            String color = randomRGBA(random);
            if (!excepts.contains(color)) {
                return color;
            }
        }
    }

    public static String color2rgb(java.awt.Color color) {
        return String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String color2css(java.awt.Color color) {
        return "rgba(" + color.getRed() + ","
                + color.getGreen() + ","
                + color.getBlue() + ","
                + color.getAlpha() / 255f + ")";
    }

    public static String hsb2rgba(float h, float s, float b) {
        return color2rgba(Color.hsb(h, s, b));
    }

    public static String color2AlphaHex(java.awt.Color color) {
        return String.format("#%02X%02X%02X%02X",
                color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static float[] color2srgb(Color color) {
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

    public static int calculateColorDistanceSquare(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        double redDiff = (color1.getRed() - color2.getRed()) * 255;
        double greenDiff = (color1.getGreen() - color2.getGreen()) * 255;
        double blueDiff = (color1.getBlue() - color2.getBlue()) * 255;
        int v = (int) Math.round(2 * redDiff * redDiff + 4 * greenDiff * greenDiff + 3 * blueDiff * blueDiff);
        return v;
    }

    // distance2 = Math.pow(distance, 2)
    // distance: 0-255
    public static boolean isColorMatchSquare(Color color1, Color color2, int distance2) {
        if (color1 == null || color2 == null) {
            return false;
        }
        if (color1.equals(color2)) {
            return true;
        } else if (distance2 == 0 || color1.equals(Color.TRANSPARENT) || color2.equals(Color.TRANSPARENT)) {
            return false;
        }
        return calculateColorDistanceSquare(color1, color2) <= distance2;
    }

    public static StringTable colorsTable(StringTable table, ColorData color1, ColorData color2, ColorData color3) {
        List<String> row = new ArrayList<>();
        row.add(message("Color"));
        row.add("<DIV style=\"width: 50px;  background-color:"
                + color2css(color1.getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
        row.add("<DIV style=\"width: 50px;  background-color:"
                + color2css(color2.getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
        row.add("<DIV style=\"width: 50px;  background-color:"
                + color2css(color3.getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Name"), color1.getColorName() + "", color2.getColorName() + "", color3.getColorName() + ""));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Hue"), color1.getHue(), color2.getHue(), color3.getHue()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Saturation"), color1.getSaturation(), color2.getSaturation(), color3.getSaturation()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Brightness"), color1.getBrightness(), color2.getBrightness(), color3.getBrightness()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("RYBAngle"), color1.getRybAngle(), color2.getRybAngle(), color3.getRybAngle()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Opacity"), color1.getOpacity(), color2.getOpacity(), color3.getOpacity()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("RGBA", color1.getRgba(), color2.getRgba(), color3.getRgba()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("RGB", color1.getRgb(), color2.getRgb(), color3.getRgb()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("sRGB", color1.getSrgb(), color2.getSrgb(), color3.getSrgb()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("HSBA", color1.getHsb(), color2.getHsb(), color3.getHsb()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("CalculatedCMYK"), color1.getCalculatedCMYK(), color2.getCalculatedCMYK(), color3.getCalculatedCMYK()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Adobe RGB", color1.getAdobeRGB(), color2.getAdobeRGB(), color3.getAdobeRGB()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Apple RGB", color1.getAppleRGB(), color2.getAppleRGB(), color3.getAppleRGB()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("ECI RGB", color1.getEciRGB(), color2.getEciRGB(), color3.getEciRGB()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("sRGB Linear", color1.getSRGBLinear(), color2.getSRGBLinear(), color3.getSRGBLinear()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Adobe RGB Linear", color1.getAdobeRGBLinear(), color2.getAdobeRGBLinear(), color3.getAdobeRGBLinear()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Apple RGB Linear", color1.getAppleRGBLinear(), color2.getAppleRGBLinear(), color3.getAppleRGBLinear()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("ECI CMYK", color1.getEciCMYK(), color2.getEciCMYK(), color3.getEciCMYK()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("Adobe CMYK Uncoated FOGRA29", color1.getAdobeCMYK(), color2.getAdobeCMYK(), color3.getAdobeCMYK()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("XYZ", color1.getXyz(), color2.getXyz(), color3.getXyz()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("CIE-L*ab", color1.getCieLab(), color2.getCieLab(), color3.getCieLab()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("LCH(ab)", color1.getLchab(), color2.getLchab(), color3.getLchab()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("CIE-L*uv", color1.getCieLuv(), color2.getCieLuv(), color3.getCieLuv()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList("LCH(uv)", color1.getLchuv(), color2.getLchuv(), color3.getLchuv()));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Value"), color1.getColorValue() + "", color2.getColorValue() + "", color3.getColorValue() + ""));
        table.add(row);
        return table;
    }

}
