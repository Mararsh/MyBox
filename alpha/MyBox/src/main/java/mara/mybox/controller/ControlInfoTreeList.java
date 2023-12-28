package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.TitleSeparater;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeList extends BaseTreeTableViewController<InfoNode> {

    protected BaseInfoTreeController infoController;
    protected static final int AutoExpandThreshold = 500;
    protected boolean expandAll;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String category;

    @FXML
    protected TreeTableColumn<InfoNode, Long> idColumn;
    @FXML
    protected TreeTableColumn<InfoNode, Date> timeColumn;
    @FXML
    protected Label titleLabel;
    @FXML
    protected CheckBox nodesListCheck;
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
            }

            timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TreeTableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setParameters(BaseInfoTreeController parent) {
        infoController = parent;
        tableTreeNode = parent.tableTreeNode;
        tableTreeNodeTag = parent.tableTreeNodeTag;
        category = infoController.category;
        parentController = parent;
        baseName = parent.baseName + "_" + category;
        baseTitle = category;

        infoController.showNodesList(false);
        if (nodesListCheck != null) {
            nodesListCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    infoController.showNodesList(nodesListCheck.isSelected());
                }
            });
        }

        loadTree();
    }

    /*
        tree
     */
    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(InfoNode selectNode) {
        if (task != null) {
            task.cancel();
        }
        clearTree();
        task = new FxSingletonTask<Void>(this) {
            private TreeItem<InfoNode> rootItem;

            @Override

            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    InfoNode rootNode = root(conn);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    rootItem = new TreeItem(rootNode);
                    rootItem.setExpanded(true);
                    int size = tableTreeNode.categorySize(conn, category);
                    if (size < 1) {
                        return true;
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    rootItem.getChildren().add(new TreeItem(new InfoNode()));
                    unfold(conn, rootItem, size < AutoExpandThreshold);
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

    public InfoNode createNode(InfoNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        InfoNode newNode = new InfoNode(targetNode, name);
        newNode = tableTreeNode.insertData(newNode);
        return newNode;
    }

    public void updateNode(InfoNode node) {
        TreeItem<InfoNode> treeItem = find(node);
        if (treeItem == null) {
            return;
        }
        treeItem.setValue(node);
        unfold(treeItem, false);
    }

    /*
        data
     */
    public InfoNode copyNode(Connection conn, InfoNode sourceNode, InfoNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return null;
        }
        try {
            InfoNode newNode = sourceNode.copyIn(targetNode);
            newNode = tableTreeNode.insertData(conn, newNode);
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

    public boolean copyNodeAndDescendants(Connection conn, InfoNode sourceNode, InfoNode targetNode) {
        return copyDescendants(conn, sourceNode, copyNode(conn, sourceNode, targetNode));
    }

    public boolean copyDescendants(Connection conn, InfoNode sourceNode, InfoNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return false;
        }
        try {
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            List<InfoNode> children = tableTreeNode.children(conn, sourceid);
            if (children != null && !children.isEmpty()) {
                conn.setAutoCommit(true);
                for (InfoNode child : children) {
                    InfoNode newNode = InfoNode.create()
                            .setParentid(targetid)
                            .setCategory(category)
                            .setTitle(child.getTitle())
                            .setInfo(child.getInfo());
                    tableTreeNode.insertData(conn, newNode);
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
    public String title(InfoNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(InfoNode node) {
        return node == null ? null : node.getInfo();
    }

    @Override
    public boolean validNode(InfoNode node) {
        return node != null;
    }

    @Override
    public boolean equalItem(TreeItem<InfoNode> item1, TreeItem<InfoNode> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        return equalNode(item1.getValue(), item2.getValue());
    }

    @Override
    public boolean equalNode(InfoNode node1, InfoNode node2) {
        return InfoNode.equal(node1, node2);
    }

    public InfoNode root(Connection conn) {
        return tableTreeNode.findAndCreateRoot(conn, category);
    }

    public boolean isRoot(InfoNode node) {
        if (treeView.getRoot() == null || node == null) {
            return false;
        }
        return equalNode(treeView.getRoot().getValue(), node);
    }

    public List<InfoNode> ancestor(Connection conn, InfoNode node) {
        return tableTreeNode.ancestor(conn, node.getNodeid());
    }

    public List<TreeItem<InfoNode>> ancestor(TreeItem<InfoNode> item) {
        if (item == null) {
            return null;
        }
        List<TreeItem<InfoNode>> ancestor = null;
        TreeItem<InfoNode> parent = item.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public String chainName(TreeItem<InfoNode> item) {
        if (item == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<InfoNode>> ancestor = ancestor(item);
        if (ancestor != null) {
            for (TreeItem<InfoNode> a : ancestor) {
                chainName += title(a.getValue()) + TitleSeparater;
            }
        }
        chainName += title(item.getValue());
        return chainName;
    }


    /*
        actions
     */
    @FXML
    public void popViewMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeViewPopWhenMouseHovering", true)) {
            showViewMenu(event);
        }
    }

    @FXML
    public void showViewMenu(Event event) {
        TreeItem<InfoNode> item = selected();
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(viewMenuItems(item));

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AboutTreeInformation"), StyleTools.getIconImageView("iconClaw.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            openHtml(HelpTools.aboutTreeInformation());
        });
        items.add(menu);

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeViewPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeViewPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(TreeItem<InfoNode> item) {
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

        if (nodesListCheck != null) {
            menu = new MenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    listChildren(item);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    listDescentants(item);
                }
            });
            items.add(menu);

        }

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        return items;
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<InfoNode> item) {
        popNode(item);
    }

    @FXML
    @Override
    public void addAction() {
        addChild(selected());
    }

    public void addChild(TreeItem<InfoNode> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        InfoNode targetNode = targetItem.getValue();
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
            private InfoNode newNode;

            @Override
            protected boolean handle() {
                newNode = createNode(targetNode, name);
                return newNode != null;
            }

            @Override
            protected void whenSucceeded() {
                TreeItem<InfoNode> newItem = new TreeItem<>(newNode);
                targetItem.getChildren().add(newItem);
                targetItem.setExpanded(true);
                nodeAdded(targetNode, newNode);
                popSuccessful();
            }

        };
        start(task, thisPane);
    }

    protected void popNode(TreeItem<InfoNode> item) {
        if (item == null || infoController == null) {
            return;
        }
        infoController.popNode(item.getValue());
    }

    @Override
    public void unfold(TreeItem<InfoNode> item, boolean descendants) {
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

    public void unfold(Connection conn, TreeItem<InfoNode> item, boolean descendants) {
        try {
            if (item == null || item.isLeaf()) {
                return;
            }
            if (isLoaded(item)) {
                for (TreeItem<InfoNode> childItem : item.getChildren()) {
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
                InfoNode node = item.getValue();
                if (node == null) {
                    return;
                }
                List<InfoNode> children = tableTreeNode.children(conn, node.getNodeid());
                if (children != null) {
                    for (InfoNode childNode : children) {
                        if (task == null || task.isCancelled()) {
                            return;
                        }
                        TreeItem<InfoNode> childItem = new TreeItem(childNode);
                        item.getChildren().add(childItem);
                        if (!tableTreeNode.childrenEmpty(conn, childNode.getNodeid())) {
                            childItem.expandedProperty().addListener(
                                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                        if (newVal && !childItem.isLeaf() && !isLoaded(childItem)) {
                                            unfold(childItem, false);
                                        }
                                    });
                            TreeItem<InfoNode> dummyItem = new TreeItem(new InfoNode());
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

    protected void listChildren(TreeItem<InfoNode> item) {
        if (item == null || infoController == null) {
            return;
        }
        infoController.loadChildren(item.getValue());
    }

    protected void listDescentants(TreeItem<InfoNode> item) {
        if (item == null || infoController == null) {
            return;
        }
        infoController.loadDescendants(item.getValue());
    }

    @FXML
    protected void importExamples() {
        InfoTreeNodeImportController controller
                = (InfoTreeNodeImportController) childStage(Fxmls.InfoTreeNodeImportFxml);
        controller.setCaller(infoController);
        controller.importExamples();
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree();
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
