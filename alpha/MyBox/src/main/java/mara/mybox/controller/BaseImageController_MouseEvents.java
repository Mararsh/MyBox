package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
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
        MyBoxLog.console("here");
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
        MyBoxLog.console("here");
        if (event == null || p == null) {
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            if (addPointWhenClick) {
                if (maskPolyline != null && maskPolyline.isVisible()) {
                    maskPolylineData.add(p.getX(), p.getY());
                    maskShapeDataChanged();

                } else if (maskPolygon != null && maskPolygon.isVisible()) {
                    MyBoxLog.console("here");
                    maskPolygonData.add(p.getX(), p.getY());
                    maskShapeDataChanged();
                }
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            if (popShapeMenu) {
                DoubleShape shapeData = currentMaskShapeData();
                if (shapeData != null) {
                    popEventMenu(event, maskShapeMenu(event, shapeData, p));
                } else {
                    popImageMenu(event.getScreenX(), event.getScreenY());
                }
            } else {
                popImageMenu(event.getScreenX(), event.getScreenY());
            }

        }
    }

    public boolean canAddPoint() {
        return isMaskPolygonShown() || isMaskPolylineShown();
    }

    protected List<MenuItem> maskShapeMenu(Event event, DoubleShape shapeData, DoublePoint p) {
        try {
            if (event == null || shapeData == null) {
                return null;
            }

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            String info = DoubleShape.values(shapeData);
            if (p != null) {
                info += "\n" + message("Point") + ": " + scale(p.getX()) + ", " + scale(p.getY());
            }
            menu = new MenuItem(info);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);

            items.add(new SeparatorMenuItem());

            Menu anchorStyleMenu = new Menu(message("Anchor"), StyleTools.getIconImageView("iconAnchor.png"));
            items.add(anchorStyleMenu);

            CheckMenuItem anchorShowItem = new CheckMenuItem(message("ShowAnchors"), StyleTools.getIconImageView("iconAnchor.png"));
            anchorShowItem.setSelected(UserConfig.getBoolean(baseName + "ImageShapeShowAnchor", true));
            anchorShowItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    UserConfig.setBoolean(baseName + "ImageShapeShowAnchor", anchorShowItem.isSelected());
                    showAnchors = anchorShowItem.isSelected();
                    setMaskAnchorsStyle();
                }
            });
            anchorStyleMenu.getItems().add(anchorShowItem);

            CheckMenuItem anchorMenuItem = new CheckMenuItem(
                    isMaskPolylinesShown() ? message("PopLineMenu") : message("PopAnchorMenu"),
                    StyleTools.getIconImageView("iconMenu.png"));
            anchorMenuItem.setSelected(UserConfig.getBoolean(baseName + "ImageShapeAnchorPopMenu", true));
            anchorMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    UserConfig.setBoolean(baseName + "ImageShapeAnchorPopMenu", anchorMenuItem.isSelected());
                    popAnchorMenu = anchorMenuItem.isSelected();
                }
            });
            anchorStyleMenu.getItems().add(anchorMenuItem);

            anchorStyleMenu.getItems().add(new SeparatorMenuItem());

            ToggleGroup anchorGroup = new ToggleGroup();
            String current = UserConfig.getString(baseName + "ImageShapeAnchorShape", "Rectangle");

            RadioMenuItem rectItem = new RadioMenuItem(message("Rectangle"), StyleTools.getIconImageView("iconRectangle.png"));
            rectItem.setSelected(!"Circle".equals(current) && !"Text".equals(current));
            rectItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    UserConfig.setString(baseName + "ImageShapeAnchorShape", "Rectangle");
                    anchorShape = AnchorShape.Rectangle;
                    redrawMaskShape();
                }
            });
            rectItem.setToggleGroup(anchorGroup);
            anchorStyleMenu.getItems().add(rectItem);

            RadioMenuItem circleItem = new RadioMenuItem(message("Circle"), StyleTools.getIconImageView("iconCircle.png"));
            circleItem.setSelected("Circle".equals(current));
            circleItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    UserConfig.setString(baseName + "ImageShapeAnchorShape", "Circle");
                    anchorShape = AnchorShape.Circle;
                    redrawMaskShape();
                }
            });
            circleItem.setToggleGroup(anchorGroup);
            anchorStyleMenu.getItems().add(circleItem);

            RadioMenuItem numberItem = new RadioMenuItem(message("Name"), StyleTools.getIconImageView("iconNumber.png"));
            numberItem.setSelected("Number".equals(current));
            numberItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    UserConfig.setString(baseName + "ImageShapeAnchorShape", "Number");
                    anchorShape = AnchorShape.Number;
                    redrawMaskShape();
                }
            });
            numberItem.setToggleGroup(anchorGroup);
            anchorStyleMenu.getItems().add(numberItem);

            items.add(new SeparatorMenuItem());

            if (canAddPoint()) {
                CheckMenuItem pointMenuItem = new CheckMenuItem(message("AddPointWhenLeftClick"), StyleTools.getIconImageView("iconAdd.png"));
                pointMenuItem.setSelected(UserConfig.getBoolean(baseName + "ImageShapeAddPointWhenLeftClick", true));
                pointMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent cevent) {
                        UserConfig.setBoolean(baseName + "ImageShapeAddPointWhenLeftClick", pointMenuItem.isSelected());
                        addPointWhenClick = pointMenuItem.isSelected();
                    }
                });
                items.add(pointMenuItem);
            }

            if (maskPolyline != null && maskPolyline.isVisible()) {
                if (p != null) {
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
                }

                menu = new MenuItem(message("RemoveLastPoint"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        if (maskPolylineData.removeLast()) {
                            maskShapeDataChanged();
                        }
                    }
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());

            } else if (maskPolygon != null && maskPolygon.isVisible()) {

                items.add(new SeparatorMenuItem());
                if (p != null) {
                    menu = new MenuItem(message("AddPointInShape"), StyleTools.getIconImageView("iconAdd.png"));
                    menu.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent mevent) {
                            maskPolygonData.add(p.getX(), p.getY());
                            maskShapeDataChanged();
                        }
                    });
                    items.add(menu);
                }

                menu = new MenuItem(message("RemoveLastPoint"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        if (maskPolygonData.removeLast()) {
                            maskShapeDataChanged();
                        }
                    }
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());

            }

            menu = new MenuItem(message("TranslateShape"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    ShapeTranslateInputController.open((BaseImageController) myController, shapeData, p);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("ScaleShape"), StyleTools.getIconImageView("iconExpand.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    ShapeScaleInputController.open((BaseImageController) myController, shapeData);
                }
            });
            items.add(menu);

            if (supportPath) {
                menu = new MenuItem(message("RotateShape"), StyleTools.getIconImageView("iconRotateRight.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        ShapeRotateInputController.open((BaseImageController) myController, shapeData, p);
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("ShearShape"), StyleTools.getIconImageView("iconShear.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        ShapeShearInputController.open((BaseImageController) myController, shapeData);
                    }
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

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
        MyBoxLog.console("here");
        scrollPane.setPannable(true);
        event.consume();
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
            maskShapeDataChanged();
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
