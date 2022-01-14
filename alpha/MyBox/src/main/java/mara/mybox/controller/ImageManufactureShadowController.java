package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.UserConfig;

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
    protected ColorSet colorSetController;

    @Override
    public void initPane() {
        try {
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
                            UserConfig.setString("ImageShadowSize", newValue);
                            ValidationTools.setEditorNormal(shadowBox);
                        } else {
                            ValidationTools.setEditorBadStyle(shadowBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(shadowBox);
                    }
                }
            });
            shadowBox.getSelectionModel().select(UserConfig.getInt("ImageShadowSize", 10) + "");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.imageTab();
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
            task = new SingletonTask<Void>(this) {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxImageTools.addShadowAlpha(imageView.getImage(),
                            shadow, (Color) colorSetController.rect.getFill());
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
            imageController.start(task);
        }
    }
}
