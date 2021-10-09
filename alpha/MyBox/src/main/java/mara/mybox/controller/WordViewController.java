package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
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

    @Override
    public void sourceFileChanged(File file) {
        loadFile(file);
    }

    @Override
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
                    Charset charset = getCharset();
                    if ("doc".equalsIgnoreCase(suffix)) {
                        html = MicrosoftDocumentTools.word2html(file, charset);
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
                    sourceFile = file;
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                    webViewController.loadContents(null, html);
                }

            };
            start(task);
            return true;
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadFile(sourceFile);
    }
}
