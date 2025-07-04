package mara.mybox.controller;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTmpTools;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import thridparty.pdfdom.PDFDomTree;
import thridparty.pdfdom.PDFDomTreeConfig;
import thridparty.pdfdom.SaveResourceToDirHandler;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public abstract class PdfViewController_Html extends PdfViewController_Texts {

    protected final String checkBottomScript, checkTopScript;
    protected PdfInformation pdfInformation;
    protected File htmlFile, subPath;
    protected PDFDomTreeConfig domConfig;
    protected WebView webView;
    protected WebEngine webEngine;
    protected boolean atTop, atBottom, setScroll;
    protected Task htmlTask;
    protected int htmlPage;

    @FXML
    protected ControlWebView webViewController;

    public PdfViewController_Html() {
        checkBottomScript
                = " function checkBottom() { "
                + "     var scrollTop = document.documentElement.scrollTop||document.body.scrollTop;  "
                + "     var windowHeight = document.documentElement.clientHeight || document.body.clientHeight; "
                + "     var scrollHeight = document.documentElement.scrollHeight||document.body.scrollHeight;"
                //                + "     alert(scrollTop + ' ' + windowHeight + ' ' + scrollHeight);  "
                + "     if ( scrollTop + windowHeight - scrollHeight < 50 || windowHeight > scrollHeight) {  "
                + "          alert('AtBottom');  "
                + "     };"
                + " }; "
                + "checkBottom(); ";

        checkTopScript
                = " function checkTop() { "
                + "     var scrollTop = document.documentElement.scrollTop||document.body.scrollTop;  "
                + "     if ( scrollTop == 0 ) {  "
                + "          alert('AtTop');  "
                + "     };"
                + " }; "
                + "checkTop(); ";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            webViewController.setParent(this);
            webView = webViewController.webView;
            webEngine = webViewController.webEngine;

            domConfig = PDFDomTreeConfig.createDefaultConfig();

            // https://stackoverflow.com/questions/51048312/javafx-webview-scrollevent-listener-zooms-in-and-scrolls-only-want-it-to-zoom-i?r=SearchResults
            webView.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    double deltaY = event.getDeltaY();
                    if (deltaY > 0) {
                        webEngine.executeScript(checkTopScript);

                        if (event.isControlDown()) {
                            webView.setZoom(webView.getZoom() * 1.1);
                        }
                    } else {
                        webEngine.executeScript(checkBottomScript);

                        if (event.isControlDown()) {
                            webView.setZoom(webView.getZoom() / 1.1);
                        }
                    }
                    event.consume();
                }
            });

            webEngine.setJavaScriptEnabled(true);
            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    try {
                        if ("AtBottom".equals(ev.getData())) {
                            if (setScroll) {
                                setScroll = false;
                                return;
                            }
                            if (atBottom) {  // Go next at second time
                                atBottom = false;
                                pageNextAction();
                            } else {         // buffering at first time
                                atBottom = true;
                                setScroll = true;
                                int h = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");
                                webEngine.executeScript("window.scrollTo(0," + (h - 200) + ");");
                            }
                        } else if ("AtTop".equals(ev.getData())) {
                            if (setScroll) {
                                setScroll = false;
                                return;
                            }
                            if (atTop) {  // Go previous at second time
                                atTop = false;
                                pagePreviousAction();
                            } else {         // buffering at first time
                                atTop = true;
                                setScroll = true;
                                webEngine.executeScript("window.scrollTo(0, 200);");
                            }
                        }

                    } catch (Exception e) {
                        MyBoxLog.debug(e);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void convertHtml(boolean pop) {
        if (imageView.getImage() == null) {
            return;
        }
        if (htmlTask != null) {
            htmlTask.cancel();
        }
        htmlTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                htmlFile = FileTmpTools.getTempFile(".html");
                try (PDDocument doc = Loader.loadPDF(sourceFile, password);
                        Writer output = new PrintWriter(htmlFile, "utf-8")) {
                    subPath = new File(htmlFile.getParent() + File.separator
                            + htmlFile.getName().substring(0, htmlFile.getName().length() - 5));
                    subPath.mkdirs();
                    domConfig.setFontHandler(new SaveResourceToDirHandler(subPath));
                    domConfig.setImageHandler(new SaveResourceToDirHandler(subPath));
                    PDFDomTree parser = new PDFDomTree(domConfig);
                    parser.setStartPage(frameIndex + 1);
                    parser.setEndPage(frameIndex + 1);
                    parser.setPageStart(sourceFile.getAbsolutePath() + " "
                            + MessageFormat.format(message("PageNumber3"), (frameIndex + 1) + ""));
//                    parser.setSpacingTolerance(0f);
                    parser.writeText(doc, output);
                } catch (Exception e) {
                    error = e.toString();
                }
                return htmlFile.exists();
            }

            @Override
            protected void whenSucceeded() {
                if (pop) {
                    HtmlPopController.openFile(myController, htmlFile);
                } else {
                    webViewController.loadFile(htmlFile);
                    webView.requestFocus();
                    atBottom = false;
                    htmlPage = frameIndex;
                }

            }
        };
        start(htmlTask, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
    }

    @FXML
    public void editHtml() {
        HtmlEditorController.openHtml(webViewController.loadedHtml());
    }

}
