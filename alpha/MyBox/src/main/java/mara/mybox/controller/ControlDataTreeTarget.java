package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class ControlDataTreeTarget extends BaseDataTreeViewController {

    @FXML
    protected RadioButton beforeRadio, afterRadio, inRadio;

    public void setParameters(DataTreeController parent) {
        try {
            initTree(parent.nodeTable);

            loadTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        if (item.getParent() == null) {
            inRadio.setSelected(true);
        }
    }
}
