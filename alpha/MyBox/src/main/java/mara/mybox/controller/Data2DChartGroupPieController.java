package mara.mybox.controller;

import javafx.scene.Node;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-30
 * @License Apache License Version 2.0
 */
public class Data2DChartGroupPieController extends Data2DChartPieController {

    public Data2DChartGroupPieController() {
        baseTitle = message("GroupData") + " - " + message("PieChart");
    }

    @Override
    public String chartTitle() {
        if (group == null) {
            return null;
        }
        return group.getIdColName() + groupid + " - " + group.parameterValue(groupid - 1) + "\n"
                + super.chartTitle();
    }

    @Override
    public void drawPieChart() {
        drawFrame();
    }

    @Override
    public void drawFrame() {
        if (outputData == null) {
            return;
        }
        pieMaker.setDefaultChartTitle(chartTitle());
        super.drawPieChart();
    }

    @Override
    public Node snapNode() {
        return chartController.chartPane;
    }

    /*
        static
     */
    public static Data2DChartGroupPieController open(ControlData2DLoad tableController) {
        try {
            Data2DChartGroupPieController controller = (Data2DChartGroupPieController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartGroupPieFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
