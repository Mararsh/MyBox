package mara.mybox.fxml.chart;

import java.util.List;
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
        dataNumber = 1;
    }

    @Override
    public void removeControls(Series<X, Y> series) {
        super.removeControls(series);
        getPlotChildren().removeAll(upperLine, lowerLine, text);
    }

    @Override
    public void displayControls() {
        super.displayControls();
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < dataNumber) {
                return;
            }
            getPlotChildren().removeAll(upperLine, lowerLine, text);
            if (dataNumber < 3) {
                return;
            }
            String prefix = "-fx-stroke-dash-array: " + lineWidth * 2 + "; -fx-stroke-width:" + lineWidth + "px; -fx-stroke:";
            upperLine.setStyle(prefix + palette.get(seriesList.get(1).getName()));
            drawLine(seriesList.get(1).getData(), upperLine, false);

            lowerLine.setStyle(prefix + palette.get(seriesList.get(2).getName()));
            drawLine(seriesList.get(2).getData(), lowerLine, false);

            text.setStyle("-fx-font-size:" + xyOptions.getLabelFontSize() + "px; -fx-text-fill: black;");
            text.setText(info);
            getPlotChildren().add(text);
            text.setLayoutX(10);
            text.setLayoutY(10);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public ResidualChart setInfo(String info) {
        this.info = info;
        return this;
    }

}
