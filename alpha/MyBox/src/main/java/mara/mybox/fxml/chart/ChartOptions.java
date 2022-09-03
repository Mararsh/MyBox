package mara.mybox.fxml.chart;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-11
 * @License Apache License Version 2.0
 */
public class ChartOptions<X, Y> {

    protected String chartName;
    protected ChartType chartType;
    protected Chart chart;

    protected LabelType labelType;
    protected String defaultChartTitle, defaultCategoryLabel, defaultValueLabel,
            chartTitle, categoryLabel, valueLabel;
    protected int scale, labelFontSize, titleFontSize, tickFontSize;
    protected boolean popLabel, displayLabelName, plotAnimated;
    protected Side titleSide, legendSide;

    protected InvalidAs invalidAs;

    protected Map<Node, Node> nodeLabels = new HashMap<>();
    protected Map<String, String> palette;

    public static enum ChartType {
        Bar, StackedBar, Line, Bubble, Scatter, Area, StackedArea, Pie,
        BoxWhiskerChart, SimpleRegressionChart, ResidualChart
    }

    public static enum LabelType {
        NotDisplay, CategoryAndValue, Value, Category, Pop, Point
    }

    public ChartOptions() {
    }

    public ChartOptions(String chartName) {
        this.chartName = chartName;
    }

    public void clearChart() {
        chart = null;
        invalidAs = null;
    }

