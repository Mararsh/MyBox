package mara.mybox.controller;

import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Color;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Matting;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Outline;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Polygon;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoublePolygon;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScopeInput_Apply extends ControlImageScopeInput_Outline {

    public void applyScope() {
        if (isSettingValues) {
            return;
        }
        try {
            clearControls();
            if (initScopeType() == null || pickScopeValues() == null) {
                return;
            }
            setControls();
            showScope();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public ImageScope initScopeType() {
        try {
            if (srcImage() == null || scope == null) {
                return null;
            }
            if (scopeTypeGroup.getSelectedToggle() == null) {
                scope.setScopeType(ImageScope.ScopeType.Whole);
            } else {
                if (scopeWholeRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Whole);

                } else if (scopeMattingRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);

                } else if (scopeRectangleRadio.isSelected()) {
                    showMaskRectangle();
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);

                } else if (scopeCircleRadio.isSelected()) {
                    showMaskCircle();
                    scope.setScopeType(ImageScope.ScopeType.Circle);

                } else if (scopeEllipseRadio.isSelected()) {
                    showMaskEllipse();
                    scope.setScopeType(ImageScope.ScopeType.Ellipse);

                } else if (scopePolygonRadio.isSelected()) {
                    showMaskPolygon();
                    scope.setScopeType(ImageScope.ScopeType.Polygon);
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.setAll(pointsController.getPoints());

                } else if (scopeColorRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Color);

                } else if (scopeOutlineRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Outline);

                }
            }
            return scope;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void setControls() {
        try {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            tabPane.getTabs().clear();
            areaBox.getChildren().clear();
            opBox.getChildren().clear();
            if (srcImage() == null || scope == null) {
                return;
            }
            isSettingValues = true;
            if (scope.getScopeType() == null) {
                scope.setScopeType(ScopeType.Whole);
            }
            UserConfig.setBoolean(baseName + "ImageShapeAddPointWhenLeftClick", true);
            switch (scope.getScopeType()) {
                case Matting:
                    tabPane.getTabs().setAll(areaTab, matchTab);
                    areaBox.getChildren().setAll(eightNeighborCheck, pointsBox);
                    opBox.getChildren().setAll(shapeButton, withdrawButton);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Rectangle:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(rectangleBox, goScopeButton);
                    opBox.getChildren().setAll(shapeButton, pickColorBox);
                    rectLeftTopXInput.setText(scale(maskRectangleData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskRectangleData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskRectangleData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskRectangleData.getMaxY(), 2) + "");
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case Circle:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(circleBox, goScopeButton);
                    opBox.getChildren().setAll(shapeButton, pickColorBox);
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    break;

                case Ellipse:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(rectangleBox, goScopeButton);
                    opBox.getChildren().setAll(shapeButton, pickColorBox);
                    rectLeftTopXInput.setText(scale(maskEllipseData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskEllipseData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskEllipseData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskEllipseData.getMaxY(), 2) + "");
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case Polygon:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(pointsBox);
                    opBox.getChildren().setAll(shapeButton, withdrawButton, pickColorBox, addPointCheck);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Color:
                    tabPane.getTabs().setAll(colorsTab, matchTab);
                    opBox.getChildren().setAll(pickColorBox);
                    break;

                case Outline:
                    tabPane.getTabs().setAll(pixTab);
                    if (outlinesList.getItems().isEmpty()) {
                        initPixTab();
                    }
                    break;

            }
            isSettingValues = false;
            refreshStyle(tabPane);
            refreshStyle(opBox);

            if (selectedTab != null && tabPane.getTabs().contains(selectedTab)) {
                tabPane.getSelectionModel().select(selectedTab);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void clearControls() {
        try {
            isSettingValues = true;
            clearMask();
            image = srcImage();
            imageView.setImage(image);
            if (scope == null) {
                scope = new ImageScope(image);
            }
            if (sourceFile != null) {
                scope.setFile(sourceFile.getAbsolutePath());
            }
            scope.setMaskOpacity(maskOpacity);
            scope.setMaskColor(maskColor);
            resetShapeOptions();

            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getEditor().setStyle(null);
            areaExcludedCheck.setSelected(false);
            colorExcludedCheck.setSelected(false);
            outlinesList.getSelectionModel().select(null);
            isSettingValues = false;

            pickColorCheck.setSelected(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showScope() {
        if (scope.getScopeType() != ImageScope.ScopeType.Outline) {
            indicateScope();
        } else {
            loadOutlineSource(scope.getOutlineSource(), scope.getRectangle());
        }
    }

}
