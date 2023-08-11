package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
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

        if (event.getClickCount() == 1) {
            imageSingleClicked(event, p);

        }
        maskControlDragged = false;
    }

    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (event == null || p == null) {
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            if (maskPolyline != null && maskPolyline.isVisible()) {
                if (!maskControlDragged) {
                    maskPolylineData.add(p.getX(), p.getY());
                    double x = p.getX() * viewXRatio();
                    double y = p.getY() * viewYRatio();
                    addMaskPolylinePoint(maskPolylineData.getSize(), p, x, y);
                    maskShapeChanged();
                }

            } else if (maskPolygon != null && maskPolygon.isVisible()) {
                if (!maskControlDragged) {
                    maskPolygonData.add(p.getX(), p.getY());
                    double x = p.getX() * viewXRatio();
                    double y = p.getY() * viewYRatio();
                    addMaskPolygonPoint(maskPolygonData.getSize(), p, x, y);
                    maskShapeChanged();
                }

            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            DoubleShape shapeData = currentMaskShapeData();
            if (shapeData != null) {
                if (DoubleShape.translateCenterAbs(shapeData, p.getX(), p.getY())) {
                    drawMaskShape();
                    maskShapeDataChanged();
                }
            } else {
                popImageMenu(event.getScreenX(), event.getScreenY());
            }

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

    @FXML
    public void handlerPressed(MouseEvent event) {
        controlPressed(event);
    }

    @FXML
    public void moveShape(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor) {
            return;
        }
        DoubleShape shapeData = currentMaskShapeData();
        if (shapeData == null) {
            return;
        }
        double offsetX = imageOffsetX(event);
        double offsetY = imageOffsetY(event);
        if (DoubleShape.translateRel(shapeData, offsetX, offsetY)) {
            drawMaskShape();
            maskShapeDataChanged();
        }
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

            if (x < maskRectangleData.getMaxX() && y < maskRectangleData.getMaxY()) {
                if (x >= imageWidth() - 1) {
                    x = imageWidth() - 2;
                }
                if (y >= imageHeight() - 1) {
                    y = imageHeight() - 2;
                }
                maskRectangleData.changeX(x);
                maskRectangleData.changeY(y);
                drawMaskRectangle();
                maskShapeDataChanged();
            }
        }
    }

    @FXML
    public void maskHandlerTopCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskHandlerTopCenter == null || !maskHandlerTopCenter.isVisible()
                || !maskPane.getChildren().contains(maskHandlerTopCenter)) {
            return;
        }
        if (maskRectangle != null && maskRectangle.isVisible()) {
            double y = maskEventY(event);
            if (y < maskRectangleData.getMaxY()) {
                if (y >= imageHeight() - 1) {
                    y = imageHeight() - 2;
                }
                maskRectangleData.changeY(y);
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
                ry = ry * imageYRatio();
                maskEllipseData.setHeight(ry);
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
        if (isPickingColor || maskHandlerTopRight == null || !maskHandlerTopRight.isVisible()
                || !maskPane.getChildren().contains(maskHandlerTopRight)) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double x = maskEventX(event);
            double y = maskEventY(event);

            if (x > maskRectangleData.getX() && y < maskRectangleData.getMaxY()) {
                if (x <= 0) {
                    x = 1;
                }
                if (y >= imageHeight() - 1) {
                    y = imageHeight() - 2;
                }
                maskRectangleData.setMaxX(x);
                maskRectangleData.changeY(y);
                drawMaskRectangle();
                maskShapeDataChanged();
            }

        }

    }

    @FXML
    public void maskHandlerBottomLeftReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskHandlerBottomLeft == null || !maskHandlerBottomLeft.isVisible()
                || !maskPane.getChildren().contains(maskHandlerBottomLeft)) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double x = maskEventX(event);
            double y = maskEventY(event);

            if (x < maskRectangleData.getMaxX() && y > maskRectangleData.getY()) {
                if (x >= imageWidth() - 1) {
                    x = imageWidth() - 2;
                }
                if (y <= 0) {
                    y = 1;
                }
                maskRectangleData.changeX(x);
                maskRectangleData.setMaxY(y);
                drawMaskRectangle();
                maskShapeDataChanged();
            }

        }

    }

    @FXML
    public void maskHandlerBottomCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskHandlerBottomCenter == null || !maskHandlerBottomCenter.isVisible()
                || !maskPane.getChildren().contains(maskHandlerBottomCenter)) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double y = maskEventY(event);
            if (y > maskRectangleData.getY()) {
                if (y <= 0) {
                    y = 1;
                }
                maskRectangleData.setMaxY(y);
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
                ry = ry * imageYRatio();
                maskEllipseData.setHeight(ry);
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
        if (isPickingColor || maskHandlerBottomRight == null || !maskHandlerBottomRight.isVisible()
                || !maskPane.getChildren().contains(maskHandlerBottomRight)) {
            return;
        }

        double x = maskEventX(event);
        double y = maskEventY(event);

        if (maskLine != null && maskLine.isVisible()) {
            maskLineData.setEndX(x);
            maskLineData.setEndY(y);
            drawMaskLine();
            maskShapeDataChanged();

        } else if (x > maskRectangleData.getX() && y > maskRectangleData.getY()) {
            if (x <= 0) {
                x = 1;
            }
            if (y <= 0) {
                y = 1;
            }
            maskRectangleData.setMaxX(x);
            maskRectangleData.setMaxY(y);
            drawMaskRectangle();
            maskShapeDataChanged();
        }
    }

    @FXML
    public void maskHandlerLeftCenterReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (isPickingColor || maskHandlerLeftCenter == null || !maskHandlerLeftCenter.isVisible()
                || !maskPane.getChildren().contains(maskHandlerLeftCenter)) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double x = maskEventX(event);
            if (x < maskRectangleData.getMaxX()) {
                if (x >= imageWidth() - 1) {
                    x = imageWidth() - 2;
                }
                maskRectangleData.changeX(x);
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
                rx = rx * imageXRatio();
                maskEllipseData.setWidth(rx);
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
        if (isPickingColor || maskHandlerRightCenter == null || !maskHandlerRightCenter.isVisible()
                || !maskPane.getChildren().contains(maskHandlerRightCenter)) {
            return;
        }

        if (maskRectangle != null && maskRectangle.isVisible()) {
            double x = maskEventX(event);

            if (x > maskRectangleData.getX()) {
                if (x <= 0) {
                    x = 1;
                }
                maskRectangleData.setMaxX(x);
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
                rx = rx * imageXRatio();
                maskEllipseData.setWidth(rx);
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
        if (maskSVGPath != null) {
            maskSVGPath.setCursor(Cursor.HAND);
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
        if (maskSVGPath != null) {
            maskSVGPath.setCursor(Cursor.MOVE);
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
