package mara.mybox.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
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
import mara.mybox.tools.TextFileTools;
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
public class SvgEditorController extends XmlEditorController {

    protected WebEngine webEngine;
    protected String currentXML;

    @FXML
    protected ControlSvgTree treeController;
    @FXML
    protected WebView webView;

    public SvgEditorController() {
        baseTitle = message("SVGEditor");
        TipsLabelKey = "SVGEditorTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            domController = treeController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG);
    }

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

    @Override
    public String makeBlank() {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 500 500\" >\n</svg>";
    }

    public void loadHtml(String xml) {
        try {
            currentXML = xml;
            webEngine.getLoadWorker().cancel();
            webEngine.loadContent(currentXML);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void synchronizeDomXML(String xml) {
        super.synchronizeDomXML(xml);
        loadHtml(xml);
    }

    @Override
    public void synchronizeTexts() {
        String xml = xmlByText();
        loadDom(xml, true);
        loadHtml(xml);
    }

    @Override
    public void openSavedFile(File file) {
        SvgEditorController.open(file);
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
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("SVGExamplesPopWhenMouseHovering", true)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        try {
            Menu w3menu = new Menu("w3");
            List<MenuItem> items = new ArrayList<>();
            items.add(exampleMenu("accessible.svg"));
            items.add(exampleMenu("AJ_Digital_Camera.svg"));
            items.add(exampleMenu("alphachannel.svg"));
            items.add(exampleMenu("android.svg"));
            items.add(exampleMenu("basura.svg"));
            items.add(exampleMenu("cartman.svg"));
            items.add(exampleMenu("compuserver_msn_Ford_Focus.svg"));
            items.add(exampleMenu("displayWebStats.svg"));
            items.add(exampleMenu("gaussian3.svg"));
            items.add(exampleMenu("jsonatom.svg"));
            items.add(exampleMenu("lineargradient2.svg"));
            items.add(exampleMenu("mouseEvents.svg"));
            items.add(exampleMenu("ny1.svg"));
            items.add(exampleMenu("radialgradient2.svg"));
            items.add(exampleMenu("rg1024_eggs.svg"));
            items.add(exampleMenu("rg1024_green_grapes.svg"));
            items.add(exampleMenu("rg1024_metal_effect.svg"));
            items.add(exampleMenu("rg1024_Ufo_in_metalic_style.svg"));
            items.add(exampleMenu("snake.svg"));
            items.add(exampleMenu("star.svg"));
            items.add(exampleMenu("Steps.svg"));
            items.add(exampleMenu("svg2009.svg"));
            items.add(exampleMenu("tiger.svg"));
            items.add(exampleMenu("USStates.svg"));
            items.add(exampleMenu("yinyang.svg"));

            items.add(new SeparatorMenuItem());

            MenuItem menuItem = new MenuItem("http://dev.w3.org/SVG/tools/svgweb/samples/svg-files/");
            menuItem.setStyle("-fx-text-fill: #2e598a;");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("http://dev.w3.org/SVG/tools/svgweb/samples/svg-files/", true);
                }
            });
            items.add(menuItem);

            w3menu.getItems().addAll(items);

            Menu batikMenu = new Menu("batik");
            items.clear();
            items.add(exampleMenu("3D.svg"));
            items.add(exampleMenu("anne.svg"));
            items.add(exampleMenu("asf-logo.svg"));
            items.add(exampleMenu("barChart.svg"));
            items.add(exampleMenu("batik3D.svg"));
            items.add(exampleMenu("batikYin.svg"));
            items.add(exampleMenu("batikFX.svg"));
            items.add(exampleMenu("logoShadowOffset.svg"));
            items.add(exampleMenu("mapSpain.svg"));
            items.add(exampleMenu("mapWaadt.svg"));
            items.add(exampleMenu("moonPhases.svg"));
            items.add(exampleMenu("sizeOfSun.svg"));
            items.add(exampleMenu("strokeFont.svg"));

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem("https://github.com/apache/xmlgraphics-batik/tree/main/samples");
            menuItem.setStyle("-fx-text-fill: #2e598a;");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://github.com/apache/xmlgraphics-batik/tree/main/samples", true);
                }
            });
            items.add(menuItem);

            batikMenu.getItems().addAll(items);

            items.clear();
            items.add(w3menu);
            items.add(batikMenu);

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
            MenuItem menu = new MenuItem(filename);
            menu.setOnAction((ActionEvent mevent) -> {
                File exampleFile = FxFileTools.getInternalFile("/data/examples/" + filename,
                        "data", filename, false);
                if (exampleFile == null || !exampleFile.exists()) {
                    return;
                }
                File tmpFile = FileTmpTools.generateFile(FileNameTools.prefix(filename), FileNameTools.suffix(filename));
                FileCopyTools.copyFile(exampleFile, tmpFile);
                if (tmpFile == null || !tmpFile.exists()) {
                    return;
                }
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
    public static SvgEditorController open(File file) {
        try {
            SvgEditorController controller = (SvgEditorController) WindowTools.openStage(Fxmls.SvgEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
