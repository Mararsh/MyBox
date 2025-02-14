package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.ImageScope.ShapeType;
import static mara.mybox.image.data.ImageScope.ShapeType.Circle;
import static mara.mybox.image.data.ImageScope.ShapeType.Ellipse;
import static mara.mybox.image.data.ImageScope.ShapeType.Matting4;
import static mara.mybox.image.data.ImageScope.ShapeType.Outline;
import static mara.mybox.image.data.ImageScope.ShapeType.Polygon;
import static mara.mybox.image.data.ImageScope.ShapeType.Rectangle;
import static mara.mybox.image.data.ImageScope.ShapeType.Whole;
import mara.mybox.image.tools.ColorConvertTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScope_Load extends ControlImageScope_Set {

    public void pickScope() {
        if (isSettingValues) {
            return;
        }
        try {
            resetScope();
            if (shapeTypeGroup.getSelectedToggle() == null
                    || wholeRadio.isSelected()) {
                scope.setShapeType(ImageScope.ShapeType.Whole);

            } else {
                if (matting4Radio.isSelected()) {
                    scope.setShapeType(ImageScope.ShapeType.Matting4);

                } else if (matting8Radio.isSelected()) {
                    scope.setShapeType(ImageScope.ShapeType.Matting8);

                } else if (rectangleRadio.isSelected()) {
                    if (maskRectangleData == null) {
                        if (scope.getRectangle() != null) {
                            maskRectangleData = scope.getRectangle().copy();
                        } else {
                            setMaskRectangleDefaultValues();
                        }
                    }
                    scope.setShapeType(ImageScope.ShapeType.Rectangle);

                } else if (circleRadio.isSelected()) {
                    if (maskCircleData == null) {
                        if (scope.getCircle() != null) {
                            maskCircleData = scope.getCircle().copy();
                        } else {
                            setMaskCircleDefaultValues();
                        }
                    }
                    scope.setShapeType(ImageScope.ShapeType.Circle);

                } else if (ellipseRadio.isSelected()) {
                    if (maskEllipseData == null) {
                        if (scope.getEllipse() != null) {
                            maskEllipseData = scope.getEllipse().copy();
                        } else {
                            setMaskEllipseDefaultValues();
                        }
                    }
                    scope.setShapeType(ImageScope.ShapeType.Ellipse);

                } else if (polygonRadio.isSelected()) {
                    if (maskPolygonData == null) {
                        if (scope.getPolygon() != null) {
                            maskPolygonData = scope.getPolygon().copy();
                        }
                    }
                    scope.setShapeType(ImageScope.ShapeType.Polygon);

                } else if (outlineRadio.isSelected()) {
                    if (maskRectangleData == null) {
                        if (scope.getRectangle() != null) {
                            maskRectangleData = scope.getRectangle().copy();
                        } else {
                            setMaskRectangleDefaultValues();
                        }
                    }
                    scope.setShapeType(ImageScope.ShapeType.Outline);

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
        if (inScope == null || inScope.getShapeType() == null) {
            pickScope();
            return;
        }
        resetScope();
        isSettingValues = true;
        showShapeType(inScope);
        showShapeData(inScope);
        showColorData(inScope);
        isSettingValues = false;
        matchController.loadValuesFrom(inScope);
        setControls();
        needFixSize = true;
        indicateScope();
    }

    private boolean showShapeType(ImageScope inScope) {
        if (inScope == null || inScope.getShapeType() == null) {
            return false;
        }
        ShapeType type = inScope.getShapeType();
        if (type == null) {
            type = ShapeType.Whole;
        }
        scope.setShapeType(type);
        switch (type) {
            case Whole:
                wholeRadio.setSelected(true);
                break;
            case Matting4:
                matting4Radio.setSelected(true);
                break;
            case Matting8:
                matting8Radio.setSelected(true);
                break;
            case Rectangle:
                rectangleRadio.setSelected(true);
                break;
            case Circle:
                circleRadio.setSelected(true);
                break;
            case Ellipse:
                ellipseRadio.setSelected(true);
                break;
            case Polygon:
                polygonRadio.setSelected(true);
                break;
            case Outline:
                outlineRadio.setSelected(true);
                break;
        }
        return true;
    }

    private boolean showShapeData(ImageScope inScope) {
        if (inScope == null || inScope.getShapeType() == null) {
            return false;
        }
        try {
            pointsController.isSettingValues = true;
            pointsController.tableData.clear();
            pointsController.isSettingValues = false;
            shapeExcludedCheck.setSelected(inScope.isShapeExcluded());
            switch (inScope.getShapeType()) {
                case Matting4:
                case Matting8:
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
        if (inScope == null || inScope.getShapeType() == null) {
            return false;
        }
        try {
            colorsList.getItems().clear();
            colorExcludedCheck.setSelected(inScope.isColorExcluded());
            List<java.awt.Color> colors = inScope.getColors();
            if (colors != null) {
                List<Color> list = new ArrayList<>();
                for (java.awt.Color color : colors) {
                    list.add(ColorConvertTools.converColor(color));
                }
                colorsList.getItems().addAll(list);
                colorsList.getSelectionModel().selectLast();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    protected void setControls() {
        try {
            shapeBox.getChildren().clear();
            shapeOperationsPane.getChildren().clear();
            if (image == null || scope == null) {
                return;
            }
            ShapeType type = scope.getShapeType();
            if (type == null) {
                type = ImageScope.ShapeType.Whole;
                scope.setShapeType(type);
            }
            UserConfig.setBoolean(baseName + "AddPointWhenLeftClick", true);
            switch (type) {
                case Whole:
                    showLeftPane();
                    break;

                case Matting4:
                case Matting8:
                    shapeBox.getChildren().setAll(pointsBox);
                    shapeOperationsPane.getChildren().setAll(shapeButton, clearButton, withdrawButton,
                            clearDataWhenLoadImageCheck);
                    VBox.setVgrow(shapeBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    addPointCheck.setSelected(true);
                    break;

                case Rectangle:
                    shapeBox.getChildren().setAll(rectangleBox, goScopeButton);
                    shapeOperationsPane.getChildren().setAll(shapeButton,
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
                    shapeBox.getChildren().setAll(circleBox, goScopeButton);
                    shapeOperationsPane.getChildren().setAll(shapeButton,
                            shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    showMaskCircle();
                    isSettingValues = true;
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    isSettingValues = false;
                    break;

                case Ellipse:
                    shapeBox.getChildren().setAll(rectangleBox, goScopeButton);
                    shapeOperationsPane.getChildren().setAll(shapeButton,
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
                    shapeBox.getChildren().setAll(pointsBox);
                    shapeOperationsPane.getChildren().setAll(shapeButton, clearButton, withdrawButton,
                            addPointCheck, shapeCanMoveCheck, clearDataWhenLoadImageCheck);
                    VBox.setVgrow(shapeBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    showMaskPolygon();
                    break;

                case Outline:
                    shapeBox.getChildren().setAll(outlineBox);
                    VBox.setVgrow(shapeBox, Priority.ALWAYS);
                    VBox.setVgrow(outlineBox, Priority.ALWAYS);
                    shapeOperationsPane.getChildren().setAll(clearDataWhenLoadImageCheck);
                    showLeftPane();
                    break;

            }

            handleTransparentCheck.setVisible(type != ImageScope.ShapeType.Outline);

            matchController.loadValuesFrom(scope);

            refreshStyle(tabPane);
            refreshStyle(shapeOperationsPane);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetScope() {
        try {
            clearMaskShapes();
            image = srcImage();
            if (scope == null) {
                scope = new ImageScope();
            }
            scope.setImage(image);
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
