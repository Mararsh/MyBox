package mara.mybox.controller;

import java.util.Arrays;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlImageTools;
import static mara.mybox.tools.FxmlTools.badStyle;

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
    protected ColorPicker arcColorPicker;
    @FXML
    protected Button transForArcButton;
    @FXML
    protected ComboBox arcBox;

    public ImageManufactureArcController() {
        ImageArcKey = "ImageArcKey";
    }

    @Override
    protected void initializeNext2() {
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

            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                transForArcButton.setDisable(true);
            } else {
                transForArcButton.setDisable(false);
            }

            arcBox.getItems().clear();
            arcBox.getItems().addAll(Arrays.asList(values.getImageInfo().getxPixels() / 6 + "",
                    values.getImageInfo().getxPixels() / 8 + "",
                    values.getImageInfo().getxPixels() / 4 + "",
                    values.getImageInfo().getxPixels() / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            arcBox.getSelectionModel().select(0);

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initArcTab() {
        try {
            arcBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        arc = Integer.valueOf(newValue);
                        if (arc >= 0) {
                            arcBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(ImageArcKey, newValue);
                        } else {
                            arc = 0;
                            arcBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        arc = 0;
                        arcBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            arcColorPicker.setValue(Color.TRANSPARENT);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void arcTransparentAction() {
        arcColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void arcWhiteAction() {
        arcColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void arcBlackAction() {
        arcColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void arcAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.addArc(values.getCurrentImage(), arc, arcColorPicker.getValue());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void arcAction2() {
        if (arc <= 0) {
            return;
        }
        final Image newImage = FxmlImageTools.addArcFx(values.getCurrentImage(), arc, arcColorPicker.getValue());
        if (newImage == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("ErrorForBigImage"));
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }

        values.setUndoImage(values.getCurrentImage());
        values.setCurrentImage(newImage);
        imageView.setImage(newImage);
        setImageChanged(true);

        setBottomLabel();

    }

}
