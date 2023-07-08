package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import static mara.mybox.controller.BaseImageController_ImageView.DefaultAnchorColor;
import static mara.mybox.controller.BaseImageController_ImageView.DefaultStrokeColor;
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
import mara.mybox.value.UserConfig;

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
    protected boolean isDrawing;

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
                resetMaskShapes();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
        values
     */
    public Color strokeColor() {
        Color strokeColor = Color.web(UserConfig.getString("StrokeColor", DefaultStrokeColor));
        if (strokeColor == null) {
            strokeColor = Color.RED;
        }
        return strokeColor;
    }

    public double strokeWidth() {
        double strokeWidth = UserConfig.getInt("StrokeWidth", 2);
        if (strokeWidth <= 0) {
            strokeWidth = 2.0d;
        }
        return strokeWidth;
    }

    public List<Double> strokeDash() {
        double strokeWidth = strokeWidth();
        List<Double> dash = new ArrayList<>();
        dash.add(strokeWidth);
        dash.add(strokeWidth * 3);
        return dash;
    }

    public StrokeLineCap strokeLineCap() {
        return StrokeLineCap.BUTT;
    }

    public float shapeOpacity() {
        return 1.0f;
    }

    public Color shapeFill() {
        return Color.TRANSPARENT;
    }

    public double viewXRatio() {
        return imageView.getBoundsInParent().getWidth() / getImageWidth();
    }

    public double viewYRatio() {
        return imageView.getBoundsInParent().getHeight() / getImageHeight();
    }

    public double imageXRatio() {
        return getImageWidth() / imageView.getBoundsInParent().getWidth();
    }

    public double imageYRatio() {
        return getImageHeight() / imageView.getBoundsInParent().getHeight();
    }

    public int anchorRadius() {
        return UserConfig.getInt("AnchorWidth", 10) / 2;
    }

    /*
        all shapes
     */
    public void setMaskStyles() {
        try {
            if (isSettingValues) {
                return;
            }
            if (xyText != null) {
                xyText.setFill(strokeColor());
            }

            checkCoordinate();
            drawMaskRulerXY();

            setMaskShapesStyle();

            setAnchorStyle();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setAnchorStyle() {
        try {
            Color anchorColor = Color.web(UserConfig.getString("AnchorColor", DefaultAnchorColor));
            int anchorWidth = UserConfig.getInt("AnchorWidth", 10);
            setMaskAnchorsStyle(anchorColor, anchorWidth);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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

    public void setMaskShapesStyle() {
        setShapeStyle(maskRectangleLine);
        setShapeStyle(maskCircleLine);
        setShapeStyle(maskEllipseLine);
        setShapeStyle(maskLine);
        setShapeStyle(maskPolygonLine);
        setShapeStyle(maskPolyline);

    }

    public void setMaskAnchorsStyle(Color anchorColor, double anchorWidth) {
        if (isSettingValues || maskPane == null) {
            return;
        }
        if (anchorColor == null) {
            anchorColor = Color.BLUE;
        }
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

    }

    public void clearMaskShapes() {
        try {
            drawMaskRulerXY();
            resetMaskRectangle(false);
            resetMaskCircle(false);
            resetMaskEllipse(false);
            resetMaskLine(false);
            resetMaskPolygon(false);
            resetMaskPolyline(false);
            resetMaskPolylineLines(false);
            resetMaskPenlines(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetMaskShapes() {
        try {
            if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
                resetMaskRectangle(true);
            } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
                resetMaskCircle(true);
            } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
                resetMaskEllipse(true);
            } else if (maskLine != null && maskLine.isVisible()) {
                resetMaskLine(true);
            } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
                resetMaskPolygon(true);
            } else if (maskPolyline != null && maskPolyline.isVisible()) {
                resetMaskPolyline(true);
            }
            resetMaskPolylineLines(true);
            resetMaskPenlines(true);
            drawMaskRulerXY();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // Any mask operations when pane size is changed
    public void redrawMaskShapes() {
        try {
            setMaskStyles();

            drawMaskRulerXY();
            checkCoordinate();

            drawMaskRectangle();
            drawMaskCircle();
            drawMaskEllipse();
            drawMaskLine();
            drawMaskPolygon();
            drawMaskPolyline();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        rectangle
     */
    public void resetMaskRectangle(boolean show) {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        setMaskRectangleVisible(show);
        if (show && imageView.getImage() != null) {
            drawMaskRectangle();
        }
    }

    public void setMaskRectangleVisible(boolean show) {
        try {
            if (imageView == null || maskPane == null || maskRectangleLine == null) {
                return;
            }
            maskRectangleLine.setOpacity(1);
            if (show && imageView.getImage() != null) {
                maskRectangleLine.setVisible(true);
                if (!maskPane.getChildren().contains(maskRectangleLine)) {
                    maskPane.getChildren().addAll(maskRectangleLine);
                    if (leftCenterHandler != null && !maskPane.getChildren().contains(leftCenterHandler)) {
                        maskPane.getChildren().addAll(leftCenterHandler, rightCenterHandler,
                                topLeftHandler, topCenterHandler, topRightHandler,
                                bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
                    }
                }
                setMaskStyles();
            } else {
                maskPane.getChildren().removeAll(maskRectangleLine,
                        leftCenterHandler, rightCenterHandler,
                        topLeftHandler, topCenterHandler, topRightHandler,
                        bottomLeftHandler, bottomCenterHandler, bottomRightHandler);
                maskRectangleLine.setVisible(false);
                rectDrawnNotify.set(!rectDrawnNotify.get());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            int anchorHW = anchorRadius();
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

            rectDrawnNotify.set(!rectDrawnNotify.get());
            updateLabelsTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    /*
        circle
     */
    public void resetMaskCircle(boolean show) {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        setMaskCircleVisible(show);
        if (show && imageView.getImage() != null) {
            drawMaskCircle();
        }
    }

    public void setMaskCircleVisible(boolean show) {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        maskCircleLine.setOpacity(1);
        if (show && imageView.getImage() != null) {
            maskCircleLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskCircleLine)) {
                maskPane.getChildren().addAll(maskCircleLine,
                        leftCenterHandler, rightCenterHandler,
                        topCenterHandler, bottomCenterHandler);
            }
            setMaskStyles();
        } else {
            maskPane.getChildren().removeAll(maskCircleLine,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
            maskCircleLine.setVisible(false);
            circleDrawnNotify.set(!circleDrawnNotify.get());
        }
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
            int anchorHW = anchorRadius();
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

            circleDrawnNotify.set(!circleDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        ellipse
     */
    public void resetMaskEllipse(boolean show) {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        setMaskEllipseVisible(show);
        if (show && imageView.getImage() != null) {
            drawMaskEllipse();
        }
    }

    public void setMaskEllipseVisible(boolean show) {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        maskEllipseLine.setOpacity(1);
        if (show && imageView.getImage() != null) {
            maskEllipseLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskEllipseLine)) {
                maskPane.getChildren().addAll(maskEllipseLine,
                        leftCenterHandler, rightCenterHandler,
                        topCenterHandler, bottomCenterHandler);
            }
            setMaskStyles();
        } else {
            maskPane.getChildren().removeAll(maskEllipseLine,
                    leftCenterHandler, rightCenterHandler,
                    topCenterHandler, bottomCenterHandler);
            maskEllipseLine.setVisible(false);
            ellipseDrawnNotify.set(!ellipseDrawnNotify.get());
        }
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
            int anchorHW = anchorRadius();
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

            ellipseDrawnNotify.set(!ellipseDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    /*
        line
     */
    public void resetMaskLine(boolean show) {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        setMaskLineVisible(show);
        if (show && imageView.getImage() != null) {
            drawMaskLine();
        }
    }

    public void setMaskLineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskLine == null) {
            return;
        }
        maskLine.setOpacity(1);
        if (show && imageView.getImage() != null) {
            maskLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskLine)) {
                maskPane.getChildren().addAll(maskLine,
                        topLeftHandler, bottomRightHandler);
            }
            setMaskStyles();
        } else {
            maskPane.getChildren().removeAll(maskLine,
                    topLeftHandler, bottomRightHandler);
            maskLine.setVisible(false);
            lineDrawnNotify.set(!lineDrawnNotify.get());
        }
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
            int anchorHW = anchorRadius();
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

            lineDrawnNotify.set(!lineDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    /*
        polyline
     */
    public void resetMaskPolyline(boolean show) {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        setMaskPolylineVisible(show);
        if (show && imageView.getImage() != null) {
            drawMaskPolyline();
        }
    }

    public void setMaskPolylineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPolyline.setOpacity(1);
        if (show && imageView.getImage() != null) {
            maskPolyline.setVisible(true);
            if (!maskPane.getChildren().contains(maskPolyline)) {
                maskPane.getChildren().addAll(maskPolyline);
            }
            setMaskStyles();

        } else {
            maskPolyline.setVisible(false);
            maskPolyline.getPoints().clear();
            maskPane.getChildren().remove(maskPolyline);
            if (maskPolylineData != null) {
                maskPolylineData.clear();
                maskPolylineData = null;
            }
            clearPolylinePoints();
            polylineDrawnNotify.set(!polylineDrawnNotify.get());
        }
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

            polylineDrawnNotify.set(!polylineDrawnNotify.get());
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

            Text text = new Text(index + "");
            text.setFill(Color.BLUE);
            text.setFont(new Font(10));
            text.setLayoutX(imageView.getLayoutX() + x);
            text.setLayoutY(imageView.getLayoutY() + y);
            text.setId("PolylinePoint" + index);
            NodeStyleTools.setTooltip(text, message("Point") + " " + index + "\n" + p.text(2));
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearPolylinePoints() {
        if (maskPane == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("PolylinePoint")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    /*
        polygon
     */
    public void resetMaskPolygon(boolean show) {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        setMaskPolygonVisible(show);
        if (show && imageView.getImage() != null) {
            drawMaskPolygon();
        }
    }

    public void setMaskPolygonVisible(boolean show) {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonLine.setOpacity(1);
        if (show && imageView.getImage() != null) {
            maskPolygonLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskPolygonLine)) {
                maskPane.getChildren().addAll(maskPolygonLine);
            }
            setMaskStyles();

        } else {
            maskPolygonLine.setVisible(false);
            maskPolygonLine.getPoints().clear();
            maskPane.getChildren().remove(maskPolygonLine);
            if (maskPolygonData != null) {
                maskPolygonData.clear();
                maskPolygonData = null;
            }
            clearPolygonPoints();
            polygonDrawnNotify.set(!polygonDrawnNotify.get());
        }
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
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                DoublePoint p = maskPolygonData.get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskPolygonPoint(i + 1, p, x, y);
            }
            polygonDrawnNotify.set(!polygonDrawnNotify.get());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public void addMaskPolygonPoint(int index, DoublePoint p, double x, double y) {
        try {
            maskPolygonLine.getPoints().add(x);
            maskPolygonLine.getPoints().add(y);

            Text text = new Text(index + "");
            text.setFill(Color.BLUE);
            text.setFont(new Font(10));
            text.setLayoutX(imageView.getLayoutX() + x);
            text.setLayoutY(imageView.getLayoutY() + y);
            text.setId("PolygonPoint" + index);
            NodeStyleTools.setTooltip(text, message("Point") + " " + index + "\n" + p.text(2));
            maskPane.getChildren().add(text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearPolygonPoints() {
        if (maskPane == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("PolygonPoint")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    /*
       polyline lines
     */
    public void resetMaskPolylineLines(boolean show) {
        try {
            if (imageView == null || maskPane == null) {
                return;
            }
            if (maskPolylineLines != null) {
                maskPane.getChildren().removeAll(maskPolylineLines);
                maskPolylineLines.clear();
            }
            if (maskPolylineLineData != null) {
                maskPolylineLineData.clear();
            }
            if (show && imageView.getImage() != null) {
                maskPolylineLines = new ArrayList<>();
                maskPolylineLineData = new DoublePolyline();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // Polyline of Java shows weird results. So I just use lines directly.
    public boolean drawMaskPolylineLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity) {
        maskPane.getChildren().removeAll(maskPolylineLines);
        maskPolylineLines.clear();
        int size = maskPolylineLineData.getSize();
        if (size == 0) {
            return true;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (size == 1) {
            DoublePoint p1 = maskPolylineLineData.get(0);
            int anchorHW = anchorRadius();
        } else if (size > 1) {
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
        }
        return true;
    }

    /*
        pen lines
     */
    public void resetMaskPenlines(boolean show) {
        if (imageView == null || maskPane == null) {
            return;
        }
        clearMaskPenLines();
        if (maskPenData != null) {
            maskPenData.clear();
        }
        if (show) {
            maskPenLines = new ArrayList<>();
            maskPenData = new DoubleLines();
        }

    }

    // strokeWidth is value expected shown on image, so it needs apply ratio for view
    public boolean drawMaskPenLines(double strokeWidth, Color strokeColor, boolean dotted, float opacity) {
        clearMaskPenLines();
        int size = maskPenData.getPointsSize();
        if (size == 0) {
            return true;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (size == 1) {
            DoublePoint p1 = maskPenData.getPoint(0);
            int anchorHW = anchorRadius();
        } else if (size > 1) {
            double lastx, lasty = -1, thisx, thisy;
            for (List<DoublePoint> lineData : maskPenData.getLines()) {
                List<Line> penLine = new ArrayList<>();
                lastx = -1;
                for (DoublePoint p : lineData) {
                    thisx = p.getX();
                    thisy = p.getY();
                    Line line = makeMaskPenLine(strokeWidth, strokeColor, dotted, opacity, drawStrokeWidth,
                            lastx, lasty, thisx, thisy, xRatio, yRatio);
                    if (line != null) {
                        penLine.add(line);
                        maskPane.getChildren().add(line);
                        line.setLayoutX(imageView.getLayoutX());
                        line.setLayoutY(imageView.getLayoutY());
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
                maskPenLines.add(penLine);
            }
        }
        return true;
    }

    public void clearMaskPenLines() {
        if (maskPenLines != null) {
            for (List<Line> penline : maskPenLines) {
                maskPane.getChildren().removeAll(penline);
            }
            maskPenLines.clear();
        }
    }

    public Line drawMaskPenLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity,
            DoublePoint lastPonit, DoublePoint thisPoint) {
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (lastPonit == null) {
            DoublePoint p1 = thisPoint;
            int anchorHW = anchorRadius();
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

    public boolean drawMaskMosaicLines(double strokeWidth) {
        clearMaskPenLines();
        int size = maskPenData.getPointsSize();
        if (size == 0) {
            return true;
        }
        double xRatio = viewXRatio();
        double yRatio = viewYRatio();
        if (size == 1) {
            DoublePoint p1 = maskPenData.getPoint(0);
            int anchorHW = anchorRadius();
        } else if (size > 1) {
            double lastx, lasty = -1, thisx, thisy;
            for (List<DoublePoint> lineData : maskPenData.getLines()) {
                List<Line> penLine = new ArrayList<>();
                lastx = -1;
                for (DoublePoint p : lineData) {
                    thisx = p.getX() * xRatio;
                    thisy = p.getY() * yRatio;
                    if (lastx >= 0) {
                        Line line = new Line(lastx, lasty, thisx, thisy);
                        line.setStrokeWidth(strokeWidth);
                        penLine.add(line);
                        maskPane.getChildren().add(line);
                        line.setLayoutX(imageView.getLayoutX());
                        line.setLayoutY(imageView.getLayoutY());
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
                maskPenLines.add(penLine);
            }

        }
        return true;
    }

    /*
        static
     */
    public static void updateMaskStroke() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController_Mask) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.setMaskStyles();
                } catch (Exception e) {
                }
            }
        }
    }

}
