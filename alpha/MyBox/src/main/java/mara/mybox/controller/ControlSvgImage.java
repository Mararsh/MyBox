package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-7-5
 * @License Apache License Version 2.0
 */
public class ControlSvgImage extends BaseImageController {

    protected ControlSvgShape svgShape;
    protected Element shape;
    protected ShapeType shapeType;
    protected DoublePoint lastPoint;
    protected Line line;

    public enum ShapeType {
        Rectangle, Circle, Ellipse, Line, Polyline, Polygon, Path
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            imageView.toBack();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadBackGround() {
        try {
            imageView.toBack();
            File tmpFile = svgShape.optionsController.toImage();
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
        imageView.setOpacity(svgShape.optionsController.bgOpacity);
    }

    @Override
    public Color strokeColor() {
        Color strokeColor = svgShape.strokeColorController.color();
        if (strokeColor == null) {
            strokeColor = Color.RED;
        }
        return strokeColor;
    }

    @Override
    public double strokeWidth() {
        double strokeWidth = svgShape.strokeWidth;
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
        return svgShape.fillOpacity;
    }

    @Override
    public Color shapeFill() {
        if (svgShape.fillCheck.isSelected()) {
            return svgShape.fillColorController.color();
        } else {
            return Color.TRANSPARENT;
        }
    }

    public void loadShape(Element element) {
        try {
            shapeType = null;
            initMaskControls(false);
            clearLine();
            if (isSettingValues || element == null) {
                return;
            }
            shape = element;
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
                    drawLine();
                    break;
                case "polyline":

                    break;
                case "polygon":

                    break;
                case "path":

                    break;
                default:
                    popError(message("InvalidData"));
                    return;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void clearLine() {
        if (line != null) {
            maskPane.getChildren().remove(line);
            line = null;
        }
        maskPane.getChildren().removeAll(topLeftHandler, bottomRightHandler);
    }

    public void drawLine() {
        try {
            double x1 = Double.parseDouble(shape.getAttribute("x1"));
            double y1 = Double.parseDouble(shape.getAttribute("y1"));
            double x2 = Double.parseDouble(shape.getAttribute("x2"));
            double y2 = Double.parseDouble(shape.getAttribute("y2"));

            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
            double x1r = x1 * xRatio;
            double y1r = y1 * yRatio;
            double x2r = x2 * xRatio;
            double y2r = y2 * yRatio;

            double layX = imageView.getLayoutX() + x1r;
            double layY = imageView.getLayoutY() + y1r;

            double x1d = layX;
            double y1d = layY;
            double x2d = layX + x2r - x1r + 1;
            double y2d = layY + y2r - y1r + 1;

            line = new Line(x1d, y1d, x2d, y2d);
            line.setVisible(true);

            topLeftHandler.setLayoutX(x1d - anchorHW);
            topLeftHandler.setLayoutY(y1d - anchorHW);
            bottomRightHandler.setLayoutX(x2d - anchorHW);
            bottomRightHandler.setLayoutY(y2d - anchorHW);

            if (!maskPane.getChildren().contains(line)) {
                maskPane.getChildren().add(line);
                maskPane.getChildren().addAll(topLeftHandler, bottomRightHandler);
            }

            setShapeStyle(line);
            setAnchorStyle();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (null == shapeType || imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (isPickingColor) {
            return;
        }
        switch (shapeType) {
            case Polyline: {
                if (event.getButton() != MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                DoublePoint p0 = maskPolylineLineData.get(0);
                double offsetX = p.getX() - p0.getX();
                double offsetY = p.getY() - p0.getY();
                if (offsetX != 0 || offsetY != 0) {
                    maskPolylineLineData = maskPolylineLineData.move(offsetX, offsetY);
                    drawMaskPolyline();
                }
            }
            break;
        }

    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (null == shapeType || imageView.getImage() == null) {
            return;
        }
        if (isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        showXY(event, p);

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (lastPoint != null && lastPoint.getX() == p.getX() && lastPoint.getY() == p.getY()) {
            return;
        }
        switch (shapeType) {
            case Polyline:
                scrollPane.setPannable(false);
                maskPolylineLineData.add(p);
                lastPoint = p;
                drawMaskPolyline();
                break;
        }
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        if (null == shapeType || imageView.getImage() == null) {
            return;
        }
        if (isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        showXY(event, p);

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (lastPoint != null && lastPoint.getX() == p.getX() && lastPoint.getY() == p.getY()) {
            return;
        }
        switch (shapeType) {
            case Polyline:
                scrollPane.setPannable(false);
                maskPolylineLineData.add(p);
                lastPoint = p;
                drawMaskPolyline();
                break;

        }

    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (null == shapeType || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        showXY(event, p);

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        switch (shapeType) {
            case Polyline:
                if (lastPoint == null || lastPoint.getX() != p.getX() || lastPoint.getY() != p.getY()) {
                    maskPolylineLineData.add(p);
                    lastPoint = p;
                }
                drawMaskPolyline();
                break;

        }
    }

}
