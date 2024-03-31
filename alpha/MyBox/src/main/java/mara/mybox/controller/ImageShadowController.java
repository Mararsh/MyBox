package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.PixelDemos;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageShadowController extends BaseImageEditController {

    protected int w, h;
    protected Color color;
    protected boolean blur;

    @FXML
    protected ControlImageShadow shadowController;

    public ImageShadowController() {
        baseTitle = message("Shadow");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Shadow");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        if (!shadowController.pickValues()) {
            return false;
        }
        if (shadowController.wPercenatge()) {
            w = (int) (srcImage().getWidth() * shadowController.wPer / 100);
        } else {
            w = shadowController.w;
        }
        if (shadowController.hPercenatge()) {
            h = (int) (srcImage().getWidth() * shadowController.hPer / 100);
        } else {
            h = shadowController.h;
        }
        blur = shadowController.blur();
        color = shadowController.color();
        opInfo = message("HorizontalOffset") + ":" + w + " "
                + message("VerticalOffset") + ":" + h + " "
                + message("Color") + ":" + color + " "
                + message("Blur") + ":" + blur;
        return true;
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        handledImage = FxImageTools.addShadow(currentTask, srcImage(), w, h, color, blur);
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        PixelDemos.shadow(currentTask, files,
                SwingFXUtils.fromFXImage(demoImage, null),
                shadowController.awtColor(), srcFile());
    }


    /*
        static methods
     */
    public static ImageShadowController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShadowController controller = (ImageShadowController) WindowTools.branchStage(
                    parent, Fxmls.ImageShadowFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
