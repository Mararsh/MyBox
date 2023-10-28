package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 *
 * ImageManufactureScopeController < ImageManufactureScopeController_Save <
 * ImageManufactureScopeController_Set < ImageManufactureScopeController_Outline
 * < ImageManufactureScopeController_Colors <
 * ImageManufactureScopeController_Points < ImageManufactureScopeController_Area
 * ImageManufactureScopeController_Base < ImageViewerController
 */
public class ControlImageScopeInput extends ControlImageScopeInput_Save {

    @Override
    public void initControls() {
        try {
            super.initControls();

            initSplitPane();
            initScopeView();
            initSetBox();
            initColorsTab();
            initMatchTab();

            tableColor = new TableColor();
            popShapeMenu = true;
            supportPath = false;
            shapeStyle = null;

            pointsController.tableData.addListener(new ListChangeListener<DoublePoint>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends DoublePoint> c) {
                    pickPoints();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(eightNeighborCheck, new Tooltip(message("EightNeighborCheckComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initSplitPane() {
        try {
            String mv = UserConfig.getString(baseName + "ScopePanePosition", "0.5");
            splitPane.setDividerPositions(Double.valueOf(mv));

            splitPane.getDividers().get(0).positionProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        if (Math.abs(newValue.doubleValue() - oldValue.doubleValue()) * splitPane.getWidth() > 5) {
                            UserConfig.setString(baseName + "ScopePanePosition", newValue.doubleValue() + "");
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initScopeView() {
        try {
            imageView.toBack();
            showNotify = new SimpleBooleanProperty(false);

            scopeTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkScopeType();
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

    public void initSetBox() {
        try {

            areaExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    scope.setAreaExcluded(newValue);
                    if (scope.getScopeType() == ScopeType.Outline) {
                        makeOutline();
                    } else {
                        indicateScope();
                    }
                }
            });

            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    scope.setColorExcluded(newValue);
                    indicateScope();
                }
            });

            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    indicateScope();
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
                    if (isSettingValues) {
                        return;
                    }
                    if (checkMatchType()) {
                        indicateScope();
                    }
                }
            });

            int colorDistance = UserConfig.getInt(baseName + "ColorDistance", 20);
            colorDistance = colorDistance <= 0 ? 20 : colorDistance;
            scopeDistanceSelector.setValue(colorDistance + "");
            scopeDistanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (checkDistanceValue()) {
                        indicateScope();
                    }
                }
            });

            eightNeighborCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    scope.setEightNeighbor(eightNeighborCheck.isSelected());
                    if (checkMatchType()) {
                        indicateScope();
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
                    checkMatchType();
                    indicateScope();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseImageController parent) {
        try {
            this.parentController = parent;
            imageController = parent;

            loadImage(imageController.sourceFile,
                    imageController.imageInformation,
                    imageController.image,
                    false);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        if (super.afterImageLoaded()) {
            srcImage = image;
            checkScopeType();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (!colorsTab.isDisable() && pickColorCheck.isSelected()) {
            tabPane.getSelectionModel().select(colorsTab);
            Color color = ImageViewTools.imagePixel(p, imageView);
            if (color != null) {
                addColor(color);
            }
            return;
        }

        if (event.getClickCount() == 1) {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (addPointWhenClick) {
                    if (scope.getScopeType() == ScopeType.Matting) {
                        int x = (int) Math.round(p.getX());
                        int y = (int) Math.round(p.getY());
                        isSettingValues = true;
                        pointsController.addPoint(x, y);
                        isSettingValues = false;
                        scope.addPoint(x, y);
                        indicateScope();

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
    public void mousePressed(MouseEvent event) {
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
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
        if (scope.getScopeType() == ScopeType.Outline) {
            makeOutline();
        } else {
            indicateScope();
        }
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
                    makeOutline();
                    return;
                default:
                    return;
            }
            indicateScope();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
