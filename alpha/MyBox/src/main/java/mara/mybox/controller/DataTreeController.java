package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.db.table.TableInfo;
import mara.mybox.db.table.TableNote;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class DataTreeController extends BaseController {

    protected BaseDataTable dataTable;
    protected TableDataNode dataNodeTable;
    protected TableDataTag dataTagTable;
    protected TableDataNodeTag dataNodeTagTable;

    @FXML
    protected ControlDataTreeManage treeController;
    @FXML
    protected ControlDataNodeEditor nodeController;

    public void initTree(BaseDataTable table) {
        try {
            if (table == null) {
                return;
            }
            dataTable = table;
            dataNodeTable = new TableDataNode(dataTable);
            dataTagTable = new TableDataTag(dataTable);
            dataNodeTagTable = new TableDataNodeTag(dataTable);

            nodeController.setParameters(this);
            treeController.setParameters(this);

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

    public void popNode(DataNode item) {
        if (item == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = item.toHtml();
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlTableController.open(null, html);
            }

        };
        start(task);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (nodeController == null) {
            return super.keyEventsFilter(event);
        }
        if (nodeController.thisPane.isFocused() || nodeController.thisPane.isFocusWithin()) {
            if (nodeController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return nodeController.keyEventsFilter(event); // pass event to editor
    }

    @FXML
    @Override
    public void saveAction() {
        if (nodeController != null) {
            nodeController.saveAction();
        }
    }

    @FXML
    @Override
    public void addAction() {
        if (nodeController != null) {
            nodeController.addAction();
        }
    }

    @FXML
    @Override
    public void copyAction() {
        if (nodeController != null) {
            nodeController.copyAction();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        if (nodeController != null) {
            nodeController.recoverAction();
        }
    }

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

        if (nodeController.parentNode != null
                && id == nodeController.parentNode.getNodeid()) {
//            nodeController.attributesController.setParentNode(node);
        }
        if (nodeController.currentNode != null
                && id == nodeController.currentNode.getNodeid()) {
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

        if (nodeController.currentNode != null
                && id == nodeController.currentNode.getNodeid()) {
//            nodeController.attributesController.setParentNode(parent);
        }
        if (nodeController.parentNode != null
                && id == nodeController.parentNode.getNodeid()) {
//            nodeController.attributesController.setParentNode(node);
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
                    nodeController.currentNode
                            = dataNodeTable.readData(conn, nodeController.currentNode);
                    nodeController.parentNode
                            = dataNodeTable.readData(conn, nodeController.parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                nodeController.editNode(nodeController.currentNode);
//                treeController.loadTree(tableController.loadedParent);
            }
        };
        start(task);
    }

    public void nodeSaved() {
        if (nodeController.currentNode == null) {
            return;
        }
        treeController.updateNode(nodeController.currentNode);
        nodeController.resetStatus();
    }

    public void newNodeSaved() {
        if (nodeController.currentNode == null) {
            return;
        }
        treeController.addNewNode(treeController.find(nodeController.parentNode),
                nodeController.currentNode, false);
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
        static methods
     */
    public static DataTreeController open(BaseDataTable table) {
        DataTreeController controller = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
        controller.requestMouse();
        controller.initTree(table);
        return controller;
    }

    public static DataTreeController infoTree() {
        return open(new TableInfo());
    }

    public static DataTreeController noteTree() {
        return open(new TableNote());
    }

}
