package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeManageController extends BaseDataTreeController {

    @FXML
    protected ControlDataTreeManage treeManageController;

//    @Override
//    public void initControls() {
//        try {
//            super.initControls();
//
//            treeManageController.setManager(this);
//
//        } catch (Exception e) {
//            MyBoxLog.error(e);
//        }
//    }

    /*
        synchronize
     */
    public void nodeAdded(DataNode parent, DataNode newNode) {
        if (parent == null || newNode == null) {
            return;
        }

    }

    public void nodeRenamed(DataNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();

        if (nodeController.attributesController.parentNode != null
                && id == nodeController.attributesController.parentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(node);
        }
        if (nodeController.attributesController.currentNode != null
                && id == nodeController.attributesController.currentNode.getNodeid()) {
            nodeController.attributesController.renamed(node.getTitle());
        }
    }

    public void nodeDeleted(DataNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();

        nodeController.editNode(null);
    }

    public void nodeMoved(DataNode parent, DataNode node) {
        if (parent == null || node == null) {
            return;
        }
        long id = node.getNodeid();

        if (nodeController.attributesController.currentNode != null
                && id == nodeController.attributesController.currentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(parent);
        }
        if (nodeController.attributesController.parentNode != null
                && id == nodeController.attributesController.parentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(node);
        }
    }

    public void nodesMoved(DataNode parent, List<DataNode> nodes) {
        if (parent == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        treeController.loadTree(parent);
    }

    public void nodesCopied(DataNode parent) {
        treeController.loadTree(parent);
    }

    public void nodesDeleted() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
//                    tableController.loadedParent = tableTree.readData(conn, tableController.loadedParent);
                    nodeController.attributesController.currentNode
                            = dataNodeTable.readData(conn, nodeController.attributesController.currentNode);
                    nodeController.attributesController.parentNode
                            = dataNodeTable.readData(conn, nodeController.attributesController.parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                nodeController.editNode(nodeController.attributesController.currentNode);
//                treeController.loadTree(tableController.loadedParent);
            }
        };
        start(task);
    }

    public void nodeSaved() {
        if (nodeController.attributesController.currentNode == null) {
            return;
        }
        long id = nodeController.attributesController.currentNode.getNodeid();
//        if (tableController.loadedParent != null && id == tableController.loadedParent.getNodeid()) {
//            tableController.loadedParent = nodeController.attributesController.currentNode;
//            tableController.makeConditionPane();
//        }
//        for (int i = 0; i < tableController.tableData.size(); i++) {
//            TreeNode tnode = tableController.tableData.get(i);
//            if (tnode.getNodeid() == id) {
//                tableController.tableData.set(i, nodeController.attributesController.currentNode);
//                break;
//            }
//        }
        treeController.updateNode(nodeController.attributesController.currentNode);
        nodeController.resetStatus();
    }

    public void newNodeSaved() {
        if (nodeController.attributesController.currentNode == null) {
            return;
        }
        treeController.addNewNode(treeController.find(nodeController.attributesController.parentNode),
                nodeController.attributesController.currentNode, false);
//        if (tableController.loadedParent != null
//                && nodeController.attributesController.parentNode.getNodeid() == tableController.loadedParent.getNodeid()) {
//            tableController.loadNodes(nodeController.attributesController.currentNode);
//        }
        nodeController.resetStatus();
    }

    public void nodeChanged() {
        if (isSettingValues) {
            return;
        }
        String currentTitle = getTitle();
        if (nodeController.nodeChanged.get()) {
            if (!currentTitle.endsWith(" *")) {
                setTitle(currentTitle + " *");
            }
        } else {
            if (currentTitle.endsWith(" *")) {
                setTitle(currentTitle.substring(0, currentTitle.length() - 2));
            }
        }
    }


    /*
        tree
     */
    public boolean editNode(DataNode node) {
        MyBoxLog.debug("here");
        if (!checkBeforeNextAction()) {
            return false;
        }
        return nodeController.editNode(node);
    }

    @FXML
    @Override
    public void copyAction() {
//        InfoTreeNodesCopyController.oneOpen(this);
    }

    @FXML
    protected void moveAction() {
//        InfoTreeNodesMoveController.oneOpen(this);
    }

    public void pasteNode(DataNode node) {
        nodeController.pasteNode(node);
    }

    public void executeNode(DataNode node) {
        if (node == null) {
            return;
        }
        editNode(node);
        if (nodeController.startButton != null) {
            nodeController.startAction();
        } else if (nodeController.goButton != null) {
            nodeController.goAction();
        } else if (startButton != null) {
            startAction();
        } else if (goButton != null) {
            goAction();
        }
    }


    /*
        node
     */
    @FXML
    protected void addNode() {
        if (editNode(null)) {
            nodeController.newNodeCreated();
        }
    }

    @FXML
    protected void copyNode() {
        if (!checkBeforeNextAction()) {
            return;
        }
        nodeController.attributesController.copyNode();
        nodeController.newNodeCreated();
    }

    @FXML
    protected void recoverNode() {
        nodeController.editNode(nodeController.attributesController.currentNode);
    }

    @FXML
    @Override
    public void saveAction() {
//        nodeController.attributesController.saveNode();
    }

    @Override
    public void sourceFileChanged(File file) {
        nodeController.sourceFileChanged(file);
    }

    public boolean isNodeChanged() {
        return nodeController.nodeChanged.get();
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!isNodeChanged()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("DataChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else if (result.get() == buttonNotSave) {
                nodeController.nodeChanged.set(false);
                return true;
            } else {
                return false;
            }
        }
    }

}
