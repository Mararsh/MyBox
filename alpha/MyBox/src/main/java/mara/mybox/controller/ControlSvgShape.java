package mara.mybox.controller;

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
import mara.mybox.tools.StringTools;
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
public class ControlSvgShape extends BaseController {

    protected SvgEditorController editor;
    protected SVG svg;
    protected Document doc;
    protected Node parentNode;
    protected Element shape;
    protected WebEngine webEngine;
    protected float fillOpacity, strokeOpacity;
    protected int strokeWidth;

    @FXML
    protected Label parentLabel;
    @FXML
    protected ToggleGroup elementType;
    @FXML
    protected RadioButton rectRadio, circleRadio, ellipseRadio, lineRadio,
            polylineRadio, polygonRadio, pathRadio,
            linecapSquareRadio, linecapRoundRadio, linecapButtRadio;
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
    protected TextField circleXInput, circleYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput,
            ellipseXInput, ellipseYInput, ellipseXRadiusInput, ellipseYRadiusInput,
            lineX1Input, lineY1Input, lineX2Input, lineY2Input,
            dashInput;
    @FXML
    protected TextArea pathArea, styleArea, xmlArea, polylineArea, polygonArea;
    @FXML
    protected ComboBox<String> strokeWidthSelector, fillOpacitySelector, strokeOpacitySelector;
    @FXML
    protected CheckBox fillCheck, wrapXmlCheck;
    @FXML
    protected ControlColorSet fillColorController, strokeColorController;
    @FXML
    protected ControlSvgOptions svgOptionsController;
    @FXML
    protected ControlImageShape imageController;

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

