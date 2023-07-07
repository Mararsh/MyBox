package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePoint;
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
    protected boolean isLoading;

    protected DoublePoint lastPoint;

    public enum ShapeType {
        Rectangle, Circle, Ellipse, Line, Polyline, Polygon, Path
    }

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
        if (svgShapeControl.optionsController.bgColorCheck.isSelected()) {
            borderLine.setFill(svgShapeControl.optionsController.bgColorController.color());
            borderLine.setOpacity(1 - svgShapeControl.optionsController.bgOpacity);
            imageView.setOpacity(1);
        } else {
            borderLine.setFill(Color.TRANSPARENT);
            imageView.setOpacity(svgShapeControl.optionsController.bgOpacity);
        }
    }

    public void loadShape(Element element) {
        try {
            isLoading = true;
            shapeType = null;
            shape = element;
            initMaskControls(false);
            if (element != null) {
                switch (element.getNodeName().toLowerCase()) {
                    case "rect":
                        shapeType = ShapeType.Rectangle;
                        setMaskRectangleLineVisible(true);
                        double x = Double.parseDouble(shape.getAttribute("x"));
                        double y = Double.parseDouble(shape.getAttribute("y"));
                        double width = Double.parseDouble(shape.getAttribute("width"));
                        double height = Double.parseDouble(shape.getAttribute("height"));
                        maskRectangleData = new DoubleRectangle(x, y, x + width - 1, y + height - 1);
                        drawMaskRectangleLine();
                        break;
                    case "circle":
                        shapeType = ShapeType.Circle;
                        setMaskCircleLineVisible(true);
                        double cx = Double.parseDouble(shape.getAttribute("cx"));
                        double cy = Double.parseDouble(shape.getAttribute("cy"));
                        double r = Double.parseDouble(shape.getAttribute("r"));
                        maskCircleData = new DoubleCircle(cx, cy, r);
                        drawMaskCircleLine();
                        break;
                    case "ellipse":
                        shapeType = ShapeType.Ellipse;
                        setMaskEllipseLineVisible(true);
                        double ex = Double.parseDouble(shape.getAttribute("cx"));
                        double ey = Double.parseDouble(shape.getAttribute("cy"));
                        double erx = Double.parseDouble(shape.getAttribute("rx"));
                        double ery = Double.parseDouble(shape.getAttribute("ry"));
                        maskEllipseData = new DoubleEllipse(ex - erx, ey - ery, ex + erx, ey + ery);
                        drawMaskEllipseLine();
                        break;
                    case "line":
                        shapeType = ShapeType.Line;
                        setMaskLineLineVisible(true);
                        double x1 = Double.parseDouble(shape.getAttribute("x1"));
                        double y1 = Double.parseDouble(shape.getAttribute("y1"));
                        double x2 = Double.parseDouble(shape.getAttribute("x2"));
                        double y2 = Double.parseDouble(shape.getAttribute("y2"));
                        maskLineData = new DoubleLine(x1, y1, x2, y2);
                        drawMaskLineLine();
                        break;
                    case "polyline":
                        shapeType = ShapeType.Polyline;
                        setMaskPolylineVisible(true);
                        maskPolylineData = new DoublePolyline();
                        maskPolylineData.addAll(shape.getAttribute("points"));
                        drawMaskPolyline();
                        break;
                    case "polygon":

                        break;
                    case "path":

                        break;
                    default:
                        popError(message("InvalidData"));

                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        isLoading = false;
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
        double strokeWidth = svgShapeControl.strokeWidth;
        if (strokeWidth <= 0) {
            strokeWidth = 2.0d;
        }
        return strokeWidth;
    }

    @Override
    public List<Double> strokeDash() {
        double strokeWidth = strokeWidth();
        List<Double> dash = new ArrayList<>();
        dash.add(strokeWidth);
        dash.add(strokeWidth * 3);
        return dash;
    }

    @Override
    public float shapeOpacity() {
        return svgShapeControl.fillOpacity;
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
        if (isLoading) {
            return;
        }
        svgShapeControl.loadShape(shape);
        svgShapeControl.loadXml(shape);
    }

    public String scaleValue(double d) {
        return DoubleTools.scale2(d) + "";
    }

    @Override
    public boolean drawMaskRectangleLine() {
        try {
            if (!super.drawMaskRectangleLine() || isLoading || shape == null) {
                return false;
            }
            shape.setAttribute("x", scaleValue(maskRectangleData.getSmallX()));
            shape.setAttribute("y", scaleValue(maskRectangleData.getSmallY()));
            shape.setAttribute("width", scaleValue(maskRectangleData.getWidth()));
            shape.setAttribute("height", scaleValue(maskRectangleData.getHeight()));
            updateSvgShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean drawMaskCircleLine() {
        try {
            if (!super.drawMaskCircleLine() || isLoading || shape == null) {
                return false;
            }
            shape.setAttribute("cx", scaleValue(maskCircleData.getCenterX()));
            shape.setAttribute("cy", scaleValue(maskCircleData.getCenterY()));
            shape.setAttribute("r", scaleValue(maskCircleData.getRadius()));
            updateSvgShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean drawMaskEllipseLine() {
        try {
            if (!super.drawMaskEllipseLine() || isLoading || shape == null) {
                return false;
            }
            shape.setAttribute("cx", scaleValue(maskEllipseData.getCenterX()));
            shape.setAttribute("cy", scaleValue(maskEllipseData.getCenterY()));
            shape.setAttribute("rx", scaleValue(maskEllipseData.getRadiusX()));
            shape.setAttribute("ry", scaleValue(maskEllipseData.getRadiusY()));
            updateSvgShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean drawMaskLineLine() {
        try {
            if (!super.drawMaskLineLine() || isLoading || shape == null) {
                return false;
            }
            shape.setAttribute("x1", scaleValue(maskLineData.getStartX()));
            shape.setAttribute("y1", scaleValue(maskLineData.getStartY()));
            shape.setAttribute("x2", scaleValue(maskLineData.getEndX()));
            shape.setAttribute("y2", scaleValue(maskLineData.getEndY()));
            updateSvgShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean drawMaskPolyline() {
        try {
            if (!super.drawMaskPolyline() || isLoading || shape == null) {
                return false;
            }
            shape.setAttribute("points", DoublePoint.toText(maskPolylineData.getPoints(), 2));
            updateSvgShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
