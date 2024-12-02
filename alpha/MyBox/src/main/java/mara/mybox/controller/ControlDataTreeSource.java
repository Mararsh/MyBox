package mara.mybox.controller;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-12-2
 * @License Apache License Version 2.0
 */
public class ControlDataTreeSource extends BaseDataTreeViewController {

    @FXML
    protected Label topLabel;

    @Override
    public BooleanProperty getSelectedProperty(DataNode node) {
        return node.getSelected();
    }

    public void setParameters(DataTreeController parent, DataNode node) {
        try {
            initTree(parent.nodeTable);

            loadTree(node);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setLabel(String label) {
        topLabel.setText(label);
    }

}
