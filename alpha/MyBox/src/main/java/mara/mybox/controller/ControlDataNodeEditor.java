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
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-3
 * @License Apache License Version 2.0
 */
public class ControlDataNodeEditor extends BaseController {

    protected DataTreeController treeController;
    protected BaseDataValuesController dataController;

    protected String defaultExt;
    protected final SimpleBooleanProperty nodeChanged;
    protected boolean nodeExecutable, attributesChanged;
    protected BaseNodeTable nodeTable;
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
    protected Label chainLabel;
    @FXML
    protected ControlDataNodeTags tagsController;

    public ControlDataNodeEditor() {
        defaultExt = "txt";
        nodeChanged = new SimpleBooleanProperty(false);
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setParameters(DataTreeController controller) {
        try {
            treeController = controller;
            nodeTable = treeController.nodeTable;
            tagTable = treeController.tagTable;
            nodeTagsTable = treeController.nodeTagsTable;

            baseName = baseName + "_" + nodeTable.getTableName();
            saveButton = controller.saveButton;
            addButton = controller.addButton;
            copyButton = controller.copyButton;
            recoverButton = controller.recoverButton;

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

            updateStatus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean editNode(DataNode node) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        if (node == null) {
            currentNode = null;
            addAction();
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    currentNode = nodeTable.query(conn, node.getNodeid());
                    if (currentNode != null) {
                        parentNode = nodeTable.query(conn, currentNode.getParentid());
                    } else {
                        parentNode = null;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    loadAttributes();
                    tagsController.loadTags();
                    dataController.editValues();
                    resetStatus();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

        };
        start(task, thisPane);
        return true;
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

    protected String nodeTitle() {
        String title = titleInput.getText();
        if (title == null || title.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Title"));
            if (tabPane != null && nodeTab != null) {
                tabPane.getSelectionModel().select(nodeTab);
            }
            return null;
        }
        if (title.contains(TitleSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + TitleSeparater + "\"");
            return null;
        }
        return title;
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
        String title = treeController.baseTitle;
        if (currentNode != null) {
            title += ": "
                    + (currentNode.getNodeid() < 0 ? message("NewData") : currentNode.getNodeid())
                    + " - " + currentNode.getTitle();
            treeController.showRightPane();
        }
        setTitle(title + (changed ? " *" : ""));

        thisPane.setVisible(currentNode != null && parentNode != null);

        nodeChanged.set(changed);
        isSettingValues = false;
    }

    public void newNodeCreated() {
        popInformation(message("InputNewNode"));
        if (tabPane != null && nodeTab != null) {
            tabPane.getSelectionModel().select(nodeTab);
        }
    }

    public boolean isNewNode() {
        return currentNode == null || currentNode.getNodeid() < 0;
    }

    public void refreshNode() {
        if (parentNode != null) {
            parentNode = nodeTable.query(parentNode.getNodeid());
            if (parentNode == null) {
                resetStatus();
                editNode(null);
                return;
            }
        }

        if (currentNode != null) {
            currentNode = nodeTable.query(currentNode.getNodeid());
            if (currentNode == null) {
                resetStatus();
                editNode(null);
            }
        }

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
        if (currentNode.getNodeid() < 0) {
            idInput.setText(message("NewData"));
        } else {
            idInput.setText(currentNode.getNodeid() + "");
        }
        orderInput.setText(currentNode.getOrderNumber() + "");
        timeInput.setText(DateTools.datetimeToString(currentNode.getUpdateTime()));
        isSettingValues = false;

        selectButton.setVisible(currentNode.getNodeid() < 0 || parentNode == null);
        refreshParentNode();
        attributesChanged(false);
    }

    public void renamed(String newName) {
        if (titleInput == null) {
            return;
        }
        isSettingValues = true;
        titleInput.setText(newName);
        isSettingValues = false;
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
//        InfoTreeNodeParentController.open(this);
    }
//
//    protected void checkParentNode(DataNode node) {
//        if (parentNode == null || node.getNodeid() != parentNode.getNodeid()) {
//            return;
//        }
//        refreshParentNode();
//    }
//
//    protected void setParentNode(DataNode node) {
//        parentNode = node;
//        refreshParentNode();
//    }

    protected void refreshParentNode() {
        task = new FxTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (parentNode == null) {
                        chainName = "";
                    } else {
                        chainName = treeController.chainName(conn, parentNode);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                chainLabel.setText(message("ParentNode") + ": " + chainName);
            }
        };
        start(task, thisPane);
    }

    /*
        actions
     */
    @FXML
    @Override
    public void saveAction() {
        if (parentNode == null) {
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
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode savedNode;
            private long nodeid;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    node.setNodeid(attributes.getNodeid());
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
                if (isNewNode()) {
                    treeController.addNewNode(parentNode, savedNode);
                } else {
                    treeController.updateNode(savedNode);
                }
                resetStatus();
                editNode(savedNode);
                popSaved();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void addAction() {
        try {
            if (!treeController.checkBeforeNextAction()) {
                return;
            }
            parentNode = currentNode;
            currentNode = DataNode.create();
            currentNode.setParentid(parentNode != null ? parentNode.getNodeid() : -1);
            currentNode.setTitle(message("Node") + new Date().getTime());

            loadAttributes();
            tagsController.loadTags();
            dataController.editValues();

            dataController.changed = false;
            tagsController.changed = false;
            attributesChanged = false;
            updateStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void copyAction() {
        try {
            if (!treeController.checkBeforeNextAction()) {
                return;
            }
            if (currentNode == null) {
                addAction();
                return;
            }
            currentNode = currentNode.copy();

            loadAttributes();
            tagsController.loadedTags.clear();
            dataController.editValues();

            dataController.changed = true;
            tagsController.changed = true;
            attributesChanged = true;
            updateStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        resetStatus();
        editNode(currentNode);
    }

    public void pasteNode(DataNode node) {
//        if (valueInput == null || node == null) {
//            return;
//        }
//        String v = TreeNode.majorInfo(node);
//        if (v == null || v.isBlank()) {
//            return;
//        }
//        valueInput.replaceText(valueInput.getSelection(), v);
//        valueInput.requestFocus();
//        tabPane.getSelectionModel().select(valueTab);
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

}
