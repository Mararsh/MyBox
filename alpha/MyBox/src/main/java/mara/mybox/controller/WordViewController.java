package mara.mybox.controller;

import java.io.File;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public class WordViewController extends BaseWebViewController {

    public WordViewController() {
        baseTitle = Languages.message("WordView");
        TipsLabelKey = "WordViewTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.WordS, VisitHistory.FileType.Html);
    }

    public boolean loadFile(File file) {
        if (file == null) {
            getMyStage().setTitle(getBaseTitle());
            return false;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return false;
            }
            task = new SingletonTask<Void>() {

                private String html;

                @Override
                protected boolean handle() {
                    String suffix = FileNameTools.getFileSuffix(file);
                    if ("doc".equalsIgnoreCase(suffix)) {
                        html = MicrosoftDocumentTools.word2html(file, getCharset());
                    } else if ("docx".equalsIgnoreCase(suffix)) {
                        String text = MicrosoftDocumentTools.extractText(file);
                        if (text == null) {
                            return false;
                        }
                        html = text.replaceAll("\n", "<BR>\n");
                    } else {
                        error = Languages.message("NotSupport");
                        return false;
                    }
                    return html != null;
                }

                @Override
                protected void whenSucceeded() {
                    setSourceFile(file);
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                    loadContents(html);
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
            return true;
        }
    }

}
