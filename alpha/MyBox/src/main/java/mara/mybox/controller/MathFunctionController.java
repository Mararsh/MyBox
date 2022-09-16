package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class MathFunctionController extends TreeManageController {

    @FXML
    protected ControlMathFunctionEditor editorController;

    public MathFunctionController() {
        baseTitle = message("MathFunction");
        category = TreeNode.MathFunction;
        TipsLabelKey = "MathFunctionTips";
        nameMsg = message("Title");
        valueMsg = message("MathFunction");
        moreMsg = message("FunctionDomain");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            nodeController = editorController;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editorController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void itemClicked() {
    }

}
