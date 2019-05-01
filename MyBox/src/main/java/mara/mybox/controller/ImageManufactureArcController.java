package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.ImageManufacture;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureArcController extends ImageManufactureController {

    final protected String ImageArcKey;
    protected int arc;

    @FXML
    protected ComboBox arcBox;

    public ImageManufactureArcController() {

        ImageArcKey = "ImageArcKey";
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initArcTab();
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
            tabPane.getSelectionModel().select(arcTab);

            if (values.getImageInfo() != null
                    && CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                transparentButton.setDisable(true);
                colorPicker.setValue(Color.WHITE);
            } else {
                transparentButton.setDisable(false);
                colorPicker.setValue(Color.TRANSPARENT);
            }

            arcBox.getItems().clear();
            arcBox.getItems().addAll(Arrays.asList((int) values.getImage().getWidth() / 6 + "",
                    (int) values.getImage().getWidth() / 8 + "",
                    (int) values.getImage().getWidth() / 4 + "",
                    (int) values.getImage().getWidth() / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            arcBox.getSelectionModel().select(0);

            pickColorButton.setSelected(false);

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initArcTab() {
        try {
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        arc = Integer.valueOf(newValue);
                        if (arc >= 0) {
                            FxmlControl.setEditorNormal(arcBox);
                            AppVaribles.setUserConfigValue(ImageArcKey, newValue);
                        } else {
                            arc = 0;
                            FxmlControl.setEditorBadStyle(arcBox);
                        }
                    } catch (Exception e) {
                        arc = 0;
                        FxmlControl.setEditorBadStyle(arcBox);
                    }
                }
            });

            colorPicker.setValue(Color.TRANSPARENT);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void setTransparentAction() {
        colorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void setWhiteAction() {
        colorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void setBlackAction() {
        colorPicker.setValue(Color.BLACK);
    }

    @FXML
    @Override
    public void okAction() {
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = ImageManufacture.addArc(imageView.getImage(), arc, colorPicker.getValue());
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Arc, newImage);
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
                            updateLabelTitle();
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
