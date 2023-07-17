package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
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
public class SimpleRegressionChart<X, Y> extends LabeledScatterChart<X, Y> {

    protected Line regressionLine;
    protected Text text;
    protected boolean displayText, displayFittedPoints, displayFittedLine;
    protected String model;

    public SimpleRegressionChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
        regressionLine = new Line();
        text = new Text();
    }

    public synchronized void displayControls() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (getData() != null && 2 == getData().size()) {
                        t.cancel();
                        makeControls();
                    }
                });
            }
        }, 100, 100);
    }

    public void makeControls() {
        try {
            getPlotChildren().removeAll(regressionLine, text);
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null) {
                return;
            }
            getPlotChildren().removeAll(regressionLine, text);
            regressionLine.setStyle("-fx-stroke-width:" + chartMaker.getLineWidth()
                    + "px; -fx-stroke:" + chartMaker.getPalette().get(seriesList.get(1).getName()));
            drawLine(seriesList.get(1).getData(), regressionLine, displayFittedPoints);
            regressionLine.setVisible(displayFittedLine);

            text.setStyle("-fx-font-size:" + chartMaker.getLabelFontSize() + "px; -fx-text-fill: black;");
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
            MyBoxLog.debug(e);
        }
    }

    public void displayFittedPoints(boolean display) {
        try {
            displayFittedPoints = display;
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null) {
                return;
            }
            List<XYChart.Data<X, Y>> data1 = seriesList.get(1).getData();
            for (int i = 0; i < data1.size(); i++) {
                data1.get(i).getNode().setVisible(display);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void displayFittedLine(boolean display) {
        try {
            displayFittedLine = display;
            regressionLine.setVisible(display);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void displayText(boolean display) {
        try {
            displayText = display;
            text.setVisible(display);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }


    /*
        get/set
     */
    public SimpleRegressionChart<X, Y> setModel(String model) {
        this.model = model;
        return this;
    }

    public SimpleRegressionChart<X, Y> setDisplayFittedPoints(boolean displayFittedPoints) {
        this.displayFittedPoints = displayFittedPoints;
        return this;
    }

    public SimpleRegressionChart<X, Y> setDisplayFittedLine(boolean displayFittedLine) {
        this.displayFittedLine = displayFittedLine;
        return this;
    }

    public SimpleRegressionChart<X, Y> setDisplayText(boolean displayText) {
        this.displayText = displayText;
        return this;
    }

}
