package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2019-2-17
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageContrast extends PixelsOperation {

    protected ContrastAlgorithm algorithm;

    public static enum ContrastAlgorithm {
        Gray_Histogram_Equalization,
        Gray_Histogram_Stretching,
        Gray_Histogram_Shifting,
        Luma_Histogram_Equalization,
        HSB_Histogram_Equalization,
        Adaptive_Histogram_Equalization
    }

    public ImageContrast() {
        this.operationType = OperationType.Contrast;
    }

    @Override
    public BufferedImage operateImage() {
        if (image == null || operationType != OperationType.Contrast || null == algorithm) {
            return image;
        }
        switch (algorithm) {
            case Gray_Histogram_Equalization:
                return grayHistogramEqualization(task, image);
            case Gray_Histogram_Shifting:
                return grayHistogramShifting(task, image, intPara1);
            case Gray_Histogram_Stretching:
                return grayHistogramStretching(task, image, intPara1, intPara2);
            case Luma_Histogram_Equalization:
                return image;
            case HSB_Histogram_Equalization:
                return brightnessHistogramEqualization(task, image);
            default:
                return image;
        }
    }

    public static int[] grayHistogram(FxTask task, BufferedImage greyImage) {
        if (greyImage == null) {
            return null;
        }
        int width = greyImage.getWidth();
        int height = greyImage.getHeight();
        int[] greyHistogram = new int[256];
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int grey = new Color(greyImage.getRGB(x, y)).getRed();
                greyHistogram[grey]++;
            }
        }
        return greyHistogram;
    }

    // https://en.wikipedia.org/wiki/Histogram_equalization
    public static BufferedImage grayHistogramEqualization(FxTask task, BufferedImage image) {
        if (image == null) {
            return null;
        }
        BufferedImage grayImage = image;
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            grayImage = ImageGray.byteGray(task, image);
        }
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        int[] greyHistogram = grayHistogram(task, grayImage);
        if (greyHistogram == null) {
            return null;
        }

        float nf = 255.0f / (width * height);
        int cumulative = 0;
        int[] lookUpTable = new int[256];
        for (int i = 0; i < 256; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            cumulative += greyHistogram[i];
            lookUpTable[i] = Math.round(cumulative * nf);
        }

        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int grey = new Color(grayImage.getRGB(x, y)).getRed();
                grey = lookUpTable[grey];
                target.setRGB(x, y, ColorConvertTools.rgb2Pixel(grey, grey, grey));
            }
        }
        return target;
    }

    public static BufferedImage grayHistogramShifting(FxTask task, BufferedImage image, int offset) {
        if (image == null) {
            return null;
        }
        BufferedImage grayImage = image;
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            grayImage = ImageGray.byteGray(task, image);
            if (task != null && !task.isWorking()) {
                return null;
            }
        }
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int grey = new Color(grayImage.getRGB(x, y)).getRed();
                grey = Math.max(Math.min(grey + offset, 255), 0);
                target.setRGB(x, y, ColorConvertTools.rgb2Pixel(grey, grey, grey));
            }
        }
        return target;
    }

    public static BufferedImage grayHistogramStretching(FxTask task, BufferedImage image,
            int leftThreshold, int rightThreshold) {
        if (image == null || leftThreshold < 0 || rightThreshold < 0) {
            return null;
        }
        BufferedImage grayImage = image;
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            grayImage = ImageGray.byteGray(task, image);
        }
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        int min = 0, max = 0;
        int[] greyHistogram = grayHistogram(task, image);
        if (greyHistogram == null) {
            return null;
        }
        for (int i = 0; i < 256; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            if (greyHistogram[i] >= leftThreshold) {
                min = i;
                break;
            }
        }
        for (int i = 255; i >= 0; --i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            if (greyHistogram[i] >= rightThreshold) {
                max = i;
                break;
            }
        }
        if (min == 0 && max == 255) {
            return grayImage;
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        float scale = 255.0f / (max - min);
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int grey = new Color(grayImage.getRGB(x, y)).getRed();
                if (grey <= min) {
                    grey = 0;
                } else if (grey >= max) {
                    grey = 255;
                } else {
                    grey = (int) ((grey - min) * scale);
                }
                target.setRGB(x, y, ColorConvertTools.rgb2Pixel(grey, grey, grey));
            }
        }
        return target;
    }

    public static BufferedImage brightnessHistogramEqualization(FxTask task, BufferedImage colorImage) {
        if (colorImage == null) {
            return null;
        }
        int width = colorImage.getWidth();
        int height = colorImage.getHeight();
        int[] brightnessHistogram = new int[101];
        int nonTransparent = 0;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int p = colorImage.getRGB(x, y);
                if (p == 0) {                        // ignore transparent
                    continue;
                }
                nonTransparent++;
                int brightness = Math.round(ColorConvertTools.getBrightness(new Color(p)) * 100);
                brightnessHistogram[brightness]++;
            }
        }

        float nf = 100.0f / nonTransparent;
        int cumulative = 0;
        int[] lookUpTable = new int[101];
        for (int i = 0; i < 101; ++i) {
            cumulative += brightnessHistogram[i];
            lookUpTable[i] = Math.round(cumulative * nf);
        }

        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int p = colorImage.getRGB(x, y);
                if (p == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(p));
                    int brightness = Math.round(hsb[2] * 100);
                    brightness = lookUpTable[brightness];
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], hsb[1], brightness / 100.0f));
                }
            }
        }
        return target;
    }

    public static Image grayHistogramEqualization(FxTask task, Image grayImage) {
        BufferedImage image = SwingFXUtils.fromFXImage(grayImage, null);
        image = grayHistogramEqualization(task, image);
        if (image == null || (task != null && !task.isWorking())) {
            return null;
        }
        return SwingFXUtils.toFXImage(image, null);
    }

    public static Image brightnessHistogramEqualization(FxTask task, Image colorImage) {
        BufferedImage image = SwingFXUtils.fromFXImage(colorImage, null);
        image = brightnessHistogramEqualization(task, image);
        if (image == null || (task != null && !task.isWorking())) {
            return null;
        }
        return SwingFXUtils.toFXImage(image, null);
    }

    /*
        get/set
     */
    public ContrastAlgorithm getAlgorithm() {
        return algorithm;
    }

    public ImageContrast setAlgorithm(ContrastAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

}
