package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageBlendColorController extends BasePixelsController {

    protected PixelsBlend blend;

    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected ControlImagesBlend blendController;

    public ImageBlendColorController() {
        baseTitle = message("BlendColor");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            colorController.init(this, baseName + "NewColor", Color.PINK);

            blendController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            close();
            return false;
        }
        blend = blendController.pickValues();
        return blend != null;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            operation = message("BlendColor");
            opInfo = colorController.css();
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    editor.imageView.getImage(), inScope,
                    PixelsOperation.OperationType.Blend)
                    .setColorPara1(colorController.awtColor())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            ((PixelsOperationFactory.BlendColor) pixelsOperation)
                    .setBlender(blend);
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }


    /*
        static methods
     */
    public static ImageBlendColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageBlendColorController controller = (ImageBlendColorController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageBlendColorFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
