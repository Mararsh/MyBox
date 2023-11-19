package mara.mybox.controller;

import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageGray;
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
public class ImageGreyController extends BasePixelsController {

    public ImageGreyController() {
        baseTitle = message("Grey");
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            ImageGray imageGray = new ImageGray(inImage, inScope);
            imageGray.setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            operation = message("Grey");
            opInfo = null;
            return imageGray.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageGreyController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageGreyController controller = (ImageGreyController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageGreyFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
