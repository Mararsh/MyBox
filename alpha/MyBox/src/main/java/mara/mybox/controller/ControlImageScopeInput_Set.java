package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Polygon;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScopeInput_Set extends ControlImageScopeInput_Outline {

    public void checkScopeType() {
        if (isSettingValues) {
            return;
        }
        try {
            clearScope();
            if (scopeTypeGroup.getSelectedToggle() == null) {
                scope.setScopeType(ImageScope.ScopeType.Rectangle);
            } else {
                if (scopeMattingRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);

                } else if (scopeRectangleRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);

                } else if (scopeCircleRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Circle);

                } else if (scopeEllipseRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Ellipse);

                } else if (scopePolygonRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Polygon);

                } else if (scopeColorRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Color);

                } else if (scopeOutlineRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                }
            }
            setScopeValues();

            setScopeControls();

            indicateScope();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void setScopeValues() {
        try {
            if (srcImage() == null || scope == null) {
                return;
            }
            scope.setMaskOpacity(maskOpacity);
            scope.setMaskColor(maskColor);
            pickColors();
            isSettingValues = true;
            switch (scope.getScopeType()) {
                case Matting:
                    scope.clearPoints();
                    for (DoublePoint p : pointsController.tableData) {
                        scope.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
                    }
                    checkMatchType();
                    break;

                case Rectangle:
                    showMaskRectangle();
                    scope.setRectangle(maskRectangleData.copy());
                    checkMatchType();
                    break;

                case Circle:
                    showMaskCircle();
                    scope.setCircle(maskCircleData.copy());
                    checkMatchType();
                    break;

                case Ellipse:
                    showMaskEllipse();
                    scope.setEllipse(maskEllipseData.copy());
                    checkMatchType();
                    break;

                case Polygon:
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.setAll(pointsController.getPoints());
                    showMaskPolygon();
                    scope.setPolygon(maskPolygonData.copy());
                    checkMatchType();
                    break;

                case Color:
                    checkMatchType();
                    break;

                case Outline:
                    if (!outlinesList.getItems().isEmpty()) {
                        outlinesList.getSelectionModel().select(null);
                        outlinesList.getSelectionModel().select(0);
                    }
                    break;
                default:
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void clearScope() {
        try {
            isSettingValues = true;
            clearMask();
            imageView.setImage(srcImage());
            if (srcImage != null) {
                scope = new ImageScope(srcImage);
                if (sourceFile != null) {
                    scope.setFile(sourceFile.getAbsolutePath());
                }
            } else {
                scope = new ImageScope();
            }
            scope.setMaskOpacity(maskOpacity);
            scope.setMaskColor(maskColor);
            resetShapeOptions();

            opPane.getChildren().clear();
            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getEditor().setStyle(null);
            areaExcludedCheck.setSelected(false);
            colorExcludedCheck.setSelected(false);
            outlinesList.getSelectionModel().select(null);
            pickColorCheck.setSelected(false);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean checkMatchType() {
        if (!isValidScope()) {
            return false;
        }
        try {
            int max = 255, step = 10;
            RadioButton selected = (RadioButton) matchGroup.getSelectedToggle();
            if (selected.equals(colorRGBRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Color);
                if (squareRootCheck.isSelected()) {
                    max = 255 * 255;
                    step = 100;
                }

            } else if (selected.equals(colorRedRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Red);

            } else if (selected.equals(colorGreenRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Green);

            } else if (selected.equals(colorBlueRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Blue);

            } else if (selected.equals(colorSaturationRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Saturation);
                max = 100;

            } else if (selected.equals(colorHueRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Hue);
                max = 360;

            } else if (selected.equals(colorBrightnessRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Brightness);
                max = 100;

            }

            NodeStyleTools.setTooltip(scopeDistanceSelector, new Tooltip("0~" + max));
            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += step) {
                vList.add(i + "");
            }
            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getItems().addAll(vList);
            scopeDistanceSelector.setValue("20");

            return checkDistanceValue();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean checkDistanceValue() {
        if (!isValidScope() || scope.getColorScopeType() == null
                || scopeDistanceSelector.getSelectionModel().getSelectedItem() == null) {
            return false;
        }
        boolean valid = true;
        try {
            int distance = Integer.parseInt(scopeDistanceSelector.getValue());
            switch (scope.getColorScopeType()) {
                case Hue:
                    if (distance >= 0 && distance <= 360) {
                        scope.setHsbDistance(distance / 360.0f);
                    } else {
                        valid = false;
                    }
                    break;
                case Brightness:
                case Saturation:
                    if (distance >= 0 && distance <= 100) {
                        scope.setHsbDistance(distance / 100.0f);
                    } else {
                        valid = false;
                    }
                    break;
                default:
                    if (squareRootCheck.isSelected() && colorRGBRadio.isSelected()) {
                        if (distance >= 0 && distance <= 255 * 255) {
                            scope.setColorDistanceSquare(distance);
                        } else {
                            valid = false;
                        }
                    } else {
                        if (distance >= 0 && distance <= 255) {
                            scope.setColorDistance(distance);
                        } else {
                            valid = false;
                        }
                    }
            }
        } catch (Exception e) {
            valid = false;
        }
        if (valid) {
            ValidationTools.setEditorNormal(scopeDistanceSelector);
        } else {
            ValidationTools.setEditorBadStyle(scopeDistanceSelector);
        }
        return valid;
    }

    protected void setScopeControls() {
        try {
            tabPane.getTabs().clear();
            opPane.getChildren().clear();
            areaBox.getChildren().clear();
            scopeTips.setText("");
            if (srcImage() == null || scope == null) {
                return;
            }
            isSettingValues = true;
            String tips = "";
            if (scope.getScopeType() == null) {
                scope.setScopeType(ScopeType.Rectangle);
            }
            switch (scope.getScopeType()) {
                case Matting:
                    tips = message("ScopeMattingTips");
                    tabPane.getTabs().addAll(areaTab, matchTab, optionsTab);
                    tabPane.getSelectionModel().select(areaTab);
                    opPane.getChildren().addAll(popAnchorCheck, addPointCheck, anchorCheck, withdrawPointButton, operationsButton);
                    areaBox.getChildren().add(pointsBox);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Rectangle:
                    tips = message("ScopeDragMoveTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab);
                    tabPane.getSelectionModel().select(areaTab);
                    opPane.getChildren().addAll(popAnchorCheck, anchorCheck, operationsButton);
                    areaBox.getChildren().addAll(rectangleBox, goScopeButton);
                    rectLeftTopXInput.setText(scale(maskRectangleData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskRectangleData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskRectangleData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskRectangleData.getMaxY(), 2) + "");
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case Circle:
                    tips = message("ScopeDragMoveTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab);
                    tabPane.getSelectionModel().select(areaTab);
                    opPane.getChildren().addAll(popAnchorCheck, anchorCheck, operationsButton);
                    areaBox.getChildren().addAll(circleBox, goScopeButton);
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    break;

                case Ellipse:
                    tips = message("ScopeDragMoveTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().addAll(rectangleBox, goScopeButton);
                    opPane.getChildren().addAll(popAnchorCheck, anchorCheck, operationsButton);
                    rectLeftTopXInput.setText(scale(maskEllipseData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskEllipseData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskEllipseData.getMaxX(), 2) + "");
                    rightBottomYInput.setText(scale(maskEllipseData.getMaxY(), 2) + "");
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case Polygon:
                    tips = message("ScopePolygonTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab);
                    tabPane.getSelectionModel().select(areaTab);
                    opPane.getChildren().addAll(popAnchorCheck, addPointCheck, anchorCheck, withdrawPointButton, operationsButton);
                    areaBox.getChildren().addAll(pointsBox);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Color:
                    tips = message("ScopeColorTips");
                    tabPane.getTabs().addAll(colorsTab, matchTab, optionsTab);
                    tabPane.getSelectionModel().select(colorsTab);
                    break;

                case Outline:
                    tips = message("ScopeOutlineTips");
                    tabPane.getTabs().addAll(pixTab, optionsTab);
                    tabPane.getSelectionModel().select(pixTab);
                    if (outlinesList.getItems().isEmpty()) {
                        initPixTab();
                    }
                    opPane.getChildren().addAll(operationsButton, anchorCheck);
                    break;

            }
            scopeTips.setText(tips);
            areaBox.applyCss();
            areaBox.layout();
            refreshStyle(tabPane);
            isSettingValues = false;

            showNotify.set(!showNotify.get());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

}
