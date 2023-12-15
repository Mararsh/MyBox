package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageGlassController extends BasePixelsController {

    protected ImageMosaic mosaic;

    @FXML
    protected ControlImageMosaic mosaicController;

    public ImageGlassController() {
        baseTitle = message("FrostedGlass");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("FrostedGlass");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        mosaic = mosaicController.pickValues(true);
        return mosaic != null;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            mosaic.setImage(inImage).setScope(inScope);
            mosaic.init()
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            opInfo = message("Intensity") + ": " + mosaicController.intensity;
            return mosaic.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageGlassController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageGlassController controller = (ImageGlassController) WindowTools.branchStage(
                    parent, Fxmls.ImageGlassFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
