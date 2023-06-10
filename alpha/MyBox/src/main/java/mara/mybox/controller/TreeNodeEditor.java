package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.NodeSeparater;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class TreeNodeEditor extends TreeTagsController {

    protected boolean nodeChanged;
    protected String defaultExt;
    protected InfoNode parentNode;
    protected SingletonTask tagsTask;

    @FXML
    protected Tab valueTab, attributesTab;
    @FXML
    protected TextField idInput, nameInput, timeInput;
    @FXML
    protected TextInputControl valueInput, moreInput;
    @FXML
    protected Label chainLabel, nameLabel, valueLabel, moreLabel, timeLabel;
    @FXML
    protected CheckBox wrapCheck;

    public TreeNodeEditor() {
        defaultExt = "txt";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    if (isSettingValues) {
                        return;
                    }
                    nodeChanged(true);
                    if (attributesTab != null) {
                        attributesTab.setText(message("Attributes") + "*");
                    }
                }
            });

            if (valueInput != null) {
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        nodeChanged(true);
                        if (valueTab != null) {
                            valueTab.setText(treeController.valueMsg + "*");
                        } else if (attributesTab != null) {
                            attributesTab.setText(message("Attributes") + "*");
                        }
                    }
                });
            }

            if (moreInput != null) {
                moreInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        nodeChanged(true);
                        if (attributesTab != null) {
                            attributesTab.setText(message("Attributes") + "*");
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(TreeManageController treeController) {
        try {
            super.setParameters(treeController);

            nameLabel.setText(treeController.nameMsg);
            if (valueLabel != null) {
                valueLabel.setText(treeController.valueMsg);
            }
            if (moreLabel != null) {
                moreLabel.setText(treeController.moreMsg);
            }
            timeLabel.setText(treeController.timeMsg);

            treeController.tagsController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    isSettingValues = true;
                    tableData.setAll(treeController.tagsController.tableData);
                    isSettingValues = false;
                    markTags();
                }
            });

            if (wrapCheck != null && (valueInput instanceof TextArea)) {
                wrapCheck.setSelected(UserConfig.getBoolean(category + "ValueWrap", false));
                wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(category + "ValueWrap", newValue);
                        ((TextArea) valueInput).setWrapText(newValue);
                    }
                });
                ((TextArea) valueInput).setWrapText(wrapCheck.isSelected());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void nodeChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        nodeChanged = changed;
        treeController.nodeChanged();
    }

    @Override
    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        nodeChanged(true);
        if (attributesTab != null) {
            attributesTab.setText(message("Attributes") + "*");
        }
        selectedNotify.set(!selectedNotify.get());
    }

    protected void editNode(InfoNode node) {
        isSettingValues = true;
        currentNode = node;
        if (node != null) {
            parentController.setTitle(parentController.baseTitle + ": " + node.getNodeid() + " - " + node.getTitle());
            idInput.setText(node.getNodeid() + "");
            nameInput.setText(node.getTitle());
            if (valueInput != null) {
                valueInput.setText(node.getValue());
            }
            if (moreInput != null) {
                moreInput.setText(node.getMore());
            }
            timeInput.setText(DateTools.datetimeToString(node.getUpdateTime()));
            selectButton.setVisible(node.getNodeid() < 0 || node.getParentid() < 0);
        } else {
            parentController.setTitle(parentController.baseTitle);
            idInput.setText(message("NewData"));
            nameInput.setText("");
            if (valueInput != null) {
                valueInput.setText("");
            }
            if (moreInput != null) {
                moreInput.setText("");
            }
            timeInput.setText("");
            selectButton.setVisible(true);
        }
        isSettingValues = false;
        nodeChanged(false);
        refreshParentNode();
        refreshAction();
        showEditorPane();
        if (attributesTab != null) {
            attributesTab.setText(message("Attributes"));
        }
        if (valueTab != null) {
            valueTab.setText(treeController.valueMsg);
        }
    }

    protected void showEditorPane() {
        treeController.showRightPane();
    }

    protected void checkParentNode(InfoNode node) {
        if (parentNode == null || node.getNodeid() != parentNode.getNodeid()) {
            return;
        }
        refreshParentNode();
    }

    protected void setParentNode(InfoNode node) {
        parentNode = node;
        refreshParentNode();
    }

    protected void refreshParentNode() {
        SingletonTask updateTask = new SingletonTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (currentNode != null) {
                        if (currentNode.getParentid() >= 0) {
                            parentNode = tableTreeNode.find(conn, currentNode.getParentid());
                        } else {
                            parentNode = tableTreeNode.readData(conn, parentNode);
                        }
                    }
                    if (parentNode == null) {
                        chainName = "";
                    } else {
                        chainName = treeController.nodesController.chainName(conn, parentNode);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                chainLabel.setText(chainName);
            }
        };
        start(updateTask, false);
    }

    protected void copyNode() {
        isSettingValues = true;
        parentController.setTitle(parentController.baseTitle + ": " + message("NewData"));
        idInput.setText(message("NewData"));
        nameInput.appendText(" " + message("Copy"));
        currentNode = null;
        selectButton.setVisible(true);
        isSettingValues = false;
        nodeChanged(false);
    }

    public InfoNode pickNodeData() {
        String name = nameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameters") + ": " + treeController.nameMsg);
            if (tabPane != null && attributesTab != null) {
                tabPane.getSelectionModel().select(attributesTab);
            }
            return null;
        }
        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return null;
        }
        InfoNode node = InfoNode.create()
                .setCategory(category).setTitle(name);
        if (valueInput != null) {
            node.setValue(valueInput.getText());
        }
        if (moreInput != null) {
            node.setMore(moreInput.getText());
        }
        return node;
    }

    public void saveNode() {
        InfoNode node = pickNodeData();
        if (node == null) {
            return;
        }
        if (parentNode == null) {
            selectParent();
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private boolean newData = false;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    node.setUpdateTime(new Date());
                    if (currentNode != null) {
                        currentNode = tableTreeNode.readData(conn, currentNode);
                        if (currentNode == null) {
                            conn.close();
                            return true;
                        }
                        if (currentNode.getParentid() >= 0) {
                            parentNode = tableTreeNode.find(conn, currentNode.getParentid());
                        } else {
                            parentNode = tableTreeNode.readData(conn, parentNode);
                        }
                        if (parentNode == null) {
                            currentNode = null;
                            conn.close();
                            return true;
                        } else {
                            node.setNodeid(currentNode.getNodeid());
                            node.setParentid(parentNode.getNodeid());
                            currentNode = tableTreeNode.updateData(conn, node);
                            newData = false;
                        }
                    } else {
                        node.setParentid(parentNode.getNodeid());
                        currentNode = tableTreeNode.insertData(conn, node);
                        newData = true;
                    }
                    if (currentNode == null) {
                        conn.close();
                        return false;
                    }
                    long nodeid = currentNode.getNodeid();
                    List<String> nodeTags = tableTreeNodeTag.nodeTagNames(conn, nodeid);
                    List<Tag> selected = selectedItems();
                    if (selected == null || selected.isEmpty()) {
                        tableTreeNodeTag.removeTags(conn, nodeid);
                    } else {
                        List<String> selectedNames = new ArrayList<>();
                        for (Tag tag : selected) {
                            selectedNames.add(tag.getTag());
                        }
                        List<String> items = new ArrayList<>();
                        for (String tagName : selectedNames) {
                            if (!nodeTags.contains(tagName)) {
                                items.add(tagName);
                            }
                        }
                        tableTreeNodeTag.addTags(conn, nodeid, category, items);
                        items.clear();
                        for (String tagName : nodeTags) {
                            if (!selectedNames.contains(tagName)) {
                                items.add(tagName);
                            }
                        }
                        tableTreeNodeTag.removeTags(conn, nodeid, category, items);
                    }
                    conn.commit();
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
                return currentNode != null;
            }

            @Override
            protected void whenSucceeded() {
                if (parentNode == null) {
                    selectParent();
                }
                if (currentNode == null) {
                    copyNode();
                    popError(message("NotExist"));
                } else {
                    editNode(currentNode);
                    if (newData) {
                        treeController.newNodeSaved();
                    } else {
                        treeController.nodeSaved();
                    }
                    popSaved();
                }
            }

        };
        start(task, false);
    }

    @FXML
    @Override
    public void saveAsAction() {
        String codes = valueInput.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile(message(category) + "-" + DateTools.nowFileString() + "." + defaultExt);
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

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
        start(task);
    }

    @FXML
    public void clearValue() {
        valueInput.clear();
    }

    public void renamed(String newName) {
        if (nameInput == null) {
            return;
        }
        isSettingValues = true;
        nameInput.setText(newName);
        isSettingValues = false;
    }

    @FXML
    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        markTags();
    }

    public void loadFile(File file) {
        if (file == null || !file.exists() || !checkBeforeNextAction()) {
            return;
        }
        editNode(null);
        if (task != null) {
            task.cancel();
        }
        valueInput.clear();
        task = new SingletonCurrentTask<Void>(this) {

            String codes;

            @Override
            protected boolean handle() {
                codes = TextFileTools.readTexts(file);
                return codes != null;
            }

            @Override
            protected void whenSucceeded() {
                valueInput.setText(codes);
                recordFileOpened(file);
            }

        };
        start(task);
    }

    public void pasteText(String text) {
        if (valueInput == null || text == null || text.isEmpty()) {
            return;
        }
        valueInput.replaceText(valueInput.getSelection(), text);
        valueInput.requestFocus();
        tabPane.getSelectionModel().select(valueTab);
    }

    public void markTags() {
        if (tableData.isEmpty() || currentNode == null) {
            return;
        }
        if (tagsTask != null) {
            tagsTask.cancel();
        }
        tagsTask = new SingletonTask<Void>(this) {
            private List<String> nodeTags;

            @Override
            protected boolean handle() {
                nodeTags = tableTreeNodeTag.nodeTagNames(currentNode.getNodeid());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (nodeTags != null && !nodeTags.isEmpty()) {
                    isSettingValues = true;
                    for (Tag tag : tableData) {
                        if (nodeTags.contains(tag.getTag())) {
                            tableView.getSelectionModel().select(tag);
                        }
                    }
                    isSettingValues = false;
                }
            }

        };
        start(tagsTask, false);
    }

    @FXML
    @Override
    public void addTag() {
        treeController.tagsController.addTag(true);
    }

    @FXML
    public void selectParent() {
        TreeNodeParentController.open(this);
    }

    @Override
    public void cleanPane() {
        try {
            if (tagsTask != null) {
                tagsTask.cancel();
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
