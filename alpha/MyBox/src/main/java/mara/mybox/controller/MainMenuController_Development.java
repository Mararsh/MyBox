package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.tools.Data2DExampleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Development extends MainMenuController_Settings {

    protected HBox memoryBox, cpuBox;
    protected Timer memoryMonitorTimer, cpuMonitorTimer;
    protected final int memoryMonitorInterval = 1000, cpuMonitorInterval = 1000;
    protected Runtime r;
    protected OperatingSystemMXBean osmxb;
    protected Label sysMemLabel, myboxMemLabel, sysCpuLabel, myboxCpuLabel;
    protected ProgressBar sysMemBar, myboxMemBar, sysCpuBar, myboxCpuBar;
    protected long mb;

    @FXML
    protected Menu devMenu;
    @FXML
    protected CheckMenuItem monitorMemroyCheck, monitorCpuCheck, detailedDebugCheck, popErrorCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            devMenu.setOnShowing((Event e) -> {
                checkDev();
            });
            checkDev();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkDev() {
        monitorMemroyCheck.setSelected(UserConfig.getBoolean("MonitorMemroy", false));
        monitorCpuCheck.setSelected(UserConfig.getBoolean("MonitorCpu", false));
        popErrorCheck.setSelected(AppVariables.popErrorLogs);
        detailedDebugCheck.setSelected(AppVariables.detailedDebugLogs);
        checkMemroyMonitor();
        checkCpuMonitor();
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
                            long physicalFree = osmxb.getFreeMemorySize() / mb;
                            long physicalTotal = osmxb.getTotalMemorySize() / mb;
                            long physicalUse = physicalTotal - physicalFree;
                            String sysInfo = System.getProperty("os.name")
                                    + " " + Languages.message("PhysicalMemory") + ":" + physicalTotal + "MB"
                                    + " " + Languages.message("Used") + ":"
                                    + physicalUse + "MB (" + FloatTools.percentage(physicalUse, physicalTotal) + "%)";
                            sysMemLabel.setText(sysInfo);
                            sysMemBar.setProgress(physicalUse * 1.0f / physicalTotal);

                            long freeMemory = r.freeMemory() / mb;
                            long totalMemory = r.totalMemory() / mb;
                            long maxMemory = r.maxMemory() / mb;
                            long usedMemory = totalMemory - freeMemory;
                            String myboxInfo = "MyBox"
                                    //                    + "  " + AppVariables.getMessage("AvailableProcessors") + ":" + availableProcessors
                                    + " " + Languages.message("AvaliableMemory") + ":" + maxMemory + "MB"
                                    + " " + Languages.message("Required") + ":"
                                    + totalMemory + "MB(" + FloatTools.percentage(totalMemory, maxMemory) + "%)"
                                    + " " + Languages.message("Used") + ":"
                                    + usedMemory + "MB(" + FloatTools.percentage(usedMemory, maxMemory) + "%)";
                            myboxMemLabel.setText(myboxInfo);
                            myboxMemBar.setProgress(usedMemory * 1.0f / maxMemory);
                        }
                    });
                }
            }, 0, memoryMonitorInterval);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        UserConfig.setBoolean("MonitorMemroy", v);
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

                            float load = (float) osmxb.getCpuLoad();
                            long s = (long) (osmxb.getSystemLoadAverage() / 1000000000);
                            String sysInfo = System.getProperty("os.name")
                                    + " " + Languages.message("SystemLoadAverage") + ":" + s + "s"
                                    + " " + Languages.message("SystemCpuUsage") + ":"
                                    + FloatTools.roundFloat2(load * 100) + "%";
                            sysCpuLabel.setText(sysInfo);
                            sysCpuBar.setProgress(load);

                            load = (float) osmxb.getProcessCpuLoad();
                            s = osmxb.getProcessCpuTime() / 1000000000;
                            String myboxInfo = "MyBox"
                                    + " " + Languages.message("RecentCpuTime") + ":" + s + "s"
                                    + " " + Languages.message("RecentCpuUsage") + ":"
                                    + FloatTools.roundFloat2(load * 100) + "%";
                            myboxCpuLabel.setText(myboxInfo);
                            myboxCpuBar.setProgress(load);

                        }
                    });
                }
            }, 0, cpuMonitorInterval);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        UserConfig.setBoolean("MonitorCpu", v);
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
    protected void detailedDebug() {
        AppVariables.detailedDebugLogs = detailedDebugCheck.isSelected();
        UserConfig.setBoolean("DetailedDebugLogs", AppVariables.detailedDebugLogs);
    }

    @FXML
    protected void popError() {
        AppVariables.popErrorLogs = popErrorCheck.isSelected();
        UserConfig.setBoolean("PopErrorLogs", AppVariables.popErrorLogs);
    }

    @FXML
    protected void MyBoxProperties(ActionEvent event) {
        openStage(Fxmls.MyBoxPropertiesFxml);
    }

    @FXML
    protected void MyBoxLogs(ActionEvent event) {
        openStage(Fxmls.MyBoxLogsFxml);
    }

    @FXML
    protected void MyBoxTables(ActionEvent event) {
        openScene(Fxmls.MyBoxTablesFxml);
    }

    @FXML
    protected void runSystemCommand(ActionEvent event) {
        openScene(Fxmls.RunSystemCommandFxml);
    }

    @FXML
    protected void JConsole(ActionEvent event) {
        try {
            String cmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "jconsole";
            if (SystemTools.isWindows()) {
                cmd += ".exe";
            }
            new ProcessBuilder(cmd).start();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void openTTC2TTF(ActionEvent event) {
        openScene(Fxmls.FileTTC2TTFFxml);
    }

    // This is for developement to generate Icons automatically in different color style
    @FXML
    public void makeIcons() {
        openScene(Fxmls.MyBoxIconsFxml);
    }

    @FXML
    public void makeDocuments() {
        ShortcutsController.documents();
        FunctionsListController.documents();
    }

    @FXML
    public void autoTesting() {
        openScene(Fxmls.AutoTestingCasesFxml);
    }

    @FXML
    public void allTableNames() {
        TextPopController.loadText(DataInternalTable.allTableNames());
    }

    @FXML
    public void myBoxBaseVerificationList() {
        DataFileCSV data = Data2DExampleTools.MyBoxBaseVerificationList(
                parentController, Languages.isChinese());
        Data2DManufactureController.openDef(data);
    }

    @FXML
    protected void messageAuthor(ActionEvent event) {
        openStage(Fxmls.MessageAuthorFxml);
    }

    @FXML
    protected void DevTmp(ActionEvent event) {
        DevTmpController.open();
    }

}
