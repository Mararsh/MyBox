package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import static mara.mybox.bufferedimage.ImageScope.ColorScopeType.Brightness;
import static mara.mybox.bufferedimage.ImageScope.ColorScopeType.Hue;
import static mara.mybox.bufferedimage.ImageScope.ColorScopeType.Saturation;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Circle;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Ellipse;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Matting;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Outline;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Polygon;
import static mara.mybox.bufferedimage.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScopeTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_Base extends BaseShapeController {

    protected BasePixelsController handler;
    protected BaseImageController imageController;
    protected TableColor tableColor;
    protected java.awt.Color maskColor;
    protected float maskOpacity;
    protected SimpleBooleanProperty showNotify;

    @FXML
    protected ToggleGroup scopeTypeGroup, matchGroup;
    @FXML
    protected Tab areaTab, colorsTab, matchTab, pixTab;
    @FXML
    protected VBox viewBox, setBox, areaBox, rectangleBox, circleBox, pointsBox;
    @FXML
    protected ComboBox<String> scopeDistanceSelector, opacitySelector;
    @FXML
    protected ListView<Image> outlinesList;
    @FXML
    protected ControlColorSet colorController, maskColorController;
    @FXML
    protected ListView<Color> colorsList;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected CheckBox areaExcludedCheck, colorExcludedCheck,
            scopeExcludeCheck, ignoreTransparentCheck,
            scopeOutlineKeepRatioCheck, eightNeighborCheck, squareRootCheck;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button shapeButton, goScopeButton, popScopeButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton,
            clearColorsButton, deleteColorsButton, saveColorsButton;
    @FXML
    protected RadioButton scopeWholeRadio, scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio,
            scopeEllipseRadio, scopePolygonRadio, scopeColorRadio, scopeOutlineRadio,
            colorRGBRadio, colorGreenRadio, colorRedRadio, colorBlueRadio,
            colorSaturationRadio, colorHueRadio, colorBrightnessRadio;
    @FXML
    protected Label scopePointsLabel, scopeColorsLabel, pointsSizeLabel, colorsSizeLabel, rectangleLabel;
    @FXML
    protected HBox opBox, pickColorBox;

    public Image srcImage() {
        if (imageController != null) {
            image = imageController.imageView.getImage();
        } else {
            image = imageView.getImage();
        }
        if (image == null) {
            image = new Image("img/" + "cover" + AppValues.AppYear + "g9.png");
            loadImage(image);
        }
        return image;
    }

    public java.awt.Color maskColor() {
        return maskColor;
    }

    public Color maskFxColor() {
        return ColorConvertTools.converColor(maskColor);
    }

    public Image scopeImage() {
        return selectedScope(maskColor, false);
    }

    public Image selectedScope(java.awt.Color bgColor, boolean cutMargins) {
        if (pickScopeValues() == null) {
            return null;
        }
        return ScopeTools.selectedScope(srcImage(), scope,
                bgColor, cutMargins,
                scopeExcludeCheck.isSelected(),
                ignoreTransparentCheck.isSelected());
    }

    public Image maskImage() {
        if (pickScopeValues() == null) {
            return null;
        }
        return ScopeTools.maskScope(srcImage(), scope,
                scopeExcludeCheck.isSelected(),
                ignoreTransparentCheck.isSelected());
    }

    public boolean isValidScope() {
        return srcImage() != null && scope != null;
    }

    public ImageScope pickScopeValues() {
        try {
            if (!pickBaseValues()) {
                return null;
            }
            boolean valid = true;
            switch (scope.getScopeType()) {
                case Matting:
                    scope.clearPoints();
                    for (DoublePoint p : pointsController.tableData) {
                        scope.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
                    }
                    valid = pickMatchValues();
                    break;

                case Rectangle:
                    scope.setRectangle(maskRectangleData.copy());
                    valid = pickColorValues();
                    break;

                case Circle:
                    scope.setCircle(maskCircleData.copy());
                    valid = pickColorValues();
                    break;

                case Ellipse:
                    scope.setEllipse(maskEllipseData.copy());
                    valid = pickColorValues();
                    break;

                case Polygon:
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.setAll(pointsController.getPoints());
                    scope.setPolygon(maskPolygonData.copy());
                    valid = pickColorValues();
                    break;

                case Color:
                    valid = pickColorValues();
                    break;

                case Outline:
                    break;

            }

            if (!valid) {
                popError(message("InvalidParameters"));
                return null;
            }
            return scope;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean pickBaseValues() {
        try {
            if (srcImage() == null || scope == null) {
                return false;
            }
            scope.setImage(srcImage())
                    .setAreaExcluded(areaExcludedCheck.isSelected())
                    .setColorExcluded(colorExcludedCheck.isSelected())
                    .setEightNeighbor(eightNeighborCheck.isSelected())
                    .setMaskColor(maskColor)
                    .setMaskOpacity(maskOpacity);
            if (imageController.sourceFile != null) {
                scope.setFile(imageController.sourceFile.getAbsolutePath());
            } else {
                scope.setFile("Unknown");
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickColorValues() {
        if (!isValidScope()) {
            return false;
        }
        try {
            List<Color> colors = colorsList.getItems();
            if (colors == null || colors.isEmpty()) {
                scope.getColors().clear();
            } else {
                for (Color color : colors) {
                    scope.addColor(ColorConvertTools.converColor(color));
                }
            }
            return pickMatchValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean pickMatchValues() {
        if (!isValidScope()) {
            return false;
        }
        boolean valid = true;
        try {
            RadioButton selected = (RadioButton) matchGroup.getSelectedToggle();
            if (selected.equals(colorRGBRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Color);

            } else if (selected.equals(colorRedRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Red);

            } else if (selected.equals(colorGreenRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Green);

            } else if (selected.equals(colorBlueRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Blue);

            } else if (selected.equals(colorSaturationRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Saturation);

            } else if (selected.equals(colorHueRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Hue);

            } else if (selected.equals(colorBrightnessRadio)) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Brightness);
            }

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

    public void showScope() {
    }

}
