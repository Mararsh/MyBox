package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
    protected long threshold;
    protected int offset, percentage;

    public static enum ContrastAlgorithm {
        GrayHistogramEqualization,
        GrayHistogramStretching,
        GrayHistogramShifting,
        SaturationHistogramEqualization,
        SaturationHistogramStretching,
        SaturationHistogramShifting,
        BrightnessHistogramEqualization,
        BrightnessHistogramStretching,
        BrightnessHistogramShifting,
        SaturationBrightnessHistogramEqualization,
        SaturationBrightnessHistogramStretching,
        SaturationBrightnessHistogramShifting
    }

    public ImageContrast() {
        this.operationType = OperationType.Contrast;
        threshold = 0;
        percentage = 0;
        offset = 0;
    }

    @Override
    public BufferedImage operateImage() {
        if (image == null || operationType != OperationType.Contrast || null == algorithm) {
            return image;
        }
        switch (algorithm) {
            case GrayHistogramEqualization:
                return grayHistogramEqualization(task, image);
            case GrayHistogramStretching:
                return grayHistogramStretching(task, image, threshold, percentage);
            case GrayHistogramShifting:
                return grayHistogramShifting(task, image, offset);
            case SaturationHistogramEqualization:
                return saturationHistogramEqualization(task, image);
            case SaturationHistogramStretching:
                return saturationHistogramStretching(task, image, threshold, percentage);
            case SaturationHistogramShifting:
                return saturationHistogramShifting(task, image, offset);
            case BrightnessHistogramEqualization:
                return brightnessHistogramEqualization(task, image);
            case BrightnessHistogramStretching:
                return brightnessHistogramStretching(task, image, threshold, percentage);
            case BrightnessHistogramShifting:
                return brightnessHistogramShifting(task, image, offset);
            case SaturationBrightnessHistogramEqualization:
                return saturationBrightnessHistogramEqualization(task, image);
            case SaturationBrightnessHistogramStretching:
                return saturationBrightnessHistogramStretching(task, image, threshold, percentage);
            case SaturationBrightnessHistogramShifting:
                return saturationBrightnessHistogramShifting(task, image, offset);
            default:
                return image;
        }
    }

    // https://en.wikipedia.org/wiki/Histogram_equalization
    public static BufferedImage grayHistogramEqualization(FxTask task, BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        long[] greyHistogram = new long[256];
        int pixel, grey;
        long count = 0;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    continue;
                }
                grey = ColorConvertTools.pixel2grayValue(pixel);
                greyHistogram[grey]++;
                count++;
            }
        }
        if (count == 0) {
            return image;
        }
        float nf = 255.0f / count;
        long cumulative = 0;
        int[] lookUpTable = new int[256];
        for (int i = 0; i < 256; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            cumulative += greyHistogram[i];
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
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    grey = ColorConvertTools.pixel2grayValue(pixel);
                    grey = lookUpTable[grey];
                    target.setRGB(x, y, ColorConvertTools.rgb2Pixel(grey, grey, grey));
                }
            }
        }
        return target;
    }

    // https://blog.csdn.net/fang20277/article/details/51801093
    public static BufferedImage grayHistogramStretching(FxTask task, BufferedImage image,
            long threshold, int percentage) {
        if (image == null || threshold < 0 || percentage < 0) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        long[] greyHistogram = new long[256];
        int pixel, grey;
        long count = 0;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    continue;
                }
                grey = ColorConvertTools.pixel2grayValue(pixel);
                greyHistogram[grey]++;
                count++;
            }
        }
        if (count == 0) {
            return image;
        }
        int min = 0, max = 0;
        long v, cumulative = 0, cumulativeThreshold = width * height * percentage / 100;
        for (int i = 0; i < 256; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = greyHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                min = i;
                break;
            }
        }
        cumulative = 0;
        for (int i = 255; i >= 0; --i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = greyHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                max = i;
                break;
            }
        }
        if (min == max) {
            return null;
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        float scale = 255.0f / (max - min) + 0.5f;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    grey = ColorConvertTools.pixel2grayValue(pixel);
                    grey = (int) ((grey - min) * scale);
                    if (grey < 0) {
                        grey = 0;
                    } else if (grey > 255) {
                        grey = 255;
                    }
                    target.setRGB(x, y, ColorConvertTools.rgb2Pixel(grey, grey, grey));
                }
            }
        }
        return target;
    }

    public static BufferedImage grayHistogramShifting(FxTask task, BufferedImage image, int offset) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int pixel, grey;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    grey = ColorConvertTools.pixel2grayValue(pixel);
                    grey = Math.max(Math.min(grey + offset, 255), 0);
                    target.setRGB(x, y, ColorConvertTools.rgb2Pixel(grey, grey, grey));
                }
            }
        }
        return target;
    }

    public static BufferedImage saturationHistogramEqualization(FxTask task, BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        long[] saturationHistogram = new long[101];
        long nonTransparent = 0;
        int saturation;
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
                    continue;
                }
                nonTransparent++;
                saturation = Math.round(ColorConvertTools.getSaturation(new Color(p)) * 100);
                saturationHistogram[saturation]++;
            }
        }
        if (nonTransparent == 0) {
            return image;
        }
        float nf = 100.0f / nonTransparent;
        long cumulative = 0;
        int[] lookUpTable = new int[101];
        for (int i = 0; i < 101; ++i) {
            cumulative += saturationHistogram[i];
            lookUpTable[i] = Math.round(cumulative * nf);
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int pixel;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    saturation = Math.round(hsb[1] * 100);
                    saturation = lookUpTable[saturation];
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], saturation / 100.0f, hsb[2]));
                }
            }
        }
        return target;
    }

    public static BufferedImage saturationHistogramStretching(FxTask task, BufferedImage image,
            long threshold, int percentage) {
        if (image == null || threshold < 0 || percentage < 0) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        long[] saturationHistogram = new long[101];
        long nonTransparent = 0;
        int saturation, pixel;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    continue;
                }
                nonTransparent++;
                saturation = Math.round(ColorConvertTools.getSaturation(new Color(pixel)) * 100);
                saturationHistogram[saturation]++;
            }
        }
        if (nonTransparent == 0) {
            return image;
        }
        int min = 0, max = 0;
        long v, cumulative = 0, cumulativeThreshold = width * height * percentage / 100;
        for (int i = 0; i < 101; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = saturationHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                min = i;
                break;
            }
        }
        cumulative = 0;
        for (int i = 100; i >= 0; --i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = saturationHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                max = i;
                break;
            }
        }
        if (min == max) {
            return null;
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        float scale = 100.0f / (max - min);
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    saturation = Math.round(hsb[1] * 100);
                    if (saturation <= min) {
                        saturation = 0;
                    } else if (saturation >= max) {
                        saturation = 100;
                    } else {
                        saturation = (int) ((saturation - min) * scale);
                    }
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], saturation / 100.0f, hsb[2]));
                }
            }
        }
        return target;
    }

    public static BufferedImage saturationHistogramShifting(FxTask task, BufferedImage image, int offset) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int pixel, saturation;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    saturation = Math.round(hsb[1] * 100);
                    saturation = Math.max(Math.min(saturation + offset, 100), 0);
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], saturation / 100.0f, hsb[2]));
                }
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
        long[] brightnessHistogram = new long[101];
        long nonTransparent = 0;
        int brightness;
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
                brightness = Math.round(ColorConvertTools.getBrightness(new Color(p)) * 100);
                brightnessHistogram[brightness]++;
            }
        }

        float nf = 100.0f / nonTransparent;
        long cumulative = 0;
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
                    brightness = Math.round(hsb[2] * 100);
                    brightness = lookUpTable[brightness];
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], hsb[1], brightness / 100.0f));
                }
            }
        }
        return target;
    }

    public static BufferedImage brightnessHistogramStretching(FxTask task, BufferedImage image,
            long threshold, int percentage) {
        if (image == null || threshold < 0 || percentage < 0) {
            return null;
        }
        int brightness;
        int width = image.getWidth();
        int height = image.getHeight();
        long[] brightnessHistogram = new long[101];
        long nonTransparent = 0;
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
                    continue;
                }
                nonTransparent++;
                brightness = Math.round(ColorConvertTools.getBrightness(new Color(p)) * 100);
                brightnessHistogram[brightness]++;
            }
        }
        if (nonTransparent == 0) {
            return image;
        }
        int min = 0, max = 0;
        long v, cumulative = 0, cumulativeThreshold = width * height * percentage / 100;
        for (int i = 0; i < 101; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = brightnessHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                min = i;
                break;
            }
        }
        cumulative = 0;
        for (int i = 100; i >= 0; --i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = brightnessHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                max = i;
                break;
            }
        }
        if (min == max) {
            return null;
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        float scale = 100.0f / (max - min);
        int pixel;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    brightness = Math.round(hsb[2] * 100);
                    if (brightness <= min) {
                        brightness = 0;
                    } else if (brightness >= max) {
                        brightness = 100;
                    } else {
                        brightness = (int) ((brightness - min) * scale);
                    }
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], hsb[1], brightness / 100.0f));
                }
            }
        }
        return target;
    }

    public static BufferedImage brightnessHistogramShifting(FxTask task, BufferedImage image, int offset) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int pixel, brightness;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    brightness = Math.round(hsb[2] * 100);
                    brightness = Math.max(Math.min(brightness + offset, 100), 0);
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], hsb[1], brightness / 100.0f));
                }
            }
        }
        return target;
    }

    public static BufferedImage saturationBrightnessHistogramEqualization(FxTask task, BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        long[] saturationHistogram = new long[101];
        long[] brightnessHistogram = new long[101];
        long nonTransparent = 0;
        int pixel, saturation, brightness;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    continue;
                }
                nonTransparent++;
                float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                saturation = Math.round(hsb[1] * 100);
                saturationHistogram[saturation]++;
                brightness = Math.round(hsb[2] * 100);
                brightnessHistogram[brightness]++;
            }
        }
        if (nonTransparent == 0) {
            return image;
        }
        float nf = 100.0f / nonTransparent;
        long sCumulative = 0, bCumulative = 0;
        int[] sLlookUpTable = new int[101], bLlookUpTable = new int[101];
        for (int i = 0; i < 101; ++i) {
            sCumulative += saturationHistogram[i];
            sLlookUpTable[i] = Math.round(sCumulative * nf);

            bCumulative += brightnessHistogram[i];
            bLlookUpTable[i] = Math.round(bCumulative * nf);
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
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    saturation = Math.round(hsb[1] * 100);
                    saturation = sLlookUpTable[saturation];
                    brightness = Math.round(hsb[2] * 100);
                    brightness = bLlookUpTable[brightness];
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], saturation / 100.0f, brightness / 100.0f));
                }
            }
        }
        return target;
    }

    public static BufferedImage saturationBrightnessHistogramStretching(FxTask task, BufferedImage image,
            long threshold, int percentage) {
        if (image == null || threshold < 0 || percentage < 0) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        long[] saturationHistogram = new long[101];
        long[] brightnessHistogram = new long[101];
        long nonTransparent = 0;
        int pixel, saturation, brightness;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    continue;
                }
                nonTransparent++;
                float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                saturation = Math.round(hsb[1] * 100);
                saturationHistogram[saturation]++;
                brightness = Math.round(hsb[2] * 100);
                brightnessHistogram[brightness]++;
            }
        }
        if (nonTransparent == 0) {
            return image;
        }
        int sMin = 0, sMax = 0, bMin = 0, bMax = 0;
        long v, cumulative = 0, cumulativeThreshold = width * height * percentage / 100;
        for (int i = 0; i < 101; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = saturationHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                sMin = i;
                break;
            }
        }
        cumulative = 0;
        for (int i = 100; i >= 0; --i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = saturationHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                sMax = i;
                break;
            }
        }
        cumulative = 0;
        for (int i = 0; i < 101; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = brightnessHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                bMin = i;
                break;
            }
        }
        cumulative = 0;
        for (int i = 100; i >= 0; --i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            v = brightnessHistogram[i];
            cumulative += v;
            if (v > threshold || cumulative > cumulativeThreshold) {
                bMax = i;
                break;
            }
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        float sScale = sMin == sMax ? -1 : (100.0f / (sMax - sMin));
        float bScale = bMin == bMax ? -1 : (100.0f / (bMax - bMin));
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    saturation = Math.round(hsb[1] * 100);
                    if (sScale > 0) {
                        if (saturation <= sMin) {
                            saturation = 0;
                        } else if (saturation >= sMax) {
                            saturation = 100;
                        } else {
                            saturation = (int) ((saturation - sMin) * sScale);
                        }
                    }
                    brightness = Math.round(hsb[2] * 100);
                    if (bScale > 0) {
                        if (brightness <= bMin) {
                            brightness = 0;
                        } else if (brightness >= bMax) {
                            brightness = 100;
                        } else {
                            brightness = (int) ((brightness - bMin) * bScale);
                        }
                    }
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], saturation / 100.0f, brightness / 100.0f));
                }
            }
        }
        return target;
    }

    public static BufferedImage saturationBrightnessHistogramShifting(FxTask task, BufferedImage image, int offset) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int pixel, saturation, brightness;
        for (int y = 0; y < height; y++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            for (int x = 0; x < width; x++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                pixel = image.getRGB(x, y);
                if (pixel == 0) {
                    target.setRGB(x, y, 0);
                } else {
                    float[] hsb = ColorConvertTools.color2hsb(new Color(pixel));
                    saturation = Math.round(hsb[1] * 100);
                    saturation = Math.max(Math.min(saturation + offset, 100), 0);
                    brightness = Math.round(hsb[2] * 100);
                    brightness = Math.max(Math.min(brightness + offset, 100), 0);
                    target.setRGB(x, y, Color.HSBtoRGB(hsb[0], saturation / 100.0f, brightness / 100.0f));
                }
            }
        }
        return target;
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

    public long getThreshold() {
        return threshold;
    }

    public ImageContrast setThreshold(long thresholdValue) {
        this.threshold = thresholdValue;
        return this;
    }

    public int getPercentage() {
        return percentage;
    }

    public ImageContrast setPercentage(int percentage) {
        this.percentage = percentage;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public ImageContrast setOffset(int offset) {
        this.offset = offset;
        return this;
    }

}
