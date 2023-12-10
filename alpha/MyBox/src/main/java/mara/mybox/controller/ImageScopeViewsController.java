package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-11-12
 * @License Apache License Version 2.0
 */
public class ImageScopeViewsController extends BaseChildController {

    protected BasePixelsController handler;

    @FXML
    protected BaseImageController selectedController, sourceController, maskController;
    @FXML
    protected Tab selectedTab, sourceTab, maskTab;
    @FXML
    protected VBox scopeBox;

    public ImageScopeViewsController() {
        baseTitle = message("Scope");
    }

    protected void setParameters(BasePixelsController parent) {
        try {
            handler = parent;

            handler.scopeController.showNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshAction();
                }
            });

            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        refreshSource();
        refreshMask();
        refreshScope();
    }

    public boolean checkValid() {
        if (handler == null || !handler.isShowing()
                || handler.imageController == null
                || !handler.imageController.isShowing()) {
            close();
            return false;
        }
        return true;
    }

    public Image srcImage() {
        if (!checkValid()) {
            return null;
        }
        return handler.srcImage();
    }

    @FXML
    public void refreshSource() {
        Image srcImage = srcImage();
        if (srcImage == null) {
            return;
        }
        sourceController.loadImage(srcImage);
    }

    @FXML
    public void refreshMask() {
        Image srcImage = srcImage();
        if (srcImage == null) {
            return;
        }
        maskController.loadImage(handler.scopeController.imageView.getImage());
    }

    @FXML
    public void refreshScope() {
        Image srcImage = srcImage();
        if (srcImage == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image selectedScope;

            @Override
            protected boolean handle() {
                try {
                    selectedScope = handler.scopeController.scopeImage(this);
                    return true;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                selectedController.loadImage(selectedScope);
            }

        };
        start(task, scopeBox);
    }

    /*
        static methods
     */
    public static ImageScopeViewsController open(BasePixelsController parent) {
        try {
            if (parent == null || !parent.isShowing()) {
                return null;
            }
            ImageScopeViewsController controller = (ImageScopeViewsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageScopeViewsFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
