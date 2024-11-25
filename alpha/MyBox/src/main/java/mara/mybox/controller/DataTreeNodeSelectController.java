package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.HtmlStyles;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class DataTreeNodeSelectController extends BaseDataTreeViewController {

    protected DataTreeController treeController;
    protected DataNode sourceNode;

    @FXML
    protected Label nodeLabel;
    @FXML
    protected ControlWebView viewController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            viewController.initStyle = HtmlStyles.styleValue("Table");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(DataTreeController controller, DataNode node) {
        try {
            this.treeController = controller;
            if (!treeRunning()) {
                close();
                return;
            }
            this.nodeTable = treeController.nodeTable;
            this.dataName = treeController.dataName;

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

    public boolean treeRunning() {
        return treeController != null
                && treeController.getMyStage() != null
                && treeController.getMyStage().isShowing();
    }

    @Override
    public boolean isSourceNode(DataNode node) {
        return equalNode(node, sourceNode);
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null || item.getValue() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    html = nodeTable.nodeHtml(this, conn, controller, item.getValue(), 4);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                viewController.loadContents(html);
            }

        };
        start(task, thisPane);
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
