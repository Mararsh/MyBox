package mara.mybox.image.data;

import java.awt.Color;
import java.util.List;
import mara.mybox.color.ColorMatch;
import mara.mybox.data.ListKMeans;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.data.ImageQuantizationFactory.KMeansRegionQuantization;

/**
 * @Author Mara
 * @CreateDate 2019-10-7
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageRGBKMeans extends ListKMeans<Color> {

    protected KMeansRegionQuantization regionQuantization;
    protected ColorMatch colorMatch;
    protected List<Color> colors;

    public ImageRGBKMeans() {
    }

    public static ImageRGBKMeans create() {
        return new ImageRGBKMeans();
    }

    public ImageRGBKMeans init(KMeansRegionQuantization quantization) {
        try {
            if (quantization == null) {
                return this;
            }
            regionQuantization = quantization;
            colorMatch = quantization.colorMatch;
            colors = regionQuantization.regionColors;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return this;
    }

    @Override
    public boolean isDataEmpty() {
        return colors == null || colors.isEmpty();
    }

    @Override
    public int dataSize() {
        return colors.size();
    }

    @Override
    public Color getData(int index) {
        return colors.get(index);
    }

    @Override
    public List<Color> allData() {
        return regionQuantization.regionColors;
    }

    @Override
    public boolean run() {
        if (colorMatch == null
                || regionQuantization == null || isDataEmpty()
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
            return colorMatch.distance(p1, p2);
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
            return colorMatch.isMatch(p1, p2);
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
                Color regionColor = getData(index);
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

    @Override
    public Color preProcess(Color color) {
        return regionQuantization.map(new Color(color.getRGB(), false));
    }

    /*
        get/set
     */
    public double getThreshold() {
        return colorMatch.getThreshold();
    }

    public ImageRGBKMeans setThreshold(double threshold) {
        colorMatch.setThreshold(threshold);
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
