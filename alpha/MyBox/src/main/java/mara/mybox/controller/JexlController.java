package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlController extends TreeManageController {

    @FXML
    protected JexlEditor editorController;
    @FXML
    protected ControlWebView webViewController;

    public JexlController() {
        baseTitle = message("JEXL");
        TipsLabelKey = "JEXLTips";
        category = TreeNode.JEXL;
        nameMsg = message("Title");
        valueMsg = message("Codes");
    }

    @Override
    public void initControls() {
        try {
            nodeController = editorController;
            super.initControls();

            editorController.setParameters(this);
            webViewController.setParent(this, ControlWebView.ScrollType.Bottom);

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

}
