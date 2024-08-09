package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.db.data.TreeNode.TitleSeparater;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.cell.TreeTableIDCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlDataTreeView extends BaseTreeTableViewController<TreeNode> {

    protected BaseDataTreeController dataController;
    protected static final int AutoExpandThreshold = 500;
    protected boolean expandAll;
    protected TableTree treeTable;
    protected TableTreeTag treeTagTable;
    protected BaseTable dataTable;

    @FXML
    protected TreeTableColumn<TreeNode, Long> idColumn;
    @FXML
    protected TreeTableColumn<TreeNode, Date> timeColumn;
    @FXML
    protected Label titleLabel;
    @FXML
    protected Button helpButton;


    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            if (okButton != null) {
                okButton.disableProperty().bind(treeView.getSelectionModel().selectedItemProperty().isNull());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (helpButton != null) {
                NodeStyleTools.setTooltip(helpButton, new Tooltip(message("AboutTreeInformation")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initTree() {
        try {
            super.initTree();

            if (idColumn != null) {
                idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("nodeid"));
                idColumn.setCellFactory(new TreeTableIDCell());
            }

            timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TreeTableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setParameters(BaseDataTreeController controller) {
        dataController = controller;
        dataTable = dataController.dataTable;
        treeTable = dataController.treeTable;
        treeTagTable = dataController.treeTagTable;
        parentController = dataController;
        baseName = dataTable.getTableName();
        baseTitle = baseName;

        loadTree();
    }

    /*
        tree
     */
    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(TreeNode selectNode) {
        if (task != null) {
            task.cancel();
        }
        clearTree();
        task = new FxSingletonTask<Void>(this) {
            private TreeItem<TreeNode> rootItem;

            @Override

            protected boolean handle() {
                rootItem = null;
                if (dataTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    TreeNode rootNode = TreeNode.createRoot(dataTable);
                    rootItem = new TreeItem(rootNode);
                    rootItem.setExpanded(true);
                    List<TreeNode> nodes = roots(conn);
                    if (nodes == null || nodes.isEmpty()) {
                        return true;
                    }
                    for (TreeNode node : nodes) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        TreeItem nodeItem = new TreeItem(node);
                        rootItem.getChildren().add(nodeItem);
                        int size = treeTable.decentantsSize(conn, node.getNodeid());
                        unfold(conn, nodeItem, size < AutoExpandThreshold);
                    }

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return task != null && !isCancelled();
            }

            @Override
            protected void whenSucceeded() {
                focusNode = selectNode;
                setRoot(rootItem);
            }

        };
        start(task, thisPane);
    }

    public TreeNode createNode(TreeNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        TreeNode newNode = new TreeNode(targetNode, name);
        newNode = treeTable.insertData(newNode);
        return newNode;
    }

    public void updateNode(TreeNode node) {
        TreeItem<TreeNode> treeItem = find(node);
        if (treeItem == null) {
            return;
        }
        treeItem.setValue(node);
        unfold(treeItem, false);
    }

    /*
        data
     */
    public TreeNode copyNode(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return null;
        }
        try {
            TreeNode newNode = sourceNode.copyIn(targetNode);
            newNode = treeTable.insertData(conn, newNode);
            if (newNode == null) {
                return null;
            }
            conn.commit();
            return newNode;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public boolean copyNodeAndDescendants(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        return copyDescendants(conn, sourceNode, copyNode(conn, sourceNode, targetNode));
    }

    public boolean copyDescendants(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return false;
        }
        try {
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            List<TreeNode> children = treeTable.children(conn, sourceid);
            if (children != null && !children.isEmpty()) {
                conn.setAutoCommit(true);
                for (TreeNode child : children) {
                    TreeNode newNode = TreeNode.create()
                            .setParentid(targetid)
                            .setDataTable(child.getDataTable())
                            .setTitle(child.getTitle());
                    treeTable.insertData(conn, newNode);
                    copyDescendants(conn, child, newNode);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }


    /*
        values
     */
    @Override
    public String title(TreeNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(TreeNode node) {
        return node == null ? null : node.texts();
    }

    @Override
    public boolean validNode(TreeNode node) {
        return node != null;
    }

    @Override
    public boolean equalItem(TreeItem<TreeNode> item1, TreeItem<TreeNode> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        return equalNode(item1.getValue(), item2.getValue());
    }

    @Override
    public boolean equalNode(TreeNode node1, TreeNode node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.getNodeid() == node2.getNodeid();
    }

    public List<TreeNode> roots(Connection conn) {
        return treeTable.findRoots(conn);
    }

    public boolean isRoot(TreeNode node) {
        if (treeView.getRoot() == null || node == null) {
            return false;
        }
        return equalNode(treeView.getRoot().getValue(), node);
    }

    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return treeTable.ancestor(conn, node.getNodeid());
    }

    public List<TreeItem<TreeNode>> ancestor(TreeItem<TreeNode> item) {
        if (item == null) {
            return null;
        }
        List<TreeItem<TreeNode>> ancestor = null;
        TreeItem<TreeNode> parent = item.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public String chainName(TreeItem<TreeNode> item) {
        if (item == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<TreeNode>> ancestor = ancestor(item);
        if (ancestor != null) {
            for (TreeItem<TreeNode> a : ancestor) {
                chainName += title(a.getValue()) + TitleSeparater;
            }
        }
        chainName += title(item.getValue());
        return chainName;
    }


    /*
        actions
     */
    @Override
    public List<MenuItem> viewMenuItems(TreeItem<TreeNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            popNode(item);
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        items.addAll(foldMenuItems(item));

        items.add(new SeparatorMenuItem());

//        if (nodesListCheck != null) {
//            menu = new MenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
//            menu.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    listChildren(item);
//                }
//            });
//            items.add(menu);
//
//            menu = new MenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
//            menu.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    listDescentants(item);
//                }
//            });
//            items.add(menu);
//
//        }
        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AboutTreeInformation"), StyleTools.getIconImageView("iconClaw.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            openHtml(HelpTools.aboutTreeInformation());
        });
        items.add(menu);

        return items;
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<TreeNode> item) {
        popNode(item);
    }

    @FXML
    @Override
    public void addAction() {
        addChild(selected());
    }

    public void addChild(TreeItem<TreeNode> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        TreeNode targetNode = targetItem.getValue();
        if (targetNode == null) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(targetItem);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("Add"), message("Node") + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(TitleSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + TitleSeparater + "\"");
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private TreeNode newNode;

            @Override
            protected boolean handle() {
                newNode = createNode(targetNode, name);
                return newNode != null;
            }

            @Override
            protected void whenSucceeded() {
                TreeItem<TreeNode> newItem = new TreeItem<>(newNode);
                targetItem.getChildren().add(newItem);
                targetItem.setExpanded(true);
                nodeAdded(targetNode, newNode);
                popSuccessful();
            }

        };
        start(task, thisPane);
    }

    protected void popNode(TreeItem<TreeNode> item) {
        if (item == null || dataController == null) {
            return;
        }
        dataController.popNode(item.getValue());
    }

    @Override
    public void unfold(TreeItem<TreeNode> item, boolean descendants) {
        if (item == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    unfold(conn, item, descendants);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                treeView.refresh();
            }

        };
        start(task, thisPane);
    }

    public void unfold(Connection conn, TreeItem<TreeNode> item, boolean descendants) {
        try {
            if (item == null || item.isLeaf()) {
                return;
            }
            if (isLoaded(item)) {
                for (TreeItem<TreeNode> childItem : item.getChildren()) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    if (descendants) {
                        unfold(conn, childItem, true);
                    } else {
                        childItem.setExpanded(false);
                    }
                }
            } else {
                item.getChildren().clear();
                TreeNode node = item.getValue();
                if (node == null) {
                    return;
                }
                List<TreeNode> children = treeTable.children(conn, node.getNodeid());
                if (children != null) {
                    for (TreeNode childNode : children) {
                        if (task == null || task.isCancelled()) {
                            return;
                        }
                        TreeItem<TreeNode> childItem = new TreeItem(childNode);
                        item.getChildren().add(childItem);
                        if (!treeTable.childrenEmpty(conn, childNode.getNodeid())) {
                            childItem.expandedProperty().addListener(
                                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                        if (newVal && !childItem.isLeaf() && !isLoaded(childItem)) {
                                            unfold(childItem, false);
                                        }
                                    });
                            TreeItem<TreeNode> dummyItem = new TreeItem(new TreeNode());
                            childItem.getChildren().add(dummyItem);
                        }
                        if (descendants) {
                            unfold(conn, childItem, true);
                        } else {
                            childItem.setExpanded(false);
                        }
                    }
                }
            }
            item.setExpanded(true);
        } catch (Exception e) {
            error = e.toString();
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree();
    }

    @FXML
    public void queryAction() {

    }

    @FXML
    public void aboutTreeInformation() {
        openHtml(HelpTools.aboutTreeInformation());
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

}
