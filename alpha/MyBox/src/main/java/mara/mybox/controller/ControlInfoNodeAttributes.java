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
import mara.mybox.db.data.InfoNodeTag;
import mara.mybox.db.data.Tag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ControlInfoNodeAttributes extends InfoTreeTagsController {

    protected InfoTreeManageController manager;
    protected InfoTreeNodeEditor editor;
    protected InfoNode parentNode;
    protected FxTask tagsTask;

    @FXML
    protected TextField idInput, nameInput, timeInput;
    @FXML
    protected Label parentLabel, chainLabel, nameLabel, timeLabel;

    public ControlInfoNodeAttributes() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    attributesChanged();
                }
            });

            if (parentLabel != null) {
                parentLabel.setText(message("ParentNode") + ": ");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(InfoTreeManageController manager) {
        try {
            super.setParameters(manager);
            this.manager = manager;
            this.editor = manager.editor;

            nameLabel.setText(manager.nameMsg);
            timeLabel.setText(manager.timeMsg);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        attributes
     */
    public void attributesChanged() {
        if (isSettingValues || editor == null) {
            return;
        }
        if (editor.attributesTab != null) {
            editor.attributesTab.setText(message("Attributes") + "*");
        }
        editor.nodeChanged(true);
    }

    protected void editNode(InfoNode node) {
        currentNode = node;
        isSettingValues = true;
        if (node != null) {
            idInput.setText(node.getNodeid() + "");
            nameInput.setText(node.getTitle());
            timeInput.setText(DateTools.datetimeToString(node.getUpdateTime()));
            selectButton.setVisible(node.getNodeid() < 0 || node.getParentid() < 0);
        } else {
            idInput.setText(message("NewData"));
            nameInput.setText("");
            timeInput.setText("");
            selectButton.setVisible(true);
        }
        isSettingValues = false;
        refreshParentNode();
        refreshAction();
    }

    protected void copyNode() {
        isSettingValues = true;
        parentController.setTitle(parentController.baseTitle + ": " + message("NewData"));
        idInput.setText(message("NewData"));
        nameInput.appendText(" " + message("Copy"));
        currentNode = null;
        selectButton.setVisible(true);
        isSettingValues = false;
        attributesChanged();
    }

    public void saveNode() {
        InfoNode node = editor.pickNodeData();
        if (node == null) {
            return;
        }
        if (parentNode == null) {
            selectParent();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
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
                    editor.editNode(currentNode);
                    if (newData) {
                        manager.newNodeSaved();
                    } else {
                        manager.nodeSaved();
                    }
                    popSaved();
                }
            }

        };
        start(task);
    }

    public void renamed(String newName) {
        if (nameInput == null) {
            return;
        }
        isSettingValues = true;
        nameInput.setText(newName);
        isSettingValues = false;
    }

    /*
        parent
     */
    @FXML
    public void selectParent() {
        InfoTreeNodeParentController.open(this);
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
        FxTask updateTask = new FxTask<Void>(this) {
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
                        chainName = manager.treeController.chainName(conn, parentNode);
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


    /*
        tags
     */
    @Override
    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        attributesChanged();
        selectedNotify.set(!selectedNotify.get());
    }

    @FXML
    @Override
    public void addTag() {
        String name = askName();
        if (name == null || name.isBlank()) {
            return;
        }
        FxTask tagTask = new FxTask<Void>(this) {
            private Tag tag = null;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tag = tableTag.insertData(conn, new Tag(category, name));
                    if (tag != null && currentNode != null) {
                        tableTreeNodeTag.insertData(conn,
                                new InfoNodeTag(currentNode.getNodeid(), tag.getTgid()));
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return tag != null;
            }

            @Override
            protected void whenSucceeded() {
                tableData.add(0, tag);
                tableView.getSelectionModel().select(tag);
                attributesChanged();
                popSuccessful();
                tagsChanged();
            }

        };
        start(tagTask, thisPane);
    }

    @Override
    public void tagsChanged() {
        if (isSettingValues) {
            return;
        }
        manager.tagsController.isSettingValues = true;
        manager.tagsController.tableData.setAll(tableData);
        manager.tagsController.isSettingValues = false;
    }

    public void synchronizeTags() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        tableData.setAll(manager.tagsController.tableData);
        isSettingValues = false;
        if (tableData.isEmpty() || currentNode == null) {
            return;
        }
        if (tagsTask != null) {
            tagsTask.cancel();
        }
        tagsTask = new FxTask<Void>(this) {
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
