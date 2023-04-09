package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.db.data.TreeNode.NodeSeparater;
import mara.mybox.db.data.TreeNodeTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.cell.TreeTableHierachyCell;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class BaseTreeInfoController extends BaseController {

    protected static final int AutoExpandThreshold = 1000;
    protected TreeNode ignoreNode = null;
    protected boolean expandAll, nodeExecutable;
    protected final SimpleBooleanProperty loadedNotify;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String category;

    @FXML
    protected TreeTableView<TreeNode> infoTree;
    @FXML
    protected TreeTableColumn<TreeNode, String> hierarchyColumn, titleColumn, valueColumn;
    @FXML
    protected TreeTableColumn<TreeNode, Date> timeColumn;
    @FXML
    protected Label titleLabel;

    public BaseTreeInfoController() {
        loadedNotify = new SimpleBooleanProperty(false);
    }

    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();

            if (okButton != null) {
                okButton.disableProperty().bind(infoTree.getSelectionModel().selectedItemProperty().isNull());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTree() {
        try {
            hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
            hierarchyColumn.setCellFactory(new TreeTableHierachyCell());
            titleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new TreeTableTextTrimCell());
            valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            valueColumn.setCellFactory(new TreeTableTextTrimCell());
            timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TreeTableDateCell());

            infoTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            infoTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<TreeNode> item = selected();
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    /*
        tree
     */
    public void loadTree() {
        loadTree(null);
    }

    public synchronized void loadTree(TreeNode selectNode) {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            private boolean expand;
            private TreeItem<TreeNode> rootItem;

            @Override

            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    TreeNode rootNode = root(conn);
                    rootItem = new TreeItem(rootNode);
                    ignoreNode = getIgnoreNode();
                    int size = categorySize(conn);
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
                infoTree.setRoot(rootItem);
                rootItem.setExpanded(true);
                if (selectNode != null) {
                    TreeItem<TreeNode> selecItem = find(selectNode);
                    if (selecItem != null) {
                        select(selecItem);
                    } else {
                        select(rootItem);
                    }
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                notifyLoaded();
            }

        };
        start(task);
    }

    public void itemSelected(TreeItem<TreeNode> item) {
    }

    protected void expandChildren(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
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
                    try (Connection conn = DerbyBase.getConnection()) {
                        expandChildren(conn, item);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    infoTree.refresh();
                }
            };
            start(task);
        }
    }

    protected void expandChildren(Connection conn, TreeItem<TreeNode> item) {
        if (conn == null || item == null) {
            return;
        }
        item.getChildren().clear();
        TreeNode node = item.getValue();
        if (node == null) {
            return;
        }
        ignoreNode = getIgnoreNode();
        List<TreeNode> children = children(conn, node);
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                TreeNode child = children.get(i);
                if (ignoreNode != null && equal(child, ignoreNode)) {
                    continue;
                }
                TreeItem<TreeNode> childNode = new TreeItem(child);
                expandChildren(conn, childNode);
                childNode.setExpanded(true);
                item.getChildren().add(childNode);
            }
        }
        item.setExpanded(true);
    }

    protected void loadChildren(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
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
                    try (Connection conn = DerbyBase.getConnection()) {
                        loadChildren(conn, item);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    infoTree.refresh();
                }
            };
            start(task);

        }
    }

    protected void loadChildren(Connection conn, TreeItem<TreeNode> item) {
        if (conn == null || item == null) {
            return;
        }
        item.getChildren().clear();
        TreeNode node = item.getValue();
        if (node == null) {
            return;
        }
        ignoreNode = getIgnoreNode();
        List<TreeNode> children = children(conn, node);
        if (children != null) {
            for (TreeNode child : children) {
                if (ignoreNode != null && equal(child, ignoreNode)) {
                    continue;
                }
                TreeItem<TreeNode> childItem = new TreeItem(child);
                item.getChildren().add(childItem);
                childItem.setExpanded(false);
                if (!childrenEmpty(conn, child)) {
                    childItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !childItem.isLeaf() && !loaded(childItem)) {
                                    loadChildren(childItem);
                                }
                            });
                    TreeItem<TreeNode> dummyItem = new TreeItem(dummy());
                    childItem.getChildren().add(dummyItem);
                }
            }
        }
        item.setExpanded(true);
    }

    protected void addNewNode(TreeItem<TreeNode> item, TreeNode node, boolean select) {
        if (item == null || node == null) {
            return;
        }
        TreeItem<TreeNode> child = new TreeItem(node);
        item.getChildren().add(child);
        child.setExpanded(false);
        if (select) {
            select(item);
        }
    }

    protected boolean loaded(TreeItem<TreeNode> item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        try {
            TreeItem<TreeNode> child = (TreeItem<TreeNode>) (item.getChildren().get(0));
            return isDummy(child.getValue());
        } catch (Exception e) {
            return true;
        }
    }

    public void notifyLoaded() {
        loadedNotify.set(!loadedNotify.get());
    }


    /*
        values
     */
    public TreeItem<TreeNode> selected() {
        if (infoTree == null) {
            return null;
        }
        TreeItem<TreeNode> selecteItem = infoTree.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = infoTree.getRoot();
        }
        return selecteItem;
    }

    public TreeNode getIgnoreNode() {
        return ignoreNode;
    }

    public boolean equal(TreeNode node1, TreeNode node2) {
        return id(node1) == id(node2);
    }

    protected boolean isRoot(TreeNode node) {
        if (infoTree.getRoot() == null || node == null) {
            return false;
        }
        return equal(infoTree.getRoot().getValue(), node);
    }

    public String chainName(TreeItem<TreeNode> node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<TreeNode>> ancestor = ancestor(node);
        if (ancestor != null) {
            for (TreeItem<TreeNode> a : ancestor) {
                chainName += name(a.getValue()) + NodeSeparater;
            }
        }
        chainName += name(node.getValue());
        return chainName;
    }

    public TreeNode root(Connection conn) {
        return tableTreeNode.findAndCreateRoot(conn, category);
    }

    public int categorySize(Connection conn) {
        return tableTreeNode.categorySize(conn, category);
    }

    public boolean childrenEmpty(Connection conn, TreeNode node) {
        return tableTreeNode.childrenEmpty(conn, id(node));
    }

    public long id(TreeNode node) {
        if (node == null) {
            return -1;
        }
        return node.getNodeid();
    }

    public List<TreeNode> children(Connection conn, TreeNode node) {
        return tableTreeNode.children(conn, id(node));
    }

    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return tableTreeNode.ancestor(conn, id(node));
    }

    public List<TreeItem<TreeNode>> ancestor(TreeItem<TreeNode> node) {
        if (node == null) {
            return null;
        }
        List<TreeItem<TreeNode>> ancestor = null;
        TreeItem<TreeNode> parent = node.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public TreeNode dummy() {
        return new TreeNode();
    }

    public boolean isDummy(TreeNode node) {
        if (node == null) {
            return false;
        }
        return node.getTitle() != null;
    }

    public String name(TreeNode node) {
        if (node == null) {
            return null;
        }
        return node.getTitle();
    }

    /*
        actions
     */
    @FXML
    public void popFunctionsMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        popFunctionsMenu(event, selected());
    }

    public void popFunctionsMenu(MouseEvent event, TreeItem<TreeNode> node) {
        if (getMyWindow() == null) {
            return;
        }
        List<MenuItem> items = makeFunctionsMenu(node);
        items.add(new SeparatorMenuItem());

        MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
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
        if (event == null) {
            Robot r = new Robot();
            popMenu.show(infoTree, r.getMouseX() + 40, r.getMouseY() + 20);
        } else {
            popMenu.show(infoTree, event.getScreenX(), event.getScreenY());
        }
    }

    protected List<MenuItem> makeFunctionsMenu(TreeItem<TreeNode> item) {
        TreeItem<TreeNode> targetItem = item == null ? infoTree.getRoot() : item;

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(StringTools.menuSuffix(chainName(targetItem)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Manage"), StyleTools.getIconImageView("iconData.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            openManager();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addNode(targetItem);
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples();
        });
        items.add(menu);

        menu = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodes();
        });
        items.add(menu);

        menu = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNodes();
        });
        items.add(menu);

        return items;
    }

    protected void addNode(TreeItem<TreeNode> targetItem) {
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
        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
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
            start(task);
        }
    }

    @FXML
    protected void foldNodes() {
        fold(selected());
    }

    @FXML
    protected void unfoldNodes() {
        expandChildren(selected());
    }

    protected void fold(TreeItem<TreeNode> node) {
        if (node == null) {
            return;
        }
        List<TreeItem<TreeNode>> children = node.getChildren();
        if (children != null) {
            for (TreeItem<TreeNode> child : children) {
                fold(child);
                child.setExpanded(false);
            }
        }
        node.setExpanded(false);
    }

    @FXML
    protected void importExamples() {
        TreeNodeImportController controller
                = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setCaller(this);
        controller.importExamples();
    }

    public void select(TreeItem<TreeNode> nodeitem) {
        if (infoTree == null || nodeitem == null) {
            return;
        }
        isSettingValues = true;
        infoTree.getSelectionModel().select(nodeitem);
        isSettingValues = false;
        infoTree.scrollTo(infoTree.getRow(nodeitem));
        itemSelected(nodeitem);
    }

    public TreeItem<TreeNode> find(TreeNode node) {
        if (infoTree == null || node == null) {
            return null;
        }
        return find(infoTree.getRoot(), node);
    }

    public TreeItem<TreeNode> find(TreeItem<TreeNode> item, TreeNode node) {
        if (item == null || node == null) {
            return null;
        }
        if (equal(node, item.getValue())) {
            return item;
        }
        List<TreeItem<TreeNode>> children = item.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<TreeNode> child : children) {
            TreeItem<TreeNode> find = find(child, node);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

    protected void doubleClicked(TreeItem<TreeNode> item) {
    }

    protected void nodeAdded(TreeNode parent, TreeNode newNode) {
    }

    public TreeNode createNode(TreeNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        TreeNode newNode = new TreeNode(targetNode, name);
        newNode = tableTreeNode.insertData(newNode);
        return newNode;
    }

    public TreeManageController openManager() {
        if (category == null) {
            return null;
        }
        switch (category) {
            case TreeNode.WebFavorite:
                return WebFavoritesController.oneOpen();
            case TreeNode.Notebook:
                return NotesController.oneOpen();
            case TreeNode.JShellCode:
                return JShellController.open("");
            case TreeNode.SQL:
                return DatabaseSqlController.open(false);
            case TreeNode.JavaScript:
                return JavaScriptController.open("");
            case TreeNode.InformationInTree:
                return TreeManageController.oneOpen();
            case TreeNode.JEXLCode:
                return JexlController.open("", "", "");
            case TreeNode.RowFilter:
                return RowFilterController.open();
            case TreeNode.MathFunction:
                return MathFunctionController.open();

        }
        return null;
    }

    @FXML
    public void infoTree() {
        infoTree(selected());
    }

    public void infoTree(TreeItem<TreeNode> node) {
        if (node == null) {
            return;
        }
        TreeNode nodeValue = node.getValue();
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
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('TreeNode', this.checked);\">")
                            .append(message("Unfold")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('SerialNumber', this.checked);\">")
                            .append(message("HierarchyNumber")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('NodeTag', this.checked);\">")
                            .append(message("Tags")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('nodeValue', this.checked);\">")
                            .append(message("Values")).append("</INPUT>\n")
                            .append("</DIV>\n")
                            .append("<HR>\n");
                    try (Connection conn = DerbyBase.getConnection()) {
                        treeView(conn, nodeValue, 4, "", s);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    s.append("\n<HR>\n<TreeNode style=\"font-size:0.8em\">* ").append(message("HtmlEditableComments")).append("</P>\n");
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    WebAddressController c = WebBrowserController.openHtml(
                            HtmlWriteTools.html(chainName(node), HtmlStyles.styleValue("Default"), s.toString()), true);
                }
            };
            start(task);
        }
    }

    protected void treeView(Connection conn, TreeNode node, int indent, String serialNumber, StringBuilder s) {
        try {
            if (conn == null || node == null) {
                return;
            }
            List<TreeNode> children = children(conn, node);
            String indentNode = " ".repeat(indent);
            String spaceNode = "&nbsp;".repeat(indent);
            String nodePageid = "item" + node.getNodeid();
            String nodeName = node.getTitle();
            String displayName = "<SPAN class=\"SerialNumber\">" + serialNumber + "&nbsp;&nbsp;</SPAN>" + nodeName;
            if (children != null && !children.isEmpty()) {
                displayName = "<a href=\"javascript:nodeClicked('" + nodePageid + "')\">" + displayName + "</a>";
            }
            s.append(indentNode).append("<DIV style=\"padding: 2px;\">").append(spaceNode)
                    .append(displayName).append("\n");
            List<TreeNodeTag> tags = tableTreeNodeTag.nodeTags(conn, node.getNodeid());
            if (tags != null && !tags.isEmpty()) {
                String indentTag = " ".repeat(indent + 8);
                String spaceTag = "&nbsp;".repeat(2);
                s.append(indentTag).append("<SPAN class=\"NodeTag\">\n");
                for (TreeNodeTag nodeTag : tags) {
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    s.append(indentTag).append(spaceTag)
                            .append("<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: ")
                            .append(FxColorTools.color2rgb(color))
                            .append("; color: ").append(FxColorTools.color2rgb(FxColorTools.foreColor(color))).append(";\">")
                            .append(nodeTag.getTag().getTag()).append("</SPAN>\n");
                }
                s.append(indentTag).append("</SPAN>\n");
            }
            s.append(indentNode).append("</DIV>\n");
            String nodeValue = node.getValue();
            String moreValue = node.getMore();
            if (nodeValue != null && !nodeValue.isBlank()) {
                s.append(indentNode).append("<DIV class=\"nodeValue\">")
                        .append("<DIV style=\"padding: 0 0 0 ").append((indent + 4) * 6).append("px;\">")
                        .append("<DIV class=\"valueBox\">\n");
                String nodeDisplay;
                if (category.equals(TreeNode.WebFavorite)) {
                    nodeDisplay = "<A href=\"" + nodeValue + "\">";
                    if (moreValue != null && !moreValue.isBlank()) {
                        try {
                            nodeDisplay += "<IMG src=\"" + new File(node.getMore()).toURI().toString() + "\" width=40/>";
                        } catch (Exception e) {
                        }
                    }
                    nodeDisplay += nodeValue + "</A>";
                } else if (category.equals(TreeNode.Notebook)) {
                    nodeDisplay = nodeValue;
                } else {
                    nodeDisplay = HtmlWriteTools.stringToHtml(nodeValue);
                }
                s.append(indentNode).append(nodeDisplay).append("\n");
                s.append(indentNode).append("</DIV></DIV></DIV>\n");
            }
            if (moreValue != null && !moreValue.isBlank() && !category.equals(TreeNode.WebFavorite)) {
                s.append(indentNode).append("<DIV class=\"nodeValue\">")
                        .append("<DIV style=\"padding: 0 0 0 ").append((indent + 4) * 6).append("px;\">")
                        .append("<DIV class=\"valueBox\">\n");
                s.append(indentNode).append(HtmlWriteTools.stringToHtml(moreValue)).append("\n");
                s.append(indentNode).append("</DIV></DIV></DIV>\n");
            }
            if (children != null && !children.isEmpty()) {
                s.append(indentNode).append("<DIV class=\"TreeNode\" id='").append(nodePageid).append("'>\n");
                for (int i = 0; i < children.size(); i++) {
                    TreeNode child = children.get(i);
                    String ps = serialNumber == null || serialNumber.isBlank() ? "" : serialNumber + ".";
                    treeView(conn, child, indent + 4, ps + (i + 1), s);
                }
                s.append(indentNode).append("</DIV>\n");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree();
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

}
