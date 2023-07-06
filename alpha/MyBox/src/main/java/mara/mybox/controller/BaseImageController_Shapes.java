package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Window;
import static mara.mybox.controller.BaseImageController_ImageView.DefaultAnchorColor;
import static mara.mybox.controller.BaseImageController_ImageView.DefaultStrokeColor;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
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
    protected DoublePolygon maskPolygonData;
    protected DoublePolyline maskPolylineData;
    protected DoublePolyline maskPolylineLineData;
    protected List<Line> maskPolylineLines;
    protected DoubleLines maskPenData;
    protected List<List<Line>> maskPenLines;
    protected final SimpleBooleanProperty rectDrawnNotify;

    @FXML
    protected Rectangle maskRectangleLine, leftCenterHandler, topLeftHandler, topCenterHandler, topRightHandler,
            bottomLeftHandler, bottomCenterHandler, bottomRightHandler, rightCenterHandler,
            polygonP1, polygonP2;
    @FXML
    protected Circle maskCircleLine;
    @FXML
    protected Ellipse maskEllipseLine;
    @FXML
    protected Polygon maskPolygonLine;
    @FXML
    protected Polyline maskPolyline;

    public BaseImageController_Shapes() {
        rectDrawnNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
        drawMaskControls();
    }

    // Any mask operations when pane size is changed
    public void drawMaskControls() {
        try {
            setMaskStyles();

            drawMaskRulerXY();
            checkCoordinate();

            drawMaskRectangleLine();
            drawMaskCircleLine();
            drawMaskEllipseLine();
            drawMaskPolygonLine();
            drawMaskPolyline();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;
            updateLabelsTitle();
            if (imageChanged) {
                resetMaskControls();
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

            setMaskLinesStyle();

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

    public float shapeOpacity() {
        return 1.0f;
    }

    public Color shapeFill() {
        return Color.TRANSPARENT;
    }

    public void setShapeStyle(Shape shape) {
        if (shape == null) {
            return;
        }
        shape.setStroke(strokeColor());
        shape.setStrokeWidth(strokeWidth());
        shape.getStrokeDashArray().clear();
        List<Double> dash = strokeDash();
        if (dash != null) {
            shape.getStrokeDashArray().addAll(dash);
        }
        shape.setOpacity(shapeOpacity());
        shape.setFill(shapeFill());
    }

    public void setMaskLinesStyle() {
        setShapeStyle(maskRectangleLine);
        setShapeStyle(maskCircleLine);
        setShapeStyle(maskEllipseLine);
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

            if (UserConfig.getBoolean("AnchorSolid", true)) {
                topLeftHandler.setFill(anchorColor);
                topCenterHandler.setFill(anchorColor);
                topRightHandler.setFill(anchorColor);
                bottomLeftHandler.setFill(anchorColor);
                bottomCenterHandler.setFill(anchorColor);
                bottomRightHandler.setFill(anchorColor);
                leftCenterHandler.setFill(anchorColor);
                rightCenterHandler.setFill(anchorColor);
            } else {
                topLeftHandler.setFill(null);
                topCenterHandler.setFill(null);
                topRightHandler.setFill(null);
                bottomLeftHandler.setFill(null);
                bottomCenterHandler.setFill(null);
                bottomRightHandler.setFill(null);
                leftCenterHandler.setFill(null);
                rightCenterHandler.setFill(null);
            }

        }

        if (polygonP1 != null) {
            polygonP1.setStroke(anchorColor);
            polygonP2.setStroke(anchorColor);

            polygonP1.setWidth(anchorWidth);
            polygonP2.setWidth(anchorWidth);

            if (UserConfig.getBoolean("AnchorSolid", true)) {
                polygonP1.setFill(anchorColor);
                polygonP2.setFill(anchorColor);
            } else {
                polygonP1.setFill(null);
                polygonP2.setFill(null);
            }
        }

    }

    public void initMaskControls(boolean show) {
        try {
            drawMaskRulerXY();
            initMaskRectangleLine(show);
            initMaskCircleLine(show);
            initMaskEllipseLine(show);
            initMaskPolygonLine(show);
            initMaskPolyline(show);
            initMaskPolylineLine(show);
            initMaskPenlines(show);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void resetMaskControls() {
        try {
            if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
                initMaskRectangleLine(true);
            }
            if (maskCircleLine != null && maskCircleLine.isVisible()) {
                initMaskCircleLine(true);
            }
            if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
                initMaskEllipseLine(true);
            }
            if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
                initMaskPolygonLine(true);
            }
            if (maskPolyline != null && maskPolyline.isVisible()) {
                initMaskPolyline(true);
            }
            initMaskPolylineLine(true);
            initMaskPenlines(true);
            drawMaskRulerXY();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initMaskRectangleLine(boolean show) {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        maskRectangleLine.setOpacity(1);
        setMaskRectangleLineVisible(show);
        if (show && imageView.getImage() != null) {
            setDafultMaskRectangleValues();
            drawMaskRectangleLine();
        }
    }

    public void setMaskRectangleLineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
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

    }

    public void setDafultMaskRectangleValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }

        double w = getImageWidth();
        double h = getImageHeight();
        maskRectangleData = new DoubleRectangle(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public void countMaskRectangleData() {
        if (maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)
                || maskRectangleData == null
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        double xRatio = getImageWidth() / imageView.getBoundsInParent().getWidth();
        double yRatio = getImageHeight() / imageView.getBoundsInParent().getHeight();

        double smallX = maskRectangleLine.getLayoutX() - imageView.getLayoutX();
        smallX = smallX * xRatio;
        if (smallX >= getImageWidth() - 1) {
            smallX = getImageWidth() - 2;
        }
        maskRectangleData.setSmallX(smallX);

        double smallY = maskRectangleLine.getLayoutY() - imageView.getLayoutY();
        smallY = smallY * yRatio;
        if (smallY >= getImageHeight() - 1) {
            smallY = getImageHeight() - 2;
        }
        maskRectangleData.setSmallY(smallY);

        double bigX = maskRectangleLine.getLayoutX() + maskRectangleLine.getWidth() - imageView.getLayoutX();
        bigX = bigX * xRatio;
        if (bigX <= 0) {
            bigX = 1;
        }
        maskRectangleData.setBigX(bigX);

        double bigY = maskRectangleLine.getLayoutY() + maskRectangleLine.getHeight() - imageView.getLayoutY();
        bigY = bigY * yRatio;
        if (bigY <= 0) {
            bigY = 1;
        }
        maskRectangleData.setBigY(bigY);

    }

    public boolean drawMaskRectangleLine() {
        try {
            if (maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)
                    || maskRectangleData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }
            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
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

    public void initMaskCircleLine(boolean show) {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        maskCircleLine.setOpacity(1);
        setMaskCircleLineVisible(show);
        if (show && imageView.getImage() != null) {
            setDafultMaskCircleValues();
            drawMaskCircleLine();
        }
    }

    public void setMaskCircleLineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
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
        }
    }

    public void setDafultMaskCircleValues() {
        if (imageView == null || maskPane == null || maskCircleLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskCircleData = new DoubleCircle(w / 2, h / 2, Math.min(w, h) / 4);
    }

    public boolean drawMaskCircleLine() {
        try {
            if (maskCircleLine == null || !maskCircleLine.isVisible()
                    || maskCircleData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }

            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
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

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public void initMaskEllipseLine(boolean show) {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        maskEllipseLine.setOpacity(1);
        setMaskEllipseLineVisible(show);
        if (show && imageView.getImage() != null) {
            setDafultMaskEllipseValues();
            drawMaskEllipseLine();
        }
    }

    public void setMaskEllipseLineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
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
        }
    }

    public void setDafultMaskEllipseValues() {
        if (imageView == null || maskPane == null || maskEllipseLine == null) {
            return;
        }
        double w = getImageWidth();
        double h = getImageHeight();
        maskEllipseData = new DoubleEllipse(w / 4, h / 4, w * 3 / 4, h * 3 / 4);
    }

    public boolean drawMaskEllipseLine() {
        try {
            if (maskEllipseLine == null || !maskEllipseLine.isVisible()
                    || maskEllipseData == null
                    || imageView == null || imageView.getImage() == null) {
                return false;
            }

            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
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

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public void initMaskPolygonLine(boolean show) {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonLine.setOpacity(1);
        setMaskPolygonLineVisible(show);
        if (show && imageView.getImage() != null) {
            setDafultMaskPolygonValues();
            drawMaskPolygonLine();
        }
    }

    public void setMaskPolygonLineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        if (show && imageView.getImage() != null) {
            maskPolygonLine.setVisible(true);
            if (!maskPane.getChildren().contains(maskPolygonLine)) {
                maskPane.getChildren().addAll(maskPolygonLine, polygonP1, polygonP2);
            }
            setMaskStyles();

        } else {
            maskPane.getChildren().removeAll(maskPolygonLine, polygonP1, polygonP2);
            maskPolygonLine.setVisible(false);
        }
    }

    public void setDafultMaskPolygonValues() {
        if (imageView == null || maskPane == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonData = new DoublePolygon();
        maskPolygonLine.getPoints().clear();

    }

    public boolean drawMaskPolygonLine() {
        try {
            if (maskPolygonLine == null || !maskPolygonLine.isVisible()
                    || maskPolygonData == null || imageView == null || imageView.getImage() == null) {
                return false;
            }

            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();

            List<Double> d = new ArrayList<>();
            for (int i = 0; i < maskPolygonData.getSize(); ++i) {
                d.add(maskPolygonData.get(i).getX() * xRatio);
                d.add(maskPolygonData.get(i).getY() * yRatio);
            }
            maskPolygonLine.getPoints().setAll(d);

            if (d.isEmpty()) {
                maskPolygonLine.setOpacity(0);
                polygonP1.setOpacity(0);
                polygonP2.setOpacity(0);

            } else if (maskPolygonData.getSize() > 2) {

                maskPolygonLine.setOpacity(1);
                polygonP1.setOpacity(0);
                polygonP2.setOpacity(0);

                maskPolygonLine.setLayoutX(imageView.getLayoutX());
                maskPolygonLine.setLayoutY(imageView.getLayoutY());

            } else if (maskPolygonData.getSize() == 2) {

                maskPolygonLine.setOpacity(0);
                polygonP1.setOpacity(1);
                polygonP2.setOpacity(1);

                DoublePoint p1 = maskPolygonData.get(0);
                polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
                polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);

                DoublePoint p2 = maskPolygonData.get(1);
                polygonP2.setLayoutX(imageView.getLayoutX() + p2.getX() * xRatio - anchorHW);
                polygonP2.setLayoutY(imageView.getLayoutY() + p2.getY() * yRatio - anchorHW);

            } else if (maskPolygonData.getSize() == 1) {

                maskPolygonLine.setOpacity(0);
                polygonP1.setOpacity(1);
                polygonP2.setOpacity(0);

                DoublePoint p1 = maskPolygonData.get(0);
                polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
                polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
            }

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public void initMaskPolyline(boolean show) {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPolyline.setOpacity(1);
        setMaskPolylineVisible(show);
        if (show && imageView.getImage() != null) {
            setDafultMaskPolylineValues();
            drawMaskPolyline();
        }
    }

    public void setMaskPolylineVisible(boolean show) {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        if (show && imageView.getImage() != null) {
            maskPolyline.setVisible(true);
            if (!maskPane.getChildren().contains(maskPolyline)) {
                maskPane.getChildren().addAll(maskPolyline, polygonP1);
            }
            setMaskStyles();

        } else {
            maskPane.getChildren().removeAll(maskPolyline, polygonP1, polygonP2);
            maskPolyline.setVisible(false);
        }
    }

    public void setDafultMaskPolylineValues() {
        if (imageView == null || maskPane == null || maskPolyline == null) {
            return;
        }
        maskPolylineData = new DoublePolyline();
        maskPolyline.getPoints().clear();

    }

    public boolean drawMaskPolyline() {
        try {
            if (maskPolyline == null || imageView == null
                    || imageView.getImage() == null || !maskPolyline.isVisible()) {
                return false;
            }
            maskPolyline.setOpacity(0);
            polygonP1.setOpacity(0);

            maskPolyline.getPoints().clear();

            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
            double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();

            List<Double> d = new ArrayList<>();
            for (int i = 0; i < maskPolylineData.getSize(); ++i) {
                d.add(maskPolylineData.get(i).getX() * xRatio);
                d.add(maskPolylineData.get(i).getY() * yRatio);
            }
            maskPolyline.getPoints().addAll(d);

            if (d.isEmpty()) {

            } else if (maskPolylineData.getSize() > 1) {

                maskPolyline.setLayoutX(imageView.getLayoutX());
                maskPolyline.setLayoutY(imageView.getLayoutY());

                if (maskPolylineData.getSize() == 2) {  // Have to add one more to make it displayed.
                    maskPolyline.getPoints().addAll(maskPolylineData.get(1).getX() * xRatio + 0.5,
                            maskPolylineData.get(1).getY() * yRatio + 0.5);
                }
                maskPolyline.setOpacity(1);

            } else if (maskPolylineData.getSize() == 1) {

                polygonP1.setOpacity(1);

                DoublePoint p1 = maskPolylineData.get(0);
                polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
                polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);

            }

//            updateLabelTitle();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public void initMaskPolylineLine(boolean show) {
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
            if (polygonP1 != null) {
                polygonP1.setOpacity(0);
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
        polygonP1.setOpacity(0);
        int size = maskPolylineLineData.getSize();
        if (size == 0) {
            return true;
        }
        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (size == 1) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = maskPolylineLineData.get(0);
            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
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

    public void initMaskPenlines(boolean show) {
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
        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (size == 1) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = maskPenData.getPoint(0);
            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
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
        if (polygonP1 != null) {
            polygonP1.setOpacity(0);
        }
    }

    public Line drawMaskPenLine(double strokeWidth, Color strokeColor, boolean dotted, float opacity,
            DoublePoint lastPonit, DoublePoint thisPoint) {
        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        double drawStrokeWidth = strokeWidth * xRatio;
        if (lastPonit == null) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = thisPoint;
            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
            return null;
        } else if (thisPoint != null) {
            Line line = makeMaskPenLine(strokeWidth, strokeColor, dotted, opacity, drawStrokeWidth,
                    lastPonit.getX(), lastPonit.getY(), thisPoint.getX(), thisPoint.getY(), xRatio, yRatio);
            if (line != null) {
                polygonP1.setOpacity(0);
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
        polygonP1.setOpacity(0);
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
        double xRatio = imageView.getBoundsInParent().getWidth() / getImageWidth();
        double yRatio = imageView.getBoundsInParent().getHeight() / getImageHeight();
        if (size == 1) {
            polygonP1.setOpacity(1);
            DoublePoint p1 = maskPenData.getPoint(0);
            int anchorHW = UserConfig.getInt("AnchorWidth", 10) / 2;
            polygonP1.setLayoutX(imageView.getLayoutX() + p1.getX() * xRatio - anchorHW);
            polygonP1.setLayoutY(imageView.getLayoutY() + p1.getY() * yRatio - anchorHW);
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
