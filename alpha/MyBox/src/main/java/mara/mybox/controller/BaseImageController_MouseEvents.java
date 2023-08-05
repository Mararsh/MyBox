package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleArc;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleCubic;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleQuadratic;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.fximage.ImageViewTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_MouseEvents extends BaseImageController_Shapes {

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (isPickingColor) {
            pickColor(p, imageView);
            return;

        }

//        if (event.getClickCount() > 1) {  // Notice: Double click always trigger single click at first
//            imageDoubleClicked(event, p);
//        }
        if (event.getClickCount() == 1) {
            imageSingleClicked(event, p);

        }
        maskControlDragged = false;
    }

    @FXML
    public void mousePressed(MouseEvent event) {
        mousePoint(event);
    }

    @FXML
    public void mouseDragged(MouseEvent event) {
        mousePoint(event);
    }

    public void mousePoint(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null
                || isPickingColor || maskControlDragged
                || event.getButton() == MouseButton.SECONDARY
                || maskPolylines == null) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        scrollPane.setPannable(false);
        makeCurrentLine(p);
        lastPoint = p;
    }

    @FXML
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null
                || isPickingColor || maskControlDragged
                || event.getButton() == MouseButton.SECONDARY
                || maskPolylines == null) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        makeCurrentLine(p);
        addMaskLinesData();
        maskPane.getChildren().remove(currentPolyline);
        currentPolyline = null;
        lastPoint = null;
    }

    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (event == null || p == null) {
            return;
        }
        boolean shapeVisible = false;
        if (maskRectangle != null && maskRectangle.isVisible()) {
            if (singleClickedRectangle(event, p)) {
                maskShapeDataChanged();
                return;
            }
            shapeVisible = true;

        } else if (maskCircle != null && maskCircle.isVisible()) {
            if (singleClickedCircle(event, p)) {
                maskShapeDataChanged();
                return;
            }
            shapeVisible = true;

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            if (singleClickedEllipse(event, p)) {
                maskShapeDataChanged();
                return;
            }
            shapeVisible = true;

        } else if (maskLine != null && maskLine.isVisible()) {
            if (singleClickedLine(event, p)) {
                maskShapeDataChanged();
                return;
            }
            shapeVisible = true;

        } else if (maskPolyline != null && maskPolyline.isVisible()) {
            if (!maskControlDragged) {
                if (singleClickedPolyline(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (maskPolygon != null && maskPolygon.isVisible()) {
            if (!maskControlDragged) {
                if (singleClickedPolygon(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (isMaskPolylinesShown()) {
            if (!maskControlDragged) {
                if (singleClickedLines(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (isMaskQuadraticShown()) {
            if (!maskControlDragged) {
                if (singleClickedQuadratic(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (isMaskCubicShown()) {
            if (!maskControlDragged) {
                if (singleClickedCubic(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (isMaskArcShown()) {
            if (!maskControlDragged) {
                if (singleClickedArc(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        }

        if (!shapeVisible && event.getButton() == MouseButton.SECONDARY) {
            popImageMenu(event.getScreenX(), event.getScreenY());
        }
    }

    protected boolean singleClickedRectangle(MouseEvent event, DoublePoint p) {
        if (p == null || maskRectangle == null || !maskRectangle.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleRectangle moved = maskRectangleData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskRectangleData = moved;
                drawMaskRectangle();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedCircle(MouseEvent event, DoublePoint p) {
        if (p == null || maskCircle == null || !maskCircle.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleCircle moved = maskCircleData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskCircleData = moved;
                drawMaskCircle();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedEllipse(MouseEvent event, DoublePoint p) {
        if (p == null || maskEllipse == null || !maskEllipse.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleEllipse moved = maskEllipseData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskEllipseData = moved;
                drawMaskEllipse();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedLine(MouseEvent event, DoublePoint p) {
        if (p == null || maskLine == null || !maskLine.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleLine moved = maskLineData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskLineData = moved;
                drawMaskLine();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedPolyline(MouseEvent event, DoublePoint p) {
        if (p == null || maskPolyline == null || !maskPolyline.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            maskPolylineData.add(p.getX(), p.getY());
            double x = p.getX() * viewXRatio();
            double y = p.getY() * viewYRatio();
            addMaskPolylinePoint(maskPolylineData.getSize(), p, x, y);
            maskShapeChanged();
            return true;

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolylineData.getSize() > 0) {
            DoublePolyline moved = maskPolylineData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskPolylineData = moved;
                drawMaskPolyline();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedPolygon(MouseEvent event, DoublePoint p) {
        if (p == null || maskPolygon == null || !maskPolygon.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            maskPolygonData.add(p.getX(), p.getY());
            double x = p.getX() * viewXRatio();
            double y = p.getY() * viewYRatio();
            addMaskPolygonPoint(maskPolygonData.getSize(), p, x, y);
            maskShapeChanged();
            return true;

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolygonData.getSize() > 0) {
            DoublePolygon moved = maskPolygonData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskPolygonData = moved;
                drawMaskPolygon();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedLines(MouseEvent event, DoublePoint p) {
        if (p == null || maskPolylines == null) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY && maskPolylinesData.getLinesSize() > 0) {
            DoublePolylines moved = maskPolylinesData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskPolylinesData = moved;
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedQuadratic(MouseEvent event, DoublePoint p) {
        if (p == null || maskQuadratic == null || !maskQuadratic.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleQuadratic moved = maskQuadraticData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskQuadraticData = moved;
                drawMaskQuadratic();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedCubic(MouseEvent event, DoublePoint p) {
        if (p == null || maskCubic == null || !maskCubic.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleCubic moved = maskCubicData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskCubicData = moved;
                drawMaskCubic();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedArc(MouseEvent event, DoublePoint p) {
        if (p == null || maskArc == null || !maskArc.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            DoubleArc moved = maskArcData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskArcData = moved;
                drawMaskArc();
                return true;
            }
        }
        return false;
    }

    @FXML
    public void handlerPressed(MouseEvent event) {
        controlPressed(event);
    }

    @FXML
    public void rectangleReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor
                || maskRectangle == null || !maskRectangle.isVisible()
                || !maskPane.getChildren().contains(maskRectangle)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskRectangleData = maskRectangleData.translateRel(offsetX, offsetY);
        drawMaskRectangle();
        maskShapeDataChanged();
    }

    @FXML
    public void circleReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskCircle == null || !maskCircle.isVisible()
                || !maskPane.getChildren().contains(maskCircle)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskCircleData = maskCircleData.translateRel(offsetX, offsetY);
        drawMaskCircle();
        maskShapeDataChanged();
    }

    @FXML
    public void ellipseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskEllipse == null || !maskEllipse.isVisible()
                || !maskPane.getChildren().contains(maskEllipse)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskEllipseData = maskEllipseData.translateRel(offsetX, offsetY);
        drawMaskEllipse();
        maskShapeDataChanged();
    }

    @FXML
    public void lineReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskLine == null || !maskLine.isVisible()
                || !maskPane.getChildren().contains(maskLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }

        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskLineData = maskLineData.translateRel(offsetX, offsetY);
        drawMaskLine();
        maskShapeDataChanged();
    }

    @FXML
    public void polylineReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskPolyline == null || !maskPolyline.isVisible()
                || !maskPane.getChildren().contains(maskPolyline)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskPolylineData = maskPolylineData.translateRel(offsetX, offsetY);
        drawMaskPolyline();
        maskShapeDataChanged();
    }

    @FXML
    public void polygonReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskPolygon == null || !maskPolygon.isVisible()
                || !maskPane.getChildren().contains(maskPolygon)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskPolygonData = maskPolygonData.translateRel(offsetX, offsetY);
        drawMaskPolygon();
        maskShapeDataChanged();
    }

    @FXML
    public void quadraticReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskQuadratic == null || !maskQuadratic.isVisible()
                || !maskPane.getChildren().contains(maskQuadratic)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskQuadraticData = maskQuadraticData.translateRel(offsetX, offsetY);
        drawMaskQuadratic();
        maskShapeDataChanged();
    }

    @FXML
    public void cubicReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskCubic == null || !maskCubic.isVisible()
                || !maskPane.getChildren().contains(maskCubic)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskCubicData = maskCubicData.translateRel(offsetX, offsetY);
        drawMaskCubic();
        maskShapeDataChanged();
    }

    @FXML
    public void arcReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskArc == null || !maskArc.isVisible()
                || !maskPane.getChildren().contains(maskArc)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!DoubleShape.changed(offsetX, offsetY)) {
            return;
        }
        maskArcData = maskArcData.translateRel(offsetX, offsetY);
        drawMaskArc();
        maskShapeDataChanged();
    }

    @FXML
    public void maskHandlerTopLeftReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskHandlerTopLeft == null || !maskHandlerTopLeft.isVisible()
                || !maskPane.getChildren().contains(maskHandlerTopLeft)) {
            return;
        }

        double x = maskEventX(event);
        double y = maskEventY(event);

        if (maskLine != null && maskLine.isVisible()) {

            maskLineData.setStartX(x);
            maskLineData.setStartY(y);
            drawMaskLine();
            maskShapeDataChanged();

        } else if (maskRectangle != null && maskRectangle.isVisible()) {

            if (x < maskRectangleData.getBigX() && y < maskRectangleData.getBigY()) {
                if (x >= imageWidth() - 1) {
                    x = imageWidth() - 2;
                }
                if (y >= imageHeight() - 1) {
                    y = imageHeight() - 2;
                }
                maskRectangleData.setSmallX(x);
                maskRectangleData.setSmallY(y);
                drawMaskRectangle();
                maskShapeDataChanged();
            }
        }
    }

    @FXML
    public void maskHandlerTopCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }
        if (maskRectangle != null && maskRectangle.isVisible()) {
            double y = maskEventY(event);
            if (y < maskRectangleData.getBigY()) {
                if (y >= imageHeight() - 1) {
                    y = imageHeight() - 2;
                }
                maskRectangleData.setSmallY(y);
                drawMaskRectangle();
                maskShapeDataChanged();
            }

        } else if (maskCircle != null && maskCircle.isVisible()) {
            double r = maskHandlerBottomCenter.getY() - event.getY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double ry = maskHandlerBottomCenter.getY() - event.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                maskEllipseData.setRadiusY(ry);
                drawMaskEllipse();
                maskShapeDataChanged();
            }

        } else if (maskArc != null && maskArc.isVisible()) {
            double ry = maskHandlerBottomCenter.getY() - event.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                maskArcData.setRadiusY(ry);
                drawMaskArc();
                maskShapeDataChanged();
            }
        }

    }

    @FXML
    public void maskHandlerTopRightReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double x = maskEventX(event);
        double y = maskEventY(event);

        if (x > maskRectangleData.getSmallX() && y < maskRectangleData.getBigY()) {
            if (x <= 0) {
                x = 1;
            }
            if (y >= imageHeight() - 1) {
                y = imageHeight() - 2;
            }
            maskRectangleData.setBigX(x);
            maskRectangleData.setSmallY(y);
            drawMaskRectangle();
            maskShapeDataChanged();
        }
    }

    @FXML
    public void maskHandlerBottomLeftReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }
        double x = maskEventX(event);
        double y = maskEventY(event);

        if (x < maskRectangleData.getBigX() && y > maskRectangleData.getSmallY()) {
            if (x >= imageWidth() - 1) {
                x = imageWidth() - 2;
            }
            if (y <= 0) {
                y = 1;
            }
            maskRectangleData.setSmallX(x);
            maskRectangleData.setBigY(y);
            drawMaskRectangle();
            maskShapeDataChanged();
        }
    }

    @FXML
    public void maskHandlerBottomCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double y = maskEventY(event);
            if (y > maskRectangleData.getSmallY()) {
                if (y <= 0) {
                    y = 1;
                }
                maskRectangleData.setBigY(y);
                drawMaskRectangle();
                maskShapeDataChanged();
            }

        } else if (maskCircle != null && maskCircle.isVisible()) {
            double r = event.getY() - maskHandlerTopCenter.getY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double ry = event.getY() - maskHandlerTopCenter.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                maskEllipseData.setRadiusY(ry);
                drawMaskEllipse();
                maskShapeDataChanged();
            }

        } else if (maskArc != null && maskArc.isVisible()) {
            double ry = event.getY() - maskHandlerTopCenter.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                maskArcData.setRadiusY(ry);
                drawMaskArc();
                maskShapeDataChanged();
            }

        }

    }

    @FXML
    public void maskHandlerBottomRightReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double x = maskEventX(event);
        double y = maskEventY(event);

        if (maskLine != null && maskLine.isVisible()) {
            maskLineData.setEndX(x);
            maskLineData.setEndY(y);
            drawMaskLine();
            maskShapeDataChanged();

        } else if (x > maskRectangleData.getSmallX() && y > maskRectangleData.getSmallY()) {
            if (x <= 0) {
                x = 1;
            }
            if (y <= 0) {
                y = 1;
            }
            maskRectangleData.setBigX(x);
            maskRectangleData.setBigY(y);
            drawMaskRectangle();
            maskShapeDataChanged();
        }
    }

    @FXML
    public void maskHandlerLeftCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double x = maskEventX(event);
            if (x < maskRectangleData.getBigX()) {
                if (x >= imageWidth() - 1) {
                    x = imageWidth() - 2;
                }
                maskRectangleData.setSmallX(x);
                drawMaskRectangle();
                maskShapeDataChanged();
            }

        } else if (maskCircle != null && maskCircle.isVisible()) {
            double r = maskHandlerRightCenter.getX() - event.getX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double rx = maskHandlerRightCenter.getX() - event.getX();
            if (rx > 0) {
                rx = rx * imageXRatio() / 2;
                maskEllipseData.setRadiusX(rx);
                drawMaskEllipse();
                maskShapeDataChanged();
            }

        } else if (maskArc != null && maskArc.isVisible()) {
            double rx = maskHandlerRightCenter.getX() - event.getX();
            if (rx > 0) {
                rx = rx * imageXRatio() / 2;
                maskArcData.setRadiusX(rx);
                drawMaskArc();
                maskShapeDataChanged();
            }

        }

    }

    @FXML
    public void maskHandlerRightCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double x = maskEventX(event);

            if (x > maskRectangleData.getSmallX()) {
                if (x <= 0) {
                    x = 1;
                }
                maskRectangleData.setBigX(x);
                drawMaskRectangle();
                maskShapeDataChanged();
            }

        } else if (maskCircle != null && maskCircle.isVisible()) {
            double r = event.getX() - maskHandlerLeftCenter.getX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double rx = event.getX() - maskHandlerLeftCenter.getX();
            if (rx > 0) {
                rx = rx * imageXRatio() / 2;
                maskEllipseData.setRadiusX(rx);
                drawMaskEllipse();
                maskShapeDataChanged();
            }

        } else if (maskArc != null && maskArc.isVisible()) {
            double rx = event.getX() - maskHandlerLeftCenter.getX();
            if (rx > 0) {
                rx = rx * imageXRatio() / 2;
                maskArcData.setRadiusX(rx);
                drawMaskArc();
                maskShapeDataChanged();
            }

        }

    }

    /*
        pick color
     */
    protected void checkPickingColor() {
        if (isPickingColor) {
            startPickingColor();
        } else {
            stopPickingColor();
        }
    }

    protected void startPickingColor() {
        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
            paletteController = ColorsPickingController.oneOpen(this);
        }
        imageView.setCursor(Cursor.HAND);
        if (maskRectangle != null) {
            maskRectangle.setCursor(Cursor.HAND);
        }
        if (maskCircle != null) {
            maskCircle.setCursor(Cursor.HAND);
        }
        if (maskEllipse != null) {
            maskEllipse.setCursor(Cursor.HAND);
        }
        if (maskLine != null) {
            maskLine.setCursor(Cursor.HAND);
        }
        if (maskPolygon != null) {
            maskPolygon.setCursor(Cursor.HAND);
        }
        if (maskPolyline != null) {
            maskPolyline.setCursor(Cursor.HAND);
        }
        if (maskQuadratic != null) {
            maskQuadratic.setCursor(Cursor.HAND);
        }
        if (maskCubic != null) {
            maskCubic.setCursor(Cursor.HAND);
        }
        if (maskArc != null) {
            maskArc.setCursor(Cursor.HAND);
        }
        if (svgPath != null) {
            svgPath.setCursor(Cursor.HAND);
        }
    }

    protected void stopPickingColor() {
        if (paletteController != null) {
            paletteController.closeStage();
            paletteController = null;
        }
        imageView.setCursor(Cursor.DEFAULT);
        if (maskRectangle != null) {
            maskRectangle.setCursor(Cursor.MOVE);
        }
        if (maskCircle != null) {
            maskCircle.setCursor(Cursor.MOVE);
        }
        if (maskEllipse != null) {
            maskEllipse.setCursor(Cursor.MOVE);
        }
        if (maskLine != null) {
            maskLine.setCursor(Cursor.MOVE);
        }
        if (maskPolygon != null) {
            maskPolygon.setCursor(Cursor.MOVE);
        }
        if (maskPolyline != null) {
            maskPolyline.setCursor(Cursor.MOVE);
        }
        if (maskQuadratic != null) {
            maskQuadratic.setCursor(Cursor.MOVE);
        }
        if (maskCubic != null) {
            maskCubic.setCursor(Cursor.MOVE);
        }
        if (maskArc != null) {
            maskArc.setCursor(Cursor.MOVE);
        }
        if (svgPath != null) {
            svgPath.setCursor(Cursor.MOVE);
        }
    }

    protected Color pickColor(DoublePoint p, ImageView view) {
        Color color = ImageViewTools.imagePixel(p, view);
        if (color != null) {
            startPickingColor();
            if (paletteController != null && paletteController.getMyStage().isShowing()) {
                paletteController.pickColor(color);
            }
        }
        return color;
    }

}
