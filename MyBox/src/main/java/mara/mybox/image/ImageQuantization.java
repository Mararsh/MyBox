package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @License Apache License Version 2.0
 */
// http://web.cs.wpi.edu/~matt/courses/cs563/talks/color_quant/CQindex.html
public class ImageQuantization extends PixelsOperation {

    public static enum QuantizationAlgorithm {
        RGBUniformQuantization, HSBUniformQuantization,
        PopularityQuantization, KMeansClustering
//        MedianCutQuantization, ANN
    }

    protected QuantizationAlgorithm algorithm;
    protected int quantizationSize, regionSize, weight1, weight2, weight3, intValue;
    protected boolean recordCount, ceil;
    protected Map<Color, Long> counts;
    protected List<ColorCount> sortedCounts;
    protected long totalCount;

    public static ImageQuantization create(Image image, ImageScope scope,
            QuantizationAlgorithm algorithm, int quantizationSize,
            int regionSize, int weight1, int weight2, int weight3,
            boolean recordCount, boolean dithering, boolean ceil) throws Exception {
        return create(SwingFXUtils.fromFXImage(image, null), scope,
                algorithm, quantizationSize, regionSize, weight1, weight2, weight3,
                recordCount, dithering, ceil);
    }

    public static ImageQuantization create(BufferedImage image, ImageScope scope,
            QuantizationAlgorithm algorithm, int quantizationSize,
            int regionSize, int weight1, int weight2, int weight3,
            boolean recordCount, boolean dithering, boolean ceil) throws Exception {
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
                .setRecordCount(recordCount).setCeil(ceil)
                .setOperationType(OperationType.Quantization)
                .setImage(image).setScope(scope).setIsDithering(dithering);
        return quantization.build();
    }

    public ImageQuantization build() throws Exception {
        return this;
    }

    public void countColor(Color mappedColor) {
        if (recordCount && counts != null) {
            if (counts.containsKey(mappedColor)) {
                counts.put(mappedColor, counts.get(mappedColor) + 1);
            } else {
                counts.put(mappedColor, Long.valueOf(1));
            }
        }
    }

    public static class ColorCount {

        public Color color;
        public long count;

        public ColorCount(Color color, long count) {
            this.color = color;
            this.count = count;
        }
    }

