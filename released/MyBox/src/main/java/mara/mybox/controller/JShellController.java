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
 * @CreateDate 2022-3-4
 * @License Apache License Version 2.0
 */
public class JShellController extends TreeManageController {

    @FXML
    protected JShellEditor editorController;
    @FXML
    protected ControlWebView webViewController;
    @FXML
    protected JShellSnippets snippetsController;
    @FXML
    protected JShellPaths pathsController;

    public JShellController() {
        baseTitle = message("JShell");
        TipsLabelKey = "JShellTips";
        category = TreeNode.JShellCode;
        nameMsg = message("Title");
        valueMsg = message("Codes");
    }

    @Override
    public void initControls() {
        try {
            nodeController = editorController;
            super.initControls();

            webViewController.setParent(this, ControlWebView.ScrollType.Bottom);
            snippetsController.setParameters(this);
            editorController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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
        PopTools.popHtmlStyle(mouseEvent, webViewController);
    }

    @FXML
    public void editResults() {
        webViewController.editAction();
    }

    @FXML
    public void clearResults() {
        editorController.outputs = "";
        webViewController.loadContents("");
    }

    public void edit(String script) {
        editNode(null);
        editorController.valueInput.setText(script);
    }

    /*
        static methods
     */
    public static JShellController open(String script) {
        JShellController controller = (JShellController) WindowTools.openStage(Fxmls.JShellFxml);
        controller.edit(script);
        controller.requestMouse();
        return controller;
    }

}
