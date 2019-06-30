package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import com.sun.management.OperatingSystemMXBean;
import java.io.File;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.AppVaribles.getUserConfigValue;
import static mara.mybox.value.AppVaribles.logger;

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
    private RadioMenuItem chineseMenuItem, englishMenuItem,
            pdf500mbRadio, pdf1gbRadio, pdf2gbRadio, pdfUnlimitRadio,
            font12MenuItem, font15MenuItem, font17MenuItem,
            defaultColorMenuItem, pinkMenuItem, redMenuItem, blueMenuItem, lightBlueMenuItem, orangeMenuItem;
    @FXML
    private CheckMenuItem stopAlarmCheck, monitorMemroyCheck, monitorCpuCheck,
            newWindowCheck, replaceWhiteMenu, restoreStagesSizeCheck, popRecentCheck, controlTextCheck;
    @FXML
    private Menu settingsMenu, recentMenu;
    @FXML
    private MenuItem closeOtherMenu;

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

            checkRecent();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void checkSettings() {
        checkLanguage();
        checkPdfMem();
        checkFontSize();
        stopAlarmCheck.setSelected(AppVaribles.getUserConfigBoolean("StopAlarmsWhenExit"));
        monitorMemroyCheck.setSelected(AppVaribles.getUserConfigBoolean("MonitorMemroy", false));
        monitorCpuCheck.setSelected(AppVaribles.getUserConfigBoolean("MonitorCpu", false));
        controlTextCheck.setSelected(AppVaribles.getUserConfigBoolean("ControlDisplayText", false));
        replaceWhiteMenu.setSelected(AppVaribles.isAlphaAsWhite());
        newWindowCheck.setSelected(AppVaribles.openStageInNewWindow);
        restoreStagesSizeCheck.setSelected(AppVaribles.restoreStagesSize);
        popRecentCheck.setSelected(AppVaribles.fileRecentNumber > 0);
        checkMemroyMonitor();
        checkCpuMonitor();
        checkControlColor();

    }

    private void checkRecent() {
        recentMenu.getItems().clear();
        List<VisitHistory> his = VisitHistory.getRecentMenu();
        if (his == null || his.isEmpty()) {
            return;
        }
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            final String fxml = h.getDataMore();
            MenuItem menu = new MenuItem(getMessage(fname));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    loadScene(fxml);
                }
            });
            recentMenu.getItems().add(menu);
        }
    }

    protected void checkLanguage() {
        if (AppVaribles.currentBundle == CommonValues.BundleZhCN) {
            chineseMenuItem.setSelected(true);
        } else {
            englishMenuItem.setSelected(true);
        }
    }

    protected void checkFontSize() {
        switch (AppVaribles.sceneFontSize) {
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

    protected void checkPdfMem() {
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdf1gbRadio.setSelected(true);
                break;
            case "2GB":
                pdf2gbRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdf500mbRadio.setSelected(true);
        }
    }

    protected void checkControlColor() {
        switch (AppVaribles.ControlColor) {
            case Default:
                defaultColorMenuItem.setSelected(true);
                break;
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
            default:
                defaultColorMenuItem.setSelected(true);
                break;
        }
    }

    @FXML
    protected void showHome(ActionEvent event) {
        openStage(CommonValues.MyboxFxml);
    }

    @FXML
    protected void resetWindows(ActionEvent event) {
        AppVaribles.resetWindows();
        refresh();
    }

    @FXML
    protected void closeOtherWindows(ActionEvent event) {
        final List<BaseController> controllers = new ArrayList();
        controllers.addAll(AppVaribles.openedStages.values());
        for (BaseController controller : controllers) {
            if (controller != parentController) {
                controller.closeStage();
            }
        }
    }

    @FXML
    protected void jvmProperties(ActionEvent event) {
        openStage(CommonValues.JvmPropertiesFxml);
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
                                    + " " + AppVaribles.getMessage("PhysicalMemory") + ":" + physicalTotal + "MB"
                                    + " " + AppVaribles.getMessage("Used") + ":"
                                    + physicalUse + "MB (" + FloatTools.roundFloat2(physicalUse * 100.0f / physicalTotal) + "%)";
                            sysMemLabel.setText(sysInfo);
                            sysMemBar.setProgress(physicalUse * 1.0f / physicalTotal);

                            long freeMemory = r.freeMemory() / mb;
                            long totalMemory = r.totalMemory() / mb;
                            long maxMemory = r.maxMemory() / mb;
                            long usedMemory = totalMemory - freeMemory;
                            String myboxInfo = "MyBox"
                                    //                    + "  " + AppVaribles.getMessage("AvailableProcessors") + ":" + availableProcessors
                                    + " " + AppVaribles.getMessage("AvaliableMemory") + ":" + maxMemory + "MB"
                                    + " " + AppVaribles.getMessage("Required") + ":"
                                    + totalMemory + "MB(" + FloatTools.roundFloat2(totalMemory * 100.0f / maxMemory) + "%)"
                                    + " " + AppVaribles.getMessage("Used") + ":"
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
        AppVaribles.setUserConfigValue("MonitorMemroy", v);
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
                                    + " " + AppVaribles.getMessage("SystemLoadAverage") + ":" + s + "s"
                                    + " " + AppVaribles.getMessage("SystemCpuUsage") + ":"
                                    + FloatTools.roundFloat2(load * 100) + "%";
                            sysCpuLabel.setText(sysInfo);
                            sysCpuBar.setProgress(load);

                            load = (float) osmxb.getProcessCpuLoad();
                            s = (long) (osmxb.getProcessCpuTime() / 1000000000);
                            String myboxInfo = "MyBox"
                                    + " " + AppVaribles.getMessage("RecentCpuTime") + ":" + s + "s"
                                    + " " + AppVaribles.getMessage("RecentCpuUsage") + ":"
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
        AppVaribles.setUserConfigValue("MonitorCpu", v);
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
        AppVaribles.setLanguage("zh");
        refresh();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVaribles.setLanguage("en");
        refresh();
    }

    @FXML
    protected void setFont12(ActionEvent event) {
        AppVaribles.setSceneFontSize(12);
        refresh();
    }

    @FXML
    protected void setFont15(ActionEvent event) {
        AppVaribles.setSceneFontSize(15);
        refresh();
    }

    @FXML
    protected void setFont17(ActionEvent event) {
        AppVaribles.setSceneFontSize(17);
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
        AppVaribles.controlDisplayText = controlTextCheck.isSelected();
        AppVaribles.setUserConfigValue("ControlDisplayText", controlTextCheck.isSelected());
        refresh();
    }

    @FXML
    @Override
    public BaseController refresh() {
        return parentController.refresh();
    }

    @FXML
    protected void setStopAlarm(ActionEvent event) {
        AppVaribles.setUserConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
    }

    @FXML
    protected void newWindowAction() {
        AppVaribles.setOpenStageInNewWindow(newWindowCheck.isSelected());
    }

    @FXML
    protected void restoreStagesSizeAction() {
        AppVaribles.setRestoreStagesSize(restoreStagesSizeCheck.isSelected());
    }

    @FXML
    protected void popRecentAction() {
        if (popRecentCheck.isSelected()) {
            AppVaribles.fileRecentNumber = 15;
        } else {
            AppVaribles.fileRecentNumber = 0;
        }
        AppVaribles.setUserConfigInt("FileRecentNumber", AppVaribles.fileRecentNumber);

    }

    @FXML
    protected void replaceWhiteAction(ActionEvent event) {
        AppVaribles.setUserConfigValue("AlphaAsWhite", replaceWhiteMenu.isSelected());
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
            AppVaribles.setUserConfigValue("InterfaceStyle", style);
            if (parentController != null) {
                parentController.setInterfaceStyle(style);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void clearSettings(ActionEvent event) {
        super.clearSettings(event);
        String f = parentController.myFxml;
        if (f.contains("ImageManufacture") && !f.contains("ImageManufactureBatch")) {
            f = CommonValues.ImageManufactureFileFxml;
        }
        BaseController c = loadScene(f);
        c.getMyStage().setTitle(parentController.getMyStage().getTitle());
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    private void exit(ActionEvent event) {
        FxmlStage.appExit();
    }

    @Override
    public BaseController loadScene(String newFxml) {
        try {
            if (AppVaribles.openStageInNewWindow) {
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
    private void openPDFAttributes(ActionEvent event) {
        loadScene(CommonValues.PdfAttributesFxml);
    }

    @FXML
    private void openPdfConvertImages(ActionEvent event) {
        loadScene(CommonValues.PdfConvertImagesFxml);
    }

    @FXML
    private void openPdfConvertImagesBatch(ActionEvent event) {
        loadScene(CommonValues.PdfConvertImagesBatchFxml);
    }

    @FXML
    private void openImagesCombinePdf(ActionEvent event) {
        loadScene(CommonValues.ImagesCombinePdfFxml);
    }

    @FXML
    private void openPdfExtractImages(ActionEvent event) {
        loadScene(CommonValues.PdfExtractImagesFxml);
    }

    @FXML
    private void openPdfExtractTexts(ActionEvent event) {
        loadScene(CommonValues.PdfExtractTextsFxml);
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
    private void openSplitPdf(ActionEvent event) {
        loadScene(CommonValues.PdfSplitFxml);
    }

    @FXML
    private void openCompressPdfImages(ActionEvent event) {
        loadScene(CommonValues.PdfCompressImagesFxml);
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
    private void openImageConverter(ActionEvent event) {
        loadScene(CommonValues.ImageConverterFxml);
    }

    @FXML
    private void openImageConverterBatch(ActionEvent event) {
        loadScene(CommonValues.ImageConverterBatchFxml);
    }

    @FXML
    private void openImageManufacture(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureFileFxml);
    }

    @FXML
    private void openImageManufactureSize(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureSizeFxml);
    }

    @FXML
    private void openImageManufactureCrop(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureCropFxml);

    }

    @FXML
    private void openImageManufactureColor(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureColorFxml);
    }

    @FXML
    private void openImageManufactureEffects(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureEffectsFxml);
    }

    @FXML
    private void openImageManufactureText(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureTextFxml);
    }

    @FXML
    private void openImageManufactureDoodle(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureDoodleFxml);

    }

    @FXML
    private void openImageManufactureMosaic(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureMosaicFxml);

    }

    @FXML
    private void openImageManufactureArc(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureArcFxml);
    }

    @FXML
    private void openImageManufactureShadow(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureShadowFxml);
    }

    @FXML
    private void openImageManufactureTransform(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureTransformFxml);

    }

    @FXML
    private void openImageManufactureMargins(ActionEvent event) {
        loadScene(CommonValues.ImageManufactureMarginsFxml);
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
    private void openConvolutionKernelManager(ActionEvent event) {
        loadScene(CommonValues.ConvolutionKernelManagerFxml);
    }

    @FXML
    private void openColorPalette(ActionEvent event) {
        openStage(CommonValues.ColorPaletteFxml);
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
    private void openFileMerge(ActionEvent event) {
        loadScene(CommonValues.FileMergeFxml);
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
    private void showAbout(ActionEvent event) {
        openStage(CommonValues.AboutFxml);
    }

    @FXML
    private void settingsAction(ActionEvent event) {
        BaseController c = openStage(CommonValues.SettingsFxml);
        c.parentController = parentController;
        c.parentFxml = parentFxml;
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
    public void developerGuideExternal(ActionEvent event) {
        try {
            File help = checkHelps();
            if (help != null) {
                if (!browseURI(help.toURI())) {
                    parentController.popError(getMessage("DesktopNotSupportBrowse"));
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void userGuideOverview(ActionEvent event) {
        try {
            String link = "https://github.com/Mararsh/MyBox/releases/download/v"
                    + CommonValues.AppDocVersion + "/MyBox-UserGuide-" + CommonValues.AppDocVersion
                    + "-Overview-" + AppVaribles.getLanguage() + ".pdf";
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
                    + "-PdfTools-" + AppVaribles.getLanguage() + ".pdf";
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
                    + "-ImageTools-" + AppVaribles.getLanguage() + ".pdf";
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
                    + "-DesktopTools-" + AppVaribles.getLanguage() + ".pdf";
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
                    + "-NetworkTools-" + AppVaribles.getLanguage() + ".pdf";
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
                    + "-DeveloperGuide-" + AppVaribles.getLanguage() + ".pdf";
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
