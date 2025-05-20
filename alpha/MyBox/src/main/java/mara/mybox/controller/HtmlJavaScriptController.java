package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class HtmlJavaScriptController extends BaseJavaScriptController {

    public HtmlJavaScriptController() {
        baseTitle = "JavaScript";
    }

    /*
        static
     */
    public static HtmlJavaScriptController open(BaseController parent, ControlWebView sourceWebView) {
        try {
            HtmlJavaScriptController controller
                    = (HtmlJavaScriptController) WindowTools.referredTopStage(parent, Fxmls.HtmlJavaScriptFxml);
            controller.setParameters(sourceWebView);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
