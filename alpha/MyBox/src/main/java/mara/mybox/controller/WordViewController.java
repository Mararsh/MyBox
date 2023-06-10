package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.Fxmls;
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
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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

    @Override
    public void pageLoaded() {

    }

    @FXML
    @Override
    public void refreshAction() {
        loadFile(sourceFile);
    }

    /*
        static
     */
    public static WordViewController open() {
        try {
            WordViewController controller = (WordViewController) WindowTools.openStage(Fxmls.WordViewFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static WordViewController openFile(File file) {
        WordViewController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

}
