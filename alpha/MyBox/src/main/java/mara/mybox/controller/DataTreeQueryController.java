package mara.mybox.controller;

import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-5-7
 * @License Apache License Version 2.0
 */
public class DataTreeQueryController extends BaseTaskController {

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

            baseTitle = nodeTable.getTreeName() + " - " + message("Query");
            setTitle(baseTitle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
