package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends Data2DHandleController {

    @Override
    public void setParameters(ControlData2DEditTable editController) {
        try {
            super.setParameters(editController);

            sourceController.showAllPages(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void handleAllTask() {
        popError(message("NotSupport"));
    }

    @Override
    public boolean handleRows() {
        try {
            super.handleRows();
            if (handledData == null) {
                return false;
            }
            int rowsNumber = handledData.size(), columnsNumber = handledData.get(0).size();
            List<List<String>> transposed = new ArrayList<>();
            for (int r = 0; r < columnsNumber; ++r) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < rowsNumber; ++c) {
                    row.add(handledData.get(c).get(r));
                }
                transposed.add(row);
            }
            handledData = transposed;
            handledColumns = null;
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
