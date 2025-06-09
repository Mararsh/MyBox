package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-6-7
 * @License Apache License Version 2.0
 */
public class ColorsBlendController extends BaseController {

    protected ColorData colorA, colorB;

    @FXML
    protected ControlColorInput colorAController, colorBController;
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
            colorAController.setParameter(baseName + "_A", Color.YELLOW);
            colorAController.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            colorBController.setParameter(baseName + "_B", Color.SKYBLUE);
            colorBController.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            blendController.setParameters(this);
            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void goAction() {
        try {
            colorA = colorAController.colorData;
            if (colorA == null || colorA.getRgba() == null) {
                popError(message("SelectToHandle") + ": A");
                return;
            }
            colorB = colorBController.colorData;
            if (colorB == null || colorB.getRgba() == null) {
                popError(message("SelectToHandle") + ": B");
                return;
            }

            PixelsBlend blender = blendController.pickValues(-1f);
            ColorData blended = new ColorData(blender.blend(colorA.getColorValue(), colorB.getColorValue()));

            String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"400\" width=\"600\">\n"
                    + "    <circle cx=\"200\" cy=\"200\" fill=\"" + colorA.css() + "\" r=\"198\"/>\n"
                    + "    <circle cx=\"400\" cy=\"200\" fill=\"" + colorB.css()
                    + "\" r=\"198\"/>\n"
                    + "    <path d=\"M 299.50,372.34 A 199.00 199.00 0 0 0 300 28 A 199.00 199.00 0 0 0 300 372\" "
                    + "  fill=\"" + blended.css() + "\" />\n"
                    + "</svg>";
            viewController.drawSVG(svg);
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
        if (colorAController.thisPane.isFocused() || colorAController.thisPane.isFocusWithin()) {
            if (colorAController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (colorBController.thisPane.isFocused() || colorBController.thisPane.isFocusWithin()) {
            if (colorBController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (blendController.thisPane.isFocused() || blendController.thisPane.isFocusWithin()) {
            if (blendController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (viewController.thisPane.isFocused() || viewController.thisPane.isFocusWithin()) {
            if (viewController.keyEventsFilter(event)) {
                return true;
            }
        }
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
