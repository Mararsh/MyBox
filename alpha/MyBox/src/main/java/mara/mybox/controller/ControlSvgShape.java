package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.SVG;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class ControlSvgShape extends ControlShapeOptions {

    protected SvgEditorController editor;
    protected SVG svg;
    protected Document doc;
    protected Element element;
    protected WebEngine webEngine;

    @FXML
    protected Label parentLabel;
    @FXML
    protected FlowPane typePane;
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
            initSvgOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        svg
     */
    public void initSvgOptions() {
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

    public void editShape(SvgEditorController editor, Element node) {
        try {
            super.setParameters(showController);

            this.editor = editor;
            doc = (Document) editor.treeController.doc.cloneNode(true);
            svg = new SVG(doc);
            optionsController.loadDoc(doc, null);

            loadElement(node);
            addListener();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadElement(Element node) {
        try {
            if (node != null) {
                typePane.setDisable(true);
                element = (Element) node.cloneNode(false);
                loadShape(element);
                loadStyle(element);
                pickStyle();
                super.switchShape();
                loadXml(element);
            } else {
                switchShape();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String scaleValue(double d) {
        return DoubleTools.scale2(d) + "";
    }

    /*
        shape
     */
    @Override
    public void switchShape() {
        if (isSettingValues) {
            return;
        }
        element = null;
        super.switchShape();
        if (shape2Element() && style2Element()) {
            loadXml(element);
        }
    }

    @Override
    public void setShapeControls() {
        try {
            super.setShapeControls();
            showController.infoLabel.setText("");
            if (imageController == null || shapeType == null) {
                return;
            }
            isSettingValues = true;
            switch (shapeType) {
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
        isSettingValues = false;
    }

    @Override
    public void shapeDataChanged() {
        setShapeControls();
        shape2Element();
    }

    public void loadShape(Element node) {
        try {
            if (node == null) {
                return;
            }
            isSettingValues = true;
            switch (node.getNodeName().toLowerCase()) {
                case "rect":
                    if (loadRect(node)) {
                        rectangleRadio.setSelected(true);
                    }
                    break;
                case "circle":
                    if (loadCircle(node)) {
                        circleRadio.setSelected(true);
                    }
                    break;
                case "ellipse":
                    if (loadEllipse(node)) {
                        ellipseRadio.setSelected(true);
                    }
                    break;
                case "line":
                    if (loadLine(node)) {
                        lineRadio.setSelected(true);
                    }
                    break;
                case "polyline":
                    if (loadPolyline(node)) {
                        polylineRadio.setSelected(true);
                    }
                    break;
                case "polygon":
                    if (loadPolygon(node)) {
                        polygonRadio.setSelected(true);
                    }
                    break;
                case "path":
                    if (loadPath(node)) {
                        pathRadio.setSelected(true);
                    }
                    break;
                default:
                    popError(message("InvalidData"));
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean loadRect(Element node) {
        try {
            float x, y, w, h;
            try {
                x = Float.parseFloat(node.getAttribute("x"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(node.getAttribute("y"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                w = Float.parseFloat(node.getAttribute("width"));
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
            try {
                h = Float.parseFloat(node.getAttribute("height"));
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

    public boolean loadCircle(Element node) {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                r = Float.parseFloat(node.getAttribute("r"));
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

    public boolean loadEllipse(Element node) {
        try {
            float x, y, rx, ry;
            try {
                x = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                rx = Float.parseFloat(node.getAttribute("rx"));
            } catch (Exception e) {
                rx = -1f;
            }
            if (rx <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            try {
                ry = Float.parseFloat(node.getAttribute("ry"));
            } catch (Exception e) {
                ry = -1f;
            }
            if (ry <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            imageController.maskEllipseData = new DoubleEllipse(x, y, x + rx * 2 - 1, y + ry * 2 - 1);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean loadLine(Element node) {
        try {
            float x1, y1, x2, y2;
            try {
                x1 = Float.parseFloat(node.getAttribute("x1"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x1");
                return false;
            }
            try {
                y1 = Float.parseFloat(node.getAttribute("y1"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y1");
                return false;
            }
            try {
                x2 = Float.parseFloat(node.getAttribute("x2"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x2");
                return false;
            }
            try {
                y2 = Float.parseFloat(node.getAttribute("y2"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y2");
                return false;
            }
            imageController.maskLineData = new DoubleLine(x1, y1, x2, y2);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean loadPolyline(Element node) {
        try {
            List<DoublePoint> list = DoublePoint.parseList(node.getAttribute("points"));
            if (list != null && !list.isEmpty()) {
                imageController.maskPolylineData.setAll(list);
            } else {
                imageController.maskPolylineData.clear();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean loadPolygon(Element node) {
        try {
            List<DoublePoint> list = DoublePoint.parseList(node.getAttribute("points"));
            if (list != null && !list.isEmpty()) {
                imageController.maskPolygonData.setAll(list);
            } else {
                imageController.maskPolygonData.clear();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean loadPath(Element node) {
        try {
            imageController.pathData = new DoublePath(node.getAttribute("d"));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean shape2Element() {
        try {
            if (imageController == null || shapeType == null) {
                popError(message("InvalidData"));
                return false;
            }
            switch (shapeType) {
                case Rectangle:
                    if (element == null) {
                        element = doc.createElement("rect");
                    }
                    element.setAttribute("x", scaleValue(imageController.maskRectangleData.getSmallX()));
                    element.setAttribute("y", scaleValue(imageController.maskRectangleData.getSmallY()));
                    element.setAttribute("width", scaleValue(imageController.maskRectangleData.getWidth()));
                    element.setAttribute("height", scaleValue(imageController.maskRectangleData.getHeight()));
                    return true;
                case Circle:
                    if (element == null) {
                        element = doc.createElement("circle");
                    }
                    element.setAttribute("cx", scaleValue(imageController.maskCircleData.getCenterX()));
                    element.setAttribute("cy", scaleValue(imageController.maskCircleData.getCenterY()));
                    element.setAttribute("r", scaleValue(imageController.maskCircleData.getRadius()));
                    return true;
                case Ellipse:
                    if (element == null) {
                        element = doc.createElement("ellipse");
                    }
                    element.setAttribute("cx", scaleValue(imageController.maskEllipseData.getCenterX()));
                    element.setAttribute("cy", scaleValue(imageController.maskEllipseData.getCenterY()));
                    element.setAttribute("rx", scaleValue(imageController.maskEllipseData.getRadiusX()));
                    element.setAttribute("ry", scaleValue(imageController.maskEllipseData.getRadiusY()));
                    return true;
                case Line:
                    if (element == null) {
                        element = doc.createElement("line");
                    }
                    element.setAttribute("x1", scaleValue(imageController.maskLineData.getStartX()));
                    element.setAttribute("y1", scaleValue(imageController.maskLineData.getStartY()));
                    element.setAttribute("x2", scaleValue(imageController.maskLineData.getEndX()));
                    element.setAttribute("y2", scaleValue(imageController.maskLineData.getEndY()));
                    return true;
                case Polyline:
                    if (element == null) {
                        element = doc.createElement("polyline");
                    }
                    element.setAttribute("points", DoublePoint.toText(imageController.maskPolylineData.getPoints(), 2));
                    return true;
                case Polygon:
                    if (element == null) {
                        element = doc.createElement("polygon");
                    }
                    element.setAttribute("points", DoublePoint.toText(imageController.maskPolygonData.getPoints(), 2));
                    return true;
                case Path:
                    if (element == null) {
                        element = doc.createElement("path");
                    }
                    element.setAttribute("d", imageController.pathData.getContent());
                    return true;
            }
            popError(message("InvalidData"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        if (!super.pickShape()) {
            return false;
        }
        return shape2Element();
    }

    @FXML
    public void goShape() {
        if (pickShape()) {
            loadXml(element);
            redrawShape();
        }
    }

    /*
        style
     */
    public void loadStyle(Element node) {
        try {
            if (node == null) {
                return;
            }

            isSettingValues = true;
            try {
                strokeColorController.setColor(Color.web(node.getAttribute("stroke")));
            } catch (Exception e) {
                strokeWidthSelector.setValue("-1");
            }
            try {
                strokeWidthSelector.setValue(Float.valueOf(node.getAttribute("stroke-width")) + "");
            } catch (Exception e) {
                strokeWidthSelector.setValue("-1");
            }

            String v = node.getAttribute("stroke-dasharray");
            dashCheck.setSelected(v != null && !v.isBlank());
            dashInput.setText(v);

            v = node.getAttribute("stroke-linecap");
            if (v == null || v.isBlank()) {
                linecapSquareRadio.setSelected(false);
                linecapRoundRadio.setSelected(false);
                linecapButtRadio.setSelected(false);
            } else {
                if ("square".equalsIgnoreCase(v)) {
                    linecapSquareRadio.setSelected(true);
                } else if ("round".equalsIgnoreCase(v)) {
                    linecapRoundRadio.setSelected(true);
                } else {
                    linecapButtRadio.setSelected(true);
                }
            }

            v = node.getAttribute("fill");
            if (v == null || v.isBlank() || "none".equalsIgnoreCase(v)) {
                fillCheck.setSelected(false);
            } else {
                fillCheck.setSelected(true);
                try {
                    fillColorController.setColor(Color.web(v));
                } catch (Exception e) {
                    fillColorController.setColor(Color.TRANSPARENT);
                }
            }
            v = node.getAttribute("fill-opacity");
            try {
                fillOpacitySelector.setValue(Float.valueOf(v) + "");
            } catch (Exception e) {
                fillOpacitySelector.setValue("-1");
            }

            styleArea.setText(node.getAttribute("style"));
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean pickStyle() {
        try {
            if (!super.pickStyle()) {
                return false;
            }
            String v = styleArea.getText();
            if (v != null && !v.isBlank()) {
                v = StringTools.trimBlanks(v);
                style.setMore(v);
                TableStringValues.add("SvgStyleHistories", v);
            } else {
                style.setMore(null);
            }
            return style2Element();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean style2Element() {
        try {
            if (style == null || element == null) {
                popError(message("InvalidData"));
                return false;
            }
            if (style.getStrokeWidth() <= 0) {
                element.removeAttribute("stroke");
                element.removeAttribute("stroke-width");
            } else {
                element.setAttribute("stroke", style.getStrokeColorCss());
                element.setAttribute("stroke-width", style.getStrokeWidth() + "");
            }

            if (style.isIsFillColor()) {
                element.setAttribute("fill", style.getFilleColorCss());
            } else {
                element.removeAttribute("fill");
            }
            if (style.getFillOpacity() > 0) {
                element.setAttribute("fill-opacity", style.getFillOpacity() + "");
            } else {
                element.removeAttribute("fill-opacity");
            }

            if (style.isIsStrokeDash()) {
                String dash = style.getStrokeDashText();
                if (dash != null && !dash.isBlank()) {
                    element.setAttribute("stroke-dasharray", dash);
                } else {
                    element.removeAttribute("stroke-dasharray");
                }
            } else {
                element.removeAttribute("stroke-dasharray");
            }

            String v = style.getLineCapText();
            if (v != null) {
                element.setAttribute("stroke-linecap", v);
            } else {
                element.removeAttribute("stroke-linecap");
            }

            String more = style.getMore();
            if (more != null && !more.isBlank()) {
                element.setAttribute("style", more);
            } else {
                element.removeAttribute("style");
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @FXML
    public void goStyle() {
        if (pickStyle()) {
            loadXml(element);
            redrawShape();
        }
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
    protected void popStyleHistories(Event event) {
        if (UserConfig.getBoolean("SvgStyleHistoriesPopWhenMouseHovering", false)) {
            showStyleHistories(event);
        }
    }

    @FXML
    protected void showStyleHistories(Event event) {
        PopTools.popStringValues(this, styleArea, event, "SvgStyleHistories", false, true);
    }

    @FXML
    protected void clearStyle() {
        styleArea.clear();
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

    public void loadXml(Element element) {
        try {
            isSettingValues = true;
            xmlArea.setText(XmlTools.transform(element, true));
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void goXml() {
        try {
            element = XmlTools.toElement(this, xmlArea.getText());
            element = (Element) doc.importNode(element, true);
            loadElement(element);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popXml() {
        TextPopController.openInput(this, xmlArea);
    }

    /*
        buttons
     */
    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();

            if (tab == shapeTab) {
                goShape();
                return true;

            } else if (tab == styleTab) {
                goStyle();
                return true;

            } else if (tab == xmlTab) {
                goXml();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
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
