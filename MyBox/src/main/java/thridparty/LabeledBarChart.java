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

/**
 * @Author Roland
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 *
 * #### Changed a little by Mara
 */
public class LabeledBarChart<X, Y> extends BarChart<X, Y> {

    protected Map<Node, TextFlow> nodeMap = new HashMap<>();
    protected boolean displayLabel, intValue;
    protected String cssFile;

    public static LabeledBarChart create() {
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setSide(Side.BOTTOM);
        categoryAxis.setTickLabelRotation(45);

        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setSide(Side.LEFT);

        return new LabeledBarChart(categoryAxis, numberAxis);
    }

    public LabeledBarChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    private void init() {
        displayLabel = true;
        this.setBarGap(0.0);
        this.setCategoryGap(2);
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
        if (!displayLabel) {
            return;
        }
        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);

            String v = item.getYValue() + "";
            if (intValue) {
                try {
                    v = Math.round(Double.valueOf(v)) + "";
                } catch (Exception e) {
                }
            }
            Text text = new Text(v);
            text.setStyle("-fx-font-size: 0.6em;  -fx-text-fill: black;");

            TextFlow textFlow = new TextFlow(text);
            textFlow.setTextAlignment(TextAlignment.CENTER);

            nodeMap.put(item.getNode(), textFlow);
            this.getPlotChildren().add(textFlow);
        }
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        if (displayLabel) {
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
        if (!displayLabel) {
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

    public boolean isDisplayLabel() {
        return displayLabel;
    }

    public LabeledBarChart setDisplayLabel(boolean displayLabel) {
        this.displayLabel = displayLabel;
        return this;
    }

    public boolean isIntValue() {
        return intValue;
    }

    public LabeledBarChart setIntValue(boolean intValue) {
        this.intValue = intValue;
        return this;
    }

}
