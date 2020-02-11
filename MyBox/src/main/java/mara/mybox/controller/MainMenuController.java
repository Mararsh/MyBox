package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.net.URI;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
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

    @FXML
    private Pane mainMenuPane;
    @FXML
    private ToggleGroup langGroup;
    @FXML
    private RadioMenuItem chineseMenuItem, englishMenuItem,
            font12MenuItem, font15MenuItem, font17MenuItem,
            normalIconMenuItem, bigIconMenuItem, smallIconMenuItem,
            pinkMenuItem, redMenuItem, blueMenuItem, lightBlueMenuItem, orangeMenuItem;
    @FXML
    private CheckMenuItem monitorMemroyCheck, monitorCpuCheck,
            newWindowCheck, restoreStagesSizeCheck, popRecentCheck, controlTextCheck;
    @FXML
    private Menu settingsMenu, recentMenu;
    @FXML
    private MenuItem manageLanguagesMenuItem;

    @Override
    public void initializeNext() {
        try {
            settingsMenu.setOnShowing(new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    checkSettings();
                }
            });
            checkSettings();

            recentMenu.setOnShowing(new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    recentMenu.getItems().clear();
                    recentMenu.getItems().addAll(getRecentMenu());
                }
            });

//            menuBar.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent e) {
//                    logger.debug("mouse:" + e.getButton() + "  " + e.getEventType());
//
////                    e.consume();
//                }
//            });
//            String os = System.getProperty("os.name").toLowerCase();
//            if (!os.contains("windows")) {
//                imageMenu.getItems().removeAll(imageOcrMenu, imageOcrBatchMenu);
//                pdfMenu.getItems().removeAll(pdfOcrBatchMenu);
//            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void checkSettings() {
        checkLanguage();
        checkFontSize();
        checkIconSize();
        monitorMemroyCheck.setSelected(AppVariables.getUserConfigBoolean("MonitorMemroy", false));
        monitorCpuCheck.setSelected(AppVariables.getUserConfigBoolean("MonitorCpu", false));
        controlTextCheck.setSelected(AppVariables.getUserConfigBoolean("ControlDisplayText", false));
        newWindowCheck.setSelected(AppVariables.openStageInNewWindow);
        restoreStagesSizeCheck.setSelected(AppVariables.restoreStagesSize);
        popRecentCheck.setSelected(AppVariables.fileRecentNumber > 0);
        checkControlColor();

    }

    protected void checkLanguage() {
        List<MenuItem> items = new ArrayList();
        items.addAll(settingsMenu.getItems());
        int pos1 = items.indexOf(englishMenuItem);
        int pos2 = items.indexOf(manageLanguagesMenuItem);
        for (int i = pos2 - 1; i > pos1; --i) {
            items.remove(i);
        }
        List<String> languages = ConfigTools.languages();
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
        } else if (AppVariables.currentBundle == CommonValues.BundleEnUS) {
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
    protected void download(ActionEvent event) {
        loadScene(CommonValues.DownloadFxml);
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
    protected void Shortcuts(ActionEvent event) {
        openStage(CommonValues.ShortcutsFxml);
    }

    private void makeMemoryMonitorBox() {
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

    private void startMemoryMonitorTimer() {
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
            logger.error(e.toString());
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

    private void makeCpuMonitorBox() {
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

    private void startCpuMonitorTimer() {
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
            logger.error(e.toString());
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
            logger.error(e.toString());
        }
    }

    @FXML
    public void clearSettings(ActionEvent event) {
        if (!super.clearSettings()) {
            return;
        }
        String f = parentController.getMyFxml();
        BaseController c = loadScene(f);
        c.getMyStage().setTitle(parentController.getMyStage().getTitle());
        popSuccessful();
    }

    @FXML
    private void exit(ActionEvent event) {
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
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public BaseController openStage(String newFxml) {
        return parentController.openStage(newFxml);
    }

    @FXML
    private void openPdfView(ActionEvent event) {
        loadScene(CommonValues.PdfViewFxml);
    }

    @FXML
    private void openPdfHtmlViewer(ActionEvent event) {
        loadScene(CommonValues.PdfHtmlViewerFxml);
    }

    @FXML
    private void openPDFAttributes(ActionEvent event) {
        loadScene(CommonValues.PdfAttributesFxml);
    }

    @FXML
    private void openPDFAttributesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfAttributesBatchFxml);
    }

    @FXML
    private void openPdfConvertImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfConvertImagesBatchFxml);
    }

    @FXML
    private void openPdfConvertHtmlsBatch(ActionEvent event) {
        loadScene(CommonValues.PdfConvertHtmlsBatchFxml);
    }

    @FXML
    private void openImagesCombinePdf(ActionEvent event) {
        loadScene(CommonValues.ImagesCombinePdfFxml);
    }

    @FXML
    private void openPdfExtractTextsBatch(ActionEvent event) {
        loadScene(CommonValues.PdfExtractTextsBatchFxml);
    }

    @FXML
    private void openPdfExtractImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfExtractImagesBatchFxml);
    }

    @FXML
    private void openMergePdf(ActionEvent event) {
        loadScene(CommonValues.PdfMergeFxml);
    }

    @FXML
    private void openPdfSplitBatch(ActionEvent event) {
        loadScene(CommonValues.PdfSplitBatchFxml);
    }

    @FXML
    private void openPdfOCRBatch(ActionEvent event) {
        loadScene(CommonValues.PdfOCRBatchFxml);
    }

    @FXML
    private void openCompressPdfImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfCompressImagesBatchFxml);
    }

    @FXML
    private void openImageViewer(ActionEvent event) {
        loadScene(CommonValues.ImageViewerFxml);
    }

    @FXML
    private void openImagesBrowser(ActionEvent event) {
        loadScene(CommonValues.ImagesBrowserFxml);
    }

    @FXML
    private void openImageData(ActionEvent event) {
        loadScene(CommonValues.ImageAnalyseFxml);
    }

    @FXML
    private void openImageConverterBatch(ActionEvent event) {
        loadScene(CommonValues.ImageConverterBatchFxml);
    }

    @FXML
    private void openImageManufacture(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureFxml);
    }

    @FXML
    private void openImageManufactureBatchSize(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchSizeFxml);
    }

    @FXML
    private void openImageManufactureBatchCrop(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchCropFxml);
    }

    @FXML
    private void openImageManufactureBatchColor(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchColorFxml);
    }

    @FXML
    private void openImageManufactureBatchEffects(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchEffectsFxml);
    }

    @FXML
    private void openImageManufactureBatchEnhancement(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchEnhancementFxml);
    }

    @FXML
    private void openImageManufactureBatchReplaceColor(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchReplaceColorFxml);
    }

    @FXML
    private void openImageManufactureBatchText(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchTextFxml);
    }

    @FXML
    private void openImageManufactureBatchArc(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchArcFxml);
    }

    @FXML
    private void openImageManufactureBatchShadow(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchShadowFxml);
    }

    @FXML
    private void openImageManufactureBatchTransform(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchTransformFxml);
    }

    @FXML
    private void openImageManufactureBatchMargins(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureBatchMarginsFxml);
    }

    @FXML
    private void openImageSplit(ActionEvent event) {
        loadScene(CommonValues.ImageSplitFxml);
    }

    @FXML
    private void openImageSample(ActionEvent event) {
        loadScene(CommonValues.ImageSampleFxml);
    }

    @FXML
    private void openImagesCombine(ActionEvent event) {
        loadScene(CommonValues.ImagesCombineFxml);
    }

    @FXML
    private void openImageGifViewer(ActionEvent event) {
        loadScene(CommonValues.ImageGifViewerFxml);
    }

    @FXML
    private void openImageGifEditer(ActionEvent event) {
        loadScene(CommonValues.ImageGifEditerFxml);
    }

    @FXML
    private void openImageTiffEditer(ActionEvent event) {
        loadScene(CommonValues.ImageTiffEditerFxml);
    }

    @FXML
    private void openImageFramesViewer(ActionEvent event) {
        loadScene(CommonValues.ImageFramesViewerFxml);
    }

    @FXML
    private void openImagesBlend(ActionEvent event) {
        loadScene(CommonValues.ImagesBlendFxml);
    }

    @FXML
    private void openImageStatistic(ActionEvent event) {
        loadScene(CommonValues.ImageStatisticFxml);
    }

    @FXML
    private void openImageAlphaExtract(ActionEvent event) {
        loadScene(CommonValues.ImageAlphaExtractBatchFxml);
    }

    @FXML
    private void openImageAlphaAdd(ActionEvent event) {
        loadScene(CommonValues.ImageAlphaAddBatchFxml);
    }

    @FXML
    private void openImageOCR(ActionEvent event) {
        loadScene(CommonValues.ImageOCRFxml);
    }

    @FXML
    private void openImageOCRBatch(ActionEvent event) {
        loadScene(CommonValues.ImageOCRBatchFxml);
    }

    @FXML
    private void openConvolutionKernelManager(ActionEvent event) {
        loadScene(CommonValues.ConvolutionKernelManagerFxml);
    }

    @FXML
    private void openColorPalette(ActionEvent event) {
        openStage(CommonValues.ColorPaletteFxml);
    }

    @FXML
    private void openManageColors(ActionEvent event) {
        loadScene(CommonValues.ManageColorsFxml);
    }

    @FXML
    private void openIccProfileEditor(ActionEvent event) {
        loadScene(CommonValues.IccProfileEditorFxml);
    }

    @FXML
    private void openChromaticityDiagram(ActionEvent event) {
        loadScene(CommonValues.ChromaticityDiagramFxml);
    }

    @FXML
    private void openChromaticAdaptationMatrix(ActionEvent event) {
        loadScene(CommonValues.ChromaticAdaptationMatrixFxml);
    }

    @FXML
    private void openColorConversion(ActionEvent event) {
        loadScene(CommonValues.ColorConversionFxml);
    }

    @FXML
    private void openRGBColorSpaces(ActionEvent event) {
        loadScene(CommonValues.RGBColorSpacesFxml);
    }

    @FXML
    private void openRGB2XYZConversionMatrix(ActionEvent event) {
        loadScene(CommonValues.RGB2XYZConversionMatrixFxml);
    }

    @FXML
    private void openRGB2RGBConversionMatrix(ActionEvent event) {
        loadScene(CommonValues.RGB2RGBConversionMatrixFxml);
    }

    @FXML
    private void openIlluminants(ActionEvent event) {
        loadScene(CommonValues.IlluminantsFxml);
    }

    @FXML
    private void openMatricesCalculation(ActionEvent event) {
        loadScene(CommonValues.MatricesCalculationFxml);
    }

    @FXML
    private void openPixelsCalculator(ActionEvent event) {
        openStage(CommonValues.PixelsCalculatorFxml);
    }

    @FXML
    private void openFilesRename(ActionEvent event) {
        loadScene(CommonValues.FilesRenameFxml);
    }

    @FXML
    private void openDirectorySynchronize(ActionEvent event) {
        loadScene(CommonValues.DirectorySynchronizeFxml);
    }

    @FXML
    private void openFilesArrangement(ActionEvent event) {
        loadScene(CommonValues.FilesArrangementFxml);
    }

    @FXML
    private void openDeleteEmptyDirectories(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteEmptyDirFxml);
    }

    @FXML
    private void openDeleteNestedDirectories(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteNestedDirFxml);
    }

    @FXML
    private void openAlarmClock(ActionEvent event) {
        loadScene(CommonValues.AlarmClockFxml);
    }

    @FXML
    private void openHtmlEditor(ActionEvent event) {
        loadScene(CommonValues.HtmlEditorFxml);
    }

    @FXML
    private void openTextEditer(ActionEvent event) {
        loadScene(CommonValues.TextEditerFxml);
    }

    @FXML
    private void openTextEncodingBatch(ActionEvent event) {
        loadScene(CommonValues.TextEncodingBatchFxml);
    }

    @FXML
    private void openTextLineBreakBatch(ActionEvent event) {
        loadScene(CommonValues.TextLineBreakBatchFxml);
    }

    @FXML
    private void openBytesEditer(ActionEvent event) {
        loadScene(CommonValues.BytesEditerFxml);
    }

    @FXML
    private void openFileCut(ActionEvent event) {
        loadScene(CommonValues.FileCutFxml);
    }

    @FXML
    private void openFilesMerge(ActionEvent event) {
        loadScene(CommonValues.FilesMergeFxml);
    }

    @FXML
    private void openFilesDelete(ActionEvent event) {
        loadScene(CommonValues.FilesDeleteFxml);
    }

    @FXML
    private void openFilesCopy(ActionEvent event) {
        loadScene(CommonValues.FilesCopyFxml);
    }

    @FXML
    private void openFilesMove(ActionEvent event) {
        loadScene(CommonValues.FilesMoveFxml);
    }

    @FXML
    private void openFilesFind(ActionEvent event) {
        loadScene(CommonValues.FilesFindFxml);
    }

    @FXML
    private void openMarkdownEditer(ActionEvent event) {
        loadScene(CommonValues.MarkdownEditorFxml);
    }

    @FXML
    private void openMarkdownToHtml(ActionEvent event) {
        loadScene(CommonValues.MarkdownToHtmlFxml);
    }

    @FXML
    private void openHtmlToMarkdown(ActionEvent event) {
        loadScene(CommonValues.HtmlToMarkdownFxml);
    }

    @FXML
    private void openRecordImages(ActionEvent event) {
        loadScene(CommonValues.RecordImagesInSystemClipboardFxml);
    }

    @FXML
    private void openWeiboSnap(ActionEvent event) {
        WeiboSnapController controller
                = (WeiboSnapController) loadScene(CommonValues.WeiboSnapFxml);
    }

    @FXML
    private void openBarcodeCreator(ActionEvent event) {
        loadScene(CommonValues.BarcodeCreatorFxml);
    }

    @FXML
    private void openBarcodeDecoder(ActionEvent event) {
        loadScene(CommonValues.BarcodeDecoderFxml);
    }

    @FXML
    private void openMessageDigest(ActionEvent event) {
        loadScene(CommonValues.MessageDigestFxml);
    }

    @FXML
    private void openFilesCompare(ActionEvent event) {
        loadScene(CommonValues.FilesCompareFxml);
    }

    @FXML
    private void openFilesArchiveCompress(ActionEvent event) {
        loadScene(CommonValues.FilesArchiveCompressFxml);
    }

    @FXML
    private void openFilesCompressBatch(ActionEvent event) {
        loadScene(CommonValues.FilesCompressBatchFxml);
    }

    @FXML
    private void openFileDecompressUnarchive(ActionEvent event) {
        loadScene(CommonValues.FileDecompressUnarchiveFxml);
    }

    @FXML
    private void openFilesDecompressUnarchiveBatch(ActionEvent event) {
        loadScene(CommonValues.FilesDecompressUnarchiveBatchFxml);
    }

    @FXML
    private void openFilesRedundancy(ActionEvent event) {
        loadScene(CommonValues.FilesRedundancyFxml);
    }

    @FXML
    private void openWebBrowser(ActionEvent event) {
        loadScene(CommonValues.WebBrowserFxml);
    }

    @FXML
    private void openMediaPlayer(ActionEvent event) {
        loadScene(CommonValues.MediaPlayerFxml);
    }

    @FXML
    private void openMediaList(ActionEvent event) {
        loadScene(CommonValues.MediaListFxml);
    }

    @FXML
    private void openFFmpegMergeImages(ActionEvent event) {
        loadScene(CommonValues.FFmpegMergeImagesFxml);
    }

    @FXML
    private void openFFmpegMergeImageFiles(ActionEvent event) {
        loadScene(CommonValues.FFmpegMergeImageFilesFxml);
    }

    @FXML
    private void openFFmpegInformation(ActionEvent event) {
        loadScene(CommonValues.FFmpegInformationFxml);
    }

    @FXML
    private void openFFmpegProbeMediaInformation(ActionEvent event) {
        loadScene(CommonValues.FFmpegProbeMediaInformationFxml);
    }

    @FXML
    private void openFFmpegConvertMediaFiles(ActionEvent event) {
        loadScene(CommonValues.FFmpegConvertMediaFilesFxml);
    }

    @FXML
    private void openFFmpegConvertMediaStreams(ActionEvent event) {
        loadScene(CommonValues.FFmpegConvertMediaStreamsFxml);
    }

    @FXML
    private void openSecurityCertificates(ActionEvent event) {
        loadScene(CommonValues.SecurityCertificatesFxml);
    }

    @FXML
    private void openGameElimniation(ActionEvent event) {
        loadScene(CommonValues.GameElimniationFxml);
    }

    @FXML
    private void openLocationsData(ActionEvent event) {
        loadScene(CommonValues.LocationsDataFxml);
    }

    @FXML
    private void openDataGeographyCode(ActionEvent event) {
        loadScene(CommonValues.GeographyCodeFxml);
    }

    @FXML
    private void openLocationsDataInMap(ActionEvent event) {
        loadScene(CommonValues.LocationsDataInMapFxml);
    }

    @FXML
    private void openLocationInMap(ActionEvent event) {
        loadScene(CommonValues.LocationInMapFxml);
    }

    @FXML
    private void openEpidemicReports(ActionEvent event) {
        loadScene(CommonValues.EpidemicReportsFxml);
    }

    @FXML
    private void showAbout(ActionEvent event) {
        openStage(CommonValues.AboutFxml);
    }

    @FXML
    private void settingsAction(ActionEvent event) {
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
//        try {
//            String link = "https://github.com/Mararsh/MyBox/releases/download/v5.8/MyBox-DevGuide-2.0"
//                    + "-" + AppVariables.getLanguage() + ".pdf";
//            browseURI(new URI(link));
//        } catch (Exception e) {
//            logger.error(e.toString());
//        }

    }

    @FXML
    public void userGuideOverview(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-Overview-" + AppVariables.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void userGuidePdfTools(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-PdfTools-" + AppVariables.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void userGuideImageTools(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-ImageTools-" + AppVariables.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void userGuideDesktopTools(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-DesktopTools-" + AppVariables.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void userGuideNetworkTools(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-NetworkTools-" + AppVariables.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void userGuideDeveloperGuide(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-DeveloperGuide-" + AppVariables.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
