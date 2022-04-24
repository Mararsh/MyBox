package mara.mybox.fxml.chart;

import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.controller.BaseData2DChartXYController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.chart.ChartTools.ChartCoordinate;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-12
 * @License Apache License Version 2.0
 */
public class XYChartOptions<X, Y> {

    protected BaseData2DChartXYController chartController;
    protected XYChart xyChart;
    protected LabelType labelType;
    protected ChartCoordinate xCoordinate, yCoordinate, sCoordinate;
    protected int labelFontSize, scale;
    protected boolean popLabel, isXY, isCategoryNumbers;
    protected ChartTools.LabelLocation labelLocation;
    protected Map<Node, Node> nodeLabels = new HashMap<>();

    public XYChartOptions(XYChart xyChart) {
        this.xyChart = xyChart;
        labelType = LabelType.NameAndValue;
        xCoordinate = ChartCoordinate.Cartesian;
        yCoordinate = ChartCoordinate.Cartesian;
        labelFontSize = 10;
        scale = 2;
        popLabel = true;
        isXY = true;
        isCategoryNumbers = false;
        labelLocation = ChartTools.LabelLocation.Above;
    }

    public XYChartOptions(BaseData2DChartXYController chartController) {
        this.chartController = chartController;
        refreshOptions();
    }

    public final void refreshOptions() {
        xyChart = chartController.getXyChart();
        labelFontSize = chartController.getLabelFontSize();
        labelType = chartController.getLabelType();
        scale = chartController.getScale();
        popLabel = chartController.getPopLabelCheck().isSelected();
        isXY = chartController.isXY();
        isCategoryNumbers = chartController.isCategoryNumbers();
        xCoordinate = chartController.getxCoordinate();
        yCoordinate = chartController.getyCoordinate();
        sCoordinate = chartController.getsCoordinate();
        labelLocation = chartController.getLabelLocation();
    }

    protected void makeLabels(XYChart.Series<X, Y> series, ObservableList<Node> nodes) {
        if (labelType == null || xyChart == null) {
            return;
        }
        try {
            refreshOptions();
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
                if (isCategoryNumbers) {
                    categoryDis = DoubleTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(category)), scale);
                } else {
                    categoryDis = category;
                }
                number = item.getYValue().toString();
                numberDis = DoubleTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(number)), scale);
            } else {
                categoryName = xyChart.getYAxis().getLabel();
                category = item.getYValue().toString();
                if (isCategoryNumbers) {
                    categoryDis = DoubleTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(category)), scale);
                } else {
                    categoryDis = category;
                }
                number = item.getXValue().toString();
                numberDis = DoubleTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(number)), scale);
            }

            if (item.getExtraValue() != null) {
                double d = (double) item.getExtraValue();
                extra = "\n" + message("Size") + ": " + DoubleTools.format(d, scale);
                extraDis = "\n" + message("Size") + ": " + DoubleTools.format(ChartTools.realValue(sCoordinate, d), scale);
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
                case Name:
                    display = categoryName + ": " + categoryDis;
                    break;
                case Value:
                    display = numberName + ": " + numberDis + extraDis;
                    break;
                case NameAndValue:
                    display = categoryName + ": " + categoryDis + "\n"
                            + numberName + ": " + numberDis + extraDis;
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
    public XYChartOptions<X, Y> setLabelType(LabelType labelType) {
        this.labelType = labelType;
        return this;
    }

    public XYChartOptions<X, Y> setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
        return this;
    }

    public XYChartOptions<X, Y> setIsXY(boolean isXY) {
        this.isXY = isXY;
        return this;
    }

}
