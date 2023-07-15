package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
public class ControlSvgShape extends ControlShapeOptions {

    protected SvgEditorController editor;
    protected SVG svg;
    protected Document doc;
    protected Node parentNode;
    protected WebEngine webEngine;

    @FXML
    protected Label parentLabel;
    @FXML
    protected ToggleGroup elementType;
    @FXML
    protected Tab shapeTab, styleTab, xmlTab;
    @FXML
    protected TextArea styleArea, xmlArea;
    @FXML
    protected ControlPoints polylinePointsController, polygonPointsController;
    @FXML
    protected CheckBox wrapXmlCheck;
    @FXML
    protected ControlSvgOptions optionsController;
    @FXML
    protected ControlSvgImage showController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            showController.svgShapeControl = this;

            elementType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    showShape();
                }
            });

            initXML();
            initOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        doc
     */
    public void setParameters(SvgEditorController editorController, String hierarchyNumber) {
        try {
            editor = editorController;
            doc = (Document) editor.treeController.doc.cloneNode(true);
            parentNode = XmlTools.find(doc, hierarchyNumber);
            svg = new SVG(doc);
            optionsController.loadDoc(doc, null);

            super.setParameters(showController);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void newDoc() {
        try {
            doc = SvgTools.blankDoc();
            svg = new SVG(doc);
            parentNode = svg.getSvgNode();
            optionsController.loadDoc(doc, null);
            showShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadShape(Element element) {
        try {
            shapeBox.getChildren().clear();
            if (element == null) {
                return;
            }
            switch (element.getNodeName().toLowerCase()) {
                case "rect":

                    shapeBox.getChildren().add(rectangleBox);
                    rectXInput.setText(element.getAttribute("x"));
                    rectYInput.setText(element.getAttribute("y"));
                    rectWidthInput.setText(element.getAttribute("width"));
                    rectHeightInput.setText(element.getAttribute("height"));
                    break;
                case "circle":
                    circleRadio.setSelected(true);
                    shapeBox.getChildren().add(circleBox);
                    circleXInput.setText(element.getAttribute("cx"));
                    circleYInput.setText(element.getAttribute("cy"));
                    circleRadiusInput.setText(element.getAttribute("r"));
                    break;
                case "ellipse":
                    ellipseRadio.setSelected(true);
                    shapeBox.getChildren().add(ellipseBox);
                    ellipseXInput.setText(element.getAttribute("cx"));
                    ellipseYInput.setText(element.getAttribute("cy"));
                    ellipseXRadiusInput.setText(element.getAttribute("rx"));
                    ellipseYRadiusInput.setText(element.getAttribute("ry"));
                    break;
                case "line":
                    lineRadio.setSelected(true);
                    shapeBox.getChildren().add(lineBox);
                    lineX1Input.setText(element.getAttribute("x1"));
                    lineY1Input.setText(element.getAttribute("y1"));
                    lineX2Input.setText(element.getAttribute("x2"));
                    lineY2Input.setText(element.getAttribute("y2"));
                    break;
                case "polyline":
                    polylineRadio.setSelected(true);
                    shapeBox.getChildren().add(pointsBox);
                    polylinePointsController.loadText(element.getAttribute("points"));
                    break;
                case "polygon":
                    polygonRadio.setSelected(true);
                    shapeBox.getChildren().add(pointsBox);
                    polygonPointsController.loadText(element.getAttribute("points"));
                    break;
                case "path":
                    pathRadio.setSelected(true);
                    shapeBox.getChildren().add(pathBox);
                    pathArea.setText(element.getAttribute("d"));
                    break;
                default:
                    popError(message("InvalidData"));
                    return;
            }
            refreshStyle(shapeOutBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        style
     */
    public void pickStyle(Element element) {
        try {
            if (element == null) {
                return;
            }
            element.setAttribute("stroke", strokeColorController.css());
            element.setAttribute("stroke-width", style.getStrokeWidth() + "");
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
                element.setAttribute("fill", fillColorController.css());
                element.setAttribute("fill-opacity", style.getFillOpacity() + "");
            } else {
                element.setAttribute("fill", "none");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadStyle(Element element) {
        try {
            if (element == null) {
                return;
            }
            try {
                strokeColorController.setColor(Color.web(element.getAttribute("stroke")));
            } catch (Exception e) {
                strokeColorController.setColor(Color.BLACK);
            }
            try {
                strokeWidthSelector.setValue(Float.valueOf(element.getAttribute("stroke-width")) + "");
            } catch (Exception e) {
                strokeWidthSelector.setValue("1px");
            }
            dashInput.setText(element.getAttribute("stroke-dasharray"));
            String v = element.getAttribute("stroke-linecap");
            if ("square".equalsIgnoreCase(v)) {
                linecapSquareRadio.setSelected(true);
            } else if ("round".equalsIgnoreCase(v)) {
                linecapRoundRadio.setSelected(true);
            } else {
                linecapButtRadio.setSelected(true);
            }
            v = element.getAttribute("fill");
            if (v == null || "none".equalsIgnoreCase(v)) {
                fillCheck.setSelected(false);
            } else {
                fillCheck.setSelected(true);
                try {
                    fillColorController.setColor(Color.web(v));
                } catch (Exception e) {
                    fillColorController.setColor(Color.TRANSPARENT);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        xml
     */
    public void initXML() {
        try {
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void goXml() {
        try {
            Element element = XmlTools.toElement(this, xmlArea.getText());
            element = (Element) doc.importNode(element, true);
            loadShape(element);
            loadStyle(element);
            showController.loadShape(element);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popXml() {
        TextPopController.openInput(this, xmlArea);
    }

    public void loadXml(Element element) {
        try {
            xmlArea.setText(XmlTools.transform(element, true));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        options
     */
    public void initOptions() {
        try {
            optionsController.noBgColor();

            optionsController.sizeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    showController.loadBackGround();
                }
            });

            optionsController.opacityNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    showController.setBackGroundOpacity();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        helps
     */
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

}
