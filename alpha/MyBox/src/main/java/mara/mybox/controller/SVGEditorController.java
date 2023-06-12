package mara.mybox.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SVGEditorController extends XmlEditorController {

    protected WebEngine webEngine;
    protected String currentXML;

    @FXML
    protected WebView webView;

    public SVGEditorController() {
        baseTitle = message("SVGEditor");
        TipsLabelKey = "SVGEditorTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG);
    }

    // http://dev.w3.org/SVG/tools/svgweb/samples/svg-files/
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
    public boolean writePanes(String xml) {
        loadHtml(xml);
        return super.writePanes(xml);
    }

    public void loadHtml(String xml) {
        currentXML = xml;
        webEngine.getLoadWorker().cancel();
        webEngine.loadContent(currentXML);
    }

    @Override
    public void synchronizeDom() {
        Platform.runLater(() -> {
            String xml = xmlByDom();
            loadText(xml, true);
            loadHtml(xml);
        });
    }

    @Override
    public void synchronizeTexts() {
        Platform.runLater(() -> {
            String xml = xmlByText();
            loadDom(xml, true);
            loadHtml(xml);
        });
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
    public void pdfAction() {
        if (currentXML == null || currentXML.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = FileTmpTools.generateFile("pdf");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(currentXML.getBytes("utf-8"));
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            PDFTranscoder transcoder = new PDFTranscoder();
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        if (tmpFile.exists()) {
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
        File tmpFile = FileTmpTools.generateFile("png");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(currentXML.getBytes("utf-8"));
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        if (tmpFile.exists()) {
            if (tmpFile.length() > 0) {
                ImageViewerController.openFile(tmpFile);
            } else {
                FileDeleteTools.delete(tmpFile);
            }
        }
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("SVGExamplesPopWhenMouseHovering", true)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            items.add(exampleMenu("AJ_Digital_Camera.svg"));

            items.add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean("SVGExamplesPopWhenMouseHovering", true));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("SVGExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            items.add(pMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected MenuItem exampleMenu(String filename) {
        try {
            File exampleFile = FxFileTools.getInternalFile("/data/examples/" + filename,
                    "data", filename, false);
            if (exampleFile == null || !exampleFile.exists()) {
                return null;
            }
            File tmpFile = FileTmpTools.generateFile(FileNameTools.suffix(filename));
            FileCopyTools.copyFile(exampleFile, tmpFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return null;
            }
            MenuItem menu = new MenuItem(filename);
            menu.setOnAction((ActionEvent mevent) -> {
                sourceFileChanged(tmpFile);
            });
            return menu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static SVGEditorController open(BaseController parent, String file) {
        try {
            SVGEditorController controller = (SVGEditorController) WindowTools.openStage(Fxmls.SVGEditorFxml);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
