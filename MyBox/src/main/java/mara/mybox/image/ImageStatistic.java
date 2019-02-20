package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.IntStatistic;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2019-2-8 19:26:01
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageStatistic {

    private BufferedImage image;

    public ImageStatistic() {
    }

    public ImageStatistic(BufferedImage image) {
        this.image = image;
    }

    public int[] grayHistogram() {
        return grayHistogram(image);
    }

    public static int[] grayHistogram(BufferedImage image) {
        if (image == null) {
            return null;
        }
        BufferedImage grayImage = ImageGray.byteGray(image);
        int[] histogram = new int[256];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int grey = new Color(grayImage.getRGB(x, y)).getRed();
                histogram[grey]++;
            }
        }
        return histogram;
    }

    public static Map<String, Object> analyze(BufferedImage image) {
        Map<String, Object> data = new HashMap<>();
        try {
            if (image == null) {
                return data;
            }
            int[] greyHistogram = new int[256];
            int[] redHistogram = new int[256];
            int[] greenHistogram = new int[256];
            int[] blueHistogram = new int[256];
            int[] alphaHistogram = new int[256];
            int[] hueHistogram = new int[361];
            int[] saturationHistogram = new int[101];
            int[] brightnessHistogram = new int[101];
            Color color;
            long redSum, greenSum, blueSum, alphaSum, hueSum, saturationSum, brightnessSum, greySum;
            redSum = greenSum = blueSum = alphaSum = hueSum = saturationSum = brightnessSum = greySum = 0;
            int redMaximum, greenMaximum, blueMaximum, alphaMaximum, hueMaximum, saturationMaximum, brightnessMaximum, greyMaximum;
            redMaximum = greenMaximum = blueMaximum = alphaMaximum = hueMaximum = saturationMaximum = brightnessMaximum = greyMaximum = 0;
            int redMinimum, greenMinimum, blueMinimum, alphaMinimum, hueMinimum, saturationMinimum, brightnessMinimum, greyMinimum;
            redMinimum = greenMinimum = blueMinimum = alphaMinimum = hueMinimum = saturationMinimum = brightnessMinimum = greyMinimum = 255;
            int v;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    color = new Color(image.getRGB(x, y));

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
                    greyHistogram[v]++;
                    greySum += v;
                    if (v > greyMaximum) {
                        greyMaximum = v;
                    }
                    if (v < greyMinimum) {
                        greyMinimum = v;
                    }

                }
            }
            data.put("greyHistogram", greyHistogram);
            data.put("redHistogram", redHistogram);
            data.put("greenHistogram", greenHistogram);
            data.put("blueHistogram", blueHistogram);
            data.put("alphaHistogram", alphaHistogram);
            data.put("hueHistogram", hueHistogram);
            data.put("saturationHistogram", saturationHistogram);
            data.put("brightnessHistogram", brightnessHistogram);

            long pxielsNumber = image.getWidth() * image.getHeight();
            int redMean = (int) (redSum / pxielsNumber);
            int greenMean = (int) (greenSum / pxielsNumber);
            int blueMean = (int) (blueSum / pxielsNumber);
            int alphaMean = (int) (alphaSum / pxielsNumber);
            int hueMean = (int) (hueSum / pxielsNumber);
            int saturationMean = (int) (saturationSum / pxielsNumber);
            int brightnessMean = (int) (brightnessSum / pxielsNumber);
            int greyMean = (int) (greySum / pxielsNumber);

            long redVariable, greenVariable, blueVariable, alphaVariable, hueVariable, saturationVariable, brightnessVariable, greyVariable;
            redVariable = greenVariable = blueVariable = alphaVariable = hueVariable = saturationVariable = brightnessVariable = greyVariable = 0;
            long redSkewness, greenSkewness, blueSkewness, alphaSkewness, hueSkewness, saturationSkewness, brightnessSkewness, greySkewness;
            redSkewness = greenSkewness = blueSkewness = alphaSkewness = hueSkewness = saturationSkewness = brightnessSkewness = greySkewness = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    color = new Color(image.getRGB(x, y));

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
                    greyVariable += Math.pow(v - greyMean, 2);
                    greySkewness += Math.pow(v - greyMean, 3);

                }
            }

            redVariable = (int) Math.sqrt(redVariable / pxielsNumber);
            greenVariable = (int) Math.sqrt(greenVariable / pxielsNumber);
            blueVariable = (int) Math.sqrt(blueVariable / pxielsNumber);
            alphaVariable = (int) Math.sqrt(alphaVariable / pxielsNumber);
            hueVariable = (int) Math.sqrt(hueVariable / pxielsNumber);
            saturationVariable = (int) Math.sqrt(saturationVariable / pxielsNumber);
            brightnessVariable = (int) Math.sqrt(brightnessVariable / pxielsNumber);
            greyVariable = (int) Math.sqrt(greyVariable / pxielsNumber);

            redSkewness = (int) Math.pow(redSkewness / pxielsNumber, 1.0 / 3);
            greenSkewness = (int) Math.pow(greenSkewness / pxielsNumber, 1.0 / 3);
            blueSkewness = (int) Math.pow(blueSkewness / pxielsNumber, 1.0 / 3);
            alphaSkewness = (int) Math.pow(alphaSkewness / pxielsNumber, 1.0 / 3);
            hueSkewness = (int) Math.pow(hueSkewness / pxielsNumber, 1.0 / 3);
            saturationSkewness = (int) Math.pow(saturationSkewness / pxielsNumber, 1.0 / 3);
            brightnessSkewness = (int) Math.pow(brightnessSkewness / pxielsNumber, 1.0 / 3);
            greySkewness = (int) Math.pow(greySkewness / pxielsNumber, 1.0 / 3);

            List<IntStatistic> statistic = new ArrayList<>();
            statistic.add(new IntStatistic("Grey", greySum, greyMean, (int) greyVariable, (int) greySkewness,
                    greyMinimum, greyMaximum, IntStatistic.maximumIndex(greyHistogram)));
            statistic.add(new IntStatistic("Red", redSum, redMean, (int) redVariable, (int) redSkewness,
                    redMinimum, redMaximum, IntStatistic.maximumIndex(redHistogram)));
            statistic.add(new IntStatistic("Green", greenSum, greenMean, (int) greenVariable, (int) greenSkewness,
                    greenMinimum, greenMaximum, IntStatistic.maximumIndex(greenHistogram)));
            statistic.add(new IntStatistic("Blue", blueSum, blueMean, (int) blueVariable, (int) blueSkewness,
                    blueMinimum, blueMaximum, IntStatistic.maximumIndex(blueHistogram)));
            statistic.add(new IntStatistic("Alpha", alphaSum, alphaMean, (int) alphaVariable, (int) alphaSkewness,
                    alphaMinimum, alphaMaximum, IntStatistic.maximumIndex(alphaHistogram)));
            statistic.add(new IntStatistic("Hue", hueSum, hueMean, (int) hueVariable, (int) hueSkewness,
                    hueMinimum, hueMaximum, IntStatistic.maximumIndex(hueHistogram)));
            statistic.add(new IntStatistic("Saturation", saturationSum, saturationMean, (int) saturationVariable, (int) saturationSkewness,
                    saturationMinimum, saturationMaximum, IntStatistic.maximumIndex(saturationHistogram)));
            statistic.add(new IntStatistic("Brightness", brightnessSum, brightnessMean, (int) brightnessVariable, (int) brightnessSkewness,
                    brightnessMinimum, brightnessMaximum, IntStatistic.maximumIndex(brightnessHistogram)));

            data.put("statistic", statistic);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return data;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
