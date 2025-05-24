package mara.mybox.controller;

import javafx.scene.Node;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-14
 * @License Apache License Version 2.0
 */
public class Data2DChartGroupBubbleController extends Data2DChartBubbleController {

    public Data2DChartGroupBubbleController() {
        baseTitle = message("GroupData") + " - " + message("BubbleChart");
    }

    @Override
    public boolean initChart() {
        return initChart(false);
    }

    @Override
    public void drawXYChart() {
        drawFrame();
    }

    @Override
    public void drawFrame() {
        if (outputData == null) {
            return;
        }
        chartMaker.setDefaultChartTitle(chartTitle());
        super.drawXYChart();
    }

    @Override
    public Node snapNode() {
        return chartController.chartPane;
    }

    /*
        static
     */
    public static Data2DChartGroupBubbleController open(BaseData2DLoadController tableController) {
        try {
            Data2DChartGroupBubbleController controller = (Data2DChartGroupBubbleController) WindowTools.referredStage(
                    tableController, Fxmls.Data2DChartGroupBubbleFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
