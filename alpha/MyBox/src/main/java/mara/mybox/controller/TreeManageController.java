package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeManageController extends BaseInfoTreeController {

    @FXML
    protected ControlInfoTreeManage nodesController;
    @FXML
    protected BaseInfoTreeNodeController nodeController;

    @Override
    public void initValues() {
        try {
            super.initValues();
            treeController = nodesController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nodesController.setParameters(this);
            nodeController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (nodeController != null) {
                return nodeController.keyEventsFilter(event); // pass event to editor
            }
            return false;
        } else {
            return true;
        }
    }

    /*
        synchronize
     */
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        if (parent == null || newNode == null) {
            return;
        }
        if (loadedParent != null && parent.getNodeid() == loadedParent.getNodeid()) {
            loadNodes(parent);
        }
    }

    public void nodeRenamed(InfoNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();
        if (loadedParent != null && id == loadedParent.getNodeid()) {
            loadedParent = node;
            makeConditionPane();
        } else {
            for (int i = 0; i < tableData.size(); i++) {
                InfoNode tnode = tableData.get(i);
                if (tnode.getNodeid() == id) {
                    tableData.set(i, node);
                    break;
                }
            }
        }
        if (nodeController.attributesController.parentNode != null
                && id == nodeController.attributesController.parentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(node);
        }
        if (nodeController.attributesController.currentNode != null
                && id == nodeController.attributesController.currentNode.getNodeid()) {
            nodeController.attributesController.renamed(node.getTitle());
        }
    }

    public void nodeDeleted(InfoNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();
        if (loadedParent != null && id == loadedParent.getNodeid()) {
            loadedParent = null;
            makeConditionPane();
            tableData.clear();
        } else {
            for (int i = 0; i < tableData.size(); i++) {
                InfoNode tnode = tableData.get(i);
                if (tnode.getNodeid() == id) {
                    tableData.remove(tnode);
                    break;
                }
            }
        }
        nodeController.editNode(null);
    }

    public void nodeMoved(InfoNode parent, InfoNode node) {
        if (parent == null || node == null) {
            return;
        }
        long id = node.getNodeid();
        if (loadedParent != null) {
            loadNodes(loadedParent);
        }
        if (nodeController.attributesController.currentNode != null
                && id == nodeController.attributesController.currentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(parent);
        }
        if (nodeController.attributesController.parentNode != null
                && id == nodeController.attributesController.parentNode.getNodeid()) {
            nodeController.attributesController.setParentNode(node);
        }
    }

    public void nodesMoved(InfoNode parent, List<InfoNode> nodes) {
        if (parent == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        loadNodes(loadedParent);
        nodesController.loadTree();
    }

    public void nodesCopied(InfoNode parent) {
        nodesController.updateNode(parent);
    }

    public void nodesDeleted() {
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    loadedParent = tableTreeNode.readData(conn, loadedParent);
                    nodeController.attributesController.currentNode
                            = tableTreeNode.readData(conn, nodeController.attributesController.currentNode);
                    nodeController.attributesController.parentNode
                            = tableTreeNode.readData(conn, nodeController.attributesController.parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                nodeController.editNode(nodeController.attributesController.currentNode);
                nodesController.loadTree(loadedParent);
            }
        };
        start(task);
    }

    public void nodeSaved() {
        if (nodeController.attributesController.currentNode == null) {
            return;
        }
        long id = nodeController.attributesController.currentNode.getNodeid();
        if (loadedParent != null && id == loadedParent.getNodeid()) {
            loadedParent = nodeController.attributesController.currentNode;
            makeConditionPane();
        }
        for (int i = 0; i < tableData.size(); i++) {
            InfoNode tnode = tableData.get(i);
            if (tnode.getNodeid() == id) {
                tableData.set(i, nodeController.attributesController.currentNode);
                break;
            }
        }
        nodesController.updateNode(nodeController.attributesController.currentNode);
        nodeController.nodeChanged(false);
    }

    public void newNodeSaved() {
        if (nodeController.attributesController.currentNode == null) {
            return;
        }
        nodesController.addNewNode(nodesController.find(nodeController.attributesController.parentNode),
                nodeController.attributesController.currentNode, false);
        if (loadedParent != null
                && nodeController.attributesController.parentNode.getNodeid() == loadedParent.getNodeid()) {
            loadNodes(nodeController.attributesController.currentNode);
        }
        nodeController.nodeChanged(false);
    }

    public void nodeChanged() {
        if (isSettingValues) {
            return;
        }
        String currentTitle = getTitle();
        if (nodeController.nodeChanged) {
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
    @Override
    public void loadTree() {
        try (Connection conn = DerbyBase.getConnection()) {
            if (tableTreeNode.categoryEmpty(conn, category)) {
                File file = InfoNode.exampleFile(category);
                if (file != null) {
                    if (AppVariables.isTesting
                            || PopTools.askSure(getTitle(), message("ImportExamples") + ": " + message(category))) {
                        nodesController.importExamples();
                        return;
                    }
                }
            }
            nodesController.loadTree();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        table
     */
    @Override
    protected long clearData() {
        if (queryConditions != null) {
            return tableTreeNode.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected void afterDeletion() {
        super.afterDeletion();
        nodesDeleted();
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        nodesDeleted();
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("View"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                viewAction();
            });
            menu.setDisable(copyButton.isDisabled());
            items.add(menu);

            if (pasteButton != null) {
                menu = new MenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pasteAction();
                });
                menu.setDisable(pasteButton.isDisabled());
                items.add(menu);
            }

            menu = new MenuItem(message("Move"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveAction();
            });
            menu.setDisable(moveDataButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyAction();
            });
            menu.setDisable(copyButton.isDisabled());
            items.add(menu);

            items.addAll(super.makeTableContextMenu());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void itemClicked() {
        editAction();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean none = isNoneSelected();
        deleteButton.setDisable(none);
        copyButton.setDisable(none);
        moveDataButton.setDisable(none);
        if (pasteButton != null) {
            pasteButton.setDisable(none);
        }
    }

    @FXML
    @Override
    public void addAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (loadedParent != null) {
            nodeController.attributesController.parentNode = loadedParent;
        }
        editNode(null);
    }

    @FXML
    @Override
    public void editAction() {
        editNode(selectedItem());
    }

    public void editNode(InfoNode node) {
        if (!checkBeforeNextAction()) {
            return;
        }
        nodeController.editNode(node);
    }

    @FXML
    @Override
    public void copyAction() {
        TreeNodesCopyController.oneOpen(this);
    }

    @FXML
    protected void moveAction() {
        TreeNodesMoveController.oneOpen(this);
    }

    @FXML
    @Override
    public void pasteAction() {
        pasteNode(selectedItem());
    }

    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        String v = node.getInfo();
        if (v == null || v.isBlank()) {
            return;
        }
        nodeController.pasteText(v);
    }

    public void executeNode(InfoNode node) {
        if (node == null) {
            return;
        }
        String v = node.getInfo();
        if (v == null || v.isBlank()) {
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
        editNode(null);
    }

    @FXML
    protected void copyNode() {
        if (!checkBeforeNextAction()) {
            return;
        }
        nodeController.attributesController.copyNode();
    }

    @FXML
    protected void recoverNode() {
        nodeController.editNode(nodeController.attributesController.currentNode);
    }

    @FXML
    @Override
    public void saveAction() {
        nodeController.attributesController.saveNode();
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !file.exists() || !checkBeforeNextAction()) {
            return;
        }
        nodeController.loadFile(file);
    }

    public boolean isNodeChanged() {
        return nodeController.nodeChanged;
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
                nodeController.nodeChanged = false;
                return true;
            } else {
                return false;
            }
        }
    }

    /*
        static methods
     */
    public static TreeManageController oneOpen() {
        TreeManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeManageController) {
                try {
                    controller = (WebFavoritesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeManageController) WindowTools.openStage(Fxmls.TreeManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
