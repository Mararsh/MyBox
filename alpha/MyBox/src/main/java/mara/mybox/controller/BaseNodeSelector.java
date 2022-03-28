package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
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
import mara.mybox.fxml.style.HtmlStyles;
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

    protected static final int AutoExpandThreshold = 1000;
    protected static final String nodeSeparator = " > ";

    protected P ignoreNode = null;
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

    protected abstract int totalCount(Connection conn);

    protected abstract int childrenCount(Connection conn, P node);

    protected abstract List<P> children(Connection conn, P node);

    protected abstract List<P> ancestor(Connection conn, P node);

    protected abstract P createNode(P targetNode, String name);

    protected abstract void delete(Connection conn, P node);

    protected abstract void clearTree(Connection conn, P node);

    protected abstract P rename(P node, String name);

    protected abstract void itemSelected(TreeItem<P> item);

    protected abstract void doubleClicked(TreeItem<P> item);

    protected abstract void copyNode(TreeItem<P> item, Boolean onlyContents);

    protected abstract void moveNode(TreeItem<P> item);

    protected abstract void editNode(TreeItem<P> item);

    protected abstract void exportNode(TreeItem<P> item);

    protected abstract void importAction();

    protected abstract void importExamples();

    protected abstract void nodeAdded(P parent, P newNode);

    protected abstract void nodeDeleted(P node);

    protected abstract void nodeRenamed(P node);

    protected abstract void nodeMoved(P parent, P node);

    protected abstract void treeView(Connection conn, P node, int indent, StringBuilder s);


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
                if (tips != null && !tips.isBlank()) {
                    NodeStyleTools.setTooltip(this, tips);
                } else {
                    NodeStyleTools.removeTooltip(this);
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
                    } else {
                        itemSelected(item);
                    }
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
            if (task != null) {
                task.cancel();
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
                        int size = totalCount(conn);
                        if (size < 1) {
                            return true;
                        }
                        expand = size <= AutoExpandThreshold;
                        if (expand) {
                            expandChildren(conn, rootItem);
                        } else {
                            loadChildren(conn, rootItem);
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
                    TreeItem<P> selecItem = find(selectNode);
                    if (selecItem != null) {
                        select(selecItem);
                    } else {
                        select(rootItem);
                    }
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

    protected List<MenuItem> makeNodeMenu(TreeItem<P> item) {
        TreeItem<P> targetItem = item == null ? treeView.getRoot() : item;
        boolean isRoot = targetItem == null || isRoot(targetItem.getValue());

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(chainName(targetItem));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Add"), StyleTools.getIconImage("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addNode(targetItem);
        });
        items.add(menu);

        if (manageMode) {
            menu = new MenuItem(message("Edit"), StyleTools.getIconImage("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                editNode(targetItem);
            });
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNode(targetItem);
            });
            items.add(menu);

            menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameNode(targetItem);
            });
            menu.setDisable(isRoot);
            items.add(menu);

            menu = new MenuItem(message("CopyNodeAndContents"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(targetItem, false);
            });
            menu.setDisable(isRoot);
            items.add(menu);

            menu = new MenuItem(message("CopyNodeContents"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(targetItem, true);
            });
            menu.setDisable(isRoot);
            items.add(menu);

            menu = new MenuItem(message("Move"), StyleTools.getIconImage("iconRef.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNode(targetItem);
            });
            menu.setDisable(isRoot);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Export"), StyleTools.getIconImage("iconExport.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                exportNode(targetItem);
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

    protected void addNode(TreeItem<P> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        P targetNode = targetItem.getValue();
        if (targetNode == null) {
            popError(message("SelectToHandle"));
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
        synchronized (this) {
            if (task != null) {
                task.cancel();
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
                    nodeAdded(targetNode, newNode);
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    protected void deleteNode(TreeItem<P> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        P node = targetItem.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
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
        synchronized (this) {
            if (task != null) {
                task.cancel();
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
                        nodeDeleted(rootItem.getValue());
                    } else {
                        targetItem.getChildren().clear();
                        if (targetItem.getParent() != null) {
                            targetItem.getParent().getChildren().remove(targetItem);
                        }
                        itemSelected(treeView.getSelectionModel().getSelectedItem());
                        nodeDeleted(targetItem.getValue());
                    }

                    popSuccessful();
                }

            };
            start(task);
        }
    }

    protected void renameNode(TreeItem<P> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        P nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(item);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("RenameNode"), name(nodeValue) + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(nodeSeparator)) {
            popError(message("NodeNameNotInclude") + " \"" + nodeSeparator + "\"");
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
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
                    item.setValue(updatedNode);
                    item.getParent().getChildren().set(item.getParent().getChildren().indexOf(item), item); // force item refreshed
                    nodeRenamed(updatedNode);
                    popSuccessful();
                }
            };
            start(task);
        }
    }

    protected void copyNode(TreeItem<P> item) {
        copyNode(item, false);
    }

    @FXML
    protected void foldNodes() {
        fold(currectSelected());
    }

    @FXML
    protected void unfoldNodes() {
        expandChildren(currectSelected());
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

    protected void loadChildren(TreeItem<P> item) {
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
                        loadChildren(conn, item);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.refresh();
                    select(item);
                }
            };
            start(task);

        }
    }

    protected void loadChildren(Connection conn, TreeItem<P> item) {
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
                TreeItem<P> childItem = new TreeItem(child);
                item.getChildren().add(childItem);
                childItem.setExpanded(false);
                if (childrenCount(conn, child) > 0) {
                    childItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !childItem.isLeaf() && !loaded(childItem)) {
                                    loadChildren(childItem);
                                }
                            });
                    TreeItem<P> dummyItem = new TreeItem(dummy());
                    childItem.getChildren().add(dummyItem);
                }
            }
        }
        item.setExpanded(true);
    }

    protected void addNewNode(TreeItem<P> item, P node) {
        if (item == null || node == null) {
            return;
        }
        TreeItem<P> child = new TreeItem(node);
        item.getChildren().add(child);
        child.setExpanded(false);
        select(item);
    }

    protected void updateChild(TreeItem<P> item, P node) {
        if (item == null || node == null) {
            return;
        }
        for (TreeItem<P> child : item.getChildren()) {
            P value = child.getValue();
            if (value != null && equal(node, value)) {
                child.setValue(node);
                return;
            }
        }
        loadChildren(item);
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
        treeView(currectSelected());
    }

    public void treeView(TreeItem<P> node) {
        if (node == null) {
            return;
        }
        P nodeValue = node.getValue();
        if (nodeValue == null) {
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
                            + "              nodes[i].style.display = '';\n"
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
                            .append("    <SPAN style=\"font-size:0.8em\">").append(message("HtmlEditableComments")).append("</SPANE><BR>\n")
                            .append("    <INPUT type=\"checkbox\" checked=true onclick=\"showClass('TreeNode', this.checked);\">")
                            .append(message("Unfold")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked=true onclick=\"showClass('NodeTag', this.checked);\">")
                            .append(message("Tags")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked=true onclick=\"showClass('nodeValue', this.checked);\">")
                            .append(message("Values")).append("</INPUT>\n")
                            .append("    <HR>\n")
                            .append("</DIV>\n");
                    try ( Connection conn = DerbyBase.getConnection()) {
                        treeView(conn, nodeValue, 4, s);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    WebAddressController c = WebBrowserController.oneLoad(
                            HtmlWriteTools.html(chainName(node), HtmlStyles.styleValue("Default"), s.toString()), true);
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

    public TreeItem<P> currectSelected() {
        TreeItem<P> selecteItem = treeView.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = treeView.getRoot();
        }
        return selecteItem;
    }

    public void select(P node) {
        select(treeView, node);
    }

    public void select(TreeView<P> treeView, P node) {
        if (treeView == null || node == null) {
            return;
        }
        select(find(treeView.getRoot(), node));
    }

    public void select(TreeItem<P> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        isSettingValues = true;
        treeView.getSelectionModel().select(nodeitem);
        isSettingValues = false;
        treeView.scrollTo(treeView.getRow(nodeitem));
        itemSelected(nodeitem);
    }

    public TreeItem<P> find(P node) {
        if (treeView == null || node == null) {
            return null;
        }
        return find(treeView.getRoot(), node);
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
//            selectedNotify = null;
//            changedNotify = null;
//            selectedItem = null;
//            changedItem = null;
            ignoreNode = null;

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