    public List<ColorCount> sortCounts() {
        totalCount = 0;
        if (counts == null) {
            return null;
        }
        sortedCounts = new ArrayList<>();
        for (Color color : counts.keySet()) {
            sortedCounts.add(new ColorCount(color, counts.get(color)));
            totalCount += counts.get(color);
        }
        Collections.sort(sortedCounts, new Comparator<ColorCount>() {
            @Override
            public int compare(ColorCount v1, ColorCount v2) {
                long diff = v2.count - v1.count;
                if (diff == 0) {
                    return 0;
                } else if (diff > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return sortedCounts;
    }

    public StringTable countTable(String name) {
        try {
            sortedCounts = sortCounts();
            if (sortedCounts == null || totalCount == 0) {
                return null;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("ID"), message("PixelsNumber"),
                    message("Percentage"), message("Color"),
                    message("Red"), message("Green"), message("Blue"), message("Opacity"),
                    message("Hue"), message("Brightness"), message("Saturation")
            ));
            String title = message(algorithm.name());
            if (name != null) {
                title += "_" + name;
            }
            StringTable table = new StringTable(names, title, 3);
            int id = 1;
            for (ColorCount count : sortedCounts) {
                List<String> row = new ArrayList<>();
                javafx.scene.paint.Color color = ImageColor.converColor(count.color);
                int red = (int) Math.round(color.getRed() * 255);
                int green = (int) Math.round(color.getGreen() * 255);
                int blue = (int) Math.round(color.getBlue() * 255);
                row.addAll(Arrays.asList((id++) + "", StringTools.format(count.count),
                        (int) (count.count * 100 / totalCount) + "%",
                        FxmlColor.color2rgba(color), red + " ", green + " ", blue + " ",
                        (int) Math.round(color.getOpacity() * 100) + "%",
                        Math.round(color.getHue()) + " ",
                        Math.round(color.getSaturation() * 100) + "%",
                        Math.round(color.getBrightness() * 100) + "%"
                ));
                table.add(row);
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public Color operateColor(Color color) {
        return color;
    }

    public static class RGBUniformQuantization extends ImageQuantization {

        protected int redMod, redOffset, greenMod, greenOffset, blueMod, blueOffset;

        public static RGBUniformQuantization create() throws Exception {
            return new RGBUniformQuantization();
        }

        @Override
        public RGBUniformQuantization build() throws Exception {
            double redWegiht = weight1 < 1 ? 1 : weight1;
            double greenWegiht = weight2 < 1 ? 1 : weight2;
            double blueWegiht = weight3 < 1 ? 1 : weight3;
            double sum = weight1 + weight2 + weight3;
            redWegiht = redWegiht / sum;
            greenWegiht = greenWegiht / sum;
            blueWegiht = blueWegiht / sum;
            double x = Math.pow(quantizationSize * 1d / (redWegiht * greenWegiht * blueWegiht), 1d / 3d);

            double redValue = 256d / (redWegiht * x);
            redMod = ceil ? (int) (redValue) : (int) Math.ceil(redValue);
            double greenValue = 256d / (greenWegiht * x);
            greenMod = ceil ? (int) (greenValue) : (int) Math.ceil(greenValue);
            double blueValue = 256d / (blueWegiht * x);
            blueMod = ceil ? (int) (blueValue) : (int) Math.ceil(blueValue);

//            MyBoxLog.console(redMod + " " + greenMod + " " + blueMod + " ");
            if (redMod <= 0 || greenMod <= 0 || blueMod <= 0) {
                throw new Exception(AppVariables.message("InvalidParameters"));
            }

            redOffset = redMod / 2;
            greenOffset = greenMod / 2;
            blueOffset = blueMod / 2;

            if (recordCount) {
                counts = new HashMap<>();
            }
            return this;
        }

        @Override
        public Color operateColor(Color color) {
            if (color.getRGB() == 0) {
                return color;
            }
            int red, green, blue;

            int v = color.getRed();
            v = v - (v % redMod) + redOffset;
            red = Math.min(Math.max(v, 0), 255);

            v = color.getGreen();
            v = v - (v % greenMod) + greenOffset;
            green = Math.min(Math.max(v, 0), 255);

            v = color.getBlue();
            v = v - (v % blueMod) + blueOffset;
            blue = Math.min(Math.max(v, 0), 255);

            Color mappedColor = new Color(red, green, blue);
            countColor(mappedColor);
            return mappedColor;

        }

    }

    public static class HSBUniformQuantization extends ImageQuantization {

        protected int hueMod, saturationMod, brightnessMod,
                hueOffset, saturationOffset, brightnessOffset;

        public static HSBUniformQuantization create() throws Exception {
            return new HSBUniformQuantization();
        }

        @Override
        public HSBUniformQuantization build() throws Exception {
            double hueWegiht = weight1 < 1 ? 1 : weight1;
            double saturationWegiht = weight2 < 1 ? 1 : weight2;
            double brightnessWegiht = weight3 < 1 ? 1 : weight3;
            double sum = hueWegiht + saturationWegiht + brightnessWegiht;
            hueWegiht = hueWegiht / sum;
            saturationWegiht = saturationWegiht / sum;
            brightnessWegiht = brightnessWegiht / sum;
            double x = Math.pow(quantizationSize * 1d / (hueWegiht * saturationWegiht * brightnessWegiht), 1d / 3d);

            double hueValue = 360d / (hueWegiht * x);
            hueMod = ceil ? (int) (hueValue) : (int) Math.ceil(hueValue);
            double saturationValue = 100d / (saturationWegiht * x);
            saturationMod = ceil ? (int) (saturationValue) : (int) Math.ceil(saturationValue);
            double brightnessValue = 100d / (brightnessWegiht * x);
            brightnessMod = ceil ? (int) (brightnessValue) : (int) Math.ceil(brightnessValue);

            if (hueMod <= 0 || saturationMod <= 0 || brightnessMod <= 0) {
                throw new Exception(AppVariables.message("InvalidParameters"));
            }

            hueOffset = hueMod / 2;
            saturationOffset = saturationMod / 2;
            brightnessOffset = brightnessMod / 2;

            if (recordCount) {
                counts = new HashMap<>();
            }
            return this;
        }

        @Override
        public Color operateColor(Color color) {
            if (color.getRGB() == 0) {
                return color;
            }
            if (hueMod <= 0 || saturationMod <= 0 || brightnessMod <= 0) {
                return color;
            }
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            float h, s, b;

            int v = (int) (hsb[0] * 360);
            v = v - (v % hueMod) + hueOffset;
            h = Math.min(Math.max(v / 360.0f, 0.0f), 1.0f);

            v = (int) (hsb[1] * 100);
            v = v - (v % saturationMod) + saturationOffset;
            s = Math.min(Math.max(v / 100.0f, 0.0f), 1.0f);

            v = (int) (hsb[2] * 100);
            v = v - (v % brightnessMod) + brightnessOffset;
            b = Math.min(Math.max(v / 100.0f, 0.0f), 1.0f);

            Color mappedColor = Color.getHSBColor(h, s, b);
            countColor(mappedColor);
            return mappedColor;
        }

    }

    public static class RegionQuantization extends ImageQuantization {

        protected RGBUniformQuantization rgbPalette;
        protected boolean large;

        public static RegionQuantization create(BufferedImage image,
                ImageScope scope, boolean dithering,
                int regionSize, int weight1, int weight2, int weight3) {
            RegionQuantization palette = new RegionQuantization();
            palette.setRegionSize(regionSize)
                    .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                    .setImage(image).setScope(scope).
                    setOperationType(OperationType.Quantization).
                    setIsDithering(dithering);
            palette.build();
            return palette;
        }

        @Override
        public RegionQuantization build() {
            this.recordCount = false;   // never count in this regionQuantization
            try {
                large = image.getWidth() * image.getHeight() > regionSize;
                if (large) {
                    rgbPalette = RGBUniformQuantization.create();
                    rgbPalette.setQuantizationSize(regionSize)
                            .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                            .setRecordCount(false).build();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
            }

            return this;
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

    public class PopularityRegion {

        protected long redAccum, greenAccum, blueAccum, pixelsCount;
        protected Color regionColor, averageColor;
    }

    public static class PopularityRegionQuantization extends RegionQuantization {

        protected Map<Color, PopularityRegion> regionsMap;

        public static PopularityRegionQuantization create(BufferedImage image,
                ImageScope scope, boolean dithering,
                int regionSize, int weight1, int weight2, int weight3) {
            PopularityRegionQuantization palette = new PopularityRegionQuantization();
            palette.setRegionSize(regionSize)
                    .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                    .setImage(image).setScope(scope).
                    setOperationType(OperationType.Quantization).
                    setIsDithering(dithering);
            palette.build();
            return palette;
        }

        @Override
        public RegionQuantization build() {
            super.build();
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

        public static PopularityQuantization create() throws Exception {
            return new PopularityQuantization();
        }

        @Override
        public PopularityQuantization build() {
            try {
                regionQuantization = PopularityRegionQuantization.create(image,
                        scope, isDithering, regionSize, weight1, weight2, weight3);
                regionQuantization.operate();
                regions = regionQuantization.getRegions(quantizationSize);

                if (recordCount) {
                    counts = new HashMap<>();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
            }
            return this;
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
                int minDistance = Integer.MAX_VALUE;
                PopularityRegion nearestRegion = regions.get(0);
                for (int i = 0; i < regions.size(); ++i) {
                    PopularityRegion region = regions.get(i);
                    int distance = ImageColor.calculateColorDistanceSquare(region.averageColor, color);
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

        public static KMeansRegionQuantization create(BufferedImage image,
                ImageScope scope, boolean dithering,
                int regionSize, int weight1, int weight2, int weight3) {
            KMeansRegionQuantization palette = new KMeansRegionQuantization();
            palette.setRegionSize(regionSize)
                    .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                    .setImage(image).setScope(scope).
                    setOperationType(OperationType.Quantization).
                    setIsDithering(dithering);
            palette.build();
            return palette;
        }

        @Override
        public RegionQuantization build() {
            super.build();
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
        public KMeansClusteringQuantization build() {
            kmeans = ImageRGBKMeans.create();
            kmeans.setSourceImage(image).setScope(scope).setIsDithering(isDithering)
                    .setRegionSize(regionSize)
                    .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                    .setK(quantizationSize);
            if (kmeans.init().run()) {
                kmeans.makeMap();
            }
            if (recordCount) {
                counts = new HashMap<>();
            }
            return this;
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

    /*
        get/set
     */
    public QuantizationAlgorithm getAlgorithm() {
        return algorithm;
    }

    public ImageQuantization setAlgorithm(QuantizationAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public int getQuantizationSize() {
        return quantizationSize;
    }

    public ImageQuantization setQuantizationSize(int quantizationSize) {
        this.quantizationSize = quantizationSize;
        return this;
    }

    public int getWeight1() {
        return weight1;
    }

    public ImageQuantization setWeight1(int weight1) {
        this.weight1 = weight1;
        return this;
    }

    public int getWeight2() {
        return weight2;
    }

    public ImageQuantization setWeight2(int weight2) {
        this.weight2 = weight2;
        return this;
    }

    public int getWeight3() {
        return weight3;
    }

    public ImageQuantization setWeight3(int weight3) {
        this.weight3 = weight3;
        return this;
    }

    public boolean isRecordCount() {
        return recordCount;
    }

    public ImageQuantization setRecordCount(boolean recordCount) {
        this.recordCount = recordCount;
        return this;
    }

    public Map<Color, Long> getCounts() {
        return counts;
    }

    public ImageQuantization setCounts(Map<Color, Long> counts) {
        this.counts = counts;
        return this;
    }

    public List<ColorCount> getSortedCounts() {
        return sortedCounts;
    }

    public ImageQuantization setSortedCounts(List<ColorCount> sortedCounts) {
        this.sortedCounts = sortedCounts;
        return this;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public ImageQuantization setTotalCount(long totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public int getIntValue() {
        return intValue;
    }

    public ImageQuantization setIntValue(int intValue) {
        this.intValue = intValue;
        return this;
    }

    public int getRegionSize() {
        return regionSize;
    }

    public ImageQuantization setRegionSize(int regionSize) {
        this.regionSize = regionSize;
        return this;
    }

    public boolean isCeil() {
        return ceil;
    }

    public ImageQuantization setCeil(boolean ceil) {
        this.ceil = ceil;
        return this;
    }

}
