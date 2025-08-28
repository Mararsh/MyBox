package mara.mybox.image.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.tools.AlphaTools;
import mara.mybox.tools.FloatMatrixTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:35:49
 * @License Apache License Version 2.0
 */
public class ImageConvolution extends PixelsOperation {

    protected ConvolutionKernel kernel;
    protected int matrixWidth, matrixHeight, edge_op, color_op, radiusX, radiusY, maxX, maxY;
    protected boolean keepOpacity, isEmboss, isInvert;
    protected float[][] matrix;
    protected int[][] intMatrix;
    protected int intScale;

    public static enum SmoothAlgorithm {
        GaussianBlur, AverageBlur, MotionBlur
    }

    public static enum SharpenAlgorithm {
        UnsharpMasking, EightNeighborLaplace, FourNeighborLaplace
    }

    public ImageConvolution() {
        this.operationType = OperationType.Convolution;
    }

    public static ImageConvolution create() {
        return new ImageConvolution();
    }

    public ImageConvolution setKernel(ConvolutionKernel kernel) {
        this.kernel = kernel;
        matrix = kernel.getMatrix();
        matrixWidth = matrix[0].length;
        matrixHeight = matrix.length;
        intMatrix = new int[matrixHeight][matrixWidth];
        intScale = 10000; // Run integer calcualation instead of float/double calculation
        for (int matrixY = 0; matrixY < matrixHeight; matrixY++) {
            for (int matrixX = 0; matrixX < matrixWidth; matrixX++) {
                intMatrix[matrixY][matrixX] = Math.round(matrix[matrixY][matrixX] * intScale);
            }
        }
        edge_op = kernel.getEdge();
        radiusX = matrixWidth / 2;
        radiusY = matrixHeight / 2;
        maxX = image.getWidth() - 1;
        maxY = image.getHeight() - 1;
        isEmboss = (kernel.getType() == ConvolutionKernel.Convolution_Type.EMBOSS);
        color_op = kernel.getColor();
        keepOpacity = (kernel.getType() != ConvolutionKernel.Convolution_Type.EMBOSS
                && kernel.getType() != ConvolutionKernel.Convolution_Type.EDGE_DETECTION);
        isInvert = kernel.isInvert();
        return this;
    }

    @Override
    public BufferedImage start() {
        if (image == null || kernel == null || kernel.getMatrix() == null) {
            return image;
        }
        return super.start();
    }

    @Override
    protected BufferedImage operateImage() {
        if (image == null || operationType == null) {
            return image;
        }
        if (scope == null || scope.isWhole()) {
            return applyConvolution(task, image, kernel);
        }
        BufferedImage target = super.operateImage();
        if (kernel.isGrey()) {
            target = ImageGray.byteGray(task, target);
        } else if (kernel.isBW()) {
            target = ImageBinary.byteBinary(task, target);
        }
        return target;
    }

    @Override
    protected Color operatePixel(BufferedImage target, Color color, int x, int y) {
        Color newColor = applyConvolution(x, y);
        if (isEmboss) {
            int v = 128, red, blue, green;
            red = Math.min(Math.max(newColor.getRed() + v, 0), 255);
            green = Math.min(Math.max(newColor.getGreen() + v, 0), 255);
            blue = Math.min(Math.max(newColor.getBlue() + v, 0), 255);
            newColor = new Color(red, green, blue, newColor.getAlpha());
        }
        target.setRGB(x, y, newColor.getRGB());
        return newColor;
    }

