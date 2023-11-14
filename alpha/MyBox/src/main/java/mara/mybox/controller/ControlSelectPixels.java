package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Outline;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Polygon;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.cell.ListColorCell;
import mara.mybox.fxml.cell.ListImageCell;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 *
 */
public class ControlSelectPixels extends ControlSelectPixels_Save {

    public ControlSelectPixels() {
        TipsLabelKey = "ScopeTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initOptions();
            initAreaTab();
            initColorsTab();
            initMatchTab();
            initPixTab();

            thisPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(opacitySelector, new Tooltip(message("Opacity")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initOptions() {
        try {
            tableColor = new TableColor();
            popShapeMenu = true;
            supportPath = false;
            shapeStyle = null;
            showNotify = new SimpleBooleanProperty(false);

            scopeTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    applyScope();
                }
            });

            scopeExcludeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

            maskColorController.init(this, baseName + "MaskColor", Color.TRANSPARENT);
            maskColor = maskColorController.awtColor();
            maskColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    maskColor = maskColorController.awtColor();
                    scope.setMaskColor(maskColor);
                    redrawMaskShape();
                }
            });

            maskOpacity = UserConfig.getFloat(baseName + "ScopeOpacity", 0.5f);
            opacitySelector.getItems().addAll(
                    Arrays.asList("0.5", "0.2", "1", "0", "0.8", "0.3", "0.6", "0.7", "0.9", "0.4")
            );
            opacitySelector.setValue(maskOpacity + "");
            opacitySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        if (newVal == null) {
                            return;
                        }
                        float f = Float.parseFloat(newVal);
                        if (f >= 0 && f <= 1.0) {
                            maskOpacity = f;
                            ValidationTools.setEditorNormal(opacitySelector);
                            UserConfig.setFloat(baseName + "ScopeOpacity", f);
                            scope.setMaskOpacity(maskOpacity);
                            redrawMaskShape();
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initAreaTab() {
        try {
            areaExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

            pointsController.tableData.addListener(new ListChangeListener<DoublePoint>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends DoublePoint> c) {
                    if (isSettingValues
                            || pointsController.isSettingValues
                            || pointsController.isSettingTable) {
                        return;
                    }
                    showScope();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initColorsTab() {
        try {
            colorsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            colorsList.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ListColorCell();
                }
            });
            colorsList.getItems().addListener(new ListChangeListener<Color>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends Color> c) {
                    int size = colorsList.getItems().size();
                    colorsSizeLabel.setText(message("Count") + ": " + size);
                    if (size > 100) {
                        colorsSizeLabel.setStyle(NodeStyleTools.redTextStyle());
                    } else {
                        colorsSizeLabel.setStyle(NodeStyleTools.blueTextStyle());
                    }
                    clearColorsButton.setDisable(size == 0);
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

            clearColorsButton.setDisable(true);
            deleteColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());
            saveColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());

            colorController.init(this, baseName + "Color", Color.THISTLE);
            colorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    addColor((Color) newValue);
                }
            });

            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMatchTab() {
        try {
            matchGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }

                }
            });

            int colorDistance = UserConfig.getInt(baseName + "ColorDistance", 20);
            colorDistance = colorDistance <= 0 ? 20 : colorDistance;
            scopeDistanceSelector.setValue(colorDistance + "");
            scopeDistanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

            eightNeighborCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        showScope();
                    }
                }
            });

            squareRootCheck.setSelected(UserConfig.getBoolean(baseName + "ColorDistanceSquare", false));
            squareRootCheck.disableProperty().bind(colorRGBRadio.selectedProperty().not());
            squareRootCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || !colorRGBRadio.isSelected()) {
                        return;
                    }
                    showScope();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initPixTab() {
        try {
            outlinesList.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
                @Override
                public ListCell<Image> call(ListView<Image> param) {
                    return new ListImageCell();
                }
            });

            outlinesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldValue, Image newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    loadOutlineSource(newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BasePixelsController parent) {
        try {
            this.parentController = parent;
            handler = parent;
            imageController = handler.imageController;

            imageController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    loadImage();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void loadImage() {
        if (imageController == null || !imageController.isShowing()) {
            return;
        }
        loadImage(srcImage());
    }

    @Override
    public boolean afterImageLoaded() {
        if (super.afterImageLoaded()) {
            imageView.setRotate(0);
            loadScope(imageController.scope);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void checkPickingColor() {
        if (!tabPane.getTabs().contains(colorsTab)) {
            isPickingColor = false;
        }
        if (isPickingColor) {
            tabPane.getSelectionModel().select(colorsTab);
        }
        super.checkPickingColor();
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (tabPane.getTabs().contains(colorsTab) && isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, imageController.imageView);
            if (color != null) {
                addColor(color);
            }
        } else if (event.getClickCount() == 1) {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (addPointWhenClick) {
                    if (scope.getScopeType() == ScopeType.Matting) {
                        int x = (int) Math.round(p.getX());
                        int y = (int) Math.round(p.getY());
                        isSettingValues = true;
                        pointsController.addPoint(x, y);
                        isSettingValues = false;
                        showScope();

                    } else if (scope.getScopeType() == ScopeType.Polygon
                            && !maskControlDragged) {
                        maskPolygonData.add(p.getX(), p.getY());
                        maskShapeDataChanged();
                    }
                }

            } else if (event.getButton() == MouseButton.SECONDARY) {
                popEventMenu(event, maskShapeMenu(event, currentMaskShapeData(), p));
            }
        }
        maskControlDragged = false;
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        ImageCanvasInputController controller = ImageCanvasInputController.open(this, baseTitle);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                Image canvas = controller.getCanvas();
                if (canvas != null) {
                    loadImage(canvas);
                }
                controller.close();
            }
        });
    }

    @FXML
    @Override
    public boolean popAction() {
        ImageScopeViewsController.open(handler);
        return true;
    }

    @FXML
    @Override
    public void refreshAction() {
        isSettingValues = false;
        redrawMaskShape();
    }

    @Override
    public boolean redrawMaskShape() {
        if (scope == null) {
            clearMaskShapes();
            return true;
        }
        showScope();
        return true;
    }

    @Override
    public void maskShapeDataChanged() {
        try {
            if (isSettingValues || !isValidScope()) {
                return;
            }
            switch (scope.getScopeType()) {
                case Rectangle:
                    rectLeftTopXInput.setText(scale(maskRectangleData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskRectangleData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskRectangleData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskRectangleData.getMaxY(), 2) + "");
                    scope.setRectangle(maskRectangleData.copy());
                    break;
                case Ellipse:
                    rectLeftTopXInput.setText(scale(maskEllipseData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskEllipseData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskEllipseData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskEllipseData.getMaxY(), 2) + "");
                    scope.setEllipse(maskEllipseData.copy());
                    break;
                case Circle:
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    scope.setCircle(maskCircleData.copy());
                    break;
                case Polygon:
                    pointsController.loadList(maskPolygonData.getPoints());
                    scope.setPolygon(maskPolygonData.copy());
                    break;
                case Outline:
                    scope.setRectangle(maskRectangleData.copy());
                    indicateOutline();
                    return;
                default:
                    return;
            }
            showScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void showSaved() {
        ImageScopesSavedController.load(this);
    }

}
