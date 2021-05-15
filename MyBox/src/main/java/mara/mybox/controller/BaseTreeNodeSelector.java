package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTree;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public abstract class BaseTreeNodeSelector extends BaseNodeSelector<TreeNode> {

    protected BaseTreeNodeSelector treeController;
    protected TableTree tableTree;
    protected String category;

    public BaseTreeNodeSelector() {
    }

    public void setParent(BaseController parent, String category) {
        super.setParent(parent, true);
        tableTree = new TableTree();
        this.category = category;
    }

    public void setCaller(BaseTreeNodeSelector treeController) {
        setParent(null, false);
        this.treeController = treeController;
        this.tableTree = treeController.tableTree;
        this.category = treeController.category;
        cloneTree(treeController.treeView, treeView, getIgnoreNode());
    }

    @Override
    public String display(TreeNode node) {
        return node.getTitle();
    }

    @Override
    public String tooltip(TreeNode node) {
        if (node.getAttribute() != null && !node.getAttribute().isBlank()) {
            return node.getTitle() + "\n" + node.getAttribute();
        } else {
            return null;
        }
    }

    public TreeNode root() {
        return tableTree.findAndCreateRoot(category);
    }

    @Override
    public TreeNode root(Connection conn) {
        return tableTree.findAndCreateRoot(conn, category);
    }

    @Override
    public int size(Connection conn, TreeNode root) {
        return TableTree.size(conn, root.getNodeid());
    }

    @Override
    public long id(TreeNode node) {
        return node.getNodeid();
    }

    @Override
    public List<TreeNode> children(Connection conn, TreeNode node) {
        return tableTree.children(conn, id(node));
    }

    @Override
    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return tableTree.ancestor(conn, id(node));
    }

    @Override
    public boolean equal(TreeNode node1, TreeNode node2) {
        return node1.getNodeid() == node2.getNodeid();
    }

    @Override
    public TreeNode dummy() {
        return new TreeNode();
    }

    @Override
    public boolean isDummy(TreeNode node) {
        return node.getTitle() != null;
    }

    @Override
    public TreeNode createNode(TreeNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        TreeNode newNode = new TreeNode(targetNode.getNodeid(), name);
        newNode = tableTree.insertData(newNode);
        return newNode;
    }

    @Override
    public String name(TreeNode node) {
        return node.getTitle();
    }

    @FXML
    @Override
    protected void exportNode() {

    }

    @Override
    protected void clearTree(Connection conn, TreeNode node) {
        TableTree.deleteChildren(conn, node.getNodeid());
    }

    @Override
    protected TreeNode rename(TreeNode node, String name) {
        node.setTitle(name);
        return tableTree.updateData(node);
    }

    @Override
    protected void delete(Connection conn, TreeNode node) {
        tableTree.deleteData(conn, node);
    }

    @Override
    protected void copyNode(Boolean onlyContents) {
    }

    @FXML
    @Override
    protected void moveNode() {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        TreeNodeMoveController controller = (TreeNodeMoveController) FxmlStage.openStage(CommonValues.TreeNodeMoveFxml);
        controller.setCaller(this, selectedItem.getValue(), chainName);
    }

    public BaseTreeNodeSelector oneOpen() {
        BaseTreeNodeSelector controller = null;
        Stage stage = FxmlStage.findStage(getBaseTitle());
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (BaseTreeNodeSelector) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (BaseTreeNodeSelector) FxmlStage.openStage(myFxml);
        }
        if (controller != null) {
            controller.getMyStage().toFront();
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

}
