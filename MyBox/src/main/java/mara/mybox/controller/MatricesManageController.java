package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class MatricesManageController extends BaseController {

    protected BaseMatrixController editController;

    @FXML
    protected ControlMatricesList listController;

    public MatricesManageController() {
        baseTitle = message("MatricesManage");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            editController = listController.editController;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        listController.loadTableData();
//        editController.setManager(listController);
    }

    @Override
    public boolean checkBeforeNextAction() {
        return editController.checkBeforeNextAction();
    }

    public static MatricesManageController oneOpen() {
        MatricesManageController controller = null;
        Stage stage = FxmlStage.findStage(message("MatricesManage"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (MatricesManageController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (MatricesManageController) FxmlStage.openStage(CommonValues.MatricesManageFxml);
        }
        if (controller != null) {
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

}
