package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.db.table.TableInfo;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class InfoTreeController extends BaseDataTreeManageController {

    @FXML
    protected InfoNodeController infoController;

    public InfoTreeController() {
        baseTitle = Languages.message("Notes");
        TipsLabelKey = "NotesTips";
    }

    @Override
    public void initTreeValues() {
        try {
            dataTable = new TableInfo();
            nodeController = infoController;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static InfoTreeController oneOpen() {
        InfoTreeController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof InfoTreeController) {
                try {
                    controller = (InfoTreeController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (InfoTreeController) WindowTools.openStage(Fxmls.InfoTreeFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
