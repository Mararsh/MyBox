package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.ListKMeans;
import mara.mybox.image.ImageQuantization.KMeansRegionQuantization;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-10-7
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageRGBKMeans extends ListKMeans<Color> {

    // Each channel is 4 bit depth and reigons size = 64 *64 * 64 = 262,144
    // When ditDepth is larger than 4, the results are worse due to
    // similiar selected colors  by too small regions.
    protected int bitDepth = 4;
    protected KMeansRegionQuantization regionQuantization;
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
                    scope, isDithering, bitDepth);
            regionQuantization.operate();
            data = regionQuantization.regionColors;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return this;
    }

    @Override
    public void initCenters() {
        try {
            centers = new ArrayList<>();
            if (data.size() < k) {
                centers.addAll(data);
                return;
            }
            int mod = data.size() / k;
            for (int i = 0; i < data.size(); i = i + mod) {
                centers.add(data.get(i));
                if (centers.size() == k) {
                    return;
                }
            }
            while (centers.size() < k) {
                int index = new Random().nextInt(data.size());
                Color d = data.get(index);
                if (!centers.contains(d)) {
                    centers.add(d);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public double distance(Color p1, Color p2) {
        try {
            if (p1 == null || p2 == null) {
                return Integer.MAX_VALUE;
            }
            return ImageColor.calculateColorDistance2(p1, p2);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean equal(Color p1, Color p2) {
        try {
            if (p1 == null || p2 == null) {
                return false;
            }
            return ImageColor.isColorMatch2(p1, p2, equalDistance);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public Color calculateCenters(List<Integer> cluster) {
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public Color map(Color color) {
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
                int distance = ImageColor.calculateColorDistance2(regionColor, centerColor);
                if (distance < minDistance) {
                    minDistance = distance;
                    mappedColor = centerColor;
                }
            }
        }
        return mappedColor;
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

    public int getBitDepth() {
        return bitDepth;
    }

    public ImageRGBKMeans setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
        return this;
    }

}
