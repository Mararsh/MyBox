package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Map;
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
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class BoxWhiskerChart<X, Y> extends LabeledLineChart<X, Y> {

    protected Rectangle[] boxs;
    protected Line[] vLines, minLines, maxLines, medianLines, meanLines,
            uMidOutlierLines, uExOutlierLines, lMidOutlierLines, lExOutlierLines;
    protected int boxWidth, dataSize, currentSeriesSize;
    protected boolean handleOutliers, handleMean;

    public BoxWhiskerChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
        currentSeriesSize = 0;
    }

    public int currentSeriesSize() {
        currentSeriesSize = getData() == null ? 0 : getData().size();
        return currentSeriesSize;
    }

    public int expectedSeriesSize() {
        int seriesSize = 5;
        if (handleMean) {
            seriesSize++;
        }
        if (handleOutliers) {
            seriesSize += 4;
        }
        return seriesSize;
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        currentSeriesSize();
    }

    private void clearMain() {
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
        } catch (Exception e) {
        }
    }

    private void clearMean() {
        try {
            if (meanLines != null) {
                for (Line line : meanLines) {
                    getPlotChildren().remove(line);
                }
                meanLines = null;
            }
        } catch (Exception e) {
        }
    }

    private void clearOutliers() {
        try {
            if (uMidOutlierLines != null) {
                for (Line line : uMidOutlierLines) {
                    getPlotChildren().remove(line);
                }
                uMidOutlierLines = null;
            }
            if (uExOutlierLines != null) {
                for (Line line : uExOutlierLines) {
                    getPlotChildren().remove(line);
                }
                uExOutlierLines = null;
            }
            if (lMidOutlierLines != null) {
                for (Line line : lMidOutlierLines) {
                    getPlotChildren().remove(line);
                }
                lMidOutlierLines = null;
            }
            if (lExOutlierLines != null) {
                for (Line line : lExOutlierLines) {
                    getPlotChildren().remove(line);
                }
                lExOutlierLines = null;
            }
        } catch (Exception e) {
        }
    }

    private void makeMain() {
        try {
            clearMain();
            if (!chartMaker.displayLabel()) {
                return;
            }
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            int startIndex = handleMean ? 1 : 0;
            if (seriesList == null || seriesList.size() < 5 + startIndex) {
                return;
            }
            dataSize = seriesList.get(startIndex + 0).getData().size();
            boxs = new Rectangle[dataSize];
            vLines = new Line[dataSize];
            minLines = new Line[dataSize];
            maxLines = new Line[dataSize];
            medianLines = new Line[dataSize];
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            List<XYChart.Data<X, Y>> data0 = seriesList.get(startIndex + 0).getData();
            List<XYChart.Data<X, Y>> data1 = seriesList.get(startIndex + 1).getData();
            List<XYChart.Data<X, Y>> data2 = seriesList.get(startIndex + 2).getData();
            List<XYChart.Data<X, Y>> data3 = seriesList.get(startIndex + 3).getData();
            List<XYChart.Data<X, Y>> data4 = seriesList.get(startIndex + 4).getData();

            Map<String, String> palette = chartMaker.getPalette();
            String color0 = palette.get(seriesList.get(startIndex + 0).getName());
            String color2 = palette.get(seriesList.get(startIndex + 2).getName());
            String color4 = palette.get(seriesList.get(startIndex + 4).getName());
            for (int i = 0; i < dataSize; i++) {
                Bounds regionBounds4 = data4.get(i).getNode().getBoundsInParent();
                Bounds regionBounds3 = data3.get(i).getNode().getBoundsInParent();
                Bounds regionBounds2 = data2.get(i).getNode().getBoundsInParent();
                Bounds regionBounds1 = data1.get(i).getNode().getBoundsInParent();
                Bounds regionBounds0 = data0.get(i).getNode().getBoundsInParent();

                vLines[i] = new Line();
                getPlotChildren().add(vLines[i]);
                vLines[i].setStyle("-fx-stroke-dash-array: 4;-fx-stroke-width:1px; -fx-stroke:black;");

                maxLines[i] = new Line();
                getPlotChildren().add(maxLines[i]);
                maxLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + color4);

                medianLines[i] = new Line();
                getPlotChildren().add(medianLines[i]);
                medianLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + color2);

                minLines[i] = new Line();
                getPlotChildren().add(minLines[i]);
                minLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + color0);

                boxs[i] = new Rectangle();
                boxs[i].setWidth(boxWidth);
                boxs[i].setFill(Color.TRANSPARENT);
                boxs[i].setStyle("-fx-stroke-width:1px; -fx-stroke:black;");
                getPlotChildren().add(boxs[i]);

                if (chartMaker.isXY) {
                    double y4 = regionBounds4.getMinY() + regionBounds4.getHeight() / 2;
                    double y3 = regionBounds3.getMinY() + regionBounds3.getHeight() / 2;
                    double y2 = regionBounds2.getMinY() + regionBounds2.getHeight() / 2;
                    double y1 = regionBounds1.getMinY() + regionBounds1.getHeight() / 2;
                    double y0 = regionBounds0.getMinY() + regionBounds0.getHeight() / 2;
                    double x = regionBounds3.getMinX() + regionBounds3.getWidth() / 2;
                    double leftX = x - boxWidth / 2;
                    double rightX = x + boxWidth / 2;

                    boxs[i].setLayoutX(leftX);
                    boxs[i].setLayoutY(y3);
                    boxs[i].setWidth(boxWidth);
                    boxs[i].setHeight(y1 - y3);

                    vLines[i].setStartX(x);
                    vLines[i].setStartY(y4);
                    vLines[i].setEndX(x);
                    vLines[i].setEndY(y0);

                    maxLines[i].setStartX(leftX);
                    maxLines[i].setStartY(y4);
                    maxLines[i].setEndX(rightX);
                    maxLines[i].setEndY(y4);

                    medianLines[i].setStartX(leftX);
                    medianLines[i].setStartY(y2);
                    medianLines[i].setEndX(rightX);
                    medianLines[i].setEndY(y2);

                    minLines[i].setStartX(leftX);
                    minLines[i].setStartY(y0);
                    minLines[i].setEndX(rightX);
                    minLines[i].setEndY(y0);

                } else {
                    double x4 = regionBounds4.getMinX() + regionBounds4.getWidth() / 2;
                    double x3 = regionBounds3.getMinX() + regionBounds3.getWidth() / 2;
                    double x2 = regionBounds2.getMinX() + regionBounds2.getWidth() / 2;
                    double x1 = regionBounds1.getMinX() + regionBounds1.getWidth() / 2;
                    double x0 = regionBounds0.getMinX() + regionBounds0.getWidth() / 2;
                    double y = regionBounds3.getMinY() + regionBounds3.getHeight() / 2;
                    double topY = y - boxWidth / 2;
                    double bottomY = y + boxWidth / 2;

                    boxs[i].setLayoutX(x1);
                    boxs[i].setLayoutY(topY);
                    boxs[i].setHeight(boxWidth);
                    boxs[i].setWidth(x3 - x1);

                    vLines[i].setStartX(x0);
                    vLines[i].setStartY(y);
                    vLines[i].setEndX(x4);
                    vLines[i].setEndY(y);

                    maxLines[i].setStartX(x4);
                    maxLines[i].setStartY(topY);
                    maxLines[i].setEndX(x4);
                    maxLines[i].setEndY(bottomY);

                    medianLines[i].setStartX(x2);
                    medianLines[i].setStartY(topY);
                    medianLines[i].setEndX(x2);
                    medianLines[i].setEndY(bottomY);

                    minLines[i].setStartX(x0);
                    minLines[i].setStartY(topY);
                    minLines[i].setEndX(x0);
                    minLines[i].setEndY(bottomY);
                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void makeMean() {
        try {
            clearMean();
            if (!chartMaker.displayLabel()) {
                return;
            }
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 1) {
                return;
            }
            dataSize = seriesList.get(0).getData().size();
            meanLines = new Line[dataSize];
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            Map<String, String> palette = chartMaker.getPalette();
            String colorMean = palette.get(seriesList.get(0).getName());
            List<XYChart.Data<X, Y>> data0 = seriesList.get(0).getData();
            for (int i = 0; i < dataSize; i++) {
                Bounds regionBoundsMean = data0.get(i).getNode().getBoundsInParent();
                meanLines[i] = new Line();
                getPlotChildren().add(meanLines[i]);
                meanLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + colorMean);

                if (chartMaker.isXY) {
                    double yMean = regionBoundsMean.getMinY() + regionBoundsMean.getHeight() / 2;
                    double x = regionBoundsMean.getMinX() + regionBoundsMean.getWidth() / 2;
                    double leftX = x - boxWidth / 2;
                    double rightX = x + boxWidth / 2;
                    meanLines[i].setStartX(leftX);
                    meanLines[i].setStartY(yMean);
                    meanLines[i].setEndX(rightX);
                    meanLines[i].setEndY(yMean);

                } else {
                    double xMean = regionBoundsMean.getMinX() + regionBoundsMean.getWidth() / 2;
                    double y = regionBoundsMean.getMinY() + regionBoundsMean.getHeight() / 2;
                    double topY = y - boxWidth / 2;
                    double bottomY = y + boxWidth / 2;
                    meanLines[i].setStartX(xMean);
                    meanLines[i].setStartY(topY);
                    meanLines[i].setEndX(xMean);
                    meanLines[i].setEndY(bottomY);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void makeOutliers() {
        try {
            clearOutliers();
            if (!chartMaker.displayLabel()) {
                return;
            }
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            int startIndex = handleMean ? 1 : 0;
            if (seriesList == null || seriesList.size() < 9 + startIndex) {
                return;
            }
            dataSize = seriesList.get(startIndex + 5).getData().size();
            uMidOutlierLines = new Line[dataSize];
            uExOutlierLines = new Line[dataSize];
            lMidOutlierLines = new Line[dataSize];
            lExOutlierLines = new Line[dataSize];
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            List<XYChart.Data<X, Y>> data0 = seriesList.get(startIndex + 0).getData();
            List<XYChart.Data<X, Y>> data4 = seriesList.get(startIndex + 4).getData();
            List<XYChart.Data<X, Y>> data5 = seriesList.get(startIndex + 5).getData();
            List<XYChart.Data<X, Y>> data6 = seriesList.get(startIndex + 6).getData();
            List<XYChart.Data<X, Y>> data7 = seriesList.get(startIndex + 7).getData();
            List<XYChart.Data<X, Y>> data8 = seriesList.get(startIndex + 8).getData();
            Map<String, String> palette = chartMaker.getPalette();
            String color5 = palette.get(seriesList.get(startIndex + 5).getName());
            String color6 = palette.get(seriesList.get(startIndex + 6).getName());
            String color7 = palette.get(seriesList.get(startIndex + 7).getName());
            String color8 = palette.get(seriesList.get(startIndex + 8).getName());
            String stylePrefix = "-fx-stroke-dash-array: 2;-fx-stroke-width:1px;-fx-stroke:";
            for (int i = 0; i < dataSize; i++) {
                Bounds regionBounds0 = data0.get(i).getNode().getBoundsInParent();
                Bounds regionBounds4 = data4.get(i).getNode().getBoundsInParent();
                Bounds regionBounds5 = data5.get(i).getNode().getBoundsInParent();
                Bounds regionBounds6 = data6.get(i).getNode().getBoundsInParent();
                Bounds regionBounds7 = data7.get(i).getNode().getBoundsInParent();
                Bounds regionBounds8 = data8.get(i).getNode().getBoundsInParent();

                uExOutlierLines[i] = new Line();
                getPlotChildren().add(uExOutlierLines[i]);
                uExOutlierLines[i].setStyle(stylePrefix + color8);

                uMidOutlierLines[i] = new Line();
                getPlotChildren().add(uMidOutlierLines[i]);
                uMidOutlierLines[i].setStyle(stylePrefix + color7);

                lMidOutlierLines[i] = new Line();
                getPlotChildren().add(lMidOutlierLines[i]);
                lMidOutlierLines[i].setStyle(stylePrefix + color6);

                lExOutlierLines[i] = new Line();
                getPlotChildren().add(lExOutlierLines[i]);
                lExOutlierLines[i].setStyle(stylePrefix + color5);

                vLines[i] = new Line();
                getPlotChildren().add(vLines[i]);
                vLines[i].setStyle("-fx-stroke-dash-array: 4;-fx-stroke-width:1px; -fx-stroke:black;");

                if (chartMaker.isXY) {
                    double y0 = regionBounds0.getMinY() + regionBounds0.getHeight() / 2;
                    double y4 = regionBounds4.getMinY() + regionBounds4.getHeight() / 2;
                    double y5 = regionBounds5.getMinY() + regionBounds5.getHeight() / 2;
                    double y6 = regionBounds6.getMinY() + regionBounds6.getHeight() / 2;
                    double y7 = regionBounds7.getMinY() + regionBounds7.getHeight() / 2;
                    double y8 = regionBounds8.getMinY() + regionBounds8.getHeight() / 2;
                    double x = regionBounds5.getMinX() + regionBounds5.getWidth() / 2;
                    double leftX = x - boxWidth / 2;
                    double rightX = x + boxWidth / 2;

                    uExOutlierLines[i].setStartX(leftX);
                    uExOutlierLines[i].setStartY(y8);
                    uExOutlierLines[i].setEndX(rightX);
                    uExOutlierLines[i].setEndY(y8);

                    uMidOutlierLines[i].setStartX(leftX);
                    uMidOutlierLines[i].setStartY(y7);
                    uMidOutlierLines[i].setEndX(rightX);
                    uMidOutlierLines[i].setEndY(y7);

                    lMidOutlierLines[i].setStartX(leftX);
                    lMidOutlierLines[i].setStartY(y6);
                    lMidOutlierLines[i].setEndX(rightX);
                    lMidOutlierLines[i].setEndY(y6);

                    lExOutlierLines[i].setStartX(leftX);
                    lExOutlierLines[i].setStartY(y5);
                    lExOutlierLines[i].setEndX(rightX);
                    lExOutlierLines[i].setEndY(y5);

                    vLines[i].setStartX(x);
                    vLines[i].setStartY(Math.min(y8, y4));
                    vLines[i].setEndX(x);
                    vLines[i].setEndY(Math.max(y5, y0));

                } else {
                    double x0 = regionBounds0.getMinX() + regionBounds0.getWidth() / 2;
                    double x4 = regionBounds4.getMinX() + regionBounds4.getWidth() / 2;
                    double x5 = regionBounds5.getMinX() + regionBounds5.getWidth() / 2;
                    double x6 = regionBounds6.getMinX() + regionBounds6.getWidth() / 2;
                    double x7 = regionBounds7.getMinX() + regionBounds7.getWidth() / 2;
                    double x8 = regionBounds8.getMinX() + regionBounds8.getWidth() / 2;
                    double y = regionBounds5.getMinY() + regionBounds5.getHeight() / 2;
                    double topY = y - boxWidth / 2;
                    double bottomY = y + boxWidth / 2;

                    uExOutlierLines[i].setStartX(x8);
                    uExOutlierLines[i].setStartY(topY);
                    uExOutlierLines[i].setEndX(x8);
                    uExOutlierLines[i].setEndY(bottomY);

                    uMidOutlierLines[i].setStartX(x7);
                    uMidOutlierLines[i].setStartY(topY);
                    uMidOutlierLines[i].setEndX(x7);
                    uMidOutlierLines[i].setEndY(bottomY);

                    lMidOutlierLines[i].setStartX(x6);
                    lMidOutlierLines[i].setStartY(topY);
                    lMidOutlierLines[i].setEndX(x6);
                    lMidOutlierLines[i].setEndY(bottomY);

                    lExOutlierLines[i].setStartX(x5);
                    lExOutlierLines[i].setStartY(topY);
                    lExOutlierLines[i].setEndX(x5);
                    lExOutlierLines[i].setEndY(bottomY);

                    vLines[i].setStartX(Math.min(x8, x4));
                    vLines[i].setStartY(y);
                    vLines[i].setEndX(Math.max(x5, x0));
                    vLines[i].setEndY(y);

                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private synchronized void makeBoxWhisker() {
        makeMain();
        if (handleMean) {
            makeMean();
        }
        if (handleOutliers) {
            makeOutliers();
        }
    }

    public synchronized void displayBoxWhisker() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (getData() != null && expectedSeriesSize() == currentSeriesSize()) {
                        t.cancel();
                        makeBoxWhisker();
                    }
                });
            }
        }, 100, 100);
    }

    /*
        get/set
     */
    public int getBoxWidth() {
        boxWidth = boxWidth < 0 ? 40 : boxWidth;
        return boxWidth;
    }

    public BoxWhiskerChart<X, Y> setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
        UserConfig.setInt("BoxWhiskerChartBoxWidth", getBoxWidth());
        return this;
    }

    public boolean isHandleOutliers() {
        return handleOutliers;
    }

    public BoxWhiskerChart<X, Y> setHandleOutliers(boolean handleOutliers) {
        this.handleOutliers = handleOutliers;
        UserConfig.setBoolean("BoxWhiskerChartHandleOutliers", handleOutliers);
        return this;
    }

    public boolean isHandleMean() {
        return handleMean;
    }

    public BoxWhiskerChart<X, Y> setHandleMean(boolean handleMean) {
        this.handleMean = handleMean;
        UserConfig.setBoolean("BoxWhiskerChartHandleMean", handleMean);
        return this;
    }

}
