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
import mara.mybox.image.ImageConvertTools;
import mara.mybox.image.ImageEffectTools;
import mara.mybox.objects.ConvolutionKernel;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:35:49
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlEffectTools {

    private static final Logger logger = LogManager.getLogger();

    public static Image applyConvolution(Image image, ConvolutionKernel kernel) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageEffectTools.applyConvolution(source, kernel);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image applyMatrix(Image image, float[][] matrix, boolean removeAlpha) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target;
        if (removeAlpha) {
            target = ImageEffectTools.applyMatrix(ImageConvertTools.clearAlpha(source), matrix);
        } else {
            target = ImageEffectTools.applyMatrix(source, matrix);
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;

    }

    public static Image applyConvolutionByScope(Image image, ConvolutionKernel kernel, ImageScope scope) {
        if (image == null || scope == null || kernel == null || kernel.getMatrix() == null) {
            return image;
        }
        int imageWidth = (int) image.getWidth();
        int imageHeight = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(imageWidth, imageHeight);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        float[][] matrix = kernel.getMatrix();
        int edge_op = kernel.getEdge();
        int radiusX = matrix[0].length / 2, radiusY = matrix.length / 2,
                maxX = imageWidth - 1, maxY = imageHeight - 1;
        boolean isEmboss = (kernel.getType() == ConvolutionKernel.Convolution_Type.EMBOSS);
        boolean isGray = (kernel.getGray() > 0);
        boolean keepOpacity = (kernel.getType() != ConvolutionKernel.Convolution_Type.EMBOSS
                && kernel.getType() != ConvolutionKernel.Convolution_Type.EDGE_DETECTION);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    if (x < radiusX || x + radiusX > maxX
                            || y < radiusY || y + radiusY > maxY) {
                        if (edge_op == ConvolutionKernel.Edge_Op.COPY) {
                            pixelWriter.setColor(x, y, color);
                            continue;
                        }
                    }
                    Color newColor = FxmlEffectTools.applyConvolution(pixelReader, imageWidth, imageHeight,
                            matrix, edge_op, keepOpacity, x, y);
                    if (isEmboss) {
                        double v = 128.0 / 255, red, blue, green;
                        red = Math.min(Math.max(newColor.getRed() + v, 0.0), 1.0);
                        green = Math.min(Math.max(newColor.getGreen() + v, 0.0), 1.0);
                        blue = Math.min(Math.max(newColor.getBlue() + v, 0.0), 1.0);
                        newColor = new Color(red, green, blue, newColor.getOpacity());
                        if (isGray) {
                            newColor = newColor.grayscale();
                        }
                    }
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image applyConvolutionByMatting(Image source, ConvolutionKernel kernel,
            List<Point> points, int distance) {
        try {
            if (source == null || kernel == null
                    || points == null || points.isEmpty()
                    || distance < 0 || distance > 255) {
                return source;
            }
            int imageWidth = (int) source.getWidth();
            int imageHeight = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelReader, 0, 0);

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<Point> queue = new LinkedList<>();

            float[][] matrix = kernel.getMatrix();
            int radiusX = matrix[0].length / 2, radiusY = matrix.length / 2,
                    maxX = imageWidth - 1, maxY = imageHeight - 1;
            int edge_op = kernel.getEdge();
            boolean keepOpacity = (kernel.getType() != ConvolutionKernel.Convolution_Type.EMBOSS
                    && kernel.getType() != ConvolutionKernel.Convolution_Type.EDGE_DETECTION);
            boolean isEmboss = (kernel.getType() == ConvolutionKernel.Convolution_Type.EMBOSS);
            boolean isGray = (kernel.getGray() > 0);
            for (Point point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    Point p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (isColorMatch(color, startColor, distance)) {
                        if (x < radiusX || x + radiusX > maxX
                                || y < radiusY || y + radiusY > maxY) {
                            if (edge_op == ConvolutionKernel.Edge_Op.COPY) {
                                pixelWriter.setColor(x, y, color);
                                continue;
                            }
                        }
                        Color newColor = applyConvolution(pixelReader, imageWidth, imageHeight,
                                matrix, edge_op, keepOpacity, x, y);
                        if (isEmboss) {
                            double v = 128.0 / 255, red, blue, green;
                            red = Math.min(Math.max(newColor.getRed() + v, 0.0), 1.0);
                            green = Math.min(Math.max(newColor.getGreen() + v, 0.0), 1.0);
                            blue = Math.min(Math.max(newColor.getBlue() + v, 0.0), 1.0);
                            newColor = new Color(red, green, blue, newColor.getOpacity());
                            if (isGray) {
                                newColor = newColor.grayscale();
                            }
                        }
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

    public static Image applyConvolution(Image image, float[][] kernel,
            int edge_op, boolean keepOpacity) {
        if (image == null || kernel == null || kernel.length == 0) {
            return image;
        }
        try {
            int imageWidth = (int) image.getWidth();
            int imageHeight = (int) image.getHeight();
            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();

            for (int j = 0; j < imageHeight; j++) {
                for (int i = 0; i < imageWidth; i++) {
                    Color newColor = FxmlEffectTools.applyConvolution(pixelReader, imageWidth, imageHeight,
                            kernel, edge_op, keepOpacity, i, j);
                    pixelWriter.setColor(i, j, newColor);
                }
            }
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Color applyConvolution(PixelReader pixelReader, int imageWidth, int imageHeight,
            float[][] matrix, int edge_op, boolean keepOpacity, int x, int y) {
        if (pixelReader == null || matrix == null || matrix.length == 0) {
            return null;
        }
        try {
            int matrixWidth = matrix[0].length;
            int matrixHeight = matrix.length;
            double red = 0.0, green = 0.0, blue = 0.0, opacity = 0.0;
            int convolveX, convolveY, radiusX = matrixWidth / 2, radiusY = matrixHeight / 2,
                    maxX = imageWidth - 1, maxY = imageHeight - 1;
            for (int matrixY = 0; matrixY < matrixHeight; matrixY++) {
                for (int matrixX = 0; matrixX < matrixWidth; matrixX++) {
                    convolveX = x - radiusX + matrixX;
                    convolveY = y - radiusY + matrixY;
                    if (convolveX < 0 || convolveX > maxX || convolveY < 0 || convolveY > maxY) {
                        if (edge_op == ConvolutionKernel.Edge_Op.MOD) {
                            convolveX = (convolveX + imageWidth) % imageWidth;
                            convolveY = (convolveY + imageHeight) % imageHeight;
                        } else {
                            continue; // Fill_zero
                        }
                    }
                    Color color = pixelReader.getColor(convolveX, convolveY);
                    red += color.getRed() * matrix[matrixY][matrixX];
                    green += color.getGreen() * matrix[matrixY][matrixX];
                    blue += color.getBlue() * matrix[matrixY][matrixX];
                    if (keepOpacity) {
                        opacity += color.getOpacity() * matrix[matrixY][matrixX];
                    }
                }
            }
            red = Math.min(Math.max(red, 0), 1.0);
            green = Math.min(Math.max(green, 0), 1.0);
            blue = Math.min(Math.max(blue, 0), 1.0);
            if (keepOpacity) {
                opacity = Math.min(Math.max(opacity, 0), 1.0);
            } else {
                opacity = 1.0;
            }
            Color color = new Color(red, green, blue, opacity);
            return color;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image posterizingImage(Image image, int size) {
        if (image == null || size < 0) {
            return image;
        }
        int imageWidth = (int) image.getWidth();
        int imageHeight = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(imageWidth, imageHeight);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                Color newColor = FxmlColorTools.posterizingColor(color, size);
                pixelWriter.setColor(x, y, newColor);
            }
        }
        return newImage;
    }

    public static Image posterizingByScope(Image image, int size, ImageScope scope) {
        if (image == null || size < 0 || scope == null) {
            return image;
        }
        int imageWidth = (int) image.getWidth();
        int imageHeight = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(imageWidth, imageHeight);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (scope.inScope(x, y, color)) {
                    Color newColor = FxmlColorTools.posterizingColor(color, size);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image posterizingByMatting(Image source,
            int size, List<Point> points, int distance) {
        if (source == null || size < 0
                || points == null || points.isEmpty()
                || distance < 0 || distance > 255) {
            return source;
        }
        try {
            int imageWidth = (int) source.getWidth();
            int imageHeight = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelReader, 0, 0);

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<Point> queue = new LinkedList<>();

            for (Point point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    Point p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (isColorMatch(color, startColor, distance)) {
                        Color newColor = FxmlColorTools.posterizingColor(color, size);
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

    public static Image thresholdingImage(Image image, int threshold, int smallValue, int bigValue) {
        if (image == null || threshold < 0 || threshold > 255
                || smallValue < 0 || smallValue > 255 || bigValue < 0 || bigValue > 255) {
            return image;
        }
        int imageWidth = (int) image.getWidth();
        int imageHeight = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(imageWidth, imageHeight);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                Color newColor = FxmlColorTools.thresholdingColor(color,
                        threshold, smallValue, bigValue);
                pixelWriter.setColor(x, y, newColor);
            }
        }
        return newImage;
    }

    public static Image thresholdingByScope(Image image,
            int threshold, int smallValue, int bigValue, ImageScope scope) {
        if (image == null || threshold < 0 || threshold > 255
                || smallValue < 0 || smallValue > 255 || bigValue < 0 || bigValue > 255
                || scope == null) {
            return image;
        }
        int imageWidth = (int) image.getWidth();
        int imageHeight = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(imageWidth, imageHeight);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (scope.inScope(x, y, color)) {
                    Color newColor = FxmlColorTools.thresholdingColor(color,
                            threshold, smallValue, bigValue);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image thresholdingByMatting(Image source,
            int threshold, int smallValue, int bigValue,
            List<Point> points, int distance) {
        if (source == null || threshold < 0 || threshold > 255
                || smallValue < 0 || smallValue > 255 || bigValue < 0 || bigValue > 255
                || points == null || points.isEmpty()
                || distance < 0 || distance > 255) {
            return source;
        }
        try {
            int imageWidth = (int) source.getWidth();
            int imageHeight = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelReader, 0, 0);

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<Point> queue = new LinkedList<>();

            for (Point point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    Point p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (isColorMatch(color, startColor, distance)) {
                        Color newColor = FxmlColorTools.thresholdingColor(color,
                                threshold, smallValue, bigValue);
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

}
