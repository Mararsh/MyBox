package mara.mybox.fxml.chart;

import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
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

    protected String categoryLabel, valueLabel;
    protected ChartCoordinate categoryCoordinate, numberCoordinate, sizeCoordinate,
            xCoordinate, yCoordinate;
    protected int lineWidth, categoryFontSize, categoryMargin, categoryTickRotation,
            numberFontSize, numberTickRotation;
    protected boolean isXY, categoryIsNumbers,
            displayCategoryMark, displayCategoryTick, displayNumberMark, displayNumberTick,
            altRowsFill, altColumnsFill, displayVlines, displayHlines, displayVZero, displayHZero;
    protected LabelLocation labelLocation;
    protected Side categorySide, numberSide;
    protected double barGap, categoryGap;

    public static enum LabelLocation {
        Above, Below, Center
    }

    public static enum ChartCoordinate {
        Cartesian, LogarithmicE, Logarithmic10, SquareRoot
    }

    public static final String DefaultBubbleStyle
            = "radial-gradient(center 50% 50%, radius 50%, transparent 0%, transparent 90%, derive(-fx-bubble-fill,0%) 100%)";

    public XYChartOptions(ChartType chartType, String chartName) {
        this.chartType = chartType;
        this.chartName = chartName;
        makeChart();
    }

    public final XYChart makeChart() {
        try {
            if (chartType == null || chartName == null) {
                return null;
            }
            initXYChartOptions();
            initAxis();
            switch (chartType) {
                case Bar:
                    makeBarChart();
                    break;
                case StackedBar:
                    makeStackedBarChart();
                    break;
                case Line:
                    makeLineChart();
                    break;
                case Scatter:
                    makeScatterChart​();
                    break;
                case Area:
                    makeAreaChart();
                    break;
                case StackedArea:
                    makeStackedAreaChart();
                    break;
                case Bubble:
                    makeBubbleChart();
                    break;
                case BoxWhiskerChart:
                    makeBoxWhiskerChart();
                    break;
                case SimpleRegressionChart:
                    makeSimpleRegressionChart();
                    break;
                case ResidualChart:
                    makeResidualChart();
                    break;
                default:
                    break;
            }
            initXYChart();
            styleChart();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return xyChart;
    }

    public final void initXYChartOptions() {
        try {
            if (chartName == null) {
                return;
            }
            initChartOptions();

            isXY = UserConfig.getBoolean(chartName + "XY", true);
            switch (chartType) {
                case Bubble:
                    categoryIsNumbers = true;
                    break;
                case Bar:
                case StackedBar:
                case Area:
                case StackedArea:
                    categoryIsNumbers = false;
                    break;
                default:
                    categoryIsNumbers = UserConfig.getBoolean(chartName + "CategoryIsNumbers", false);
                    break;
            }
            displayCategoryMark = UserConfig.getBoolean(chartName + "DisplayCategoryMark", true);
            displayCategoryTick = UserConfig.getBoolean(chartName + "DisplayCategoryTick", true);
            displayNumberMark = UserConfig.getBoolean(chartName + "DisplayNumberMark", true);
            displayNumberTick = UserConfig.getBoolean(chartName + "DisplayNumberTick", true);
            plotAnimated = UserConfig.getBoolean(chartName + "PlotAnimated", false);

            categoryFontSize = UserConfig.getInt(chartName + "CategoryFontSize", 10);
            categoryMargin = UserConfig.getInt(chartName + "CategoryMargin", 2);
            categoryTickRotation = UserConfig.getInt(chartName + "CategoryTickRotation", 90);
            numberFontSize = UserConfig.getInt(chartName + "NumberFontSize", 10);
            numberTickRotation = UserConfig.getInt(chartName + "NumberTickRotation", 0);
            lineWidth = UserConfig.getInt(chartName + "LineWidth", 2);
            barGap = UserConfig.getDouble(chartName + "BarGap", 2d);
            categoryGap = UserConfig.getDouble(chartName + "CategoryGap", 20d);

            labelLocation = LabelLocation.Center;
            String saved = UserConfig.getString(chartName + "LabelLocation", "Center");
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
    }

    public void initAxis() {
        try {
            if (categoryIsNumbers) {
                categoryNumberAxis = new NumberAxis();
                categoryAxis = categoryNumberAxis;
                ChartTools.setChartCoordinate(categoryNumberAxis, categoryCoordinate);
            } else {
                categoryStringAxis = new CategoryAxis();
                categoryAxis = categoryStringAxis;
                categoryStringAxis.setGapStartAndEnd(true);
//                if (isXY) {
//                    categoryStringAxis.setEndMargin(100);
//                } else {
//                    categoryStringAxis.setEndMargin(20);
//                }
            }
            categoryAxis.setLabel(categoryLabel);
            categoryAxis.setSide(categorySide);
            categoryAxis.setTickLabelsVisible(displayCategoryTick);
            categoryAxis.setTickMarkVisible(displayCategoryMark);
            categoryAxis.setTickLabelRotation(categoryTickRotation);
            categoryAxis.setAnimated(false);
            categoryAxis.setStyle("-fx-font-size: " + categoryFontSize + "px;");

            valueAxis = new NumberAxis();
            valueAxis.setLabel(valueLabel);
            valueAxis.setSide(numberSide);
            valueAxis.setTickLabelsVisible(displayNumberTick);
            valueAxis.setTickMarkVisible(displayNumberMark);
            valueAxis.setTickLabelRotation(numberTickRotation);
            ChartTools.setChartCoordinate(valueAxis, numberCoordinate);
            valueAxis.setStyle("-fx-font-size: " + numberFontSize + "px;");

            if (isXY) {
                xAxis = categoryAxis;
                yAxis = valueAxis;
                xCoordinate = categoryCoordinate;
                yCoordinate = numberCoordinate;
            } else {
                xAxis = valueAxis;
                yAxis = categoryAxis;
                xCoordinate = numberCoordinate;
                yCoordinate = categoryCoordinate;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initXYChart() {
        try {
            xyChart.setAlternativeRowFillVisible(altRowsFill);
            xyChart.setAlternativeColumnFillVisible(altColumnsFill);
            xyChart.setVerticalGridLinesVisible(displayVlines);
            xyChart.setHorizontalGridLinesVisible(displayHlines);
            xyChart.setVerticalZeroLineVisible(displayVZero);
            xyChart.setHorizontalZeroLineVisible(displayHZero);

            if (legendSide == null) {
                xyChart.setLegendVisible(false);
            } else {
                xyChart.setLegendVisible(true);
                xyChart.setLegendSide(legendSide);
            }
            xyChart.getXAxis().setAnimated(false); // If not, the axis becomes messed
            chart = xyChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean makeLineChart() {
        try {
            lineChart = new LabeledLineChart(xAxis, yAxis).setOptions(this);
            xyChart = lineChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBarChart() {
        try {
            barChart = new LabeledBarChart(xAxis, yAxis).setOptions(this);
            barChart.setBarGap(barGap);
            barChart.setCategoryGap(categoryGap);
            xyChart = barChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeStackedBarChart() {
        try {
            stackedBarChart = new LabeledStackedBarChart(xAxis, yAxis).setOptions(this);
            stackedBarChart.setCategoryGap(categoryGap);
            xyChart = stackedBarChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeScatterChart​() {
        try {
            scatterChart = new LabeledScatterChart​(xAxis, yAxis).setOptions(this);
            xyChart = scatterChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeAreaChart() {
        try {
            areaChart = new LabeledAreaChart(xAxis, yAxis).setOptions(this).setOptions(this);
            xyChart = areaChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeStackedAreaChart() {
        try {
            stackedAreaChart = new LabeledStackedAreaChart(xAxis, yAxis).setOptions(this);
            xyChart = stackedAreaChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBubbleChart() {
        try {
            bubbleChart = new LabeledBubbleChart(xAxis, yAxis).setOptions(this);
            xyChart = bubbleChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBoxWhiskerChart() {
        try {
            boxWhiskerChart = new BoxWhiskerChart(xAxis, yAxis);
            boxWhiskerChart.setOptions(this);
            xyChart = boxWhiskerChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeSimpleRegressionChart() {
        try {
            simpleRegressionChart = new SimpleRegressionChart(xAxis, yAxis);
            simpleRegressionChart.setOptions(this);
            xyChart = simpleRegressionChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeResidualChart() {
        try {
            residualChart = new ResidualChart(xAxis, yAxis);
            residualChart.setOptions(this);
            xyChart = residualChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    protected void makeLabels(XYChart.Series<X, Y> series, ObservableList<Node> nodes) {
        if (labelType == null || xyChart == null) {
            return;
        }
        try {
            xyChart.setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");
            for (int s = 0; s < series.getData().size(); s++) {
                XYChart.Data<X, Y> item = series.getData().get(s);
                Node label = makeLabel(series.getName(), item);
                if (label != null) {
                    label.setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");
                    nodeLabels.put(item.getNode(), label);
                    nodes.add(label);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private Node makeLabel(String numberName, XYChart.Data item) {
        if (item == null || item.getNode() == null) {
            return null;
        }
        try {
            String categoryName, category, number, extra, categoryDis, numberDis, extraDis;
            if (isXY) {
                categoryName = xyChart.getXAxis().getLabel();
                category = item.getXValue().toString();
                if (categoryIsNumbers) {
                    categoryDis = DoubleTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(category)), scale);
                } else {
                    categoryDis = category;
                }
                number = item.getYValue().toString();
                numberDis = DoubleTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(number)), scale);
            } else {
                categoryName = xyChart.getYAxis().getLabel();
                category = item.getYValue().toString();
                if (categoryIsNumbers) {
                    categoryDis = DoubleTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(category)), scale);
                } else {
                    categoryDis = category;
                }
                number = item.getXValue().toString();
                numberDis = DoubleTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(number)), scale);
            }

            if (item.getExtraValue() != null) {
                double d = (double) item.getExtraValue();
                extra = "\n" + (displayLabelName ? message("Size") + ": " : "") + DoubleTools.format(d, scale);
                extraDis = "\n" + (displayLabelName ? message("Size") + ": " : "")
                        + DoubleTools.format(ChartTools.realValue(sizeCoordinate, d), scale);
            } else {
                extra = "";
                extraDis = "";
            }
            if (popLabel || labelType == LabelType.Pop) {
                NodeStyleTools.setTooltip(item.getNode(),
                        categoryName + ": " + categoryDis + "\n"
                        + numberName + ": " + numberDis + extraDis);
            }
            if (labelType == null || labelType == LabelType.NotDisplay) {
                return null;
            }
            String display = null;

            switch (labelType) {
                case Category:
                    display = (displayLabelName ? categoryName + ": " : "") + categoryDis;
                    break;
                case Value:
                    display = (displayLabelName ? numberName + ": " : "") + numberDis + extraDis;
                    break;
                case CategoryAndValue:
                    display = (displayLabelName ? categoryName + ": " : "") + categoryDis + "\n"
                            + (displayLabelName ? numberName + ": " : "") + numberDis + extraDis;
                    break;
            }
            if (display != null && !display.isBlank()) {
                Text text = new Text(display);
                text.setTextAlignment(TextAlignment.CENTER);
                return text;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    protected void displayLabels() {
        if (labelType == null || labelLocation == null
                || labelType == LabelType.NotDisplay || labelType == LabelType.Pop) {
            return;
        }
        for (Node node : nodeLabels.keySet()) {
            Node text = nodeLabels.get(node);
            switch (labelLocation) {
                case Below:
                    LocateTools.belowCenter(text, node);
                    break;
                case Above:
                    LocateTools.aboveCenter(text, node);
                    break;
                case Center:
                    LocateTools.center(text, node);
                    break;
            }
        }
    }

    protected void removeLabels(XYChart.Series<X, Y> series, ObservableList<Node> nodes) {
        if (labelType == null || labelType == LabelType.NotDisplay || labelType == LabelType.Pop) {
            return;
        }
        for (Node node : nodeLabels.keySet()) {
            Node text = nodeLabels.get(node);
            nodes.remove(text);
        }
        nodeLabels.clear();
    }

    /*
        get/set
     */
    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public String getValueLabel() {
        return valueLabel;
    }

    public void setValueLabel(String valueLabel) {
        this.valueLabel = valueLabel;
    }

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

    public void setCategoryIsNumbers(boolean categoryIsNumbers) {
        this.categoryIsNumbers = categoryIsNumbers;
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
            valueAxis.setTickMarkVisible(displayNumberTick);
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
    }

    public boolean isAltColumnsFill() {
        return altColumnsFill;
    }

    public void setAltColumnsFill(boolean altColumnsFill) {
        this.altColumnsFill = altColumnsFill;
    }

    public boolean isDisplayVlines() {
        return displayVlines;
    }

    public void setDisplayVlines(boolean displayVlines) {
        this.displayVlines = displayVlines;
    }

    public boolean isDisplayHlines() {
        return displayHlines;
    }

    public void setDisplayHlines(boolean displayHlines) {
        this.displayHlines = displayHlines;
    }

    public boolean isDisplayVZero() {
        return displayVZero;
    }

    public void setDisplayVZero(boolean displayVZero) {
        this.displayVZero = displayVZero;
    }

    public boolean isDisplayHZero() {
        return displayHZero;
    }

    public void setDisplayHZero(boolean displayHZero) {
        this.displayHZero = displayHZero;
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
        UserConfig.setDouble(chartName + "CcategoryGap", getCategoryGap());
    }

    public XYChart getXyChart() {
        return xyChart;
    }

    public void setXyChart(XYChart xyChart) {
        this.xyChart = xyChart;
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

}
