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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.fxml.FxmlControl.warnStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigValue;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterOptionsController extends BaseController {

    protected ImageAttributes attributes;
    protected boolean includeDpi;

    @FXML
    protected ComboBox<String> colorSpaceSelector, compressionSelector, qualitySelector, dpiSelector, icoWidthSelector;
    @FXML
    protected ToggleGroup formatGroup, alphaGroup, binaryGroup;
    @FXML
    protected TextField profileInput, thresholdInput;
    @FXML
    protected CheckBox ditherCheck;
    @FXML
    protected CheckBox embedProfileCheck, alphaCheck;
    @FXML
    protected FlowPane formatPane, colorspacePane, profilePane, alphaPane, dpiPane, compressPane, qualityPane, icoPane;
    @FXML
    protected VBox csBox, compressBox, binaryBox;
    @FXML
    protected Button iccSelectButton;
    @FXML
    protected RadioButton alphaKeepRadio, alphaRemoveRadio, alphaPreKeepRadio, alphaPreReomveRadio;
    @FXML
    protected Label formatCommentsLabel;

    public ImageConverterOptionsController() {
        baseTitle = AppVariables.message("ImageConverterBatch");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldV, Toggle newV) {
                    checkFileFormat();
                }
            });
            checkFileFormat();

            colorSpaceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v,
                        String oldV, String newV) {
                    checkColorSpace();
                }
            });
            checkColorSpace();

            embedProfileCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    checkEmbed();
                }
            });

            profileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldv, String newv) {
                    checkProfile();
                }
            });

            alphaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldV, Toggle newV) {
                    checkAlpha();
                }
            });
            checkAlpha();

            compressionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v,
                        String oldV, String newV) {
                    checkCompression();
                }
            });

            qualitySelector.getItems().addAll(Arrays.asList(
                    "100", "90", "80", "75", "60", "50", "30", "10"
            ));
            qualitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v,
                        String oldV, String newV) {
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
                public void changed(ObservableValue<? extends String> ov,
                        String oldv, String newv) {
                    checkThreshold();
                }
            });

            ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    checkDither();
                }
            });

            icoWidthSelector.getItems().addAll(Arrays.asList(
                    "45", "40", "30", "50", "25", "80", "120", "24", "64", "128", "256", "512", "48", "96", "144"
            ));
            icoWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v,
                        String oldV, String newV) {
                    checkIcoWidth();
                }
            });
            icoWidthSelector.getSelectionModel().select(getUserConfigValue("ImageConverterIcoWidth", "45"));

            isSettingValues = true;
            embedProfileCheck.setSelected(getUserConfigBoolean("ImageConverterEmbed", true));
            FxmlControl.setRadioSelected(binaryGroup, getUserConfigValue("ImageConverterBinary", message("OTSU")));
            thresholdInput.setText(getUserConfigValue("ImageConverterThreashold", null));
            ditherCheck.setSelected(getUserConfigBoolean("ImageConverterDither", true));
            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void initDpiBox(boolean include) {
        includeDpi = include;
        if (include) {
            dpiSelector.getItems().addAll(Arrays.asList(
                    "96", "72", "300", "160", "240", "120", "600", "400"
            ));
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v,
                        String oldV, String newV) {
                    checkDpi();
                }
            });
            dpiSelector.getSelectionModel().select(getUserConfigValue("ImageConverterDpi", "96"));

        } else {
            FxmlControl.setEditorNormal(dpiSelector);
            thisPane.getChildren().remove(dpiPane);
        }
    }

    public void checkFileFormat() {
        if (isSettingValues) {
            return;
        }
        attributes = new ImageAttributes();

        String format = ((RadioButton) formatGroup.getSelectedToggle()).getText();
        attributes.setImageFormat(format);
        setUserConfigValue("ImageConverterFormat", format);

        if ("pcx".equals(format)) {
            formatCommentsLabel.setText(message("PcxComments"));
            formatCommentsLabel.setStyle(warnStyle);
        } else {
            formatCommentsLabel.setText("");
            formatCommentsLabel.setStyle(null);
        }

        FxmlControl.setEditorNormal(dpiSelector);
        FxmlControl.setEditorNormal(qualitySelector);
        FxmlControl.setEditorNormal(icoWidthSelector);
        if ("ico".equals(format)) {
            thisPane.getChildren().clear();
            thisPane.getChildren().addAll(formatPane, icoPane);
            return;
        } else {
            thisPane.getChildren().clear();
            thisPane.getChildren().addAll(formatPane, colorspacePane, csBox, embedProfileCheck, alphaPane);
            if (includeDpi) {
                thisPane.getChildren().add(dpiPane);
            }
            thisPane.getChildren().addAll(compressBox, binaryBox);
        }

        List<String> csList = new ArrayList<>();
        switch (format) {
            case "wbmp":
                csList.add(message("BlackOrWhite"));
                break;
            default:
                csList.addAll(ImageValue.RGBColorSpaces);
                if ("raw".equals(format) || CommonValues.CMYKImages.contains(format)) {
                    csList.addAll(ImageValue.CMYKColorSpaces);
                }
                csList.addAll(Arrays.asList(message("Gray"), message("BlackOrWhite"), message("IccProfile")));
                break;
        }
        isSettingValues = true;
        colorSpaceSelector.getItems().clear();
        colorSpaceSelector.getItems().addAll(csList);
        String dcs = getUserConfigValue("ImageConverterColor", "sRGB");
        if (csList.contains(dcs)) {
            colorSpaceSelector.getSelectionModel().select(dcs);
        } else {
            colorSpaceSelector.getSelectionModel().select(0);
        }
        isSettingValues = false;
        checkColorSpace();

        switch (format) {
            case "tif":
            case "tiff":
                if (!csBox.getChildren().contains(embedProfileCheck)) {
                    csBox.getChildren().add(embedProfileCheck);
                }
                embedProfileCheck.setDisable(true);
                embedProfileCheck.setSelected(true);
                break;
            case "png":
            case "jpg":
            case "jpeg":
                if (!csBox.getChildren().contains(embedProfileCheck)) {
                    csBox.getChildren().add(embedProfileCheck);
                }
                embedProfileCheck.setDisable(false);
                break;
            default:
                if (csBox.getChildren().contains(embedProfileCheck)) {
                    csBox.getChildren().remove(embedProfileCheck);
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
        if (colorSpace == null) {
            return;
        }
        attributes.setColorSpaceName(colorSpace);
        attributes.setProfile(null);
        attributes.setProfileName(null);
        setUserConfigValue("ImageConverterColor", colorSpace);

        if (message("IccProfile").equals(colorSpace)) {
            if (!csBox.getChildren().contains(profilePane)) {
                csBox.getChildren().add(profilePane);
                FxmlControl.refreshStyle(profilePane);
            }
            checkProfile();
        } else {
            profileInput.setStyle(null);
            if (csBox.getChildren().contains(profilePane)) {
                csBox.getChildren().remove(profilePane);
            }
        }
        if (message("BlackOrWhite").equals(colorSpace)) {
            if (!thisPane.getChildren().contains(binaryBox)) {
                thisPane.getChildren().add(binaryBox);
                FxmlControl.refreshStyle(binaryBox);
            }
            if (csBox.getChildren().contains(embedProfileCheck)) {
                csBox.getChildren().remove(embedProfileCheck);
            }
            checkBinary();
        } else {
            if (thisPane.getChildren().contains(binaryBox)) {
                thisPane.getChildren().remove(binaryBox);
            }
            if (!csBox.getChildren().contains(embedProfileCheck)) {
                csBox.getChildren().add(embedProfileCheck);
            }
            thresholdInput.setStyle(null);
        }

        if (CommonValues.PremultiplyAlphaImages.contains(attributes.getImageFormat())
                && !message("BlackOrWhite").equals(colorSpace)) {
            alphaKeepRadio.setDisable(false);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(false);
            alphaPreReomveRadio.setDisable(false);
            alphaKeepRadio.fire();
        } else if (CommonValues.AlphaImages.contains(attributes.getImageFormat())
                && !message("BlackOrWhite").equals(colorSpace)) {
            alphaKeepRadio.setDisable(false);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(true);
            alphaPreReomveRadio.setDisable(false);
            alphaKeepRadio.fire();
        } else {
            alphaKeepRadio.setDisable(true);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(true);
            alphaPreReomveRadio.setDisable(false);
            alphaRemoveRadio.fire();
        }

        checkEmbed();

        checkAlpha();

    }

    public void checkProfile() {
        if (!csBox.getChildren().contains(profilePane)) {
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
        setUserConfigValue("ImageConverterEmbed", embedProfileCheck.isSelected());
    }

    public void checkAlpha() {
        if (isSettingValues) {
            return;
        }
        String alpha = ((RadioButton) alphaGroup.getSelectedToggle()).getText();
        if (message("Keep").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.Keep);
        } else if (message("Remove").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.Remove);
        } else if (message("PremultipliedAndKeep").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.PremultipliedAndKeep);
        } else if (message("PremultipliedAndRemove").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.PremultipliedAndRemove);
        }
        setUserConfigValue("ImageConverterAlpha", alpha);

        boolean hasAlpha = attributes.getAlpha() == ImageAttributes.Alpha.Keep
                || attributes.getAlpha() == ImageAttributes.Alpha.PremultipliedAndKeep;
        String[] compressionTypes = ImageValue.getCompressionTypes(attributes.getImageFormat(),
                attributes.getColorSpaceName(), hasAlpha);
        if (compressionTypes != null && compressionTypes.length > 0) {
            isSettingValues = true;
            compressionSelector.getItems().clear();
            compressionSelector.getItems().addAll(Arrays.asList(compressionTypes));
            String c = getUserConfigValue("ImageConverterCompressionType", "Deflate");
            if (compressionSelector.getItems().contains(c)) {
                compressionSelector.getSelectionModel().select(c);
            } else {
                compressionSelector.getSelectionModel().select(0);
            }
            String q = getUserConfigValue("ImageConverterQuality", "100");
            qualitySelector.getSelectionModel().select(q);
            if (!thisPane.getChildren().contains(compressBox)) {
                thisPane.getChildren().add(compressBox);
            }
            isSettingValues = false;
            checkCompression();
            checkQuality();
        } else {
            isSettingValues = true;
            compressionSelector.getItems().clear();
            FxmlControl.setEditorNormal(qualitySelector);
            if (thisPane.getChildren().contains(compressBox)) {
                thisPane.getChildren().remove(compressBox);
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
        setUserConfigValue("ImageConverterCompressionType", v);
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
                setUserConfigValue("ImageConverterQuality", v + "");
            } else {
                FxmlControl.setEditorBadStyle(qualitySelector);
            }
        } catch (Exception e) {
//            logger.debug(e.toString());
            FxmlControl.setEditorBadStyle(qualitySelector);
        }
    }

    public void checkIcoWidth() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.valueOf(icoWidthSelector.getValue());
            if (v > 0) {
                attributes.setWidth(v);
                FxmlControl.setEditorNormal(icoWidthSelector);
                setUserConfigValue("ImageConverterIcoWidth", v + "");
            } else {
                FxmlControl.setEditorBadStyle(icoWidthSelector);
            }
        } catch (Exception e) {
//            logger.debug(e.toString());
            FxmlControl.setEditorBadStyle(icoWidthSelector);
        }
    }

    public void checkBinary() {
        try {
            if (isSettingValues || !thisPane.getChildren().contains(binaryBox)) {
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
            setUserConfigValue("ImageConverterBinary", s);

            checkDither();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkThreshold() {
        try {
            if (!thisPane.getChildren().contains(binaryBox)) {
                return;
            }
            int inputValue = Integer.parseInt(thresholdInput.getText());
            if (inputValue >= 0 && inputValue <= 255) {
                attributes.setThreshold(inputValue);
                thresholdInput.setStyle(null);
                AppVariables.setUserConfigValue("ImageConverterThreashold", inputValue + "");
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
        setUserConfigValue("ImageConverterDither", ditherCheck.isSelected());
    }

    public void checkDpi() {
        if (isSettingValues || !thisPane.getChildren().contains(dpiPane)) {
            return;
        }
        try {
            int v = Integer.valueOf(dpiSelector.getValue());
            if (v > 0) {
                attributes.setDensity(v);
                FxmlControl.setEditorNormal(dpiSelector);
                setUserConfigValue("ImageConverterDpi", v + "");
            } else {
                FxmlControl.setEditorBadStyle(dpiSelector);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(dpiSelector);
        }
    }

    @FXML
    public void popIccFile(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVariables.fileRecentNumber * 2 / 3 + 1;
                return VisitHistory.getRecentFile(VisitHistory.FileType.Icc, fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
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
        selectIccFile(AppVariables.getUserConfigPath(sourcePathKey));
    }

    public void selectIccFile(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path != null && path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonFxValues.IccProfileExtensionFilter);
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
