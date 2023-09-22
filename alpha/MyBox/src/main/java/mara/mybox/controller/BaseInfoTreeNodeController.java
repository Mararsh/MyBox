package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class BaseInfoTreeNodeController extends BaseController {

    protected TreeManageController manager;
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

    public BaseInfoTreeNodeController() {
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

    public void setParameters(TreeManageController treeController) {
        try {
            this.manager = treeController;
            attributesController.setParameters(treeController);

            if (valueLabel != null) {
                valueLabel.setText(treeController.valueMsg);
            }

            if (wrapCheck != null && (valueInput instanceof TextArea)) {
                wrapCheck.setSelected(UserConfig.getBoolean(treeController.category + "ValueWrap", false));
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

    protected void editNode(InfoNode node) {
        updateTitle(node);
        editInfo(node);
        attributesController.editNode(node);
        showEditorPane();
        nodeChanged(false);
    }

    protected void updateTitle(InfoNode node) {
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

    @FXML
    @Override
    public void saveAsAction() {
        String codes = valueInput.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile(message(manager.category) + "-" + DateTools.nowFileString() + "." + defaultExt);
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
        if (valueInput != null) {
            valueInput.clear();
        }
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

}
