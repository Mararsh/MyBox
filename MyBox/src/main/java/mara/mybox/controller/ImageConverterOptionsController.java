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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import mara.mybox.dev.MyBoxLog;
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
    protected ToggleGroup colorSpaceGroup, compressGroup;

    @FXML
    protected ComboBox<String> qualitySelector, icoWidthSelector;
    @FXML
    protected ToggleGroup formatGroup, alphaGroup, binaryGroup;
    @FXML
    protected TextField profileInput, thresholdInput;
    @FXML
    protected CheckBox ditherCheck;
    @FXML
    protected CheckBox embedProfileCheck, alphaCheck;
    @FXML
    protected FlowPane colorspacePane, alphaPane, dpiPane, compressPane, qualityPane, icoPane;
    @FXML
    protected VBox formatBox, colorspaceBox, compressBox, binaryBox;
    @FXML
    protected HBox profileBox;
    @FXML
    protected Button iccSelectButton;
    @FXML
    protected RadioButton pcxRadio, alphaKeepRadio, alphaRemoveRadio, alphaPreKeepRadio, alphaPreReomveRadio;

    public ImageConverterOptionsController() {
        baseTitle = AppVariables.message("ImageConverterBatch");

    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            colorSpaceGroup = new ToggleGroup();
            compressGroup = new ToggleGroup();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldV, Toggle newV) {
                    checkFileFormat();
                }
            });

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
            icoWidthSelector.getSelectionModel().select(getUserConfigValue(baseName + "IcoWidth", "45"));

            isSettingValues = true;
            FxmlControl.setRadioSelected(formatGroup, getUserConfigValue(baseName + "Format", "png"));
            FxmlControl.setTooltip(pcxRadio, message("PcxComments"));
            embedProfileCheck.setSelected(getUserConfigBoolean(baseName + "ProfileEmbed", true));
            FxmlControl.setRadioSelected(binaryGroup, getUserConfigValue(baseName + "Binary", message("OTSU")));
            thresholdInput.setText(getUserConfigValue(baseName + "Threashold", null));
            ditherCheck.setSelected(getUserConfigBoolean(baseName + "Dither", true));
            dpiSelector.setValue(getUserConfigValue(baseName + "Dpi", "96"));
            qualitySelector.setValue(getUserConfigValue(baseName + "Quality", "96"));
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void setValues(boolean setDPI) {
        includeDpi = setDPI;
        if (includeDpi) {
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
            dpiSelector.getSelectionModel().select(getUserConfigValue(baseName + "Dpi", "96"));
        }
        checkFileFormat();
    }

    public void checkFileFormat() {
        if (isSettingValues) {
            return;
        }
        attributes = new ImageAttributes();

        String format = ((RadioButton) formatGroup.getSelectedToggle()).getText();
        attributes.setImageFormat(format);
        setUserConfigValue(baseName + "Format", format);

        FxmlControl.setEditorNormal(dpiSelector);
        FxmlControl.setEditorNormal(qualitySelector);
        FxmlControl.setEditorNormal(icoWidthSelector);
        if ("ico".equals(format)) {
            thisPane.getChildren().clear();
            thisPane.getChildren().addAll(formatBox, icoPane);
            return;
        } else {
            thisPane.getChildren().clear();
            thisPane.getChildren().addAll(formatBox, colorspaceBox, alphaPane);
            if (includeDpi) {
                thisPane.getChildren().add(dpiPane);
            }
            thisPane.getChildren().addAll(compressBox, binaryBox);
        }

        colorspacePane.getChildren().clear();
        colorSpaceGroup.getToggles().clear();
        List<String> colorSpaceList = new ArrayList<>();
        switch (format) {
            case "wbmp":
                colorSpaceList.add(message("BlackOrWhite"));
                break;
            default:
                colorSpaceList.addAll(ImageValue.RGBColorSpaces);
                if ("raw".equals(format) || CommonValues.CMYKImages.contains(format)) {
                    colorSpaceList.addAll(ImageValue.CMYKColorSpaces);
                }
                colorSpaceList.addAll(Arrays.asList(message("Gray"), message("BlackOrWhite"), message("IccProfile")));
                break;
        }
        String dcs = getUserConfigValue(baseName + "ColorSpace", "sRGB");
        for (String colorSpace : colorSpaceList) {
            RadioButton button = new RadioButton(colorSpace);
            button.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    if (button.isSelected()) {
                        setColorSpace(colorSpace);
                    }
                }
            });
            if (dcs.equals(colorSpace)) {
                button.fire();
            }
            colorSpaceGroup.getToggles().add(button);
            colorspacePane.getChildren().add(button);
        }
        if (colorSpaceGroup.getSelectedToggle() == null) {
            colorSpaceGroup.getToggles().get(0).setSelected(true);
        }

        switch (format) {
            case "tif":
            case "tiff":
                if (!colorspaceBox.getChildren().contains(embedProfileCheck)) {
                    colorspaceBox.getChildren().add(embedProfileCheck);
                }
                embedProfileCheck.setDisable(true);
                embedProfileCheck.setSelected(true);
                break;
            case "png":
            case "jpg":
            case "jpeg":
                if (!colorspaceBox.getChildren().contains(embedProfileCheck)) {
                    colorspaceBox.getChildren().add(embedProfileCheck);
                }
                embedProfileCheck.setDisable(false);
                break;
            default:
                if (colorspaceBox.getChildren().contains(embedProfileCheck)) {
                    colorspaceBox.getChildren().remove(embedProfileCheck);
                }
                break;
        }

        checkDpi();
        FxmlControl.refreshStyle(thisPane);

    }

    public void setColorSpace(String colorSpace) {
        if (isSettingValues || colorSpace == null) {
            return;
        }
        attributes.setColorSpaceName(colorSpace);
        attributes.setProfile(null);
        attributes.setProfileName(null);
        setUserConfigValue(baseName + "ColorSpace", colorSpace);

        if (message("IccProfile").equals(colorSpace)) {
            if (!colorspaceBox.getChildren().contains(profileBox)) {
                colorspaceBox.getChildren().add(profileBox);
            }
            checkProfile();
        } else {
            profileInput.setStyle(null);
            if (colorspaceBox.getChildren().contains(profileBox)) {
                colorspaceBox.getChildren().remove(profileBox);
            }
        }
        if (message("BlackOrWhite").equals(colorSpace)) {
            if (!thisPane.getChildren().contains(binaryBox)) {
                thisPane.getChildren().add(binaryBox);
            }
            checkBinary();
        } else {
            if (thisPane.getChildren().contains(binaryBox)) {
                thisPane.getChildren().remove(binaryBox);
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
        FxmlControl.refreshStyle(thisPane);
    }

    public void checkProfile() {
        if (!colorspaceBox.getChildren().contains(profileBox)) {
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
            MyBoxLog.debug(e.toString());
        }
    }

    public void checkEmbed() {
        if (isSettingValues || attributes == null
                || !colorspaceBox.getChildren().contains(embedProfileCheck)) {
            return;
        }
        attributes.setEmbedProfile(embedProfileCheck.isSelected());
        setUserConfigValue(baseName + "ProfileEmbed", embedProfileCheck.isSelected());
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
        setUserConfigValue(baseName + "Alpha", alpha);

        boolean hasAlpha = attributes.getAlpha() == ImageAttributes.Alpha.Keep
                || attributes.getAlpha() == ImageAttributes.Alpha.PremultipliedAndKeep;

        compressPane.getChildren().clear();
        compressGroup.getToggles().clear();
        String defaultCompress = getUserConfigValue(baseName + "CompressionType", "Deflate");
        String[] compressionTypes = ImageValue.getCompressionTypes(attributes.getImageFormat(),
                attributes.getColorSpaceName(), hasAlpha);
        if (compressionTypes != null && compressionTypes.length > 0) {
            for (String compress : compressionTypes) {
                RadioButton button = new RadioButton(compress);
                button.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                        if (button.isSelected()) {
                            attributes.setCompressionType(compress);
                            setUserConfigValue(baseName + "CompressionType", compress);
                        }
                    }
                });
                if (defaultCompress.equals(compress)) {
                    button.fire();
                }
                compressGroup.getToggles().add(button);
                compressPane.getChildren().add(button);
            }
            if (compressGroup.getSelectedToggle() == null) {
                compressGroup.getToggles().get(0).setSelected(true);
            }
            isSettingValues = true;
            String q = getUserConfigValue(baseName + "Quality", "100");
            qualitySelector.getSelectionModel().select(q);
            if (!thisPane.getChildren().contains(compressBox)) {
                thisPane.getChildren().add(compressBox);
            }
            isSettingValues = false;
            checkQuality();
        } else {
            isSettingValues = true;
            FxmlControl.setEditorNormal(qualitySelector);
            if (thisPane.getChildren().contains(compressBox)) {
                thisPane.getChildren().remove(compressBox);
            }
            isSettingValues = false;
            attributes.setCompressionType(null);
            attributes.setQuality(100);
        }
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
                setUserConfigValue(baseName + "Quality", v + "");
            } else {
                FxmlControl.setEditorBadStyle(qualitySelector);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
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
                setUserConfigValue(baseName + "IcoWidth", v + "");
            } else {
                FxmlControl.setEditorBadStyle(icoWidthSelector);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
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
            setUserConfigValue(baseName + "Binary", s);

            checkDither();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                AppVariables.setUserConfigValue(baseName + "Threashold", inputValue + "");
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
        setUserConfigValue(baseName + "Dither", ditherCheck.isSelected());
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
                setUserConfigValue(baseName + "Dpi", v + "");
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
                int fileNumber = AppVariables.fileRecentNumber * 3 / 4;
                return VisitHistoryTools.getRecentReadWrite(VisitHistory.FileType.Icc, fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Icc, pathNumber);
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
//            MyBoxLog.error(e.toString());
        }
    }

    public void iccFileSelected(File file) {
        profileInput.setText(file.getAbsolutePath());
        recordFileOpened(file, VisitHistory.FileType.Icc);
    }

}
