package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-2
 * @License Apache License Version 2.0
 */
public class ControlSvgHtml extends ControlSvgOptions {

    protected WebEngine webEngine;
    protected String currentXML;

    @FXML
    protected WebView webView;

    @Override
    public void initControls() {
        try {
            super.initControls();

            webView.setCache(false);
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sizeChanged() {
        drawSVG();
    }

    @Override
    public void bgColorChanged() {
        drawSVG();
    }

    @Override
    public void opacityChanged() {
        drawSVG();
    }

    public void drawSVG() {
        if (doc == null) {
            currentXML = null;
            webEngine.loadContent("");
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    currentXML = toXML(this);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                webEngine.loadContent(currentXML);
            }

        };
        start(task);
    }

    @FXML
    public void htmlAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        HtmlEditorController.openHtml(currentXML);
    }

    @FXML
    @Override
    public void systemMethod() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = FileTmpTools.getTempFile(".svg");
        TextFileTools.writeFile(tmpFile, currentXML);
        if (tmpFile != null && tmpFile.exists()) {
            browse(tmpFile);
        } else {
            popError(message("Failed"));
        }
    }

    @FXML
    public void pdfAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private File tmpFile;

            @Override
            protected boolean handle() {
                try {
                    tmpFile = SvgTools.textToPDF(this,
                            myController, currentXML, width, height, viewBox);
                    return tmpFile != null && tmpFile.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (tmpFile.length() > 0) {
                    PdfViewController.open(tmpFile);
                } else {
                    FileDeleteTools.delete(tmpFile);
                }
            }

        };
        start(task);
    }

    @FXML
    public void viewAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private File tmpFile;

            @Override
            protected boolean handle() {
                try {
                    tmpFile = SvgTools.textToImage(this,
                            myController, currentXML, width, height, viewBox);
                    return tmpFile != null && tmpFile.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (tmpFile.length() > 0) {
                    ImageEditorController.openFile(tmpFile);
                } else {
                    FileDeleteTools.delete(tmpFile);
                }
            }

        };
        start(task);
    }

    @FXML
    protected void txtAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController.edit(currentXML);
    }

    @FXML
    protected void popXml() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        HtmlPopController.showHtml(this, currentXML);
    }

}
