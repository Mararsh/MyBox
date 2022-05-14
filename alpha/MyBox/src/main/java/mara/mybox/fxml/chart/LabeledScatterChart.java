package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Map;
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

    protected XYChartOptions xyOptions;

    protected boolean written;
    protected int dataNumber;
    protected int lineWidth = 2;
    protected Map<String, String> palette;

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
    }

    public LabeledScatterChart setOptions(XYChartOptions xyOptions) {
        this.xyOptions = xyOptions;
        return this;
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        writeControls(series, seriesIndex);
        written = seriesIndex == dataNumber - 1;
    }

    public void writeControls(Series<X, Y> series, int seriesIndex) {
        xyOptions.makeLabels(series, getPlotChildren());
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        super.seriesRemoved(series);
        removeControls(series);
        written = false;
    }

    public void removeControls(Series<X, Y> series) {
        xyOptions.removeLabels(getPlotChildren());
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (dataNumber <= 0 || written) {
            displayControls();
        }
    }

    public void displayControls() {
        xyOptions.displayLabels();
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
                if (xyOptions.isXY) {
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
            MyBoxLog.debug(e.toString());
        }
    }


    /*
        set/set
     */
    public int getDataNumber() {
        return dataNumber;
    }

    public LabeledScatterChart setDataNumber(int dataNumber) {
        this.dataNumber = dataNumber;
        return this;
    }

    public LabeledScatterChart setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public LabeledScatterChart setPalette(Map<String, String> palette) {
        this.palette = palette;
        return this;
    }

}
