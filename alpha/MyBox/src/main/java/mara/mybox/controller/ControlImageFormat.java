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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.FileFilters;

import mara.mybox.value.FileExtensions;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @License Apache License Version 2.0
 */
public class ControlImageFormat extends BaseController {

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
    protected FlowPane formatPane, colorspacePane, alphaPane, dpiPane, compressPane, qualityPane, icoPane;
    @FXML
    protected VBox colorspaceBox, compressBox, binaryBox;
    @FXML
    protected HBox profileBox;
    @FXML
    protected Button iccSelectButton;
    @FXML
    protected RadioButton pngRadio, jpgRadio, tifRadio, gifRadio, bmpRadio, pnmRadio, wbmpRadio, icoRadio, pcxRadio,
            alphaKeepRadio, alphaRemoveRadio, alphaPreKeepRadio, alphaPreReomveRadio;

    public ControlImageFormat() {
        baseTitle = Languages.message("ImageConverterBatch");

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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeTools.setTooltip(pcxRadio, Languages.message("PcxComments"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseController parent, boolean setDPI) {
        try {
            parentController = parent;
            baseName = parent.baseName;

            NodeTools.setRadioSelected(formatGroup, UserConfig.getUserConfigString(baseName + "Format", "png"));
            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldV, Toggle newV) {
                    checkFileFormat();
                }
            });

            embedProfileCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "ProfileEmbed", true));
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

            qualitySelector.getItems().addAll(Arrays.asList(
                    "100", "90", "80", "75", "60", "50", "30", "10"
            ));
            qualitySelector.setValue(UserConfig.getUserConfigString(baseName + "Quality", "100"));
            qualitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkQuality();
                }
            });

            NodeTools.setRadioSelected(binaryGroup, UserConfig.getUserConfigString(baseName + "Binary", Languages.message("OTSU")));
            binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkBinary();
                }
            });

            thresholdInput.setText(UserConfig.getUserConfigString(baseName + "Threashold", "128"));
            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkThreshold();
                }
            });

            ditherCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Dither", true));
            ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    checkDither();
                }
            });

            icoWidthSelector.getItems().addAll(Arrays.asList(
                    "45", "40", "30", "50", "25", "80", "120", "24", "64", "128", "256", "512", "48", "96", "144"
            ));
            icoWidthSelector.getSelectionModel().select(UserConfig.getUserConfigString(baseName + "IcoWidth", "45"));
            icoWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkIcoWidth();
                }
            });

            includeDpi = setDPI;
            if (includeDpi) {
                dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                        checkDpi();
                    }
                });
            }

            checkFileFormat();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void checkFileFormat() {
        if (isSettingValues) {
            return;
        }
        attributes = new ImageAttributes();
        String format = ((RadioButton) formatGroup.getSelectedToggle()).getText();
        attributes.setImageFormat(format);
        UserConfig.setUserConfigString(baseName + "Format", format);

        NodeTools.setEditorNormal(dpiSelector);
        NodeTools.setEditorNormal(qualitySelector);
        NodeTools.setEditorNormal(icoWidthSelector);
        if ("ico".equals(format)) {
            thisPane.getChildren().remove(1, thisPane.getChildren().size());
            thisPane.getChildren().addAll(icoPane);
            checkIcoWidth();
            return;
        } else {
            thisPane.getChildren().remove(1, thisPane.getChildren().size());
            thisPane.getChildren().addAll(colorspaceBox, alphaPane);
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
                colorSpaceList.add(Languages.message("BlackOrWhite"));
                break;
            default:
                colorSpaceList.addAll(ImageColorSpace.RGBColorSpaces);
                if ("raw".equals(format) || FileExtensions.CMYKImages.contains(format)) {
                    colorSpaceList.addAll(ImageColorSpace.CMYKColorSpaces);
                }
                colorSpaceList.addAll(Arrays.asList(Languages.message("Gray"), Languages.message("BlackOrWhite"), Languages.message("IccProfile")));
                break;
        }
        String dcs = UserConfig.getUserConfigString(baseName + "ColorSpace", "sRGB");
        setColorSpace(dcs);
        for (String colorSpace : colorSpaceList) {
            RadioButton button = new RadioButton(colorSpace);
            button.setSelected(dcs.equals(colorSpace));
            button.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    if (button.isSelected()) {
                        setColorSpace(colorSpace);
                    }
                }
            });
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
        refreshStyle(thisPane);

    }

    public void setColorSpace(String colorSpace) {
        if (isSettingValues || colorSpace == null) {
            return;
        }
        attributes.setColorSpaceName(colorSpace);
        attributes.setProfile(null);
        attributes.setProfileName(null);
        UserConfig.setUserConfigString(baseName + "ColorSpace", colorSpace);

        if (Languages.message("IccProfile").equals(colorSpace)) {
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
        if (Languages.message("BlackOrWhite").equals(colorSpace)) {
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

        if (FileExtensions.PremultiplyAlphaImages.contains(attributes.getImageFormat())
                && !Languages.message("BlackOrWhite").equals(colorSpace)) {
            alphaKeepRadio.setDisable(false);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(false);
            alphaPreReomveRadio.setDisable(false);
            alphaKeepRadio.fire();
        } else if (FileExtensions.AlphaImages.contains(attributes.getImageFormat())
                && !Languages.message("BlackOrWhite").equals(colorSpace)) {
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
        refreshStyle(thisPane);
    }

    public void checkProfile() {
        if (!colorspaceBox.getChildren().contains(profileBox)) {
            return;
        }
        try {
            File file = new File(profileInput.getText());
            ICC_Profile profile = ImageColorSpace.iccProfile(file.getAbsolutePath());
            if (profile == null) {
                profileInput.setStyle(badStyle);
            } else {
                profileInput.setStyle(null);
                attributes.setProfile(profile);
                attributes.setProfileName(FileNameTools.getFilePrefix(file));
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
        UserConfig.setUserConfigBoolean(baseName + "ProfileEmbed", embedProfileCheck.isSelected());
    }

    public void checkAlpha() {
        if (isSettingValues) {
            return;
        }
        String alpha = ((RadioButton) alphaGroup.getSelectedToggle()).getText();
        if (Languages.message("Keep").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.Keep);
        } else if (Languages.message("Remove").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.Remove);
        } else if (Languages.message("PremultipliedAndKeep").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.PremultipliedAndKeep);
        } else if (Languages.message("PremultipliedAndRemove").equals(alpha)) {
            attributes.setAlpha(ImageAttributes.Alpha.PremultipliedAndRemove);
        }
        UserConfig.setUserConfigString(baseName + "Alpha", alpha);

        boolean hasAlpha = attributes.getAlpha() == ImageAttributes.Alpha.Keep
                || attributes.getAlpha() == ImageAttributes.Alpha.PremultipliedAndKeep;

        compressPane.getChildren().clear();
        compressGroup.getToggles().clear();
        String defaultCompress = UserConfig.getUserConfigString(baseName + "CompressionType", "Deflate");
        String[] compressionTypes = ImageColorSpace.getCompressionTypes(attributes.getImageFormat(),
                attributes.getColorSpaceName(), hasAlpha);
        if (compressionTypes != null && compressionTypes.length > 0) {
            for (String compress : compressionTypes) {
                RadioButton button = new RadioButton(compress);
                button.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                        if (button.isSelected()) {
                            attributes.setCompressionType(compress);
                            UserConfig.setUserConfigString(baseName + "CompressionType", compress);
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
            String q = UserConfig.getUserConfigString(baseName + "Quality", "100");
            qualitySelector.getSelectionModel().select(q);
            if (!thisPane.getChildren().contains(compressBox)) {
                thisPane.getChildren().add(compressBox);
            }
            isSettingValues = false;
            checkQuality();
        } else {
            isSettingValues = true;
            NodeTools.setEditorNormal(qualitySelector);
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
                NodeTools.setEditorNormal(qualitySelector);
                UserConfig.setUserConfigString(baseName + "Quality", v + "");
            } else {
                NodeTools.setEditorBadStyle(qualitySelector);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            NodeTools.setEditorBadStyle(qualitySelector);
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
                NodeTools.setEditorNormal(icoWidthSelector);
                UserConfig.setUserConfigString(baseName + "IcoWidth", v + "");
            } else {
                NodeTools.setEditorBadStyle(icoWidthSelector);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            NodeTools.setEditorBadStyle(icoWidthSelector);
        }
    }

    public void checkBinary() {
        try {
            if (isSettingValues || !thisPane.getChildren().contains(binaryBox)) {
                return;
            }
            RadioButton selected = (RadioButton) binaryGroup.getSelectedToggle();
            String s = selected.getText();
            if (Languages.message("Threshold").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_THRESHOLD);
                thresholdInput.setDisable(false);
                checkThreshold();
            } else if (Languages.message("OTSU").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_OTSU);
                thresholdInput.setStyle(null);
                thresholdInput.setDisable(true);
            } else {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.DEFAULT);
                thresholdInput.setStyle(null);
                thresholdInput.setDisable(true);
            }
            UserConfig.setUserConfigString(baseName + "Binary", s);

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
                UserConfig.setUserConfigString(baseName + "Threashold", inputValue + "");
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
        UserConfig.setUserConfigBoolean(baseName + "Dither", ditherCheck.isSelected());
    }

    public void checkDpi() {
        if (isSettingValues || !thisPane.getChildren().contains(dpiPane)) {
            return;
        }
        attributes.setDensity(dpi);
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
        selectIccFile(UserConfig.getUserConfigPath(baseName + "SourcePath"));
    }

    public void selectIccFile(File path) {
        try {
            File file = mara.mybox.fxml.FxFileTools.selectFile(this, path, FileFilters.IccProfileExtensionFilter);
            if (file == null) {
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

    /*
        get/set
     */
    public ImageAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ImageAttributes attributes) {
        this.attributes = attributes;
    }

    public ToggleGroup getCompressGroup() {
        return compressGroup;
    }

    public void setCompressGroup(ToggleGroup compressGroup) {
        this.compressGroup = compressGroup;
    }

}
