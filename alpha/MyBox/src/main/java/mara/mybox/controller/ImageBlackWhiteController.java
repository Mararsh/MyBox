package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageBinary;
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
public class ImageBlackWhiteController extends BaseScopeController {

    protected int threshold;

    @FXML
    protected ControlImageBinary binaryController;

    public ImageBlackWhiteController() {
        baseTitle = message("BlackOrWhite");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            binaryController.setParameters(editor.imageView);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            operation = message("BlackOrWhite");
            opInfo = message("Threshold") + ": " + threshold;
            threshold = binaryController.threshold();
            ImageBinary imageBinary = new ImageBinary(inImage);
            imageBinary.setScope(inScope)
                    .setIntPara1(threshold)
                    .setIsDithering(binaryController.dither())
                    .setExcludeScope(excludeRadio.isSelected())
                    .setSkipTransparent(ignoreTransparentCheck.isSelected());
            return imageBinary.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageBlackWhiteController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageBlackWhiteController controller = (ImageBlackWhiteController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageBlackWhiteFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
