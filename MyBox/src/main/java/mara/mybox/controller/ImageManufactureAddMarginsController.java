package mara.mybox.controller;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.CommonValues;
import mara.mybox.image.FxmlImageTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureAddMarginsController extends ImageManufactureController {

    protected int addMarginWidth;

    @FXML
    protected ColorPicker addMarginsColorPicker;
    @FXML
    protected ComboBox addMarginBox;
    @FXML
    protected CheckBox addMarginsTopCheck, addMarginsBottomCheck, addMarginsLeftCheck, addMarginsRightCheck;
    @FXML
    protected Button addMarginsOkButton, transForAddMarginsButton;

    public ImageManufactureAddMarginsController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initAddMarginsTab();
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
                transForAddMarginsButton.setDisable(true);
            } else {
                transForAddMarginsButton.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initAddMarginsTab() {
        try {
            addMarginBox.getItems().addAll(Arrays.asList("5", "10", "2", "15", "20", "30", "1"));
            addMarginBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        addMarginWidth = Integer.valueOf(newValue);
                        if (addMarginWidth > 0) {
                            addMarginBox.getEditor().setStyle(null);
                        } else {
                            addMarginWidth = 0;
                            addMarginBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        addMarginWidth = 0;
                        addMarginBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            addMarginBox.getSelectionModel().select(0);

            addMarginsOkButton.disableProperty().bind(
                    addMarginBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void addMarginsTransparentAction() {
        addMarginsColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void addMarginsBlackAction() {
        addMarginsColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void addMarginsWhiteAction() {
        addMarginsColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void addMarginsAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.addMarginsFx(values.getCurrentImage(),
                        addMarginsColorPicker.getValue(), addMarginWidth,
                        addMarginsTopCheck.isSelected(), addMarginsBottomCheck.isSelected(),
                        addMarginsLeftCheck.isSelected(), addMarginsRightCheck.isSelected());
                recordImageHistory(ImageOperationType.Add_Margins, newImage);
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

}
