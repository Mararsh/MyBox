package mara.mybox.fxml.chart;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.controller.Data2DChartController;

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

    protected Data2DChartController chartController;
    protected ChartOptions<X, Y> options;

    public LabeledLineChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public final void init() {
        this.setLegendSide(Side.TOP);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        options = new ChartOptions<>(this);
    }

    public LabeledLineChart setChartController(Data2DChartController chartController) {
        this.chartController = chartController;
        options = new ChartOptions<>(chartController);
        setCreateSymbols(chartController.displayLabel());
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        options.makeLabels(series, getPlotChildren());
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        options.removeLabels(series, getPlotChildren());
        super.seriesRemoved(series);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        options.displayLabels();
    }

}
