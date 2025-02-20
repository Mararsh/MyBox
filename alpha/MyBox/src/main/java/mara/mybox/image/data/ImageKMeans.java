package mara.mybox.image.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.color.ColorMatch;
import mara.mybox.data.ListKMeans;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2025-2-20
 * @License Apache License Version 2.0
 */
public class ImageKMeans extends ListKMeans<Integer> {

    protected ColorMatch colorMatch;
    protected BufferedImage image;
    protected int width, height, dataSize;

    public ImageKMeans() {
    }

    public static ImageKMeans create() {
        return new ImageKMeans();
    }

    public ImageKMeans init(BufferedImage image, ColorMatch colorMatch) {
        this.image = image;
        this.colorMatch = colorMatch;
        if (image != null) {
            width = image.getWidth();
            height = image.getHeight();
            dataSize = width * height;
        }
        return this;
    }

    @Override
    public boolean isDataEmpty() {
        return image == null;
    }

    @Override
    public int dataSize() {
        return dataSize;
    }

    @Override
    public Integer getData(int index) {
        return image.getRGB(index % width, index / width);
    }

    @Override
    public List<Integer> allData() {
        List<Integer> rgb = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                rgb.add(image.getRGB(i, j));
            }
        }
        return rgb;
    }

    @Override
    public boolean run() {
        if (colorMatch == null || isDataEmpty()) {
            return false;
        }
        return super.run();
    }

    @Override
    public double distance(Integer p1, Integer p2) {
        try {
            if (p1 == null || p2 == null) {
                return Double.MAX_VALUE;
            }
            return colorMatch.distance(new Color(p1), new Color(p2));
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return Double.MAX_VALUE;
        }
    }

    @Override
    public boolean equal(Integer p1, Integer p2) {
        try {
            if (p1 == null || p2 == null) {
                return false;
            }
            return colorMatch.isMatch(new Color(p1), new Color(p2));
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public Integer calculateCenters(List<Integer> cluster) {
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
                color = new Color(getData(index));
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
            }
            int size = cluster.size();
            color = new Color(r / size, g / size, b / size);
            return color.getRGB();
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
