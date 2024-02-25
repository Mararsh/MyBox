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
public class Data2DChartGroupSelfComparisonBarsController extends Data2DChartSelfComparisonBarsController {

    public Data2DChartGroupSelfComparisonBarsController() {
        baseTitle = message("GroupData") + " - " + message("SelfComparisonBarsChart");
    }

    @Override
    public void drawFrame() {
        if (outputData == null) {
            return;
        }
        outputHtml(makeHtml());
    }

    @Override
    public Node snapNode() {
        return webViewController.webView;
    }

    /*
        static
     */
    public static Data2DChartGroupSelfComparisonBarsController open(BaseData2DLoadController tableController) {
        try {
            Data2DChartGroupSelfComparisonBarsController controller = (Data2DChartGroupSelfComparisonBarsController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DChartGroupSelfComparisonBarsFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
