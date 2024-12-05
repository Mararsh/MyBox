package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 */
public class ControlSelectPixels extends BaseImageScope {

    protected BaseImageController imageController;

    public void setParameters(BaseImageController parent) {
        try {
            this.parentController = parent;
            imageController = parent;

            toolbar.getChildren().removeAll(selectFileButton, fileMenuButton);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public Image srcImage() {
        if (imageController != null) {
            image = imageController.imageView.getImage();
            sourceFile = imageController.sourceFile;
        }
        return image;
    }

    @FXML
    public void saveScope() {
        ControlDataImageScope.open(this, scope);
    }

    @FXML
    @Override
    public void selectAction() {
        DataSelectImageScopeController.open(this);
    }

}
