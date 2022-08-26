package mara.mybox.fxml.chart;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-13
 * @License Apache License Version 2.0
 */
public class XYChartOptions<X, Y> extends ChartOptions<X, Y> {

    protected XYChart xyChart;
    protected LabeledLineChart lineChart;
    protected LabeledBarChart barChart;
    protected LabeledStackedBarChart stackedBarChart;
    protected LabeledScatterChart​ scatterChart​;
    protected LabeledAreaChart areaChart;
    protected LabeledStackedAreaChart stackedAreaChart;
    protected LabeledBubbleChart bubbleChart;
    protected BoxWhiskerChart boxWhiskerChart;
    protected SimpleRegressionChart simpleRegressionChart;
    protected ResidualChart residualChart;

    protected CategoryAxis categoryStringAxis;
    protected NumberAxis categoryNumberAxis, valueAxis;
    protected Axis xAxis, yAxis, categoryAxis;

    protected ChartCoordinate categoryCoordinate, numberCoordinate, sizeCoordinate,
            xCoordinate, yCoordinate;
    protected int lineWidth, categoryFontSize, categoryMargin, categoryTickRotation,
            numberFontSize, numberTickRotation;
    protected boolean isXY, categoryIsNumbers,
            displayCategoryMark, displayCategoryTick, displayNumberMark, displayNumberTick,
            altRowsFill, altColumnsFill, displayVlines, displayHlines, displayVZero, displayHZero,
            dotted;
    protected LabelLocation labelLocation;
    protected Side categorySide, numberSide;
    protected Sort sort;
    protected double barGap, categoryGap;
    protected String bubbleStyle;

    public static enum LabelLocation {
        Above, Below, Center
    }

    public static enum ChartCoordinate {
        Cartesian, LogarithmicE, Logarithmic10, SquareRoot
    }

    public static enum Sort {
        None, X, Y
    }

    public static final String DefaultBubbleStyle
            = "radial-gradient(center 50% 50%, radius 50%, transparent 0%, transparent 90%, derive(-fx-bubble-fill,0%) 100%)";

    public XYChartOptions() {
    }

