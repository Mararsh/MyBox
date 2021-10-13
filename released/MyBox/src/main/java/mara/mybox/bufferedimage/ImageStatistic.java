package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.bufferedimage.ColorComponentTools.ColorComponent;
import mara.mybox.data.IntStatistic;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-2-8 19:26:01
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageStatistic {

    protected BufferedImage image;
    protected Map<ColorComponent, ComponentStatistic> data;
    protected long nonTransparent;

    public ImageStatistic analyze() {
        try {
            if (image == null) {
                return null;
            }
            Color color;
            long redSum, greenSum, blueSum, alphaSum, hueSum, saturationSum, brightnessSum, graySum;
            redSum = greenSum = blueSum = alphaSum = hueSum = saturationSum = brightnessSum = graySum = 0;
            int redMaximum, greenMaximum, blueMaximum, alphaMaximum, hueMaximum, saturationMaximum, brightnessMaximum, grayMaximum;
            redMaximum = greenMaximum = blueMaximum = alphaMaximum = hueMaximum = saturationMaximum = brightnessMaximum = grayMaximum = 0;
            int redMinimum, greenMinimum, blueMinimum, alphaMinimum, hueMinimum, saturationMinimum, brightnessMinimum, grayMinimum;
            redMinimum = greenMinimum = blueMinimum = alphaMinimum = hueMinimum = saturationMinimum = brightnessMinimum = grayMinimum = 255;
            int v;
            int[] grayHistogram = new int[256];
            int[] redHistogram = new int[256];
            int[] greenHistogram = new int[256];
            int[] blueHistogram = new int[256];
            int[] alphaHistogram = new int[256];
            int[] hueHistogram = new int[361];
            int[] saturationHistogram = new int[101];
            int[] brightnessHistogram = new int[101];
            nonTransparent = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int p = image.getRGB(x, y);
                    if (p == 0) {
                        continue;
                    }
                    nonTransparent++;
                    color = new Color(p, true);

                    v = color.getRed();
                    redHistogram[v]++;
                    redSum += v;
                    if (v > redMaximum) {
                        redMaximum = v;
                    }
                    if (v < redMinimum) {
                        redMinimum = v;
                    }

                    v = color.getGreen();
                    greenHistogram[v]++;
                    greenSum += v;
                    if (v > greenMaximum) {
                        greenMaximum = v;
                    }
                    if (v < greenMinimum) {
                        greenMinimum = v;
                    }

                    v = color.getBlue();
                    blueHistogram[v]++;
                    blueSum += v;
                    if (v > blueMaximum) {
                        blueMaximum = v;
                    }
                    if (v < blueMinimum) {
                        blueMinimum = v;
                    }

                    v = color.getAlpha();
                    alphaHistogram[v]++;
                    alphaSum += v;
                    if (v > alphaMaximum) {
                        alphaMaximum = v;
                    }
                    if (v < alphaMinimum) {
                        alphaMinimum = v;
                    }

                    v = (int) (ColorConvertTools.getHue(color) * 360);
                    hueHistogram[v]++;
                    hueSum += v;
                    if (v > hueMaximum) {
                        hueMaximum = v;
                    }
                    if (v < hueMinimum) {
                        hueMinimum = v;
                    }

                    v = (int) (ColorConvertTools.getSaturation(color) * 100);
                    saturationHistogram[v]++;
                    saturationSum += v;
                    if (v > saturationMaximum) {
                        saturationMaximum = v;
                    }
                    if (v < saturationMinimum) {
                        saturationMinimum = v;
                    }

                    v = (int) (ColorConvertTools.getBrightness(color) * 100);
                    brightnessHistogram[v]++;
                    brightnessSum += v;
                    if (v > brightnessMaximum) {
                        brightnessMaximum = v;
                    }
                    if (v < brightnessMinimum) {
                        brightnessMinimum = v;
                    }

                    v = ColorConvertTools.color2grayValue(color);
                    grayHistogram[v]++;
                    graySum += v;
                    if (v > grayMaximum) {
                        grayMaximum = v;
                    }
                    if (v < grayMinimum) {
                        grayMinimum = v;
                    }

                }
            }
            if (nonTransparent == 0) {
                return null;
            }

            int redMean = (int) (redSum / nonTransparent);
            int greenMean = (int) (greenSum / nonTransparent);
            int blueMean = (int) (blueSum / nonTransparent);
            int alphaMean = (int) (alphaSum / nonTransparent);
            int hueMean = (int) (hueSum / nonTransparent);
            int saturationMean = (int) (saturationSum / nonTransparent);
            int brightnessMean = (int) (brightnessSum / nonTransparent);
            int grayMean = (int) (graySum / nonTransparent);

            double redVariable, greenVariable, blueVariable, alphaVariable, hueVariable, saturationVariable, brightnessVariable, grayVariable;
            redVariable = greenVariable = blueVariable = alphaVariable = hueVariable = saturationVariable = brightnessVariable = grayVariable = 0;
            double redSkewness, greenSkewness, blueSkewness, alphaSkewness, hueSkewness, saturationSkewness, brightnessSkewness, graySkewness;
            redSkewness = greenSkewness = blueSkewness = alphaSkewness = hueSkewness = saturationSkewness = brightnessSkewness = graySkewness = 0;
            double d;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int p = image.getRGB(x, y);
                    if (p == 0) {
                        continue;
                    }
                    color = new Color(p, true);

                    d = color.getRed();
                    redVariable += Math.pow(d - redMean, 2);
                    redSkewness += Math.pow(d - redMean, 3);

                    d = color.getGreen();
                    greenVariable += Math.pow(d - greenMean, 2);
                    greenSkewness += Math.pow(d - greenMean, 3);

                    d = color.getBlue();
                    blueVariable += Math.pow(d - blueMean, 2);
                    blueSkewness += Math.pow(d - blueMean, 3);

                    d = color.getAlpha();
                    alphaVariable += Math.pow(d - alphaMean, 2);
                    alphaSkewness += Math.pow(d - alphaMean, 3);

                    d = ColorConvertTools.getHue(color) * 100;
                    hueVariable += Math.pow(d - hueMean, 2);
                    hueSkewness += Math.pow(d - hueMean, 3);

                    d = ColorConvertTools.getSaturation(color) * 100;
                    saturationVariable += Math.pow(d - saturationMean, 2);
                    saturationSkewness += Math.pow(d - saturationMean, 3);

                    d = ColorConvertTools.getBrightness(color) * 100;
                    brightnessVariable += Math.pow(d - brightnessMean, 2);
                    brightnessSkewness += Math.pow(d - brightnessMean, 3);

                    d = ColorConvertTools.color2grayValue(color);
                    grayVariable += Math.pow(d - grayMean, 2);
                    graySkewness += Math.pow(d - grayMean, 3);

                }
            }

            double p = 1d / nonTransparent;
            redVariable = Math.sqrt(redVariable * p);
            greenVariable = Math.sqrt(greenVariable * p);
            blueVariable = Math.sqrt(blueVariable * p);
            alphaVariable = Math.sqrt(alphaVariable * p);
            hueVariable = Math.sqrt(hueVariable * p);
            saturationVariable = Math.sqrt(saturationVariable * p);
            brightnessVariable = Math.sqrt(brightnessVariable * p);
            grayVariable = Math.sqrt(grayVariable * p);

            redSkewness = Math.abs(Math.cbrt(redSkewness * p));
            greenSkewness = Math.abs(Math.cbrt(greenSkewness * p));
            blueSkewness = Math.abs(Math.cbrt(blueSkewness * p));
            alphaSkewness = Math.abs(Math.cbrt(alphaSkewness * p));
            hueSkewness = Math.abs(Math.cbrt(hueSkewness * p));
            saturationSkewness = Math.abs(Math.cbrt(saturationSkewness * p));
            brightnessSkewness = Math.abs(Math.cbrt(brightnessSkewness * p));
            graySkewness = Math.abs(Math.cbrt(graySkewness * p));

            IntStatistic grayStatistic = new IntStatistic(ColorComponent.Gray.name(),
                    graySum, grayMean, (int) grayVariable, (int) graySkewness,
                    grayMinimum, grayMaximum, grayHistogram);
            ComponentStatistic grayData = ComponentStatistic.create().setComponent(ColorComponent.Gray).
                    setHistogram(grayHistogram).setStatistic(grayStatistic);
            data.put(ColorComponent.Gray, grayData);

            IntStatistic redStatistic = new IntStatistic(ColorComponent.RedChannel.name(),
                    redSum, redMean, (int) redVariable, (int) redSkewness,
                    redMinimum, redMaximum, redHistogram);
            ComponentStatistic redData = ComponentStatistic.create().setComponent(ColorComponent.RedChannel).
                    setHistogram(redHistogram).setStatistic(redStatistic);
            data.put(ColorComponent.RedChannel, redData);

            IntStatistic greenStatistic = new IntStatistic(ColorComponent.GreenChannel.name(),
                    greenSum, greenMean, (int) greenVariable, (int) greenSkewness,
                    greenMinimum, greenMaximum, greenHistogram);
            ComponentStatistic greenData = ComponentStatistic.create().setComponent(ColorComponent.GreenChannel).
                    setHistogram(greenHistogram).setStatistic(greenStatistic);
            data.put(ColorComponent.GreenChannel, greenData);

            IntStatistic blueStatistic = new IntStatistic(ColorComponent.BlueChannel.name(),
                    blueSum, blueMean, (int) blueVariable, (int) blueSkewness,
                    blueMinimum, blueMaximum, blueHistogram);
            ComponentStatistic blueData = ComponentStatistic.create().setComponent(ColorComponent.BlueChannel).
                    setHistogram(blueHistogram).setStatistic(blueStatistic);
            data.put(ColorComponent.BlueChannel, blueData);

            IntStatistic hueStatistic = new IntStatistic(ColorComponent.Hue.name(),
                    hueSum, hueMean, (int) hueVariable, (int) hueSkewness,
                    hueMinimum, hueMaximum, hueHistogram);
            ComponentStatistic hueData = ComponentStatistic.create().setComponent(ColorComponent.Hue).
                    setHistogram(hueHistogram).setStatistic(hueStatistic);
            data.put(ColorComponent.Hue, hueData);

            IntStatistic saturationStatistic = new IntStatistic(ColorComponent.Saturation.name(),
                    saturationSum, saturationMean, (int) saturationVariable, (int) saturationSkewness,
                    saturationMinimum, saturationMaximum, saturationHistogram);
            ComponentStatistic saturationData = ComponentStatistic.create().setComponent(ColorComponent.Saturation).
                    setHistogram(saturationHistogram).setStatistic(saturationStatistic);
            data.put(ColorComponent.Saturation, saturationData);

            IntStatistic brightnessStatistic = new IntStatistic(ColorComponent.Brightness.name(),
                    brightnessSum, brightnessMean, (int) brightnessVariable, (int) brightnessSkewness,
                    brightnessMinimum, brightnessMaximum, brightnessHistogram);
            ComponentStatistic brightnessData = ComponentStatistic.create().setComponent(ColorComponent.Brightness).
                    setHistogram(brightnessHistogram).setStatistic(brightnessStatistic);
            data.put(ColorComponent.Brightness, brightnessData);

            IntStatistic alphaStatistic = new IntStatistic(ColorComponent.AlphaChannel.name(),
                    alphaSum, alphaMean, (int) alphaVariable, (int) alphaSkewness,
                    alphaMinimum, alphaMaximum, alphaHistogram);
            ComponentStatistic alphaData = ComponentStatistic.create().setComponent(ColorComponent.AlphaChannel).
                    setHistogram(alphaHistogram).setStatistic(alphaStatistic);
            data.put(ColorComponent.AlphaChannel, alphaData);

            return this;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public ComponentStatistic data(ColorComponent component) {
        return data.get(component);
    }

    public int[] histogram(ColorComponent component) {
        ComponentStatistic s = data.get(component);
        return s.getHistogram();
    }

    public IntStatistic statistic(ColorComponent component) {
        ComponentStatistic s = data.get(component);
        return s.getStatistic();
    }

    public static int[] grayHistogram(BufferedImage image) {
        if (image == null) {
            return null;
        }
        BufferedImage grayImage = ImageGray.byteGray(image);
        int[] histogram = new int[256];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int gray = new Color(grayImage.getRGB(x, y)).getRed();
                histogram[gray]++;
            }
        }
        return histogram;
    }

    /*
        static 
     */
    public static ImageStatistic create(BufferedImage image) {
        return new ImageStatistic().setImage(image).setData(new HashMap<>());
    }

    public static long nonTransparent(BufferedImage image) {
        if (image == null) {
            return 0;
        }
        long nonTransparent = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == 0) {
                    nonTransparent++;
                }
            }
        }
        return nonTransparent;
    }

    /*
        get/set
     */
    public BufferedImage getImage() {
        return image;
    }

    public ImageStatistic setImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    public Map<ColorComponent, ComponentStatistic> getData() {
        return data;
    }

    public ImageStatistic setData(Map<ColorComponent, ComponentStatistic> data) {
        this.data = data;
        return this;
    }

    public long getNonTransparent() {
        return nonTransparent;
    }

    public void setNonTransparent(long nonTransparent) {
        this.nonTransparent = nonTransparent;
    }

}
