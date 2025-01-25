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
public class Data2DGroupLocationsController extends Data2DLocationDistributionController {

    public Data2DGroupLocationsController() {
        baseTitle = message("GroupData") + " - " + message("XYChart");
    }

    /*
        static
     */
    public static Data2DGroupLocationsController open(BaseData2DLoadController tableController) {
        try {
            Data2DGroupLocationsController controller = (Data2DGroupLocationsController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DChartGroupXYFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
