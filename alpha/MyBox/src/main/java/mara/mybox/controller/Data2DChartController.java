package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartController extends Data2DHandleController {

    /*
        static
     */
    public static Data2DChartController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartController controller = (Data2DChartController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartFxml, false);
//            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
