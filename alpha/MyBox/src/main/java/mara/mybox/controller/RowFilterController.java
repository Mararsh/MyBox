package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-15
 * @License Apache License Version 2.0
 */
public class RowFilterController extends InfoTreeManageController {

    protected ControlWebView htmlWebView;
    protected String outputs = "";

    @FXML
    protected RowFilterEditor editorController;

    public RowFilterController() {
        baseTitle = message("RowFilter");
        category = InfoNode.RowFilter;
        nameMsg = message("Title");
        valueMsg = message("RowFilter");
    }

    @Override
    public void initControls() {
        try {
            editor = editorController;
            super.initControls();

            editorController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void edit(String script, boolean reversed, long max) {
        if (!checkBeforeNextAction()) {
            return;
        }
        editorController.editNode(null);
        editorController.load(script, reversed, max);
    }


    /*
        static
     */
    public static RowFilterController open() {
        try {
            RowFilterController controller = (RowFilterController) WindowTools.openStage(Fxmls.RowFilterFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static RowFilterController open(String script, boolean reversed, long max) {
        try {
            RowFilterController controller = (RowFilterController) WindowTools.openStage(Fxmls.RowFilterFxml);
            controller.edit(script, reversed, max);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
