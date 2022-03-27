package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-27
 * @License Apache License Version 2.0
 */
public class TreeNodeCopyController extends TreeNodesController {
    
    protected TreeNode sourceNode;
    protected boolean onlyContents;
    
    @FXML
    protected Label sourceLabel;
    
    public TreeNodeCopyController() {
        baseTitle = message("CopyNode");
    }
    
    public void setCaller(TreeNodesController nodesController, TreeNode sourceNode, String name, boolean onlyContents) {
        this.sourceNode = sourceNode;
        this.onlyContents = onlyContents;
        sourceLabel.setText(message("NodeCopyed") + ":\n" + name);
        ignoreNode = sourceNode;
        setCaller(nodesController);
    }
    
    @Override
    public TreeNode getIgnoreNode() {
        return sourceNode;
    }
    
    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
            alertError(message("SelectToHandle"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
            if (targetItem == null) {
                alertError(message("SelectNodeCopyInto"));
                return;
            }
            TreeNode targetNode = targetItem.getValue();
            if (targetNode == null) {
                alertError(message("SelectNodeCopyInto"));
                return;
            }
            task = new SingletonTask<Void>(this) {
                
                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (!onlyContents) {
                            TreeNode newNode = new TreeNode(targetNode, sourceNode.getTitle(), sourceNode.getValue());
                            newNode = tableTreeNode.insertData(conn, newNode);
                            if (newNode == null) {
                                return false;
                            }
                            ok = copyContents(conn, sourceNode, newNode);
                        } else {
                            ok = copyContents(conn, sourceNode, targetNode);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return ok;
                }
                
                protected boolean copyContents(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
                    if (conn == null || sourceNode == null || targetNode == null) {
                        return false;
                    }
                    try {
                        error = copyMembers(conn, sourceNode, targetNode);
                        if (error != null) {
                            return false;
                        }
                        conn.setAutoCommit(true);
                        List<TreeNode> children = tableTreeNode.children(conn, sourceNode.getNodeid());
                        if (children != null) {
                            for (TreeNode child : children) {
                                TreeNode newBook = new TreeNode(targetNode, child.getTitle(), child.getValue());
                                newBook = tableTreeNode.insertData(conn, newBook);
                                if (newBook == null) {
                                    continue;
                                }
                                copyContents(conn, child, newBook);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }
                
                @Override
                protected void whenSucceeded() {
                    if (caller == null || !caller.getMyStage().isShowing()) {
                        caller = caller.oneOpen();
                    }
                    caller.nodeAdded(targetNode, sourceNode);
                    caller.loadTree(targetNode);
                    caller.popSuccessful();
                    closeStage();
                }
            };
            start(task);
        }
    }
    
    protected String copyMembers(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            return "InvalidData";
        }
        try {
            if (tableTreeNode == null) {
                tableTreeNode = new TableTreeNode();
            }
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            List<TreeNode> leaves = tableTreeNode.children(conn, sourceid);
            if (leaves != null) {
                conn.setAutoCommit(false);
                for (TreeNode node : leaves) {
                    TreeNode newNode = TreeNode.create().setParentid(targetid).setCategory(category)
                            .setTitle(node.getTitle()).setValue(node.getValue()).setMore(node.getMore());
                    tableTreeNode.insertData(conn, newNode);
                }
                conn.commit();
            }
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }
    
}
