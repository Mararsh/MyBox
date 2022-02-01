package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class HtmlScriptController extends TextInputController {

    protected ControlWebView controlWebView;

    @FXML
    protected TextField resultInput;

    public HtmlScriptController() {
        baseTitle = "JavaScript";
    }

    public void setParameters(ControlWebView controlWebView) {
        try {
            this.controlWebView = controlWebView;
            super.setParameters(controlWebView.parentController != null ? controlWebView.parentController : controlWebView,
                    "JavaScript", "");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    @FXML
    @Override
    public void okAction() {
        String script = textArea.getText();
        if (script == null || script.isBlank()) {
            return;
        }
        try {
            Object o = controlWebView.webEngine.executeScript(script);
            if (o != null) {
                resultInput.setText(o.toString());
            } else {
                resultInput.setText("");
            }
            popDone();
        } catch (Exception e) {
            resultInput.setText(e.toString());
        }
    }

    public static HtmlScriptController open(ControlWebView controlWebView) {
        try {
            HtmlScriptController controller = (HtmlScriptController) WindowTools.openChildStage(
                    controlWebView.getMyWindow(), Fxmls.HtmlScriptFxml, false);
            controller.setParameters(controlWebView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
