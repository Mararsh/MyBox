package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-4-25
 * @License Apache License Version 2.0
 */
public class ControlDataTreeTable extends ControlDataTreePages {

    protected BaseDataTreeController dataController;

    @FXML
    protected FlowPane namesPane;

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeTable = dataController.nodeTable;
            tagTable = dataController.tagTable;
            nodeTagsTable = dataController.nodeTagsTable;
            dataName = dataController.dataName;
            baseName = dataController.baseName + "_" + baseName;
            viewController = dataController.viewController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean isSourceNode(DataNode node) {
        return dataController != null && dataController.isSourceNode(node);
    }

    @Override
    public void loadNode(DataNode node) {
        loadNode(node, true);
    }

    public void loadNode(DataNode node, boolean refreshChildren) {
        if (task != null) {
            task.cancel();
        }
        if (refreshChildren) {
            resetTable();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode currentNode;

            @Override
            protected boolean handle() {
                currentNode = null;
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    long id;
                    if (node == null) {
                        id = RootID;
                    } else {
                        id = node.getNodeid();
                        if (node.getChildrenSize() == 0 && id != RootID) {
                            id = node.getParentid();
                        }
                    }
                    currentNode = nodeTable.readChain(this, conn, id);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return currentNode != null;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (currentNode != null) {
                    dataController.currentNode = currentNode;
                    if (currentNode.isRoot()) {
                        dataController.rootNode = currentNode.cloneAll();
                    }
                    if (refreshChildren) {
                        loadTableData();
                    } else {
                        writeNamesPane();
                    }
                }
            }

        };
        start(task);
    }

    @Override
    public List<DataNode> readPageData(FxTask currentTask, Connection conn) {
        if (nodeTable == null || dataController.currentNode == null) {
            return null;
        }
        List<DataNode> nodes = new ArrayList<>();
        String sql = "SELECT * FROM " + nodeTable.tableName
                + " WHERE parentid=? AND parentid<>nodeid "
                + " ORDER BY " + nodeTable.orderColumns
                + " OFFSET " + pagination.startRowOfCurrentPage
                + " ROWS FETCH NEXT " + pagination.pageSize + " ROWS ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, dataController.currentNode.getNodeid());
            try (ResultSet results = statement.executeQuery()) {
                String prefix = dataController.currentNode.getHierarchyNumber();
                if (prefix == null || prefix.isBlank()) {
                    prefix = "";
                } else {
                    prefix += ".";
                }
                long index = pagination.startRowOfCurrentPage;
                while (results != null && results.next()) {
                    if (currentTask == null || currentTask.isCancelled()) {
                        return null;
                    }
                    DataNode childNode = nodeTable.readData(results);
                    childNode.setIndex(index);
                    childNode.setHierarchyNumber(prefix + (++index));
                    childNode.setChildrenSize(nodeTable.childrenSize(conn, childNode.getNodeid()));
                    nodes.add(childNode);
                }
            } catch (Exception e) {
                if (currentTask != null) {
                    currentTask.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return nodes;
    }

    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        if (nodeTable == null || dataController.currentNode == null) {
            return -1;
        }
        long size = nodeTable.childrenSize(conn, dataController.currentNode.getNodeid());
        dataSizeLoaded = true;
        return size;
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        writeNamesPane();
        dataController.viewNode(dataController.currentNode);
    }

    public void writeNamesPane() {
        try {
            namesPane.getChildren().clear();
            if (nodeTable == null || dataController.currentNode == null) {
                return;
            }
            for (DataNode node : dataController.currentNode.getChainNodes()) {
                Hyperlink viewLink = new Hyperlink(node.getTitle());
                NodeStyleTools.setTooltip(viewLink, new Tooltip(message("View")));
                viewLink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dataController.showNode(node);
                    }
                });
                namesPane.getChildren().add(viewLink);

                Hyperlink unfoldLink = new Hyperlink(">");
                NodeStyleTools.setTooltip(unfoldLink, new Tooltip(message("Unfold")));
                unfoldLink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        loadNode(node);
                    }
                });
                namesPane.getChildren().add(unfoldLink);

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected long clearData(FxTask currentTask) {
        try {
            if (nodeTable == null || dataController.currentNode == null) {
                return -1;
            }
            return nodeTable.deleteDecentants(dataController.currentNode.getNodeid());
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -2;
        }
    }

    public void resetTable() {
        try {
            isSettingValues = true;
            tableData.clear();
            isSettingValues = false;
            namesPane.getChildren().clear();
            paginationController.reset();
            tableChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public DataNode selectedNode() {
        DataNode node = tableView.getSelectionModel().getSelectedItem();
        return node != null ? node : dataController.currentNode;
    }

    @Override
    public void clicked(Event event) {
        dataController.leftClicked(event, selectedNode());
    }

    @Override
    public void doubleClicked(Event event) {
        dataController.doubleClicked(event, selectedNode());
    }

    @Override
    public void rightClicked(Event event) {
        dataController.rightClicked(event, selectedNode());
    }

    public boolean refreshNode(DataNode node) {
        if (nodeTable == null
                || dataController.currentNode == null
                || !dataController.currentNode.equals(node)) {
            return false;
        }
        loadNode(dataController.currentNode);
        return true;
    }

    public void nodeSaved(DataNode parent, DataNode node) {
        try {
            if (nodeTable == null || dataController.currentNode == null) {
                return;
            }

            for (DataNode anode : dataController.currentNode.getChainNodes()) {
                if (anode.equals(node)) {
                    loadNode(dataController.currentNode, false);
                    return;
                }
            }

            for (int i = 0; i < tableData.size(); i++) {
                DataNode tnode = tableData.get(i);
                if (tnode.equals(node)) {
                    loadTableData();
                    return;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
