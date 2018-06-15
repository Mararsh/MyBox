/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.ImageTools;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-15
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAttributesController extends BaseController {

    @FXML
    private Pane imageAttributesPane;

    @FXML
    private ToggleGroup ImageFormatGroup;
    @FXML
    private RadioButton RGB;
    @FXML
    private ToggleGroup ImageColorGroup;
    @FXML
    private RadioButton ARGB;
    @FXML
    private ToggleGroup DensityGroup;
    @FXML
    private TextField densityInput;
    @FXML
    private HBox qualityBox;
    @FXML
    private ToggleGroup QualityGroup;
    @FXML
    private TextField qualityInput;
    @FXML
    private Button previewButton;
    @FXML
    private HBox compressBox;
    @FXML
    private ToggleGroup CompressionGroup;
    @FXML
    private HBox colorBox;
    @FXML
    private ToggleGroup ColorConversionGroup;
    @FXML
    private TextField thresholdInput;

    private ImageType imageColor;
    private int quality, threshold, density;
    private String imageFormat, compressionType;
    private int colorConversion;

    public static class ColorConversion {

        public static int DEFAULT = 0;
        public static int OTSU = 1;
        public static int THRESHOLD = 9;
    }

    @Override
    protected void initStage2() {

        FxmlTools.setNonnegativeValidation(densityInput);

        ImageFormatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageFormat();
            }
        });
        FxmlTools.setRadioSelected(ImageFormatGroup, AppVaribles.getConfigValue("imageFormat", null));
        checkImageFormat();

        DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkDensity();
            }
        });
        FxmlTools.setRadioSelected(DensityGroup, AppVaribles.getConfigValue("density", null));
        checkDensity();

        densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkDensity();
            }
        });
        densityInput.setText(AppVaribles.getConfigValue("densityInput", null));

        ImageColorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageColor();
            }
        });
        FxmlTools.setRadioSelected(ImageColorGroup, AppVaribles.getConfigValue("imageColor", null));
        checkImageColor();

        QualityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkQuality();
            }
        });
        FxmlTools.setRadioSelected(QualityGroup, AppVaribles.getConfigValue("quality", null));
        checkQuality();

        qualityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkQuality();
            }
        });
        qualityInput.setText(AppVaribles.getConfigValue("qualityInput", null));

        ColorConversionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkColorConversion();
            }
        });
        FxmlTools.setRadioSelected(ColorConversionGroup, AppVaribles.getConfigValue("colorConversion", null));

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkColorConversion();
            }
        });
        thresholdInput.setText(AppVaribles.getConfigValue("thresholdInput", null));
    }

    @FXML
    private void showHelp(ActionEvent event) {
        try {
            File help = FileTools.getHelpFile(getClass(), "/docs/ImageHelp.html", "ImageHelp.html");
            Desktop.getDesktop().browse(help.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkImageFormat() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            imageFormat = selected.getText();
            AppVaribles.setConfigValue("imageFormat", imageFormat);

            String[] compressionTypes = ImageTools.getCompressionTypes(imageFormat, imageColor);
            checkCompressionTypes(compressionTypes);

            if (compressionTypes == null) {
                qualityBox.setDisable(true);
            } else {
                qualityBox.setDisable(false);
                if (qualityInput.getStyle().equals(FxmlTools.badStyle)) {
                    FxmlTools.setRadioFirstSelected(QualityGroup);
                }
            }

            if ("jpg".equals(imageFormat) || "bmp".equals(imageFormat)) {
                ARGB.setDisable(true);
                if (ARGB.isSelected()) {
                    RGB.setSelected(true);
                }
            } else {
                ARGB.setDisable(false);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkImageColor() {
        try {
            RadioButton selected = (RadioButton) ImageColorGroup.getSelectedToggle();
            String s = selected.getText();
            AppVaribles.setConfigValue("imageColor", s);
            if (getMessage("Color").equals(s)) {
                imageColor = ImageType.RGB;
            } else if (getMessage("ColorAlpha").equals(s)) {
                imageColor = ImageType.ARGB;
            } else if (getMessage("ShadesOfGray").equals(s)) {
                imageColor = ImageType.GRAY;
            } else if (getMessage("BlackOrWhite").equals(s)) {
                imageColor = ImageType.BINARY;
            } else {
                imageColor = ImageType.RGB;
            }

//            if ("tif".equals(imageFormat) || "bmp".equals(imageFormat)) {
            String[] compressionTypes = ImageTools.getCompressionTypes(imageFormat, imageColor);
            checkCompressionTypes(compressionTypes);
//            }

            if (imageColor == ImageType.BINARY) {
                colorBox.setDisable(false);
                checkColorConversion();
            } else {
                colorBox.setDisable(true);
                if (thresholdInput.getStyle().equals(FxmlTools.badStyle)) {
                    FxmlTools.setRadioFirstSelected(ColorConversionGroup);
                }
            }

//            logger.debug(imageColor);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCompressionTypes(String[] types) {
        compressBox.getChildren().removeAll(compressBox.getChildren());
        CompressionGroup = new ToggleGroup();
        if (types == null) {
            RadioButton newv = new RadioButton("none");
            newv.setToggleGroup(CompressionGroup);
            compressBox.getChildren().add(newv);
            newv.setSelected(true);
            qualityBox.setDisable(true);
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
            qualityBox.setDisable(false);
        }

        CompressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkCompressionType();
            }
        });
        FxmlTools.setRadioSelected(CompressionGroup, AppVaribles.getConfigValue("compressionType", null));
        checkCompressionType();
    }

    private void checkDensity() {
        try {
            RadioButton selected = (RadioButton) DensityGroup.getSelectedToggle();
            String s = selected.getText();
            densityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(densityInput.getText());
                if (inputValue > 0) {
                    AppVaribles.setConfigValue("densityInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue > 0) {
                    density = inputValue;
                    AppVaribles.setConfigValue("density", s);
                } else {
                    densityInput.setStyle(FxmlTools.badStyle);
                }

            } else {
                density = Integer.parseInt(s.substring(0, s.length() - 3));
                AppVaribles.setConfigValue("density", s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCompressionType() {
        try {
            RadioButton selected = (RadioButton) CompressionGroup.getSelectedToggle();
            compressionType = selected.getText();
            AppVaribles.setConfigValue("compressionType", compressionType);
        } catch (Exception e) {
            compressionType = null;
        }
    }

    private void checkColorConversion() {
        threshold = -1;
        thresholdInput.setStyle(null);
        try {
            RadioButton selected = (RadioButton) ColorConversionGroup.getSelectedToggle();
            String s = selected.getText();

            if (getMessage("Threshold").equals(s)) {
                colorConversion = PdfConvertPicturesController.ColorConversion.THRESHOLD;
            } else if (getMessage("OTSU").equals(s)) {
                colorConversion = PdfConvertPicturesController.ColorConversion.OTSU;
                AppVaribles.setConfigValue("colorConversion", s);
            } else {
                colorConversion = PdfConvertPicturesController.ColorConversion.DEFAULT;
                AppVaribles.setConfigValue("colorConversion", s);
            }

            int inputValue;
            try {
                inputValue = Integer.parseInt(thresholdInput.getText());
                if (inputValue >= 0 && inputValue <= 100) {
                    AppVaribles.setConfigValue("thresholdInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }

            if (colorConversion == PdfConvertPicturesController.ColorConversion.THRESHOLD) {
                if (inputValue >= 0) {
                    threshold = inputValue;
                    AppVaribles.setConfigValue("colorConversion", s);
                } else {
                    thresholdInput.setStyle(FxmlTools.badStyle);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkQuality() {
        try {
            RadioButton selected = (RadioButton) QualityGroup.getSelectedToggle();
            String s = selected.getText();
            qualityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(qualityInput.getText());
                if (inputValue >= 0 && inputValue <= 100) {
                    AppVaribles.setConfigValue("qualityInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue >= 0) {
                    quality = inputValue;
                    AppVaribles.setConfigValue("quality", s);
                } else {
                    qualityInput.setStyle(FxmlTools.badStyle);
                }
            } else {
                quality = Integer.parseInt(s.substring(0, s.length() - 1));
                AppVaribles.setConfigValue("quality", s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public Pane getImageAttributesPane() {
        return imageAttributesPane;
    }

    public void setImageAttributesPane(Pane imageAttributesPane) {
        this.imageAttributesPane = imageAttributesPane;
    }

    public ToggleGroup getImageFormatGroup() {
        return ImageFormatGroup;
    }

    public void setImageFormatGroup(ToggleGroup ImageFormatGroup) {
        this.ImageFormatGroup = ImageFormatGroup;
    }

    public RadioButton getRGB() {
        return RGB;
    }

    public void setRGB(RadioButton RGB) {
        this.RGB = RGB;
    }

    public ToggleGroup getImageColorGroup() {
        return ImageColorGroup;
    }

    public void setImageColorGroup(ToggleGroup ImageColorGroup) {
        this.ImageColorGroup = ImageColorGroup;
    }

    public RadioButton getARGB() {
        return ARGB;
    }

    public void setARGB(RadioButton ARGB) {
        this.ARGB = ARGB;
    }

    public ToggleGroup getDensityGroup() {
        return DensityGroup;
    }

    public void setDensityGroup(ToggleGroup DensityGroup) {
        this.DensityGroup = DensityGroup;
    }

    public TextField getDensityInput() {
        return densityInput;
    }

    public void setDensityInput(TextField densityInput) {
        this.densityInput = densityInput;
    }

    public HBox getQualityBox() {
        return qualityBox;
    }

    public void setQualityBox(HBox qualityBox) {
        this.qualityBox = qualityBox;
    }

    public ToggleGroup getQualityGroup() {
        return QualityGroup;
    }

    public void setQualityGroup(ToggleGroup QualityGroup) {
        this.QualityGroup = QualityGroup;
    }

    public TextField getQualityInput() {
        return qualityInput;
    }

    public void setQualityInput(TextField qualityInput) {
        this.qualityInput = qualityInput;
    }

    public Button getPreviewButton() {
        return previewButton;
    }

    public void setPreviewButton(Button previewButton) {
        this.previewButton = previewButton;
    }

    public HBox getCompressBox() {
        return compressBox;
    }

    public void setCompressBox(HBox compressBox) {
        this.compressBox = compressBox;
    }

    public ToggleGroup getCompressionGroup() {
        return CompressionGroup;
    }

    public void setCompressionGroup(ToggleGroup CompressionGroup) {
        this.CompressionGroup = CompressionGroup;
    }

    public HBox getColorBox() {
        return colorBox;
    }

    public void setColorBox(HBox colorBox) {
        this.colorBox = colorBox;
    }

    public ToggleGroup getColorConversionGroup() {
        return ColorConversionGroup;
    }

    public void setColorConversionGroup(ToggleGroup ColorConversionGroup) {
        this.ColorConversionGroup = ColorConversionGroup;
    }

    public TextField getThresholdInput() {
        return thresholdInput;
    }

    public void setThresholdInput(TextField thresholdInput) {
        this.thresholdInput = thresholdInput;
    }

    public ImageType getImageColor() {
        return imageColor;
    }

    public void setImageColor(ImageType imageColor) {
        this.imageColor = imageColor;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getDensity() {
        return density;
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public int getColorConversion() {
        return colorConversion;
    }

    public void setColorConversion(int colorConversion) {
        this.colorConversion = colorConversion;
    }

    @Override
    public Stage getThisStage() {
        if (thisStage == null) {
            if (imageAttributesPane != null && imageAttributesPane.getScene() != null) {
                thisStage = (Stage) imageAttributesPane.getScene().getWindow();
            }
        }
        return thisStage;
    }
}
