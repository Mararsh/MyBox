package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.cell.TreeTableIDCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class BaseDataTreeViewController extends BaseTreeTableViewController<DataNode> {

    protected static final int AutoExpandThreshold = 500;
    protected boolean expandAll;
    protected BaseNodeTable nodeTable;
    protected String dataName;

    @FXML
    protected TreeTableColumn<DataNode, Long> idColumn;
    @FXML
    protected TreeTableColumn<DataNode, Float> orderColumn;
    @FXML
    protected TreeTableColumn<DataNode, Date> timeColumn;
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

            orderColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("orderNumber"));

            timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TreeTableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    /*
        tree
     */
    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(DataNode selectNode) {
        if (task != null) {
            task.cancel();
        }
        clearTree();
        task = new FxSingletonTask<Void>(this) {

            private TreeItem<DataNode> rootItem;
            private int size;

            @Override
            protected boolean handle() {
                rootItem = null;
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    DataNode rootNode = nodeTable.getRoot(conn);
                    if (rootNode == null) {
                        return false;
                    }
                    rootItem = new TreeItem(rootNode);
                    rootItem.setExpanded(true);
                    rootItem.getChildren().add(new TreeItem(new DataNode()));
                    size = nodeTable.size(conn);
                    if (size > 1) {
                        unfold(this, conn, rootItem, size < AutoExpandThreshold);
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
                if (size <= 1) {
                    whenTreeEmpty();
                }
            }

        };
        start(task, thisPane);
    }

    public void whenTreeEmpty() {
    }

    public void updateNode(DataNode node) {
        TreeItem<DataNode> treeItem = find(node);
        if (treeItem == null) {
            return;
        }
        treeItem.setValue(node);
//        refreshItem(treeItem);
    }

    public void refreshItem(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        item.getChildren().clear();
        item.getChildren().add(new TreeItem(new DataNode()));
        unfold(item, false);
    }

    /*
        data
     */
    public DataNode copyNode(Connection conn, DataNode sourceNode, DataNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return null;
        }
        try {
            DataNode newNode = DataNode.createChild(targetNode);
            newNode = nodeTable.insertData(conn, newNode);
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

    public boolean copyNodeAndDescendants(Connection conn, DataNode sourceNode, DataNode targetNode) {
        return copyDescendants(conn, sourceNode, copyNode(conn, sourceNode, targetNode));
    }

    public boolean copyDescendants(Connection conn, DataNode sourceNode, DataNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return false;
        }
        try {
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            List<DataNode> children = nodeTable.children(conn, sourceid);
            if (children != null && !children.isEmpty()) {
                conn.setAutoCommit(true);
                for (DataNode child : children) {
                    DataNode newNode = DataNode.create()
                            .setParentid(targetid)
                            .setTitle(child.getTitle());
                    nodeTable.insertData(conn, newNode);
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
    public String title(DataNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(DataNode node) {
        return node == null ? null : node.toText();
    }

    @Override
    public boolean validNode(DataNode node) {
        return node != null;
    }

    @Override
    public boolean equalItem(TreeItem<DataNode> item1, TreeItem<DataNode> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        return equalNode(item1.getValue(), item2.getValue());
    }

    @Override
    public boolean equalNode(DataNode node1, DataNode node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.getNodeid() == node2.getNodeid();
    }

    public boolean isRoot(DataNode node) {
        if (treeView.getRoot() == null || node == null) {
            return false;
        }
        return node.getNodeid() == RootID;
    }

    public List<DataNode> ancestor(Connection conn, DataNode node) {
        return nodeTable.ancestor(conn, node.getNodeid());
    }

    public List<TreeItem<DataNode>> ancestor(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        List<TreeItem<DataNode>> ancestor = null;
        TreeItem<DataNode> parent = item.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public String chainName(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<DataNode>> ancestor = ancestor(item);
        if (ancestor != null) {
            for (TreeItem<DataNode> a : ancestor) {
                chainName += title(a.getValue()) + TitleSeparater;
            }
        }
        chainName += title(item.getValue());
        return chainName;
    }

    public String chainName(Connection conn, DataNode node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<DataNode> ancestor = ancestor(conn, node);
        if (ancestor != null) {
            for (DataNode a : ancestor) {
                chainName += a.getTitle() + TitleSeparater;
            }
        }
        chainName += node.getTitle();
        return chainName;
    }

    @Override
    public String hierarchyNumber(TreeItem<DataNode> item) {
        String num = super.hierarchyNumber(item);
        if (item != null && item.getValue() != null) {
            item.getValue().setHierarchyNumber(num);
        }
        return num;
    }

    public String shortDescription(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        return item.getValue().shortDescription(chainName(item));
    }

    /*
        actions
     */
    @Override
    public List<MenuItem> viewMenuItems(TreeItem<DataNode> item) {
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
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        popNode(item);
    }

    protected void popNode(TreeItem<DataNode> item) {
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
                try {
                    html = item.getValue().toHtml();
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

    // item should be unvisible in the treeView while doing this
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
                    try (ResultSet results = statement.executeQuery()) {
                        while (results != null && results.next()) {
                            if (task == null || task.isCancelled()) {
                                return;
                            }
                            DataNode childNode = nodeTable.readData(results);
                            TreeItem<DataNode> childItem = new TreeItem(childNode);
                            item.getChildren().add(childItem);
                            if (nodeTable.hasChildren(conn, childNode.getNodeid())) {
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

    @FXML
    public void popDataMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeDataPopWhenMouseHovering", true)) {
            showDataMenu(event);
        }
    }

    @FXML
    public void showDataMenu(Event event) {
        TreeItem<DataNode> item = selected();
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(dataMenuItems(item));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeDataPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeDataPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> dataMenuItems(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        return items;
    }

}
