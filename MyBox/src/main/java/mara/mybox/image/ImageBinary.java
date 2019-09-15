package mara.mybox.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.image.file.ImageFileReaders;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-2-15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageBinary extends PixelsOperation {

    protected boolean grayed, calculate;

    public ImageBinary() {
        intPara1 = -1;
    }

    public ImageBinary(BufferedImage image, OperationType operationType) {
        this.image = image;
        this.operationType = operationType;
        intPara1 = -1;
    }

    public ImageBinary(BufferedImage image, ImageScope scope, OperationType operationType) {
        this.image = image;
        this.scope = scope;
        this.operationType = operationType;
        intPara1 = -1;
    }

    public ImageBinary(BufferedImage image, int threshold) {
        this.image = image;
        this.scope = null;
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = threshold;
    }

    public ImageBinary(BufferedImage image, ImageScope scope, int threshold) {
        this.image = image;
        this.scope = scope;
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = threshold;
    }

    public ImageBinary(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = -1;
        grayed = false;
    }

    public ImageBinary(Image image, ImageScope scope) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.scope = scope;
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = -1;
        grayed = false;
    }

    public ImageBinary(Image image, int threshold) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.scope = null;
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = threshold;
        grayed = false;
    }

    public ImageBinary(Image image, ImageScope scope, int threshold) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.BlackOrWhite;
        this.scope = scope;
        intPara1 = threshold;
        grayed = false;
    }

    @Override
    public BufferedImage operate() {
        if (image == null || operationType == null
                || operationType != OperationType.BlackOrWhite) {
            return image;
        }
        grayed = false;
        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
            if (!isDithering && !calculate && intPara1 < 0) {
                return byteBinary(image);
            }
            image = ImageGray.byteGray(image);
            grayed = true;
        }
        if (intPara1 < 0) {
            if (grayed) {
                intPara1 = OTSU(image);
            } else {
                intPara1 = calculateThreshold(image);
            }
        }
        return operateImage();
    }

    @Override
    protected Color operateColor(Color color) {
        int gray;
        if (grayed) {
            gray = color.getRed();
        } else {
            gray = ImageColor.RGB2GrayValue(color);
        }
        Color newColor;
        if (gray < intPara1) {
            newColor = Color.BLACK;
        } else {
            newColor = Color.WHITE;
        }
        return newColor;
    }

    public static BufferedImage byteBinary(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage binImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D graphics = binImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            return binImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    public static BufferedImage intBinary(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage greyImage = ImageGray.intGray(image);
            int threshold = OTSU(greyImage);
            int WHITE = Color.WHITE.getRGB();
            int BLACK = Color.BLACK.getRGB();
            BufferedImage binImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int grey = new Color(greyImage.getRGB(x, y)).getRed();
                    if (grey > threshold) {
                        binImage.setRGB(x, y, WHITE);
                    } else {
                        binImage.setRGB(x, y, BLACK);
                    }
                }
            }
            return binImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    public static Image binary(Image image) {
        try {
            BufferedImage bm = SwingFXUtils.fromFXImage(image, null);
            bm = byteBinary(bm);
            return SwingFXUtils.toFXImage(bm, null);
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    //  OTSU algorithm: ICV=PA∗(MA−M)2+PB∗(MB−M)2
    // https://blog.csdn.net/taoyanbian1022/article/details/9030825
    // https://blog.csdn.net/liyuanbhu/article/details/49387483
    public static int OTSU(BufferedImage grayImage) {
        try {
            int width = grayImage.getWidth();
            int height = grayImage.getHeight();

            int[] grayNumber = new int[256];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
//                    int r = 0xFF & grayImage.getRGB(i, j);
                    int gray = ImageColor.grayPixel2GrayValue(grayImage.getRGB(i, j));
                    grayNumber[gray]++;
                }
            }

            float pixelTotal = width * height;
            float[] grayRadio = new float[256];
            for (int i = 0; i < 256; i++) {
                grayRadio[i] = grayNumber[i] / pixelTotal;
            }

            float backgroundNumber, foregroundNumber, backgoundValue, foregroundValue;
            float backgoundAverage, foregroundAverage, imageAverage, delta, deltaMax = 0;
            int threshold = 0;
            for (int gray = 0; gray < 256; gray++) {
                backgroundNumber = 0;
                foregroundNumber = 0;
                backgoundValue = 0;
                foregroundValue = 0;
                for (int i = 0; i < 256; i++) {
                    if (i <= gray) {
                        backgroundNumber += grayRadio[i];
                        backgoundValue += i * grayRadio[i];
                    } else {
                        foregroundNumber += grayRadio[i];
                        foregroundValue += i * grayRadio[i];
                    }
                }

                backgoundAverage = backgoundValue / backgroundNumber;
                foregroundAverage = foregroundValue / foregroundNumber;
                imageAverage = backgoundValue + foregroundValue;

                delta = backgroundNumber * (backgoundAverage - imageAverage) * (backgoundAverage - imageAverage)
                        + foregroundNumber * (foregroundAverage - imageAverage) * (foregroundAverage - imageAverage);

                if (delta > deltaMax) {
                    deltaMax = delta;
                    threshold = gray;
                }
            }
//            logger.debug("threshold:" + threshold);
            return threshold;

        } catch (Exception e) {
            logger.error(e.toString());
            return -1;
        }
    }

    public static int calculateThreshold(File file) {
        try {
            BufferedImage bufferImage = ImageFileReaders.readImage(file);
            return OTSU(ImageGray.byteGray(bufferImage));
        } catch (Exception e) {
            logger.error(e.toString());
            return -1;
        }
    }

    public static int calculateThreshold(BufferedImage image) {
        try {
            BufferedImage grayImage = ImageGray.byteGray(image);
            return OTSU(grayImage);
        } catch (Exception e) {
            logger.error(e.toString());
            return -1;
        }
    }

    public static int calculateThreshold(Image image) {
        try {
            BufferedImage bm = SwingFXUtils.fromFXImage(image, null);
            return calculateThreshold(bm);
        } catch (Exception e) {
            logger.error(e.toString());
            return -1;
        }
    }

    public boolean isGrayed() {
        return grayed;
    }

    public void setGrayed(boolean grayed) {
        this.grayed = grayed;
    }

    public boolean isCalculate() {
        return calculate;
    }

    public void setCalculate(boolean calculate) {
        this.calculate = calculate;
    }

}
