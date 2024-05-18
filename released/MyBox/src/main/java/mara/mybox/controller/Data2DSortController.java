package mara.mybox.controller;

import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.writer.Data2DWriter;
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
public class Data2DSortController extends BaseData2DTaskTargetsController {

    public Data2DSortController() {
        baseTitle = message("Sort");
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            if (orders == null || orders.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Order"));
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
        outputData = sortPage(checkedColsIndices, showRowNumber());
        return outputData != null && !outputData.isEmpty();
    }

    @Override
    public boolean handleAllData(FxTask currentTask, Data2DWriter writer) {
        try {
            TmpTable tmpTable = tmpTable(targetController.name(), checkedColsIndices, showRowNumber());
            if (tmpTable == null) {
                return false;
            }
            boolean ok = tmpTable.sort(currentTask, writer, maxData);
            tmpTable.drop();
            return ok;
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
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
