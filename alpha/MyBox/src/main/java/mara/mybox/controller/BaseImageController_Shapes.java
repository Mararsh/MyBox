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
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
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
    protected DoubleLines maskLinesData;
    protected List<List<Line>> maskLines;
    protected DoublePath pathData;
    protected SVGPath svgPath;
    public boolean maskPointDragged;
    public ShapeType shapeType = null;
    public final SimpleBooleanProperty maskShapeChangedNotify = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty maskShapeDataChanged = new SimpleBooleanProperty(false);

    public enum ShapeType {
        Rectangle, Circle, Ellipse, Line, Polygon, Polyline, Lines, Path, Text;
    }

    @FXML
    protected Rectangle maskRectangle, leftCenterHandler, topLeftHandler, topCenterHandler, topRightHandler,
            bottomLeftHandler, bottomCenterHandler, bottomRightHandler, rightCenterHandler;
    @FXML
    protected Circle maskCircle;
    @FXML
    protected Ellipse maskEllipse;
    @FXML
    protected Line maskLine;
    @FXML
    protected Polygon maskPolygon;
    @FXML
    protected Polyline maskPolyline;


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
        if (maskRectangle != null && maskRectangle.isVisible() && maskRectangleData != null) {
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
        setShapeStyle(maskRectangle);
        setShapeStyle(maskCircle);
        setShapeStyle(maskEllipse);
        setShapeStyle(maskLine);
        setShapeStyle(maskPolygon);
        setShapeStyle(maskPolyline);
        setShapeStyle(svgPath);
        if (maskLines != null && !maskLines.isEmpty()) {
            for (List<Line> lines : maskLines) {
                for (Line line : lines) {
                    setShapeStyle(line);
                }
            }
        }
    }

    public void setShapeStyle(Shape shape) {
        if (shape == null) {
            return;
        }
        Color strokeColor = strokeColor();
        if (strokeColor.equals(Color.TRANSPARENT)) {
            // Have not found how to make line as transparent. For display only.
            strokeColor = Color.WHITE;
        }
        shape.setStroke(strokeColor);
        double strokeWidth = strokeWidth();
        shape.setStrokeWidth(strokeWidth);
        shape.getStrokeDashArray().clear();
        shape.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);
        shape.setFill(Color.TRANSPARENT);
    }

    public void setMaskAnchorsStyle() {
        setMaskAnchorsStyle(anchorColor(), anchorSize());
    }

    public void setMaskAnchorsStyle(Color anchorColor, float anchorSize) {
        if (isSettingValues || maskPane == null) {
            return;
        }
        try {
            if (topLeftHandler != null) {
                topLeftHandler.setStroke(anchorColor);
                topCenterHandler.setStroke(anchorColor);
                topRightHandler.setStroke(anchorColor);
                bottomLeftHandler.setStroke(anchorColor);
                bottomCenterHandler.setStroke(anchorColor);
                bottomRightHandler.setStroke(anchorColor);
                leftCenterHandler.setStroke(anchorColor);
                rightCenterHandler.setStroke(anchorColor);

                topLeftHandler.setWidth(anchorSize);
                topLeftHandler.setHeight(anchorSize);
                topCenterHandler.setWidth(anchorSize);
                topCenterHandler.setHeight(anchorSize);
                topRightHandler.setWidth(anchorSize);
                topRightHandler.setHeight(anchorSize);
                bottomLeftHandler.setWidth(anchorSize);
                bottomLeftHandler.setHeight(anchorSize);
                bottomCenterHandler.setWidth(anchorSize);
                bottomCenterHandler.setHeight(anchorSize);
                bottomRightHandler.setWidth(anchorSize);
                bottomRightHandler.setHeight(anchorSize);
                leftCenterHandler.setWidth(anchorSize);
                leftCenterHandler.setHeight(anchorSize);
                rightCenterHandler.setWidth(anchorSize);
                rightCenterHandler.setHeight(anchorSize);

                topLeftHandler.setFill(anchorColor);
                topCenterHandler.setFill(anchorColor);
                topRightHandler.setFill(anchorColor);
                bottomLeftHandler.setFill(anchorColor);
                bottomCenterHandler.setFill(anchorColor);
                bottomRightHandler.setFill(anchorColor);
                leftCenterHandler.setFill(anchorColor);
                rightCenterHandler.setFill(anchorColor);
            }

            Font font = new Font(anchorSize);
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

    public void clearMask() {
        try {
            drawMaskRulers();
            clearMaskShapes();
            clearMaskShapesData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskShapes() {
        try {
            drawMaskRulers();
            clearMaskRectangle();
            clearMaskCircle();
            clearMaskEllipse();
            clearMaskLine();
            clearMaskPolyline();
            clearMaskPolygon();
            clearMaskLines();
            clearPath();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskShapesData() {
        try {
            clearMaskRectangleData();
            clearMaskCircleData();
            clearMaskEllipseData();
            clearMaskLineData();
            clearMaskPolylineData();
            clearMaskPolygonData();
            clearMaskLinesData();
            clearPathData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Any mask operations when pane size is changed
    public void redrawMaskShapes() {
        try {
            drawMaskRulers();
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
            if (drawPath()) {
                return;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void maskShapeChanged() {
        if (!isSettingValues) {
            maskShapeChangedNotify.set(!maskShapeChangedNotify.get());
        }
    }

    public void maskShapeDataChanged() {
        if (!isSettingValues) {
            maskShapeDataChanged.set(!maskShapeDataChanged.get());
        }
    }

    /*
        rectangle
     */
    public boolean showMaskRectangle() {
        if (imageView == null || maskPane == null || maskRectangle == null) {
            return false;
        }
        try {
            if (!maskPane.getChildren().contains(maskRectangle)) {
                maskPane.getChildren().addAll(maskRectangle);
                if (leftCenterHandler != null && !maskPane.getChildren().contains(leftCenterHandler)) {
                    maskPane.getChildren().addAll(leftCenterHandler, rightCenterHandler,
                            topLeftHandler, topCenterHandler, topRightHandler,
                            bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
                }
            }
            maskRectangle.setOpacity(1);
            maskRectangle.setVisible(true);
            return drawMaskRectangle();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setMaskRectangleDefaultValues() {
        if (imageView == null || maskPane == null || maskRectangle == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskRectangleData = new DoubleRectangle(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskRectangle() {
        try {
            if (maskRectangle == null
                    || !maskPane.getChildren().contains(maskRectangle)
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskRectangleData == null) {
                setMaskRectangleDefaultValues();
            }
            float anchorHW = anchorSize() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double x1 = maskRectangleData.getSmallX() * xRatio;
            double y1 = maskRectangleData.getSmallY() * yRatio;
            double x2 = maskRectangleData.getBigX() * xRatio;
            double y2 = maskRectangleData.getBigY() * yRatio;
            maskRectangle.setLayoutX(imageView.getLayoutX() + x1);
            maskRectangle.setLayoutY(imageView.getLayoutY() + y1);
            maskRectangle.setWidth(x2 - x1 + 1);
            maskRectangle.setHeight(y2 - y1 + 1);

            double lineX = maskRectangle.getLayoutX();
            double lineY = maskRectangle.getLayoutY();
            topLeftHandler.setLayoutX(lineX - anchorHW);
            topLeftHandler.setLayoutY(lineY - anchorHW);
            topCenterHandler.setLayoutX(lineX + maskRectangle.getWidth() / 2 - anchorHW);
            topCenterHandler.setLayoutY(lineY - anchorHW);
            topRightHandler.setLayoutX(lineX + maskRectangle.getWidth() - anchorHW);
            topRightHandler.setLayoutY(lineY - anchorHW);
            bottomLeftHandler.setLayoutX(lineX - anchorHW);
            bottomLeftHandler.setLayoutY(lineY + maskRectangle.getHeight() - anchorHW);
            bottomCenterHandler.setLayoutX(lineX + maskRectangle.getWidth() / 2 - anchorHW);
            bottomCenterHandler.setLayoutY(lineY + maskRectangle.getHeight() - anchorHW);
            bottomRightHandler.setLayoutX(lineX + maskRectangle.getWidth() - anchorHW);
            bottomRightHandler.setLayoutY(lineY + maskRectangle.getHeight() - anchorHW);
            leftCenterHandler.setLayoutX(lineX - anchorHW);
            leftCenterHandler.setLayoutY(lineY + maskRectangle.getHeight() / 2 - anchorHW);
            rightCenterHandler.setLayoutX(lineX + maskRectangle.getWidth() - anchorHW);
            rightCenterHandler.setLayoutY(lineY + maskRectangle.getHeight() / 2 - anchorHW);

            setShapeStyle(maskRectangle);
            setMaskAnchorsStyle();

            shapeType = ShapeType.Rectangle;

            maskShapeChanged();

            updateLabelsTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskRectangle() {
        try {
            if (imageView == null || maskPane == null || maskRectangle == null) {
                return;
            }
            maskPane.getChildren().removeAll(maskRectangle,
                    leftCenterHandler, rightCenterHandler,
                    topLeftHandler, topCenterHandler, topRightHandler,
                    bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
            maskRectangle.setVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskRectangleData() {
        if (maskRectangleData != null) {
            maskRectangleData = null;
        }
    }

    /*
        circle
     */
    public boolean showMaskCircle() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskCircle)) {
            maskPane.getChildren().addAll(maskCircle,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
        }
        maskCircle.setOpacity(1);
        maskCircle.setVisible(true);
        return drawMaskCircle();
    }

    public void setMaskCircleDefaultValues() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskCircleData = new DoubleCircle(w / 2, h / 2, Math.min(w, h) / 4);
    }

    public boolean drawMaskCircle() {
        try {
            if (maskCircle == null || !maskCircle.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskCircleData == null) {
                setMaskCircleDefaultValues();
            }
            float anchorHW = anchorSize() / 2;
            double xRatio = viewXRatio();
            double r = maskCircleData.getRadius() * xRatio;
            double x = maskCircleData.getCenterX() * xRatio;
            double y = maskCircleData.getCenterY() * xRatio;
            maskCircle.setLayoutX(imageView.getLayoutX() + x);  // Circle's layout is about its center
            maskCircle.setLayoutY(imageView.getLayoutY() + y);
            maskCircle.setRadius(r);

            topCenterHandler.setLayoutX(maskCircle.getLayoutX() - anchorHW);
            topCenterHandler.setLayoutY(maskCircle.getLayoutY() - maskCircle.getRadius() - anchorHW);
            bottomCenterHandler.setLayoutX(maskCircle.getLayoutX() - anchorHW);
            bottomCenterHandler.setLayoutY(maskCircle.getLayoutY() + maskCircle.getRadius() - anchorHW);
            leftCenterHandler.setLayoutX(maskCircle.getLayoutX() - maskCircle.getRadius() - anchorHW);
            leftCenterHandler.setLayoutY(maskCircle.getLayoutY() - anchorHW);
            rightCenterHandler.setLayoutX(maskCircle.getLayoutX() + maskCircle.getRadius() - anchorHW);
            rightCenterHandler.setLayoutY(maskCircle.getLayoutY() - anchorHW);

            setShapeStyle(maskCircle);
            setMaskAnchorsStyle();

            shapeType = ShapeType.Circle;

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskCircle() {
        if (imageView == null || maskPane == null || maskCircle == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskCircle,
                leftCenterHandler, rightCenterHandler,
                topCenterHandler, bottomCenterHandler);
        maskCircle.setVisible(false);
    }

    public void clearMaskCircleData() {
        if (maskCircleData != null) {
            maskCircleData = null;
        }
    }

    /*
        ellipse
     */
    public boolean showMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskEllipse)) {
            maskPane.getChildren().addAll(maskEllipse,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
        }
        maskEllipse.setOpacity(1);
        maskEllipse.setVisible(true);
        return drawMaskEllipse();
    }

    public void setMaskEllipseDefaultValues() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskEllipseData = new DoubleEllipse(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskEllipse() {
        try {
            if (maskEllipse == null || !maskEllipse.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskEllipseData == null) {
                setMaskEllipseDefaultValues();
            }
            float anchorHW = anchorSize() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double rx = maskEllipseData.getRadiusX() * xRatio;
            double ry = maskEllipseData.getRadiusY() * yRatio;
            double cx = maskEllipseData.getCenterX() * xRatio;
            double cy = maskEllipseData.getCenterY() * xRatio;
            maskEllipse.setLayoutX(imageView.getLayoutX() + cx);
            maskEllipse.setLayoutY(imageView.getLayoutY() + cy);
            maskEllipse.setRadiusX(rx);
            maskEllipse.setRadiusY(ry);

            topCenterHandler.setLayoutX(maskEllipse.getLayoutX() - anchorHW);
            topCenterHandler.setLayoutY(maskEllipse.getLayoutY() - maskEllipse.getRadiusY() - anchorHW);
            bottomCenterHandler.setLayoutX(maskEllipse.getLayoutX() - anchorHW);
            bottomCenterHandler.setLayoutY(maskEllipse.getLayoutY() + maskEllipse.getRadiusY() - anchorHW);
            leftCenterHandler.setLayoutX(maskEllipse.getLayoutX() - maskEllipse.getRadiusX() - anchorHW);
            leftCenterHandler.setLayoutY(maskEllipse.getLayoutY() - anchorHW);
            rightCenterHandler.setLayoutX(maskEllipse.getLayoutX() + maskEllipse.getRadiusX() - anchorHW);
            rightCenterHandler.setLayoutY(maskEllipse.getLayoutY() - anchorHW);

            setShapeStyle(maskEllipse);
            setMaskAnchorsStyle();

            shapeType = ShapeType.Ellipse;

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

    }

    public void clearMaskEllipse() {
        if (imageView == null || maskPane == null || maskEllipse == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskEllipse,
                leftCenterHandler, rightCenterHandler,
                topCenterHandler, bottomCenterHandler);
        maskEllipse.setVisible(false);
    }

    public void clearMaskEllipseData() {
        if (maskEllipseData != null) {
            maskEllipseData = null;
        }
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
            float anchorHW = anchorSize() / 2;
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

            shapeType = ShapeType.Line;

            maskShapeChanged();

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

    }

    public void clearMaskLineData() {
        if (maskLineData != null) {
            maskLineData = null;
        }
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
        double w = getImageWidth();
        double h = getImageHeight();
        maskPolylineData = new DoublePolyline();
        maskPolylineData.add(10, 10);
        maskPolylineData.add(w / 2, 10);
        maskPolylineData.add(w / 4, h / 2);
        maskPolylineData.add(w - 30, h / 2);
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
            Color color = anchorColor();
            Font font = new Font(anchorSize());
            for (int i = 0; i < maskPolylineData.getSize(); ++i) {
                DoublePoint p = maskPolylineData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolylinePoint(i + 1, p, x, y, color, font);
            }

            setShapeStyle(maskPolyline);

            shapeType = ShapeType.Polyline;

            maskShapeChanged();

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
            text.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scrollPane.setPannable(false);
                }
            });
            text.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    maskPointDragged = true;
                    scrollPane.setPannable(true);
                    double nx = maskHandlerX(text, event);
                    double ny = maskHandlerY(text, event);
                    if (DoubleShape.changed(nx - p.getX(), ny - p.getY())) {
                        maskPolylineData.getPoints().set(index - 1, new DoublePoint(nx, ny));
                        drawMaskPolyline();
                        maskShapeDataChanged();
                    }
                }
            });
            NodeStyleTools.setTooltip(text, message("Point") + " " + index + "\n" + p.text(2));
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void addMaskPolylinePoint(int index, DoublePoint p, double x, double y) {
        addMaskPolylinePoint(index, p, x, y, anchorColor(), new Font(anchorSize()));
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

        clearPolylinePoints();

    }

    public void removeMaskPolylineLastPoint() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        int size = maskPolylineData.getSize();
        maskPolylineData.remove(size - 1);

        for (Node node : maskPane.getChildren()) {
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().equalsIgnoreCase("PolylinePoint" + size)) {
                maskPane.getChildren().remove(node);
                break;
            }
        }
    }

    public void clearMaskPolylineData() {
        if (maskPolylineData != null) {
            maskPolylineData.clear();
            maskPolylineData = null;
        }
    }

    /*
        polygon
     */
    public boolean showMaskPolygon() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskPolygon)) {
            maskPane.getChildren().addAll(maskPolygon);
        }
        maskPolygon.setOpacity(1);
        maskPolygon.setVisible(true);
        return drawMaskPolygon();
    }

    public void setMaskPolygonDefaultValues() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskPolygonData = new DoublePolygon();
        maskPolygonData.add(10, 10);
        maskPolygonData.add(w / 2, 10);
        maskPolygonData.add(w / 4, h / 2);
        maskPolygonData.add(w - 30, h / 2);
    }

    public boolean drawMaskPolygon() {
        try {
            if (maskPolygon == null || !maskPolygon.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskPolygonData == null) {
                setMaskPolygonDefaultValues();
            }
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();

            clearPolygonPoints();
            maskPolygon.getPoints().clear();
            maskPolygon.setLayoutX(imageView.getLayoutX());
            maskPolygon.setLayoutY(imageView.getLayoutY());
            Color color = anchorColor();
            Font font = new Font(anchorSize());
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                DoublePoint p = maskPolygonData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolygonPoint(i + 1, p, x, y, color, font);
            }

            setShapeStyle(maskPolygon);

            shapeType = ShapeType.Polygon;

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolygonPoint(int index, DoublePoint p,
            double x, double y, Color color, Font font) {
        try {
            maskPolygon.getPoints().add(x);
            maskPolygon.getPoints().add(y);

            Text text = new Text(index + "");
            text.setFill(color);
            text.setFont(font);
            text.setLayoutX(imageView.getLayoutX() + x);
            text.setLayoutY(imageView.getLayoutY() + y);
            text.setId("PolygonPoint" + index);
            text.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scrollPane.setPannable(false);
                }
            });
            text.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    maskPointDragged = true;
                    scrollPane.setPannable(true);
                    double nx = maskHandlerX(text, event);
                    double ny = maskHandlerY(text, event);
                    if (DoubleShape.changed(nx - p.getX(), ny - p.getY())) {
                        maskPolygonData.getPoints().set(index - 1, new DoublePoint(nx, ny));
                        drawMaskPolygon();
                        maskShapeDataChanged();
                    }
                }
            });
            NodeStyleTools.setTooltip(text, message("Point") + " " + index + "\n" + p.text(2));
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void addMaskPolygonPoint(int index, DoublePoint p, double x, double y) {
        addMaskPolygonPoint(index, p, x, y, anchorColor(), new Font(anchorSize()));
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
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolygon);
        maskPolygon.setVisible(false);
        maskPolygon.getPoints().clear();
        clearPolygonPoints();
    }

    public void removeMaskPolygonLastPoint() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        int size = maskPolygonData.getSize();
        maskPolygonData.remove(size - 1);

        for (Node node : maskPane.getChildren()) {
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().equalsIgnoreCase("PolygonPoint" + size)) {
                maskPane.getChildren().remove(node);
                break;
            }
        }
        maskShapeChanged();
    }

    public void clearMaskPolygonData() {
        if (maskPolygonData != null) {
            maskPolygonData.clear();
            maskPolygonData = null;
        }
    }

    /*
        lines
     */
    public void showMaskLines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        clearMaskLines();
        maskLines = new ArrayList<>();
        maskLinesData = new DoubleLines();
        drawMaskLines();
    }

    public boolean drawMaskLines() {
        shapeType = ShapeType.Lines;
        return true;
    }

    public Line drawMaskLinesLine(DoublePoint lastPonit, DoublePoint thisPoint) {
        if (lastPonit == null || thisPoint == null) {
            return null;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        Line line = makeMaskLinesLine(lastPonit.getX(), lastPonit.getY(), thisPoint.getX(), thisPoint.getY(), xRatio, yRatio);
        if (line != null) {
            maskPane.getChildren().add(line);
            line.setLayoutX(imageView.getLayoutX());
            line.setLayoutY(imageView.getLayoutY());
        }
        return line;
    }

    public Line makeMaskLinesLine(double lastx, double lasty, double thisx, double thisy, double xRatio, double yRatio) {
        if (lastx == thisx && lasty == thisy) {
            return null;
        }
        Line line = new Line(lastx * xRatio, lasty * yRatio, thisx * xRatio, thisy * yRatio);
        return line;
    }

    public void clearMaskLines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        if (maskLines != null) {
            for (List<Line> line : maskLines) {
                maskPane.getChildren().removeAll(line);
            }
            maskLines.clear();
            maskLines = null;
        }
    }

    public void clearMaskLinesData() {
        if (maskLinesData != null) {
            maskLinesData.clear();
            maskLinesData = null;
        }
    }

    /*
        path
     */
    public void setPathDefaultValues() {
        if (imageView == null || maskPane == null) {
            return;
        }
        svgPath = new SVGPath();
        svgPath.setContent("M 10,30\n"
                + "           A 20,20 0,0,1 50,30\n"
                + "           A 20,20 0,0,1 90,30\n"
                + "           Q 90,60 50,90\n"
                + "           Q 10,60 10,30 z");
        pathData = new DoublePath();
    }

    public boolean drawPath() {
        try {
            if (imageView == null || maskPane == null) {
                return false;
            }
            if (svgPath == null) {
                setPathDefaultValues();
            }
            svgPath.setLayoutX(imageView.getLayoutX());
            svgPath.setLayoutY(imageView.getLayoutY());
            setShapeStyle(svgPath);
            if (!maskPane.getChildren().contains(svgPath)) {
                maskPane.getChildren().add(svgPath);
            }
            svgPath.setVisible(false);

            shapeType = ShapeType.Path;

            maskShapeChanged();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearPath() {
        try {
            if (maskPane == null) {
                return;
            }
            if (svgPath != null) {
                maskPane.getChildren().remove(svgPath);
                svgPath.setVisible(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearPathData() {
        if (pathData != null) {
            pathData = null;
        }
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
