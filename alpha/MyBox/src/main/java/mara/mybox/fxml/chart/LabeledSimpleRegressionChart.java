package mara.mybox.fxml.chart;

import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Line;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-5-2
 * @License Apache License Version 2.0
 */
public class LabeledSimpleRegressionChart<X, Y> extends LabeledScatterChart<X, Y> {

    protected Line regressionLine;
    protected int lineWidth;
    protected boolean displayFitted, written;

    public LabeledSimpleRegressionChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        written = seriesIndex == 1;
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        super.seriesRemoved(series);
        getPlotChildren().remove(regressionLine);
        written = false;
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (written) {
            displayLine();
        }
    }

    public void displayFitted(boolean display) {
        try {
            displayFitted = display;
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 2) {
                return;
            }
            List<XYChart.Data<X, Y>> data1 = seriesList.get(1).getData();
            for (int i = 0; i < data1.size(); i++) {
                data1.get(i).getNode().setVisible(displayFitted);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayLine() {
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 2) {
                return;
            }
            getPlotChildren().remove(regressionLine);
            List<XYChart.Data<X, Y>> data1 = seriesList.get(1).getData();
            double startX = Double.MAX_VALUE, startY = Double.MAX_VALUE, endX = -Double.MAX_VALUE, endY = -Double.MAX_VALUE;
            for (int i = 0; i < data1.size(); i++) {
                Bounds regionBounds = data1.get(i).getNode().getBoundsInParent();
                double x = regionBounds.getMinX() + regionBounds.getWidth() / 2;
                double y = regionBounds.getMinY() + regionBounds.getHeight() / 2;
                if (options.isXY) {
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
                data1.get(i).getNode().setVisible(displayFitted);
            }
            if (startX == Double.MAX_VALUE || endX == -Double.MAX_VALUE) {
                return;
            }
            regressionLine = new Line();
            regressionLine.setStyle("-fx-stroke-width:" + lineWidth + "px; -fx-stroke:black;");
            getPlotChildren().add(regressionLine);
            regressionLine.setStartX(startX);
            regressionLine.setStartY(startY);
            regressionLine.setEndX(endX);
            regressionLine.setEndY(endY);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }


    /*
        get/set
     */
    public int getLineWidth() {
        return lineWidth;
    }

    public LabeledSimpleRegressionChart<X, Y> setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public boolean isDisplayFitted() {
        return displayFitted;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayFitted(boolean displayFitted) {
        this.displayFitted = displayFitted;
        return this;
    }

}
