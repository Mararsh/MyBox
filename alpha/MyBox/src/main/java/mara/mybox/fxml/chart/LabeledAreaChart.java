package mara.mybox.fxml.chart;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.controller.Data2DChartController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.chart.ChartTools.ChartCoordinate;
import mara.mybox.fxml.chart.ChartTools.LabelLocation;
import mara.mybox.fxml.chart.ChartTools.LabelType;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @CreateDate 2022-1-25
 * @License Apache License Version 2.0
 */
public class LabeledAreaChart<X, Y> extends AreaChart<X, Y> {

    protected Map<Node, Node> nodeMap = new HashMap<>();
    protected LabelType labelType;
    protected LabelLocation labelLocation;
    protected Data2DChartController chartController;
    protected ChartCoordinate chartCoordinate;
    protected int labelFontSize, scale;
    protected boolean popLabel;

    public LabeledAreaChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        labelType = LabelType.NameAndValue;
        chartCoordinate = ChartCoordinate.Cartesian;
        labelFontSize = 10;
        scale = 2;
        popLabel = false;
        labelLocation = LabelLocation.Above;
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    public LabeledAreaChart setChartController(Data2DChartController chartController) {
        this.chartController = chartController;
        labelType = chartController.getLabelType();
        labelFontSize = chartController.getLabelFontSize();
        scale = chartController.getScale();
        popLabel = chartController.getPopLabelCheck().isSelected();
        labelLocation = chartController.getLabelLocation();
        setChartCoordinate(chartController.getChartCoordinate());
        setCreateSymbols(labelType != null && labelType != LabelType.NotDisplay);
        return this;
    }

    public LabeledAreaChart setChartCoordinate(ChartCoordinate chartCoordinate) {
        this.chartCoordinate = chartCoordinate;
        ChartTools.setChartCoordinate(this, chartCoordinate);
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        if (labelType == null) {
            return;
        }
        try {
            setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");
            boolean isXY = getXAxis() instanceof CategoryAxis;
            for (int s = 0; s < series.getData().size(); s++) {
                Data<X, Y> item = series.getData().get(s);
                Node label = ChartTools.makeLabel(item, isXY, labelType, popLabel, chartCoordinate, scale);
                if (label != null) {
                    label.setStyle("-fx-font-size: " + labelFontSize + "px;  -fx-text-fill: black;");
                    nodeMap.put(item.getNode(), label);
                    getPlotChildren().add(label);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        if (labelType != null && labelType != LabelType.NotDisplay && labelType != LabelType.Pop) {
            for (Node bar : nodeMap.keySet()) {
                Node text = nodeMap.get(bar);
                this.getPlotChildren().remove(text);
            }
            nodeMap.clear();
        }
        super.seriesRemoved(series);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
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

    /*
       get/set
     */
    public Map<Node, Node> getNodeMap() {
        return nodeMap;
    }

    public LabeledAreaChart setNodeMap(Map<Node, Node> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public LabeledAreaChart setLabelType(LabelType labelType) {
        this.labelType = labelType;
        return this;
    }

    public int getTextSize() {
        return labelFontSize;
    }

    public LabeledAreaChart setTextSize(int textSize) {
        this.labelFontSize = textSize;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public LabeledAreaChart setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public Data2DChartController getChartController() {
        return chartController;
    }
}
