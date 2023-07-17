package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class MainMenuController extends MainMenuController_Help {

    @FXML
    protected Menu recentMenu;

    @Override
    public void initControls() {
        try {
            super.initControls();

            recentMenu.setOnShowing((Event e) -> {
                recentMenu.getItems().clear();
                recentMenu.getItems().addAll(VisitHistoryTools.getRecentMenu(this));
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
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
