package mara.mybox.controller;

import java.util.List;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public class Data2DSortController extends BaseData2DHandleController {

    public Data2DSortController() {
        baseTitle = message("Sort");
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            List<String> sortNames = sortNames();
            if (sortNames == null || sortNames.isEmpty()) {
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
        MyBoxLog.console(checkedColsIndices);
        outputData = sortedData(checkedColsIndices, showRowNumber());
        return outputData != null && !outputData.isEmpty();
    }

    @Override
    public DataFileCSV generatedFile() {
        return sortedFile(targetController.name(), checkedColsIndices, showRowNumber());
    }

    /*
        static
     */
    public static Data2DSortController open(ControlData2DLoad tableController) {
        try {
            Data2DSortController controller = (Data2DSortController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSortFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
