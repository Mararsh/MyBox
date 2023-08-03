package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import mara.mybox.data.DoubleArc;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleCubic;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleQuadratic;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
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
    protected DoublePolylines maskPolylinesData;
    protected DoubleQuadratic maskQuadraticData;
    protected DoubleCubic maskCubicData;
    protected DoubleArc maskArcData;
    protected DoublePath pathData;
    protected SVGPath svgPath;
    public boolean maskPointDragged;
    protected Polyline currentPolyline;
    protected List<Polyline> maskPolylines;
    protected DoublePoint lastPoint;

    protected ShapeStyle shapeStyle = null;
    public SimpleBooleanProperty maskShapeChanged = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty maskShapeDataChanged = new SimpleBooleanProperty(false);

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
    @FXML
    protected Arc maskArc;
    @FXML
    protected CubicCurve maskCubic;
    @FXML
    protected QuadCurve maskQuadratic;


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
        drawMaskRulers();
        checkCoordinate();
        setShapeStyle(maskRectangle);
        setShapeStyle(maskCircle);
        setShapeStyle(maskEllipse);
        setShapeStyle(maskLine);
        setMaskPolylineStyle();
        setMaskPolygonStyle();
        setMaskPolyinesStyle();
        setShapeStyle(maskQuadratic);
        setShapeStyle(maskCubic);
        setShapeStyle(maskArc);
        setShapeStyle(svgPath);
    }

    public void setShapeStyle(Shape shape) {
        if (shape == null || !shape.isVisible()) {
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

        shape.setFill(Color.TRANSPARENT);

        shape.getStrokeDashArray().clear();
        shape.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);

    }

    public void setMaskAnchorsStyle() {
        if (shapeStyle == null) {
            setMaskAnchorsStyle(anchorColor(), anchorSize());
        } else {
            setMaskAnchorsStyle(shapeStyle.getAnchorColor(), shapeStyle.getAnchorSize());
        }
    }

    public void setMaskAnchorsStyle(Color anchorColor, float anchorSize) {
        if (maskPane == null) {
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
                if (node.getId().startsWith("MaskPoint")) {
                    Text text = (Text) node;
                    text.setFill(anchorColor);
                    text.setFont(font);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setTextStyle(Text text) {
        if (text == null) {
            return;
        }
        if (shapeStyle == null) {
            text.setFill(anchorColor());
            text.setFont(new Font(anchorSize()));

        } else {
            text.setFill(shapeStyle.getAnchorColor());
            text.setFont(new Font(shapeStyle.getAnchorSize()));
        }
    }

    public void clearMask() {
        clearMaskShapes();
        clearMaskShapesData();
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
            clearMaskPolylines();
            clearMaskQuadratic();
            clearMaskCubic();
            clearMaskArc();
            clearPath();
            shapeStyle = null;
            maskPointDragged = false;
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
            clearMaskPolylinesData();
            clearMaskQuadraticData();
            clearMaskCubicData();
            clearMaskArcData();
            clearPathData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Any mask operations when pane size is changed
    public boolean redrawMaskShapes() {
        try {
            drawMaskRulers();
            checkCoordinate();
            if (drawMaskRectangle()) {
                return true;
            }
            if (drawMaskCircle()) {
                return true;
            }
            if (drawMaskEllipse()) {
                return true;
            }
            if (drawMaskLine()) {
                return true;
            }
            if (drawMaskPolygon()) {
                return true;
            }
            if (drawMaskPolyline()) {
                return true;
            }
            if (drawMaskPolylines()) {
                return true;
            }
            if (drawMaskQuadratic()) {
                return true;
            }
            if (drawMaskCubic()) {
                return true;
            }
            if (drawMaskArc()) {
                return true;
            }
            if (drawPath()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void maskShapeChanged() {
        if (!isSettingValues && maskShapeChanged != null) {
            maskShapeChanged.set(!maskShapeChanged.get());
        }
    }

    public void maskShapeDataChanged() {
        if (!isSettingValues && maskShapeDataChanged != null) {
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
        double w = imageWidth();
        double h = imageHeight();
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
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double x1 = maskRectangleData.getSmallX() * xRatio;
            double y1 = maskRectangleData.getSmallY() * yRatio;
            double x2 = maskRectangleData.getBigX() * xRatio;
            double y2 = maskRectangleData.getBigY() * yRatio;
            maskRectangle.setLayoutX(layoutX);
            maskRectangle.setLayoutY(layoutY);
            maskRectangle.setX(x1);
            maskRectangle.setY(y1);
            maskRectangle.setWidth(x2 - x1 + 1);
            maskRectangle.setHeight(y2 - y1 + 1);

            double anchorHW = anchorSize() * xRatio / 2;

            topLeftHandler.setLayoutX(layoutX);
            topLeftHandler.setLayoutY(layoutY);
            topLeftHandler.setX(x1 - anchorHW);
            topLeftHandler.setY(y1 - anchorHW);

            topCenterHandler.setLayoutX(layoutX);
            topCenterHandler.setLayoutY(layoutY);
            topCenterHandler.setX(x1 + maskRectangle.getWidth() / 2 - anchorHW);
            topCenterHandler.setY(y1 - anchorHW);

            topRightHandler.setLayoutX(layoutX);
            topRightHandler.setLayoutY(layoutY);
            topRightHandler.setX(x1 + maskRectangle.getWidth() - anchorHW);
            topRightHandler.setY(y1 - anchorHW);

            bottomLeftHandler.setLayoutX(layoutX);
            bottomLeftHandler.setLayoutY(layoutY);
            bottomLeftHandler.setX(x1 - anchorHW);
            bottomLeftHandler.setY(y1 + maskRectangle.getHeight() - anchorHW);

            bottomCenterHandler.setLayoutX(layoutX);
            bottomCenterHandler.setLayoutY(layoutY);
            bottomCenterHandler.setX(x1 + maskRectangle.getWidth() / 2 - anchorHW);
            bottomCenterHandler.setY(y1 + maskRectangle.getHeight() - anchorHW);

            bottomRightHandler.setLayoutX(layoutX);
            bottomRightHandler.setLayoutY(layoutY);
            bottomRightHandler.setX(x1 + maskRectangle.getWidth() - anchorHW);
            bottomRightHandler.setY(y1 + maskRectangle.getHeight() - anchorHW);

            leftCenterHandler.setLayoutX(layoutX);
            leftCenterHandler.setLayoutY(layoutY);
            leftCenterHandler.setX(x1 - anchorHW);
            leftCenterHandler.setY(y1 + maskRectangle.getHeight() / 2 - anchorHW);

            rightCenterHandler.setLayoutX(layoutX);
            rightCenterHandler.setLayoutY(layoutY);
            rightCenterHandler.setX(x1 + maskRectangle.getWidth() - anchorHW);
            rightCenterHandler.setY(y1 + maskRectangle.getHeight() / 2 - anchorHW);

            setShapeStyle(maskRectangle);
            setMaskAnchorsStyle();

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
        double w = imageWidth();
        double h = imageHeight();
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
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double r = maskCircleData.getRadius() * xRatio;
            double x = maskCircleData.getCenterX() * xRatio;
            double y = maskCircleData.getCenterY() * yRatio;
            maskCircle.setLayoutX(layoutX);
            maskCircle.setLayoutY(layoutY);
            maskCircle.setCenterX(x);
            maskCircle.setCenterY(y);
            maskCircle.setRadius(r);

            double anchorHW = anchorSize() * xRatio / 2;
            topCenterHandler.setLayoutX(layoutX);
            topCenterHandler.setLayoutY(layoutY);
            topCenterHandler.setX(x - anchorHW);
            topCenterHandler.setY(y - r - anchorHW);

            bottomCenterHandler.setLayoutX(layoutX);
            bottomCenterHandler.setLayoutY(layoutY);
            bottomCenterHandler.setX(x - anchorHW);
            bottomCenterHandler.setY(y + r - anchorHW);

            leftCenterHandler.setLayoutX(layoutX);
            leftCenterHandler.setLayoutY(layoutY);
            leftCenterHandler.setX(x - r - anchorHW);
            leftCenterHandler.setY(y - anchorHW);

            rightCenterHandler.setLayoutX(layoutX);
            rightCenterHandler.setLayoutY(layoutY);
            rightCenterHandler.setX(x + r - anchorHW);
            rightCenterHandler.setY(y - anchorHW);

            setShapeStyle(maskCircle);
            setMaskAnchorsStyle();

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
        double w = imageWidth();
        double h = imageHeight();
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
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double rx = maskEllipseData.getRadiusX() * xRatio;
            double ry = maskEllipseData.getRadiusY() * yRatio;
            double cx = maskEllipseData.getCenterX() * xRatio;
            double cy = maskEllipseData.getCenterY() * yRatio;
            maskEllipse.setLayoutX(layoutX);
            maskEllipse.setLayoutY(layoutY);
            maskEllipse.setCenterX(cx);
            maskEllipse.setCenterY(cy);
            maskEllipse.setRadiusX(rx);
            maskEllipse.setRadiusY(ry);

            double anchorHW = anchorSize() * xRatio / 2;
            topCenterHandler.setLayoutX(layoutX);
            topCenterHandler.setLayoutY(layoutY);
            topCenterHandler.setX(cx - anchorHW);
            topCenterHandler.setY(cy - ry - anchorHW);

            bottomCenterHandler.setLayoutX(layoutX);
            bottomCenterHandler.setLayoutY(layoutY);
            bottomCenterHandler.setX(cx - anchorHW);
            bottomCenterHandler.setY(cy + ry - anchorHW);

            leftCenterHandler.setLayoutX(layoutX);
            leftCenterHandler.setLayoutY(layoutY);
            leftCenterHandler.setX(cx - rx - anchorHW);
            leftCenterHandler.setY(cy - anchorHW);

            rightCenterHandler.setLayoutX(layoutX);
            rightCenterHandler.setLayoutY(layoutY);
            rightCenterHandler.setX(cx + rx - anchorHW);
            rightCenterHandler.setY(cy - anchorHW);

            setShapeStyle(maskEllipse);
            setMaskAnchorsStyle();

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
        double w = imageWidth();
        double h = imageHeight();
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
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            double startX = maskLineData.getStartX() * xRatio;
            double startY = maskLineData.getStartY() * yRatio;
            double endX = maskLineData.getEndX() * xRatio;
            double endY = maskLineData.getEndY() * yRatio;

            maskLine.setLayoutX(layoutX);
            maskLine.setLayoutY(layoutY);
            maskLine.setStartX(startX);
            maskLine.setStartY(startY);
            maskLine.setEndX(endX);
            maskLine.setEndY(endY);
            maskLine.setVisible(true);

            double anchorHW = anchorSize() * xRatio / 2;
            topLeftHandler.setLayoutX(layoutX);
            topLeftHandler.setLayoutY(layoutY);
            topLeftHandler.setX(startX - anchorHW);
            topLeftHandler.setY(startY - anchorHW);

            bottomRightHandler.setLayoutX(layoutX);
            bottomRightHandler.setLayoutY(layoutY);
            bottomRightHandler.setX(endX - anchorHW);
            bottomRightHandler.setY(endY - anchorHW);

            setShapeStyle(maskLine);
            setMaskAnchorsStyle();

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
        point
        index: 0-based
     */
    public void addMaskPoint(int index, DoublePoint p, double x, double y) {
        try {
            Text text = new Text((index + 1) + "");
            text.setLayoutX(imageView.getLayoutX());
            text.setLayoutY(imageView.getLayoutY());
            text.setX(x);
            text.setY(y);
            text.setId("MaskPoint" + index);
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
                    double nx = maskEventX(event);
                    double ny = maskEventY(event);
                    if (DoubleShape.changed(nx - p.getX(), ny - p.getY())) {
                        maskPointChanged(index, new DoublePoint(nx, ny));
                    }
                }
            });
            text.hoverProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        popNodeMenu(text, maskPointMenu(text, index, p));
                    }
                }
            });
            setTextStyle(text);
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected List<MenuItem> maskPointMenu(Text text, int index, DoublePoint p) {
        try {
            if (text == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            String title = message("Point") + " " + (index + 1);
            menu = new MenuItem(title);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            menu = new MenuItem(StringTools.menuPrefix(p.text(2)));
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                PointInputController inputController = PointInputController.open(this, title, p, 3);
                inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        maskPointChanged(index, inputController.picked);
                        inputController.close();
                    }
                });
            });
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteMaskPoint(index);
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void maskPointChanged(int index, DoublePoint newValue) {
        if (maskPolyline != null && maskPolyline.isVisible() && maskPolylineData != null) {
            maskPolylineData.set(index, newValue);
            drawMaskPolyline();
            maskShapeDataChanged();

        } else if (maskPolygon != null && maskPolygon.isVisible() && maskPolygonData != null) {
            maskPolygonData.set(index, newValue);
            drawMaskPolygon();
            maskShapeDataChanged();
        }
    }

    public void deleteMaskPoint(int index) {
        if (maskPolyline != null && maskPolyline.isVisible() && maskPolylineData != null) {
            maskPolylineData.remove(index);
            drawMaskPolyline();
            maskShapeDataChanged();

        } else if (maskPolygon != null && maskPolygon.isVisible() && maskPolygonData != null) {
            maskPolygonData.remove(index);
            drawMaskPolygon();
            maskShapeDataChanged();
        }
    }

    public void clearMaskPoints() {
        if (maskPane == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().startsWith("MaskPoint")) {
                maskPane.getChildren().remove(node);
            }
        }
    }

    public void setMaskPointsStyle() {
        if (maskPane == null) {
            return;
        }
        for (Node node : maskPane.getChildren()) {
            if (node == null || !(node instanceof Text) || node.getId() == null) {
                continue;
            }
            if (node.getId().startsWith("MaskPoint")) {
                setTextStyle((Text) node);
            }
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
        double w = imageWidth();
        double h = imageHeight();
        maskPolylineData = new DoublePolyline();
        maskPolylineData.add(30, 50);
        maskPolylineData.add(w / 2, 10);
        maskPolylineData.add(w / 4, h / 3);
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
            double layoutX = imageView.getLayoutX();
            double layoutY = imageView.getLayoutY();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();

            clearMaskPoints();
            maskPolyline.getPoints().clear();
            maskPolyline.setLayoutX(layoutX);
            maskPolyline.setLayoutY(layoutY);
            for (int i = 0; i < maskPolylineData.getSize(); ++i) {
                DoublePoint p = maskPolylineData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolylinePoint(i, p, x, y);
            }

            setMaskPolylineStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolylinePoint(int index, DoublePoint p, double x, double y) {
        try {
            maskPolyline.getPoints().add(x);
            maskPolyline.getPoints().add(y);

            addMaskPoint(index, p, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskPolyline() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolyline);
        maskPolyline.setVisible(false);
        maskPolyline.getPoints().clear();

        clearMaskPoints();
    }

    public void clearMaskPolylineData() {
        if (maskPolylineData != null) {
            maskPolylineData.clear();
            maskPolylineData = null;
        }
    }

    public void setMaskPolylineStyle() {
        setShapeStyle(maskPolyline);
        setMaskPointsStyle();
    }

    /*
        polylines
     */
    public void showMaskPolylines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        clearMaskPolylines();
        maskPolylines = new ArrayList<>();
        drawMaskPolylines();
    }

    public boolean drawMaskPolylines() {
        try {
            if (maskPolylines == null || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskPolylinesData == null) {
                maskPolylinesData = new DoublePolylines();
            }
            clearMaskPolylines();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            for (int i = 0; i < maskPolylinesData.getLinesSize(); i++) {
                List<DoublePoint> points = maskPolylinesData.getLines().get(i);
                if (points.isEmpty()) {
                    continue;
                }
                Polyline pline = new Polyline();
                for (DoublePoint p : points) {
                    pline.getPoints().add(p.getX() * xRatio);
                    pline.getPoints().add(p.getY() * yRatio);
                }
                maskPolylines.add(pline);
                maskPane.getChildren().add(pline);
                pline.setLayoutX(imageView.getLayoutX());
                pline.setLayoutY(imageView.getLayoutY());
                setShapeStyle(pline);
                pline.hoverProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            popNodeMenu(pline, lineMenu(pline, points));
                        }
                    }
                });
            }

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected List<MenuItem> lineMenu(Polyline line, List<DoublePoint> points) {
        try {
            if (line == null) {
                return null;
            }
            int index = maskPolylines.indexOf(line);
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            String title = message("Line") + " " + (index + 1);
            menu = new MenuItem(title);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                LineInputController inputController = LineInputController.open(this, title, points, 3);
                inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        List<DoublePoint> line = inputController.picked;
                        if (line == null || line.isEmpty()) {
                            popError(message("InvalidValue"));
                            return;
                        }
                        inputController.close();
                        maskPolylinesData.setLine(index, line);
                        maskShapeDataChanged();
                    }
                });
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void setMaskPolyinesStyle() {
        if (maskPolylines != null) {
            for (Polyline line : maskPolylines) {
                setShapeStyle(line);
            }
        }
    }

    public boolean makeCurrentLine(DoublePoint p) {
        if (!DoubleShape.changed(lastPoint, p)) {
            return false;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        if (currentPolyline == null) {
            currentPolyline = new Polyline();
            currentPolyline.setStroke(Color.RED);
            currentPolyline.setStrokeWidth((shapeStyle != null ? shapeStyle.getStrokeWidth() : strokeWidth()) * xRatio);
            currentPolyline.getStrokeDashArray().clear();
            maskPane.getChildren().add(currentPolyline);
            currentPolyline.setLayoutX(imageView.getLayoutX());
            currentPolyline.setLayoutY(imageView.getLayoutY());
        }
        currentPolyline.getPoints().add(p.getX() * xRatio);
        currentPolyline.getPoints().add(p.getY() * yRatio);
        return true;
    }

    public void addMaskLinesData() {
        if (maskPolylines == null || maskPolylinesData == null
                || currentPolyline == null) {
            return;
        }
        List<DoublePoint> newLine = new ArrayList<>();
        double xRatio = imageXRatio();
        double yRatio = imageYRatio();
        List<Double> values = currentPolyline.getPoints();
        for (int i = 0; i < values.size(); i++) {
            newLine.add(new DoublePoint(values.get(i) * xRatio, values.get(++i) * yRatio));
        }
        maskPolylinesData.addLine(newLine);
        maskShapeDataChanged();
    }

    public void hideMaskPolylines() {
        if (maskPolylines != null) {
            for (Polyline line : maskPolylines) {
                line.setOpacity(0);
            }
        }
        if (currentPolyline != null) {
            maskPane.getChildren().remove(currentPolyline);
            currentPolyline = null;
        }
        lastPoint = null;
    }

    public void clearMaskPolylines() {
        if (imageView == null || maskPane == null) {
            return;
        }
        if (maskPolylines != null) {
            for (Polyline line : maskPolylines) {
                maskPane.getChildren().remove(line);
            }
            maskPolylines.clear();
        }
        if (currentPolyline != null) {
            maskPane.getChildren().remove(currentPolyline);
            currentPolyline = null;
        }
        lastPoint = null;
    }

    public void clearMaskPolylinesData() {
        if (maskPolylinesData != null) {
            maskPolylinesData.clear();
            maskPolylinesData = null;
        }
        maskPolylines = null;
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
        double w = imageWidth();
        double h = imageHeight();
        maskPolygonData = new DoublePolygon();
        maskPolygonData.add(50, 80);
        maskPolygonData.add(w / 2, 10);
        maskPolygonData.add(w / 4, h / 3);
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

            clearMaskPoints();
            maskPolygon.getPoints().clear();
            maskPolygon.setLayoutX(imageView.getLayoutX());
            maskPolygon.setLayoutY(imageView.getLayoutY());
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                DoublePoint p = maskPolygonData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolygonPoint(i, p, x, y);
            }

            setMaskPolygonStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addMaskPolygonPoint(int index, DoublePoint p, double x, double y) {
        try {
            maskPolygon.getPoints().add(x);
            maskPolygon.getPoints().add(y);

            addMaskPoint(index, p, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearMaskPolygon() {
        if (imageView == null || maskPane == null || maskPolygon == null) {
            return;
        }
        maskPane.getChildren().remove(maskPolygon);
        maskPolygon.setVisible(false);
        maskPolygon.getPoints().clear();
        clearMaskPoints();
    }

    public void clearMaskPolygonData() {
        if (maskPolygonData != null) {
            maskPolygonData.clear();
            maskPolygonData = null;
        }
    }

    public void setMaskPolygonStyle() {
        setShapeStyle(maskPolygon);
        setMaskPointsStyle();
    }

    /*
        quadratic curve
     */
    public boolean showMaskQuadratic() {
        if (imageView == null || maskPane == null || maskQuadratic == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskQuadratic)) {
            maskPane.getChildren().addAll(maskQuadratic,
                    topLeftHandler, bottomRightHandler);
        }
        maskQuadratic.setOpacity(1);
        maskQuadratic.setVisible(true);
        return drawMaskQuadratic();
    }

    public void setMaskQuadraticDefaultValues() {
        if (imageView == null || maskPane == null || maskQuadratic == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();

    }

    public boolean drawMaskQuadratic() {
        try {
            if (maskQuadratic == null || !maskQuadratic.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskQuadraticData == null) {
                setMaskQuadraticDefaultValues();
            }
            float anchorHW = anchorSize() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
//            double startX = imageView.getLayoutX() + maskLineData.getStartX() * xRatio;
//            double startY = imageView.getLayoutY() + maskLineData.getStartY() * yRatio;
//            double endX = imageView.getLayoutX() + maskLineData.getEndX() * xRatio;
//            double endY = imageView.getLayoutY() + maskLineData.getEndY() * yRatio;
//
//            maskLine.setStartX(startX);
//            maskLine.setStartY(startY);
//            maskLine.setEndX(endX);
//            maskLine.setEndY(endY);
//            maskLine.setVisible(true);
//
//            topLeftHandler.setLayoutX(startX - anchorHW);
//            topLeftHandler.setLayoutY(startY - anchorHW);
//            bottomRightHandler.setLayoutX(endX - anchorHW);
//            bottomRightHandler.setLayoutY(endY - anchorHW);

            setShapeStyle(maskQuadratic);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskQuadratic() {
        if (imageView == null || maskPane == null || maskQuadratic == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskQuadratic,
                topLeftHandler, bottomRightHandler);
        maskQuadratic.setVisible(false);

    }

    public void clearMaskQuadraticData() {
        if (maskQuadraticData != null) {
            maskQuadraticData = null;
        }
    }

    /*
        cubic curve
     */
    public boolean showMaskCubic() {
        if (imageView == null || maskPane == null || maskCubic == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskCubic)) {
            maskPane.getChildren().addAll(maskCubic,
                    topLeftHandler, bottomRightHandler);
        }
        maskCubic.setOpacity(1);
        maskCubic.setVisible(true);
        return drawMaskCubic();
    }

    public void setMaskCubicDefaultValues() {
        if (imageView == null || maskPane == null || maskCubic == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();

    }

    public boolean drawMaskCubic() {
        try {
            if (maskCubic == null || !maskCubic.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskCubicData == null) {
                setMaskCubicDefaultValues();
            }
            float anchorHW = anchorSize() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
//            double startX = imageView.getLayoutX() + maskLineData.getStartX() * xRatio;
//            double startY = imageView.getLayoutY() + maskLineData.getStartY() * yRatio;
//            double endX = imageView.getLayoutX() + maskLineData.getEndX() * xRatio;
//            double endY = imageView.getLayoutY() + maskLineData.getEndY() * yRatio;
//
//            maskLine.setStartX(startX);
//            maskLine.setStartY(startY);
//            maskLine.setEndX(endX);
//            maskLine.setEndY(endY);
//            maskLine.setVisible(true);
//
//            topLeftHandler.setLayoutX(startX - anchorHW);
//            topLeftHandler.setLayoutY(startY - anchorHW);
//            bottomRightHandler.setLayoutX(endX - anchorHW);
//            bottomRightHandler.setLayoutY(endY - anchorHW);

            setShapeStyle(maskCubic);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskCubic() {
        if (imageView == null || maskPane == null || maskCubic == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskCubic,
                topLeftHandler, bottomRightHandler);
        maskCubic.setVisible(false);

    }

    public void clearMaskCubicData() {
        if (maskCubicData != null) {
            maskCubicData = null;
        }
    }

    /*
        arc
     */
    public boolean showMaskArc() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return false;
        }
        if (!maskPane.getChildren().contains(maskArc)) {
            maskPane.getChildren().addAll(maskArc,
                    topLeftHandler, bottomRightHandler);
        }
        maskArc.setOpacity(1);
        maskArc.setVisible(true);
        return drawMaskCubic();
    }

    public void setMaskArcDefaultValues() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return;
        }
        double w = imageWidth();
        double h = imageHeight();

    }

    public boolean drawMaskArc() {
        try {
            if (maskArc == null || !maskArc.isVisible()
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            if (maskArcData == null) {
                setMaskArcDefaultValues();
            }
            float anchorHW = anchorSize() / 2;
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
//            double startX = imageView.getLayoutX() + maskLineData.getStartX() * xRatio;
//            double startY = imageView.getLayoutY() + maskLineData.getStartY() * yRatio;
//            double endX = imageView.getLayoutX() + maskLineData.getEndX() * xRatio;
//            double endY = imageView.getLayoutY() + maskLineData.getEndY() * yRatio;
//
//            maskLine.setStartX(startX);
//            maskLine.setStartY(startY);
//            maskLine.setEndX(endX);
//            maskLine.setEndY(endY);
//            maskLine.setVisible(true);
//
//            topLeftHandler.setLayoutX(startX - anchorHW);
//            topLeftHandler.setLayoutY(startY - anchorHW);
//            bottomRightHandler.setLayoutX(endX - anchorHW);
//            bottomRightHandler.setLayoutY(endY - anchorHW);

            setShapeStyle(maskArc);
            setMaskAnchorsStyle();

            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void clearMaskArc() {
        if (imageView == null || maskPane == null || maskArc == null) {
            return;
        }
        maskPane.getChildren().removeAll(maskArc,
                topLeftHandler, bottomRightHandler);
        maskArc.setVisible(false);

    }

    public void clearMaskArcData() {
        if (maskArcData != null) {
            maskArcData = null;
        }
    }

    /*
        path
     */
    public void setPathDefaultValues() {
        pathData = new DoublePath("M 10,30\n"
                + "           A 20,20 0,0,1 50,30\n"
                + "           A 20,20 0,0,1 90,30\n"
                + "           Q 90,60 50,90\n"
                + "           Q 10,60 10,30 z");
    }

    public boolean showPath() {
        if (imageView == null || maskPane == null) {
            return false;
        }
        svgPath = new SVGPath();
        maskPane.getChildren().add(svgPath);
        svgPath.setOpacity(1);
        svgPath.setVisible(true);
        return drawPath();
    }

    public boolean drawPath() {
        try {
            if (imageView == null || maskPane == null || svgPath == null) {
                return false;
            }
            if (pathData == null) {
                setPathDefaultValues();
            }
            svgPath.setContent(pathData.getContent());

            svgPath.setLayoutX(imageView.getLayoutX());
            svgPath.setLayoutY(imageView.getLayoutY());
            setShapeStyle(svgPath);

            svgPath.setVisible(true);
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
                svgPath = null;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearPathData() {
        svgPath = null;
        pathData = null;
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
