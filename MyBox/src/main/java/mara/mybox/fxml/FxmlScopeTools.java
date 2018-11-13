package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import static mara.mybox.fxml.FxmlImageTools.isColorMatch;
import mara.mybox.image.ImageScopeTools;
import mara.mybox.objects.Circle;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.Point;
import mara.mybox.objects.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:44:49
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlScopeTools {

    private static final Logger logger = LogManager.getLogger();

    public static Image indicateRectangle(Image image,
            Color color, int lineWidth, Rectangle rect) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateRectangle(source,
                FxmlImageTools.colorConvert(color), lineWidth, rect);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image indicateCircle(Image image,
            Color color, int lineWidth, Circle circle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateCircle(source,
                FxmlImageTools.colorConvert(color), lineWidth, circle);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image scopeImage(Image image, ImageScope scope) {
        if ((scope.getScopeType() == ImageScope.ScopeType.All
                || (scope.getAreaScopeType() == ImageScope.AreaScopeType.AllArea
                && scope.getColorScopeType() == ImageScope.ColorScopeType.AllColor))) {
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
                if (!scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, opacityColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

        return newImage;
    }

    // https://en.wikipedia.org/wiki/Flood_fill
    // https://www.codeproject.com/Articles/6017/QuickFill-An-Efficient-Flood-Fill-Algorithm
    public static Image scopeMatting(Image source, ImageScope scope) {
        try {
            if (scope == null) {
                return source;
            }
            int width = (int) source.getWidth();
            int height = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(width, height);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            double opacity = scope.getOpacity();
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    Color color = pixelReader.getColor(x, y);
                    if (color == Color.TRANSPARENT) {
                        pixelWriter.setColor(x, y, color);
                        continue;
                    }
                    Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                    pixelWriter.setColor(x, y, newColor);
                }
            }
            if (scope.getPoints() == null || scope.getPoints().isEmpty()) {
                return newImage;
            }

            boolean[][] visited = new boolean[height][width];
            Queue<Point> queue = new LinkedList<>();
            List<Point> points = scope.getPoints();
            int distance = scope.getColorDistance();

            for (Point point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    mara.mybox.objects.Point p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= width || y < 0 || y >= height
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color pixelColor = pixelReader.getColor(x, y);
                    if (isColorMatch(pixelColor, startColor, distance)) {
                        pixelWriter.setColor(x, y, pixelColor);
                        queue.add(new mara.mybox.objects.Point(x + 1, y));
                        queue.add(new mara.mybox.objects.Point(x - 1, y));
                        queue.add(new mara.mybox.objects.Point(x, y + 1));
                        queue.add(new mara.mybox.objects.Point(x, y - 1));
                    }
                }
            }

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
