package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    protected SvgEditorController editor;
    protected ControlSvgOptions optionsController;
    protected WebEngine webEngine;
    protected String currentXML;

    @FXML
    protected WebView webView;

    public void setParameters(SvgEditorController editor) {
        try {
            webEngine = webView.getEngine();
            webView.setCache(false);
            webEngine.setJavaScriptEnabled(true);

            this.editor = editor;
            optionsController = editor.optionsController;
            optionsController.changeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawSVG();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawSVG() {
        if (optionsController.doc == null) {
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
                    currentXML = optionsController.toXML(this);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                webEngine.loadContent(currentXML);
                editor.showRightPane();
            }

        };
        start(task);
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

            menu = new MenuItem(message("Image"), StyleTools.getIconImageView("iconSVG.png"));
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

            menu = new MenuItem(message("SVG"), StyleTools.getIconImageView("iconSVG.png"));
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
                            myController, currentXML,
                            optionsController.width,
                            optionsController.height,
                            optionsController.viewBox);
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
                            myController, currentXML,
                            optionsController.width,
                            optionsController.height,
                            optionsController.viewBox);
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
        TextPopController.loadText(currentXML);
    }

    @FXML
    protected void xmlAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        XmlEditorController.load(currentXML);
    }

    @FXML
    protected void svgAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        SvgEditorController.load(currentXML);
    }

}
