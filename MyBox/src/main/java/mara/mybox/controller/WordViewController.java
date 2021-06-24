package mara.mybox.controller;

import java.io.File;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public class WordViewController extends ControlWebview {

    public WordViewController() {
        baseTitle = AppVariables.message("WordView");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Word, VisitHistory.FileType.Html);
    }

    @Override
    public void sourceFileChanged(File file) {
        loadFile(file);
    }

    @Override
    public void loadFile(File file) {
        setSourceFile(file);
        if (file == null) {
            getMyStage().setTitle(getBaseTitle());
            return;
        }
        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String html;

                @Override
                protected boolean handle() {
                    html = MicrosoftDocumentTools.word2html(sourceFile, charset);
                    return html != null;
                }

                @Override
                protected void whenSucceeded() {
                    loadContents(html);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
