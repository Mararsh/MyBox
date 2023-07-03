package mara.mybox.controller;

import java.awt.Rectangle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class ControlSvgShape extends BaseImageController {

    protected SVG svg;
    protected Rectangle svgRect;
    protected Document doc;
    protected Node parentNode;
    protected Element shape;
    protected WebEngine webEngine;
    protected double width, height;
    protected float bgOpacity;
    protected int strokeWidth;

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
    protected Tab parametersTab, xmlTab;
    @FXML
    protected ScrollPane shapePane;
    @FXML
    protected VBox shapeBox, rectangleBox, circleBox, ellipseBox, lineBox,
            polylineBox, polygonBox, pathBox;
    @FXML
    protected TextField circleCenterXInput, circleCenterYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput, dashInput;
    @FXML
    protected TextArea pathArea, styleArea, xmlArea;
    @FXML
    protected ComboBox<String> strokeWidthSelector;
    @FXML
    protected CheckBox fillCheck, wrapXmlCheck;
    @FXML
    protected ControlColorSet fillColorController, strokeColorController;
    @FXML
    protected ControlSvgView viewController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            elementType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    createShape();
                }
            });

            strokeWidth = UserConfig.getInt(baseName + "StrokeWidth", 2);
            strokeWidthSelector.getItems().addAll(
                    "2", "1", "3", "4", "0", "6", "10", "15", "20"
            );
            strokeWidthSelector.setValue(strokeWidth + "");
            strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            UserConfig.setInt(baseName + "StrokeWidth", v);
                            strokeWidthSelector.getEditor().setStyle(null);
                        } else {
                            strokeWidthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        strokeWidthSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            strokeColorController.init(this, baseName + "StrokeColor", Color.BLACK);
            fillColorController.init(this, baseName + "FillColor", Color.TRANSPARENT);

            fillCheck.setSelected(false);

            shapeBox.getChildren().remove(selectPane);

            wrapXmlCheck.setSelected(UserConfig.getBoolean(baseName + "WarpXML", true));
            wrapXmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WarpXML", newValue);
                    xmlArea.setWrapText(newValue);
                }
            });
            xmlArea.setWrapText(wrapXmlCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addShape(Document inDoc, String hierarchyNumber, Rectangle rect) {
        try {
            doc = (Document) inDoc.cloneNode(true);
            parentNode = XmlTools.find(doc, hierarchyNumber);
            svg = new SVG(doc);
            svgRect = rect;
            createShape();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void newDoc() {
        try {
            doc = SvgTools.blankDoc();
            svg = new SVG(doc);
            parentNode = svg.getSvgNode();
            createShape();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void createShape() {
        try {
            if (isSettingValues || doc == null) {
                return;
            }
            pickSVG();

            if (rectRadio.isSelected()) {
                shapePane.setContent(rectangleBox);
                rectXInput.setText(width / 4 + "");
                rectYInput.setText(height / 4 + "");
                rectWidthInput.setText(width / 2 + "");
                rectHeightInput.setText(height / 2 + "");

            } else if (circleRadio.isSelected()) {
                shapePane.setContent(circleBox);
                circleCenterXInput.setText(width / 2 + "");
                circleCenterYInput.setText(height / 2 + "");
                circleRadiusInput.setText(Math.min(width, height) / 4 + "");

            } else if (ellipseRadio.isSelected()) {

            } else if (lineRadio.isSelected()) {

            } else if (polylineRadio.isSelected()) {

            } else if (polygonRadio.isSelected()) {

            } else if (pathRadio.isSelected()) {

            } else {
                popError(message("InvalidData"));
                return;
            }

            pickParameters();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadShape(Element element) {
        try {
            if (isSettingValues || element == null) {
                return;
            }
            isSettingValues = true;
            switch (element.getNodeName().toLowerCase()) {
                case "rect":
                    rectRadio.setSelected(true);
                    shapePane.setContent(rectangleBox);
                    rectXInput.setText(element.getAttribute("x"));
                    rectYInput.setText(element.getAttribute("y"));
                    rectWidthInput.setText(element.getAttribute("width"));
                    rectHeightInput.setText(element.getAttribute("height"));
                    break;
                case "circle":
                    circleRadio.setSelected(true);
                    shapePane.setContent(circleBox);
                    circleCenterXInput.setText(element.getAttribute("cx"));
                    circleCenterYInput.setText(element.getAttribute("cy"));
                    circleRadiusInput.setText(element.getAttribute("r"));
                    break;
                default:
                    popError(message("InvalidData"));
                    isSettingValues = false;
                    return;
            }
            isSettingValues = false;
            refreshStyle(shapeBox);

            displayShape(element);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pickSVG() {
        try {
            if (doc == null || parentNode == null) {
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean pickParameters() {
        try {
            if (doc == null || parentNode == null) {
                return false;
            }
            Element element = null;
            if (rectRadio.isSelected()) {
                element = pickRect();

            } else if (circleRadio.isSelected()) {
                element = pickCircle();

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
            if (element == null) {
                return false;
            }
            element.setAttribute("stroke", strokeColorController.rgb());
            element.setAttribute("stroke-width", strokeWidth + "");
            if (fillCheck.isSelected()) {
                element.setAttribute("fill", fillColorController.rgb());
            } else {
                element.setAttribute("fill", "none");
            }
            displayShape(element);
            xmlArea.setText(XmlTools.transform(shape, true));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public Element pickRect() {
        try {
            if (doc == null || !rectRadio.isSelected()) {
                return null;
            }
            float x, y, w, h;
            try {
                x = Float.parseFloat(rectXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return null;
            }
            try {
                y = Float.parseFloat(rectYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return null;
            }
            try {
                w = Float.parseFloat(rectWidthInput.getText());
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return null;
            }
            try {
                h = Float.parseFloat(rectHeightInput.getText());
            } catch (Exception e) {
                h = -1f;
            }
            if (h <= 0) {
                popError(message("InvalidParameter") + ": " + message("Height"));
                return null;
            }
            Element element = doc.createElement("rect");
            element.setAttribute("x", x + "");
            element.setAttribute("y", y + "");
            element.setAttribute("width", w + "");
            element.setAttribute("height", h + "");
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Element pickCircle() {
        try {
            if (doc == null || !circleRadio.isSelected()) {
                return null;
            }
            float x, y, r;
            try {
                x = Float.parseFloat(circleCenterXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return null;
            }
            try {
                y = Float.parseFloat(circleCenterYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return null;
            }
            try {
                r = Float.parseFloat(circleRadiusInput.getText());
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return null;
            }
            Element element = doc.createElement("circle");
            element.setAttribute("cx", x + "");
            element.setAttribute("cy", y + "");
            element.setAttribute("r", r + "");
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void displayShape(Element element) {
        try {
            parentNode.removeChild(shape);
        } catch (Exception e) {
        }
        shape = element;
        if (shape != null) {
            parentNode.appendChild(shape);
        }
        viewController.loadDoc(doc, shape);
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
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == parametersTab) {
            pickParameters();
        } else if (tab == xmlTab) {
            pickXml();
        }
        return true;
    }

    public void pickXml() {
        try {
            Document nd = XmlTools.textToDoc(this, xmlArea.getText());
            Element element = (Element) doc.importNode(nd.getDocumentElement(), false);
            loadShape(element);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popXml() {
        TextPopController.openInput(this, xmlArea);
    }

}
