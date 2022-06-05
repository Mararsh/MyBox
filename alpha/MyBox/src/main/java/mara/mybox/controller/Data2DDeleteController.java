package mara.mybox.controller;

import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DDeleteController extends BaseData2DHandleController {

    public Data2DDeleteController() {
        baseTitle = message("Delete");
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.copy(checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    /*
        static
     */
    public static Data2DDeleteController open(ControlData2DEditTable tableController) {
        try {
            Data2DDeleteController controller = (Data2DDeleteController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DCopyFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
