package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-16
 * @License Apache License Version 2.0
 */
public class Data2DSelectController extends BaseChildController {

    protected BaseData2DLoadController targetController;

    @FXML
    protected BaseData2DListController listController;

    public Data2DSelectController() {
        baseTitle = message("ManageData");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            rightPaneControl = listController.rightPaneControl;
            initRightPaneControl();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseData2DLoadController target) {
        try {
            targetController = target;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (listController.viewController.data2D == null) {
            popError(message("SelectToHandle"));
            return;
        }
        targetController.loadDef(listController.viewController.data2D, false);
        close();
    }

    /*
        static
     */
    public static Data2DSelectController open(BaseData2DLoadController caller) {
        try {
            Data2DSelectController controller = (Data2DSelectController) WindowTools.childStage(caller, Fxmls.Data2DSelectFxml);
            controller.setParameters(caller);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
