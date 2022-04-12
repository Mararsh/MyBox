package mara.mybox.fxml.chart;

import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.controller.Data2DChartController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.chart.ChartTools.ChartCoordinate;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2022-4-12
 * @License Apache License Version 2.0
 */
public class ChartOptions<X, Y> {

    protected XYChart xyChart;
    protected LabelType labelType;
    protected ChartCoordinate xCoordinate, yCoordinate;
    protected int labelFontSize, scale;
    protected boolean popLabel, isXY, isCategoryNumbers;
    protected ChartTools.LabelLocation labelLocation;
    protected Map<Node, Node> nodeMap = new HashMap<>();

    public ChartOptions() {
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

    public ChartOptions(Data2DChartController chartController) {
        xyChart = chartController.getXyChart();
        labelFontSize = chartController.getLabelFontSize();
        labelType = chartController.getLabelType();
        scale = chartController.getScale();
        popLabel = chartController.getPopLabelCheck().isSelected();
        isXY = chartController.isXY();
        isCategoryNumbers = chartController.isCategoryNumbers();
        xCoordinate = chartController.getxCoordinate();
        yCoordinate = chartController.getyCoordinate();
        labelLocation = chartController.getLabelLocation();
    }

    protected void makeLabels(XYChart.Series<X, Y> series, ObservableList<Node> nodes) {
        if (labelType == null) {
            return;
        }
        try {
            xyChart.setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");
            for (int s = 0; s < series.getData().size(); s++) {
                XYChart.Data<X, Y> item = series.getData().get(s);
                Node label = makeLabel(item);
                if (label != null) {
                    label.setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");
                    nodeMap.put(item.getNode(), label);
                    nodes.add(label);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public Node makeLabel(XYChart.Data item) {
        if (item == null || item.getNode() == null) {
            return null;
        }
        try {
            String name, value, extra, nameLabel, valueLabel;
            if (isXY) {
                name = item.getXValue().toString();
                if (isCategoryNumbers) {
                    nameLabel = DoubleTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(name)), scale);
                } else {
                    nameLabel = name;
                }
                value = item.getYValue().toString();
                valueLabel = DoubleTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(value)), scale);
            } else {
                name = item.getYValue().toString();
                if (isCategoryNumbers) {
                    nameLabel = DoubleTools.format(ChartTools.realValue(yCoordinate, Double.valueOf(name)), scale);
                } else {
                    nameLabel = name;
                }
                value = item.getXValue().toString();
                valueLabel = DoubleTools.format(ChartTools.realValue(xCoordinate, Double.valueOf(value)), scale);
            }

            if (item.getExtraValue() != null) {
                extra = " - " + DoubleTools.format((double) item.getExtraValue(), scale);
            } else {
                extra = "";
            }
            if (popLabel || labelType == LabelType.Pop) {
                NodeStyleTools.setTooltip(item.getNode(), name + " - " + value + extra);
            }
            if (labelType == null || labelType == LabelType.NotDisplay) {
                return null;
            }
            String display = null;

            switch (labelType) {
                case Name:
                    display = nameLabel;
                    break;
                case Value:
                    display = valueLabel + extra;
                    break;
                case NameAndValue:
                    display = nameLabel + " - " + valueLabel + extra;
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
        for (Node node : nodeMap.keySet()) {
            Node text = nodeMap.get(node);
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
        if (labelType != null && labelType != LabelType.NotDisplay && labelType != LabelType.Pop) {
            for (Node bar : nodeMap.keySet()) {
                Node text = nodeMap.get(bar);
                nodes.remove(text);
            }
            nodeMap.clear();
        }
    }

    /*
        get/set
     */
    public ChartOptions<X, Y> setLabelType(LabelType labelType) {
        this.labelType = labelType;
        return this;
    }

    public ChartOptions<X, Y> setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
        return this;
    }

    public ChartOptions<X, Y> setIsXY(boolean isXY) {
        this.isXY = isXY;
        return this;
    }

}
