package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

/**
 * @param <P>
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public abstract class BaseNodeSelector<P> extends BaseController {

    protected static final int AutoExpandThreshold = 100;
    protected static final String nodeSeparator = " > ";

    protected P ignoreNode = null, selectedNode = null, changedNode = null;
    protected SimpleBooleanProperty selectedNotify = new SimpleBooleanProperty(false),
            changedNotify = new SimpleBooleanProperty(false);
    protected boolean expandAll, manageMode;

    @FXML
    protected TreeView<P> treeView;
    @FXML
    protected Label titleLabel;

    /*
        abstract methods
     */
    protected abstract String name(P node);

    protected abstract long id(P node);

    protected abstract String display(P node);

    protected abstract String tooltip(P node);

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

    protected abstract void treeView(Connection conn, P node, int indent, StringBuilder s);

    /*
        methods may need changed
     */
    protected void itemSelected(TreeItem<P> item) {
        loadNode(item != null ? item.getValue() : null);
    }

    protected void doubleClicked(TreeItem<P> item) {
        okAction();
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
    protected void exportNode() {

    }

    @FXML
    protected void importAction() {

    }

    @FXML
    protected void importExamples() {

    }

    /*
        Common methods may need not changed
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();

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
                    popFunctionsMenu(event, item);
                } else {

                    if (event.getClickCount() > 1) {
                        doubleClicked(item);
                    }
                    itemSelected(item);
                }
            }
        });
    }

    public void setManager(BaseController parent, boolean manageMode) {
        this.parentController = parent;
        if (parent != null) {
            this.baseName = parent.baseName;
        }
        this.ignoreNode = getIgnoreNode();
        this.manageMode = manageMode;
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

    @FXML
    public void popFunctionsMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        popFunctionsMenu(event, currectSelected());
    }

    public void popFunctionsMenu(MouseEvent event, TreeItem<P> node) {
        List<MenuItem> items = makeNodeMenu(node);
        items.add(new SeparatorMenuItem());

        MenuItem menu = new MenuItem(message("PopupClose"));
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

    protected List<MenuItem> makeNodeMenu(TreeItem<P> selected) {
        TreeItem<P> node = selected == null ? treeView.getRoot() : selected;
        boolean isRoot = selected == null || isRoot(selected.getValue());

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(chainName(node));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Add"), StyleTools.getIconImage("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addNode();
        });
        items.add(menu);

        if (manageMode) {
            menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNode();
            });
            items.add(menu);

            menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameNode();
            });
            menu.setDisable(isRoot);
            items.add(menu);

            menu = new MenuItem(message("CopyNodeAndContents"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(false);
            });
            menu.setDisable(isRoot);
            items.add(menu);

            menu = new MenuItem(message("CopyNodeContents"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(true);
            });
            menu.setDisable(isRoot);
            items.add(menu);

            menu = new MenuItem(message("Move"), StyleTools.getIconImage("iconRef.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNode();
            });
            menu.setDisable(isRoot);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Export"), StyleTools.getIconImage("iconExport.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                exportNode();
            });
            items.add(menu);

            menu = new MenuItem(message("TreeView"), StyleTools.getIconImage("iconTree.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                treeView();
            });
            items.add(menu);

            menu = new MenuItem(message("Import"), StyleTools.getIconImage("iconImport.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                importAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Examples"), StyleTools.getIconImage("iconExamples.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                importExamples();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(message("Unfold"), StyleTools.getIconImage("iconPLus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodes();
        });
        items.add(menu);

        menu = new MenuItem(message("Fold"), StyleTools.getIconImage("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNodes();
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImage("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        return items;
    }

    @FXML
    protected void addNode() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<P> selectedItem = currectSelected();
            if (selectedItem == null) {
                return;
            }
            TreeItem<P> targetItem = selectedItem;
            P targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
            }
            String chainName = chainName(targetItem);
            String name = PopTools.askValue(getBaseTitle(), chainName, message("Add"), message("Node") + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            if (name.contains(nodeSeparator)) {
                popError(message("NameShouldNotInclude") + " \"" + nodeSeparator + "\"");
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
            TreeItem<P> selectedItem = currectSelected();
            if (selectedItem == null) {
                return;
            }
            TreeItem<P> targetItem = selectedItem;
            P node = targetItem.getValue();
            if (node == null) {
                return;
            }
            boolean isRoot = isRoot(node);
            if (isRoot) {
                if (!PopTools.askSure(this, getBaseTitle(), message("Delete"), message("SureDeleteAll"))) {
                    return;
                }
            } else {
                String chainName = chainName(targetItem);
                if (!PopTools.askSure(this, getBaseTitle(), chainName, message("Delete"))) {
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
            P nodeValue = node.getValue();
            if (nodeValue == null || isRoot(nodeValue)) {
                return;
            }
            String chainName = chainName(node);
            String name = PopTools.askValue(getBaseTitle(), chainName, message("RenameNode"), name(nodeValue) + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            if (name.contains(nodeSeparator)) {
                popError(message("NodeNameNotInclude") + " \"" + nodeSeparator + "\"");
                return;
            }
            task = new SingletonTask<Void>(this) {
                private P updatedNode;

                @Override
                protected boolean handle() {
                    updatedNode = rename(nodeValue, name);
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
        fold(currectSelected());
    }

    @FXML
    protected void unfoldNodes() {
        expandChildren(currectSelected());
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

    @FXML
    public void treeView() {
        TreeItem<P> item = currectSelected();
        if (item != null) {
            treeView(item.getValue());
        }
    }

    public void treeView(P node) {
        if (node == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private StringBuilder s;

                @Override
                protected boolean handle() {
                    s = new StringBuilder();
                    // https://www.jb51.net/article/116957.htm
                    s.append(" <script>\n"
                            + "    function nodeClicked(id) {\n"
                            + "      var obj = document.getElementById(id);\n"
                            + "      var objv = obj.style.display;\n"
                            + "      if (objv == 'none') {\n"
                            + "        obj.style.display = 'block';\n"
                            + "      } else {\n"
                            + "        obj.style.display = 'none';\n"
                            + "      }\n"
                            + "    }\n"
                            + "    function showClass(className, show) {\n"
                            + "      var nodes = document.getElementsByClassName(className);  　\n"
                            + "      if ( show) {\n"
                            + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                            + "              nodes[i].style.display = 'block';\n"
                            + "           }\n"
                            + "       } else {\n"
                            + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                            + "              nodes[i].style.display = 'none';\n"
                            + "           }\n"
                            + "       }\n"
                            + "    }\n"
                            + "  </script>\n\n");
                    s.append("<DIV>\n")
                            .append("<DIV>\n")
                            .append("<INPUT type=\"checkbox\" checked=true onclick=\"showClass('TreeNode', this.checked);\">")
                            .append(message("Unfold")).append("</INPUT>\n")
                            .append("<INPUT type=\"checkbox\" checked=true onclick=\"showClass('LeafTag', this.checked);\">")
                            .append(message("Tags")).append("</INPUT>\n")
                            .append("<INPUT type=\"checkbox\" checked=true onclick=\"showClass('valueBox', this.checked);\">")
                            .append(message("Values")).append("</INPUT>\n")
                            .append("</DIV>\n");
                    try ( Connection conn = DerbyBase.getConnection()) {
                        treeView(conn, node, 4, s);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    WebBrowserController.oneLoad(HtmlWriteTools.html(null, s.toString()), true);
                }
            };
            start(task);
        }
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

    public TreeItem<P> currectSelected() {
        TreeItem<P> selecteItem = treeView.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = treeView.getRoot();
        }
        return selecteItem;
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

    @Override
    public void cleanPane() {
        try {
            selectedNotify = null;
            changedNotify = null;
            ignoreNode = null;
            selectedNode = null;
            changedNode = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
