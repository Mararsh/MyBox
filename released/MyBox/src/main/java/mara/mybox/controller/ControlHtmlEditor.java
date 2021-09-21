package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.web.HTMLEditor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-18
 * @License Apache License Version 2.0
 */
public class ControlHtmlEditor extends ControlWebView {

    @FXML
    protected HTMLEditor htmlEditor;

    @Override
    public void initControls() {
        try {
            webView = WebViewTools.webview(htmlEditor);
            initWebView();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
