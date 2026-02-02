package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageScope;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public abstract class BasePixelsController extends BaseImageEditController {

    protected ControlImageScope scopeHandler;

    @FXML
    protected ControlSelectPixels scopeController;

    @Override
    protected void initMore() {
        scopeHandler = scopeController.handleController;
        scopeHandler.setImageEditor(imageController);
    }

    @Override
    protected void loadImage() {
        if (imageController == null || !imageController.isShowing()) {
            close();
            return;
        }
        scopeHandler.loadImage(srcImage());
        updateStageTitle();
    }

    public ImageScope scope() {
        return scopeHandler.pickScopeValues();
    }

    public boolean excludeScope() {
        return scopeHandler.scopeExcludeCheck.isSelected();
    }

    public boolean skipTransparent() {
        return !scopeHandler.handleTransparentCheck.isSelected();
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
    public boolean menuAction(Event event) {
        if (scopeHandler.menuAction(event)) {
            return true;
        }
        return super.menuAction(event);
    }

    @FXML
    @Override
    public boolean popAction() {
        if (scopeHandler.popAction()) {
            return true;
        }
        return super.popAction();
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (super.handleKeyEvent(event)) {
            return true;
        }
        return scopeController.handleKeyEvent(event);
    }

}
