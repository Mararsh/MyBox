package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureScopeController_Set extends ImageManufactureScopeController_Outline {

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
            setBox.setVisible(scope != null && scope.getScopeType() != ImageScope.ScopeType.All);
            areaTab.setDisable(true);
            pointsTab.setDisable(true);
            colorsTab.setDisable(true);
            matchTab.setDisable(true);
            pixTab.setDisable(true);
            scopeTips.setText("");
            if (image == null || scope == null) {
                saveTab.setDisable(true);
                return;
            }
            saveTab.setDisable(false);
            isSettingValues = true;
            switch (scope.getScopeType()) {
                case All:
                    scopeTips.setText(Languages.message("WholeImage"));
                    saveTab.setDisable(true);
                    break;
                case Matting:
                    scopeTips.setText(Languages.message("ScopeMattingTips"));
                    pointsTab.setDisable(false);
                    matchTab.setDisable(false);
                    tabPane.getSelectionModel().select(pointsTab);
                    break;

                case Rectangle:
                    scopeTips.setText(Languages.message("ScopeRectangleTips"));
                    areaTab.setDisable(false);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    rectangleLabel.setText(Languages.message("Rectangle"));
                    tabPane.getSelectionModel().select(areaTab);
                    break;

                case Circle:
                    scopeTips.setText(Languages.message("ScopeCircleTips"));
                    areaTab.setDisable(false);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(circleBox);
                    tabPane.getSelectionModel().select(areaTab);
                    break;

                case Ellipse:
                    scopeTips.setText(Languages.message("ScopeEllipseTips"));
                    areaTab.setDisable(false);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    rectangleLabel.setText(Languages.message("Ellipse"));
                    tabPane.getSelectionModel().select(areaTab);
                    break;

                case Polygon:
                    pointsTab.setDisable(false);
                    scopeTips.setText(Languages.message("ScopePolygonTips"));
                    tabPane.getSelectionModel().select(pointsTab);
                    break;

                case Color:
                    scopeTips.setText(Languages.message("ScopeColorTips"));
                    colorsTab.setDisable(false);
                    matchTab.setDisable(false);
                    tabPane.getSelectionModel().select(colorsTab);
                    break;

                case RectangleColor:
                    scopeTips.setText(Languages.message("ScopeRectangleColorsTips"));
                    areaTab.setDisable(false);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    colorsTab.setDisable(false);
                    matchTab.setDisable(false);
                    rectangleLabel.setText(Languages.message("Rectangle"));
                    tabPane.getSelectionModel().select(areaTab);
                    break;

                case CircleColor:
                    scopeTips.setText(Languages.message("ScopeCircleColorsTips"));
                    areaTab.setDisable(false);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(circleBox);
                    colorsTab.setDisable(false);
                    matchTab.setDisable(false);
                    tabPane.getSelectionModel().select(areaTab);
                    break;

                case EllipseColor:
                    scopeTips.setText(Languages.message("ScopeEllipseColorsTips"));
                    areaTab.setDisable(false);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    colorsTab.setDisable(false);
                    matchTab.setDisable(false);
                    rectangleLabel.setText(Languages.message("Ellipse"));
                    tabPane.getSelectionModel().select(areaTab);
                    break;

                case PolygonColor:
                    scopeTips.setText(Languages.message("ScopePolygonColorsTips"));
                    pointsTab.setDisable(false);
                    colorsTab.setDisable(false);
                    matchTab.setDisable(false);
                    tabPane.getSelectionModel().select(pointsTab);
                    break;

                case Outline:
                    scopeTips.setText(Languages.message("ScopeOutlineTips"));
                    pixTab.setDisable(false);
                    if (outlinesList.getItems().isEmpty()) {
                        initPixTab();
                    }
                    tabPane.getSelectionModel().select(pixTab);
                    break;

                default:
                    return;
            }
            setScopeName();
            refreshStyle(tabPane);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void setScopeValues() {
        try {
            if (image == null || scope == null) {
                return;
            }
            switch (scope.getScopeType()) {
                case Matting:
                    checkMatchType();
                    break;

                case Rectangle:
                    initMaskRectangleLine(true);
                    scope.setRectangle(maskRectangleData.cloneValues());
                    indicateScope();
                    break;

                case Circle:
                    initMaskCircleLine(true);
                    scope.setCircle(maskCircleData.cloneValues());
                    indicateScope();
                    break;

                case Ellipse:
                    initMaskEllipseLine(true);
                    scope.setEllipse(maskEllipseData.cloneValues());
                    indicateScope();
                    break;

                case Polygon:
                    initMaskPolygonLine(true);
                    scope.setPolygon(maskPolygonData.cloneValues());
                    indicateScope();
                    break;

                case Color:
                    checkMatchType();
                    break;

                case RectangleColor:
                    initMaskRectangleLine(true);
                    scope.setRectangle(maskRectangleData.cloneValues());
                    checkMatchType();
                    break;

                case CircleColor:
                    initMaskCircleLine(true);
                    scope.setCircle(maskCircleData.cloneValues());
                    checkMatchType();
                    break;

                case EllipseColor:
                    initMaskEllipseLine(true);
                    scope.setEllipse(maskEllipseData.cloneValues());
                    checkMatchType();
                    break;

                case PolygonColor:
                    initMaskPolygonLine(true);
                    scope.setPolygon(maskPolygonData.cloneValues());
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void clearScope() {
        try {
            initMaskControls(false);
            isSettingValues = true;
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

            pointsList.getItems().clear();
            colorsList.getItems().clear();
            scopeDistanceSelector.getItems().clear();
            areaExcludedCheck.setSelected(false);
            colorExcludedCheck.setSelected(false);
            pickColorCheck.setSelected(false);
            scopeDistanceSelector.getEditor().setStyle(null);
            outlinesList.getSelectionModel().select(null);
            areaTab.setDisable(true);
            pointsTab.setDisable(true);
            colorsTab.setDisable(true);
            matchTab.setDisable(true);
            pixTab.setDisable(true);
            saveTab.setDisable(true);
            pickColorCheck.setSelected(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkScopeType() {
        if (isSettingValues) {
            return;
        }
        try {
            clearScope();
            if (scopeTypeGroup.getSelectedToggle() == null) {
                scope.setScopeType(ImageScope.ScopeType.All);
            } else {
                RadioButton selected = (RadioButton) scopeTypeGroup.getSelectedToggle();
                if (selected.equals(scopeAllRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.All);

                } else if (selected.equals(scopeMattingRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);

                } else if (selected.equals(scopeRectangleRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);

                } else if (selected.equals(scopeCircleRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Circle);

                } else if (selected.equals(scopeEllipseRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Ellipse);

                } else if (selected.equals(scopePolygonRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Polygon);

                } else if (selected.equals(scopeColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Color);

                } else if (selected.equals(scopeRectangleColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.RectangleColor);

                } else if (selected.equals(scopeCircleColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.CircleColor);

                } else if (selected.equals(scopeEllipseColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.EllipseColor);

                } else if (selected.equals(scopePolygonColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.PolygonColor);

                } else if (selected.equals(scopeOutlineRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                }
            }

            setScopeControls();
            setScopeValues();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkMatchType() {
        if (isSettingValues || scope == null || matchGroup.getSelectedToggle() == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
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
                    String value = scopeDistanceSelector.getValue();
                    List<String> vList = new ArrayList<>();
                    for (int i = 0; i <= max; i += step) {
                        vList.add(i + "");
                    }
                    isSettingValues = true;
                    scopeDistanceSelector.getItems().clear();
                    scopeDistanceSelector.getItems().addAll(vList);
                    scopeDistanceSelector.getSelectionModel().select(value);
                    isSettingValues = false;

                    if (checkDistanceValue()) {
                        indicateScope();
                    }

                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
        });
    }

    protected boolean checkDistanceValue() {
        if (scope.getColorScopeType() == null
                || scopeDistanceSelector.getSelectionModel().getSelectedItem() == null) {
            return false;
        }
        boolean valid = true;
        try {
            int distance = Integer.valueOf(scopeDistanceSelector.getValue());
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
            MyBoxLog.debug(e.toString());
            valid = false;
        }
        if (valid) {
            ValidationTools.setEditorNormal(scopeDistanceSelector);
        } else {
            ValidationTools.setEditorBadStyle(scopeDistanceSelector);
        }
        return valid;
    }

}
