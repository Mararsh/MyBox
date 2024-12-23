package mara.mybox.controller;

import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class BaseDataSelectController extends BaseDataTreeViewController {

    public void setParameters(BaseController parent, BaseNodeTable table) {
        try {
            if (parent == null || table == null) {
                close();
                return;
            }
            parentController = parent;
            nodeTable = table;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        okAction();
    }

}
