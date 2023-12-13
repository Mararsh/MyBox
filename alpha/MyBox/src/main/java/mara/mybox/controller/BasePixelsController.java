package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import mara.mybox.bufferedimage.ImageScope;

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
        imageView = scopeController.imageView;
    }

    @Override
    public void loadImage(Image inImage) {
        scopeController.loadImage(inImage);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return scopeController.keyEventsFilter(event);
        } else {
            return true;
        }
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
    protected void handleImage() {
        scope = scope();
        handledImage = handleImage(srcImage(), scope);
    }

    protected Image handleImage(Image inImage, ImageScope inScope) {
        return null;
    }

}
