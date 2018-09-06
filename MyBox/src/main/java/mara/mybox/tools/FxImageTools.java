package mara.mybox.tools;

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
import mara.mybox.image.ImageConvertionTools;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
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
public class FxImageTools {

    private static final Logger logger = LogManager.getLogger();

    public class ImageManufactureType {

        public static final int Brighter = 0;
        public static final int Darker = 1;
        public static final int Gray = 2;
        public static final int Invert = 3;
        public static final int Saturate = 4;
        public static final int Desaturate = 5;
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
    public static BufferedImage getWritableData(Image image, String format) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        if (!CommonValues.NoAlphaImages.contains(format.toLowerCase())
                || !bufferedImage.isAlphaPremultiplied()) {
            return bufferedImage;
        }
        if (AppVaribles.alphaAsBlack) {
            return ImageConvertionTools.RemoveAlpha(bufferedImage);
        } else {
            return ImageConvertionTools.ReplaceAlphaAsWhite(bufferedImage);
        }
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
                if (scope.inScope(x, y, color)) {
                    double v = color.getSaturation() + change;
                    if (v > 1.0) {
                        v = 1.0;
                    }
                    if (v < 0.0) {
                        v = 0.0;
                    }
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
                if (scope.inScope(x, y, color)) {
                    double v = color.getBrightness() + change;
                    if (v > 1.0) {
                        v = 1.0;
                    }
                    if (v < 0.0) {
                        v = 0.0;
                    }
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
                if (scope.inScope(x, y, color)) {
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

    public static Image setOpacity(Image image, int opacity, ImageScope scope) {
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
                    Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity / 100.0);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeRed(Image image, int change, ImageScope scope) {
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
                    int red = (int) (color.getRed() * 255) + change;
                    if (red > 255) {
                        red = 255;
                    } else if (red < 0) {
                        red = 0;
                    }
                    Color newColor = new Color(red / 255.0, color.getGreen(), color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeGreen(Image image, int change, ImageScope scope) {
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
                    int green = (int) (color.getGreen() * 255) + change;
                    if (green > 255) {
                        green = 255;
                    } else if (green < 0) {
                        green = 0;
                    }
                    Color newColor = new Color(color.getRed(), green / 255.0, color.getBlue(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeBlue(Image image, int change, ImageScope scope) {
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
                    int blue = (int) (color.getBlue() * 255) + change;
                    if (blue > 255) {
                        blue = 255;
                    } else if (blue < 0) {
                        blue = 0;
                    }
                    Color newColor = new Color(color.getRed(), color.getGreen(), blue / 255.0, color.getOpacity());
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
                    pixelWriter.setColor(x, y, color.grayscale());
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeBinary(Image image, int precent, ImageScope scope) {
        double threshold = precent / 100.0;
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
                    double gray = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                    if (gray < threshold) {
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
        BufferedImage target = ImageConvertionTools.scaleImage(source, width, height);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image rotateImage(Image image, int angle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertionTools.rotateImage(source, angle);
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
        BufferedImage target = ImageConvertionTools.shearImage(source, shearX, shearY);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image addWatermark(Image image, String textString,
            java.awt.Font font, Color color, int x, int y, float transparent, int shadow) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertionTools.addWatermarkText(source, textString,
                font, FxImageTools.colorConvert(color), x, y, transparent, shadow);
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

    public static java.awt.Color colorConvert(Color color) {
        java.awt.Color newColor = new java.awt.Color((int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
        return newColor;
    }

    public static Image cutEdges(Image image, Color color,
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

    public static Image addEdgesFx(Image image, Color color, int edgeWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (image == null || edgeWidth <= 0) {
                return image;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int totalWidth = width, totalHegiht = height;
            int x1 = 0, y1 = 0, x2 = width, y2 = height;
            if (addLeft) {
                totalWidth += edgeWidth;
                x1 = edgeWidth;
                x2 = width + edgeWidth;
            }
            if (addRight) {
                totalWidth += edgeWidth;
            }
            if (addTop) {
                totalHegiht += edgeWidth;
                y1 = edgeWidth;
                y2 = height + edgeWidth;
            }
            if (addBottom) {
                totalHegiht += edgeWidth;
            }
//            logger.debug(width + "  " + totalWidth);

            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(totalWidth, totalHegiht);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(x1, y1, width, height, pixelReader, 0, 0);

//            logger.debug(x1 + "  " + y1);
//            logger.debug(totalWidth + "  " + totalHegiht);
            if (addLeft) {
                for (int x = 0; x < edgeWidth; x++) {
                    for (int y = 0; y < totalHegiht; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addRight) {
                for (int x = totalWidth - 1; x > totalWidth - edgeWidth - 1; x--) {
                    for (int y = 0; y < totalHegiht; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addTop) {
                for (int y = 0; y < edgeWidth; y++) {
                    for (int x = 0; x < totalWidth; x++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addBottom) {
                for (int y = totalHegiht - 1; y > totalHegiht - edgeWidth - 1; y--) {
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
    public static Image addEdgesFx2(Image image, Color color, int edgeWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (image == null || edgeWidth <= 0) {
                return image;
            }
            Group group = new Group();
            double imageWidth = image.getWidth(), imageHeight = image.getHeight();
            double totalWidth = image.getWidth(), totalHeight = image.getHeight();
            ImageView view = new ImageView(image);
            view.setFitWidth(imageWidth);
            view.setFitHeight(imageHeight);
            if (addLeft) {
                view.setX(edgeWidth);
                totalWidth += edgeWidth;
            } else {
                view.setX(0);
            }
            if (addTop) {
                view.setY(edgeWidth);
                totalHeight += edgeWidth;
            } else {
                view.setY(0);
            }
            if (addBottom) {
                totalHeight += edgeWidth;
            }
            if (addRight) {
                totalWidth += edgeWidth;
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
        BufferedImage target = ImageConvertionTools.addArc(source, arc, FxImageTools.colorConvert(bgColor));
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
        BufferedImage target = ImageConvertionTools.addShadow(source, shadow, FxImageTools.colorConvert(color));
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

        blurEdgesFx(pixelWriter, color, width, height, shadow);

//        int max = 120;
//        if (shadow < max) {
//            blurEdgesFx(pixelWriter, color, width, height, shadow);
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
//            blurEdgesFx(pixelWriter, color, width, height, max);
//
//        }
        return newImage;
    }

    private static void blurEdgesFx(PixelWriter pixelWriter, Color color,
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
    }

    public static Image blurImage(Image image, int size) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertionTools.blurImage(source, size);
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

    public static Image indicateSplit(Image image,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize) {
        if (rows == null || cols == null
                || rows.size() < 2 || cols.size() < 2) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConvertionTools.indicateSplit(source, rows, cols,
                FxImageTools.colorConvert(lineColor), lineWidth);
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
            Color bgColor, int interval, int edge) {
        try {
            if (images == null || images.isEmpty()) {
                return null;
            }
            Group group = new Group();

            int x = edge, y = edge, width = 0, height = 0;
            for (int i = 0; i < images.size(); i++) {
                Image image = images.get(i);
                ImageView view = new ImageView(image);
                view.setFitWidth(image.getWidth());
                view.setFitHeight(image.getHeight());
                view.setX(x);
                view.setY(y);
                group.getChildren().add(view);

                x = edge;
                y += image.getHeight() + interval;

                if (image.getWidth() > width) {
                    width = (int) image.getWidth();
                }
            }

            width += 2 * edge;
            height = y + edge - interval;
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
        BufferedImage target = ImageConvertionTools.combineSingleColumn(images);
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

            double x = imageCombine.getEdgesValue(), y = imageCombine.getEdgesValue(), imageWidth, imageHeight;
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

                x = imageCombine.getEdgesValue();
                y += imageHeight + imageCombine.getIntervalValue();

                if (imageWidth > totalWidth) {
                    totalWidth = imageWidth;
                }
            }

            totalWidth += 2 * imageCombine.getEdgesValue();
            totalHeight = y + imageCombine.getEdgesValue() - imageCombine.getIntervalValue();
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
            int x = imageCombine.getEdgesValue(), y = imageCombine.getEdgesValue(), imageWidth, imageHeight;
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

            totalWidth = x + imageCombine.getEdgesValue() - imageCombine.getIntervalValue();
            if (!isPart) {
                totalHeight += 2 * imageCombine.getEdgesValue();
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

    public static Image blurBottomEdge(Image image, int size) {
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

    public static Image blurLeftEdge(Image image, int size) {
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

    public static Image blurEdgesFx(Image image, int size) {
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

}
