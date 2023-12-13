package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
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

    protected Color darkColor, lightColor;

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

            darkColor = Colors.customizeColorDark();
            darkColorSetController.init(this, baseName + "DarkColor", darkColor).setColor(darkColor);
            darkColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    darkColor = (Color) nv;
                    updateView();
                }
            });

            lightColor = Colors.customizeColorLight();
            lightColorSetController.init(this, baseName + "LightColor", lightColor).setColor(lightColor);
            lightColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    lightColor = (Color) nv;
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
            Image image = StyleTools.makeImage(null, "iconAdd.png", darkColor, lightColor);
            if (image != null) {
                exampleView.setImage(image);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
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
            FileDeleteTools.clearDir(new File(AppVariables.MyboxDataPath + "/buttons/"));
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
