package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import mara.mybox.MyBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.getUserConfigInt;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigInt;
import static mara.mybox.value.AppVariables.setUserConfigValue;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-14
 * @Description
 * @License Apache License Version 2.0
 */
public class SettingsController extends BaseController {

    protected int recentFileNumber, newJVM;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab interfaceTab, baseTab, pdfTab, imageTab, dataTab, mapTab;
    @FXML
    protected ToggleGroup langGroup, pdfMemGroup, controlColorGroup, derbyGroup, splitPanesGroup;
    @FXML
    protected CheckBox stopAlarmCheck, newWindowCheck, restoreStagesSizeCheck,
            anchorSolidCheck, controlsTextCheck, hidpiIconsCheck,
            clearCurrentRootCheck, splitPaneSensitiveCheck,
            mousePassControlPanesCheck, popColorSetCheck;
    @FXML
    protected TextField jvmInput, dataDirInput, fileRecentInput, thumbnailWidthInput,
            tiandituWebKeyInput, gaodeWebKeyInput, gaodeServiceKeyInput,
            webConnectTimeoutInput, webReadTimeoutInput;
    @FXML
    protected VBox localBox, dataBox;
    @FXML
    protected ComboBox<String> styleBox, imageWidthBox, fontSizeBox, iconSizeBox,
            strokeWidthBox, anchorWidthBox, popSizeSelector, popDurationSelector;
    @FXML
    protected HBox pdfMemBox, imageHisBox, derbyBox;
    @FXML
    protected Button settingsRecentOKButton, settingsChangeRootButton,
            settingsDataPathButton, settingsJVMButton;
    @FXML
    protected RadioButton chineseRadio, englishRadio, redRadio, orangeRadio, pinkRadio, lightBlueRadio, blueRadio,
            pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio,
            embeddedRadio, networkRadio;
    @FXML
    protected ColorSet strokeColorSetController, anchorColorSetController, alphaColorSetController,
            popBgColorController, popInfoColorController, popErrorColorController, popWarnColorController;
    @FXML
    protected ListView languageList;
    @FXML
    protected Label alphaLabel, currentJvmLabel, currentDataPathLabel, currentTempPathLabel,
            derbyStatus;

