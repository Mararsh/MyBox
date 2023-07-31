package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
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
                || isPickingColor || event.getButton() == MouseButton.SECONDARY
                || maskLines == null || maskLinesData == null) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        scrollPane.setPannable(false);
        if (lastPoint != null) {
            double offsetX = p.getX() - lastPoint.getX();
            double offsetY = p.getY() - lastPoint.getY();
            if (DoubleShape.changed(offsetX, offsetY)) {
                drawLinePoint(p);
            }
        } else {
            maskLinesData.addPoint(p);
        }
        lastPoint = p;
    }

    @FXML
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null
                || isPickingColor || event.getButton() == MouseButton.SECONDARY
                || maskLines == null || maskLinesData == null) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        if (DoubleShape.changed(lastPoint, p)) {
            maskLinesData.endLine(p);
        } else {
            maskLinesData.endLine(null);
        }
        lastPoint = null;
        maskShapeDataChanged();
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
            if (!maskPointDragged) {
                if (singleClickedPolyline(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (maskPolygon != null && maskPolygon.isVisible()) {
            if (!maskPointDragged) {
                if (singleClickedPolygon(event, p)) {
                    maskShapeDataChanged();
                    return;
                }
            }
            shapeVisible = true;

        } else if (maskLines != null && maskLinesData != null) {
            if (singleClickedLines(event, p)) {
                return;
            }
            shapeVisible = true;

        }

        maskPointDragged = false;
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
        if (p == null || maskLines == null || maskLinesData == null) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY && maskLinesData.getPointsSize() > 0) {
            DoubleLines moved = maskLinesData.translateAbs(p.getX(), p.getY());
            if (moved != null) {
                maskLinesData = moved;
            }
        }
        return true;
    }

    @FXML
    public void handlerPressed(MouseEvent event) {
        scrollPane.setPannable(false);
        mouseX = event.getX();
        mouseY = event.getY();
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
    public void topLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || topLeftHandler == null || !topLeftHandler.isVisible()
                || !maskPane.getChildren().contains(topLeftHandler)) {
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
    public void topCenterHandlerReleased(MouseEvent event) {
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
            double r = bottomCenterHandler.getY() - event.getY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double ry = bottomCenterHandler.getY() - event.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskShapeDataChanged();
            }
        }

    }

    @FXML
    public void topRightHandlerReleased(MouseEvent event) {
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
    public void bottomLeftHandlerReleased(MouseEvent event) {
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
    public void bottomCenterHandlerReleased(MouseEvent event) {
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
            double r = event.getY() - topCenterHandler.getY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double ry = event.getY() - topCenterHandler.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskShapeDataChanged();
            }
        }

    }

    @FXML
    public void bottomRightHandlerReleased(MouseEvent event) {
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
    public void leftCenterHandlerReleased(MouseEvent event) {
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
            double r = rightCenterHandler.getX() - event.getX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double rx = rightCenterHandler.getX() - event.getX();
            if (rx > 0) {
                rx = rx * imageYRatio() / 2;
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskShapeDataChanged();
            }
        }

    }

    @FXML
    public void rightCenterHandlerReleased(MouseEvent event) {
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
            double r = event.getX() - leftCenterHandler.getLayoutX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircle();
                maskShapeDataChanged();
            }

        } else if (maskEllipse != null && maskEllipse.isVisible()) {
            double rx = event.getX() - leftCenterHandler.getLayoutX();
            if (rx > 0) {
                rx = rx * imageYRatio() / 2;
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
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
        MyBoxLog.console(maskLine != null);
        if (maskLine != null) {
            maskLine.setCursor(Cursor.HAND);
        }
        if (maskPolygon != null) {
            maskPolygon.setCursor(Cursor.HAND);
        }
        if (maskPolyline != null) {
            maskPolyline.setCursor(Cursor.HAND);
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
