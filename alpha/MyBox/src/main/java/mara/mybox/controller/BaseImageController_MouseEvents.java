package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import mara.mybox.data.DoublePoint;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_MouseEvents extends BaseImageController_Shapes {

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
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
                drawMaskRectangleLine();
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
                drawMaskCircleLine();
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
                drawMaskEllipseLine();
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
                drawMaskLineLine();
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
            maskPolyline.getPoints().add(p.getX() * viewXRatio());
            maskPolyline.getPoints().add(p.getY() * viewYRatio());
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
            maskPolygonLine.getPoints().add(p.getX() * viewXRatio());
            maskPolygonLine.getPoints().add(p.getY() * viewYRatio());
            polygonDrawnNotify.set(!polygonDrawnNotify.get());
            return true;

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolygonData.getSize() > 0) {
            DoublePoint p0 = maskPolygonData.getPoints().get(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (coordinateChanged(offsetX, offsetY)) {
                maskPolygonData = maskPolygonData.move(offsetX, offsetY);
                drawMaskPolygonLine();
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
        drawMaskRectangleLine();
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
        drawMaskCircleLine();
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
        drawMaskEllipseLine();
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
        drawMaskLineLine();
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
        drawMaskPolygonLine();
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
            drawMaskLineLine();
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
                drawMaskRectangleLine();
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
                drawMaskRectangleLine();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircleLine();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY();
            if (ry > 0) {
                maskEllipseData.setRadiusY(ry * imageYRatio() / 2);
                drawMaskEllipseLine();
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
            drawMaskRectangleLine();
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
            drawMaskRectangleLine();
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
                drawMaskRectangleLine();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = bottomCenterHandler.getLayoutY() + event.getY() - topCenterHandler.getLayoutY();
            if (r > 0) {
                maskCircleData.setRadius(r * imageYRatio() / 2);
                drawMaskCircleLine();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = bottomCenterHandler.getLayoutY() + event.getY() - topCenterHandler.getLayoutY();
            if (ry > 0) {
                maskEllipseData.setRadiusY(ry * imageYRatio() / 2);
                drawMaskEllipseLine();
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
            drawMaskLineLine();
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
            drawMaskRectangleLine();
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
                drawMaskRectangleLine();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircleLine();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX();
            if (rx > 0) {
                maskEllipseData.setRadiusX(rx * imageXRatio() / 2);
                drawMaskEllipseLine();
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
                drawMaskRectangleLine();
                maskRectChangedByEvent();
            }

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double r = rightCenterHandler.getLayoutX() + event.getX() - leftCenterHandler.getLayoutX();
            if (r > 0) {
                maskCircleData.setRadius(r * imageXRatio() / 2);
                drawMaskCircleLine();
                maskCircleChangedByEvent();
            }

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = rightCenterHandler.getLayoutX() + event.getX() - leftCenterHandler.getLayoutX();
            if (rx > 0) {
                maskEllipseData.setRadiusX(rx * imageXRatio() / 2);
                drawMaskEllipseLine();
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
