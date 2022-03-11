package mara.mybox.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeLeafTag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
import mara.mybox.db.table.TableTreeLeafTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class TreeLeafEditor extends BaseController {

    protected TreeManageController treeController;
    protected TreeNode treeNode, nodeOfCurrentLeaf;
    protected TableTree tableTree;
    protected TableTreeLeaf tableTreeLeaf;
    protected TableTag tableTag;
    protected TableTreeLeafTag tableTreeLeafTag;
    protected boolean tagsChanged;

    @FXML
    protected TextField idInput, nameInput, valueInput, moreInput, timeInput;
    @FXML
    protected Label nameLabel, valueLabel, moreLabel, timeLabel;
    @FXML
    protected Button addTreeLeafTagButton;
    @FXML
    protected ControlCheckBoxList tagsController;

    public void setParameters(TreeManageController treeController) {
        try {
            this.treeController = treeController;
            this.baseName = treeController.baseName;
            tableTree = treeController.tableTree;
            tableTag = new TableTag();
            tableTreeLeafTag = new TableTreeLeafTag();
            saveButton = treeController.saveButton;

            nameLabel.setText(treeController.nameMsg);
            valueLabel.setText(treeController.valueMsg);
            if (moreLabel != null) {
                moreLabel.setText(treeController.moreMsg);
            }
            timeLabel.setText(treeController.timeMsg);

            tagsController.setParent(treeController);
            refreshTags();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void editLeaf(TreeLeaf leaf) {
        synchronized (this) {
            treeController.currentLeaf = leaf;
            if (leaf != null) {
                idInput.setText(leaf.getLeafid() + "");
                nameInput.setText(leaf.getName());
                valueInput.setText(leaf.getValue());
                if (moreInput != null) {
                    moreInput.setText(leaf.getMore());
                }
                timeInput.setText(DateTools.datetimeToString(leaf.getTime()));
            } else {
                idInput.setText("");
                nameInput.setText("");
                valueInput.setText("");
                if (moreInput != null) {
                    moreInput.setText("");
                }
                timeInput.setText("");
            }
            refreshTags();
            treeController.updateNodeOfCurrentLeaf();
            treeController.showRightPane();
        }
    }

    @FXML
    protected void copyLeaf() {
        idInput.setText("");
        nameInput.appendText(" " + message("Copy"));
        treeController.currentLeaf = null;
    }

    public void saveLeaf() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                popError(message("InvalidData") + ": " + message("Title"));
                return;
            }
            String value = valueInput.getText();
            if (TreeNode.WebFavorite.equals(treeController.category)) {
                try {
                    URL url = new URL(value);
                } catch (Exception e) {
                    popError(message("InvalidData") + ": " + message("Address"));
                    return;
                }
            }
            String more = moreInput != null ? moreInput.getText() : null;
            task = new SingletonTask<Void>(this) {
                private TreeLeaf data;
                private boolean notExist = false;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        data = new TreeLeaf();
                        data.setName(name);
                        data.setValue(value);
                        data.setMore(more);
                        data.setTime(new Date());
                        if (treeController.currentLeaf != null) {
                            treeController.currentLeaf = tableTreeLeaf.readData(conn, treeController.currentLeaf);
                            nodeOfCurrentLeaf = tableTree.readData(conn, nodeOfCurrentLeaf);
                            if (treeController.currentLeaf == null || nodeOfCurrentLeaf == null) {
                                notExist = true;
                                treeController.currentLeaf = null;
                                return true;
                            } else {
                                data.setLeafid(treeController.currentLeaf.getLeafid());
                                data.setParentid(treeController.currentLeaf.getParentid());
                                treeController.currentLeaf = tableTreeLeaf.updateData(conn, data);
                            }
                        } else {
                            if (nodeOfCurrentLeaf == null) {
                                nodeOfCurrentLeaf = treeController.nodesController.root(conn);
                            }
                            data.setParentid(nodeOfCurrentLeaf.getNodeid());
                            treeController.currentLeaf = tableTreeLeaf.insertData(conn, data);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return treeController.currentLeaf != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (notExist) {
                        copyLeaf();
                        popError(message("NotExist"));
                    } else {
                        popSaved();
                        editLeaf(treeController.currentLeaf);
                        if (treeController.nodesController.selectedNode != null
                                && treeController.currentLeaf.getParentid() == treeController.nodesController.selectedNode.getNodeid()) {
                            treeController.refreshAction();
                        }
                    }
                }

            };
            start(task, false);
        }
    }

    @FXML
    public void refreshTags() {
        synchronized (this) {
            tagsController.clear();
            thisPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>(this) {
                private List<String> tagsString;
                private List<Integer> selected;

                @Override
                protected boolean handle() {
                    tagsString = new ArrayList<>();
                    selected = new ArrayList<>();
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<Tag> tags = tableTag.readAll(conn);
                        if (tags != null && !tags.isEmpty()) {
                            for (Tag tag : tags) {
                                tagsString.add(tag.getTag());
                            }
                            if (treeController.currentLeaf != null) {
                                List<Long> tagIDs = tableTreeLeafTag.readTags(conn, treeController.currentLeaf.getLeafid());
                                if (tagIDs != null && !tagIDs.isEmpty()) {
                                    for (int i = 0; i < tags.size(); i++) {
                                        Tag tag = tags.get(i);
                                        if (tagIDs.contains(tag.getTgid())) {
                                            selected.add(i);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tagsController.setValues(tagsString);
                    tagsController.setCheckIndices(selected);
                    tagsChanged(false);
                }

                @Override
                protected void finalAction() {
                    thisPane.setDisable(false);
                }

            };
            start(noteTagsTask, false);
        }
    }

    @FXML
    public void selectAllTags() {
        tagsController.checkAll();
        tagsChanged(true);
    }

    @FXML
    public void selectNoneTags() {
        tagsController.checkNone();
        tagsChanged(true);
    }

    @FXML
    public void addTag() {
        treeController.tagsController.addTag(true);
    }

    @FXML
    public void okTags() {
        if (treeController.currentLeaf == null) {
            return;
        }
        synchronized (this) {
            treeController.rightPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    List<String> selected = tagsController.checkedValues();
                    long leafid = treeController.currentLeaf.getLeafid();
                    try ( Connection conn = DerbyBase.getConnection();
                             PreparedStatement query = conn.prepareStatement(TableTag.QueryTag);
                             PreparedStatement delete = conn.prepareStatement(TableTreeLeafTag.DeleteLeafTags)) {
                        delete.setLong(1, leafid);
                        delete.executeUpdate();
                        conn.commit();
                        if (selected != null) {
                            List<TreeLeafTag> tags = new ArrayList<>();
                            for (String value : selected) {
                                Tag tag = tableTag.query(conn, query, value);
                                if (tag == null) {
                                    continue;
                                }
                                tags.add(new TreeLeafTag(leafid, tag.getTgid()));
                            }
                            tableTreeLeafTag.insertList(conn, tags);
                        }
                        conn.commit();
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    tagsChanged(false);
                }

                @Override
                protected void finalAction() {
                    treeController.rightPane.setDisable(false);
                }

            };
            start(noteTagsTask, false);
        }
    }

    public void tagsChanged(boolean changed) {
        tagsChanged = changed;
    }

}
