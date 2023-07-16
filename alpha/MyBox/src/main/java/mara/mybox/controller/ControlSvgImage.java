package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.SVGPath;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-7-5
 * @License Apache License Version 2.0
 */
public class ControlSvgImage extends BaseImageController {

    protected ControlSvgShape svgShapeControl;
    protected Element element;
    protected DoublePoint lastPoint;

    @FXML
    protected Label infoLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            imageView.toBack();

            loadShape(null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadBackGround() {
        try {
            imageView.toBack();
            File tmpFile = svgShapeControl.optionsController.toImage();
            if (tmpFile != null && tmpFile.exists()) {
                loadImage(FxImageTools.readImage(tmpFile));
                FileDeleteTools.delete(tmpFile);
                setBackGroundOpacity();
            } else {
                loadImage(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setBackGroundOpacity() {
        imageView.setOpacity(svgShapeControl.optionsController.bgOpacity);
    }

    public void loadShape(Element element) {
        try {
            isSettingValues = true;
            this.element = element;
            clearMask();
            if (svgPath != null && maskPane.getChildren().contains(svgPath)) {
                maskPane.getChildren().remove(svgPath);
                svgPath.setContent("");
            }
            infoLabel.setText("");
            if (element == null) {
                isSettingValues = false;
                return;
            }
            switch (element.getNodeName().toLowerCase()) {
                case "rect":
                    double x = Double.parseDouble(this.element.getAttribute("x"));
                    double y = Double.parseDouble(this.element.getAttribute("y"));
                    double width = Double.parseDouble(this.element.getAttribute("width"));
                    double height = Double.parseDouble(this.element.getAttribute("height"));
                    maskRectangleData = new DoubleRectangle(x, y, x + width - 1, y + height - 1);
                    showMaskRectangle();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "circle":
                    double cx = Double.parseDouble(this.element.getAttribute("cx"));
                    double cy = Double.parseDouble(this.element.getAttribute("cy"));
                    double r = Double.parseDouble(this.element.getAttribute("r"));
                    maskCircleData = new DoubleCircle(cx, cy, r);
                    showMaskCircle();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "ellipse":
                    double ex = Double.parseDouble(this.element.getAttribute("cx"));
                    double ey = Double.parseDouble(this.element.getAttribute("cy"));
                    double erx = Double.parseDouble(this.element.getAttribute("rx"));
                    double ery = Double.parseDouble(this.element.getAttribute("ry"));
                    maskEllipseData = new DoubleEllipse(ex - erx, ey - ery, ex + erx, ey + ery);
                    showMaskEllipse();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "line":
                    double x1 = Double.parseDouble(this.element.getAttribute("x1"));
                    double y1 = Double.parseDouble(this.element.getAttribute("y1"));
                    double x2 = Double.parseDouble(this.element.getAttribute("x2"));
                    double y2 = Double.parseDouble(this.element.getAttribute("y2"));
                    maskLineData = new DoubleLine(x1, y1, x2, y2);
                    showMaskLine();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "polyline":
                    maskPolylineData = new DoublePolyline();
                    maskPolylineData.addAll(this.element.getAttribute("points"));
                    showMaskPolyline();
                    infoLabel.setText(message("ShapePointsMoveComments"));
                    break;
                case "polygon":
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.addAll(this.element.getAttribute("points"));
                    showMaskPolygon();
                    infoLabel.setText(message("ShapePointsMoveComments"));
                    break;
                case "path":
                    if (svgPath == null) {
                        svgPath = new SVGPath();
                    }
                    svgPath.setContent(this.element.getAttribute("d"));
                    break;
                default:
                    popError(message("InvalidData"));

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        isSettingValues = false;
    }

    public void updateSvgShape() {
        svgShapeControl.loadShape(element);
        svgShapeControl.loadXml(element);
    }

    public String scaleValue(double d) {
        return DoubleTools.scale2(d) + "";
    }

    @Override
    public void maskShapeChanged() {
        if (isSettingValues || element == null) {
            return;
        }
        try {
            switch (element.getTagName().toLowerCase()) {
                case "rect":
                    element.setAttribute("x", scaleValue(maskRectangleData.getSmallX()));
                    element.setAttribute("y", scaleValue(maskRectangleData.getSmallY()));
                    element.setAttribute("width", scaleValue(maskRectangleData.getWidth()));
                    element.setAttribute("height", scaleValue(maskRectangleData.getHeight()));
                    break;
                case "circle":
                    element.setAttribute("cx", scaleValue(maskCircleData.getCenterX()));
                    element.setAttribute("cy", scaleValue(maskCircleData.getCenterY()));
                    element.setAttribute("r", scaleValue(maskCircleData.getRadius()));
                    break;
                case "ellipse":
                    element.setAttribute("cx", scaleValue(maskEllipseData.getCenterX()));
                    element.setAttribute("cy", scaleValue(maskEllipseData.getCenterY()));
                    element.setAttribute("rx", scaleValue(maskEllipseData.getRadiusX()));
                    element.setAttribute("ry", scaleValue(maskEllipseData.getRadiusY()));
                    break;
                case "line":
                    element.setAttribute("x1", scaleValue(maskLineData.getStartX()));
                    element.setAttribute("y1", scaleValue(maskLineData.getStartY()));
                    element.setAttribute("x2", scaleValue(maskLineData.getEndX()));
                    element.setAttribute("y2", scaleValue(maskLineData.getEndY()));
                    break;
                case "polyline":
                    element.setAttribute("points", DoublePoint.toText(maskPolylineData.getPoints(), 2));
                    break;
                case "polygon":
                    element.setAttribute("points", DoublePoint.toText(maskPolygonData.getPoints(), 2));
                    break;
                default:
                    return;
            }
//            updateSvgShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
