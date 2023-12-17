package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-2-15
 * @License Apache License Version 2.0
 */
public class ImageBinary extends PixelsOperation {

    protected BinaryAlgorithm algorithm;
    private BufferedImage defaultBinary;
    private int threashold, blackInt;

    public static enum BinaryAlgorithm {
        OTSU, Threshold, Default
    }

    public ImageBinary() {
        init();
    }

    final public void init() {
        intPara1 = -1;
        defaultBinary = null;
        operationType = OperationType.BlackOrWhite;
        algorithm = BinaryAlgorithm.Default;
        blackInt = Color.BLACK.getRGB();
    }

    public ImageBinary(Image image) {
        init();
        this.image = SwingFXUtils.fromFXImage(image, null);
    }

    @Override
    public BufferedImage operate() {
        if (image == null || operationType == null
                || operationType != OperationType.BlackOrWhite) {
            return image;
        }
        threashold = -1;
        if (algorithm == BinaryAlgorithm.OTSU) {
            threashold = threshold(task, image);
        } else if (algorithm == BinaryAlgorithm.Threshold) {
            threashold = intPara1;
        }
        if (threashold <= 0) {
            algorithm = BinaryAlgorithm.Default;
            defaultBinary = byteBinary(task, image);
        }
        return operateImage();
    }

    @Override
    protected Color operateColor(Color color) {
        Color newColor;
        if (algorithm == BinaryAlgorithm.Default) {
            if (blackInt == defaultBinary.getRGB(currentX, currentY)) {
                newColor = Color.BLACK;
            } else {
                newColor = Color.WHITE;
            }
        } else {
            if (ColorConvertTools.color2grayValue(color) < threashold) {
                newColor = Color.BLACK;
            } else {
                newColor = Color.WHITE;
            }
        }
        return newColor;
    }

    /*
        static
     */
    public static BufferedImage byteBinary(FxTask task, BufferedImage srcImage) {
        try {
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            BufferedImage naImage = AlphaTools.removeAlpha(task, srcImage, Color.WHITE);
            BufferedImage binImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g = binImage.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.drawImage(naImage, 0, 0, null);
            g.dispose();
            return binImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage intBinary(FxTask task, BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            int threshold = threshold(task, image);
            if (threshold < 0) {
                return null;
            }
            int WHITE = Color.WHITE.getRGB();
            int BLACK = Color.BLACK.getRGB();
            BufferedImage binImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int x = 0; x < width; x++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int p = image.getRGB(x, y);
                    if (p == 0) {
                        binImage.setRGB(x, y, 0);
                    } else {
                        int grey = ColorConvertTools.pixel2grayValue(p);
                        if (grey > threshold) {
                            binImage.setRGB(x, y, WHITE);
                        } else {
                            binImage.setRGB(x, y, BLACK);
                        }
                    }
                }
            }
            return binImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return image;
        }
    }

    public static Image binary(FxTask task, Image image) {
        try {
            BufferedImage bm = SwingFXUtils.fromFXImage(image, null);
            bm = byteBinary(task, bm);
            return SwingFXUtils.toFXImage(bm, null);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return image;
        }
    }

    //  OTSU algorithm: ICV=PA∗(MA−M)2+PB∗(MB−M)2
    // https://blog.csdn.net/taoyanbian1022/article/details/9030825
    // https://blog.csdn.net/liyuanbhu/article/details/49387483
    public static int threshold(FxTask task, BufferedImage image) {
        try {
            if (image == null) {
                return -1;
            }
            int width = image.getWidth();
            int height = image.getHeight();
            int[] grayNumber = new int[256];
            float pixelTotal = 0;
            for (int i = 0; i < width; ++i) {
                if (task != null && !task.isWorking()) {
                    return -1;
                }
                for (int j = 0; j < height; ++j) {
                    if (task != null && !task.isWorking()) {
                        return -1;
                    }
                    int p = image.getRGB(i, j);
                    if (p == 0) {
                        continue;
                    }
                    pixelTotal++;
                    int gray = ColorConvertTools.pixel2grayValue(p);
                    grayNumber[gray]++;
                }
            }

            float[] grayRadio = new float[256];
            for (int i = 0; i < 256; ++i) {
                if (task != null && !task.isWorking()) {
                    return -1;
                }
                grayRadio[i] = grayNumber[i] / pixelTotal;
            }

            float backgroundNumber, foregroundNumber, backgoundValue, foregroundValue;
            float backgoundAverage, foregroundAverage, imageAverage, delta, deltaMax = 0;
            int threshold = 0;
            for (int gray = 0; gray < 256; gray++) {
                if (task != null && !task.isWorking()) {
                    return -1;
                }
                backgroundNumber = 0;
                foregroundNumber = 0;
                backgoundValue = 0;
                foregroundValue = 0;
                for (int i = 0; i < 256; ++i) {
                    if (task != null && !task.isWorking()) {
                        return -1;
                    }
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
//            MyBoxLog.debug("threshold:" + threshold);
            return threshold;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int threshold(FxTask task, File file) {
        try {
            BufferedImage bufferImage = ImageFileReaders.readImage(task, file);
            return threshold(task, bufferImage);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int threshold(FxTask task, Image image) {
        try {
            return threshold(task, SwingFXUtils.fromFXImage(image, null));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    /*
        get/set
     */
    public BinaryAlgorithm getAlgorithm() {
        return algorithm;
    }

    public ImageBinary setAlgorithm(BinaryAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public int getThreashold() {
        return threashold;
    }

}
