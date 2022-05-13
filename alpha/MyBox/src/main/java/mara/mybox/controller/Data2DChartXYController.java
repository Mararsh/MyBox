package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartXYController extends BaseData2DChartController {

    protected XYChart xyChart;

    @FXML
    protected ToggleGroup chartGroup;
    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            bubbleChartRadio, areaChartRadio, stackedAreaChartRadio, labelLocaionAboveRadio;
    @FXML
    protected ControlData2DChartXY chartController;
    @FXML
    protected VBox columnsBox, xyPlotBox, categoryNumbersBox, bubbleBox;

    public Data2DChartXYController() {
        baseTitle = message("XYChart");
        TipsLabelKey = "DataChartXYTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        okAction();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            chartController.data2D = data2D;
            ChartType chartType = null;
            if (barChartRadio.isSelected()) {
                chartType = ChartType.Bar;
            } else if (stackedBarChartRadio.isSelected()) {
                chartType = ChartType.StackedBar;
            } else if (lineChartRadio.isSelected()) {
                chartType = ChartType.Line;
            } else if (scatterChartRadio.isSelected()) {
                chartType = ChartType.Scatter;
            } else if (areaChartRadio.isSelected()) {
                chartType = ChartType.Area;
            } else if (stackedAreaChartRadio.isSelected()) {
                chartType = ChartType.StackedArea;
            } else if (bubbleChartRadio.isSelected()) {
                chartType = ChartType.Bubble;
            }
            chartController.initChart(chartType, chartType.name());
            xyChart = chartController.xyChart;
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
            chartController.writeChart(outputColumns, outputData, true);
            makePalette();
            setChartStyle();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setChartStyle() {
        try {
            if (isSettingValues) {
                return;
            }
            chartController.chartOptions.setPalette(palette);

            if (xyChart instanceof LineChart) {
                ChartTools.setLineChartColors(xyChart, chartController.chartOptions.getLineWidth(), palette,
                        chartController.chartOptions.showLegend());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
