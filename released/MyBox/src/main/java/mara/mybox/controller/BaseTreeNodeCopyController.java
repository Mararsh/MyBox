package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-4-27
 * @License Apache License Version 2.0
 */
public abstract class BaseTreeNodeCopyController extends BaseTreeNodeSelector {

    protected TreeNode sourceNode;
    protected boolean onlyContents;

    @FXML
    protected Label sourceLabel;

    public BaseTreeNodeCopyController() {
        baseTitle = Languages.message("CopyNode");
    }

    protected abstract String copyMembers(Connection conn, TreeNode sourceNode, TreeNode targetNode);

    public void setCaller(BaseTreeNodeSelector treeController, TreeNode sourceNode, String name, boolean onlyContents) {
        this.sourceNode = sourceNode;
        this.onlyContents = onlyContents;
        sourceLabel.setText(Languages.message("NodeCopyed") + ":\n" + name);
        ignoreNode = sourceNode;
        setCaller(treeController);
    }

    @Override
    public TreeNode getIgnoreNode() {
        return sourceNode;
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
            if (targetItem == null) {
                alertError(Languages.message("SelectNodeCopyInto"));
                return;
            }
            TreeNode targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (!onlyContents) {
                            TreeNode newNode = new TreeNode(targetNode.getNodeid(), sourceNode.getTitle(), sourceNode.getAttribute());
                            newNode = tableTree.insertData(conn, newNode);
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
                        long sourceid = sourceNode.getNodeid();
                        long targetid = targetNode.getNodeid();
                        error = copyMembers(conn, sourceNode, targetNode);
                        if (error != null) {
                            return false;
                        }
                        conn.setAutoCommit(true);
                        List<TreeNode> children = tableTree.children(conn, sourceid);
                        if (children != null) {
                            for (TreeNode child : children) {
                                TreeNode newBook = new TreeNode(targetid, child.getTitle(), child.getAttribute());
                                newBook = tableTree.insertData(conn, newBook);
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
                    if (treeController == null || !treeController.getMyStage().isShowing()) {
                        treeController = treeController.oneOpen();
                    } else {
                        treeController.nodeChanged(targetNode);
                    }
                    treeController.loadTree(targetNode);
                    treeController.popSuccessful();
                    closeStage();
                }
            };
            start(task);
        }
    }

}
