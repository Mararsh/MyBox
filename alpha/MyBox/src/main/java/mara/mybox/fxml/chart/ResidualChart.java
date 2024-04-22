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
 * @CreateDate 2022-5-10
 * @License Apache License Version 2.0
 */
public class ResidualChart<X, Y> extends LabeledScatterChart<X, Y> {

    protected Line upperLine, lowerLine;
    protected Text text;
    protected String info;

    public ResidualChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
        upperLine = new Line();
        lowerLine = new Line();
        text = new Text();
    }

    public synchronized void displayControls(int dataSize) {
        if (dataSize < 3) {
            getPlotChildren().removeAll(upperLine, lowerLine, text);
            return;
        }
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (getData() != null && dataSize == getData().size()) {
                        t.cancel();
                        makeControls();
                    }
                });
                Platform.requestNextPulse();
            }
        }, 100, 100);
    }

    public void makeControls() {
        try {
            getPlotChildren().removeAll(upperLine, lowerLine, text);
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 3) {
                return;
            }
            String prefix = "-fx-stroke-dash-array: " + chartMaker.getLineWidth() * 2
                    + "; -fx-stroke-width:" + chartMaker.getLineWidth() + "px; -fx-stroke:";
            upperLine.setStyle(prefix + chartMaker.getPalette().get(seriesList.get(1).getName()));
            drawLine(seriesList.get(1).getData(), upperLine, false);

            lowerLine.setStyle(prefix + chartMaker.getPalette().get(seriesList.get(2).getName()));
            drawLine(seriesList.get(2).getData(), lowerLine, false);

            text.setStyle("-fx-font-size:" + chartMaker.getLabelFontSize() + "px; -fx-text-fill: black;");
            text.setText(info);
            getPlotChildren().add(text);
            text.setLayoutX(10);
            text.setLayoutY(10);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public ResidualChart setInfo(String info) {
        this.info = info;
        return this;
    }

}
