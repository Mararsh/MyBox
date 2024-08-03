package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.db.data.TreeNode.TitleSeparater;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2024-8-3
 * @License Apache License Version 2.0
 */
public class BaseDataTreeNodeController extends BaseController {

    protected DataTreeManageController manager;
    protected String defaultExt;
    protected final SimpleBooleanProperty nodeChanged;
    protected BaseController valuesEditor;

    @FXML
    protected Tab attributesTab, valueTab, tagsTab;
    @FXML
    protected ControlDataTreeNodeAttributes attributesController;
    @FXML
    protected ControlDataTreeNodeTags tagsController;

    public BaseDataTreeNodeController() {
        defaultExt = "txt";
        nodeChanged = new SimpleBooleanProperty(false);
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setManager(DataTreeManageController treeController) {
        try {
            this.manager = treeController;
            attributesController.setParameters(manager);
            tagsController.setParameters(manager);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean editNode(TreeNode node) {
        updateEditorTitle(node);
        attributesController.editNode(node);
        editValue(node);
        showEditorPane();
        nodeChanged(false);
        return true;
    }

    protected void updateEditorTitle(TreeNode node) {
        if (node != null) {
            manager.setTitle(manager.baseTitle + ": "
                    + node.getNodeid() + " - " + node.getTitle());
        } else {
            manager.setTitle(manager.baseTitle);
        }
    }

    protected void editValue(TreeNode node) {
        if (valueInput == null) {
            return;
        }
        isSettingValues = true;
        if (node != null) {
            valueInput.setText(node.getInfo());
        } else {
            valueInput.setText("");
        }
        isSettingValues = false;
    }

    public TreeNode pickNodeData() {
        String title = nodeTitle();
        if (title == null || title.isBlank()) {
            return null;
        }
        TreeNode node = TreeNode.create()
                .setDataTable(manager.dataTable)
                .setTitle(title);
        return pickValue(node);
    }

    public void saveNode() {
//        TreeNode node = editor.pickNodeData();
//        if (node == null) {
//            return;
//        }
//        if (parentNode == null) {
//            selectParent();
//            return;
//        }
//        if (task != null) {
//            task.cancel();
//        }
//        task = new FxSingletonTask<Void>(this) {
//            private boolean newData = false;
//
//            @Override
//            protected boolean handle() {
//                try (Connection conn = DerbyBase.getConnection()) {
//                    node.setUpdateTime(new Date());
//                    if (currentNode != null) {
//                        currentNode = tableTree.readData(conn, currentNode);
//                        if (currentNode == null) {
//                            conn.close();
//                            return true;
//                        }
//                        if (currentNode.getParentid() >= 0) {
//                            parentNode = tableTree.find(conn, currentNode.getParentid());
//                        } else {
//                            parentNode = tableTree.readData(conn, parentNode);
//                        }
//                        if (parentNode == null) {
//                            currentNode = null;
//                            conn.close();
//                            return true;
//                        } else {
//                            node.setNodeid(currentNode.getNodeid());
//                            node.setParentid(parentNode.getNodeid());
//                            currentNode = tableTree.updateData(conn, node);
//                            newData = false;
//                        }
//                    } else {
//                        node.setParentid(parentNode.getNodeid());
//                        currentNode = tableTree.insertData(conn, node);
//                        newData = true;
//                    }
//                    if (currentNode == null) {
//                        conn.close();
//                        return false;
//                    }
//                    long nodeid = currentNode.getNodeid();
//                    List<String> nodeTags = tableTreeTag.nodeTagNames(conn, nodeid);
//                    List<Tag> selected = selectedItems();
//                    if (selected == null || selected.isEmpty()) {
//                        tableTreeNodeTag.removeTags(conn, nodeid);
//                    } else {
//                        List<String> selectedNames = new ArrayList<>();
//                        for (Tag tag : selected) {
//                            selectedNames.add(tag.getTag());
//                        }
//                        List<String> items = new ArrayList<>();
//                        for (String tagName : selectedNames) {
//                            if (!nodeTags.contains(tagName)) {
//                                items.add(tagName);
//                            }
//                        }
//                        tableTreeNodeTag.addTags(conn, nodeid, category, items);
//                        items.clear();
//                        for (String tagName : nodeTags) {
//                            if (!selectedNames.contains(tagName)) {
//                                items.add(tagName);
//                            }
//                        }
//                        tableTreeNodeTag.removeTags(conn, nodeid, category, items);
//                    }
//                    conn.commit();
//                } catch (Exception e) {
//                    error = e.toString();
//                    MyBoxLog.error(e);
//                    return false;
//                }
//                return currentNode != null;
//            }
//
//            @Override
//            protected void whenSucceeded() {
//                if (parentNode == null) {
//                    selectParent();
//                }
//                if (currentNode == null) {
//                    copyNode();
//                    popError(message("NotExist"));
//                } else {
//                    editor.editNode(currentNode);
//                    if (newData) {
//                        manager.newNodeSaved();
//                    } else {
//                        manager.nodeSaved();
//                    }
//                    popSaved();
//                }
//            }
//
//        };
//        start(task);
    }

    protected TreeNode pickValue(TreeNode node) {
        if (node == null) {
            return null;
        }
        String info = null;
        if (valueInput != null) {
            info = valueInput.getText();
        }
        return node.setInfo(info);
    }

    protected String nodeTitle() {
        String title = attributesController.nameInput.getText();
        if (title == null || title.isBlank()) {
            popError(message("InvalidParameters") + ": " + manager.nameMsg);
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

    protected void showEditorPane() {
        manager.showRightPane();
    }

    public void valueChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        if (valueTab != null) {
            valueTab.setText(manager.valueMsg + (changed ? "*" : ""));
        }
        if (changed) {
            nodeChanged(changed);
        }
    }

    public void nodeChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        nodeChanged.set(changed);
        if (!changed) {
            if (valueTab != null) {
                valueTab.setText(manager.valueMsg);
            }
            if (attributesTab != null) {
                attributesTab.setText(message("Attributes"));
            }
        }
    }

    public void newNodeCreated() {
        popInformation(message("InputNewNode"));
        if (tabPane != null && attributesTab != null) {
            tabPane.getSelectionModel().select(attributesTab);
        }
    }

    public boolean isNewNode() {
        return attributesController.currentNode == null;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (valueInput == null) {
            return;
        }
        String codes = valueInput.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile(message(manager.category) + "-" + DateTools.nowFileString() + "." + defaultExt);
        if (file == null) {
            return;
        }
        FxTask saveAsTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                File tfile = TextFileTools.writeFile(file, codes, Charset.forName("UTF-8"));
                return tfile != null && tfile.exists();
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Saved"));
                recordFileWritten(file);
            }

        };
        start(saveAsTask, false);
    }

    @FXML
    public void clearValue() {
        if (valueInput != null) {
            valueInput.clear();
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (valueInput == null
                || file == null || !file.exists()
                || !manager.checkBeforeNextAction()) {
            return;
        }
        editNode(null);
        if (task != null) {
            task.cancel();
        }
        valueInput.clear();
        task = new FxSingletonTask<Void>(this) {

            String codes;

            @Override
            protected boolean handle() {
                codes = TextFileTools.readTexts(this, file);
                return codes != null;
            }

            @Override
            protected void whenSucceeded() {
                valueInput.setText(codes);
                recordFileOpened(file);
            }

        };
        start(task, thisPane);
    }

    public void pasteNode(TreeNode node) {
        if (valueInput == null || node == null) {
            return;
        }
        String v = TreeNode.majorInfo(node);
        if (v == null || v.isBlank()) {
            return;
        }
        valueInput.replaceText(valueInput.getSelection(), v);
        valueInput.requestFocus();
        tabPane.getSelectionModel().select(valueTab);
    }

    protected String editorName() {
        return manager.category;
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(editorName() + "HistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, valueInput, event, editorName() + "Histories", false);
    }

}
