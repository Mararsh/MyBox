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
import mara.mybox.data.DoublePolygon;
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
                hideLeftPane();

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
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.setAll(pointsController.getPoints());

                } else if (scopeColorRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Color);

                } else if (scopeOutlineRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Outline);

                }

                showLeftPane();
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

    @Override
    public synchronized void showScope() {
        if (scope.getScopeType() == ImageScope.ScopeType.Outline) {
            loadOutlineSource(scope.getOutlineSource());
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
