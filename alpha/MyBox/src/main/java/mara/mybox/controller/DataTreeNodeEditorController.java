package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-3
 * @License Apache License Version 2.0
 */
public class DataTreeNodeEditorController extends BaseDataTreeHandleController {

    protected BaseDataValuesController dataController;
    protected SimpleBooleanProperty nodeChanged;
    protected boolean nodeExecutable, attributesChanged;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected DataNode parentNode, currentNode;

    @FXML
    protected Tab nodeTab, dataTab, tagsTab;
    @FXML
    protected ScrollPane dataPane;
    @FXML
    protected TextField idInput, titleInput, orderInput, timeInput;
    @FXML
    protected Label parentLabel;
    @FXML
    protected ControlDataNodeTags tagsController;

    public DataTreeNodeEditorController() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setTree(BaseDataTreeViewController controller) {
        try {
            treeController = controller;
            parentController = controller;
            nodeTable = treeController.nodeTable;
            tagTable = treeController.tagTable;
            nodeTagsTable = treeController.nodeTagsTable;

            initEditor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setTable(BaseNodeTable table) {
        try {
            nodeTable = table;
            tagTable = new TableDataTag(nodeTable);
            nodeTagsTable = new TableDataNodeTag(nodeTable);

            initEditor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initEditor() {
        try {
            baseName = baseName + "_" + nodeTable.getTableName();
            baseTitle = nodeTable.getTreeName() + " - " + message("EditNode");
            setTitle(baseTitle);

            nodeChanged = new SimpleBooleanProperty(false);

            titleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

            orderInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

            tagsController.setParameters(this);

            dataController = (BaseDataValuesController) WindowTools.loadFxml(nodeTable.getDataFxml());
            dataPane.setContent(dataController.getMyScene().getRoot());
            dataController.refreshStyle();
            dataController.setParameters(this);

            setAlwaysTop(true, false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadNode(DataNode node, boolean execute) {
        if (node == null) {
            editNull();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {
            private DataNode savedNode, savedParent;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    savedParent = nodeTable.query(conn, savedNode.getParentid());
                    return savedParent != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                currentNode = savedNode;
                parentNode = savedParent;
                loadData();
                if (execute) {
                    dataController.startAction();
                }
            }

            @Override
            protected void whenFailed() {
                if (treeController != null) {
                    treeController.popError(message("Invalid") + ": " + message("Node"));
                } else {
                    popError(message("Invalid") + ": " + message("Node"));
                }
                close();
            }

        };
        start(task, thisPane);
    }

    public void addNode(DataNode parent) {
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {

            private DataNode savedParent;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedParent = nodeTable.query(conn, parent.getNodeid());
                    return savedParent != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                parentNode = savedParent;
                currentNode = DataNode.createChild(parentNode, message("NewData"));
                loadData();
            }

            @Override
            protected void whenFailed() {
                if (treeController != null) {
                    treeController.popError(message("Invalid") + ": " + message("Node"));
                } else {
                    popError(message("Invalid") + ": " + message("Node"));
                }
                close();
            }

        };
        start(task);
    }

    protected void loadData() {
        try {
            loadAttributes();
            tagsController.loadTags();
            dataController.editValues();
            resetStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetStatus() {
        dataController.changed = false;
        tagsController.changed = false;
        attributesChanged = false;
        updateStatus();
    }

    protected void updateStatus() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (dataTab != null) {
            dataTab.setText(nodeTable.getDataName() + (dataController.changed ? "*" : ""));
        }
        if (nodeTab != null) {
            nodeTab.setText(message("Node") + (attributesChanged ? "*" : ""));
        }
        if (tagsTab != null) {
            tagsTab.setText(message("Tags") + (tagsController.changed ? "*" : ""));
        }
        boolean changed = dataController.changed || attributesChanged || tagsController.changed;
        String title = baseTitle;
        if (currentNode != null) {
            title += ": "
                    + (currentNode.getNodeid() < 0 ? message("NewData") : currentNode.getNodeid())
                    + " - " + currentNode.getTitle();
        }
        setTitle(title + (changed ? " *" : ""));

        nodeChanged.set(changed);
        isSettingValues = false;
    }

    public void editNull() {
        if (parentNode != null) {
            currentNode = DataNode.createChild(parentNode, message("NewData"));
        } else {
            currentNode = DataNode.create().setTitle(message("NewData"));
        }
        loadData();
    }

    /*
        attributes
     */
    protected void loadAttributes() {
        if (currentNode == null) {
            return;
        }
        isSettingValues = true;
        titleInput.setText(currentNode.getTitle());
        titleInput.setDisable(currentNode.isRoot());
        if (currentNode.getNodeid() < 0) {
            idInput.setText(message("NewData"));
        } else {
            idInput.setText(currentNode.getNodeid() + "");
        }
        orderInput.setText(currentNode.getOrderNumber() + "");
        timeInput.setText(DateTools.datetimeToString(currentNode.getUpdateTime()));
        isSettingValues = false;

        refreshParentNode();
        attributesChanged(false);
    }

    public void attributesChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        attributesChanged = changed;
        updateStatus();
    }

    protected DataNode pickAttributes() {
        String title = titleInput.getText();
        if (title == null || title.isBlank()) {
            popError(message("Invalid") + ": " + message("Title"));
            return null;
        }
        float orderNum;
        try {
            orderNum = Float.parseFloat(orderInput.getText());
        } catch (Exception e) {
            popError(message("Invalid") + ": " + message("OrderNumber"));
            return null;
        }

        DataNode node = DataNode.create();
        if (currentNode != null) {
            node.setNodeid(currentNode.getNodeid());
        }
        if (parentNode != null) {
            node.setParentid(parentNode.getNodeid());
        }
        node.setTitle(title);
        node.setOrderNumber(orderNum);
        node.setUpdateTime(new Date());
        return node;
    }

    @FXML
    public void selectParent() {
        DataSelectParentController.open(this, currentNode);
    }

    protected void setParentNode(DataNode node) {
        parentNode = node;
        attributesChanged(true);
        refreshParentNode();
    }

    protected void refreshParentNode() {
        if (parentNode == null) {
            parentLabel.setText(null);
            return;
        }
        FxTask ptask = new FxTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    chainName = nodeTable.chainName(this, conn, parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                parentLabel.setText(parentNode.shortDescription(chainName));
            }
        };
        start(ptask, thisPane);
    }

    /*
        actions
     */
    @FXML
    @Override
    public void saveAction() {
        if (parentNode == null || parentNode.getNodeid() < 0) {
            tabPane.getSelectionModel().select(nodeTab);
            popError(message("Invalid") + ": " + message("ParentNode"));
            return;
        }
        DataNode attributes = pickAttributes();
        if (attributes == null) {
            return;
        }
        DataNode node = dataController.pickValues(attributes);
        if (node == null) {
            popError(message("Invalid") + ": " + message("Data"));
            return;
        }
        boolean isNewNode = node.getNodeid() < 0;
        boolean isParentChanged = currentNode == null
                || currentNode.getParentid() != parentNode.getNodeid();
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode savedNode;
            private long nodeid;
            private boolean invalidParent;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    parentNode = nodeTable.query(conn, parentNode.getNodeid());
                    if (parentNode == null) {
                        conn.close();
                        invalidParent = true;
                        return false;
                    }
                    node.setUpdateTime(new Date());
                    savedNode = nodeTable.writeData(conn, node);
                    if (savedNode == null) {
                        conn.close();
                        error = message("Failed");
                        return false;
                    }
                    conn.commit();

                    nodeid = savedNode.getNodeid();
                    if (nodeid < 0) {
                        conn.close();
                        error = message("Failed");
                        return false;
                    }

                    nodeTagsTable.setAll(conn, nodeid,
                            tagsController.selectedItems());
                    conn.commit();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (treeRunning()) {
                    if (isNewNode) {
                        treeController.addNewNode(parentNode, savedNode);
                    } else if (isParentChanged) {
                        treeController.updateNode(parentNode, savedNode);
                    } else {
                        treeController.updateNode(savedNode);
                    }
                }
                if (parentRunning()) {
                    parentController.popSaved();
                    resetStatus();
                    close();
                } else {
                    popSaved();
                    resetStatus();
                }

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (invalidParent) {
                    tabPane.getSelectionModel().select(nodeTab);
                    popError(message("Invalid") + ": " + message("ParentNode"));
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void recoverAction() {
        resetStatus();
        loadNode(currentNode, false);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!nodeChanged.get()) {
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
                nodeChanged.set(false);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (dataController != null) {
            if (dataController.thisPane.isFocused() || dataController.thisPane.isFocusWithin()) {
                if (dataController.keyEventsFilter(event)) {
                    return true;
                }
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (dataController != null) {
            return dataController.keyEventsFilter(event);
        }
        return false;
    }

    /*
        static methods
     */
    public static DataTreeNodeEditorController open(BaseController parent) {
        DataTreeNodeEditorController controller = (DataTreeNodeEditorController) WindowTools
                .branchStage(parent, Fxmls.DataTreeNodeEditorFxml);
        controller.setParentController(parent);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController loadNode(BaseDataTreeViewController parent,
            DataNode node, boolean execute) {
        DataTreeNodeEditorController controller = open(parent);
        controller.setTree(parent);
        controller.loadNode(node, execute);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController editNode(BaseDataTreeViewController parent, DataNode node) {
        return loadNode(parent, node, false);
    }

    public static DataTreeNodeEditorController addNode(BaseDataTreeViewController parent, DataNode parentNode) {
        DataTreeNodeEditorController controller = open(parent);
        controller.setTree(parent);
        controller.addNode(parentNode);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController executeNode(BaseDataTreeViewController parent, DataNode node) {
        return loadNode(parent, node, true);
    }

}
