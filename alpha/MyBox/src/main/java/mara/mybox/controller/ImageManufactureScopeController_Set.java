package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.tools.DoubleTools.scale;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Set extends ImageManufactureScopeController_Outline {

    public void checkScopeType() {
        if (isSettingValues) {
            return;
        }
        try {
            clearScope();
            if (scopeTypeGroup.getSelectedToggle() == null) {
                scope.setScopeType(ImageScope.ScopeType.All);
            } else {
                if (scopeAllRadio.isSelected()) {
                    scope.setScopeType(ImageScope.ScopeType.All);

                } else if (scopeMattingRadio.isSelected()) {
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
            if (image == null || scope == null) {
                return;
            }
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
                    scope.setRectangle(maskRectangleData.cloneValues());
                    checkMatchType();
                    break;

                case Circle:
                    showMaskCircle();
                    scope.setCircle(maskCircleData.cloneValues());
                    checkMatchType();
                    break;

                case Ellipse:
                    showMaskEllipse();
                    scope.setEllipse(maskEllipseData.cloneValues());
                    checkMatchType();
                    break;

                case Polygon:
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.setAll(pointsController.tableData);
                    showMaskPolygon();
                    scope.setPolygon(maskPolygonData.cloneValues());
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
            if (imageView.getImage() != null) {
                scope = new ImageScope(imageView.getImage());
                if (sourceFile != null) {
                    scope.setFile(sourceFile.getAbsolutePath());
                }
            } else {
                scope = new ImageScope();
            }
            scopeView.setImage(null);
            outlineSource = null;

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
        if (scope == null || matchGroup.getSelectedToggle() == null) {
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
            isSettingValues = true;
            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getItems().addAll(vList);
            scopeDistanceSelector.setValue("20");
            isSettingValues = false;

            return checkDistanceValue();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean checkDistanceValue() {
        if (scope.getColorScopeType() == null
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

    protected void setScopeName() {
        if (scope == null) {
            return;
        }
        String name = scope.getName();
        if (name == null || name.isEmpty()) {
            name = scope.getScopeType() + "_" + DateTools.datetimeToString(new Date());
        }
        scopeNameInput.setText(name);
    }

    protected void setScopeControls() {
        try {
            setBox.setVisible(!scopeWhole());
            tabPane.getTabs().clear();
            areaBox.getChildren().clear();
            scopeTips.setText("");
            NodeStyleTools.removeTooltip(scopeTips);
            if (image == null || scope == null) {
                return;
            }
            isSettingValues = true;
            String tips = "";
            switch (scope.getScopeType()) {
                case All:
                    tips = message("WholeImage");
                    break;
                case Matting:
                    tips = message("ScopeMattingTips");
                    tabPane.getTabs().addAll(areaTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().add(pointsBox);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Rectangle:
                    tips = message("ScopeRectangleColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().addAll(rectangleBox, goScopeButton);
                    rectLeftTopXInput.setText(scale(maskRectangleData.getX(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskRectangleData.getY(), 2) + "");
                    rightBottomXInput.setText(scale(maskRectangleData.getBigX(), 2) + "");
                    rightBottomYInput.setText(scale(maskRectangleData.getBigY(), 2) + "");
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case Circle:
                    tips = message("ScopeCircleColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().addAll(circleBox, goScopeButton);
                    circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
                    circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
                    circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
                    break;

                case Ellipse:
                    tips = message("ScopeEllipseColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().addAll(rectangleBox, goScopeButton);
                    rectLeftTopXInput.setText(scale(maskEllipseData.getX1(), 2) + "");
                    rectLeftTopYInput.setText(scale(maskEllipseData.getY1(), 2) + "");
                    rightBottomXInput.setText(scale(maskEllipseData.getX2(), 2) + "");
                    rightBottomYInput.setText(scale(maskEllipseData.getY2(), 2) + "");
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case Polygon:
                    tips = message("ScopePolygonColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().add(pointsBox);
                    VBox.setVgrow(areaBox, Priority.ALWAYS);
                    VBox.setVgrow(pointsBox, Priority.ALWAYS);
                    break;

                case Color:
                    tips = message("ScopeColorTips");
                    tabPane.getTabs().addAll(colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(colorsTab);
                    break;

                case Outline:
                    tips = message("ScopeOutlineTips");
                    tabPane.getTabs().addAll(pixTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(pixTab);
                    if (outlinesList.getItems().isEmpty()) {
                        initPixTab();
                    }
                    break;

            }
            scopeTips.setText(tips);
            if (!tips.isBlank()) {
                NodeStyleTools.setTooltip(scopeTips, tips);
            }
            setScopeName();
            areaBox.applyCss();
            areaBox.layout();
            refreshStyle(tabPane);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

}
