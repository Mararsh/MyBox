package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(eightNeighborCheck, new Tooltip(Languages.message("EightNeighborCheckComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initSplitPane() {
        try {
            String mv = UserConfig.getString(baseName + "ScopePanePosition", "0.5");
            splitPane.setDividerPositions(Double.parseDouble(mv));

            splitPane.getDividers().get(0).positionProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        if (Math.abs(newValue.doubleValue() - oldValue.doubleValue()) * splitPane.getWidth() > 5) {
                            UserConfig.setString(baseName + "ScopePanePosition", newValue.doubleValue() + "");
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                    Arrays.asList(Languages.message("ScopeTransparency0.5"), Languages.message("ScopeTransparency0"), Languages.message("ScopeTransparency1"),
                            Languages.message("ScopeTransparency0.2"), Languages.message("ScopeTransparency0.8"), Languages.message("ScopeTransparency0.3"),
                            Languages.message("ScopeTransparency0.6"), Languages.message("ScopeTransparency0.7"), Languages.message("ScopeTransparency0.9"),
                            Languages.message("ScopeTransparency0.4"))
            );
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        if (newVal == null) {
                            return;
                        }
                        float f = Float.valueOf(newVal.substring(0, 3));
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

            opacitySelector.getSelectionModel().select(UserConfig.getString(baseName + "ScopeTransparency", Languages.message("ScopeTransparency0.5")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    public void initMatchTab() {
        try {
            matchGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkMatchType();
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
                    indicateScope();
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
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

//    public void initSplitDivider() {
//        try {
//            // https://stackoverflow.com/questions/40707295/how-to-add-listener-to-divider-position?r=SearchResults
//            for (Node node : splitPane.lookupAll(".split-pane-divider")) {
//                node.setOnMouseReleased(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        paneSize();
//                    }
//                });
//            }
//
//        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
//        }
//    }
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
            scopeAllRadio.fire();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        super.viewSizeChanged(change);
        if (change < sizeChangeAware || isSettingValues
                || imageView == null || imageView.getImage() == null
                || scope == null || scope.getScopeType() == null || !scopeView.isVisible()) {
            return;
        }
        // Following handlers can conflict with threads' status changes which must check variables carefully
        switch (scope.getScopeType()) {
            case Operate:
                scopeView.setFitWidth(imageView.getFitWidth());
                scopeView.setFitHeight(imageView.getFitHeight());
                scopeView.setLayoutX(imageView.getLayoutX());
                scopeView.setLayoutY(imageView.getLayoutY());
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
    public void paneSizeChanged(double change) {

    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;

            if (imageChanged) {
                indicateScope();
                drawMaskControls();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
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
            int ix = (int) Math.round(p.getX());
            int iy = (int) Math.round(p.getY());
            scope.addPoint(ix, iy);
            pointsList.getItems().add(ix + "," + iy);
            pointsList.getSelectionModel().selectLast();
            indicateScope();
        } else {
            super.imageClicked(event, p);
            switch (scope.getScopeType()) {
                case Rectangle:
                case RectangleColor:
                    if (!scope.getRectangle().same(maskRectangleData)) {
                        scope.setRectangle(maskRectangleData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Circle:
                case CircleColor:
                    if (!scope.getCircle().same(maskCircleData)) {
                        scope.setCircle(maskCircleData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Ellipse:
                case EllipseColor:
                    if (!scope.getEllipse().same(maskEllipseData)) {
                        scope.setEllipse(maskEllipseData.cloneValues());
                        indicateScope();
                    }
                    break;
                case Polygon:
                case PolygonColor:
                    if (!scope.getPolygon().same(maskPolygonData)) {
                        pointsList.getItems().clear();
                        for (DoublePoint mp : maskPolygonData.getPoints()) {
                            pointsList.getItems().add((int) mp.getX() + "," + (int) mp.getY());
                        }
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

}
