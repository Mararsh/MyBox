package mara.mybox.controller;

import javafx.fxml.FXML;
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
public class ImageSelectScopeController extends BaseImageScopeController {

    public ImageSelectScopeController() {
        baseTitle = message("SelectScope");
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
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    handledImage = scopeController.selectedScope(
                            bgColorController.awtColor(), true);
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImageViewerController.openImage(handledImage);
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
