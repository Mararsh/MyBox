package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.tools.MatrixTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:35:49
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConvolution extends PixelsOperation {

    protected ConvolutionKernel kernel;
    protected int matrixWidth, matrixHeight, edge_op, radiusX, radiusY, maxX, maxY;
    protected boolean keepOpacity, isEmboss, isGray;
    protected float[][] matrix;
    protected int[][] intMatrix;
    protected int sum;

    public static enum BlurAlgorithm {
        AverageBlur, GaussianBlur
    }

    public ImageConvolution() {
        this.operationType = OperationType.Convolution;
    }

    public ImageConvolution(BufferedImage image) {
        this.image = image;
        this.operationType = OperationType.Convolution;
        this.scope = null;
    }

    public ImageConvolution(BufferedImage image, ImageScope scope) {
        this.image = image;
        this.operationType = OperationType.Convolution;
        this.scope = scope;
    }

    public ImageConvolution(BufferedImage image, ConvolutionKernel kernel) {
        this.image = image;
        this.operationType = OperationType.Convolution;
        this.scope = null;
        init(kernel);
    }

    public ImageConvolution(BufferedImage image, ImageScope scope, ConvolutionKernel kernel) {
        this.image = image;
        this.operationType = OperationType.Convolution;
        this.scope = scope;
        init(kernel);
    }

    public ImageConvolution(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
    }

    public ImageConvolution(Image image, ImageScope scope) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
        this.scope = scope;
    }

    public ImageConvolution(Image image, ConvolutionKernel kernel) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
        this.scope = null;
        init(kernel);
    }

    public ImageConvolution(Image image, ImageScope scope, ConvolutionKernel kernel) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Convolution;
        this.scope = scope;
        init(kernel);
    }

    private void init(ConvolutionKernel kernel) {
        setKernel(kernel);
    }

    public void setKernel(ConvolutionKernel kernel) {
        this.kernel = kernel;
        matrix = kernel.getMatrix();
        matrixWidth = matrix[0].length;
        matrixHeight = matrix.length;
        intMatrix = new int[matrixHeight][matrixWidth];
        sum = 10000; // Run integer calcualation instead of float/double calculation
        for (int matrixY = 0; matrixY < matrixHeight; matrixY++) {
            for (int matrixX = 0; matrixX < matrixWidth; matrixX++) {
                intMatrix[matrixY][matrixX] = Math.round(matrix[matrixY][matrixX] * sum);
            }
        }
        edge_op = kernel.getEdge();
        radiusX = matrixWidth / 2;
        radiusY = matrixHeight / 2;
        maxX = image.getWidth() - 1;
        maxY = image.getHeight() - 1;
        isEmboss = (kernel.getType() == ConvolutionKernel.Convolution_Type.EMBOSS);
        isGray = (kernel.getGray() > 0);
        keepOpacity = (kernel.getType() != ConvolutionKernel.Convolution_Type.EMBOSS
                && kernel.getType() != ConvolutionKernel.Convolution_Type.EDGE_DETECTION);
    }

    @Override
    public BufferedImage operate() {
        if (image == null || kernel == null || kernel.getMatrix() == null) {
            return image;
        }
        return super.operate();
    }

    @Override
    public BufferedImage operateImage() {
        if (image == null || operationType == null) {
            return image;
        }
        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
            return applyConvolution(image, kernel);

        }
        return super.operateImage();
    }

    @Override
    protected Color operatePixel(BufferedImage target, Color color, int x, int y) {
        int pixel = image.getRGB(x, y);
        if (x < radiusX || x + radiusX > maxX
                || y < radiusY || y + radiusY > maxY) {
            if (edge_op == ConvolutionKernel.Edge_Op.COPY) {
                target.setRGB(x, y, pixel);
                return new Color(pixel, true);
            }
        }
        Color newColor = applyConvolution(x, y);
        if (isEmboss) {
            int v = 128, red, blue, green;
            red = Math.min(Math.max(newColor.getRed() + v, 0), 255);
            green = Math.min(Math.max(newColor.getGreen() + v, 0), 255);
            blue = Math.min(Math.max(newColor.getBlue() + v, 0), 255);
            newColor = new Color(red, green, blue, newColor.getAlpha());
            if (isGray) {
                newColor = ImageColor.RGB2Gray(newColor);
            }
        }
        target.setRGB(x, y, newColor.getRGB());
        return newColor;
    }

    public Color applyConvolution(int x, int y) {
        try {
            int red = 0, green = 0, blue = 0, opacity = 0;
            int convolveX, convolveY;
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
                    Color color = new Color(image.getRGB(convolveX, convolveY));
                    red += color.getRed() * intMatrix[matrixY][matrixX];
                    green += color.getGreen() * intMatrix[matrixY][matrixX];
                    blue += color.getBlue() * intMatrix[matrixY][matrixX];
                    if (keepOpacity) {
                        opacity += color.getAlpha() * intMatrix[matrixY][matrixX];
                    }
                }
            }
            red = Math.min(Math.max(red / sum, 0), 255);
            green = Math.min(Math.max(green / sum, 0), 255);
            blue = Math.min(Math.max(blue / sum, 0), 255);
            if (keepOpacity) {
                opacity = Math.min(Math.max(opacity / sum, 0), 255);
            } else {
                opacity = 255;
            }
            Color color = new Color(red, green, blue, opacity);
            return color;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static BufferedImage applyConvolution(BufferedImage source,
            ConvolutionKernel convolutionKernel) {
        BufferedImage clearedSource;
        int type = convolutionKernel.getType();
        if (type == ConvolutionKernel.Convolution_Type.EDGE_DETECTION
                || type == ConvolutionKernel.Convolution_Type.EMBOSS) {
            clearedSource = ImageManufacture.removeAlpha(source);
        } else {
            clearedSource = source;
        }
        float[] k = MatrixTools.matrix2Array(convolutionKernel.getMatrix());
        if (k == null) {
            return clearedSource;
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
        BufferedImage target = applyConvolveOp(clearedSource, imageOp);
        if (type == ConvolutionKernel.Convolution_Type.EMBOSS) {
            PixelsOperation pixelsOperation = PixelsOperation.newPixelsOperation(target, null,
                    OperationType.RGB, ColorActionType.Increase);
            pixelsOperation.setIntPara1(128);
            target = pixelsOperation.operate();
            if (convolutionKernel.getGray() > 0) {
                target = ImageGray.byteGray(target);
            }
        }
        return target;
    }

    public static BufferedImage applyConvolveOp(BufferedImage source, ConvolveOp imageOp) {
        if (source == null || imageOp == null) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        imageOp.filter(source, target);
        return target;
    }

    public ConvolutionKernel getKernel() {
        return kernel;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public void setMatrixWidth(int matrixWidth) {
        this.matrixWidth = matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public void setMatrixHeight(int matrixHeight) {
        this.matrixHeight = matrixHeight;
    }

    public int getEdge_op() {
        return edge_op;
    }

    public void setEdge_op(int edge_op) {
        this.edge_op = edge_op;
    }

    public int getRadiusX() {
        return radiusX;
    }

    public void setRadiusX(int radiusX) {
        this.radiusX = radiusX;
    }

    public int getRadiusY() {
        return radiusY;
    }

    public void setRadiusY(int radiusY) {
        this.radiusY = radiusY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public boolean isKeepOpacity() {
        return keepOpacity;
    }

    public void setKeepOpacity(boolean keepOpacity) {
        this.keepOpacity = keepOpacity;
    }

    public boolean isIsEmboss() {
        return isEmboss;
    }

    public void setIsEmboss(boolean isEmboss) {
        this.isEmboss = isEmboss;
    }

    public boolean isIsGray() {
        return isGray;
    }

    public void setIsGray(boolean isGray) {
        this.isGray = isGray;
    }

    public float[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(float[][] matrix) {
        this.matrix = matrix;
    }

}
