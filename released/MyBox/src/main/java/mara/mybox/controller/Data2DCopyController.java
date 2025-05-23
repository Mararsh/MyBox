package mara.mybox.controller;

import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DCopyController extends BaseData2DTaskTargetsController {

    public Data2DCopyController() {
        baseTitle = message("CopyFilterQueryConvert");
    }

    @Override
    public boolean handleAllData(FxTask currentTask, Data2DWriter writer) {
        return data2D.copy(currentTask, writer, checkedColsIndices,
                rowNumberCheck.isSelected(), invalidAs)
                >= 0;
    }

    /*
        static
     */
    public static Data2DCopyController open(BaseData2DLoadController tableController) {
        try {
            Data2DCopyController controller = (Data2DCopyController) WindowTools.referredStage(
                    tableController, Fxmls.Data2DCopyFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
