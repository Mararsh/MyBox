package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlTreeInfoSelect extends BaseTreeInfoController {

    protected BaseTreeInfoController caller;

    public void setCaller(BaseTreeInfoController caller) {
        if (caller == null) {
            return;
        }
        this.parentController = caller;
        this.baseName = caller.baseName;
        this.caller = caller;
        tableTreeNode = caller.tableTreeNode;
        tableTreeNodeTag = caller.tableTreeNodeTag;
        category = caller.category;
        cloneTree(caller.infoTree);
    }

    public void cloneTree(TreeTableView<TreeNode> sourceTreeView) {
        if (sourceTreeView == null) {
            return;
        }
        TreeItem<TreeNode> sourceRoot = sourceTreeView.getRoot();
        if (sourceRoot == null) {
            return;
        }
        TreeItem<TreeNode> targetRoot = new TreeItem(sourceRoot.getValue());
        infoTree.setRoot(targetRoot);
        targetRoot.setExpanded(sourceRoot.isExpanded());
        cloneNode(sourceRoot, targetRoot);
    }

    public void cloneNode(TreeItem<TreeNode> sourceNode, TreeItem<TreeNode> targetNode) {
        if (sourceNode == null || targetNode == null) {
            return;
        }
        List<TreeItem<TreeNode>> sourceChildren = sourceNode.getChildren();
        if (sourceChildren == null) {
            return;
        }
        for (TreeItem<TreeNode> sourceChild : sourceChildren) {
            TreeItem<TreeNode> targetChild = new TreeItem<>(sourceChild.getValue());
            targetNode.getChildren().add(targetChild);
            targetChild.setExpanded(sourceChild.isExpanded());
            cloneNode(sourceChild, targetChild);
        }
    }

    @Override
    protected void doubleClicked(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        okAction();
    }

    @Override
    protected void nodeAdded(TreeNode parent, TreeNode newNode) {
        caller.addNewNode(caller.find(parent), newNode, true);
    }

    public TreeNode copyNode(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return null;
        }
        try {
            TreeNode newNode = sourceNode.copyTo(targetNode);
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
