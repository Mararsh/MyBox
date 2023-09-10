package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.data.Tag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeAttributes extends TreeTagsController {

    protected BaseInfoTreeNodeEditor editor;
    protected InfoNode parentNode;
    protected SingletonTask tagsTask;

    @FXML
    protected TextField idInput, nameInput, timeInput;
    @FXML
    protected Label chainLabel, nameLabel, timeLabel;

    public ControlInfoTreeAttributes() {
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
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setParameters(TreeManageController treeController) {
        try {
            super.setParameters(treeController);
            this.editor = treeController.nodeController;

            nameLabel.setText(treeController.nameMsg);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void nodeChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        editor.nodeChanged(changed);
        treeController.nodeChanged();
        if (editor.attributesTab != null) {
            editor.attributesTab.setText(message("Attributes") + (changed ? "*" : ""));
        }
    }

    @Override
    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        nodeChanged(true);
        if (editor.attributesTab != null) {
            editor.attributesTab.setText(message("Attributes") + "*");
        }
        selectedNotify.set(!selectedNotify.get());
    }

    protected void editNode(InfoNode node) {
        currentNode = node;
        isSettingValues = true;
        if (node != null) {
            parentController.setTitle(parentController.baseTitle + ": " + node.getNodeid() + " - " + node.getTitle());
            idInput.setText(node.getNodeid() + "");
            nameInput.setText(node.getTitle());
            timeInput.setText(DateTools.datetimeToString(node.getUpdateTime()));
            selectButton.setVisible(node.getNodeid() < 0 || node.getParentid() < 0);
        } else {
            parentController.setTitle(parentController.baseTitle);
            idInput.setText(message("NewData"));
            nameInput.setText("");
            timeInput.setText("");
            selectButton.setVisible(true);
        }
        isSettingValues = false;
        nodeChanged(node == null);
        refreshParentNode();
        refreshAction();
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
        nodeChanged(true);
    }

    public void saveNode() {
        MyBoxLog.debug("here");
        InfoNode node = editor.pickNodeData();
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
