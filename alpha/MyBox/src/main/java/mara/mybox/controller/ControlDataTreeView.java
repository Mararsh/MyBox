package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.cell.TreeTableIDCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class ControlDataTreeView extends BaseTreeTableViewController<DataNode> {

    protected BaseDataTreeController dataController;
    protected ControlWebView viewController;
    protected boolean expandAll;
    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;

    @FXML
    protected TreeTableColumn<DataNode, Long> idColumn, childrenColumn;
    @FXML
    protected TreeTableColumn<DataNode, String> hierarchyNumberColumn;
    @FXML
    protected TreeTableColumn<DataNode, Float> orderColumn;
    @FXML
    protected TreeTableColumn<DataNode, Date> timeColumn;

    /*
        tree
     */
    @Override
    public void initTree() {
        try {
            super.initTree();

            idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("nodeid"));
            idColumn.setCellFactory(new TreeTableIDCell());

            hierarchyNumberColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("hierarchyNumber"));

            childrenColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("childrenSize"));
            childrenColumn.setCellFactory(new TreeTableIDCell());

            orderColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("orderNumber"));

            timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TreeTableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeTable = dataController.nodeTable;
            tagTable = dataController.tagTable;
            nodeTagsTable = dataController.nodeTagsTable;
            dataName = dataController.dataName;
            baseName = dataController.baseName;

            if (dataController.viewController != null) {
                viewController = dataController.viewController;
            }

            refreshStyle();

            loadTree(dataController.currentNode);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTree(DataNode node) {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode rootNode;
            private TreeItem<DataNode> rootItem, selectItem;
            private long size;

            @Override
            protected boolean handle() {
                rootItem = null;
                selectItem = null;
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    rootNode = nodeTable.getRoot(conn);
                    if (rootNode == null) {
                        return false;
                    }
                    size = nodeTable.childrenSize(conn, RootID);
                    rootNode.setChildrenSize(size);
                    rootItem = new TreeItem(rootNode);
                    rootItem.setExpanded(true);

                    if (size > 0) {
                        rootItem.getChildren().add(new TreeItem(new DataNode()));
                        conn.setAutoCommit(true);
                        unfold(this, conn, rootItem, false);
                        if (node != null) {
                            selectItem = unfoldAncestors(this, conn, rootItem, node);
                        }
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return task != null && !isCancelled();
            }

            @Override
            protected void whenSucceeded() {
                dataController.rootNode = rootNode;
                setRoot(rootItem);
                if (selectItem != null) {
                    focusItem(selectItem);
                }
                if (size <= 1) {
                    whenTreeEmpty();
                }
            }

        };
        start(task, thisPane);
    }

    public void whenTreeEmpty() {
        if (AppVariables.isTesting) {
            return;
        }
        File file = nodeTable.exampleFile();
        if (file != null && PopTools.askSure(getTitle(), message("ImportExamples") + ": " + baseTitle)) {
            dataController.importExamples(null);
        }
    }

    @Override
    public void focusItem(TreeItem<DataNode> nodeitem) {
        super.focusItem(nodeitem);
        try {
            nodeitem.getValue().getSelected().set(true);
        } catch (Exception e) {
        }
    }

    public void resetTree() {
        try {
            isSettingValues = true;
            treeView.setRoot(null);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        values
     */
    @Override
    public String title(DataNode node) {
        return dataController.title(node);
    }

    @Override
    public String value(DataNode node) {
        return dataController.value(node);
    }

    @Override
    public boolean validNode(DataNode node) {
        return dataController.validNode(node);
    }

    @Override
    public void setHierarchyNumber(DataNode node, String hierarchyNumber) {
        dataController.setHierarchyNumber(node, hierarchyNumber);
    }

    @Override
    public boolean equalNode(DataNode node1, DataNode node2) {
        return dataController.equalNode(node1, node2);
    }

    @Override
    public boolean isSourceNode(DataNode node) {
        return dataController.isSourceNode(node);
    }

    @Override
    public BooleanProperty getSelectedProperty(DataNode node) {
        return node.getSelected();
    }

    public DataNode selectedNode() {
        return selectedValue();
    }

    /*
        operations
     */
    @Override
    public void itemClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            return;
        }
        dataController.currentNode = node;
        dataController.leftClicked(event, node);
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        dataController.doubleClicked(event, item.getValue());
    }

    @Override
    public void rightClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        dataController.rightClicked(event, item.getValue());
    }

    @Override
    public void unfold(TreeItem<DataNode> item, boolean descendants) {
        if (item == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private TreeItem<DataNode> parentItem, tempItem;
            private int itemIndex;

            @Override
            protected boolean handle() {
                itemIndex = -100;
                parentItem = item.getParent();
                if (parentItem != null) {
                    itemIndex = parentItem.getChildren().indexOf(item);
                    if (itemIndex < 0) {
                        return false;
                    }
                } else if (treeView.getRoot() != item) {
                    return false;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    tempItem = new TreeItem(item.getValue());
                    tempItem.getChildren().add(new TreeItem(new DataNode()));
                    conn.setAutoCommit(true);
                    unfold(this, conn, tempItem, descendants);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return tempItem != null;
            }

            @Override
            protected void whenSucceeded() {
                if (treeView.getRoot() == item) {
                    setRoot(tempItem);
                } else if (itemIndex >= 0) {
                    parentItem.getChildren().set(itemIndex, tempItem);
                    treeView.refresh();
                    focusItem(tempItem);
                }

            }

        };
        start(task, thisPane);
    }

    // item should be invisible in the treeView while doing this
    public void unfold(FxTask task, Connection conn, TreeItem<DataNode> item, boolean descendants) {
        try {
            if (item == null || item.isLeaf()) {
                return;
            }
            if (isLoaded(item)) {
                for (TreeItem<DataNode> childItem : item.getChildren()) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    if (descendants) {
                        unfold(task, conn, childItem, true);
                    } else {
                        childItem.setExpanded(false);
                    }
                }
            } else {
                item.getChildren().clear();
                DataNode node = item.getValue();
                if (node == null) {
                    return;
                }

                String sql = "SELECT " + BaseNodeTable.NodeFields + " FROM " + nodeTable.getTableName()
                        + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + nodeTable.getOrderColumns();
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, node.getNodeid());
                    String prefix = node.getHierarchyNumber();
                    if (prefix == null || prefix.isBlank()) {
                        prefix = "";
                    } else {
                        prefix += ".";
                    }
                    long index = 0;
                    try (ResultSet results = statement.executeQuery()) {
                        while (results != null && results.next()) {
                            if (task == null || task.isCancelled()) {
                                return;
                            }
                            DataNode childNode = nodeTable.readData(results);
                            childNode.setIndex(index);
                            childNode.setHierarchyNumber(prefix + (++index));
                            childNode.setChildrenSize(nodeTable.childrenSize(conn, childNode.getNodeid()));
                            TreeItem<DataNode> childItem = new TreeItem(childNode);
                            item.getChildren().add(childItem);
                            if (childNode.getChildrenSize() > 0) {
                                childItem.expandedProperty().addListener(
                                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                            if (newVal && !childItem.isLeaf() && !isLoaded(childItem)) {
                                                unfold(childItem, false);
                                            }
                                        });
                                TreeItem<DataNode> dummyItem = new TreeItem(new DataNode());
                                childItem.getChildren().add(dummyItem);
                            }
                            if (descendants) {
                                unfold(task, conn, childItem, true);
                            } else {
                                childItem.setExpanded(false);
                            }
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }

            }
            item.setExpanded(true);
        } catch (Exception e) {
            error = e.toString();
        }
    }

    public void unfoldNodeAncestors(DataNode node) {
        if (node == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        TreeItem<DataNode> rootItem = getRootItem();
        if (rootItem == null) {
            return;
        }
        treeView.setRoot(null);
        task = new FxSingletonTask<Void>(this) {

            private TreeItem<DataNode> item;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    item = unfoldAncestors(this, conn, rootItem, node);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                treeView.setRoot(rootItem);
                treeView.refresh();
                if (item != null) {
                    moveToItem(item);
                }
            }

        };
        start(task, thisPane);
    }

    public TreeItem<DataNode> unfoldAncestors(FxTask ptask, Connection conn,
            TreeItem<DataNode> rootItem, DataNode node) {
        try {
            if (conn == null || rootItem == null || node == null) {
                return null;
            }
            conn.setAutoCommit(true);
            node = nodeTable.readChain(ptask, conn, node);
            if (node == null) {
                return null;
            }
            List<DataNode> chainNodes = node.getChainNodes();
            if (chainNodes == null) {
                return null;
            }
            TreeItem<DataNode> parentItem = rootItem, chainItem = rootItem;
            for (DataNode chainNode : chainNodes) {
                unfold(ptask, conn, parentItem, false);
                chainItem = findChild(parentItem, chainNode);
                if (chainItem == null) {
                    return null;
                }
                parentItem.setExpanded(true);
                parentItem = chainItem;
            }
            node.setChildrenSize(nodeTable.childrenSize(conn, node.getNodeid()));
            if (chainItem.isLeaf() && node.getChildrenSize() > 0) {
                TreeItem<DataNode> unloadItem = chainItem;
                unloadItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !unloadItem.isLeaf() && !isLoaded(unloadItem)) {
                                unfold(unloadItem, false);
                            }
                        });
                TreeItem<DataNode> dummyItem = new TreeItem(new DataNode());
                unloadItem.getChildren().add(dummyItem);
            }
            return chainItem;
        } catch (Exception e) {
            error = e.toString();
            return null;
        }
    }

    @Override
    public boolean focusNode(DataNode node) {
        if (treeView == null || node == null) {
            return false;
        }
        unfoldNodeAncestors(node);
        return treeView.getRoot() != null;
    }

    public void nodeSaved(DataNode parent, DataNode node) {
        try {
            TreeItem<DataNode> nodeItem = find(node);
            if (nodeItem != null) {
                try {
                    nodeItem.setValue(node);
                    TreeItem<DataNode> currentParentItem = nodeItem.getParent();
                    if (currentParentItem.getValue().equals(parent)) {
                        return;
                    }
                    currentParentItem.getChildren().remove(nodeItem);
                    refreshNode(parent);
                    return;
                } catch (Exception e) {
                }
            }
            unfoldNodeAncestors(node);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void unfoldNode(DataNode node) {
        unfold(find(node), false);
    }

    public void refreshNode(DataNode node) {
        TreeItem<DataNode> item = find(node);
        if (item == null) {
            return;
        }
        item.getChildren().clear();
        item.getChildren().add(new TreeItem(new DataNode()));
        unfold(item, false);
    }

}
