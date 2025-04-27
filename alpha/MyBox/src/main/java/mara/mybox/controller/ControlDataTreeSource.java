package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-12-2
 * @License Apache License Version 2.0
 */
public class ControlDataTreeSource extends BaseDataTreeController {

    @FXML
    protected Label topLabel;

    public void setParameters(BaseDataTreeController parent, DataNode node) {
        try {
            initDataTree(parent.nodeTable, node);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setLabel(String label) {
        topLabel.setText(label);
    }

}
