package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-5-2
 * @License Apache License Version 2.0
 */
public class LabeledSimpleRegressionChart<X, Y> extends LabeledScatterChart<X, Y> {

    protected Line regressionLine, upperConfidenceLine, lowerConfidenceLine;
    protected Text text;
    protected boolean written, displayText,
            displayFittedPoints, displayConfidenceLowerPoints, displayConfidenceUpperPoints,
            displayFittedLine, displayConfidenceLowerLine, displayConfidenceUpperLine;
    protected String model;

    public LabeledSimpleRegressionChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
        regressionLine = new Line();
        upperConfidenceLine = new Line();
        lowerConfidenceLine = new Line();
        text = new Text();
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        written = seriesIndex == 3;
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        super.seriesRemoved(series);
        getPlotChildren().removeAll(regressionLine, upperConfidenceLine, lowerConfidenceLine, text);
        written = false;
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (written) {
            displayResults();
        }
    }

    public void displayFittedPoints(boolean display) {
        try {
            displayFittedPoints = display;
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 4) {
                return;
            }
            List<XYChart.Data<X, Y>> data1 = seriesList.get(1).getData();
            for (int i = 0; i < data1.size(); i++) {
                data1.get(i).getNode().setVisible(display);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayConfidenceLowerPoints(boolean display) {
        try {
            displayConfidenceLowerPoints = display;
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 4) {
                return;
            }
            List<XYChart.Data<X, Y>> data2 = seriesList.get(2).getData();
            for (int i = 0; i < data2.size(); i++) {
                data2.get(i).getNode().setVisible(display);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayConfidenceUpperPoints(boolean display) {
        try {
            displayConfidenceUpperPoints = display;
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 4) {
                return;
            }
            List<XYChart.Data<X, Y>> data3 = seriesList.get(3).getData();
            for (int i = 0; i < data3.size(); i++) {
                data3.get(i).getNode().setVisible(display);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayFittedLine(boolean display) {
        try {
            displayFittedLine = display;
            regressionLine.setVisible(display);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayConfidenceLowerLine(boolean display) {
        try {
            displayConfidenceLowerLine = display;
            lowerConfidenceLine.setVisible(display);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayConfidenceUpperLine(boolean display) {
        try {
            displayConfidenceUpperLine = display;
            upperConfidenceLine.setVisible(display);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayText(boolean display) {
        try {
            displayText = display;
            text.setVisible(display);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayResults() {
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 4) {
                return;
            }

            getPlotChildren().removeAll(regressionLine, upperConfidenceLine, lowerConfidenceLine, text);

            Map<String, String> palette = chartController.getPalette();

            regressionLine.setStyle("-fx-stroke-width:2px; -fx-stroke:" + palette.get(seriesList.get(1).getName()));
            drawLine(seriesList.get(1).getData(), regressionLine, displayFittedPoints);
            regressionLine.setVisible(displayFittedLine);

            lowerConfidenceLine.setStyle("-fx-stroke-dash-array: 4 4;-fx-stroke-width:1px; -fx-stroke:"
                    + palette.get(seriesList.get(2).getName()));
            drawLine(seriesList.get(2).getData(), lowerConfidenceLine, displayConfidenceLowerPoints);
            lowerConfidenceLine.setVisible(displayConfidenceLowerLine);

            upperConfidenceLine.setStyle("-fx-stroke-dash-array: 4 4; -fx-stroke-width:1px; -fx-stroke:"
                    + palette.get(seriesList.get(3).getName()));
            drawLine(seriesList.get(3).getData(), upperConfidenceLine, displayConfidenceUpperPoints);
            upperConfidenceLine.setVisible(displayConfidenceUpperLine);

            text.setStyle("-fx-font-size:" + chartController.getLabelFontSize() + "px; -fx-text-fill: black;");
            text.setText(model);
            getPlotChildren().add(text);
            if (regressionLine.getStartY() > 60) {
                text.setLayoutX(10);
                text.setLayoutY(10);
            } else {
                text.setLayoutX(10);
                text.setLayoutY(text.getParent().getBoundsInParent().getHeight() - text.getBoundsInParent().getHeight() - 10);
            }
            text.setVisible(displayText);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void drawLine(List<XYChart.Data<X, Y>> data, Line line, boolean pointsVisible) {
        try {
            if (data == null || line == null) {
                return;
            }
            double startX = Double.MAX_VALUE, startY = Double.MAX_VALUE,
                    endX = -Double.MAX_VALUE, endY = -Double.MAX_VALUE;
            for (int i = 0; i < data.size(); i++) {
                Bounds regionBounds = data.get(i).getNode().getBoundsInParent();
                double x = regionBounds.getMinX() + regionBounds.getWidth() / 2;
                double y = regionBounds.getMinY() + regionBounds.getHeight() / 2;
                if (chartController.isXY()) {
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
                data.get(i).getNode().setVisible(pointsVisible);
            }
            if (startX == Double.MAX_VALUE || endX == -Double.MAX_VALUE) {
                return;
            }
            getPlotChildren().add(line);
            line.setStartX(startX);
            line.setStartY(startY);
            line.setEndX(endX);
            line.setEndY(endY);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        get/set
     */
    public LabeledSimpleRegressionChart<X, Y> setModel(String model) {
        this.model = model;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayFittedPoints(boolean displayFittedPoints) {
        this.displayFittedPoints = displayFittedPoints;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayConfidenceLowerPoints(boolean displayConfidenceLowerPoints) {
        this.displayConfidenceLowerPoints = displayConfidenceLowerPoints;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayConfidenceUpperPoints(boolean displayConfidenceUpperPoints) {
        this.displayConfidenceUpperPoints = displayConfidenceUpperPoints;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayFittedLine(boolean displayFittedLine) {
        this.displayFittedLine = displayFittedLine;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayConfidenceLowerLine(boolean displayConfidenceLowerLine) {
        this.displayConfidenceLowerLine = displayConfidenceLowerLine;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayConfidenceUpperLine(boolean displayConfidenceUpperLine) {
        this.displayConfidenceUpperLine = displayConfidenceUpperLine;
        return this;
    }

    public LabeledSimpleRegressionChart<X, Y> setDisplayText(boolean displayText) {
        this.displayText = displayText;
        return this;
    }

}
