package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureCropController extends ImageManufactureOperationController {

    protected int centerX, centerY, radius;

    @FXML
    protected ToggleGroup shapeGroup;
    @FXML
    protected ToolBar cropBar;
    @FXML
    protected HBox setBox, colorBox;
    @FXML
    protected Rectangle bgRect;
    @FXML
    protected Button paletteButton;
    @FXML
    protected CheckBox putCheck;

    public ImageManufactureCropController() {
        baseTitle = AppVariables.message("ImageManufactureCrop");
        operation = ImageOperation.Crop;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = cropPane;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }

            isSettingValues = true;
            imageController.scopeSetCheck.setSelected(true);
            isSettingValues = false;
            imageController.checkViewScope();
            try {
                String c = AppVariables.getUserConfigValue("CropBackgroundColor", Color.TRANSPARENT.toString());
                bgRect.setFill(Color.web(c));
            } catch (Exception e) {
                bgRect.setFill(Color.TRANSPARENT);
                AppVariables.setUserConfigValue("CropBackgroundColor", Color.TRANSPARENT.toString());
            }
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay((Color) bgRect.getFill()));

            putCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageCropPutClipboard", putCheck.isSelected());
                }
            });
            putCheck.setSelected(AppVariables.getUserConfigBoolean("ImageCropPutClipboard", false));

            cropButton.disableProperty().bind(parent.cropButton.disableProperty());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void eventsHandler(KeyEvent event) {
        // do thing. "Crop button" exists in both this pane and frame pane. Should only response once.
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (paletteButton.equals(control)) {
            bgRect.setFill(color);
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("CropBackgroundColor", color.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("Crop"), true);
    }

    @FXML
    @Override
    public void cropAction() {
        parent.cropAction();
    }

}
