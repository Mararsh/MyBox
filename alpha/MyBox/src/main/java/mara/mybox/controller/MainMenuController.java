package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppVariables;

import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class MainMenuController extends MainMenuController_Help {

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
                recentMenu.getItems().addAll(VisitHistoryTools.getRecentMenu(this));
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSettings() {
        checkLanguage();
        checkFontSize();
        checkIconSize();
        monitorMemroyCheck.setSelected(UserConfig.getUserConfigBoolean("MonitorMemroy", false));
        monitorCpuCheck.setSelected(UserConfig.getUserConfigBoolean("MonitorCpu", false));
        controlTextCheck.setSelected(AppVariables.controlDisplayText);
        hidpiIconsCheck.setSelected(AppVariables.hidpiIcons);
        newWindowCheck.setSelected(AppVariables.openStageInNewWindow);
        restoreStagesSizeCheck.setSelected(AppVariables.restoreStagesSize);
        popRecentCheck.setSelected(AppVariables.fileRecentNumber > 0);
        popColorSetCheck.setSelected(UserConfig.getUserConfigBoolean("PopColorSetWhenMousePassing", true));
        controlPanesCheck.setSelected(UserConfig.getUserConfigBoolean("MousePassControlPanes", true));
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
        List<String> languages = Languages.userLanguages();
        if (languages != null && !languages.isEmpty()) {
            String lang = Languages.getLanguage();
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
                        Languages.setLanguage(name);
                        parentController.reload();
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
        if (AppVariables.currentBundle == Languages.BundleZhCN) {
            chineseMenuItem.setSelected(true);
        } else if (AppVariables.currentBundle == Languages.BundleEn) {
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
            case DarkGreen:
                darkGreenMenuItem.setSelected(true);
                break;
        }
    }

    @Override
    public boolean leavingScene() {
        stopMemoryMonitorTimer();
        stopCpuMonitorTimer();
        return super.leavingScene();
    }
}