    public XYChartOptions initXYChartOptions() {
        try {
            if (chartName == null) {
                return this;
            }
            initChartOptions();

            isXY = UserConfig.getBoolean(chartName + "XY", true);
            switch (chartType) {
                case Bubble:
                    categoryIsNumbers = true;
                    labelLocation = LabelLocation.Center;
                    break;
                case SimpleRegressionChart:
                case ResidualChart:
                    categoryIsNumbers = true;
                    labelLocation = LabelLocation.Above;
                    break;
                case Bar:
                case StackedBar:
                case Area:
                case StackedArea:
                    categoryIsNumbers = false;
                    labelLocation = LabelLocation.Above;
                    break;
                default:
                    categoryIsNumbers = UserConfig.getBoolean(chartName + "CategoryIsNumbers", false);
                    labelLocation = LabelLocation.Below;
                    break;
            }
            displayCategoryMark = UserConfig.getBoolean(chartName + "DisplayCategoryMark", true);
            displayCategoryTick = UserConfig.getBoolean(chartName + "DisplayCategoryTick", true);
            displayNumberMark = UserConfig.getBoolean(chartName + "DisplayNumberMark", true);
            displayNumberTick = UserConfig.getBoolean(chartName + "DisplayNumberTick", true);
            plotAnimated = UserConfig.getBoolean(chartName + "PlotAnimated", false);

            altRowsFill = UserConfig.getBoolean(chartName + "AltRowsFill", false);
            altColumnsFill = UserConfig.getBoolean(chartName + "AltColumnsFill", false);
            displayVlines = UserConfig.getBoolean(chartName + "DisplayVlines", true);
            displayHlines = UserConfig.getBoolean(chartName + "DisplayHlines", true);
            displayVZero = UserConfig.getBoolean(chartName + "DisplayVZero", true);
            displayHZero = UserConfig.getBoolean(chartName + "DisplayHZero", true);
            dotted = UserConfig.getBoolean(chartName + "Dotted", false);

            categoryFontSize = UserConfig.getInt(chartName + "CategoryFontSize", 10);
            categoryMargin = UserConfig.getInt(chartName + "CategoryMargin", 2);
            categoryTickRotation = UserConfig.getInt(chartName + "CategoryTickRotation", 90);
            numberFontSize = UserConfig.getInt(chartName + "NumberFontSize", 10);
            numberTickRotation = UserConfig.getInt(chartName + "NumberTickRotation", 0);
            lineWidth = UserConfig.getInt(chartName + "LineWidth", 2);
            barGap = UserConfig.getDouble(chartName + "BarGap", 2d);
            categoryGap = UserConfig.getDouble(chartName + "CategoryGap", 20d);

            bubbleStyle = UserConfig.getString(chartName + "BubbleStyle", DefaultBubbleStyle);

            String saved = UserConfig.getString(chartName + "LabelLocation", labelLocation.name());
            if (saved != null) {
                for (LabelLocation type : LabelLocation.values()) {
                    if (type.name().equals(saved)) {
                        labelLocation = type;
                        break;
                    }
                }
            }

            categorySide = Side.BOTTOM;
            saved = UserConfig.getString(chartName + "CategorySide", "BOTTOM");
            if (saved != null) {
                for (Side value : Side.values()) {
                    if (value.name().equals(saved)) {
                        categorySide = value;
                        break;
                    }
                }
            }

            numberSide = Side.LEFT;
            saved = UserConfig.getString(chartName + "NumberSide", "LEFT");
            if (saved != null) {
                for (Side value : Side.values()) {
                    if (value.name().equals(saved)) {
                        numberSide = value;
                        break;
                    }
                }
            }

            categoryCoordinate = ChartCoordinate.Cartesian;
            saved = UserConfig.getString(chartName + "CategoryCoordinate", "Cartesian");
            if (saved != null) {
                for (ChartCoordinate value : ChartCoordinate.values()) {
                    if (value.name().equals(saved)) {
                        categoryCoordinate = value;
                        break;
                    }
                }
            }

            numberCoordinate = ChartCoordinate.Cartesian;
            saved = UserConfig.getString(chartName + "NumberCoordinate", "Cartesian");
            if (saved != null) {
                for (ChartCoordinate value : ChartCoordinate.values()) {
                    if (value.name().equals(saved)) {
                        numberCoordinate = value;
                        break;
                    }
                }
            }

            sizeCoordinate = ChartCoordinate.Cartesian;
            saved = UserConfig.getString(chartName + "SizeCoordinate", "Cartesian");
            if (saved != null) {
                for (ChartCoordinate value : ChartCoordinate.values()) {
                    if (value.name().equals(saved)) {
                        sizeCoordinate = value;
                        break;
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return this;
    }

    /*
        get/set
     */
    public ChartCoordinate getCategoryCoordinate() {
        categoryCoordinate = categoryCoordinate == null ? ChartCoordinate.Cartesian : categoryCoordinate;
        return categoryCoordinate;
    }

    public void setCategoryCoordinate(ChartCoordinate categoryCoordinate) {
        this.categoryCoordinate = categoryCoordinate;
        UserConfig.setString(chartName + "CategoryCoordinate", getCategoryCoordinate().name());
    }

    public ChartCoordinate getNumberCoordinate() {
        numberCoordinate = numberCoordinate == null ? ChartCoordinate.Cartesian : numberCoordinate;
        return numberCoordinate;
    }

    public void setNumberCoordinate(ChartCoordinate numberCoordinate) {
        this.numberCoordinate = numberCoordinate;
        UserConfig.setString(chartName + "NumberCoordinate", getNumberCoordinate().name());
    }

    public ChartCoordinate getSizeCoordinate() {
        sizeCoordinate = sizeCoordinate == null ? ChartCoordinate.Cartesian : sizeCoordinate;
        return sizeCoordinate;
    }

    public void setSizeCoordinate(ChartCoordinate sizeCoordinate) {
        this.sizeCoordinate = sizeCoordinate;
        UserConfig.setString(chartName + "SizeCoordinate", getSizeCoordinate().name());
    }

    public ChartCoordinate getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(ChartCoordinate xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public ChartCoordinate getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(ChartCoordinate yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public int getLineWidth() {
        lineWidth = lineWidth <= 0 ? 2 : lineWidth;
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        UserConfig.setInt(chartName + "LineWidth", getLineWidth());
    }

    @Override
    public XYChartOptions setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
        if (categoryAxis != null) {
            categoryAxis.setLabel((displayLabelName ? message("Category") + ": " : "") + categoryLabel);
        }
        return this;
    }

    public int getCategoryFontSize() {
        categoryFontSize = categoryFontSize < 0 ? 10 : categoryFontSize;
        return categoryFontSize;
    }

    public void setCategoryFontSize(int categoryFontSize) {
        this.categoryFontSize = categoryFontSize;
        UserConfig.setInt(chartName + "CategoryFontSize", getCategoryFontSize());
        if (categoryAxis != null) {
            categoryAxis.setStyle("-fx-font-size: " + this.categoryFontSize + "px;");
        }
    }

    public int getCategoryMargin() {
        categoryMargin = categoryMargin < 0 ? 0 : categoryMargin;
        return categoryMargin;
    }

    public void setCategoryMargin(int categoryMargin) {
        this.categoryMargin = categoryMargin;
        UserConfig.setInt(chartName + "CategoryMargin", getCategoryMargin());
        if (categoryStringAxis != null) {
            categoryStringAxis.setStartMargin(this.categoryMargin);
            categoryStringAxis.setEndMargin(this.categoryMargin);
        }
    }

    public int getCategoryTickRotation() {
        categoryTickRotation = categoryTickRotation < 0 ? 90 : categoryTickRotation;
        return categoryTickRotation;
    }

    public void setCategoryTickRotation(int categoryTickRotation) {
        this.categoryTickRotation = categoryTickRotation;
        UserConfig.setInt(chartName + "CategoryTickRotation", getCategoryTickRotation());
        if (categoryAxis != null) {
            categoryAxis.setTickLabelRotation(this.categoryTickRotation);
        }
    }

    @Override
    public XYChartOptions setValueLabel(String valueLabel) {
        this.valueLabel = valueLabel;
        if (valueAxis != null) {
            valueAxis.setLabel((displayLabelName ? message("Value") + ": " : "") + valueLabel);
        }
        return this;
    }

    public int getNumberFontSize() {
        numberFontSize = numberFontSize < 0 ? 10 : numberFontSize;
        return numberFontSize;
    }

    public void setNumberFontSize(int numberFontSize) {
        this.numberFontSize = numberFontSize;
        UserConfig.setInt(chartName + "NumberFontSize", getNumberFontSize());
        if (valueAxis != null) {
            valueAxis.setStyle("-fx-font-size: " + this.numberFontSize + "px;");
        }
    }

    public int getNumberTickRotation() {
        numberTickRotation = numberTickRotation < 0 ? 0 : numberTickRotation;
        return numberTickRotation;
    }

    public void setNumberTickRotation(int numberTickRotation) {
        this.numberTickRotation = numberTickRotation;
        UserConfig.setInt(chartName + "NumberTickRotation", getNumberTickRotation());
        if (valueAxis != null) {
            valueAxis.setTickLabelRotation(this.numberTickRotation);
        }
    }

    @Override
    public void setTickFontSize(int tickFontSize) {
        super.setTickFontSize(tickFontSize);
        if (categoryAxis != null) {
            categoryAxis.setStyle("-fx-font-size: " + categoryFontSize
                    + "px; -fx-tick-label-font-size: " + this.tickFontSize + "px; ");
        }
        if (valueAxis != null) {
            valueAxis.setStyle("-fx-font-size: " + numberFontSize
                    + "px; -fx-tick-label-font-size: " + this.tickFontSize + "px; ");
        }
    }

    public boolean isIsXY() {
        return isXY;
    }

    public void setIsXY(boolean isXY) {
        this.isXY = isXY;
        UserConfig.setBoolean(chartName + "XY", isXY);
    }

    public boolean isCategoryIsNumbers() {
        return categoryIsNumbers;
    }

    public XYChartOptions setCategoryIsNumbers(boolean categoryIsNumbers) {
        this.categoryIsNumbers = categoryIsNumbers;
        return this;
    }

    public boolean isDisplayCategoryMark() {
        return displayCategoryMark;
    }

    public void setDisplayCategoryMark(boolean displayCategoryMark) {
        this.displayCategoryMark = displayCategoryMark;
        UserConfig.setBoolean(chartName + "DisplayCategoryMark", displayCategoryMark);
        if (categoryAxis != null) {
            categoryAxis.setTickMarkVisible(displayCategoryMark);
        }
    }

    public boolean isDisplayCategoryTick() {
        return displayCategoryTick;
    }

    public void setDisplayCategoryTick(boolean displayCategoryTick) {
        this.displayCategoryTick = displayCategoryTick;
        UserConfig.setBoolean(chartName + "DisplayCategoryTick", displayCategoryTick);
        if (categoryAxis != null) {
            categoryAxis.setTickLabelsVisible(displayCategoryTick);
        }
    }

    public boolean isDisplayNumberMark() {
        return displayNumberMark;
    }

    public void setDisplayNumberMark(boolean displayNumberMark) {
        this.displayNumberMark = displayNumberMark;
        UserConfig.setBoolean(chartName + "DisplayNumberMark", displayNumberTick);
        if (valueAxis != null) {
            valueAxis.setTickMarkVisible(displayNumberMark);
        }
    }

    public boolean isDisplayNumberTick() {
        return displayNumberTick;
    }

    public void setDisplayNumberTick(boolean displayNumberTick) {
        this.displayNumberTick = displayNumberTick;
        UserConfig.setBoolean(chartName + "DisplayNumberTick", displayNumberTick);
        if (valueAxis != null) {
            valueAxis.setTickLabelsVisible(displayNumberTick);
        }
    }

    public boolean isAltRowsFill() {
        return altRowsFill;
    }

    public void setAltRowsFill(boolean altRowsFill) {
        this.altRowsFill = altRowsFill;
        UserConfig.setBoolean(chartName + "AltRowsFill", altRowsFill);
        if (xyChart != null) {
            xyChart.setAlternativeRowFillVisible(altRowsFill);
        }
    }

    public boolean isAltColumnsFill() {
        return altColumnsFill;
    }

    public void setAltColumnsFill(boolean altColumnsFill) {
        this.altColumnsFill = altColumnsFill;
        UserConfig.setBoolean(chartName + "AltColumnsFill", altColumnsFill);
        if (xyChart != null) {
            xyChart.setAlternativeColumnFillVisible(altColumnsFill);
        }
    }

    public boolean isDisplayVlines() {
        return displayVlines;
    }

    public void setDisplayVlines(boolean displayVlines) {
        this.displayVlines = displayVlines;
        UserConfig.setBoolean(chartName + "DisplayVlines", displayVlines);
        if (xyChart != null) {
            xyChart.setVerticalGridLinesVisible(displayVlines);
        }
    }

    public boolean isDisplayHlines() {
        return displayHlines;
    }

    public void setDisplayHlines(boolean displayHlines) {
        this.displayHlines = displayHlines;
        UserConfig.setBoolean(chartName + "DisplayHlines", displayHlines);
        if (xyChart != null) {
            xyChart.setHorizontalGridLinesVisible(displayHlines);
        }
    }

    public boolean isDisplayVZero() {
        return displayVZero;
    }

    public void setDisplayVZero(boolean displayVZero) {
        this.displayVZero = displayVZero;
        UserConfig.setBoolean(chartName + "DisplayVZero", displayVZero);
        if (xyChart != null) {
            xyChart.setVerticalZeroLineVisible(displayVZero);
        }
    }

    public boolean isDisplayHZero() {
        return displayHZero;
    }

    public void setDisplayHZero(boolean displayHZero) {
        this.displayHZero = displayHZero;
        UserConfig.setBoolean(chartName + "DisplayHZero", displayHZero);
        if (xyChart != null) {
            xyChart.setHorizontalZeroLineVisible(displayHZero);
        }
    }

    public LabelLocation getLabelLocation() {
        labelLocation = labelLocation == null ? LabelLocation.Center : labelLocation;
        return labelLocation;
    }

    public void setLabelLocation(LabelLocation labelLocation) {
        this.labelLocation = labelLocation;
        UserConfig.setString(chartName + "LabelLocation", getLabelLocation().name());
    }

    public Side getCategorySide() {
        categorySide = categorySide == null ? Side.BOTTOM : categorySide;
        return categorySide;
    }

    public void setCategorySide(Side categorySide) {
        this.categorySide = categorySide;
        UserConfig.setString(chartName + "CategorySide", getCategorySide().name());
        if (categoryAxis != null) {
            categoryAxis.setSide(this.categorySide);
        }
    }

    public Side getNumberSide() {
        numberSide = numberSide == null ? Side.LEFT : numberSide;
        return numberSide;
    }

    public void setNumberSide(Side numberSide) {
        this.numberSide = numberSide;
        UserConfig.setString(chartName + "NumberSide", getNumberSide().name());
        if (valueAxis != null) {
            valueAxis.setSide(this.numberSide);
        }
    }

    public double getBarGap() {
        barGap = barGap < 0 ? 0 : barGap;
        return barGap;
    }

    public void setBarGap(double barGap) {
        this.barGap = barGap;
        UserConfig.setDouble(chartName + "BarGap", getBarGap());
    }

    public double getCategoryGap() {
        categoryGap = categoryGap < 0 ? 4 : categoryGap;
        return categoryGap;
    }

    public void setCategoryGap(double categoryGap) {
        this.categoryGap = categoryGap;
        UserConfig.setDouble(chartName + "CategoryGap", getCategoryGap());
    }

    public XYChart getXyChart() {
        return xyChart;
    }

    public void setXyChart(XYChart xyChart) {
        this.xyChart = xyChart;
    }

    public CategoryAxis getCategoryStringAxis() {
        return categoryStringAxis;
    }

    public void setCategoryStringAxis(CategoryAxis categoryStringAxis) {
        this.categoryStringAxis = categoryStringAxis;
    }

    public NumberAxis getCategoryNumberAxis() {
        return categoryNumberAxis;
    }

    public void setCategoryNumberAxis(NumberAxis categoryNumberAxis) {
        this.categoryNumberAxis = categoryNumberAxis;
    }

    public NumberAxis getValueAxis() {
        return valueAxis;
    }

    public void setValueAxis(NumberAxis valueAxis) {
        this.valueAxis = valueAxis;
    }

    public Axis getxAxis() {
        return xAxis;
    }

    public void setxAxis(Axis xAxis) {
        this.xAxis = xAxis;
    }

    public Axis getyAxis() {
        return yAxis;
    }

    public void setyAxis(Axis yAxis) {
        this.yAxis = yAxis;
    }

    public Axis getCategoryAxis() {
        return categoryAxis;
    }

    public void setCategoryAxis(Axis categoryAxis) {
        this.categoryAxis = categoryAxis;
    }

    public String getBubbleStyle() {
        bubbleStyle = bubbleStyle == null || bubbleStyle.isBlank() ? DefaultBubbleStyle : bubbleStyle;
        return bubbleStyle;
    }

    public void setBubbleStyle(String bubbleStyle) {
        this.bubbleStyle = bubbleStyle;
        UserConfig.setString(chartName + "BubbleStyle", bubbleStyle);
        if (bubbleChart != null) {
            ChartTools.setBubbleChart​Colors(bubbleChart, getBubbleStyle(), palette, legendSide != null);
        }
    }

    public boolean isDotted() {
        return dotted;
    }

    public XYChartOptions<X, Y> setDotted(boolean dotted) {
        this.dotted = dotted;
        UserConfig.setBoolean(chartName + "Dotted", dotted);
        return this;
    }

    public LabeledLineChart getLineChart() {
        return lineChart;
    }

    public void setLineChart(LabeledLineChart lineChart) {
        this.lineChart = lineChart;
    }

    public LabeledBarChart getBarChart() {
        return barChart;
    }

    public void setBarChart(LabeledBarChart barChart) {
        this.barChart = barChart;
    }

    public LabeledStackedBarChart getStackedBarChart() {
        return stackedBarChart;
    }

    public void setStackedBarChart(LabeledStackedBarChart stackedBarChart) {
        this.stackedBarChart = stackedBarChart;
    }

    public LabeledScatterChart getScatterChart() {
        return scatterChart;
    }

    public void setScatterChart(LabeledScatterChart scatterChart) {
        this.scatterChart = scatterChart;
    }

    public LabeledAreaChart getAreaChart() {
        return areaChart;
    }

    public void setAreaChart(LabeledAreaChart areaChart) {
        this.areaChart = areaChart;
    }

    public LabeledStackedAreaChart getStackedAreaChart() {
        return stackedAreaChart;
    }

    public void setStackedAreaChart(LabeledStackedAreaChart stackedAreaChart) {
        this.stackedAreaChart = stackedAreaChart;
    }

    public LabeledBubbleChart getBubbleChart() {
        return bubbleChart;
    }

    public void setBubbleChart(LabeledBubbleChart bubbleChart) {
        this.bubbleChart = bubbleChart;
    }

    public BoxWhiskerChart getBoxWhiskerChart() {
        return boxWhiskerChart;
    }

    public void setBoxWhiskerChart(BoxWhiskerChart boxWhiskerChart) {
        this.boxWhiskerChart = boxWhiskerChart;
    }

    public SimpleRegressionChart getSimpleRegressionChart() {
        return simpleRegressionChart;
    }

    public void setSimpleRegressionChart(SimpleRegressionChart simpleRegressionChart) {
        this.simpleRegressionChart = simpleRegressionChart;
    }

    public ResidualChart getResidualChart() {
        return residualChart;
    }

    public void setResidualChart(ResidualChart residualChart) {
        this.residualChart = residualChart;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

}
