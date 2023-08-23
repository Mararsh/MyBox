package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-23
 * @License Apache License Version 2.0
 */
public class FindReplacePopController extends MenuTextBaseController {

    @FXML
    protected ControlFindReplace replaceController;

    public FindReplacePopController() {
        baseTitle = Languages.message("FindReplace");
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);

            replaceController.setEditInput(parent, textInput);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static FindReplacePopController replaceMenu(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            closeAll();
            FindReplacePopController controller
                    = (FindReplacePopController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.FindReplacePopFxml, false);
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
