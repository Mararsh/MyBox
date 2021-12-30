package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.Languages;

/**
 * @param <P>
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public abstract class BaseNodeSelector<P> extends BaseController {

    protected static final int AutoExpandThreshold = 100;

    protected P ignoreNode = null, selectedNode = null, changedNode = null;
    protected SimpleBooleanProperty selectedNotify, changedNotify;
    protected boolean expandAll, manageMode;
    protected String nodeSeparator = " > ";

    @FXML
    protected TreeView<P> treeView;
    @FXML
    protected Label titleLabel;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button importButton, deleteNodeButton, addNodeButton, moveDataNodeButton, copyNodeButton, renameNodeButton;

    public BaseNodeSelector() {
    }

    /*
        abstract methods
     */
    protected abstract String name(P node);

    protected abstract String display(P node);

    protected abstract String tooltip(P node);

    protected abstract long id(P node);

    protected abstract P dummy();

    protected abstract boolean isDummy(P node);

    protected abstract P root(Connection conn);

    protected abstract int size(Connection conn, P root);

    protected abstract List<P> children(Connection conn, P node);

    protected abstract List<P> ancestor(Connection conn, P node);

    protected abstract P createNode(P targetNode, String name);

    protected abstract void delete(Connection conn, P node);

    protected abstract void clearTree(Connection conn, P node);

    protected abstract P rename(P node, String name);

    protected abstract void copyNode(Boolean onlyContents);

    @FXML
    protected abstract void moveNode();

    /*
        methods may need changed
     */
    protected void itemSelected(TreeItem<P> item) {
        if (isSettingValues) {
            return;
        }
        loadNode(item != null ? item.getValue() : null);
        if (moveDataNodeButton != null) {
            boolean isRoot = item == null || isRoot(item.getValue());
            moveDataNodeButton.setDisable(isRoot);
            copyNodeButton.setDisable(isRoot);
            renameNodeButton.setDisable(isRoot);
        }
    }

    protected void nodeChanged(P node) {
        changedNode = node;
        changedNotify.set(!changedNotify.get());
    }

    protected void loadNode(P node) {
        selectedNode = node;
        selectedNotify.set(!selectedNotify.get());
    }

    @FXML
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void exportNode() {

    }

    /*
        Common methods may need not changed
     */
    @Override
    public void initValues() {
        try {
            super.initValues();
            selectedNotify = new SimpleBooleanProperty(false);
            changedNotify = new SimpleBooleanProperty(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();

            if (moveDataNodeButton != null) {
                moveDataNodeButton.setDisable(true);
                copyNodeButton.setDisable(true);
                renameNodeButton.setDisable(true);
            }
            if (okButton != null) {
                okButton.disableProperty().bind(treeView.getSelectionModel().selectedItemProperty().isNull());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTree() {
        treeView.setCellFactory(p -> new TreeCell<P>() {
            @Override
            public void updateItem(P item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(display(item));
                String tips = tooltip(item);
                if (tips != null) {
                    NodeStyleTools.setTooltip(this, tips);
                }
            }
        });
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                TreeItem<P> item = treeView.getSelectionModel().getSelectedItem();
                if (event.getButton() == MouseButton.SECONDARY) {
                    popNodeMenu(event, item);
                } else {
                    itemSelected(item);
                }
            }
        });
    }

    public void setParent(BaseController parent, boolean manageMode) {
        this.parentController = parent;
        if (parent != null) {
            this.baseName = parent.baseName;
        }
        this.ignoreNode = getIgnoreNode();
        this.manageMode = manageMode;
        if (!manageMode && buttonsPane != null) {
            thisPane.getChildren().remove(buttonsPane);
        }
    }

    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(P selectNode) {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private boolean expand;
                private TreeItem<P> rootItem;

                @Override

                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        P rootNode = root(conn);
                        rootItem = new TreeItem(rootNode);
                        ignoreNode = getIgnoreNode();
                        int size = size(conn, rootNode);
                        if (size < 1) {
                            return true;
                        }
                        expand = size <= AutoExpandThreshold;
                        if (expand) {
                            expandChildren(conn, rootItem);
                        } else {
                            List<P> nodes = children(conn, rootNode);
                            loadChildren(rootItem, nodes);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.setRoot(rootItem);
                    rootItem.setExpanded(true);
                    select(selectNode == null ? rootItem.getValue() : selectNode);
                }
            };
            start(task);
        }
    }

    public P getIgnoreNode() {
        return ignoreNode;
    }

    public boolean equal(P node1, P node2) {
        return id(node1) == id(node2);
    }

    protected boolean isRoot(P node) {
        if (treeView.getRoot() == null || node == null) {
            return false;
        }
        return equal(treeView.getRoot().getValue(), node);
    }

    public String chainName(Connection conn, P node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<P> ancestor = ancestor(conn, node);
        if (ancestor != null) {
            for (P a : ancestor) {
                chainName += name(a) + nodeSeparator;
            }
        }
        chainName += name(node);
        return chainName;
    }

    protected void popNodeMenu(MouseEvent event, TreeItem<P> selected) {
        if (isSettingValues) {
            return;
        }
        TreeItem<P> node = selected == null ? treeView.getRoot() : selected;

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(display(node.getValue()));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Add"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addNode();
        });
        items.add(menu);

        if (manageMode) {
            menu = new MenuItem(Languages.message("Delete"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNode();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("Move"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNode();
            });
            menu.setDisable(isRoot(node.getValue()));
            items.add(menu);

            menu = new MenuItem(Languages.message("CopyNodeAndContents"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(false);
            });
            menu.setDisable(isRoot(node.getValue()));
            items.add(menu);

            menu = new MenuItem(Languages.message("CopyNodeContents"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(true);
            });
            menu.setDisable(isRoot(node.getValue()));
            items.add(menu);

            menu = new MenuItem(Languages.message("Rename"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameNode();
            });
            menu.setDisable(isRoot(node.getValue()));
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("Export"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                exportNode();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(Languages.message("Unfold"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodes();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Fold"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNodes();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(treeView, event.getScreenX(), event.getScreenY());

    }

    @FXML
    protected void addNode() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<P> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                selectedItem = treeView.getRoot();
                if (selectedItem == null) {
                    return;
                }
            }
            TreeItem<P> targetItem = selectedItem;
            P targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
            }
            String chainName = chainName(targetItem);
            String name = PopTools.askValue(getBaseTitle(), chainName, Languages.message("Add"), Languages.message("Node") + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            if (name.contains(nodeSeparator)) {
                popError(Languages.message("NameShouldNotInclude") + " \"" + nodeSeparator + "\"");
                return;
            }
            task = new SingletonTask<Void>(this) {
                private P newNode;

                @Override
                protected boolean handle() {
                    newNode = createNode(targetNode, name);
                    return newNode != null;
                }

                @Override
                protected void whenSucceeded() {
                    TreeItem<P> newItem = new TreeItem<>(newNode);
                    targetItem.getChildren().add(newItem);
                    targetItem.setExpanded(true);
                    nodeChanged(targetNode);
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    @FXML
    protected void deleteNode() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<P> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                selectedItem = treeView.getRoot();
                if (selectedItem == null) {
                    return;
                }
            }
            TreeItem<P> targetItem = selectedItem;
            P node = targetItem.getValue();
            if (node == null) {
                return;
            }
            boolean isRoot = isRoot(node);
            if (isRoot) {
                if (!PopTools.askSure(getBaseTitle(), Languages.message("Delete"), Languages.message("SureDeleteAll"))) {
                    return;
                }
            } else {
                String chainName = chainName(targetItem);
                if (!PopTools.askSure(getBaseTitle(), chainName, Languages.message("Delete"))) {
                    return;
                }
            }
            task = new SingletonTask<Void>(this) {

                private TreeItem<P> rootItem;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (isRoot) {
                            clearTree(conn, node);
                            P rootNode = root(conn);
                            rootItem = new TreeItem(rootNode);
                        } else {
                            delete(conn, node);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isRoot) {
                        treeView.setRoot(rootItem);
                        rootItem.setExpanded(true);
                        itemSelected(rootItem);
                    } else {
                        targetItem.getChildren().clear();
                        if (targetItem.getParent() != null) {
                            targetItem.getParent().getChildren().remove(targetItem);
                        }
                        itemSelected(treeView.getSelectionModel().getSelectedItem());
                    }
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    @FXML
    protected void renameNode() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<P> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            TreeItem<P> node = selectedItem;
            P book = node.getValue();
            if (book == null || isRoot(book)) {
                return;
            }
            String chainName = chainName(node);
            String name = PopTools.askValue(getBaseTitle(), chainName, Languages.message("RenameNode"), name(book) + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            if (name.contains(nodeSeparator)) {
                popError(Languages.message("NodeNameNotInclude") + " \"" + nodeSeparator + "\"");
                return;
            }
            task = new SingletonTask<Void>(this) {
                private P updatedNode;

                @Override
                protected boolean handle() {
                    updatedNode = rename(book, name);
                    return updatedNode != null;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.refresh();
                    nodeChanged(updatedNode);
                    popSuccessful();
                }
            };
            start(task);
        }
    }

    @FXML
    protected void copyNode() {
        copyNode(false);
    }

    @FXML
    protected void foldNodes() {
        TreeItem<P> selecteItem = treeView.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = treeView.getRoot();
        }
        fold(selecteItem);
    }

    @FXML
    protected void unfoldNodes() {
        TreeItem<P> selecteItem = treeView.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = treeView.getRoot();
        }
        expandChildren(selecteItem);
    }

    protected void expandChildren(Connection conn, TreeItem<P> item) {
        if (conn == null || item == null) {
            return;
        }
        item.getChildren().clear();
        P node = item.getValue();
        if (node == null) {
            return;
        }
        ignoreNode = getIgnoreNode();
        List<P> children = children(conn, node);
        if (children != null) {
            for (P child : children) {
                if (ignoreNode != null && equal(child, ignoreNode)) {
                    continue;
                }
                TreeItem<P> childNode = new TreeItem(child);
                expandChildren(conn, childNode);
                childNode.setExpanded(true);
                item.getChildren().add(childNode);
            }
        }
        item.setExpanded(true);
    }

    protected void expandChildren(TreeItem<P> item) {
        if (item == null) {
            return;
        }
        P node = item.getValue();
        if (node == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        expandChildren(conn, item);
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
            start(task);
        }
    }

    protected void loadChildren(TreeItem<P> item) {
        if (item == null) {
            return;
        }
        item.getChildren().clear();
        P node = item.getValue();
        if (node == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private List<P> nodes;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        nodes = children(conn, node);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (nodes == null) {
                        return;
                    }
                    loadChildren(item, nodes);
                    treeView.refresh();
                }
            };
            start(task);
        }
    }

    protected void loadChildren(TreeItem<P> item, List<P> nodes) {
        if (item == null || nodes == null) {
            return;
        }
        P dummy = dummy();
        ignoreNode = getIgnoreNode();
        for (P node : nodes) {
            if (ignoreNode != null && equal(node, ignoreNode)) {
                continue;
            }
            TreeItem<P> child = new TreeItem(node);
            item.getChildren().add(child);
            child.setExpanded(false);
            child.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                        if (newVal && !child.isLeaf() && !loaded(child)) {
                            loadChildren(child);
                        }
                    });
            TreeItem<P> dummyItem = new TreeItem(dummy);
            child.getChildren().add(dummyItem);
        }
    }

    protected boolean loaded(TreeItem<P> item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        try {
            TreeItem<P> child = (TreeItem<P>) (item.getChildren().get(0));
            return isDummy(child.getValue());
        } catch (Exception e) {
            return true;
        }
    }

    protected void fold(TreeItem<P> node) {
        if (node == null) {
            return;
        }
        List<TreeItem<P>> children = node.getChildren();
        if (children != null) {
            for (TreeItem<P> child : children) {
                fold(child);
                child.setExpanded(false);
            }
        }
        node.setExpanded(false);
    }

    @FXML
    public void refreshAction() {
        loadTree();
    }

    public List<TreeItem<P>> ancestor(TreeItem<P> node) {
        if (node == null) {
            return null;
        }
        List<TreeItem<P>> ancestor = null;
        TreeItem<P> parent = node.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public String chainName(TreeItem<P> node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<P>> ancestor = ancestor(node);
        if (ancestor != null) {
            for (TreeItem<P> a : ancestor) {
                chainName += name(a.getValue()) + nodeSeparator;
            }
        }
        chainName += name(node.getValue());
        return chainName;
    }

    public void cloneTree(TreeView<P> sourceTreeView, TreeView<P> targetTreeView, P ignore) {
        if (sourceTreeView == null || targetTreeView == null) {
            return;
        }
        TreeItem<P> sourceRoot = sourceTreeView.getRoot();
        if (sourceRoot == null) {
            return;
        }
        TreeItem<P> targetRoot = new TreeItem(sourceRoot.getValue());
        targetTreeView.setRoot(targetRoot);
        targetRoot.setExpanded(sourceRoot.isExpanded());
        cloneNode(sourceRoot, targetRoot, ignore);
        TreeItem<P> selected = sourceTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            P sourceSelected = selected.getValue();
            if (ignore == null || !equal(ignore, sourceSelected)) {
                select(targetTreeView, sourceSelected);
            }
        }
    }

    public void cloneNode(TreeItem<P> sourceNode, TreeItem<P> targetNode, P ignore) {
        if (sourceNode == null || targetNode == null) {
            return;
        }
        List<TreeItem<P>> sourceChildren = sourceNode.getChildren();
        if (sourceChildren == null) {
            return;
        }
        for (TreeItem<P> sourceChild : sourceChildren) {
            if (ignore != null && equal(sourceChild.getValue(), ignore)) {
                continue;
            }
            TreeItem<P> targetChild = new TreeItem<>(sourceChild.getValue());
            targetChild.setExpanded(sourceChild.isExpanded());
            targetNode.getChildren().add(targetChild);
            cloneNode(sourceChild, targetChild, ignore);
        }
    }

    public void select(P node) {
        select(treeView, node);
    }

    public void select(TreeView<P> treeView, P node) {
        if (treeView == null || node == null) {
            return;
        }
        TreeItem<P> item = find(treeView.getRoot(), node);
        isSettingValues = true;
        treeView.getSelectionModel().select(item);
        isSettingValues = false;
        itemSelected(item);
    }

    public TreeItem<P> find(TreeItem<P> item, P node) {
        if (item == null || node == null) {
            return null;
        }
        if (equal(node, item.getValue())) {
            return item;
        }
        List<TreeItem<P>> children = item.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<P> child : children) {
            TreeItem<P> find = find(child, node);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

}
