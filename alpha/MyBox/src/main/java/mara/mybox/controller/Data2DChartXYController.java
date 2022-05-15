package mara.mybox.controller;

import java.util.ArrayList;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.XYChartOptions;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartXYController extends BaseData2DChartController {

    protected XYChartOptions xyOptions;

    @FXML
    protected ToggleGroup chartGroup;
    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            bubbleChartRadio, areaChartRadio, stackedAreaChartRadio;
    @FXML
    protected VBox columnsBox;
    @FXML
    protected FlowPane valueColumnPane, categoryColumnsPane;
    @FXML
    protected ControlData2DChartXY chartController;

    public Data2DChartXYController() {
        baseTitle = message("XYChart");
        TipsLabelKey = "DataChartXYTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            xyOptions = chartController.xyOptions;

            checkChartType();
            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        checkChartType();
                        okAction();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

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
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            String title = selectedCategory;
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
                title += " - " + selectedValue;
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
            title += " - " + sourceController.checkedColsNames();

            ChartType chartType;
            String chartName;
            if (barChartRadio.isSelected()) {
                chartType = ChartType.Bar;
                chartName = message("BarChart");
            } else if (stackedBarChartRadio.isSelected()) {
                chartType = ChartType.StackedBar;
                chartName = message("StackedBarChart");
            } else if (lineChartRadio.isSelected()) {
                chartType = ChartType.Line;
                chartName = message("LineChart");
            } else if (scatterChartRadio.isSelected()) {
                chartType = ChartType.Scatter;
                chartName = message("ScatterChart");
            } else if (areaChartRadio.isSelected()) {
                chartType = ChartType.Area;
                chartName = message("AreaChart");
            } else if (stackedAreaChartRadio.isSelected()) {
                chartType = ChartType.StackedArea;
                chartName = message("StackedAreaChart");
            } else if (bubbleChartRadio.isSelected()) {
                chartType = ChartType.Bubble;
                chartName = message("BubbleChart");
            } else {
                return false;
            }
            xyOptions.init(chartType, chartName)
                    .setDefaultChartTitle(title)
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(selectedValue)
                    .setPalette(makePalette());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartController.writeXYChart(outputColumns, outputData);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
