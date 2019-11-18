package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import mara.mybox.MyBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableImageHistory;
import mara.mybox.db.TableVisitHistory;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-14
 * @Description
 * @License Apache License Version 2.0
 */
public class SettingsController extends BaseController {

    protected int maxImageHis, recentFileNumber, newJVM;
    protected String selectedLanguages;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab interfaceTab, baseTab, pdfTab, imageTab, dataTab, ocrTab;
    @FXML
    protected ToggleGroup langGroup, pdfMemGroup, controlColorGroup;
    @FXML
    protected CheckBox stopAlarmCheck, newWindowCheck, restoreStagesSizeCheck,
            copyToSystemClipboardCheck, anchorSolidCheck, controlsTextCheck, recordLoadCheck,
            clearCurrentRootCheck, hidpiCheck, derbyServerCheck;
    @FXML
    protected TextField jvmInput, imageMaxHisInput, dataDirInput, fileRecentInput, thumbnailWidthInput,
            ocrDirInput;
    @FXML
    protected VBox localBox, dataBox, ocrBox;
    @FXML
    protected ComboBox<String> styleBox, imageWidthBox, fontSizeBox, iconSizeBox,
            strokeWidthBox, anchorWidthBox;
    @FXML
    protected HBox pdfMemBox, imageHisBox;
    @FXML
    protected Button settingsImageHisOKButton, settingsRecentOKButton, settingsChangeRootButton,
            settingsAlphaColorButton, settingsStrokeColorButton, settingsAnchorColorButton,
            settingsDataPathButton, settingsJVMButton;
    @FXML
    protected RadioButton chineseRadio, englishRadio, redRadio, orangeRadio, pinkRadio, lightBlueRadio, blueRadio,
            pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;
    @FXML
    protected Rectangle alphaRect, strokeRect, anchorRect;
    @FXML
    protected ListView languageList;
    @FXML
    protected Label alphaLabel, currentJvmLabel, currentDataPathLabel, currentTempPathLabel,
            currentOCRFilesLabel, derbyStatus;

    public SettingsController() {
        baseTitle = AppVariables.message("Settings");

    }

