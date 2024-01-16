package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.ImageItem;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_Save extends ControlSelectPixels_mask {

    public void loadScope(ImageScope inScope) {
        scope = inScope != null ? inScope.cloneValues() : new ImageScope();
        File file = null;
        if (scope.getFile() != null) {
            file = new File(scope.getFile());
        }
        if (file == null || !file.exists()) {
            file = ImageItem.exampleImageFile();
        }
        scope.setFile(file.toString());
        sourceFileChanged(file);
    }

    public void loadScope() {
        if (scope == null || scope.getScopeType() == null) {
            applyScope();
            return;
        }
        clearControls();
        isSettingValues = true;
        showScopeType(scope);
        showAreaData(scope);
        showColorData(scope);
        isSettingValues = false;
        matchController.show(scope);
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
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
