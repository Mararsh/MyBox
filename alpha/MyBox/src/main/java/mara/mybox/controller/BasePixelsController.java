package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public abstract class BasePixelsController extends BaseChildController {

    protected BaseImageController imageController;
    protected ImageEditorController editor;
    protected String operation, opInfo;
    protected Image handledImage;

    @FXML
    protected ControlSelectPixels scopeController;
    @FXML
    protected ControlColorSet bgColorController;

    protected void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
            }
            this.imageController = parent;
            if (imageController instanceof ImageEditorController) {
                editor = (ImageEditorController) imageController;
            }
            scopeController.setParameters(this);

            if (bgColorController != null) {
                bgColorController.init(this, baseName + "BackgroundColor", Color.DARKGREEN);
            }

            reset();
            initMore();

            scopeController.loadImage();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initMore() {
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return scopeController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    public void reset() {
        operation = null;
        opInfo = null;
        handledImage = null;
    }

    protected Image srcImage() {
        return scopeController.srcImage();
    }

    public ImageScope scope() {
        return scopeController.pickScopeValues();
    }

    public boolean scopeExclude() {
        return scopeController.scopeExcludeCheck.isSelected();
    }

    public boolean ignoreTransparent() {
        return scopeController.ignoreTransparentCheck.isSelected();
    }

    protected boolean checkOptions() {
        if (imageController == null || !imageController.isShowing()) {
            close();
            return false;
        }
        return srcImage() != null;
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
        reset();
        task = new SingletonCurrentTask<Void>(this) {
            private ImageScope scope;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    handledImage = handleImage(srcImage(), scope);
                    return handledImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage(operation, opInfo, scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
                afterHandle();
            }
        };
        start(task);
    }

    protected Image handleImage(Image inImage, ImageScope inScope) {
        return null;
    }

    protected void afterHandle() {

    }

    @FXML
    protected void demo() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {
            private Image demoImage;

            @Override
            protected boolean handle() {
                try {
                    demoImage = ScaleTools.demoImage(srcImage());
                    demoImage = handleImage(demoImage, scope());
                    return demoImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, demoImage);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void undoAction() {
        if (editor != null) {
            editor.undoAction();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        if (editor != null) {
            editor.recoverAction();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        imageController.saveAction();
    }

    /*
        static methods
     */
    public static BasePixelsController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            BasePixelsController controller = (BasePixelsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageBlackWhiteFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
