package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLine;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;

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

        } else if (event.getClickCount() > 1) {  // Notice: Double click always trigger single click at first
            imageDoubleClicked(event, p);

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
        if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            singleClickedPolygonLine(event, p);
        }

        if (event.getButton() != MouseButton.SECONDARY) {
            return;
        }
        Timer menuTimer = new Timer();
        menuTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    popImageMenu(event.getScreenX(), event.getScreenY());
                });
            }
        }, 100);  // double click will be eaten by the menu if not delay

    }

    protected void singleClickedPolygonLine(MouseEvent event, DoublePoint p) {
        if (p == null || maskPolygonLine == null || !maskPolygonLine.isVisible()) {
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            maskPolygonData.add(p.getX(), p.getY());
            drawMaskPolygonLine();

        } else if (event.getButton() == MouseButton.SECONDARY && maskPolygonData.getSize() > 2) {
            List<DoublePoint> maskPoints = maskPolygonData.getPoints();
            DoublePoint p0 = maskPoints.get(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();

            if (offsetX != 0 || offsetY != 0) {
                maskPolygonData = maskPolygonData.move(offsetX, offsetY);
                drawMaskPolygonLine();
            }
        }

    }

    public void imageDoubleClicked(MouseEvent event, DoublePoint p) {
        doubleClickedRectangle(event, p);
        doubleClickedCircle(event, p);
        doubleClickedEllipse(event, p);
    }

    protected void doubleClickedRectangle(MouseEvent event, DoublePoint p) {
        if (p == null || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        double x = p.getX();
        double y = p.getY();
        if (event.getButton() == MouseButton.PRIMARY) {

            if (x < maskRectangleData.getBigX() && y < maskRectangleData.getBigY()) {
                maskRectangleData.setSmallX(x);
                maskRectangleData.setSmallY(y);
                drawMaskRectangleLine();
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {

            if (x > maskRectangleData.getSmallX() && y > maskRectangleData.getSmallY()) {
                maskRectangleData.setBigX(x);
                maskRectangleData.setBigY(y);
                drawMaskRectangleLine();
            }

        }

    }

    protected void doubleClickedCircle(MouseEvent event, DoublePoint p) {
        if (p == null || maskCircleLine == null || !maskCircleLine.isVisible()) {
            return;
        }
        double x = p.getX();
        double y = p.getY();
        if (event.getButton() == MouseButton.PRIMARY) {

            maskCircleData.setCenterX(x);
            maskCircleData.setCenterY(y);
            drawMaskCircleLine();

        } else if (event.getButton() == MouseButton.SECONDARY) {

            if (x != maskCircleData.getCenterX() || y != maskCircleData.getCenterY()) {
                double dx = x - maskCircleData.getCenterX();
                double dy = y - maskCircleData.getCenterY();
                maskCircleData.setRadius(Math.sqrt(dx * dx + dy * dy));
                drawMaskCircleLine();
            }

        }
    }

    protected void doubleClickedEllipse(MouseEvent event, DoublePoint p) {
        if (p == null || maskEllipseLine == null || !maskEllipseLine.isVisible()) {
            return;
        }
        double x = p.getX();
        double y = p.getY();
        if (event.getButton() == MouseButton.PRIMARY) {
            if (x != maskEllipseData.getCenterX()) {
                double xr = Math.abs(x - maskEllipseData.getCenterX());
                maskEllipseData = new DoubleEllipse(
                        maskEllipseData.getCenterX() - xr,
                        maskEllipseData.getCenterY() - maskEllipseData.getRadiusY(),
                        maskEllipseData.getCenterX() + xr,
                        maskEllipseData.getCenterY() + maskEllipseData.getRadiusY()
                );
                drawMaskEllipseLine();
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            if (y != maskEllipseData.getCenterY()) {
                double yr = Math.abs(y - maskEllipseData.getCenterY());
                maskEllipseData = new DoubleEllipse(
                        maskEllipseData.getCenterX() - maskEllipseData.getRadiusX(),
                        maskEllipseData.getCenterY() - yr,
                        maskEllipseData.getCenterX() + maskEllipseData.getRadiusX(),
                        maskEllipseData.getCenterY() + yr
                );
                drawMaskEllipseLine();
            }

        }
    }

    @FXML
    public void handlerPressed(MouseEvent event) {
        scrollPane.setPannable(false);
        mouseX = event.getX();
        mouseY = event.getY();
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
        double offsetX = maskRectangleLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskRectangleLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

        if (x <= 0 - maskRectangleData.getWidth()) {
            x = 0 - maskRectangleData.getWidth() + 1;
        }
        if (x >= getImageWidth()) {
            x = getImageWidth() - 1;
        }
        if (y <= 0 - maskRectangleData.getHeight()) {
            y = 0 - maskRectangleData.getHeight() + 1;
        }
        if (y >= getImageHeight()) {
            y = getImageHeight() - 1;
        }
        maskRectangleData = new DoubleRectangle(x, y,
                x + maskRectangleData.getWidth() - 1, y + maskRectangleData.getHeight() - 1);
        drawMaskRectangleLine();
    }

    @FXML
    public void circleReleased(MouseEvent event) {
        if (isPickingColor || maskCircleLine == null || !maskCircleLine.isVisible()
                || !maskPane.getChildren().contains(maskCircleLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskCircleLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskCircleLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

        if (x <= 0 - maskCircleData.getRadius()) {
            x = 0 - maskCircleData.getRadius() + 1;
        }
        if (x >= getImageWidth() + maskCircleData.getRadius()) {
            x = getImageWidth() + maskCircleData.getRadius() - 1;
        }
        if (y <= 0 - maskCircleData.getRadius()) {
            y = 0 - maskCircleData.getRadius() + 1;
        }
        if (y >= getImageHeight() + maskCircleData.getRadius()) {
            y = getImageHeight() + maskCircleData.getRadius() - 1;
        }

        maskCircleData.setCenterX(x);
        maskCircleData.setCenterY(y);
        drawMaskCircleLine();
    }

    @FXML
    public void ellipseReleased(MouseEvent event) {
        if (isPickingColor || maskEllipseLine == null || !maskEllipseLine.isVisible()
                || !maskPane.getChildren().contains(maskEllipseLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskEllipseLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskEllipseLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

        double rx = maskEllipseData.getRadiusX();
        double ry = maskEllipseData.getRadiusY();

        if (x <= 0 - rx) {
            x = 0 - rx + 1;
        }
        if (x >= getImageWidth() + rx) {
            x = getImageWidth() + rx - 1;
        }
        if (y <= 0 - ry) {
            y = 0 - ry + 1;
        }
        if (y >= getImageHeight() + ry) {
            y = getImageHeight() + ry - 1;
        }

        maskEllipseData = new DoubleEllipse(x - rx, y - ry, x + rx, y + ry);
        drawMaskEllipseLine();
    }

    @FXML
    public void lineReleased(MouseEvent event) {
        if (isPickingColor || maskLine == null || !maskLine.isVisible()
                || !maskPane.getChildren().contains(maskLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);

        double startX = maskLine.getStartX() + event.getX() - mouseX - imageView.getLayoutX();
        double startY = maskLine.getStartY() + event.getY() - mouseY - imageView.getLayoutY();
        double endX = maskLine.getEndX() + event.getX() - mouseX - imageView.getLayoutX();
        double endY = maskLine.getEndY() + event.getY() - mouseY - imageView.getLayoutY();

        double xradio = imageXRatio();
        double yradio = imageYRatio();

        maskLineData = new DoubleLine(startX * xradio, startY * yradio, endX * xradio, endY * yradio);
        drawMaskLineLine();
    }

    @FXML
    public void polygonReleased(MouseEvent event) {
        if (isPickingColor || maskPolygonLine == null || !maskPolygonLine.isVisible()
                || !maskPane.getChildren().contains(maskPolygonLine)
                || (mouseX == event.getX() && mouseY == event.getY())) {
            return;
        }
        scrollPane.setPannable(true);
        double offsetX = maskPolygonLine.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = maskPolygonLine.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

        List<DoublePoint> maskPoints = maskPolygonData.getPoints();
        List<DoublePoint> points = new ArrayList<>();
        for (int i = 0; i < maskPoints.size(); ++i) {
            DoublePoint mp = maskPoints.get(i);
            points.add(new DoublePoint(mp.getX() + x, mp.getY() + y));
        }
        maskPolygonData.setAll(points);
        drawMaskPolygonLine();

    }

    @FXML
    public void topLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || topLeftHandler == null || !topLeftHandler.isVisible()
                || !maskPane.getChildren().contains(topLeftHandler)) {
            return;
        }

        double offsetX = topLeftHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = topLeftHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

        if (maskLine != null && maskLine.isVisible()) {

            maskLineData.setStartX(x);
            maskLineData.setStartY(y);
            drawMaskLineLine();

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
            double offsetY = topCenterHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
            double y = offsetY * imageYRatio();
            if (y < maskRectangleData.getBigY()) {
                if (y >= getImageHeight() - 1) {
                    y = getImageHeight() - 2;
                }
                maskRectangleData.setSmallY(y);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY() + mouseY;
            if (d > 0) {
                d = d * imageYRatio();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = (bottomCenterHandler.getLayoutY() - topCenterHandler.getLayoutY() - event.getY() + mouseY) / 2;
            if (ry > 0) {
                ry = ry * imageYRatio();
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();

                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

    @FXML
    public void topRightHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double offsetX = topRightHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = topRightHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

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
        }
    }

    @FXML
    public void bottomLeftHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double offsetX = bottomLeftHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = bottomLeftHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

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
        }
    }

    @FXML
    public void bottomCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double offsetY = bottomCenterHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
            double y = offsetY * imageYRatio();

            if (y > maskRectangleData.getSmallY()) {
                if (y <= 0) {
                    y = 1;
                }
                maskRectangleData.setBigY(y);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = bottomCenterHandler.getLayoutY() + event.getY() - mouseY - topCenterHandler.getLayoutY();
            if (d > 0) {
                d = d * imageYRatio();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double ry = (bottomCenterHandler.getLayoutY() + event.getY() - mouseY - topCenterHandler.getLayoutY()) / 2;
            if (ry > 0) {
                ry = ry * imageYRatio();
                double rx = maskEllipseData.getRadiusX();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();

                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

    @FXML
    public void bottomRightHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        double offsetX = bottomRightHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
        double offsetY = bottomRightHandler.getLayoutY() + event.getY() - mouseY - imageView.getLayoutY();
        double x = offsetX * imageXRatio();
        double y = offsetY * imageYRatio();

        if (maskLine != null && maskLine.isVisible()) {
            maskLineData.setEndX(x);
            maskLineData.setEndY(y);
            drawMaskLineLine();

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
        }
    }

    @FXML
    public void leftCenterHandlerReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            double offsetX = leftCenterHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
            double x = offsetX * imageXRatio();

            if (x < maskRectangleData.getBigX()) {
                if (x >= getImageWidth() - 1) {
                    x = getImageWidth() - 2;
                }
                maskRectangleData.setSmallX(x);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX() + mouseX;
            if (d > 0) {
                d = d * imageXRatio();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = (rightCenterHandler.getLayoutX() - leftCenterHandler.getLayoutX() - event.getX() + mouseX) / 2;
            if (rx > 0) {
                rx = rx * imageXRatio();
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
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
            double offsetX = rightCenterHandler.getLayoutX() + event.getX() - mouseX - imageView.getLayoutX();
            double x = offsetX * imageXRatio();

            if (x > maskRectangleData.getSmallX()) {
                if (x <= 0) {
                    x = 1;
                }
                maskRectangleData.setBigX(x);
                drawMaskRectangleLine();
            }
        }

        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            double d = rightCenterHandler.getLayoutX() + event.getX() - mouseX - leftCenterHandler.getLayoutX();
            if (d > 0) {
                d = d * imageXRatio();
                maskCircleData.setRadius(d / 2);
                drawMaskCircleLine();
            }
        }

        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            double rx = (rightCenterHandler.getLayoutX() + event.getX() - mouseX - leftCenterHandler.getLayoutX()) / 2;
            if (rx > 0) {
                rx = rx * imageXRatio();
                double ry = maskEllipseData.getRadiusY();
                double cx = maskEllipseData.getCenterX();
                double cy = maskEllipseData.getCenterY();
                maskEllipseData = new DoubleEllipse(cx - rx, cy - ry, cx + rx, cy + ry);
                drawMaskEllipseLine();
            }
        }

    }

}
