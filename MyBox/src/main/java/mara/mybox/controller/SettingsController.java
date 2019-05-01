package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import mara.mybox.controller.base.BaseController;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import mara.mybox.data.ControlStyle;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.db.TableImageHistory;
import mara.mybox.db.TableImageInit;
import mara.mybox.db.TableVisitHistory;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.value.AppVaribles.getUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-10-14
 * @Description
 * @License Apache License Version 2.0
 */
public class SettingsController extends BaseController {

    private int maxImageHis, recentFileNumber;

    @FXML
    private ToggleGroup langGroup, pdfMemGroup;
    @FXML
    private RadioButton chineseRadio, englishRadio;
    @FXML
    private RadioButton pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;
    @FXML
    private CheckBox showCommentsCheck, stopAlarmCheck, newWindowCheck, alphaWhiteCheck, restoreStagesSizeCheck,
            anchorSolidCheck, coordinateCheck, rulerXCheck, rulerYCheck, removeAlphaCopyCheck, controlsTextCheck;
    @FXML
    private TextField imageMaxHisInput, tempDirInput, fileRecentInput;
    @FXML
    protected ComboBox<String> styleBox, controlsColorBox, imageWidthBox, fontSizeBox, strokeWidthBox, anchorWidthBox;
    @FXML
    protected HBox pdfMemBox, imageHisBox;
    @FXML
    protected ColorPicker strokeColorPicker, anchorColorPicker;
    @FXML
    private Button setImageHisButton, setFileRecentButton, closeButton;

    public SettingsController() {
        baseTitle = AppVaribles.getMessage("Settings");

    }

