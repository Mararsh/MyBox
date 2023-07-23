package mara.mybox.fxml.chart;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
public class LabeledStackedAreaChart<X, Y> extends StackedAreaChart<X, Y> {

    protected XYChartMaker chartMaker;

    public LabeledStackedAreaChart(Axis xAxis, Axis yAxis) {
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

    public LabeledStackedAreaChart setMaker(XYChartMaker<X, Y> chartMaker) {
        this.chartMaker = chartMaker;
        setCreateSymbols(chartMaker.displayLabel());
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
        try {
            super.layoutPlotChildren();
            chartMaker.displayLabels();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
