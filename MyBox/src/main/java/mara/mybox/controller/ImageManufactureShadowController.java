package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.FxmlImageManufacture;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureShadowController extends ImageManufactureController {

    final protected String ImageShadowKey;
    protected int shadow;

    @FXML
    protected ComboBox shadowBox;
    @FXML
    protected CheckBox preAlphaCheck;
    @FXML
    protected ImageView preAlphaTipsView;

    public ImageManufactureShadowController() {
        ImageShadowKey = "ImageShadowKey";
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initShadowTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;
            tabPane.getSelectionModel().select(shadowTab);

            shadowBox.getItems().clear();
            shadowBox.getItems().addAll(Arrays.asList((int) values.getImage().getWidth() / 100 + "",
                    (int) values.getImage().getWidth() / 50 + "",
                    (int) values.getImage().getWidth() / 200 + "",
                    (int) values.getImage().getWidth() / 30 + "",
                    "0", "4", "5", "3", "2", "1", "6"));
            shadowBox.getSelectionModel().select(0);

            if (values.getImageInfo() != null
                    && CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
                if (AppVaribles.isAlphaAsWhite()) {
                    colorPicker.setValue(Color.BLACK);
                } else {
                    colorPicker.setValue(Color.WHITE);
                }
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
                colorPicker.setValue(Color.BLACK);

            }
            pickColorButton.setSelected(false);

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initShadowTab() {
        try {
            shadowBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shadow = Integer.valueOf(newValue);
                        if (shadow >= 0) {
                            AppVaribles.setUserConfigValue(ImageShadowKey, newValue);
                            FxmlControl.setEditorNormal(shadowBox);
                        } else {
                            shadow = 0;
                            FxmlControl.setEditorBadStyle(shadowBox);
                        }
                    } catch (Exception e) {
                        shadow = 0;
                        FxmlControl.setEditorBadStyle(shadowBox);
                    }
                }
            });

//            alphaWhiteCheck.setSelected(AppVaribles.isAlphaAsWhite());
//            alphaWhiteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue<? extends Boolean> ov,
//                        Boolean old_toggle, Boolean new_toggle) {
//                    AppVaribles.setUserConfigValue("AlphaAsWhite", new_toggle);
//                }
//            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void shadowBlackAction() {
        colorPicker.setValue(Color.BLACK);
    }

    @FXML
    @Override
    public void okAction() {
        if (shadow <= 0) {
            return;
        }

        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                if (preAlphaCheck.isSelected()) {
                    newImage = FxmlImageManufacture.addShadowNoAlpha(imageView.getImage(),
                            shadow, colorPicker.getValue());
                } else {
                    newImage = FxmlImageManufacture.addShadowAlpha(imageView.getImage(),
                            shadow, colorPicker.getValue());
                }
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Shadow, newImage);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(imageView.getImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);
                        }
                    });
                }
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
