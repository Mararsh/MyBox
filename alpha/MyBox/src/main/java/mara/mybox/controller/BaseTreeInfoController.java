package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
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
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.cell.TreeTableHierachyCell;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class BaseTreeInfoController extends BaseController {

    protected static final int AutoExpandThreshold = 500;
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
            titleColumn.setCellFactory(new Callback<TreeTableColumn<TreeNode, String>, TreeTableCell<TreeNode, String>>() {
                @Override
                public TreeTableCell<TreeNode, String> call(TreeTableColumn<TreeNode, String> param) {

                    TreeTableCell<TreeNode, String> cell = new TreeTableCell<TreeNode, String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            setText(StringTools.abbreviate(item, 60));
                            setGraphic(null);
                            if (isSourceNode(getTableRow().getItem())) {
                                setStyle(NodeStyleTools.darkRedTextStyle());
                            } else {
                                setStyle(null);
                            }
                        }
                    };
                    return cell;
                }
            });
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
                        showItemMenu(item);
                    } else if (event.getClickCount() > 1) {
                        doubleClicked(item);
                    } else {
                        itemSelected(item);
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
        infoTree.setRoot(null);
        task = new SingletonTask<Void>(this) {
            private TreeItem<TreeNode> rootItem;

            @Override

            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    TreeNode rootNode = root(conn);
                    rootItem = new TreeItem(rootNode);
                    int size = tableTreeNode.categorySize(conn, category);
                    if (size < 1) {
                        return true;
                    }
                    rootItem.getChildren().add(new TreeItem(new TreeNode()));
                    unfold(conn, rootItem, size < AutoExpandThreshold);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                infoTree.setRoot(rootItem);
                rootItem.setExpanded(true);
                if (selectNode != null) {
                    select(find(selectNode));
                }
                notifyLoaded();
            }

        };
        start(task, infoTree);
    }

    protected void unfold(Connection conn, TreeItem<TreeNode> item, boolean descendants) {
        if (item == null || item.isLeaf()) {
            return;
        }
        if (loaded(item)) {
            for (TreeItem<TreeNode> childItem : item.getChildren()) {
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
            List<TreeNode> children = tableTreeNode.children(conn, node.getNodeid());
            if (children != null) {
                for (TreeNode childNode : children) {
                    TreeItem<TreeNode> childItem = new TreeItem(childNode);
                    item.getChildren().add(childItem);
                    if (!tableTreeNode.childrenEmpty(conn, childNode.getNodeid())) {
                        childItem.expandedProperty().addListener(
                                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                    if (newVal && !childItem.isLeaf() && !loaded(childItem)) {
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
    }

    protected boolean loaded(TreeItem<TreeNode> item) {
        try {
            return item.getChildren().get(0).getValue().getTitle() != null;
        } catch (Exception e) {
            return true;
        }
    }

    protected void unfold(TreeItem<TreeNode> item, boolean descendants) {
        if (item == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

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

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                infoTree.refresh();
            }
        };
        start(task, infoTree);
    }

    public void itemSelected(TreeItem<TreeNode> item) {
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

    public void notifyLoaded() {
        loadedNotify.set(!loadedNotify.get());
    }


    /*
        values
     */
    public TreeNode root(Connection conn) {
        return tableTreeNode.findAndCreateRoot(conn, category);
    }

    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return tableTreeNode.ancestor(conn, node.getNodeid());
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

    protected boolean isRoot(TreeNode node) {
        if (infoTree.getRoot() == null || node == null) {
            return false;
        }
        return equal(infoTree.getRoot().getValue(), node);
    }

    public String chainName(TreeItem<TreeNode> item) {
        if (item == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<TreeNode>> ancestor = ancestor(item);
        if (ancestor != null) {
            for (TreeItem<TreeNode> a : ancestor) {
                chainName += a.getValue().getTitle() + NodeSeparater;
            }
        }
        chainName += item.getValue().getTitle();
        return chainName;
    }

    public boolean isSourceNode(TreeNode node) {
        return false;
    }

    public boolean equal(TreeNode node1, TreeNode node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.getNodeid() == node2.getNodeid();
    }

    public boolean equalOrDescendant(TreeItem<TreeNode> item1, TreeItem<TreeNode> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        TreeNode node1 = item1.getValue();
        TreeNode node2 = item2.getValue();
        if (node1 == null || node2 == null) {
            return false;
        }
        if (node1.getNodeid() == node2.getNodeid()) {
            return true;
        }
        return equalOrDescendant(item1.getParent(), item2);
    }


    /*
        actions
     */
    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        TreeItem<TreeNode> treeItem = selected();
        List<MenuItem> items = makeFunctionsMenu(treeItem);

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);
        popEventMenu(event, items);
    }

    public void showItemMenu(TreeItem<TreeNode> item) {
        popNodeMenu(infoTree, makeFunctionsMenu(item));
    }

    public List<MenuItem> makeFunctionsMenu(TreeItem<TreeNode> item) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(StringTools.menuSuffix(item.getValue().getTitle()));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());
        items.addAll(functionItems(item));
        items.add(new SeparatorMenuItem());
        return items;
    }

    protected List<MenuItem> functionItems(TreeItem<TreeNode> item) {
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("UnfoldNode"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNode();
        });
        items.add(menu);

        menu = new MenuItem(message("UnfoldNodeAndDescendants"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodeAndDecendants();
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNode"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNode();
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNodeAndDescendants"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNodeAndDecendants();
        });
        items.add(menu);

        menu = new MenuItem(message("CopyValue"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, item.getValue().getValue());
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(message("CopyTitle"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, item.getValue().getTitle());
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addNode(item);
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples();
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
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

            @Override
            protected void finalAction() {
                super.finalAction();
            }

        };
        start(task, infoTree);
    }

    @FXML
    protected void foldNode() {
        fold(selected(), false);
    }

    @FXML
    protected void foldNodeAndDecendants() {
        fold(selected(), true);
    }

    protected void fold(TreeItem<TreeNode> item, boolean descendants) {
        if (item == null) {
            return;
        }
        if (descendants) {
            List<TreeItem<TreeNode>> children = item.getChildren();
            if (children != null) {
                for (TreeItem<TreeNode> child : children) {
                    fold(child, true);
                    child.setExpanded(false);
                }
            }
        }
        item.setExpanded(false);
    }

    @FXML
    protected void unfoldNode() {
        unfold(selected(), false);
    }

    @FXML
    protected void unfoldNodeAndDecendants() {
        unfold(selected(), true);
    }

    @FXML
    protected void importExamples() {
        TreeNodeImportController controller
                = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setCaller(this);
        controller.importExamples();
    }

    protected void afterImport() {
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

    public void updateParent(TreeNode node) {
        TreeItem<TreeNode> treeItem = find(node);
        if (treeItem == null) {
            return;
        }
        treeItem.setValue(node);
        unfold(treeItem, false);
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
        SingletonTask infoTask = new SingletonTask<Void>(this) {
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
        start(infoTask, false);
    }

    protected void treeView(Connection conn, TreeNode node, int indent, String serialNumber, StringBuilder s) {
        try {
            if (conn == null || node == null) {
                return;
            }
            List<TreeNode> children = tableTreeNode.children(conn, node.getNodeid());
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
