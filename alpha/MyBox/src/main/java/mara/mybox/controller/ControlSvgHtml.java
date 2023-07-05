package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
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
            MyBoxLog.error(e.toString());
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
        try {
            if (doc == null) {
                currentXML = null;
                webEngine.loadContent("");
                return;
            }
            currentXML = toXML();
            webEngine.loadContent(currentXML);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
    public void systemWebBrowser() {
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
        File tmpFile = SvgTools.textToPDF(this, currentXML, width, height, viewBox);
        if (tmpFile != null && tmpFile.exists()) {
            if (tmpFile.length() > 0) {
                PdfViewController.open(tmpFile);
            } else {
                FileDeleteTools.delete(tmpFile);
            }
        }
    }

    @FXML
    public void viewAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = SvgTools.textToImage(this, currentXML, width, height, viewBox);
        if (tmpFile != null && tmpFile.exists()) {
            if (tmpFile.length() > 0) {
                ImageViewerController.openFile(tmpFile);
            } else {
                FileDeleteTools.delete(tmpFile);
            }
        }
    }

    @FXML
    protected void txtAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(currentXML);
        controller.requestMouse();
    }

    @FXML
    protected void popXml() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        HtmlPopController.openHtml(currentXML);
    }

}
