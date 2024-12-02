package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.fxml.WindowTools;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeHandleController extends BaseBatchFileController {

    protected BaseDataTreeViewController treeController;
    protected BaseNodeTable nodeTable;
    protected String dataName, chainName;

    public boolean treeRunning() {
        return WindowTools.isRunning(treeController);
    }

    @FXML
    public void treeAction() {
        DataTreeController.open(null, false, nodeTable);
    }

}
