package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchSizeController extends ImageManufactureBatchController {

    private float scale;
    private int sizeType, customWidth, customHeight, keepWidth, keepHeight;

    @FXML
    private ToggleGroup pixelsGroup;
    @FXML
    private ComboBox<String> scaleBox;
    @FXML
    private TextField customWidthInput, customHeightInput, keepWidthInput, keepHeightInput;
    @FXML
    private RadioButton scaleRadio, widthRadio, heightRadio, customRadio;

    private static class SizeType {

        public static int Scale = 0;
        public static int Width = 1;
        public static int Height = 2;
        public static int Custom = 3;

    }

    public ImageManufactureBatchSizeController() {
        baseTitle = AppVariables.message("ImageManufactureBatchSize");
    }

    @Override
    public void initializeNext() {
        try {

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(customWidthInput.styleProperty().isEqualTo(badStyle))
                    .or(customHeightInput.styleProperty().isEqualTo(badStyle))
                    .or(keepWidthInput.styleProperty().isEqualTo(badStyle))
                    .or(keepHeightInput.styleProperty().isEqualTo(badStyle))
                    .or(scaleBox.getEditor().styleProperty().isEqualTo(badStyle)));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {

            keepWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkKeepWidth();
                }
            });

            keepHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkKeepHeight();
                }
            });

            customWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCustomWidth();
                }
            });

            customHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCustomHeight();
                }
            });

            pixelsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            scaleBox.getItems().addAll(Arrays.asList("0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScale();
                }
            });
            scaleBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkType() {
        scaleBox.setDisable(true);
        scaleBox.setStyle(null);
        keepWidthInput.setDisable(true);
        keepWidthInput.setStyle(null);
        keepHeightInput.setDisable(true);
        keepHeightInput.setStyle(null);
        customWidthInput.setDisable(true);
        customWidthInput.setStyle(null);
        customHeightInput.setDisable(true);
        customHeightInput.setStyle(null);

        RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
        if (selected.equals(scaleRadio)) {
            sizeType = SizeType.Scale;
            scaleBox.setDisable(false);
            checkScale();

        } else if (selected.equals(widthRadio)) {
            sizeType = SizeType.Width;
            keepWidthInput.setDisable(false);
            checkKeepWidth();

        } else if (selected.equals(heightRadio)) {
            sizeType = SizeType.Height;
            keepHeightInput.setDisable(false);
            checkKeepHeight();

        } else if (selected.equals(customRadio)) {
            sizeType = SizeType.Custom;
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkCustomWidth();
            checkCustomHeight();
        }
    }

    private void checkScale() {
        try {
            scale = Float.valueOf(scaleBox.getSelectionModel().getSelectedItem());
            if (scale >= 0) {
                FxmlControl.setEditorNormal(scaleBox);
            } else {
                FxmlControl.setEditorBadStyle(scaleBox);
            }
        } catch (Exception e) {
            scale = 0;
            FxmlControl.setEditorBadStyle(scaleBox);
        }
    }

    private void checkCustomWidth() {
        try {
            customWidth = Integer.valueOf(customWidthInput.getText());
            if (customWidth > 0) {
                customWidthInput.setStyle(null);
            } else {
                customWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            customWidthInput.setStyle(badStyle);
        }
    }

    private void checkCustomHeight() {
        try {
            customHeight = Integer.valueOf(customHeightInput.getText());
            if (customHeight > 0) {
                customHeightInput.setStyle(null);
            } else {
                customHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            customHeightInput.setStyle(badStyle);
        }
    }

    private void checkKeepWidth() {
        try {
            keepWidth = Integer.valueOf(keepWidthInput.getText());
            if (keepWidth > 0) {
                keepWidthInput.setStyle(null);
            } else {
                keepWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            keepWidthInput.setStyle(badStyle);
        }

    }

    private void checkKeepHeight() {
        try {
            keepHeight = Integer.valueOf(keepHeightInput.getText());
            if (keepHeight > 0) {
                keepHeightInput.setStyle(null);
            } else {
                keepHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            keepHeightInput.setStyle(badStyle);
        }
    }

    @FXML
    public void pixelsCalculator() {
        try {
            final PixelsCalculationController controller
                    = (PixelsCalculationController) FxmlStage.openStage(myStage,
                            CommonValues.PixelsCalculatorFxml,
                            true, Modality.WINDOW_MODAL, null);

            if (sizeType == SizeType.Custom) {
                controller.setSource(null, customWidthInput, customHeightInput);
            } else if (sizeType == SizeType.Width) {
                controller.setSource(null, keepWidthInput, null);
            } else if (sizeType == SizeType.Height) {
                controller.setSource(null, null, keepHeightInput);
            } else if (sizeType == SizeType.Scale) {
                controller.setSource(null, null, null);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (sizeType == SizeType.Scale) {
                target = ImageManufacture.scaleImage(source, scale);

            } else if (sizeType == SizeType.Width) {
                target = ImageManufacture.scaleImageWidthKeep(source, keepWidth);

            } else if (sizeType == SizeType.Height) {
                target = ImageManufacture.scaleImageHeightKeep(source, keepHeight);

            } else if (sizeType == SizeType.Custom) {
                target = ImageManufacture.scaleImage(source, customWidth, customHeight);
            }

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
