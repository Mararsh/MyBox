package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javafx.beans.property.BooleanProperty;
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
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.cell.TreeTableIDCell;
import mara.mybox.fxml.style.HtmlStyles;
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
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;
    protected DataNode currentNode;

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
    @FXML
    protected ControlWebView viewController;


    /*
        init
     */
    @Override
    public void initValues() {
        try {
            super.initValues();

            if (viewController != null) {
                viewController.setParent(this);
                viewController.initStyle = HtmlStyles.styleValue("Table");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            loadCurrent(null);
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
    public void initDataTree(BaseNodeTable table, DataNode node) {
        try {
            if (table == null) {
                return;
            }
            initDataTree(table);

            loadTree(node);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initDataTree(BaseNodeTable table) {
        try {
            if (table == null) {
                return;
            }
            nodeTable = table;
            tagTable = new TableDataTag(nodeTable);
            nodeTagsTable = new TableDataNodeTag(nodeTable);

            dataName = nodeTable.getTableName();
            baseName = baseName + "_" + dataName;
            baseTitle = nodeTable.getTreeName();
            setTitle(baseTitle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(DataNode selectNode) {
        if (task != null) {
            task.cancel();
        }
        clearTree();
        task = new FxSingletonTask<Void>(this) {

            private TreeItem<DataNode> rootItem, selectItem;
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
                    size = nodeTable.size(conn);
                    if (size > 1) {
                        rootItem.getChildren().add(new TreeItem(new DataNode()));
                        conn.setAutoCommit(true);
                        unfold(this, conn, rootItem, size < AutoExpandThreshold);
                    }
                    if (selectNode != null) {
                        selectItem = unfoldAncestors(this, conn, rootItem, selectNode);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return task != null && !isCancelled();
            }

            @Override
            protected void whenSucceeded() {
                setRoot(rootItem);
                if (selectItem != null) {
                    focusItem(selectItem);
                }
                if (size <= 1) {
                    whenTreeEmpty();
                }
                afterTreeLoaded();
            }

        };
        start(task, thisPane);
    }

    public void whenTreeEmpty() {
    }

    public void afterTreeLoaded() {
    }

    @Override
    public void focusItem(TreeItem<DataNode> nodeitem) {
        super.focusItem(nodeitem);
        try {
            nodeitem.getValue().getSelected().set(true);
        } catch (Exception e) {
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
        return nodeTable.valuesString(node);
    }

    @Override
    public boolean validNode(DataNode node) {
        return node != null;
    }

    @Override
    public void setHierarchyNumber(DataNode node, String hierarchyNumber) {
        if (node != null) {
            node.setHierarchyNumber(hierarchyNumber);
        }
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

    public List<TreeItem<DataNode>> ancestorItems(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        List<TreeItem<DataNode>> ancestor = null;
        TreeItem<DataNode> parent = item.getParent();
        if (parent != null) {
            ancestor = ancestorItems(parent);
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
        List<TreeItem<DataNode>> ancestors = ancestorItems(item);
        if (ancestors != null) {
            for (TreeItem<DataNode> a : ancestors) {
                chainName += title(a.getValue()) + TitleSeparater;
            }
        }
        chainName += title(item.getValue());
        return chainName;
    }

    public String shortDescription(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        return item.getValue().shortDescription(chainName(item));
    }

    public boolean equalOrDescendant(FxTask<Void> currentTask, Connection conn,
            DataNode targetNode, List<DataNode> sourceNodes) {
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

    @Override
    public BooleanProperty getSelectedProperty(DataNode node) {
        return node.getSelected();
    }

    /*
        operations
     */
    @Override
    public List<MenuItem> viewMenuItems(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            popNode(item.getValue());
        });
        menu.setDisable(item == null);
        items.add(menu);

        if (valueColumn != null) {
            menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextClipboardTools.copyToSystemClipboard(this, value(item.getValue()));
            });
            menu.setDisable(item == null);
            items.add(menu);
        }

        menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        items.addAll(foldMenuItems(item));

        items.add(new SeparatorMenuItem());

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
    public void itemClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        loadCurrent(item.getValue());
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        popNode(item.getValue());
    }

    protected void loadCurrent(DataNode node) {
        nullCurrent();
        if (viewController == null || node == null) {
            return;
        }
        FxTask loadTask = new FxSingletonTask<Void>(this) {
            private String html;
            private DataNode savedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    html = nodeTable.valuesHtml(this, conn, controller, savedNode,
                            node.getHierarchyNumber(), 4);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                viewController.loadContent(html);
                currentNode = savedNode;
                if (editButton != null) {
                    editButton.setVisible(true);
                }
                if (goButton != null) {
                    goButton.setVisible(nodeTable.isNodeExecutable(savedNode));
                }
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(loadTask, rightPane);
    }

    protected void editNode(DataNode node) {
        if (node == null) {
            return;
        }
        DataTreeNodeEditorController.editNode(this, node);
    }

    protected void executeNode(DataNode node) {
        if (node == null) {
            return;
        }
        if (nodeTable instanceof TableNodeWebFavorite) {
            FxTask exTask = new FxTask<Void>(this) {
                private String address;

                @Override
                protected boolean handle() {
                    try (Connection conn = DerbyBase.getConnection()) {
                        DataNode savedNode = nodeTable.query(conn, node.getNodeid());
                        if (savedNode == null) {
                            return false;
                        }
                        address = savedNode.getStringValue("address");
                        return address != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    WebBrowserController.openAddress(address, true);
                }
            };
            start(exTask, false);
        } else {
            DataTreeNodeEditorController.executeNode(this, node);
        }
    }

    protected void addChild(DataNode node) {
        if (node == null) {
            return;
        }
        DataTreeNodeEditorController.addNode(this, node);
    }

    protected void popNode(DataNode node) {
        if (node == null) {
            return;
        }
        FxTask popTask = new FxSingletonTask<Void>(this) {
            private String html;
            private DataNode savedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    html = nodeTable.valuesHtml(this, conn, controller, savedNode,
                            node.getHierarchyNumber(), 4);
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
        start(popTask, false);
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

    public void unfoldAncestors(DataNode node) {
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
                    focusItem(item);
                }
            }

        };
        start(task, thisPane);
    }

    protected TreeItem<DataNode> unfoldAncestors(FxTask ptask, Connection conn,
            TreeItem<DataNode> rootItem, DataNode node) {
        try {
            if (conn == null || rootItem == null || node == null) {
                return null;
            }
            TreeItem<DataNode> parent, item;
            conn.setAutoCommit(true);
            List<DataNode> ancestors = nodeTable.ancestors(conn, node.getNodeid());
            parent = rootItem;
            parent.setExpanded(true);
            if (ancestors != null) {
                for (DataNode ancestor : ancestors) {
                    item = findChild(parent, ancestor);
                    if (item == null) {
                        item = new TreeItem(ancestor);
                        parent.getChildren().add(item);
                    }
                    unfold(ptask, conn, item, false);
                    parent = item;
                    parent.setExpanded(true);
                }
            }
            item = findChild(parent, node);
            if (item == null) {
                item = new TreeItem(node);
                parent.getChildren().add(item);
            }
            if (item.isLeaf() && nodeTable.hasChildren(conn, node.getNodeid())) {
                TreeItem<DataNode> unloadItem = item;
                unloadItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !unloadItem.isLeaf() && !isLoaded(unloadItem)) {
                                unfold(unloadItem, false);
                            }
                        });
                TreeItem<DataNode> dummyItem = new TreeItem(new DataNode());
                unloadItem.getChildren().add(dummyItem);
            }
            return item;
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
        unfoldAncestors(node);
        return treeView.getRoot() != null;
    }

    public void nodeSaved(DataNode parent, DataNode node) {
        try {
            checkCurrent(node);
            TreeItem<DataNode> nodeItem = find(node);
            if (nodeItem != null) {
                try {
                    node.setHierarchyNumber(nodeItem.getValue().getHierarchyNumber());
                    nodeItem.setValue(node);
                    TreeItem<DataNode> currentParentItem = nodeItem.getParent();
                    if (currentParentItem.getValue().equals(parent)) {
                        return;
                    }
                    currentParentItem.getChildren().remove(nodeItem);
                    TreeItem<DataNode> newParentItem = find(parent);
                    if (newParentItem != null) {
                        newParentItem.getChildren().add(nodeItem);
                        reorderChildlren(newParentItem);
                        return;
                    }
                } catch (Exception e) {
                }
            }
            unfoldAncestors(node);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshNode(DataNode node) {
        refreshItem(find(node));
    }

    public void refreshItem(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        item.getChildren().clear();
        item.getChildren().add(new TreeItem(new DataNode()));
        unfold(item, false);
    }

    public void reorderChildlren(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        List<TreeItem<DataNode>> children = item.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        List<TreeItem<DataNode>> items = new ArrayList<>();
        for (TreeItem<DataNode> child : children) {
            items.add(child);
        }
        item.getChildren().clear();
        Collections.sort(items, new Comparator<TreeItem<DataNode>>() {
            @Override
            public int compare(TreeItem<DataNode> v1, TreeItem<DataNode> v2) {
                DataNode node1 = v1.getValue();
                DataNode node2 = v2.getValue();
                if (node1 == null) {
                    return -1;
                }
                if (node2 == null) {
                    return 1;
                }
                float diff = node1.getOrderNumber() - node2.getOrderNumber();
                if (diff == 0) {
                    return 0;
                } else if (diff > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        for (TreeItem<DataNode> child : items) {
            item.getChildren().add(child);
        }
    }

    protected void nullCurrent() {
        currentNode = null;
        if (editButton != null) {
            editButton.setVisible(false);
        }
        if (goButton != null) {
            goButton.setVisible(false);
        }
        if (viewController != null) {
            viewController.loadContent("");
        }
    }

    protected void reloadCurrent() {
        loadCurrent(currentNode);
    }

    protected void checkCurrent(DataNode node) {
        if (currentNode != null && currentNode.equals(node)) {
            loadCurrent(node);
        }
    }

    /*
        action
     */
    @FXML
    @Override
    public void refreshAction() {
        loadTree();
    }

    @FXML
    public void queryAction() {

    }

    @FXML
    @Override
    public boolean popAction() {
        TreeItem<DataNode> item = selectedItem();
        if (item == null) {
            return false;
        }
        popNode(item.getValue());
        return true;
    }

    @FXML
    public void editAction() {
        if (currentNode == null) {
            TreeItem<DataNode> item = selectedItem();
            if (item == null) {
                return;
            }
            currentNode = item.getValue();
        }
        DataTreeNodeEditorController.editNode(this, currentNode);
    }

    @FXML
    @Override
    public void goAction() {
        if (currentNode == null) {
            TreeItem<DataNode> item = selectedItem();
            if (item == null) {
                return;
            }
            currentNode = item.getValue();
        }
        executeNode(currentNode);
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
    public void manageAction() {
        DataTreeController.open(null, false, nodeTable);
    }

    @FXML
    public void popDataMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeDataPopWhenMouseHovering", true)) {
            showDataMenu(event);
        }
    }

    @FXML
    public void showDataMenu(Event event) {
        TreeItem<DataNode> item = selectedItem();
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
