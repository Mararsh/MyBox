package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Circle;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Ellipse;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Line;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Lines;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Polygon;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Polyline;
import static mara.mybox.controller.BaseImageController_Shapes.ShapeType.Rectangle;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleRectangle;
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

            switchShape();
            addListener();
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

    /*
        shape
     */
    @Override
    public void setShapeControls() {
        try {
            super.setShapeControls();
            showController.infoLabel.setText("");
            if (imageController == null || imageController.shapeType == null) {
                return;
            }
            switch (imageController.shapeType) {
                case Circle:
                case Rectangle:
                case Ellipse:
                case Line:
                    showController.infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case Polyline:
                case Polygon:
                case Lines:
                    showController.infoLabel.setText(message("ShapePointsMoveComments"));
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadShape(Element element) {
        try {
            if (element == null) {
                return;
            }
            isSettingValues = true;
            switch (element.getNodeName().toLowerCase()) {
                case "rect":
                    if (loadRect(element)) {
                        rectangleRadio.setSelected(true);
                    }
                    break;
                case "circle":
                    if (loadRect(element)) {
                        circleRadio.setSelected(true);
                    }
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
            isSettingValues = true;

            switchShape();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean loadRect(Element element) {
        try {
            float x, y, w, h;
            try {
                x = Float.parseFloat(element.getAttribute("x"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(element.getAttribute("y"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                w = Float.parseFloat(element.getAttribute("width"));
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
            try {
                h = Float.parseFloat(element.getAttribute("height"));
            } catch (Exception e) {
                h = -1f;
            }
            if (h <= 0) {
                popError(message("InvalidParameter") + ": " + message("Height"));
                return false;
            }
            imageController.maskRectangleData = new DoubleRectangle(x, y, x + w - 1, y + h - 1);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean loadCircle(Element element) {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(element.getAttribute("cx"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(element.getAttribute("cy"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                r = Float.parseFloat(element.getAttribute("r"));
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            imageController.maskCircleData = new DoubleCircle(x, y, r);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    public void goShape() {
        goAction();
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

    @FXML
    public void goStyle() {
        goAction();
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
