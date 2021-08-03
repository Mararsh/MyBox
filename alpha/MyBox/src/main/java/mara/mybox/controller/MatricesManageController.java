package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

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
        baseTitle = Languages.message("MatricesManage");
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
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MatricesManageController) {
                try {
                    controller = (MatricesManageController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MatricesManageController) WindowTools.openStage(Fxmls.MatricesManageFxml);
        }
        return controller;
    }

}
