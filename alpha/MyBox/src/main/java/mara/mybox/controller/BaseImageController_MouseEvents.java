package mara.mybox.controller;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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

            if (UserConfig.getBoolean("ImageShapeAddPointWhenLeftClick", true)) {
                if (maskPolyline != null && maskPolyline.isVisible()) {
                    maskPolylineData.add(p.getX(), p.getY());
                    double x = p.getX() * viewXRatio();
                    double y = p.getY() * viewYRatio();
                    addMaskPolylinePoint(maskPolylineData.getSize(), p, x, y);
                    maskShapeDataChanged();

                } else if (maskPolygon != null && maskPolygon.isVisible()) {
                    maskPolygonData.add(p.getX(), p.getY());
                    double x = p.getX() * viewXRatio();
                    double y = p.getY() * viewYRatio();
                    addMaskPolygonPoint(maskPolygonData.getSize(), p, x, y);
                    maskShapeDataChanged();
                }
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            DoubleShape shapeData = currentMaskShapeData();
            if (shapeData != null) {
                popEventMenu(event, maskShapeMenu(event, shapeData, p));
            } else {
                popImageMenu(event.getScreenX(), event.getScreenY());
            }

        }
    }

    protected List<MenuItem> maskShapeMenu(Event event, DoubleShape shapeData, DoublePoint p) {
        try {
            if (event == null || shapeData == null) {
                return null;
            }

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            Rectangle2D bounds = DoubleShape.getBound(shapeData);
            double x1 = scale(bounds.getMinX());
            double y1 = scale(bounds.getMinY());
            double x2 = scale(bounds.getMaxX());
            double y2 = scale(bounds.getMaxY());
            double cx = scale(bounds.getCenterX());
            double cy = scale(bounds.getCenterY());
            double w = scale(bounds.getWidth());
            double h = scale(bounds.getHeight());
            double px = p != null ? scale(p.getX()) : 0;
            double py = p != null ? scale(p.getY()) : 0;
            String info = shapeData.name() + "\n"
                    + message("LeftTop") + ": " + x1 + ", " + y1 + "\n"
                    + message("RightBottom") + ": " + x2 + ", " + y2 + "\n"
                    + message("Center") + ": " + cx + ", " + cy + "\n"
                    + message("Width") + ": " + w + "  " + message("Height") + ": " + h;
            if (p != null) {
                info += message("Point") + ": " + px + ", " + py;
            }
            menu = new MenuItem(info);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (p != null) {
                if (maskPolyline != null && maskPolyline.isVisible()) {
                    menu = new MenuItem(message("AddPointInShape"), StyleTools.getIconImageView("iconAdd.png"));
                    menu.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent mevent) {
                            maskPolylineData.add(p.getX(), p.getY());
                            double x = p.getX() * viewXRatio();
                            double y = p.getY() * viewYRatio();
                            addMaskPolylinePoint(maskPolylineData.getSize(), p, x, y);
                            maskShapeDataChanged();
                        }
                    });
                    items.add(menu);

                    items.add(new SeparatorMenuItem());

                } else if (maskPolygon != null && maskPolygon.isVisible()) {
                    menu = new MenuItem(message("AddPointInShape"), StyleTools.getIconImageView("iconAdd.png"));
                    menu.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent mevent) {
                            maskPolygonData.add(p.getX(), p.getY());
                            double x = p.getX() * viewXRatio();
                            double y = p.getY() * viewYRatio();
                            addMaskPolygonPoint(maskPolygonData.getSize(), p, x, y);
                            maskShapeDataChanged();
                        }
                    });
                    items.add(menu);
                    items.add(new SeparatorMenuItem());
                }

                menu = new MenuItem(message("TranslateShapeCenterToPoint"), StyleTools.getIconImageView("iconMove.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        translateRel(shapeData, px - cx, py - cy);
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("TranslateShapeLeftTopToPoint"), StyleTools.getIconImageView("iconMove.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        translateRel(shapeData, px - x1, py - y1);
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("TranslateShapeRightBottomToPoint"), StyleTools.getIconImageView("iconMove.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        translateRel(shapeData, px - x2, py - y2);
                    }
                });
                items.add(menu);

            }

            menu = new MenuItem(message("TranslateShapeCenterToImageCenter"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    translateRel(shapeData, imageWidth() * 0.5 - cx, imageHeight() * 0.5 - cy);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("TranslateShapeCenterTo"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    PointInputController inputController = PointInputController.open(myController,
                            message("TranslateShapeCenterTo"), new DoublePoint(cx, cy));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                            translateRel(shapeData, inputController.picked.getX() - cx, inputController.picked.getY() - cy);
                            inputController.close();
                        }
                    });
                }
            });
            items.add(menu);;

            menu = new MenuItem(message("TranslateShapeLeftTopTo"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    PointInputController inputController = PointInputController.open(myController,
                            message("TranslateShapeLeftTopTo"), new DoublePoint(x1, y1));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                            translateRel(shapeData, inputController.picked.getX() - x1, inputController.picked.getY() - y1);
                            inputController.close();
                        }
                    });
                }
            });
            items.add(menu);

            menu = new MenuItem(message("TranslateShapeRightBottomTo"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    PointInputController inputController = PointInputController.open(myController,
                            message("TranslateShapeRightBottomTo"), new DoublePoint(x2, y2));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                            translateRel(shapeData, inputController.picked.getX() - x2, inputController.picked.getY() - y2);
                            inputController.close();
                        }
                    });
                }
            });
            items.add(menu);

            menu = new MenuItem(message("ScaleShape"), StyleTools.getIconImageView("iconExpand.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    PointInputController inputController = PointInputController.open(myController,
                            message("ScaleShape"), new DoublePoint(2, 2));
                    inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                            scale(shapeData, inputController.picked.getX(), inputController.picked.getY());
                            inputController.close();
                        }
                    });
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            // have not found right way to convert java Arc2D to SVG arc
//            if (!(shapeData instanceof DoubleArc)) {
            menu = new MenuItem(message("SVGPath") + " - " + message("AbsoluteCoordinate"), StyleTools.getIconImageView("iconSVG.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(myController, shapeData.svgAbs());
                }
            });
            items.add(menu);

            menu = new MenuItem(message("SVGPath") + " - " + message("RelativeCoordinate"), StyleTools.getIconImageView("iconSVG.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    TextPopController.loadText(myController, shapeData.svgRel());
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
//            }

            menu = new MenuItem(message("ImageCoordinateDecimalDigits"), StyleTools.getIconImageView("iconNumber.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    String value = PopTools.askValue(getBaseTitle(), null, message("ImageCoordinateDecimalDigits"),
                            UserConfig.getInt("ImageDecimal", 3) + "");
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(value);
                        UserConfig.setInt("ImageDecimal", v);
                        popInformation(message("TakeEffectNextTime"));
                    } catch (Exception e) {
                        popError(e.toString());
                    }
                }
            });
            items.add(menu);

            menu = new MenuItem(message("ImageMenu"), StyleTools.getIconImageView("iconMenu.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    popImageMenu(event);
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void translateRel(DoubleShape shapeData, double offsetX, double offsetY) {
        shapeData.translateRel(offsetX, offsetY);
        drawMaskShape();
        maskShapeDataChanged();
    }

    public void scale(DoubleShape shapeData, double scaleX, double scaleY) {
        shapeData.scale(scaleX, scaleY);
        drawMaskShape();
        maskShapeDataChanged();
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
                || isPickingColor || event.getButton() == MouseButton.SECONDARY
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
    public void translateShape(MouseEvent event) {
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
