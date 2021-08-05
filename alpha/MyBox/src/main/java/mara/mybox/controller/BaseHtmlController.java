package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-20
 * @License Apache License Version 2.0
 */
public abstract class BaseHtmlController extends BaseController {

    protected WebView webView;
    protected WebEngine webEngine;
    protected Label webLabel;

    @FXML
    protected ControlWebBrowserBox webviewController;

    public BaseHtmlController() {
        baseTitle = Languages.message("Html");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
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
            if (webviewController == null) {
                return;
            }
            webviewController.setParameters(this);
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

    protected void updateStatus(boolean changed) {
        String t = getBaseTitle();
        if (webviewController.address != null) {
            t += "  " + webviewController.address;
        } else if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        getMyStage().setTitle(t);
    }

    public void loadContents(String contents) {
        if (!checkBeforeNextAction()) {
            return;
        }
        webviewController.loadContents(contents);
        updateStatus(true);
    }

    public void loadAddress(String address) {
        if (!checkBeforeNextAction()) {
            return;
        }
        webviewController.loadAddress(address);
        updateStatus(true);
    }

    public void load(String address, String contents) {
        if (!checkBeforeNextAction()) {
            return;
        }
        webviewController.loadContents(contents);
        webviewController.setAddress(address);
        updateStatus(true);
    }

    public void loadFile(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        webviewController.loadFile(file);
        updateStatus(false);
    }

    @Override
    public void cleanPane() {
        if (timer != null) {
            timer.cancel();
        }
        if (webEngine != null && webEngine.getLoadWorker() != null) {
            webEngine.getLoadWorker().cancel();
        }
        webEngine = null;
        super.cleanPane();
    }

}
