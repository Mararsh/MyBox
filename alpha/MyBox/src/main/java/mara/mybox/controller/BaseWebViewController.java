package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-18
 * @License Apache License Version 2.0
 */
public class BaseWebViewController extends BaseController {

    protected WebView webView;
    protected WebEngine webEngine;

    @FXML
    protected ControlWebView webViewController;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            if (webViewController == null) {
                return;
            }
            webViewController.setParent(this);
            webView = webViewController.webView;
            webEngine = webViewController.webEngine;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (webViewController == null) {
            return;
        }
        webViewController.loadFile(file);
    }

    @Override
    public void setSourceFile(File file) {
        this.sourceFile = file;
        if (webViewController == null) {
            return;
        }
        webViewController.setSourceFile(file);
    }

    public boolean loadFile(File file) {
        if (webViewController == null) {
            return false;
        }
        return webViewController.loadFile(file);
    }

    public boolean loadAddress(String value) {
        if (webViewController == null) {
            return false;
        }
        return webViewController.loadAddress(value);
    }

    public boolean loadContents(String contents) {
        if (webViewController == null) {
            return false;
        }
        return webViewController.loadContents(contents);
    }

    public boolean loadContents(String address, String contents) {
        if (webViewController == null) {
            return false;
        }
        return webViewController.loadContents(address, contents);
    }

    public void setAddress(String value) {
        if (webViewController == null) {
            return;
        }
        webViewController.setAddress(value);
    }

    public String getAddress() {
        if (webViewController == null) {
            return null;
        }
        return webViewController.address;
    }

    public void addressChanged() {
    }

    public boolean validAddress(String value) {
        return checkBeforeNextAction();
    }

    protected void pageIsLoading() {

    }

    protected void afterPageLoaded() {
        try {
            updateStageTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void updateStageTitle() {
        if (myStage == null) {
            return;
        }
        String title = getBaseTitle();
        if (getAddress() != null) {
            title += "  " + getAddress();
        } else if (sourceFile != null) {
            title += "  " + sourceFile.getAbsolutePath();
        }
        myStage.setTitle(title);
    }

    protected void afterSaveAs(File file) {

    }

    protected void setWebViewLabel(String string) {
        if (webViewController == null) {
            return;
        }
        webViewController.setWebViewLabel(string);
    }

    protected void setAddressChanged(boolean changed) {
        if (webViewController == null) {
            return;
        }
        webViewController.addressChanged = changed;
    }

    protected boolean getAddressChanged() {
        if (webViewController == null) {
            return false;
        }
        return webViewController.addressChanged;
    }

    protected Charset getCharset() {
        if (webViewController == null) {
            return null;
        }
        return webViewController.charset;
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        if (webViewController == null) {
            return;
        }
        webViewController.popFunctionsMenu(mouseEvent);
    }

    @FXML
    public void editAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.edit(WebViewTools.getHtml(webEngine));
    }

    @FXML
    @Override
    public void findAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.find(WebViewTools.getHtml(webEngine));
    }

    @FXML
    public void zoomIn() {
        if (webViewController == null) {
            return;
        }
        webViewController.zoomIn();
    }

    @FXML
    public void zoomOut() {
        if (webViewController == null) {
            return;
        }
        webViewController.zoomOut();
    }

    @FXML
    public void backAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.backAction();
    }

    @FXML
    public void forwardAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.forwardAction();
    }

    @FXML
    public void refreshAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.refreshAction();
    }

    @FXML
    @Override
    public void cancelAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.cancelAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (webViewController == null) {
            return false;
        }
        return webViewController.popAction();
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (webViewController == null) {
            return false;
        }
        return webViewController.menuAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (webViewController == null) {
                return false;
            }
            return webViewController.keyEventsFilter(event);
        }
        return true;
    }

}
