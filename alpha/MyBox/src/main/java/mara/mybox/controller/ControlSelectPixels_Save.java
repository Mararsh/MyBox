package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_Save extends ControlSelectPixels_mask {

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
        scope = inScope.cloneValues();
        showScopeType(scope);
        showAreaData(scope);
        showColorData(scope);
        matchController.show(scope);
        setControls();
        showScope();
    }

    private boolean showScopeType(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        isSettingValues = true;
        switch (scope.getScopeType()) {
            case Matting:
                scopeMattingRadio.setSelected(true);
                break;
            case Colors:
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
        isSettingValues = false;
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
            isSettingValues = true;
            colorsList.getItems().clear();
            colorExcludedCheck.setSelected(scope.isColorExcluded());
            eightNeighborCheck.setSelected(scope.isEightNeighbor());
            switch (scope.getScopeType()) {
                case Colors:
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
            isSettingValues = false;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            isSettingValues = false;
            return false;
        }
    }

}
