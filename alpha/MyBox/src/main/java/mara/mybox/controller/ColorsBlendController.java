package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-29
 * @License Apache License Version 2.0
 */
public class ColorsBlendController extends BaseController {

    protected ColorData colorData;

    @FXML
    protected ControlColorInput color1Controller, color2Controller;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected ControlSvgHtml viewController;

    public ColorsBlendController() {
        baseTitle = message("BlendColors");
    }

    @Override
    public void initControls() {
        try {
            color1Controller.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            color2Controller.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            blendController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void goAction() {
        try {
            colorData = color1Controller.colorData;
            if (colorData == null || colorData.getRgba() == null) {
                return;
            }

//            colorData = new ColorData(colorData.getRgba())
//                    .setvSeparator(separator).convert();
//            htmlController.displayHtml(colorData.html());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("ColorHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.colorHelps(true));
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
//        if (colorController.keyEventsFilter(event)) {
//            return true;
//        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static ColorsBlendController open() {
        try {
            ColorsBlendController controller = (ColorsBlendController) WindowTools.openStage(Fxmls.ColorsBlendFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
