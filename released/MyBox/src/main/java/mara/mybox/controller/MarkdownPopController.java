package mara.mybox.controller;

import java.io.File;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public class MarkdownPopController extends TextPopController {

    public MarkdownPopController() {
        baseTitle = "Markdown";
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Markdown);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            textArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuMarkdownEditController.open(myController, textArea, event);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        MarkdownEditorController controller = (MarkdownEditorController) WindowTools.openStage(Fxmls.MarkdownEditorFxml);
        controller.loadContents(textArea.getText());
        controller.requestMouse();
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
            task = new SingletonTask<Void>(this) {
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
                    MarkdownEditorController controller = (MarkdownEditorController) WindowTools.openStage(Fxmls.MarkdownEditorFxml);
                    controller.sourceFileChanged(file);
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static MarkdownPopController open(BaseController parent, TextInputControl textInput) {
        try {
            if (textInput == null) {
                return null;
            }
            MarkdownPopController controller = (MarkdownPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.MarkdownPopFxml, false);
            controller.setSourceInput(parent.baseName, textInput);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
