package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeController extends BaseController {

    protected ControlDataTreeView treeController;
    protected BaseDataTreeNodeController nodeController;

    protected BaseDataTable dataTable;
    protected TableDataNode dataNodeTable;
    protected TableDataTag dataTagTable;
    protected TableDataNodeTag dataNodeTagTable;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataTable == null) {
                return;
            }
            dataNodeTable = new TableDataNode(dataTable);
            dataTagTable = new TableDataTag(dataTable);
            dataNodeTagTable = new TableDataNodeTag(dataTable);

            nodeController.setParameters(this);

            treeController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void popNode(DataNode item) {
        if (item == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = item.toHtml();
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlTableController.open(null, html);
            }

        };
        start(task);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (nodeController == null) {
            return super.keyEventsFilter(event);
        }
        if (nodeController.thisPane.isFocused() || nodeController.thisPane.isFocusWithin()) {
            if (nodeController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return nodeController.keyEventsFilter(event); // pass event to editor
    }

    /*
        synchronize
     */
    public void nodeAdded(DataNode parent, DataNode newNode) {
        if (parent == null || newNode == null) {
            return;
        }

    }

    public void nodeRenamed(DataNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();

        if (nodeController.attributesController.parentNode != null
                && id == nodeController.attributesController.parentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(node);
        }
        if (nodeController.attributesController.currentNode != null
                && id == nodeController.attributesController.currentNode.getNodeid()) {
            nodeController.attributesController.renamed(node.getTitle());
        }
    }

    public void nodeDeleted(DataNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();

        nodeController.editNode(null);
    }

    public void nodeMoved(DataNode parent, DataNode node) {
        if (parent == null || node == null) {
            return;
        }
        long id = node.getNodeid();

        if (nodeController.attributesController.currentNode != null
                && id == nodeController.attributesController.currentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(parent);
        }
        if (nodeController.attributesController.parentNode != null
                && id == nodeController.attributesController.parentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(node);
        }
    }

    public void nodesMoved(DataNode parent, List<DataNode> nodes) {
        if (parent == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        treeController.loadTree(parent);
    }

    public void nodesCopied(DataNode parent) {
        treeController.loadTree(parent);
    }

    public void nodesDeleted() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
//                    tableController.loadedParent = tableTree.readData(conn, tableController.loadedParent);
                    nodeController.attributesController.currentNode
                            = dataNodeTable.readData(conn, nodeController.attributesController.currentNode);
                    nodeController.attributesController.parentNode
                            = dataNodeTable.readData(conn, nodeController.attributesController.parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                nodeController.editNode(nodeController.attributesController.currentNode);
//                treeController.loadTree(tableController.loadedParent);
            }
        };
        start(task);
    }

    public void nodeSaved() {
        if (nodeController.attributesController.currentNode == null) {
            return;
        }
        treeController.updateNode(nodeController.attributesController.currentNode);
        nodeController.resetStatus();
    }

    public void newNodeSaved() {
        if (nodeController.attributesController.currentNode == null) {
            return;
        }
        treeController.addNewNode(treeController.find(nodeController.attributesController.parentNode),
                nodeController.attributesController.currentNode, false);
        nodeController.resetStatus();
    }

    public void nodeChanged() {
        if (isSettingValues) {
            return;
        }
        String currentTitle = getTitle();
        if (nodeController.nodeChanged.get()) {
            if (!currentTitle.endsWith(" *")) {
                setTitle(currentTitle + " *");
            }
        } else {
            if (currentTitle.endsWith(" *")) {
                setTitle(currentTitle.substring(0, currentTitle.length() - 2));
            }
        }
    }

}
