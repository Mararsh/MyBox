package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Colors;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Matting;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Outline;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Polygon;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Whole;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class BaseImageScope_Load extends BaseImageScope_Values {

    public void pickScope() {
        if (isSettingValues) {
            return;
        }
        try {
            resetScope();
            if (scopeTypeGroup.getSelectedToggle() == null
                    || scopeWholeRadio.isSelected()) {
                scope.setScopeType(ImageScope.ScopeType.Whole);

            } else {
                if (scopeMattingRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);

                } else if (scopeRectangleRadio.isSelected()) {
                    if (maskRectangleData == null) {
                        if (scope.getRectangle() != null) {
                            maskRectangleData = scope.getRectangle().copy();
                        } else {
                            setMaskRectangleDefaultValues();
                        }
                    }
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);

                } else if (scopeCircleRadio.isSelected()) {
                    if (maskCircleData == null) {
                        if (scope.getCircle() != null) {
                            maskCircleData = scope.getCircle().copy();
                        } else {
                            setMaskCircleDefaultValues();
                        }
                    }
                    scope.setScopeType(ImageScope.ScopeType.Circle);

                } else if (scopeEllipseRadio.isSelected()) {
                    if (maskEllipseData == null) {
                        if (scope.getEllipse() != null) {
                            maskEllipseData = scope.getEllipse().copy();
                        } else {
                            setMaskEllipseDefaultValues();
                        }
                    }
                    scope.setScopeType(ImageScope.ScopeType.Ellipse);

                } else if (scopePolygonRadio.isSelected()) {
                    if (maskPolygonData == null) {
                        if (scope.getPolygon() != null) {
                            maskPolygonData = scope.getPolygon().copy();
                        }
                    }
                    scope.setScopeType(ImageScope.ScopeType.Polygon);

                } else if (scopeColorRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Colors);

                } else if (scopeOutlineRadio.isSelected()) {
                    if (maskRectangleData == null) {
                        if (scope.getRectangle() != null) {
                            maskRectangleData = scope.getRectangle().copy();
                        } else {
                            setMaskRectangleDefaultValues();
                        }
                    }
                    scope.setScopeType(ImageScope.ScopeType.Outline);

                }

            }
            setControls();
            indicateScope();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // not apply image file
    public void applyScope(ImageScope inScope) {
        if (inScope == null || inScope.getScopeType() == null) {
            pickScope();
            return;
        }
        resetScope();
        isSettingValues = true;
        showScopeType(inScope);
        showAreaData(inScope);
        showColorData(inScope);
        isSettingValues = false;
        matchController.show(inScope);
        setControls();
        needFixSize = true;
        indicateScope();
    }

    private boolean showScopeType(ImageScope inScope) {
        if (inScope == null || inScope.getScopeType() == null) {
            return false;
        }
        ScopeType type = inScope.getScopeType();
        if (type == null) {
            type = ScopeType.Whole;
        }
        scope.setScopeType(type);
        switch (type) {
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

    private boolean showAreaData(ImageScope inScope) {
        if (inScope == null || inScope.getScopeType() == null) {
            return false;
        }
        try {
            pointsController.isSettingValues = true;
            pointsController.tableData.clear();
            pointsController.isSettingValues = false;
            areaExcludedCheck.setSelected(inScope.isAreaExcluded());
            switch (inScope.getScopeType()) {
                case Matting:
                    pointsController.loadIntList(inScope.getPoints());
                    return true;
                case Rectangle:
                case Outline:
                    MyBoxLog.console(inScope.getRectangle() != null);
                    if (inScope.getRectangle() != null) {
                        maskRectangleData = inScope.getRectangle().copy();
                    }
                case Circle:
                    if (inScope.getCircle() != null) {
                        maskCircleData = inScope.getCircle().copy();
                    }
                case Ellipse:
                    if (inScope.getEllipse() != null) {
                        maskEllipseData = inScope.getEllipse().copy();
                    }
                case Polygon: {
                    if (inScope.getPolygon() != null) {
                        maskPolygonData = inScope.getPolygon().copy();
                        pointsController.loadList(maskPolygonData.getPoints());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    private boolean showColorData(ImageScope inScope) {
        if (inScope == null || inScope.getScopeType() == null) {
            return false;
        }
        try {
            colorsList.getItems().clear();
            colorExcludedCheck.setSelected(inScope.isColorExcluded());
            eightNeighborCheck.setSelected(inScope.isEightNeighbor());
            switch (inScope.getScopeType()) {
                case Colors:
                case Rectangle:
                case Circle:
                case Ellipse:
                case Polygon:
                    List<java.awt.Color> colors = inScope.getColors();
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

    protected void setControls() {
        try {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            tabPane.getTabs().clear();
            areaBox.getChildren().clear();
            opPane.getChildren().clear();
            if (image == null || scope == null) {
                return;
            }
            ScopeType type = scope.getScopeType();
            if (type == null) {
                type = ImageScope.ScopeType.Whole;
                scope.setScopeType(type);
            }
            UserConfig.setBoolean(baseName + "AddPointWhenLeftClick", true);
            switch (type) {
                case Whole:
                    hideLeftPane();
                    break;
                case Matting:
                    tabPane.getTabs().setAll(areaTab, matchTab);
                    areaBox.getChildren().setAll(eightNeighborCheck, pointsBox);
                    opPane.getChildren().setAll(shapeButton, clearButton, withdrawButton,
                            clearDataWhenLoadImageCheck);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    addPointCheck.setSelected(true);
                    break;

                case Rectangle:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(rectangleBox, goScopeButton);
                    opPane.getChildren().setAll(shapeButton, pickColorBox,
                            shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    showMaskRectangle();
                    isSettingValues = true;
                    rectLeftTopXInput.setText(scale(maskRectangleData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskRectangleData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskRectangleData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskRectangleData.getMaxY(), 2) + "");
                    isSettingValues = false;
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case Circle:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(circleBox, goScopeButton);
                    opPane.getChildren().setAll(shapeButton, pickColorBox,
                            shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    showMaskCircle();
                    isSettingValues = true;
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    isSettingValues = false;
                    break;

                case Ellipse:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(rectangleBox, goScopeButton);
                    opPane.getChildren().setAll(shapeButton, pickColorBox,
                            shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    showMaskEllipse();
                    isSettingValues = true;
                    rectLeftTopXInput.setText(scale(maskEllipseData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskEllipseData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskEllipseData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskEllipseData.getMaxY(), 2) + "");
                    isSettingValues = false;
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case Polygon:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(pointsBox);
                    opPane.getChildren().setAll(shapeButton, clearButton, withdrawButton,
                            pickColorBox, addPointCheck, shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    showMaskPolygon();
                    break;

                case Colors:
                    tabPane.getTabs().setAll(colorsTab, matchTab);
                    opPane.getChildren().setAll(clearButton, withdrawButton, pickColorBox,
                            shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    showLeftPane();
                    break;

                case Outline:
                    tabPane.getTabs().setAll(pixTab);
                    opPane.getChildren().setAll(clearDataWhenLoadImageCheck);
                    showLeftPane();
                    break;

            }

            pickColorCheck.setSelected(type == ImageScope.ScopeType.Colors);
            handleTransparentCheck.setVisible(type != ImageScope.ScopeType.Outline);

            if (selectedTab != null && tabPane.getTabs().contains(selectedTab)) {
                tabPane.getSelectionModel().select(selectedTab);
            }

            matchController.setDistanceValue(scope);

            refreshStyle(tabPane);
            refreshStyle(opPane);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetScope() {
        try {
            clearMaskShapes();
            image = srcImage();
            if (scope == null) {
                scope = new ImageScope(image);
            } else {
                scope.setImage(image);
            }
            imageView.setRotate(0);
            imageView.setImage(image);

            if (sourceFile != null && sourceFile.exists()) {
                scope.setFile(sourceFile.getAbsolutePath());
            } else {
                scope.setFile("Unknown");
            }
            scope.setMaskOpacity(maskOpacity);
            scope.setMaskColor(maskColor);
            resetShapeOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