    @Override
    public void initializeNext() {
        try {
            initInterfaceTab();
            initBaseTab();
            initDataTab();
            initPdfTab();
            initImageTab();
            initOCRTab();

            isSettingValues = true;
            initSettingValues();
            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initSettingValues() {
        try {
            stopAlarmCheck.setSelected(AppVariables.getUserConfigBoolean("StopAlarmsWhenExit"));
            newWindowCheck.setSelected(AppVariables.openStageInNewWindow);

            maxImageHis = AppVariables.getUserConfigInt("MaxImageHistories", 20);
            imageMaxHisInput.setText(maxImageHis + "");

            thumbnailWidthInput.setText(AppVariables.getUserConfigInt("ThumbnailWidth", 100) + "");

            recordLoadCheck.setSelected(AppVariables.getUserConfigBoolean("RecordImageLoad", true));

            recentFileNumber = AppVariables.getUserConfigInt("FileRecentNumber", 20);
            fileRecentInput.setText(recentFileNumber + "");

            String style = AppVariables.getUserConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
            switch (style) {
                case CommonValues.DefaultStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("DefaultStyle"));
                    break;
                case CommonValues.caspianStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("caspianStyle"));
                    break;
                case CommonValues.WhiteOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("WhiteOnBlackStyle"));
                    break;
                case CommonValues.PinkOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("PinkOnBlackStyle"));
                    break;
                case CommonValues.YellowOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("YellowOnBlackStyle"));
                    break;
                case CommonValues.GreenOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("GreenOnBlackStyle"));
                    break;
                case CommonValues.WhiteOnBlueStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("WhiteOnBlueStyle"));
                    break;
                case CommonValues.WhiteOnGreenStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("WhiteOnGreenStyle"));
                    break;
                case CommonValues.WhiteOnPurpleStyle:
                    styleBox.getSelectionModel().select(AppVariables.message("WhiteOnVioletredStyle"));
                    break;
                default:
                    break;
            }

            switch (AppVariables.ControlColor) {
                case Pink:
                    pinkRadio.fire();
                    break;
                case Blue:
                    blueRadio.fire();
                    break;
                case LightBlue:
                    lightBlueRadio.fire();
                    break;
                case Orange:
                    orangeRadio.fire();
                    break;
                case Default:
                case Red:
                default:
                    redRadio.fire();

            }

            controlsTextCheck.setSelected(AppVariables.getUserConfigBoolean("ControlDisplayText", false));

            imageWidthBox.getSelectionModel().select(AppVariables.getUserConfigInt("MaxImageSampleWidth", 4096) + "");

            checkLanguage();
            checkPdfMem();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }


    /*
        Interface settings
     */
    public void initInterfaceTab() {
        try {

            langGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLanguage();
                }
            });

            fontSizeBox.getItems().addAll(Arrays.asList(
                    "9", "10", "12", "14", "15", "16", "17", "18", "19", "20", "21", "22")
            );
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
            fontSizeBox.getSelectionModel().select(AppVariables.sceneFontSize + "");

            iconSizeBox.getItems().addAll(Arrays.asList(
                    "20", "15", "25", "18", "22", "12", "10")
            );
            iconSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                setIconSize(v);
                                FxmlControl.setEditorNormal(iconSizeBox);
                            } else {
                                FxmlControl.setEditorBadStyle(iconSizeBox);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(iconSizeBox);
                        }
                    }
                }
            });
            iconSizeBox.getSelectionModel().select(AppVariables.iconSize + "");

            newWindowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVariables.setOpenStageInNewWindow(newWindowCheck.isSelected());
                }
            });

            restoreStagesSizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVariables.setRestoreStagesSize(restoreStagesSizeCheck.isSelected());
                }
            });

            styleBox.getItems().addAll(Arrays.asList(message("DefaultStyle"), message("caspianStyle"),
                    message("WhiteOnBlackStyle"), message("PinkOnBlackStyle"),
                    message("YellowOnBlackStyle"), message("GreenOnBlackStyle"),
                    message("WhiteOnBlueStyle"), message("WhiteOnGreenStyle"),
                    message("WhiteOnVioletredStyle")));
            styleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkStyle(newValue);
                    }
                }
            });

            FxmlControl.setTooltip(redRadio, new Tooltip(message("DefaultColor")));
            FxmlControl.setTooltip(pinkRadio, new Tooltip(message("Pink")));
            FxmlControl.setTooltip(orangeRadio, new Tooltip(message("Orange")));
            FxmlControl.setTooltip(lightBlueRadio, new Tooltip(message("LightBlue")));
            FxmlControl.setTooltip(blueRadio, new Tooltip(message("Blue")));

            controlColorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkControlsColor(newValue);
                }
            });

            controlsTextCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.controlDisplayText = controlsTextCheck.isSelected();
                    AppVariables.setUserConfigValue("ControlDisplayText", controlsTextCheck.isSelected());
                    refresh();
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void checkLanguage() {
        if (AppVariables.currentBundle == CommonValues.BundleZhCN) {
            chineseRadio.setSelected(true);
        } else {
            englishRadio.setSelected(true);
        }
    }

    protected void checkStyle(String s) {
        try {
            if (message("DefaultStyle").equals(s)) {
                setStyle(CommonValues.DefaultStyle);
            } else if (message("caspianStyle").equals(s)) {
                setStyle(CommonValues.caspianStyle);
            } else if (message("WhiteOnBlackStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnBlackStyle);
            } else if (message("PinkOnBlackStyle").equals(s)) {
                setStyle(CommonValues.PinkOnBlackStyle);
            } else if (message("YellowOnBlackStyle").equals(s)) {
                setStyle(CommonValues.YellowOnBlackStyle);
            } else if (message("GreenOnBlackStyle").equals(s)) {
                setStyle(CommonValues.GreenOnBlackStyle);
            } else if (message("WhiteOnBlueStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnBlueStyle);
            } else if (message("WhiteOnGreenStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnGreenStyle);
            } else if (message("WhiteOnVioletredStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnPurpleStyle);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkControlsColor(Toggle s) {
        try {
            if (isSettingValues) {
                return;
            }
            if (s == null || redRadio.equals(s)) {
                ControlStyle.setConfigColorStyle("default");
            } else if (pinkRadio.equals(s)) {
                ControlStyle.setConfigColorStyle("pink");
            } else if (lightBlueRadio.equals(s)) {
                ControlStyle.setConfigColorStyle("lightblue");
            } else if (blueRadio.equals(s)) {
                ControlStyle.setConfigColorStyle("blue");
            } else if (orangeRadio.equals(s)) {
                ControlStyle.setConfigColorStyle("orange");
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
            AppVariables.setUserConfigValue("InterfaceStyle", style);
            if (parentController != null) {
                parentController.setInterfaceStyle(style);
            }
            setInterfaceStyle(style);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        AppVariables.setLanguage("zh");
        refresh();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVariables.setLanguage("en");
        refresh();
    }

    /*
        Base settings
     */
    public void initBaseTab() {
        try {
            int mb = 1024 * 1024;
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            final long totalM = osmxb.getTotalPhysicalMemorySize() / mb;
            String m = message("PhysicalMemory") + ": " + totalM + "MB";
            Runtime r = Runtime.getRuntime();
            final long jvmM = r.maxMemory() / mb;
            m += "    " + message("JvmXmx") + ": " + jvmM + "MB";
            currentJvmLabel.setText(m);
            jvmInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(jvmInput.getText());
                        if (v > 50 && v <= totalM - 50) {
                            jvmInput.setStyle(null);
                            if (jvmM == v) {
                                settingsJVMButton.setDisable(true);
                                return;
                            }
                            newJVM = v;
                            settingsJVMButton.setDisable(false);
                        } else {
                            jvmInput.setStyle(badStyle);
                            settingsJVMButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        jvmInput.setStyle(badStyle);
                        settingsJVMButton.setDisable(true);
                    }
                }
            });
            isSettingValues = true;
            jvmInput.setText(jvmM + "");
            settingsJVMButton.setDisable(true);
            isSettingValues = false;

            dataDirInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    String p = dataDirInput.getText();
                    if (isSettingValues || p == null || p.trim().isEmpty()
                            || p.trim().equals(AppVariables.MyboxDataPath)) {
                        settingsChangeRootButton.setDisable(true);
                        return;
                    }
                    settingsChangeRootButton.setDisable(false);
                }
            });
            dataDirInput.setText(AppVariables.MyboxDataPath);
            currentDataPathLabel.setText(MessageFormat.format(message("CurrentValue"), AppVariables.MyboxDataPath));
            clearCurrentRootCheck.setText(MessageFormat.format(message("ClearPathWhenChange"), AppVariables.MyboxDataPath));

            hidpiCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.disableHiDPI = hidpiCheck.isSelected();
                    ConfigTools.writeConfigValue("DisableHidpi", AppVariables.disableHiDPI ? "true" : "false");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MyBox.restart();
                            } catch (Exception e) {
                                logger.debug(e.toString());
                            }
                        }
                    });
                }
            });
            isSettingValues = true;
            AppVariables.disableHiDPI = "true".equals(ConfigTools.readConfigValue("DisableHidpi"));
            hidpiCheck.setSelected(AppVariables.disableHiDPI);
            isSettingValues = false;

            derbyServerCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    derbyServerCheck.setDisable(true);
                    DerbyBase.mode = derbyServerCheck.isSelected() ? "client" : "embedded";
                    ConfigTools.writeConfigValue("DerbyMode", DerbyBase.mode);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String ret = DerbyBase.startDerby();
                                popInformation(ret, 6000);
                                isSettingValues = true;
                                derbyServerCheck.setSelected("client".equals(DerbyBase.mode));
                                isSettingValues = false;
                            } catch (Exception e) {
                                logger.debug(e.toString());
                            }
                            derbyServerCheck.setDisable(false);
                            if ("client".equals(DerbyBase.mode)) {
                                derbyStatus.setText(MessageFormat.format(message("DerbyServerListening"), DerbyBase.port + ""));
                            } else {
                                derbyStatus.setText(message("DerbyEmbeddedMode"));
                            }
                        }
                    });
                }
            });
            isSettingValues = true;
            derbyServerCheck.setSelected("client".equals(DerbyBase.mode));
            isSettingValues = false;

            if ("client".equals(DerbyBase.mode)) {
                derbyStatus.setText(MessageFormat.format(message("DerbyServerListening"), DerbyBase.port + ""));
            } else {
                derbyStatus.setText(message("DerbyEmbeddedMode"));
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    protected void setJVM() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ConfigTools.writeConfigValue("JVMmemory", "-Xms" + newJVM + "m");
                    MyBox.restart();
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
            }
        });
    }

    @FXML
    protected void recoverJVM() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ConfigTools.writeConfigValue("JVMmemory", null);
                    popInformation(message("EffectNextStart"));
