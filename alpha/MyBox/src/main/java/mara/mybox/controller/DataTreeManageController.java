package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class DataTreeManageController extends BaseDataTreeController {

    protected BaseDataTreeNodeController editor;

    @FXML
    protected ControlDataTreeManage treeController;

    @Override
    public void initValues() {
        try {
            super.initValues();
            treeView = treeController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            treeController.setManager(this);
            editor.setManager(this);

            loadData();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (editor != null) {
            if (editor.thisPane.isFocused() || editor.thisPane.isFocusWithin()) {
                if (editor.keyEventsFilter(event)) {
                    return true;
                }
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (editor == null) {
            return false;
        }
        return editor.keyEventsFilter(event); // pass event to editor
    }

    /*
        synchronize
     */
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        if (parent == null || newNode == null) {
            return;
        }

    }

    public void nodeRenamed(InfoNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();

        if (editor.attributesController.parentNode != null
                && id == editor.attributesController.parentNode.getNodeid()) {
            editor.attributesController.setParentNode(node);
        }
        if (editor.attributesController.currentNode != null
                && id == editor.attributesController.currentNode.getNodeid()) {
            editor.attributesController.renamed(node.getTitle());
        }
    }

    public void nodeDeleted(InfoNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();

        editor.editNode(null);
    }

    public void nodeMoved(InfoNode parent, InfoNode node) {
        if (parent == null || node == null) {
            return;
        }
        long id = node.getNodeid();

        if (editor.attributesController.currentNode != null
                && id == editor.attributesController.currentNode.getNodeid()) {
            editor.attributesController.setParentNode(parent);
        }
        if (editor.attributesController.parentNode != null
                && id == editor.attributesController.parentNode.getNodeid()) {
            editor.attributesController.setParentNode(node);
        }
    }

    public void nodesMoved(InfoNode parent, List<InfoNode> nodes) {
        if (parent == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        treeController.loadTree(parent);
    }

    public void nodesCopied(InfoNode parent) {
        treeController.loadTree(parent);
    }

    public void nodesDeleted() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tableController.loadedParent = tableTreeNode.readData(conn, tableController.loadedParent);
                    editor.attributesController.currentNode
                            = tableTreeNode.readData(conn, editor.attributesController.currentNode);
                    editor.attributesController.parentNode
                            = tableTreeNode.readData(conn, editor.attributesController.parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                editor.editNode(editor.attributesController.currentNode);
                treeController.loadTree(tableController.loadedParent);
            }
        };
        start(task);
    }

    public void nodeSaved() {
        if (editor.attributesController.currentNode == null) {
            return;
        }
        long id = editor.attributesController.currentNode.getNodeid();
        if (tableController.loadedParent != null && id == tableController.loadedParent.getNodeid()) {
            tableController.loadedParent = editor.attributesController.currentNode;
            tableController.makeConditionPane();
        }
        for (int i = 0; i < tableController.tableData.size(); i++) {
            InfoNode tnode = tableController.tableData.get(i);
            if (tnode.getNodeid() == id) {
                tableController.tableData.set(i, editor.attributesController.currentNode);
                break;
            }
        }
        treeController.updateNode(editor.attributesController.currentNode);
        editor.nodeChanged(false);
    }

    public void newNodeSaved() {
        if (editor.attributesController.currentNode == null) {
            return;
        }
        treeController.addNewNode(treeController.find(editor.attributesController.parentNode),
                editor.attributesController.currentNode, false);
        if (tableController.loadedParent != null
                && editor.attributesController.parentNode.getNodeid() == tableController.loadedParent.getNodeid()) {
            tableController.loadNodes(editor.attributesController.currentNode);
        }
        editor.nodeChanged(false);
    }

    public void nodeChanged() {
        if (isSettingValues) {
            return;
        }
        String currentTitle = getTitle();
        if (editor.nodeChanged.get()) {
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
    public boolean editNode(InfoNode node) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        return editor.editNode(node);
    }

    @FXML
    @Override
    public void copyAction() {
        InfoTreeNodesCopyController.oneOpen(this);
    }

    @FXML
    protected void moveAction() {
        InfoTreeNodesMoveController.oneOpen(this);
    }

    public void pasteNode(InfoNode node) {
        editor.pasteNode(node);
    }

    public void executeNode(InfoNode node) {
        if (node == null) {
            return;
        }
        editNode(node);
        if (editor.startButton != null) {
            editor.startAction();
        } else if (editor.goButton != null) {
            editor.goAction();
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
            editor.newNodeCreated();
        }
    }

    @FXML
    protected void copyNode() {
        if (!checkBeforeNextAction()) {
            return;
        }
        editor.attributesController.copyNode();
        editor.newNodeCreated();
    }

    @FXML
    protected void recoverNode() {
        editor.editNode(editor.attributesController.currentNode);
    }

    @FXML
    @Override
    public void saveAction() {
        editor.attributesController.saveNode();
    }

    @Override
    public void sourceFileChanged(File file) {
        editor.sourceFileChanged(file);
    }

    public boolean isNodeChanged() {
        return editor.nodeChanged.get();
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
                editor.nodeChanged.set(false);
                return true;
            } else {
                return false;
            }
        }
    }

    /*
        Tags
     */
    @Override
    public void tagsChanged() {
        editor.attributesController.synchronizeTags();
    }

    /*
        static methods
     */
    public static DataTreeManageController oneOpen() {
        DataTreeManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataTreeManageController) {
                try {
                    controller = (WebFavoritesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataTreeManageController) WindowTools.openStage(Fxmls.InfoTreeManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
