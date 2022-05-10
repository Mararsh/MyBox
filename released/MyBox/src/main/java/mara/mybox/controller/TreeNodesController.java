package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.TreeNodeTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNodesController extends BaseNodeSelector<TreeNode> {

    protected TreeManageController treeController;
    protected TreeNodesController caller;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String category;

    public TreeNodesController() {
    }

    public void setParameters(TreeManageController parent, boolean manageMode) {
        super.setManager(parent, manageMode);
        treeController = parent;
        tableTreeNode = parent.tableTreeNode;
        tableTreeNodeTag = parent.tableTreeNodeTag;
        category = treeController.category;
        baseTitle = category;
    }

    public void setCaller(TreeNodesController caller) {
        super.setManager(null, false);
        this.caller = caller;
        tableTreeNode = caller.tableTreeNode;
        tableTreeNodeTag = caller.tableTreeNodeTag;
        category = caller.category;
        cloneTree(caller.treeView, treeView, getIgnoreNode());
    }

    @Override
    public void listChildren(TreeItem<TreeNode> item) {
        if (item == null || caller != null || treeController == null) {
            return;
        }
        treeController.loadChildren(item.getValue());
    }

    @Override
    public void listDescentants(TreeItem<TreeNode> item) {
        if (item == null || caller != null || treeController == null) {
            return;
        }
        treeController.loadDescendants(item.getValue());
    }

    @Override
    protected void doubleClicked(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        if (caller != null) {
            okAction();
        } else if (manageMode && treeController != null) {
            editNode(item);
        }
    }

    @Override
    protected void nodeAdded(TreeNode parent, TreeNode newNode) {
        if (caller != null) {
            caller.addNewNode(caller.find(parent), newNode);
        }
        if (treeController != null) {
            treeController.nodeAdded(parent, newNode);
        }
    }

    @Override
    protected void nodeDeleted(TreeNode node) {
        if (treeController == null) {
            return;
        }
        treeController.nodeDeleted(node);
    }

    @Override
    protected void nodeRenamed(TreeNode node) {
        if (treeController == null) {
            return;
        }
        treeController.nodeRenamed(node);
    }

    @Override
    public void nodeMoved(TreeNode parent, TreeNode node) {
        if (treeController == null) {
            return;
        }
        treeController.nodeMoved(parent, node);
    }

    @Override
    public String display(TreeNode node) {
        return node.getTitle();
    }

    @Override
    public String tooltip(TreeNode node) {
        String v = node.getValue();
        if (v != null && !v.isBlank()) {
            if (v.length() > 300) {
                v = v.substring(0, 300);
            }
            return v;
        } else {
            return null;
        }
    }

    @Override
    protected String serialNumber(TreeNode node) {
        return node.getSerialNumber();
    }

    public TreeNode root() {
        return tableTreeNode.findAndCreateRoot(category);
    }

    @Override
    public TreeNode root(Connection conn) {
        return tableTreeNode.findAndCreateRoot(conn, category);
    }

    @Override
    public int categorySize(Connection conn) {
        return tableTreeNode.categorySize(conn, category);
    }

    @Override
    public boolean childrenEmpty(Connection conn, TreeNode node) {
        return tableTreeNode.childrenEmpty(conn, id(node));
    }

    @Override
    public long id(TreeNode node) {
        if (node == null) {
            return -1;
        }
        return node.getNodeid();
    }

    @Override
    public List<TreeNode> children(Connection conn, TreeNode node) {
        return tableTreeNode.children(conn, id(node));
    }

    @Override
    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return tableTreeNode.ancestor(conn, id(node));
    }

    @Override
    public TreeNode dummy() {
        return new TreeNode();
    }

    @Override
    public boolean isDummy(TreeNode node) {
        if (node == null) {
            return false;
        }
        return node.getTitle() != null;
    }

    @Override
    public TreeNode createNode(TreeNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        TreeNode newNode = new TreeNode(targetNode, name, null);
        newNode = tableTreeNode.insertData(newNode);
        return newNode;
    }

    @Override
    public String name(TreeNode node) {
        if (node == null) {
            return null;
        }
        return node.getTitle();
    }

    @Override
    protected void exportNode(TreeItem<TreeNode> item) {
        TreeNodeExportController exportController
                = (TreeNodeExportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeExportFxml);
        exportController.setParamters(treeController, item);
    }

    @Override
    protected void clearTree(Connection conn, TreeNode node) {
        if (node == null || conn == null) {
            return;
        }
        tableTreeNode.deleteChildren(conn, node.getNodeid());
    }

    @Override
    protected TreeNode rename(TreeNode node, String name) {
        if (node == null || name == null) {
            return null;
        }
        node.setTitle(name);
        return tableTreeNode.updateData(node);
    }

    @Override
    protected void delete(Connection conn, TreeNode node) {
        tableTreeNode.deleteData(conn, node);
    }

    @Override
    protected void copyNode(TreeItem<TreeNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        TreeNodeCopyController controller
                = (TreeNodeCopyController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeCopyFxml);

        controller.setCaller(this, item.getValue(), chainName);
    }

    @Override
    protected void moveNode(TreeItem<TreeNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        TreeNodeMoveController controller = (TreeNodeMoveController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeMoveFxml);
        controller.setCaller(this, item.getValue(), chainName);
    }

    @Override
    protected void editNode(TreeItem<TreeNode> item) {
        if (item == null || treeController == null) {
            return;
        }
        treeController.editNode(item.getValue());
    }

    @Override
    protected void pasteNode(TreeItem<TreeNode> item) {
        if (item == null || treeController == null) {
            return;
        }
        treeController.pasteNode(item.getValue());
    }

    @Override
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
            if (nodeValue != null && !nodeValue.isBlank()) {
                s.append(indentNode).append("<DIV class=\"nodeValue\">")
                        .append("<DIV style=\"padding: 0 0 0 ").append((indent + 4) * 6).append("px;\">")
                        .append("<DIV class=\"valueBox\">\n");
                String nodeDisplay;
                if (category.equals(TreeNode.WebFavorite)) {
                    nodeDisplay = "<A href=\"" + nodeValue + "\">";
                    if (node.getMore() != null && !node.getMore().isBlank()) {
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
    protected void importExamples() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.importExamples(treeController);
    }

    @FXML
    @Override
    protected void importAction() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setManage(treeController);
    }

    public TreeNode copyNode(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return null;
        }
        try {
            TreeNode newNode = new TreeNode(targetNode, sourceNode.getTitle(), sourceNode.getValue());
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
            List<TreeNode> children = tableTreeNode.children(conn, sourceid);
            if (children != null && !children.isEmpty()) {
                conn.setAutoCommit(true);
                for (TreeNode child : children) {
                    TreeNode newNode = TreeNode.create().setParentid(targetid).setCategory(category)
                            .setTitle(child.getTitle()).setValue(child.getValue()).setMore(child.getMore());
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

}
