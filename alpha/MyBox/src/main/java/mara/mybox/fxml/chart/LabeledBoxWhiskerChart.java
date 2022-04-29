package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import mara.mybox.dev.MyBoxLog;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class LabeledBoxWhiskerChart<X, Y> extends LabeledLineChart<X, Y> {

    protected Rectangle[] boxs;
    protected Line[] vLines, minLines, maxLines, medianLines;

    public LabeledBoxWhiskerChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public void writeBoxs(int width) {
        try {
            if (boxs != null) {
                for (Rectangle rect : boxs) {
                    getPlotChildren().remove(rect);
                }
                boxs = null;
            }
            if (vLines != null) {
                for (Line line : vLines) {
                    getPlotChildren().remove(line);
                }
                vLines = null;
            }
            if (minLines != null) {
                for (Line line : minLines) {
                    getPlotChildren().remove(line);
                }
                minLines = null;
            }
            if (maxLines != null) {
                for (Line line : maxLines) {
                    getPlotChildren().remove(line);
                }
                maxLines = null;
            }
            if (medianLines != null) {
                for (Line line : medianLines) {
                    getPlotChildren().remove(line);
                }
                medianLines = null;
            }
            applyCss();
            layout();
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 4) {
                return;
            }
            List<XYChart.Data<X, Y>> data4 = seriesList.get(4).getData();
            List<XYChart.Data<X, Y>> data0 = seriesList.get(0).getData();
            List<XYChart.Data<X, Y>> data1 = seriesList.get(1).getData();
            List<XYChart.Data<X, Y>> data2 = seriesList.get(2).getData();
            List<XYChart.Data<X, Y>> data3 = seriesList.get(3).getData();
            new Timer().schedule(new TimerTask() {
                private final int len = data0.size();

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (boxs != null) {
                            return;
                        }
                        try {
                            for (int i = 0; i < len; i++) {
                                Bounds regionBounds4 = data3.get(i).getNode().getBoundsInParent();
                                Bounds regionBounds0 = data1.get(i).getNode().getBoundsInParent();
                                double y4 = regionBounds4.getMinY();
                                double y0 = regionBounds0.getMinY();
                                if (y4 < 0 || y0 < 0 || y0 < y4) {
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            return;
                        }
                        boxs = new Rectangle[len];
                        vLines = new Line[len];
                        minLines = new Line[len];
                        maxLines = new Line[len];
                        medianLines = new Line[len];
                        int boxWidth = width;
                        if (boxWidth <= 0) {
                            boxWidth = 40;
                        }
                        for (int i = 0; i < len; i++) {
                            Bounds regionBounds4 = data4.get(i).getNode().getBoundsInParent();
                            Bounds regionBounds3 = data3.get(i).getNode().getBoundsInParent();
                            Bounds regionBounds2 = data2.get(i).getNode().getBoundsInParent();
                            Bounds regionBounds1 = data1.get(i).getNode().getBoundsInParent();
                            Bounds regionBounds0 = data0.get(i).getNode().getBoundsInParent();
                            double y4 = regionBounds4.getMinY() + regionBounds4.getHeight() / 2;
                            double y3 = regionBounds3.getMinY() + regionBounds3.getHeight() / 2;
                            double y2 = regionBounds2.getMinY() + regionBounds2.getHeight() / 2;
                            double y1 = regionBounds1.getMinY() + regionBounds1.getHeight() / 2;
                            double y0 = regionBounds0.getMinY() + regionBounds0.getHeight() / 2;
                            double x = regionBounds3.getMinX() + regionBounds3.getWidth() / 2;
                            double leftX = x - boxWidth / 2;
                            double rightX = x + boxWidth / 2;
                            boxs[i] = new Rectangle(boxWidth, y1 - y3);
                            boxs[i].setStroke(Color.BLACK);
                            boxs[i].setStrokeWidth(1);
                            boxs[i].setFill(Color.TRANSPARENT);
                            getPlotChildren().add(boxs[i]);
                            boxs[i].setLayoutX(leftX);
                            boxs[i].setLayoutY(y3);

                            vLines[i] = new Line(x, y4, x, y0);
                            getPlotChildren().add(vLines[i]);
                            vLines[i].setStyle("-fx-stroke-dash-array: 4 4;");

                            maxLines[i] = new Line(leftX, y4, rightX, y4);
                            getPlotChildren().add(maxLines[i]);

                            medianLines[i] = new Line(leftX, y2, rightX, y2);
                            medianLines[i].setStrokeWidth(2);
                            getPlotChildren().add(medianLines[i]);

                            minLines[i] = new Line(leftX, y0, rightX, y0);
                            getPlotChildren().add(minLines[i]);

                        }
                        applyCss();
                        layout();
                        cancel();
                    });
                }
            }, 500, 500);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
