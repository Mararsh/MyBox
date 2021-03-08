package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-20
 * @License Apache License Version 2.0
 */
public abstract class BaseHtmlController extends BaseController {

    protected WebView webView;
    protected WebEngine webEngine;
    protected Label webLabel;
    protected boolean needEdit, needSnap;

    @FXML
    protected ControlWebBrowserBox webviewController;

    public BaseHtmlController() {
        baseTitle = AppVariables.message("Html");
        needEdit = needSnap = false;

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            needEdit = needSnap = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initWebView();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initWebView() {
        try {
            webviewController.setValues(this, needSnap, needEdit);
            webView = webviewController.webView;
            webEngine = webviewController.webView.getEngine();
            webLabel = webviewController.bottomLabel;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        loadFile(file);
    }

    protected void updateTitle(boolean changed) {
        String t = getBaseTitle();
        if (webviewController.address != null) {
            t += "  " + webviewController.address;
        } else if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (changed) {
            t += "*";
        }
        getMyStage().setTitle(t);
    }

    public void loadContents(String contents) {
        webviewController.loadContents(contents);
        updateTitle(true);
    }

    public void loadAddress(String address) {
        webviewController.loadAddress(address);
        updateTitle(true);
    }

    public void load(String address, String contents) {
        webviewController.loadContents(contents);
        webviewController.setAddress(address);
        updateTitle(true);
    }

    public void loadFile(File file) {
        webviewController.loadFile(file);
        updateTitle(false);
    }

    @Override
    public boolean leavingScene() {
        if (timer != null) {
            timer.cancel();
        }
        if (webEngine != null && webEngine.getLoadWorker() != null) {
            webEngine.getLoadWorker().cancel();
        }
        webEngine = null;
        return super.leavingScene();
    }

}
