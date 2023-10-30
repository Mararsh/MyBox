package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImageSelectScopeController extends BaseChildController {

    protected BaseImageController imageController;
    protected ImageEditorController editor;

    @FXML
    protected ControlImageScopeInput scopeController;
    @FXML
    protected ControlColorSet bgColorController;

    public ImageSelectScopeController() {
        baseTitle = message("SelectScope");
        TipsLabelKey = "ImageScopeTips";
    }

    protected void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
            }
            this.imageController = parent;
            if (imageController instanceof ImageEditorController) {
                editor = (ImageEditorController) imageController;
            }
            setControls();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setControls() {
        try {
            scopeController.setParameters(imageController);
            bgColorController.init(this, baseName + "BackgroundColor", Color.DARKGREEN);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image scopedImage;

            @Override
            protected boolean handle() {
                try {
                    scopedImage = scopeController.scopedImage(bgColorController.color());
                    return scopedImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImageViewerController.openImage(scopedImage);
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
    public static ImageSelectScopeController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSelectScopeController controller = (ImageSelectScopeController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageSelectScopeFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
