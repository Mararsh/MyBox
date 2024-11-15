package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.data.DataValues;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
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
    protected boolean nodeExecutable;
    protected BaseDataTable dataTable;
    protected TableDataNode nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected DataNode parentNode, currentNode;
    protected DataValues dataValues;

    @FXML
    protected Tab nodeTab, dataTab, tagsTab;
    @FXML
    protected TextField titleInput;
    @FXML
    protected ScrollPane dataPane;
    @FXML
    protected ControlDataNodeAttributes attributesController;
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
            dataTable = treeController.dataTable;
            nodeTable = treeController.nodeTable;
            tagTable = treeController.tagTable;
            nodeTagsTable = treeController.nodeTagsTable;

            baseName = baseName + "_" + dataTable.getTableName();
            saveButton = controller.saveButton;
            addButton = controller.addButton;
            copyButton = controller.copyButton;
            recoverButton = controller.recoverButton;

            attributesController.setParameters(treeController);
            tagsController.setParameters(this);

            dataController = (BaseDataValuesController) WindowTools.loadFxml(dataTable.getFxml());
            dataPane.setContent(dataController.getMyScene().getRoot());
            dataController.refreshStyle();
            dataController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean editNode(DataNode node) {
        if (!treeController.checkBeforeNextAction()) {
            return false;
        }
        if (node == null) {
            addAction();
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {

            protected DataValues values;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    dataValues = node.dataValues(conn, dataTable);
                    currentNode = node;
                    parentNode = nodeTable.query(conn, currentNode.getParentid());
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    attributesController.loadAttributes();
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

    protected String nodeTitle() {
        String title = attributesController.titleInput.getText();
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
        attributesController.changed = false;
        updateStatus();
    }

    protected void updateStatus() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (dataTab != null) {
            dataTab.setText(message(dataTable.getTableTitle()) + (dataController.changed ? "*" : ""));
        }
        if (nodeTab != null) {
            nodeTab.setText(message("Node") + (attributesController.changed ? "*" : ""));
        }
        if (tagsTab != null) {
            tagsTab.setText(message("Tags") + (tagsController.changed ? "*" : ""));
        }
        boolean changed = dataController.changed || attributesController.changed || tagsController.changed;
        String title = treeController.baseTitle;
        if (currentNode != null) {
            title += ": "
                    + (currentNode.getNodeid() < 0 ? message("NewData") : currentNode.getNodeid())
                    + " - " + currentNode.getTitle();
            treeController.showRightPane();
        }
        setTitle(title + (changed ? " *" : ""));

        saveButton.setDisable(!changed);
        recoverButton.setDisable(!changed);
        copyButton.setDisable(currentNode == null);

        boolean isValid = dataValues != null && parentNode != null;
        thisPane.setVisible(isValid);
        saveButton.setVisible(isValid);
        addButton.setVisible(isValid);
        copyButton.setVisible(isValid);
        recoverButton.setVisible(isValid);

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

    @FXML
    @Override
    public void saveAction() {
        DataNode node = attributesController.pickAttributes();
        if (node == null) {
            popError(message("Invalid") + ": " + message("Node"));
            return;
        }
        DataValues values = dataController.pickValues();
        if (values == null) {
            popError(message("Invalid") + ": " + message("Value"));
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
                    DataValues savedValues = (DataValues) dataTable.writeData(conn, values);
                    if (savedValues == null) {
                        conn.close();
                        error = message("Failed");
                        return false;
                    }
                    conn.commit();

                    nodeid = savedValues.getId(dataTable);
                    if (nodeid < 0) {
                        conn.close();
                        error = message("Failed");
                        return false;
                    }
                    node.setNodeid(nodeid);
                    node.setUpdateTime(new Date());

                    savedNode = nodeTable.writeData(conn, node);
                    if (savedNode == null) {
                        conn.close();
                        return false;
                    }
                    conn.commit();

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
            MyBoxLog.console(currentNode != null);
            if (!treeController.checkBeforeNextAction()) {
                return;
            }
            parentNode = currentNode;
            currentNode = DataNode.create();
            currentNode.setParentid(parentNode != null ? parentNode.getNodeid() : -1);
            currentNode.setTitle(message("Node") + new Date().getTime());
            dataValues = null;

            attributesController.loadAttributes();
            tagsController.loadTags();
            dataController.editValues();

            dataController.changed = false;
            tagsController.changed = false;
            attributesController.changed = false;
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
            if (dataValues != null) {
                dataValues = dataValues.copy();
                dataValues.setId(dataTable, -1);
            }

            attributesController.loadAttributes();
            tagsController.loadedTags.clear();
            dataController.editValues();

            dataController.changed = true;
            tagsController.changed = true;
            attributesController.changed = true;
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

    @FXML
    @Override
    public void saveAsAction() {
//        if (valueInput == null) {
//            return;
//        }
//        String codes = valueInput.getText();
//        if (codes == null || codes.isBlank()) {
//            popError(message("NoData"));
//            return;
//        }
//        File file = chooseSaveFile(message(manager.category) + "-" + DateTools.nowFileString() + "." + defaultExt);
//        if (file == null) {
//            return;
//        }
//        FxTask saveAsTask = new FxTask<Void>(this) {
//
//            @Override
//            protected boolean handle() {
//                File tfile = TextFileTools.writeFile(file, codes, Charset.forName("UTF-8"));
//                return tfile != null && tfile.exists();
//            }
//
//            @Override
//            protected void whenSucceeded() {
//                popInformation(message("Saved"));
//                recordFileWritten(file);
//            }
//
//        };
//        start(saveAsTask, false);
    }

    @Override
    public void sourceFileChanged(File file) {
//        if (valueInput == null
//                || file == null || !file.exists()
//                || !manager.checkBeforeNextAction()) {
//            return;
//        }
//        editNode(null);
//        if (task != null) {
//            task.cancel();
//        }
//        valueInput.clear();
//        task = new FxSingletonTask<Void>(this) {
//
//            String codes;
//
//            @Override
//            protected boolean handle() {
//                codes = TextFileTools.readTexts(this, file);
//                return codes != null;
//            }
//
//            @Override
//            protected void whenSucceeded() {
//                valueInput.setText(codes);
//                recordFileOpened(file);
//            }
//
//        };
//        start(task, thisPane);
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
        if (attributesController.thisPane.isFocused() || attributesController.thisPane.isFocusWithin()) {
            if (attributesController.keyEventsFilter(event)) {
                return true;
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
