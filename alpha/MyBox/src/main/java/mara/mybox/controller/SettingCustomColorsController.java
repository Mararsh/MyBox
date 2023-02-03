package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.fxml.WindowTools.refreshInterfaceAll;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
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
    protected ColorSet darkColorSetController, lightColorSetController;
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
            getMyStage().centerOnScreen();

            darkColor = Colors.customizeColorDark();
            darkColorSetController.init(this, "CustomizeColorDark", darkColor);
            darkColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    darkColor = (Color) nv;
                    updateView();
                }
            });

            lightColor = Colors.customizeColorLight();
            lightColorSetController.init(this, "CustomizeColorLight", lightColor);
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
            BufferedImage image = StyleTools.makeIcon(StyleData.StyleColor.Customize, "iconAdd.png");
            if (image == null) {
                return;
            }
            exampleView.setImage(SwingFXUtils.toFXImage(image, null));
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
                    UserConfig.setString("CustomizeColorDark", FxColorTools.color2rgba(darkColorSetController.color()));
                    UserConfig.setString("CustomizeColorLight", FxColorTools.color2rgba(lightColorSetController.color()));

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
