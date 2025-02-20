package mara.mybox.image.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.color.ColorMatch;
import mara.mybox.controller.ControlImageQuantization;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.data.ImageQuantization.QuantizationAlgorithm;
import static mara.mybox.image.data.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @License Apache License Version 2.0
 */
public class ImageQuantizationFactory {

    public static ImageQuantization createFX(Image image, ImageScope scope,
            ControlImageQuantization quantizationController, boolean recordCount) {
        return create(image != null ? SwingFXUtils.fromFXImage(image, null) : null,
                scope, quantizationController, recordCount);
    }

    public static ImageQuantization create(BufferedImage image, ImageScope scope,
            ControlImageQuantization quantizationController, boolean recordCount) {
        return create(image, scope, quantizationController.getAlgorithm(),
                quantizationController.getQuanColors(),
                quantizationController.getRegionSize(),
                quantizationController.getAlgorithm() == QuantizationAlgorithm.HSBUniformQuantization
                ? quantizationController.getHsbWeight1() : quantizationController.getRgbWeight1(),
                quantizationController.getAlgorithm() == QuantizationAlgorithm.HSBUniformQuantization
                ? quantizationController.getHsbWeight2() : quantizationController.getRgbWeight2(),
                quantizationController.getAlgorithm() == QuantizationAlgorithm.HSBUniformQuantization
                ? quantizationController.getHsbWeight3() : quantizationController.getRgbWeight3(),
                recordCount,
                quantizationController.getQuanDitherCheck().isSelected(),
                quantizationController.getFirstColorCheck().isSelected(),
                quantizationController.colorMatch());
    }

