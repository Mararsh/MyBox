package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.data.BaseTask;
import mara.mybox.dev.DevTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class MainMenuController extends BaseController {

    private HBox memoryBox, cpuBox;
    private Timer memoryMonitorTimer, cpuMonitorTimer;
    private final int memoryMonitorInterval = 1000, cpuMonitorInterval = 1000;
    private Runtime r;
    private OperatingSystemMXBean osmxb;
    private Label sysMemLabel, myboxMemLabel, sysCpuLabel, myboxCpuLabel;
    private ProgressBar sysMemBar, myboxMemBar, sysCpuBar, myboxCpuBar;
    private long mb;
    private BaseTask iconTask;

    @FXML
    protected Pane mainMenuPane;
    @FXML
    protected ToggleGroup langGroup;
    @FXML
    protected RadioMenuItem chineseMenuItem, englishMenuItem,
            font12MenuItem, font15MenuItem, font17MenuItem,
            normalIconMenuItem, bigIconMenuItem, smallIconMenuItem,
            pinkMenuItem, redMenuItem, blueMenuItem, lightBlueMenuItem, orangeMenuItem;
    @FXML
    protected CheckMenuItem monitorMemroyCheck, monitorCpuCheck,
            newWindowCheck, restoreStagesSizeCheck, popRecentCheck, controlTextCheck, hidpiIconsCheck;
    @FXML
    protected Menu settingsMenu, recentMenu, helpMenu;
    @FXML
    protected MenuItem manageLanguagesMenuItem, makeIconsItem;

    @Override
    public void initControls() {
        try {
            super.initControls();
            settingsMenu.setOnShowing((Event e) -> {
                checkSettings();
            });
            checkSettings();

            recentMenu.setOnShowing((Event e) -> {
                recentMenu.getItems().clear();
                recentMenu.getItems().addAll(getRecentMenu());
            });

            helpMenu.setOnShowing((Event e) -> {
                if (!AppVariables.devMode) {
                    helpMenu.getItems().remove(makeIconsItem);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSettings() {
        checkLanguage();
        checkFontSize();
        checkIconSize();
        monitorMemroyCheck.setSelected(AppVariables.getUserConfigBoolean("MonitorMemroy", false));
        monitorCpuCheck.setSelected(AppVariables.getUserConfigBoolean("MonitorCpu", false));
        controlTextCheck.setSelected(AppVariables.controlDisplayText);
        hidpiIconsCheck.setSelected(AppVariables.hidpiIcons);
        newWindowCheck.setSelected(AppVariables.openStageInNewWindow);
        restoreStagesSizeCheck.setSelected(AppVariables.restoreStagesSize);
        popRecentCheck.setSelected(AppVariables.fileRecentNumber > 0);
        checkControlColor();
        checkMemroyMonitor();
        checkCpuMonitor();
    }

    protected void checkLanguage() {
        List<MenuItem> items = new ArrayList();
        items.addAll(settingsMenu.getItems());
        int pos1 = items.indexOf(englishMenuItem);
        int pos2 = items.indexOf(manageLanguagesMenuItem);
        for (int i = pos2 - 1; i > pos1; --i) {
            items.remove(i);
        }
        List<String> languages = ConfigTools.userLanguages();
        if (languages != null && !languages.isEmpty()) {
            String lang = AppVariables.getLanguage();
            for (int i = 0; i < languages.size(); ++i) {
                final String name = languages.get(i);
                RadioMenuItem langItem = new RadioMenuItem(name);
                langItem.setToggleGroup(langGroup);
                langItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setLanguage(name);
                        refresh();
                    }
                });
                items.add(pos1 + 1 + i, langItem);
                if (name.equals(lang)) {
                    isSettingValues = true;
                    langItem.setSelected(true);
                    isSettingValues = false;
                }
            }
        }
        settingsMenu.getItems().clear();
        settingsMenu.getItems().addAll(items);
        if (AppVariables.currentBundle == CommonValues.BundleZhCN) {
            chineseMenuItem.setSelected(true);
        } else if (AppVariables.currentBundle == CommonValues.BundleEn) {
            englishMenuItem.setSelected(true);
        }

    }

    protected void checkFontSize() {
        switch (AppVariables.sceneFontSize) {
            case 12:
                font12MenuItem.setSelected(true);
                break;
            case 15:
                font15MenuItem.setSelected(true);
                break;
            case 17:
                font17MenuItem.setSelected(true);
                break;
            default:
                font12MenuItem.setSelected(false);
                font15MenuItem.setSelected(false);
                font17MenuItem.setSelected(false);
                break;
        }
    }

    protected void checkIconSize() {
        switch (AppVariables.iconSize) {
            case 20:
                normalIconMenuItem.setSelected(true);
                break;
            case 15:
                smallIconMenuItem.setSelected(true);
                break;
            case 30:
                bigIconMenuItem.setSelected(true);
                break;
            default:
                normalIconMenuItem.setSelected(false);
                smallIconMenuItem.setSelected(false);
                bigIconMenuItem.setSelected(false);
                break;
        }
    }

    protected void checkControlColor() {
        switch (AppVariables.ControlColor) {
            case Default:
            case Red:
                redMenuItem.setSelected(true);
                break;
            case Pink:
                pinkMenuItem.setSelected(true);
                break;
            case Blue:
                blueMenuItem.setSelected(true);
                break;
            case LightBlue:
                lightBlueMenuItem.setSelected(true);
                break;
            case Orange:
                orangeMenuItem.setSelected(true);
                break;
        }
    }

    @FXML
    protected void showHome(ActionEvent event) {
        openStage(CommonValues.MyboxFxml);
    }

    @FXML
    protected void resetWindows(ActionEvent event) {
        AppVariables.resetWindows();
        refresh();
    }

    @FXML
    protected void fullScreen(ActionEvent event) {
        parentController.getMyStage().setFullScreen(true);
    }

    @FXML
    protected void closeWindow(ActionEvent event) {
        parentController.closeStage();
    }

    @FXML
    protected void closeOtherWindows(ActionEvent event) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            if (parentController != null) {
                if (!window.equals(parentController.getMyStage())) {
                    window.hide();
                }
            } else {
                if (!window.equals(myStage)) {
                    window.hide();
                }
            }
        }
    }

    @FXML
    protected void MyBoxProperties(ActionEvent event) {
        openStage(CommonValues.MyBoxPropertiesFxml);
    }

    @FXML
    protected void MyBoxLogs(ActionEvent event) {
        openStage(CommonValues.MyBoxLogsFxml);
    }

    @FXML
    protected void Shortcuts(ActionEvent event) {
        openStage(CommonValues.ShortcutsFxml);
    }

    protected void makeMemoryMonitorBox() {
        sysMemLabel = new Label();
        sysMemBar = new ProgressBar();
        sysMemBar.setPrefHeight(20);
        sysMemBar.setPrefWidth(70);

        myboxMemLabel = new Label();
        myboxMemBar = new ProgressBar();
        myboxMemBar.setPrefHeight(20);
        myboxMemBar.setPrefWidth(70);

        memoryBox = new HBox();
        memoryBox.setAlignment(Pos.CENTER_LEFT);
        memoryBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        memoryBox.setSpacing(5);
        VBox.setVgrow(memoryBox, Priority.NEVER);
        HBox.setHgrow(memoryBox, Priority.ALWAYS);
        memoryBox.setStyle(" -fx-font-size: 0.8em;");

        memoryBox.getChildren().addAll(myboxMemLabel, myboxMemBar, new Label("    "), sysMemLabel, sysMemBar);
        mb = 1024 * 1024;
        if (r == null) {
            r = Runtime.getRuntime();
        }
        if (osmxb == null) {
            osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        }
    }

    protected void startMemoryMonitorTimer() {
        try {
            if (memoryBox == null) {
                return;
            }
            stopMemoryMonitorTimer();
            memoryMonitorTimer = new Timer();
            memoryMonitorTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            long physicalFree = osmxb.getFreePhysicalMemorySize() / mb;
                            long physicalTotal = osmxb.getTotalPhysicalMemorySize() / mb;
                            long physicalUse = physicalTotal - physicalFree;
                            String sysInfo = System.getProperty("os.name")
                                    + " " + AppVariables.message("PhysicalMemory") + ":" + physicalTotal + "MB"
                                    + " " + AppVariables.message("Used") + ":"
                                    + physicalUse + "MB (" + FloatTools.roundFloat2(physicalUse * 100.0f / physicalTotal) + "%)";
                            sysMemLabel.setText(sysInfo);
                            sysMemBar.setProgress(physicalUse * 1.0f / physicalTotal);

                            long freeMemory = r.freeMemory() / mb;
                            long totalMemory = r.totalMemory() / mb;
                            long maxMemory = r.maxMemory() / mb;
                            long usedMemory = totalMemory - freeMemory;
                            String myboxInfo = "MyBox"
                                    //                    + "  " + AppVariables.getMessage("AvailableProcessors") + ":" + availableProcessors
                                    + " " + AppVariables.message("AvaliableMemory") + ":" + maxMemory + "MB"
                                    + " " + AppVariables.message("Required") + ":"
                                    + totalMemory + "MB(" + FloatTools.roundFloat2(totalMemory * 100.0f / maxMemory) + "%)"
                                    + " " + AppVariables.message("Used") + ":"
                                    + usedMemory + "MB(" + FloatTools.roundFloat2(usedMemory * 100.0f / maxMemory) + "%)";
                            myboxMemLabel.setText(myboxInfo);
                            myboxMemBar.setProgress(usedMemory * 1.0f / maxMemory);
                        }
                    });
                }
            }, 0, memoryMonitorInterval);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void stopMemoryMonitorTimer() {
        if (memoryMonitorTimer != null) {
            memoryMonitorTimer.cancel();
        }
        memoryMonitorTimer = null;
    }

    @FXML
    protected void checkMemroyMonitor() {
        boolean v = monitorMemroyCheck.isSelected();
        AppVariables.setUserConfigValue("MonitorMemroy", v);
        if (v) {
            if (memoryBox == null) {
                makeMemoryMonitorBox();
            }
            if (!thisPane.getChildren().contains(memoryBox)) {
                thisPane.getChildren().add(memoryBox);
            }
            startMemoryMonitorTimer();
        } else {
            stopMemoryMonitorTimer();
            if (memoryBox != null && thisPane.getChildren().contains(memoryBox)) {
                thisPane.getChildren().remove(memoryBox);
            }
        }
    }

    protected void makeCpuMonitorBox() {
        sysCpuLabel = new Label();
        sysCpuBar = new ProgressBar();
        sysCpuBar.setPrefHeight(20);
        sysCpuBar.setPrefWidth(70);

        myboxCpuLabel = new Label();
        myboxCpuBar = new ProgressBar();
        myboxCpuBar.setPrefHeight(20);
        myboxCpuBar.setPrefWidth(70);

        cpuBox = new HBox();
        cpuBox.setAlignment(Pos.CENTER_LEFT);
        cpuBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cpuBox.setSpacing(5);
        VBox.setVgrow(cpuBox, Priority.NEVER);
        HBox.setHgrow(cpuBox, Priority.ALWAYS);
        cpuBox.setStyle(" -fx-font-size: 0.8em;");

        cpuBox.getChildren().addAll(myboxCpuLabel, myboxCpuBar, new Label("    "), sysCpuLabel, sysCpuBar);
        if (osmxb == null) {
            osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        }
    }

    protected void startCpuMonitorTimer() {
        try {
            if (cpuBox == null) {
                return;
            }
            stopCpuMonitorTimer();
            cpuMonitorTimer = new Timer();
            cpuMonitorTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            float load = (float) osmxb.getSystemCpuLoad();
                            long s = (long) (osmxb.getSystemLoadAverage() / 1000000000);
                            String sysInfo = System.getProperty("os.name")
                                    + " " + AppVariables.message("SystemLoadAverage") + ":" + s + "s"
                                    + " " + AppVariables.message("SystemCpuUsage") + ":"
                                    + FloatTools.roundFloat2(load * 100) + "%";
                            sysCpuLabel.setText(sysInfo);
                            sysCpuBar.setProgress(load);

                            load = (float) osmxb.getProcessCpuLoad();
                            s = osmxb.getProcessCpuTime() / 1000000000;
                            String myboxInfo = "MyBox"
                                    + " " + AppVariables.message("RecentCpuTime") + ":" + s + "s"
                                    + " " + AppVariables.message("RecentCpuUsage") + ":"
                                    + FloatTools.roundFloat2(load * 100) + "%";
                            myboxCpuLabel.setText(myboxInfo);
                            myboxCpuBar.setProgress(load);

                        }
                    });
                }
            }, 0, cpuMonitorInterval);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void stopCpuMonitorTimer() {
        if (cpuMonitorTimer != null) {
            cpuMonitorTimer.cancel();
        }
        cpuMonitorTimer = null;
    }

    @FXML
    protected void checkCpuMonitor() {
        boolean v = monitorCpuCheck.isSelected();
        AppVariables.setUserConfigValue("MonitorCpu", v);
        if (v) {
            if (cpuBox == null) {
                makeCpuMonitorBox();
            }
            if (!thisPane.getChildren().contains(cpuBox)) {
                thisPane.getChildren().add(cpuBox);
            }
            startCpuMonitorTimer();
        } else {
            stopCpuMonitorTimer();
            if (cpuBox != null && thisPane.getChildren().contains(cpuBox)) {
                thisPane.getChildren().remove(cpuBox);
            }
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        AppVariables.setLanguage("zh");
        refresh();
    }

    @FXML
    protected void openManageLanguages(ActionEvent event) {
        loadScene(CommonValues.MyBoxLanguagesFxml);
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVariables.setLanguage("en");
        refresh();
    }

    @FXML
    protected void setFont12(ActionEvent event) {
        AppVariables.setSceneFontSize(12);
        refresh();
    }

    @FXML
    protected void setFont15(ActionEvent event) {
        AppVariables.setSceneFontSize(15);
        refresh();
    }

    @FXML
    protected void setFont17(ActionEvent event) {
        AppVariables.setSceneFontSize(17);
        refresh();
    }

    @FXML
    protected void normalIcon(ActionEvent event) {
        AppVariables.setIconSize(20);
        refresh();
    }

    @FXML
    protected void bigIcon(ActionEvent event) {
        AppVariables.setIconSize(30);
        refresh();
    }

    @FXML
    protected void smallIcon(ActionEvent event) {
        AppVariables.setIconSize(15);
        refresh();
    }

    @FXML
    protected void setDefaultColor(ActionEvent event) {
        ControlStyle.setConfigColorStyle("default");
        refresh();
    }

    @FXML
    protected void setPink(ActionEvent event) {
        ControlStyle.setConfigColorStyle("pink");
        refresh();
    }

    @FXML
    protected void setRed(ActionEvent event) {
        ControlStyle.setConfigColorStyle("red");
        refresh();
    }

    @FXML
    protected void setBlue(ActionEvent event) {
        ControlStyle.setConfigColorStyle("blue");
        refresh();
    }

    @FXML
    protected void setLightBlue(ActionEvent event) {
        ControlStyle.setConfigColorStyle("lightBlue");
        refresh();
    }

    @FXML
    protected void setOrange(ActionEvent event) {
        ControlStyle.setConfigColorStyle("orange");
        refresh();
    }

    @FXML
    protected void setControlDisplayText(ActionEvent event) {
        AppVariables.controlDisplayText = controlTextCheck.isSelected();
        AppVariables.setUserConfigValue("ControlDisplayText", controlTextCheck.isSelected());
        refresh();
    }

    @FXML
    protected void hidpiIcons(ActionEvent event) {
        AppVariables.hidpiIcons = hidpiIconsCheck.isSelected();
        AppVariables.setUserConfigValue("HidpiIcons", AppVariables.hidpiIcons);
        if (AppVariables.hidpiIcons) {
            if (Toolkit.getDefaultToolkit().getScreenResolution() <= 120) {
                parentController.alertInformation(message("HidpiIconsComments"));
            }
        } else {
            if (Toolkit.getDefaultToolkit().getScreenResolution() > 120) {
                parentController.alertInformation(message("HidpiIconsComments"));
            }
        }
        refresh();
    }

    @FXML
    @Override
    public BaseController refresh() {
        return parentController.refresh();
    }

    @FXML
    protected void newWindowAction() {
        AppVariables.setOpenStageInNewWindow(newWindowCheck.isSelected());
    }

    @FXML
    protected void restoreStagesSizeAction() {
        AppVariables.setRestoreStagesSize(restoreStagesSizeCheck.isSelected());
    }

    @FXML
    protected void popRecentAction() {
        if (popRecentCheck.isSelected()) {
            AppVariables.fileRecentNumber = 15;
        } else {
            AppVariables.fileRecentNumber = 0;
        }
        AppVariables.setUserConfigInt("FileRecentNumber", AppVariables.fileRecentNumber);

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

    @FXML
    protected void setDefaultStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.DefaultStyle);
    }

    @FXML
    protected void setWhiteOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnBlackStyle);
    }

    @FXML
    protected void setYellowOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.YellowOnBlackStyle);
    }

    @FXML
    protected void setWhiteOnGreenStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnGreenStyle);
    }

    @FXML
    protected void setCaspianStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.caspianStyle);
    }

    @FXML
    protected void setGreenOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.GreenOnBlackStyle);
    }

    @FXML
    protected void setPinkOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.PinkOnBlackStyle);
    }

    @FXML
    protected void setBlackOnYellowStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.BlackOnYellowStyle);
    }

    @FXML
    protected void setWhiteOnPurpleStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnPurpleStyle);
    }

    @FXML
    protected void setWhiteOnBlueStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnBlueStyle);
    }

    @Override
    public void setInterfaceStyle(String style) {
        try {
            AppVariables.setUserConfigValue("InterfaceStyle", style);
            if (parentController != null) {
                parentController.setInterfaceStyle(style);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editConfigFile(ActionEvent event) {
        TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        controller.openTextFile(AppVariables.MyboxConfigFile);
        controller.popInformation(message("TakeEffectWhenReboot"));
    }

    @FXML
    public void clearSettings(ActionEvent event) {
        parentController.clearUserSettings();
    }

    @FXML
    protected void exit(ActionEvent event) {
        FxmlStage.appExit();
    }

    @Override
    public BaseController loadScene(String newFxml) {
        try {
            if (AppVariables.openStageInNewWindow) {
                return parentController.openStage(newFxml);
            } else {
                return parentController.loadScene(newFxml);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public BaseController openStage(String newFxml) {
        return parentController.openStage(newFxml);
    }

    @FXML
    protected void openPdfView(ActionEvent event) {
        loadScene(CommonValues.PdfViewFxml);
    }

    @FXML
    protected void openPdfHtmlViewer(ActionEvent event) {
        loadScene(CommonValues.PdfHtmlViewerFxml);
    }

    @FXML
    protected void openPDFAttributes(ActionEvent event) {
        loadScene(CommonValues.PdfAttributesFxml);
    }

    @FXML
    protected void openPDFAttributesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfAttributesBatchFxml);
    }

    @FXML
    protected void openPdfConvertImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfConvertImagesBatchFxml);
    }

    @FXML
    protected void openPdfConvertHtmlsBatch(ActionEvent event) {
        loadScene(CommonValues.PdfConvertHtmlsBatchFxml);
    }

    @FXML
    protected void openImagesCombinePdf(ActionEvent event) {
        loadScene(CommonValues.ImagesCombinePdfFxml);
    }

    @FXML
    protected void openPdfExtractTextsBatch(ActionEvent event) {
        loadScene(CommonValues.PdfExtractTextsBatchFxml);
    }

    @FXML
    protected void openPdfExtractImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfExtractImagesBatchFxml);
    }

    @FXML
    protected void openPdfImagesConvertBatch(ActionEvent event) {
        loadScene(CommonValues.PdfImagesConvertBatchFxml);
    }

    @FXML
    protected void openMergePdf(ActionEvent event) {
        loadScene(CommonValues.PdfMergeFxml);
    }

    @FXML
    protected void openPdfSplitBatch(ActionEvent event) {
        loadScene(CommonValues.PdfSplitBatchFxml);
    }

    @FXML
    protected void openPdfOCRBatch(ActionEvent event) {
        loadScene(CommonValues.PdfOCRBatchFxml);
    }

    @FXML
    protected void openCompressPdfImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfCompressImagesBatchFxml);
    }

    @FXML
    protected void openImageViewer(ActionEvent event) {
        loadScene(CommonValues.ImageViewerFxml);
    }

    @FXML
    protected void openImagesBrowser(ActionEvent event) {
        loadScene(CommonValues.ImagesBrowserFxml);
    }

    @FXML
    protected void openImageData(ActionEvent event) {
        loadScene(CommonValues.ImageAnalyseFxml);
    }

    @FXML
    protected void openImageConverterBatch(ActionEvent event) {
        loadScene(CommonValues.ImageConverterBatchFxml);
    }

    @FXML
    protected void openImageManufacture(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureFxml);
    }

    @FXML
    protected void openImageManufactureBatchSize(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchSizeFxml);
    }

    @FXML
    protected void openImageManufactureBatchCrop(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchCropFxml);
    }

    @FXML
    protected void openImageManufactureBatchColor(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchColorFxml);
    }

    @FXML
    protected void openImageManufactureBatchEffects(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchEffectsFxml);
    }

    @FXML
    protected void openImageManufactureBatchEnhancement(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchEnhancementFxml);
    }

    @FXML
    protected void openImageManufactureBatchReplaceColor(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchReplaceColorFxml);
    }

    @FXML
    protected void openImageManufactureBatchText(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchTextFxml);
    }

    @FXML
    protected void openImageManufactureBatchArc(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchArcFxml);
    }

    @FXML
    protected void openImageManufactureBatchShadow(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchShadowFxml);
    }

    @FXML
    protected void openImageManufactureBatchTransform(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchTransformFxml);
    }

    @FXML
    protected void openImageManufactureBatchMargins(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchMarginsFxml);
    }

    @FXML
    protected void openImageSplit(ActionEvent event) {
        loadScene(CommonValues.ImageSplitFxml);
    }

    @FXML
    protected void openImageSample(ActionEvent event) {
        loadScene(CommonValues.ImageSampleFxml);
    }

    @FXML
    protected void openImagesCombine(ActionEvent event) {
        loadScene(CommonValues.ImagesCombineFxml);
    }

    @FXML
    protected void openImageGifViewer(ActionEvent event) {
        loadScene(CommonValues.ImageGifViewerFxml);
    }

    @FXML
    protected void openImageGifEditer(ActionEvent event) {
        loadScene(CommonValues.ImageGifEditerFxml);
    }

    @FXML
    protected void openImageTiffEditer(ActionEvent event) {
        loadScene(CommonValues.ImageTiffEditerFxml);
    }

    @FXML
    protected void openImageFramesViewer(ActionEvent event) {
        loadScene(CommonValues.ImageFramesViewerFxml);
    }

    @FXML
    protected void openImagesBlend(ActionEvent event) {
        loadScene(CommonValues.ImagesBlendFxml);
    }

    @FXML
    protected void openImageStatistic(ActionEvent event) {
        loadScene(CommonValues.ImageStatisticFxml);
    }

    @FXML
    protected void openImageAlphaExtract(ActionEvent event) {
        loadScene(CommonValues.ImageAlphaExtractBatchFxml);
    }

    @FXML
    protected void openImageAlphaAdd(ActionEvent event) {
        loadScene(CommonValues.ImageAlphaAddBatchFxml);
    }

    @FXML
    protected void openImageOCR(ActionEvent event) {
        loadScene(CommonValues.ImageOCRFxml);
    }

    @FXML
    protected void openImageOCRBatch(ActionEvent event) {
        loadScene(CommonValues.ImageOCRBatchFxml);
    }

    @FXML
    protected void openConvolutionKernelManager(ActionEvent event) {
        loadScene(CommonValues.ConvolutionKernelManagerFxml);
    }

    @FXML
    protected void openColorPalette(ActionEvent event) {
        openStage(CommonValues.ColorPaletteManageFxml);
    }

    @FXML
    protected void openManageColors(ActionEvent event) {
        loadScene(CommonValues.ManageColorsFxml);
    }

    @FXML
    protected void openIccProfileEditor(ActionEvent event) {
        loadScene(CommonValues.IccProfileEditorFxml);
    }

    @FXML
    protected void openChromaticityDiagram(ActionEvent event) {
        loadScene(CommonValues.ChromaticityDiagramFxml);
    }

    @FXML
    protected void openChromaticAdaptationMatrix(ActionEvent event) {
        loadScene(CommonValues.ChromaticAdaptationMatrixFxml);
    }

    @FXML
    protected void openColorConversion(ActionEvent event) {
        loadScene(CommonValues.ColorConversionFxml);
    }

    @FXML
    protected void openRGBColorSpaces(ActionEvent event) {
        loadScene(CommonValues.RGBColorSpacesFxml);
    }

    @FXML
    protected void openRGB2XYZConversionMatrix(ActionEvent event) {
        loadScene(CommonValues.RGB2XYZConversionMatrixFxml);
    }

    @FXML
    protected void openRGB2RGBConversionMatrix(ActionEvent event) {
        loadScene(CommonValues.RGB2RGBConversionMatrixFxml);
    }

    @FXML
    protected void openIlluminants(ActionEvent event) {
        loadScene(CommonValues.IlluminantsFxml);
    }

    @FXML
    protected void openMatricesManage(ActionEvent event) {
        loadScene(CommonValues.MatricesManageFxml);
    }

    @FXML
    protected void openMatrixUnaryCalculation(ActionEvent event) {
        loadScene(CommonValues.MatrixUnaryCalculationFxml);
    }

    @FXML
    protected void openMatricesBinaryCalculation(ActionEvent event) {
        loadScene(CommonValues.MatricesBinaryCalculationFxml);
    }

    @FXML
    protected void openPixelsCalculator(ActionEvent event) {
        openStage(CommonValues.PixelsCalculatorFxml);
    }

    @FXML
    protected void openFilesRename(ActionEvent event) {
        loadScene(CommonValues.FilesRenameFxml);
    }

    @FXML
    protected void openDirectorySynchronize(ActionEvent event) {
        loadScene(CommonValues.DirectorySynchronizeFxml);
    }

    @FXML
    protected void openFilesArrangement(ActionEvent event) {
        loadScene(CommonValues.FilesArrangementFxml);
    }

    @FXML
    protected void openDeleteEmptyDirectories(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteEmptyDirFxml);
    }

    @FXML
    protected void openDeleteSysTempPath(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteSysTempFxml);
    }

    @FXML
    protected void openDeleteNestedDirectories(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteNestedDirFxml);
    }

    @FXML
    protected void openAlarmClock(ActionEvent event) {
        loadScene(CommonValues.AlarmClockFxml);
    }

    @FXML
    protected void openHtmlEditor(ActionEvent event) {
        loadScene(CommonValues.HtmlEditorFxml);
    }

    @FXML
    protected void openTextEditer(ActionEvent event) {
        loadScene(CommonValues.TextEditerFxml);
    }

    @FXML
    protected void openTextEncodingBatch(ActionEvent event) {
        loadScene(CommonValues.TextEncodingBatchFxml);
    }

    @FXML
    protected void openTextLineBreakBatch(ActionEvent event) {
        loadScene(CommonValues.TextLineBreakBatchFxml);
    }

    @FXML
    protected void openTextReplaceBatch(ActionEvent event) {
        loadScene(CommonValues.TextReplaceBatchFxml);
    }

    @FXML
    protected void openTextToHtml(ActionEvent event) {
        loadScene(CommonValues.TextToHtmlFxml);
    }

    @FXML
    protected void openBytesEditer(ActionEvent event) {
        loadScene(CommonValues.BytesEditerFxml);
    }

    @FXML
    protected void openFileCut(ActionEvent event) {
        loadScene(CommonValues.FileCutFxml);
    }

    @FXML
    protected void openFilesMerge(ActionEvent event) {
        loadScene(CommonValues.FilesMergeFxml);
    }

    @FXML
    protected void openFilesDelete(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteFxml);
    }

    @FXML
    protected void openFilesCopy(ActionEvent event) {
        loadScene(CommonValues.FilesCopyFxml);
    }

    @FXML
    protected void openFilesMove(ActionEvent event) {
        loadScene(CommonValues.FilesMoveFxml);
    }

    @FXML
    protected void openFilesFind(ActionEvent event) {
        loadScene(CommonValues.FilesFindFxml);
    }

    @FXML
    protected void openMarkdownEditer(ActionEvent event) {
        loadScene(CommonValues.MarkdownEditorFxml);
    }

    @FXML
    protected void openMarkdownToHtml(ActionEvent event) {
        loadScene(CommonValues.MarkdownToHtmlFxml);
    }

    @FXML
    protected void openMarkdownToText(ActionEvent event) {
        loadScene(CommonValues.MarkdownToTextFxml);
    }

    @FXML
    protected void openMarkdownToPdf(ActionEvent event) {
        loadScene(CommonValues.MarkdownToPdfFxml);
    }

    @FXML
    protected void openHtmlToMarkdown(ActionEvent event) {
        loadScene(CommonValues.HtmlToMarkdownFxml);
    }

    @FXML
    protected void openHtmlToText(ActionEvent event) {
        loadScene(CommonValues.HtmlToTextFxml);
    }

    @FXML
    protected void openHtmlToPdf(ActionEvent event) {
        loadScene(CommonValues.HtmlToPdfFxml);
    }

    @FXML
    protected void openHtmlSetCharset(ActionEvent event) {
        loadScene(CommonValues.HtmlSetCharsetFxml);
    }

    @FXML
    protected void openHtmlSetStyle(ActionEvent event) {
        loadScene(CommonValues.HtmlSetStyleFxml);
    }

    @FXML
    protected void openHtmlSnap(ActionEvent event) {
        loadScene(CommonValues.HtmlSnapFxml);
    }

    @FXML
    protected void openHtmlMergeAsHtml(ActionEvent event) {
        loadScene(CommonValues.HtmlMergeAsHtmlFxml);
    }

    @FXML
    protected void openHtmlMergeAsMarkdown(ActionEvent event) {
        loadScene(CommonValues.HtmlMergeAsMarkdownFxml);
    }

    @FXML
    protected void openHtmlMergeAsPDF(ActionEvent event) {
        loadScene(CommonValues.HtmlMergeAsPDFFxml);
    }

    @FXML
    protected void openHtmlMergeAsText(ActionEvent event) {
        loadScene(CommonValues.HtmlMergeAsTextFxml);
    }

    @FXML
    protected void openHtmlFrameset(ActionEvent event) {
        loadScene(CommonValues.HtmlFramesetFxml);
    }

    @FXML
    protected void openRecordImages(ActionEvent event) {
        loadScene(CommonValues.RecordImagesInSystemClipboardFxml);
    }

    @FXML
    protected void openWeiboSnap(ActionEvent event) {
        WeiboSnapController controller
                = (WeiboSnapController) loadScene(CommonValues.WeiboSnapFxml);
    }

    @FXML
    protected void openBarcodeCreator(ActionEvent event) {
        loadScene(CommonValues.BarcodeCreatorFxml);
    }

    @FXML
    protected void openBarcodeDecoder(ActionEvent event) {
        loadScene(CommonValues.BarcodeDecoderFxml);
    }

    @FXML
    protected void openMessageDigest(ActionEvent event) {
        loadScene(CommonValues.MessageDigestFxml);
    }

    @FXML
    protected void openFilesCompare(ActionEvent event) {
        loadScene(CommonValues.FilesCompareFxml);
    }

    @FXML
    protected void openFilesArchiveCompress(ActionEvent event) {
        loadScene(CommonValues.FilesArchiveCompressFxml);
    }

    @FXML
    protected void openFilesCompressBatch(ActionEvent event) {
        loadScene(CommonValues.FilesCompressBatchFxml);
    }

    @FXML
    protected void openFileDecompressUnarchive(ActionEvent event) {
        loadScene(CommonValues.FileDecompressUnarchiveFxml);
    }

    @FXML
    protected void openFilesDecompressUnarchiveBatch(ActionEvent event) {
        loadScene(CommonValues.FilesDecompressUnarchiveBatchFxml);
    }

    @FXML
    protected void openFilesRedundancy(ActionEvent event) {
        loadScene(CommonValues.FilesRedundancyFxml);
    }

    @FXML
    protected void openTTC2TTF(ActionEvent event) {
        loadScene(CommonValues.FileTTC2TTFFxml);
    }

    @FXML
    protected void openWebBrowser(ActionEvent event) {
        loadScene(CommonValues.WebBrowserFxml);
    }

    @FXML
    protected void openConvertUrl(ActionEvent event) {
        loadScene(CommonValues.HtmlConvertUrlFxml);
    }

    @FXML
    protected void openMediaPlayer(ActionEvent event) {
        loadScene(CommonValues.MediaPlayerFxml);
    }

    @FXML
    protected void openMediaList(ActionEvent event) {
        loadScene(CommonValues.MediaListFxml);
    }

    @FXML
    protected void openScreenRecorder(ActionEvent event) {
        loadScene(CommonValues.FFmpegScreenRecorderFxml);
    }

    @FXML
    protected void openFFmpegMergeImages(ActionEvent event) {
        loadScene(CommonValues.FFmpegMergeImagesFxml);
    }

    @FXML
    protected void openFFmpegMergeImageFiles(ActionEvent event) {
        loadScene(CommonValues.FFmpegMergeImageFilesFxml);
    }

    @FXML
    protected void openFFmpegInformation(ActionEvent event) {
        loadScene(CommonValues.FFmpegInformationFxml);
    }

    @FXML
    protected void openFFmpegProbeMediaInformation(ActionEvent event) {
        loadScene(CommonValues.FFmpegProbeMediaInformationFxml);
    }

    @FXML
    protected void openFFmpegConvertMediaFiles(ActionEvent event) {
        loadScene(CommonValues.FFmpegConvertMediaFilesFxml);
    }

    @FXML
    protected void openFFmpegConvertMediaStreams(ActionEvent event) {
        loadScene(CommonValues.FFmpegConvertMediaStreamsFxml);
    }

    @FXML
    protected void openSecurityCertificates(ActionEvent event) {
        loadScene(CommonValues.SecurityCertificatesFxml);
    }

    @FXML
    protected void restoreCheckingSSLCertifications(ActionEvent event) {
        restoreCheckingSSL();
    }

    @FXML
    protected void downloadManage(ActionEvent event) {
        loadScene(CommonValues.DownloadManageFxml);
    }

    @FXML
    protected void downloadFirstLevelLinks(ActionEvent event) {
        loadScene(CommonValues.DownloadFirstLevelLinksFxml);
    }

    @FXML
    protected void openGameElimniation(ActionEvent event) {
        loadScene(CommonValues.GameElimniationFxml);
    }

    @FXML
    protected void openGameMine(ActionEvent event) {
        loadScene(CommonValues.GameMineFxml);
    }

    @FXML
    protected void openDataset(ActionEvent event) {
        loadScene(CommonValues.DatasetFxml);
    }

    @FXML
    protected void openLocationData(ActionEvent event) {
        loadScene(CommonValues.LocationDataFxml);
    }

    @FXML
    protected void openGeographyCode(ActionEvent event) {
        loadScene(CommonValues.GeographyCodeFxml);
    }

    @FXML
    protected void openLocationsDataInMap(ActionEvent event) {
        loadScene(CommonValues.LocationsDataInMapFxml);
    }

    @FXML
    protected void openLocationInMap(ActionEvent event) {
        loadScene(CommonValues.LocationInMapFxml);
    }

    @FXML
    protected void openLocationTools(ActionEvent event) {
        loadScene(CommonValues.LocationToolsFxml);
    }

    @FXML
    protected void openEpidemicReports(ActionEvent event) {
        loadScene(CommonValues.EpidemicReportsFxml);
    }

    @FXML
    protected void openDataClipboard(ActionEvent event) {
        loadScene(CommonValues.DataClipboardFxml);
    }

    @FXML
    protected void openDataCsv(ActionEvent event) {
        loadScene(CommonValues.DataFileCSVFxml);
    }

    @FXML
    protected void openDataExcel(ActionEvent event) {
        loadScene(CommonValues.DataFileExcelFxml);
    }

    @FXML
    protected void openExcelConvert(ActionEvent event) {
        loadScene(CommonValues.DataConvertExcelFxml);
    }

    @FXML
    protected void openCsvConvert(ActionEvent event) {
        loadScene(CommonValues.DataConvertCsvFxml);
    }

    @FXML
    protected void messageAuthor(ActionEvent event) {
        openStage(CommonValues.MessageAuthorFxml);
    }

    @FXML
    protected void showAbout(ActionEvent event) {
        openStage(CommonValues.AboutFxml);
    }

    @FXML
    protected void settingsAction(ActionEvent event) {
        BaseController c = openStage(CommonValues.SettingsFxml);
        c.setParentController(parentController);
        c.setParentFxml(parentFxml);
    }

    @Override
    public Stage getMyStage() {
        if (myStage == null) {
            if (mainMenuPane != null && mainMenuPane.getScene() != null) {
                myStage = (Stage) mainMenuPane.getScene().getWindow();
            }
        }
        return myStage;
    }

    @FXML
    public void documents(ActionEvent event) {
        openStage(CommonValues.DocumentsFxml);
    }

    // This is for developement to generate Icons automatically in different color style
    // Appears when "Developement mode" is on
    @FXML
    public void makeIcons() {
        synchronized (this) {
            if (iconTask != null && !iconTask.isQuit()) {
                return;
            }
            iconTask = DevTools.makeIconsTask(parentController);
            if (iconTask == null) {
                return;
            }
            parentController.openHandlingStage(iconTask, Modality.WINDOW_MODAL);
            iconTask.setSelf(iconTask);
            Thread thread = new Thread(iconTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
