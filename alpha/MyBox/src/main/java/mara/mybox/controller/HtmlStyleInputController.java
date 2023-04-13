package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class HtmlStyleInputController extends TextInputController {

    public static HtmlStyleInputController open(BaseController parent, String title, String initValue) {
        try {
            HtmlStyleInputController controller = (HtmlStyleInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.HtmlStyleInputFxml, false);
            controller.setParameters(parent, title, initValue);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
