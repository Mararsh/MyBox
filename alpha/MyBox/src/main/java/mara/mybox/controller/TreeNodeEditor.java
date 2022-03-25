package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
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
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
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
    protected SimpleBooleanProperty changeNotify;
    protected String defaultExt;

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
        changeNotify = new SimpleBooleanProperty(false);
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
                        nodeChanged(true);
                        if (valueTab != null) {
                            valueTab.setText(treeController.valueMsg + "*");
                        }
                    }
                });
            }

            if (moreInput != null) {
                moreInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
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
        changeNotify.set(!changeNotify.get());
        if (changed) {
            if (!treeController.getTitle().endsWith(" *")) {
                treeController.setTitle(treeController.getTitle() + " *");
            }
        } else {
            if (treeController.getTitle().endsWith(" *")) {
                treeController.setTitle(treeController.getTitle().substring(0, treeController.getTitle().length() - 2));
            }
        }
    }

    @Override
    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        nodeChanged(true);
        selectedNotify.set(!selectedNotify.get());
    }

    protected synchronized void editNode(TreeNode node) {
        isSettingValues = true;
        treeController.currentNode = node;
        if (node != null) {
            treeController.setTitle(treeController.baseTitle + ": " + node.getNodeid() + " - " + node.getTitle());
            idInput.setText(node.getNodeid() + "");
            nameInput.setText(node.getTitle());
            if (valueInput != null) {
                valueInput.setText(node.getValue());
            }
            if (moreInput != null) {
                moreInput.setText(node.getMore());
            }
            timeInput.setText(DateTools.datetimeToString(node.getUpdateTime()));
            selectButton.setVisible(node.getNodeid() < 0);
        } else {
            treeController.setTitle(treeController.baseTitle);
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
        updateParentNode();
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

    protected void updateParentNode() {
        SingletonTask updateTask = new SingletonTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    if (treeController.currentNode != null) {
                        if (treeController.parentNode == null
                                || treeController.parentNode.getNodeid() != treeController.currentNode.getParentid()) {
                            treeController.parentNode = tableTreeNode.find(conn, treeController.currentNode.getParentid());
                        }
                    }
                    if (treeController.parentNode == null) {
                        treeController.parentNode = treeController.nodesController.root(conn);
                    }
                    if (treeController.parentNode == null) {
                        return false;
                    }
                    chainName = treeController.nodesController.chainName(conn, treeController.parentNode);
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

    protected synchronized void copyNode() {
        isSettingValues = true;
        idInput.setText(message("NewData"));
        nameInput.appendText(" " + message("Copy"));
        treeController.currentNode = null;
        selectButton.setVisible(true);
        isSettingValues = false;
        nodeChanged(true);
    }

    public TreeNode pickNodeData() {
        String name = nameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameters") + ": " + treeController.nameMsg);
            if (tabPane != null && attributesTab != null) {
                tabPane.getSelectionModel().select(attributesTab);
            }
            return null;
        }
        TreeNode node = TreeNode.create()
                .setCategory(category).setTitle(name);
        if (valueInput != null) {
            node.setValue(valueInput.getText());
        }
        if (moreInput != null) {
            node.setMore(moreInput.getText());
        }
        return node;
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
        task = new SingletonTask<Void>(this) {

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
        task = new SingletonTask<Void>(this) {

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
        valueInput.replaceText(valueInput.getSelection(), text);
        valueInput.requestFocus();
    }

    public void markTags() {
        if (task != null) {
            task.cancel();
        }
        if (tableData.isEmpty() || treeController.currentNode == null) {
            return;
        }
        task = new SingletonTask<Void>(this) {
            private List<String> nodeTags;

            @Override
            protected boolean handle() {
                nodeTags = tableTreeNodeTag.nodeTagNames(treeController.currentNode.getNodeid());
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
        if (treeController.currentNode != null && treeController.currentNode.getNodeid() >= 0) {
            selectButton.setVisible(false);
            return;
        }
        TreeNodeParentController.open(treeController);
    }

}
