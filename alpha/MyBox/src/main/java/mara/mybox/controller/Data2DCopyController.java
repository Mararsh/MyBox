package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DCopyController extends Data2DOperationController {

    public void setParameters(ControlData2DEditTable tableController) {
        super.setParameters(tableController, false, true);
    }

    @Override
    public boolean handleForTable() {
        try {
            handledData = new ArrayList<>();
            int colsNumber = tableController.data2D.tableColsNumber();
            for (int row : selectedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                List<String> newRow = new ArrayList<>();
                for (int c = 0; c < colsNumber; c++) {
                    if (selectedColumnsIndices.contains(c)) {
                        newRow.add(c, tableRow.get(c + 1));
                    } else {
                        newRow.add(null);
                    }
                }
                handledData.add(newRow);
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
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
