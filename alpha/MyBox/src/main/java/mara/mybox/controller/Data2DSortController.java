package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
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
            List<String> colsNames = new ArrayList<>();
            for (String name : checkedColsNames) {
                if (!colsNames.contains(name)) {
                    colsNames.add(name);
                }
            }
            for (String name : sortNames) {
                if (!colsNames.contains(name)) {
                    colsNames.add(name);
                }
            }
            checkedColsIndices = new ArrayList<>();
            for (String name : colsNames) {
                checkedColsIndices.add(data2D.colOrder(name));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            DataFileCSV csvData = sortedData(targetController.name(), checkedColsIndices);
            if (csvData == null) {
                return false;
            }
            outputData = csvData.allRows(false);
            FileDeleteTools.delete(csvData.getFile());
            outputColumns = csvData.columns;
            if (showColNames()) {
                outputData.add(0, csvData.columnNames());
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        return sortedData(targetController.name(), checkedColsIndices);
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
