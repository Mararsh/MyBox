package mara.mybox.controller;

import java.io.File;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
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
                    MenuMarkdownEditController.mdMenu(myController, textArea, event);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void editAction() {
        MarkdownEditorController.edit(textArea.getText());
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
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
                MarkdownEditorController.open(file);
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static MarkdownPopController open(BaseController parent, TextInputControl textInput) {
        try {
            if (textInput == null) {
                return null;
            }
            MarkdownPopController controller = (MarkdownPopController) WindowTools.openStage(Fxmls.MarkdownPopFxml);
            controller.setSourceInput(parent, textInput);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MarkdownPopController openFile(BaseController parent, String filename) {
        try {
            if (filename == null) {
                return null;
            }
            MarkdownPopController controller = (MarkdownPopController) WindowTools.openStage(Fxmls.MarkdownPopFxml);
            controller.setFile(parent, filename);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
