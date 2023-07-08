package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
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
    protected Element shape;
    protected ShapeType shapeType;
    protected SVGPath svgPath;

    protected DoublePoint lastPoint;

    public enum ShapeType {
        Rectangle, Circle, Ellipse, Line, Polyline, Polygon, Path
    }

    @FXML
    protected CheckBox displayAnchorsCheck, pickPointCheck;
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
            shapeType = null;
            shape = element;
            clearMaskShapes();
            if (svgPath != null && maskPane.getChildren().contains(svgPath)) {
                maskPane.getChildren().remove(svgPath);
                svgPath.setContent("");
            }
            infoLabel.setText("");
            if (element == null) {
                return;
            }
            switch (element.getNodeName().toLowerCase()) {
                case "rect":
                    shapeType = ShapeType.Rectangle;
                    setMaskRectangleVisible(true);
                    double x = Double.parseDouble(shape.getAttribute("x"));
                    double y = Double.parseDouble(shape.getAttribute("y"));
                    double width = Double.parseDouble(shape.getAttribute("width"));
                    double height = Double.parseDouble(shape.getAttribute("height"));
                    maskRectangleData = new DoubleRectangle(x, y, x + width - 1, y + height - 1);
                    drawMaskRectangle();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "circle":
                    shapeType = ShapeType.Circle;
                    setMaskCircleVisible(true);
                    double cx = Double.parseDouble(shape.getAttribute("cx"));
                    double cy = Double.parseDouble(shape.getAttribute("cy"));
                    double r = Double.parseDouble(shape.getAttribute("r"));
                    maskCircleData = new DoubleCircle(cx, cy, r);
                    drawMaskCircle();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "ellipse":
                    shapeType = ShapeType.Ellipse;
                    setMaskEllipseVisible(true);
                    double ex = Double.parseDouble(shape.getAttribute("cx"));
                    double ey = Double.parseDouble(shape.getAttribute("cy"));
                    double erx = Double.parseDouble(shape.getAttribute("rx"));
                    double ery = Double.parseDouble(shape.getAttribute("ry"));
                    maskEllipseData = new DoubleEllipse(ex - erx, ey - ery, ex + erx, ey + ery);
                    drawMaskEllipse();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "line":
                    shapeType = ShapeType.Line;
                    setMaskLineVisible(true);
                    double x1 = Double.parseDouble(shape.getAttribute("x1"));
                    double y1 = Double.parseDouble(shape.getAttribute("y1"));
                    double x2 = Double.parseDouble(shape.getAttribute("x2"));
                    double y2 = Double.parseDouble(shape.getAttribute("y2"));
                    maskLineData = new DoubleLine(x1, y1, x2, y2);
                    drawMaskLine();
                    infoLabel.setText(message("ShapeDragMoveComments"));
                    break;
                case "polyline":
                    shapeType = ShapeType.Polyline;
                    setMaskPolylineVisible(true);
                    maskPolylineData = new DoublePolyline();
                    maskPolylineData.addAll(shape.getAttribute("points"));
                    drawMaskPolyline();
                    infoLabel.setText(message("ShapePointsMoveComments"));
                    break;
                case "polygon":
                    shapeType = ShapeType.Polygon;
                    setMaskPolygonVisible(true);
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.addAll(shape.getAttribute("points"));
                    drawMaskPolygon();
                    infoLabel.setText(message("ShapePointsMoveComments"));
                    break;
                case "path":
                    shapeType = ShapeType.Path;
                    drawPath();
                    break;
                default:
                    popError(message("InvalidData"));

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public Color strokeColor() {
        Color strokeColor = svgShapeControl.strokeColorController.color();
        if (strokeColor == null) {
            strokeColor = Color.RED;
        }
        return strokeColor;
    }

    @Override
    public double strokeWidth() {
        float strokeWidth = svgShapeControl.strokeWidth;
        if (strokeWidth <= 0) {
            strokeWidth = 2.0f;
        }
        return strokeWidth;
    }

    @Override
    public StrokeLineCap strokeLineCap() {
        if (svgShapeControl.linecapSquareRadio.isSelected()) {
            return StrokeLineCap.SQUARE;
        } else if (svgShapeControl.linecapRoundRadio.isSelected()) {
            return StrokeLineCap.ROUND;
        } else {
            return StrokeLineCap.BUTT;
        }
    }

    @Override
    public List<Double> strokeDash() {
        try {
            String text = svgShapeControl.dashInput.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            String[] values = text.split("\\s+");
            if (values == null || values.length == 0) {
                return null;
            }
            List<Double> dash = new ArrayList<>();
            for (String v : values) {
                dash.add(Double.valueOf(v));
            }
            return dash;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public float shapeOpacity() {
        if (svgShapeControl.fillCheck.isSelected()) {
            return svgShapeControl.fillOpacity;
        } else {
            return 1.0f;
        }
    }

    @Override
    public Color shapeFill() {
        if (svgShapeControl.fillCheck.isSelected()) {
            return svgShapeControl.fillColorController.color();
        } else {
            return Color.TRANSPARENT;
        }
    }

    public void updateSvgShape() {
        svgShapeControl.loadShape(shape);
        svgShapeControl.loadXml(shape);
    }

    public String scaleValue(double d) {
        return DoubleTools.scale2(d) + "";
    }

    @Override
    public void maskRectChangedByEvent() {
        try {
            if (shape == null) {
                return;
            }
            shape.setAttribute("x", scaleValue(maskRectangleData.getSmallX()));
            shape.setAttribute("y", scaleValue(maskRectangleData.getSmallY()));
            shape.setAttribute("width", scaleValue(maskRectangleData.getWidth()));
            shape.setAttribute("height", scaleValue(maskRectangleData.getHeight()));
            updateSvgShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskCircleChangedByEvent() {
        try {
            if (shape == null) {
                return;
            }
            shape.setAttribute("cx", scaleValue(maskCircleData.getCenterX()));
            shape.setAttribute("cy", scaleValue(maskCircleData.getCenterY()));
            shape.setAttribute("r", scaleValue(maskCircleData.getRadius()));
            updateSvgShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskEllipseChangedByEvent() {
        try {
            if (shape == null) {
                return;
            }
            shape.setAttribute("cx", scaleValue(maskEllipseData.getCenterX()));
            shape.setAttribute("cy", scaleValue(maskEllipseData.getCenterY()));
            shape.setAttribute("rx", scaleValue(maskEllipseData.getRadiusX()));
            shape.setAttribute("ry", scaleValue(maskEllipseData.getRadiusY()));
            updateSvgShape();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskLineChangedByEvent() {
        try {
            if (shape == null) {
                return;
            }
            shape.setAttribute("x1", scaleValue(maskLineData.getStartX()));
            shape.setAttribute("y1", scaleValue(maskLineData.getStartY()));
            shape.setAttribute("x2", scaleValue(maskLineData.getEndX()));
            shape.setAttribute("y2", scaleValue(maskLineData.getEndY()));
            updateSvgShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskPolylineChangedByEvent() {
        try {
            if (shape == null) {
                return;
            }
            shape.setAttribute("points", DoublePoint.toText(maskPolylineData.getPoints(), 2));
            updateSvgShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskPolygonChangedByEvent() {
        try {
            if (shape == null) {
                return;
            }
            shape.setAttribute("points", DoublePoint.toText(maskPolygonData.getPoints(), 2));
            updateSvgShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean drawPath() {
        try {
            if (shape == null) {
                return false;
            }
            if (svgPath == null) {
                svgPath = new SVGPath();
            }
            svgPath.setContent(shape.getAttribute("d"));
            setShapeStyle(svgPath);
            maskPane.getChildren().add(svgPath);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
