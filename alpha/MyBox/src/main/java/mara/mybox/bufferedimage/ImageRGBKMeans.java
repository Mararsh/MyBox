package mara.mybox.bufferedimage;

import java.awt.Color;
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
    protected int equalDistance = 16;

    public static ImageRGBKMeans create() {
        return new ImageRGBKMeans();
    }

    public ImageRGBKMeans init(KMeansRegionQuantization quantization) {
        try {
            if (quantization == null) {
                return this;
            }
            regionQuantization = quantization;
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
                if (task != null && !task.isWorking()) {
                    return;
                }
                centers.add(data.get(i));
                if (centers.size() == k) {
                    return;
                }
            }
            while (centers.size() < k) {
                if (task != null && !task.isWorking()) {
                    return;
                }
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
    public boolean run() {
        if (regionQuantization == null || data == null
                || regionQuantization.rgbPalette.counts == null) {
            return false;
        }
        return super.run();
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
                if (task != null && !task.isWorking()) {
                    return null;
                }
                Color regionColor = data.get(index);
                Long colorCount = regionQuantization.rgbPalette.counts.get(regionColor);
                if (colorCount != null && colorCount > maxCount) {
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
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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
    public int getEqualDistance() {
        return equalDistance;
    }

    public ImageRGBKMeans setEqualDistance(int equalDistance) {
        this.equalDistance = equalDistance;
        return this;
    }

    public KMeansRegionQuantization getRegionQuantization() {
        return regionQuantization;
    }

    public ImageRGBKMeans setRegionQuantization(KMeansRegionQuantization regionQuantization) {
        this.regionQuantization = regionQuantization;
        return this;
    }

}
