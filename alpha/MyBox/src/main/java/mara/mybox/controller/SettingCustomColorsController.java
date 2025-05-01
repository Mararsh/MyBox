package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-2-2
 * @License Apache License Version 2.0
 */
public class SettingCustomColorsController extends BaseChildController {

    @FXML
    protected ControlColorSet darkColorSetController, lightColorSetController;
    @FXML
    protected ImageView exampleView;
    @FXML
    protected CheckBox useCheck;

    public void setParameters(BaseController parent) {
        try {
            parentController = parent;
            if (parent != null) {
                baseName = parent.baseName;
                getMyStage().setTitle(parent.getTitle());
            }

            Color darkColor = Colors.customizeColorDark();
            darkColorSetController.init(this, baseName + "DarkColor", darkColor).setColor(darkColor);
            darkColorSetController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    updateView();
                }
            });

            Color lightColor = Colors.customizeColorLight();
            lightColorSetController.init(this, baseName + "LightColor", lightColor).setColor(lightColor);
            lightColorSetController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    updateView();
                }
            });

            updateView();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateView() {
        try {
            Image image = StyleTools.makeImage(null, "iconAdd.png",
                    darkColorSetController.color(),
                    lightColorSetController.color());
            if (image != null) {
                exampleView.setImage(image);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void defaultAction() {
        darkColorSetController.setColor(Color.web("0x8B008BFF"));
        lightColorSetController.setColor(Color.web("0xF8F8FFFF"));
        updateView();
    }

    @FXML
    @Override
    public void okAction() {
        String dark = FxColorTools.color2rgba(darkColorSetController.color());
        String light = FxColorTools.color2rgba(lightColorSetController.color());
        if (!dark.equalsIgnoreCase(UserConfig.getString("CustomizeColorDark", null))
                || !light.equalsIgnoreCase(UserConfig.getString("CustomizeColorLight", null))) {
            UserConfig.setString("CustomizeColorDark", dark);
            UserConfig.setString("CustomizeColorLight", light);
            FileDeleteTools.clearDir(null, new File(AppVariables.MyboxDataPath + "/buttons/customized/"));
        }
        if (useCheck.isSelected() || AppVariables.ControlColor == StyleData.StyleColor.Customize) {
            StyleTools.setConfigStyleColor(parentController, "customize");
        } else {
            parentController.refreshInterface();
        }
        closeStage();
    }

    public static SettingCustomColorsController open(BaseController parent) {
        try {
            SettingCustomColorsController controller = (SettingCustomColorsController) WindowTools.childStage(
                    parent, Fxmls.SettingCustomColorsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
