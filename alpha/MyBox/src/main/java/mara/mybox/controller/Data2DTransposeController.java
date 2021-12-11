package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends Data2DOperationController {

    public void setParameters(ControlData2DEditTable tableController) {
        super.setParameters(tableController, false);
    }

    @Override
    public boolean hanldeData() {
        if (!isAll && selectedNames != null) {
            selectedData.add(0, selectedNames);
        }
        handledData = TextTools.transpose(selectedData);
        handledNames = null;
        return true;
    }

    /*
        static
     */
    public static Data2DTransposeController open(ControlData2DEditTable tableController) {
        try {
            Data2DTransposeController controller = (Data2DTransposeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTransposeFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
