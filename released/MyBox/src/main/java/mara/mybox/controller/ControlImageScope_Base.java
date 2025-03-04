package mara.mybox.controller;

import java.util.ArrayList;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.image.ScopeTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.image.data.ImageScope;
import static mara.mybox.image.data.ImageScope.ShapeType.Circle;
import static mara.mybox.image.data.ImageScope.ShapeType.Ellipse;
import static mara.mybox.image.data.ImageScope.ShapeType.Polygon;
import static mara.mybox.image.data.ImageScope.ShapeType.Rectangle;
import mara.mybox.image.tools.ColorConvertTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScope_Base extends BaseShapeController {

    protected ImageScope scope;
    protected TableColor tableColor;
    protected java.awt.Color maskColor;
    protected float maskOpacity;
    protected SimpleBooleanProperty showNotify, changedNotify;
    protected String background;

    @FXML
    protected ToggleGroup shapeTypeGroup;
    @FXML
    protected Tab shapeTab, colorsTab, matchTab, controlsTab;
    @FXML
    protected VBox viewBox, shapeBox, rectangleBox, circleBox, pointsBox, outlineBox;
    @FXML
    protected ComboBox<String> opacitySelector;
    @FXML
    protected ImageView outlineView;
    @FXML
    protected ControlColorSet colorController, maskColorController;
    @FXML
    protected ListView<Color> colorsList;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected ControlColorMatch matchController;
    @FXML
    protected ControlOutline outlineController;
    @FXML
    protected CheckBox shapeExcludedCheck, colorExcludedCheck, scopeExcludeCheck,
            handleTransparentCheck, clearDataWhenLoadImageCheck;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button shapeButton, goShapeButton, popScopeButton,
            clearColorsButton, deleteColorsButton, saveColorsButton;
    @FXML
    protected RadioButton wholeRadio, matting4Radio, matting8Radio, rectangleRadio, circleRadio,
            ellipseRadio, polygonRadio, outlineRadio;
    @FXML
    protected Label scopePointsLabel, scopeColorsLabel, pointsSizeLabel, colorsSizeLabel, rectangleLabel;
    @FXML
    protected FlowPane shapeOperationsPane;

    public Image srcImage() {
        return image;
    }

    public java.awt.Color maskColor() {
        return maskColor;
    }

    public Color maskFxColor() {
        return ColorConvertTools.converColor(maskColor);
    }

    public Image scopeImage(FxTask currentTask) {
        return selectedScope(currentTask, maskColor, false);
    }

    public Image selectedScope(FxTask currentTask, java.awt.Color bgColor, boolean cutMargins) {
        if (pickScopeValues() == null) {
            return null;
        }
        return ScopeTools.selectedScope(currentTask,
                srcImage(), scope,
                bgColor, cutMargins,
                scopeExcludeCheck.isSelected(),
                !handleTransparentCheck.isSelected());
    }

    public Image maskImage(FxTask currentTask) {
        return ScopeTools.maskScope(currentTask,
                srcImage(), scope,
                scopeExcludeCheck.isSelected(),
                !handleTransparentCheck.isSelected());
    }

    public boolean isValidScope() {
        return srcImage() != null && scope != null;
    }

    public ImageScope pickScopeValues() {
        try {
            if (!pickEnvValues()) {
                return null;
            }
            switch (scope.getShapeType()) {
                case Matting4:
                case Matting8:
                    scope.clearPoints();
                    for (DoublePoint p : pointsController.tableData) {
                        scope.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
                    }
                    break;

                case Rectangle:
                    scope.setRectangle(maskRectangleData.copy());
                    break;

                case Circle:
                    scope.setCircle(maskCircleData.copy());
                    break;

                case Ellipse:
                    scope.setEllipse(maskEllipseData.copy());
                    break;

                case Polygon:
                    maskPolygonData = new DoublePolygon();
                    maskPolygonData.setAll(pointsController.getPoints());
                    scope.setPolygon(maskPolygonData.copy());
                    break;
            }
            if (!pickColorValues()) {
                popError(message("InvalidParameters"));
                return null;
            }
            return scope;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean pickEnvValues() {
        try {
            image = srcImage();
            if (image == null || scope == null) {
                return false;
            }
            scope.setImage(image)
                    .setShapeExcluded(shapeExcludedCheck.isSelected())
                    .setColorExcluded(colorExcludedCheck.isSelected())
                    .setMaskColor(maskColor)
                    .setMaskOpacity(maskOpacity);
            if (background != null) {
                scope.setBackground(background);
            } else if (sourceFile != null && sourceFile.exists()) {
                scope.setBackground(sourceFile.getAbsolutePath());
            } else {
                scope.setBackground(null);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickColorValues() {
        try {
            List<Color> list = colorsList.getItems();
            int size = list.size();
            colorsSizeLabel.setText(message("Count") + ": " + size);
            if (size > 100) {
                colorsSizeLabel.setStyle(NodeStyleTools.redTextStyle());
            } else {
                colorsSizeLabel.setStyle(NodeStyleTools.blueTextStyle());
            }
            clearColorsButton.setDisable(size == 0);
            if (list.isEmpty()) {
                scope.clearColors();
            } else {
                List<java.awt.Color> colors = new ArrayList<>();
                for (Color color : list) {
                    colors.add(FxColorTools.toAwtColor(color));
                }
                scope.setColors(colors);
            }
            scope.setColorExcluded(colorExcludedCheck.isSelected());
            return matchController.pickValuesTo(scope);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
