package mara.mybox.controller;

import java.awt.color.ICC_Profile;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.image.data.ImageAttributes;
import mara.mybox.image.data.ImageColorSpace;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileExtensions;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @License Apache License Version 2.0
 */
public class ControlImageFormat extends BaseFileController {

    protected ImageAttributes attributes;
    protected boolean includeDpi;
    protected ToggleGroup colorSpaceGroup, compressGroup;
    protected SimpleBooleanProperty notify;

    @FXML
    protected ComboBox<String> qualitySelector, icoWidthSelector;
    @FXML
    protected ToggleGroup formatGroup, alphaGroup;
    @FXML
    protected TextField profileInput;
    @FXML
    protected CheckBox embedProfileCheck, alphaCheck;
    @FXML
    protected FlowPane formatPane, colorspacePane, alphaPane, dpiPane, compressPane, qualityPane, icoPane;
    @FXML
    protected VBox colorspaceBox, compressBox, binaryBox;
    @FXML
    protected HBox profileBox;
    @FXML
    protected RadioButton pngRadio, jpgRadio, tifRadio, gifRadio, bmpRadio, pnmRadio,
            wbmpRadio, icoRadio, pcxRadio, webpRadio,
            alphaKeepRadio, alphaRemoveRadio, alphaPreKeepRadio, alphaPreReomveRadio;
    @FXML
    protected ControlImageBinary binaryController;

