package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-10-13
 * @License Apache License Version 2.0
 */
public class Data2DRowFilterEdit extends BaseInputController {

    protected BaseData2DTaskController taskController;

    @FXML
    protected ControlData2DRowFilter filterController;

    public Data2DRowFilterEdit() {
    }

    public void setParameters(BaseData2DTaskController handleController, DataFilter filter) {
        try {
            super.setParameters(handleController, null);
            this.taskController = handleController;

            filterController.load(handleController.data2D, filter);
            thisPane.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkInput() {
        return getFilter() != null;
    }

    public DataFilter getFilter() {
        DataFilter filter = filterController.pickFilter(taskController.isAllPages());
        if (filter == null) {
            alertError(filterController.error);
            return null;
        }
        return filter;
    }

    /*
        static
     */
    public static Data2DRowFilterEdit open(BaseData2DTaskController handleController, DataFilter filter) {
        try {
            Data2DRowFilterEdit controller = (Data2DRowFilterEdit) WindowTools.branchStage(
                    handleController, Fxmls.Data2DRowFilterEditFxml);
            controller.setParameters(handleController, filter);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
