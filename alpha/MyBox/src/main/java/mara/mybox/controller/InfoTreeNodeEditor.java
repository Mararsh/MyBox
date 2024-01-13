package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.TitleSeparater;
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
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class InfoTreeNodeEditor extends BaseController {

    protected InfoTreeManageController manager;
    protected String defaultExt;
    protected boolean nodeChanged;

    @FXML
    protected Tab attributesTab, valueTab;
    @FXML
    protected ControlInfoNodeAttributes attributesController;
    @FXML
    protected TextInputControl valueInput, moreInput;
    @FXML
    protected Label valueLabel;
    @FXML
    protected CheckBox wrapCheck;

    public InfoTreeNodeEditor() {
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

            if (valueInput != null) {
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        valueChanged(true);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(InfoTreeManageController treeController) {
        try {
            this.manager = treeController;
            attributesController.setParameters(manager);

            if (valueLabel != null) {
                valueLabel.setText(manager.valueMsg);
            }

            if (wrapCheck != null && (valueInput instanceof TextArea)) {
                wrapCheck.setSelected(UserConfig.getBoolean(manager.category + "ValueWrap", false));
                wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(treeController.category + "ValueWrap", newValue);
                        ((TextArea) valueInput).setWrapText(newValue);
                    }
                });
                ((TextArea) valueInput).setWrapText(wrapCheck.isSelected());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean editNode(InfoNode node) {
        updateEditorTitle(node);
        editInfo(node);
        attributesController.editNode(node);
        showEditorPane();
        nodeChanged(false);
        return true;
    }

    protected void updateEditorTitle(InfoNode node) {
        if (node != null) {
            manager.setTitle(manager.baseTitle + ": "
                    + node.getNodeid() + " - " + node.getTitle());
        } else {
            manager.setTitle(manager.baseTitle);
        }
    }

    protected void editInfo(InfoNode node) {
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

    public InfoNode pickNodeData() {
        String title = nodeTitle();
        if (title == null || title.isBlank()) {
            return null;
        }
        String info = nodeInfo();
        InfoNode node = InfoNode.create()
                .setCategory(manager.category)
                .setTitle(title)
                .setInfo(info);
        return node;
    }

    protected String nodeInfo() {
        String info = null;
        if (valueInput != null) {
            info = valueInput.getText();
        }
        return info;
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
        this.nodeChanged = changed;
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
        start(task);
    }

    public void pasteNode(InfoNode node) {
        if (valueInput == null || node == null) {
            return;
        }
        String v = InfoNode.majorInfo(node);
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
