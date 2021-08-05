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
import mara.mybox.MyBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.fxml.WindowTools.refreshInterfaceAll;
import static mara.mybox.fxml.WindowTools.reloadAll;
import static mara.mybox.fxml.WindowTools.styleAll;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    protected RadioButton chineseRadio, englishRadio,
            redRadio, orangeRadio, pinkRadio, lightBlueRadio, blueRadio, darkGreenRadio,
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
        baseTitle = Languages.message("Settings");

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

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(hidpiIconsCheck, new Tooltip(Languages.message("HidpiIconsComments")));
            NodeStyleTools.setTooltip(redRadio, new Tooltip(Languages.message("MyBoxDarkRed")));
            NodeStyleTools.setTooltip(pinkRadio, new Tooltip(Languages.message("MyBoxDarkPink")));
            NodeStyleTools.setTooltip(orangeRadio, new Tooltip(Languages.message("MyBoxOrange")));
            NodeStyleTools.setTooltip(lightBlueRadio, new Tooltip(Languages.message("MyBoxDarkGreyBlue")));
            NodeStyleTools.setTooltip(blueRadio, new Tooltip(Languages.message("MyBoxDarkBlue")));
            NodeStyleTools.setTooltip(darkGreenRadio, new Tooltip(Languages.message("MyBoxDarkGreen")));
            NodeStyleTools.setTooltip(imageHisBox, new Tooltip(Languages.message("ImageHisComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initSettingValues() {
        try {
            stopAlarmCheck.setSelected(UserConfig.getUserConfigBoolean("StopAlarmsWhenExit"));
            newWindowCheck.setSelected(AppVariables.openStageInNewWindow);

            thumbnailWidthInput.setText(UserConfig.getUserConfigInt("ThumbnailWidth", 100) + "");

            recentFileNumber = UserConfig.getUserConfigInt("FileRecentNumber", 20);
            fileRecentInput.setText(recentFileNumber + "");

            String style = UserConfig.getUserConfigString("InterfaceStyle", AppValues.DefaultStyle);
            switch (style) {
                case AppValues.DefaultStyle:
                    styleBox.getSelectionModel().select(Languages.message("DefaultStyle"));
                    break;
                case AppValues.caspianStyle:
                    styleBox.getSelectionModel().select(Languages.message("caspianStyle"));
                    break;
                case AppValues.WhiteOnBlackStyle:
                    styleBox.getSelectionModel().select(Languages.message("WhiteOnBlackStyle"));
                    break;
                case AppValues.PinkOnBlackStyle:
                    styleBox.getSelectionModel().select(Languages.message("PinkOnBlackStyle"));
                    break;
                case AppValues.YellowOnBlackStyle:
                    styleBox.getSelectionModel().select(Languages.message("YellowOnBlackStyle"));
                    break;
                case AppValues.GreenOnBlackStyle:
                    styleBox.getSelectionModel().select(Languages.message("GreenOnBlackStyle"));
                    break;
                case AppValues.WhiteOnBlueStyle:
                    styleBox.getSelectionModel().select(Languages.message("WhiteOnBlueStyle"));
                    break;
                case AppValues.WhiteOnGreenStyle:
                    styleBox.getSelectionModel().select(Languages.message("WhiteOnGreenStyle"));
                    break;
                case AppValues.WhiteOnPurpleStyle:
                    styleBox.getSelectionModel().select(Languages.message("WhiteOnVioletredStyle"));
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
                case DarkGreen:
                    darkGreenRadio.fire();
                    break;
                case Red:
                default:
                    redRadio.fire();

            }

            controlsTextCheck.setSelected(AppVariables.controlDisplayText);
            hidpiIconsCheck.setSelected(AppVariables.hidpiIcons);

            imageWidthBox.getSelectionModel().select(UserConfig.getUserConfigInt("MaxImageSampleWidth", 4096) + "");

            splitPaneSensitiveCheck.setSelected(UserConfig.getUserConfigBoolean("ControlSplitPanesSensitive", false));
            mousePassControlPanesCheck.setSelected(UserConfig.getUserConfigBoolean("MousePassControlPanes", true));
            popColorSetCheck.setSelected(UserConfig.getUserConfigBoolean("PopColorSetWhenMousePassing", true));

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
                                ValidationTools.setEditorNormal(fontSizeBox);
                            } else {
                                ValidationTools.setEditorBadStyle(fontSizeBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(fontSizeBox);
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
                                ValidationTools.setEditorNormal(iconSizeBox);
                            } else {
                                ValidationTools.setEditorBadStyle(iconSizeBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(iconSizeBox);
                        }
                    }
                }
            });
            iconSizeBox.getSelectionModel().select(AppVariables.iconSize + "");

            newWindowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setOpenStageInNewWindow(newWindowCheck.isSelected());
                }
            });

            restoreStagesSizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setRestoreStagesSize(restoreStagesSizeCheck.isSelected());
                }
            });

            styleBox.getItems().addAll(Arrays.asList(Languages.message("DefaultStyle"), Languages.message("caspianStyle"),
                    Languages.message("WhiteOnBlackStyle"), Languages.message("PinkOnBlackStyle"),
                    Languages.message("YellowOnBlackStyle"), Languages.message("GreenOnBlackStyle"),
                    Languages.message("WhiteOnBlueStyle"), Languages.message("WhiteOnGreenStyle"),
                    Languages.message("WhiteOnVioletredStyle")));
            styleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkStyle(newValue);
                    }
                }
            });

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
                    UserConfig.setUserConfigBoolean("ControlDisplayText", AppVariables.controlDisplayText);
                    refreshInterfaceAll();
                }
            });

            hidpiIconsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.hidpiIcons = hidpiIconsCheck.isSelected();
                    UserConfig.setUserConfigBoolean("HidpiIcons", AppVariables.hidpiIcons);
                    refreshInterfaceAll();
                }
            });

            popSizeSelector.getItems().addAll(Arrays.asList(
                    "1.5", "1", "1.2", "2", "2.5", "0.8")
            );
            popSizeSelector.setValue(UserConfig.getUserConfigString("PopTextSize", "1.5"));
            popSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            float f = Float.parseFloat(newValue);
                            if (f > 0) {
                                UserConfig.setUserConfigString("PopTextSize", newValue);
                                ValidationTools.setEditorNormal(popSizeSelector);
                                popSuccessful();
                            } else {
                                ValidationTools.setEditorBadStyle(popSizeSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(popSizeSelector);
                        }
                    }
                }
            });

            popDurationSelector.getItems().addAll(Arrays.asList(
                    "3000", "5000", "2000", "1500", "1000", "4000", "2500")
            );
            popDurationSelector.setValue(UserConfig.getUserConfigInt("PopTextDuration", 3000) + "");
            popDurationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                UserConfig.setUserConfigInt("PopTextDuration", v);
                                ValidationTools.setEditorNormal(popDurationSelector);
                                popSuccessful();
                            } else {
                                ValidationTools.setEditorBadStyle(popDurationSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(popDurationSelector);
                        }
                    }
                }
            });

            popBgColorController.init(this, "PopTextBgColor", Color.BLACK);
            popBgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setUserConfigString("PopTextBgColor", popBgColorController.rgb());
                    popSuccessful();
                }
            });

            popInfoColorController.init(this, "PopInfoColor", Color.WHITE);
            popInfoColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setUserConfigString("PopInfoColor", popInfoColorController.rgb());
                    popSuccessful();
                }
            });

            popErrorColorController.init(this, "PopErrorColor", Color.AQUA);
            popErrorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setUserConfigString("PopErrorColor", popErrorColorController.rgb());
                    popSuccessful();
                }
            });

            popWarnColorController.init(this, "PopWarnColor", Color.ORANGE);
            popWarnColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    UserConfig.setUserConfigString("PopWarnColor", popWarnColorController.rgb());
                    popSuccessful();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkLanguage() {
        if (AppVariables.currentBundle == Languages.BundleZhCN) {
            chineseRadio.setSelected(true);
        } else {
            englishRadio.setSelected(true);
        }
    }

    protected void checkStyle(String s) {
        try {
            if (Languages.message("DefaultStyle").equals(s)) {
                setStyle(AppValues.MyBoxStyle);
            } else if (Languages.message("caspianStyle").equals(s)) {
                setStyle(AppValues.caspianStyle);
            } else if (Languages.message("WhiteOnBlackStyle").equals(s)) {
                setStyle(AppValues.WhiteOnBlackStyle);
            } else if (Languages.message("PinkOnBlackStyle").equals(s)) {
                setStyle(AppValues.PinkOnBlackStyle);
            } else if (Languages.message("YellowOnBlackStyle").equals(s)) {
                setStyle(AppValues.YellowOnBlackStyle);
            } else if (Languages.message("GreenOnBlackStyle").equals(s)) {
                setStyle(AppValues.GreenOnBlackStyle);
            } else if (Languages.message("WhiteOnBlueStyle").equals(s)) {
                setStyle(AppValues.WhiteOnBlueStyle);
            } else if (Languages.message("WhiteOnGreenStyle").equals(s)) {
                setStyle(AppValues.WhiteOnGreenStyle);
            } else if (Languages.message("WhiteOnVioletredStyle").equals(s)) {
                setStyle(AppValues.WhiteOnPurpleStyle);
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
                StyleTools.setConfigStyleColor("Pink");
            } else if (lightBlueRadio.isSelected()) {
                StyleTools.setConfigStyleColor("LightBlue");
            } else if (blueRadio.isSelected()) {
                StyleTools.setConfigStyleColor("Blue");
            } else if (orangeRadio.isSelected()) {
                StyleTools.setConfigStyleColor("Orange");
            } else if (darkGreenRadio.isSelected()) {
                StyleTools.setConfigStyleColor("DarkGreen");
            } else {
                StyleTools.setConfigStyleColor("Red");
            }
            refreshInterfaceAll();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setStyle(String style) {
        try {
            UserConfig.setUserConfigString("InterfaceStyle", style);
            styleAll(style);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        Languages.setLanguage("zh");
        reloadAll();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        Languages.setLanguage("en");
        reloadAll();
    }

    @FXML
    protected void mousePassControlPanes() {
        UserConfig.setUserConfigBoolean("MousePassControlPanes", mousePassControlPanesCheck.isSelected());
    }

    @FXML
    protected void popColorSet() {
        UserConfig.setUserConfigBoolean("PopColorSetWhenMousePassing", popColorSetCheck.isSelected());
    }

    @FXML
    protected void splitPaneSensitive() {
        UserConfig.setUserConfigBoolean("ControlSplitPanesSensitive", splitPaneSensitiveCheck.isSelected());
    }

    /*
        Base settings
     */
    public void initBaseTab() {
        try {
            int mb = 1024 * 1024;
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            final long totalM = osmxb.getTotalPhysicalMemorySize() / mb;
            String m = Languages.message("PhysicalMemory") + ": " + totalM + "MB";
            Runtime r = Runtime.getRuntime();
            final long jvmM = r.maxMemory() / mb;
            m += "    " + Languages.message("JvmXmx") + ": " + jvmM + "MB";
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
                            jvmInput.setStyle(NodeStyleTools.badStyle);
                            settingsJVMButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        jvmInput.setStyle(NodeStyleTools.badStyle);
                        settingsJVMButton.setDisable(true);
                    }
                }
            });
            isSettingValues = true;
            jvmInput.setText(jvmM + "");
            settingsJVMButton.setDisable(true);
            isSettingValues = false;

            webConnectTimeoutInput.setText(UserConfig.getUserConfigInt("WebConnectTimeout", 10000) + "");
            webReadTimeoutInput.setText(UserConfig.getUserConfigInt("WebReadTimeout", 10000) + "");

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
            if (!PopTools.askSure(getBaseTitle(), Languages.message("ChangeDataPathConfirm"))) {
                return;
            }
            popInformation(Languages.message("CopyingFilesFromTo"));
            String oldPath = AppVariables.MyboxDataPath;
            if (FileCopyTools.copyWholeDirectory(new File(oldPath), new File(newPath), null, false)) {
                File lckFile = new File(newPath + File.separator
                        + "mybox_derby" + File.separator + "db.lck");
                if (lckFile.exists()) {
                    try {
                        FileDeleteTools.delete(lckFile);
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
                dataDirInput.setStyle(NodeStyleTools.badStyle);
            }

        } catch (Exception e) {
            popFailed();
            dataDirInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    @FXML
    protected void okWebConnectTimeout() {
        try {
            int v = Integer.parseInt(webConnectTimeoutInput.getText());
            if (v > 0) {
                UserConfig.setUserConfigInt("WebConnectTimeout", v);
                webConnectTimeoutInput.setStyle(null);
                popSuccessful();
            } else {
                webConnectTimeoutInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            webConnectTimeoutInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    @FXML
    protected void okWebReadTimeout() {
        try {
            int v = Integer.parseInt(webReadTimeoutInput.getText());
            if (v > 0) {
                UserConfig.setUserConfigInt("WebReadTimeout", v);
                webReadTimeoutInput.setStyle(null);
                popSuccessful();
            } else {
                webReadTimeoutInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            webReadTimeoutInput.setStyle(NodeStyleTools.badStyle);
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
            currentDataPathLabel.setText(MessageFormat.format(Languages.message("CurrentValue"), AppVariables.MyboxDataPath));
            clearCurrentRootCheck.setText(MessageFormat.format(Languages.message("ClearPathWhenChange"), AppVariables.MyboxDataPath));

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
                    UserConfig.setUserConfigBoolean("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void setFileRecentAction(ActionEvent event) {
        UserConfig.setUserConfigInt("FileRecentNumber", recentFileNumber);
        AppVariables.fileRecentNumber = recentFileNumber;
        popSuccessful();
    }

    @FXML
    protected void clearFileHistories(ActionEvent event) {
        if (!PopTools.askSure(getBaseTitle(), Languages.message("SureClear"))) {
            return;
        }
        new TableVisitHistory().clear();
        popSuccessful();
    }

    @FXML
    protected void noFileHistories(ActionEvent event) {
        fileRecentInput.setText("0");
        UserConfig.setUserConfigInt("FileRecentNumber", 0);
        AppVariables.fileRecentNumber = 0;
        popSuccessful();
    }

    public void setDerbyMode() {
        isSettingValues = true;
        if (DerbyStatus.Nerwork == DerbyBase.status) {
            networkRadio.setSelected(true);
            derbyStatus.setText(MessageFormat.format(Languages.message("DerbyServerListening"), DerbyBase.port + ""));
        } else if (DerbyStatus.Embedded == DerbyBase.status) {
            embeddedRadio.setSelected(true);
            derbyStatus.setText(Languages.message("DerbyEmbeddedMode"));
        } else {
            networkRadio.setSelected(false);
            embeddedRadio.setSelected(false);
            derbyStatus.setText(MessageFormat.format(Languages.message("DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath));
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
            handling(task);
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
        String pm = UserConfig.getUserConfigString("PdfMemDefault", "1GB");
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
        UserConfig.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        UserConfig.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        UserConfig.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        UserConfig.setPdfMem("Unlimit");
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
                                UserConfig.setUserConfigInt("StrokeWidth", v);
                                ValidationTools.setEditorNormal(strokeWidthBox);
                                if (parentController instanceof BaseImageShapesController) {
                                    ((BaseImageShapesController) parentController).setMaskStroke();
                                } else if (parentController instanceof BaseImageController) {
                                    ((BaseImageController) parentController).setMaskStroke();
                                }
                            } else {
                                ValidationTools.setEditorBadStyle(strokeWidthBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(strokeWidthBox);
                        }
                    }
                }
            });
            strokeWidthBox.getSelectionModel().select(UserConfig.getUserConfigString("StrokeWidth", "3"));

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
                                UserConfig.setUserConfigInt("AnchorWidth", v);
                                ValidationTools.setEditorNormal(anchorWidthBox);
                                if (parentController instanceof BaseImageShapesController) {
                                    ((BaseImageShapesController) parentController).setMaskStroke();
                                } else if (parentController instanceof BaseImageController) {
                                    ((BaseImageController) parentController).setMaskStroke();
                                }
                            } else {
                                ValidationTools.setEditorBadStyle(anchorWidthBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(anchorWidthBox);
                        }
                    }
                }
            });
            anchorWidthBox.getSelectionModel().select(UserConfig.getUserConfigString("AnchorWidth", "10"));

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
                    UserConfig.setUserConfigBoolean("AnchorSolid", new_toggle);
                    if (parentController instanceof BaseImageShapesController) {
                        ((BaseImageShapesController) parentController).setMaskStroke();
                    } else if (parentController instanceof BaseImageController) {
                        ((BaseImageController) parentController).setMaskStroke();
                    }
                }
            });
            anchorSolidCheck.setSelected(UserConfig.getUserConfigBoolean("AnchorSolid", true));

            alphaColorSetController.init(this, "AlphaAsColor", Color.WHITE);
            alphaColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (!Color.WHITE.equals((Color) newValue)) {
                        alphaLabel.setText(Languages.message("AlphaReplaceComments"));
                        alphaLabel.setStyle(NodeStyleTools.darkRedText);
                    } else {
                        alphaLabel.setText("");
                        popSuccessful();
                    }
                }
            });

            thumbnailWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(thumbnailWidthInput.getText());
                        if (v > 0) {
                            UserConfig.setUserConfigInt("ThumbnailWidth", v);
                            thumbnailWidthInput.setStyle(null);
                            popSuccessful();
                        } else {
                            thumbnailWidthInput.setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        thumbnailWidthInput.setStyle(NodeStyleTools.badStyle);
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
                                UserConfig.setUserConfigInt("MaxImageSampleWidth", v);
                                ValidationTools.setEditorNormal(imageWidthBox);
                            } else {
                                ValidationTools.setEditorBadStyle(imageWidthBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(imageWidthBox);
                        }
                    }
                }
            });
            imageWidthBox.getSelectionModel().select(UserConfig.getUserConfigString("MaxImageSampleWidth", "4096"));

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
                fileRecentInput.setStyle(NodeStyleTools.badStyle);
                settingsRecentOKButton.setDisable(true);
            }
        } catch (Exception e) {
            fileRecentInput.setStyle(NodeStyleTools.badStyle);
            settingsRecentOKButton.setDisable(true);
        }
    }

    @FXML
    protected void clearImageHistories(ActionEvent event) {
        if (!PopTools.askSure(getBaseTitle(), Languages.message("SureClear"))) {
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
            tiandituWebKeyInput.setText(UserConfig.getUserConfigString("TianDiTuWebKey", AppValues.TianDiTuWebKey));
            gaodeWebKeyInput.setText(UserConfig.getUserConfigString("GaoDeMapWebKey", AppValues.GaoDeMapWebKey));
            gaodeServiceKeyInput.setText(UserConfig.getUserConfigString("GaoDeMapServiceKey", AppValues.GaoDeMapServiceKey));
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
            popError(Languages.message("InvalidData"));
            return;
        }
        UserConfig.setUserConfigString("TianDiTuWebKey", tiandituKey);
        UserConfig.setUserConfigString("GaoDeMapWebKey", daodeWeb);
        UserConfig.setUserConfigString("GaoDeMapServiceKey", gaoServiceKey);
    }

    @FXML
    public void defaultMapAction() {
        tiandituWebKeyInput.setText(AppValues.TianDiTuWebKey);
        gaodeWebKeyInput.setText(AppValues.GaoDeMapWebKey);
        gaodeServiceKeyInput.setText(AppValues.GaoDeMapServiceKey);
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
