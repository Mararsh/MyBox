package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.style.NodeStyleTools;
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
            MyBoxLog.error(e);
        }

    }

    @FXML
    public void saveScope() {
        if (scope == null || scope.getFile() == null || saveScopeButton.isDisabled()) {
            return;
        }
        String name = scopeNameInput.getText().trim();
        if (name.isEmpty()) {
            popError(message("InvalidParameters"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        scope.setName(name);
        if (sourceFile != null) {
            scope.setFile(sourceFile.getAbsolutePath());
        } else {
            scope.setFile("Unknown");
        }
        task = new SingletonCurrentTask<Void>(this) {

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
        start(task);
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

    private boolean showScopeType(ImageScope scope) {
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
            case Circle:
                scopeCircleRadio.setSelected(true);
                break;
            case Ellipse:
                scopeEllipseRadio.setSelected(true);
                break;
            case Polygon:
                scopePolygonRadio.setSelected(true);
                break;
            case Outline:
                scopeOutlineRadio.setSelected(true);
                break;
        }
        return true;

    }

    private boolean showAreaData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        try {
            pointsController.isSettingValues = true;
            pointsController.clearAction();
            pointsController.isSettingValues = true;
            areaExcludedCheck.setSelected(scope.isAreaExcluded());
            switch (scope.getScopeType()) {
                case Matting: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        pointsController.isSettingValues = true;
                        for (IntPoint p : points) {
                            pointsController.tableData.add(new DoublePoint(p.getX(), p.getY()));
                        }
                        pointsController.isSettingValues = true;
                    }
                    return true;
                }
                case Rectangle:
                case Outline:
                    maskRectangleData = scope.getRectangle();
                    return showMaskRectangle();
                case Circle:
                    maskCircleData = scope.getCircle();
                    return showMaskCircle();
                case Ellipse:
                    maskEllipseData = scope.getEllipse();
                    return showMaskEllipse();
                case Polygon: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        pointsController.isSettingValues = true;
                        for (IntPoint p : points) {
                            pointsController.tableData.add(new DoublePoint(p.getX(), p.getY()));
                        }
                        pointsController.isSettingValues = true;
                    }
                    maskPolygonData = scope.getPolygon();
                    return showMaskPolygon();
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    private boolean showColorData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        try {
            colorsList.getItems().clear();
            colorExcludedCheck.setSelected(scope.isColorExcluded());
            switch (scope.getScopeType()) {
                case Color:
                case Rectangle:
                case Circle:
                case Ellipse:
                case Polygon:
                    List<java.awt.Color> colors = scope.getColors();
                    if (colors != null) {
                        List<Color> list = new ArrayList<>();
                        for (java.awt.Color color : colors) {
                            list.add(ColorConvertTools.converColor(color));
                        }
                        colorsList.getItems().addAll(list);
                        colorsList.getSelectionModel().selectLast();
                    }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    private void showMatchType(ImageScope scope) {
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
            MyBoxLog.debug(e);
        }

    }

    private void showDistanceValue(ImageScope scope) {
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
            MyBoxLog.debug(e);
        }
    }

}
