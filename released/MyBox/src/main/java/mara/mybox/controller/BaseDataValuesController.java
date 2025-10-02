package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableStringValues;
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
 * @CreateDate 2024-11-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataValuesController extends BaseFileController {

    protected DataTreeNodeEditorController nodeEditor;
    protected BaseNodeTable nodeTable;
    protected boolean changed;
    protected TextInputControl valueInput;
    protected CheckBox valueWrapCheck;
    protected String valueName;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setParameters(DataTreeNodeEditorController controller) {
        try {
            this.nodeEditor = controller;
            this.parentController = nodeEditor;
            this.baseName = nodeEditor.baseName + "_" + baseName;
            nodeTable = nodeEditor.nodeTable;

            initEditor();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initEditor() {
        try {
            if (valueInput != null) {
                listenChanged(valueInput);

                if (valueWrapCheck != null) {
                    manageWrapped(valueWrapCheck, (TextArea) valueInput);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void listenChanged(TextInputControl input) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue v, String ov, String nv) {
                if (!isSettingValues) {
                    valueChanged(true);
                }
            }
        });
    }

    public void manageWrapped(CheckBox check, TextArea input) {
        check.setSelected(UserConfig.getBoolean(baseName + "Wrap", false));
        check.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                UserConfig.setBoolean(baseName + "Wrap", check.isSelected());
                input.setWrapText(check.isSelected());
            }
        });
        input.setWrapText(check.isSelected());
    }

    protected void editValues() {
        try {
            if (valueInput == null || valueName == null) {
                return;
            }
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                valueInput.setText(nodeEditor.currentNode.getStringValue(valueName));
            } else {
                valueInput.clear();
            }
            isSettingValues = false;
            valueChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected DataNode pickValues(DataNode node) {
        try {
            String value = valueInput.getText();
            if (value != null && !value.isBlank()) {
                value = value.trim();
                TableStringValues.add(baseName + "Histories", value);
                node.setValue(valueName, value);
            } else {
                node.setValue(valueName, null);
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void valueChanged(boolean changed) {
        if (isSettingValues || nodeEditor == null) {
            return;
        }
        this.changed = changed;
        nodeEditor.updateStatus();
    }

    @FXML
    @Override
    public void clearAction() {
        if (valueInput != null) {
            valueInput.clear();
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (valueInput == null || file == null || !file.exists()
                || !nodeEditor.checkBeforeNextAction()) {
            return;
        }
        nodeEditor.loadNode(null, false);
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
        File file = saveAsFile(baseTitle + "-" + DateTools.nowFileString());
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
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "HistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        if (valueInput != null) {
            PopTools.popSavedValues(this, valueInput, event, baseName + "Histories");
        }
    }

}
