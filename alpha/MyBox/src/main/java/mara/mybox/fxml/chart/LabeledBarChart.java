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

    protected XYChartOptions xyOptions;

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

    public LabeledBarChart setOptions(XYChartOptions xyOptions) {
        this.xyOptions = xyOptions;
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        xyOptions.makeLabels(series, getPlotChildren());
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        xyOptions.removeLabels(series, getPlotChildren());
        super.seriesRemoved(series);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        xyOptions.displayLabels();
    }

    public LabeledBarChart<X, Y> setLabelType(LabelType labelType) {
        xyOptions.setLabelType(labelType);
        return this;
    }

    public LabeledBarChart<X, Y> setLabelFontSize(int labelFontSize) {
        xyOptions.setLabelFontSize(labelFontSize);
        return this;
    }

}
