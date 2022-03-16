package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class TreeLeafEditor extends TreeTagsController {

    protected boolean leafChanged;
    protected SimpleBooleanProperty changeNotify;

    @FXML
    protected TextField idInput, nameInput, timeInput;
    @FXML
    protected TextInputControl valueInput, moreInput;
    @FXML
    protected Label chainLabel, nameLabel, valueLabel, moreLabel, timeLabel;

    public TreeLeafEditor() {
        changeNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    leafChanged(true);
                }
            });

            if (valueInput != null) {
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        leafChanged(true);
                    }
                });
            }

            if (moreInput != null) {
                moreInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        leafChanged(true);
                    }
                });
            }

            if (valueInput != null) {
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        leafChanged(true);
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void leafChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        leafChanged = changed;
        changeNotify.set(!changeNotify.get());
    }

    @Override
    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        leafChanged(true);
        selectedNotify.set(!selectedNotify.get());
    }

    protected synchronized void editLeaf(TreeLeaf leaf) {
        isSettingValues = true;
        treeController.currentLeaf = leaf;
        if (leaf != null) {
            idInput.setText(leaf.getLeafid() + "");
            nameInput.setText(leaf.getName());
            if (valueInput != null) {
                valueInput.setText(leaf.getValue());
            }
            if (moreInput != null) {
                moreInput.setText(leaf.getMore());
            }
            timeInput.setText(DateTools.datetimeToString(leaf.getTime()));
            selectButton.setVisible(leaf.getLeafid() < 0);
        } else {
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
        leafChanged(false);
        updateNodeOfCurrentLeaf();
        refreshAction();
        showEditorPane();
    }

    protected void showEditorPane() {
        treeController.showRightPane();
    }

    protected void updateNodeOfCurrentLeaf() {
        SingletonTask updateTask = new SingletonTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    if (treeController.currentLeaf != null) {
                        if (treeController.nodeOfCurrentLeaf == null
                                || treeController.nodeOfCurrentLeaf.getNodeid() != treeController.currentLeaf.getParentid()) {
                            treeController.nodeOfCurrentLeaf = tableTree.find(conn, treeController.currentLeaf.getParentid());
                        }
                    }
                    if (treeController.nodeOfCurrentLeaf == null) {
                        treeController.nodeOfCurrentLeaf = treeController.nodesController.root(conn);
                    }
                    if (treeController.nodeOfCurrentLeaf == null) {
                        return false;
                    }
                    chainName = treeController.nodesController.chainName(conn, treeController.nodeOfCurrentLeaf);
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

    protected synchronized void copyLeaf() {
        isSettingValues = true;
        idInput.setText(message("NewData"));
        nameInput.appendText(" " + message("Copy"));
        treeController.currentLeaf = null;
        selectButton.setVisible(true);
        isSettingValues = false;
        leafChanged(true);
    }

    public void markTags() {
        if (task != null) {
            task.cancel();
        }
        if (tableData.isEmpty() || treeController.currentLeaf == null) {
            return;
        }
        task = new SingletonTask<Void>(this) {
            private List<String> leafTags;

            @Override
            protected boolean handle() {
                leafTags = tableTreeLeafTag.leafTagNames(treeController.currentLeaf.getLeafid());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (leafTags != null && !leafTags.isEmpty()) {
                    isSettingValues = true;
                    for (Tag tag : tableData) {
                        if (leafTags.contains(tag.getTag())) {
                            tableView.getSelectionModel().select(tag);
                        }
                    }
                    isSettingValues = false;
                }
            }

            @Override
            protected void finalAction() {
                thisPane.setDisable(false);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void addTag() {
        treeController.tagsController.addTag(true);
    }

    @FXML
    public void selectParent() {
        if (treeController.currentLeaf != null && treeController.currentLeaf.getLeafid() >= 0) {
            selectButton.setVisible(false);
            return;
        }
        TreeLeafParentController.open(treeController);
    }

}
