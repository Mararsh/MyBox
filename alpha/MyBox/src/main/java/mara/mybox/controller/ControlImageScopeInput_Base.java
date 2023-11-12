package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScopeTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlImageScopeInput_Base extends BaseShapeController {

    protected BaseImageScopeController handler;
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
    protected CheckBox areaExcludedCheck, colorExcludedCheck, scopeOutlineKeepRatioCheck,
            eightNeighborCheck, squareRootCheck, popCheck;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button shapeButton, goScopeButton, popScopeButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton,
            clearColorsButton, deleteColorsButton, saveColorsButton;
    @FXML
    protected RadioButton scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio,
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

    public ImageScope finalScope() {
        try {
            if (!isValidScope()) {
                popError(message("InvalidParameters"));
                return null;
            }
            if (sourceFile != null) {
                scope.setFile(sourceFile.getAbsolutePath());
            } else {
                scope.setFile("Unknown");
            }
            scope.setImage(srcImage())
                    .setAreaExcluded(areaExcludedCheck.isSelected())
                    .setColorExcluded(colorExcludedCheck.isSelected())
                    .setEightNeighbor(eightNeighborCheck.isSelected())
                    .setMaskColor(maskColor)
                    .setMaskOpacity(maskOpacity);
            return scope;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Image scopeImage() {
        if (finalScope() == null) {
            return srcImage();
        }
        return selectedScope(maskColor, false);
    }

    public Image selectedScope(java.awt.Color bgColor, boolean cutMargins) {
        if (finalScope() == null) {
            return srcImage();
        }
        return ScopeTools.selectedScope(srcImage(), scope,
                bgColor, cutMargins,
                handler.excludeRadio.isSelected(),
                handler.ignoreTransparentCheck.isSelected());
    }

    public Image maskImage() {
        if (finalScope() == null) {
            return null;
        }
        return ScopeTools.maskScope(srcImage(), scope,
                handler.excludeRadio.isSelected(),
                handler.ignoreTransparentCheck.isSelected());
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        ImageCanvasInputController controller = ImageCanvasInputController.open(this, baseTitle);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                Image canvas = controller.getCanvas();
                if (canvas != null) {
                    loadImage(canvas);
                }
                controller.close();
            }
        });
    }

    protected synchronized void indicateScope() {
        if (finalScope() == null) {
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

    public boolean isValidScope() {
        return srcImage() != null
                && scope != null
                && scope.getScopeType() != null;
    }

    @FXML
    @Override
    public boolean popAction() {
        ImageScopeViewsController.open(handler);
        return true;
    }

}
