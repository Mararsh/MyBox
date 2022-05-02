package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends BaseData2DHandleController {

    public Data2DTransposeController() {
        baseTitle = message("Transpose");
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        if (sourceController.allPages()) {
            infoLabel.setText(message("AllRowsLoadComments"));
        }
        return ok;
    }

    @Override
    public void handleAllTask() {
        if (targetController == null) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setTask(task);
                outputData = data2D.allRows(sourceController.checkedColsIndices, showRowNumber());
                return transpose();
            }

            @Override
            protected void whenSucceeded() {
                if (targetController == null || targetController.inTable()) {
                    updateTable();
                } else {
                    outputExternal();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
            }

        };
        start(task);
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = sourceController.selectedData(showRowNumber());
            if (outputData == null) {
                return false;
            }
            return transpose();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean transpose() {
        try {
            outputColumns = null;
            if (outputData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = sourceController.checkedColsNames();
                if (showRowNumber()) {
                    names.add(0, message("SourceRowNumber"));
                }
                outputData.add(0, names);
            }
            int rowsNumber = outputData.size(), columnsNumber = outputData.get(0).size();
            List<List<String>> transposed = new ArrayList<>();
            for (int r = 0; r < columnsNumber; ++r) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < rowsNumber; ++c) {
                    row.add(outputData.get(c).get(r));
                }
                transposed.add(row);
            }
            outputData = transposed;
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    /*
        static
     */
    public static Data2DTransposeController open(ControlData2DEditTable tableController) {
        try {
            Data2DTransposeController controller = (Data2DTransposeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTransposeFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
