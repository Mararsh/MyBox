package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-9
 * @License Apache License Version 2.0
 */
public class Data2DDefinitionController extends TreeManageController {

    @FXML
    protected Data2DDefinitionEditor editorController;

    public Data2DDefinitionController() {
        baseTitle = message("Data2DDefinition");
        TipsLabelKey = "Data2DDefinitionTips";
        category = InfoNode.Data2DDefinition;
        nameMsg = message("Title");
        valueMsg = message("Codes");
    }

    @Override
    public void initControls() {
        try {
            nodeController = editorController;
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean controlAltL() {
        editorController.columnsController.clearAction();
        return true;
    }

    /*
        static methods
     */
    public static Data2DDefinitionController open() {
        Data2DDefinitionController controller = (Data2DDefinitionController) WindowTools.openStage(Fxmls.Data2DDefinitionFxml);
        controller.requestMouse();
        return controller;
    }

}
