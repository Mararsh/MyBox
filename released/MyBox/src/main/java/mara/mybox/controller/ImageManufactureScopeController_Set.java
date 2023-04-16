package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Set extends ImageManufactureScopeController_Outline {

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
            scopeTips.setText("");
            NodeStyleTools.removeTooltip(scopeTips);
            NodeStyleTools.setTooltip(scopeTipsView, "");
            scopeTipsView.setVisible(false);
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
                    tabPane.getTabs().addAll(pointsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(pointsTab);
                    break;

                case Rectangle:
                    tips = message("ScopeRectangleTips");
                    tabPane.getTabs().addAll(areaTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case Circle:
                    tips = message("ScopeCircleTips");
                    tabPane.getTabs().addAll(areaTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(circleBox);
                    break;

                case Ellipse:
                    tips = message("ScopeEllipseTips");
                    tabPane.getTabs().addAll(areaTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case Polygon:
                    tips = message("ScopePolygonTips");
                    tabPane.getTabs().addAll(pointsTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(pointsTab);
                    break;

                case Color:
                    tips = message("ScopeColorTips");
                    tabPane.getTabs().addAll(colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(colorsTab);
                    break;

                case RectangleColor:
                    tips = message("ScopeRectangleColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case CircleColor:
                    tips = message("ScopeCircleColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(circleBox);
                    break;

                case EllipseColor:
                    tips = message("ScopeEllipseColorsTips");
                    tabPane.getTabs().addAll(areaTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(areaTab);
                    areaBox.getChildren().clear();
                    areaBox.getChildren().add(rectangleBox);
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case PolygonColor:
                    tips = message("ScopePolygonColorsTips");
                    tabPane.getTabs().addAll(pointsTab, colorsTab, matchTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(pointsTab);
                    break;

                case Outline:
                    tips = message("ScopeOutlineTips");
                    tabPane.getTabs().addAll(pixTab, optionsTab, saveTab);
                    tabPane.getSelectionModel().select(pixTab);
                    if (outlinesList.getItems().isEmpty()) {
                        initPixTab();
                    }
                    break;

                default:
                    return;
            }
            scopeTips.setText(tips);
            if (!tips.isBlank()) {
                NodeStyleTools.setTooltip(scopeTips, tips);
                NodeStyleTools.setTooltip(scopeTipsView, tips);
                scopeTipsView.setVisible(true);
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
