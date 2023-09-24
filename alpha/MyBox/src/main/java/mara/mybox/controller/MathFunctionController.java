package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class MathFunctionController extends InfoTreeManageController {

    @FXML
    protected MathFunctionEditor editorController;
    @FXML
    protected ControlMathFunctionCalculator calculateController;

    public MathFunctionController() {
        baseTitle = message("MathFunction");
        TipsLabelKey = "MathFunctionTips";
        category = InfoNode.MathFunction;
        nameMsg = message("Title");
        valueMsg = message("MathFunction");
    }

    @Override
    public void initControls() {
        try {
            editor = editorController;
            super.initControls();

            editorController.setParameters(this);
            calculateController.setParameters(editorController);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void itemClicked() {
    }

    /*
        static
     */
    public static MathFunctionController open() {
        try {
            MathFunctionController controller = (MathFunctionController) WindowTools.openStage(Fxmls.MathFunctionFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
