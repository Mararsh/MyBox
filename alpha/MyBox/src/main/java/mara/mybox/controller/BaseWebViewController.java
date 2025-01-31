package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WebViewTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-18
 * @License Apache License Version 2.0
 */
public class BaseWebViewController extends BaseFileController {

    protected WebView webView;
    protected WebEngine webEngine;

    @FXML
    protected ControlWebView webViewController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            if (webViewController != null) {
                webViewController.setParent(this);
                webView = webViewController.webView;
                webEngine = webViewController.webEngine;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (webViewController != null) {

                webViewController.addressChangedNotify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                        addressChanged();
                    }
                });

                webViewController.addressInvalidNotify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                        addressInvalid();
                    }
                });

                webViewController.pageLoadingNotify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                        pageLoading();
                    }
                });

                webViewController.pageLoadedNotify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                        pageLoaded();
                    }
                });

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    public void setScrollType(ControlWebView.ScrollType scrollType) {
        webViewController.scrollType = scrollType;
    }

    @Override
    public void sourceFileChanged(File file) {
        loadFile(file);
    }

    @Override
    public void setSourceFile(File file) {
        if (file == null || !file.exists()) {
            popError(message("InvalidAddress"));
            return;
        }
        if (webViewController != null) {
            webViewController.setSourceFile(file);
        }
    }

    public boolean loadFile(File file) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        if (file == null || !file.exists()) {
            popError(message("InvalidData"));
            return false;
        }
        if (webViewController != null) {
            boolean ret = webViewController.loadFile(file);
            if (ret) {
                sourceFile = file;
            }
            return ret;
        } else {
            return false;
        }
    }

    public boolean loadAddress(String address) {
        if (!checkBeforeNextAction() || webViewController == null) {
            return false;
        }
        sourceFile = null;
        boolean ret = webViewController.loadAddress(address);
        if (ret) {
            sourceFile = webViewController.sourceFile;
        }
        return ret;
    }

    public boolean loadContents(String contents) {
        if (!checkBeforeNextAction() || webViewController == null) {
            return false;
        }
        sourceFile = null;
        return webViewController.loadContent(contents);
    }

    public boolean loadContents(String address, String contents) {
        if (!checkBeforeNextAction() || webViewController == null) {
            return false;
        }
        sourceFile = null;
        boolean ret = webViewController.loadContent(address, contents);
        if (ret) {
            sourceFile = webViewController.sourceFile;
        }
        return ret;
    }

    public void pageLoading() {

    }

    public void pageLoaded() {
        updateStageTitle();
    }

    public void addressChanged() {

    }

    public void addressInvalid() {
        popError(message("InvalidAddress"));
    }

    public String title() {
        String title = getBaseTitle();
        String htmlTitle = webViewController.title();
        if (htmlTitle != null) {
            title += " - " + htmlTitle;
        }
        return title;
    }

    public void initStyle(String style) {
        webViewController.initStyle(style);
    }

    public void updateStageTitle() {
        setTitle(title());
    }

    public void setWebViewLabel(String string) {
        if (webViewController == null) {
            return;
        }
        webViewController.setWebViewLabel(string);
    }

    public Charset getCharset() {
        if (webViewController == null) {
            return null;
        }
        return webViewController.charset;
    }

    public String html() {
        if (webViewController == null) {
            return null;
        }
        return webViewController.currentHtml();
    }

    public boolean hasHtml() {
        String html = html();
        return html != null && !html.isBlank();
    }

    public void clear() {
        if (webViewController == null) {
            return;
        }
        webViewController.clear();
    }

    @FXML
    @Override
    public void popOperationsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.popOperationsMenu(event);
    }

    @FXML
    @Override
    public void showOperationsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.showOperationsMenu(event);
    }

    @FXML
    @Override
    public void popFunctionsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.popFunctionsMenu(event);
    }

    @FXML
    @Override
    public void showFunctionsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.showFunctionsMenu(event);
    }

    @FXML
    public void editAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.editAction();
    }

    @FXML
    @Override
    public void clearAction() {
        loadContents("");
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
    @Override
    public void refreshAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.refresh();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.saveAsAction();
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
    protected void showHtmlStyle(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.showHtmlStyle(event);
    }

    @FXML
    protected void popHtmlStyle(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.popHtmlStyle(event);
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

    @FXML
    public void snapAction() {
        ImageEditorController.openImage(NodeTools.snap(webView));
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (webViewController == null) {
            return false;
        }
        return webViewController.keyEventsFilter(event);
    }

    @Override
    public boolean controlAltB() {
        saveAsAction();
        return true;
    }

}
