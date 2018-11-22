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
import mara.mybox.image.ImageConvertTools;
import mara.mybox.image.ImageEffectTools;
import mara.mybox.objects.ConvolutionKernel;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.IntPoint;
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

    public enum EffectsOperationType {
        Blur, Sharpen, Clarity, Emboss, EdgeDetect, Thresholding, Posterizing, Gray, BlackOrWhite, Sepia
    }

    public static Image applyConvolution(Image image, ConvolutionKernel kernel, ImageScope scope) {
        if (image == null) {
            return null;
        }
        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
            return applyConvolution(image, kernel);
        } else if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            return applyConvolutionByMatting(image, kernel, scope);
        } else {
            return applyConvolutionByScope(image, kernel, scope);
        }
    }

    public static Image applyConvolution(Image image, ConvolutionKernel kernel) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageEffectTools.applyConvolution(source, kernel);
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

    public static Image applyConvolutionByMatting(Image source, ConvolutionKernel kernel, ImageScope scope) {
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

            int imageWidth = (int) source.getWidth();
            int imageHeight = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();

            boolean excluded = scope.isColorExcluded();
            float[][] matrix = kernel.getMatrix();
            int radiusX = matrix[0].length / 2, radiusY = matrix.length / 2,
                    maxX = imageWidth - 1, maxY = imageHeight - 1;
            boolean keepOpacity = (kernel.getType() != ConvolutionKernel.Convolution_Type.EMBOSS
                    && kernel.getType() != ConvolutionKernel.Convolution_Type.EDGE_DETECTION);
            boolean isEmboss = (kernel.getType() == ConvolutionKernel.Convolution_Type.EMBOSS);
            boolean isGray = (kernel.getGray() > 0);

            if (excluded) {
                for (int y = 0; y < imageHeight; y++) {
                    for (int x = 0; x < imageWidth; x++) {
                        applyConvolutionByMatting(pixelReader, pixelWriter, kernel, scope, imageWidth, imageHeight,
                                radiusX, radiusY, maxX, maxY, keepOpacity, isEmboss, isGray,
                                x, y);
                    }
                }
            } else {
                pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelReader, 0, 0);
            }

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<IntPoint> queue = new LinkedList<>();

            for (IntPoint point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (FxmlColorTools.isColorMatch(color, startColor, distance)) {
                        if (excluded) {
                            pixelWriter.setColor(x, y, color);
                        } else {
                            applyConvolutionByMatting(pixelReader, pixelWriter, kernel, scope, imageWidth, imageHeight,
                                    radiusX, radiusY, maxX, maxY, keepOpacity, isEmboss, isGray,
                                    x, y);
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

    public static void applyConvolutionByMatting(PixelReader pixelReader, PixelWriter pixelWriter,
            ConvolutionKernel kernel, ImageScope scope,
            int imageWidth, int imageHeight,
            int radiusX, int radiusY, int maxX, int maxY,
            boolean keepOpacity, boolean isEmboss, boolean isGray,
            int x, int y) {
        float[][] matrix = kernel.getMatrix();
        int edge_op = kernel.getEdge();
        Color color = pixelReader.getColor(x, y);
        if (color == Color.TRANSPARENT) {
            pixelWriter.setColor(x, y, color);
            return;
        }
        if (x < radiusX || x + radiusX > maxX
                || y < radiusY || y + radiusY > maxY) {
            if (edge_op == ConvolutionKernel.Edge_Op.COPY) {
                pixelWriter.setColor(x, y, color);
                return;
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

    public static Image applyConvolution(Image image, float[][] kernel, int edge_op, boolean keepOpacity) {
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

    public static Image posterizingImage(Image image, int size, ImageScope scope) {
        if (image == null || size < 0) {
            return image;
        }
        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
            return FxmlEffectTools.posterizingImage(image, size);
        } else if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            return FxmlEffectTools.posterizingByMatting(image, size, scope);
        } else {
            return FxmlEffectTools.posterizingByScope(image, size, scope);
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

    public static Image posterizingByMatting(Image source, int size, ImageScope scope) {
        try {
            if (source == null || size < 0 || scope == null) {
                return source;
            }
            List<IntPoint> points = scope.getPoints();
            double distance = scope.getColorDistance();
            if (points == null || points.isEmpty()
                    || distance < 0 || distance > 1) {
                return source;
            }
            int imageWidth = (int) source.getWidth();
            int imageHeight = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            boolean excluded = scope.isColorExcluded();
            if (excluded) {
                for (int y = 0; y < imageHeight; y++) {
                    for (int x = 0; x < imageWidth; x++) {
                        Color color = pixelReader.getColor(x, y);
                        Color newColor = FxmlColorTools.posterizingColor(color, size);
                        pixelWriter.setColor(x, y, newColor);
                    }
                }
            } else {
                pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelReader, 0, 0);
            }

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<IntPoint> queue = new LinkedList<>();

            for (IntPoint point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (FxmlColorTools.isColorMatch(color, startColor, distance)) {
                        if (excluded) {
                            pixelWriter.setColor(x, y, color);
                        } else {
                            Color newColor = FxmlColorTools.posterizingColor(color, size);
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

    public static Image thresholdingImage(Image image,
            int threshold, int smallValue, int bigValue, ImageScope scope) {
        if (image == null || threshold < 0 || threshold > 255
                || smallValue < 0 || smallValue > 255 || bigValue < 0 || bigValue > 255) {
            return image;
        }
        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
            return FxmlEffectTools.thresholdingImage(image, threshold, smallValue, bigValue);
        } else if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            return FxmlEffectTools.thresholdingByMatting(image, threshold, smallValue, bigValue, scope);
        } else {
            return FxmlEffectTools.thresholdingByScope(image, threshold, smallValue, bigValue, scope);
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
            int threshold, int smallValue, int bigValue, ImageScope scope) {
        try {
            if (source == null || threshold < 0 || threshold > 255
                    || smallValue < 0 || smallValue > 255 || bigValue < 0 || bigValue > 255) {
                return source;
            }
            List<IntPoint> points = scope.getPoints();
            double distance = scope.getColorDistance();
            if (points == null || points.isEmpty()
                    || distance < 0 || distance > 1) {
                return source;
            }

            int imageWidth = (int) source.getWidth();
            int imageHeight = (int) source.getHeight();
            PixelReader pixelReader = source.getPixelReader();
            WritableImage newImage = new WritableImage(imageWidth, imageHeight);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            boolean excluded = scope.isColorExcluded();
            if (excluded) {
                for (int y = 0; y < imageHeight; y++) {
                    for (int x = 0; x < imageWidth; x++) {
                        Color color = pixelReader.getColor(x, y);
                        Color newColor = FxmlColorTools.thresholdingColor(color,
                                threshold, smallValue, bigValue);
                        pixelWriter.setColor(x, y, newColor);
                    }
                }
            } else {
                pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelReader, 0, 0);
            }

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<IntPoint> queue = new LinkedList<>();

            for (IntPoint point : points) {
                Color startColor = pixelReader.getColor(point.getX(), point.getY());
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    Color color = pixelReader.getColor(x, y);
                    if (FxmlColorTools.isColorMatch(color, startColor, distance)) {
                        if (excluded) {
                            pixelWriter.setColor(x, y, color);
                        } else {
                            Color newColor = FxmlColorTools.thresholdingColor(color,
                                    threshold, smallValue, bigValue);
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

    public static Image makeColor(Image source, EffectsOperationType type,
            double value, ImageScope scope) {
        if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
            return makeColorByMatting(source, type, value, scope);
        } else {
            return makeColorByScope(source, type, value, scope);
        }
    }

    public static Image makeColorByScope(Image source, EffectsOperationType type,
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
                    Color newColor = makeColor(color, type, value);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeColorByMatting(Image source, EffectsOperationType type,
            double value, ImageScope scope) {
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
                        Color newColor = makeColor(color, type, value);
                        pixelWriter.setColor(x, y, newColor);
                    }
                }
            } else {
                pixelWriter.setPixels(0, 0, width, height, pixelReader, 0, 0);
            }

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
                        if (excluded) {
                            pixelWriter.setColor(x, y, color);
                        } else {
                            Color newColor = makeColor(color, type, value);
                            pixelWriter.setColor(x, y, newColor);
                        }
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

    public static Color makeColor(Color color, EffectsOperationType type, double value) {
        Color newColor = color;
        switch (type) {
            case Gray:
                newColor = color.grayscale();  //  JDK internal: 0.21 * red + 0.71 * green + 0.07 * blue
                break;
            case BlackOrWhite:
                double gray = 21.26 * color.getRed() + 71.52 * color.getGreen() + 7.22 * color.getBlue();
                if (gray < value) {
                    newColor = Color.BLACK;
                } else {
                    newColor = Color.WHITE;
                }
                break;
            case Sepia:
                newColor = FxmlColorTools.pixel2Sepia(color, value);
                break;
            default:
                break;
        }
        return newColor;

    }

}
