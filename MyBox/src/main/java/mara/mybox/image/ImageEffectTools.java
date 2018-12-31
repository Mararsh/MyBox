package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import static mara.mybox.image.ImageConvertTools.clearAlpha;
import mara.mybox.objects.ConvolutionKernel;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 20:02:44
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageEffectTools {

    public static BufferedImage applyMatrix(BufferedImage source, float[][] matrix) {
        return applyMatrix(source, matrix, ConvolutionKernel.Edge_Op.FILL_ZERO);
    }

    public static BufferedImage applyMatrix(BufferedImage source, float[][] matrix, int edge_op) {
        if (source == null || matrix == null) {
            return source;
        }
        float[] a = ValueTools.matrix2Array(matrix);
        Kernel k = new Kernel(matrix.length, matrix[0].length, a);
        return applyKernel(source, k, edge_op);
    }

    public static BufferedImage applyKernel(BufferedImage source, Kernel filter) {
        return applyKernel(source, filter, ConvolutionKernel.Edge_Op.FILL_ZERO);
    }

    public static BufferedImage applyKernel(BufferedImage source, Kernel filter, int edge_op) {
        if (source == null || filter == null) {
            return source;
        }
        ConvolveOp imageOp;
        if (edge_op == ConvolutionKernel.Edge_Op.COPY) {
            imageOp = new ConvolveOp(filter, ConvolveOp.EDGE_NO_OP, null);
        } else {
            imageOp = new ConvolveOp(filter, ConvolveOp.EDGE_ZERO_FILL, null);
        }
        return applyConvolveOp(source, imageOp);
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

    public static BufferedImage applyConvolution(BufferedImage source, ConvolutionKernel convolutionKernel) {
        BufferedImage clearedSource;
        int type = convolutionKernel.getType();
        if (type == ConvolutionKernel.Convolution_Type.EDGE_DETECTION
                || type == ConvolutionKernel.Convolution_Type.EMBOSS) {
            clearedSource = clearAlpha(source);
        } else {
            clearedSource = source;
        }
        float[] k = ValueTools.matrix2Array(convolutionKernel.getMatrix());
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
            target = ImageAdjustColorTools.changeRGB(target, 128);
            if (convolutionKernel.getGray() > 0) {
                target = ImageGrayTools.color2Gray(target);
            }
        }
        return target;
    }

    public static BufferedImage applyMatrixLocal(BufferedImage source, float[][] matrix, int edge_op) {
        if (source == null || matrix == null || matrix.length == 0) {
            return source;
        }
        try {
            int imageWidth = source.getWidth();
            int imageHeight = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            int matrixWidth = matrix[0].length;
            int matrixHeight = matrix.length;
            BufferedImage target = new BufferedImage(imageWidth, imageHeight, imageType);
            int convolveX, convolveY, radiusX = matrixWidth / 2, radiusY = matrixHeight / 2,
                    maxX = imageWidth - 1, maxY = imageHeight - 1;
            for (int pixelY = 0; pixelY < imageHeight; pixelY++) {
                for (int pixelX = 0; pixelX < imageWidth; pixelX++) {
                    if (pixelX < radiusX || pixelX + radiusX > maxX
                            || pixelY < radiusY || pixelY + radiusY > maxY) {
                        if (edge_op == ConvolutionKernel.Edge_Op.COPY) {
                            target.setRGB(pixelX, pixelY, source.getRGB(pixelX, pixelY));
                            continue;
                        }
                    }
                    double red = 0.0, green = 0.0, blue = 0.0, alpha = 0.0;
                    for (int matrixY = 0; matrixY < matrixHeight; matrixY++) {
                        for (int matrixX = 0; matrixX < matrixWidth; matrixX++) {
                            convolveX = pixelX - radiusX + matrixX;
                            convolveY = pixelY - radiusY + matrixY;
                            if (convolveX < 0 || convolveX > maxX || convolveY < 0 || convolveY > maxY) {
                                if (edge_op == ConvolutionKernel.Edge_Op.MOD) {
                                    convolveX = (convolveX + imageWidth) % imageWidth;
                                    convolveY = (convolveY + imageHeight) % imageHeight;
                                } else {
                                    continue; // Fill_zero
                                }
                            }
                            Color color = new Color(source.getRGB(convolveX, convolveY), true);
                            red += color.getRed() * matrix[matrixY][matrixX];
                            green += color.getGreen() * matrix[matrixY][matrixX];
                            blue += color.getBlue() * matrix[matrixY][matrixX];
                            alpha += color.getAlpha() * matrix[matrixY][matrixX];
                        }
                    }
                    red = Math.min(Math.max(red, 0), 255);
                    green = Math.min(Math.max(green, 0), 255);
                    blue = Math.min(Math.max(blue, 0), 255);
                    alpha = Math.min(Math.max(alpha, 0), 255);
                    Color newColor = new Color((int) red, (int) green, (int) blue, (int) alpha);
                    target.setRGB(pixelX, pixelY, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html?page=2
    public static BufferedImage thresholding(BufferedImage source, int threshold, int smallValue, int bigValue) {
        try {
            short[] thresholdArray = new short[256];
            for (int i = 0; i < 256; i++) {
                if (i < threshold) {
                    thresholdArray[i] = (short) smallValue;
                } else {
                    thresholdArray[i] = (short) bigValue;
                }
            }
            BufferedImageOp thresholdingOp = new LookupOp(new ShortLookupTable(0, thresholdArray), null);
            return thresholdingOp.filter(source, null);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html?page=1
    public static BufferedImage posterizing(BufferedImage source, int size) {
        try {
            short[] posterize = new short[256];
            for (int i = 0; i < 256; i++) {
                posterize[i] = (short) (i - (i % size));
            }
            BufferedImageOp posterizeOp = new LookupOp(new ShortLookupTable(0, posterize), null);
            return posterizeOp.filter(source, null);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static BufferedImage sepiaImage(BufferedImage source, int sepiaIntensity) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int pixel = source.getRGB(i, j);
                    Color newColor = ImageColorTools.pixel2Sepia(pixel, sepiaIntensity);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
