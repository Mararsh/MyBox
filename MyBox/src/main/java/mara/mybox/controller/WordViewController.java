package mara.mybox.controller;

import java.io.File;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public class WordViewController extends BaseWebViewController {

    public WordViewController() {
        baseTitle = AppVariables.message("WordView");
        TipsLabelKey = "WordViewTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.WordS, VisitHistory.FileType.Html);
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
                    String suffix = FileTools.getFileSuffix(sourceFile);
                    if ("doc".equalsIgnoreCase(suffix)) {
                        html = MicrosoftDocumentTools.word2html(sourceFile, charset);
                    } else if ("docx".equalsIgnoreCase(suffix)) {
                        String text = MicrosoftDocumentTools.extractText(sourceFile);
                        if (text == null) {
                            return false;
                        }
                        html = text.replaceAll("\n", "<BR>\n");
                    } else {
                        error = AppVariables.message("NotSupport");
                        return false;
                    }
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