            fillOpacity = UserConfig.getFloat(baseName + "FillOpacity", 0.3f);
            fillOpacitySelector.getItems().addAll(
                    "0.3", "0.5", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
            );
            fillOpacitySelector.setValue(fillOpacity + "");
            fillOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        fillOpacity = Float.parseFloat(newValue);
                        if (fillOpacity >= 0) {
                            UserConfig.setFloat(baseName + "FillOpacity", fillOpacity);
                        }
                        fillOpacitySelector.getEditor().setStyle(null);
                    } catch (Exception e) {
                        fillOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            strokeOpacity = UserConfig.getFloat(baseName + "StrokeOpacity", 1.0f);
            strokeOpacitySelector.getItems().addAll(
                    "1.0", "0.3", "0", "0.05", "0.02", "0.1", "0.2", "0.5", "0.8", "0.6", "0.4", "0.7", "0.9"
            );
            strokeOpacitySelector.setValue(strokeOpacity + "");
            strokeOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        strokeOpacity = Float.parseFloat(newValue);
                        if (strokeOpacity >= 0) {
                            UserConfig.setFloat(baseName + "StrokeOpacity", strokeOpacity);
                        }
                        strokeOpacitySelector.getEditor().setStyle(null);
                    } catch (Exception e) {
                        strokeOpacitySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

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

            svgOptionsController.drawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    draw();
                }
            });

            imageController.svgShape = this;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(SvgEditorController editorController, String hierarchyNumber) {
        try {
            editor = editorController;
            doc = (Document) editor.treeController.doc.cloneNode(true);
            parentNode = XmlTools.find(doc, hierarchyNumber);
            svg = new SVG(doc);
            svgOptionsController.loadDoc(doc, null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void draw() {
        imageController.loadBackGround();
        if (shape == null) {
            createShape();
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
            double width, height;
            if (imageController.image != null) {
                width = imageController.image.getWidth();
                height = imageController.image.getHeight();
            } else {
                width = 500;
                height = 500;
            }
            double min = Math.min(width, height);
            if (rectRadio.isSelected()) {
                shapePane.setContent(rectangleBox);
                rectXInput.setText((int) (width / 4) + "");
                rectYInput.setText((int) (height / 4) + "");
                rectWidthInput.setText((int) (width / 2) + "");
                rectHeightInput.setText((int) (height / 2) + "");

            } else if (circleRadio.isSelected()) {
                shapePane.setContent(circleBox);
                circleXInput.setText((int) (width / 2) + "");
                circleYInput.setText((int) (height / 2) + "");
                circleRadiusInput.setText((int) (min / 4) + "");

            } else if (ellipseRadio.isSelected()) {
                shapePane.setContent(ellipseBox);
                ellipseXInput.setText((int) (width / 2) + "");
                ellipseYInput.setText((int) (height / 2) + "");
                ellipseXRadiusInput.setText((int) (min * 2 / 5) + "");
                ellipseYRadiusInput.setText((int) (min / 4) + "");

            } else if (lineRadio.isSelected()) {
                shapePane.setContent(lineBox);
                lineX1Input.setText((int) (width / 4) + "");
                lineY1Input.setText((int) (height / 5) + "");
                lineX2Input.setText((int) (width * 4 / 5) + "");
                lineY2Input.setText((int) (height * 4 / 5) + "");

            } else if (polylineRadio.isSelected()) {
                shapePane.setContent(polylineBox);
                polylineArea.setText("0,100 50,25 50,75 100,0");

            } else if (polygonRadio.isSelected()) {
                shapePane.setContent(polygonBox);
                polygonArea.setText("0,100 50,25 50,75 100,0");

            } else if (pathRadio.isSelected()) {
                shapePane.setContent(pathBox);
                pathArea.setText("M 10,30\n"
                        + "           A 20,20 0,0,1 50,30\n"
                        + "           A 20,20 0,0,1 90,30\n"
                        + "           Q 90,60 50,90\n"
                        + "           Q 10,60 10,30 z");

            } else {
                popError(message("InvalidData"));
                return;
            }

            pickParameters();

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
                element = pickEllipse();

            } else if (lineRadio.isSelected()) {
                element = pickLine();

            } else if (polylineRadio.isSelected()) {
                element = pickPolyline();

            } else if (polygonRadio.isSelected()) {
                element = pickPolygon();

            } else if (pathRadio.isSelected()) {
                element = pickPath();

            }
            if (element == null) {
                return false;
            }
            element.setAttribute("stroke", strokeColorController.rgb());
            element.setAttribute("stroke-width", strokeWidth + "");
            element.setAttribute("stroke-opacity", strokeOpacity + "");
            String dash = dashInput.getText();
            if (dash != null && !dash.isBlank()) {
                element.setAttribute("stroke-dasharray", dash);
            }
            if (linecapSquareRadio.isSelected()) {
                element.setAttribute("stroke-linecap", "square");
            } else if (linecapRoundRadio.isSelected()) {
                element.setAttribute("stroke-linecap", "round");
            } else {
                element.removeAttribute("stroke-linecap");
            }
            if (fillCheck.isSelected()) {
                element.setAttribute("fill", fillColorController.rgb());
                element.setAttribute("fill-opacity", fillOpacity + "");
            } else {
                element.setAttribute("fill", "none");
            }

            imageController.displayShape(element);
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
                x = Float.parseFloat(circleXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return null;
            }
            try {
                y = Float.parseFloat(circleYInput.getText());
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

    public Element pickEllipse() {
        try {
            if (doc == null || !ellipseRadio.isSelected()) {
                return null;
            }
            float x, y, rx, ry;
            try {
                x = Float.parseFloat(ellipseXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return null;
            }
            try {
                y = Float.parseFloat(ellipseYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return null;
            }
            try {
                rx = Float.parseFloat(ellipseXRadiusInput.getText());
            } catch (Exception e) {
                rx = -1f;
            }
            if (rx <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return null;
            }
            try {
                ry = Float.parseFloat(ellipseYRadiusInput.getText());
            } catch (Exception e) {
                ry = -1f;
            }
            if (ry <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return null;
            }
            Element element = doc.createElement("ellipse");
            element.setAttribute("cx", x + "");
            element.setAttribute("cy", y + "");
            element.setAttribute("rx", rx + "");
            element.setAttribute("ry", ry + "");
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Element pickLine() {
        try {
            if (doc == null || !lineRadio.isSelected()) {
                return null;
            }
            float x1, y1, x2, y2;
            try {
                x1 = Float.parseFloat(lineX1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x1");
                return null;
            }
            try {
                y1 = Float.parseFloat(lineY1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y1");
                return null;
            }
            try {
                x2 = Float.parseFloat(lineX2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x2");
                return null;
            }
            try {
                y2 = Float.parseFloat(lineY2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y2");
                return null;
            }

            Element element = doc.createElement("line");
            element.setAttribute("x1", x1 + "");
            element.setAttribute("y1", y1 + "");
            element.setAttribute("x2", x2 + "");
            element.setAttribute("y2", y2 + "");
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Element pickPolyline() {
        try {
            if (doc == null || !polylineRadio.isSelected()) {
                return null;
            }
            String p = polylineArea.getText();
            if (p == null || p.isBlank()) {
                popError(message("NoData"));
                return null;
            }
            Element element = doc.createElement("polyline");
            element.setAttribute("points", StringTools.trimBlanks(p));
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Element pickPolygon() {
        try {
            if (doc == null || !polygonRadio.isSelected()) {
                return null;
            }
            String p = polygonArea.getText();
            if (p == null || p.isBlank()) {
                popError(message("NoData"));
                return null;
            }
            Element element = doc.createElement("polygon");
            element.setAttribute("points", StringTools.trimBlanks(p));
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Element pickPath() {
        try {
            if (doc == null || !pathRadio.isSelected()) {
                return null;
            }
            String d = pathArea.getText();
            if (d == null || d.isBlank()) {
                popError(message("NoData"));
                return null;
            }
            Element element = doc.createElement("path");
            element.setAttribute("d", StringTools.trimBlanks(d));
            return element;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
                    circleXInput.setText(element.getAttribute("cx"));
                    circleYInput.setText(element.getAttribute("cy"));
                    circleRadiusInput.setText(element.getAttribute("r"));
                    break;
                case "ellipse":
                    ellipseRadio.setSelected(true);
                    shapePane.setContent(ellipseBox);
                    ellipseXInput.setText(element.getAttribute("cx"));
                    ellipseYInput.setText(element.getAttribute("cy"));
                    ellipseXRadiusInput.setText(element.getAttribute("rx"));
                    ellipseYRadiusInput.setText(element.getAttribute("ry"));
                    break;
                case "line":
                    lineRadio.setSelected(true);
                    shapePane.setContent(lineBox);
                    lineX1Input.setText(element.getAttribute("x1"));
                    lineY1Input.setText(element.getAttribute("y1"));
                    lineX2Input.setText(element.getAttribute("x2"));
                    lineY2Input.setText(element.getAttribute("y2"));
                    break;
                case "polyline":
                    polylineRadio.setSelected(true);
                    shapePane.setContent(polylineBox);
                    polylineArea.setText(element.getAttribute("points"));
                    break;
                case "polygon":
                    polygonRadio.setSelected(true);
                    shapePane.setContent(polygonBox);
                    polygonArea.setText(element.getAttribute("points"));
                    break;
                case "path":
                    pathRadio.setSelected(true);
                    shapePane.setContent(pathBox);
                    pathArea.setText(element.getAttribute("d"));
                    break;
                default:
                    popError(message("InvalidData"));
                    isSettingValues = false;
                    return;
            }
            isSettingValues = false;
            refreshStyle(shapeBox);

            imageController.displayShape(element);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        editor.popEventMenu(event, HelpTools.svgHelps(true));
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

    @FXML
    public void popXml() {
        TextPopController.openInput(this, xmlArea);
    }

}
