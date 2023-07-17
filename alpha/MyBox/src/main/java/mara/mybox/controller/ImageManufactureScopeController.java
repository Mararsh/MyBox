package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
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
public class ImageManufactureScopeController extends ImageManufactureScopeController_Save {

    @Override
    public void initControls() {
        try {
            super.initControls();

            initSplitPane();
            initScopeView();
            initSetBox();
            initPointsTab();
            initColorsTab();
            initMatchTab();
            initSaveTab();

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
            scopeView.visibleProperty().bind(setBox.visibleProperty());
            imageView.toBack();

            scopeTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkScopeType();
                }
            });

            opacitySelector.getItems().addAll(
                    Arrays.asList(message("ScopeTransparency0.5"), message("ScopeTransparency0"), message("ScopeTransparency1"),
                            message("ScopeTransparency0.2"), message("ScopeTransparency0.8"), message("ScopeTransparency0.3"),
                            message("ScopeTransparency0.6"), message("ScopeTransparency0.7"), message("ScopeTransparency0.9"),
                            message("ScopeTransparency0.4"))
            );
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        if (newVal == null) {
                            return;
                        }
                        float f = Float.parseFloat(newVal.substring(0, 3));
                        if (f >= 0 && f <= 1.0) {
                            opacity = 1 - f;
                            scopeView.setOpacity(opacity);
                            ValidationTools.setEditorNormal(opacitySelector);
                            UserConfig.setString(baseName + "ScopeTransparency", newVal);
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            opacitySelector.getSelectionModel().select(UserConfig.getString(baseName + "ScopeTransparency", message("ScopeTransparency0.5")));

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

    public void setParameters(ImageManufactureController parent) {
        try {
            this.parentController = parent;
            imageController = parent;
            scopesSavedController = imageController.scopeSavedController;
            sourceFile = imageController.sourceFile;
            imageInformation = imageController.imageInformation;
            image = imageController.image;
            tableColor = new TableColor();

            refreshStyle();

            loadImage(sourceFile, imageInformation, imageController.image, parent.imageChanged);
            checkScopeType();
            scopeAllRadio.setSelected(true);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        super.viewSizeChanged(change);
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null
                || scope == null || scope.getScopeType() == null || !scopeView.isVisible()) {
            return;
        }
        // Following handlers can conflict with threads' status changes which must check variables carefully
        switch (scope.getScopeType()) {
            case Operate:
                break;
            case Outline:
                makeOutline();
                break;
            default:
                indicateScope();
                break;
        }
    }

    @Override
    public void refinePane() {
        super.refinePane();
        scopeView.setFitWidth(imageView.getFitWidth());
        scopeView.setFitHeight(imageView.getFitHeight());
        scopeView.setLayoutX(imageView.getLayoutX());
        scopeView.setLayoutY(imageView.getLayoutY());
    }

    @Override
    public void paneSizeChanged(double change) {
        refinePane();
        redrawMaskShapes();
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;

            if (imageChanged) {
                indicateScope();
                redrawMaskShapes();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
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
        if (scope.getScopeType() == ScopeType.Matting) {
            int x = (int) Math.round(p.getX());
            int y = (int) Math.round(p.getY());
            pointsController.isSettingValues = true;
            pointsController.tableData.add(new DoublePoint(x, y));
            pointsController.isSettingValues = false;
            scope.addPoint(x, y);
            indicateScope();
        } else {
            super.paneClicked(event, p);
            switch (scope.getScopeType()) {
                case Rectangle:
                    if (!scope.getRectangle().same(maskRectangleData)) {
                        scope.setRectangle(maskRectangleData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Circle:
                    if (!scope.getCircle().same(maskCircleData)) {
                        scope.setCircle(maskCircleData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Ellipse:
                    if (!scope.getEllipse().same(maskEllipseData)) {
                        scope.setEllipse(maskEllipseData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Polygon:
                    if (!scope.getPolygon().same(maskPolygonData)) {
                        pointsController.isSettingValues = true;
                        pointsController.tableData.clear();
                        for (DoublePoint d : maskPolygonData.getPoints()) {
                            pointsController.tableData.add(
                                    new DoublePoint(Math.round(d.getX()), Math.round(d.getY())));
                        }
                        pointsController.isSettingValues = false;
                        scope.setPolygon(maskPolygonData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Outline:
                    if (!scope.getRectangle().same(maskRectangleData)) {
                        scope.setRectangle(maskRectangleData.cloneValues());
                        makeOutline();
                    }
                    break;
            }
        }

    }

    @FXML
    @Override
    public void refreshAction() {
        isSettingValues = false;
        if (task != null) {
            task.cancel();
        }
        viewSizeChanged(sizeChangeAware + 1);
    }
}
