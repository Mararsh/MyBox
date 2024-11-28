package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class DataTreeNodeSelectController extends BaseDataTreeViewController {

    protected DataNode sourceNode;

    @FXML
    protected Label nodeLabel;

    public void setParameters(BaseController parent, BaseNodeTable table, DataNode node) {
        try {
            parentController = parent;
            if (!parentRunning()) {
                close();
                return;
            }
            this.nodeTable = table;
            this.dataName = table.getDataName();

            baseName = baseName + "_" + dataName;

            sourceNode = node;
            if (sourceNode != null) {
                nodeLabel.setText(message("Node") + ": " + sourceNode.shortDescription());
            }

            initMore();

            loadTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMore() {

    }

    public boolean parentRunning() {
        return parentController != null
                && parentController.getMyStage() != null
                && parentController.getMyStage().isShowing();
    }

    @Override
    public boolean isSourceNode(DataNode node) {
        return equalNode(node, sourceNode);
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        okAction();
    }

    public boolean checkOptions(FxTask<Void> currentTask, Connection conn, DataNode targetNode) {
        if (sourceNode == null) {
            return true;
        }
        List<DataNode> sourceNodes = new ArrayList<>();
        sourceNodes.add(sourceNode);
        return checkOptions(currentTask, conn, sourceNodes, targetNode);
    }

    public boolean checkOptions(FxTask<Void> currentTask, Connection conn,
            List<DataNode> sourceNodes, DataNode targetNode) {
        if (sourceNodes == null || sourceNodes.isEmpty()) {
            displayError(message("SelectSourceNodes"));
            return false;
        }
        if (targetNode == null) {
            displayError(message("SelectTargetNode"));
            return false;
        }
        for (DataNode source : sourceNodes) {
            if (nodeTable.equalOrDescendant(currentTask, conn, targetNode, source)) {
                displayError(message("TreeTargetComments"));
                return false;
            }
        }
        return true;
    }

}
