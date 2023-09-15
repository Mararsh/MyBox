package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.NodeSeparater;
import mara.mybox.db.data.InfoNodeTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TreeTableDateCell;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class BaseInfoTreeController extends BaseTreeTableViewController<InfoNode> {

    protected static final int AutoExpandThreshold = 500;
    protected boolean expandAll, nodeExecutable;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String category;

    @FXML
    protected TreeTableColumn<InfoNode, Date> timeColumn;
    @FXML
    protected Label titleLabel;


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
    public void initTree() {
        try {
            super.initTree();

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

    public void loadTree(InfoNode selectNode) {
        if (task != null) {
            task.cancel();
        }
        clearTree();
        task = new SingletonCurrentTask<Void>(this) {
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
        values
     */
    @Override
    public String title(InfoNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(InfoNode node) {
        return node == null ? null : node.getValue();
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
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.getNodeid() == node2.getNodeid();
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
                chainName += a.getValue().getTitle() + NodeSeparater;
            }
        }
        chainName += item.getValue().getTitle();
        return chainName;
    }

    /*
        actions
     */
    @Override
    public List<MenuItem> functionItems(TreeItem<InfoNode> inItem) {
        List<MenuItem> items = viewItems(inItem);

        TreeItem<InfoNode> item = validItem(inItem);
        MenuItem menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(item);
        });
        items.add(menu);

        return items;
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
        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
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

    @Override
    public void unfold(TreeItem<InfoNode> item, boolean descendants) {
        if (item == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

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

    @FXML
    protected void importExamples() {
        TreeNodeImportController controller
                = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setCaller(this);
        controller.importExamples();
    }

    protected void afterImport() {
    }

    public TreeManageController openManager() {
        if (category == null) {
            return null;
        }
        switch (category) {
            case InfoNode.WebFavorite:
                return WebFavoritesController.oneOpen();
            case InfoNode.Notebook:
                return NotesController.oneOpen();
            case InfoNode.JShellCode:
                return JShellController.open("");
            case InfoNode.SQL:
                return DatabaseSqlController.open(false);
            case InfoNode.JavaScript:
                return JavaScriptController.loadScript("");
            case InfoNode.InformationInTree:
                return TreeManageController.oneOpen();
            case InfoNode.JEXLCode:
                return JexlController.open("", "", "");
            case InfoNode.RowFilter:
                return RowFilterController.open();
            case InfoNode.MathFunction:
                return MathFunctionController.open();
            case InfoNode.ImageMaterial:
                return ImageMaterialController.open();
            case InfoNode.Data2DDefinition:
                return Data2DDefinitionController.open();

        }
        return null;
    }

    @FXML
    public void infoTree() {
        infoTree(selected());
    }

    public void infoTree(TreeItem<InfoNode> node) {
        if (node == null) {
            return;
        }
        InfoNode nodeValue = node.getValue();
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

    protected void treeView(Connection conn, InfoNode node, int indent, String serialNumber, StringBuilder s) {
        try {
            if (conn == null || node == null) {
                return;
            }
            List<InfoNode> children = tableTreeNode.children(conn, node.getNodeid());
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
            List<InfoNodeTag> tags = tableTreeNodeTag.nodeTags(conn, node.getNodeid());
            if (tags != null && !tags.isEmpty()) {
                String indentTag = " ".repeat(indent + 8);
                String spaceTag = "&nbsp;".repeat(2);
                s.append(indentTag).append("<SPAN class=\"NodeTag\">\n");
                for (InfoNodeTag nodeTag : tags) {
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
                if (category.equals(InfoNode.WebFavorite)) {
                    nodeDisplay = "<A href=\"" + nodeValue + "\">";
                    if (moreValue != null && !moreValue.isBlank()) {
                        try {
                            nodeDisplay += "<IMG src=\"" + new File(node.getMore()).toURI().toString() + "\" width=40/>";
                        } catch (Exception e) {
                        }
                    }
                    nodeDisplay += nodeValue + "</A>";
                } else if (category.equals(InfoNode.Notebook)) {
                    nodeDisplay = nodeValue;
                } else {
                    nodeDisplay = HtmlWriteTools.stringToHtml(nodeValue);
                }
                s.append(indentNode).append(nodeDisplay).append("\n");
                s.append(indentNode).append("</DIV></DIV></DIV>\n");
            }
            if (moreValue != null && !moreValue.isBlank() && !category.equals(InfoNode.WebFavorite)) {
                s.append(indentNode).append("<DIV class=\"nodeValue\">")
                        .append("<DIV style=\"padding: 0 0 0 ").append((indent + 4) * 6).append("px;\">")
                        .append("<DIV class=\"valueBox\">\n");
                s.append(indentNode).append(HtmlWriteTools.stringToHtml(moreValue)).append("\n");
                s.append(indentNode).append("</DIV></DIV></DIV>\n");
            }
            if (children != null && !children.isEmpty()) {
                s.append(indentNode).append("<DIV class=\"TreeNode\" id='").append(nodePageid).append("'>\n");
                for (int i = 0; i < children.size(); i++) {
                    InfoNode child = children.get(i);
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
