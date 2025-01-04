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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.image.data.ImageScope;
import static mara.mybox.image.data.ImageScope.ScopeType.Circle;
import static mara.mybox.image.data.ImageScope.ScopeType.Ellipse;
import static mara.mybox.image.data.ImageScope.ScopeType.Matting;
import static mara.mybox.image.data.ImageScope.ScopeType.Outline;
import static mara.mybox.image.data.ImageScope.ScopeType.Polygon;
import static mara.mybox.image.data.ImageScope.ScopeType.Rectangle;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ScopeTools;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class BaseImageScope_Base extends BaseShapeController {

    protected ImageScope scope;
    protected TableColor tableColor;
    protected java.awt.Color maskColor;
    protected float maskOpacity;
    protected SimpleBooleanProperty showNotify, changedNotify;

    @FXML
    protected ToggleGroup scopeTypeGroup;
    @FXML
    protected Tab areaTab, colorsTab, matchTab, pixTab;
    @FXML
    protected VBox viewBox, areaBox, rectangleBox, circleBox, pointsBox;
    @FXML
    protected ComboBox<String> opacitySelector;
    @FXML
    protected ListView<Image> outlinesList;
    @FXML
    protected ControlColorSet colorController, maskColorController;
    @FXML
    protected ListView<Color> colorsList;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected ControlColorMatch matchController;
    @FXML
    protected CheckBox areaExcludedCheck, colorExcludedCheck, scopeExcludeCheck,
            handleTransparentCheck, scopeOutlineKeepRatioCheck, eightNeighborCheck,
            clearDataWhenLoadImageCheck;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button shapeButton, goScopeButton, popScopeButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton,
            clearColorsButton, deleteColorsButton, saveColorsButton, fileMenuButton;
    @FXML
    protected RadioButton scopeWholeRadio, scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio,
            scopeEllipseRadio, scopePolygonRadio, scopeColorRadio, scopeOutlineRadio;
    @FXML
    protected Label scopePointsLabel, scopeColorsLabel, pointsSizeLabel, colorsSizeLabel, rectangleLabel;
    @FXML
    protected FlowPane opPane;
    @FXML
    protected HBox pickColorBox;

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
                    valid = matchController.pickValues(scope, 50);
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

                case Colors:
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
            image = srcImage();
            if (image == null || scope == null) {
                return false;
            }
            scope.setImage(image)
                    .setAreaExcluded(areaExcludedCheck.isSelected())
                    .setColorExcluded(colorExcludedCheck.isSelected())
                    .setEightNeighbor(eightNeighborCheck.isSelected())
                    .setMaskColor(maskColor)
                    .setMaskOpacity(maskOpacity);
            if (sourceFile != null && sourceFile.exists()) {
                scope.setFile(sourceFile.getAbsolutePath());
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
                scope.clearColors();
                return true;
            } else {
                for (Color color : colors) {
                    scope.addColor(ColorConvertTools.converColor(color));
                }
                boolean valid = matchController.pickValues(scope, 50);
                if (!valid) {
                    popError(message("InvalidParameters"));
                }
                return valid;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
