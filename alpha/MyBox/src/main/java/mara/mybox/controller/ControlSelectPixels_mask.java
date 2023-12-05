package mara.mybox.controller;

import javafx.scene.control.Tab;
import javafx.scene.image.Image;
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
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_mask extends ControlSelectPixels_Outline {

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

            if (scopeTypeGroup.getSelectedToggle() == null
                    || scopeWholeRadio.isSelected()) {
                scope.setScopeType(ImageScope.ScopeType.Whole);

            } else {

                if (scopeMattingRadio.isSelected()) {
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
            ScopeType type = scope.getScopeType();
            if (type == null) {
                type = ScopeType.Whole;
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
                    opBox.getChildren().setAll(shapeButton, withdrawButton, clearDataWhenLoadImageCheck);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    addPointCheck.setSelected(true);
                    break;

                case Rectangle:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(rectangleBox, goScopeButton);
                    opBox.getChildren().setAll(shapeButton, pickColorBox);
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
                    opBox.getChildren().setAll(shapeButton, pickColorBox);
                    isSettingValues = true;
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    isSettingValues = false;
                    break;

                case Ellipse:
                    tabPane.getTabs().setAll(areaTab, colorsTab, matchTab);
                    areaBox.getChildren().setAll(rectangleBox, goScopeButton);
                    opBox.getChildren().setAll(shapeButton, pickColorBox);
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
                    opBox.getChildren().setAll(shapeButton, withdrawButton, pickColorBox, addPointCheck, clearDataWhenLoadImageCheck);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Color:
                    tabPane.getTabs().setAll(colorsTab, matchTab);
                    opBox.getChildren().setAll(pickColorBox, clearDataWhenLoadImageCheck);
                    showLeftPane();
                    break;

                case Outline:
                    tabPane.getTabs().setAll(pixTab);
                    showLeftPane();
                    break;

            }

            pickColorCheck.setSelected(type == ScopeType.Color);
            handleTransparentCheck.setVisible(type != ScopeType.Outline);

            if (selectedTab != null && tabPane.getTabs().contains(selectedTab)) {
                tabPane.getSelectionModel().select(selectedTab);
            }

            matchController.setDistanceValue(scope);

            refreshStyle(tabPane);
            refreshStyle(opBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearControls() {
        try {
            clearMaskShapes();
            image = srcImage();
            imageView.setRotate(0);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void showScope() {
        if (scope.getScopeType() == ImageScope.ScopeType.Outline) {
            indicateOutline();
            return;
        }
        if (pickScopeValues() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image maskImage;

            @Override
            protected boolean handle() {
                try {
                    maskImage = maskImage();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return maskImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                image = maskImage;
                imageView.setImage(maskImage);
                if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
                    drawMattingPoints();
                } else {
                    drawMaskShape();
                }
                showNotify.set(!showNotify.get());
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, viewBox);
    }

    public void drawMattingPoints() {
        try {
            clearMaskAnchors();
            double xRatio = viewXRatio();
            double yRatio = viewYRatio();
            for (int i = 0; i < scope.getPoints().size(); i++) {
                IntPoint p = scope.getPoints().get(i);
                double x = p.getX() * xRatio;
                double y = p.getY() * yRatio;
                addMaskAnchor(i, new DoublePoint(p.getX(), p.getY()), x, y);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
