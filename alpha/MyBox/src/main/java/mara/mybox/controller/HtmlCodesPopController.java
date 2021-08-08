package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public class HtmlCodesPopController extends BaseController {

    protected String separateLine;
    protected Clipboard clipboard;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox wrapCheck, openCheck;

    public HtmlCodesPopController() {
        baseTitle = message("HtmlCodes");
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            textArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuHtmlCodesController.open(myController, textArea, event);
                }
            });

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
        HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
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
                        File tmpFile = HtmlWriteTools.writeHtml(textArea.getText());
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
                        HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
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
    public static HtmlCodesPopController open(BaseController parent, String text) {
        try {
            if (text == null) {
                return null;
            }
            HtmlCodesPopController controller = (HtmlCodesPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlCodesPopFxml, false);
            controller.loadText(text);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
