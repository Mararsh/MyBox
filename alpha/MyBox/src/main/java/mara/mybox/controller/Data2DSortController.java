package mara.mybox.controller;

import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public class Data2DSortController extends BaseData2DTargetsController {

    public Data2DSortController() {
        baseTitle = message("Sort");
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            if (orders == null || orders.isEmpty()) {
                outOptionsError(message("SelectToHandle") + ": " + message("Order"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        outputData = sortedData(checkedColsIndices, showRowNumber());
        return outputData != null && !outputData.isEmpty();
    }

    @Override
    public DataFileCSV generatedFile(FxTask currentTask) {
        return sortedFile(targetController.name(), checkedColsIndices, showRowNumber());
    }

    /*
        static
     */
    public static Data2DSortController open(BaseData2DLoadController tableController) {
        try {
            Data2DSortController controller = (Data2DSortController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DSortFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
