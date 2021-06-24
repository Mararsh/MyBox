package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureShadowController extends ImageManufactureOperationController {

    protected int shadow;

    @FXML
    protected ComboBox shadowBox;
    @FXML
    protected CheckBox preAlphaCheck;
    @FXML
    protected ColorSet colorSetController;

    @Override
    public void initPane() {
        try {
            if (imageController.imageInformation != null
                    && CommonValues.NoAlphaImages.contains(imageController.imageInformation.getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }

            colorSetController.init(this, baseName + "Color", Color.BLACK);

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
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.hideScopePane();
        imageController.showImagePane();
    }

    @FXML
    @Override
    public void okAction() {
        if (shadow <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    if (preAlphaCheck.isSelected()) {
                        newImage = FxmlImageManufacture.addShadowNoAlpha(imageView.getImage(),
                                shadow, (Color) colorSetController.rect.getFill());
                    } else {
                        newImage = FxmlImageManufacture.addShadowAlpha(imageView.getImage(),
                                shadow, (Color) colorSetController.rect.getFill());
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    imageController.updateImage(ImageOperation.Shadow, shadow + "", null, newImage, cost);
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }
}
