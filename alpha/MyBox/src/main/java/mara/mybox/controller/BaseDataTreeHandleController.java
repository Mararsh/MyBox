package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeHandleController extends BaseTaskController {

    protected BaseDataTreeViewController treeController;
    protected BaseNodeTable nodeTable;
    protected String dataName, chainName;

    public void setParameters(BaseDataTreeViewController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            treeController = parent;
            nodeTable = treeController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean treeRunning() {
        return WindowTools.isRunning(treeController);
    }

    @FXML
    public void manageAction() {
        DataTreeController.open(null, false, nodeTable);
    }

}
