package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.stage.Stage;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.MenuTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class MainMenuController extends MainMenuController_Development {

    @FXML
    protected Menu toolsMenu, recentMenu, helpMenu;

    @Override
    public void initControls() {
        try {
            super.initControls();

            toolsMenu.setOnShowing((Event e) -> {
                toolsMenu.getItems().clear();
                toolsMenu.getItems().addAll(MenuTools.toolsMenu(this, e));
            });

            recentMenu.setOnShowing((Event e) -> {
                recentMenu.getItems().clear();
                recentMenu.getItems().addAll(VisitHistoryTools.getRecentMenu(this, false));
            });

            helpMenu.setOnShowing((Event e) -> {
                helpMenu.getItems().clear();
                helpMenu.getItems().addAll(MenuTools.helpMenu(this, e));
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
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

    @Override
    public BaseController openStage(String newFxml) {
        return parentController.openStage(newFxml);
    }

    @Override
    public void cleanPane() {
        try {
            stopMemoryMonitorTimer();
            stopCpuMonitorTimer();
        } catch (Exception e) {
        }
        super.cleanPane();
    }
}
