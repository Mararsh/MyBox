package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-14
 * @License Apache License Version 2.0
 */
public class DatabaseSQLController extends TreeManageController {

    @FXML
    protected DatabaseSqlEditor editorController;

    public DatabaseSQLController() {
        baseTitle = message("DatabaseSQL");
        category = TreeNode.SQL;
        nameMsg = message("Title");
        valueMsg = "SQL";
    }

    @Override
    public void initControls() {
        try {
            leafController = editorController;
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    public void setInternal(boolean internal) {
        editorController.internal = internal;
    }

    /*
        static
     */
    public static DatabaseSQLController open(boolean internal) {
        DatabaseSQLController controller = (DatabaseSQLController) WindowTools.openStage(Fxmls.DatabaseSQLFxml);
        controller.setInternal(internal);
        controller.requestMouse();
        return controller;
    }

}
