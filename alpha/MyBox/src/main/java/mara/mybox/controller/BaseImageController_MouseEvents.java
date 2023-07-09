package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;

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

//        } else if (event.getClickCount() > 1) {  // Notice: Double click always trigger single click at first
//            imageDoubleClicked(event, p);
        } else if (event.getClickCount() == 1) {
            imageSingleClicked(event, p);

        }
    }

    @FXML
    public void mousePressed(MouseEvent event) {

    }

    @FXML
    public void mouseDragged(MouseEvent event) {

    }

    @FXML
    public void mouseReleased(MouseEvent event) {

    }

    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (event == null || p == null) {
            return;
        }
        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            if (singleClickedRectangle(event, p)) {
                maskRectChangedByEvent();
                return;
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            if (singleClickedCircle(event, p)) {
                maskCircleChangedByEvent();
                return;
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            if (singleClickedEllipse(event, p)) {
                maskEllipseChangedByEvent();
                return;
            }

        } else if (maskLine != null && maskLine.isVisible()) {
            if (singleClickedLine(event, p)) {
                maskLineChangedByEvent();
                return;
            }

        } else if (maskPolyline != null && maskPolyline.isVisible()) {
            if (singleClickedPolyline(event, p)) {
                maskPolylineChangedByEvent();
                return;
            }

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            if (singleClickedPolygonLine(event, p)) {
                maskPolygonChangedByEvent();
                return;
            }

        } else if (maskPenData != null && maskPenLines != null) {
            if (singleClickedPenLines(event, p)) {
                return;
            }

        } else if (maskPolylineLineData != null && maskPolylineLines != null) {
            if (singleClickedPolylineLines(event, p)) {
                return;
            }

        }
        if (event.getButton() == MouseButton.SECONDARY) {
            popImageMenu(event.getScreenX(), event.getScreenY());
        }
    }

    protected boolean singleClickedRectangle(MouseEvent event, DoublePoint p) {
        if (p == null || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            double offsetX = p.getX() - maskRectangleData.getSmallX();
            double offsetY = p.getY() - maskRectangleData.getSmallY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskRectangleData = maskRectangleData.move(offsetX, offsetY);
                drawMaskRectangle();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedCircle(MouseEvent event, DoublePoint p) {
        if (p == null || maskCircleLine == null || !maskCircleLine.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            double offsetX = p.getX() - maskCircleData.getCenterX();
            double offsetY = p.getY() - maskCircleData.getCenterY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskCircleData = maskCircleData.move(offsetX, offsetY);
                drawMaskCircle();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedEllipse(MouseEvent event, DoublePoint p) {
        if (p == null || maskEllipseLine == null || !maskEllipseLine.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            double offsetX = p.getX() - maskEllipseData.getCenterX();
            double offsetY = p.getY() - maskEllipseData.getCenterY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskEllipseData = maskEllipseData.move(offsetX, offsetY);
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
            double offsetX = p.getX() - maskLineData.getStartX();
            double offsetY = p.getY() - maskLineData.getStartY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskLineData = maskLineData.move(offsetX, offsetY);
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
            polylineDrawnNotify.set(!polylineDrawnNotify.get());
            return true;

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolylineData.getSize() > 0) {
            DoublePoint p0 = maskPolylineData.getPoints().get(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskPolylineData = maskPolylineData.move(offsetX, offsetY);
                drawMaskPolyline();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedPolygonLine(MouseEvent event, DoublePoint p) {
        if (p == null || maskPolygonLine == null || !maskPolygonLine.isVisible()) {
            return false;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            maskPolygonData.add(p.getX(), p.getY());
            double x = p.getX() * viewXRatio();
            double y = p.getY() * viewYRatio();
            addMaskPolygonPoint(maskPolygonData.getSize(), p, x, y);
            polygonDrawnNotify.set(!polygonDrawnNotify.get());
            return true;

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolygonData.getSize() > 0) {
            DoublePoint p0 = maskPolygonData.getPoints().get(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskPolygonData = maskPolygonData.move(offsetX, offsetY);
                drawMaskPolygon();
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedPenLines(MouseEvent event, DoublePoint p) {
        if (p == null || maskPenData == null || maskPenLines == null) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY && maskPenData.getLinesSize() > 0) {
            DoublePoint p0 = maskPenData.getPoint(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskPenData = maskPenData.move(offsetX, offsetY);
                return true;
            }
        }
        return false;
    }

    protected boolean singleClickedPolylineLines(MouseEvent event, DoublePoint p) {
        if (p == null || maskPolylineLineData == null || maskPolylineLines == null) {
            return false;
        }
        if (event.getButton() == MouseButton.SECONDARY && maskPolylineLineData.getSize() > 0) {
            DoublePoint p0 = maskPolylineLineData.get(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskPolylineLineData = maskPolylineLineData.move(offsetX, offsetY);
                return true;
            }
        }
        return false;
    }

    @FXML
    public void handlerPressed(MouseEvent event) {
        scrollPane.setPannable(false);
        mouseX = event.getX();
        mouseY = event.getY();
    }

    public double imageOffsetX(MouseEvent event) {
        return (event.getX() - mouseX) * imageXRatio();
    }

    public double imageOffsetY(MouseEvent event) {
        return (event.getY() - mouseY) * imageYRatio();
    }

    public boolean coordinateChanged(double offsetX, double offsetY) {
        return Math.abs(offsetX) > 0.01 || Math.abs(offsetY) > 0.01;
    }

    @FXML
    public void rectangleReleased(MouseEvent event) {
        if (isPickingColor
                || maskRectangleLine == null || !maskRectangleLine.isVisible()
                || !maskPane.getChildren().contains(maskRectangleLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!coordinateChanged(offsetX, offsetY)) {
            return;
        }
        maskRectangleData = maskRectangleData.move(offsetX, offsetY);
        drawMaskRectangle();
        maskRectChangedByEvent();
    }

    @FXML
    public void circleReleased(MouseEvent event) {
        if (isPickingColor || maskCircleLine == null || !maskCircleLine.isVisible()
                || !maskPane.getChildren().contains(maskCircleLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!coordinateChanged(offsetX, offsetY)) {
            return;
        }
        maskCircleData = maskCircleData.move(offsetX, offsetY);
        drawMaskCircle();
        maskCircleChangedByEvent();
    }

    @FXML
    public void ellipseReleased(MouseEvent event) {
        if (isPickingColor || maskEllipseLine == null || !maskEllipseLine.isVisible()
                || !maskPane.getChildren().contains(maskEllipseLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!coordinateChanged(offsetX, offsetY)) {
            return;
        }
        maskEllipseData = maskEllipseData.move(offsetX, offsetY);
        drawMaskEllipse();
        maskEllipseChangedByEvent();
    }

    @FXML
    public void lineReleased(MouseEvent event) {
        if (isPickingColor || maskLine == null || !maskLine.isVisible()
                || !maskPane.getChildren().contains(maskLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!coordinateChanged(offsetX, offsetY)) {
            return;
        }
        maskLineData = maskLineData.move(offsetX, offsetY);
        drawMaskLine();
        maskLineChangedByEvent();
    }

    @FXML
    public void polylineReleased(MouseEvent event) {
        if (isPickingColor || maskPolyline == null || !maskPolyline.isVisible()
                || !maskPane.getChildren().contains(maskPolyline)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!coordinateChanged(offsetX, offsetY)) {
            return;
        }
        maskPolylineData = maskPolylineData.move(offsetX, offsetY);
        drawMaskPolyline();
        maskPolylineChangedByEvent();
    }

    @FXML
    public void polygonReleased(MouseEvent event) {
        if (isPickingColor || maskPolygonLine == null || !maskPolygonLine.isVisible()
                || !maskPane.getChildren().contains(maskPolygonLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (!coordinateChanged(offsetX, offsetY)) {
            return;
        }
        maskPolygonData = maskPolygonData.move(offsetX, offsetY);
        drawMaskPolygon();
        maskPolygonChangedByEvent();
    }

    public double maskHandlerX(Shape shape, MouseEvent event) {
        return (shape.getLayoutX() + event.getX() - imageView.getLayoutX())
                * imageXRatio();
    }

    public double maskHandlerY(Shape shape, MouseEvent event) {
        return (shape.getLayoutY() + event.getY() - imageView.getLayoutY())
                * imageYRatio();
    }

    @FXML
    public void topLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || topLeftHandler == null || !topLeftHandler.isVisible()
                || !maskPane.getChildren().contains(topLeftHandler)) {
            return;
        }

        double x = maskHandlerX(topLeftHandler, event);
        double y = maskHandlerY(topLeftHandler, event);

        if (maskLine != null && maskLine.isVisible()) {

            maskLineData.setStartX(x);
            maskLineData.setStartY(y);
            drawMaskLine();
            maskLineChangedByEvent();

        } else if (maskRectangleLine != null && maskRectangleLine.isVisible()) {

            if (x < maskRectangleData.getBigX() && y < maskRectangleData.getBigY()) {
                if (x >= getImageWidth() - 1) {
                    x = getImageWidth() - 2;
                }
                if (y >= getImageHeight() - 1) {
                    y = getImageHeight() - 2;
                }
                maskRectangleData.setSmallX(x);
                maskRectangleData.setSmallY(y);
                drawMaskRectangle();
                maskRectChangedByEvent();
            }
        }
    }

    @FXML
    public void topCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }
        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double y = maskHandlerY(topCenterHandler, event);
            if (y < maskRectangleData.getBigY()) {
                if (y >= getImageHeight() - 1) {
                    y = getImageHeight() - 2;
                }
                maskRectangleData.setSmallY(y);
                drawMaskRectangle();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircle();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskEllipseChangedByEvent();
            }
        }

    }

    @FXML
    public void topRightHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double x = maskHandlerX(topRightHandler, event);
        double y = maskHandlerY(topRightHandler, event);

        if (x > maskRectangleData.getSmallX() && y < maskRectangleData.getBigY()) {
            if (x <= 0) {
                x = 1;
            }
            if (y >= getImageHeight() - 1) {
                y = getImageHeight() - 2;
            }
            maskRectangleData.setBigX(x);
            maskRectangleData.setSmallY(y);
            drawMaskRectangle();
            maskRectChangedByEvent();
        }
    }

    @FXML
    public void bottomLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }
        double x = maskHandlerX(bottomLeftHandler, event);
        double y = maskHandlerY(bottomLeftHandler, event);

        if (x < maskRectangleData.getBigX() && y > maskRectangleData.getSmallY()) {
            if (x >= getImageWidth() - 1) {
                x = getImageWidth() - 2;
            }
            if (y <= 0) {
                y = 1;
            }
            maskRectangleData.setSmallX(x);
            maskRectangleData.setBigY(y);
            drawMaskRectangle();
            maskRectChangedByEvent();
        }
    }

    @FXML
    public void bottomCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double y = maskHandlerY(bottomCenterHandler, event);
            if (y > maskRectangleData.getSmallY()) {
                if (y <= 0) {
                    y = 1;
                }
                maskRectangleData.setBigY(y);
                drawMaskRectangle();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = bottomCenterHandler.getLayoutY() + event.getY() - topCenterHandler.getLayoutY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircle();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = bottomCenterHandler.getLayoutY() + event.getY() - topCenterHandler.getLayoutY();
            if (ry > 0) {
                ry = ry * imageYRatio() / 2;
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskEllipseChangedByEvent();
            }
        }

    }

    @FXML
    public void bottomRightHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double x = maskHandlerX(bottomRightHandler, event);
        double y = maskHandlerY(bottomRightHandler, event);

        if (maskLine != null && maskLine.isVisible()) {
            maskLineData.setEndX(x);
            maskLineData.setEndY(y);
            drawMaskLine();
            maskLineChangedByEvent();

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
            maskRectChangedByEvent();
        }
    }

    @FXML
    public void leftCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double x = maskHandlerX(leftCenterHandler, event);
            if (x < maskRectangleData.getBigX()) {
                if (x >= getImageWidth() - 1) {
                    x = getImageWidth() - 2;
                }
                maskRectangleData.setSmallX(x);
                drawMaskRectangle();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircle();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX();
            if (rx > 0) {
                rx = rx * imageYRatio() / 2;
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskEllipseChangedByEvent();
            }
        }

    }

    @FXML
    public void rightCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double x = maskHandlerX(rightCenterHandler, event);

            if (x > maskRectangleData.getSmallX()) {
                if (x <= 0) {
                    x = 1;
                }
                maskRectangleData.setBigX(x);
                drawMaskRectangle();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = rightCenterHandler.getLayoutX() + event.getX() - leftCenterHandler.getLayoutX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircle();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = rightCenterHandler.getLayoutX() + event.getX() - leftCenterHandler.getLayoutX();
            if (rx > 0) {
                rx = rx * imageYRatio() / 2;
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipse();
                maskEllipseChangedByEvent();
            }
        }

    }

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

}
