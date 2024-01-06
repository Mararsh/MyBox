package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public abstract class BasePixelsController extends BaseImageEditController {

    @FXML
    protected ControlSelectPixels scopeController;

    @Override
    protected void initMore() {
        scopeController.setParameters(this);
    }

    @Override
    protected void loadImage() {
        if (imageController == null || !imageController.isShowing()) {
            close();
            return;
        }
        scopeController.loadImage(srcImage());
        updateStageTitle();
    }

    public ImageScope scope() {
        return scopeController.pickScopeValues();
    }

    public boolean excludeScope() {
        return scopeController.scopeExcludeCheck.isSelected();
    }

    public boolean skipTransparent() {
        return !scopeController.handleTransparentCheck.isSelected();
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        scope = scope();
        handledImage = handleImage(currentTask, srcImage(), scope);
    }

    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        return null;
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (scopeController.menuAction()) {
            return true;
        }
        return super.menuAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (scopeController.popAction()) {
            return true;
        }
        return super.popAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return scopeController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

}
