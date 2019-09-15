package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureShadowController extends ImageManufactureOperationController {

    protected int shadow;

    @FXML
    protected Rectangle bgRect;
    @FXML
    protected ComboBox shadowBox;
    @FXML
    protected CheckBox preAlphaCheck;
    @FXML
    protected Button paletteButton;

    public ImageManufactureShadowController() {
        baseTitle = AppVariables.message("ImageManufactureShadow");
        operation = ImageOperation.Shadow;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = shadowPane;

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

            if (parent.imageInformation != null
                    && CommonValues.NoAlphaImages.contains(parent.imageInformation.getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }
            String c = AppVariables.getUserConfigValue("ImageShadowBackground", Color.BLACK.toString());
            bgRect.setFill(Color.web(c));
            FxmlControl.setTooltip(bgRect, FxmlColor.colorDisplay((Color) bgRect.getFill()));

            shadowBox.getItems().clear();
            int width = (int) imageView.getImage().getWidth();
            shadowBox.getItems().addAll(Arrays.asList(width / 100 + "",
                    width / 50 + "",
                    width / 200 + "",
                    width / 30 + "",
                    "10", "5", "15", "3", "8", "1", "6", "20", "25"));
            shadowBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            shadow = v;
                            AppVariables.setUserConfigValue("ImageShadowSize", newValue);
                            FxmlControl.setEditorNormal(shadowBox);
                        } else {
                            FxmlControl.setEditorBadStyle(shadowBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(shadowBox);
                    }
                }
            });
            shadowBox.getSelectionModel().select(AppVariables.getUserConfigInt("ImageShadowSize", 10) + "");

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (paletteButton.equals(control)) {
            bgRect.setFill(color);
            FxmlControl.setTooltip(bgRect, FxmlColor.colorDisplay(color));
            AppVariables.setUserConfigValue("ImageShadowBackground", color.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("Shadow"), true);
    }

    @FXML
    @Override
    public void okAction() {
        if (shadow <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    if (preAlphaCheck.isSelected()) {
                        newImage = FxmlImageManufacture.addShadowNoAlpha(imageView.getImage(),
                                shadow, (Color) bgRect.getFill());
                    } else {
                        newImage = FxmlImageManufacture.addShadowAlpha(imageView.getImage(),
                                shadow, (Color) bgRect.getFill());
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    parent.updateImage(ImageOperation.Shadow, shadow + "", null, newImage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