    public Color applyConvolution(int x, int y) {
        try {
            int red = 0, green = 0, blue = 0, opacity = 0;
            int convolveX, convolveY;
            if (x < radiusX || x + radiusX > maxX
                    || y < radiusY || y + radiusY > maxY) {
                if (edge_op == ConvolutionKernel.Edge_Op.FILL_ZERO) {
                    red = green = blue = opacity = 0;
                } else {
                    /* copy */
                    Color color = new Color(image.getRGB(x, y), true);
                    red = color.getRed();
                    green = color.getGreen();
                    blue = color.getBlue();
                    opacity = color.getAlpha();
                }
            } else {
                matrix:
                for (int matrixY = 0; matrixY < matrixHeight; matrixY++) {
                    if (taskInvalid()) {
                        return null;
                    }
                    for (int matrixX = 0; matrixX < matrixWidth; matrixX++) {
                        if (taskInvalid()) {
                            return null;
                        }
                        convolveX = x - radiusX + matrixX;
                        convolveY = y - radiusY + matrixY;
                        Color color = new Color(image.getRGB(convolveX, convolveY), true);
                        red += color.getRed() * intMatrix[matrixY][matrixX];
                        green += color.getGreen() * intMatrix[matrixY][matrixX];
                        blue += color.getBlue() * intMatrix[matrixY][matrixX];
                        if (keepOpacity) {
                            opacity += color.getAlpha() * intMatrix[matrixY][matrixX];
                        }
                    }
                }
            }
            red = Math.min(Math.max(red / intScale, 0), 255);
            green = Math.min(Math.max(green / intScale, 0), 255);
            blue = Math.min(Math.max(blue / intScale, 0), 255);
            if (keepOpacity) {
                opacity = Math.min(Math.max(opacity / intScale, 0), 255);
            } else {
                opacity = 255;
            }
            Color color;
            if (isInvert) {
                color = new Color(255 - red, 255 - green, 255 - blue, opacity);
            } else {
                color = new Color(red, green, blue, opacity);
            }
            return color;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    /*
        static
     */
    public static BufferedImage applyConvolution(FxTask task, BufferedImage inSource,
            ConvolutionKernel convolutionKernel) {
        BufferedImage source;
        int type = convolutionKernel.getType();
        if (type == ConvolutionKernel.Convolution_Type.EDGE_DETECTION
                || type == ConvolutionKernel.Convolution_Type.EMBOSS) {
            source = AlphaTools.removeAlpha(task, inSource);
        } else {
            source = inSource;
        }
        if (task != null && !task.isWorking()) {
            return null;
        }
        float[] k = FloatMatrixTools.matrix2Array(task, convolutionKernel.getMatrix());
        if (task != null && !task.isWorking()) {
            return null;
        }
        if (k == null) {
            return source;
        }
        int w = convolutionKernel.getWidth();
        int h = convolutionKernel.getHeight();
        Kernel kernel = new Kernel(w, h, k);
        ConvolveOp imageOp;
        if (convolutionKernel.getEdge() == ConvolutionKernel.Edge_Op.COPY) {
            imageOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        } else {
            imageOp = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        }
        BufferedImage target = applyConvolveOp(source, imageOp);
        if (task != null && !task.isWorking()) {
            return null;
        }
        if (type == ConvolutionKernel.Convolution_Type.EMBOSS) {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(target, null,
                    OperationType.RGB, ColorActionType.Increase);
            pixelsOperation.setIntPara1(128).setTask(task);
            target = pixelsOperation.start();
            if (task != null && !task.isWorking()) {
                return null;
            }
        }
        if (convolutionKernel.isInvert()) {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(target, null,
                    OperationType.RGB, ColorActionType.Invert).setTask(task);
            target = pixelsOperation.start();
            if (task != null && !task.isWorking()) {
                return null;
            }
        }
        if (convolutionKernel.isGrey()) {
            target = ImageGray.byteGray(task, target);
        } else if (convolutionKernel.isBW()) {
            target = ImageBinary.byteBinary(task, target);
        }
        if (task != null && !task.isWorking()) {
            return null;
        }
        return target;
    }

    // source should have no alpha
    public static BufferedImage applyConvolveOp(BufferedImage source, ConvolveOp imageOp) {
        if (source == null || imageOp == null) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_RGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        imageOp.filter(source, target);
        return target;
    }

    /*
        get/set
     */
    @Override
    public ImageConvolution setImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    @Override
    public ImageConvolution setImage(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        return this;
    }

    @Override
    public ImageConvolution setScope(ImageScope scope) {
        this.scope = scope;
        return this;
    }

    public ConvolutionKernel getKernel() {
        return kernel;
    }

    public int[][] getIntMatrix() {
        return intMatrix;
    }

    public ImageConvolution setIntMatrix(int[][] intMatrix) {
        this.intMatrix = intMatrix;
        return this;
    }

    public int getIntScale() {
        return intScale;
    }

    public ImageConvolution setSum(int sum) {
        this.intScale = sum;
        return this;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public ImageConvolution setMatrixWidth(int matrixWidth) {
        this.matrixWidth = matrixWidth;
        return this;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public ImageConvolution setMatrixHeight(int matrixHeight) {
        this.matrixHeight = matrixHeight;
        return this;
    }

    public int getEdge_op() {
        return edge_op;
    }

    public ImageConvolution setEdge_op(int edge_op) {
        this.edge_op = edge_op;
        return this;
    }

    public int getRadiusX() {
        return radiusX;
    }

    public ImageConvolution setRadiusX(int radiusX) {
        this.radiusX = radiusX;
        return this;
    }

    public int getRadiusY() {
        return radiusY;
    }

    public ImageConvolution setRadiusY(int radiusY) {
        this.radiusY = radiusY;
        return this;
    }

    public int getMaxX() {
        return maxX;
    }

    public ImageConvolution setMaxX(int maxX) {
        this.maxX = maxX;
        return this;
    }

    public int getMaxY() {
        return maxY;
    }

    public ImageConvolution setMaxY(int maxY) {
        this.maxY = maxY;
        return this;
    }

    public boolean isKeepOpacity() {
        return keepOpacity;
    }

    public ImageConvolution setKeepOpacity(boolean keepOpacity) {
        this.keepOpacity = keepOpacity;
        return this;
    }

    public boolean isIsEmboss() {
        return isEmboss;
    }

    public ImageConvolution setIsEmboss(boolean isEmboss) {
        this.isEmboss = isEmboss;
        return this;
    }

    public int getColor() {
        return color_op;
    }

    public ImageConvolution setColor(int color_op) {
        this.color_op = color_op;
        return this;
    }

    public boolean isIsInvert() {
        return isInvert;
    }

    public ImageConvolution setIsInvert(boolean isInvert) {
        this.isInvert = isInvert;
        return this;
    }

    public float[][] getMatrix() {
        return matrix;
    }

    public ImageConvolution setMatrix(float[][] matrix) {
        this.matrix = matrix;
        return this;
    }

}
