package mara.mybox.controller;

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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScopeInput_Base extends BaseImageController {

    protected Image srcImage;
    protected ImageScope scope;
    protected TableColor tableColor;
    protected java.awt.Color maskColor;
    protected float maskOpacity;
    protected SimpleBooleanProperty showNotify;

    @FXML
    protected ToggleGroup scopeTypeGroup, matchGroup;
    @FXML
    protected Tab areaTab, colorsTab, matchTab, pixTab, optionsTab;
    @FXML
    protected VBox setBox, areaBox, rectangleBox, circleBox, pointsBox;
    @FXML
    protected ComboBox<String> scopeDistanceSelector, opacitySelector;
    @FXML
    protected ListView<Image> outlinesList;
    @FXML
    protected ControlColorSet colorSetController, maskColorController;
    @FXML
    protected ListView<Color> colorsList;
    @FXML
    protected ControlPoints pointsController;
    @FXML
    protected CheckBox areaExcludedCheck, colorExcludedCheck, scopeOutlineKeepRatioCheck, eightNeighborCheck,
            ignoreTransparentCheck, squareRootCheck;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button goScopeButton, functionsButton, withdrawPointButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton,
            clearColorsButton, deleteColorsButton, saveColorsButton;
    @FXML
    protected RadioButton scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio,
            scopeEllipseRadio, scopePolygonRadio, scopeColorRadio, scopeOutlineRadio,
            colorRGBRadio, colorGreenRadio, colorRedRadio, colorBlueRadio,
            colorSaturationRadio, colorHueRadio, colorBrightnessRadio;
    @FXML
    protected Label scopeTips, scopePointsLabel, scopeColorsLabel, pointsSizeLabel, colorsSizeLabel, rectangleLabel;

    public Image srcImage() {
        if (srcImage == null) {
            srcImage = new Image("img/" + "cover" + AppValues.AppYear + "g9.png");
            loadImage(srcImage);
        }
        return srcImage;
    }

    public boolean finalScope() {
        try {
            if (scope == null) {
                return false;
            }
            if (sourceFile != null) {
                scope.setFile(sourceFile.getAbsolutePath());
            } else {
                scope.setFile("Unknown");
            }
            scope.setImage(srcImage());
            scope.setAreaExcluded(areaExcludedCheck.isSelected());
            scope.setColorExcluded(colorExcludedCheck.isSelected());
            scope.setEightNeighbor(eightNeighborCheck.isSelected());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected synchronized void indicateScope() {
        if (!isValidScope() || !finalScope()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image scopedImage;

            @Override
            protected boolean handle() {
                try {
                    PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                            srcImage(), scope, PixelsOperation.OperationType.ShowScope);
                    pixelsOperation.setSkipTransparent(ignoreTransparentCheck.isSelected());
                    scopedImage = pixelsOperation.operateFxImage();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return scopedImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                image = scopedImage;
                imageView.setImage(scopedImage);
                if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
                    drawMattingPoints();
                } else {
                    drawMaskShape();
                }
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task);
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

    public boolean isValidScope() {
        return srcImage() != null
                && scope != null
                && scope.getScopeType() != null
                && scope.getScopeType() != ScopeType.All;
    }

}
