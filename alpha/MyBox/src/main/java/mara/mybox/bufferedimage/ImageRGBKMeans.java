package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansRegionQuantization;
import mara.mybox.data.ListKMeans;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-10-7
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageRGBKMeans extends ListKMeans<Color> {

    protected KMeansRegionQuantization regionQuantization;
    protected int regionSize;
    protected int weight1, weight2, weight3;
    protected BufferedImage image;
    protected int equalDistance = 16;
    protected ImageScope scope;
    protected boolean isDithering;

    public static ImageRGBKMeans create() {
        return new ImageRGBKMeans();
    }

    public ImageRGBKMeans init() {
        try {
            if (image == null || k <= 0) {
                return this;
            }
            regionQuantization = KMeansRegionQuantization.create(image,
                    scope, isDithering, regionSize, weight1, weight2, weight3);
            regionQuantization.operate();
            data = regionQuantization.regionColors;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return this;
    }

    @Override
    public void initCenters() {
        try {
            centers = new ArrayList<>();
            int dataSize = data.size();
            if (dataSize < k) {
                centers.addAll(data);
                return;
            }
            int mod = data.size() / k;
            for (int i = 0; i < dataSize; i = i + mod) {
                centers.add(data.get(i));
                if (centers.size() == k) {
                    return;
                }
            }
            while (centers.size() < k) {
                int index = new Random().nextInt(dataSize);
                Color d = data.get(index);
                if (!centers.contains(d)) {
                    centers.add(d);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public double distance(Color p1, Color p2) {
        try {
            if (p1 == null || p2 == null) {
                return Double.MAX_VALUE;
            }
            return ColorMatchTools.calculateColorDistanceSquare(p1, p2);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return Double.MAX_VALUE;
        }
    }

    @Override
    public boolean equal(Color p1, Color p2) {
        try {
            if (p1 == null || p2 == null) {
                return false;
            }
            return ColorMatchTools.isColorMatchSquare(p1, p2, equalDistance);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public Color calculateCenters(List<Integer> cluster) {
        try {
            if (cluster == null || cluster.isEmpty()) {
                return null;
            }
            long maxCount = 0;
            Color centerColor = null;
            for (Integer index : cluster) {
                Color regionColor = data.get(index);
                long colorCount = regionQuantization.rgbPalette.counts.get(regionColor);
                if (colorCount > maxCount) {
                    centerColor = regionColor;
                    maxCount = colorCount;
                }
            }
            return centerColor;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

//    @Override
    public Color calculateCenters2(List<Integer> cluster) {
        try {
            if (cluster == null || cluster.isEmpty()) {
                return null;
            }
            long totalr = 0, totalg = 0, totalb = 0;
            for (Integer index : cluster) {
                Color regionColor = data.get(index);
                totalr += regionColor.getRed();
                totalg += regionColor.getGreen();
                totalb += regionColor.getBlue();
            }
            long size = cluster.size();
            Color centerColor = new Color(
                    Math.min(255, (int) (totalr / size)),
                    Math.min(255, (int) (totalg / size)),
                    Math.min(255, (int) (totalb / size)));
            return centerColor;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public Color map(Color color) {
        try {
            if (color.getRGB() == 0 || dataMap == null) {
                return color;
            }
            Color regionColor = regionQuantization.map(new Color(color.getRGB(), false));
            Color mappedColor = dataMap.get(regionColor);
            // Some new colors maybe generated outside regions due to dithering again
            if (mappedColor == null) {
                mappedColor = regionColor;
                int minDistance = Integer.MAX_VALUE;
                for (int i = 0; i < centers.size(); ++i) {
                    Color centerColor = centers.get(i);
                    int distance = ColorMatchTools.calculateColorDistanceSquare(regionColor, centerColor);
                    if (distance < minDistance) {
                        minDistance = distance;
                        mappedColor = centerColor;
                    }
                }
            }
            return mappedColor;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return color;
        }
    }

    /*
        get/set
     */
    public BufferedImage getImage() {
        return image;
    }

    public ImageRGBKMeans setSourceImage(BufferedImage sourceImage) {
        this.image = sourceImage;
        return this;
    }

    public int getEqualDistance() {
        return equalDistance;
    }

    public ImageRGBKMeans setEqualDistance(int equalDistance) {
        this.equalDistance = equalDistance;
        return this;
    }

    public ImageScope getScope() {
        return scope;
    }

    public ImageRGBKMeans setScope(ImageScope scope) {
        this.scope = scope;
        return this;
    }

    public boolean isIsDithering() {
        return isDithering;
    }

    public ImageRGBKMeans setIsDithering(boolean isDithering) {
        this.isDithering = isDithering;
        return this;
    }

    public KMeansRegionQuantization getRegionQuantization() {
        return regionQuantization;
    }

    public ImageRGBKMeans setRegionQuantization(KMeansRegionQuantization regionQuantization) {
        this.regionQuantization = regionQuantization;
        return this;
    }

    public int getRegionSize() {
        return regionSize;
    }

    public ImageRGBKMeans setRegionSize(int regionSize) {
        this.regionSize = regionSize;
        return this;
    }

    public int getWeight1() {
        return weight1;
    }

    public ImageRGBKMeans setWeight1(int weight1) {
        this.weight1 = weight1;
        return this;
    }

    public int getWeight2() {
        return weight2;
    }

    public ImageRGBKMeans setWeight2(int weight2) {
        this.weight2 = weight2;
        return this;
    }

    public int getWeight3() {
        return weight3;
    }

    public ImageRGBKMeans setWeight3(int weight3) {
        this.weight3 = weight3;
        return this;
    }

}
