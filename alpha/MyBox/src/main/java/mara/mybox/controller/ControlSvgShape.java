package mara.mybox.controller;

import java.awt.Rectangle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class ControlSvgShape extends BaseImageController {

    protected SVG svg;
    protected Rectangle svgRect;
    protected Document doc;
    protected Element element;
    protected WebEngine webEngine;
    protected double width, height;
    protected float bgOpacity;

    @FXML
    protected Label parentLabel;
    @FXML
    protected ToggleGroup elementType;
    @FXML
    protected RadioButton rectRadio, circleRadio, ellipseRadio, lineRadio,
            polylineRadio, polygonRadio, pathRadio;
    @FXML
    protected TabPane selectPane;
    @FXML
    protected Tab shapeTab, styleTab, penTab, xmlTab;
    @FXML
    protected ScrollPane shapePane;
    @FXML
    protected VBox shapeBox, rectangleBox, circleBox, ellipseBox, lineBox,
            polylineBox, polygonBox, pathBox;
    @FXML
    protected TextField circleCenterXInput, circleCenterYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput;
    @FXML
    protected TextArea pathArea, styleArea;
    @FXML
    protected ComboBox<String> strokeWidthSelector;
    @FXML
    protected WebView webView;
    @FXML
    protected ComboBox<String> opacitySelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            elementType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkType();
                }
            });

            strokeWidthSelector.getItems().addAll(
                    "2px", "1px", "3px", "4px", "0", "6px", "10px", "15px", "20px"
            );
            strokeWidthSelector.setValue(UserConfig.getString(baseName + "StrokeWidth", "2px"));

            webView.setCache(false);
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

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
                    } catch (Exception e) {
                        opacitySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            shapeBox.getChildren().remove(selectPane);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkType() {
        try {
            if (doc == null) {
                return;
            }
            if (svg == null) {
                svg = new SVG(doc);
            }
            width = svg.getWidth();
            if (width <= 0) {
                if (svg.getViewBox() != null) {
                    width = svg.getViewBox().getWidth();
                }
                if (width <= 0 && svgRect != null) {
                    width = svgRect.getWidth();
                }
            }
            height = svg.getHeight();
            if (height <= 0) {
                if (svg.getViewBox() != null) {
                    height = svg.getViewBox().getHeight();
                }
                if (height <= 0 && svgRect != null) {
                    height = svgRect.getHeight();
                }
            }

            if (rectRadio.isSelected()) {
                shapePane.setContent(rectangleBox);
                element = doc.createElement("rect");
                element.setAttribute("x", width / 4 + "");
                element.setAttribute("y", height / 4 + "");
                element.setAttribute("width", width / 2 + "");
                element.setAttribute("height", height / 2 + "");

                rectXInput.setText(element.getAttribute("x"));
                rectYInput.setText(element.getAttribute("y"));
                rectWidthInput.setText(element.getAttribute("width"));
                rectHeightInput.setText(element.getAttribute("height"));

            } else if (circleRadio.isSelected()) {
                shapePane.setContent(circleBox);
                element = doc.createElement("circle");
                element.setAttribute("cx", width / 2 + "");
                element.setAttribute("cy", height / 2 + "");
                element.setAttribute("r", Math.min(width, height) / 4 + "");

                circleCenterXInput.setText(element.getAttribute("cx"));
                circleCenterYInput.setText(element.getAttribute("cy"));
                circleRadiusInput.setText(element.getAttribute("r"));

            } else if (ellipseRadio.isSelected()) {
                shapePane.setContent(ellipseBox);

            } else if (lineRadio.isSelected()) {
                shapePane.setContent(lineBox);

            } else if (polylineRadio.isSelected()) {
                shapePane.setContent(polylineBox);

            } else if (polygonRadio.isSelected()) {
                shapePane.setContent(polygonBox);

            } else if (pathRadio.isSelected()) {
                shapePane.setContent(pathBox);

            } else {
                shapePane.setContent(null);

            }
            refreshStyle(shapeBox);

//            String xml = SvgTools.focus(doc, element, bgOpacity);
//            webEngine.getLoadWorker().cancel();
//            webEngine.loadContent(xml);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setDoc(Document inDoc, Rectangle rect) {
        doc = inDoc;
        svg = new SVG(doc);
        svgRect = rect;
        checkType();
    }

    public void newDoc() {
        doc = SvgTools.blankDoc();
        svg = new SVG(doc);
        checkType();
    }

    public Element newElement() {
        try {

            return element;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    public void popExamplesPathMenu(Event event) {
        if (UserConfig.getBoolean("SvgPathExamplesPopWhenMouseHovering", false)) {
            showExamplesPathMenu(event);
        }
    }

    @FXML
    public void showExamplesPathMenu(Event event) {
        PopTools.popValues(this, pathArea, "SvgPathExamples", HelpTools.svgPathExamples(), event);
    }

    @FXML
    public void popExamplesStyleMenu(Event event) {
        if (UserConfig.getBoolean("SvgStyleExamplesPopWhenMouseHovering", false)) {
            showExamplesStyleMenu(event);
        }
    }

    @FXML
    public void showExamplesStyleMenu(Event event) {
        PopTools.popValues(this, styleArea, "SvgStyleExamples", HelpTools.svgStyleExamples(), event);
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("SvgHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.svgHelps(true));
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        return false;
    }

    @FXML
    @Override
    public void clearAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == shapeTab) {

        }
        newElement();
    }

    @FXML
    public void popXml() {

    }

    @FXML
    public void popHtml() {

    }

}
