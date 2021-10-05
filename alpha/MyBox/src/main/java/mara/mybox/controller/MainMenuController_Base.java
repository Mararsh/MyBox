package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.util.Timer;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Base extends BaseController {

    protected HBox memoryBox, cpuBox;
    protected Timer memoryMonitorTimer, cpuMonitorTimer;
    protected final int memoryMonitorInterval = 1000, cpuMonitorInterval = 1000;
    protected Runtime r;
    protected OperatingSystemMXBean osmxb;
    protected Label sysMemLabel, myboxMemLabel, sysCpuLabel, myboxCpuLabel;
    protected ProgressBar sysMemBar, myboxMemBar, sysCpuBar, myboxCpuBar;
    protected long mb;

    @FXML
    protected Pane mainMenuPane;
    @FXML
    protected MenuBar menuBar;
    @FXML
    protected ToggleGroup langGroup;
    @FXML
    protected RadioMenuItem chineseMenuItem, englishMenuItem,
            font12MenuItem, font15MenuItem, font17MenuItem,
            normalIconMenuItem, bigIconMenuItem, smallIconMenuItem,
            pinkMenuItem, redMenuItem, blueMenuItem, lightBlueMenuItem, orangeMenuItem, darkGreenMenuItem;
    @FXML
    protected CheckMenuItem monitorMemroyCheck, monitorCpuCheck,
            newWindowCheck, restoreStagesSizeCheck, popRecentCheck, popColorSetCheck, controlPanesCheck,
            controlTextCheck, hidpiIconsCheck;
    @FXML
    protected Menu settingsMenu, recentMenu, helpMenu;
    @FXML
    protected MenuItem languagesSperatorMenuItem, makeIconsItem;

    @Override
    public Stage getMyStage() {
        if (myStage == null) {
            if (mainMenuPane != null && mainMenuPane.getScene() != null) {
                myStage = (Stage) mainMenuPane.getScene().getWindow();
            }
        }
        return myStage;
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

}
