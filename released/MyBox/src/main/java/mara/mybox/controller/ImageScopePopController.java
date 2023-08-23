package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-23
 * @License Apache License Version 2.0
 */
public class ImageScopePopController extends ImagePopController {

    protected ImageManufactureScopeController scopeController;

    public void setScopeController(ImageManufactureScopeController scopeController) {
        try {
            this.scopeController = scopeController;

            setSourceImageView(scopeController, scopeController.imageView);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (scopeController == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image scopeImage;

            @Override
            protected boolean handle() {
                try {
                    PixelsOperation pixelsOperation = PixelsOperationFactory.create(scopeController.imageView.getImage(),
                            scopeController.scope, PixelsOperation.OperationType.PreOpacity, PixelsOperation.ColorActionType.Set);
                    pixelsOperation.setSkipTransparent(scopeController.ignoreTransparentCheck.isSelected());
                    pixelsOperation.setIntPara1(255 - (int) (scopeController.opacity * 255));
                    pixelsOperation.setExcludeScope(true);
                    scopeImage = pixelsOperation.operateFxImage();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(scopeImage);
            }
        };
        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            scopeController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static ImageScopePopController open(ImageManufactureScopeController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageScopePopController controller = (ImageScopePopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.ImageScopePopFxml, false);
            controller.setScopeController(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
