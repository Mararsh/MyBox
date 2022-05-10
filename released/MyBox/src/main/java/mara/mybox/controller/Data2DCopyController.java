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
public class Data2DCopyController extends BaseData2DHandleController {

    public Data2DCopyController() {
        baseTitle = message("Copy");
    }

    @Override
    public boolean checkOptions() {
        targetController.setNotInTable(sourceController.allPages());
        return super.checkOptions();
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.copy(sourceController.checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    /*
        static
     */
    public static Data2DCopyController open(ControlData2DEditTable tableController) {
        try {
            Data2DCopyController controller = (Data2DCopyController) WindowTools.openChildStage(
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
