package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Base extends ImageViewerController {

    protected TableColor tableColor;
    protected ImageManufactureController imageController;
    protected ImageManufactureScopesSavedController scopesSavedController;
    protected float opacity;
    protected BufferedImage outlineSource;

    @FXML
    protected ImageView scopeView, scopeTipsView;
    @FXML
    protected ToggleGroup scopeTypeGroup, matchGroup;
    @FXML
    protected Tab areaTab, pointsTab, colorsTab, matchTab, pixTab, optionsTab, saveTab;
    @FXML
    protected VBox setBox, areaBox, rectangleBox, circleBox;
    @FXML
    protected ComboBox<String> scopeDistanceSelector, opacitySelector;
    @FXML
    protected ListView<Image> outlinesList;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected ListView<Color> colorsList;
    @FXML
    protected ListView<String> pointsList;
    @FXML
    protected CheckBox areaExcludedCheck, colorExcludedCheck, scopeOutlineKeepRatioCheck, eightNeighborCheck,
            ignoreTransparentCheck, squareRootCheck;
    @FXML
    protected TextField scopeNameInput, rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button saveScopeButton, deletePointsButton, clearPointsButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton,
            clearColorsButton, deleteColorsButton, saveColorsButton;
    @FXML
    protected RadioButton scopeAllRadio, scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio,
            scopePolygonRadio, scopeColorRadio, scopeRectangleColorRadio, scopeCircleColorRadio,
            scopeEllipseColorRadio, scopePolygonColorRadio, scopeOutlineRadio, scopeEllipseRadio,
            colorRGBRadio, colorGreenRadio, colorRedRadio, colorBlueRadio,
            colorSaturationRadio, colorHueRadio, colorBrightnessRadio;
    @FXML
    protected Label scopeTips, scopePointsLabel, scopeColorsLabel, pointsSizeLabel, colorsSizeLabel, rectangleLabel;

    protected synchronized void indicateScope() {
        if (isSettingValues || imageView == null || !scopeView.isVisible() || scope == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image scopedImage;

            @Override
            protected boolean handle() {
                try {
                    PixelsOperation pixelsOperation = PixelsOperationFactory.create(imageView.getImage(),
                            scope, PixelsOperation.OperationType.ShowScope);
                    if (!ignoreTransparentCheck.isSelected()) {
                        pixelsOperation.setSkipTransparent(false);
                    }
                    scopedImage = pixelsOperation.operateFxImage();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return scopedImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                scopeView.setImage(scopedImage);
                scopeView.setFitWidth(imageView.getFitWidth());
                scopeView.setFitHeight(imageView.getFitHeight());
                scopeView.setLayoutX(imageView.getLayoutX());
                scopeView.setLayoutY(imageView.getLayoutY());
            }

        };
        parentController.start(task);
    }

    @Override
    protected void popImageMenu(double x, double y) {
        if (!UserConfig.getBoolean(baseName + "ContextMenu", true)
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageScopeController.open((ImageManufactureScopeController) this, x, y);
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
            MenuImageScopeController.open((ImageManufactureScopeController) this, localToScreen.getX(), localToScreen.getY());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        ImageScopePopController.open((ImageManufactureScopeController) this);
        return true;
    }

}