    public static ImageQuantization create(BufferedImage image, ImageScope scope,
            QuantizationAlgorithm algorithm, int quantizationSize,
            int regionSize, int weight1, int weight2, int weight3,
            boolean recordCount, boolean dithering, boolean firstColor,
            ColorMatch colorMatch) {
        try {
            ImageQuantization quantization;
            switch (algorithm) {
                case RGBUniformQuantization:
                    quantization = RGBUniformQuantization.create();
                    break;
                case HSBUniformQuantization:
                    quantization = HSBUniformQuantization.create();
                    break;
                case PopularityQuantization:
                    quantization = PopularityQuantization.create();
                    break;
                case KMeansClustering:
                    quantization = KMeansClusteringQuantization.create();
                    break;
//             case ANN:
//                return new RGBUniform(image, quantizationSize);
                default:
                    quantization = RGBUniformQuantization.create();
                    break;
            }
            quantization.setAlgorithm(algorithm).
                    setQuantizationSize(quantizationSize)
                    .setRegionSize(regionSize)
                    .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                    .setRecordCount(recordCount)
                    .setFirstColor(firstColor)
                    .setColorMatch(colorMatch)
                    .setOperationType(PixelsOperation.OperationType.Quantization)
                    .setImage(image).setScope(scope).setIsDithering(dithering);
            return quantization.buildPalette();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static class RGBUniformQuantization extends ImageQuantization {

        protected int redMod, redOffset, greenMod, greenOffset, blueMod, blueOffset;
        protected int redSize, greenSize, blueSize;

        public static RGBUniformQuantization create() {
            return new RGBUniformQuantization();
        }

        @Override
        public RGBUniformQuantization buildPalette() {
            try {
                regionSize = quantizationSize;
                algorithm = QuantizationAlgorithm.RGBUniformQuantization;

                float redWegiht = weight1 < 1 ? 1 : weight1;
                float greenWegiht = weight2 < 1 ? 1 : weight2;
                float blueWegiht = weight3 < 1 ? 1 : weight3;
                float sum = weight1 + weight2 + weight3;
                redWegiht = redWegiht / sum;
                greenWegiht = greenWegiht / sum;
                blueWegiht = blueWegiht / sum;
                float x = (float) Math.cbrt(quantizationSize / (redWegiht * greenWegiht * blueWegiht));

                float redValue = redWegiht * x;
                redMod = (int) (256 / redValue);
                redSize = (int) (256 / redMod) + 1;

                float greenValue = greenWegiht * x;
                greenMod = (int) (256 / greenValue);
                greenSize = (int) (256 / greenMod) + 1;

                float blueValue = blueWegiht * x;
                blueMod = (int) (256 / blueValue);
                blueSize = (int) (256 / blueMod) + 1;

//                MyBoxLog.console("quantizationSize:" + quantizationSize + " x:" + x);
//                MyBoxLog.console("redMod:" + redMod + " greenMod:" + greenMod + " blueMod:" + blueMod);
//                MyBoxLog.console("redSize:" + redSize + " greenSize:" + greenSize + " blueSize:" + blueSize);
                if (redSize <= 0 || greenSize <= 0 || blueSize <= 0
                        || redMod <= 0 || greenMod <= 0 || blueMod <= 0) {
                    MyBoxLog.error(message("InvalidParameters"));
                    return this;
                }

                if (firstColor) {
                    palette = new Color[redSize][greenSize][blueSize];
                } else {
                    redOffset = redMod / 2;
                    greenOffset = greenMod / 2;
                    blueOffset = blueMod / 2;
                }

                if (recordCount) {
                    counts = new HashMap<>();
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return this;
        }

        @Override
        public String resultInfo() {
            return message(algorithm.name()) + "\n"
                    + message("ColorsRegionSize") + ": " + redSize * greenSize * blueSize + "\n"
                    + "redMod: " + redMod + " greenMod: " + greenMod + " blueMod: " + blueMod + "\n"
                    + "redSize: " + redSize + " greenSize: " + greenSize + " blueSize: " + blueSize;
        }

        @Override
        public Color operateColor(Color color) {
            try {
                if (color.getRGB() == 0) {
                    return color;
                }
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                Color mappedColor;
                if (firstColor) {
                    int indexRed = red / redMod;
                    int indexGreen = green / greenMod;
                    int indexBlue = blue / blueMod;
                    if (palette[indexRed][indexGreen][indexBlue] == null) {
                        palette[indexRed][indexGreen][indexBlue] = color;
                        mappedColor = color;
                    } else {
                        mappedColor = palette[indexRed][indexGreen][indexBlue];
                    }

                } else {
                    red = red - (red % redMod) + redOffset;
                    red = Math.min(Math.max(red, 0), 255);

                    green = green - (green % greenMod) + greenOffset;
                    green = Math.min(Math.max(green, 0), 255);

                    blue = blue - (blue % blueMod) + blueOffset;
                    blue = Math.min(Math.max(blue, 0), 255);

                    mappedColor = new Color(red, green, blue);
                }

                countColor(mappedColor);
                return mappedColor;
            } catch (Exception e) {
//                MyBoxLog.error(e);
                return color;
            }
        }
    }

    public static class HSBUniformQuantization extends ImageQuantization {

        protected int hueMod, saturationMod, brightnessMod,
                hueOffset, saturationOffset, brightnessOffset;
        protected int hueSize, saturationSize, brightnessSize;

        public static HSBUniformQuantization create() throws Exception {
            return new HSBUniformQuantization();
        }

        @Override
        public HSBUniformQuantization buildPalette() {
            try {
                regionSize = quantizationSize;
                algorithm = QuantizationAlgorithm.HSBUniformQuantization;

                float hueWegiht = weight1 < 1 ? 1 : weight1;
                float saturationWegiht = weight2 < 1 ? 1 : weight2;
                float brightnessWegiht = weight3 < 1 ? 1 : weight3;
                float sum = hueWegiht + saturationWegiht + brightnessWegiht;
                hueWegiht = hueWegiht / sum;
                saturationWegiht = saturationWegiht / sum;
                brightnessWegiht = brightnessWegiht / sum;
                float x = (float) Math.cbrt(quantizationSize / (hueWegiht * saturationWegiht * brightnessWegiht));

                float hueValue = hueWegiht * x;
                hueMod = (int) (360 / hueValue);
                hueSize = (int) (360 / hueMod) + 1;

                float saturationValue = saturationWegiht * x;
                saturationMod = (int) (100 / saturationValue);
                saturationSize = (int) (100 / saturationMod) + 1;

                float brightnessValue = brightnessWegiht * x;
                brightnessMod = (int) (100 / brightnessValue);
                brightnessSize = (int) (100 / brightnessMod) + 1;

//                MyBoxLog.console("regionSize:" + regionSize + " x:" + x);
//                MyBoxLog.console("hueMod:" + hueMod + " saturationMod:" + saturationMod + " brightnessMod:" + brightnessMod);
//                MyBoxLog.console("hueSize:" + hueSize + " saturationSize:" + saturationSize + " brightnessSize:" + brightnessSize);
                if (hueSize <= 0 || saturationSize <= 0 || saturationSize <= 0
                        || hueMod <= 0 || saturationMod <= 0 || brightnessMod <= 0) {
                    MyBoxLog.error(message("InvalidParameters"));
                    return this;
                }

                if (firstColor) {
                    palette = new Color[hueSize][saturationSize][brightnessSize];
                } else {
                    hueOffset = hueMod / 2;
                    saturationOffset = saturationMod / 2;
                    brightnessOffset = brightnessMod / 2;
                }

                if (recordCount) {
                    counts = new HashMap<>();
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return this;
        }

        @Override
        public String resultInfo() {
            return message(algorithm.name()) + "\n"
                    + message("ColorsRegionSize") + ": " + hueSize * saturationSize * brightnessSize + "\n"
                    + "hueMod: " + hueMod + " saturationMod: " + saturationMod + " brightnessMod: " + brightnessMod + "\n"
                    + "hueSize: " + hueSize + " saturationSize: " + saturationSize + " brightnessSize: " + brightnessSize;
        }

        @Override
        public Color operateColor(Color color) {
            try {
                if (color.getRGB() == 0) {
                    return color;
                }
                Color mappedColor;
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                float h, s, b;

                int hue = (int) (hsb[0] * 360);
                int saturation = (int) (hsb[1] * 100);
                int brightness = (int) (hsb[2] * 100);
                if (firstColor) {
                    int indexHue = hue / hueMod;
                    int indexSaturation = saturation / saturationMod;
                    int indexBrightness = brightness / brightnessMod;
                    if (palette[indexHue][indexSaturation][indexBrightness] == null) {
                        palette[indexHue][indexSaturation][indexBrightness] = color;
                        mappedColor = color;
                    } else {
                        mappedColor = palette[indexHue][indexSaturation][indexBrightness];
                    }

                } else {
                    hue = hue - (hue % hueMod) + hueOffset;
                    h = Math.min(Math.max(hue / 360.0f, 0.0f), 1.0f);

                    saturation = saturation - (saturation % saturationMod) + saturationOffset;
                    s = Math.min(Math.max(saturation / 100.0f, 0.0f), 1.0f);

                    brightness = brightness - (brightness % brightnessMod) + brightnessOffset;
                    b = Math.min(Math.max(brightness / 100.0f, 0.0f), 1.0f);
                    mappedColor = Color.getHSBColor(h, s, b);
                }

                countColor(mappedColor);
                return mappedColor;
            } catch (Exception e) {
//                MyBoxLog.error(e);
                return color;
            }
        }

    }

    public static class RegionQuantization extends ImageQuantization {

        protected RGBUniformQuantization rgbPalette;
        protected boolean large;

        @Override
        public RegionQuantization buildPalette() {
            try {
                large = image.getWidth() * image.getHeight() > regionSize;
                if (large) {
                    rgbPalette = RGBUniformQuantization.create();
                    rgbPalette.setQuantizationSize(regionSize)
                            .setFirstColor(firstColor)
                            .setRecordCount(recordCount)
                            .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                            .setIsDithering(isDithering)
                            .setTask(task);
                    rgbPalette.buildPalette();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            return this;
        }

        @Override
        public String resultInfo() {
            return rgbPalette.resultInfo();
        }

        @Override
        public Color operateColor(Color color) {
            if (color.getRGB() == 0) {
                return color;
            }
            Color regionColor = map(color);
            handleRegionColor(color, regionColor);
            return regionColor;
        }

        public Color map(Color color) {
            if (color.getRGB() == 0) {
                return color;
            }
            Color regionColor;
            if (rgbPalette != null) {
                regionColor = rgbPalette.operateColor(color);
            } else {
                regionColor = new Color(color.getRGB(), false);
            }
            return regionColor;
        }

        public void handleRegionColor(Color color, Color regionColor) {

        }

    }

    public static class PopularityRegionQuantization extends RegionQuantization {

        protected Map<Color, PopularityRegion> regionsMap;

        public static PopularityRegionQuantization create() {
            return new PopularityRegionQuantization();
        }

        @Override
        public RegionQuantization buildPalette() {
            super.buildPalette();
            regionsMap = new HashMap<>();
            return this;
        }

        @Override
        public void handleRegionColor(Color color, Color regionColor) {
            PopularityRegion region = regionsMap.get(regionColor);
            if (region == null) {
                region = new PopularityRegion();
                region.regionColor = regionColor;
                region.redAccum = color.getRed();
                region.greenAccum = color.getGreen();
                region.blueAccum = color.getBlue();
                region.pixelsCount = 1;
                regionsMap.put(regionColor, region);
            } else {
                region.redAccum += color.getRed();
                region.greenAccum += color.getGreen();
                region.blueAccum += color.getBlue();
                region.pixelsCount += 1;
            }
        }

        public List<PopularityRegion> getRegions(int quantizationSize) {
            List<PopularityRegion> regions = new ArrayList<>();
            regions.addAll(regionsMap.values());
            for (PopularityRegion region : regions) {
                region.averageColor = new Color(
                        Math.min(255, (int) (region.redAccum / region.pixelsCount)),
                        Math.min(255, (int) (region.greenAccum / region.pixelsCount)),
                        Math.min(255, (int) (region.blueAccum / region.pixelsCount)));
            }
            Collections.sort(regions, new Comparator<PopularityRegion>() {
                @Override
                public int compare(PopularityRegion r1, PopularityRegion r2) {
                    long diff = r2.pixelsCount - r1.pixelsCount;
                    if (diff == 0) {
                        return 0;
                    } else if (diff > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            if (quantizationSize < regions.size()) {
                regions = regions.subList(0, quantizationSize);
            }
            regionsMap.clear();
            return regions;
        }

    }

    public static class PopularityQuantization extends ImageQuantization {

        protected PopularityRegionQuantization regionQuantization;
        protected List<PopularityRegion> regions;

        public static PopularityQuantization create() {
            return new PopularityQuantization();
        }

        @Override
        public PopularityQuantization buildPalette() {
            try {
                algorithm = QuantizationAlgorithm.PopularityQuantization;
                colorMatch = new ColorMatch();

                regionQuantization = PopularityRegionQuantization.create();
                regionQuantization.setQuantizationSize(regionSize)
                        .setRegionSize(regionSize)
                        .setFirstColor(firstColor)
                        .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                        .setRecordCount(false)
                        .setImage(image).setScope(scope)
                        .setOperationType(PixelsOperation.OperationType.Quantization)
                        .setIsDithering(isDithering)
                        .setTask(task);
                regionQuantization.buildPalette().start();
                regions = regionQuantization.getRegions(quantizationSize);

                if (recordCount) {
                    counts = new HashMap<>();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            return this;
        }

        @Override
        public String resultInfo() {
            if (regions == null) {
                return null;
            }
            return message(algorithm.name()) + "\n"
                    + message("ColorsNumber") + ": " + regions.size() + "\n-----\n"
                    + regionQuantization.resultInfo();
        }

        @Override
        public Color operateColor(Color color) {
            if (color.getRGB() == 0) {
                return color;
            }
            Color mappedColor = null;
            Color regionColor = regionQuantization.map(color);
            for (int i = 0; i < regions.size(); ++i) {
                PopularityRegion region = regions.get(i);
                if (region.regionColor.equals(regionColor)) {
                    mappedColor = region.averageColor;
                    break;
                }
            }
            if (mappedColor == null) {
                double minDistance = Integer.MAX_VALUE;
                PopularityRegion nearestRegion = regions.get(0);
                for (int i = 0; i < regions.size(); ++i) {
                    PopularityRegion region = regions.get(i);
                    double distance = colorMatch.distance(region.averageColor, color);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestRegion = region;
                    }
                }
                mappedColor = nearestRegion.averageColor;
            }
            countColor(mappedColor);
            return mappedColor;
        }

    }

    public static class KMeansRegionQuantization extends RegionQuantization {

        protected List<Color> regionColors;

        public static KMeansRegionQuantization create() {
            return new KMeansRegionQuantization();
        }

        @Override
        public RegionQuantization buildPalette() {
            super.buildPalette();
            regionColors = new ArrayList<>();
            return this;
        }

        @Override
        public void handleRegionColor(Color color, Color regionColor) {
            if (!regionColors.contains(regionColor)) {
                regionColors.add(regionColor);
            }
        }

    }

    public static class KMeansClusteringQuantization extends ImageQuantization {

        protected ImageRGBKMeans kmeans;

        public static KMeansClusteringQuantization create() throws Exception {
            return new KMeansClusteringQuantization();
        }

        @Override
        public KMeansClusteringQuantization buildPalette() {
            algorithm = QuantizationAlgorithm.KMeansClustering;

            kmeans = imageKMeans();
            if (recordCount) {
                counts = new HashMap<>();
            }
            return this;
        }

        @Override
        public String resultInfo() {
            if (kmeans == null) {
                return null;
            }
            return message(algorithm.name()) + "\n"
                    + message("ColorsNumber") + ": " + kmeans.centerSize() + "\n"
                    + message("ActualLoop") + ": " + kmeans.getLoopCount() + "\n-----\n"
                    + kmeans.regionQuantization.resultInfo();
        }

        @Override
        public Color operateColor(Color color) {
            if (color.getRGB() == 0) {
                return color;
            }
            Color mappedColor = kmeans.map(color);
            countColor(mappedColor);
            return mappedColor;
        }

        /*
            get/set
         */
        public ImageRGBKMeans getKmeans() {
            return kmeans;
        }

        public void setKmeans(ImageRGBKMeans kmeans) {
            this.kmeans = kmeans;
        }

    }

    // https://www.researchgate.net/publication/220502178_On_spatial_quantization_of_color_images
    public static class Spatialquantization {

    }

    // https://www.codeproject.com/Tips/1046574/OctTree-Based-Nearest-Color-Search
    public class ColorFinder {
        // Declare a root node to contain all of the source colors.

        private Node rootNode;

        public ColorFinder(Color[] colors) {
            // Create the root node.
            rootNode = new Node(0, colors);
            // Add all source colors to it.
            for (int i = 0; i < colors.length; ++i) {
                rootNode.AddColor(i);
            }
        }

        public int GetNearestColorIndex(Color color) {
            return rootNode.GetNearestColorIndex(color)[0];
        }

        private class Node {

            private int level;
            private Color[] colors;
            private final Node[] children = new Node[8];
            private int colorIndex = -1;
            // Cached distance calculations.
            private final int[][] Distance = new int[256][256];
            // Cached bit masks.
            private final int[] Mask = {128, 64, 32, 16, 8, 4, 2, 1};

            public Node() {
                // precalculate every possible distance
                for (int i = 0; i < 256; ++i) {
                    for (int j = 0; j < 256; ++j) {
                        Distance[i][j] = ((i - j) * (i - j));
                    }
                }
            }

            public Node(int level, Color[] colors) {
                this.level = level;
                this.colors = colors;
            }

            public void AddColor(int colorIndex) {
                if (level == 7) {
                    // LEAF MODE.
                    // The deepest level contains the averageColor mappedValue and no children.
                    this.colorIndex = colorIndex;
                } else {
                    // BRANCH MODE.
                    // Get the oct mappedValue for the specified source averageColor at this level.
                    int index = colorIndex(colors[colorIndex], level);
                    // If the necessary child node doesn't exist, root it.
                    if (children[index] == null) {
                        children[index] = new Node((level + 1), colors);
                    }
                    // Pass the averageColor along to the proper child node.
                    children[index].AddColor(colorIndex);
                }
            }

            public int colorIndex(Color color, int level) {
                // Take 1 bit from each averageColor channel
                // to return a 3-bit value ranging from 0 to 7.
                // Level 0 uses the highest bit, level 1 uses the second-highest bit, etc.
                int shift = (7 - level);
                return (((color.getRed() & Mask[level]) >> shift)
                        | ((color.getGreen() & Mask[level]) << 1 >> shift)
                        | ((color.getBlue() & Mask[level]) << 2 >> shift));
            }

            public int distance(int b1, int b2) {
                return Distance[b1][b2];
            }

            public int distance(Color c1, Color c2) {
                return distance(c1.getRed(), c2.getRed())
                        + distance(c1.getGreen(), c2.getGreen())
                        + distance(c1.getBlue(), c2.getBlue());
            }

            public int[] GetNearestColorIndex(Color color) {
                int[] ret = new int[2];
                ret[0] = -1;
                ret[1] = Integer.MAX_VALUE;
                if (level == 7) {                                // LEAF MODE.

                    ret[0] = colorIndex;
                    ret[1] = distance(color, colors[colorIndex]);
                } else {                                           // BRANCH MODE.
                    int index = colorIndex(color, level);
                    if (children[index] == null) {
                        int minDistance = Integer.MAX_VALUE;
                        int minIndex = -1;
                        for (Node child : children) {
                            if (child != null) {
                                int[] v = child.GetNearestColorIndex(color);
                                if (v[1] < minDistance) {
                                    minIndex = v[0];
                                    minDistance = v[1];
                                }
                            }
                        }
                        ret[0] = minIndex;
                        ret[1] = minDistance;
                    } else {                                         // DIRECT CHILD EXISTS.
                        ret = children[index].GetNearestColorIndex(color);
                    }
                }
                return ret;
            }

        }

    }

    public static class Octree {

        private OctreeNode rootNode;

        public Octree(BufferedImage image, int colorsNumber) {

            rootNode = new OctreeNode();
            rootNode.afterSetParam();
            for (int i = 0; i < image.getWidth(); ++i) {
                for (int j = 0; j < image.getHeight(); ++j) {
                    int colorValue = image.getRGB(i, j);
                    if (colorValue == 0) {    // transparency is not involved
                        continue;
                    }
                    rootNode.addColor(colorValue, colorsNumber);
                }
            }
        }

        public static class OctreeNode {

            protected int level = 0;
            protected OctreeNode parent;
            protected OctreeNode[] children = new OctreeNode[8];
            protected boolean isLeaf = false;
            protected int redAccum = 0;
            protected int greenAccum = 0;
            protected int blueAccum = 0;
            protected int piexlsCount = 0;
            protected Map<Integer, List<OctreeNode>> levelMapping;

            public void addColor(int colorValue, int number) {
                if (level != 0 || this.parent != null) {
                    throw new UnsupportedOperationException();
                }

                int speed = 7 + 1 - number;

                int r = colorValue >> 16 & 0xFF;
                int g = colorValue >> 8 & 0xFF;
                int b = colorValue & 0xFF;
                OctreeNode proNode = this;
                for (int i = 7; i >= speed; --i) {
                    int item = ((r >> i & 1) << 2) + ((g >> i & 1) << 1) + (b >> i & 1);
                    OctreeNode child = proNode.getChild(item);
                    if (child == null) {
                        child = new OctreeNode();
                        child.setLevel(8 - i);
                        child.setParent(proNode);
                        child.afterSetParam();
                        this.levelMapping.get(child.getLevel()).add(child);
                        proNode.setChild(item, child);
                    }

                    if (i == speed) {
                        child.setIsLeaf(true);
                    }
                    if (child.isIsLeaf()) {
                        child.setPixel(r, g, b);
                        break;
                    }
                    proNode = child;
                }

            }

            public OctreeNode getChild(int index) {
                return children[index];
            }

            public void setChild(int index, OctreeNode node) {
                children[index] = node;
            }

            public OctreeNode getDepestNode() {
                for (int i = 7; i > 0; --i) {
                    List<OctreeNode> levelList = this.levelMapping.get(i);
                    if (!levelList.isEmpty()) {
                        return levelList.remove(levelList.size() - 1);
                    }
                }
                return null;
            }

            public int getLeafNum() {
                if (isLeaf) {
                    return 1;
                }
                int i = 0;
                for (OctreeNode child : this.children) {
                    if (child != null) {
                        i += child.getLeafNum();
                    }
                }
                return i;
            }

            public void afterSetParam() {
                if (this.getParent() == null && this.level == 0) {
                    levelMapping = new HashMap<>();
                    for (int i = 1; i <= 8; ++i) {
                        levelMapping.put(i, new ArrayList<>());
                    }
                }
            }

            public void setPixel(int r, int g, int b) {
                this.redAccum += r;
                this.greenAccum += g;
                this.blueAccum += b;
                this.piexlsCount += 1;
            }

            /*
                get/set
             */
            public int getLevel() {
                return level;
            }

            public void setLevel(int level) {
                this.level = level;
            }

            public OctreeNode getParent() {
                return parent;
            }

            public void setParent(OctreeNode parent) {
                this.parent = parent;
            }

            public OctreeNode[] getChildren() {
                return children;
            }

            public void setChildren(OctreeNode[] children) {
                this.children = children;
            }

            public boolean isIsLeaf() {
                return isLeaf;
            }

            public void setIsLeaf(boolean isLeaf) {
                this.isLeaf = isLeaf;
            }

            public int getRedAccum() {
                return redAccum;
            }

            public void setRedAccum(int redAccum) {
                this.redAccum = redAccum;
            }

            public int getGreenAccum() {
                return greenAccum;
            }

            public void setGreenAccum(int greenAccum) {
                this.greenAccum = greenAccum;
            }

            public int getBlueAccum() {
                return blueAccum;
            }

            public void setBlueAccum(int blueAccum) {
                this.blueAccum = blueAccum;
            }

            public int getPiexlsCount() {
                return piexlsCount;
            }

            public void setPiexlsCount(int piexlsCount) {
                if (!isLeaf) {
                    throw new UnsupportedOperationException();
                }
                this.piexlsCount = piexlsCount;
            }

            public Map<Integer, List<OctreeNode>> getLevelMapping() {
                return levelMapping;
            }

            public void setLevelMapping(Map<Integer, List<OctreeNode>> levelMapping) {
                this.levelMapping = levelMapping;
            }

        }
    }

}
