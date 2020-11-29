package thridparty;

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
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlControl.ChartCoordinate;
import mara.mybox.fxml.FxmlControl.LabelType;
import mara.mybox.fxml.Logarithmic10Coordinate;
import mara.mybox.fxml.LogarithmicECoordinate;
import mara.mybox.fxml.SquareRootCoordinate;
import mara.mybox.tools.StringTools;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Roland
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 *
 * #### Changed a little by Mara
 */
public class LabeledBarChart<X, Y> extends BarChart<X, Y> {

    protected Map<Node, TextFlow> nodeMap = new HashMap<>();
    protected boolean intValue;
    protected String cssFile;
    protected LabelType labelType;
    protected ChartCoordinate chartCoordinate;
    protected int textSize;

    public static LabeledBarChart create(boolean displayCategoryAxis, ChartCoordinate chartCoordinate) {
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setSide(Side.BOTTOM);
        categoryAxis.setTickLabelsVisible(displayCategoryAxis);
        categoryAxis.setGapStartAndEnd(true);

        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setSide(Side.LEFT);
        switch (chartCoordinate) {
            case LogarithmicE:
                numberAxis.setTickLabelFormatter(new LogarithmicECoordinate());
                break;
            case Logarithmic10:
                numberAxis.setTickLabelFormatter(new Logarithmic10Coordinate());
                break;
            case SquareRoot:
                numberAxis.setTickLabelFormatter(new SquareRootCoordinate());
                break;
        }

        return new LabeledBarChart(categoryAxis, numberAxis)
                .setChartCoordinate(chartCoordinate);
    }

    public LabeledBarChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    private void init() {
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

    public LabeledBarChart style(String cssFile) {
        this.getStylesheets().add(LabeledBarChart.class.getResource(cssFile).toExternalForm());
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        if (labelType == null || labelType == LabelType.NotDisplay || labelType == LabelType.Pop) {
            return;
        }
        try {
            for (int j = 0; j < series.getData().size(); j++) {
                Data<X, Y> item = series.getData().get(j);
                String name = item.getXValue() + "";
                String value = item.getYValue() + "";
                String label;
                String labelValue = StringTools.format(FxmlControl.realValue(chartCoordinate, Double.valueOf(value)));
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
                this.getPlotChildren().add(textFlow);
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
