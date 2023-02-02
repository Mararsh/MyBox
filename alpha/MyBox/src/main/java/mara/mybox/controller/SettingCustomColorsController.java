package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.fxml.WindowTools.refreshInterfaceAll;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-2-2
 * @License Apache License Version 2.0
 */
public class SettingCustomColorsController extends BaseChildController {

    @FXML
    protected ColorSet darkColorSetController, lightColorSetController;
    @FXML
    protected CheckBox useCheck;

    public void setParameters(BaseController parent) {
        try {
            parentController = parent;
            if (parent != null) {
                baseName = parent.baseName;
                getMyStage().setTitle(parent.getTitle());
            }
            getMyStage().centerOnScreen();

            darkColorSetController.init(this, "CustomizeColorDark", Color.web(AppVariables.CustomizeColorDark));
            lightColorSetController.init(this, "CustomizeColorLight", Color.web(AppVariables.CustomizeColorLight));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    String color = FxColorTools.color2rgba(darkColorSetController.color());
                    AppVariables.CustomizeColorDark = color;
                    UserConfig.setString("CustomizeColorDark", color);

                    color = FxColorTools.color2rgba(lightColorSetController.color());
                    AppVariables.CustomizeColorLight = color;
                    UserConfig.setString("CustomizeColorLight", color);

                    StyleTools.makeCustomizeIcons(task, true);

                    if (useCheck.isSelected()) {
                        StyleTools.setConfigStyleColor("customize");
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (AppVariables.ControlColor == StyleData.StyleColor.Customize) {
                    refreshInterfaceAll();
                } else {
                    parentController.refreshInterface();
                }
            }

        };
        start(task);
    }

    public static SettingCustomColorsController open(BaseController parent) {
        try {
            SettingCustomColorsController controller = (SettingCustomColorsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.SettingCustomColorsFxml, true);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
