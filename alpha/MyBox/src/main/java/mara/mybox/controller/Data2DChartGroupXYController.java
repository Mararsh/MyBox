package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-14
 * @License Apache License Version 2.0
 */
public class Data2DChartGroupXYController extends Data2DChartXYController {

    public Data2DChartGroupXYController() {
        baseTitle = message("GroupData") + " - " + message("XYChart");
    }

    @Override
    public void changeChartAsType() {
        if (group == null) {
            return;
        }
        playController.clear();
        if (chartTypesController.needChangeData()) {
            chartController.clearChart();
        } else {
            initChart(false);
            drawXYChart();
        }
    }

    @Override
    public boolean initChart() {
        return initChart(false);
    }

    @Override
    public String chartTitle() {
        if (group == null) {
            return null;
        }
        return group.getIdColName() + groupid + " - " + group.getParameterValue() + "\n"
                + super.chartTitle();
    }

    @Override
    public String categoryName() {
        return selectedCategory;
    }

    @Override
    public void drawXYChart() {
        displayFrames();
    }

    @Override
    public void initFrames() {
        makeIndices();
    }

    @Override
    public void drawFrame() {
        if (outputData == null) {
            return;
        }
        chartMaker.setDefaultChartTitle(chartTitle());
        chartController.writeXYChart(outputColumns, outputData,
                categoryIndex, valueIndices);
    }

    /*
        static
     */
    public static Data2DChartGroupXYController open(ControlData2DLoad tableController) {
        try {
            Data2DChartGroupXYController controller = (Data2DChartGroupXYController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartGroupXYFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
