package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartOption;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-5-11
 * @License Apache License Version 2.0
 */
public class Data2DChartPieOptionsController extends Data2DChartFxOptionsController {
    
    protected ControlData2DChartPie pieChartController;
    protected PieChartOption fxPieChart;
    protected PieChart pieChart;
    
    @FXML
    protected CheckBox clockwiseCheck;
    
    public Data2DChartPieOptionsController() {
    }
    
    public void setParameters(ControlData2DChartPie pieChartController) {
        try {
            this.pieChartController = pieChartController;
            
            initDataTab();
            initPlotTab();
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        plot
     */
    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();
            
            clockwiseCheck.setSelected(fxPieChart.isClockwise());
            clockwiseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                fxPieChart.setClockwise(clockwiseCheck.isSelected());
                if (pieChart != null) {
                    pieChart.setClockwise(clockwiseCheck.isSelected());
                }
            });
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static methods
     */
    public static Data2DChartPieOptionsController open(ControlData2DChartFx chartController) {
        try {
            if (chartController == null) {
                return null;
            }
            Data2DChartPieOptionsController controller = (Data2DChartPieOptionsController) WindowTools.openChildStage(
                    chartController.getMyWindow(), Fxmls.Data2DChartFxOptionsFxml, false);
//            controller.setParameters(chartController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }
    
}
