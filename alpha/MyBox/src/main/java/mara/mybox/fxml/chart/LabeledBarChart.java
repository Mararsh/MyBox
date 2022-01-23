package mara.mybox.fxml.chart;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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
import static mara.mybox.fxml.chart.ChartTools.createCategoryAxis;
import static mara.mybox.fxml.chart.ChartTools.createNumberAxis;
import mara.mybox.tools.StringTools;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @License Apache License Version 2.0
 */
public class LabeledBarChart<X, Y> extends BarChart<X, Y> {

    protected CategoryAxis categoryAxis;
    protected NumberAxis numberAxis;
    protected Map<Node, TextFlow> nodeMap = new HashMap<>();
    protected boolean intValue;
    protected String cssFile;
    protected LabelType labelType;
    protected ChartCoordinate chartCoordinate;
    protected int textSize;

    public static LabeledBarChart xy(boolean showCategoryAxis, ChartCoordinate chartCoordinate) {
        return new LabeledBarChart(createCategoryAxis(showCategoryAxis), createNumberAxis(chartCoordinate))
                .setChartCoordinate(chartCoordinate);
    }

    public static LabeledBarChart yx(boolean showCategoryAxis, ChartCoordinate chartCoordinate) {
        return new LabeledBarChart(createNumberAxis(chartCoordinate), createCategoryAxis(showCategoryAxis))
                .setChartCoordinate(chartCoordinate);
    }

    public LabeledBarChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        labelType = LabelType.NameAndValue;
        textSize = 10;
//        this.setBarGap(0.0);
//        this.setCategoryGap(0.0);
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    public boolean isXY() {
        return getXAxis() instanceof CategoryAxis;
    }

    public String category(Data<X, Y> item) {
        return "" + (isXY() ? item.getXValue() : item.getYValue());
    }

    public String value(Data<X, Y> item) {
        return "" + (isXY() ? item.getYValue() : item.getXValue());
    }

    public CategoryAxis categoryAxis() {
        return isXY() ? (CategoryAxis) getXAxis() : (CategoryAxis) getYAxis();
    }

    public void displayCategoryAxis(boolean display) {
        categoryAxis().setTickLabelsVisible(display);
    }

    public NumberAxis valueAxis() {
        return isXY() ? (NumberAxis) getYAxis() : (NumberAxis) getXAxis();
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        if (labelType == null || labelType == LabelType.NotDisplay) {
            return;
        }
        try {
            for (int j = 0; j < series.getData().size(); j++) {
                Data<X, Y> item = series.getData().get(j);
                String name = category(item);
                String value = value(item);
                if (labelType == ChartTools.LabelType.Pop) {
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
                            label = name + " " + labelValue;
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

    public void clearData() {
        this.getData().clear();
        this.getPlotChildren().clear();
    }


    /*
       get/set
     */
    public Map<Node, TextFlow> getNodeMap() {
        return nodeMap;
    }

    public LabeledBarChart setNodeMap(Map<Node, TextFlow> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    public boolean isIntValue() {
        return intValue;
    }

    public LabeledBarChart setIntValue(boolean intValue) {
        this.intValue = intValue;
        return this;
    }

    public String getCssFile() {
        return cssFile;
    }

    public LabeledBarChart setCssFile(String cssFile) {
        this.cssFile = cssFile;
        return this;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public LabeledBarChart setLabelType(LabelType labelType) {
        this.labelType = labelType;
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public LabeledBarChart setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public ChartCoordinate getChartCoordinate() {
        return chartCoordinate;
    }

    public LabeledBarChart setChartCoordinate(ChartCoordinate chartCoordinate) {
        this.chartCoordinate = chartCoordinate;
        return this;
    }

}
