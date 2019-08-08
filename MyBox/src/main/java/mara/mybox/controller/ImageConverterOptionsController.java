package mara.mybox.controller;

import java.awt.color.ICC_Profile;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import mara.mybox.controller.base.BaseController;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.fxml.FxmlControl.warnStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getUserConfigBoolean;
import static mara.mybox.value.AppVaribles.getUserConfigValue;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.setUserConfigValue;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterOptionsController extends BaseController {

    protected final String ImageConverterFormatKey, ImageConverterColorKey, ImageConverterCompressionTypeKey;
    protected final String ImageConverterQualityKey, ImageConverterDpiKey, ImageConverterAlphaKey, ImageConverterEmbedKey;
    protected final String ImageConverterBinaryKey, ImageConverterThreasholdKey, ImageConverterDitherKey;
    protected final String ImageConverterAppendColorKey, ImageConverterAppendCompressionKey, ImageConverterAppendQualityKey;
    protected ImageAttributes attributes;

    @FXML
    protected ComboBox<String> formatSelector, colorSpaceSelector, alphaSelector, compressionSelector,
            qualitySelector, dpiSelector;
    @FXML
    protected ToggleGroup binaryGroup;
    @FXML
    protected TextField profileInput, thresholdInput;
    @FXML
    protected CheckBox ditherCheck;
    @FXML
    protected CheckBox embedProfileCheck, alphaCheck;
    @FXML
    protected HBox attrBox1, attrBox2, binBox, alphaBox, compressBox, profileBox, binaryBox, dpiBox;
    @FXML
    protected Button iccSelectButton;
    @FXML
    protected Label formatCommentsLabel;

    public ImageConverterOptionsController() {
        baseTitle = AppVaribles.message("ImageConverterBatch");

        ImageConverterFormatKey = "ImageConverterFormatKey";
        ImageConverterColorKey = "ImageConverterColorKey";
        ImageConverterCompressionTypeKey = "ImageConverterCompressionTypeKey";
        ImageConverterQualityKey = "ImageConverterQualityKey";
        ImageConverterAlphaKey = "ImageConverterAlphaKey";
        ImageConverterBinaryKey = "ImageConverterBinaryKey";
        ImageConverterThreasholdKey = "ImageConverterThreasholdKey";
        ImageConverterEmbedKey = "ImageConverterEmbedKey";
        ImageConverterDitherKey = "ImageConverterDitherKey";
        ImageConverterAppendColorKey = "ImageConverterDitherKey";
        ImageConverterAppendCompressionKey = "ImageConverterAppendCompressionKey";
        ImageConverterAppendQualityKey = "ImageConverterAppendQualityKey";
        ImageConverterDpiKey = "ImageConverterDpiKey";

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            formatSelector.getItems().addAll(Arrays.asList(
                    "png", "jpg", "jpeg2000", "tif", "gif", "bmp", "pcx", "pnm", "wbmp", "raw"
            ));
            formatSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkFileFormat();
                }
            });

            colorSpaceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkColorSpace();
                }
            });

            embedProfileCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    checkEmbed();
                }
            });

            profileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkProfile();
                }
            });

            alphaSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkAlpha();
                }
            });

            compressionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkCompression();
                }
            });

            qualitySelector.getItems().addAll(Arrays.asList(
                    "100", "90", "80", "75", "60", "50", "30", "10"
            ));
            qualitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkQuality();
                }
            });

            binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkBinary();
                }
            });

            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkThreshold();
                }
            });

            ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    checkDither();
                }
            });

            isSettingValues = true;
            embedProfileCheck.setSelected(getUserConfigBoolean(ImageConverterEmbedKey, true));
            FxmlControl.setRadioSelected(binaryGroup, getUserConfigValue(ImageConverterBinaryKey, message("OTSU")));
            thresholdInput.setText(getUserConfigValue(ImageConverterThreasholdKey, null));
            ditherCheck.setSelected(getUserConfigBoolean(ImageConverterDitherKey, true));
            isSettingValues = false;
            formatSelector.getSelectionModel().select(getUserConfigValue(ImageConverterFormatKey, "png"));

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void initDpiBox(boolean include) {
        if (include) {
            dpiSelector.getItems().addAll(Arrays.asList(
                    "96", "72", "300", "160", "240", "120", "600", "400"
            ));
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkDpi();
                }
            });
            dpiSelector.getSelectionModel().select(getUserConfigValue(ImageConverterDpiKey, "96"));

        } else {
            FxmlControl.setEditorNormal(dpiSelector);
            attrBox2.getChildren().remove(dpiBox);
        }
    }

    public void checkFileFormat() {
        if (isSettingValues) {
            return;
        }
        attributes = new ImageAttributes();

        String format = formatSelector.getSelectionModel().getSelectedItem();
        attributes.setImageFormat(format);
        setUserConfigValue(ImageConverterFormatKey, format);

        if ("pcx".equals(format)) {
            formatCommentsLabel.setText(message("PcxComments"));
            formatCommentsLabel.setStyle(warnStyle);
        } else {
            formatCommentsLabel.setText("");
            formatCommentsLabel.setStyle(null);
        }

        List<String> csList = new ArrayList();
        if ("wbmp".equals(format)) {
            csList.add(message("BlackOrWhite"));
        } else {
            csList.addAll(ImageValue.RGBColorSpaces);
            if ("raw".equals(format) || CommonValues.CMYKImages.contains(format)) {
                csList.addAll(ImageValue.CMYKColorSpaces);
            }
            csList.addAll(Arrays.asList(message("Gray"), message("BlackOrWhite"), message("IccProfile")));
        }
        isSettingValues = true;
        colorSpaceSelector.getItems().clear();
        colorSpaceSelector.getItems().addAll(csList);
        String dcs = getUserConfigValue(ImageConverterColorKey, "sRGB");
        if (csList.contains(dcs)) {
            colorSpaceSelector.getSelectionModel().select(dcs);
        } else {
            colorSpaceSelector.getSelectionModel().select(0);
        }
        isSettingValues = false;
        checkColorSpace();

        switch (format) {
            case "tif":
                if (!attrBox1.getChildren().contains(embedProfileCheck)) {
                    attrBox1.getChildren().add(embedProfileCheck);
                }
                embedProfileCheck.setDisable(true);
                embedProfileCheck.setSelected(true);
                break;
            case "png":
            case "jpg":
                if (!attrBox1.getChildren().contains(embedProfileCheck)) {
                    attrBox1.getChildren().add(embedProfileCheck);
                }
                embedProfileCheck.setDisable(false);
                break;
            default:
                if (attrBox1.getChildren().contains(embedProfileCheck)) {
                    attrBox1.getChildren().remove(embedProfileCheck);
                }
                break;
        }

        checkDpi();

    }

    public void checkColorSpace() {
        if (isSettingValues) {
            return;
        }
        String colorSpace = colorSpaceSelector.getSelectionModel().getSelectedItem();
        attributes.setColorSpaceName(colorSpace);
        attributes.setProfile(null);
        attributes.setProfileName(null);
        setUserConfigValue(ImageConverterColorKey, colorSpace);

        if (message("IccProfile").equals(colorSpace)) {
            if (!attrBox1.getChildren().contains(profileBox)) {
                attrBox1.getChildren().add(profileBox);
            }
            checkProfile();
        } else {
            profileInput.setStyle(null);
            if (attrBox1.getChildren().contains(profileBox)) {
                attrBox1.getChildren().remove(profileBox);
            }
        }
        if (message("BlackOrWhite").equals(colorSpace)) {
            if (!thisPane.getChildren().contains(binBox)) {
                thisPane.getChildren().add(binBox);
            }
            if (attrBox1.getChildren().contains(embedProfileCheck)) {
                attrBox1.getChildren().remove(embedProfileCheck);
            }
            checkBinary();
        } else {
            if (thisPane.getChildren().contains(binBox)) {
                thisPane.getChildren().remove(binBox);
            }
            if (!attrBox1.getChildren().contains(embedProfileCheck)) {
                attrBox1.getChildren().add(embedProfileCheck);
            }
            thresholdInput.setStyle(null);
        }

        isSettingValues = true;
        alphaSelector.getItems().clear();
        if (CommonValues.AlphaImages.contains(attributes.getImageFormat())
                && !message("BlackOrWhite").equals(colorSpace)) {
            alphaSelector.getItems().addAll(Arrays.asList(
                    message("Keep"), message("Remove"), message("PremultipliedAndKeep"), message("PremultipliedAndRemove")
            ));
        } else {
            alphaSelector.getItems().addAll(Arrays.asList(
                    message("Remove"), message("PremultipliedAndRemove")
            ));
        }
        String a = getUserConfigValue(ImageConverterAlphaKey, message("Keep"));
        if (alphaSelector.getItems().contains(a)) {
            alphaSelector.getSelectionModel().select(a);
        } else {
            alphaSelector.getSelectionModel().select(0);
        }
        isSettingValues = false;

        checkEmbed();

        checkAlpha();

    }

    public void checkProfile() {
        if (!attrBox1.getChildren().contains(profileBox)) {
            return;
        }
        try {
            File file = new File(profileInput.getText());
            ICC_Profile profile = ImageValue.iccProfile(file.getAbsolutePath());
            if (profile == null) {
                profileInput.setStyle(badStyle);
            } else {
                profileInput.setStyle(null);
                attributes.setProfile(profile);
                attributes.setProfileName(FileTools.getFilePrefix(file));
                attributes.setEmbedProfile(embedProfileCheck.isSelected());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void checkEmbed() {
        if (isSettingValues || attributes == null) {
            return;
        }
        attributes.setEmbedProfile(embedProfileCheck.isSelected());
        setUserConfigValue(ImageConverterEmbedKey, embedProfileCheck.isSelected());
    }

    public void checkAlpha() {
        if (isSettingValues) {
            return;
        }
        String alpha = alphaSelector.getSelectionModel().getSelectedItem();
        if (message("Keep").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.Keep);
        } else if (message("Remove").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.Remove);
        } else if (message("PremultipliedAndKeep").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.PremultipliedAndKeep);
        } else if (message("PremultipliedAndRemove").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.PremultipliedAndRemove);
        }
        setUserConfigValue(ImageConverterAlphaKey, alpha);

        boolean hasAlpha = attributes.getAlpha() == ImageAttributes.Alpha.Keep
                || attributes.getAlpha() == ImageAttributes.Alpha.PremultipliedAndKeep;
        String[] compressionTypes = ImageValue.getCompressionTypes(attributes.getImageFormat(),
                attributes.getColorSpaceName(), hasAlpha);
        if (compressionTypes != null && compressionTypes.length > 0) {
            isSettingValues = true;
            compressionSelector.getItems().clear();
            compressionSelector.getItems().addAll(Arrays.asList(compressionTypes));
            String c = getUserConfigValue(ImageConverterCompressionTypeKey, "Deflate");
            if (compressionSelector.getItems().contains(c)) {
                compressionSelector.getSelectionModel().select(c);
            } else {
                compressionSelector.getSelectionModel().select(0);
            }
            String q = getUserConfigValue(ImageConverterQualityKey, "100");
            qualitySelector.getSelectionModel().select(q);
            if (!attrBox2.getChildren().contains(compressBox)) {
                attrBox2.getChildren().add(compressBox);
            }
            isSettingValues = false;
            checkCompression();
            checkQuality();
        } else {
            isSettingValues = true;
            compressionSelector.getItems().clear();
            FxmlControl.setEditorNormal(qualitySelector);
            if (attrBox2.getChildren().contains(compressBox)) {
                attrBox2.getChildren().remove(compressBox);
            }
            isSettingValues = false;
            attributes.setCompressionType(null);
            attributes.setQuality(100);
        }
    }

    public void checkCompression() {
        if (isSettingValues) {
            return;
        }
        String v = compressionSelector.getValue();
        attributes.setCompressionType(v);
        setUserConfigValue(ImageConverterCompressionTypeKey, v);
    }

    public void checkQuality() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.valueOf(qualitySelector.getValue());
            if (v > 0 && v <= 100) {
                attributes.setQuality(v);
                FxmlControl.setEditorNormal(qualitySelector);
                setUserConfigValue(ImageConverterQualityKey, v + "");
            } else {
                FxmlControl.setEditorBadStyle(qualitySelector);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            FxmlControl.setEditorBadStyle(qualitySelector);
        }
    }

    public void checkBinary() {
        try {
            if (isSettingValues || !thisPane.getChildren().contains(binBox)) {
                return;
            }
            RadioButton selected = (RadioButton) binaryGroup.getSelectedToggle();
            String s = selected.getText();
            if (message("Threshold").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_THRESHOLD);
                thresholdInput.setDisable(false);
                checkThreshold();
            } else if (message("OTSU").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_OTSU);
                thresholdInput.setStyle(null);
                thresholdInput.setDisable(true);
            } else {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.DEFAULT);
                thresholdInput.setStyle(null);
                thresholdInput.setDisable(true);
            }
            setUserConfigValue(ImageConverterBinaryKey, s);

            checkDither();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkThreshold() {
        try {
            if (!thisPane.getChildren().contains(binBox)) {
                return;
            }
            int inputValue = Integer.parseInt(thresholdInput.getText());
            if (inputValue >= 0 && inputValue <= 255) {
                attributes.setThreshold(inputValue);
                thresholdInput.setStyle(null);
                AppVaribles.setUserConfigValue(ImageConverterThreasholdKey, inputValue + "");
            } else {
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdInput.setStyle(badStyle);
        }
    }

    public void checkDither() {
        if (isSettingValues || attributes == null) {
            return;
        }
        attributes.setIsDithering(ditherCheck.isSelected());
        setUserConfigValue(ImageConverterDitherKey, ditherCheck.isSelected());
    }

    public void checkDpi() {
        if (isSettingValues || !attrBox2.getChildren().contains(dpiBox)) {
            return;
        }
        try {
            int v = Integer.valueOf(dpiSelector.getValue());
            if (v > 0) {
                attributes.setDensity(v);
                FxmlControl.setEditorNormal(dpiSelector);
                setUserConfigValue(ImageConverterDpiKey, v + "");
            } else {
                FxmlControl.setEditorBadStyle(dpiSelector);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(dpiSelector);
        }
    }

    @FXML
    public void popIccFile(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
                return VisitHistory.getRecentFile(VisitHistory.FileType.Icc, fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
                return VisitHistory.getRecentPath(VisitHistory.FileType.Icc, pathNumber);
            }

            @Override
            public void handleSelect() {
                selectIccAction();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                iccFileSelected(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void selectIccAction() {
        selectIccFile(AppVaribles.getUserConfigPath(sourcePathKey));
    }

    public void selectIccFile(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path != null && path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.IccProfileExtensionFilter);
            File file = fileChooser.showOpenDialog(myStage);
            if (file == null || !file.exists()) {
                return;
            }
            iccFileSelected(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void iccFileSelected(File file) {
        profileInput.setText(file.getAbsolutePath());
        recordFileOpened(file, VisitHistory.FileType.Icc, VisitHistory.FileType.Icc);
    }

}
