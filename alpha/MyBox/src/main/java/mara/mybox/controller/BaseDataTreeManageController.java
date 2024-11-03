package mara.mybox.controller;

import java.io.File;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTreeManageController extends BaseDataTreeController {

    @FXML
    protected ControlDataTreeManage treeManageController;

    public abstract void initTreeValues();

    @Override
    public void initValues() {
        try {
            super.initValues();

            treeController = treeManageController;
            initTreeValues();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tree
     */
    public boolean editNode(DataNode node) {
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
