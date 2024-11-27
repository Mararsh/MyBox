package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.db.data.InfoNode;
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
        DataTreeController controller = DataTreeController.imageScope(this, false);
//        ((ControlDataImageScope) controller.nodeController.dataController).loadScope(scope);  ###########
    }

    @FXML
    @Override
    public void selectAction() {
        InfoTreeNodeSelectController controller = InfoTreeNodeSelectController.open(this, InfoNode.ImageScope);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                InfoNode node = controller.selected();
                if (node == null) {
                    return;
                }
                scope = ImageScopeTools.fromXML(null, myController, node.getInfo());
                applyScope(scope);
                controller.close();
            }
        });
    }

}
