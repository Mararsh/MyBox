package mara.mybox.fxml.chart;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.chart.ChartTools.ChartCoordinate;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.tools.StringTools;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @CreateDate 2022-1-24
 * @License Apache License Version 2.0
 */
public class LabeledLineChart<X, Y> extends LineChart<X, Y> {

    protected Map<Node, TextFlow> nodeMap = new HashMap<>();
    protected boolean intValue;
    protected String cssFile;
    protected LabelType labelType;
    protected ChartCoordinate chartCoordinate;
    protected int textSize;

    public LabeledLineChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        labelType = LabelType.NameAndValue;
        textSize = 10;
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    public LabeledLineChart setChartCoordinate(ChartCoordinate chartCoordinate) {
        this.chartCoordinate = chartCoordinate;
        ChartTools.setChartCoordinate(this, chartCoordinate);
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        if (labelType == null || labelType == LabelType.NotDisplay) {
            setCreateSymbols(false);
            return;
        }
        try {
            setCreateSymbols(true);
            if (labelType == LabelType.Point) {
                return;
            }
            setStyle("-fx-font-size: " + textSize + "px;  -fx-text-fill: black;");
            boolean isXY = getXAxis() instanceof CategoryAxis;
            for (int s = 0; s < series.getData().size(); s++) {
                Data<X, Y> item = series.getData().get(s);
                String name, value;
                if (isXY) {
                    name = item.getXValue().toString();
                    value = item.getYValue().toString();
                } else {
                    name = item.getYValue().toString();
                    value = item.getXValue().toString();
                }
                if (labelType == LabelType.Pop) {
                    NodeStyleTools.setTooltip(item.getNode(), name + ": " + value);
                } else {
                    String label;
                    String labelValue = StringTools.format(ChartTools.realValue(chartCoordinate, Double.valueOf(value)));
                    switch (labelType) {
                        case Name:
                            label = name;
                            break;
                        case Value:
                            label = labelValue;
                            break;
                        case NameAndValue:
                        default:
                            label = name + ": " + labelValue;
                            break;
                    }
                    Text text = new Text(label);
                    text.setStyle("-fx-font-size: " + textSize + "px;  -fx-text-fill: black;");
                    TextFlow textFlow = new TextFlow(text);
                    textFlow.setTextAlignment(TextAlignment.CENTER);

                    nodeMap.put(item.getNode(), textFlow);
                    getPlotChildren().add(textFlow);
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
        if (labelType == null || labelType == LabelType.NotDisplay || labelType == LabelType.Pop) {
            return;
        }
        for (Node bar : nodeMap.keySet()) {
            TextFlow textFlow = nodeMap.get(bar);
            textFlow.relocate(bar.getBoundsInParent().getMinX(), bar.getBoundsInParent().getMinY() - 10);
        }
    }

    /*
       get/set
     */
    public Map<Node, TextFlow> getNodeMap() {
        return nodeMap;
    }

    public LabeledLineChart setNodeMap(Map<Node, TextFlow> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    public boolean isIntValue() {
        return intValue;
    }

    public LabeledLineChart setIntValue(boolean intValue) {
        this.intValue = intValue;
        return this;
    }

    public String getCssFile() {
        return cssFile;
    }

    public LabeledLineChart setCssFile(String cssFile) {
        this.cssFile = cssFile;
        return this;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public LabeledLineChart setLabelType(LabelType labelType) {
        this.labelType = labelType;
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public LabeledLineChart setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public ChartCoordinate getChartCoordinate() {
        return chartCoordinate;
    }

}
