package mara.mybox.controller;

import javafx.scene.control.TextInputControl;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-23
 * @License Apache License Version 2.0
 */
public class FindController extends FindReplaceController {

    public FindController() {
        baseTitle = message("Find");
    }

    /*
        static methods
     */
    public static FindController forInput(BaseController parent, TextInputControl input) {
        try {
            if (parent == null || input == null) {
                return null;
            }
            FindController controller = (FindController) WindowTools.branchStage(parent, Fxmls.FindFxml);
            controller.setInput(parent, input);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static FindController forEditor(BaseTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            FindController controller = (FindController) WindowTools.branchStage(parent, Fxmls.FindFxml);
            controller.setEditor(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
