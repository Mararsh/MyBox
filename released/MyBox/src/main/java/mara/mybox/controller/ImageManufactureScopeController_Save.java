package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.IntPoint;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Save extends ImageManufactureScopeController_Set {

    public void initSaveTab() {
        try {
            saveScopeButton.disableProperty().bind(scopeNameInput.textProperty().isEmpty()
                    .or(scopeDistanceSelector.visibleProperty()
                            .and(scopeDistanceSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    public void saveScope() {
        if (scope == null || scope.getFile() == null || saveScopeButton.isDisabled()) {
            return;
        }
        synchronized (this) {
            String name = scopeNameInput.getText().trim();
            if (name.isEmpty()) {
                popError(message("InvalidParameters"));
                return;
            }
            scope.setName(name);
            if (sourceFile != null) {
                scope.setFile(sourceFile.getAbsolutePath());
            } else {
                scope.setFile("Unknown");
            }
            SingletonTask saveTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    TableImageScope.write(scope);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    scopesSavedController.loadTableData();
                    popSaved();
                }
            };
            start(saveTask, false);

        }
    }

    public void showScope(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return;
        }
        clearScope();
        this.scope = scope;
        setScopeControls();
        isSettingValues = true;
        showScopeType(scope);
        showAreaData(scope);
        showColorData(scope);
        showMatchType(scope);
        showDistanceValue(scope);
        eightNeighborCheck.setSelected(scope.isEightNeighbor());
        isSettingValues = false;
        if (scope.getScopeType() != ImageScope.ScopeType.Outline) {
            indicateScope();
        } else {
            loadOutlineSource(scope.getOutlineSource(), scope.getRectangle());
        }
    }

    public boolean showScopeType(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        switch (scope.getScopeType()) {
            case All:
                scopeTypeGroup.selectToggle(null);
                break;
            case Matting:
                scopeMattingRadio.setSelected(true);
                break;
            case Color:
                scopeColorRadio.setSelected(true);
                break;
            case Rectangle:
                scopeRectangleRadio.setSelected(true);
                break;
            case RectangleColor:
                scopeRectangleColorRadio.setSelected(true);
                break;
            case Circle:
                scopeCircleRadio.setSelected(true);
                break;
            case CircleColor:
                scopeCircleColorRadio.setSelected(true);
                break;
            case Ellipse:
                scopeEllipseRadio.setSelected(true);
                break;
            case EllipseColor:
                scopeEllipseColorRadio.setSelected(true);
                break;
            case Polygon:
                scopePolygonRadio.setSelected(true);
                break;
            case PolygonColor:
                scopePolygonColorRadio.setSelected(true);
                break;
            case Outline:
                scopeOutlineRadio.setSelected(true);
                break;
        }
        return true;

    }

    public boolean showAreaData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        try {
            areaExcludedCheck.setSelected(scope.isAreaExcluded());
            switch (scope.getScopeType()) {
                case Matting: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            pointsList.getItems().add(p.getX() + "," + p.getY());
                        }
                        pointsList.getSelectionModel().selectLast();
                    }
                    return true;
                }
                case Rectangle:
                case RectangleColor:
                case Outline:
                    setMaskRectangleLineVisible(true);
                    maskRectangleData = scope.getRectangle();
                    return drawMaskRectangleLineAsData();
                case Circle:
                case CircleColor:
                    initMaskCircleLine(true);
                    maskCircleData = scope.getCircle();
                    return drawMaskCircleLineAsData();
                case Ellipse:
                case EllipseColor:
                    initMaskEllipseLine(true);
                    maskEllipseData = scope.getEllipse();
                    return drawMaskEllipseLineAsData();
                case Polygon:
                case PolygonColor: {
                    initMaskPolygonLine(true);
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            pointsList.getItems().add(p.getX() + "," + p.getY());
                        }
                        pointsList.getSelectionModel().selectLast();
                    }
                    maskPolygonData = scope.getPolygon();
                    return drawMaskPolygonLineAsData();
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public boolean showColorData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        try {
            colorExcludedCheck.setSelected(scope.isColorExcluded());
            switch (scope.getScopeType()) {
                case Color:
                case RectangleColor:
                case CircleColor:
                case EllipseColor:
                case PolygonColor:
                    List<java.awt.Color> colors = scope.getColors();
                    if (colors != null) {
                        List<Color> list = new ArrayList<>();
                        for (java.awt.Color color : colors) {
                            list.add(ColorConvertTools.converColor(color));
                        }
                        colorsList.getItems().clear();
                        colorsList.getItems().addAll(list);
                        colorsList.getSelectionModel().selectLast();
                    }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    protected void showMatchType(ImageScope scope) {
        try {
            if (scope == null) {
                return;
            }
            switch (scope.getColorScopeType()) {
                case Color:
                    colorRGBRadio.setSelected(true);
                    break;
                case Red:
                    colorRedRadio.setSelected(true);
                    break;
                case Green:
                    colorGreenRadio.setSelected(true);
                    break;
                case Blue:
                    colorBlueRadio.setSelected(true);
                    break;
                case Hue:
                    colorHueRadio.setSelected(true);
                    break;
                case Brightness:
                    colorBrightnessRadio.setSelected(true);
                    break;
                case Saturation:
                    colorSaturationRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected void showDistanceValue(ImageScope scope) {
        try {
            int distance, max = 255;
            switch (scope.getColorScopeType()) {
                case Hue:
                    max = 360;
                    distance = (int) (scope.getHsbDistance() * 360);
                    break;
                case Brightness:
                case Saturation:
                    max = 100;
                    distance = (int) (scope.getHsbDistance() * 100);
                    break;
                default:
                    distance = scope.getColorDistance();
            }
            NodeStyleTools.setTooltip(scopeDistanceSelector, new Tooltip("0~" + max));
            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += 10) {
                vList.add(i + "");
            }
            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getItems().addAll(vList);
            scopeDistanceSelector.getSelectionModel().select(distance + "");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
