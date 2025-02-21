package mara.mybox.image.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import mara.mybox.color.ColorMatch;
import mara.mybox.data.ListKMeans;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2025-2-20
 * @License Apache License Version 2.0
 */
public class ImageKMeans extends ListKMeans<Color> {

    protected ColorMatch colorMatch;
    protected Color[] colors;
    protected int dataSize;   // can not handle big image

    public ImageKMeans() {
    }

    public static ImageKMeans create() {
        return new ImageKMeans();
    }

    public ImageKMeans init(BufferedImage image, ColorMatch match) {
        try {
            int w = image.getWidth();
            int h = image.getHeight();
            colorMatch = match != null ? match : new ColorMatch();
            dataSize = w * h;
            colors = new Color[dataSize];
            int index = 0;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    colors[index++] = new Color(image.getRGB(x, y));
                }
            }
            return this;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isDataEmpty() {
        return colors == null;
    }

    @Override
    public int dataSize() {
        return dataSize;
    }

    @Override
    public Color getData(int index) {
        try {
            return colors[index];
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Color> allData() {
        return Arrays.asList(colors);
    }

    @Override
    public boolean run() {
        if (colorMatch == null || isDataEmpty()) {
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
            Color color;
            int r = 0, g = 0, b = 0;
            for (Integer index : cluster) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                color = getData(index);
                if (color == null) {
                    continue;
                }
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
            }
            int size = cluster.size();
            color = new Color(r / size, g / size, b / size);
            return color;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        get/set
     */
    public double getThreshold() {
        return colorMatch.getThreshold();
    }

    public ImageKMeans setThreshold(double threshold) {
        colorMatch.setThreshold(threshold);
        return this;
    }

}
