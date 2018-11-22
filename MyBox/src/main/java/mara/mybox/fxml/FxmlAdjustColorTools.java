package mara.mybox.fxml;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.IntPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:32:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlAdjustColorTools {

    private static final Logger logger = LogManager.getLogger();

    public enum ColorObjectType {
        Brightness, Sauration, Hue, Opacity, Red, Green, Blue, Yellow, Cyan, Magenta, RGB, Color
    }

    public enum ColorActionType {
        Increase, Decrease, Set, Filter, Invert
    }

    public static Image ajustColor(Image source, ColorObjectType objectType, ColorActionType actionType,
            double change, Color inColor, ImageScope scope) {
        if (actionType == ColorActionType.Decrease) {
            change = 0 - change;
        }
        if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            if (objectType == ColorObjectType.Color) {
                return setColorByMatting(source, inColor, scope);
            } else {
                return changeColorByMatting(source, objectType, actionType, change, scope);
            }
        } else {
            if (objectType == ColorObjectType.Color) {
                return setColorByScope(source, inColor, scope);
            } else {
                return changeColorByScope(source, objectType, actionType, change, scope);
            }
        }
    }

    public static Image setColorByScope(Image source,
            Color newColor, ImageScope scope) {
        PixelReader pixelReader = source.getPixelReader();
        WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (scope == null || scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image setColorByMatting(Image source, Color newColor, ImageScope scope) {
        try {
            if (source == null) {
                return source;
            }
            List<IntPoint> points = scope.getPoints();
            double distance = scope.getColorDistance();
            if (points == null || points.isEmpty()
                    || distance < 0 || distance > 1) {
                return source;
            }

            int width = (int) source.getWidth();
            int height = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(width, height);
            PixelWriter pixelWriter = newImage.getPixelWriter();

            boolean excluded = scope.isColorExcluded();
            if (excluded) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        if (color == Color.TRANSPARENT) {
                            pixelWriter.setColor(x, y, color);
                            continue;
                        }
                        pixelWriter.setColor(x, y, newColor);
                    }
                }
            } else {
                pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);
            }

            boolean[][] visited = new boolean[height][width];
            Queue<IntPoint> queue = new LinkedList<>();
            for (IntPoint point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= width || y < 0 || y >= height
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (FxmlColorTools.isColorMatch(color, startColor, distance)) {
                        if (excluded) {
                            pixelWriter.setColor(x, y, color);
                        } else {
                            pixelWriter.setColor(x, y, newColor);
                        }
                        queue.add(new IntPoint(x + 1, y));
                        queue.add(new IntPoint(x - 1, y));
                        queue.add(new IntPoint(x, y + 1));
                        queue.add(new IntPoint(x, y - 1));
                    }
                }
            }

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image changeColorByScope(Image source,
            ColorObjectType objectType, ColorActionType actionType,
            double change, ImageScope scope) {
        PixelReader pixelReader = source.getPixelReader();
        WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        Color newColor;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                } else if (scope == null || scope.inScope(x, y, color)) {
                    newColor = changeColor(color, objectType, actionType, change);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeColorByMatting(Image source,
            ColorObjectType objectType, ColorActionType actionType,
            double change, ImageScope scope) {
        try {
            if (source == null) {
                return source;
            }
            List<IntPoint> points = scope.getPoints();
            double distance = scope.getColorDistance();
            if (points == null || points.isEmpty()
                    || distance < 0 || distance > 1) {
                return source;
            }

            int width = (int) source.getWidth();
            int height = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(width, height);
            PixelWriter pixelWriter = newImage.getPixelWriter();

            boolean excluded = scope.isColorExcluded();
            if (excluded) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        if (color == Color.TRANSPARENT) {
                            pixelWriter.setColor(x, y, color);
                            continue;
                        }
                        Color newColor = changeColor(color, objectType, actionType, change);
                        pixelWriter.setColor(x, y, newColor);
                    }
                }
            } else {
                pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);
            }

            boolean[][] visited = new boolean[height][width];
            Queue<IntPoint> queue = new LinkedList<>();
            Color newColor;
            for (IntPoint point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= width || y < 0 || y >= height
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (color != Color.TRANSPARENT
                            && FxmlColorTools.isColorMatch(color, startColor, distance)) {
                        if (excluded) {
                            pixelWriter.setColor(x, y, color);
                        } else {
                            newColor = changeColor(color, objectType, actionType, change);
                            pixelWriter.setColor(x, y, newColor);
                        }
                        queue.add(new IntPoint(x + 1, y));
                        queue.add(new IntPoint(x - 1, y));
                        queue.add(new IntPoint(x, y + 1));
                        queue.add(new IntPoint(x, y - 1));
                    }
                }
            }

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Color changeColor(Color color,
            ColorObjectType type, ColorActionType actionType,
            double change) {
        Color newColor = color;
        double v, red, blue, green;
        switch (type) {
            case Brightness:
                if (actionType == ColorActionType.Set) {
                    v = change;
                } else {
                    v = color.getBrightness() * (1.0 + change);
                }
                v = Math.min(Math.max(v, 0.0), 1.0);
                newColor = Color.hsb(color.getHue(), color.getSaturation(), v, color.getOpacity());
                break;
            case Sauration:
                if (actionType == ColorActionType.Set) {
                    v = change;
                } else {
                    v = color.getSaturation() * (1.0 + change);
                }
                v = Math.min(Math.max(v, 0.0), 1.0);
                newColor = Color.hsb(color.getHue(), v, color.getBrightness(), color.getOpacity());
                break;
            case Hue:
                if (actionType == ColorActionType.Set) {
                    v = change;
                } else {
                    v = color.getHue() + change;
                }
                if (v > 360.0) {
                    v = v - 360.0;
                }
                if (v < 0.0) {
                    v = v + 360.0;
                }
                newColor = Color.hsb(v, color.getSaturation(), color.getBrightness(), color.getOpacity());
                break;
            case Opacity:
                v = Math.min(Math.max(change, 0.0), 1.0);
                newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), v);
                break;
            case Red:
                switch (actionType) {
                    case Set:
                        red = change;
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        newColor = new Color(red, color.getGreen(), color.getBlue(), color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        red = color.getRed() + change;
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        newColor = new Color(red, color.getGreen(), color.getBlue(), color.getOpacity());
                        break;
                    case Filter:
                        newColor = new Color(color.getRed(), 0, 0, color.getOpacity());
                        break;
                    case Invert:
                        newColor = new Color(1.0 - color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
                        break;
                }
                break;
            case Green:
                switch (actionType) {
                    case Set:
                        green = change;
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        newColor = new Color(color.getRed(), green, color.getBlue(), color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        green = color.getGreen() + change;
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        newColor = new Color(color.getRed(), green, color.getBlue(), color.getOpacity());
                        break;
                    case Filter:
                        newColor = new Color(0, color.getGreen(), 0, color.getOpacity());
                        break;
                    case Invert:
                        newColor = new Color(color.getRed(), 1.0 - color.getGreen(), color.getBlue(), color.getOpacity());
                        break;
                }
                break;
            case Blue:
                switch (actionType) {
                    case Set:
                        blue = change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        newColor = new Color(color.getRed(), color.getGreen(), blue, color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        blue = color.getBlue() + change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        newColor = new Color(color.getRed(), color.getGreen(), blue, color.getOpacity());
                        break;
                    case Filter:
                        newColor = new Color(0, 0, color.getBlue(), color.getOpacity());
                        break;
                    case Invert:
                        newColor = new Color(color.getRed(), color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                        break;
                }
                break;
            case Yellow:
                switch (actionType) {
                    case Set:
                        red = change;
                        green = change;
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        newColor = new Color(red, green, color.getBlue(), color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        red = color.getRed() + change;
                        green = color.getGreen() + change;
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        newColor = new Color(red, green, color.getBlue(), color.getOpacity());
                        break;
                    case Filter:
                        newColor = new Color(color.getRed(), color.getGreen(), 0, color.getOpacity());
                        break;
                    case Invert:
                        newColor = new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), color.getBlue(), color.getOpacity());
                        break;
                }
                break;
            case Cyan:
                switch (actionType) {
                    case Set:
                        blue = change;
                        green = change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        newColor = new Color(color.getRed(), green, blue, color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        blue = color.getBlue() + change;
                        green = color.getGreen() + change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        newColor = new Color(color.getRed(), green, blue, color.getOpacity());
                        break;
                    case Filter:
                        newColor = new Color(0, color.getGreen(), color.getBlue(), color.getOpacity());
                        break;
                    case Invert:
                        newColor = new Color(color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                        break;
                }
                break;
            case Magenta:
                switch (actionType) {
                    case Set:
                        blue = change;
                        red = change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        newColor = new Color(red, color.getGreen(), blue, color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        blue = color.getBlue() + change;
                        red = color.getRed() + change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        newColor = new Color(red, color.getGreen(), blue, color.getOpacity());
                        break;
                    case Filter:
                        newColor = new Color(color.getRed(), 0, color.getBlue(), color.getOpacity());
                        break;
                    case Invert:
                        newColor = new Color(1.0 - color.getRed(), color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                        break;
                }
                break;
            case RGB:
                switch (actionType) {
                    case Set:
                        blue = change;
                        green = change;
                        red = change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        newColor = new Color(red, green, blue, color.getOpacity());
                        break;
                    case Increase:
                    case Decrease:
                        blue = color.getBlue() + change;
                        green = color.getGreen() + change;
                        red = color.getRed() + change;
                        blue = Math.min(Math.max(blue, 0.0), 1.0);
                        green = Math.min(Math.max(green, 0.0), 1.0);
                        red = Math.min(Math.max(red, 0.0), 1.0);
                        newColor = new Color(red, green, blue, color.getOpacity());
                        break;
                    case Invert:
                        newColor = color.invert();
                        break;
                }
                break;
            default:
                break;
        }
        return newColor;
    }

}