//                    MyBox.restart();
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
            }
        });
    }

    @FXML
    protected void selectDataPath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(AppVariables.MyboxDataPath));
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory);
            dataDirInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void changeDataPath(ActionEvent event) {
        try {
            String newPath = dataDirInput.getText();
            if (isSettingValues || newPath == null || newPath.trim().isEmpty()
                    || newPath.trim().equals(AppVariables.MyboxDataPath)) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getBaseTitle());
            alert.setContentText(AppVariables.message("ChangeDataPathConfirm"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
            popInformation(message("CopyingFilesFromTo"));
            String oldPath = AppVariables.MyboxDataPath;
            if (FileTools.copyWholeDirectory(new File(oldPath), new File(newPath), null, false)) {
                File lckFile = new File(newPath + File.separator
                        + "mybox_derby" + File.separator + "db.lck");
                if (lckFile.exists()) {
                    try {
                        lckFile.delete();
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
                AppVariables.MyboxDataPath = newPath;
                ConfigTools.writeConfigValue("MyBoxDataPath", newPath);
                dataDirInput.setStyle(null);
                if (clearCurrentRootCheck.isSelected()) {
                    ConfigTools.writeConfigValue("MyBoxOldDataPath", oldPath);
                }
                MyBox.restart();
            } else {
                popFailed();
                dataDirInput.setStyle(badStyle);
            }

        } catch (Exception e) {
            popFailed();
            dataDirInput.setStyle(badStyle);
        }
    }

    /*
        Data settings
     */
    public void initDataTab() {
        try {

            fileRecentInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRecentFile();
                }
            });

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVariables.setUserConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    protected void setFileRecentAction(ActionEvent event
    ) {
        AppVariables.setUserConfigInt("FileRecentNumber", recentFileNumber);
        AppVariables.fileRecentNumber = recentFileNumber;
        popSuccessul();
    }

    @FXML
    protected void clearFileHistories(ActionEvent event
    ) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        new TableVisitHistory().clear();
        popSuccessul();
    }

    @FXML
    protected void noFileHistories(ActionEvent event
    ) {
        fileRecentInput.setText("0");
        AppVariables.setUserConfigInt("FileRecentNumber", 0);
        AppVariables.fileRecentNumber = 0;
        popSuccessul();
    }

    /*
        PDF settings
     */
    public void initPdfTab() {
        try {

        } catch (Exception e) {
            logger.debug(e.toString());
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

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVariables.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVariables.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVariables.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVariables.setPdfMem("Unlimit");
    }

    /*
        Image settings
     */
    public void initImageTab() {
        try {
            strokeWidthBox.getItems().addAll(Arrays.asList(
                    "1", "3", "5", "7", "9"));
            strokeWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVariables.setUserConfigInt("StrokeWidth", v);
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
            strokeWidthBox.getSelectionModel().select(AppVariables.getUserConfigValue("StrokeWidth", "3"));

            try {
                String c = AppVariables.getUserConfigValue("StrokeColor", ImageMaskController.DefaultStrokeColor);
                strokeRect.setFill(Color.web(c));
            } catch (Exception e) {
                strokeRect.setFill(Color.web(ImageMaskController.DefaultStrokeColor));
                AppVariables.setUserConfigValue("StrokeColor", ImageMaskController.DefaultStrokeColor);
            }
            FxmlControl.setTooltip(strokeRect, FxmlColor.colorNameDisplay((Color) strokeRect.getFill()));

            anchorWidthBox.getItems().addAll(Arrays.asList(
                    "10", "15", "20", "25", "30", "40", "50"));
            anchorWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVariables.setUserConfigInt("AnchorWidth", v);
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
            anchorWidthBox.getSelectionModel().select(AppVariables.getUserConfigValue("AnchorWidth", "10"));

            try {
                String color = AppVariables.getUserConfigValue("AnchorColor", ImageMaskController.DefaultAnchorColor);
                anchorRect.setFill(Color.web(color));
            } catch (Exception e) {
                anchorRect.setFill(Color.web(ImageMaskController.DefaultAnchorColor));
                AppVariables.setUserConfigValue("AnchorColor", ImageMaskController.DefaultAnchorColor);
            }
            FxmlControl.setTooltip(anchorRect, FxmlColor.colorNameDisplay((Color) anchorRect.getFill()));

            anchorSolidCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("AnchorSolid", new_toggle);
                    if (parentController != null) {
                        parentController.setMaskStroke();
                    }
                }
            });
            anchorSolidCheck.setSelected(AppVariables.getUserConfigBoolean("AnchorSolid", true));

            try {
                String color = AppVariables.getUserConfigValue("AlphaAsColor", Color.WHITE.toString());
                alphaRect.setFill(Color.web(color));
                if (!Color.web(color).equals(Color.WHITE)) {
                    alphaLabel.setText(message("AlphaReplaceComments"));
                    alphaLabel.setStyle(FxmlControl.darkRedText);
                } else {
                    alphaLabel.setText("");
                }
            } catch (Exception e) {
                alphaRect.setFill(Color.WHITE);
                AppVariables.setUserConfigValue("AlphaAsColor", Color.WHITE.toString());
            }
            FxmlControl.setTooltip(alphaRect, FxmlColor.colorNameDisplay((Color) alphaRect.getFill()));

            FxmlControl.setTooltip(imageHisBox, new Tooltip(message("ImageHisComments")));

            imageMaxHisInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkImageMaxHis();
                }
            });

            thumbnailWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(thumbnailWidthInput.getText());
                        if (v > 0) {
                            AppVariables.setUserConfigInt("ThumbnailWidth", v);
                            thumbnailWidthInput.setStyle(null);
                            popSuccessul();
                        } else {
                            thumbnailWidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        thumbnailWidthInput.setStyle(badStyle);
                    }
                }
            });

            recordLoadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("RecordImageLoad", recordLoadCheck.isSelected());
                    popSuccessul();
                }
            });
            FxmlControl.setTooltip(recordLoadCheck, new Tooltip(message("RecordImageLoad")));

            imageWidthBox.getItems().addAll(Arrays.asList(
                    "4096", "2048", "8192", "1024", "10240", "6144", "512", "15360", "20480", "30720"));
            imageWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVariables.setUserConfigInt("MaxImageSampleWidth", v);
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
            imageWidthBox.getSelectionModel().select(AppVariables.getUserConfigValue("MaxImageSampleWidth", "4096"));

            copyToSystemClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("CopyToSystemClipboard", copyToSystemClipboardCheck.isSelected());
                }
            });
            copyToSystemClipboardCheck.setSelected(AppVariables.getUserConfigBoolean("CopyToSystemClipboard", true));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void checkImageMaxHis() {
        try {
            int v = Integer.valueOf(imageMaxHisInput.getText());
            if (v >= 0) {
                maxImageHis = v;
                imageMaxHisInput.setStyle(null);
                settingsImageHisOKButton.setDisable(false);
            } else {
                imageMaxHisInput.setStyle(badStyle);
                settingsImageHisOKButton.setDisable(true);
            }
        } catch (Exception e) {
            imageMaxHisInput.setStyle(badStyle);
            settingsImageHisOKButton.setDisable(true);
        }
    }

    private void checkRecentFile() {
        try {
            int v = Integer.valueOf(fileRecentInput.getText());
            if (v >= 0) {
                recentFileNumber = v;
                fileRecentInput.setStyle(null);
                settingsRecentOKButton.setDisable(false);
            } else {
                fileRecentInput.setStyle(badStyle);
                settingsRecentOKButton.setDisable(true);
            }
        } catch (Exception e) {
            fileRecentInput.setStyle(badStyle);
            settingsRecentOKButton.setDisable(true);
        }
    }

    @FXML
    public void strokePalette() {
        showPalette(settingsAnchorColorButton, message("Settings") + " - " + message("StrokeColor"));
    }

    @FXML
    public void anchorPalette() {
        showPalette(settingsStrokeColorButton, message("Settings") + " - " + message("AnchorColor"));
    }

    @FXML
    public void alphaPalette() {
        showPalette(settingsAlphaColorButton, message("Settings") + " - " + message("AlphaColor"));
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        try {
            if (settingsAnchorColorButton.equals(control)) {
                strokeRect.setFill(color);
                FxmlControl.setTooltip(strokeRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
                AppVariables.setUserConfigValue("StrokeColor", color.toString());
                if (parentController != null) {
                    parentController.setMaskStroke();
                }

            } else if (settingsStrokeColorButton.equals(control)) {
                anchorRect.setFill(color);
                FxmlControl.setTooltip(anchorRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
                AppVariables.setUserConfigValue("AnchorColor", color.toString());
                if (parentController != null) {
                    parentController.setMaskStroke();
                }

            } else if (settingsAlphaColorButton.equals(control)) {
                alphaRect.setFill(color);
                FxmlControl.setTooltip(alphaRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
                AppVariables.setUserConfigValue("AlphaAsColor", color.toString());
                if (!color.equals(Color.WHITE)) {
                    alphaLabel.setText(message("AlphaReplaceComments"));
                    alphaLabel.setStyle(FxmlControl.darkRedText);
                } else {
                    alphaLabel.setText("");
                }

            }
            popSuccessul();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            popError(e.toString());
            return false;
        }
    }

    @FXML
    protected void clearImageHistories(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        new TableImageHistory().clear();
        popSuccessul();
    }

    @FXML
    protected void setImageHisAction(ActionEvent event) {
        try {
            AppVariables.setUserConfigInt("MaxImageHistories", maxImageHis);
//            if (parentController != null && parentFxml != null
//                    && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
//                ImageManufactureBaseController p = (ImageManufactureBaseController) parentController;
//                p.updateHisBox();
//            }
            popSuccessul();
        } catch (Exception e) {

        }
    }

    @FXML
    protected void noImageHistories(ActionEvent event) {
        imageMaxHisInput.setText("0");
//        AppVariables.setUserConfigInt("MaxImageHistories", 0);
//        if (parentController != null && parentFxml != null
//                && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
//            ImageManufactureBaseController p = (ImageManufactureBaseController) parentController;
//            p.updateHisBox();
//        }
        popSuccessul();
    }

    /*
        OCR
     */
    protected void initOCRTab() {
        try {
            OCRTools.initDataFiles();
            ocrDirInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        File file = new File(newValue);
                        if (!file.exists() || !file.isDirectory()) {
                            ocrDirInput.setStyle(badStyle);
                            return;
                        }
                        ocrDirInput.setStyle(null);
                        AppVariables.setUserConfigValue("TessDataPath", file.getAbsolutePath());

                        setLanguagesList();
                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }
                }
            });
            ocrDirInput.setText(AppVariables.getUserConfigValue("TessDataPath", ""));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void setLanguagesList() {
        try {
            OCRTools.initDataFiles();
            languageList.getItems().clear();
            languageList.getItems().addAll(OCRTools.namesList());
            languageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            languageList.setPrefHeight(200);
            languageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    checkLanguages();
                }
            });
            selectedLanguages = AppVariables.getUserConfigValue("ImageOCRLanguages", null);
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
                currentOCRFilesLabel.setText(
                        MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                isSettingValues = true;
                String[] langs = selectedLanguages.split("\\+");
                Map<String, String> codes = OCRTools.codeName();
                for (String code : langs) {
                    String name = codes.get(code);
                    if (name == null) {
                        name = code;
                    }
                    languageList.getSelectionModel().select(name);
                }
                isSettingValues = false;
            } else {
                currentOCRFilesLabel.setText(
                        MessageFormat.format(message("CurrentDataFiles"), ""));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    protected void setOCRPath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            String dataPath = AppVariables.getUserConfigValue("TessDataPath", null);
            if (dataPath != null) {
                chooser.setInitialDirectory(new File(dataPath));
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory);
            ocrDirInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        List<String> langsList = languageList.getSelectionModel().getSelectedItems();
        selectedLanguages = null;
        Map<String, String> names = OCRTools.nameCode();
        for (String name : langsList) {
            String code = names.get(name);
            if (code == null) {
                code = name;
            }
            if (selectedLanguages == null) {
                selectedLanguages = code;
            } else {
                selectedLanguages += "+" + code;
            }
        }
        if (selectedLanguages != null) {
            AppVariables.setUserConfigValue("ImageOCRLanguages", selectedLanguages);
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
        } else {
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), ""));
        }
    }

    @FXML
    public void upAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index - 1));
            languageList.getItems().set(index - 1, lang);
            newselected.add(index - 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void topAction() {
        List<Integer> selectedIndices = new ArrayList<>();
        selectedIndices.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selectedIndices.isEmpty()) {
            return;
        }
        List<String> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedItems());
        isSettingValues = true;
        int size = selectedIndices.size();
        for (int i = size - 1; i >= 0; i--) {
            int index = selectedIndices.get(i);
            languageList.getItems().remove(index);
        }
        languageList.getSelectionModel().clearSelection();
        languageList.getItems().addAll(0, selected);
        languageList.getSelectionModel().selectRange(0, size);
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void downAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == languageList.getItems().size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index + 1));
            languageList.getItems().set(index + 1, lang);
            newselected.add(index + 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    /*
        others
     */
    @FXML
    public void clearSettings(ActionEvent event) {
        if (!super.clearSettings()) {
            return;
        }
        refresh();
    }

    @FXML
    public void closeAction(ActionEvent event) {
        closeStage();
    }

}
