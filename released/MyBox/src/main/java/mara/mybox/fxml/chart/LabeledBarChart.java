package mara.mybox.fxml.chart;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.fxml.chart.ChartOptions.LabelType;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @License Apache License Version 2.0
 */
public class LabeledBarChart<X, Y> extends BarChart<X, Y> {

    protected XYChartMaker chartMaker;

    public LabeledBarChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    public LabeledBarChart   setMaker(XYChartMaker<X,Y> chartMaker) {
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

    public LabeledBarChart<X, Y> setLabelType(LabelType labelType) {
        chartMaker.setLabelType(labelType);
        return this;
    }

    public LabeledBarChart<X, Y> setLabelFontSize(int labelFontSize) {
        chartMaker.setLabelFontSize(labelFontSize);
        return this;
    }

}
