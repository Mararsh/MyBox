package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.StatisticCalculation;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledBoxWhiskerChart;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class Data2DChartBoxWhiskerController extends BaseData2DChartXYController {

    protected int lineWidth, categorysCol;
    protected LabeledBoxWhiskerChart boxWhiskerChart;
    protected StatisticCalculation calculation;
    protected List<List<String>> categoryValues;

    @FXML
    protected ComboBox<String> lineWdithSelector;
    @FXML
    protected VBox dataOptionsBox;
    @FXML
    protected HBox lineWidthBox;
    @FXML
    protected RadioButton categoryStringRadio, categoryNumberRadio;
    @FXML
    protected ToggleGroup categoryValuesGroup;

    public Data2DChartBoxWhiskerController() {
        baseTitle = message("BoxWhiskerChart");
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
                                if (boxWhiskerChart != null) {
                                    ChartTools.setLineChartColors(boxWhiskerChart, lineWidth, palette, legendSide != null);
                                }
                            } else {
                                lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
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
                    redrawChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        if (rowsRadio.isSelected()) {
            if (!dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                dataOptionsBox.getChildren().add(1, categoryColumnsPane);
            }
        } else {
            if (dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                dataOptionsBox.getChildren().remove(categoryColumnsPane);
            }
        }
    }

    @Override
    public void checkChartType() {
        try {
            setSourceLabel(message("XYChartLabel"));

            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String title() {
        String prefix = categoryName() + " - ";
        return prefix + valuesNames();
    }

    @Override
    public boolean isCategoryNumbers() {
        return rowsRadio.isSelected() && categoryNumberRadio.isSelected();
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        objectChanged();
        if (rowsRadio.isSelected()) {
            if (selectedCategory == null) {
                infoLabel.setText(message("SelectToHandle"));
                okButton.setDisable(true);
                return false;
            }
            categorysCol = data2D.colOrder(selectedCategory);
        }
        calculation = new StatisticCalculation()
                .setMedian(true)
                .setMaximum(true)
                .setMinimum(true)
                .setUpperQuartile(true)
                .setLowerQuartile(true)
                .setScale(scale);
        switch (objectType) {
            case Rows:
                calculation.setStatisticObject(StatisticCalculation.StatisticObject.Rows);
                break;
            case All:
                calculation.setStatisticObject(StatisticCalculation.StatisticObject.All);
                break;
            default:
                calculation.setStatisticObject(StatisticCalculation.StatisticObject.Columns);
                break;
        }
        calculation.setHandleController(this).setData2D(data2D)
                .setColsIndices(sourceController.checkedColsIndices())
                .setColsNames(sourceController.checkedColsNames());
        return ok;
    }

    @Override
    public boolean initData() {
        return super.initData() && calculation.prepare();
    }

    @Override
    public void readData() {
        if (sourceController.allPages()) {
            outputData = data2D.allRows(colsIndices, true);
        } else {
            outputData = sourceController.selectedData(
                    sourceController.checkedRowsIndices(), colsIndices, true);
        }
        if (rowsRadio.isSelected()) {
            List<Integer> indices = new ArrayList<>();
            indices.add(categorysCol);
            if (sourceController.allPages()) {
                categoryValues = data2D.allRows(indices, false);
            } else {
                categoryValues = sourceController.selectedData(
                        sourceController.checkedRowsIndices(), indices, false);
            }
        }
    }

    @Override
    public void clearChart() {
        super.clearChart();
        boxWhiskerChart = null;
    }

    @Override
    public void makeChart() {
        try {
            makeAxis();

            makeLineChart();

            makeXYChart();
            makeFinalChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean makeLineChart() {
        try {
            boxWhiskerChart = new LabeledBoxWhiskerChart(xAxis, yAxis);
            xyChart = boxWhiskerChart;
            boxWhiskerChart.setChartController(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public void writeChartData() {
        if (outputData == null || outputData.isEmpty()) {
            this.popError(message("NoData"));
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    return calculation.statisticData(outputData);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputColumns = calculation.getOutputColumns();
                outputData = calculation.getOutputData();
                writeChart();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public void writeChart() {
        try {
            palette = new HashMap();
            Random random = new Random();
            XYChart.Data xyData;
            double numberValue, numberCoordinateValue;
            for (int r = 0; r < outputData.size(); r++) {
                List<String> rowData = outputData.get(r);
                XYChart.Series series = new XYChart.Series();
                series.setName(rowData.get(0));
                String rgb = FxColorTools.color2rgb(FxColorTools.randomColor(random));
                palette.put(series.getName(), rgb);

                for (int c = 1; c < outputColumns.size(); c++) {
                    Data2DColumn column = outputColumns.get(c);
                    String category = column.getColumnName();
                    numberValue = data2D.doubleValue(rowData.get(c));
                    numberCoordinateValue = ChartTools.coordinateValue(nCoordinate, numberValue);
                    xyData = new XYChart.Data(category, numberCoordinateValue);
                    series.getData().add(xyData);
                }
                xyChart.getData().add(r, series);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setChartStyle() {
        if (chart == null) {
            return;
        }
        ChartTools.setLineChartColors(boxWhiskerChart, lineWidth, palette, legendSide != null);
        chart.requestLayout();
    }


    /*
        static
     */
    public static Data2DChartBoxWhiskerController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartBoxWhiskerController controller = (Data2DChartBoxWhiskerController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartBoxWhiskerFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