    @Override
    public void initializeNext() {
        try {

            langGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLanguage();
                }
            });

            fileRecentInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRecentFile();
                }
            });

            strokeWidthBox.getItems().addAll(Arrays.asList(
                    "1", "3", "5", "7", "9"));
            strokeWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVaribles.setUserConfigInt("StrokeWidth", v);
                                FxmlControl.setEditorNormal(strokeWidthBox);
                                if (parentController != null) {
                                    parentController.setMaskStroke();
                                }
                            } else {
                                FxmlControl.setEditorBadStyle(strokeWidthBox);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(strokeWidthBox);
                        }
                    }
                }
            });
            strokeWidthBox.getSelectionModel().select(AppVaribles.getUserConfigValue("StrokeWidth", "3"));

            strokeColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    AppVaribles.setUserConfigValue("StrokeColor", newValue.toString());
                    if (parentController != null) {
                        parentController.setMaskStroke();
                    }
                }
            });
            strokeColorPicker.setValue(Color.web(AppVaribles.getUserConfigValue("StrokeColor", "#FF0000")));

            anchorColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    AppVaribles.setUserConfigValue("AnchorColor", newValue.toString());
                    if (parentController != null) {
                        parentController.setMaskStroke();
                    }
                }
            });
            anchorColorPicker.setValue(Color.web(AppVaribles.getUserConfigValue("AnchorColor", "#0000FF")));

            anchorWidthBox.getItems().addAll(Arrays.asList(
                    "10", "15", "20", "25", "30", "40", "50"));
            anchorWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVaribles.setUserConfigInt("AnchorWidth", v);
                                FxmlControl.setEditorNormal(anchorWidthBox);
                                if (parentController != null) {
                                    parentController.setMaskStroke();
                                }
                            } else {
                                FxmlControl.setEditorBadStyle(anchorWidthBox);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(anchorWidthBox);
                        }
                    }
                }
            });
            anchorWidthBox.getSelectionModel().select(AppVaribles.getUserConfigValue("AnchorWidth", "10"));

            anchorSolidCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVaribles.setUserConfigValue("AnchorSolid", new_toggle);
                    if (parentController != null) {
                        parentController.setMaskStroke();
                    }
                }
            });
            anchorSolidCheck.setSelected(AppVaribles.getUserConfigBoolean("AnchorSolid", true));

            fontSizeBox.getItems().addAll(Arrays.asList(
                    "9", "10", "12", "14", "15", "16", "17", "18", "19", "20", "21", "22"));
            fontSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                setSceneFontSize(v);
                                FxmlControl.setEditorNormal(fontSizeBox);
                            } else {
                                FxmlControl.setEditorBadStyle(fontSizeBox);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(fontSizeBox);
                        }
                    }
                }
            });
            fontSizeBox.getSelectionModel().select(AppVaribles.sceneFontSize + "");

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setUserConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

            showCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setShowComments(showCommentsCheck.isSelected());
                }
            });

            newWindowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setOpenStageInNewWindow(newWindowCheck.isSelected());
                }
            });

            restoreStagesSizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setRestoreStagesSize(restoreStagesSizeCheck.isSelected());
                }
            });

            Tooltip tips = new Tooltip(getMessage("PdfMemComments"));
            tips.setFont(new Font(16));

            tips = new Tooltip(getMessage("ImageHisComments"));
            tips.setFont(new Font(16));
            FxmlControl.quickTooltip(imageHisBox, tips);

            rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("RulerX", rulerXCheck.isSelected());
                    if (parentController != null) {
                        parentController.drawMaskRulerX();
                    }
                }
            });
            rulerXCheck.setSelected(AppVaribles.getUserConfigBoolean("RulerX", true));

            rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("RulerY", rulerYCheck.isSelected());
                    if (parentController != null) {
                        parentController.drawMaskRulerY();
                    }
                }
            });
            rulerYCheck.setSelected(AppVaribles.getUserConfigBoolean("RulerY", true));

            coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("ImagePopCooridnateKey", coordinateCheck.isSelected());
                }
            });
            coordinateCheck.setSelected(AppVaribles.getUserConfigBoolean("ImagePopCooridnateKey", false));

            imageMaxHisInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkImageMaxHis();
                }
            });

            tempDirInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        final File file = new File(newValue);
                        if (!file.exists() || !file.isDirectory()) {
                            tempDirInput.setStyle(badStyle);
                            return;
                        }
                        tempDirInput.setStyle(null);
                        AppVaribles.setUserConfigValue(CommonValues.userTempPathKey, file.getAbsolutePath());
                    } catch (Exception e) {
                    }
                }

            });
            tempDirInput.setText(AppVaribles.getUserConfigPath(CommonValues.userTempPathKey).getAbsolutePath());

            styleBox.getItems().addAll(Arrays.asList(
                    getMessage("DefaultStyle"), getMessage("caspianStyle"),
                    getMessage("WhiteOnBlackStyle"), getMessage("PinkOnBlackStyle"),
                    getMessage("YellowOnBlackStyle"), getMessage("GreenOnBlackStyle"),
                    getMessage("WhiteOnBlueStyle"), getMessage("WhiteOnGreenStyle"),
                    getMessage("WhiteOnVioletredStyle")));
            styleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkStyle(newValue);
                    }
                }
            });

            controlsColorBox.getItems().addAll(Arrays.asList(
                    getMessage("DefaultColor"), getMessage("Pink"),
                    getMessage("Red"), getMessage("Blue"),
                    getMessage("Orange")
            ));
            controlsColorBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkControlsColor(newValue);
                    }
                }
            });

            controlsTextCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVaribles.controlDisplayText = controlsTextCheck.isSelected();
                    AppVaribles.setUserConfigValue("ControlDisplayText", controlsTextCheck.isSelected());
                    refresh();
                }
            });

            imageWidthBox.getItems().addAll(Arrays.asList(
                    "4096", "2048", "8192", "1024", "10240", "6144", "512", "15360", "20480", "30720"));
            imageWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVaribles.setUserConfigInt("MaxImageSampleWidth", v);
                                FxmlControl.setEditorNormal(imageWidthBox);
                            } else {
                                FxmlControl.setEditorBadStyle(imageWidthBox);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(imageWidthBox);
                        }
                    }
                }
            });
            imageWidthBox.getSelectionModel().select(AppVaribles.getUserConfigValue("MaxImageSampleWidth", "4096"));

            alphaWhiteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVaribles.setUserConfigValue("AlphaAsWhite", new_toggle);
                }
            });
            alphaWhiteCheck.setSelected(AppVaribles.isAlphaAsWhite());

            removeAlphaCopyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVaribles.setUserConfigValue("RemoveAlphaCopy", new_toggle);
                }
            });
            removeAlphaCopyCheck.setSelected(AppVaribles.getUserConfigBoolean("RemoveAlphaCopy", true));

            isSettingValues = true;
            initValues();
            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initValues() {
        try {
            stopAlarmCheck.setSelected(AppVaribles.getUserConfigBoolean("StopAlarmsWhenExit"));
            showCommentsCheck.setSelected(AppVaribles.showComments);
            newWindowCheck.setSelected(AppVaribles.openStageInNewWindow);

            maxImageHis = AppVaribles.getUserConfigInt("MaxImageHistories", 20);
            imageMaxHisInput.setText(maxImageHis + "");

            recentFileNumber = AppVaribles.getUserConfigInt("FileRecentNumber", 20);
            fileRecentInput.setText(recentFileNumber + "");

            String style = AppVaribles.getUserConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
            switch (style) {
                case CommonValues.DefaultStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("DefaultStyle"));
                    break;
                case CommonValues.caspianStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("caspianStyle"));
                    break;
                case CommonValues.WhiteOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnBlackStyle"));
                    break;
                case CommonValues.PinkOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("PinkOnBlackStyle"));
                    break;
                case CommonValues.YellowOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("YellowOnBlackStyle"));
                    break;
                case CommonValues.GreenOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("GreenOnBlackStyle"));
                    break;
                case CommonValues.WhiteOnBlueStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnBlueStyle"));
                    break;
                case CommonValues.WhiteOnGreenStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnGreenStyle"));
                    break;
                case CommonValues.WhiteOnPurpleStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnVioletredStyle"));
                    break;
                default:
                    break;
            }

            switch (AppVaribles.ControlColor) {
                case Default:
                    controlsColorBox.getSelectionModel().select(AppVaribles.getMessage("DefaultColor"));
                    break;
                case Red:
                    controlsColorBox.getSelectionModel().select(AppVaribles.getMessage("Red"));
                    break;
                case Pink:
                    controlsColorBox.getSelectionModel().select(AppVaribles.getMessage("Pink"));
                    break;
                case Blue:
                    controlsColorBox.getSelectionModel().select(AppVaribles.getMessage("Blue"));
                    break;
                case Orange:
                    controlsColorBox.getSelectionModel().select(AppVaribles.getMessage("Orange"));
                    break;
                default:
                    controlsColorBox.getSelectionModel().select(AppVaribles.getMessage("DefaultColor"));
                    break;
            }

            controlsTextCheck.setSelected(AppVaribles.getUserConfigBoolean("ControlDisplayText", false));

            imageWidthBox.getSelectionModel().select(AppVaribles.getUserConfigInt("MaxImageSampleWidth", 4096) + "");

            checkLanguage();
            checkPdfMem();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void checkLanguage() {
        if (AppVaribles.currentBundle == CommonValues.BundleZhCN) {
            chineseRadio.setSelected(true);
        } else {
            englishRadio.setSelected(true);
        }
    }

    protected void checkPdfMem() {
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdfMem1GRadio.setSelected(true);
                break;
            case "2GB":
                pdfMem2GRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfMemUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdfMem500MRadio.setSelected(true);
        }
    }

    protected void checkStyle(String s) {
        try {
            if (getMessage("DefaultStyle").equals(s)) {
                setStyle(CommonValues.DefaultStyle);
            } else if (getMessage("caspianStyle").equals(s)) {
                setStyle(CommonValues.caspianStyle);
            } else if (getMessage("WhiteOnBlackStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnBlackStyle);
            } else if (getMessage("PinkOnBlackStyle").equals(s)) {
                setStyle(CommonValues.PinkOnBlackStyle);
            } else if (getMessage("YellowOnBlackStyle").equals(s)) {
                setStyle(CommonValues.YellowOnBlackStyle);
            } else if (getMessage("GreenOnBlackStyle").equals(s)) {
                setStyle(CommonValues.GreenOnBlackStyle);
            } else if (getMessage("WhiteOnBlueStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnBlueStyle);
            } else if (getMessage("WhiteOnGreenStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnGreenStyle);
            } else if (getMessage("WhiteOnVioletredStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnPurpleStyle);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkControlsColor(String s) {
        try {
            if (isSettingValues) {
                return;
            }
            if (getMessage("DefaultColor").equals(s)) {
                ControlStyle.setConfigColorStyle("default");
            } else if (getMessage("Pink").equals(s)) {
                ControlStyle.setConfigColorStyle("Pink");
            } else if (getMessage("Red").equals(s)) {
                ControlStyle.setConfigColorStyle("Red");
            } else if (getMessage("Blue").equals(s)) {
                ControlStyle.setConfigColorStyle("Blue");
            } else if (getMessage("Orange").equals(s)) {
                ControlStyle.setConfigColorStyle("Orange");
            } else {
                return;
            }
            refresh();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void setStyle(String style) {
        try {
            AppVaribles.setUserConfigValue("InterfaceStyle", style);
            if (parentController != null) {
                parentController.setInterfaceStyle(style);
            }
            setInterfaceStyle(style);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    private void checkImageMaxHis() {
        try {
            int v = Integer.valueOf(imageMaxHisInput.getText());
            if (v >= 0) {
                maxImageHis = v;
                imageMaxHisInput.setStyle(null);
                setImageHisButton.setDisable(false);
            } else {
                imageMaxHisInput.setStyle(badStyle);
                setImageHisButton.setDisable(true);
            }
        } catch (Exception e) {
            imageMaxHisInput.setStyle(badStyle);
            setImageHisButton.setDisable(true);
        }
    }

    private void checkRecentFile() {
        try {
            int v = Integer.valueOf(fileRecentInput.getText());
            if (v >= 0) {
                recentFileNumber = v;
                fileRecentInput.setStyle(null);
                setFileRecentButton.setDisable(false);
            } else {
                fileRecentInput.setStyle(badStyle);
                setFileRecentButton.setDisable(true);
            }
        } catch (Exception e) {
            fileRecentInput.setStyle(badStyle);
            setFileRecentButton.setDisable(true);
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        AppVaribles.setLanguage("zh");
        refresh();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVaribles.setLanguage("en");
        refresh();
    }

    @FXML
    protected void replaceWhiteAction(ActionEvent event) {
        AppVaribles.setUserConfigValue("AlphaAsWhite", alphaWhiteCheck.isSelected());
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVaribles.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVaribles.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVaribles.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVaribles.setPdfMem("Unlimit");
    }

    @FXML
    protected void clearImageHistories(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.getMessage("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        new TableImageInit().clear();
        new TableImageHistory().clear();
        if (parentController != null && parentFxml != null
                && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
            ImageManufactureController p = (ImageManufactureController) parentController;
            p.updateHisBox();
        }
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void setImageHisAction(ActionEvent event) {
        try {
            AppVaribles.setUserConfigInt("MaxImageHistories", maxImageHis);
            if (parentController != null && parentFxml != null
                    && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
                ImageManufactureController p = (ImageManufactureController) parentController;
                p.updateHisBox();
            }
            popInformation(AppVaribles.getMessage("Successful"));
        } catch (Exception e) {

        }
    }

    @FXML
    protected void noImageHistories(ActionEvent event) {
        imageMaxHisInput.setText("0");
        AppVaribles.setUserConfigInt("MaxImageHistories", 0);
        if (parentController != null && parentFxml != null
                && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
            ImageManufactureController p = (ImageManufactureController) parentController;
            p.updateHisBox();
        }
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void setFileRecentAction(ActionEvent event) {
        AppVaribles.setUserConfigInt("FileRecentNumber", recentFileNumber);
        AppVaribles.fileRecentNumber = recentFileNumber;
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void clearFileHistories(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.getMessage("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        new TableVisitHistory().clear();
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void noFileHistories(ActionEvent event) {
        fileRecentInput.setText("0");
        AppVaribles.setUserConfigInt("FileRecentNumber", 0);
        AppVaribles.fileRecentNumber = 0;
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void selectTemp(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVaribles.getUserTempPath();
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            if (CommonValues.AppDataPaths.contains(directory)) {
                alertError(AppVaribles.getMessage("DirectoryReserved"));
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setUserConfigValue(CommonValues.userTempPathKey, directory.getPath());

            tempDirInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void clearSettings(ActionEvent event) {
        super.clearSettings(event);
        refresh();
    }

    @FXML
    private void close() {
        closeStage();
    }

}
