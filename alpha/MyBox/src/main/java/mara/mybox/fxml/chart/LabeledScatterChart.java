package mara.mybox.fxml.chart;

import java.util.List;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import mara.mybox.dev.MyBoxLog;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @CreateDate 2022-1-25
 * @License Apache License Version 2.0
 */
public class LabeledScatterChart<X, Y> extends ScatterChart<X, Y> {

    protected XYChartMaker chartMaker;

    public LabeledScatterChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        chartMaker = new XYChartMaker<Axis, Axis>();
    }

    public LabeledScatterChart setMaker(XYChartMaker<X, Y> chartMaker) {
        this.chartMaker = chartMaker;
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        chartMaker.makeLabels(series, getPlotChildren());
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        chartMaker.removeLabels(getPlotChildren());
        super.seriesRemoved(series);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        chartMaker.displayLabels();
    }

    public void drawLine(List<XYChart.Data<X, Y>> data, Line line, boolean pointsVisible) {
        try {
            if (data == null || line == null) {
                return;
            }
            double startX = Double.MAX_VALUE, startY = Double.MAX_VALUE,
                    endX = -Double.MAX_VALUE, endY = -Double.MAX_VALUE;
            for (int i = 0; i < data.size(); i++) {
                Node node = data.get(i).getNode();
                Bounds regionBounds = node.getBoundsInParent();
                double x = regionBounds.getMinX() + regionBounds.getWidth() / 2;
                double y = regionBounds.getMinY() + regionBounds.getHeight() / 2;
                if (chartMaker.isXY) {
                    if (x > endX) {
                        endX = x;
                        endY = y;
                    }
                    if (x < startX) {
                        startX = x;
                        startY = y;
                    }
                } else {
                    if (y > endY) {
                        endX = x;
                        endY = y;
                    }
                    if (y < startY) {
                        startX = x;
                        startY = y;
                    }
                }
                node.setVisible(pointsVisible);
            }
            if (startX == Double.MAX_VALUE || endX == -Double.MAX_VALUE) {
                return;
            }
            if (!getPlotChildren().contains(line)) {
                getPlotChildren().add(line);
            }
            line.setStartX(startX);
            line.setStartY(startY);
            line.setEndX(endX);
            line.setEndY(endY);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
