package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.MenuTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.SystemConfig;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    @FXML
    protected Label titleLabel;
    @FXML
    protected VBox menuBox, imageBox, documentBox, fileBox, recentBox, networkBox, dataBox,
            settingsBox, aboutBox, mediaBox;

    public MyBoxController() {
        baseTitle = message("AppTitle") + " v" + AppValues.AppVersion;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            titleLabel.setText(baseTitle);
            titleLabel.requestFocus();

//            Languages.checkStatus();
//            if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
//                bottomLabel.setText(MessageFormat.format(message("AlarmClocksRunning"), scheduledTasks.size()));
//            }
            if (DerbyBase.isStarted() && !SystemConfig.getBoolean("MyBoxWarningDisplayed", false)) {
                alertInformation(message("MyBoxWarning"));
                SystemConfig.setBoolean("MyBoxWarningDisplayed", true);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void popDocumentMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showDocumentMenu(event);
        }
    }

    @FXML
    protected void showDocumentMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("Document"));
        items.addAll(MenuTools.documentToolsMenu(this, event));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(documentBox, items);
    }

    @FXML
    public void popImageMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showImageMenu(event);
        }
    }

    @FXML
    protected void showImageMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("Image"));
        items.addAll(MenuTools.imageToolsMenu(this, event));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(imageBox, items);
    }

    @FXML
    public void popFileMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showFileMenu(event);
        }
    }

    @FXML
    protected void showFileMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("File"));
        items.addAll(MenuTools.fileToolsMenu(this, event));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(fileBox, items);
    }

    @FXML
    public void popNetworkMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showNetworkMenu(event);
        }
    }

    @FXML
    protected void showNetworkMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("Network"));
        items.addAll(MenuTools.networkToolsMenu(this, event));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(networkBox, items);
    }

    @FXML
    public void popDataMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showDataMenu(event);
        }
    }

    @FXML
    protected void showDataMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("Data"));
        items.addAll(MenuTools.dataToolsMenu(this, event));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(dataBox, items);
    }

    @FXML
    public void popMediaMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showMediaMenu(event);
        }
    }

    @FXML
    protected void showMediaMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("Media"));
        items.addAll(MenuTools.mediaToolsMenu(this, event));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(mediaBox, items);
    }

    @FXML
    public void popRecentMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showRecentMenu(event);
        }
    }

    @FXML
    protected void showRecentMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("RecentAccessed"));
        items.addAll(VisitHistoryTools.getRecentMenu(this, true));
        items.add(new SeparatorMenuItem());
        items.add(MenuTools.popCheckMenu(event, "MyBoxHome"));
        popCenterMenu(recentBox, items);
    }

    @FXML
    public void popSettingsMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showSettingsMenu(event);
        }
    }

    @FXML
    protected void showSettingsMenu(Event event) {
        String lang = Languages.getLangName();
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
                    MyBoxLog.debug(e);
                }
                derbyServer.setDisable(false);
            });
            Platform.requestNextPulse();
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

        List<MenuItem> items = MenuTools.initMenu(message("Settings"));
        items.addAll(langItems);
        items.addAll(Arrays.asList(new SeparatorMenuItem(), derbyServer, mybox,
                new SeparatorMenuItem(), settings));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(settingsBox, items);

    }

    @FXML
    public void popAboutMenu(Event event) {
        if (MenuTools.isPopMenu("MyBoxHome")) {
            showAboutMenu(event);
        }
    }

    @FXML
    protected void showAboutMenu(Event event) {
        List<MenuItem> items = MenuTools.initMenu(message("Help"));
        items.addAll(MenuTools.helpMenu(this, event));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(aboutBox, items);

    }

    @FXML
    protected void hideMenu(MouseEvent event) {
        if (popMenu != null) {
            popMenu.hide();
            popMenu = null;
        }
    }

}
