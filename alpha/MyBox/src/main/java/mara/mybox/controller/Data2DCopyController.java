package mara.mybox.controller;

import mara.mybox.data.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DCopyController extends Data2DHandleController {

    public Data2DCopyController() {
        includeTable = true;
    }

    @Override
    public boolean checkOptions() {
        targetController.setHandleFile(allPages());
        return super.checkOptions();
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.copy(tableController.checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    /*
        static
     */
    public static Data2DCopyController open(ControlData2DEditTable tableController) {
        try {
            Data2DCopyController controller = (Data2DCopyController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DCopyFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
