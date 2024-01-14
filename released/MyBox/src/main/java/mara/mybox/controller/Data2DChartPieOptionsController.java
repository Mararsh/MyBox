package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartOptions;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-5-11
 * @License Apache License Version 2.0
 */
public class Data2DChartPieOptionsController extends BaseData2DChartFxOptionsController {

    protected ControlData2DChartPie pieChartController;
    protected PieChartOptions pieOptions;

    @FXML
    protected CheckBox clockwiseCheck;

    public Data2DChartPieOptionsController() {
    }

    public void setParameters(ControlData2DChartPie pieChartController) {
        try {
            this.pieChartController = pieChartController;
            this.pieOptions = pieChartController.pieMaker;

            chartController = pieChartController;
            options = pieOptions;
            chartName = options.getChartName();
            titleLabel.setText(chartName);

            isSettingValues = true;
            initDataTab();
            initPlotTab();
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        plot
     */
    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            clockwiseCheck.setSelected(pieOptions.isClockwise());
            clockwiseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                pieOptions.setClockwise(clockwiseCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static Data2DChartPieOptionsController open(ControlData2DChartPie chartController) {
        try {
            if (chartController == null) {
                return null;
            }
            Data2DChartPieOptionsController controller = (Data2DChartPieOptionsController) WindowTools.branchStage(
                    chartController, Fxmls.Data2DChartPieOptionsFxml);
            controller.setParameters(chartController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
