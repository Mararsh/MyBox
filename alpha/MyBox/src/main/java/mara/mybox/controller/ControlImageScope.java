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
import javafx.util.Callback;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.cell.ListColorCell;
import mara.mybox.fxml.cell.ListImageCell;
import mara.mybox.fxml.image.ImageViewTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.image.data.ImageScope.ShapeType;
import static mara.mybox.image.data.ImageScope.ShapeType.Matting4;
import static mara.mybox.image.data.ImageScope.ShapeType.Matting8;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 */
public class ControlImageScope extends ControlImageScope_Load {

    protected BaseImageController imageEditor;
    protected ControlDataImageScope dataEditor;

    public void setImageEditor(BaseImageController parent) {
        try {
            this.parentController = parent;
            imageEditor = parent;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setDataEditor(ControlDataImageScope parent) {
        try {
            parentController = parent;
            dataEditor = parent;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public Image srcImage() {
        if (imageEditor != null) {
            image = imageEditor.imageView.getImage();
            sourceFile = imageEditor.sourceFile;

        } else if (dataEditor != null) {
            image = dataEditor.srcImage;
            sourceFile = dataEditor.sourceFile;
        }
        return image;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initOptions();
            initShapeTab();
            initColorsTab();
            initMatchTab();

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

    public void initOptions() {
        try {
            tableColor = new TableColor();
            popShapeMenu = true;
            shapeStyle = null;
            needFixSize = true;
            showNotify = new SimpleBooleanProperty(false);
            changedNotify = new SimpleBooleanProperty(false);

            scopeExcludeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        indicateScope();
                        changedNotify.set(!changedNotify.get());
                    }
                }
            });

            handleTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        indicateScope();
                        changedNotify.set(!changedNotify.get());
                    }
                }
            });

            maskColorController.init(this, baseName + "MaskColor", Color.TRANSPARENT);
            maskColor = maskColorController.awtColor();
            maskColorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (!isSettingValues) {
                        maskColor = maskColorController.awtColor();
                        scope.setMaskColor(maskColor);
                        indicateScope();
                    }
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
                        if (isSettingValues || newVal == null) {
                            return;
                        }
                        float f = Float.parseFloat(newVal);
                        if (f >= 0 && f <= 1.0) {
                            maskOpacity = f;
                            ValidationTools.setEditorNormal(opacitySelector);
                            UserConfig.setFloat(baseName + "ScopeOpacity", f);
                            scope.setMaskOpacity(maskOpacity);
                            indicateScope();
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            clearDataWhenLoadImageCheck.setSelected(UserConfig.getBoolean(baseName + "ClearDataWhenLoadImage", true));
            clearDataWhenLoadImageCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    if (!isSettingValues) {
                        UserConfig.setBoolean(baseName + "ClearDataWhenLoadImage", clearDataWhenLoadImageCheck.isSelected());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initShapeTab() {
        try {
            tabPane.getTabs().remove(controlsTab);

            shapeTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (!isSettingValues) {
                        pickScope();
                        changedNotify.set(!changedNotify.get());
                    }
                }
            });

            outlineKeepRatioCheck.setSelected(true);

            shapeExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        indicateScope();
                        changedNotify.set(!changedNotify.get());
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
                    indicateScope();
                    changedNotify.set(!changedNotify.get());
                }
            });

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
                    changedNotify.set(!changedNotify.get());
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

            deleteColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());
            saveColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());

            colorController.init(this, baseName + "Color", Color.GOLD);
            colorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    addColor((Color) colorController.rect.getFill());
                    changedNotify.set(!changedNotify.get());
                }
            });

            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        indicateScope();
                        changedNotify.set(!changedNotify.get());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMatchTab() {
        try {
            matchController.changeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        indicateScope();
                        changedNotify.set(!changedNotify.get());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        if (super.afterImageLoaded()) {
            if (UserConfig.getBoolean(baseName + "ClearDataWhenLoadImage", true)) {

                pointsController.isSettingValues = true;
                pointsController.tableData.clear();
                pointsController.isSettingValues = false;

                isSettingValues = true;
                colorsList.getItems().clear();
                isSettingValues = false;

                if (scope != null) {
                    scope.resetParameters();
                }

                outlinesList.getSelectionModel().clearSelection();
            }

            pickScope();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void fitView() {
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
        ImageScopeViewsController.open(this);
        return true;
    }

    @FXML
    @Override
    public void refreshAction() {
        isSettingValues = false;
        indicateScope();
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (scope == null || isSettingValues) {
            return false;
        }
        try {
            switch (scope.getShapeType()) {
                case Matting4:
                case Matting8:
                case Polygon:
                    pointsController.removeLastItem();
                    return true;

            }
            return false;
        } catch (Exception e) {
        }
        return false;
    }

    @FXML
    @Override
    public void clearAction() {
        if (scope == null || isSettingValues) {
            return;
        }
        try {
            switch (scope.getShapeType()) {
                case Matting4:
                case Matting8:
                case Polygon:
                    pointsController.clear();
                    break;

                case Whole:
                    clearColors();
                    break;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, srcImage());
            if (color != null) {
                addColor(color);
                changedNotify.set(!changedNotify.get());
            }
        } else if (event.getClickCount() == 1) {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (addPointWhenClick) {
                    if (scope.getShapeType() == ShapeType.Matting4
                            || scope.getShapeType() == ShapeType.Matting8) {
                        int x = (int) Math.round(p.getX());
                        int y = (int) Math.round(p.getY());
                        isSettingValues = true;
                        pointsController.addPoint(x, y);
                        isSettingValues = false;
                        indicateScope();
                        changedNotify.set(!changedNotify.get());

                    } else if (scope.getShapeType() == ShapeType.Polygon
                            && !maskControlDragged) {
                        maskPolygonData.add(p.getX(), p.getY());
                        maskShapeDataChanged();
                        changedNotify.set(!changedNotify.get());
                    }
                }

            } else if (event.getButton() == MouseButton.SECONDARY) {
                popEventMenu(event, maskShapeMenu(event, currentMaskShapeData(), p));
            }
        }
        maskControlDragged = false;
    }

    @Override
    public void maskShapeDataChanged() {
        try {
            if (isSettingValues || !isValidScope()) {
                return;
            }
            switch (scope.getShapeType()) {
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
            }
            indicateScope();
            changedNotify.set(!changedNotify.get());
        } catch (Exception e) {
            MyBoxLog.error(e);
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

    @FXML
    public void aboutScope() {
        openHtml(HelpTools.aboutImageScope());
    }

}
