package mara.mybox.controller;

import java.awt.Toolkit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.StyleTools;
import static mara.mybox.fxml.WindowTools.refreshInterfaceAll;
import static mara.mybox.fxml.WindowTools.reloadAll;
import static mara.mybox.fxml.WindowTools.styleAll;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

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
        StyleTools.setConfigStyleColor("red");
        refreshInterfaceAll();
    }

    @FXML
    protected void setPink(ActionEvent event) {
        StyleTools.setConfigStyleColor("pink");
        refreshInterfaceAll();
    }

    @FXML
    protected void setRed(ActionEvent event) {
        StyleTools.setConfigStyleColor("red");
        refreshInterfaceAll();
    }

    @FXML
    protected void setBlue(ActionEvent event) {
        StyleTools.setConfigStyleColor("blue");
        refreshInterfaceAll();
    }

    @FXML
    protected void setLightBlue(ActionEvent event) {
        StyleTools.setConfigStyleColor("lightBlue");
        refreshInterfaceAll();
    }

    @FXML
    protected void setOrange(ActionEvent event) {
        StyleTools.setConfigStyleColor("orange");
        refreshInterfaceAll();
    }

    @FXML
    protected void setDarkGeen(ActionEvent event) {
        StyleTools.setConfigStyleColor("darkgreen");
        refreshInterfaceAll();
    }

    @FXML
    protected void setControlDisplayText(ActionEvent event) {
        AppVariables.controlDisplayText = controlTextCheck.isSelected();
        UserConfig.setBoolean("ControlDisplayText", controlTextCheck.isSelected());
        refreshInterfaceAll();
    }

    @FXML
    protected void hidpiIcons(ActionEvent event) {
        AppVariables.hidpiIcons = hidpiIconsCheck.isSelected();
        UserConfig.setBoolean("HidpiIcons", AppVariables.hidpiIcons);
        if (AppVariables.hidpiIcons) {
            if (Toolkit.getDefaultToolkit().getScreenResolution() <= 120) {
                parentController.alertInformation(Languages.message("HidpiIconsComments"));
            }
        } else {
            if (Toolkit.getDefaultToolkit().getScreenResolution() > 120) {
                parentController.alertInformation(Languages.message("HidpiIconsComments"));
            }
        }
        refreshInterfaceAll();
    }

    @FXML
    protected void newWindowAction() {
        UserConfig.setOpenStageInNewWindow(newWindowCheck.isSelected());
    }

    @FXML
    protected void restoreStagesSizeAction() {
        UserConfig.setRestoreStagesSize(restoreStagesSizeCheck.isSelected());
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
        UserConfig.setBoolean("PopColorSetWhenMousePassing", popColorSetCheck.isSelected());
    }

    @FXML
    protected void controlPanesAction() {
        UserConfig.setBoolean("MousePassControlPanes", controlPanesCheck.isSelected());
    }

    @FXML
    protected void setDefaultStyle(ActionEvent event) {
        setStyle(AppValues.DefaultStyle);
    }

    @FXML
    protected void setWhiteOnBlackStyle(ActionEvent event) {
        setStyle(AppValues.WhiteOnBlackStyle);
    }

    @FXML
    protected void setYellowOnBlackStyle(ActionEvent event) {
        setStyle(AppValues.YellowOnBlackStyle);
    }

    @FXML
    protected void setWhiteOnGreenStyle(ActionEvent event) {
        setStyle(AppValues.WhiteOnGreenStyle);
    }

    @FXML
    protected void setCaspianStyle(ActionEvent event) {
        setStyle(AppValues.caspianStyle);
    }

    @FXML
    protected void setGreenOnBlackStyle(ActionEvent event) {
        setStyle(AppValues.GreenOnBlackStyle);
    }

    @FXML
    protected void setPinkOnBlackStyle(ActionEvent event) {
        setStyle(AppValues.PinkOnBlackStyle);
    }

    @FXML
    protected void setBlackOnYellowStyle(ActionEvent event) {
        setStyle(AppValues.BlackOnYellowStyle);
    }

    @FXML
    protected void setWhiteOnPurpleStyle(ActionEvent event) {
        setStyle(AppValues.WhiteOnPurpleStyle);
    }

    @FXML
    protected void setWhiteOnBlueStyle(ActionEvent event) {
        setStyle(AppValues.WhiteOnBlueStyle);
    }

    public void setStyle(String style) {
        try {
            UserConfig.setString("InterfaceStyle", style);
            styleAll(style);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editConfigFile(ActionEvent event) {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        controller.openTextFile(AppVariables.MyboxConfigFile);
        controller.popInformation(Languages.message("TakeEffectWhenReboot"));
    }

    @FXML
    public void clearSettings(ActionEvent event) {
        parentController.clearUserSettings();
    }

}
