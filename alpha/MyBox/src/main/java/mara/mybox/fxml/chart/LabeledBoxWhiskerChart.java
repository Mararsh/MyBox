package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class LabeledBoxWhiskerChart<X, Y> extends LabeledLineChart<X, Y> {

    protected Rectangle[] boxs;
    protected Line[] vLines, minLines, maxLines, medianLines,
            uMidOutlierLines, uExOutlierLines, lMidOutlierLines, lExOutlierLines;
    protected int boxWidth, dataSize;
    protected boolean handleOutliers, written;
    protected Map<String, String> palette;

    public LabeledBoxWhiskerChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        written = false;
        if (seriesIndex == 4) {
            writeMain();
            written = !handleOutliers;
        }
        if (handleOutliers && seriesIndex == 8) {
            writeOutliers();
            written = true;
        }
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        super.seriesRemoved(series);
        clearMain();
        clearOutliers();
        written = false;
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (!written) {
            return;
        }
        if (handleOutliers) {
            displayMain();
            displayOutliers();
        } else {
            displayMain();
        }
    }

    public void clearOutliers() {
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

    public void clearMain() {
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

    public void writeMain() {
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 5) {
                return;
            }
            clearMain();
            dataSize = seriesList.get(0).getData().size();
            boxs = new Rectangle[dataSize];
            vLines = new Line[dataSize];
            minLines = new Line[dataSize];
            maxLines = new Line[dataSize];
            medianLines = new Line[dataSize];
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            String color0 = palette.get(seriesList.get(0).getName());
            String color2 = palette.get(seriesList.get(2).getName());
            String color4 = palette.get(seriesList.get(4).getName());
            for (int i = 0; i < dataSize; i++) {
                boxs[i] = new Rectangle();
                getPlotChildren().add(boxs[i]);
                boxs[i].setWidth(boxWidth);
                boxs[i].setFill(Color.TRANSPARENT);
                boxs[i].setStyle("-fx-stroke-width:1px; -fx-stroke:black;");

                vLines[i] = new Line();
                getPlotChildren().add(vLines[i]);
                vLines[i].setStyle("-fx-stroke-dash-array: 4 4;-fx-stroke-width:1px; -fx-stroke:black;");

                maxLines[i] = new Line();
                getPlotChildren().add(maxLines[i]);
                maxLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + color4);

                medianLines[i] = new Line();
                getPlotChildren().add(medianLines[i]);
                medianLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + color2);

                minLines[i] = new Line();
                getPlotChildren().add(minLines[i]);
                minLines[i].setStyle("-fx-stroke-width:2px;-fx-stroke:" + color0);

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void writeOutliers() {
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 9) {
                return;
            }
            clearOutliers();
            dataSize = seriesList.get(5).getData().size();
            uMidOutlierLines = new Line[dataSize];
            uExOutlierLines = new Line[dataSize];
            lMidOutlierLines = new Line[dataSize];
            lExOutlierLines = new Line[dataSize];
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            String color5 = palette.get(seriesList.get(5).getName());
            String color6 = palette.get(seriesList.get(6).getName());
            String color7 = palette.get(seriesList.get(7).getName());
            String color8 = palette.get(seriesList.get(8).getName());
            String stylePrefix = "-fx-stroke-dash-array: 2 2;-fx-stroke-width:1px;-fx-stroke:";
            for (int i = 0; i < dataSize; i++) {
                uExOutlierLines[i] = new Line();
                getPlotChildren().add(uExOutlierLines[i]);
                uExOutlierLines[i].setStyle(stylePrefix + color5);

                uMidOutlierLines[i] = new Line();
                getPlotChildren().add(uMidOutlierLines[i]);
                uMidOutlierLines[i].setStyle(stylePrefix + color6);

                lMidOutlierLines[i] = new Line();
                getPlotChildren().add(lMidOutlierLines[i]);
                lMidOutlierLines[i].setStyle(stylePrefix + color7);

                lExOutlierLines[i] = new Line();
                getPlotChildren().add(lExOutlierLines[i]);
                lExOutlierLines[i].setStyle(stylePrefix + color8);

                if (vLines[i] == null) {
                    vLines[i] = new Line();
                    getPlotChildren().add(vLines[i]);
                    vLines[i].setStyle("-fx-stroke-dash-array: 4 4;-fx-stroke-width:1px; -fx-stroke:black;");
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayMain() {
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 5) {
                return;
            }
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            List<XYChart.Data<X, Y>> data0 = seriesList.get(0).getData();
            List<XYChart.Data<X, Y>> data1 = seriesList.get(1).getData();
            List<XYChart.Data<X, Y>> data2 = seriesList.get(2).getData();
            List<XYChart.Data<X, Y>> data3 = seriesList.get(3).getData();
            List<XYChart.Data<X, Y>> data4 = seriesList.get(4).getData();
            dataSize = data0.size();
            for (int i = 0; i < dataSize; i++) {
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

                if (boxs[i] != null) {
                    boxs[i].setLayoutX(leftX);
                    boxs[i].setLayoutY(y3);
                    boxs[i].setWidth(boxWidth);
                    boxs[i].setHeight(y1 - y3);
                }

                if (vLines[i] != null) {
                    vLines[i].setStartX(x);
                    vLines[i].setStartY(y4);
                    vLines[i].setEndX(x);
                    vLines[i].setEndY(y0);
                }

                if (maxLines[i] != null) {
                    maxLines[i].setStartX(leftX);
                    maxLines[i].setStartY(y4);
                    maxLines[i].setEndX(rightX);
                    maxLines[i].setEndY(y4);
                }

                if (medianLines[i] != null) {
                    medianLines[i].setStartX(leftX);
                    medianLines[i].setStartY(y2);
                    medianLines[i].setEndX(rightX);
                    medianLines[i].setEndY(y2);
                }

                if (minLines[i] != null) {
                    minLines[i].setStartX(leftX);
                    minLines[i].setStartY(y0);
                    minLines[i].setEndX(rightX);
                    minLines[i].setEndY(y0);
                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void displayOutliers() {
        try {
            List<XYChart.Series<X, Y>> seriesList = this.getData();
            if (seriesList == null || seriesList.size() < 9) {
                return;
            }
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            List<XYChart.Data<X, Y>> data0 = seriesList.get(0).getData();
            List<XYChart.Data<X, Y>> data4 = seriesList.get(4).getData();
            List<XYChart.Data<X, Y>> data5 = seriesList.get(5).getData();
            List<XYChart.Data<X, Y>> data6 = seriesList.get(6).getData();
            List<XYChart.Data<X, Y>> data7 = seriesList.get(7).getData();
            List<XYChart.Data<X, Y>> data8 = seriesList.get(8).getData();
            dataSize = data0.size();
            for (int i = 0; i < dataSize; i++) {
                Bounds regionBounds0 = data0.get(i).getNode().getBoundsInParent();
                Bounds regionBounds4 = data4.get(i).getNode().getBoundsInParent();
                Bounds regionBounds5 = data5.get(i).getNode().getBoundsInParent();
                Bounds regionBounds6 = data6.get(i).getNode().getBoundsInParent();
                Bounds regionBounds7 = data7.get(i).getNode().getBoundsInParent();
                Bounds regionBounds8 = data8.get(i).getNode().getBoundsInParent();
                double y0 = regionBounds0.getMinY() + regionBounds0.getHeight() / 2;
                double y4 = regionBounds4.getMinY() + regionBounds4.getHeight() / 2;
                double y5 = regionBounds5.getMinY() + regionBounds5.getHeight() / 2;
                double y6 = regionBounds6.getMinY() + regionBounds6.getHeight() / 2;
                double y7 = regionBounds7.getMinY() + regionBounds7.getHeight() / 2;
                double y8 = regionBounds8.getMinY() + regionBounds8.getHeight() / 2;
                double x = regionBounds5.getMinX() + regionBounds5.getWidth() / 2;
                double leftX = x - boxWidth / 2;
                double rightX = x + boxWidth / 2;

                if (uExOutlierLines[i] != null) {
                    uExOutlierLines[i].setStartX(leftX);
                    uExOutlierLines[i].setStartY(y5);
                    uExOutlierLines[i].setEndX(rightX);
                    uExOutlierLines[i].setEndY(y5);
                }
                if (uMidOutlierLines[i] != null) {
                    uMidOutlierLines[i].setStartX(leftX);
                    uMidOutlierLines[i].setStartY(y6);
                    uMidOutlierLines[i].setEndX(rightX);
                    uMidOutlierLines[i].setEndY(y6);
                }
                if (lMidOutlierLines[i] != null) {
                    lMidOutlierLines[i].setStartX(leftX);
                    lMidOutlierLines[i].setStartY(y7);
                    lMidOutlierLines[i].setEndX(rightX);
                    lMidOutlierLines[i].setEndY(y7);
                }
                if (lExOutlierLines[i] != null) {
                    lExOutlierLines[i].setStartX(leftX);
                    lExOutlierLines[i].setStartY(y8);
                    lExOutlierLines[i].setEndX(rightX);
                    lExOutlierLines[i].setEndY(y8);
                }
                if (vLines[i] != null) {
                    lExOutlierLines[i].setStartX(x);
                    lExOutlierLines[i].setStartY(Math.min(y5, y4));
                    lExOutlierLines[i].setEndX(x);
                    lExOutlierLines[i].setEndY(Math.max(y8, y0));
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void refreshBox() {
        writeMain();
        displayMain();
        if (handleOutliers) {
            writeOutliers();
            displayOutliers();
        }
    }

    /*
        get/set
     */
    public int getBoxWidth() {
        return boxWidth;
    }

    public LabeledBoxWhiskerChart<X, Y> setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
        return this;
    }

    public boolean isHandleOutliers() {
        return handleOutliers;
    }

    public LabeledBoxWhiskerChart<X, Y> setHandleOutliers(boolean handleOutliers) {
        this.handleOutliers = handleOutliers;
        return this;
    }

    public Map<String, String> getPalette() {
        return palette;
    }

    public LabeledBoxWhiskerChart<X, Y> setPalette(Map<String, String> palette) {
        this.palette = palette;
        return this;
    }

}
