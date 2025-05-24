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

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected String dataName, chainName;

    public void setParameters(BaseDataTreeController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            dataController = parent;
            parentController = parent;
            nodeTable = dataController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean dataRunning() {
        return WindowTools.isRunning(dataController);
    }

    public boolean parentRunning() {
        return WindowTools.isRunning(parentController);
    }

    @FXML
    public void manageAction() {
        DataTreeController.open(null, false, nodeTable);
        setIconified(true);
    }

}
