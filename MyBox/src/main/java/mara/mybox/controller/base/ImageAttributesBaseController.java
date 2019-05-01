package mara.mybox.controller.base;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.data.ImageAttributes;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.ImageValue;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAttributesBaseController extends BaseController {

    private final String ImageFormatKey, ImageColorKey, ImageCompressionTypeKey;
    private final String ImageQualityKey, ImageQualityInputKey;
    private final String ImageBinaryKey, ImageBinaryInputKey;

    public String creatSubdirKey, previewKey, fillZeroKey;
    public String appendColorKey, appendCompressionTypeKey;
    public String appendDensityKey, appendQualityKey, appendSizeKey;

    @FXML
    public Pane imageConverterAttributes;

    @FXML
    public ToggleGroup ImageFormatGroup, ImageColorGroup, QualityGroup, CompressionGroup, binaryGroup;
    @FXML
    public RadioButton RGB, ARGB, Gray, Binary, fullQuality;
    @FXML
    public TextField qualityInput, thresholdInput;
    @FXML
    public HBox qualityBox, compressBox, colorBox;
    @FXML
    public RadioButton rawSelect, pcxSelect;
    @FXML
    public CheckBox ditherCheck;

    public ImageAttributes imageAttributes = new ImageAttributes();

    public static class BinaryAlgorithm {

        public static int DEFAULT = 0;
        public static int OTSU = 1;
        public static int THRESHOLD = 9;
    }

    public ImageAttributesBaseController() {
        ImageFormatKey = "ImageFormatKey";
        ImageColorKey = "ImageColorKey";
        ImageCompressionTypeKey = "ImageCompressionTypeKey";
        ImageQualityKey = "ImageQualityKey";
        ImageQualityInputKey = "ImageQualityInputKey";
        ImageBinaryKey = "ImageBinaryKey";
        ImageBinaryInputKey = "ImageBinaryInputKey";
    }

    @Override
    public void initializeNext() {
        imageAttributes = new ImageAttributes();

        ImageFormatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageFormat();
            }
        });
        FxmlControl.setRadioSelected(ImageFormatGroup, AppVaribles.getUserConfigValue(ImageFormatKey, "png"));
        checkImageFormat();

        ImageColorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageColor();
            }
        });
        FxmlControl.setRadioSelected(ImageColorGroup, AppVaribles.getUserConfigValue(ImageColorKey, AppVaribles.getMessage("Colorful")));
        checkImageColor();

        QualityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkQuality();
            }
        });
        FxmlControl.setRadioSelected(QualityGroup, AppVaribles.getUserConfigValue(ImageQualityKey, "100%"));
        checkQuality();

        qualityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkQuality();
            }
        });
        qualityInput.setText(AppVaribles.getUserConfigValue(ImageQualityInputKey, null));

        binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkColorConversion();
            }
        });
        FxmlControl.setRadioSelected(binaryGroup, AppVaribles.getUserConfigValue(ImageBinaryKey, AppVaribles.getMessage("OTSU")));

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkColorConversion();
            }
        });
        thresholdInput.setText(AppVaribles.getUserConfigValue(ImageBinaryInputKey, null));

        ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                imageAttributes.setIsDithering(newValue);
            }
        });
        imageAttributes.setIsDithering(ditherCheck.isSelected());
        FxmlControl.setComments(ditherCheck, new Tooltip(getMessage("DitherComments")));

        initializeNext2();
    }

    public void initializeNext2() {

    }

    public void checkImageFormat() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            String imageFormat = selected.getText();
            imageAttributes.setImageFormat(imageFormat);
            AppVaribles.setUserConfigValue(ImageFormatKey, imageFormat);

            String[] compressionTypes = ImageValue.getCompressionTypes(imageFormat, imageAttributes.getColorSpace());
            checkCompressionTypes(compressionTypes);

            if (compressionTypes != null && "jpg".equals(imageFormat)) {
                qualityBox.setDisable(false);
//                if (qualityInput.getStyle().equals(FxmlTools.badStyle)) {
//                    fullQuality.setSelected(true);
//                }
            } else {
                qualityInput.setStyle(null);
                qualityBox.setDisable(true);
                fullQuality.setSelected(true);
            }

            switch (imageFormat) {
                case "wbmp":
                    Binary.setDisable(false);
                    RGB.setDisable(true);
                    ARGB.setDisable(true);
                    Gray.setDisable(true);
                    Binary.setSelected(true);
                    break;
                case "jpg":
                case "bmp":
                case "pnm":
                case "gif":
                    ARGB.setDisable(true);
                    Binary.setDisable(false);
                    RGB.setDisable(false);
                    Gray.setDisable(false);
                    if (ARGB.isSelected()) {
                        RGB.setSelected(true);
                    }
                    break;
                default:
                    ARGB.setDisable(false);
                    Binary.setDisable(false);
                    RGB.setDisable(false);
                    Gray.setDisable(false);
                    break;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkImageColor() {
        try {
            RadioButton selected = (RadioButton) ImageColorGroup.getSelectedToggle();
            String s = selected.getText();
            AppVaribles.setUserConfigValue(ImageColorKey, s);
            if (getMessage("Colorful").equals(s)) {
                imageAttributes.setColorSpace(ImageType.RGB);
            } else if (getMessage("ColorAlpha").equals(s)) {
                imageAttributes.setColorSpace(ImageType.ARGB);
            } else if (getMessage("ShadesOfGray").equals(s)) {
                imageAttributes.setColorSpace(ImageType.GRAY);
            } else if (getMessage("BlackOrWhite").equals(s)) {
                imageAttributes.setColorSpace(ImageType.BINARY);
            } else {
                imageAttributes.setColorSpace(ImageType.RGB);
            }

//            if ("tif".equals(imageFormat) || "bmp".equals(imageFormat)) {
            String[] compressionTypes = ImageValue.getCompressionTypes(imageAttributes.getImageFormat(), imageAttributes.getColorSpace());
            checkCompressionTypes(compressionTypes);
//            }

            if (imageAttributes.getColorSpace() == ImageType.BINARY) {
                colorBox.setDisable(false);
                checkColorConversion();
            } else {
                colorBox.setDisable(true);
                if (thresholdInput.getStyle().equals(FxmlControl.badStyle)) {
                    FxmlControl.setRadioFirstSelected(binaryGroup);
                }
            }

//            logger.debug(imageColor);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkCompressionTypes(String[] types) {
        compressBox.getChildren().removeAll(compressBox.getChildren());
        CompressionGroup = new ToggleGroup();
        if (types == null) {
            RadioButton newv = new RadioButton(AppVaribles.getMessage("None"));
            newv.setToggleGroup(CompressionGroup);
            compressBox.getChildren().add(newv);
            newv.setSelected(true);
        } else {
            boolean cSelected = false;
            for (String ctype : types) {
                RadioButton newv = new RadioButton(ctype);
                newv.setToggleGroup(CompressionGroup);
                compressBox.getChildren().add(newv);
                if (!cSelected) {
                    newv.setSelected(true);
                    cSelected = true;
                }
            }
        }

        CompressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkCompressionType();
            }
        });
        FxmlControl.setRadioSelected(CompressionGroup,
                AppVaribles.getUserConfigValue(ImageCompressionTypeKey, AppVaribles.getMessage("None")));
        checkCompressionType();
    }

    public void checkCompressionType() {
        try {
            RadioButton selected = (RadioButton) CompressionGroup.getSelectedToggle();
            imageAttributes.setCompressionType(selected.getText());
            AppVaribles.setUserConfigValue(ImageCompressionTypeKey, selected.getText());
        } catch (Exception e) {
            imageAttributes.setCompressionType(null);
        }
    }

    public void checkColorConversion() {
        thresholdInput.setStyle(null);
        try {
            RadioButton selected = (RadioButton) binaryGroup.getSelectedToggle();
            String s = selected.getText();

            if (getMessage("Threshold").equals(s)) {
                imageAttributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_THRESHOLD);
            } else if (getMessage("OTSU").equals(s)) {
                imageAttributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_OTSU);
                AppVaribles.setUserConfigValue(ImageBinaryKey, s);
            } else {
                imageAttributes.setBinaryConversion(ImageAttributes.BinaryConversion.DEFAULT);
                AppVaribles.setUserConfigValue(ImageBinaryKey, s);
            }

            int inputValue;
            try {
                inputValue = Integer.parseInt(thresholdInput.getText());
                if (inputValue >= 0 && inputValue <= 255) {
                    AppVaribles.setUserConfigValue(ImageBinaryInputKey, inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }

            if (imageAttributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                if (inputValue >= 0) {
                    imageAttributes.setThreshold(inputValue);
                    AppVaribles.setUserConfigValue(ImageBinaryKey, s);
                } else {
                    thresholdInput.setStyle(FxmlControl.badStyle);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkQuality() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            if ("jpg".equals(selected.getText())) {
                qualityBox.setDisable(false);
            } else {
                qualityInput.setStyle(null);
                qualityBox.setDisable(true);
                fullQuality.setSelected(true);
                imageAttributes.setQuality(100);
                return;
            }
            selected = (RadioButton) QualityGroup.getSelectedToggle();
            String s = selected.getText();
            qualityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(qualityInput.getText());
                if (inputValue >= 0 && inputValue <= 100) {
                    AppVaribles.setUserConfigValue(ImageQualityInputKey, inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue >= 0) {
                    imageAttributes.setQuality(inputValue);
                    AppVaribles.setUserConfigValue(ImageQualityKey, s);
                } else {
                    qualityInput.setStyle(FxmlControl.badStyle);
                }
            } else {
                imageAttributes.setQuality(Integer.parseInt(s.substring(0, s.length() - 1)));
                AppVaribles.setUserConfigValue(ImageQualityKey, s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
