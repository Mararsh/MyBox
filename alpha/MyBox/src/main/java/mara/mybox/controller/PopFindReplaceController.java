package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-23
 * @License Apache License Version 2.0
 */
public class PopFindReplaceController extends PopTextBaseController {

    @FXML
    protected ControlFindReplace findReplaceController;

    public PopFindReplaceController() {
        baseTitle = Languages.message("FindReplace");
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);

            findReplaceController.setEditInput(parent, textInput);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        static methods
     */
    public static PopFindReplaceController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof PopFindReplaceController) {
                    ((PopFindReplaceController) object).close();
                }
            }
            PopFindReplaceController controller
                    = (PopFindReplaceController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.PopFindReplaceFxml, false);
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
