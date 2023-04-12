package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Settings extends MyBoxController_Recent {

    @FXML
    protected void showSettingsMenu(MouseEvent event) {
        String lang = Languages.getLanguage();
        List<MenuItem> langItems = new ArrayList();
        ToggleGroup langGroup = new ToggleGroup();
        RadioMenuItem English = new RadioMenuItem("English");
        English.setToggleGroup(langGroup);
        English.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            Languages.setLanguage("en");
            loadScene(myFxml);
        });
        langItems.add(English);
        if ("en".equals(lang)) {
            isSettingValues = true;
            English.setSelected(true);
            isSettingValues = false;
        }
        RadioMenuItem Chinese = new RadioMenuItem("中文");
        Chinese.setToggleGroup(langGroup);
        Chinese.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            Languages.setLanguage("zh");
            loadScene(myFxml);
        });
        langItems.add(Chinese);
        if ("zh".equals(lang)) {
            isSettingValues = true;
            Chinese.setSelected(true);
            isSettingValues = false;
        }

        List<String> languages = Languages.userLanguages();
        if (languages != null && !languages.isEmpty()) {

            for (int i = 0; i < languages.size(); ++i) {
                final String name = languages.get(i);
                RadioMenuItem langItem = new RadioMenuItem(name);
                langItem.setToggleGroup(langGroup);
                langItem.setOnAction((ActionEvent event1) -> {
                    if (isSettingValues) {
                        return;
                    }
                    Languages.setLanguage(name);
                    loadScene(myFxml);
                });
                langItems.add(langItem);
                if (name.equals(lang)) {
                    isSettingValues = true;
                    langItem.setSelected(true);
                    isSettingValues = false;
                }
            }
        }

        CheckMenuItem derbyServer = new CheckMenuItem(Languages.message("DerbyServerMode"));
        derbyServer.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            derbyServer.setDisable(true);
            DerbyBase.mode = derbyServer.isSelected() ? "client" : "embedded";
            ConfigTools.writeConfigValue("DerbyMode", DerbyBase.mode);
            Platform.runLater(() -> {
                try {
                    String ret = DerbyBase.startDerby();
                    if (ret != null) {
                        popInformation(ret, 6000);
                        isSettingValues = true;
                        derbyServer.setSelected("client".equals(DerbyBase.mode));
                        isSettingValues = false;
                    } else {
                        popFailed();
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
                derbyServer.setDisable(false);
            });
        });
        isSettingValues = true;
        derbyServer.setSelected("client".equals(DerbyBase.mode));
        isSettingValues = false;

        MenuItem mybox = new MenuItem(Languages.message("MyBoxProperties"));
        mybox.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.MyBoxPropertiesFxml);
        });

        MenuItem settings = new MenuItem(Languages.message("SettingsDot"));
        settings.setOnAction((ActionEvent event1) -> {
            BaseController c = openStage(Fxmls.SettingsFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(langItems);
        items.addAll(Arrays.asList(derbyServer, mybox, new SeparatorMenuItem(), settings));

        popCenterMenu(settingsBox, items);

    }

}
