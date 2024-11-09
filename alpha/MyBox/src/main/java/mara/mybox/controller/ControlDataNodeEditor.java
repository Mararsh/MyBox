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

    protected DataTreeController dataController;
    protected ControlDataTreeView treeController;
    protected BaseDataNodeValues dataEditor;

    protected String defaultExt;
    protected final SimpleBooleanProperty nodeChanged;
    protected BaseController valuesEditor;
    protected boolean nodeExecutable, valueChanged;
    protected BaseDataTable dataTable;
    protected TableDataNode dataNodeTable;
    protected TableDataTag dataTagTable;
    protected TableDataNodeTag dataNodeTagTable;
    protected DataNode parentNode, currentNode;
    protected DataValues dataValues;

    @FXML
    protected Tab attributesTab, valueTab, tagsTab;
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
            dataController = controller;
            dataTable = dataController.dataTable;
            dataNodeTable = dataController.dataNodeTable;
            dataTagTable = dataController.dataTagTable;
            dataNodeTagTable = dataController.dataNodeTagTable;
            treeController = dataController.treeController;
            saveButton = controller.saveButton;
            addButton = controller.addButton;
            copyButton = controller.copyButton;
            recoverButton = controller.recoverButton;

            attributesController.setParameters(dataController);
            tagsController.setParameters(this);

            dataEditor = (BaseDataNodeValues) WindowTools.loadFxml(dataTable.getFxml());
            dataPane.setContent(dataEditor.getMyScene().getRoot());
            dataEditor.refreshStyle();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean editNode(DataNode node) {
        if (node == null) {
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
                    DataValues values = (DataValues) dataTable.query(conn, node.getNodeid());
                    if (values == null) {
                        return false;
                    }
                    dataValues = values;
                    currentNode = node;
                    parentNode = dataNodeTable.query(conn, currentNode.getParentid());
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return currentNode != null;
            }

            @Override
            protected void whenSucceeded() {
                loadNode();
            }

        };
        start(task, thisPane);
        return true;
    }

    protected void loadNode() {
        try {
            attributesController.loadAttributes();
            tagsController.loadTags(currentNode);
            dataEditor.editValues(dataValues);
            resetStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected String nodeTitle() {
        String title = attributesController.titleInput.getText();
        if (title == null || title.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Title"));
            if (tabPane != null && attributesTab != null) {
                tabPane.getSelectionModel().select(attributesTab);
            }
            return null;
        }
        if (title.contains(TitleSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + TitleSeparater + "\"");
            return null;
        }
        return title;
    }

    public void valueChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        valueChanged = changed;
        if (valueTab != null) {
            valueTab.setText(dataController.dataTable.getTableTitle() + (changed ? "*" : ""));
        }
        updateStatus();
    }

    public void attributesChanged() {
        if (isSettingValues) {
            return;
        }
        if (attributesTab != null) {
            attributesTab.setText(message("Node") + (attributesController.changed ? "*" : ""));
        }
        updateStatus();
    }

    public void tagsChanged() {
        if (isSettingValues) {
            return;
        }
        if (tagsTab != null) {
            tagsTab.setText(message("Tags") + (tagsController.changed ? "*" : ""));
        }
        updateStatus();
    }

    public void resetStatus() {
        valueChanged = false;
        tagsController.changed = false;
        attributesController.changed = false;
        updateStatus();
    }

    protected void updateStatus() {
        boolean changed = valueChanged || attributesController.changed || tagsController.changed;
        if (!changed) {
            if (valueTab != null) {
                valueTab.setText(dataController.dataTable.getTableTitle());
            }
            if (attributesTab != null) {
                attributesTab.setText(message("Node"));
            }
            if (tagsTab != null) {
                tagsTab.setText(message("Tags"));
            }
        }
        String title = dataController.baseTitle;
        if (currentNode != null) {
            title += ": " + currentNode.getNodeid() + " - " + currentNode.getTitle();
            dataController.showRightPane();
        }
        setTitle(title + (changed ? " *" : ""));

        saveButton.setDisable(!changed);
        recoverButton.setDisable(!changed);
        copyButton.setDisable(currentNode == null);

        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        nodeChanged.set(changed);
        isSettingValues = false;
    }

    public void newNodeCreated() {
        popInformation(message("InputNewNode"));
        if (tabPane != null && attributesTab != null) {
            tabPane.getSelectionModel().select(attributesTab);
        }
    }

    public boolean isNewNode() {
        return currentNode == null;
    }

    @FXML
    @Override
    public void saveAction() {
        DataNode node = attributesController.pickAttributes();
        if (node == null) {
            popError(message("Invalid") + ": " + message("Node"));
            return;
        }
        DataValues values = dataEditor.pickNodeValues();
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

                    nodeid = (long) savedValues.getValue(dataTable.getIdColumnName());
                    if (nodeid < 0) {
                        conn.close();
                        error = message("Failed");
                        return false;
                    }
                    node.setNodeid(nodeid);
                    node.setUpdateTime(new Date());
                    savedNode = dataNodeTable.writeData(conn, node);
                    if (savedNode == null) {
                        conn.close();
                        return false;
                    }
                    conn.commit();

                    dataNodeTagTable.setAll(conn, savedNode.getNodeid(),
                            tagsController.selectedItems());
                    conn.commit();
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
                return savedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                long id;
                try {
                    id = (long) values.getValue(dataTable.getIdColumnName());
                } catch (Exception e) {
                    id = -2;
                }
                if (id < 0) {
                    treeController.addNewNode(treeController.find(parentNode), savedNode, false);
                } else {
                    treeController.updateNode(savedNode);
                }
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
            currentNode = DataNode.create().setDataTable(dataTable);
            if (parentNode != null) {
                currentNode.setParentid(parentNode.getNodeid());
            }
            currentNode.setTitle(message("Node") + new Date().getTime());
            dataValues = null;
            attributesController.loadAttributes();
            tagsController.loadTags(currentNode);
            dataEditor.editValues(dataValues);
            valueChanged = false;
            tagsController.changed = false;
            attributesController.changed = true;
            updateStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void copyAction() {
        try {
            if (currentNode == null) {
                addAction();
                return;
            }
            currentNode.setNodeid(-1);
            currentNode.setTitle(currentNode.getTitle() + " " + message("Copy"));
            attributesController.changed = true;
            updateStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
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

    @FXML
    public void clearValue() {
//        if (valueInput != null) {
//            valueInput.clear();
//        }
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
        if (valuesEditor != null) {
            if (valuesEditor.thisPane.isFocused() || valuesEditor.thisPane.isFocusWithin()) {
                if (valuesEditor.keyEventsFilter(event)) {
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
        if (valuesEditor != null) {
            return valuesEditor.keyEventsFilter(event);
        }
        return false;
    }

}
