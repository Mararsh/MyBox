package mara.mybox.image;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import mara.mybox.image.ImageBlendTools.ImagesBlendMode;
import mara.mybox.image.ImageBlendTools.ImagesRelativeLocation;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.objects.ImageCombine;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlImageTools {

    private static final Logger logger = LogManager.getLogger();

    public class ImageManufactureType {

        public static final int Brighter = 0;
        public static final int Darker = 1;
        public static final int Gray = 2;
        public static final int Invert = 3;
        public static final int Saturate = 4;
        public static final int Desaturate = 5;
    }

    public static java.awt.Color colorConvert(Color color) {
        java.awt.Color newColor = new java.awt.Color((int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
        return newColor;
    }

    public static Image manufactureImage(Image image, int manuType) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                switch (manuType) {
                    case ImageManufactureType.Brighter:
                        color = color.brighter();
                        break;
                    case ImageManufactureType.Darker:
                        color = color.darker();
                        break;
                    case ImageManufactureType.Gray:
                        color = color.grayscale();
                        break;
                    case ImageManufactureType.Invert:
                        color = color.invert();
                        break;
                    case ImageManufactureType.Saturate:
                        color = color.saturate();
                        break;
                    case ImageManufactureType.Desaturate:
                        color = color.desaturate();
                        break;
                    default:
                        break;
                }
                pixelWriter.setColor(x, y, color);
            }
        }
        return newImage;
    }

    // https://stackoverflow.com/questions/19548363/image-saved-in-javafx-as-jpg-is-pink-toned
    public static BufferedImage getBufferedImage(Image image) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        return source;
    }

    public static BufferedImage checkAlpha(Image image, String format) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.checkAlpha(source, format);
        return target;
    }

    public static BufferedImage clearAlpha(Image image) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.clearAlpha(source);
        return target;
    }

    public static Image changeSaturate(Image image, float change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double v = Math.min(Math.max(color.getSaturation() * (1.0 + change), 0.0), 1.0);
                    Color newColor = Color.hsb(color.getHue(), v, color.getBrightness(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeBrightness(Image image, float change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double v = Math.min(Math.max(color.getBrightness() * (1.0 + change), 0.0), 1.0);
                    Color newColor = Color.hsb(color.getHue(), color.getSaturation(), v, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeHue(Image image, int change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double v = color.getHue() + change;
                    if (v > 360.0) {
                        v = v - 360.0;
                    }
                    if (v < 0.0) {
                        v = v + 360.0;
                    }
                    Color newColor = Color.hsb(v, color.getSaturation(), color.getBrightness(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image setOpacity(Image image, float opacity, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeRed(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                    Color newColor = new Color(red, color.getGreen(), color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeGreen(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                    Color newColor = new Color(color.getRed(), green, color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeBlue(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                    Color newColor = new Color(color.getRed(), color.getGreen(), blue, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeYellow(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                    double green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                    Color newColor = new Color(red, green, color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeCyan(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                    double blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                    Color newColor = new Color(color.getRed(), green, blue, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeMagenta(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                    double blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                    Color newColor = new Color(red, color.getGreen(), blue, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeRGB(Image image, double change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    double red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                    double green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                    double blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                    Color newColor = new Color(red, green, blue, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image keepRed(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    Color newColor = new Color(color.getRed(), 0, 0, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image keepGreen(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    Color newColor = new Color(0, color.getGreen(), 0, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image keepBlue(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    Color newColor = new Color(0, 0, color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image replaceColors(Image image, Color newColor, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeInvert(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, color.invert());
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeRedInvert(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        Color color, newColor;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    newColor = new Color(1.0 - color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeGreenInvert(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        Color color, newColor;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    newColor = new Color(color.getRed(), 1.0 - color.getGreen(), color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeBlueInvert(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        Color color, newColor;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    newColor = new Color(color.getRed(), color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeGray(Image image, ImageScope scope) {

        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, color.grayscale()); //  JDK internal: 0.21 * red + 0.71 * green + 0.07 * blue
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;

    }

    public static Image makeBinaryFx(Image image, ImageScope scope, int precent) {
        double p = precent / 100.0;
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }

                if (scope.inScope(x, y, color)) {
                    double gray = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                    if (gray < p) {
                        pixelWriter.setColor(x, y, Color.BLACK);
                    } else {
                        pixelWriter.setColor(x, y, Color.WHITE);
                    }
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;

    }

    public static Image makeSepia(Image image, ImageScope scope, int sepiaIntensity) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, pixel2Sepia(color, sepiaIntensity));
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;

    }

    public static Color pixel2Sepia(Color color, int sepiaIntensity) {
        int sepiaDepth = 20;
        int gray = (int) (color.grayscale().getRed() * 255);
        int r = gray, g = gray, b = gray;
        r = Math.min(r + (sepiaDepth * 2), 255);
        g = Math.min(g + sepiaDepth, 255);
        b = Math.min(Math.max(b - sepiaIntensity, 0), 255);
        Color newColor = new Color(r / 255.0f, g / 255.0f, b / 255.0f, color.getOpacity());
        return newColor;
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static double calculateColorDistance2(Color color1, Color color2) {
        if (color1 == color2) {
            return 0;
        }
        double v = 2 * Math.pow(color1.getRed() * 255 - color2.getRed() * 255, 2)
                + 4 * Math.pow(color1.getGreen() * 255 - color2.getGreen() * 255, 2)
                + 3 * Math.pow(color1.getBlue() * 255 - color2.getBlue() * 255, 2);
        return v;
    }

    public static boolean isColorMatch(Color color1, Color color2, int distance) {
        if (color1 == color2) {
            return true;
        } else if (distance == 0) {
            return false;
        }
        return calculateColorDistance2(color1, color2) <= Math.pow(distance, 2);
    }

    public static boolean isHueMatch(Color color1, Color color2, int distance) {
        return Math.abs(color1.getHue() - color2.getHue()) <= distance;
    }

    public static Image scaleImage(Image image, String format, float scale) {
        int targetW = (int) Math.round(image.getWidth() * scale);
        int targetH = (int) Math.round(image.getHeight() * scale);
        return scaleImage(image, format, targetW, targetH);
    }

    public static Image scaleImage(Image image, String format, int width, int height) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.scaleImage(source, width, height);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image rotateImage(Image image, int angle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.rotateImage(source, angle);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image horizontalImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int j = 0; j < height; j++) {
            int l = 0, r = width - 1;
            while (l <= r) {
                Color cl = pixelReader.getColor(l, j);
                Color cr = pixelReader.getColor(r, j);
                pixelWriter.setColor(l, j, cr);
                pixelWriter.setColor(r, j, cl);
                l++;
                r--;
            }
        }
        return newImage;
    }

    public static Image verticalImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int i = 0; i < width; i++) {
            int t = 0, b = height - 1;
            while (t <= b) {
                Color ct = pixelReader.getColor(i, t);
                Color cb = pixelReader.getColor(i, b);
                pixelWriter.setColor(i, t, cb);
                pixelWriter.setColor(i, b, ct);
                t++;
                b--;
            }
        }
        return newImage;
    }

    public static Image shearImage(Image image, float shearX, float shearY) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.shearImage(source, shearX, shearY);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image addWatermark(Image image, String textString,
            java.awt.Font font, Color color, int x, int y,
            float transparent, int shadow, int angle, boolean isOutline) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.addWatermarkText(source, textString,
                font, FxmlImageTools.colorConvert(color), x, y, transparent, shadow, angle, isOutline);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // This way may fail for big image
    public static Image addWatermarkFx(Image image, String textString,
            Font font, Color color, int x, int y, float transparent, int shadow) {
        try {
            Group group = new Group();

            Text text = new Text(x, y, textString);
            text.setFill(color);
            text.setFont(font);
            if (shadow > 0) {
                DropShadow dropShadow = new DropShadow();
                dropShadow.setOffsetX(shadow);
                dropShadow.setOffsetY(shadow);
                text.setEffect(dropShadow);
            }

            group.getChildren().add(text);

            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ImageInput(image));
            blend.setOpacity(1.0 - transparent);
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static Image cutMarginsByWidth(Image image, int MarginWidth,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {

        try {
            int imageWidth = (int) image.getWidth();
            int imageHeight = (int) image.getHeight();
            PixelReader pixelReader = image.getPixelReader();

            int top = 0, bottom = imageHeight - 1, left = 0, right = imageWidth - 1;
            if (cutTop) {
                top = MarginWidth;
            }
            if (cutBottom) {
                bottom -= MarginWidth;
            }
            if (cutLeft) {
                left = MarginWidth;
            }
            if (cutRight) {
                right -= MarginWidth;
            }
            return cropImage(image, left, top, right, bottom);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image cutMarginsByColor(Image image, Color color,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {

        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            PixelReader pixelReader = image.getPixelReader();

            int top = 0, bottom = height - 1, left = 0, right = width - 1;
            if (cutTop) {
                for (int j = 0; j < height; j++) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (!pixelReader.getColor(i, j).equals(color)) {
//                            logger.debug("hasValue: " + i + " " + j + " " + color);
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        top = j;
                        break;
                    }
                }
            }
//            logger.debug("top: " + top);
            if (top < 0) {
                return null;
            }
            if (cutBottom) {
                for (int j = height - 1; j >= 0; j--) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (!pixelReader.getColor(i, j).equals(color)) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        bottom = j;
                        break;
                    }
                }
            }
//            logger.debug("bottom: " + bottom);
            if (bottom < 0) {
                return null;
            }
            if (cutLeft) {
                for (int i = 0; i < width; i++) {
                    boolean hasValue = false;
                    for (int j = 0; j < height; j++) {
                        if (!pixelReader.getColor(i, j).equals(color)) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        left = i;
                        break;
                    }
                }
            }
//            logger.debug("left: " + left);
            if (left < 0) {
                return null;
            }
            if (cutRight) {
                for (int i = width - 1; i >= 0; i--) {
                    boolean hasValue = false;
                    for (int j = 0; j < height; j++) {
                        if (!pixelReader.getColor(i, j).equals(color)) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        right = i;
                        break;
                    }
                }
            }
//            logger.debug("right: " + right);
            if (right < 0) {
                return null;
            }

//            logger.debug(left + " " + top + " " + right + " " + bottom);
            return cropImage(image, left, top, right, bottom);

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image addMarginsFx(Image image, Color color, int MarginWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (image == null || MarginWidth <= 0) {
                return image;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int totalWidth = width, totalHegiht = height;
            int x1 = 0, y1 = 0, x2 = width, y2 = height;
            if (addLeft) {
                totalWidth += MarginWidth;
                x1 = MarginWidth;
                x2 = width + MarginWidth;
            }
            if (addRight) {
                totalWidth += MarginWidth;
            }
            if (addTop) {
                totalHegiht += MarginWidth;
                y1 = MarginWidth;
                y2 = height + MarginWidth;
            }
            if (addBottom) {
                totalHegiht += MarginWidth;
            }
//            logger.debug(width + "  " + totalWidth);

            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(totalWidth, totalHegiht);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(x1, y1, width, height, pixelReader, 0, 0);

//            logger.debug(x1 + "  " + y1);
//            logger.debug(totalWidth + "  " + totalHegiht);
            if (addLeft) {
                for (int x = 0; x < MarginWidth; x++) {
                    for (int y = 0; y < totalHegiht; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addRight) {
                for (int x = totalWidth - 1; x > totalWidth - MarginWidth - 1; x--) {
                    for (int y = 0; y < totalHegiht; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addTop) {
                for (int y = 0; y < MarginWidth; y++) {
                    for (int x = 0; x < totalWidth; x++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addBottom) {
                for (int y = totalHegiht - 1; y > totalHegiht - MarginWidth - 1; y--) {
                    for (int x = 0; x < totalWidth; x++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }

            return newImage;

        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }

    }

    // This way may fail for big image
    public static Image addMarginsFx2(Image image, Color color, int MarginWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (image == null || MarginWidth <= 0) {
                return image;
            }
            Group group = new Group();
            double imageWidth = image.getWidth(), imageHeight = image.getHeight();
            double totalWidth = image.getWidth(), totalHeight = image.getHeight();
            ImageView view = new ImageView(image);
            view.setFitWidth(imageWidth);
            view.setFitHeight(imageHeight);
            if (addLeft) {
                view.setX(MarginWidth);
                totalWidth += MarginWidth;
            } else {
                view.setX(0);
            }
            if (addTop) {
                view.setY(MarginWidth);
                totalHeight += MarginWidth;
            } else {
                view.setY(0);
            }
            if (addBottom) {
                totalHeight += MarginWidth;
            }
            if (addRight) {
                totalWidth += MarginWidth;
            }
            group.getChildren().add(view);

            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, totalWidth, totalHeight, color));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;

        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }

    }

    public static Image addArc(Image image, int arc, Color bgColor) {
        if (image == null) {
            return null;
        }
        if (arc <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.addArc(source, arc, FxmlImageTools.colorConvert(bgColor));
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // This way may fail for big image
    public static Image addArcFx(Image image, int arc, Color bgColor) {
        try {
            if (image == null || arc <= 0) {
                return null;
            }
            Group group = new Group();
            double imageWidth = image.getWidth(), imageHeight = image.getHeight();
            Scene scene = new Scene(group);

            ImageView view = new ImageView(image);
            view.setFitWidth(imageWidth);
            view.setFitHeight(imageHeight);

            Rectangle clip = new Rectangle(imageWidth, imageHeight);
            clip.setArcWidth(arc);
            clip.setArcHeight(arc);
            view.setClip(clip);

            group.getChildren().add(view);

            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, imageWidth, imageHeight, bgColor));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;

        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }

    }

    // This way may fail for big image
    public static Image addArcFx2(Image image, int arc, Color bgColor) {
        try {
            if (image == null || arc <= 0) {
                return null;
            }

            double imageWidth = image.getWidth(), imageHeight = image.getHeight();

            final Canvas canvas = new Canvas(imageWidth, imageHeight);
            final GraphicsContext g = canvas.getGraphicsContext2D();
            g.setGlobalBlendMode(BlendMode.ADD);
            g.setFill(bgColor);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(image, 0, 0);

            Rectangle clip = new Rectangle(imageWidth, imageHeight);
            clip.setArcWidth(arc);
            clip.setArcHeight(arc);
            canvas.setClip(clip);
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = canvas.snapshot(parameters, null);
            return newImage;

        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }

    }

    public static Image addShadow(Image image, int shadow, Color color) {
        if (image == null) {
            return null;
        }
        if (shadow <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.addShadow(source, shadow, FxmlImageTools.colorConvert(color));
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // Algorithm figured and verified by Mara
    public static Image addShadowBigFx(Image image, int shadow, Color color) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        if (shadow >= width - 1 && shadow >= height - 1) {
            return image;
        }
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width + shadow, height + shadow);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);

        blurMarginsFx(pixelWriter, color, width, height, shadow);

//        int max = 120;
//        if (shadow < max) {
//            blurMarginsFx(pixelWriter, color, width, height, shadow);
//        } else {
//            // bottom lines
//            for (int y = height; y < height + shadow - max; y++) {
//                for (int x = 2 * shadow - max; x < width + shadow - max; x++) {
//                    pixelWriter.setColor(x, y, color);
//                }
//            }
//            // left lines
//            for (int x = width; x < width + shadow - max; x++) {
//                for (int y = 2 * shadow - max; y < height + shadow - max; y++) {
//                    pixelWriter.setColor(x, y, color);
//                }
//            }
//            blurMarginsFx(pixelWriter, color, width, height, max);
//
//        }
        return newImage;
    }

    private static void blurMarginsFx(PixelWriter pixelWriter, Color color,
            int width, int height, int shadow) {
        // bottom lines
        for (int y = height; y < height + shadow; y++) {
            for (int x = shadow; x < width; x++) {
                double blur = (y - height) * 1.0 / shadow;
                blurPixel(pixelWriter, color, x, y, blur);
            }
        }
        // left lines
        for (int x = width; x < width + shadow; x++) {
            for (int y = shadow; y < height; y++) {
                double blur = (x - width) * 1.0 / shadow;
                blurPixel(pixelWriter, color, x, y, blur);
            }
        }

        // left bottom corner
        for (int y = height; y < height + shadow; y++) {
            for (int x = shadow; x < shadow + shadow; x++) {
                double blur = Math.sqrt((shadow + shadow - x) * (shadow + shadow - x) + (y - height) * (y - height)) / shadow;
                blurPixel(pixelWriter, color, x, y, blur);
            }
        }
        // right top corner
        for (int x = width; x < width + shadow; x++) {
            for (int y = shadow; y < shadow + shadow; y++) {
                double blur = Math.sqrt((x - width) * (x - width) + (shadow + shadow - y) * (shadow + shadow - y)) / shadow;
                blurPixel(pixelWriter, color, x, y, blur);
            }
        }

        // right bottom corner
        for (int y = height; y < height + shadow; y++) {
            for (int x = width; x < width + shadow; x++) {
                double blur = Math.sqrt((x - width) * (x - width) + (y - height) * (y - height)) / shadow;
                blurPixel(pixelWriter, color, x, y, blur);
            }
        }
    }

    private static void blurPixel(PixelWriter pixelWriter, Color color, int x, int y, double blur) {
        double newRed = color.getRed() + blur;
        if (newRed > 1) {
            newRed = 1;
        }
        double newGreen = color.getGreen() + blur;
        if (newGreen > 1) {
            newGreen = 1;
        }
        double newBlue = color.getBlue() + blur;
        if (newBlue > 1) {
            newBlue = 1;
        }
        if (newRed == 1 && newGreen == 1 && newBlue == 1) {
            pixelWriter.setColor(x, y, Color.TRANSPARENT);
        } else {
            pixelWriter.setColor(x, y, new Color(newRed, newGreen, newBlue, color.getOpacity()));
        }
    }

    // This way may fail for big image
    public static Image addShadowFx(Image image, int shadow, Color color) {
        try {
            if (image == null || shadow <= 0) {
                return null;
            }
            double imageWidth = image.getWidth(), imageHeight = image.getHeight();
            Group group = new Group();
            Scene s = new Scene(group);

            ImageView view = new ImageView(image);
            view.setFitWidth(imageWidth);
            view.setFitHeight(imageHeight);

            DropShadow dropShadow = new DropShadow();
            dropShadow.setOffsetX(shadow);
            dropShadow.setOffsetY(shadow);
            dropShadow.setColor(color);
            view.setEffect(dropShadow);

            group.getChildren().add(view);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }

    }

    public static Image cropImage(Image image, int x1, int y1, int x2, int y2) {
        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            if (x1 >= x2 || y1 >= y2
                    || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
                    || x2 > width || y2 > height) {
                return image;
            }
            int w = x2 - x1 + 1;
            int h = y2 - y1 + 1;
            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(w, h);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, w, h, pixelReader, x1, y1);
            return newImage;
        } catch (Exception e) {
            logger.debug(e.toString());
            return image;
        }
    }

    public static Image blurImage(Image image, int radius) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.blurImage(source, radius);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;

    }

    public static Image sharpenImage(Image image) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.sharpenImage(source);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image edgeDetectImage(Image image) {
        if (image == null) {
            return null;
        }
        BufferedImage source = clearAlpha(image);
        BufferedImage target = ImageConvertTools.edgeDetect(source);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image embossImage(Image image, int direction, int size, boolean gray) {
        if (image == null) {
            return null;
        }
        BufferedImage source = clearAlpha(image);
        BufferedImage target = ImageConvertTools.embossImage(source, direction, size, gray);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image thresholdingImage(Image image, int threshold, int smallValue, int bigValue) {

        if (image == null) {
            return null;
        }
        BufferedImage source = clearAlpha(image);
        BufferedImage target = ImageConvertTools.thresholding(source, threshold, smallValue, bigValue);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image posterizingImage(Image image, int size) {

        if (image == null) {
            return null;
        }
        BufferedImage source = clearAlpha(image);
        BufferedImage target = ImageConvertTools.posterizing(source, size);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image indicateScopeFx(Image image, ImageScope scope) {
        if ((scope.getAreaScopeType() == AreaScopeType.AllArea) && scope.isAllColors()) {
            return image;
        }
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        double opacity = scope.getOpacity();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                Color opacityColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                if (scope.indicateOpacity(x, y, color)) {
                    pixelWriter.setColor(x, y, opacityColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

        return newImage;
    }

    public static Image indicateArea(Image image,
            Color color, int lineWidth,
            int x1, int y1, int x2, int y2) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.showArea(source,
                FxmlImageTools.colorConvert(color), lineWidth, x1, y1, x2, y2);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image indicateSplit(Image image,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize) {
        if (rows == null || cols == null
                || rows.size() < 2 || cols.size() < 2) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertTools.indicateSplit(source, rows, cols,
                FxmlImageTools.colorConvert(lineColor), lineWidth, showSize);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // This way may fail for big image
    public static Image indicateSplitFx(Image image,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize) {
        try {
            if (rows == null || cols == null) {
                return image;
            }
            Group group = new Group();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int row;
            for (int i = 0; i < rows.size(); i++) {
                row = rows.get(i);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                Line rowLine = new Line(0, row, width, row);
                rowLine.setStroke(lineColor);
                rowLine.setStrokeWidth(lineWidth);
                group.getChildren().add(rowLine);
            }
            int col;
            for (int i = 0; i < cols.size(); i++) {
                col = cols.get(i);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                Line colLine = new Line(col, 0, col, height);
                colLine.setStroke(lineColor);
                colLine.setStrokeWidth(lineWidth);
                group.getChildren().add(colLine);
            }

            if (showSize) {
                for (int i = 0; i < rows.size() - 1; i++) {
                    int h = rows.get(i + 1) - rows.get(i);
                    for (int j = 0; j < cols.size() - 1; j++) {
                        int w = cols.get(j + 1) - cols.get(j);
                        Text text = new Text();
                        text.setX(cols.get(j) + w / 3);
                        text.setY(rows.get(i) + h / 3);
                        text.setFill(lineColor);
                        text.setText(w + "x" + h);
                        text.setFont(new javafx.scene.text.Font(lineWidth * 3.0));
                        group.getChildren().add(text);
                    }
                }
            }

            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ImageInput(image));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);

            return newImage;
        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }
    }

    // This way may fail for big image
    public static Image combineImagesColumnFx(List<Image> images,
            Color bgColor, int interval, int Margin) {
        try {
            if (images == null || images.isEmpty()) {
                return null;
            }
            Group group = new Group();

            int x = Margin, y = Margin, width = 0, height = 0;
            for (int i = 0; i < images.size(); i++) {
                Image image = images.get(i);
                ImageView view = new ImageView(image);
                view.setFitWidth(image.getWidth());
                view.setFitHeight(image.getHeight());
                view.setX(x);
                view.setY(y);
                group.getChildren().add(view);

                x = Margin;
                y += image.getHeight() + interval;

                if (image.getWidth() > width) {
                    width = (int) image.getWidth();
                }
            }

            width += 2 * Margin;
            height = y + Margin - interval;
            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, width, height, bgColor));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static Image combineSingleColumn(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        BufferedImage target = ImageConvertTools.combineSingleColumn(images);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // This way may fail for big image
    public static Image combineSingleColumnFx(ImageCombine imageCombine,
            List<ImageFileInformation> images) {
        if (imageCombine == null || images == null) {
            return null;
        }
        try {
            Group group = new Group();

            double x = imageCombine.getMarginsValue(), y = imageCombine.getMarginsValue(), imageWidth, imageHeight;
            double totalWidth = 0, totalHeight = 0, maxWidth = 0, minWidth = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();
            if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                for (ImageFileInformation image : images) {
                    imageWidth = (int) image.getImage().getWidth();
                    if (imageWidth > maxWidth) {
                        maxWidth = imageWidth;
                    }
                }
            }
            if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                for (ImageFileInformation image : images) {
                    imageWidth = (int) image.getImage().getWidth();
                    if (imageWidth < minWidth) {
                        minWidth = imageWidth;
                    }
                }
            }
            for (int i = 0; i < images.size(); i++) {
                ImageFileInformation imageInfo = images.get(i);
                Image image = imageInfo.getImage();
                ImageView view = new ImageView(image);
                view.setPreserveRatio(true);
                view.setX(x);
                view.setY(y);
                if (sizeType == ImageCombine.CombineSizeType.KeepSize
                        || sizeType == ImageCombine.CombineSizeType.TotalWidth || sizeType == ImageCombine.CombineSizeType.TotalHeight) {
                    view.setFitWidth(image.getWidth());
                } else if (sizeType == ImageCombine.CombineSizeType.EachWidth) {
                    view.setFitWidth(imageCombine.getEachWidthValue());
                } else if (sizeType == ImageCombine.CombineSizeType.EachHeight) {
                    view.setFitHeight(imageCombine.getEachHeightValue());
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                    view.setFitWidth(maxWidth);
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                    view.setFitWidth(minWidth);
                }
                imageWidth = view.getBoundsInParent().getWidth();
                imageHeight = view.getBoundsInParent().getHeight();
//                logger.debug(imageWidth + " " + imageHeight);
                group.getChildren().add(view);

                x = imageCombine.getMarginsValue();
                y += imageHeight + imageCombine.getIntervalValue();

                if (imageWidth > totalWidth) {
                    totalWidth = imageWidth;
                }
            }

            totalWidth += 2 * imageCombine.getMarginsValue();
            totalHeight = y + imageCombine.getMarginsValue() - imageCombine.getIntervalValue();
            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, totalWidth, totalHeight, imageCombine.getBgColor()));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // This way may fail for big image
    public static Image combineSingleRowFx(ImageCombine imageCombine,
            List<ImageFileInformation> images, boolean isPart) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            Group group = new Group();
            int x = imageCombine.getMarginsValue(), y = imageCombine.getMarginsValue(), imageWidth, imageHeight;
            int totalWidth = 0, totalHeight = 0, maxHeight = 0, minHeight = Integer.MAX_VALUE;
            if (isPart) {
                y = 0;
            }
            int sizeType = imageCombine.getSizeType();
            if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                for (ImageFileInformation image : images) {
                    imageHeight = (int) image.getImage().getHeight();
                    if (imageHeight > maxHeight) {
                        maxHeight = imageHeight;
                    }
                }
            }
            if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                for (ImageFileInformation image : images) {
                    imageHeight = (int) image.getImage().getHeight();
                    if (imageHeight < minHeight) {
                        minHeight = imageHeight;
                    }
                }
            }
            for (int i = 0; i < images.size(); i++) {
                ImageFileInformation imageInfo = images.get(i);
                Image image = imageInfo.getImage();
                ImageView view = new ImageView(image);
                view.setPreserveRatio(true);
                view.setX(x);
                view.setY(y);
                if (sizeType == ImageCombine.CombineSizeType.KeepSize
                        || sizeType == ImageCombine.CombineSizeType.TotalWidth || sizeType == ImageCombine.CombineSizeType.TotalHeight) {
                    view.setFitWidth(image.getWidth());
                } else if (sizeType == ImageCombine.CombineSizeType.EachWidth) {
                    view.setFitWidth(imageCombine.getEachWidthValue());
                } else if (sizeType == ImageCombine.CombineSizeType.EachHeight) {
                    view.setFitHeight(imageCombine.getEachHeightValue());
                    logger.debug("EachHeight");
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                    view.setFitHeight(maxHeight);
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                    view.setFitHeight(minHeight);
                }
                imageWidth = (int) view.getBoundsInParent().getWidth();
                imageHeight = (int) view.getBoundsInParent().getHeight();
//                logger.debug(imageWidth + " " + imageHeight);
                group.getChildren().add(view);

                x += imageWidth + imageCombine.getIntervalValue();

                if (imageHeight > totalHeight) {
                    totalHeight = imageHeight;
                }
            }

            totalWidth = x + imageCombine.getMarginsValue() - imageCombine.getIntervalValue();
            if (!isPart) {
                totalHeight += 2 * imageCombine.getMarginsValue();
            }
            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, totalWidth, totalHeight, imageCombine.getBgColor()));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // This way may fail for big image
    public static Image combineSingleColumnFx(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            Group group = new Group();

            double x = 0, y = 0, imageWidth, imageHeight;
            double totalWidth = 0, totalHeight = 0;

            for (Image theImage : images) {
                ImageView view = new ImageView(theImage);
                imageWidth = theImage.getWidth();
                imageHeight = theImage.getHeight();

                view.setPreserveRatio(true);
                view.setX(x);
                view.setY(y);
                view.setFitWidth(imageWidth);
                view.setFitHeight(imageHeight);

                group.getChildren().add(view);
                y += imageHeight;

                if (imageWidth > totalWidth) {
                    totalWidth = imageWidth;
                }
            }
            totalHeight = y;
            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, totalWidth, totalHeight, Color.TRANSPARENT));
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static Image blurBottomMargin(Image image, int size) {
        int height = (int) image.getHeight();
        if (size >= height - 1) {
            return image;
        }
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT || y <= height - size) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                double diff = (size + y - height) * 1.0 / size;
                double newRed = color.getRed() + diff;
                if (newRed > 1) {
                    newRed = 1;
                }
                double newGreen = color.getGreen() + diff;
                if (newGreen > 1) {
                    newGreen = 1;
                }
                double newBlue = color.getBlue() + diff;
                if (newBlue > 1) {
                    newBlue = 1;
                }
                Color newColor = new Color(newRed, newGreen, newBlue, color.getOpacity());
                pixelWriter.setColor(x, y, newColor);
            }
        }
        return newImage;
    }

    public static Image blurLeftMargin(Image image, int size) {
        int width = (int) image.getWidth();
        if (size >= width - 1) {
            return image;
        }
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT || x <= width - size) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                double blur = (size + x - width) * 1.0 / size;
                double newRed = color.getRed() + blur;
                if (newRed > 1) {
                    newRed = 1;
                }
                double newGreen = color.getGreen() + blur;
                if (newGreen > 1) {
                    newGreen = 1;
                }
                double newBlue = color.getBlue() + blur;
                if (newBlue > 1) {
                    newBlue = 1;
                }
                Color newColor = new Color(newRed, newGreen, newBlue, color.getOpacity());
                pixelWriter.setColor(x, y, newColor);
            }
        }
        return newImage;
    }

    public static Image blurMarginsFx(Image image, int size) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        if (size >= width - 1 && size >= height - 1) {
            return image;
        }
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT
                        || (x <= width - size && y <= height - size)) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                double blur = 0;
                if (x > width - size) {
                    blur = (size + x - width) * 1.0 / size;
                } else if (y > height - size) {
                    blur = (size + y - height) * 1.0 / size;
                }
                double newRed = color.getRed() + blur;
                if (newRed > 1) {
                    newRed = 1;
                }
                double newGreen = color.getGreen() + blur;
                if (newGreen > 1) {
                    newGreen = 1;
                }
                double newBlue = color.getBlue() + blur;
                if (newBlue > 1) {
                    newBlue = 1;
                }
                Color newColor = new Color(newRed, newGreen, newBlue, color.getOpacity());
                pixelWriter.setColor(x, y, newColor);
            }
        }
        return newImage;
    }

    public static Image blendImages(Image foreImage, Image backImage,
            ImagesRelativeLocation location, int x, int y,
            boolean intersectOnly, ImagesBlendMode blendMode, float opacity) {
        if (foreImage == null || backImage == null || blendMode == null) {
            return null;
        }
        BufferedImage source1 = SwingFXUtils.fromFXImage(foreImage, null);
        BufferedImage source2 = SwingFXUtils.fromFXImage(backImage, null);
        BufferedImage target = ImageBlendTools.blendImages(source1, source2,
                location, x, y, intersectOnly, blendMode, opacity);
        if (target == null) {
            target = source1;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