    public SettingsController() {
        baseTitle = AppVariables.message("Settings");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initInterfaceTab();
            initBaseTab();
            initDataTab();
            initPdfTab();
            initImageTab();
            initMapTab();

            isSettingValues = true;
            initSettingValues();
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initSettingValues() {
        try {
            stopAlarmCheck.setSelected(AppVariables.getUserConfigBoolean("StopAlarmsWhenExit"));
            newWindowCheck.setSelected(AppVariables.openStageInNewWindow);

            thumbnailWidthInput.setText(AppVariables.getUserConfigInt("ThumbnailWidth", 100) + "");

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
                case Red:
                default:
                    redRadio.fire();

            }

            controlsTextCheck.setSelected(AppVariables.controlDisplayText);
            hidpiIconsCheck.setSelected(AppVariables.hidpiIcons);
            FxmlControl.setTooltip(hidpiIconsCheck, new Tooltip(message("HidpiIconsComments")));

            imageWidthBox.getSelectionModel().select(AppVariables.getUserConfigInt("MaxImageSampleWidth", 4096) + "");

            splitPaneSensitiveCheck.setSelected(getUserConfigBoolean("ControlSplitPanesSensitive", false));
            mousePassControlPanesCheck.setSelected(getUserConfigBoolean("MousePassControlPanes", true));
            popColorSetCheck.setSelected(getUserConfigBoolean("PopColorSetWhenMousePassing", true));

            checkLanguage();
            checkPdfMem();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                    checkControlsColor();
                }
            });

            controlsTextCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.controlDisplayText = controlsTextCheck.isSelected();
                    AppVariables.setUserConfigValue("ControlDisplayText", AppVariables.controlDisplayText);
                    refreshInterface();
                }
            });

            hidpiIconsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.hidpiIcons = hidpiIconsCheck.isSelected();
                    AppVariables.setUserConfigValue("HidpiIcons", AppVariables.hidpiIcons);
                    refreshInterface();
                }
            });

            popSizeSelector.getItems().addAll(Arrays.asList(
                    "1.5", "1", "1.2", "2", "2.5", "0.8")
            );
            popSizeSelector.setValue(getUserConfigValue("PopTextSize", "1.5"));
            popSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            float f = Float.parseFloat(newValue);
                            if (f > 0) {
                                setUserConfigValue("PopTextSize", newValue);
                                FxmlControl.setEditorNormal(popSizeSelector);
                                popSuccessful();
                            } else {
                                FxmlControl.setEditorBadStyle(popSizeSelector);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(popSizeSelector);
                        }
                    }
                }
            });

            popDurationSelector.getItems().addAll(Arrays.asList(
                    "3000", "5000", "2000", "1500", "1000", "4000", "2500")
            );
            popDurationSelector.setValue(getUserConfigInt("PopTextDuration", 3000) + "");
            popDurationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                setUserConfigInt("PopTextDuration", v);
                                FxmlControl.setEditorNormal(popDurationSelector);
                                popSuccessful();
                            } else {
                                FxmlControl.setEditorBadStyle(popDurationSelector);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(popDurationSelector);
                        }
                    }
                }
            });

            popBgColorController.init(this, "PopTextBgColor", Color.BLACK);
            popBgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    setUserConfigValue("PopTextBgColor", popBgColorController.rgb());
                    popSuccessful();
                }
            });

            popInfoColorController.init(this, "PopInfoColor", Color.WHITE);
            popInfoColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    setUserConfigValue("PopInfoColor", popInfoColorController.rgb());
                    popSuccessful();
                }
            });

            popErrorColorController.init(this, "PopErrorColor", Color.AQUA);
            popErrorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    setUserConfigValue("PopErrorColor", popErrorColorController.rgb());
                    popSuccessful();
                }
            });

            popWarnColorController.init(this, "PopWarnColor", Color.ORANGE);
            popWarnColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    setUserConfigValue("PopWarnColor", popWarnColorController.rgb());
                    popSuccessful();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                setStyle(CommonValues.MyBoxStyle);
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
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkControlsColor() {
        try {
            if (isSettingValues) {
                return;
            }
            if (pinkRadio.isSelected()) {
                ControlStyle.setConfigColorStyle("Pink");
            } else if (lightBlueRadio.isSelected()) {
                ControlStyle.setConfigColorStyle("LightBlue");
            } else if (blueRadio.isSelected()) {
                ControlStyle.setConfigColorStyle("Blue");
            } else if (orangeRadio.isSelected()) {
                ControlStyle.setConfigColorStyle("Orange");
            } else {
                ControlStyle.setConfigColorStyle("Red");
            }
            refreshInterface();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
//            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        AppVariables.setLanguage("zh");
        reload();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVariables.setLanguage("en");
        reload();
    }

    @FXML
    protected void mousePassControlPanes() {
        AppVariables.setUserConfigValue("MousePassControlPanes", mousePassControlPanesCheck.isSelected());
    }

    @FXML
    protected void popColorSet() {
        AppVariables.setUserConfigValue("PopColorSetWhenMousePassing", popColorSetCheck.isSelected());
    }

    @FXML
    protected void splitPaneSensitive() {
        AppVariables.setUserConfigValue("ControlSplitPanesSensitive", splitPaneSensitiveCheck.isSelected());
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

            webConnectTimeoutInput.setText(AppVariables.getUserConfigInt("WebConnectTimeout", 10000) + "");
            webReadTimeoutInput.setText(AppVariables.getUserConfigInt("WebReadTimeout", 10000) + "");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void setJVM() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    long defaultJVM = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                    if (newJVM == defaultJVM) {
                        ConfigTools.writeConfigValue("JVMmemory", null);
                    } else {
                        ConfigTools.writeConfigValue("JVMmemory", "-Xms" + newJVM + "m");
                    }
                    MyBox.restart();
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        });
    }

    @FXML
    protected void defaultJVM() {
        long defaultJVM = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        jvmInput.setText(defaultJVM + "");
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
            MyBoxLog.error(e.toString());
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
            if (!FxmlControl.askSure(getBaseTitle(), message("ChangeDataPathConfirm"))) {
                return;
            }
            popInformation(message("CopyingFilesFromTo"));
            String oldPath = AppVariables.MyboxDataPath;
            if (FileTools.copyWholeDirectory(new File(oldPath), new File(newPath), null, false)) {
                File lckFile = new File(newPath + File.separator
                        + "mybox_derby" + File.separator + "db.lck");
                if (lckFile.exists()) {
                    try {
                        FileTools.delete(lckFile);
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
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

    @FXML
    protected void okWebConnectTimeout() {
        try {
            int v = Integer.parseInt(webConnectTimeoutInput.getText());
            if (v > 0) {
                AppVariables.setUserConfigInt("WebConnectTimeout", v);
                webConnectTimeoutInput.setStyle(null);
                popSuccessful();
            } else {
                webConnectTimeoutInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            webConnectTimeoutInput.setStyle(badStyle);
        }
    }

    @FXML
    protected void okWebReadTimeout() {
        try {
            int v = Integer.parseInt(webReadTimeoutInput.getText());
            if (v > 0) {
                AppVariables.setUserConfigInt("WebReadTimeout", v);
                webReadTimeoutInput.setStyle(null);
                popSuccessful();
            } else {
                webReadTimeoutInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            webReadTimeoutInput.setStyle(badStyle);
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

            setDerbyMode();
            derbyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle old_val, Toggle new_val) {
                    checkDerbyMode();
                }
            });

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVariables.setUserConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void setFileRecentAction(ActionEvent event) {
        AppVariables.setUserConfigInt("FileRecentNumber", recentFileNumber);
        AppVariables.fileRecentNumber = recentFileNumber;
        popSuccessful();
    }

    @FXML
    protected void clearFileHistories(ActionEvent event) {
        if (!FxmlControl.askSure(getBaseTitle(), message("SureClear"))) {
            return;
        }
        new TableVisitHistory().clear();
        popSuccessful();
    }

    @FXML
    protected void noFileHistories(ActionEvent event) {
        fileRecentInput.setText("0");
        AppVariables.setUserConfigInt("FileRecentNumber", 0);
        AppVariables.fileRecentNumber = 0;
        popSuccessful();
    }

    public void setDerbyMode() {
        isSettingValues = true;
        if (DerbyStatus.Nerwork == DerbyBase.status) {
            networkRadio.setSelected(true);
            derbyStatus.setText(MessageFormat.format(message("DerbyServerListening"), DerbyBase.port + ""));
        } else if (DerbyStatus.Embedded == DerbyBase.status) {
            embeddedRadio.setSelected(true);
            derbyStatus.setText(message("DerbyEmbeddedMode"));
        } else {
            networkRadio.setSelected(false);
            embeddedRadio.setSelected(false);
            derbyStatus.setText(MessageFormat.format(message("DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath));
        }
        isSettingValues = false;
    }

    public void checkDerbyMode() {
        if (isSettingValues) {
            return;
        }
        DerbyBase.mode = networkRadio.isSelected() ? "client" : "embedded";
        ConfigTools.writeConfigValue("DerbyMode", DerbyBase.mode);
        derbyBox.setDisable(true);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private String ret;

                @Override
                protected boolean handle() {
                    ret = DerbyBase.startDerby();
                    return ret != null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(ret, 6000);
                    setDerbyMode();
                }

                @Override
                protected void finalAction() {
                    derbyBox.setDisable(false);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        PDF settings
     */
    public void initPdfTab() {
        try {

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                                if (parentController instanceof BaseImageShapesController) {
                                    ((BaseImageShapesController) parentController).setMaskStroke();
                                } else if (parentController instanceof BaseImageController) {
                                    ((BaseImageController) parentController).setMaskStroke();
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

            strokeColorSetController.init(this, "StrokeColor", Color.web(BaseImageShapesController.DefaultStrokeColor));
            strokeColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (parentController != null) {
                        if (parentController instanceof BaseImageShapesController) {
                            ((BaseImageShapesController) parentController).setMaskStroke();
                        } else if (parentController instanceof BaseImageController) {
                            ((BaseImageController) parentController).setMaskStroke();
                        }
                    }
                    popSuccessful();
                }
            });

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
                                if (parentController instanceof BaseImageShapesController) {
                                    ((BaseImageShapesController) parentController).setMaskStroke();
                                } else if (parentController instanceof BaseImageController) {
                                    ((BaseImageController) parentController).setMaskStroke();
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

            anchorColorSetController.init(this, "AnchorColor", Color.web(BaseImageShapesController.DefaultAnchorColor));
            anchorColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (parentController instanceof BaseImageShapesController) {
                        ((BaseImageShapesController) parentController).setMaskStroke();
                    } else if (parentController instanceof BaseImageController) {
                        ((BaseImageController) parentController).setMaskStroke();
                    }
                    popSuccessful();
                }
            });

            anchorSolidCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("AnchorSolid", new_toggle);
                    if (parentController instanceof BaseImageShapesController) {
                        ((BaseImageShapesController) parentController).setMaskStroke();
                    } else if (parentController instanceof BaseImageController) {
                        ((BaseImageController) parentController).setMaskStroke();
                    }
                }
            });
            anchorSolidCheck.setSelected(AppVariables.getUserConfigBoolean("AnchorSolid", true));

            alphaColorSetController.init(this, "AlphaAsColor", Color.WHITE);
            alphaColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (!Color.WHITE.equals((Color) newValue)) {
                        alphaLabel.setText(message("AlphaReplaceComments"));
                        alphaLabel.setStyle(FxmlControl.darkRedText);
                    } else {
                        alphaLabel.setText("");
                        popSuccessful();
                    }
                }
            });

            FxmlControl.setTooltip(imageHisBox, new Tooltip(message("ImageHisComments")));

            thumbnailWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(thumbnailWidthInput.getText());
                        if (v > 0) {
                            AppVariables.setUserConfigInt("ThumbnailWidth", v);
                            thumbnailWidthInput.setStyle(null);
                            popSuccessful();
                        } else {
                            thumbnailWidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        thumbnailWidthInput.setStyle(badStyle);
                    }
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

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    protected void clearImageHistories(ActionEvent event) {
        if (!FxmlControl.askSure(getBaseTitle(), message("SureClear"))) {
            return;
        }
        new TableImageEditHistory().clear();
        popSuccessful();
    }

    /*
        Map settings
     */
    public void initMapTab() {
        try {
            tiandituWebKeyInput.setText(AppVariables.getUserConfigValue("TianDiTuWebKey", CommonValues.TianDiTuWebKey));
            gaodeWebKeyInput.setText(AppVariables.getUserConfigValue("GaoDeMapWebKey", CommonValues.GaoDeMapWebKey));
            gaodeServiceKeyInput.setText(AppVariables.getUserConfigValue("GaoDeMapServiceKey", CommonValues.GaoDeMapServiceKey));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void setMapKeysAction() {
        String tiandituKey = tiandituWebKeyInput.getText();
        String daodeWeb = gaodeWebKeyInput.getText();
        String gaoServiceKey = gaodeServiceKeyInput.getText();
        if (tiandituKey == null || tiandituKey.trim().isBlank()
                || daodeWeb == null || daodeWeb.trim().isBlank()
                || gaoServiceKey == null || gaoServiceKey.trim().isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        AppVariables.setUserConfigValue("TianDiTuWebKey", tiandituKey);
        AppVariables.setUserConfigValue("GaoDeMapWebKey", daodeWeb);
        AppVariables.setUserConfigValue("GaoDeMapServiceKey", gaoServiceKey);
    }

    @FXML
    public void defaultMapAction() {
        tiandituWebKeyInput.setText(CommonValues.TianDiTuWebKey);
        gaodeWebKeyInput.setText(CommonValues.GaoDeMapWebKey);
        gaodeServiceKeyInput.setText(CommonValues.GaoDeMapServiceKey);
        setMapKeysAction();
    }

    /*
        others
     */
    @FXML
    public void clearSettings(ActionEvent event) {
        clearUserSettings();
    }

    @FXML
    public void closeAction(ActionEvent event) {
        closeStage();
    }

}
