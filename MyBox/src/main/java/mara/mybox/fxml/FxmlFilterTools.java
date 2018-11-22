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
 * @CreateDate 2018-11-10 19:26:55
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlFilterTools {

    private static final Logger logger = LogManager.getLogger();

    public enum FiltersOperationType {
        Gray,
        Invert,
        BlackOrWhite,
        Red,
        Green,
        Blue,
        RedInvert,
        GreenInvert,
        BlueInvert,
        Sepia,
        Yellow,
        Cyan,
        Magenta
    }

    public static Image filterColorByScope(Image source, FiltersOperationType type,
            double value, ImageScope scope) {
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
                    Color newColor = filterColor(color, type, value);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image filterColorByMatting(Image source, FiltersOperationType type, double value,
            List<IntPoint> points, double distance) {
        try {
            if (source == null
                    || points == null || points.isEmpty()
                    || distance < 0 || distance > 1) {
                return source;
            }
            int width = (int) source.getWidth();
            int height = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(width, height);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);

            boolean[][] visited = new boolean[height][width];
            Queue<mara.mybox.objects.IntPoint> queue = new LinkedList<>();

            for (mara.mybox.objects.IntPoint point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    mara.mybox.objects.IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= width || y < 0 || y >= height
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (FxmlColorTools.isColorMatch(color, startColor, distance)) {
                        Color newColor = filterColor(color, type, value);
                        pixelWriter.setColor(x, y, newColor);
                        queue.add(new mara.mybox.objects.IntPoint(x + 1, y));
                        queue.add(new mara.mybox.objects.IntPoint(x - 1, y));
                        queue.add(new mara.mybox.objects.IntPoint(x, y + 1));
                        queue.add(new mara.mybox.objects.IntPoint(x, y - 1));
                    }
                }
            }

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Color filterColor(Color color, FiltersOperationType type, double value) {
        Color newColor = color;
        switch (type) {
            case Gray:
                newColor = color.grayscale();  //  JDK internal: 0.21 * red + 0.71 * green + 0.07 * blue
                break;
            case Invert:
                newColor = color.invert();
                break;
            case BlackOrWhite:
                double gray = 21.26 * color.getRed() + 71.52 * color.getGreen() + 7.22 * color.getBlue();
                if (gray < value) {
                    newColor = Color.BLACK;
                } else {
                    newColor = Color.WHITE;
                }
                break;
            case Red:
                newColor = new Color(color.getRed(), 0, 0, color.getOpacity());
                break;
            case Green:
                newColor = new Color(0, color.getGreen(), 0, color.getOpacity());
                break;
            case Blue:
                newColor = new Color(0, 0, color.getBlue(), color.getOpacity());
                break;
            case Yellow:
                newColor = new Color(color.getRed(), color.getGreen(), 0, color.getOpacity());
                break;
            case Cyan:
                newColor = new Color(0, color.getGreen(), color.getBlue(), color.getOpacity());
                break;
            case Magenta:
                newColor = new Color(color.getRed(), 0, color.getBlue(), color.getOpacity());
                break;
            case RedInvert:
                newColor = new Color(1.0 - color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
                break;
            case GreenInvert:
                newColor = new Color(color.getRed(), 1.0 - color.getGreen(), color.getBlue(), color.getOpacity());
                break;
            case BlueInvert:
                newColor = new Color(color.getRed(), color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                break;
            case Sepia:
                newColor = pixel2Sepia(color, value);
                break;
            default:
                break;
        }
        return newColor;

    }

    public static Color pixel2Sepia(Color color, double sepiaIntensity) {
        double sepiaDepth = 20;
        double gray = color.grayscale().getRed() * 255;
        double r = gray, g = gray, b = gray;
        r = Math.min(r + (sepiaDepth * 2), 255);
        g = Math.min(g + sepiaDepth, 255);
        b = Math.min(Math.max(b - sepiaIntensity, 0), 255);
        Color newColor = new Color(r / 255.0f, g / 255.0f, b / 255.0f, color.getOpacity());
        return newColor;
    }

}
