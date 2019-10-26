package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import mara.mybox.data.VisitHistory;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonImageValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-30
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlViewerController extends BaseController {

    protected String html;
    protected String title;

    @FXML
    protected WebView webView;

    public HtmlViewerController() {
        baseTitle = AppVariables.message("Html");
        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        targetPathKey = "HtmlFilePath";

        sourceExtensionFilter = CommonImageValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void toFront() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        myStage.toFront();
                        webView.requestFocus();
                        timer = null;
                    }
                });
            }
        }, 1000);
    }

    public void load(String html) {
        try {
            this.html = html;
            if (html == null) {
                webView.getEngine().loadContent​("");
            } else {
                webView.getEngine().loadContent​(html);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void clear() {
        html = null;
        webView.getEngine().loadContent("");
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            if (html == null) {
                return;
            }
            String name = title;
            if (name != null) {
                name += ".htm";
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            try (BufferedWriter out = new BufferedWriter(new FileWriter(file, Charset.forName("utf-8"), false))) {
                out.write(html);
                out.flush();
            }

            popSuccessul();

        } catch (Exception e) {
            logger.error(e.toString());
            popError(e.toString());
        }

    }

}
