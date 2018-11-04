package mara.mybox.controller;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.image.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureCutMarginsController extends ImageManufactureController {

    final protected String ImageCutMarginsTypeKey;
    protected int cutMarginWidth;
    protected boolean cutMarginsByWidth;

    @FXML
    protected ToggleGroup cutMarginGroup;
    @FXML
    protected ComboBox cutMarginBox;
    @FXML
    protected ColorPicker cutMarginsColorPicker;
    @FXML
    protected Button cutMarginsWhiteButton, cutMarginsBlackButton, cutMarginsOkButton, cutMarginsTrButton;
    @FXML
    protected CheckBox cutMarginsTopCheck, cutMarginsBottomCheck, cutMarginsLeftCheck, cutMarginsRightCheck;
    @FXML
    protected RadioButton cutMarginsByWidthRadio, cutMarginsByColorRadio;

    public ImageManufactureCutMarginsController() {
        ImageCutMarginsTypeKey = "ImageCutMarginsTypeKey";
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initCutMarginsTab();
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
                cutMarginsTrButton.setDisable(true);
            } else {
                cutMarginsTrButton.setDisable(false);
            }

            checkCutMarginType();

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initCutMarginsTab() {
        try {
            cutMarginGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCutMarginType();
                }
            });
            FxmlTools.setRadioSelected(cutMarginGroup, AppVaribles.getConfigValue(ImageCutMarginsTypeKey, getMessage("ByWidth")));
            cutMarginsByWidth = cutMarginsByWidthRadio.isSelected();

            cutMarginBox.getItems().addAll(Arrays.asList("5", "10", "2", "15", "20", "30", "1"));
            cutMarginBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCutMarginWidth();
                }
            });
            cutMarginBox.getSelectionModel().select(0);

            cutMarginsOkButton.disableProperty().bind(
                    cutMarginBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCutMarginType() {
        RadioButton selected = (RadioButton) cutMarginGroup.getSelectedToggle();
        AppVaribles.setConfigValue(ImageCutMarginsTypeKey, selected.getText());
        if (getMessage("ByWidth").equals(selected.getText())) {
            cutMarginBox.setDisable(false);
            checkCutMarginWidth();
            cutMarginsTrButton.setDisable(true);
            cutMarginsWhiteButton.setDisable(true);
            cutMarginsBlackButton.setDisable(true);
            cutMarginsColorPicker.setDisable(true);
            cutMarginsByWidth = true;
        } else {
            if (values.getCurrentImage() != null) {
                popInformation(getMessage("ClickImageForColor"));
            }
            cutMarginBox.setDisable(true);
            cutMarginBox.getEditor().setStyle(null);
            if (values.getImageInfo() != null
                    && !CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                cutMarginsTrButton.setDisable(false);
            }
            cutMarginsWhiteButton.setDisable(false);
            cutMarginsBlackButton.setDisable(false);
            cutMarginsColorPicker.setDisable(false);
            cutMarginsByWidth = false;
        }
    }

    private void checkCutMarginWidth() {
        try {
            cutMarginWidth = Integer.valueOf((String) cutMarginBox.getSelectionModel().getSelectedItem());
            if (cutMarginWidth > 0) {
                cutMarginBox.getEditor().setStyle(null);
            } else {
                cutMarginWidth = 0;
                cutMarginBox.getEditor().setStyle(badStyle);
            }

        } catch (Exception e) {
            cutMarginWidth = 0;
            cutMarginBox.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    public void cutMarginsTransparentAction() {
        cutMarginsColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void cutMarginsBlackAction() {
        cutMarginsColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void cutMarginsWhiteAction() {
        cutMarginsColorPicker.setValue(Color.WHITE);
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        if (cutMarginsByWidth) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / imageView.getBoundsInLocal().getHeight());

        PixelReader pixelReader = values.getCurrentImage().getPixelReader();
        Color color = pixelReader.getColor(x, y);
        cutMarginsColorPicker.setValue(color);
        popInformation(getMessage("ContinueClickColor"));

    }

    @FXML
    public void cutMarginsAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage;
                    if (cutMarginsByWidth) {
                        newImage = FxmlImageTools.cutMarginsByWidth(values.getCurrentImage(), cutMarginWidth,
                                cutMarginsTopCheck.isSelected(), cutMarginsBottomCheck.isSelected(),
                                cutMarginsLeftCheck.isSelected(), cutMarginsRightCheck.isSelected());
                    } else {
                        newImage = FxmlImageTools.cutMarginsByColor(values.getCurrentImage(), cutMarginsColorPicker.getValue(),
                                cutMarginsTopCheck.isSelected(), cutMarginsBottomCheck.isSelected(),
                                cutMarginsLeftCheck.isSelected(), cutMarginsRightCheck.isSelected());
                    }
                    recordImageHistory(ImageOperationType.Cut_Margins, newImage);
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
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
