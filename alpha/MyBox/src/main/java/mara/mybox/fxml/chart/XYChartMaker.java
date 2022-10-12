package mara.mybox.fxml.chart;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-16
 * @License Apache License Version 2.0
 */
public class XYChartMaker<X, Y> extends XYChartOptions<X, Y> {

    public XYChartMaker() {
    }

    public XYChartMaker init(ChartType chartType, String chartName) {
        clearChart();
        this.chartType = chartType;
        this.chartName = chartName;
        initXYChartOptions();
        return this;
    }

    /*
        make chart
     */
    public final XYChart makeChart() {
        try {
            clearChart();
            if (chartType == null || chartName == null) {
                return null;
            }
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

    @Override
    public void clearChart() {
        super.clearChart();

        xyChart = null;
        lineChart = null;
        barChart = null;
        stackedBarChart = null;
        areaChart = null;
        stackedAreaChart = null;
        bubbleChart = null;
        scatterChart​ = null;
        boxWhiskerChart = null;
        simpleRegressionChart = null;
        residualChart = null;

        categoryStringAxis = null;
        categoryNumberAxis = null;
        valueAxis = null;
        xAxis = null;
        yAxis = null;
        categoryAxis = null;
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
            }
            categoryAxis.setLabel((displayLabelName ? message("Category") + ": " : "") + getCategoryLabel());
            categoryAxis.setSide(categorySide);
            categoryAxis.setTickLabelsVisible(displayCategoryTick);
            categoryAxis.setTickMarkVisible(displayCategoryMark);
            categoryAxis.setTickLabelRotation(categoryTickRotation);
            categoryAxis.setAnimated(false);
            categoryAxis.setStyle("-fx-font-size: " + categoryFontSize
                    + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");

            valueAxis = new NumberAxis();
            valueAxis.setLabel((displayLabelName ? message("Value") + ": " : "") + getValueLabel());
            valueAxis.setSide(numberSide);
            valueAxis.setTickLabelsVisible(displayNumberTick);
            valueAxis.setTickMarkVisible(displayNumberMark);
            valueAxis.setTickLabelRotation(numberTickRotation);
            ChartTools.setChartCoordinate(valueAxis, numberCoordinate);
            valueAxis.setStyle("-fx-font-size: " + numberFontSize
                    + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");

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
            lineChart = new LabeledLineChart(xAxis, yAxis).setMaker(this);
            if (sort == Sort.X) {
                lineChart.setAxisSortingPolicy​(LineChart.SortingPolicy.X_AXIS);
            } else if (sort == Sort.X) {
                lineChart.setAxisSortingPolicy​(LineChart.SortingPolicy.Y_AXIS);
            } else {
                lineChart.setAxisSortingPolicy​(LineChart.SortingPolicy.NONE);
            }
            xyChart = lineChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBarChart() {
        try {
            barChart = new LabeledBarChart(xAxis, yAxis).setMaker(this);
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
            stackedBarChart = new LabeledStackedBarChart(xAxis, yAxis).setMaker(this);
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
            scatterChart = new LabeledScatterChart​(xAxis, yAxis).setMaker(this);
            xyChart = scatterChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeAreaChart() {
        try {
            areaChart = new LabeledAreaChart(xAxis, yAxis).setMaker(this);
            xyChart = areaChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeStackedAreaChart() {
        try {
            stackedAreaChart = new LabeledStackedAreaChart(xAxis, yAxis).setMaker(this);
            xyChart = stackedAreaChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBubbleChart() {
        try {
            bubbleChart = new LabeledBubbleChart(xAxis, yAxis).setMaker(this);
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
            boxWhiskerChart.setMaker(this);
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
            simpleRegressionChart.setMaker(this);
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
            residualChart.setMaker(this);
            xyChart = residualChart;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    /*
        write data
     */
    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data) {
        writeXYChart(columns, data, null, true);
    }

    /* 
        When hasRowNumber is true:
            The first column is row number
            The second columns is "Category"
        When hasRowNumber is false:
            The first column is "Category"
        Left columns are "Value"
        if "colIndics" is not null, only contained columns are displayed
     */
    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data,
            List<Integer> colIndics, boolean hasRowNumber) {
        try {
            if (columns == null || data == null) {
                return;
            }
            if (chartType == ChartType.Bubble) {
                writeBubbleChart(columns, data, colIndics, hasRowNumber);
                return;
            }
            xyChart.getData().clear();
            XYChart.Data xyData;
            int index = 0, startIndex = hasRowNumber ? 1 : 0;
            for (int col = 1 + startIndex; col < columns.size(); col++) {
                if (colIndics != null && !colIndics.contains(col)) {
                    continue;
                }
                Data2DColumn column = columns.get(col);
                String colName = column.getColumnName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);

                double numberValue;
                for (List<String> rowData : data) {
                    if (startIndex >= rowData.size() || col >= rowData.size()) {
                        continue;
                    }
                    String category = rowData.get(startIndex);
                    if (category == null) {
                        category = "";
                    }
                    numberValue = scaleValue(rowData.get(col));
                    if (DoubleTools.invalidDouble(numberValue)) {
                        if (invalidAs == InvalidAs.Zero) {
                            numberValue = 0;
                        } else {
                            continue;
                        }
                    }
                    numberValue = ChartTools.coordinateValue(numberCoordinate, numberValue);
                    if (isXY) {
                        if (xyChart.getXAxis() instanceof NumberAxis) {
                            double categoryValue = scaleValue(category);
                            if (DoubleTools.invalidDouble(categoryValue)) {
                                if (invalidAs == InvalidAs.Zero) {
                                    categoryValue = 0;
                                } else {
                                    continue;
                                }
                            }
                            xyData = new XYChart.Data(categoryValue, numberValue);
                        } else {
                            xyData = new XYChart.Data(category, numberValue);
                        }
                    } else {
                        if (xyChart.getYAxis() instanceof NumberAxis) {
                            double categoryValue = scaleValue(category);
                            if (DoubleTools.invalidDouble(categoryValue)) {
                                if (invalidAs == InvalidAs.Zero) {
                                    categoryValue = 0;
                                } else {
                                    continue;
                                }
                            }
                            xyData = new XYChart.Data(numberValue, categoryValue);
                        } else {
                            xyData = new XYChart.Data(numberValue, category);
                        }
                    }
                    series.getData().add(xyData);
                }

                xyChart.getData().add(index++, series);
            }

            setChartStyle();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        The first column is row number
        The second columns is "Category"
        The third columns is "Value"
        Left columns are "Size"
     */
    public void writeBubbleChart(List<Data2DColumn> columns, List<List<String>> data,
            List<Integer> colIndics, boolean rowNumber) {
        try {
            if (columns == null || data == null) {
                return;
            }
            xyChart.getData().clear();
            XYChart.Data xyData;
            int index = 0, startIndex = rowNumber ? 1 : 0;
            for (int col = 2 + startIndex; col < columns.size(); col++) {
                if (colIndics != null && !colIndics.contains(col)) {
                    continue;
                }
                Data2DColumn column = columns.get(col);
                String colName = column.getColumnName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);

                double categoryValue, categoryCoordinateValue, numberValue, numberCoordinateValue,
                        sizeValue, sizeCoordinateValue;
                for (List<String> rowData : data) {
                    categoryValue = scaleValue(rowData.get(startIndex));
                    categoryCoordinateValue = ChartTools.coordinateValue(categoryCoordinate, categoryValue);
                    numberValue = scaleValue(rowData.get(startIndex + 1));
                    numberCoordinateValue = ChartTools.coordinateValue(numberCoordinate, numberValue);
                    sizeValue = scaleValue(rowData.get(col));
                    if (sizeValue <= 0) {
                        continue;
                    }
                    sizeCoordinateValue = ChartTools.coordinateValue(sizeCoordinate, sizeValue);
                    if (isXY) {
                        xyData = new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                    } else {
                        xyData = new XYChart.Data(numberCoordinateValue, categoryCoordinateValue);
                    }
                    xyData.setExtraValue(sizeCoordinateValue);
                    series.getData().add(xyData);
                }
                xyChart.getData().add(index++, series);

            }

            setChartStyle();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        style
     */
    public void setChartStyle() {
        if (xyChart == null) {
            return;
        }
        if (barChart != null) {
            ChartTools.setBarChartColors(barChart, palette, legendSide != null);
        } else if (stackedBarChart != null) {
            ChartTools.setBarChartColors(stackedBarChart, palette, legendSide != null);
        } else if (lineChart != null) {
            ChartTools.setLineChartColors(lineChart, lineWidth, palette, legendSide != null, dotted);
        } else if (areaChart != null) {
            ChartTools.setAreaChartColors(areaChart, lineWidth, palette, legendSide != null);
        } else if (stackedAreaChart != null) {
            ChartTools.setAreaChartColors(stackedAreaChart, lineWidth, palette, legendSide != null);
        } else if (scatterChart != null) {
            ChartTools.setScatterChart​Colors(scatterChart, palette, legendSide != null);
        } else if (bubbleChart != null) {
            ChartTools.setBubbleChart​Colors(bubbleChart, bubbleStyle, palette, legendSide != null);
        } else if (boxWhiskerChart != null) {
            ChartTools.setLineChartColors(boxWhiskerChart, lineWidth, palette, legendSide != null, dotted);
        } else if (simpleRegressionChart != null) {
            ChartTools.setScatterChart​Colors(simpleRegressionChart, palette, legendSide != null);
        } else if (residualChart != null) {
            ChartTools.setScatterChart​Colors(residualChart, palette, legendSide != null);
        }
    }

    /*
        labels
     */
    protected void makeLabels(XYChart.Series<X, Y> series, ObservableList<Node> nodes) {
        if (labelType == null || xyChart == null) {
            return;
        }
        try {
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
                category = item.getXValue() == null ? "" : item.getXValue().toString();
                if (categoryIsNumbers) {
                    categoryDis = NumberTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(category)), scale);
                } else {
                    categoryDis = category;
                }
                number = item.getYValue() == null ? "" : item.getYValue().toString();
                numberDis = NumberTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(number)), scale);
            } else {
                categoryName = xyChart.getYAxis().getLabel();
                category = item.getYValue() == null ? "" : item.getYValue().toString();
                if (categoryIsNumbers) {
                    categoryDis = NumberTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(category)), scale);
                } else {
                    categoryDis = category;
                }
                number = item.getXValue() == null ? "" : item.getXValue().toString();
                numberDis = NumberTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(number)), scale);
            }

            if (item.getExtraValue() != null) {
                double d = (double) item.getExtraValue();
                extra = "\n" + (displayLabelName ? message("Size") + ": " : "") + NumberTools.format(d, scale);
                extraDis = "\n" + (displayLabelName ? message("Size") + ": " : "")
                        + NumberTools.format(ChartTools.realValue(sizeCoordinate, d), scale);
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

    protected void removeLabels(ObservableList<Node> nodes) {
        if (labelType == null || labelType == LabelType.NotDisplay || labelType == LabelType.Pop) {
            return;
        }
        for (Node node : nodeLabels.keySet()) {
            Node text = nodeLabels.get(node);
            nodes.remove(text);
        }
        nodeLabels.clear();
    }

}