    public final void initChartOptions() {
        try {
            clearChart();
            if (chartName == null) {
                return;
            }

            popLabel = UserConfig.getBoolean(chartName + "PopLabel", true);
            displayLabelName = UserConfig.getBoolean(chartName + "DisplayLabelName", false);

            scale = UserConfig.getInt(chartName + "Scale", 2);
            labelFontSize = UserConfig.getInt(chartName + "LabelFontSize", 10);
            titleFontSize = UserConfig.getInt(chartName + "TitleFontSize", 12);
            tickFontSize = UserConfig.getInt(chartName + "TickFontSize", 10);

            if (chartType == ChartType.BoxWhiskerChart
                    || chartType == ChartType.Scatter
                    || chartType == ChartType.SimpleRegressionChart
                    || chartType == ChartType.ResidualChart) {
                labelType = LabelType.Point;
            } else {
                labelType = LabelType.NotDisplay;
                String saved = UserConfig.getString(chartName + "LabelType", "NotDisplay");
                if (saved != null) {
                    for (LabelType type : LabelType.values()) {
                        if (type.name().equals(saved)) {
                            labelType = type;
                            break;
                        }
                    }
                }
            }

            titleSide = Side.TOP;
            String saved = UserConfig.getString(chartName + "TitleSide", "TOP");
            if (saved != null) {
                for (Side value : Side.values()) {
                    if (value.name().equals(saved)) {
                        titleSide = value;
                        break;
                    }
                }
            }

            legendSide = Side.TOP;
            saved = UserConfig.getString(chartName + "LegendSide", "TOP");
            if (saved != null) {
                if ("NotDisplay".equals(saved)) {
                    legendSide = null;
                } else {
                    for (Side value : Side.values()) {
                        if (value.name().equals(saved)) {
                            legendSide = value;
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public Chart drawChart() {
        return chart;
    }

    public void styleChart() {
        try {
            if (chart == null) {
                return;
            }
            chart.setStyle("-fx-font-size: " + titleFontSize
                    + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");
            chart.setTitle(getChartTitle());
            chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(chart, Priority.ALWAYS);
            HBox.setHgrow(chart, Priority.ALWAYS);
            chart.setAnimated(plotAnimated);
            chart.setTitleSide(titleSide);
            chart.setLegendSide(legendSide);
            AnchorPane.setTopAnchor(chart, 2d);
            AnchorPane.setBottomAnchor​(chart, 2d);
            AnchorPane.setLeftAnchor(chart, 2d);
            AnchorPane.setRightAnchor​(chart, 2d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean displayLabel() {
        return labelType != null && labelType != LabelType.NotDisplay;
    }

    public boolean popLabel() {
        return popLabel || labelType == LabelType.Pop;
    }

    public boolean labelVisible() {
        return labelType == LabelType.Category
                || labelType == LabelType.Value
                || labelType == LabelType.CategoryAndValue;
    }

    public boolean showLegend() {
        return legendSide != null;
    }

    public double doubleValue(String v) {
        return DoubleTools.toDouble(v, invalidAs);
    }

    public double scaleValue(String v) {
        return DoubleTools.scale(v, invalidAs, scale);
    }

    protected void setLabelsStyle() {
        if (nodeLabels == null) {
            return;
        }
        for (Node node : nodeLabels.values()) {
            node.setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");;
        }
    }

    /*
        get/set
     */
    public String getChartName() {
        return chartName;
    }

    public ChartOptions setChartName(String chartName) {
        this.chartName = chartName;
        return this;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public ChartOptions setChartType(ChartType chartType) {
        this.chartType = chartType;
        return this;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public LabelType getLabelType() {
        labelType = labelType == null ? LabelType.Point : labelType;
        return labelType;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
        UserConfig.setString(chartName + "LabelType", getLabelType().name());
    }

    public String getChartTitle() {
        chartTitle = chartTitle == null ? defaultChartTitle : chartTitle;
        return chartTitle;
    }

    public ChartOptions setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
        if (chart != null) {
            chart.setTitle(this.chartTitle);
        }
        return this;
    }

    public boolean isPlotAnimated() {
        return plotAnimated;
    }

    public void setPlotAnimated(boolean plotAnimated) {
        this.plotAnimated = plotAnimated;
        UserConfig.setBoolean(chartName + "PlotAnimated", plotAnimated);
        if (chart != null) {
            chart.setAnimated(plotAnimated);
        }
    }

    public int getTitleFontSize() {
        titleFontSize = titleFontSize <= 0 ? 12 : titleFontSize;
        return titleFontSize;
    }

    public void setTitleFontSize(int titleFontSize) {
        this.titleFontSize = titleFontSize;
        UserConfig.setInt(chartName + "TitleFontSize", getTitleFontSize());
        if (chart != null) {
            chart.setStyle("-fx-font-size: " + this.titleFontSize
                    + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");
        }
    }

    public int getTickFontSize() {
        tickFontSize = tickFontSize <= 0 ? 10 : tickFontSize;
        return tickFontSize;
    }

    public void setTickFontSize(int tickFontSize) {
        this.tickFontSize = tickFontSize;
        UserConfig.setInt(chartName + "TickFontSize", getTickFontSize());
        if (chart != null) {
            chart.setStyle("-fx-font-size: " + titleFontSize
                    + "px; -fx-tick-label-font-size: " + this.tickFontSize + "px; ");
        }
    }

    public Side getTitleSide() {
        titleSide = titleSide == null ? Side.TOP : titleSide;
        return titleSide;
    }

    public void setTitleSide(Side titleSide) {
        this.titleSide = titleSide;
        UserConfig.setString(chartName + "TitleSide", getTitleSide().name());
        if (chart != null) {
            chart.setTitleSide(this.titleSide);
        }
    }

    public Side getLegendSide() {
        return legendSide;
    }

    public void setLegendSide(Side legendSide) {
        this.legendSide = legendSide;
        UserConfig.setString(chartName + "LegendSide", legendSide == null ? "NotDisplay" : getLegendSide().name());
        if (chart != null) {
            if (legendSide == null) {
                chart.setLegendVisible(false);
            } else {
                chart.setLegendVisible(true);
                chart.setLegendSide(legendSide);
            }
        }
    }

    public boolean isPopLabel() {
        return popLabel;
    }

    public void setPopLabel(boolean popLabel) {
        this.popLabel = popLabel;
    }

    public boolean isDisplayLabelName() {
        return displayLabelName;
    }

    public void setDisplayLabelName(boolean displayLabelName) {
        this.displayLabelName = displayLabelName;
        UserConfig.setBoolean(chartName + "DisplayLabelName", displayLabelName);
    }

    public int getScale() {
        scale = scale < 0 ? 2 : scale;
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
        UserConfig.setInt(chartName + "Scale", getScale());
    }

    public Map<String, String> getPalette() {
        return palette;
    }

    public ChartOptions setPalette(Map<String, String> palette) {
        this.palette = palette;
        return this;
    }

    public int getLabelFontSize() {
        labelFontSize = labelFontSize <= 0 ? 10 : labelFontSize;
        return labelFontSize;
    }

    public void setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
        UserConfig.setInt(chartName + "LabelFontSize", getLabelFontSize());
        setLabelsStyle();
    }

    public String getCategoryLabel() {
        categoryLabel = categoryLabel == null ? defaultCategoryLabel : categoryLabel;
        return categoryLabel;
    }

    public ChartOptions setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
        return this;
    }

    public String getValueLabel() {
        valueLabel = valueLabel == null ? defaultValueLabel : valueLabel;
        return valueLabel;
    }

    public ChartOptions setValueLabel(String valueLabel) {
        this.valueLabel = valueLabel;
        return this;
    }

    public String getDefaultChartTitle() {
        return defaultChartTitle;
    }

    public ChartOptions setDefaultChartTitle(String defaultChartTitle) {
        this.defaultChartTitle = defaultChartTitle;
        this.chartTitle = defaultChartTitle;
        return this;
    }

    public String getDefaultCategoryLabel() {
        return defaultCategoryLabel;
    }

    public ChartOptions setDefaultCategoryLabel(String defaultCategoryLabel) {
        this.defaultCategoryLabel = defaultCategoryLabel;
        this.categoryLabel = defaultCategoryLabel;
        return this;
    }

    public String getDefaultValueLabel() {
        return defaultValueLabel;
    }

    public ChartOptions setDefaultValueLabel(String defaultValueLabel) {
        this.defaultValueLabel = defaultValueLabel;
        this.valueLabel = defaultValueLabel;
        return this;
    }

    public ChartOptions setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

}
