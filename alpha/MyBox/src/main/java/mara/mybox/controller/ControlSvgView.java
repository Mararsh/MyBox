package mara.mybox.controller;

import java.awt.Rectangle;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-7-2
 * @License Apache License Version 2.0
 */
public class ControlSvgView extends BaseController {

    protected WebEngine webEngine;
    protected Document doc;
    protected Node focusedNode;
    protected String currentXML;
    protected float width, height, bgOpacity;
    protected Rectangle viewBox;

    @FXML
    protected WebView webView;
    @FXML
    protected TextField widthInput, heightInput, viewBoxInput;
    @FXML
    protected CheckBox bgColorCheck;
    @FXML
    protected ControlColorSet bgColorController;
    @FXML
    protected ComboBox<String> opacitySelector;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(widthInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(heightInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(viewBoxInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(viewButton, new Tooltip(message("Image")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            webView.setCache(false);
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            bgColorController.init(this, baseName + "BackgroundColor", Color.TRANSPARENT);
            bgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (bgColorCheck.isSelected()) {
                        drawSVG();
                    }
                }
            });

            bgColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    drawSVG();
                }
            });

            bgOpacity = UserConfig.getFloat(baseName + "BackgroundOpacity", 0.3f);
            opacitySelector.getItems().addAll(
                    "0.3", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.5", "0.8", "0.6", "0.4", "0.7", "0.9"
            );
            opacitySelector.setValue(bgOpacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        bgOpacity = Float.parseFloat(newValue);
                        opacitySelector.getEditor().setStyle(null);
                        drawSVG();
                    } catch (Exception e) {
                        opacitySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadDoc(Document doc, Node focus) {
        try {
            this.doc = doc;
            focusedNode = focus;
            SVG svg = new SVG(doc);
            width = svg.getWidth();
            if (width > 0) {
                widthInput.setText(width + "");
            } else {
                widthInput.clear();
            }
            height = svg.getHeight();
            if (height > 0) {
                heightInput.setText(height + "");
            } else {
                heightInput.clear();
            }
            viewBox = svg.getViewBox();
            if (viewBox != null) {
                viewBoxInput.setText(SvgTools.viewBoxString(viewBox));
            } else {
                viewBoxInput.clear();
            }

            drawSVG();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawSVG() {
        try {
            if (isSettingValues) {
                return;
            }
            if (doc == null) {
                webEngine.loadContent("");
                return;
            }
            Document view = SvgTools.focus(doc, focusedNode, bgOpacity);
            Element svgNode = XmlTools.findName(view, "svg", 0);
            if (width > 0) {
                svgNode.setAttribute("width", width + "");
            } else {
                svgNode.removeAttribute("width");
            }
            if (height > 0) {
                svgNode.setAttribute("height", height + "");
            } else {
                svgNode.removeAttribute("height");
            }
            if (viewBox != null) {
                svgNode.setAttribute("viewBox", SvgTools.viewBoxString(viewBox));
            } else {
                svgNode.removeAttribute("viewBox");
            }
            if (bgColorCheck.isSelected()) {
                String style = svgNode.getAttribute("style");
                svgNode.setAttribute("style",
                        (style == null || style.isBlank() ? "" : style + ";")
                        + "background-color: " + bgColorController.rgb() + "; ");
            }

            currentXML = XmlTools.transform(view);
            webEngine.loadContent(currentXML);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void goSize() {
        width = -1;
        try {
            float v = Float.parseFloat(widthInput.getText());
            if (v > 0) {
                width = v;
            }
        } catch (Exception e) {
        }
        height = -1;
        try {
            float v = Float.parseFloat(heightInput.getText());
            if (v > 0) {
                height = v;
            }
        } catch (Exception e) {
        }
        viewBox = SvgTools.viewBox(viewBoxInput.getText());
        drawSVG();
    }

    @FXML
    public void defaultSize() {
//        isSettingValues = true;
//        bgColorCheck.setSelected(false);
//        isSettingValues = false;
        loadDoc(doc, focusedNode);
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
