package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Shapes extends BaseImageController_Mask {

    protected DoubleRectangle maskRectangleData;
    protected DoubleCircle maskCircleData;
    protected DoubleEllipse maskEllipseData;
    protected DoubleLine maskLineData;
    protected DoublePolygon maskPolygonData;
    protected DoublePolyline maskPolylineData;
    protected DoublePolyline maskPolylineLineData;
    protected List<Line> maskPolylineLines;
    protected DoubleLines maskPenData;
    protected List<List<Line>> maskPenLines;
    protected final SimpleBooleanProperty rectDrawnNotify, circleDrawnNotify, ellipseDrawnNotify,
            lineDrawnNotify, polylineDrawnNotify, polygonDrawnNotify;
    public boolean maskPointDragged;

    @FXML
    protected Rectangle maskRectangleLine, leftCenterHandler, topLeftHandler, topCenterHandler, topRightHandler,
            bottomLeftHandler, bottomCenterHandler, bottomRightHandler, rightCenterHandler;
    @FXML
    protected Circle maskCircleLine;
    @FXML
    protected Ellipse maskEllipseLine;
    @FXML
    protected Line maskLine;
    @FXML
    protected Polygon maskPolygonLine;
    @FXML
    protected Polyline maskPolyline;

    public BaseImageController_Shapes() {
        rectDrawnNotify = new SimpleBooleanProperty(false);
        circleDrawnNotify = new SimpleBooleanProperty(false);
        ellipseDrawnNotify = new SimpleBooleanProperty(false);
        lineDrawnNotify = new SimpleBooleanProperty(false);
        polylineDrawnNotify = new SimpleBooleanProperty(false);
        polygonDrawnNotify = new SimpleBooleanProperty(false);
    }

    /*
        image
     */
    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
        redrawMaskShapes();
    }

    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;
            updateLabelsTitle();
            if (imageChanged) {
                redrawMaskShapes();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected String moreDisplayInfo() {
        if (maskRectangleLine != null && maskRectangleLine.isVisible() && maskRectangleData != null) {
            return Languages.message("SelectedSize") + ":"
                    + (int) (maskRectangleData.getWidth() / widthRatio()) + "x"
                    + (int) (maskRectangleData.getHeight() / heightRatio());
        } else {
            return "";
        }
    }

    /*
        all shapes
     */
    public void setMaskShapesStyle() {
        setShapeStyle(maskRectangleLine);
        setShapeStyle(maskCircleLine);
        setShapeStyle(maskEllipseLine);
        setShapeStyle(maskLine);
        setShapeStyle(maskPolygonLine);
        setShapeStyle(maskPolyline);

    }

    public void setShapeStyle(Shape shape) {
        if (shape == null) {
            return;
        }
        shape.setStroke(strokeColor());
        shape.setStrokeWidth(strokeWidth());
        shape.setStrokeLineCap(strokeLineCap());
        shape.getStrokeDashArray().clear();
        List<Double> dash = strokeDash();
        if (dash != null) {
            shape.getStrokeDashArray().addAll(dash);
        }
        shape.setOpacity(shapeOpacity());
        shape.setFill(shapeFill());
    }

    public void setMaskAnchorsStyle() {
        if (isSettingValues || maskPane == null) {
            return;
        }
        try {
            Color anchorColor = anchorColor();
            int anchorWidth = anchorWidth();
            if (anchorWidth <= 0) {
                anchorWidth = 10;
            }

            if (topLeftHandler != null) {
                topLeftHandler.setStroke(anchorColor);
                topCenterHandler.setStroke(anchorColor);
                topRightHandler.setStroke(anchorColor);
                bottomLeftHandler.setStroke(anchorColor);
                bottomCenterHandler.setStroke(anchorColor);
                bottomRightHandler.setStroke(anchorColor);
                leftCenterHandler.setStroke(anchorColor);
                rightCenterHandler.setStroke(anchorColor);

                topLeftHandler.setWidth(anchorWidth);
                topLeftHandler.setHeight(anchorWidth);
                topCenterHandler.setWidth(anchorWidth);
                topCenterHandler.setHeight(anchorWidth);
                topRightHandler.setWidth(anchorWidth);
                topRightHandler.setHeight(anchorWidth);
                bottomLeftHandler.setWidth(anchorWidth);
                bottomLeftHandler.setHeight(anchorWidth);
                bottomCenterHandler.setWidth(anchorWidth);
                bottomCenterHandler.setHeight(anchorWidth);
                bottomRightHandler.setWidth(anchorWidth);
                bottomRightHandler.setHeight(anchorWidth);
                leftCenterHandler.setWidth(anchorWidth);
                leftCenterHandler.setHeight(anchorWidth);
                rightCenterHandler.setWidth(anchorWidth);
                rightCenterHandler.setHeight(anchorWidth);

                topLeftHandler.setFill(anchorColor);
                topCenterHandler.setFill(anchorColor);
                topRightHandler.setFill(anchorColor);
                bottomLeftHandler.setFill(anchorColor);
                bottomCenterHandler.setFill(anchorColor);
                bottomRightHandler.setFill(anchorColor);
                leftCenterHandler.setFill(anchorColor);
                rightCenterHandler.setFill(anchorColor);
            }

            Font font = shapePointFont();
            for (Node node : maskPane.getChildren()) {
                if (node == null || !(node instanceof Text) || node.getId() == null) {
                    continue;
                }
                if (node.getId().startsWith("PolylinePoint") || node.getId().startsWith("PolygonPoint")) {
                    Text text = (Text) node;
                    text.setFill(anchorColor);
                    text.setFont(font);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskShapes() {
        try {
            drawMaskRulerXY();
            clearMaskRectangle();
            clearMaskCircle();
            clearMaskEllipse();
            clearMaskLine();
            clearMaskPolyline();
            clearMaskPolygon();
            clearMaskPolylineLines();
            clearMaskPenLines();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Any mask operations when pane size is changed
    public void redrawMaskShapes() {
        try {
            drawMaskRulerXY();
            checkCoordinate();
            if (drawMaskRectangle()) {
                return;
            }
            if (drawMaskCircle()) {
                return;
            }
            if (drawMaskEllipse()) {
                return;
            }
            if (drawMaskLine()) {
                return;
            }
            if (drawMaskPolygon()) {
                return;
            }
            if (drawMaskPolyline()) {
                return;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        rectangle
     */
    public boolean showMaskRectangle() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return false;
        }
        try {
            if (!maskPane.getChildren().contains(maskRectangleLine)) {
                maskPane.getChildren().addAll(maskRectangleLine);
                if (leftCenterHandler != null && !maskPane.getChildren().contains(leftCenterHandler)) {
                    maskPane.getChildren().addAll(leftCenterHandler, rightCenterHandler,
                            topLeftHandler, topCenterHandler, topRightHandler,
                            bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
                }
            }
            maskRectangleLine.setOpacity(1);
            maskRectangleLine.setVisible(true);
            return drawMaskRectangle();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setMaskRectangleDefaultValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskRectangleData = new DoubleRectangle(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskRectangle() {
        try {
            if (maskRectangleLine == null
                    || !maskPane.getChildren().contains(maskRectangleLine)
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskRectangleData == null) {
                setMaskRectangleDefaultValues();
            }
            int anchorHW = anchorWidth() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double x1 = maskRectangleData.getSmallX() * xRatio;
            double y1 = maskRectangleData.getSmallY() * yRatio;
            double x2 = maskRectangleData.getBigX() * xRatio;
            double y2 = maskRectangleData.getBigY() * yRatio;
            maskRectangleLine.setLayoutX(imageView.getLayoutX() + x1);
            maskRectangleLine.setLayoutY(imageView.getLayoutY() + y1);
            maskRectangleLine.setWidth(x2 - x1 + 1);
            maskRectangleLine.setHeight(y2 - y1 + 1);

            double lineX = maskRectangleLine.getLayoutX();
            double lineY = maskRectangleLine.getLayoutY();
            topLeftHandler.setLayoutX(lineX - anchorHW);
            topLeftHandler.setLayoutY(lineY - anchorHW);
            topCenterHandler.setLayoutX(lineX + maskRectangleLine.getWidth() / 2 - anchorHW);
            topCenterHandler.setLayoutY(lineY - anchorHW);
            topRightHandler.setLayoutX(lineX + maskRectangleLine.getWidth() - anchorHW);
            topRightHandler.setLayoutY(lineY - anchorHW);
            bottomLeftHandler.setLayoutX(lineX - anchorHW);
            bottomLeftHandler.setLayoutY(lineY + maskRectangleLine.getHeight() - anchorHW);
            bottomCenterHandler.setLayoutX(lineX + maskRectangleLine.getWidth() / 2 - anchorHW);
            bottomCenterHandler.setLayoutY(lineY + maskRectangleLine.getHeight() - anchorHW);
            bottomRightHandler.setLayoutX(lineX + maskRectangleLine.getWidth() - anchorHW);
            bottomRightHandler.setLayoutY(lineY + maskRectangleLine.getHeight() - anchorHW);
            leftCenterHandler.setLayoutX(lineX - anchorHW);
            leftCenterHandler.setLayoutY(lineY + maskRectangleLine.getHeight() / 2 - anchorHW);
            rightCenterHandler.setLayoutX(lineX + maskRectangleLine.getWidth() - anchorHW);
            rightCenterHandler.setLayoutY(lineY + maskRectangleLine.getHeight() / 2 - anchorHW);

            setShapeStyle(maskRectangleLine);
            setMaskAnchorsStyle();

            rectDrawnNotify.set(!rectDrawnNotify.get());
            updateLabelsTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

    }

    public void clearMaskRectangle() {
        try {
            if (imageView == null || maskPane == null || maskRectangleLine == null) {
                return;
            }
            maskPane.getChildren().removeAll(maskRectangleLine,
                    leftCenterHandler, rightCenterHandler,
                    topLeftHandler, topCenterHandler, topRightHandler,
                    bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
            maskRectangleLine.setVisible(false);
            rectDrawnNotify.set(!rectDrawnNotify.get());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        circle
     */
    public boolean showMaskCircle() {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskCircleLine)) {
            maskPane.getChildren().addAll(maskCircleLine,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
        }
        maskCircleLine.setOpacity(1);
        maskCircleLine.setVisible(true);
        return drawMaskCircle();
    }

    public void setMaskCircleDefaultValues() {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskCircleData = new DoubleCircle(w / 2, h / 2, Math.min(w, h) / 4);
    }

    public boolean drawMaskCircle() {
        try {
            if (maskCircleLine == null || !maskCircleLine.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskCircleData == null) {
                setMaskCircleDefaultValues();
            }
            int anchorHW = anchorWidth() / 2;
            double xRatio = viewXRatio();
            double r = maskCircleData.getRadius() * xRatio;
            double x = maskCircleData.getCenterX() * xRatio;
            double y = maskCircleData.getCenterY() * xRatio;
            maskCircleLine.setLayoutX(imageView.getLayoutX() + x);  // Circle's layout is about its center
            maskCircleLine.setLayoutY(imageView.getLayoutY() + y);
            maskCircleLine.setRadius(r);

            topCenterHandler.setLayoutX(maskCircleLine.getLayoutX() - anchorHW);
            topCenterHandler.setLayoutY(maskCircleLine.getLayoutY() - maskCircleLine.getRadius() - anchorHW);
            bottomCenterHandler.setLayoutX(maskCircleLine.getLayoutX() - anchorHW);
            bottomCenterHandler.setLayoutY(maskCircleLine.getLayoutY() + maskCircleLine.getRadius() - anchorHW);
            leftCenterHandler.setLayoutX(maskCircleLine.getLayoutX() - maskCircleLine.getRadius() - anchorHW);
            leftCenterHandler.setLayoutY(maskCircleLine.getLayoutY() - anchorHW);
            rightCenterHandler.setLayoutX(maskCircleLine.getLayoutX() + maskCircleLine.getRadius() - anchorHW);
            rightCenterHandler.setLayoutY(maskCircleLine.getLayoutY() - anchorHW);

            setShapeStyle(maskCircleLine);
            setMaskAnchorsStyle();

            circleDrawnNotify.set(!circleDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskCircle() {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskCircleLine,
                leftCenterHandler, rightCenterHandler,
                topCenterHandler, bottomCenterHandler);
        maskCircleLine.setVisible(false);
        circleDrawnNotify.set(!circleDrawnNotify.get());
    }

    /*
        ellipse
     */
    public boolean showMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskEllipseLine)) {
            maskPane.getChildren().addAll(maskEllipseLine,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
        }
        maskEllipseLine.setOpacity(1);
        maskEllipseLine.setVisible(true);
        return drawMaskEllipse();
    }

    public void setMaskEllipseDefaultValues() {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskEllipseData = new DoubleEllipse(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskEllipse() {
        try {
            if (maskEllipseLine == null || !maskEllipseLine.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskEllipseData == null) {
                setMaskEllipseDefaultValues();
            }
            int anchorHW = anchorWidth() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double rx = maskEllipseData.getRadiusX() * xRatio;
            double ry = maskEllipseData.getRadiusY() * yRatio;
            double cx = maskEllipseData.getCenterX() * xRatio;
            double cy = maskEllipseData.getCenterY() * xRatio;
            maskEllipseLine.setLayoutX(imageView.getLayoutX() + cx);
            maskEllipseLine.setLayoutY(imageView.getLayoutY() + cy);
            maskEllipseLine.setRadiusX(rx);
            maskEllipseLine.setRadiusY(ry);

            topCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() - anchorHW);
            topCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() - maskEllipseLine.getRadiusY() - anchorHW);
            bottomCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() - anchorHW);
            bottomCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() + maskEllipseLine.getRadiusY() - anchorHW);
            leftCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() - maskEllipseLine.getRadiusX() - anchorHW);
            leftCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() - anchorHW);
            rightCenterHandler.setLayoutX(maskEllipseLine.getLayoutX() + maskEllipseLine.getRadiusX() - anchorHW);
            rightCenterHandler.setLayoutY(maskEllipseLine.getLayoutY() - anchorHW);

            setShapeStyle(maskEllipseLine);
            setMaskAnchorsStyle();

            ellipseDrawnNotify.set(!ellipseDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

    }

    public void clearMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskEllipseLine,
                leftCenterHandler, rightCenterHandler,
                topCenterHandler, bottomCenterHandler);
        maskEllipseLine.setVisible(false);
        ellipseDrawnNotify.set(!ellipseDrawnNotify.get());
    }

    /*
        line
     */
    public boolean showMaskLine() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskLine)) {
            maskPane.getChildren().addAll(maskLine,
                    topLeftHandler, bottomRightHandler);
        }
        maskLine.setOpacity(1);
        maskLine.setVisible(true);
        return drawMaskLine();
    }

    public void setMaskLineDefaultValues() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskLineData = new DoubleLine(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskLine() {
        try {
            if (maskLine == null || !maskLine.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskLineData == null) {
                setMaskLineDefaultValues();
            }
            int anchorHW = anchorWidth() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double startX = imageView.getLayoutX() + maskLineData.getStartX() * xRatio;
            double startY = imageView.getLayoutY() + maskLineData.getStartY() * yRatio;
            double endX = imageView.getLayoutX() + maskLineData.getEndX() * xRatio;
            double endY = imageView.getLayoutY() + maskLineData.getEndY() * yRatio;

            maskLine.setStartX(startX);
            maskLine.setStartY(startY);
            maskLine.setEndX(endX);
            maskLine.setEndY(endY);
            maskLine.setVisible(true);

            topLeftHandler.setLayoutX(startX - anchorHW);
            topLeftHandler.setLayoutY(startY - anchorHW);
            bottomRightHandler.setLayoutX(endX - anchorHW);
            bottomRightHandler.setLayoutY(endY - anchorHW);

            setShapeStyle(maskLine);
            setMaskAnchorsStyle();

            lineDrawnNotify.set(!lineDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskLine() {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskLine,
                topLeftHandler, bottomRightHandler);
        maskLine.setVisible(false);
        lineDrawnNotify.set(!lineDrawnNotify.get());
    }

    /*
        polyline
     */
    public boolean showMaskPolyline() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskPolyline)) {
            maskPane.getChildren().addAll(maskPolyline);
        }
        maskPolyline.setOpacity(1);
        maskPolyline.setVisible(true);
        return drawMaskPolyline();
    }

    public void setMaskPolylineDefaultValues() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPolylineData = new DoublePolyline();
    }

    public boolean drawMaskPolyline() {
        try {
            if (maskPolyline == null || !maskPolyline.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskPolylineData == null) {
                setMaskPolylineDefaultValues();
            }
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();

            clearPolylinePoints();
            maskPolyline.getPoints().clear();
            maskPolyline.setLayoutX(imageView.getLayoutX());
            maskPolyline.setLayoutY(imageView.getLayoutY());
            for (int i = 0; i < maskPolylineData.getSize(); ++i) {
                DoublePoint p = maskPolylineData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolylinePoint(i + 1, p, x, y);
            }

            setShapeStyle(maskPolyline);

            polylineDrawnNotify.set(!polylineDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolylinePoint(int index, DoublePoint p, double x, double y,
            Color color, Font font) {
        try {
            maskPolyline.getPoints().add(x);
            maskPolyline.getPoints().add(y);

            Text text = new Text(index + "");
            text.setFill(color);
            text.setFont(font);
            text.setLayoutX(imageView.getLayoutX() + x);
            text.setLayoutY(imageView.getLayoutY() + y);
            text.setId("PolylinePoint" + index);
            text.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    maskPointDragged = true;
                    scrollPane.setPannable(true);
                    double nx = maskHandlerX(text, event);
                    double ny = maskHandlerY(text, event);
                    maskPolygonData.getPoints().set(index - 1, new DoublePoint(nx, ny));
                    drawMaskPolygon();
                    maskPolygonChangedByEvent();
                }
            });
            NodeStyleTools.setTooltip(text, message("Point") + " " + index + "\n" + p.text(2));
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void addMaskPolylinePoint(int index, DoublePoint p, double x, double y) {
        addMaskPolylinePoint(index, p, x, y, anchorColor(), shapePointFont());
    }

    public void clearPolylinePoints() {
        if (maskPane == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().startsWith("PolylinePoint")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void clearMaskPolyline() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolyline);
        maskPolyline.setVisible(false);
        maskPolyline.getPoints().clear();
        if (maskPolylineData != null) {
            maskPolylineData.clear();
            maskPolylineData = null;
        }
        clearPolylinePoints();
        polylineDrawnNotify.set(!polylineDrawnNotify.get());
    }

    /*
        polygon
     */
    public boolean showMaskPolygon() {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskPolygonLine)) {
            maskPane.getChildren().addAll(maskPolygonLine);
        }
        maskPolygonLine.setOpacity(1);
        maskPolygonLine.setVisible(true);
        return drawMaskPolygon();
    }

    public void setMaskPolygonDefaultValues() {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonData = new DoublePolygon();
    }

    public boolean drawMaskPolygon() {
        try {
            if (maskPolygonLine == null || !maskPolygonLine.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskPolygonData == null) {
                setMaskPolygonDefaultValues();
            }
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();

            clearPolygonPoints();
            maskPolygonLine.getPoints().clear();
            maskPolygonLine.setLayoutX(imageView.getLayoutX());
            maskPolygonLine.setLayoutY(imageView.getLayoutY());
            Color color = anchorColor();
            Font font = shapePointFont();
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                DoublePoint p = maskPolygonData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolygonPoint(i + 1, p, x, y, color, font);
            }

            setShapeStyle(maskPolygonLine);

            polygonDrawnNotify.set(!polygonDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolygonPoint(int index, DoublePoint p,
            double x, double y, Color color, Font font) {
        try {
            maskPolygonLine.getPoints().add(x);
            maskPolygonLine.getPoints().add(y);

            Text text = new Text(index + "");
            text.setFill(color);
            text.setFont(font);
            text.setLayoutX(imageView.getLayoutX() + x);
            text.setLayoutY(imageView.getLayoutY() + y);
            text.setId("PolygonPoint" + index);

            text.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    maskPointDragged = true;
                    scrollPane.setPannable(true);
                    double nx = maskHandlerX(text, event);
                    double ny = maskHandlerY(text, event);
                    maskPolygonData.getPoints().set(index - 1, new DoublePoint(nx, ny));
                    drawMaskPolygon();
                    maskPolygonChangedByEvent();
                }
            });
            NodeStyleTools.setTooltip(text, message("Point") + " " + index + "\n" + p.text(2));
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void addMaskPolygonPoint(int index, DoublePoint p, double x, double y) {
        addMaskPolygonPoint(index, p, x, y, anchorColor(), shapePointFont());
    }

    public void clearPolygonPoints() {
        if (maskPane == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().startsWith("PolygonPoint")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void clearMaskPolygon() {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolygonLine);
        maskPolygonLine.setVisible(false);
        maskPolygonLine.getPoints().clear();
        if (maskPolygonData != null) {
            maskPolygonData.clear();
            maskPolygonData = null;
        }
        clearPolygonPoints();
        polygonDrawnNotify.set(!polygonDrawnNotify.get());
    }

    /*
       polyline lines
     */
    public void showMaskPolylineLines() {
        try {
            if (imageView == null || maskPane == null) {
                return;
            }
            clearMaskPolylineLines();
            maskPolylineLines = new ArrayList<>();
            maskPolylineLineData = new DoublePolyline();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Polyline of Java shows weird results. So I just use lines directly.
    public boolean drawMaskPolylineLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity) {
        maskPane.getChildren().removeAll(maskPolylineLines);
        maskPolylineLines.clear();
        int size = maskPolylineLineData.getSize();
        if (size <= 1) {
            return true;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        double drawStrokeWidth = strokeWidth * xRatio;
        double lastx = -1, lasty = -1, thisx, thisy;
        for (DoublePoint p : maskPolylineLineData.getPoints()) {
            thisx = p.getX() * xRatio;
            thisy = p.getY() * yRatio;
            if (lastx >= 0) {
                Line line = new Line(lastx, lasty, thisx, thisy);
                if (strokeColor.equals(Color.TRANSPARENT)) {
                    // Have not found how to make line as transparent. For display only.
                    line.setStroke(Color.WHITE);
                } else {
                    line.setStroke(strokeColor);
                }
                line.setStrokeWidth(drawStrokeWidth);
                line.getStrokeDashArray().clear();
                if (dotted) {
                    line.getStrokeDashArray().addAll(drawStrokeWidth * 1d, drawStrokeWidth * 3d);
                }
                line.setOpacity(opacity);
                maskPolylineLines.add(line);
                maskPane.getChildren().add(line);
                line.setLayoutX(imageView.getLayoutX());
                line.setLayoutY(imageView.getLayoutY());
            }
            lastx = thisx;
            lasty = thisy;
        }
        return true;
    }

    public void clearMaskPolylineLines() {
        try {
            if (maskPane == null) {
                return;
            }
            if (maskPolylineLines != null) {
                maskPane.getChildren().removeAll(maskPolylineLines);
                maskPolylineLines.clear();
            }
            if (maskPolylineLineData != null) {
                maskPolylineLineData.clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        pen lines
     */
    public void showMaskPenlines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        clearMaskPenLines();
        maskPenLines = new ArrayList<>();
        maskPenData = new DoubleLines();
    }

    public Line drawMaskPenLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity,
            DoublePoint lastPonit, DoublePoint thisPoint) {
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (lastPonit == null) {
            return null;
        } else if (thisPoint != null) {
            Line line = makeMaskPenLine(strokeWidth, strokeColor, dotted, opacity, drawStrokeWidth,
                    lastPonit.getX(), lastPonit.getY(), thisPoint.getX(), thisPoint.getY(), xRatio, yRatio);
            if (line != null) {
                maskPane.getChildren().add(line);
                line.setLayoutX(imageView.getLayoutX());
                line.setLayoutY(imageView.getLayoutY());
            }
            return line;
        } else {
            return null;
        }
    }

    public Line makeMaskPenLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity, double drawStrokeWidth,
            double lastx, double lasty, double thisx, double thisy, double xRatio, double yRatio) {
        Line line = new Line(lastx * xRatio, lasty * yRatio, thisx * xRatio, thisy * yRatio);
        if (strokeColor.equals(Color.TRANSPARENT)) {
            // Have not found how to make line as transparent. For display only.
            line.setStroke(Color.WHITE);
        } else {
            line.setStroke(strokeColor);
        }
        line.setStrokeWidth(drawStrokeWidth);
        line.getStrokeDashArray().clear();
        if (dotted) {
            line.getStrokeDashArray().addAll(drawStrokeWidth * 1d, drawStrokeWidth * 3d);
        }
        line.setOpacity(opacity);
        return line;
    }

    public void clearMaskPenLines() {
        if (maskPenLines != null) {
            for (List<Line> penline : maskPenLines) {
                maskPane.getChildren().removeAll(penline);
            }
            maskPenLines.clear();
            maskPenLines = null;
        }
        if (maskPenData != null) {
            maskPenData.clear();
            maskPenData = null;
        }
    }

    /*
        noitify
     */
    public void maskRectChangedByEvent() {

    }

    public void maskCircleChangedByEvent() {

    }

    public void maskEllipseChangedByEvent() {

    }

    public void maskLineChangedByEvent() {

    }

    public void maskPolylineChangedByEvent() {

    }

    public void maskPolygonChangedByEvent() {

    }

    /*
        static
     */
    public static void updateMaskStrokes() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController_Mask) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.setMaskShapesStyle();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void updateMaskAnchors() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController_Mask) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.setMaskAnchorsStyle();
                } catch (Exception e) {
                }
            }
        }
    }

}
