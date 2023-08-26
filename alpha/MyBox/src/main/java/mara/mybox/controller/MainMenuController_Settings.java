package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.WindowTools.refreshInterfaceAll;
import static mara.mybox.fxml.WindowTools.reloadAll;
import static mara.mybox.fxml.WindowTools.styleAll;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Settings extends MainMenuController_Media {

    @FXML
    protected Menu settingsMenu, languagesMenu;
    @FXML
    protected ToggleGroup langGroup;
    @FXML
    protected CheckMenuItem shortcutsCanNotOmitCheck, loseFocusCommitDataCheck, clearExpiredDataCheck,
            closeCurrentCheck, recordWindowsSizeLocationCheck, popRecentCheck,
            popColorSetCheck, controlPanesCheck, controlTextCheck;
    @FXML
    protected RadioMenuItem chineseMenuItem, englishMenuItem,
            font12MenuItem, font15MenuItem, font17MenuItem,
            normalIconMenuItem, bigIconMenuItem, smallIconMenuItem,
            pinkMenuItem, redMenuItem, blueMenuItem, lightBlueMenuItem, orangeMenuItem,
            greenMenuItem, colorCustomizeMenuItem;
    @FXML
    protected Rectangle colorCustomizeRect;
    @FXML
    protected ImageView smallIconView, normalIconView, bigIconView;

    @Override
    public void initControls() {
        try {
            super.initControls();

            settingsMenu.setOnShowing((Event e) -> {
                checkSettings();
            });
            checkSettings();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            checkSettings();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkSettings() {
        checkLanguage();
        checkFontSize();
        checkIconSize();
        controlTextCheck.setSelected(AppVariables.controlDisplayText);
        closeCurrentCheck.setSelected(AppVariables.closeCurrentWhenOpenTool);
        recordWindowsSizeLocationCheck.setSelected(AppVariables.recordWindowsSizeLocation);
        popRecentCheck.setSelected(AppVariables.fileRecentNumber > 0);
        popColorSetCheck.setSelected(UserConfig.getBoolean("PopColorSetWhenMouseHovering", true));
        shortcutsCanNotOmitCheck.setSelected(AppVariables.ShortcutsCanNotOmitCtrlAlt);
        loseFocusCommitDataCheck.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);
        clearExpiredDataCheck.setSelected(UserConfig.getBoolean("ClearExpiredDataBeforeExit", true));
        controlPanesCheck.setSelected(UserConfig.getBoolean("MousePassControlPanes", true));
        checkControlColor();
    }

    protected void checkLanguage() {
        Languages.refreshBundle();
        List<MenuItem> items = new ArrayList();
        List<String> languages = Languages.userLanguages();
        if (languages != null && !languages.isEmpty()) {
            String lang = Languages.getLangName();
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
                items.add(langItem);
                if (name.equals(lang)) {
                    isSettingValues = true;
                    langItem.setSelected(true);
                    isSettingValues = false;
                }
            }
        }
        languagesMenu.getItems().clear();
        if (!items.isEmpty()) {
            languagesMenu.getItems().setAll(items);
        }
        languagesMenu.getItems().add(chineseMenuItem);
        languagesMenu.getItems().add(englishMenuItem);

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
            case Green:
                greenMenuItem.setSelected(true);
                break;
            case Customize:
                colorCustomizeMenuItem.setSelected(true);
                break;
        }
        colorCustomizeRect.setFill(Colors.customizeColorDark());
        Image image = StyleTools.getIconImage("iconExamples.png");
        smallIconView.setImage(image);
        normalIconView.setImage(image);
        bigIconView.setImage(image);
    }

    @FXML
    protected void settingsAction(ActionEvent event) {
        BaseController c = openStage(Fxmls.SettingsFxml);
        c.setParentController(parentController);
        c.setParentFxml(parentFxml);
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        Languages.setLanguage("zh");
        reloadAll();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        Languages.setLanguage("en");
        reloadAll();
    }

    @FXML
    protected void openManageLanguages(ActionEvent event) {
        loadScene(Fxmls.MyBoxLanguagesFxml);
    }

    @FXML
    protected void setFont12(ActionEvent event) {
        UserConfig.setSceneFontSize(12);
        refreshInterfaceAll();
    }

    @FXML
    protected void setFont15(ActionEvent event) {
        UserConfig.setSceneFontSize(15);
        refreshInterfaceAll();
    }

    @FXML
    protected void setFont17(ActionEvent event) {
        UserConfig.setSceneFontSize(17);
        refreshInterfaceAll();
    }

    @FXML
    protected void normalIcon(ActionEvent event) {
        UserConfig.setIconSize(20);
        refreshInterfaceAll();
    }

    @FXML
    protected void bigIcon(ActionEvent event) {
        UserConfig.setIconSize(30);
        refreshInterfaceAll();
    }

    @FXML
    protected void smallIcon(ActionEvent event) {
        UserConfig.setIconSize(15);
        refreshInterfaceAll();
    }

    @FXML
    protected void setDefaultColor(ActionEvent event) {
        setRed(event);
    }

    @FXML
    protected void setPink(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "pink");
    }

    @FXML
    protected void setRed(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "red");
    }

    @FXML
    protected void setBlue(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "blue");
    }

    @FXML
    protected void setLightBlue(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "lightBlue");
    }

    @FXML
    protected void setOrange(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "orange");
    }

    @FXML
    protected void setGeen(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "green");
    }

    @FXML
    protected void setColorCustomize(ActionEvent event) {
        StyleTools.setConfigStyleColor(parentController, "customize");
    }

    @FXML
    protected void inputColors(ActionEvent event) {
        SettingCustomColorsController.open(this);
    }

    @FXML
    protected void setControlDisplayText(ActionEvent event) {
        AppVariables.controlDisplayText = controlTextCheck.isSelected();
        UserConfig.setBoolean("ControlDisplayText", controlTextCheck.isSelected());
        refreshInterfaceAll();
    }

    @FXML
    protected void closeCurrentAction() {
        UserConfig.setBoolean("CloseCurrentWhenOpenTool", closeCurrentCheck.isSelected());
        AppVariables.closeCurrentWhenOpenTool = closeCurrentCheck.isSelected();
    }

    @FXML
    protected void RecordWindowsSizeLocationAction() {
        UserConfig.setBoolean("RecordWindowsSizeLocation", recordWindowsSizeLocationCheck.isSelected());
        AppVariables.recordWindowsSizeLocation = recordWindowsSizeLocationCheck.isSelected();
    }

    @FXML
    protected void popRecentAction() {
        if (popRecentCheck.isSelected()) {
            AppVariables.fileRecentNumber = 15;
        } else {
            AppVariables.fileRecentNumber = 0;
        }
        UserConfig.setInt("FileRecentNumber", AppVariables.fileRecentNumber);
    }

    @FXML
    protected void popColorSetAction() {
        UserConfig.setBoolean("PopColorSetWhenMouseHovering", popColorSetCheck.isSelected());
    }

    @FXML
    protected void shortcutsCanNotOmitCtrlAlt() {
        AppVariables.ShortcutsCanNotOmitCtrlAlt = shortcutsCanNotOmitCheck.isSelected();
        UserConfig.setBoolean("ShortcutsCanNotOmitCtrlAlt", AppVariables.ShortcutsCanNotOmitCtrlAlt);
    }

    @FXML
    public void loseFocusCommitData(ActionEvent event) {
        AppVariables.lostFocusCommitData(loseFocusCommitDataCheck.isSelected());
    }

    @FXML
    protected void clearExpiredDataBeforeExit() {
        UserConfig.setBoolean("ClearExpiredDataBeforeExit", clearExpiredDataCheck.isSelected());
    }

    @FXML
    protected void controlPanesAction() {
        UserConfig.setBoolean("MousePassControlPanes", controlPanesCheck.isSelected());
    }

    public void setStyle(String style) {
        try {
            UserConfig.setString("InterfaceStyle", style);
            styleAll(style);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editConfigFile(ActionEvent event) {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        controller.sourceFileChanged(AppVariables.MyboxConfigFile);
        controller.popInformation(Languages.message("TakeEffectWhenReboot"));
    }

    @FXML
    public void clearSettings(ActionEvent event) {
        parentController.clearUserSettings();
    }

    @FXML
    public void clearExpiredData(ActionEvent event) {
        parentController.clearExpiredData();
    }

}
