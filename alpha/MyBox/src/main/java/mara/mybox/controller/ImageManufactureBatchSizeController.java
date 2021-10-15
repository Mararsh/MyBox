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
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchSizeController extends BaseImageManufactureBatchController {

    protected float scale;
    protected int sizeType, customWidth, customHeight, keepWidth, keepHeight;
    protected int interpolation = -1, dither = -1, anti = -1, quality = -1;

    @FXML
    protected ToggleGroup pixelsGroup;
    @FXML
    protected ComboBox<String> scaleBox;
    @FXML
    protected TextField customWidthInput, customHeightInput, keepWidthInput, keepHeightInput;
    @FXML
    protected RadioButton scaleRadio, widthRadio, heightRadio, customRadio,
            interpolationNullRadio, interpolation9Radio, interpolation4Radio, interpolation1Radio,
            ditherNullRadio, ditherOnRadio, ditherOffRadio, antiNullRadio, antiOnRadio, antiOffRadio,
            qualityNullRadio, qualityOnRadio, qualityOffRadio;

    protected static class SizeType {

        public static int Scale = 0;
        public static int Width = 1;
        public static int Height = 2;
        public static int Custom = 3;

    }

    public ImageManufactureBatchSizeController() {
        baseTitle = Languages.message("ImageManufactureBatchSize");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(customWidthInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(customHeightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(keepWidthInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(keepHeightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(scaleBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.error(e.toString());
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
                ValidationTools.setEditorNormal(scaleBox);
            } else {
                ValidationTools.setEditorBadStyle(scaleBox);
            }
        } catch (Exception e) {
            scale = 0;
            ValidationTools.setEditorBadStyle(scaleBox);
        }
    }

    private void checkCustomWidth() {
        try {
            customWidth = Integer.valueOf(customWidthInput.getText());
            if (customWidth > 0) {
                customWidthInput.setStyle(null);
            } else {
                customWidthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            customWidthInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkCustomHeight() {
        try {
            customHeight = Integer.valueOf(customHeightInput.getText());
            if (customHeight > 0) {
                customHeightInput.setStyle(null);
            } else {
                customHeightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            customHeightInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkKeepWidth() {
        try {
            keepWidth = Integer.valueOf(keepWidthInput.getText());
            if (keepWidth > 0) {
                keepWidthInput.setStyle(null);
            } else {
                keepWidthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            keepWidthInput.setStyle(UserConfig.badStyle());
        }

    }

    private void checkKeepHeight() {
        try {
            keepHeight = Integer.valueOf(keepHeightInput.getText());
            if (keepHeight > 0) {
                keepHeightInput.setStyle(null);
            } else {
                keepHeightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            keepHeightInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    public void pixelsCalculator() {
        try {
            PixelsCalculationController controller
                    = (PixelsCalculationController) openChildStage(Fxmls.PixelsCalculatorFxml, true);
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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        interpolation = dither = anti = quality = -1;
        if (interpolation9Radio.isSelected()) {
            interpolation = 9;
        } else if (interpolation4Radio.isSelected()) {
            interpolation = 4;
        } else if (interpolation1Radio.isSelected()) {
            interpolation = 1;
        }
        if (ditherOnRadio.isSelected()) {
            dither = 1;
        } else if (ditherOffRadio.isSelected()) {
            dither = 0;
        }
        if (antiOnRadio.isSelected()) {
            anti = 1;
        } else if (antiOffRadio.isSelected()) {
            anti = 0;
        }
        if (qualityOnRadio.isSelected()) {
            quality = 1;
        } else if (qualityOffRadio.isSelected()) {
            quality = 0;
        }
        return super.makeMoreParameters();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (sizeType == SizeType.Scale) {
                target = ScaleTools.scaleImageByScale(source, scale, dither, anti, quality, interpolation);

            } else if (sizeType == SizeType.Width) {
                target = ScaleTools.scaleImageWidthKeep(source, keepWidth, dither, anti, quality, interpolation);

            } else if (sizeType == SizeType.Height) {
                target = ScaleTools.scaleImageHeightKeep(source, keepHeight, dither, anti, quality, interpolation);

            } else if (sizeType == SizeType.Custom) {
                target = ScaleTools.scaleImage(source, customWidth, customHeight, dither, anti, quality, interpolation);
            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
