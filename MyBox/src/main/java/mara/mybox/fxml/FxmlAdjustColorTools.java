package mara.mybox.fxml;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import static mara.mybox.fxml.FxmlImageTools.isColorMatch;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.Point;
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

    public enum ColorOperationType {
        Brightness,
        Sauration,
        Hue,
        Opacity,
        Red,
        Green,
        Blue,
        Yellow,
        Cyan,
        Magenta,
        RGB
    }

    public static Image ajustColorByScope(Image source, ColorOperationType type,
            double change, ImageScope scope) {
        PixelReader pixelReader = source.getPixelReader();
        WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope == null || scope.inScope(x, y, color)) {
                    Color newColor = adjustColor(color, type, change);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image ajustColorByMatting(Image source, ColorOperationType type, double change,
            List<Point> points, int distance) {
        try {
            if (source == null
                    || points == null || points.isEmpty()
                    || distance < 0 || distance > 255) {
                return source;
            }
            int width = (int) source.getWidth();
            int height = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(width, height);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);

            boolean[][] visited = new boolean[height][width];
            Queue<Point> queue = new LinkedList<>();

            for (Point point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    Point p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= width || y < 0 || y >= height
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (isColorMatch(color, startColor, distance)) {
                        Color newColor = adjustColor(color, type, change);
                        pixelWriter.setColor(x, y, newColor);
                        queue.add(new Point(x + 1, y));
                        queue.add(new Point(x - 1, y));
                        queue.add(new Point(x, y + 1));
                        queue.add(new Point(x, y - 1));
                    }
                }
            }

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Color adjustColor(Color color, ColorOperationType type, double change) {
        Color newColor = color;
        double v, red, blue, green;
        switch (type) {
            case Brightness:
                v = Math.min(Math.max(color.getBrightness() * (1.0 + change), 0.0), 1.0);
                newColor = Color.hsb(color.getHue(), color.getSaturation(), v, color.getOpacity());
                break;
            case Sauration:
                v = Math.min(Math.max(color.getSaturation() * (1.0 + change), 0.0), 1.0);
                newColor = Color.hsb(color.getHue(), v, color.getBrightness(), color.getOpacity());
                break;
            case Hue:
                v = color.getHue() + change;
                if (v > 360.0) {
                    v = v - 360.0;
                }
                if (v < 0.0) {
                    v = v + 360.0;
                }
                newColor = Color.hsb(v, color.getSaturation(), color.getBrightness(), color.getOpacity());
                break;
            case Opacity:
                newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), change);
                break;
            case Red:
                red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                newColor = new Color(red, color.getGreen(), color.getBlue(), color.getOpacity());
                break;
            case Green:
                green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                newColor = new Color(color.getRed(), green, color.getBlue(), color.getOpacity());
                break;
            case Blue:
                blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                newColor = new Color(color.getRed(), color.getGreen(), blue, color.getOpacity());
                break;
            case Yellow:
                red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                newColor = new Color(red, green, color.getBlue(), color.getOpacity());
                break;
            case Cyan:
                green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                newColor = new Color(color.getRed(), green, blue, color.getOpacity());
                break;
            case Magenta:
                red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                newColor = new Color(red, color.getGreen(), blue, color.getOpacity());
                break;
            case RGB:
                red = Math.min(Math.max(color.getRed() + change, 0.0), 1.0);
                green = Math.min(Math.max(color.getGreen() + change, 0.0), 1.0);
                blue = Math.min(Math.max(color.getBlue() + change, 0.0), 1.0);
                newColor = new Color(red, green, blue, color.getOpacity());
                break;
            default:
                break;
        }
        return newColor;

    }

}
