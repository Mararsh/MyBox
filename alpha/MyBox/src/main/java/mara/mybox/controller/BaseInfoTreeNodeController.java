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
import static mara.mybox.db.data.InfoNode.NodeSeparater;
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

    protected TreeManageController treeController;
    protected String defaultExt;
    protected boolean nodeChanged;

    @FXML
    protected Tab attributesTab, valueTab;
    @FXML
    protected ControlInfoTreeAttributes attributesController;
    @FXML
    protected TextInputControl valueInput, moreInput;
    @FXML
    protected Label valueLabel, moreLabel;
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
                        if (isSettingValues) {
                            return;
                        }
                        attributesController.nodeChanged(true);
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
                        attributesController.nodeChanged(true);
                        if (attributesTab != null) {
                            attributesTab.setText(message("Attributes") + "*");
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(TreeManageController treeController) {
        try {
            this.treeController = treeController;
            attributesController.setParameters(treeController);

            if (valueLabel != null) {
                valueLabel.setText(treeController.valueMsg);
            }
            if (moreLabel != null) {
                moreLabel.setText(treeController.moreMsg);
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
        isSettingValues = true;
        if (node != null) {
            if (valueInput != null) {
                valueInput.setText(node.getValue());
            }
            if (moreInput != null) {
                moreInput.setText(node.getMore());
            }
        } else {
            if (valueInput != null) {
                valueInput.setText("");
            }
            if (moreInput != null) {
                moreInput.setText("");
            }
        }
        isSettingValues = false;

        attributesController.editNode(node);
        showEditorPane();
        if (valueTab != null) {
            valueTab.setText(treeController.valueMsg);
        }
    }

    public InfoNode pickNodeData() {
        String name = attributesController.nameInput.getText();
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

        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return null;
        }
        InfoNode node = InfoNode.create()
                .setCategory(treeController.category).setTitle(name);

        if (valueInput != null) {
            node.setValue(valueInput.getText());
        }
        if (moreInput != null) {
            node.setMore(moreInput.getText());
        }
        return node;
    }

    protected void showEditorPane() {
        treeController.showRightPane();
    }

    public void nodeChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        this.nodeChanged = changed;
    }

    @FXML
    @Override
    public void saveAsAction() {
        String codes = valueInput.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile(message(treeController.category) + "-" + DateTools.nowFileString() + "." + defaultExt);
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
