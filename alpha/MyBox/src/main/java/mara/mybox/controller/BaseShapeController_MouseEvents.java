package mara.mybox.controller;

import java.awt.geom.Rectangle2D;
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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import static mara.mybox.data.DoubleShape.getBound;
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
public abstract class BaseShapeController_MouseEvents extends BaseShapeController_Base {

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

    @Override
    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (event == null || p == null || maskControlDragged) {
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            if (addPointWhenClick) {
                if (maskPolyline != null && maskPolyline.isVisible()) {
                    maskPolylineData.add(p.getX(), p.getY());
                    maskShapeDataChanged();

                } else if (maskPolygon != null && maskPolygon.isVisible()) {
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
                    popContextMenu(event.getScreenX(), event.getScreenY());
                }
            } else {
                popContextMenu(event.getScreenX(), event.getScreenY());
            }
        }
    }

    protected List<MenuItem> maskShapeMenu(Event event, DoubleShape shapeData, DoublePoint p) {
        try {
            if (event == null || image == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            String info = shapeData != null ? DoubleShape.values(shapeData) : "";
            if (p != null) {
                info += (info.isBlank() ? "" : "\n")
                        + message("Point") + ": " + scale(p.getX()) + ", " + scale(p.getY());
            }
            if (!info.isBlank()) {
                menu = new MenuItem(info);
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            if (!isMaskPolylinesShown()) {
                items.add(anchorShowItem());
            }

            items.add(anchorMenuItem());

            items.add(optionsMenu());

            items.add(new SeparatorMenuItem());

            List<MenuItem> pointItems = shapeDataMenu(event, p);
            if (pointItems != null) {
                items.addAll(pointItems);
            }

            List<MenuItem> opItems = shapeOperationMenu(event, shapeData, p);
            if (opItems != null) {
                items.addAll(opItems);
            }

            List<MenuItem> svgItems = DoubleShape.svgMenu(this, shapeData);
            if (svgItems != null) {
                Menu svgMenu = new Menu("SVG", StyleTools.getIconImageView("iconSVG.png"));
                svgMenu.getItems().addAll(svgItems);
                items.add(svgMenu);
            }

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

            menu = new MenuItem(message("ContextMenu"), StyleTools.getIconImageView("iconMenu.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    popContextMenu(event);
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

    protected List<MenuItem> shapeDataMenu(Event event, DoublePoint p) {
        try {
            if (event == null || image == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (isMaskPolygonShown() || isMaskPolylineShown()) {
                items.add(addPointMenu());
            }

            if (isMaskPolylineShown()) {
                if (p != null) {
                    menu = new MenuItem(message("AddPointInShape"), StyleTools.getIconImageView("iconAdd.png"));
                    menu.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent mevent) {
                            maskPolylineData.add(p.getX(), p.getY());
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

                menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        maskPolylineData.clear();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        setMaskPolylineExample();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

            } else if (isMaskPolygonShown()) {

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

                menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        maskPolygonData.clear();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        setMaskPolygonExample();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

            } else if (isMaskPolylinesShown()) {

                menu = new MenuItem(message("RemoveLastLine"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        if (maskPolylinesData.removeLastLine()) {
                            maskShapeDataChanged();
                        }
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        maskPolylinesData.clear();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        setMaskPolylinesExample();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

            } else if (isMaskPathShown()) {

                menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        maskPathData.clear();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent mevent) {
                        setMaskPathExample();
                        maskShapeDataChanged();
                    }
                });
                items.add(menu);

            } else {
                return null;
            }

            items.add(new SeparatorMenuItem());
            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected List<MenuItem> shapeOperationMenu(Event event, DoubleShape shapeData, DoublePoint p) {
        if (event == null || image == null || shapeData == null) {
            return null;
        }

        Rectangle2D bound = getBound(shapeData);
        if (bound == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        Menu translateMenu = new Menu(message("TranslateShape"), StyleTools.getIconImageView("iconMove.png"));
        items.add(translateMenu);

        menu = new MenuItem(message("ImageCenter"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                DoublePoint center = DoubleShape.getCenter(shapeData);
                if (center == null) {
                    return;
                }
                double offsetX = imageView.getImage().getWidth() * 0.5 - center.getX();
                double offsetY = imageView.getImage().getHeight() * 0.5 - center.getY();
                shapeData.translateRel(offsetX, offsetY);
                maskShapeDataChanged();
            }
        });
        translateMenu.getItems().add(menu);

        menu = new MenuItem(message("LeftTop"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                double offsetX = -bound.getMinX();
                double offsetY = -bound.getMinY();
                shapeData.translateRel(offsetX, offsetY);
                maskShapeDataChanged();
            }
        });
        translateMenu.getItems().add(menu);

        menu = new MenuItem(message("RightBottom"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {

                double offsetX = imageView.getImage().getWidth() - bound.getMaxX();
                double offsetY = imageView.getImage().getHeight() - bound.getMaxY();
                shapeData.translateRel(offsetX, offsetY);
                maskShapeDataChanged();
            }
        });
        translateMenu.getItems().add(menu);

        menu = new MenuItem(message("LeftBottom"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                double offsetX = -bound.getMinX();
                double offsetY = imageView.getImage().getHeight() - bound.getMaxY();
                shapeData.translateRel(offsetX, offsetY);
                maskShapeDataChanged();
            }
        });
        translateMenu.getItems().add(menu);

        menu = new MenuItem(message("RightTop"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                double offsetX = imageView.getImage().getWidth() - bound.getMaxX();
                double offsetY = -bound.getMinY();
                shapeData.translateRel(offsetX, offsetY);
                maskShapeDataChanged();
            }
        });
        translateMenu.getItems().add(menu);

        menu = new MenuItem(message("Set") + "...", StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                ShapeTranslateInputController.open((BaseShapeController) myController, shapeData, p);
            }
        });
        translateMenu.getItems().add(menu);

        Menu scaleMenu = new Menu(message("ScaleShape"), StyleTools.getIconImageView("iconExpand.png"));
        items.add(scaleMenu);

        menu = new MenuItem(message("ImageSize"), StyleTools.getIconImageView("iconExpand.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                if (DoubleShape.scale(shapeData,
                        imageView.getImage().getWidth() / bound.getWidth(),
                        imageView.getImage().getHeight() / bound.getHeight())) {
                    maskShapeDataChanged();
                }
            }
        });
        scaleMenu.getItems().add(menu);

        menu = new MenuItem(message("ImageWidth"), StyleTools.getIconImageView("iconExpand.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                if (DoubleShape.scale(shapeData, imageView.getImage().getWidth() / bound.getWidth(), 1)) {
                    maskShapeDataChanged();
                }
            }
        });
        scaleMenu.getItems().add(menu);

        menu = new MenuItem(message("ImageWidth") + " - " + message("KeepRatio"), StyleTools.getIconImageView("iconExpand.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {

                double ratio = imageView.getImage().getWidth() / bound.getWidth();
                if (DoubleShape.scale(shapeData, ratio, ratio)) {
                    maskShapeDataChanged();
                }
            }
        });
        scaleMenu.getItems().add(menu);

        menu = new MenuItem(message("ImageHeight"), StyleTools.getIconImageView("iconExpand.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {

                if (DoubleShape.scale(shapeData, 1, imageView.getImage().getHeight() / bound.getHeight())) {
                    maskShapeDataChanged();
                }
            }
        });
        scaleMenu.getItems().add(menu);

        menu = new MenuItem(message("ImageHeight") + " - " + message("KeepRatio"), StyleTools.getIconImageView("iconExpand.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {

                double ratio = imageView.getImage().getHeight() / bound.getHeight();
                if (DoubleShape.scale(shapeData, ratio, ratio)) {
                    maskShapeDataChanged();
                }
            }
        });
        scaleMenu.getItems().add(menu);

        menu = new MenuItem(message("Set") + "...", StyleTools.getIconImageView("iconExpand.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent mevent) {
                ShapeScaleInputController.open((BaseShapeController) myController, shapeData);
            }
        });
        scaleMenu.getItems().add(menu);

        if (supportPath) {
            menu = new MenuItem(message("RotateShape"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    ShapeRotateInputController.open((BaseShapeController) myController, shapeData, p);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("ShearShape"), StyleTools.getIconImageView("iconShear.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    ShapeShearInputController.open((BaseShapeController) myController, shapeData);
                }
            });
            items.add(menu);
        }

        items.add(new SeparatorMenuItem());

        return items;

    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        mousePoint(event);
    }

    @FXML
    @Override
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
        addPointToCurrentLine(p);
        lastPoint = p;
    }

    @FXML
    @Override
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
        addPointToCurrentLine(p);
        if (maskPolylines.contains(currentLine)) {
            maskShapeDataChanged();
        }
        currentLineData = null;
        currentLine = null;
        lastPoint = null;
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
            translateShape();
            maskControlDragged = true;
        } else {
            maskControlDragged = false;
        }
    }

    public void translateShape() {
        maskShapeDataChanged();
    }

    @FXML
    public void popShapeMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "ShapeMenuPopWhenMouseHovering", true)) {
            showShapeMenu(event);
        }
    }

    @FXML
    public void showShapeMenu(Event event) {
        try {
            if (event == null) {
                return;
            }
            List<MenuItem> items = maskShapeMenu(event, currentMaskShapeData(), null);

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "ShapeMenuPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    UserConfig.setBoolean(baseName + "ShapeMenuPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void exampleData(Event event) {
        if (isMaskPolygonShown()) {
            setMaskPolygonExample();
            maskShapeDataChanged();

        } else if (isMaskPolylineShown()) {
            setMaskPolylineExample();
            maskShapeDataChanged();

        } else if (isMaskPolylinesShown()) {
            setMaskPolylinesExample();
            maskShapeDataChanged();

        } else if (isMaskPathShown()) {
            setMaskPathExample();
            maskShapeDataChanged();
        }
    }

    /*
        pick color
     */
    @Override
    protected void startPickingColor() {
        super.startPickingColor();
        setShapesCursor(Cursor.HAND);
    }

    protected void setShapesCursor(Cursor cursor) {
        if (maskRectangle != null) {
            maskRectangle.setCursor(cursor);
        }
        if (maskCircle != null) {
            maskCircle.setCursor(cursor);
        }
        if (maskEllipse != null) {
            maskEllipse.setCursor(cursor);
        }
        if (maskLine != null) {
            maskLine.setCursor(cursor);
        }
        if (maskPolygon != null) {
            maskPolygon.setCursor(cursor);
        }
        if (maskPolyline != null) {
            maskPolyline.setCursor(cursor);
        }
        if (maskQuadratic != null) {
            maskQuadratic.setCursor(cursor);
        }
        if (maskCubic != null) {
            maskCubic.setCursor(cursor);
        }
        if (maskArc != null) {
            maskArc.setCursor(cursor);
        }
        if (maskSVGPath != null) {
            maskSVGPath.setCursor(cursor);
        }
    }

    @Override
    protected void stopPickingColor() {
        super.stopPickingColor();
        setShapesCursor(Cursor.MOVE);
    }

}
