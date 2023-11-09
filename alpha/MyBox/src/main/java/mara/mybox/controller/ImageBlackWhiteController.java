package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageBlackWhiteController extends ImageSelectScopeController {

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
                    threshold = binaryController.threshold();
                    scope = scope();
                    ImageBinary imageBinary = new ImageBinary(editor.imageView.getImage());
                    imageBinary.setScope(scope)
                            .setIntPara1(threshold)
                            .setIsDithering(binaryController.dither())
                            .setExcludeScope(excludeRadio.isSelected())
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    handledImage = imageBinary.operateFxImage();
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
                editor.updateImage("BlackOrWhite", message("Threshold") + ": " + threshold,
                        scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
    }

    @Override
    protected Image makeDemo(BufferedImage dbf, ImageScope scope) {
        try {
            ImageBinary imageBinary = new ImageBinary();
            imageBinary.setImage(dbf)
                    .setScope(scope)
                    .setIntPara1(-1)
                    .setIsDithering(true);
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
