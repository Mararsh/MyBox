package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.data.IntStatistic;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageColor.ColorComponent;

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

    public static ImageStatistic create(BufferedImage image) {
        return new ImageStatistic().setImage(image).setData(new HashMap<>());
    }

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

                    v = (int) (ImageColor.getHue(color) * 360);
                    hueHistogram[v]++;
                    hueSum += v;
                    if (v > hueMaximum) {
                        hueMaximum = v;
                    }
                    if (v < hueMinimum) {
                        hueMinimum = v;
                    }

                    v = (int) (ImageColor.getSaturation(color) * 100);
                    saturationHistogram[v]++;
                    saturationSum += v;
                    if (v > saturationMaximum) {
                        saturationMaximum = v;
                    }
                    if (v < saturationMinimum) {
                        saturationMinimum = v;
                    }

                    v = (int) (ImageColor.getBrightness(color) * 100);
                    brightnessHistogram[v]++;
                    brightnessSum += v;
                    if (v > brightnessMaximum) {
                        brightnessMaximum = v;
                    }
                    if (v < brightnessMinimum) {
                        brightnessMinimum = v;
                    }

                    v = ImageColor.RGB2GrayValue(color);
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

            long redVariable, greenVariable, blueVariable, alphaVariable, hueVariable, saturationVariable, brightnessVariable, grayVariable;
            redVariable = greenVariable = blueVariable = alphaVariable = hueVariable = saturationVariable = brightnessVariable = grayVariable = 0;
            long redSkewness, greenSkewness, blueSkewness, alphaSkewness, hueSkewness, saturationSkewness, brightnessSkewness, graySkewness;
            redSkewness = greenSkewness = blueSkewness = alphaSkewness = hueSkewness = saturationSkewness = brightnessSkewness = graySkewness = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int p = image.getRGB(x, y);
                    if (p == 0) {
                        continue;
                    }
                    color = new Color(p, true);

                    v = color.getRed();
                    redVariable += Math.pow(v - redMean, 2);
                    redSkewness += Math.pow(v - redMean, 3);

                    v = color.getGreen();
                    greenVariable += Math.pow(v - greenMean, 2);
                    greenSkewness += Math.pow(v - greenMean, 3);

                    v = color.getBlue();
                    blueVariable += Math.pow(v - blueMean, 2);
                    blueSkewness += Math.pow(v - blueMean, 3);

                    v = color.getAlpha();
                    alphaVariable += Math.pow(v - alphaMean, 2);
                    alphaSkewness += Math.pow(v - alphaMean, 3);

                    v = (int) (ImageColor.getHue(color) * 100);
                    hueVariable += Math.pow(v - hueMean, 2);
                    hueSkewness += Math.pow(v - hueMean, 3);

                    v = (int) (ImageColor.getSaturation(color) * 100);
                    saturationVariable += Math.pow(v - saturationMean, 2);
                    saturationSkewness += Math.pow(v - saturationMean, 3);

                    v = (int) (ImageColor.getBrightness(color) * 100);
                    brightnessVariable += Math.pow(v - brightnessMean, 2);
                    brightnessSkewness += Math.pow(v - brightnessMean, 3);

                    v = ImageColor.RGB2GrayValue(color);
                    grayVariable += Math.pow(v - grayMean, 2);
                    graySkewness += Math.pow(v - grayMean, 3);

                }
            }

            redVariable = (int) Math.sqrt(redVariable / nonTransparent);
            greenVariable = (int) Math.sqrt(greenVariable / nonTransparent);
            blueVariable = (int) Math.sqrt(blueVariable / nonTransparent);
            alphaVariable = (int) Math.sqrt(alphaVariable / nonTransparent);
            hueVariable = (int) Math.sqrt(hueVariable / nonTransparent);
            saturationVariable = (int) Math.sqrt(saturationVariable / nonTransparent);
            brightnessVariable = (int) Math.sqrt(brightnessVariable / nonTransparent);
            grayVariable = (int) Math.sqrt(grayVariable / nonTransparent);

            redSkewness = (int) Math.pow(redSkewness / nonTransparent, 1.0 / 3);
            greenSkewness = (int) Math.pow(greenSkewness / nonTransparent, 1.0 / 3);
            blueSkewness = (int) Math.pow(blueSkewness / nonTransparent, 1.0 / 3);
            alphaSkewness = (int) Math.pow(alphaSkewness / nonTransparent, 1.0 / 3);
            hueSkewness = (int) Math.pow(hueSkewness / nonTransparent, 1.0 / 3);
            saturationSkewness = (int) Math.pow(saturationSkewness / nonTransparent, 1.0 / 3);
            brightnessSkewness = (int) Math.pow(brightnessSkewness / nonTransparent, 1.0 / 3);
            graySkewness = (int) Math.pow(graySkewness / nonTransparent, 1.0 / 3);

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

    /*
        static methods
     */
    public static Color color(ColorComponent component, int index) {
        switch (component) {
            case RedChannel:
                return new Color(index, 0, 0, 255);
            case GreenChannel:
                return new Color(0, index, 0, 255);
            case BlueChannel:
                return new Color(0, 0, index, 255);
            case AlphaChannel:
                Color aColor = ImageColor.color(component);
                return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), index);
            case Gray:
                return new Color(index, index, index, 255);
            case Hue:
                return ImageColor.HSB2RGB(index / 360f, 1f, 1f);
            case Saturation:
                float h1 = ImageColor.getHue(ImageColor.color(component));
                return ImageColor.HSB2RGB(h1, index / 100f, 1f);
            case Brightness:
                float h2 = ImageColor.getHue(ImageColor.color(component));
                return ImageColor.HSB2RGB(h2, 1f, index / 100f);
        }
        return null;
    }

    public static Color color(String name, int index) {
        return color(ImageColor.component(name), index);
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
