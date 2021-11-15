package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.SingletonTask;
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
            task = new SingletonTask<Void>(this) {

                private File htmlFile;

                @Override
                protected boolean handle() {
                    htmlFile = MicrosoftDocumentTools.word2HtmlFile(file, getCharset());
                    return htmlFile != null;
                }

                @Override
                protected void whenSucceeded() {
                    sourceFile = file;
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                    webViewController.loadFile(htmlFile);
                }

            };
            start(task);
            return true;
        }
    }

    @Override
    protected void afterPageLoaded(boolean addressChanged) {

    }

    @FXML
    @Override
    public void refreshAction() {
        loadFile(sourceFile);
    }
}
