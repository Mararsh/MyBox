package mara.mybox.image.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.color.ColorMatch;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @License Apache License Version 2.0
 */
// http://web.cs.wpi.edu/~matt/courses/cs563/talks/color_quant/CQindex.html
public class ImageQuantization extends PixelsOperation {

    protected ColorMatch colorMatch;
    protected int maxLoop;

    public static enum QuantizationAlgorithm {
        RGBUniformQuantization, HSBUniformQuantization,
        RegionPopularityQuantization,
        RegionKMeansClustering,
        KMeansClustering
//        MedianCutQuantization, ANN
    }

    protected QuantizationAlgorithm algorithm;
    protected int quantizationSize, regionSize, weight1, weight2, weight3, intValue;
    protected boolean recordCount, firstColor;
    protected Map<Color, Long> counts;
    protected List<ColorCount> sortedCounts;
    protected long totalCount;
    protected Color[][][] palette;

    public ImageQuantization() {
        operationType = PixelsOperation.OperationType.Quantization;
    }

    public ImageQuantization buildPalette() {
        return this;
    }

    public void countColor(Color mappedColor) {
        if (recordCount) {
            if (counts == null) {
                counts = new HashMap<>();
            }
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

    public List<ColorCount> sortCounts(FxTask currentTask) {
        totalCount = 0;
        if (counts == null) {
            return null;
        }
        sortedCounts = new ArrayList<>();
        for (Color color : counts.keySet()) {
            if (currentTask != null && !currentTask.isWorking()) {
                return null;
            }
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

    public StringTable countTable(FxTask currentTask, String name) {
        try {
            sortedCounts = sortCounts(currentTask);
            if (currentTask != null && !currentTask.isWorking()) {
                return null;
            }
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
            StringTable table = new StringTable(names, title);
            int id = 1;
            for (ColorCount count : sortedCounts) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return null;
                }
                List<String> row = new ArrayList<>();
                javafx.scene.paint.Color color = ColorConvertTools.converColor(count.color);
                int red = (int) Math.round(color.getRed() * 255);
                int green = (int) Math.round(color.getGreen() * 255);
                int blue = (int) Math.round(color.getBlue() * 255);
                row.addAll(Arrays.asList((id++) + "", StringTools.format(count.count),
                        FloatTools.percentage(count.count, totalCount) + "%",
                        "<DIV style=\"width: 50px;  background-color:"
                        + FxColorTools.color2rgb(color) + "; \">&nbsp;&nbsp;&nbsp;</DIV>",
                        red + " ", green + " ", blue + " ",
                        (int) Math.round(color.getOpacity() * 100) + "%",
                        Math.round(color.getHue()) + " ",
                        Math.round(color.getSaturation() * 100) + "%",
                        Math.round(color.getBrightness() * 100) + "%"
                ));
                table.add(row);
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String resultInfo() {
        return null;
    }

    @Override
    public Color operateColor(Color color) {
        return color;
    }

    public ImageRegionKMeans imageRegionKMeans() {
        try {
            ImageQuantizationFactory.KMeansRegion regionQuantization
                    = ImageQuantizationFactory.KMeansRegion.create();
            regionQuantization.setQuantizationSize(regionSize)
                    .setRegionSize(regionSize)
                    .setFirstColor(firstColor)
                    .setWeight1(weight1).setWeight2(weight2).setWeight3(weight3)
                    .setRecordCount(true)
                    .setColorMatch(colorMatch)
                    .setImage(image).setScope(scope)
                    .setOperationType(PixelsOperation.OperationType.Quantization)
                    .setIsDithering(isDithering)
                    .setTask(task);
            regionQuantization.buildPalette().start();
            ImageRegionKMeans kmeans = ImageRegionKMeans.create();
            kmeans.setK(quantizationSize)
                    .setMaxIteration(maxLoop)
                    .setTask(task);
            if (kmeans.init(regionQuantization).run()) {
                kmeans.makeMap();
                return kmeans;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public ImageKMeans imageKMeans() {
        try {
            ImageKMeans kmeans = ImageKMeans.create();
            kmeans.setK(quantizationSize)
                    .setMaxIteration(maxLoop)
                    .setTask(task);
            if (kmeans.init(image, colorMatch).run()) {
                kmeans.makeMap();
                return kmeans;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public class PopularityRegionValue {

        protected long redAccum, greenAccum, blueAccum, pixelsCount;
        protected Color regionColor, averageColor;
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

    public boolean isFirstColor() {
        return firstColor;
    }

    public ImageQuantization setFirstColor(boolean firstColor) {
        this.firstColor = firstColor;
        return this;
    }

    public Color[][][] getPalette() {
        return palette;
    }

    public ImageQuantization setPalette(Color[][][] palette) {
        this.palette = palette;
        return this;
    }

    public ColorMatch getColorMatch() {
        return colorMatch;
    }

    public ImageQuantization setColorMatch(ColorMatch colorMatch) {
        this.colorMatch = colorMatch;
        return this;
    }

    public int getMaxLoop() {
        return maxLoop;
    }

    public ImageQuantization setMaxLoop(int maxLoop) {
        this.maxLoop = maxLoop;
        return this;
    }

}
