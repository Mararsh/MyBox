package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-7
 * @License Apache License Version 2.0
 */
public class TextPopController extends BaseController {

    protected String separateLine;
    protected Clipboard clipboard;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox wrapCheck, openCheck;

    public TextPopController() {
        baseTitle = message("Texts");
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));
            saveAsButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

            openCheck.setSelected(UserConfig.getBoolean(baseName + "Open", true));
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Open", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setStageStatus(String prefix, int minSize) {
        setAsPopup(baseName);
    }

    public void loadText(String text) {
        textArea.setText(text);
    }

    @FXML
    public void editAction() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textArea.getText());
        controller.toFront();
    }

    @FXML
    @Override
    public void saveAsAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            File file = chooseSaveFile();
            if (file == null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        File tmpFile = TextFileTools.writeFile(textArea.getText());
                        return FileTools.rename(tmpFile, file);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    recordFileWritten(file);
                    if (openCheck.isSelected()) {
                        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
                        controller.sourceFileChanged(file);
                    }
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static TextPopController open(BaseController parent, String text) {
        try {
            if (text == null) {
                return null;
            }
            TextPopController controller = (TextPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.TextPopFxml, false);
            controller.loadText(text);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
