package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
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

    protected ControlSelectPixels scopeController;

    @FXML
    protected ControlImageView selectedController, sourceController, maskController;
    @FXML
    protected Tab selectedTab, sourceTab, maskTab;
    @FXML
    protected VBox selectedBox, pixelsBox, sourceBox;

    public ImageScopeViewsController() {
        baseTitle = message("Scope");
    }

    protected void setParameters(ControlSelectPixels parent) {
        try {
            scopeController = parent;

            scopeController.showNotify.addListener(new ChangeListener<Boolean>() {
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

    public Image srcImage() {
        return scopeController.srcImage();
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
        maskController.loadImage(scopeController.imageView.getImage());
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
                    selectedScope = scopeController.scopeImage(this);
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
        start(task, selectedBox);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == sourceTab) {
            if (sourceController.keyEventsFilter(event)) {
                return true;
            }
        } else if (tab == selectedTab) {
            if (selectedController.keyEventsFilter(event)) {
                return true;
            }
        } else if (tab == maskTab) {
            if (maskController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    /*
        static methods
     */
    public static ImageScopeViewsController open(ControlSelectPixels parent) {
        try {
            if (parent == null || !parent.isShowing()) {
                return null;
            }
            ImageScopeViewsController controller = (ImageScopeViewsController) WindowTools.branchStage(
                    parent, Fxmls.ImageScopeViewsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
