package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageBlendColorController extends BaseImageScopeController {

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
            ignoreTransparentCheck = blendController.ignoreTransparentCheck;

            super.initMore();

            colorController.init(this, baseName + "NewColor", Color.PINK);

            blendController.setParameters(this, editor.imageView);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private ImageScope scope;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                            editor.imageView.getImage(),
                            scope,
                            PixelsOperation.OperationType.Blend)
                            .setColorPara1(colorController.awtColor())
                            .setExcludeScope(excludeRadio.isSelected())
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    ((PixelsOperationFactory.BlendColor) pixelsOperation).setBlender(blendController.blender());
                    handledImage = pixelsOperation.operateFxImage();
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage("BlendColor", null, scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
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
