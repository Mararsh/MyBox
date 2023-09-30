package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-14
 * @License Apache License Version 2.0
 */
public class DatabaseSqlController extends InfoTreeManageController {

    @FXML
    protected DatabaseSqlEditor editorController;

    public DatabaseSqlController() {
        baseTitle = message("DatabaseSQL");
        category = InfoNode.SQL;
        nameMsg = message("Title");
        valueMsg = "SQL";
    }

    @Override
    public void initControls() {
        try {
            editor = editorController;
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
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
    public static DatabaseSqlController open(boolean internal) {
        DatabaseSqlController controller = (DatabaseSqlController) WindowTools.openStage(Fxmls.DatabaseSqlFxml);
        controller.setInternal(internal);
        controller.requestMouse();
        return controller;
    }

}
