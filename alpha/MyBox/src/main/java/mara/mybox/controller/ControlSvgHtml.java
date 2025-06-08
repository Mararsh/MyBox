package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
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
public class ControlSvgHtml extends BaseController {

    protected ControlSvgViewOptions optionsController;
    protected WebEngine webEngine;
    protected String currentSVG;

    @FXML
    protected WebView webView;

    @Override
    public void initControls() {
        try {
            super.initControls();

            webEngine = webView.getEngine();
            webView.setCache(false);
            webEngine.setJavaScriptEnabled(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlSvgViewOptions options) {
        optionsController = options;
    }

    public void drawSVG(String svg) {
        currentSVG = svg;
        webEngine.loadContent(svg);
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Html"), StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent event) -> {
                htmlAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Image"), StyleTools.getIconImageView("iconDefault.png"));
            menu.setOnAction((ActionEvent event) -> {
                imageAction();
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            menu = new MenuItem("PDF", StyleTools.getIconImageView("iconPDF.png"));
            menu.setOnAction((ActionEvent event) -> {
                pdfAction();
            });
            items.add(menu);

            menu = new MenuItem("XML", StyleTools.getIconImageView("iconXML.png"));
            menu.setOnAction((ActionEvent event) -> {
                xmlAction();
            });
            items.add(menu);

            menu = new MenuItem("SVG", StyleTools.getIconImageView("iconSVG.png"));
            menu.setOnAction((ActionEvent event) -> {
                svgAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Text"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                txtAction();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void htmlAction() {
        if (currentSVG == null || currentSVG.isBlank()) {
            popError(message("NoData"));
            return;
        }
        HtmlEditorController.openHtml(currentSVG);
    }

    @FXML
    @Override
    public void systemMethod() {
        if (currentSVG == null || currentSVG.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = FileTmpTools.getTempFile(".svg");
        TextFileTools.writeFile(tmpFile, currentSVG);
        if (tmpFile != null && tmpFile.exists()) {
            browse(tmpFile);
        } else {
            popError(message("Failed"));
        }
    }

    @FXML
    public void pdfAction() {
        if (currentSVG == null || currentSVG.isBlank()) {
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
                            myController, currentSVG,
                            optionsController != null ? optionsController.width : -1,
                            optionsController != null ? optionsController.height : -1,
                            optionsController != null ? optionsController.viewBox : null);
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
    public void imageAction() {
        if (currentSVG == null || currentSVG.isBlank()) {
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
                            myController, currentSVG,
                            optionsController != null ? optionsController.width : -1,
                            optionsController != null ? optionsController.height : -1,
                            optionsController != null ? optionsController.viewBox : null);
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
        if (currentSVG == null || currentSVG.isBlank()) {
            popError(message("NoData"));
            return;
        }
        TextPopController.loadText(currentSVG);
    }

    @FXML
    protected void xmlAction() {
        if (currentSVG == null || currentSVG.isBlank()) {
            popError(message("NoData"));
            return;
        }
        XmlEditorController.load(currentSVG);
    }

    @FXML
    protected void svgAction() {
        if (currentSVG == null || currentSVG.isBlank()) {
            popError(message("NoData"));
            return;
        }
        SvgEditorController.load(currentSVG);
    }

}
