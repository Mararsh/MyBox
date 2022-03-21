package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class JavaScriptController extends TreeManageController {

    protected ControlWebView sourceWebView;

    @FXML
    protected JavaScriptEditor editorController;
    @FXML
    protected ControlWebView outputController;

    public JavaScriptController() {
        baseTitle = "JavaScript";
        category = TreeNode.JavaScript;
        nameMsg = message("Name");
        valueMsg = "JavaScript";
    }

    @Override
    public void initControls() {
        try {
            leafController = editorController;
            super.initControls();

            editorController.setParameters(this);
            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(ControlWebView sourceWebView) {
        this.sourceWebView = sourceWebView;
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    @Override
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @FXML
    public void popHtmlStyle(MouseEvent mouseEvent) {
        PopTools.popHtmlStyle(mouseEvent, outputController);
    }

    @FXML
    public void editResults() {
        outputController.editAction();
    }

    @FXML
    public void clearResults() {
        editorController.outputs = "";
        outputController.loadContents("");
    }

    /*
        static
     */
    public static JavaScriptController open(ControlWebView controlWebView) {
        try {
            JavaScriptController controller = (JavaScriptController) WindowTools.openChildStage(
                    controlWebView.getMyWindow(), Fxmls.JavaScriptFxml, false);
            controller.setParameters(controlWebView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