    public ControlImageFormat() {
        baseTitle = message("ImageConverterBatch");
        notify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            colorSpaceGroup = new ToggleGroup();
            compressGroup = new ToggleGroup();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void notifyChange() {
        notify.set(!notify.get());
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(pcxRadio, message("PcxComments"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseController parent, boolean setDPI) {
        try {
            parentController = parent;

            NodeTools.setRadioSelected(formatGroup, UserConfig.getString(baseName + "Format", "png"));
            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldV, Toggle newV) {
                    checkFileFormat();
                    notifyChange();
                }
            });

            embedProfileCheck.setSelected(UserConfig.getBoolean(baseName + "ProfileEmbed", false));
            embedProfileCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    checkEmbed();
                    notifyChange();
                }
            });

            profileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkProfile();
                    notifyChange();
                }
            });

            qualitySelector.getItems().addAll(Arrays.asList(
                    "100", "90", "80", "75", "60", "50", "30", "10"
            ));
            qualitySelector.setValue(UserConfig.getString(baseName + "Quality", "100"));
            qualitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkQuality();
                    notifyChange();
                }
            });

            if (parent instanceof BaseImageController) {
                binaryController.setParameters(((BaseImageController) parent).imageView);
            } else {
                binaryController.setParameters(null);
            }

            icoWidthSelector.getItems().addAll(Arrays.asList(
                    "45", "40", "30", "50", "25", "80", "120", "24", "64", "128", "256", "512", "48", "96", "144"
            ));
            icoWidthSelector.getSelectionModel().select(UserConfig.getString(baseName + "IcoWidth", "45"));
            icoWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    checkIcoWidth();
                    notifyChange();
                }
            });

            includeDpi = setDPI;
            if (includeDpi) {
                dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                        checkDpi();
                        notifyChange();
                    }
                });
            }

            checkFileFormat();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public void checkFileFormat() {
        if (isSettingValues) {
            return;
        }
        attributes = new ImageAttributes();
        String format = ((RadioButton) formatGroup.getSelectedToggle()).getText();
        attributes.setImageFormat(format);
        UserConfig.setString(baseName + "Format", format);

        ValidationTools.setEditorNormal(dpiSelector);
        ValidationTools.setEditorNormal(qualitySelector);
        ValidationTools.setEditorNormal(icoWidthSelector);

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
                colorSpaceList.add(message("BlackOrWhite"));
                break;
            default:
                colorSpaceList.addAll(ImageColorSpace.RGBColorSpaces);
                if ("raw".equals(format) || FileExtensions.CMYKImages.contains(format)) {
                    colorSpaceList.addAll(ImageColorSpace.CMYKColorSpaces);
                }
                colorSpaceList.addAll(Arrays.asList(message("Gray"), message("BlackOrWhite"), message("IccProfile")));
                break;
        }
        String dcs = UserConfig.getString(baseName + "ColorSpace", "sRGB");
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
        UserConfig.setString(baseName + "ColorSpace", colorSpace);

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
            binaryController.thresholdInput.setStyle(null);
        }

        if (FileExtensions.PremultiplyAlphaImages.contains(attributes.getImageFormat())
                && !message("BlackOrWhite").equals(colorSpace)) {
            alphaKeepRadio.setDisable(false);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(false);
            alphaPreReomveRadio.setDisable(false);
            alphaKeepRadio.setSelected(true);
        } else if (FileExtensions.AlphaImages.contains(attributes.getImageFormat())
                && !message("BlackOrWhite").equals(colorSpace)) {
            alphaKeepRadio.setDisable(false);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(true);
            alphaPreReomveRadio.setDisable(false);
            alphaKeepRadio.setSelected(true);
        } else {
            alphaKeepRadio.setDisable(true);
            alphaRemoveRadio.setDisable(false);
            alphaPreKeepRadio.setDisable(true);
            alphaPreReomveRadio.setDisable(false);
            alphaRemoveRadio.setSelected(true);
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
                profileInput.setStyle(UserConfig.badStyle());
            } else {
                profileInput.setStyle(null);
                attributes.setProfile(profile);
                attributes.setProfileName(FileNameTools.prefix(file.getName()));
                attributes.setEmbedProfile(embedProfileCheck.isSelected());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkEmbed() {
        if (isSettingValues || attributes == null
                || !colorspaceBox.getChildren().contains(embedProfileCheck)) {
            return;
        }
        attributes.setEmbedProfile(embedProfileCheck.isSelected());
        UserConfig.setBoolean(baseName + "ProfileEmbed", embedProfileCheck.isSelected());
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
        UserConfig.setString(baseName + "Alpha", alpha);

        boolean hasAlpha = attributes.getAlpha() == ImageAttributes.Alpha.Keep
                || attributes.getAlpha() == ImageAttributes.Alpha.PremultipliedAndKeep;

        compressPane.getChildren().clear();
        compressGroup.getToggles().clear();
        String defaultCompress = UserConfig.getString(baseName + "CompressionType", "Deflate");
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
                            UserConfig.setString(baseName + "CompressionType", compress);
                        }
                    }
                });
                if (defaultCompress.equals(compress)) {
                    button.setSelected(true);
                }
                compressGroup.getToggles().add(button);
                compressPane.getChildren().add(button);
            }
            if (compressGroup.getSelectedToggle() == null) {
                compressGroup.getToggles().get(0).setSelected(true);
            }
            isSettingValues = true;
            String q = UserConfig.getString(baseName + "Quality", "100");
            qualitySelector.getSelectionModel().select(q);
            if (!thisPane.getChildren().contains(compressBox)) {
                thisPane.getChildren().add(compressBox);
            }
            isSettingValues = false;
            checkQuality();
        } else {
            isSettingValues = true;
            ValidationTools.setEditorNormal(qualitySelector);
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
            int v = Integer.parseInt(qualitySelector.getValue());
            if (v > 0 && v <= 100) {
                attributes.setQuality(v);
                ValidationTools.setEditorNormal(qualitySelector);
                UserConfig.setString(baseName + "Quality", v + "");
            } else {
                ValidationTools.setEditorBadStyle(qualitySelector);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            ValidationTools.setEditorBadStyle(qualitySelector);
        }
    }

    public void checkIcoWidth() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.parseInt(icoWidthSelector.getValue());
            if (v > 0) {
                attributes.setWidth(v);
                ValidationTools.setEditorNormal(icoWidthSelector);
                UserConfig.setString(baseName + "IcoWidth", v + "");
            } else {
                ValidationTools.setEditorBadStyle(icoWidthSelector);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            ValidationTools.setEditorBadStyle(icoWidthSelector);
        }
    }

    public void checkBinary() {
        try {
            if (isSettingValues || attributes == null || !thisPane.getChildren().contains(binaryBox)) {
                return;
            }
            attributes.setImageBinary(binaryController.pickValues(-1));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkDpi() {
        if (isSettingValues || !thisPane.getChildren().contains(dpiPane)) {
            return;
        }
        attributes.setDensity(dpi);
    }

    public void showIccFileMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, false) {

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

        }.setFileType(VisitHistory.FileType.Icc).pop();
    }

    @FXML
    public void pickIccFile(Event event) {
        if (MenuTools.isPopMenu("RecentVisit") || AppVariables.fileRecentNumber <= 0) {
            selectIccAction();
        } else {
            showIccFileMenu(event);
        }
    }

    @FXML
    public void popIccFile(Event event) {
        if (MenuTools.isPopMenu("RecentVisit")) {
            showIccFileMenu(event);
        }
    }

    @FXML
    public void selectIccAction() {
        selectIccFile(UserConfig.getPath(baseName + "SourcePath"));
    }

    public void selectIccFile(File path) {
        try {
            File file = mara.mybox.fxml.FxFileTools.selectFile(this, path, FileFilters.IccProfileExtensionFilter);
            if (file == null) {
                return;
            }
            iccFileSelected(file);
        } catch (Exception e) {
//            MyBoxLog.error(e);
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
