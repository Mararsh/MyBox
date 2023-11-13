package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScopeInput_Save extends ControlImageScopeInput_Apply {

    @FXML
    public void saveScope() {
//        if (finalScope() == null) {
//            return;
//        }
//        String name = scopeNameInput.getText().trim();
//        if (name.isEmpty()) {
//            popError(message("InvalidParameters"));
//            return;
//        }
//        scope.setName(name);
//        if (task != null) {
//            task.cancel();
//        }
//        task = new SingletonCurrentTask<Void>(this) {
//
//            @Override
//            protected boolean handle() {
//                TableImageScope.write(scope);
//                return true;
//            }
//
//            @Override
//            protected void whenSucceeded() {
//                popSaved();
//            }
//        };
//        start(task);
    }

    public void loadScope(ImageScope inScope) {
        if (inScope == null || inScope.getScopeType() == null) {
            applyScope();
            return;
        }
        clearControls();
        scope = inScope;
        isSettingValues = true;
        showScopeType(scope);
        showAreaData(scope);
        showColorData(scope);
        showMatchType(scope);
        showDistanceValue(scope);
        eightNeighborCheck.setSelected(scope.isEightNeighbor());
        isSettingValues = false;
        setControls();
        showScope();
    }

    private boolean showScopeType(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        switch (scope.getScopeType()) {
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
            pointsController.tableData.clear();
            pointsController.isSettingValues = false;
            areaExcludedCheck.setSelected(scope.isAreaExcluded());
            switch (scope.getScopeType()) {
                case Matting:
                    pointsController.loadIntList(scope.getPoints());
                    return true;
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
                    pointsController.loadIntList(scope.getPoints());
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
