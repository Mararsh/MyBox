package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-24
 * @License Apache License Version 2.0
 */
public class ShapesManageController extends BaseController {

    @FXML
    protected ControlShapesManage listController;

    public ShapesManageController() {
        baseTitle = message("ManageShapes");
        TipsLabelKey = "DataManageTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static ShapesManageController oneOpen() {
        ShapesManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ShapesManageController) {
                try {
                    controller = (ShapesManageController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (ShapesManageController) WindowTools.openStage(Fxmls.ShapesManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static ShapesManageController open(Data2DDefinition def) {
        ShapesManageController controller = oneOpen();
        return controller;
    }

    public static void updateList() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ShapesManageController) {
                try {
                    ShapesManageController controller = (ShapesManageController) object;
//                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
    }

}
