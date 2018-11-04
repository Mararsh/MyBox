package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.image.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchCutMarginsController extends ImageManufactureBatchController {

    private final String ImageCutMarginsTypeKey;
    private int cutMarginWidth;
    private boolean cutMarginsByWidth;

    @FXML
    private ToggleGroup cutMarginGroup;
    @FXML
    private ComboBox<String> cutMarginBox;
    @FXML
    private RadioButton cutMarginsByColorRadio, cutMarginsByWidthRadio;
    @FXML
    private ColorPicker cutMarginsColorPicker;
    @FXML
    private Button cutMarginsTrButton, cutMarginsWhiteButton, cutMarginsBlackButton;
    @FXML
    private CheckBox cutMarginsTopCheck, cutMarginsBottomCheck, cutMarginsLeftCheck, cutMarginsRightCheck;

    public ImageManufactureBatchCutMarginsController() {
        ImageCutMarginsTypeKey = "ImageCutMarginsTypeKey";
    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(cutMarginBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(cutMarginsTopCheck.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
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
                    try {
                        cutMarginWidth = Integer.valueOf(newValue);
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
            });
            cutMarginBox.getSelectionModel().select(0);

            cutMarginsTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

            cutMarginsBottomCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

            cutMarginsLeftCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

            cutMarginsRightCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCutMarginType() {
        RadioButton selected = (RadioButton) cutMarginGroup.getSelectedToggle();
        AppVaribles.setConfigValue(ImageCutMarginsTypeKey, selected.getText());
        if (getMessage("ByWidth").equals(selected.getText())) {
            cutMarginBox.setDisable(false);
            checkMargins();
            cutMarginsTrButton.setDisable(true);
            cutMarginsWhiteButton.setDisable(true);
            cutMarginsBlackButton.setDisable(true);
            cutMarginsColorPicker.setDisable(true);
            cutMarginsByWidth = true;
        } else {
            cutMarginBox.setDisable(true);
            cutMarginBox.getEditor().setStyle(null);
            cutMarginsTrButton.setDisable(false);
            cutMarginsWhiteButton.setDisable(false);
            cutMarginsBlackButton.setDisable(false);
            cutMarginsColorPicker.setDisable(false);
            cutMarginsByWidth = false;
        }
    }

    private boolean checkMargins() {
        if (!cutMarginsTopCheck.isSelected()
                && !cutMarginsBottomCheck.isSelected()
                && !cutMarginsLeftCheck.isSelected()
                && !cutMarginsRightCheck.isSelected()) {
            cutMarginsTopCheck.setStyle(badStyle);
            popError(AppVaribles.getMessage("NothingHandled"));
            return false;
        } else {
            cutMarginsTopCheck.setStyle(null);
            return true;
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

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (!checkMargins()) {
                return null;
            }
            BufferedImage target;
            if (cutMarginsByWidth) {
                target = ImageConvertTools.cutMargins(source,
                        cutMarginWidth,
                        cutMarginsTopCheck.isSelected(), cutMarginsBottomCheck.isSelected(),
                        cutMarginsLeftCheck.isSelected(), cutMarginsRightCheck.isSelected());
            } else {
                target = ImageConvertTools.cutMargins(source,
                        FxmlImageTools.colorConvert(cutMarginsColorPicker.getValue()),
                        cutMarginsTopCheck.isSelected(), cutMarginsBottomCheck.isSelected(),
                        cutMarginsLeftCheck.isSelected(), cutMarginsRightCheck.isSelected());
            }

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }
}
