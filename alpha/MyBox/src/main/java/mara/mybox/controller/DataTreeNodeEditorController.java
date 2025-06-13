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

    protected BaseDataValuesController valuesController;
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

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setParameters(BaseController parent, BaseNodeTable table) {
        try {
            parentController = parent;
            nodeTable = table;
            tagTable = new TableDataTag(nodeTable);
            nodeTagsTable = new TableDataNodeTag(nodeTable);
            if (parentController instanceof BaseDataTreeController) {
                dataController = (BaseDataTreeController) parentController;
            }

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

            tagsController.setParameters(this, nodeTable, tagTable, nodeTagsTable);

            tagsController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateStatus();
                }
            });

            valuesController = (BaseDataValuesController) WindowTools.loadFxml(nodeTable.getDataFxml());
            dataPane.setContent(valuesController.getMyScene().getRoot());
            valuesController.refreshStyle();
            valuesController.setParameters(this);

            setAlwaysTop(true, false);

            editNull();

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
                    valuesController.startAction();
                }
            }

            @Override
            protected void whenFailed() {
                if (valuesController != null) {
                    valuesController.popError(message("Invalid") + ": " + message("Node"));
                } else {
                    popError(message("Invalid") + ": " + message("Node"));
                }
                close();
            }

        };
        start(task, thisPane);
    }

    public void addNode(DataNode parent) {
        if (parent == null) {
            editNull();
            return;
        }
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
                if (valuesController != null) {
                    valuesController.popError(message("Invalid") + ": " + message("Node"));
                } else {
                    popError(message("Invalid") + ": " + message("Node"));
                }
                close();
            }

        };
        start(task);
    }

    public void editNull() {
        currentNode = null;
        loadData();
    }

    protected void loadData() {
        try {
            loadAttributes();
            tagsController.loadTags(currentNode);
            valuesController.editValues();
            resetStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetStatus() {
        valuesController.changed = false;
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
            dataTab.setText(nodeTable.getDataName() + (valuesController.changed ? "*" : ""));
        }
        if (nodeTab != null) {
            nodeTab.setText(message("Node") + (attributesChanged ? "*" : ""));
        }
        if (tagsTab != null) {
            tagsTab.setText(message("Tags") + (tagsController.changed ? "*" : ""));
        }
        boolean changed = valuesController.changed || attributesChanged || tagsController.changed;
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

    /*
        attributes
     */
    protected void loadAttributes() {
        if (parentNode == null) {
            parentNode = nodeTable.getRoot();
        }
        if (currentNode == null) {
            currentNode = DataNode.createChild(parentNode, message("NewData"));
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
        DataSelectParentController.open(this, currentNode, parentNode);
    }

    protected void setParentNode(DataNode node) {
        parentNode = node;
        attributesChanged(true);
        refreshParentNode();
    }

    protected void refreshParentNode() {
        FxTask ptask = new FxTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (parentNode == null) {
                        parentNode = nodeTable.getRoot(conn);
                    }
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
            popError(message("Invalid") + ": " + message("ParentNode"));
            tabPane.getSelectionModel().select(nodeTab);
            selectParent();
            return;
        }
        DataNode attributes = pickAttributes();
        if (attributes == null) {
            return;
        }
        DataNode node = valuesController.pickValues(attributes);
        if (node == null) {
            popError(message("Invalid") + ": " + message("Data"));
            return;
        }
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
                if (dataRunning()) {
                    dataController.nodeSaved(parentNode, savedNode);
                } else if (openCheck.isSelected()) {
                    DataTreeController c = DataTreeController.open(nodeTable, savedNode);
                    c.popSaved();
                } else {
                    parentController.popSaved();
                }
                resetStatus();
                close();
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

    @FXML
    public void locateAction() {
        DataTreeController.open(nodeTable, currentNode);
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
        if (valuesController != null) {
            if (valuesController.thisPane.isFocused() || valuesController.thisPane.isFocusWithin()) {
                if (valuesController.keyEventsFilter(event)) {
                    return true;
                }
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (valuesController != null) {
            return valuesController.keyEventsFilter(event);
        }
        return false;
    }

    /*
        static methods
     */
    public static DataTreeNodeEditorController open() {
        DataTreeNodeEditorController controller = (DataTreeNodeEditorController) WindowTools
                .openStage(Fxmls.DataTreeNodeEditorFxml);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController open(BaseDataTreeController parent) {
        DataTreeNodeEditorController controller = open();
        controller.setParameters(parent, parent.nodeTable);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController loadNode(BaseDataTreeController parent,
            DataNode node, boolean execute) {
        DataTreeNodeEditorController controller = open(parent);
        controller.loadNode(node, execute);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController editNode(BaseDataTreeController parent, DataNode node) {
        return loadNode(parent, node, false);
    }

    public static DataTreeNodeEditorController addNode(BaseDataTreeController parent, DataNode parentNode) {
        DataTreeNodeEditorController controller = open(parent);
        controller.addNode(parentNode);
        controller.requestMouse();
        return controller;
    }

    public static DataTreeNodeEditorController executeNode(BaseDataTreeController parent, DataNode node) {
        return loadNode(parent, node, true);
    }

    public static DataTreeNodeEditorController loadNode(BaseController parent,
            BaseNodeTable table, DataNode node, boolean execute) {
        DataTreeNodeEditorController controller = open();
        controller.setParameters(parent, table);
        controller.loadNode(node, execute);
        return controller;
    }

    public static DataTreeNodeEditorController openTable(BaseController parent,
            BaseNodeTable table) {
        DataTreeNodeEditorController controller = open();
        controller.setParameters(parent, table);
        return controller;
    }

}
