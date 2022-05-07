package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledAreaChart;
import mara.mybox.fxml.chart.LabeledBarChart;
import mara.mybox.fxml.chart.LabeledBubbleChart;
import mara.mybox.fxml.chart.LabeledLineChart;
import mara.mybox.fxml.chart.LabeledScatterChart;
import mara.mybox.fxml.chart.LabeledStackedAreaChart;
import mara.mybox.fxml.chart.LabeledStackedBarChart;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartXYController extends BaseData2DChartXYController {

    protected int lineWidth;
    protected double barGap, categoryGap;
    protected LabeledLineChart lineChart;
    protected LabeledBarChart barChart;
    protected LabeledStackedBarChart stackedBarChart;
    protected LabeledScatterChart​ scatterChart​;
    protected LabeledAreaChart areaChart;
    protected LabeledStackedAreaChart stackedAreaChart;
    protected LabeledBubbleChart bubbleChart;

    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            bubbleChartRadio, areaChartRadio, stackedAreaChartRadio, labelLocaionAboveRadio;
    @FXML
    protected ComboBox<String> lineWdithSelector, barGapSelector, categoryGapSelector;
    @FXML
    protected VBox columnsBox, xyPlotBox, categoryNumbersBox, bubbleBox;
    @FXML
    protected HBox lineWidthBox, barGapBox, categoryGapBox;
    @FXML
    protected TextField bubbleStyleInput;
    @FXML
    protected RadioButton categoryStringRadio, categoryNumberRadio;
    @FXML
    protected ToggleGroup chartGroup, sizeCoordinateGroup, categoryValuesGroup;

    public Data2DChartXYController() {
        baseTitle = message("XYChart");
        TipsLabelKey = "DataChartXYTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initChartBox();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initChartBox() {
        try {
            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        checkChartType();
                        okAction();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            lineWidth = UserConfig.getInt(baseName + "LineWidth", 4);
            if (lineWidth < 0) {
                lineWidth = 1;
            }

            lineWdithSelector.getItems().addAll(Arrays.asList(
                    "4", "1", "2", "3", "5", "6", "7", "8", "9", "10"
            ));
            lineWdithSelector.getSelectionModel().select(lineWidth + "");
            lineWdithSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v >= 0) {
                                lineWidth = v;
                                lineWdithSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "LineWidth", lineWidth);
                                if (lineChart != null) {
                                    ChartTools.setLineChartColors(lineChart, lineWidth, palette, legendSide != null);
                                }
                            } else {
                                lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            bubbleStyleInput.setText(UserConfig.getString(baseName + "BubbleStyle", ChartTools.DefaultBubbleStyle));

            sCoordinate = ChartTools.ChartCoordinate.Cartesian;

            sizeCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        if (sizeLogarithmicERadio.isSelected()) {
                            sCoordinate = ChartTools.ChartCoordinate.LogarithmicE;
                        } else if (sizeLogarithmic10Radio.isSelected()) {
                            sCoordinate = ChartTools.ChartCoordinate.Logarithmic10;
                        } else if (sizeSquareRootRadio.isSelected()) {
                            sCoordinate = ChartTools.ChartCoordinate.SquareRoot;
                        } else {
                            sCoordinate = ChartTools.ChartCoordinate.Cartesian;
                        }
                        if (bubbleChartRadio.isSelected()) {
                            okAction();
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initCategoryTab() {
        try {
            super.initCategoryTab();

            barGap = UserConfig.getDouble(baseName + "ChartBarGap", 1.0d);
            if (barGap < 0) {
                barGap = 1.0d;
            }

            barGapSelector.getItems().addAll(Arrays.asList(
                    "1", "0", "0.5", "2", "4", "1.5", "5", "8", "10", "20", "30", "40", "50"
            ));
            barGapSelector.getSelectionModel().select(barGap + "");
            barGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.valueOf(nv);
                            if (d >= 0) {
                                barGap = d;
                                barGapSelector.getEditor().setStyle(null);
                                UserConfig.setDouble(baseName + "ChartBarGap", barGap);
                                if (barChart != null) {
                                    barChart.setBarGap(barGap);
                                    barChart.requestLayout();
                                }
                            } else {
                                barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            categoryGap = UserConfig.getDouble(baseName + "ChartCategoryGap", 20d);
            if (categoryGap < 0) {
                categoryGap = 1.0d;
            }

            categoryGapSelector.getItems().addAll(Arrays.asList(
                    "20", "10", "30", "5", "8", "1", "0", "0.5", "2", "4", "1.5", "40", "50"
            ));
            categoryGapSelector.getSelectionModel().select(categoryGap + "");
            categoryGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.valueOf(nv);
                            if (d >= 0) {
                                categoryGap = d;
                                categoryGapSelector.getEditor().setStyle(null);
                                UserConfig.setDouble(baseName + "ChartCategoryGap", categoryGap);
                                if (barChart != null) {
                                    barChart.setCategoryGap(categoryGap);
                                    barChart.requestLayout();
                                } else if (stackedBarChart != null) {
                                    stackedBarChart.setCategoryGap(categoryGap);
                                    stackedBarChart.requestLayout();
                                }
                            } else {
                                categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            if (UserConfig.getBoolean(baseName + "CountCategoryAsNumbers", false)) {
                categoryNumberRadio.fire();
            }
            categoryCoordinatePane.setVisible(categoryNumberRadio.isSelected());
            categoryValuesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "CountCategoryAsNumbers", categoryNumberRadio.isSelected());
                    categoryCoordinatePane.setVisible(categoryNumberRadio.isSelected());
                    okAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkChartType() {
        try {
            columnsBox.getChildren().clear();

            if (bubbleChartRadio.isSelected()) {
                columnsBox.getChildren().addAll(categoryColumnsPane, valueColumnPane);
                setSourceLabel(message("BubbleChartLabel"));

            } else {
                columnsBox.getChildren().addAll(categoryColumnsPane);
                setSourceLabel(message("XYChartLabel"));
            }

            if (barChartRadio.isSelected()) {
                labelLocaionAboveRadio.fire();
            }

            barGapBox.setDisable(!barChartRadio.isSelected() && !stackedBarChartRadio.isSelected());
            categoryGapBox.setDisable(!barChartRadio.isSelected());

            bubbleBox.setVisible(bubbleChartRadio.isSelected());

            categoryNumbersBox.setVisible(!barChartRadio.isSelected() && !stackedBarChartRadio.isSelected());

            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String title() {
        String prefix = categoryName() + " - ";
        if (bubbleChartRadio != null && bubbleChartRadio.isSelected()) {
            return prefix + valueName() + " - " + valuesNames();
        } else {
            return prefix + valuesNames();
        }
    }

    @Override
    public boolean isCategoryNumbers() {
        return categoryNumberRadio.isSelected()
                && !barChartRadio.isSelected() && !stackedBarChartRadio.isSelected();
    }

    @Override
    public boolean initData() {
        dataColsIndices = new ArrayList<>();
        outputColumns = new ArrayList<>();
        outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
        int categoryCol = data2D.colOrder(selectedCategory);
        if (categoryCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(categoryCol);
        outputColumns.add(data2D.column(categoryCol));
        if (bubbleChartRadio.isSelected()) {
            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                popError(message("SelectToHandle"));
                return false;
            }
            dataColsIndices.add(valueCol);
            outputColumns.add(data2D.column(valueCol));
        }
        checkedColsIndices = sourceController.checkedColsIndices();
        if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.addAll(checkedColsIndices);
        outputColumns.addAll(sourceController.checkedCols());
        return true;
    }

    @Override
    public void clearChart() {
        super.clearChart();
        lineChart = null;
        barChart = null;
        stackedBarChart = null;
        areaChart = null;
        stackedAreaChart = null;
        bubbleChart = null;
        scatterChart​ = null;
    }

    @Override
    public void makeChart() {
        try {
            makeAxis();

            if (barChartRadio.isSelected()) {
                makeBarChart();
            } else if (stackedBarChartRadio.isSelected()) {
                makeStackedBarChart();
            } else if (lineChartRadio.isSelected()) {
                makeLineChart();
            } else if (scatterChartRadio.isSelected()) {
                makeScatterChart​();
            } else if (areaChartRadio.isSelected()) {
                makeAreaChart();
            } else if (stackedAreaChartRadio.isSelected()) {
                makeStackedAreaChart();
            } else if (bubbleChartRadio.isSelected()) {
                makeBubbleChart();
            }

            makeXYChart();
            makeFinalChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean makeLineChart() {
        try {
            lineChart = new LabeledLineChart(xAxis, yAxis);
            xyChart = lineChart;
            lineChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBarChart() {
        try {
            barChart = new LabeledBarChart(xAxis, yAxis);
            barChart.setBarGap(barGap);
            barChart.setCategoryGap(categoryGap);
            xyChart = barChart;
            barChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeStackedBarChart() {
        try {
            stackedBarChart = new LabeledStackedBarChart(xAxis, yAxis);
            stackedBarChart.setCategoryGap(categoryGap);
            xyChart = stackedBarChart;
            stackedBarChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeScatterChart​() {
        try {
            scatterChart = new LabeledScatterChart​(xAxis, yAxis);
            xyChart = scatterChart;
            scatterChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeAreaChart() {
        try {
            areaChart = new LabeledAreaChart(xAxis, yAxis);
            xyChart = areaChart;
            areaChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeStackedAreaChart() {
        try {
            stackedAreaChart = new LabeledStackedAreaChart(xAxis, yAxis);
            xyChart = stackedAreaChart;
            stackedAreaChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeBubbleChart() {
        try {
            numberAxisX = new NumberAxis();
            numberAxisX.setLabel(categoryLabel.getText());
            numberAxisX.setSide(categorySide);
            numberAxisX.setTickLabelsVisible(categoryTickCheck.isSelected());
            numberAxisX.setTickLabelRotation(categoryTickRotation);
            numberAxisX.setAnimated(categoryAxisAnimatedCheck.isSelected());
            ChartTools.setChartCoordinate(numberAxisX, xCoordinate);
            if (xyReverseCheck.isSelected()) {
                bubbleChart = new LabeledBubbleChart(numberAxisY, numberAxisX);
            } else {
                bubbleChart = new LabeledBubbleChart(numberAxisX, numberAxisY);
            }
            xyChart = bubbleChart;
            bubbleChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public void writeChartData() {
        if (bubbleChartRadio.isSelected()) {
            writeBubbleChart();
        } else {
            writeXYChart(outputColumns, outputData, true);
        }

    }

    public void writeBubbleChart() {
        try {
            XYChart.Data xyData;
            palette = new HashMap();
            Random random = new Random();
            List<XYChart.Series> seriesList = new ArrayList<>();
            int sizeNum = checkedColsIndices.size();
            for (int i = 0; i < sizeNum; i++) {
                int colIndex = checkedColsIndices.get(i);
                Data2DColumn column = data2D.column(colIndex);
                String colName = column.getColumnName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);
                seriesList.add(series);

                Color color = column.getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(colName, rgb);
            }
            double categoryValue, categoryCoordinateValue, numberValue, numberCoordinateValue, sizeValue, sizeCoordinateValue;
            for (List<String> rowData : outputData) {
                categoryValue = data2D.doubleValue(rowData.get(0));
                categoryCoordinateValue = ChartTools.coordinateValue(cCoordinate, categoryValue);
                numberValue = data2D.doubleValue(rowData.get(1));
                numberCoordinateValue = ChartTools.coordinateValue(nCoordinate, numberValue);
                for (int i = 0; i < sizeNum; i++) {
                    sizeValue = data2D.doubleValue(rowData.get(i + 2));
                    if (sizeValue <= 0) {
                        continue;
                    }
                    sizeCoordinateValue = ChartTools.coordinateValue(sCoordinate, sizeValue);
                    xyData = xyReverseCheck.isSelected()
                            ? new XYChart.Data(numberCoordinateValue, categoryCoordinateValue)
                            : new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                    xyData.setExtraValue(sizeCoordinateValue);
                    seriesList.get(i).getData().add(xyData);
                }
            }
            bubbleChart.getData().addAll(seriesList);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setChartStyle() {
        if (chart == null) {
            return;
        }
        makePalette();
        if (barChart != null) {
            ChartTools.setBarChartColors(barChart, palette, legendSide != null);
        } else if (stackedBarChart != null) {
            ChartTools.setBarChartColors(stackedBarChart, palette, legendSide != null);
        } else if (lineChart != null) {
            ChartTools.setLineChartColors(lineChart, lineWidth, palette, legendSide != null);
        } else if (areaChart != null) {
            ChartTools.setAreaChartColors(areaChart, lineWidth, palette, legendSide != null);
        } else if (stackedAreaChart != null) {
            ChartTools.setAreaChartColors(stackedAreaChart, lineWidth, palette, legendSide != null);
        } else if (scatterChart != null) {
            ChartTools.setScatterChart​Colors(scatterChart, palette, legendSide != null);
        } else if (bubbleChart != null) {
            ChartTools.setBubbleChart​Colors(bubbleChart, bubbleStyleInput.getText(), palette, legendSide != null);
        }
        chart.requestLayout();
    }

    @FXML
    public void defaultBubbleStyle() {
        bubbleStyleInput.setText(ChartTools.DefaultBubbleStyle);
    }

    @FXML
    public void applyBubbleStyle() {
        UserConfig.setString(baseName + "BubbleStyle", bubbleStyleInput.getText());
        if (bubbleChart != null) {
            ChartTools.setBubbleChart​Colors(bubbleChart, bubbleStyleInput.getText(), palette, legendSide != null);
        }
    }


    /*
        static
     */
    public static Data2DChartXYController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartXYController controller = (Data2DChartXYController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartXYFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
